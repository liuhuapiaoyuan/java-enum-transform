import {
  CstNode,
  BaseJavaCstVisitorWithDefaults,
  parse,
  ClassDeclarationCtx,
  EnumDeclarationCtx,
  EnumConstantCtx,
  ExpressionCtx,
  IntegerLiteralCstNode,
  IntegerLiteralCtx,
  IToken
} from 'java-parser'
import { parseImport } from './parse-imports'
import { getExpressionValue } from './parse-enum-code'

/**
 * 解析
 * class class X {
 *   public static int a = 123;
 *   private boolean b = true;
 * }
 * // => { a: { type: 'primitive', value: 123, raw: '123' }, b: { type: 'primitive', value: true, raw: 'true' } }
 */
class ParseBodyMember extends BaseJavaCstVisitorWithDefaults {
  public data: Map<string, { value: ReturnType<typeof getExpressionValue>; static: boolean }> = new Map()

  protected state = {
    inClassMember: false
  }

  fieldDeclaration(ctx) {
    this.state.inClassMember = true
    super.classMemberDeclaration(ctx)
    this.state.inClassMember = false

    const isStatic = !!ctx.fieldModifier.find((x) => x.children.Static)
    const tmpCtx = ctx.variableDeclaratorList[0].children.variableDeclarator[0].children

    if (tmpCtx.variableInitializer && tmpCtx.variableDeclaratorId && tmpCtx.Equals) {
      const value = getExpressionValue(tmpCtx.variableInitializer[0].children.expression[0])
      if (value) {
        this.data.set(tmpCtx.variableDeclaratorId[0].children.Identifier[0].image, {
          value,
          static: isStatic
        })
      }
    }
  }
}

export function parseBodyMember(ast: any) {
  const v = new ParseBodyMember()
  v.visit(ast)
  return v.data
}

/**
 * 解决嵌套，动态计算的值
 * @param ast
 * @param getAstMapByPackage
 * @param getAstById
 * @param resolveExpressionValue
 * @param map
 */
export const resolveImportsValue = (
  ast: any,
  {
    getAstMapByPackage,
    getAstById,
    map = new Map(),
    resolveExpressionValue
  }: {
    map?: Map<any, ReturnType<typeof parseBodyMember>>
    getAstById: (id: string) => any
    getAstMapByPackage: (packageId: string) => Record<string, any>
    resolveExpressionValue?: (
      item: ReturnType<typeof getExpressionValue>,
      id: string,
      fieldName: string
    ) => ReturnType<typeof getExpressionValue>
  }
): null | Map<string, ParseBodyMember['data'] | { value: ReturnType<typeof getExpressionValue>; static: boolean }> => {
  if (!ast) {
    return null
  }
  const imports = parseImport(ast)
  const scopedMap = new Map<
    string,
    ParseBodyMember['data'] | { value: ReturnType<typeof getExpressionValue>; static: boolean }
  >()

  const getMemberMap = (id) => {
    const newAst = getAstById(id)
    if (!newAst) {
      return
    }

    if (map.get(id)) {
      return map.get(id)
    }
    const memberMap = parseBodyMember(newAst)
    if (resolveExpressionValue) {
      memberMap.forEach((item, key) => {
        item.value = resolveExpressionValue(item.value, id, key)
      })
    }
    map.set(id, memberMap)
    return memberMap as ParseBodyMember['data']
  }
  const getMemberMapByPackage = (id) => {
    const result = new Map()
    const newAsts = getAstMapByPackage(id)
    if (!newAsts) {
      return result
    }
    Object.keys(newAsts).forEach((name) => {
      const classid = [id].concat(name).join('.')
      result.set(name, getMemberMap(classid))
    })
    return result
  }

  imports.forEach((x) => {
    const id = x.package ? x.package.concat(x.name).filter(Boolean).join('.') : x.name
    if (!id) {
      return
    }

    if (x.wildcard) {
      if (x.name) {
        // import java.io.Util.*;
        const memberMap = getMemberMap(id)
        if (memberMap) {
          for (const [name, item] of memberMap.entries()) {
            if (item.static) {
              scopedMap.set(name, item)
            }
          }
        }
      } else {
        // import java.io.*;
        const packageMap = getMemberMapByPackage(id)
        if (packageMap) {
          for (const [name, item] of packageMap.entries()) {
            scopedMap.set(name, item)
          }
        }
      }
      return
    }

    if (x.name) {
      // import java.io.Util.a;
      if (x.member?.length) {
        if (x.member.length === 1) {
          const memberMap = getMemberMap(id)
          if (memberMap && memberMap.get(x.member[0])) {
            scopedMap.set(x.member[0], memberMap.get(x.member[0]))
          }
        }
      } else {
        // import java.io.Util;
        const memberMap = getMemberMap(id)
        if (memberMap) {
          scopedMap.set(x.name, memberMap)
        }
      }
    }
  })
  return scopedMap
}

export function resolveExpressionValue(
  item: ReturnType<typeof getExpressionValue>,
  {
    _visitMap = new Map(),
    id,
    fieldName,
    ...opts
  }: Omit<Parameters<typeof resolveImportsValue>[1], 'resolveExpressionValue'> & {
    _visitMap?: Map<any, any>
    id: string
    fieldName: string
  }
) {
  const options = {
    _visitMap,
    id,
    fieldName,
    ...opts
  }

  if (item.type === 'fqnOrRefType') {
    const fieldKey = `${id}#${fieldName}`
    const prevData = _visitMap.get(fieldKey)
    if (prevData?.status === 'post') {
      return prevData.data
    }

    if (prevData?.status === 'pre') {
      throw new Error(`存在环形依赖解析 ${fieldKey}`)
    }

    _visitMap.set(fieldKey, { status: 'pre' })
    const importsValue = resolveImportsValue(opts.getAstById(id), {
      ...opts,
      resolveExpressionValue: (item, id, key) => {
        return resolveExpressionValue(item, { ...options, fieldName: key, id })
      }
    })
    if (!importsValue) {
      _visitMap.set(fieldKey, { status: 'post', data: item })
      return item
    }

    if (item.value?.length) {
      let map: any = importsValue
      for (let i = 0; i < item.value.length; i++) {
        map = map.get(item.value[i])
        if (!(map instanceof Map)) {
          if (i === item.value.length - 1) {
            _visitMap.set(fieldKey, { status: 'post', data: map?.value ?? item })
            return map?.value ?? item
          }
          break
        }
      }
    }
  }
  return item
}
