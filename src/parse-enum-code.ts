import {
  CstNode,
  BaseJavaCstVisitorWithDefaults,
  parse,
  ClassDeclarationCtx,
  EnumDeclarationCtx,
  EnumConstantCtx
} from 'java-parser'
import { pascalCase } from 'change-case'

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
      return super.enumDeclaration(ctx, param)
    }
  }

  enumConstant(ctx: EnumConstantCtx, param?: any): any {
    if (this.isVisiting()) {
      if (!ctx.argumentList?.[0]) {
        return
      }
      const paramsLength = ctx.argumentList[0].children?.expression?.length
      let result = {
        name: ctx.Identifier[0].image,
        value: {
          type: 'primitive',
          value: ctx.Identifier[0].image,
          raw: `"${ctx.Identifier[0].image}"`
        },
        label: undefined
      }
      if (paramsLength == 0) {
        // 当参数长度是0的时候 也没有label 没有value
      } else if (paramsLength == 1) {
        // 参数长度为1的时候，第一个参数就是label
        result.label = getExpressionValue(ctx.argumentList[0].children.expression[0])
      } else if (paramsLength == 2) {
        // 第一个参数是VALUE，第二个参数是LABEL
        result.value = getExpressionValue(ctx.argumentList[0].children.expression[0])
        result.label = getExpressionValue(ctx.argumentList[0].children.expression[1])
      }
      this.state.enums.push(result)
    }
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
