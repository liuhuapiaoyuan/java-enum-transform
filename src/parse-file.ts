import * as nps from 'path'
import { CstNode, BaseJavaCstVisitorWithDefaults, parse } from 'java-parser'
import { sep, dirname } from 'path'

export interface Meta {
  type: 'enum' | 'class' | 'record' | null
  access: 'public' | 'protected' | 'private' | 'default'
  package: null | string[]
  name: string
}

interface ParseFileMetaOptions {
  classPath?: string
  filename?: string
}

class ParseFileMeta extends BaseJavaCstVisitorWithDefaults {
  constructor(protected options: ParseFileMetaOptions = {}) {
    super()
    const { classPath, filename } = this.options

    if (filename) {
      const relativeFilename = classPath ? nps.relative(classPath, filename) : filename
      this.data.package = dirname(relativeFilename).split(sep)
      this.data.name = nps.basename(relativeFilename, nps.extname(relativeFilename))
    }
  }

  public data: Meta = {
    type: null,
    access: 'default',
    package: null,
    name: ''
  }

  state = {
    inClassDeclaration: false,
    done: false
  }

  packageDeclaration(ctx) {
    if (ctx.Identifier?.length) {
      this.data.package = ctx.Identifier.map((x) => x.image)
    }
  }

  classDeclaration(ctx, param) {
    if (this.state.done || this.state.inClassDeclaration) {
      return
    }

    if (ctx.enumDeclaration) {
      this.data.type = 'enum'
    } else if (ctx.recordDeclaration) {
      this.data.type = 'record'
    } else if (ctx.normalClassDeclaration) {
      this.data.type = 'class'
    }
    const classModifierCtx = ctx.classModifier?.[0]?.children
    if (classModifierCtx) {
      if (classModifierCtx.Public) {
        this.data.access = 'public'
      } else if (classModifierCtx.Protected) {
        this.data.access = 'protected'
      } else if (classModifierCtx.Private) {
        this.data.access = 'private'
      }
      // classModifierCtx.Abstract
      // classModifierCtx.Final
    }

    this.state.inClassDeclaration = true
    return super.classDeclaration(ctx, param)
  }

  typeIdentifier(ctx, param) {
    if (!this.state.done && this.state.inClassDeclaration) {
      this.state.done = true
      this.data.name = ctx.Identifier[0]?.image || this.data.name
    }
  }
}

export function parseFileMeta(contentOrAst: string | CstNode, opts?: ParseFileMetaOptions) {
  const v = new ParseFileMeta(opts)
  v.visit(typeof contentOrAst === 'string' ? parse(contentOrAst) : contentOrAst)
  return v.data
}
