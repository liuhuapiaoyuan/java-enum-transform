/**
 * Java enum transform
 * @author 余聪
 */
import * as globby from 'globby'
import * as fsExtra from 'fs-extra'
// @ts-ignore
import * as lazy from 'lazy-value'
import { pascalCase } from 'change-case'
import { CstNode, parse } from 'java-parser'
import { formatItemToTs, formatToTs, parseEnumCode } from './parse-enum-code'
import { Meta, parseFileMeta } from './parse-file'
import { resolveExpressionValue } from './resolve-values'

type FileData = { id: string; meta: Meta; ast: CstNode }

export interface JavaEnumTransformByDirOptions {
  deRef?: boolean
  pattern?: string | string[]
  ignore?: string[]
}

export async function javaEnumTransformByDir(
  dir: string,
  { ignore = [], pattern = '**/*.java', deRef = true }: JavaEnumTransformByDirOptions = {}
) {
  const filenames = await globby(pattern, {
    absolute: true,
    onlyFiles: true,
    ignore,
    cwd: dir
  })

  const tmpFileMap = new Map<string, FileData>()
  const fileMap = new Map<string, FileData>()
  await Promise.all(
    filenames.map(async (filename) => {
      const code = await fsExtra.readFile(filename, 'utf-8')
      const ast = parse(code)
      const meta = parseFileMeta(ast, { filename, classPath: dir })
      const id = meta.package ? meta.package.concat(meta.name).join('.') : null
      if (id) {
        tmpFileMap.set(id, {
          id,
          meta,
          ast
        })
      }
    })
  )

  const sortedKeys = Array.from(tmpFileMap.keys()).sort()
  sortedKeys.forEach((name) => {
    fileMap.set(name, tmpFileMap.get(name))
    tmpFileMap.delete(name)
  })

  const deRefOptions = {
    _visitMap: new Map(),
    getAstMapByPackage: (prefix) => {
      const map: any = {}
      const ids = [...fileMap.keys()]
      ids.forEach((id) => {
        const namespace = prefix + '.'
        if (id.startsWith(namespace)) {
          const name = id.slice(namespace.length)
          if (!name.includes('.') && fileMap.get(id)) {
            map[name] = fileMap.get(id).ast
          }
        }
      })
      return map
    },
    getAstById: (id) => {
      return fileMap.get(id)?.ast
    },
    map: new Map()
  }
  function deRefFn(list: ReturnType<typeof parseEnumCode>, file: FileData) {
    const resolveExpressionValueInner = (item, fieldName) => {
      if (!item) {
        return
      }
      try {
        return resolveExpressionValue(item, {
          ...deRefOptions,
          id: file.id,
          fieldName
        })
      } catch (e) {
        e.message = `解析 Enum 数据出错, id: ${file.id}, enum: ${fieldName}
  ${e.message}`
        throw e
      }
    }
    list.forEach((item) => {
      item.enums.forEach((enumItem) => {
        if (enumItem.label) {
          enumItem.label = resolveExpressionValueInner(enumItem.label, enumItem.name)
        }
        if (enumItem.value) {
          enumItem.value = resolveExpressionValueInner(enumItem.value, enumItem.name)
        }
      })
    })
  }

  const result = new Map<string, ReturnType<typeof parseEnumCode>>()
  await Promise.all(
    Array.from(fileMap.entries())
      .filter(([, file]) => file.meta.type === 'enum')
      .map(async ([pkgName, file]) => {
        const list = parseEnumCode(file.ast)
        if (deRef) {
          deRefFn(list, file)
        }
        result.set(pkgName, list)
      })
  )
  return result
}

export function resultToCode(map: Map<string, ReturnType<typeof parseEnumCode>>) {
  let mergedList = []
  for (const [, list] of map.entries()) {
    mergedList = mergedList.concat(list)
  }

  const cache = new Map()
  return formatToTs(mergedList, {
    transformEnumName: (name) => {
      if (!/enum$/i.test(name)) {
        name = pascalCase(name + ' enum')
      }
      if (!cache.has(name)) {
        cache.set(name, 0)
        return name
      }
      cache.set(name, cache.get(name) + 1)
      return `${name}${cache.get(name)}`
    }
  })
}
/**
 * 单个文件独立渲染
 * @param item
 * @returns
 */
export function resultItemToCode(item: ReturnType<typeof parseEnumCode>[0]) {
  return formatItemToTs(item, {
    transformEnumName: (name) => {
      if (!/enum$/i.test(name)) {
        name = pascalCase(name + ' enum')
      }
      return name
    }
  })
}
