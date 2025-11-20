# AO 办公自动化系统部署文档

## 一、系统简介

本系统是一个基于 Spring Boot + Vue 3 的公司内部办公自动化系统，主要用于出差审批和备用金申请审批。

### 技术栈

**后端**:
- Spring Boot 3.1.12
- MyBatis-Plus 3.5.8
- Sa-Token 1.37.0
- Redis
- Flowable 7.1.0
- MySQL 8.0+
- Knife4j 4.3.0

**前端**:
- Vue 3.4.15
- Vite 5.0.8
- Naive UI 2.38.0
- Pinia 2.1.7
- ECharts 5.4.3

## 二、环境准备

### 1. 安装 JDK 17+
```bash
java -version
```

### 2. 安装 Maven 3.8+
```bash
mvn -version
```

### 3. 安装 MySQL 8.0+
```bash
mysql --version
```

### 4. 安装 Redis
```bash
redis-server --version
```

### 5. 安装 Node.js 18+
```bash
node -v
npm -v
```

## 三、数据库初始化

### 1. 创建数据库
```sql
CREATE DATABASE approval_system DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

### 2. 执行建表脚本
```bash
cd backend/database
mysql -u root -p approval_system < schema.sql
```

### 3. 初始化数据
```bash
mysql -u root -p approval_system < init_data.sql
```

### 测试账号
- 管理员: admin / 123456
- 员工1: zhangsan / 123456
- 员工2: lisi / 123456
- 员工3: wangwu / 123456

## 四、后端部署

### 1. 修改配置文件
编辑 `backend/src/main/resources/application.yml`:

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/approval_system?...
    username: root
    password: your_password
  
  data:
    redis:
      host: localhost
      port: 6379

file:
  upload-path: /data/approval/uploads/
```

### 2. 创建文件上传目录
```bash
mkdir -p /data/approval/uploads
```

### 3. 启动后端服务

#### 方式一：使用脚本启动
```bash
./start-backend.sh
```

#### 方式二：手动启动
```bash
cd backend
mvn clean package -DskipTests
java -jar target/approval-backend-0.0.1-SNAPSHOT.jar
```

### 4. 验证后端服务
访问接口文档: http://localhost:8080/doc.html

## 五、前端部署

### 1. 安装依赖
```bash
cd frontend
npm install
```

### 2. 修改配置（可选）
编辑 `frontend/src/api/request.js`，修改后端接口地址。

### 3. 启动前端服务

#### 方式一：使用脚本启动
```bash
./start-frontend.sh
```

#### 方式二：手动启动
```bash
cd frontend
npm run dev
```

### 4. 访问系统
浏览器访问: http://localhost:3000

## 六、生产环境部署

### 1. 前端打包
```bash
cd frontend
npm run build
```
打包后的文件在 `frontend/dist` 目录下。

### 2. Nginx 配置示例
```nginx
server {
    listen 80;
    server_name your-domain.com;
    
    # 前端静态文件
    location / {
        root /path/to/frontend/dist;
        try_files $uri $uri/ /index.html;
    }
    
    # 后端接口代理
    location /api {
        proxy_pass http://localhost:8080;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
    }
}
```

### 3. 后端服务配置
```bash
# 使用 systemd 管理服务
sudo vim /etc/systemd/system/approval-backend.service
```

```ini
[Unit]
Description=Approval System Backend
After=network.target

[Service]
Type=simple
User=your-user
WorkingDirectory=/path/to/backend
ExecStart=/usr/bin/java -jar target/approval-backend-0.0.1-SNAPSHOT.jar
Restart=always

[Install]
WantedBy=multi-user.target
```

```bash
sudo systemctl daemon-reload
sudo systemctl start approval-backend
sudo systemctl enable approval-backend
```

## 七、常见问题

### 1. 数据库连接失败
- 检查 MySQL 是否启动
- 检查数据库用户名密码是否正确
- 检查数据库是否已创建

### 2. Redis 连接失败
- 检查 Redis 是否启动: `redis-cli ping`
- 检查 Redis 配置是否正确

### 3. 文件上传失败
- 检查上传目录是否存在
- 检查目录权限是否正确: `chmod 755 /data/approval/uploads`

### 4. 前端无法访问后端
- 检查后端服务是否启动
- 检查跨域配置是否正确
- 检查防火墙设置

## 八、维护建议

### 1. 日志管理
- 后端日志位置: `backend/logs/`
- 建议配置日志轮转

### 2. 数据库备份
```bash
# 每天凌晨 2 点自动备份
0 2 * * * mysqldump -u root -p approval_system > /backup/approval_$(date +\%Y\%m\%d).sql
```

### 3. 监控建议
- 使用 Prometheus + Grafana 监控系统性能
- 配置告警通知

## 九、联系方式

如有问题，请联系技术支持团队。
