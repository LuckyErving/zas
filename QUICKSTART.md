# 增爱刷 - 快速开始指南

欢迎使用增爱刷！本文档将帮助您快速开始使用本项目。

## 🚀 快速部署到GitHub

### 步骤1: 初始化Git仓库

在项目根目录执行：

```bash
./init.sh
```

或手动执行：

```bash
git init
git add .
git commit -m "Initial commit: 增爱刷 Android刷题APP"
```

### 步骤2: 创建GitHub仓库

1. 访问 https://github.com/new
2. 创建一个新仓库（例如命名为 `zas`）
3. **不要**勾选"Initialize this repository with a README"

### 步骤3: 关联远程仓库

将下面的命令中的 `YOUR_USERNAME` 替换为你的GitHub用户名：

```bash
git remote add origin https://github.com/YOUR_USERNAME/zas.git
git branch -M main
git push -u origin main
```

### 步骤4: 触发自动构建

创建一个版本标签来触发GitHub Actions自动构建：

```bash
git tag -a v1.0.0 -m "Release version 1.0.0"
git push origin v1.0.0
```

### 步骤5: 等待构建完成

1. 访问你的GitHub仓库
2. 点击 "Actions" 标签页查看构建进度
3. 构建时间约5-10分钟（首次构建会下载依赖）
4. 构建成功后，自动发布到 "Releases" 页面
5. 下载APK文件

文件名格式：`增爱刷-1.0.0.apk`

**构建优化说明**：
- 使用 `gradle/actions/setup-gradle@v3` 自动管理Gradle
- 启用依赖缓存，后续构建更快
- 无需手动配置gradle-wrapper.jar

## 📱 安装使用

### 在Android手机上安装

1. 从GitHub Releases页面下载APK文件
2. 在手机上打开APK文件
3. 允许安装未知来源的应用
4. 完成安装

### 导入题库

1. 准备JSON格式的题库文件（参考项目中的 `AI_level3.json`）
2. 将JSON文件传输到手机
3. 打开APP，点击"刷题"页面右上角的"+"按钮
4. 选择JSON文件，输入题库名称
5. 等待导入完成

## 🔧 本地开发（可选）

如果你想在本地构建和调试：

### 环境要求

- Android Studio Hedgehog (2023.1.1) 或更高版本
- JDK 17
- Android SDK 34

### 导入项目

1. 打开Android Studio
2. 选择 "Open an Existing Project"
3. 选择项目根目录
4. 等待Gradle同步完成

### 运行应用

1. 连接Android设备或启动模拟器
2. 点击 "Run" 按钮（绿色三角形）
3. 选择目标设备
4. 等待应用安装和启动

### 本地构建APK

```bash
# 确保已下载gradle-wrapper.jar
./gradlew assembleRelease
```

生成的APK位于：`app/build/outputs/apk/release/app-release.apk`

## 📝 题库格式

### 基本结构

```json
{
  "status": 200,
  "msg": "",
  "obj": {
    "id": "unique_bank_id",
    "type": 1,
    "list": [
      {
        "id": "unique_question_id",
        "stemlist": [
          {
            "text": "这是题目内容",
            "type": 1
          }
        ],
        "answer": "A",
        "options": [
          {
            "tag": "A",
            "value": "选项A的内容",
            "valuelist": [{"text": "选项A的内容", "type": 1}]
          },
          {
            "tag": "B",
            "value": "选项B的内容",
            "valuelist": [{"text": "选项B的内容", "type": 1}]
          }
        ],
        "type": 1,
        "jx": "这是题目解析",
        "stemimg": []
      }
    ]
  }
}
```

### 题型说明

- `type: 1` - 单选题（answer示例："A"）
- `type: 2` - 多选题（answer示例："A,B"）
- `type: 3` - 判断题（options为"正确"和"错误"，answer为"A"或"B"）

### 批量转换

如果你有其他格式的题库，可以编写脚本转换为上述JSON格式。

## ❓ 常见问题

### Q1: GitHub Actions构建失败？

**A**: 检查以下几点：
- 确保仓库公开或有Actions权限
- 查看Actions日志找出具体错误
- 确认gradle配置文件无误

### Q2: APK安装失败？

**A**: 可能的原因：
- 未允许安装未知来源应用
- 系统版本过低（需要Android 7.0+）
- 存储空间不足

### Q3: 导入题库时提示格式错误？

**A**: 
- 检查JSON格式是否正确
- 确保包含所有必需字段
- 使用JSON验证工具检查语法

### Q4: 如何更新应用版本？

**A**: 
1. 修改 `app/build.gradle.kts` 中的 `versionCode` 和 `versionName`
2. 提交更改
3. 创建新的版本标签：
   ```bash
   git tag -a v1.1.0 -m "Release version 1.1.0"
   git push origin v1.1.0
   ```

### Q5: 可以在iOS上使用吗？

**A**: 目前仅支持Android平台。如需iOS版本，需要使用Flutter或React Native重新开发。

## 🤝 获取帮助

- 查看 [README.md](README.md) 了解项目详情
- 查看 [USAGE.md](USAGE.md) 了解详细使用说明
- 查看 [PROJECT_STRUCTURE.md](PROJECT_STRUCTURE.md) 了解项目结构
- 在GitHub仓库提交Issue获取帮助

## 🎉 开始使用

现在你已经完成了所有设置！开始享受增爱刷带来的高效刷题体验吧！

如有任何问题或建议，欢迎通过GitHub Issues反馈。
