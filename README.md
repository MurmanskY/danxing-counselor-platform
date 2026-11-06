# 旦行辅导员职业发展数据平台

Java 17 + Spring Boot 3.5 + Vue 2 重构版，全栈 Demo 1.0。

采用复旦蓝白色系，保留首页、荣誉殿堂、成长路线、发展日历、资源中心等入口。工作台包含人员画像、历史发展机会、计划任务、工作室、收藏、私有附件、数据治理、账号管理和操作记录。

## 1. 快速启动

准备 Docker Engine / Docker Desktop、Docker Compose v2 和 Python 3。首次构建需要联网下载依赖，并编译 MinIO，建议预留至少 4 GB 可用内存及 8 GB 磁盘空间。

在项目根目录执行：

```bash
python3 scripts/init-env.py
docker compose up -d --build
docker compose ps
```

Windows 可将 `python3` 改为 `python`，其他命令相同。

- 平台：http://localhost:8080
- 账号：`admin`
- 密码：打开新生成的 `.env`，查看 `ADMIN_PASSWORD`。
- MinIO 控制台：http://localhost:9001，账号、密码为 `.env` 中的 `MINIO_ACCESS_KEY`、`MINIO_SECRET_KEY`。
- 首次启动由 Flyway 建表，自动导入 567 条原始资料，并创建管理员辅导员及普通辅导员账号。
- 默认初始登录：汤潮（P001）的管理员辅导员账号 `admin` 和普通辅导员账号 `counselor-p001` 相互独立；其他普通辅导员为贾颖月（P002）`counselor-p002`、王宇（P003）`counselor-p003`、陈家栩（P004）`counselor-p004`。首次运行 `scripts/init-env.py` 时会分别生成账号密码，在项目根目录 `.env` 中查看对应的 `ADMIN_PASSWORD` 和 `COUNSELOR_P001_PASSWORD` 至 `COUNSELOR_P004_PASSWORD`。普通辅导员账号与管理员账号按角色分别关联画像。
- 系统角色为“普通辅导员”和“管理员辅导员”。在“工作台 → 账号管理”创建账号时，需关联对应的辅导员画像。
- 管理员辅导员首页展示其管理范围内普通辅导员的统计；普通辅导员首页展示关联的个人画像与发展积累。
- 设置头像：管理员登录后打开“工作台 → 账号管理”，在对应账号的“头像”栏选择“上传头像”，选择 JPG/JPEG 或 PNG 图片；也可以点击“恢复默认”。用户也可以从右上角账户菜单自行更换头像。

头像不需要手动写 SQL，也不会把图片二进制存进 PostgreSQL：上传接口会把图片文件保存到 MinIO 私有桶，在 PostgreSQL 的 `file_assets` 中记录文件元数据，并由 `app_users.avatar_file_id` 关联头像。请通过平台上传，以便自动完成关联、权限和旧头像清理；直接把图片放进 MinIO 或只插入数据库记录都不会正确设置头像。

默认仅监听本机，不需要公开数据库或对象存储端口。所有服务就绪后前端才会启动，首次编译 MinIO 可能需要数分钟。

```bash
docker compose logs -f backend       # 查看后端启动情况
docker compose logs -f minio         # 查看对象存储情况
docker compose down                 # 停止服务，保留数据卷
docker compose up -d                 # 再次启动
```

不要使用 `docker compose down -v`，除非明确希望删除全部数据库、会话和附件。

`.env` 只用于本地配置，不随源码分发。管理员密码只在空数据库首次启动时写入，修改 `.env` 不会重置已存在账号。
升级到角色权限模型后，原管理员账号会迁移为管理员辅导员；已有会话需重新登录以刷新角色权限。
已有安装需要在 `.env` 中分别设置 `COUNSELOR_P001_USERNAME` / `COUNSELOR_P001_PASSWORD` 至 `COUNSELOR_P004_USERNAME` / `COUNSELOR_P004_PASSWORD`（密码至少 12 个字符），然后重启后端；启动初始化会创建或关联普通辅导员账号，不会修改管理员辅导员账号。P001 可分别关联一个管理员账号和一个普通辅导员账号。如果配置的普通辅导员账号原先关联到另一个普通辅导员画像，初始化会在目标画像未绑定其他普通账号时同步其关联和显示姓名；若目标画像已绑定普通账号，则明确报错，不覆盖既有账号或关联。

## 2. 技术栈与实际用途

| 模块 | 版本 / 技术 | 已实现的用途 |
| --- | --- | --- |
| 后端 | Java 17、Spring Boot 3.5.16 | REST API、校验、事务、健康检查 |
| 数据访问 | MyBatis-Plus 3.5.17 | 资料、账号、计划、任务、日程等实体 CRUD |
| 安全 | Spring Security，随 Boot 管理版本 | BCrypt 密码、角色鉴权、会话固定攻击防护、CSRF |
| 业务数据库 | PostgreSQL 16 | JSONB 来源资料、业务关系与审计；Flyway 迁移 |
| 会话 / 缓存 | Redis 7.4、Spring Session | 服务端会话、5 分钟来源资料缓存 |
| 对象存储 | MinIO，官方固定版本源码构建 | 私有桶、文件上传、鉴权下载、删除 |
| 前端 | Vue 2.7.16、Vue Router 3.6.5 | 真实 API 驱动的响应式前台与工作台 |
| 构建 / 网关 | Vite 6.4.4、Nginx | 前端构建、同源 API 代理、历史路由回退 |

MinIO 构建版本固定为 `RELEASE.2025-10-15T17-29-55Z`，见 `infra/minio/Dockerfile`。按照上游当前的源码分发方式构建，不依赖已不可直接拉取的官方二进制镜像。

## 3. 可以演示的操作

1. 登录后进入首页，查看荣誉统计及个人近期日程。
2. 在荣誉、资源、人员或工作室列表中搜索、筛选、翻页、收藏，查看完整来源字段。
3. 在“成长路线”中选择路线，自动带入三项阶段任务，保存计划。
4. 在“我的计划”中完成任务、重新打开任务、编辑截止日期、导出 CSV、删除计划。
5. 在首页创建、编辑、删除个人安排；发展日历只展示数据库中的未来申报与参与事项。计划截止日期自动展示。
6. 在“我的附件”上传文件，再下载验证。其他账号无法查看或下载该附件。
7. 管理员在资料详情中编辑补充信息；“已核验”状态必须填写依据，原始字段不会被覆盖。
## 4. IDE 本地开发

使用 Java 17、Maven 3.9、Node.js 22。

```bash
python3 scripts/init-env.py
docker compose -f compose.yml -f compose.dev.yml up -d postgres redis minio
```

Linux/macOS 可在同一终端导入环境变量：

```bash
set -a
. ./.env
set +a
cd backend
mvn spring-boot:run
```

或者在 IDEA 的启动配置中设置 `.env` 所列变量。数据库默认连接 `localhost:5432/danxing`，用户名 `danxing`，Redis 为 `localhost:6379`，MinIO 为 `http://localhost:9000`。后端使用 8080，前端使用 5173，通过 Vite 代理连接。

另开终端：

```bash
cd frontend
npm ci
npm run dev
```

浏览器打开 http://localhost:5173。若调整后端端口，可在前端启动环境中设置 `API_TARGET`。

```bash
cd backend
mvn test
mvn package
# 产物：target/danxing-platform-1.0.0.jar
```

```bash
cd frontend
npm ci
npm run build
# 产物：dist/
```

## 5. 数据与迁移范围

| 数据 | 数量 | 说明 |
| --- | ---: | --- |
| 详细人员画像 | 4 | 保留原始人员字段、经历与年份 |
| 历史人员种子 | 148 | 尚待工号匹配，不能视为 148 位已确认在岗人员 |
| 个人荣誉 | 189 | 原始来源记录 |
| 集体荣誉 | 69 | 原始来源记录 |
| 育人成果 | 57 | 原始来源记录 |
| 历史发展机会 | 84 | 不代表当年已开放申报 |
| 工作室 | 16 | 保留来源状态与待核验信息 |

初版辅导员画像使用 `backend/src/main/resources/seed.json` 中已有的四份详细画像。`profile/` 中当前提供的四个 Excel 文件仅包含表头，没有人员数据行，暂不作为导入来源。

此次迁移的是原项目的来源数据快照、使用规则和界面功能。旧托管版的登录身份、个人计划、收藏、会话及附件没有自动迁入；新系统使用独立账号和数据库。若后续需要迁移旧个人业务数据，应先导出再建立账号映射。

助手功能已移除；升级现有数据库时，Flyway 新迁移会删除助手聊天记录表及其历史对话。

eHall、学校统一身份认证、实时申报通知接口尚未接入。人员照片文件和来源表中仅出现名称的原始附件没有虚构补齐，“下载资料摘要”生成的是文本摘要。新上传附件实际存入 MinIO。

## 6. 工程文件

| 路径 | 内容 |
| --- | --- |
| `backend/pom.xml` | Java 依赖及版本 |
| `backend/src/main/java/cn/danxing/platform/` | 控制器、服务、安全配置、实体与 Mapper |
| `backend/src/main/resources/db/migration/V1__initial_schema.sql` | 初始业务表、约束与索引 |
| `backend/src/main/resources/db/migration/V2__remove_chat_messages.sql` | 删除已下线助手的聊天记录表 |
| `backend/src/main/resources/db/migration/V4__counselor_roles_and_profiles.sql` | 可扩展角色、权限、辅导员画像及角色关联表 |
| `backend/src/main/resources/seed.json` | 567 条原始资料及数据治理信息 |
| `frontend/src/views/` | Vue 2 页面 |
| `frontend/src/components/` | 弹窗、资料卡片、表单 |
| `frontend/src/api.js` | 同源请求与 CSRF 处理 |
| `frontend/nginx.conf` | 部署网关 |
| `compose.yml`、`compose.dev.yml` | 完整部署和开发基础设施 |
| `scripts/smoke-test.py` | 实际运行环境接口验收 |
| `docs/API.md` | 接口说明 |
| `docs/VALIDATION.md` | 验证结果与范围 |

### 角色与画像

`app_roles` 保存角色编码、显示名称、是否可分配及首页统计范围（`SELF` / `MANAGER`），`app_role_permissions` 保存角色权限。新增角色时，在数据库添加角色记录并配置统计范围和权限编码，账号管理页会自动读取可分配角色，服务端也按数据库权限控制对应接口。

`counselor_profiles` 将详细人员画像按角色分别关联，并用 `manager_profile_id` 表示管理员辅导员与其管理的普通辅导员关系；`app_users.profile_id` 与角色共同将登录账号映射到个人画像。汤潮（P001）同时拥有管理员辅导员和普通辅导员身份，两个账号独立；管理员账号管理贾颖月（P002）、王宇（P003）和陈家栩（P004）。后续添加人员画像时，需同时维护画像的角色及管理关系。

## 7. 部署说明

本工程运行在支持 Java 或 Docker 的主机。原托管演示平台的 Worker 环境无法运行 JVM、PostgreSQL、Redis 和 MinIO，所以原链接仍对应此前的托管版；这份工程是新的独立部署版本。

若部署到学校服务器，在反向代理入口配置 HTTPS，将 `COOKIE_SECURE=true`，并仅向可信入口开放前端端口。保留私有桶策略；正式环境为应用配置专用 MinIO 服务账号，替换演示部署中的存储管理员凭据。需要备份 PostgreSQL 和 MinIO 数据，Redis 保存会话与缓存。

## 8. 官方参考

- Spring Boot 3.5 / Java 17：https://docs.spring.io/spring-boot/3.5/system-requirements.html
- Spring Security CSRF：https://docs.spring.io/spring-security/reference/6.5/servlet/exploits/csrf.html
- MyBatis-Plus Boot 3：https://baomidou.com/en/getting-started/install/
- Vue 2.7 / Vite：https://github.com/vitejs/vite-plugin-vue2
- MinIO 官方源码发布：https://github.com/minio/minio/releases

MinIO 上游使用 AGPLv3；使用和分发其服务时保留上游许可及相应源码获取方式。业务资料及校园图形沿用本项目已有材料。
