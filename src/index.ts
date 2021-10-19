/**
 * Java enum transform
 * @author 余聪
 */
import * as globby from 'globby'
import * as fsExtra from 'fs-extra'
import { CstNode, parse } from 'java-parser'
import escape from 'escape-string-regexp'
import { delimiter, dirname } from 'path'
import { parseEnumCode } from './parse-enum-code'
import { Meta, parseFileMeta } from './parse-file'

export async function javaEnumTransformByDir(dir: string) {
  const filenames = await globby('**/*.java', {
    absolute: false,
    onlyFiles: true,
    cwd: dir
  })

  const fileMap = new Map<string, { meta: Meta; code: string; package: string; ast: CstNode }>()
  filenames.map(async (filename) => {
    const code = await fsExtra.readFile(filename, 'utf-8')
    const ast = parse(code)
    const meta = parseFileMeta(ast)

    const pkg = meta.package?.length
      ? meta.package.join('.')
      : dirname(filename).replace(new RegExp(escape(delimiter), 'g'), '.')

    if (pkg) {
      fileMap.set(pkg, {
        meta,
        code,
        ast,
        package: pkg
      })
    }
  })

  const result = new Map<string, ReturnType<typeof parseEnumCode>>()
  await Promise.all(
    Array.from(fileMap.values())
      .filter((file) => file.meta.type === 'enum')
      .map(async (file) => {
        const list = parseEnumCode(file.ast)
        result.set(file.package, list)
      })
  )
  return result
}
