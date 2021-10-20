import { CstNode, BaseJavaCstVisitorWithDefaults, parse } from 'java-parser'

export interface Import {
  static: boolean
  package: string[]
  name?: string
  member: string[]
  wildcard: boolean
}

class ParseImport extends BaseJavaCstVisitorWithDefaults {
  public data: Import[] = []

  importDeclaration(ctx) {
    const importData: Import = {} as any
    importData.static = !!ctx.Static
    importData.wildcard = !!ctx.Star
    const images = ctx.packageOrTypeName[0].children.Identifier.map((x) => x.image)
    const nameIndex = images.findIndex((x) => /^[A-Z]/.test(x))
    if (nameIndex >= 0) {
      importData.package = images.slice(0, nameIndex)
      importData.name = images[nameIndex]
      importData.member = images.slice(nameIndex + 1)
    } else {
      importData.package = images
      importData.member = []
    }
    this.data.push(importData)
    // return super.importDeclaration(ctx)
  }
}

export function parseImport(contentOrAst: string | CstNode) {
  const v = new ParseImport()
  v.visit(typeof contentOrAst === 'string' ? parse(contentOrAst) : contentOrAst)
  return v.data
}
