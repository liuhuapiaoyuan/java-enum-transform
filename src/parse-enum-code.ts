import {
  CstNode,
  BaseJavaCstVisitorWithDefaults,
  parse,
  ClassDeclarationCtx,
  EnumDeclarationCtx,
  EnumConstantCtx,
  EnumConstantListCtx
} from 'java-parser'
import { pascalCase } from 'change-case'
import * as fs from 'fs'
function evalJava(code: string) {
  if (/^([-+]?)(\d+)L$/.test(code)) {
    code = RegExp.$1 + RegExp.$2
  }
  try {
    return eval(code)
  } catch (e) {
    e.message = `Eval 执行 ${JSON.stringify(code)} 错误：${e.message}`
    throw e
  }
}

// 解析各种形态的comment
function formatComment(rawComment: string) {
  // 识别 /** */
  let reg1 = new RegExp(/\/\*+[\/n\*\s]*([^\/n\*\s]*)[\/n\*\s]*\*+\//)
  // 识别 "//"
  let reg2 = new RegExp(/\/\/[\/n\*\s]*([^\/n\*\s]*)/)
  if (rawComment.trimStart().startsWith('//')) {
    return reg2.exec(rawComment)?.[1]
  }
  return reg1.exec(rawComment)?.[1]
}

class ExpressionValueVisitor extends BaseJavaCstVisitorWithDefaults {
  public data: {
    type: 'fqnOrRefType' | 'primitive'
    raw: string
    value: any
  }

  state = {
    prefix: null
  }

  setData(raw: any, type = 'primitive') {
    if (this.data) {
      this.data = null
      return
    }
    raw = this.state.prefix != null ? this.state.prefix + raw : raw
    let value = raw
    if (type === 'primitive') {
      value = evalJava(raw)
    }
    this.data = {
      value,
      type,
      raw
    } as any
    this.state.prefix = null
  }

  unaryExpression(ctx) {
    if (ctx.UnaryPrefixOperator) {
      this.state.prefix = ctx.UnaryPrefixOperator[0].image
    }
    return super.unaryExpression(ctx)
  }

  integerLiteral(ctx) {
    if (ctx.DecimalLiteral) {
      this.setData(ctx.DecimalLiteral[0].image)
    }
    if (ctx.HexLiteral) {
      this.setData(ctx.HexLiteral[0].image)
    }
    if (ctx.BinaryLiteral) {
      this.setData(ctx.BinaryLiteral[0].image)
    }
    if (ctx.OctalLiteral) {
      this.setData(ctx.OctalLiteral[0].image)
    }
    return super.integerLiteral(ctx)
  }
  floatingPointLiteral(ctx) {
    if (ctx.FloatLiteral) {
      this.setData(ctx.FloatLiteral[0].image)
    }
    if (ctx.HexFloatLiteral) {
      this.setData(ctx.HexFloatLiteral[0].image)
    }
    return super.floatingPointLiteral(ctx)
  }
  booleanLiteral(ctx) {
    if (ctx.True) {
      this.setData('true')
    }
    if (ctx.False) {
      this.setData('false')
    }
  }
  literal(ctx, param?: any): any {
    if (ctx.StringLiteral) {
      this.setData(ctx.StringLiteral[0].image)
    }
    if (ctx.Null) {
      this.setData('null')
    }
    if (ctx.CharLiteral) {
      this.setData(ctx.CharLiteral[0].image)
    }
    return super.literal(ctx, param)
  }

  fqnOrRefType(ctx) {
    if (this.data) {
      this.data = null
      return
    }

    const firstImg = ctx.fqnOrRefTypePartFirst[0].children.fqnOrRefTypePartCommon[0].children.Identifier[0].image
    const imgs = (ctx.fqnOrRefTypePartRest || []).map(
      (rest) => rest.children.fqnOrRefTypePartCommon[0].children.Identifier[0].image
    )
    const value = [firstImg].concat(imgs)
    this.data = {
      type: 'fqnOrRefType',
      value,
      raw: value.join('.')
    }
  }
}

export function getExpressionValue(cst: any) {
  const v = new ExpressionValueVisitor()
  v.visit(cst)
  return v.data
}

class Visitor extends BaseJavaCstVisitorWithDefaults {
  public data: Array<{
    enumClass: string
    enums: Array<{ name: string; label?: ExpressionValueVisitor['data']; value: ExpressionValueVisitor['data'] }>
  }> = []

  public state = {
    visiting: false,
    enumClass: null,
    enums: []
  }

  // 修复第一行的comment
  private firstEnumComment = ''

  startVisit(fn?) {
    this.state.visiting = true

    if (fn) {
      const res = fn()
      this.stopVisit()
      return res
    }
  }
  stopVisit() {
    if (this.state.visiting && this.state.enumClass) {
      this.data.push({
        enumClass: this.state.enumClass,
        enums: this.state.enums
      })
    }

    this.state.visiting = false
    this.state.enumClass = null
    this.state.enums = []
  }
  isVisiting() {
    return this.state.visiting
  }

  classDeclaration(ctx: ClassDeclarationCtx, param?: any): any {
    /**
     * 和服务端经过沟通，发现这种写法不太出现，暂时先不考虑支持嵌套内部类 enum
     * @example
     * public class RoleParent {
     *  class X {
     *   public enum Type {
     *     I_DEVELOPER(1, "开发者"),
     *     I_ADMIN(2,"管理"),
     *     I_USER(3,"用户");
     *   }
     *  }
     * }
     */
    if (this.isVisiting()) {
      return
    }

    if (ctx.classModifier?.[0]?.children?.Public) {
      if (ctx.enumDeclaration?.length) {
        return this.startVisit(() => super.classDeclaration(ctx, param))
      }
    }
  }

  enumDeclaration(ctx: EnumDeclarationCtx, param?: any): any {
    if (this.isVisiting()) {
      this.state.enumClass = ctx.typeIdentifier[0].children.Identifier[0].image
      // 此处修复第一行的comment
      this.firstEnumComment = ctx.enumBody?.[0].children?.enumConstantList?.[0].leadingComments?.[0].image
      return super.enumDeclaration(ctx, param)
    }
  }
  enumConstantList(ctx: EnumConstantListCtx, param?: any) {
    if (this.isVisiting()) {
      ctx.enumConstant.map((enumConstant, zIndex) => {
        let enumCtx = enumConstant.children
        let comment = zIndex == 0 ? this.firstEnumComment : enumConstant?.leadingComments?.[0]?.image
        comment = comment ? formatComment(comment) : undefined
        let commentLabel = comment
          ? {
              type: 'primitive',
              value: comment,
              raw: `"${comment}"`
            }
          : undefined
        let result = {
          name: enumCtx.Identifier[0].image,
          comment,
          value: {
            type: 'primitive',
            value: enumCtx.Identifier[0].image,
            raw: `"${enumCtx.Identifier[0].image}"`
          },
          label: comment ? commentLabel : undefined
        }
        const paramsLength = enumCtx.argumentList?.[0]?.children?.expression?.length ?? 0

        if (paramsLength == 0) {
          // 当参数长度是0的时候 也没有label 没有value
          result.label = {
            type: 'primitive',
            value: enumCtx.Identifier[0].image,
            raw: `"${enumCtx.Identifier[0].image}"`
          }
        } else if (paramsLength == 1) {
          // 参数长度为1的时候，第一个参数就是label(如果說這個label是英文，可以当value 并且有comment的情况共)
          let param1 = getExpressionValue(enumCtx.argumentList[0].children.expression[0])
          //let isValidName = /^[^\d\W\s]+\w*$/.test(param1.value)
          if (typeof commentLabel !== 'undefined' && param1.type == 'primitive') {
            result.value = param1
            result.label = param1
          } else {
            result.label = param1
          }
        } else if (paramsLength >= 2) {
          // 第一个参数是VALUE，第二个参数是LABEL
          result.value = getExpressionValue(enumCtx.argumentList[0].children.expression[0])
          result.label = getExpressionValue(enumCtx.argumentList[0].children.expression[1])
        }

        this.state.enums.push(result)
      })
    }

    return super.enumConstantList(ctx, param)
  }
}

export function parseEnumCode(codeOrAst: string | CstNode) {
  const ast = typeof codeOrAst === 'string' ? parse(codeOrAst) : codeOrAst
  const visitor = new Visitor()
  visitor.visit(ast)
  return visitor.data
}

type ArrayItem<T> = T extends Array<infer U> ? U : T

/**
 * 转换当个文件
 * @param x
 * @param param1
 * @returns
 */
export function formatItemToTs(
  x: ArrayItem<Visitor['data']>,
  { transformEnumName = (s) => s }: { transformEnumName?: (name: string) => string } = {}
) {
  const enumBodyString = x.enums
    .map((enumData) => {
      if (enumData.value.type === 'primitive') {
        const comment =
          enumData.label?.value != null
            ? `  /**
* ${enumData.label.value}
*/`
            : ''
        return `${comment ? `${comment}\n` : ''}  ${enumData.name} = ${enumData.value.raw},`
      }
      return ''
    })
    .filter(Boolean)
    .join('\n')
  if (!enumBodyString) {
    return ''
  }

  const name = transformEnumName(x.enumClass)
  const optionsString = x.enums
    .map((enumData) => {
      if (enumData.value.type === 'primitive' && enumData.label?.raw) {
        return `  { label: ${enumData.label?.raw}, value: ${name}[${JSON.stringify(enumData.name)}] },`
      }
      return ''
    })
    .filter(Boolean)
    .join('\n')
  const labelsString = x.enums
    .map((enumData) => {
      if (enumData.value.type === 'primitive' && enumData.label?.raw) {
        return `  {  ${enumData.value.raw}: ${enumData.name}  },`
      }
      return ''
    })
    .filter(Boolean)
    .join('\n')

  return `
export const enum ${name} {
${enumBodyString}
}
${
  optionsString
    ? `export const ${pascalCase(name + ' Options')} = [
${optionsString}
]
export const ${pascalCase(name + ' Labels')} = [
  ${labelsString}
]

`
    : ''
}
`.trim()
}

export function formatToTs(
  data: Visitor['data'],
  { transformEnumName = (s) => s }: { transformEnumName?: (name: string) => string } = {}
) {
  return data
    .map((x) => {
      const enumBodyString = x.enums
        .map((enumData) => {
          if (enumData.value.type === 'primitive') {
            const comment =
              enumData.label?.value != null
                ? `  /**
   * ${enumData.label.value}
   */`
                : ''
            return `${comment ? `${comment}\n` : ''}  ${enumData.name} = ${enumData.value.raw},`
          }
          return ''
        })
        .filter(Boolean)
        .join('\n')
      if (!enumBodyString) {
        return ''
      }

      const name = transformEnumName(x.enumClass)
      const optionsString = x.enums
        .map((enumData) => {
          if (enumData.value.type === 'primitive' && enumData.label?.raw) {
            return `  { label: ${enumData.label?.raw}, value: ${name}[${JSON.stringify(enumData.name)}] },`
          }
          return ''
        })
        .filter(Boolean)
        .join('\n')

      return `
export const enum ${name} {
${enumBodyString}
}
${
  optionsString
    ? `export const ${pascalCase(name + ' Options')} = [
${optionsString}
]`
    : ''
}
`.trim()
    })
    .filter(Boolean)
    .join('\n')
}
