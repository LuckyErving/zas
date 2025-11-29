# 应用图标占位说明

本目录需要放置Android应用图标文件。

## 所需文件

每个mipmap目录需要以下文件：

### mipmap-mdpi (48x48)
- ic_launcher.png
- ic_launcher_round.png

### mipmap-hdpi (72x72)
- ic_launcher.png
- ic_launcher_round.png

### mipmap-xhdpi (96x96)
- ic_launcher.png
- ic_launcher_round.png

### mipmap-xxhdpi (144x144)
- ic_launcher.png
- ic_launcher_round.png

### mipmap-xxxhdpi (192x192)
- ic_launcher.png
- ic_launcher_round.png

## 生成图标

推荐使用以下工具生成图标：

1. [Android Asset Studio](https://romannurik.github.io/AndroidAssetStudio/icons-launcher.html)
2. [App Icon Generator](https://www.appicon.co/)
3. Android Studio内置的Image Asset工具

## 临时方案

在正式图标准备好之前，项目会使用Android默认图标。

首次构建时，GitHub Actions会处理缺失的图标问题。
