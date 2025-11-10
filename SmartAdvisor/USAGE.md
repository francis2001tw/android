# SmartAdvisor - QWEN AI Chat App 使用说明

## ✅ 构建成功！

SmartAdvisor 应用已成功构建，包含以下功能：

### 主要功能

1. **与 QWEN AI 对话**
   - 完整的聊天界面，支持发送消息和接收 AI 响应
   - 流式响应显示，实时查看 AI 生成的内容
   - 消息历史记录管理

2. **Deep Thinking 显示** 🧠
   - 显示 AI 的深度思考过程
   - 可展开/折叠的思考卡片
   - 显示思考时长（秒）
   - 思考内容预览
   - 思考进行中的实时指示器

3. **现代化 UI**
   - 使用 Jetpack Compose 构建
   - Material3 设计风格
   - 流畅的动画效果
   - 响应式布局

## 📦 APK 位置

构建的 APK 文件位于：
```
SmartAdvisor/app-debug.apk
```

## 🚀 安装和运行

### 方法 1: 使用 ADB 安装
```bash
adb install app-debug.apk
```

### 方法 2: 直接传输到设备
1. 将 `app-debug.apk` 文件传输到 Android 设备
2. 在设备上打开文件管理器
3. 点击 APK 文件进行安装
4. 如果提示"未知来源"，请在设置中允许安装

## 🔧 API Key 配置

**✅ 已配置**: 应用已经配置了 QWEN API Key

当前配置的 API Key：`sk-bdf86a39034a49568f57a288077dd416`

如果需要更换 API Key，编辑文件：
```
app/src/main/java/com/example/smartadvisor/ui/viewmodel/ChatViewModel.kt
```

找到第 24 行，替换为你的 QWEN API Key：
```kotlin
apiKey = "sk-your-actual-api-key-here",
```

然后重新构建：
```bash
cd SmartAdvisor
.\gradlew.bat assembleDebug
```

## 📱 使用说明

### 启动应用
1. 打开 SmartAdvisor 应用
2. 应用会自动进入聊天界面

### 发送消息
1. 在底部输入框中输入你的问题
2. 点击发送按钮（✈️图标）
3. AI 会开始思考并生成响应

### 查看 Deep Thinking
1. 当 AI 进行深度思考时，会显示 🧠 Deep Thinking 卡片
2. 点击卡片可以展开/折叠查看完整的思考过程
3. 卡片会显示思考时长
4. 思考进行中会显示加载指示器

### 取消生成
- 在 AI 生成响应时，可以点击"Cancel"按钮停止生成

## 🏗️ 项目结构

```
SmartAdvisor/
├── app/
│   ├── src/main/java/com/example/smartadvisor/
│   │   ├── model/              # 数据模型
│   │   │   ├── Model.kt        # 核心数据类
│   │   │   ├── Message.kt      # 消息模型
│   │   │   └── Conversation.kt # 对话模型
│   │   ├── provider/           # AI 提供商
│   │   │   ├── Provider.kt     # 提供商接口
│   │   │   └── QwenProvider.kt # QWEN 实现
│   │   ├── service/            # 服务层
│   │   │   ├── ChatService.kt  # 聊天服务接口
│   │   │   └── ChatServiceImpl.kt # 服务实现
│   │   ├── repository/         # 数据仓库
│   │   │   └── ConversationRepository.kt
│   │   ├── ui/                 # UI 层
│   │   │   ├── ChatActivity.kt # 主活动
│   │   │   ├── screen/
│   │   │   │   └── ChatScreen.kt # 聊天屏幕
│   │   │   ├── components/
│   │   │   │   └── MessageBubble.kt # 消息气泡（含 Deep Thinking）
│   │   │   ├── viewmodel/
│   │   │   │   └── ChatViewModel.kt # 视图模型
│   │   │   └── theme/          # 主题
│   │   └── storage/            # 存储
│   └── build.gradle.kts        # 构建配置
└── app-debug.apk               # 构建的 APK
```

## 🎨 UI 组件说明

### ChatScreen
- 顶部标题栏：显示"Smart Advisor - QWEN Chat"
- 消息列表：显示所有对话消息
- 底部输入栏：输入消息和发送按钮
- 空状态：欢迎界面

### MessageBubble
- 用户消息：蓝色气泡，右对齐
- AI 消息：灰色气泡，左对齐
- Token 使用统计：显示在消息底部

### DeepThinkingCard
- 🧠 图标：表示深度思考
- 标题："Deep Thinking"
- 时长显示：显示思考秒数
- 展开/折叠按钮：查看完整思考内容
- 预览：折叠时显示前 100 个字符
- 进度指示器：思考进行中显示

## 🔄 重新构建

如果修改了代码，重新构建：

```bash
cd SmartAdvisor
.\gradlew.bat clean assembleDebug
```

APK 会自动复制到项目根目录。

## 📝 注意事项

1. **API Key 安全**: 不要将包含真实 API Key 的代码提交到公共仓库
2. **网络权限**: 应用需要网络权限才能与 QWEN API 通信
3. **构建目录**: 构建输出在临时目录，避免云同步冲突
4. **Deep Thinking**: 确保 QWEN API 支持 reasoning 功能

## 🐛 故障排除

### 应用无法连接到 API
- 检查 API Key 是否正确
- 检查网络连接
- 检查 QWEN API 服务状态

### 构建失败
- 运行 `.\gradlew.bat clean`
- 删除 `.gradle` 文件夹
- 重新运行 `.\gradlew.bat assembleDebug`

### Deep Thinking 不显示
- 确保 QWEN API 返回了 reasoning 数据
- 检查 MessagePart.Reasoning 是否正确解析

## 📚 参考项目

本项目参考了 rikkahub 项目的 UI 设计：
- 位置：`C:\Users\frank\Nutstore\1\Today\SideProject\GITGUB\rikkahub-master`
- 特别参考了 Deep Thinking 显示组件的实现

## 🎯 下一步改进

1. 添加设置界面，允许用户配置 API Key
2. 支持多个对话历史
3. 添加消息编辑和删除功能
4. 支持图片和文件上传
5. 添加对话导出功能
6. 优化 Deep Thinking 显示效果
7. 添加语音输入支持

---

**开发完成时间**: 2025-11-09
**构建状态**: ✅ 成功
**APK 大小**: 约 10-15 MB

