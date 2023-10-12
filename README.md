## translate
使用百度翻译API，翻译英文字幕为中文版本

在平台申请的APP_ID 详见 http://api.fanyi.baidu.com/api/trans/product/desktop?req=developer  

注意百度免费开发账号限制：不限字数，调用频次限每秒1次

### 编译运行
- 编译项目
```shell
mvn package -DskipTests=true
```
- 运行
需要在环境变量设定百度的开发者账号信息 APP_ID 和 SECURITY_KEY
```shell
APP_ID=2019_XXX_REPLACE_ME SECURITY_KEY=GHx_XXX_REPLACE_ME java -jar translate.jar xxx.srt
```

- 【可选】Spring Native 原生编译

```shell
# 会生成 translate 可执行文件
mvn -DskipTests -Pnative native:compile
./translate xxx.srt
```

