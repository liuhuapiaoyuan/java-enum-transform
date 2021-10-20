/**
 * @file main
 * @author imcuttle
 * @date 2018/4/4
 */
import { parseImport } from '../src/parse-imports'
import { readContent } from './helper'

describe('parseImport', function () {
  it('should spec', async function () {
    expect(parseImport(await readContent('com/example/import/Test.java'))).toMatchInlineSnapshot(`
Array [
  Object {
    "member": Array [],
    "name": "JsonCreator",
    "package": Array [
      "com",
      "fasterxml",
      "jackson",
      "annotation",
    ],
    "static": false,
    "wildcard": false,
  },
  Object {
    "member": Array [],
    "name": "JsonValue",
    "package": Array [
      "com",
      "fasterxml",
      "jackson",
      "annotation",
    ],
    "static": false,
    "wildcard": false,
  },
  Object {
    "member": Array [],
    "package": Array [
      "java",
      "util",
    ],
    "static": false,
    "wildcard": true,
  },
  Object {
    "member": Array [
      "abc",
    ],
    "name": "Optional",
    "package": Array [
      "java",
      "util",
    ],
    "static": true,
    "wildcard": false,
  },
  Object {
    "member": Array [],
    "name": "Optional",
    "package": Array [
      "java",
      "util",
    ],
    "static": true,
    "wildcard": true,
  },
  Object {
    "member": Array [],
    "name": "Optional",
    "package": Array [
      "java",
      "util",
    ],
    "static": true,
    "wildcard": true,
  },
]
`)
  })
})
