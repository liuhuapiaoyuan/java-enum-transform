## [1.0.6] 2023-03-10


### feat
- 支持独立渲染单独文件   resultItemToCode  这样有助于独立生成enum文件 方便整体维护
- 增强对枚举类型的格式识别
- A(B,C)的识别=》 A为代码，B为vale C为label
- A(B)的识别=》 A为代码，A为vale B为label
- A，无参数的识别=》 A为代码，A为vale 无label
### 新增对comment注释的处理
- 如果无参数，且有注释，则注释为label
- 如果参数1， 且有注释+参数为英文，则 参数为value，注释为label 。  如果参数为中文，则无法当value，则替代注释为label


## [1.0.3](https://github.com/余聪/java-enum-transform/compare/v1.0.2...v1.0.3) (2021-12-09)

### Bug Fixes

- empty arguments ([46a5a1c](https://github.com/余聪/java-enum-transform/commit/46a5a1c826ab75f6e74ecd60ba04dbb8650faceb))

## [1.0.2](https://github.com/余聪/java-enum-transform/compare/v1.0.1...v1.0.2) (2021-10-21)

## 1.0.1 (2021-10-20)
