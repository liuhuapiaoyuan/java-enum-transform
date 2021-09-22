/**
 * @file main
 * @author imcuttle
 * @date 2018/4/4
 */
import { parseConsts } from '../src/parse-consts'
import { readContent, fixture } from './helper'

describe('parseConsts', function () {
  it('should spec', async function () {
    console.log(await parseConsts(fixture()))
  })
})
