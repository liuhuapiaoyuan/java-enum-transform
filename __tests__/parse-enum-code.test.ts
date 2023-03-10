/**
 * @file main
 * @author imcuttle
 * @date 2018/4/4
 */
import { formatToTs, parseEnumCode } from '../src/parse-enum-code'
import { readContent } from './helper'

describe('parseEnumCode', function () {
  it('should spec', async function () {
    expect(parseEnumCode(await readContent('com/example/models/RoleEnum.java'))).toMatchInlineSnapshot(`
Array [
  Object {
    "enumClass": "RoleType",
    "enums": Array [
      Object {
        "label": Object {
          "raw": "\\"开发者\\"",
          "type": "primitive",
          "value": "开发者",
        },
        "name": "DEVELOPER",
        "value": Object {
          "raw": "1",
          "type": "primitive",
          "value": 1,
        },
      },
      Object {
        "label": Object {
          "raw": "\\"管理xx\\"",
          "type": "primitive",
          "value": "管理xx",
        },
        "name": "ADMIN",
        "value": Object {
          "raw": "0xee",
          "type": "primitive",
          "value": 238,
        },
      },
      Object {
        "label": Object {
          "raw": "\\"管理\\"",
          "type": "primitive",
          "value": "管理",
        },
        "name": "ADMIN",
        "value": Object {
          "raw": "X.s.l.l",
          "type": "fqnOrRefType",
          "value": Array [
            "X",
            "s",
            "l",
            "l",
          ],
        },
      },
      Object {
        "label": Object {
          "raw": "\\"用户\\"",
          "type": "primitive",
          "value": "用户",
        },
        "name": "USER",
        "value": Object {
          "raw": "0b1",
          "type": "primitive",
          "value": 1,
        },
      },
      Object {
        "label": Object {
          "raw": "\\"用户\\"",
          "type": "primitive",
          "value": "用户",
        },
        "name": "USER_X",
        "value": Object {
          "raw": "'s'",
          "type": "primitive",
          "value": "s",
        },
      },
    ],
  },
]
`)
    expect(formatToTs(parseEnumCode(await readContent('com/example/models/RoleEnum.java')))).toMatchSnapshot()
    // expect(formatToTs(parseEnumCode(await readContent('com/example/consts/ACLResource.java')))).toMatchSnapshot()
  })
})
