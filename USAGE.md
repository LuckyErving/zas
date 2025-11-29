# 使用说明

## 准备工作

由于gradle-wrapper.jar文件较大，需要在首次构建前手动下载或通过GitHub Actions自动下载。

### 方法1：使用GitHub Actions自动构建

1. 将项目推送到GitHub
2. 创建一个tag触发构建：
   ```bash
   git tag -a v1.0.0 -m "First release"
   git push origin v1.0.0
   ```
3. GitHub Actions会自动下载依赖并构建APK

### 方法2：本地构建

如果需要本地构建，请先下载gradle-wrapper.jar：

```bash
# 在项目根目录执行
curl -L https://services.gradle.org/distributions/gradle-8.2-bin.zip -o gradle.zip
unzip gradle.zip
cp gradle-8.2/lib/gradle-wrapper-8.2.jar gradle/wrapper/gradle-wrapper.jar
rm -rf gradle-8.2 gradle.zip
```

然后运行：
```bash
./gradlew assembleRelease
```

## 应用图标

当前使用默认图标。如需自定义图标，请替换以下文件：
- `app/src/main/res/mipmap-*/ic_launcher.png`
- `app/src/main/res/mipmap-*/ic_launcher_round.png`

推荐使用 [Android Asset Studio](https://romannurik.github.io/AndroidAssetStudio/icons-launcher.html) 生成图标。

## 签名配置

Release版本使用debug签名。生产环境建议配置正式签名：

1. 生成keystore文件
2. 在`app/build.gradle.kts`中配置签名信息
3. 将keystore密码等敏感信息存储在GitHub Secrets中

## 题库格式示例

参考项目根目录的`AI_level3.json`文件。关键字段：

- `stemlist[0].text`: 题目内容
- `answer`: 正确答案（单选如"A"，多选如"A,B"）
- `options`: 选项数组
- `type`: 题型（1=单选，2=多选，3=判断）
- `jx`: 解析内容

## 常见问题

### Q: 导入JSON文件失败？
A: 检查JSON格式是否正确，必须包含`status`、`obj`、`list`等字段。

### Q: 如何批量导入多个题库？
A: 可以多次点击"+"按钮分别导入不同的JSON文件。

### Q: 导出的数据如何使用？
A: 导出的JSON文件包含学习进度，重装APP后可以通过相同的导入功能恢复数据。

### Q: GitHub Actions构建失败？
A: 检查gradle版本、依赖版本是否正确，查看Actions日志定位问题。
