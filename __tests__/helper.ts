/**
 * @file helper
 */

import * as nps from 'path'
import * as fs from 'fs-extra'

function fixture(...argv: string[]) {
  return nps.join.apply(nps, [__dirname, 'fixture'].concat(argv))
}

function readContent(name) {
  return fs.readFile(fixture(name), 'utf-8')
}

export { fixture, readContent }
