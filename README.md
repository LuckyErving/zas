# 增爱刷 - Android刷题APP

<div align="center">
  <h3>📚 一款精美实用的Android刷题应用</h3>
  <p>支持单选、多选、判断题，提供收藏和错题本功能</p>
</div>

## ✨ 功能特性

- 📥 **习题导入** - 支持从本地选择JSON文件批量导入习题
- 📝 **多种题型** - 支持单选题、多选题、判断题
- 🎯 **双模式刷题** - 按顺序刷题和随机刷题两种模式
- ⭐ **收藏功能** - 标记重要题目，方便复习
- ❌ **错题本** - 自动记录答错的题目，针对性练习
- 📊 **学习统计** - 实时显示学习进度和正确率
- 💾 **数据管理** - 支持导出和导入学习数据
- 🎨 **Material Design 3** - 现代化UI设计，美观易用

## 📱 应用截图

*即将上传*

## 🚀 下载安装

前往 [Releases](../../releases) 页面下载最新版本的APK文件。

文件名格式：`增爱刷-版本号.apk`

## 📖 使用说明

### 导入习题

1. 准备JSON格式的习题文件（参考`AI_level3.json`的格式）
2. 打开APP，点击"刷题"页面右上角的"+"按钮
3. 选择JSON文件并输入题库名称
4. 等待导入完成

### JSON格式说明

习题JSON文件应包含以下结构：

```json
{
  "status": 200,
  "obj": {
    "id": "题库ID",
    "list": [
      {
        "id": "题目ID",
        "stemlist": [{"text": "题目内容", "type": 1}],
        "answer": "A",
        "options": [
          {"tag": "A", "value": "选项A内容"},
          {"tag": "B", "value": "选项B内容"}
        ],
        "type": 1,
        "jx": "解析内容"
      }
    ]
  }
}
```

题型说明：
- `type: 1` - 单选题
- `type: 2` - 多选题（答案格式如"A,B"）
- `type: 3` - 判断题（选项为"正确"和"错误"）

### 刷题模式

- **顺序刷题**：按导入顺序依次练习
- **随机刷题**：随机打乱题目顺序

### 数据管理

在"我的"页面可以：
- 查看学习统计信息
- 导出学习数据（JSON格式）
- 删除题库

## 🛠️ 技术栈

- **语言**：Kotlin
- **UI框架**：Jetpack Compose + Material Design 3
- **架构**：MVVM + Repository模式
- **数据库**：Room
- **异步处理**：Kotlin Coroutines + Flow
- **JSON解析**：Gson
- **导航**：Navigation Compose

## 🔨 构建说明

本项目使用GitHub Actions自动构建和发布。

### 本地构建

```bash
# 克隆仓库
git clone https://github.com/your-username/zas.git
cd zas

# 构建APK
./gradlew assembleRelease
```

### 自动发布

推送tag即可触发自动构建和发布：

```bash
git tag -a v1.0.0 -m "Release version 1.0.0"
git push origin v1.0.0
```

## 📄 开源协议

MIT License

## 🤝 贡献

欢迎提交Issue和Pull Request！

## 📮 联系方式

如有问题或建议，请通过Issue联系。

---

<div align="center">
  Made with ❤️ by Erving
</div>
