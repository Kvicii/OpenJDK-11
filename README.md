# OpenJDK-11

> **Read The Fucking Source Code**　　---- [RTFM](https://en.wikipedia.org/wiki/RTFM)
>     
> **源码面前，了无秘密**　　---- [侯捷](https://zh.wikipedia.org/wiki/%E4%BE%AF%E4%BF%8A%E5%82%91_%28%E4%BD%9C%E5%AE%B6%29)


## 项目介绍

`OpenJDK-11`源码，主要用来记录阅读源码的笔记与体会，可供阅读学习使用。

* `src`目录存放OpenJDK-11源码。阅读过程中产生的笔记以注释的形式直接写在源码文件中。
* `test`目录存放源码阅读过程中写的一些测试文件，使用时可将相关文件复制到新建项目中。


## 源码版本

> **openjdk version "11" 2018-09-25**
>
> **OpenJDK Runtime Environment 18.9 (build 11+28)**
>
> **OpenJDK 64-Bit Server VM 18.9 (build 11+28, mixed mode)**


## 使用说明

1. 该源码主要除了包括`src.zip`里提供的源码，此外还包括一部分未公开的源码。
   
   该源码**不支持**直接编译。如想完整编译整个`OpenJDK`项目，请自行到`OpenJDK`项目官网下载完整的工具链。
   
2. 此源码与`Oracle JDK`源码并非完全一致。
   
   `Oracle JDK`仅开源了一部分代码。想深入学习JDK的话建议阅读此源码。

3. 源码原本采用了`JDK 9`之后的`module`模块系统组织，此处修改为熟悉的包组织形式，以便阅读。

4. 因为本源码已经包含了`OpenJDK`中绝大多数公开的源码，所以**不建议**再为此项目重复关联JDK。

5. 个别源码在IDE中打开后可能会显示[错误]信息，这是因为某个源文件异常或缺失而导致的。对此，解决方案有两个：
   * 如果缺失部分影响到其他方法的理解，请自行到Google搜索相关源文件放到项目中合适的位置，或作为依赖库关联到项目中，比如部分源码需要关联Junit4测试库。    
   * 如果仅仅是因为显示的异常信息造成了视觉上的干扰，请在IDE中自行关闭或切换[错误]信息的显示方式即可。

6. **欢迎在Issues反馈好的想法、建议、意见。**


## Commit图例

| 序号 | emoji | 在本项目中的含义 |
| ---- | ----- | --------------- |
| (0) | :tada: | 初始化项目 |
| (1) | :memo: | 更新文档，包括但不限于README、.gitignore |
| (2) | :bulb: | 发布新的阅读笔记 <sub>**(注1)**</sub> |
| (3) | :sparkles: | 记录新特性 |
| (4) | :recycle: | 重构代码，主要是修改之前的阅读笔记，极少情形下会修改源码 <sub>**(注2)**</sub> |
| (5) | :pencil2: | 更正错别字、调节不合理的分组、修改源码排版 |
| (6) | :white_check_mark: | 发布测试文件 |

>
> 注1：    
>    
> 使用标记 :bulb: 统一替换旧标记 :bulb:|:bulb::clown_face:|:construction::sparkles:|:construction::speech_balloon:    
> 关于某个源码当前的阅读完成度，参见[已阅代码清单_按功能排序](已阅代码清单_按功能排序.md)    
>    
> 注2：涉及到修改源码的场景，包括但不限于： 
>    
>> 修改无意义的形参名为更易懂的形参名；    
>> 修改控制语句结构或在控制语句作用域上添加花括号；    
>> 修改匿名方法/类为有名方法/类；    
>> 拆分过长且难读的调用链，将中间过程单独摘出来；    
>> 提取频繁出现的某段操作为单个方法；    
>>     
>    
> 修改的原则是：修改量极少，且不改变原有方法逻辑    
> 修改的目的是：仅为增强可读性


## 相关链接

[OpenJDK-11官网](http://jdk.java.net/11/)    

[OpenJDK存档-第三方网站](https://adoptopenjdk.net/?variant=openjdk11&jvmVariant=hotspot)    


## 脚注

Commit信息中的emoji参考来源：
* [Full Emoji List](https://unicode.org/emoji/charts/full-emoji-list.html)    
* [gitmoji](https://gitmoji.carloscuesta.me/)    
 

## 附录

#### [已阅代码清单_按名称排序](已阅代码清单_按名称排序.md)
#### [已阅代码清单_按功能排序](已阅代码清单_按功能排序.md)
#### [测试文件清单](测试文件清单.md)
