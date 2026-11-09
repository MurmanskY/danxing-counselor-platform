# 旦行 · 辅导员职业发展数据平台

面向高校辅导员队伍的荣誉记录与职业发展数据平台 Demo。以「个人画像 + 成长路径 + 团队洞察」为核心，帮助辅导员看见自己的育人积累，帮助管理者掌握团队发展全貌。

## 1. 功能概览

| 角色 | 入口 | 内容 |
| --- | --- | --- |
| 普通辅导员 | 首页 | 个人信息横幅（头像/寄语/统计）、六维画像雷达、核心标签、成长轨迹、代表性荣誉、育人成果、工作室、协作网络、智能洞察 |
| 普通辅导员 | 成长路线 / 荣誉殿堂 / 发展日历 / 资源中心 | 发展路径导航、荣誉检索、12 个月发展事项日历、育人成果与工作室资源 |
| 管理员辅导员 | 首页（画像总览） | 筛选搜索、五项 KPI、画像卡片列表（卡片/列表视图、排序）、标签分布、工龄分布、学院分布、智能推荐 |
| 管理员辅导员 | 工作台 | 人员画像、发展机会、我的计划、工作室、收藏、附件、数据治理、账号管理、操作记录 |

## 2. 技术栈与架构

**技术栈**

- 后端：Java 17 · Spring Boot 3.5 · Spring Security（会话 + CSRF）· MyBatis-Plus · Flyway（数据库迁移）· Maven
- 前端：Vue 2.7 · Vue Router · Vite · 原生 CSS 设计系统（复旦藏青主色）
- 存储：PostgreSQL 16（业务数据）· Redis 7（目录缓存）· MinIO（私有对象存储：头像、附件）
- 交付：Docker Compose 一键编排，五个容器全部容器化

**架构**

```
浏览器
  │  http://localhost:8080
  ▼
frontend (Nginx)  ── /api 反向代理 ──►  backend (Spring Boot)
                                         │
           ┌──────────────┬──────────────┼──────────────┐
           ▼              ▼              ▼              ▼
       PostgreSQL       Redis          MinIO        Flyway 迁移
       业务数据          目录缓存        头像/附件      自动建表+种子导入
```

- 首次启动由 Flyway 建表，`Bootstrap` 自动导入 `backend/src/main/resources/seed.json`（579 条演示数据：6 份辅导员画像、190 条荣誉、16+1 个工作室、发展日历事项等），并按 `.env` 创建初始账号。
- 头像/附件为文件流：二进制存 MinIO 私有桶，元数据存 PostgreSQL `file_assets`，`app_users.avatar_file_id` 关联，请勿手工写库。仓库内置 6 位辅导员的演示头像（`backend/src/main/resources/avatars/`），首次启动自动导入并关联账号（admin 复用汤潮头像），未设置头像的账号才会被填充。
- 目录接口带 Redis 缓存（TTL 5 分钟），缓存异常自动回源不中断服务；冷启动/过期瞬间的并发请求由锁机制合并为一次回源（防缓存击穿）；直接改库后需 `docker compose restart redis backend` 才会在前端生效。
- 连接池（Hikari）上限 20；计划任务状态由单条原子 SQL 刷新，并发操作不会写脏状态。
- 会话存 Redis（8 小时），后端重启不丢登录；登录连续失败 5 次将按「账号 + IP」临时锁定 10 分钟。
- 容器默认时区 `Asia/Shanghai`（影响发展日历的"尚未开始"筛选），可通过环境变量 `TZ` 覆盖。

**目录结构**

```
├── backend/            Spring Boot 后端（含 seed.json 种子数据、Flyway 迁移）
├── frontend/           Vue 2 + Vite 前端（含 Nginx 生产配置）
├── infra/minio/        MinIO  pinned 版本的源码构建 Dockerfile
├── demo/               演示打包模板（compose、.env、一键启动脚本、使用说明）
├── scripts/
│   ├── init-env.py     生成本地 .env（随机强密码）
│   ├── export-demo.sh  生成可发给非技术人员的一键演示包
│   └── smoke-test.py   API 冒烟测试
├── profile/            辅导员原始信息 Excel（数据来源，不参与构建）
├── docs/               API 与校验说明
├── compose.yml         生产编排
└── compose.dev.yml     开发编排（暴露 5432/6379/9000 端口）
```

## 3. 快速开始（Docker Compose，推荐）

### 3.1 环境要求

- Docker Engine 24+ / Docker Desktop（Mac、Windows、Linux 均可）
- Docker Compose v2
- Python 3（仅首次生成配置需要）
- 首次构建需联网下载依赖并编译 MinIO，建议预留 8 GB 磁盘、4 GB 内存；全程约 10–25 分钟

### 3.2 部署步骤

```bash
# 1. 克隆仓库
git clone https://github.com/MurmanskY/danxing-counselor-platform.git
cd danxing-counselor-platform

# 2. 生成环境配置（随机密码，写入 .env；Windows 用 python）
python3 scripts/init-env.py

# 3. 构建并启动全部服务
docker compose up -d --build

# 4. 查看状态，等待全部 healthy
docker compose ps
```

启动完成后访问：

- 平台：http://localhost:8080
- MinIO 控制台：http://localhost:9001（账号密码见 `.env` 的 `MINIO_ACCESS_KEY` / `MINIO_SECRET_KEY`）

### 3.3 初始账号

首次启动自动创建（密码在 `.env` 中查看）：

| 账号 | 姓名 | 角色 | 画像类型 |
| --- | --- | --- | --- |
| `admin` | 汤潮 | 管理员辅导员 | 成熟型 88.5 |
| `counselor-p001` | 汤潮 | 普通辅导员 | 成熟型 88.5 |
| `counselor-p002` | 贾颖月 | 普通辅导员 | 成长型 64.0 |
| `counselor-p003` | 王宇 | 普通辅导员 | 起步型 48.3 |
| `counselor-p004` | 陈家栩 | 普通辅导员 | 起步型 42.9 |
| `counselor-p005` | 林晚晴 | 普通辅导员 | 成熟型 80.2 |
| `counselor-p006` | 赵启铭 | 普通辅导员 | 起步型 47.8 |

> 建议体验路径：先用 `admin` 看「辅导员画像总览」，再换 `counselor-p002` 看个人首页，对比两种角色视角。右上角头像 →「账号设置」可上传个人头像。

### 3.4 常用运维命令

```bash
docker compose logs -f backend     # 查看后端日志
docker compose down                # 停止（数据保留在卷中）
docker compose up -d               # 再次启动
docker compose down -v             # 停止并清空数据（下次启动重新初始化）
docker compose -f compose.yml -f compose.dev.yml up -d --build   # 开发模式（暴露数据库等端口）
```

## 4. 打包演示给非技术人员

仓库自带一键演示包模板，测试者**只需安装 Docker Desktop，双击脚本即可**，无需任何技术背景：

```bash
./scripts/export-demo.sh        # 生成 dist/danxing-demo.zip
```

将 `danxing-demo.zip` 发给测试者，解压后：

- Mac：双击「启动-Mac.command」
- Windows：双击「启动-Windows.bat」

脚本会自动检测 Docker、构建环境、等待就绪并打开浏览器；固定演示密码见包内「使用说明.txt」。详见 `demo/` 目录。

## 5. 本地开发

```bash
# 1. 启动基础服务（数据库/缓存/对象存储/后端）
python3 scripts/init-env.py
docker compose -f compose.yml -f compose.dev.yml up -d --build

# 2. 前端热更新开发（另开终端）
cd frontend
npm ci
npm run dev          # http://localhost:5173，/api 经 8080 端口 Nginx 转发到后端
```

前端构建：`cd frontend && npm run build`（产物由 Nginx 容器托管）。
后端本地运行需 Java 17 + Maven 并自行提供全部环境变量，**推荐直接使用 Docker 环境**。

## 6. 数据说明

- `backend/src/main/resources/seed.json`：全量种子数据（579 条），首次启动自动导入；修改后需全新环境或手动同步数据库生效。
- `profile/`：辅导员原始信息 Excel（真实数据来源，含个人信息），仅作存档参考，不参与应用构建。
- 演示数据中的协作网络人数、「较上月」环比等为展示用示例值，接入真实数据源后替换。

## 7. 常见问题

**Q：改了数据库数据，前端没变化？**
目录数据缓存在 Redis。执行 `docker compose restart redis backend` 刷新（会注销所有登录会话）。

**Q：修改 seed.json 后如何生效？**
种子数据仅在空库时导入。需重置：`docker compose down -v` 后重新启动。

**Q：github push 连接超时？**
直连 github.com 可能受限，配置本机代理后重试：`git config http.proxy http://127.0.0.1:7897 && git config https.proxy http://127.0.0.1:7897`。

**Q：Windows 上构建失败？**
确认 Docker Desktop 设置中已启用 WSL2 后端，并给 Docker 分配至少 4 GB 内存。
