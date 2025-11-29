#!/bin/bash

echo "初始化增爱刷项目..."

# 检查是否已经是git仓库
if [ ! -d ".git" ]; then
    echo "初始化Git仓库..."
    git init
    git add .
    git commit -m "Initial commit: 增爱刷 Android刷题APP"
    echo ""
    echo "✅ Git仓库初始化完成"
    echo ""
    echo "接下来的步骤："
    echo "1. 在GitHub创建一个新仓库"
    echo "2. 运行以下命令关联远程仓库："
    echo "   git remote add origin https://github.com/YOUR_USERNAME/zas.git"
    echo "   git branch -M main"
    echo "   git push -u origin main"
    echo ""
    echo "3. 创建第一个release标签："
    echo "   git tag -a v1.0.0 -m 'Release version 1.0.0'"
    echo "   git push origin v1.0.0"
    echo ""
    echo "4. GitHub Actions会自动构建并发布APK"
else
    echo "⚠️  Git仓库已存在"
fi

echo ""
echo "📦 项目结构已创建完成！"
echo ""
echo "⚠️  注意事项："
echo "1. 需要在GitHub Actions首次运行时自动下载gradle-wrapper.jar"
echo "2. 应用图标使用默认图标，可后续替换"
echo "3. Release构建使用debug签名，生产环境需配置正式签名"
echo ""
echo "📖 更多信息请查看："
echo "   - README.md: 项目说明"
echo "   - USAGE.md: 使用说明"
echo "   - PROJECT_STRUCTURE.md: 项目结构说明"
