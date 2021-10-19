import {
  CstNode,
  BaseJavaCstVisitorWithDefaults,
  BaseJavaCstVisitor,
  parse,
  ClassDeclarationCtx,
  EnumDeclarationCtx,
  EnumConstantCtx,
  ExpressionCtx,
  IntegerLiteralCstNode,
  IntegerLiteralCtx,
  IToken
} from 'java-parser'

class ExpressionValueVisitor extends BaseJavaCstVisitorWithDefaults {
  public data: {
    type: 'fqnOrRefType' | 'primitive'
    raw: string
    value: any
  }
  setData(raw: any, type = 'primitive') {
    let value = raw
    if (type === 'primitive') {
      value = eval(raw)
    }
    this.data = {
      value,
      type,
      raw
    } as any
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
    const firstImg = ctx.fqnOrRefTypePartFirst[0].children.fqnOrRefTypePartCommon[0].children.Identifier[0].image
    const imgs = ctx.fqnOrRefTypePartRest.map(
      (rest) => rest.children.fqnOrRefTypePartCommon[0].children.Identifier[0].image
    )
    const value = [firstImg].concat(imgs)
    this.data = {
      type: 'fqnOrRefType',
      value,
      raw: JSON.stringify(value.join('.'))
    }
  }
}

function getExpressionValue(cst: any) {
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
      let value
      let label
      if (
        // 两个参数
        // eg. `USER(1, "用户")`
        (ctx.argumentList[0].children?.expression?.length === 2 ||
          ctx.argumentList[0].children?.expression?.length === 1) &&
        (value = getExpressionValue(ctx.argumentList[0].children.expression[0]))
      ) {
        label =
          ctx.argumentList[0].children.expression[1] && getExpressionValue(ctx.argumentList[0].children.expression[1])
        this.state.enums.push({
          name: ctx.Identifier[0].image,
          label,
          value
        })
      }
    }
  }
}

export function parseEnumCode(codeOrAst: string | CstNode) {
  const ast = typeof codeOrAst === 'string' ? parse(codeOrAst) : codeOrAst
  const visitor = new Visitor()
  visitor.visit(ast)
  return visitor.data
}

export function formatToTs(data: Visitor['data']) {
  return data
    .map((x) => {
      const enumBodyString = x.enums
        .map((enumData) => {
          if (enumData.value.type === 'primitive') {
            return `  ${enumData.name} = ${enumData.value.raw},`
          }
          return ''
        })
        .filter(Boolean)
        .join('\n')
      if (!enumBodyString) {
        return ''
      }
      return `
export enum ${x.enumClass} {
${enumBodyString}
}
`.trim()
    })
    .filter(Boolean)
    .join('\n')
}
