# 接口说明

前缀 `/api`。成功返回 JSON 或空响应；异常返回 `{"message":"可读错误信息"}`。日期格式 `YYYY-MM-DD`，时间为 ISO 8601。除 CSRF 令牌与登录入口外，业务接口必须登录。

## 身份与 CSRF

1. `GET /auth/csrf` 获取 `token` 和 `headerName`，浏览器保存服务端会话 Cookie。
2. `POST /auth/login` 使用 `application/x-www-form-urlencoded`，字段 `username`、`password`，携带上一步返回的令牌请求头。
3. 登录成功后重新获取 CSRF 令牌。所有 POST、PUT、PATCH、DELETE 都携带令牌。
4. `GET /auth/me` 返回当前账号、数据库角色显示名、权限编码和关联画像 ID（不会返回密码哈希）。
5. `POST /auth/logout` 销毁服务端会话，返回 204。

会话由 Redis 保存，Cookie 为 HttpOnly、SameSite=Lax。部署 HTTPS 时启用 Secure。默认有效期 8 小时。

## 业务接口

| 方法 | 路径 | 用途 |
| --- | --- | --- |
| GET | `/catalog` | 来源资料、补充字段、规则、核验清单、字段映射 |
| GET | `/favorites` | 当前账号收藏的资料 ID |
| PUT | `/favorites/{recordId}` | 幂等设置收藏，`{"active":true}` |
| GET | `/plans` | 当前账号计划；每项为 `{plan,tasks}` |
| POST | `/plans` | 创建计划和 1–12 项任务 |
| PATCH | `/plans/{id}` | 编辑标题、截止日期、说明 |
| DELETE | `/plans/{id}` | 删除计划，数据库级联删除任务 |
| PATCH | `/tasks/{id}` | `{"done":true}`；更新计划完成状态 |
| GET | `/plans/export` | 带 BOM 的 CSV，处理公式注入字符 |
| GET | `/calendar/events` | 数据库中的未来发展日历事项，只读 |
| GET | `/calendar` | 当前账号个人日程（首页个人安排） |
| POST | `/calendar` | 创建日程 |
| PUT | `/calendar/{id}` | 更新日程；结束日期不得早于开始日期 |
| DELETE | `/calendar/{id}` | 删除自己的日程 |
| GET | `/files` | 当前账号私有附件元数据 |
| POST | `/files` | Multipart：`file`、可选 `recordId`；最大 20 MB |
| GET | `/files/{id}/download` | 校验所有权后代理下载 MinIO 对象 |
| DELETE | `/files/{id}` | 删除自己的对象与元数据 |
| GET | `/admin/users` | 有账号管理权限的角色查看账号列表 |
| GET | `/admin/roles` | 获取数据库中可分配的角色及权限编码 |
| GET | `/admin/counselor-profiles` | 获取尚未关联账号的辅导员画像选项 |
| POST | `/admin/users` | 有账号管理权限的角色新建账号并关联画像 |
| PUT | `/admin/records/{id}/review` | 更新平台补充信息及核验状态 |
| GET | `/admin/audit` | 有审计权限的角色查看最近 100 条操作记录 |
| GET | `/home/dashboard` | 返回当前账号画像 ID 和可见管理范围画像 ID |

计划创建示例：

```json
{
  "title": "心理育人专题成长计划",
  "domain": "心理健康",
  "deadline": "2027-03-01",
  "description": "围绕一线实践梳理案例与方法",
  "personId": "P001",
  "opportunityId": null,
  "tasks": ["梳理实践经历", "形成案例材料", "开展阶段复盘"]
}
```

`personId` 只能关联详细人员画像；历史种子需要完成匹配后再扩展关联。`opportunityId` 只允许关联真实的历史机会资料。`deadline` 可为 `null`，编辑时传入 `null` 可清空日期。

个人日程输入：`title`、`startDate`、`endDate`、`category`、`note`、可选 `recordId`。类别仅允许 `个人准备`、`申报安排（待核验）`。发展日历事项由数据库维护，通过 `GET /calendar/events` 读取；接口仅返回尚未开始的事项，不提供用户新增、编辑或删除操作。

核验输入：`keywords`、`philosophy`、`reviewStatus`、`note`。状态仅允许 `待核验`、`已核验`、`需修正`。已核验必须提供非空依据；不会修改 `source_records.payload`。

账号创建输入：`username`、`password`、`displayName`、`role`、`profileId`。账号 3–60 位字母、数字、点、下划线或短横线；密码至少 12 字符且不超过 72 个 UTF-8 字节；角色必须是数据库中可分配的角色，且必须与所选未关联账号的画像角色一致。

## 错误与权限

- 400：输入格式、关联资料或业务校验错误。
- 401：未登录或会话失效。
- 403：角色权限不足或 CSRF 校验失败。
- 404：记录不存在或不属于当前账号，避免泄漏其他账号的对象。
- 409：重复账号或数据约束冲突。
- 413：文件超过限制。
- 500：未预期服务异常，详细信息仅记录在服务器日志。

任何接口都不接受客户端指定所有者。管理员辅导员也不能通过个人接口读取别人的计划、日程、聊天、收藏或附件。管理统计范围只包含数据库中关联到其个人画像的普通辅导员；账号维护、资料核验与审计分别由角色权限控制。
