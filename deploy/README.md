# ECS 部署与更新

现有前后端代码通过 Docker 部署到阿里云 ECS。GitHub Actions 在 `main` 的应用或部署代码发生变化时自动构建 Linux AMD64 镜像、上传服务器、执行健康检查；也可以进入 Actions → Deploy to ECS → Run workflow 手动部署。

## 已配置的部署方式

- GitHub Secrets：`ECS_HOST`、`ECS_SSH_KEY`、`ECS_KNOWN_HOSTS`。公钥安装在目标 ECS；私钥仅保存在 Secrets，禁止写入仓库。
- 服务器安装 Docker Engine、Compose 2.24.4+、Python 3、rsync、flock。
- 服务器不必连接 Docker Hub：构建结果及 PostgreSQL、Redis 镜像由 Actions 经 SSH 上传。
- 只公开网站端口（默认 80）。PostgreSQL、Redis、MinIO API 不公开，MinIO 控制台只监听回环地址。ECS 安全组需允许网站端口。
- 云端账号与随机密码首次生成在 `/opt/danxing/shared/.env`；以后更新保留该文件，不覆盖密码。
- 每次部署保存于 `/opt/danxing/releases/<commit SHA>`，成功后 `/opt/danxing/current` 指向当前版本。
- 数据存放在 `danxing` 项目的 Docker 命名卷中，更新不会清空数据库和附件。不要使用 `down -v` 或 `docker volume prune`。

## 日常更新

正常提交并推送到 GitHub 的 `main` 即可。Actions 先运行后端测试并构建镜像，部署后检查所有容器健康、首页、管理员登录、画像数据及仪表盘。Actions 显示绿色表示服务器内部验收通过；公网可达性还受安全组、域名及云平台接入规则影响。

私有仓库 Actions 构建分钟数和缓存受账号套餐配额约束；本流程不会自动购买额度。配额不足时需在 GitHub 中检查 Billing/Actions 设置。

## 运维命令

在服务器中执行：

```bash
cd /opt/danxing/current
docker compose -p danxing --env-file /opt/danxing/shared/.env --env-file release.env -f compose.yml -f deploy/compose.ecs.yml ps
docker compose -p danxing --env-file /opt/danxing/shared/.env --env-file release.env -f compose.yml -f deploy/compose.ecs.yml logs --tail=100 backend
```

## 回滚与备份

已有部署更新前，先将 PostgreSQL 备份到 `/opt/danxing/shared/backups/`。备份失败则停止部署。上传或构建失败不会替换当前运行版本；启动或接口检查失败时尝试恢复上一版应用镜像。保留最近两次成功版本，清理旧应用镜像时不清理数据卷。

应用回滚不撤销 Flyway 数据库迁移；未来涉及不兼容数据库结构变更时，需要专门的迁移/恢复计划。自动生成的数据库备份在同一服务器上，不是异地容灾备份。MinIO 附件保存在独立持久卷，数据库备份不包含附件内容。

首次部署和普通更新不会修改已有账号密码。 `.env` 的启动密码只用于初始创建账号，修改文件不会自动重置已有账号密码。

当前为 IP/HTTP 演示入口；长期公开访问需要安排域名、HTTPS 和适用的备案手续。免费试用额度、流量及到期时间请在阿里云控制台管理。
