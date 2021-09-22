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

function getIntLiteralByExpression(ctx: ExpressionCtx): null | false | IntegerLiteralCtx {
  try {
    return (
      ctx.ternaryExpression[0].children.binaryExpression.length === 1 &&
      ctx.ternaryExpression[0].children.binaryExpression[0].children.unaryExpression[0].children.primary[0].children
        .primaryPrefix[0].children.literal[0].children.integerLiteral[0].children
    )
  } catch (err) {
    return null
  }
}

function getStringLiteralByExpression(ctx: ExpressionCtx): null | false | IToken {
  try {
    return (
      ctx.ternaryExpression[0].children.binaryExpression.length === 1 &&
      ctx.ternaryExpression[0].children.binaryExpression[0].children.unaryExpression[0].children.primary[0].children
        .primaryPrefix[0].children.literal[0].children.StringLiteral[0]
    )
  } catch (err) {
    return null
  }
}

class Visitor extends BaseJavaCstVisitorWithDefaults {
  public data: Array<{
    enumClass: string
    enums: Array<{ label: string; value: any }>
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
      let intLiteral
      let stringLiteral
      if (
        // 两个参数
        // eg. `USER(1, "用户")`
        ctx.argumentList[0].children?.expression?.length === 2 &&
        (intLiteral = getIntLiteralByExpression(ctx.argumentList[0].children.expression[0].children)) &&
        (stringLiteral = getStringLiteralByExpression(ctx.argumentList[0].children.expression[1].children))
      ) {
        let value: number
        let label: string = eval(stringLiteral.image)
        if (intLiteral.DecimalLiteral?.length) {
          value = intLiteral.DecimalLiteral[0].image
          this.state.enums.push({ label, value })
        }
      }
    }
  }
}

export function parseEnumCode(code: string) {
  const ast = parse(code)
  const visitor = new Visitor()
  visitor.visit(ast)
  return visitor.data
}
