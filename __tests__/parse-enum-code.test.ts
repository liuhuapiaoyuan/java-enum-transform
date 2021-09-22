/**
 * @file main
 * @author imcuttle
 * @date 2018/4/4
 */
import { parseEnumCode } from '../src/parse-enum-code'
import { readContent } from './helper'

describe('parseEnumCode', function () {
  it('should spec', async function () {
    console.log(parseEnumCode(await readContent('com/example/models/RoleEnum.java')))
  })
})
