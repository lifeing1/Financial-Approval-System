#!/bin/bash

# 启动后端服务
echo "=========================================="
echo "正在启动后端服务..."
echo "=========================================="

cd backend

# 检查是否已安装 Maven
if ! command -v mvn &> /dev/null; then
    echo "错误: 未检测到 Maven，请先安装 Maven"
    exit 1
fi

# 清理并打包
echo "正在编译打包..."
mvn clean package -DskipTests

# 启动服务
echo "正在启动服务..."
java -jar target/approval-backend-0.0.1-SNAPSHOT.jar

echo "后端服务已启动，访问地址: http://localhost:8080/api"
echo "接口文档地址: http://localhost:8080/api/doc.html"
