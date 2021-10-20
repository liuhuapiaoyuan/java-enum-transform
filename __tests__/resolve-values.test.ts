/**
 * @file main
 * @author imcuttle
 * @date 2018/4/4
 */
import { parse } from 'java-parser'
import { parseBodyMember, resolveExpressionValue, resolveImportsValue } from '../src/resolve-values'

const astMap = {
  'com.example.Main': parse(`import com.example.utils.*;
import com.example.Foo;
import com.example.Foo.*;`),
  'com.example.Foo': parse(`import static com.example.utils.B.b;

public class Foo {
  public static int count = 1;
  public static int count_invalid;
  public static boolean isOk = true;
  public static boolean isOk_invalid;
  public String name = "imcuttle";
  public String concatString = "a" + "b";
  public String computed_1 = b;
  public String computed_2 = b + "tail";
}
      `),
  'com.example.utils.A': parse(`
public class A {
  public static char a = 'a';
}
      `),
  'com.example.utils.B': parse(`
public class A {
  public static String b = "b";
}
      `)
}

const getAstById = jest.fn((id) => {
  return astMap[id]
})
const getAstMapByPackage = jest.fn((scope) => {
  const asts = {}
  Object.keys(astMap).forEach((name) => {
    if (name.startsWith(scope + '.') && !name.slice((scope + '.').length).includes('.')) {
      asts[name.slice((scope + '.').length)] = astMap[name]
    }
  })
  return asts
})

describe('resolve-values', function () {
  beforeEach(() => {
    getAstById.mockClear()
    getAstMapByPackage.mockClear()
  })

  it('parseBodyMember', function () {
    expect(
      parseBodyMember(
        parse(`
    import static com.example.utils.B.b;

public class Foo {
  public static int count = 1;
  public static int count_invalid;
  public static boolean isOk = true;
  public static boolean isOk_invalid;
  public String name = "imcuttle";
  // 暂不支持
  public String concatString = "a" + "b";
  public String computed_1 = b;
  // 暂不支持
  public String computed_2 = b + "tail";
}
`)
      )
    ).toMatchInlineSnapshot(`
      Map {
        "count" => Object {
          "static": true,
          "value": Object {
            "raw": "1",
            "type": "primitive",
            "value": 1,
          },
        },
        "isOk" => Object {
          "static": true,
          "value": Object {
            "raw": "true",
            "type": "primitive",
            "value": true,
          },
        },
        "name" => Object {
          "static": false,
          "value": Object {
            "raw": "\\"imcuttle\\"",
            "type": "primitive",
            "value": "imcuttle",
          },
        },
        "computed_1" => Object {
          "static": false,
          "value": Object {
            "raw": "b",
            "type": "fqnOrRefType",
            "value": Array [
              "b",
            ],
          },
        },
      }
    `)
  })

  it('resolveImportsValue', async function () {
    const result = resolveImportsValue(astMap['com.example.Main'], {
      getAstById,
      getAstMapByPackage
    })
    expect(result).toMatchSnapshot()
    // same ref
    expect(result.get('count')).toBe((result.get('Foo') as any).get('count'))

    expect(getAstById.mock.calls).toMatchInlineSnapshot(`
      Array [
        Array [
          "com.example.utils.A",
        ],
        Array [
          "com.example.utils.B",
        ],
        Array [
          "com.example.Foo",
        ],
        Array [
          "com.example.Foo",
        ],
      ]
    `)
    expect(getAstMapByPackage.mock.calls).toMatchInlineSnapshot(`
      Array [
        Array [
          "com.example.utils",
        ],
      ]
    `)
  })
  it('resolveImportsValue 1', async function () {
    const result = resolveImportsValue(
      parse(`
    import static com.example.utils.B.b;
    `),
      {
        getAstById,
        getAstMapByPackage
      }
    )
    expect(result).toMatchInlineSnapshot(`
Map {
  "b" => Object {
    "static": true,
    "value": Object {
      "raw": "\\"b\\"",
      "type": "primitive",
      "value": "b",
    },
  },
}
`)
  })

  it('resolveExpressionValue#primitive', function () {
    const opts = {
      getAstById,
      getAstMapByPackage,
      id: 'com.example.Foo',
      fieldName: 'bar'
    }

    const item = {
      value: true,
      raw: 'true',
      type: 'primitive'
    }
    expect(resolveExpressionValue(item as any, opts)).toBe(item)
  })

  it('resolveExpressionValue#fqnOrRefType', function () {
    const opts = {
      getAstById,
      getAstMapByPackage,
      id: 'com.example.Main',
      fieldName: 'foo'
    }

    const item: Parameters<typeof resolveExpressionValue>[0] = {
      value: ['Foo', 'count'],
      raw: 'Foo.count',
      type: 'fqnOrRefType'
    }

    expect(resolveExpressionValue(item, opts)).toEqual({
      raw: '1',
      type: 'primitive',
      value: 1
    })
  })

  it('resolveExpressionValue#fqnOrRefType computed', function () {
    const opts = {
      getAstById,
      getAstMapByPackage,
      id: 'com.example.Main',
      fieldName: 'foo'
    }

    const item: Parameters<typeof resolveExpressionValue>[0] = {
      value: ['Foo', 'computed_1'],
      raw: 'Foo.computed_1',
      type: 'fqnOrRefType'
    }

    expect(resolveExpressionValue(item, opts)).toEqual({
      raw: '"b"',
      type: 'primitive',
      value: 'b'
    })
  })
})
