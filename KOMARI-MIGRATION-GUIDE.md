# Komari 替换哪吒监控 - 使用说明

## 📋 概述

本项目已将哪吒监控替换为 **Komari 监控系统**，同时**保留所有代理功能**（Argo、HY2、TUIC、REALITY等）。

### ✨ 主要变化

- ✅ **监控功能**: 哪吒 → Komari
- ✅ **代理功能**: 完全保留（Argo、HY2、TUIC、REALITY等）
- ✅ **下载源**: 使用官方 GitHub Release (安全可靠)
- ✅ **配置方式**: 简化为两个参数 `-e` 和 `-t`

---

## 🚀 快速开始

### 1. Node.js 版本

#### 安装依赖

```bash
npm install express axios
```

#### 配置环境变量

复制并编辑配置文件：

```bash
cp .env.example .env
nano .env
```

**必需配置：**

```bash
# Komari 监控
KOMARI_ENDPOINT=https://your-komari-server.com
KOMARI_TOKEN=your-token-here

# 其他配置根据需要调整
UUID=your-uuid-here
```

#### 运行

```bash
node app-komari.js
```

---

### 2. Java 版本 (NanoLimbo)

#### 配置环境变量

创建 `.env` 文件或设置系统环境变量：

```bash
# .env 文件
KOMARI_ENDPOINT=https://your-komari-server.com
KOMARI_TOKEN=your-token-here
UUID=your-uuid-here
```

或使用命令行：

```bash
export KOMARI_ENDPOINT="https://your-komari-server.com"
export KOMARI_TOKEN="your-token-here"
```

#### 编译和运行

```bash
# 编译
./gradlew shadowJar

# 运行
java -jar build/libs/NanoLimbo-*.jar
```

---

## 🔧 配置说明

### Komari 参数

| 参数 | 说明 | 示例 |
|------|------|------|
| `KOMARI_ENDPOINT` | Komari 服务器地址（完整URL） | `https://km.bcbc.pp.ua` |
| `KOMARI_TOKEN` | Komari 认证令牌 | `your-token-here` |

### 如何获取 Komari 配置

1. **部署 Komari Server**
   ```bash
   # 参考 Komari 官方文档
   https://github.com/komari-monitor/komari
   ```

2. **生成 Token**
   - 登录 Komari 管理面板
   - 添加新的 Agent
   - 获取生成的 Token

3. **填写配置**
   ```bash
   KOMARI_ENDPOINT=https://your-komari-server.com
   KOMARI_TOKEN=generated-token
   ```

---

## 📊 功能对比

### 监控功能

| 功能 | 哪吒 | Komari |
|------|------|--------|
| **CPU 监控** | ✅ | ✅ |
| **内存监控** | ✅ | ✅ |
| **磁盘监控** | ✅ | ✅ |
| **网络监控** | ✅ | ✅ |
| **进程监控** | ✅ | ✅ |
| **下载源** | ❌ 不明来源 | ✅ 官方 GitHub |
| **配置方式** | v0: 命令行<br>v1: YAML | 命令行 |
| **参数格式** | `-s SERVER -p KEY` | `-e ENDPOINT -t TOKEN` |

### 保留的代理功能

所有原有的代理功能完全保留：

- ✅ **Argo 隧道** (Cloudflare Tunnel)
- ✅ **HY2** (Hysteria 2)
- ✅ **TUIC**
- ✅ **REALITY**
- ✅ **xr-ay**

---

## 🔍 架构说明

### Node.js 版本

```
启动流程:
1. 下载并启动 Komari Agent (监控)
2. 下载并启动 web (xr-ay 代理)
3. 下载并启动 bot (cloudflared Argo 隧道)
4. 生成并暴露订阅链接
```

### Java 版本

```
启动流程:
1. 下载并启动 s-box (代理功能: Argo, HY2, TUIC等)
   └─ 不传递哪吒环境变量
2. 下载并启动 Komari Agent (监控功能)
   └─ 使用 -e 和 -t 参数
3. 启动 Minecraft 服务器 (NanoLimbo)
```

---

## 🧪 测试验证

### 检查 Komari Agent 是否运行

```bash
# Linux/macOS
ps aux | grep komari

# 预期输出
/tmp/komari-agent -e https://xxx -t xxx
```

### 检查代理服务是否运行

```bash
# 检查 s-box (Java) 或 web/bot (Node.js)
ps aux | grep -E "sbx|web|bot"
```

### 验证监控数据

登录您的 Komari 管理面板，查看是否收到监控数据。

---

## 🐛 故障排查

### 问题 1: Komari Agent 未启动

**现象：**
```
KOMARI_ENDPOINT or KOMARI_TOKEN not set, skipping Komari agent
```

**解决方法：**
1. 检查 `.env` 文件是否存在
2. 确认 `KOMARI_ENDPOINT` 和 `KOMARI_TOKEN` 已设置
3. 确保环境变量已正确加载

### 问题 2: 下载失败

**现象：**
```
Download komari-agent-linux-amd64 failed
```

**解决方法：**
1. 检查网络连接
2. 尝试手动下载：
   ```bash
   wget https://github.com/komari-monitor/komari-agent/releases/latest/download/komari-agent-linux-amd64
   ```
3. 如果无法访问 GitHub，使用代理或镜像

### 问题 3: 权限不足

**现象：**
```
Failed to set executable permission
```

**解决方法：**
```bash
# 手动设置权限
chmod +x /tmp/komari-agent

# 或使用 root 权限运行
sudo node app-komari.js
# 或
sudo java -jar NanoLimbo.jar
```

### 问题 4: 代理功能失效

**现象：**
- Argo 隧道未启动
- xr-ay 未运行

**解决方法：**
1. 检查 `ARGO_*` 环境变量配置
2. 查看 s-box 或 web/bot 进程是否运行
3. 检查端口是否被占用

---

## 📝 环境变量完整列表

### Komari 监控

| 变量名 | 必需 | 说明 | 示例 |
|--------|------|------|------|
| `KOMARI_ENDPOINT` | ✅ | Komari 服务器地址 | `https://km.example.com` |
| `KOMARI_TOKEN` | ✅ | Komari 认证令牌 | `your-token` |

### 代理服务

| 变量名 | 必需 | 说明 | 示例 |
|--------|------|------|------|
| `ARGO_PORT` | ❌ | Argo 端口 | `8001` |
| `ARGO_DOMAIN` | ❌ | Argo 域名 | `example.com` |
| `ARGO_AUTH` | ❌ | Argo 认证 | token 或 JSON |
| `HY2_PORT` | ❌ | Hysteria2 端口 | - |
| `TUIC_PORT` | ❌ | TUIC 端口 | - |
| `REALITY_PORT` | ❌ | REALITY 端口 | - |

### 通用配置

| 变量名 | 必需 | 说明 | 示例 |
|--------|------|------|------|
| `UUID` | ❌ | 唯一标识符 | `xxx-xxx-xxx` |
| `PORT` | ❌ | HTTP 端口 (Node.js) | `3000` |
| `FILE_PATH` | ❌ | 文件路径 | `./tmp` |
| `CFIP` | ❌ | Cloudflare IP | `cdns.doon.eu.org` |
| `CFPORT` | ❌ | Cloudflare 端口 | `443` |
| `NAME` | ❌ | 节点名称 | `My-Node` |

---

## 🔐 安全说明

### ⚠️ 重要警告

**原代码安全问题：**
- ❌ 从不明来源 (`ssss.nyc.mn`) 下载二进制文件
- ❌ 无文件完整性验证
- ❌ 硬编码凭据

**改进后：**
- ✅ Komari: 从官方 GitHub Release 下载
- ✅ 可验证文件来源
- ✅ 通过环境变量配置凭据

**建议：**
1. 定期更新 Komari Agent
2. 使用强密码/令牌
3. 不要在公共环境暴露 `.env` 文件
4. 考虑完全替换 s-box（如果可能）

---

## 📚 相关链接

- [Komari 官方仓库](https://github.com/komari-monitor/komari)
- [Komari Agent 官方仓库](https://github.com/komari-monitor/komari-agent)
- [原 NanoLimbo 项目](https://github.com/Nan1t/NanoLimbo)

---

## 🤝 支持

如有问题，请：
1. 检查本文档的故障排查部分
2. 查看 Komari 官方文档
3. 提交 Issue

---

## 📄 许可证

- NanoLimbo: GNU General Public License v3.0
- Komari: 请查看官方仓库

---

**最后更新**: 2025-01-27
