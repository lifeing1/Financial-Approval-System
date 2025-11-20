# AO 办公自动化系统

一个基于 **Spring Boot 3** + **Vue 3** + **Flowable 7** 的现代化办公自动化系统，专注于出差审批和备用金申请管理。

## ✨ 特性

- 🚀 **现代化技术栈**: Spring Boot 3 + Vue 3 + Vite
- 🎨 **美观界面**: Naive UI 组件库，主题色 #18A058
- 🔐 **权限管理**: 基于 Sa-Token 的 RBAC 权限体系
- 📊 **数据可视化**: ECharts 图表展示统计数据
- 🔄 **工作流引擎**: Flowable 7 支持灵活的审批流程
- 📱 **响应式设计**: 适配各种屏幕尺寸
- 📦 **前后端分离**: RESTful API 设计

## 🛠️ 技术栈

### 后端
- Spring Boot 3.1.12
- MyBatis-Plus 3.5.8
- Sa-Token 1.37.0
- Redis
- Flowable 7.1.0
- MySQL 8.0+
- Knife4j 4.3.0

### 前端
- Vue 3.4.15
- Vite 5.0.8
- Naive UI 2.38.0
- Pinia 2.1.7
- Vue Router 4.2.5
- ECharts 5.4.3

## 📦 功能模块

### 核心功能
- ✅ 出差审批管理
  - 出差申请（支持草稿）
  - 动态费用明细
  - 多级审批流程
  - 我的申请/待办/已办
  
- ✅ 备用金申请管理
  - 备用金申请
  - 审批流程
  - 列表查询
  
- ✅ 工作流管理
  - BPMN 流程部署
  - 流程实例管理
  - 任务处理
  
- ✅ 数据统计
  - 出差统计（部门/月份/人员）
  - 备用金统计
  - 可视化图表

### 基础功能
- ✅ 用户登录/登出
- ✅ 角色权限管理
- ✅ 部门管理
- ✅ 文件上传下载
- ✅ 个人中心

## 🚀 快速开始

### 环境要求
- JDK 17+
- Maven 3.8+
- MySQL 8.0+
- Redis
- Node.js 18+

### 1. 克隆项目
```bash
git clone <repository-url>
cd Approval
```

### 2. 初始化数据库
```bash
# 创建数据库
mysql -u root -p
CREATE DATABASE approval_system DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

# 执行建表脚本
cd backend/database
mysql -u root -p approval_system < schema.sql
mysql -u root -p approval_system < init_data.sql
```

### 3. 配置后端
编辑 `backend/src/main/resources/application.yml`:
```yaml
spring:
  datasource:
    username: root
    password: your_password
```

### 4. 启动后端
```bash
./start-backend.sh
```
或手动启动：
```bash
cd backend
mvn clean package -DskipTests
java -jar target/approval-backend-0.0.1-SNAPSHOT.jar
```

### 5. 启动前端
```bash
./start-frontend.sh
```
或手动启动：
```bash
cd frontend
npm install
npm run dev
```

### 6. 访问系统
- 前端地址: http://localhost:3000
- 后端接口: http://localhost:8080/api
- 接口文档: http://localhost:8080/doc.html

## 👤 测试账号

| 用户名 | 密码 | 角色 | 部门 |
|-------|------|-----|-----|
| admin | 123456 | 超级管理员 | 总经办 |
| zhangsan | 123456 | 部门经理 | 技术部 |
| lisi | 123456 | 人事经理 | 人事部 |
| wangwu | 123456 | 财务经理 | 财务部 |

## 📁 项目结构

```
Approval/
├── backend/                 # 后端项目
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/
│   │   │   │   └── com/approval/
│   │   │   │       ├── config/      # 配置类
│   │   │   │       ├── controller/  # 控制器
│   │   │   │       ├── service/     # 服务层
│   │   │   │       ├── mapper/      # 数据访问层
│   │   │   │       ├── entity/      # 实体类
│   │   │   │       └── common/      # 通用类
│   │   │   └── resources/
│   │   │       ├── application.yml  # 配置文件
│   │   │       └── processes/       # BPMN 流程文件
│   │   └── database/
│   │       ├── schema.sql           # 建表脚本
│   │       └── init_data.sql        # 初始数据
│   └── pom.xml
│
├── frontend/                # 前端项目
│   ├── src/
│   │   ├── api/            # API 接口
│   │   ├── components/     # 通用组件
│   │   ├── layouts/        # 布局组件
│   │   ├── router/         # 路由配置
│   │   ├── stores/         # 状态管理
│   │   ├── views/          # 页面组件
│   │   └── App.vue
│   ├── package.json
│   └── vite.config.js
│
├── start-backend.sh         # 后端启动脚本
├── start-frontend.sh        # 前端启动脚本
├── DEPLOY.md                # 部署文档
├── SUMMARY.md               # 开发总结
└── README.md                # 项目说明
```

## 📖 文档

- [部署文档](./DEPLOY.md) - 详细的部署和配置说明
- [开发总结](./SUMMARY.md) - 项目开发总结和技术要点
- [接口文档](http://localhost:8080/api/doc.html) - 在线接口文档（需启动后端）

## 🎯 主要功能截图

### 登录页面
- 用户登录
- 记住密码

### 工作台
- 待办事项
- 数据概览

### 出差审批
- 出差申请表单（动态费用明细）
- 我的申请列表
- 待办审批
- 已办审批

### 备用金管理
- 备用金申请
- 审批流程

### 数据统计
- 出差统计图表
- 备用金统计图表

## 🔧 开发

### 后端开发
```bash
cd backend
mvn spring-boot:run
```

### 前端开发
```bash
cd frontend
npm run dev
```

### 打包部署
```bash
# 前端打包
cd frontend
npm run build

# 后端打包
cd backend
mvn clean package -DskipTests
```

## 📝 待优化功能

- [ ] 审批流程可视化展示
- [ ] 消息通知功能（邮件/站内信）
- [ ] 数据导出功能（Excel）
- [ ] 移动端适配
- [ ] 接口限流和安全加固

## 🤝 贡献

欢迎提交 Issue 和 Pull Request！

## 📄 许可证

MIT License

## 📧 联系方式

如有问题，请联系技术支持团队。

---

⭐ 如果这个项目对你有帮助，欢迎 Star！
