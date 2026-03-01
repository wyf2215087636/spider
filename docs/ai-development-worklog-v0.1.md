# AI研发协作平台 - 开发留存日志 v0.1

- 版本：v0.1
- 日期：2026-03-01
- 维护方式：持续追加（每次开发完成后更新）
- 维护人：Codex + 项目团队

## 1. 记录目标

用于沉淀每次开发的实现范围、关键决策、变更文件和联调状态，减少信息丢失，便于回溯。

## 2. 记录规范

每条记录包含：
1. 时间
2. 任务目标
3. 完成内容
4. 涉及文件
5. 验证状态
6. 风险/待办

## 3. 开发记录

### 2026-03-01 #001 文档基线与方案收敛

1. 任务目标
- 完成MVP方案、架构蓝图、Monorepo策略与全栈路线图。

2. 完成内容
- 输出 `MVP PRD v0.1`、`技术架构蓝图 v0.1`。
- 新增 `Monorepo v0.2`（JDK17 + Spring Boot3 + MyBatis-Plus + PostgreSQL15）。
- 新增 `全栈逐步落地路线图` 与 `Java增量说明`。
- 新增 `Chat UI设计规范` 与 `中英国际化方案`。

3. 涉及文件
- `docs/mvp-prd-v0.1.md`
- `docs/technical-architecture-blueprint-v0.1.md`
- `docs/monorepo-frontend-backend-strategy-v0.2.md`
- `docs/fullstack-implementation-roadmap-v0.1.md`
- `docs/technical-architecture-java-delta-v0.1.md`
- `docs/chat-ui-design-spec-v0.1.md`
- `docs/i18n-globalization-strategy-v0.1.md`

4. 验证状态
- 文档结构已校验，作为当前执行基线可用。

5. 风险/待办
- 需持续把实现与文档版本保持同步。

### 2026-03-01 #002 T0代码骨架落地

1. 任务目标
- 搭建前后端同工作空间骨架，打通健康检查链路。

2. 完成内容
- 初始化前端 `frontend/web-console`（React + Vite + i18n）。
- 初始化后端 `backend` 多模块（`common` + `api-gateway`）。
- 落地 `GET /api/v1/health` 接口与中英文消息。
- 新增 OpenAPI 契约、Docker 本地依赖（PostgreSQL15/Redis/Temporal）、开发脚本。
- 预建后续服务目录（project/orchestration/workflow/rag/tool-runtime）。

3. 涉及文件（核心）
- `frontend/web-console/src/App.tsx`
- `frontend/web-console/src/i18n/index.ts`
- `backend/api-gateway/src/main/java/com/spider/apigateway/controller/HealthController.java`
- `backend/api-gateway/src/main/resources/application.yml`
- `contracts/openapi/base.yaml`
- `infra/docker-compose/local.yml`

4. 验证状态
- 结构与代码已落地；运行验证依赖本机安装 `mvn/pnpm/docker`。

5. 风险/待办
- 需在开发机完成首次真实启动联调。

### 2026-03-01 #003 Flyway + PostgreSQL15兼容修复

1. 任务目标
- 修复 `Unsupported Database: PostgreSQL 15.3` 启动异常。

2. 完成内容
- 增加 Flyway PostgreSQL 适配依赖 `flyway-database-postgresql`。

3. 涉及文件
- `backend/api-gateway/pom.xml`

4. 验证状态
- 依赖修复已提交，需本机重拉依赖后验证启动。

5. 风险/待办
- 若仍异常，需显式锁定 Flyway 版本。

### 2026-03-01 #004 T1 Workspace全链路

1. 任务目标
- 打通 Workspace 的前后端 CRUD 最小闭环。

2. 完成内容
- 后端新增 Workspace 实体、Mapper、Service、Controller。
- 新增全局异常处理与 `WORKSPACE_NOT_FOUND` 本地化。
- 新增 Flyway `V2` 迁移（`owner/status/index`）。
- OpenAPI 新增 workspace 契约。
- 前端新增 Workspace 创建与列表页面、双语文案、接口调用。
- 修复前端 i18n key 错误（按钮/label 文案不显示问题）。

3. 涉及文件（核心）
- `backend/api-gateway/src/main/java/com/spider/apigateway/workspace/WorkspaceController.java`
- `backend/api-gateway/src/main/java/com/spider/apigateway/workspace/WorkspaceService.java`
- `backend/api-gateway/src/main/java/com/spider/apigateway/exception/GlobalExceptionHandler.java`
- `backend/api-gateway/src/main/resources/db/migration/V2__workspace_owner_status.sql`
- `frontend/web-console/src/api/client.ts`
- `frontend/web-console/src/App.tsx`

4. 验证状态
- 代码链路已贯通，待你本机启动后做接口和页面联调验收。

5. 风险/待办
- 下一步建议补 Workspace 详情/更新/删除页面动作与审计日志。

### 2026-03-01 #005 开发留存机制建立

1. 任务目标
- 建立统一开发留存文档，要求后续每次开发完成都记录。

2. 完成内容
- 新增开发留存日志文档并回填历史开发记录。
- 更新 docs 索引并加入协作留存约定。
- 约定后续每次代码开发完成后同步更新该日志。

3. 涉及文件
- `docs/ai-development-worklog-v0.1.md`
- `docs/README.md`

4. 验证状态
- 文档已创建并加入执行基线索引。

5. 风险/待办
- 后续记录需持续遵守同一模板，避免遗漏验证状态和待办项。

### 2026-03-01 #006 T2 request_id + 审计日志 + Workspace编辑删除

1. 任务目标
- 为接口增加 request_id 追踪能力，并补齐 Workspace 更新/删除链路与最小审计日志。

2. 完成内容
- 新增请求上下文与过滤器：统一生成/透传 `X-Request-Id`、记录 `X-Actor`。
- `ApiResponse` 增加 `requestId` 字段，错误与成功响应统一携带。
- 新增 `audit_log` 表（Flyway `V3`）与审计服务，Workspace 创建/更新/删除写入审计记录。
- 前端新增 Workspace 编辑、保存、取消、删除能力与双语文案。
- OpenAPI 同步补充 `requestId` 响应字段。

3. 涉及文件（核心）
- `backend/common/src/main/java/com/spider/common/request/RequestContext.java`
- `backend/api-gateway/src/main/java/com/spider/apigateway/filter/RequestContextFilter.java`
- `backend/api-gateway/src/main/java/com/spider/apigateway/audit/AuditLogService.java`
- `backend/api-gateway/src/main/resources/db/migration/V3__audit_log.sql`
- `frontend/web-console/src/App.tsx`
- `frontend/web-console/src/api/client.ts`
- `contracts/openapi/base.yaml`

4. 验证状态
- 代码已完成并通过静态自检；需在本机启动后进行接口与页面联调。

5. 风险/待办
- 下一步建议补 `request_id` 在前端可视化展示与审计日志查询接口。

### 2026-03-01 #007 启动故障修复（RequestContextFilter Bean 冲突）

1. 任务目标
- 修复后端启动时报错：`requestContextFilter` Bean 重名冲突。

2. 完成内容
- 将自定义过滤器 Bean 名称改为唯一值：`spiderRequestContextFilter`，避免与 Spring MVC 默认 Bean 冲突。

3. 涉及文件
- `backend/api-gateway/src/main/java/com/spider/apigateway/filter/RequestContextFilter.java`

4. 验证状态
- 代码已修复，需本机重启后端确认启动正常。

5. 风险/待办
- 若本地仍报同名冲突，先执行 `mvn clean` 再重启。

### 2026-03-01 #008 错误可观测性增强（前后端日志）

1. 任务目标
- 解决前端只看到 `INTERNAL_ERROR` 但无法快速定位根因的问题。

2. 完成内容
- 后端：全局异常处理增加 `requestId + method + path + stacktrace` 日志输出。
- 后端：请求过滤器增加 API 访问日志（状态码、耗时、requestId、actor）。
- 后端：Workspace 服务增加 create/update/delete 的业务日志。
- 前端：API错误解析支持展示后端 `message + requestId`，并输出结构化控制台日志。

3. 涉及文件
- `backend/api-gateway/src/main/java/com/spider/apigateway/exception/GlobalExceptionHandler.java`
- `backend/api-gateway/src/main/java/com/spider/apigateway/filter/RequestContextFilter.java`
- `backend/api-gateway/src/main/java/com/spider/apigateway/workspace/WorkspaceService.java`
- `frontend/web-console/src/api/client.ts`

4. 验证状态
- 代码已更新，待你本机重启后实际触发一次错误验证日志输出。

5. 风险/待办
- 下一步建议补日志脱敏策略，避免生产环境记录敏感字段。

### 2026-03-01 #009 Workspace创建失败修复（MyBatis UUID TypeHandler）

1. 任务目标
- 修复创建 Workspace 时 `Type handler was null ... java.util.UUID` 异常。

2. 完成内容
- 新增 PostgreSQL UUID TypeHandler。
- 在 MyBatis-Plus 配置中全局注册该 TypeHandler 包。

3. 涉及文件
- `backend/api-gateway/src/main/java/com/spider/apigateway/config/PostgresUuidTypeHandler.java`
- `backend/api-gateway/src/main/resources/application.yml`

4. 验证状态
- 代码已修复，需本机重启后端并再次创建 Workspace 验证。

5. 风险/待办
- 若仍报 UUID 相关错误，下一步将对实体字段显式指定 `jdbcType=OTHER`。

### 2026-03-01 #010 T3 审计日志查询接口 + 前端留痕面板

1. 任务目标
- 提供审计日志查询能力，并在前端展示操作留痕。

2. 完成内容
- 后端新增 `GET /api/v1/audit-logs`，支持 `resourceType/resourceId/limit` 过滤。
- 审计服务新增查询方法，按创建时间倒序返回。
- OpenAPI 增加审计日志接口与 `AuditLog` 模型。
- 前端新增“操作留痕日志”面板，支持按工作空间筛选和刷新。

3. 涉及文件
- `backend/api-gateway/src/main/java/com/spider/apigateway/audit/AuditLogController.java`
- `backend/api-gateway/src/main/java/com/spider/apigateway/audit/AuditLogService.java`
- `backend/api-gateway/src/main/java/com/spider/apigateway/audit/dto/AuditLogResponse.java`
- `frontend/web-console/src/api/client.ts`
- `frontend/web-console/src/App.tsx`
- `contracts/openapi/base.yaml`

4. 验证状态
- 代码已完成；待本机启动后验证筛选与刷新行为。

5. 风险/待办
- 下一步建议补分页（cursor/page）与按时间范围过滤。
