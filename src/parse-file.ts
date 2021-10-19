import * as fsExtra from 'fs-extra'

import { CstNode, BaseJavaCstVisitorWithDefaults, parse } from 'java-parser'

export interface Meta {
  type: 'enum' | 'class' | 'record' | null
  access: 'public' | 'protected' | 'private' | 'default'
  package: null | string[]
  name: string
}

class ParseFileMeta extends BaseJavaCstVisitorWithDefaults {
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
    if (!this.data.package) {
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
      this.data.name = ctx.Identifier[0]?.image
    }
  }
}

export function parseFileMeta(contentOrAst: string | CstNode) {
  const v = new ParseFileMeta()
  v.visit(typeof contentOrAst === 'string' ? parse(contentOrAst) : contentOrAst)
  return v.data
}

export async function parseFileMetaByFile(filename: string) {
  const content = await fsExtra.readFile(filename, 'utf8')
  return parseFileMeta(content)
}
