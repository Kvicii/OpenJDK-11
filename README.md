# OpenJDK-11

> **Read The Fucking Source Code**　　---- [RTFM](https://en.wikipedia.org/wiki/RTFM)
>     
> **源码面前，了无秘密**　　---- [侯捷](https://zh.wikipedia.org/wiki/%E4%BE%AF%E4%BF%8A%E5%82%91_%28%E4%BD%9C%E5%AE%B6%29)


## 项目介绍

`OpenJDK-11`源码笔记，主要记录阅读`JDK`源码时的理解与体会，仅供参考。

* `src`目录存放OpenJDK-11源码。阅读过程中产生的笔记以注释的形式直接写在源码文件中。
* `test`目录存放源码阅读过程中写的一些测试文件，使用时可将相关文件复制到新建项目中。
  * 注1：建议在JDK 11的环境下运行测试文件
  * 注2：不是每个类/接口都有对应的测试文件


## 源码版本

> **openjdk version "11" 2018-09-25**
>
> **OpenJDK Runtime Environment 18.9 (build 11+28)**
>
> **OpenJDK 64-Bit Server VM 18.9 (build 11+28, mixed mode)**


## 使用说明

1. 通过`git`克隆到本地，然后使用`IntelliJ IDEA`打开即可。阅读时**不需要**再为此项目重复关联JDK。

2. 该源码主要包含`src.zip`里提供的源码，与`Oracle JDK 11`源码保持一致。
   
   该源码**不支持**直接编译。如想完整编译整个`OpenJDK`项目，请到`OpenJDK`项目官网下载完整的工具链。
   
3. 由于大多数开源项目使用`OpenJDK`构建，故想深入学习Java的话建议阅读此源码。

4. `JDK`源码在9.0版本之后已采用模块系统([`Module System`](http://openjdk.java.net/projects/jigsaw/spec/sotms/))组织，此处为方便阅读，故修改为熟悉的包组织形式。

5. 如果项目因缺失个别非JDK核心源码而报错，请到谷歌搜索相关的jar包导入即可。或者，可在`Github Issues`提出反馈。如果是单元测试文件之类的测试文件报错，直接删掉就好了。

6. **欢迎在Issues反馈好的想法、建议、意见。**


## Commit图例

| 序号 | emoji | 在本项目中的含义 |
| ---- | ----- | --------------- |
| (0) | :tada: | 初始化项目 |
| (1) | :memo: | 更新文档，包括但不限于README、.gitignore |
| (2) | :bulb: | 发布新的阅读笔记 <sub>**(注1)**</sub> |
| (3) | :sparkles: | 记录新特性 |
| (4) | :recycle: | 重构代码，主要是修改之前的阅读笔记，极少情形下会修改源码 <sub>**(注2)**</sub> |
| (5) | :pencil2: | 更正错别字、调节不合理的分组、修改源码排版、目录结构等 |
| (6) | :white_check_mark: | 发布测试文件 |

>
> 注1：    
>    
> 使用标记 :bulb: 统一替换旧标记 :bulb:|:bulb::clown_face:|:construction::sparkles:|:construction::speech_balloon:    
>    
> 关于某个源码当前的阅读完成度，参见[已阅代码清单_按功能排序](已阅代码清单_按功能排序.md)    
>    
> 注2：涉及到修改源码的场景，包括但不限于： 
>    
>> 修改无意义的形参名为更易懂的形参名；    
>> 修改控制语句结构
>> 在控制语句作用域上添加花括号；    
>> 修改匿名类为非匿名类；    
>> 拆分过长且难读的调用链，将中间过程单独摘出来；    
>> 提取频繁出现的某段操作为单个方法；    
>> 将一个文件内的多个类拆分到不同的文件中（内部类不拆分）；    
>> 函数式调用与普通调用的转换；    
>> for循环和foreach循环的转换；    
>>     
>    
> 修改的原则是：修改量极少，且不改变原有的逻辑与结果（涉及到多线程的代码有些迷）    
> 修改的目的是：增强可读性，便于添加笔记


## 相关链接

[OpenJDK-11官网](http://jdk.java.net/11/)    

[OpenJDK存档-第三方网站](https://adoptopenjdk.net/?variant=openjdk11&jvmVariant=hotspot)    


## 脚注

Commit信息中的emoji参考来源：
* [Full Emoji List](https://unicode.org/emoji/charts/full-emoji-list.html)    
* [gitmoji](https://gitmoji.carloscuesta.me/)    
 

## 附录

#### [已阅代码清单_按功能排序](已阅代码清单_按功能排序.md)
#### [已阅代码清单_按名称排序](已阅代码清单_按名称排序.md)
#### [测试文件清单](测试文件清单.md)
