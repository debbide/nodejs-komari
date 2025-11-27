#!/bin/bash

# Komari 配置测试脚本
# 用于验证 Komari Agent 是否正确配置和运行

set -e

# 颜色定义
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

echo -e "${BLUE}=======================================${NC}"
echo -e "${BLUE}  Komari 配置测试脚本${NC}"
echo -e "${BLUE}=======================================${NC}"
echo ""

# 检查环境变量
echo -e "${YELLOW}[1/5] 检查环境变量...${NC}"

if [ -f ".env" ]; then
    echo -e "${GREEN}✓${NC} 找到 .env 文件"
    source .env
else
    echo -e "${RED}✗${NC} 未找到 .env 文件"
    echo -e "${YELLOW}提示: 请复制 .env.example 到 .env 并配置${NC}"
fi

if [ -z "$KOMARI_ENDPOINT" ]; then
    echo -e "${RED}✗${NC} KOMARI_ENDPOINT 未设置"
    KOMARI_MISSING=1
else
    echo -e "${GREEN}✓${NC} KOMARI_ENDPOINT: $KOMARI_ENDPOINT"
fi

if [ -z "$KOMARI_TOKEN" ]; then
    echo -e "${RED}✗${NC} KOMARI_TOKEN 未设置"
    KOMARI_MISSING=1
else
    echo -e "${GREEN}✓${NC} KOMARI_TOKEN: ${KOMARI_TOKEN:0:10}..."
fi

if [ "$KOMARI_MISSING" = "1" ]; then
    echo -e "${RED}配置不完整，无法继续测试${NC}"
    exit 1
fi

echo ""

# 检查网络连接
echo -e "${YELLOW}[2/5] 检查网络连接...${NC}"

if curl -s --head --connect-timeout 5 "https://github.com" > /dev/null; then
    echo -e "${GREEN}✓${NC} 可以访问 GitHub"
else
    echo -e "${RED}✗${NC} 无法访问 GitHub"
    echo -e "${YELLOW}提示: 可能需要代理或使用镜像${NC}"
fi

if curl -s --head --connect-timeout 5 "$KOMARI_ENDPOINT" > /dev/null; then
    echo -e "${GREEN}✓${NC} 可以访问 Komari 服务器"
else
    echo -e "${YELLOW}⚠${NC} 无法访问 Komari 服务器"
    echo -e "${YELLOW}提示: 请检查 KOMARI_ENDPOINT 是否正确${NC}"
fi

echo ""

# 检查系统架构
echo -e "${YELLOW}[3/5] 检查系统架构...${NC}"

ARCH=$(uname -m)
case $ARCH in
    x86_64)
        ARCH_NAME="amd64"
        echo -e "${GREEN}✓${NC} 架构: $ARCH (支持)"
        ;;
    aarch64|arm64)
        ARCH_NAME="arm64"
        echo -e "${GREEN}✓${NC} 架构: $ARCH (支持)"
        ;;
    *)
        echo -e "${RED}✗${NC} 架构: $ARCH (不支持)"
        exit 1
        ;;
esac

OS_NAME="linux"
echo -e "${GREEN}✓${NC} 操作系统: $OS_NAME"

echo ""

# 测试下载 Komari Agent
echo -e "${YELLOW}[4/5] 测试下载 Komari Agent...${NC}"

KOMARI_URL="https://github.com/komari-monitor/komari-agent/releases/latest/download/komari-agent-${OS_NAME}-${ARCH_NAME}"
TEMP_FILE="/tmp/komari-agent-test"

echo -e "下载地址: $KOMARI_URL"

if curl -L -o "$TEMP_FILE" "$KOMARI_URL" 2>/dev/null; then
    FILE_SIZE=$(stat -c%s "$TEMP_FILE" 2>/dev/null || stat -f%z "$TEMP_FILE" 2>/dev/null)
    if [ "$FILE_SIZE" -gt 0 ]; then
        echo -e "${GREEN}✓${NC} 下载成功 (大小: $FILE_SIZE 字节)"
        chmod +x "$TEMP_FILE"
        echo -e "${GREEN}✓${NC} 设置可执行权限成功"
    else
        echo -e "${RED}✗${NC} 下载的文件为空"
        rm -f "$TEMP_FILE"
        exit 1
    fi
else
    echo -e "${RED}✗${NC} 下载失败"
    exit 1
fi

echo ""

# 测试运行 Komari Agent
echo -e "${YELLOW}[5/5] 测试运行 Komari Agent...${NC}"

echo -e "执行命令: $TEMP_FILE -e $KOMARI_ENDPOINT -t ${KOMARI_TOKEN:0:10}..."

# 后台运行 Komari Agent
$TEMP_FILE -e "$KOMARI_ENDPOINT" -t "$KOMARI_TOKEN" > /tmp/komari-test.log 2>&1 &
KOMARI_PID=$!

echo -e "${GREEN}✓${NC} Komari Agent 已启动 (PID: $KOMARI_PID)"

# 等待 3 秒
echo -e "等待 3 秒检查运行状态..."
sleep 3

# 检查进程是否还在运行
if ps -p $KOMARI_PID > /dev/null; then
    echo -e "${GREEN}✓${NC} Komari Agent 正常运行"

    # 停止进程
    kill $KOMARI_PID 2>/dev/null
    echo -e "${GREEN}✓${NC} 已停止测试进程"
else
    echo -e "${RED}✗${NC} Komari Agent 启动失败"
    echo -e "${YELLOW}日志内容:${NC}"
    cat /tmp/komari-test.log
    rm -f "$TEMP_FILE"
    exit 1
fi

# 清理
rm -f "$TEMP_FILE"
rm -f "/tmp/komari-test.log"

echo ""
echo -e "${BLUE}=======================================${NC}"
echo -e "${GREEN}✓ 所有测试通过！${NC}"
echo -e "${BLUE}=======================================${NC}"
echo ""
echo -e "${GREEN}配置正确，可以开始使用：${NC}"
echo -e "  Node.js: ${BLUE}node app-komari.js${NC}"
echo -e "  Java:    ${BLUE}java -jar NanoLimbo.jar${NC}"
echo ""
