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

class ParseImportsByCode extends BaseJavaCstVisitorWithDefaults {
  public data: Array<{
    type: 'static' | 'all'
    from: string
    flag: string
  }>
}

class ParseConsts extends BaseJavaCstVisitorWithDefaults {
  public data: Array<{
    name: string
    maybeValue:
      | {
          type: 'raw'
          value: any
        }
      | {
          type: 'ref'
          value: string[]
        }
  }>
}

export function parseImportsByCode(code: string) {
  const v = new ParseImportsByCode()
  v.visit(parse(code))
  return v.data
}

export function parseConstsByCode(code: string) {
  const v = new ParseConsts()
  v.visit(parse(code))
  return v.data
}

export function parseByCode(code: string) {
  return {
    imports: parseImportsByCode(code),
    consts: parseConstsByCode(code)
  }
}

// export async function parseConsts(path: string) {
//   const filenames = await globby(['**/*.java'], { cwd: nps.resolve(path), absolute: false, onlyFiles: true })
//
//   const obj = {}
//   await Promise.all(
//     filenames.map(async (name) => {
//       obj[name.replace(/\..+?$/, '')] = await parseByCode(fsExtra.readFile(nps.resolve(name), 'utf8'))
//     })
//   )
//   return obj
// }
