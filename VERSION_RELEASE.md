# 版本更新指南

本文档说明如何发布新版本的增爱刷APP。

## 📋 版本号规则

遵循语义化版本号规范：`major.minor.patch`

- **major**: 重大功能变更或不兼容更新
- **minor**: 新增功能，向后兼容
- **patch**: Bug修复，向后兼容

示例：
- `1.0.0` - 首个正式版本
- `1.1.0` - 新增功能
- `1.0.1` - Bug修复

## 🔄 发布新版本步骤

### 步骤1: 更新版本号

编辑 `app/build.gradle.kts`：

```kotlin
defaultConfig {
    applicationId = "com.zengaishua.app"
    minSdk = 24
    targetSdk = 34
    versionCode = 2              // 每次发布递增
    versionName = "1.1.0"        // 新版本号
    ...
}
```

**注意**:
- `versionCode` 必须递增（整数）
- `versionName` 是用户看到的版本号（字符串）

### 步骤2: 提交更改

```bash
git add app/build.gradle.kts
git commit -m "Bump version to 1.1.0"
git push origin main
```

### 步骤3: 创建版本标签

```bash
# 创建带注释的标签
git tag -a v1.1.0 -m "Release version 1.1.0

新功能:
- 功能1描述
- 功能2描述

Bug修复:
- 修复问题1
- 修复问题2
"

# 推送标签
git push origin v1.1.0
```

### 步骤4: 等待自动构建

1. 访问GitHub仓库的Actions页面
2. 查看构建进度
3. 构建成功后，新版本会自动发布到Releases

### 步骤5: 编辑Release说明（可选）

在GitHub Releases页面可以编辑自动生成的发布说明，添加更详细的更新内容。

## 📝 版本发布检查清单

发布前请检查：

- [ ] 版本号已更新（versionCode和versionName）
- [ ] 代码已提交并推送
- [ ] 本地测试通过
- [ ] 更新日志已准备
- [ ] 已创建版本标签
- [ ] GitHub Actions构建成功
- [ ] APK可正常安装和运行

## 🔥 紧急修复版本

如果需要快速发布Bug修复版本：

```bash
# 1. 修复代码
# 2. 更新版本号（只增加patch）
# 3. 快速提交
git add .
git commit -m "Hotfix: 修复严重Bug"
git push

# 4. 创建修复版本标签
git tag -a v1.0.1 -m "Hotfix: 修复严重Bug"
git push origin v1.0.1
```

## 📦 测试版本（Beta）

如果需要发布测试版本：

```bash
# 使用beta标签
git tag -a v1.1.0-beta.1 -m "Beta version 1.1.0-beta.1"
git push origin v1.1.0-beta.1
```

在 `app/build.gradle.kts` 中设置：
```kotlin
versionName = "1.1.0-beta.1"
```

## 🗑️ 删除错误的版本

如果发布了错误的版本：

```bash
# 删除本地标签
git tag -d v1.1.0

# 删除远程标签
git push origin :refs/tags/v1.1.0
```

然后在GitHub上手动删除对应的Release。

## 🔍 查看版本历史

```bash
# 查看所有标签
git tag

# 查看标签详情
git show v1.0.0

# 查看标签之间的差异
git diff v1.0.0 v1.1.0
```

## 📊 版本规划建议

### 1.0.x 系列 - 稳定版本
- `1.0.0` - 首个正式版本
- `1.0.1` - Bug修复
- `1.0.2` - 更多Bug修复

### 1.1.x 系列 - 功能增强
- `1.1.0` - 新增功能A
- `1.1.1` - Bug修复

### 2.0.x 系列 - 重大更新
- `2.0.0` - 重大功能改进或架构变更

## ⚠️ 注意事项

1. **向后兼容**: 尽量保持数据库结构和文件格式的兼容性
2. **数据迁移**: 如果数据库结构变化，需要提供迁移方案
3. **测试充分**: 发布前充分测试新功能和修复
4. **文档更新**: 同步更新README和使用文档
5. **用户通知**: 在Release说明中详细说明更新内容

## 🎯 自动化改进建议

未来可以考虑：

1. **自动化版本号**: 使用脚本自动更新版本号
2. **Changelog生成**: 从git commit自动生成更新日志
3. **自动化测试**: 在CI中运行单元测试和UI测试
4. **多渠道发布**: 同时发布到应用商店

## 示例发布流程

完整的1.1.0版本发布示例：

```bash
# 1. 开发新功能
git checkout -b feature/new-feature
# ... 开发工作 ...
git commit -m "Add new feature"

# 2. 合并到主分支
git checkout main
git merge feature/new-feature

# 3. 更新版本号
# 编辑 app/build.gradle.kts
# versionCode = 2
# versionName = "1.1.0"

# 4. 提交版本更新
git add app/build.gradle.kts
git commit -m "Bump version to 1.1.0"
git push origin main

# 5. 创建版本标签
git tag -a v1.1.0 -m "Release version 1.1.0

新功能:
- 添加了XX功能
- 优化了YY体验

Bug修复:
- 修复了ZZ问题
"

# 6. 推送标签触发构建
git push origin v1.1.0

# 7. 等待GitHub Actions完成构建
# 8. 在GitHub Releases页面验证和完善发布说明
```

---

遵循以上流程，您就可以轻松地发布和管理增爱刷的各个版本了！
