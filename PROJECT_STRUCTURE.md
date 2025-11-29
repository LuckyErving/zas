# 增爱刷 - 项目结构说明

## 目录结构

```
zas/
├── .github/
│   └── workflows/
│       └── android.yml          # GitHub Actions自动构建配置
├── app/
│   ├── src/
│   │   └── main/
│   │       ├── java/com/zengaishua/app/
│   │       │   ├── data/
│   │       │   │   ├── dao/              # 数据访问对象
│   │       │   │   │   ├── QuestionDao.kt
│   │       │   │   │   ├── QuestionBankDao.kt
│   │       │   │   │   └── LearningRecordDao.kt
│   │       │   │   ├── database/         # 数据库配置
│   │       │   │   │   └── AppDatabase.kt
│   │       │   │   ├── model/            # 数据模型
│   │       │   │   │   ├── Models.kt
│   │       │   │   │   └── JsonModels.kt
│   │       │   │   └── repository/       # 数据仓库
│   │       │   │       └── QuestionRepository.kt
│   │       │   ├── ui/
│   │       │   │   ├── screen/           # 界面页面
│   │       │   │   │   ├── PracticeScreen.kt
│   │       │   │   │   ├── QuestionScreen.kt
│   │       │   │   │   └── MineScreen.kt
│   │       │   │   ├── theme/            # 主题配置
│   │       │   │   │   └── Theme.kt
│   │       │   │   └── viewmodel/        # ViewModel
│   │       │   │       ├── PracticeViewModel.kt
│   │       │   │       └── MineViewModel.kt
│   │       │   ├── MainActivity.kt       # 主Activity
│   │       │   └── ZasApplication.kt     # Application类
│   │       ├── res/                      # 资源文件
│   │       │   ├── values/
│   │       │   │   ├── strings.xml
│   │       │   │   ├── colors.xml
│   │       │   │   └── themes.xml
│   │       │   ├── xml/
│   │       │   │   ├── file_paths.xml
│   │       │   │   ├── backup_rules.xml
│   │       │   │   └── data_extraction_rules.xml
│   │       │   └── mipmap-*/             # 应用图标
│   │       └── AndroidManifest.xml       # 清单文件
│   ├── build.gradle.kts                  # 模块构建配置
│   └── proguard-rules.pro                # 混淆规则
├── gradle/
│   └── wrapper/
│       ├── gradle-wrapper.jar            # Gradle Wrapper
│       └── gradle-wrapper.properties     # Wrapper配置
├── build.gradle.kts                      # 项目构建配置
├── settings.gradle.kts                   # 项目设置
├── gradle.properties                     # Gradle属性
├── gradlew                               # Gradle Wrapper脚本(Unix)
├── gradlew.bat                           # Gradle Wrapper脚本(Windows)
├── AI_level3.json                        # 示例题库文件
├── README.md                             # 项目说明
├── USAGE.md                              # 使用说明
├── LICENSE                               # 开源协议
└── .gitignore                            # Git忽略配置
```

## 技术架构

### 数据层 (data/)

- **Model**: 定义数据实体和JSON解析模型
- **DAO**: Room数据库访问接口
- **Database**: Room数据库配置
- **Repository**: 封装数据操作逻辑

### UI层 (ui/)

- **Screen**: Compose界面组件
- **ViewModel**: 界面状态管理
- **Theme**: Material Design 3主题配置

### 导航

使用Navigation Compose实现页面导航：
- PracticeScreen: 刷题首页（题库列表）
- QuestionScreen: 答题界面
- MineScreen: 我的页面

## 核心功能实现

### 1. JSON导入

- 使用SAF（Storage Access Framework）选择文件
- Gson解析JSON数据
- Room数据库批量插入

### 2. 题目展示

- 根据题型动态渲染选项
- 支持单选/多选/判断三种交互
- 实时显示答题结果和解析

### 3. 数据持久化

- Room数据库存储所有数据
- 学习记录自动保存
- 支持数据导出为JSON

### 4. 自动构建

- GitHub Actions自动编译
- 自动创建Release
- 上传APK到Release页面

## 依赖说明

主要依赖库：

- Jetpack Compose: UI框架
- Room: 数据库
- Navigation Compose: 导航
- Gson: JSON解析
- Kotlin Coroutines: 异步处理
- Flow: 响应式数据流

## 构建配置

- minSdk: 24 (Android 7.0)
- targetSdk: 34 (Android 14)
- JDK: 17
- Gradle: 8.2
- Kotlin: 1.9.20
