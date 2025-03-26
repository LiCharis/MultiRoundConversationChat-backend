# 项目名称：基于大语言模型的多轮对话问答系统

## 项目简介

本项目旨在开发一个基于大语言模型的多轮对话问答系统，能够处理复杂的上下文对话，并生成自然流畅的响应。该系统通过多种 NLP 技术实现包括语义分析、关键信息提取和上下文管理。

## 功能特性

- **多轮对话管理**：动态管理对话历史，支持语义归纳和语境保持。
- **语义分析**：利用自然语言处理技术进行语义提取和推理。
- **时间衰减权重**：基于时间优化的对话历史权重管理。
- **嵌入向量相似度计算**：使用嵌入向量衡量语义相似性。
- **优化提示词构建**：构建优化提示词，提升模型响应质量。

## 技术栈

- **前端**：Next.js、Ant Design
- **后端**：Spring Boot、Java
- **数据库**：MySQL（用户信息）、MongoDB（聊天记录）
- **NLP框架**：HanLP

## 系统架构

系统采用前后端分离架构，前端通过API接口与后端交互：

1. **用户登录**：验证用户身份，初始化会话
2. **获取对话历史**：从数据库加载历史记录
3. **多轮对话处理**：处理用户输入，生成响应
4. **历史记录保存**：将对话内容存储至数据库

## 快速入门

### 前置要求

- Node.js 14.x 及以上
- Java 8 及以上
- MySQL / MongoDB 数据库

### 安装步骤

1. **克隆仓库**

   ```bash
   git clone https://github.com/LiCharis/MultiRoundConversationChat-backend.git
   ```

2. **启动后端服务**

   ```bash
   cd MultiRoundConversationChat-backend
   ./mvnw spring-boot:run
   ```

3. **启动前端服务**

   ```bash
   cd MultiRoundConversationChat-frontend
   npm install
   next dev
   ```

4. **运行数据库**

    - 确保 MySQL 和 MongoDB 正在运行，并正确配置数据库连接信息

### 使用方法

- 打开浏览器访问 `http://localhost:3000`
- 注册并登录账号，开始使用聊天系统



## 贡献指南

欢迎贡献者！请遵循以下步骤：

1. Fork 仓库
2. 创建你的特性分支 (`git checkout -b feature/AmazingFeature`)
3. 提交变更 (`git commit -m 'Add some AmazingFeature'`)
4. 推送到分支 (`git push origin feature/AmazingFeature`)
5. 打开 Pull Request

## 许可证

本项目采用 [MIT 许可证](LICENSE) - 请查看 LICENSE 文件以了解更多详情。

## 联系我们

如果有任何问题或建议，请联系 [jiayouali@gmail.com]。

---

感谢使用我们的项目！请在使用过程中随时提出改善建议。我们希望我们的系统能为您提供出色的对话体验。