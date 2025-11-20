#!/bin/bash

# 启动前端服务
echo "=========================================="
echo "正在启动前端服务..."
echo "=========================================="

cd frontend

# 检查是否已安装 Node.js
if ! command -v node &> /dev/null; then
    echo "错误: 未检测到 Node.js，请先安装 Node.js"
    exit 1
fi

# 检查是否已安装依赖
if [ ! -d "node_modules" ]; then
    echo "正在安装依赖..."
    npm install
fi

# 启动开发服务器
echo "正在启动开发服务器..."
npm run dev

echo "前端服务已启动，访问地址: http://localhost:5173"
