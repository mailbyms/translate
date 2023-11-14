## translate
使用deepl 免费API，翻译英文字幕为中文版本

注意，要先部署 deeplx，见 https://github.com/OwO-Network/DeepLX/

### 编译运行
- 编译项目
```shell
mvn package -DskipTests=true
```
- 运行
```shell
java -jar translate.jar xxx.srt
```

- 【可选】Spring Native 原生编译

```shell
# 会生成 translate 可执行文件
mvn -DskipTests -Pnative native:compile
./translate xxx.srt
```

