# java-enum-transform

[![Build status](https://img.shields.io/travis/余聪/java-enum-transform/master.svg?style=flat-square)](https://travis-ci.com/余聪/java-enum-transform)
[![Test coverage](https://img.shields.io/codecov/c/github/余聪/java-enum-transform.svg?style=flat-square)](https://codecov.io/github/余聪/java-enum-transform?branch=master)
[![NPM version](https://img.shields.io/npm/v/java-enum-transform.svg?style=flat-square)](https://www.npmjs.com/package/java-enum-transform)
[![NPM Downloads](https://img.shields.io/npm/dm/java-enum-transform.svg?style=flat-square&maxAge=43200)](https://www.npmjs.com/package/java-enum-transform)
[![Prettier](https://img.shields.io/badge/code_style-prettier-ff69b4.svg?style=flat-square)](https://prettier.io/)
[![Conventional Commits](https://img.shields.io/badge/Conventional%20Commits-1.0.0-yellow.svg?style=flat-square)](https://conventionalcommits.org)

> Java enum transform

### 1.0.4  
- 增加对无参数枚举的识别，
- 调整对单参数枚举的识别，单参数第一个参数为枚举的中文label
- 统一 单参数，双参数，无参数三者的输出形态

- [ ] TODO：单参数识别上一行的注释  // 开头的,  /**  **/


### Input

```java
// User.java
package com.example.foo;

import com.example.foo.Consts;

public class enum User {
  ADMIN(1, "管理员"),
  DEVELOPER(2, "开发人员");
  OWNER(Consts.OWNER, "拥有者");
}
```

```java
// Consts.java
package com.example.foo;

public class Consts {
  public static int OWNER = 3;
}
```

### Output

```typescript
export const enum UserEnum {
  /**
   * 管理员
   */
  ADMIN = 1,
  /**
   * 开发人员
   */
  DEVELOPER = 2,
  /**
   * 拥有者
   */
  OWNER = 3
}
export const UserEnumOptions = [
  { label: '管理员', value: UserEnum['ADMIN'] },
  { label: '开发人员', value: UserEnum['DEVELOPER'] },
  { label: '拥有者', value: UserEnum['OWNER'] }
]
```

## Installation

```bash
npm install java-enum-transform
# or use yarn
yarn add java-enum-transform
```

## Usage

```javascript
import { javaEnumTransformByDir, resultToCode } from 'java-enum-transform'

const enumCode = resultToCode(await javaEnumTransformByDir('path/to/java'))
```

## Contributing

- Fork it!
- Create your new branch:  
  `git checkout -b feature-new` or `git checkout -b fix-which-bug`
- Start your magic work now
- Make sure npm test passes
- Commit your changes:  
  `git commit -am 'feat: some description (close #123)'` or `git commit -am 'fix: some description (fix #123)'`
- Push to the branch: `git push`
- Submit a pull request :)

## Authors

This library is written and maintained by 余聪, <a href="mailto:yucong@yuanfudao.com">yucong@yuanfudao.com</a>.

## License

MIT - [余聪](https://github.com/余聪) 🐟
