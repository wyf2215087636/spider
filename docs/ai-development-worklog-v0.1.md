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

### 2026-03-01 #011 历史已删除工作空间可筛选

1. 任务目标
- 让审计筛选项支持历史已删除工作空间，满足追责场景。

2. 完成内容
- 后端新增 `GET /api/v1/audit-logs/workspace-options`，合并“当前工作空间 + 历史审计资源”。
- 返回字段包含 `deleted` 标记，前端可区分已删除对象。
- 前端审计筛选下拉改为使用该接口，并显示 `[Deleted]/[已删除]` 标签。
- OpenAPI 新增筛选项接口与 `WorkspaceAuditFilterOption` 模型。

3. 涉及文件
- `backend/api-gateway/src/main/java/com/spider/apigateway/audit/AuditLogController.java`
- `backend/api-gateway/src/main/java/com/spider/apigateway/audit/AuditLogService.java`
- `backend/api-gateway/src/main/java/com/spider/apigateway/audit/dto/WorkspaceAuditFilterOptionResponse.java`
- `frontend/web-console/src/App.tsx`
- `frontend/web-console/src/api/client.ts`
- `contracts/openapi/base.yaml`

4. 验证状态
- 代码完成，待本机启动后验证“删除后仍可在筛选项看到该资源”。

5. 风险/待办
- 下一步可将 `details` 从字符串升级为结构化 JSON，提升筛选项命名准确度。

### 2026-03-01 #012 T4 Project全链路 + 审计类型扩展

1. 任务目标
- 实现 Project CRUD 闭环，并将审计面板从 workspace 扩展到 workspace/project 双资源。

2. 完成内容
- 后端新增 Project 领域：`/api/v1/projects` CRUD、请求校验、异常处理。
- Workspace 删除前增加“项目存在性检查”，存在项目时返回冲突错误。
- 审计新增 `project.create/update/delete` 留痕。
- 审计筛选新增 `project-options` 接口，支持历史已删除项目筛选。
- 前端新增项目管理板块（创建、筛选、编辑、删除）。
- 前端审计面板支持资源类型切换（Workspace / Project）。
- OpenAPI 与 README 同步更新。

3. 涉及文件
- `backend/api-gateway/src/main/java/com/spider/apigateway/project/ProjectController.java`
- `backend/api-gateway/src/main/java/com/spider/apigateway/project/ProjectService.java`
- `backend/api-gateway/src/main/java/com/spider/apigateway/workspace/WorkspaceService.java`
- `backend/api-gateway/src/main/java/com/spider/apigateway/audit/AuditLogController.java`
- `frontend/web-console/src/App.tsx`
- `frontend/web-console/src/api/client.ts`
- `contracts/openapi/base.yaml`

4. 验证状态
- 代码已完成并通过静态检查；待本机运行验证项目 CRUD 与审计筛选链路。

5. 风险/待办
- 下一步建议为项目与审计接口补自动化测试（集成测试 + 前端 E2E）。

### 2026-03-01 #013 前端场景拆页 + 状态本地化 + 中文资源可读化

1. 任务目标
- 按你的反馈完成三项改造：中文资源改可读汉字、状态文案本地化、前端从单页拆为按场景页面。

2. 完成内容
- 前端引入 `react-router-dom`，将页面拆分为 `工作空间 / 项目 / 审计日志 / 健康检查` 四个路由页面。
- `App.tsx` 改为壳层（头部 + 语言切换 + 顶部导航 + 路由分发），避免后续功能继续堆在单页。
- 新增 `ProjectPage/AuditPage/HealthPage` 并复用现有 API 客户端，保持后端接口不变。
- 状态展示统一走本地化：`draft/active/archived/success/failed` 在 UI 层显示为中英文标签。
- 前端 `zh-CN/common.json` 改为直接中文汉字（不再使用 `\\u` 转义）。
- 后端 `messages_zh_CN.properties` 改为直接中文汉字，继续使用 UTF-8。
- 样式补充顶部导航与状态标签样式，整体视觉风格保持不变。
- 修复前端 TS 构建基线：补充 `@types/node`，完善 `tsconfig.node.json`（`target/lib/types`），恢复 `tsc -b` 可用。

3. 涉及文件
- `frontend/web-console/package.json`
- `frontend/web-console/src/main.tsx`
- `frontend/web-console/src/App.tsx`
- `frontend/web-console/src/pages/ProjectPage.tsx`
- `frontend/web-console/src/pages/AuditPage.tsx`
- `frontend/web-console/src/pages/HealthPage.tsx`
- `frontend/web-console/src/i18n/locales/zh-CN/common.json`
- `frontend/web-console/src/i18n/locales/en-US/common.json`
- `frontend/web-console/src/styles.css`
- `backend/api-gateway/src/main/resources/messages_zh_CN.properties`

4. 验证状态
- 代码改造完成并完成静态自检（路由、文案、状态映射、中文资源文件）。
- 本机已验证 `npm run build` 通过（`tsc -b && vite build`）。

5. 风险/待办
- 建议统一前端包管理器（`pnpm` 或 `npm`）并在团队内固定，避免脚本和锁文件分叉。
- 下一步可进入“项目详情 + 任务拆解 + AI 对话工作区”的分模块页面落地。

### 2026-03-01 #014 导航形态升级（顶部路由 -> 左侧分层菜单）

1. 任务目标
- 将前端导航改为更接近主流 Chat 产品的信息架构：左侧菜单承载分层路由，支持一级/二级/三级分类。

2. 完成内容
- `App` 从顶部标签导航改为左右布局：左侧 `Sidebar`，右侧 `Content`。
- 新增可扩展菜单树模型（`MenuNode` + 递归渲染），当前已按三级结构承载现有路由。
- 路由叶子节点映射：
  - 协作域 > 工作空间 > 空间列表
  - 协作域 > 项目管理 > 项目列表
  - 运行域 > 操作留痕 > 日志查询
  - 运行域 > 系统状态 > 健康检查
- 中英文文案新增菜单分层词条，后续功能统一按该层级继续扩展。
- 样式改为左侧导航风格，并增加移动端降级（小屏下自动切换为上下结构）。

3. 涉及文件
- `frontend/web-console/src/App.tsx`
- `frontend/web-console/src/styles.css`
- `frontend/web-console/src/i18n/locales/zh-CN/common.json`
- `frontend/web-console/src/i18n/locales/en-US/common.json`

4. 验证状态
- 代码改造完成，导航结构与路由功能已打通。

5. 风险/待办
- 后续新增页面时，必须先在菜单树定义归属层级，再补页面路由与实现，避免信息架构失控。

### 2026-03-01 #015 导航收敛到两级 + 可折叠 + 页面目录分域

1. 任务目标
- 将左侧菜单从三级收敛为两级，降低复杂度；同时支持一级分组折叠，并建立后续功能的目录分层约束。

2. 完成内容
- 菜单结构调整为两级：
  - 一级：业务域（协作域、运行域）
  - 二级：可直接路由进入的页面（工作空间、项目管理、操作留痕、健康检查）
- 一级分组支持折叠/展开，默认展开。
- 导航渲染改为“分组模型 + 折叠状态”实现，后续新增功能只需追加配置。
- 页面目录按域归类，避免 `pages` 平铺增长：
  - `src/pages/collab/*`
  - `src/pages/runtime/*`
- 同步更新菜单文案键，移除三级菜单遗留键，避免认知混乱。

3. 涉及文件
- `frontend/web-console/src/App.tsx`
- `frontend/web-console/src/styles.css`
- `frontend/web-console/src/i18n/locales/zh-CN/common.json`
- `frontend/web-console/src/i18n/locales/en-US/common.json`
- `frontend/web-console/src/pages/collab/WorkspacePage.tsx`
- `frontend/web-console/src/pages/collab/ProjectPage.tsx`
- `frontend/web-console/src/pages/runtime/AuditPage.tsx`
- `frontend/web-console/src/pages/runtime/HealthPage.tsx`

4. 验证状态
- 本机已通过 `npm run build`（`tsc -b && vite build`）。

5. 风险/待办
- 后续新增页面需先确定一级业务域，再在对应目录下建页面并挂二级菜单，禁止回到单层平铺目录。

### 2026-03-01 #016 私有会话隔离 + 需求交付发布/接收闭环（T5 起步）

1. 任务目标
- 落地“产品私有处理 -> 结构化发布 -> 研发接收”的最小闭环，支持按 `Actor` 隔离会话并跨角色传递最终结果。

2. 完成内容
- 后端新增私有会话与需求交付包领域：
  - `chat_session`：仅当前 `owner_actor` 可见的私有会话。
  - `requirement_handoff`：从私有会话发布的结构化交付包，支持版本号与接收状态流转。
- 新增接口：
  - `POST /api/v1/chat-sessions` 创建私有会话。
  - `GET /api/v1/chat-sessions` 查询当前 actor 的私有会话。
  - `POST /api/v1/chat-sessions/{sessionId}/publish` 发布最终需求包。
  - `GET /api/v1/requirement-handoffs` 查询已发布交付包。
  - `POST /api/v1/requirement-handoffs/{handoffId}/accept` 接收交付包。
- 新增权限与状态规则：
  - 非会话 owner 无权发布（403）。
  - 仅 `published` 状态可接收，重复接收返回冲突（409）。
- 审计留痕已接入：
  - `chat.session.create`
  - `requirement.handoff.publish`
  - `requirement.handoff.accept`
- 前端新增“交付中心”页面（协作域）：
  - 创建私有会话、选择会话发布需求包、查看并接收交付包。
  - 支持 `Actor` 切换（`system/product/backend/frontend/test`），通过 `X-Actor` 透传后端实现会话隔离演示。
- OpenAPI 契约同步新增以上路径与模型。

3. 涉及文件（核心）
- `backend/api-gateway/src/main/resources/db/migration/V5__chat_session_and_requirement_handoff.sql`
- `backend/api-gateway/src/main/java/com/spider/apigateway/handoff/HandoffService.java`
- `backend/api-gateway/src/main/java/com/spider/apigateway/handoff/ChatSessionController.java`
- `backend/api-gateway/src/main/java/com/spider/apigateway/handoff/RequirementHandoffController.java`
- `backend/api-gateway/src/main/java/com/spider/apigateway/exception/GlobalExceptionHandler.java`
- `backend/api-gateway/src/main/resources/messages*.properties`
- `contracts/openapi/base.yaml`
- `frontend/web-console/src/pages/collab/HandoffCenterPage.tsx`
- `frontend/web-console/src/App.tsx`
- `frontend/web-console/src/api/client.ts`
- `frontend/web-console/src/i18n/locales/en-US/common.json`
- `frontend/web-console/src/i18n/locales/zh-CN/common.json`
- `frontend/web-console/src/styles.css`

4. 验证状态
- 前端已通过本机构建验证：`npm run build` 成功。
- 后端未完成本地编译验证：当前环境缺少 `mvn` 命令（待你本机或 CI 验证）。

5. 风险/待办
- 下一步应补“会话消息表 + AI调用记录表”，将当前“会话元信息”扩展为完整聊天链路。
- 建议补集成测试覆盖发布/接收状态流转与 owner 权限校验。

### 2026-03-01 #017 用户系统MVP（登录 + 角色自动注入 + 会话鉴权）

1. 任务目标
- 在开发早期引入用户系统，避免后续权限模型大改：支持登录、角色识别、接口鉴权、前端登录态，并移除手动身份选择。

2. 完成内容
- 后端新增用户与会话模型：
  - `app_user`（账号、显示名、角色、状态、密码哈希）
  - `app_user_session`（token、过期时间）
- 新增认证接口：
  - `POST /api/v1/auth/login`
  - `GET /api/v1/auth/me`
  - `POST /api/v1/auth/logout`
- 请求过滤器升级为 token 鉴权：
  - 除 `/api/v1/health` 与 `/api/v1/auth/login` 外，`/api/**` 默认需要 `Authorization: Bearer <token>`。
  - 鉴权成功后自动注入 `RequestContext.actor=username`、`RequestContext.role=role`。
- 私有会话角色来源调整：
  - `chat_session.role` 不再由前端传入，改为后端从登录用户角色自动写入。
- 前端登录态改造：
  - 新增登录页，未登录不进入业务页面。
  - `client` 改为统一携带 Bearer token；移除 `X-Actor` 手工身份模式。
  - Header 右上角显示当前登录用户并支持退出登录。
- 初始化账号（迁移内置）：
  - `product / backend / frontend / test`
  - 默认密码：`spider123`

3. 涉及文件（核心）
- `backend/api-gateway/src/main/resources/db/migration/V6__user_auth_system.sql`
- `backend/api-gateway/src/main/java/com/spider/apigateway/auth/*`
- `backend/api-gateway/src/main/java/com/spider/apigateway/filter/RequestContextFilter.java`
- `backend/common/src/main/java/com/spider/common/request/RequestContext.java`
- `backend/api-gateway/src/main/java/com/spider/apigateway/exception/GlobalExceptionHandler.java`
- `backend/api-gateway/src/main/java/com/spider/apigateway/handoff/HandoffService.java`
- `frontend/web-console/src/pages/LoginPage.tsx`
- `frontend/web-console/src/App.tsx`
- `frontend/web-console/src/api/client.ts`
- `contracts/openapi/base.yaml`

4. 验证状态
- 前端已通过本机构建：`npm run build` 成功。
- 后端在当前环境无法执行 `mvn`（命令缺失），需你本机或 CI 做编译/启动验证。

5. 风险/待办
- 下一步建议接入刷新 token 或滑动续期策略，避免固定时长会话过期带来频繁重登。
- 需补接口鉴权与权限边界的集成测试（尤其是无 token、过期 token、跨角色访问）。

### 2026-03-01 #018 登录页语言切换补齐

1. 任务目标
- 为登录页补齐中英切换能力，保持未登录态与已登录态的国际化体验一致。

2. 完成内容
- 登录页新增语言切换按钮（显示当前语言标签 `ZH/EN`）。
- `App` 未登录分支接入 `toggleLanguage` 与 `languageLabel` 到登录页。
- 补充登录页头部样式（标签 + 按钮布局），避免新增元素无样式堆叠。

3. 涉及文件
- `frontend/web-console/src/pages/LoginPage.tsx`
- `frontend/web-console/src/App.tsx`
- `frontend/web-console/src/styles.css`

4. 验证状态
- 本机已通过前端构建验证：`npm run build` 成功（`tsc -b && vite build`）。

5. 风险/待办
- 后续可考虑记忆用户上次语言选择（localStorage 或用户偏好表）以提升体验。

### 2026-03-01 #019 T6第一版：LLM Gateway + Code Impact Agent + 一键AI草拟 + 聊天窗

1. 任务目标
- 在“交付中心”降低手工输入门槛：增加会话消息能力、AI一键草拟需求包、并接入可配置大模型网关（默认 DeepSeek）。

2. 完成内容
- 后端新增 `chat_message` 持久化（Flyway `V7`），支持会话消息存储与查询。
- 后端新增 LLM 网关配置与实现：
  - `spider.llm.*` 配置项（provider/base-url/api-key/model/temperature/timeout）。
  - OpenAI 兼容 `chat/completions` 调用逻辑，默认按 DeepSeek 参数路由。
  - 无 key 或调用失败时自动回退到本地草拟兜底，保证功能可用。
- 后端新增 `CodeImpactAgent`：
  - 对当前代码库做关键词扫描，输出影响文件、风险提示、测试建议。
- 后端新增 AI 草拟服务与接口：
  - `GET /api/v1/chat-sessions/{sessionId}/messages`
  - `POST /api/v1/chat-sessions/{sessionId}/messages`
  - `POST /api/v1/chat-sessions/{sessionId}/ai-draft`
  - AI 草拟结果字段：需求摘要、验收标准、影响范围、目标角色、优先级、建议任务、影响文件、风险/测试提示、模型信息。
- 前端交付中心升级：
  - 新增会话聊天窗（消息列表 + 输入发送）。
  - 新增“AI一键草拟”按钮，一键将草拟内容回填到发布表单，人工可继续修改后发布。
  - 新增 AI 证据展示区（模型、影响文件、建议任务、风险、测试建议）。
- i18n 文案补齐：
  - 英文新增聊天与 AI 草拟相关文案键。
  - 中文资源文件重建为可读中文（UTF-8），避免历史乱码。
- OpenAPI 契约同步新增消息/草拟接口与模型。

3. 涉及文件（核心）
- `backend/api-gateway/src/main/resources/db/migration/V7__chat_message_and_ai_draft.sql`
- `backend/api-gateway/src/main/java/com/spider/apigateway/ai/LlmProperties.java`
- `backend/api-gateway/src/main/java/com/spider/apigateway/ai/LlmGateway.java`
- `backend/api-gateway/src/main/java/com/spider/apigateway/ai/CodeImpactAgent.java`
- `backend/api-gateway/src/main/java/com/spider/apigateway/ai/AiDraftService.java`
- `backend/api-gateway/src/main/java/com/spider/apigateway/handoff/HandoffService.java`
- `backend/api-gateway/src/main/java/com/spider/apigateway/handoff/ChatSessionController.java`
- `frontend/web-console/src/pages/collab/HandoffCenterPage.tsx`
- `frontend/web-console/src/api/client.ts`
- `frontend/web-console/src/styles.css`
- `frontend/web-console/src/i18n/locales/zh-CN/common.json`
- `frontend/web-console/src/i18n/locales/en-US/common.json`
- `contracts/openapi/base.yaml`

4. 验证状态
- 前端本机已通过：`npm run build` 成功（`tsc -b && vite build`）。
- 后端当前运行环境无法执行 `mvn`（命令缺失），需在开发机执行编译/启动验证。

5. 风险/待办
- 当前 AI 草拟采用“在线模型 + 本地兜底”双路径，建议下一步补调用审计表（prompt/latency/token）与失败分类指标。
- 目前会话消息仍是非流式回复，下一步可升级为 SSE/WS 流式输出并增加中断控制。

### 2026-03-01 #020 T6增量：交付状态机 + AI任务拆解分发

1. 任务目标
- 将“需求交付”升级为可追踪状态流转，并把 AI 草拟进一步落地为各角色可执行任务，支持认领与状态更新。

2. 完成内容
- 交付状态机落地（后端）：
  - 状态扩展为：`draft -> in_review -> published -> accepted -> in_development -> in_testing -> done / rejected`。
  - 新增流转接口：`POST /api/v1/requirement-handoffs/{handoffId}/transition`。
  - 兼容保留 `accept` 接口（内部走 transition 逻辑）。
- 任务拆解分发落地（后端）：
  - 新增 `requirement_task` 表（Flyway `V8`），支持角色、预估工时、任务状态、认领人、来源（ai/manual）等字段。
  - 新增 AI 拆解接口：`POST /api/v1/requirement-handoffs/{handoffId}/tasks/ai-generate`。
  - 新增任务接口：
    - `GET /api/v1/requirement-handoffs/{handoffId}/tasks`
    - `POST /api/v1/requirement-handoffs/tasks/{taskId}/claim`
    - `PUT /api/v1/requirement-handoffs/tasks/{taskId}/status`
  - 任务状态：`todo / in_progress / done / blocked`。
- AI能力增强（后端）：
  - `AiDraftService` 新增任务计划生成（LLM JSON + fallback 默认计划）。
  - 输出任务计划证据：影响文件、风险提示、测试建议、模型信息。
- 交付中心升级（前端）：
  - 新增状态流转按钮（按当前状态/角色显示可执行动作）。
  - 新增“角色任务看板”：AI 生成任务、任务认领、任务状态更新。
  - 交付筛选支持全部新状态。
- 国际化与文案：
  - 补齐中英文状态机与任务看板文案。
  - 重新整理中文资源文件为可读中文（UTF-8）。
  - 后端中文消息资源同步补齐新异常文案键。
- OpenAPI 同步：
  - 增加状态流转、任务拆解、任务操作接口与模型定义。

3. 涉及文件（核心）
- `backend/api-gateway/src/main/resources/db/migration/V8__handoff_state_machine_and_tasks.sql`
- `backend/api-gateway/src/main/java/com/spider/apigateway/handoff/HandoffService.java`
- `backend/api-gateway/src/main/java/com/spider/apigateway/handoff/RequirementHandoffController.java`
- `backend/api-gateway/src/main/java/com/spider/apigateway/handoff/RequirementTaskEntity.java`
- `backend/api-gateway/src/main/java/com/spider/apigateway/handoff/RequirementTaskMapper.java`
- `backend/api-gateway/src/main/java/com/spider/apigateway/ai/AiDraftService.java`
- `backend/api-gateway/src/main/java/com/spider/apigateway/exception/GlobalExceptionHandler.java`
- `backend/api-gateway/src/main/resources/messages*.properties`
- `frontend/web-console/src/pages/collab/HandoffCenterPage.tsx`
- `frontend/web-console/src/api/client.ts`
- `frontend/web-console/src/utils/status.ts`
- `frontend/web-console/src/i18n/locales/en-US/common.json`
- `frontend/web-console/src/i18n/locales/zh-CN/common.json`
- `contracts/openapi/base.yaml`

4. 验证状态
- 前端本机已通过：`npm run build` 成功（`tsc -b && vite build`）。
- 后端当前环境无法执行 `mvn`（命令缺失），需在开发机执行编译与启动验证。

5. 风险/待办
- 当前任务计划默认“已存在即复用”，后续可加 `regenerate=true` 策略支持增量重拆解。
- 建议下一步补“流转规则 + 任务权限”集成测试，覆盖跨角色越权和非法状态跳转。

### 2026-03-01 #021 任务拆解触发条件收紧（防止“未输入也出任务”）

1. 任务目标
- 修复交互歧义：未显式选择交付包时不应触发任务拆解；并移除任务拆解兜底，确保输出来源可追溯且可解释。

2. 完成内容
- 前端取消“自动选中第一条交付包”逻辑，要求用户手动选择后再进入任务看板与拆解动作。
- 后端 `AI 任务拆解` 状态约束收紧：仅允许 `published/accepted` 状态执行拆解。
- 后端新增内容完整性校验：`title/requirementSummary/acceptanceCriteria` 任一缺失直接返回错误。
- 后端移除任务拆解默认兜底计划：当模型未返回可解析结构化任务时，直接返回失败，不再自动生成默认任务。
- 新增异常与本地化消息：
  - `RequirementHandoffInsufficientContentException`
  - `RequirementTaskPlanGenerationException`
  - 新增多语言文案键用于前端提示。

3. 涉及文件
- `frontend/web-console/src/pages/collab/HandoffCenterPage.tsx`
- `backend/api-gateway/src/main/java/com/spider/apigateway/handoff/HandoffService.java`
- `backend/api-gateway/src/main/java/com/spider/apigateway/ai/AiDraftService.java`
- `backend/api-gateway/src/main/java/com/spider/apigateway/exception/GlobalExceptionHandler.java`
- `backend/api-gateway/src/main/java/com/spider/apigateway/exception/RequirementHandoffInsufficientContentException.java`
- `backend/api-gateway/src/main/java/com/spider/apigateway/exception/RequirementTaskPlanGenerationException.java`
- `backend/api-gateway/src/main/resources/messages.properties`
- `backend/api-gateway/src/main/resources/messages_en_US.properties`
- `backend/api-gateway/src/main/resources/messages_zh_CN.properties`

4. 验证状态
- 前端本机已通过：`npm run build` 成功。
- 后端当前环境无法执行 `mvn`（命令缺失），需在开发机进行编译与接口联调验证。

5. 风险/待办
- 后续可补“拆解前确认弹窗 + 来源摘要预览”，进一步降低误操作。

### 2026-03-01 #022 任务拆解结果双语化（随页面语言自动切换）

1. 任务目标
- 让任务拆解结果支持中英双语，并随右上角语言切换自动展示对应语言内容。

2. 完成内容
- 数据库新增任务双语字段（Flyway `V9`）：
  - `requirement_task.title_zh/title_en/description_zh/description_en`
  - 对历史任务自动回填（从原 `title/description` 拷贝）并设为非空。
- 后端任务模型与响应扩展双语字段：
  - `RequirementTaskEntity` 新增双语字段映射。
  - `RequirementTaskResponse` 新增双语返回字段。
- AI任务拆解提示词与解析升级：
  - 要求模型返回每个任务的中英标题与中英描述。
  - 若单语缺失，按另一语言回填，保证双语字段完整。
- 前端任务看板按语言自动显示：
  - `zh-CN` 显示 `titleZh/descriptionZh`
  - 其他语言显示 `titleEn/descriptionEn`
  - 保留回退逻辑，避免历史数据显示为空。
- OpenAPI `RequirementTask` 模型同步新增双语字段定义。

3. 涉及文件
- `backend/api-gateway/src/main/resources/db/migration/V9__requirement_task_i18n.sql`
- `backend/api-gateway/src/main/java/com/spider/apigateway/handoff/RequirementTaskEntity.java`
- `backend/api-gateway/src/main/java/com/spider/apigateway/handoff/dto/RequirementTaskResponse.java`
- `backend/api-gateway/src/main/java/com/spider/apigateway/ai/AiDraftService.java`
- `backend/api-gateway/src/main/java/com/spider/apigateway/handoff/HandoffService.java`
- `frontend/web-console/src/api/client.ts`
- `frontend/web-console/src/pages/collab/HandoffCenterPage.tsx`
- `contracts/openapi/base.yaml`

4. 验证状态
- 前端本机已通过：`npm run build` 成功。
- 后端当前环境无法执行 `mvn`（命令缺失），需在开发机验证编译与接口。

5. 风险/待办
- 建议下一步为 `riskHints/testHints/rationale` 增加双语字段，确保任务证据说明也可随语言切换。

### 2026-03-01 #023 PMO角色落地 + 模块可见度收口

1. 任务目标
- 新增 PMO 角色，并按角色收敛模块可见范围与交付中心操作权限，形成“产品发布、PMO拆解、研发执行”的协作分工。

2. 完成内容
- 数据库新增 `pmo` 角色约束与默认账号（Flyway `V10`，账号 `pmo/spider123`）。
- 后端权限收口：
  - 仅 `pmo/admin` 可触发 `AI任务拆解`。
  - `product/pmo/admin` 可查看交付包全量任务，研发与测试仅看本角色任务。
- 前端菜单按角色可见：
  - `pmo`：项目管理、交付中心、审计、健康检查。
- 前端交付中心按角色分区：
  - `product/admin`：会话、聊天、需求包草拟与发布。
  - `pmo/admin`：任务看板中的 AI 拆解按钮。
  - `backend/frontend/test`：仅执行交付流转与任务认领/状态更新。
- OpenAPI 契约 `UserProfile.role` 增加 `pmo` 枚举。
- i18n 补齐 `PMO/管理员` 角色文案与登录提示账号列表。

3. 涉及文件
- `backend/api-gateway/src/main/resources/db/migration/V10__add_pmo_role_and_visibility.sql`
- `backend/api-gateway/src/main/java/com/spider/apigateway/handoff/HandoffService.java`
- `frontend/web-console/src/App.tsx`
- `frontend/web-console/src/pages/collab/HandoffCenterPage.tsx`
- `frontend/web-console/src/api/client.ts`
- `frontend/web-console/src/i18n/locales/zh-CN/common.json`
- `frontend/web-console/src/i18n/locales/en-US/common.json`
- `contracts/openapi/base.yaml`

4. 验证状态
- 前端需执行 `npm run build` 验证类型与构建。
- 后端当前环境无法执行 `mvn`（命令缺失），需在开发机验证编译与接口。

5. 风险/待办
- 交付状态机目前仍以产品/研发/测试为主，后续可评估是否让 PMO 参与特定流转动作（如评审拒绝/回退）。

### 2026-03-01 #024 T7.1 任务中心（角色池 + 个人认领）第一版

1. 任务目标
- 从“交付中心”拆出独立“任务中心”，实现按角色分发到任务池、处理人自认领的执行闭环。

2. 完成内容
- 后端新增任务中心查询接口：`GET /api/v1/tasks`。
- 支持任务视图：
  - `pool`：待认领任务池
  - `mine`：我的任务
  - `all`：全部任务（仅 `pmo/admin`）
- 查询维度支持：`view/projectId/status/role`。
- 权限策略：
  - 普通角色（产品/后端/前端/测试）：仅可看本角色 `pool` 与本人 `mine`。
  - `pmo/admin`：可看跨角色 `pool` / `mine` / `all`。
- 前端新增“任务中心”页面与菜单入口，提供三类视图切换、筛选、任务认领、状态更新能力。
- 前端导航接入：
  - `协作域 -> 任务中心`
  - 所有业务角色可见（含 PMO/Admin）。
- OpenAPI 增补 `GET /api/v1/tasks` 契约。

3. 涉及文件
- `backend/api-gateway/src/main/java/com/spider/apigateway/handoff/HandoffService.java`
- `backend/api-gateway/src/main/java/com/spider/apigateway/handoff/TaskCenterController.java`
- `frontend/web-console/src/pages/collab/TaskCenterPage.tsx`
- `frontend/web-console/src/App.tsx`
- `frontend/web-console/src/api/client.ts`
- `frontend/web-console/src/i18n/locales/zh-CN/common.json`
- `frontend/web-console/src/i18n/locales/en-US/common.json`
- `frontend/web-console/src/styles.css`
- `contracts/openapi/base.yaml`

4. 验证状态
- 前端需执行 `npm run build` 验证构建与类型检查。
- 后端当前环境无法执行 `mvn`（命令缺失），需在开发机验证编译与接口联调。

5. 风险/待办
- 当前任务中心仍使用任务记录内 `handoffId/projectId` 展示来源；下一步可补“需求包标题/项目名”服务端聚合返回，减少前端拼装成本。

### 2026-03-01 #025 T7.2 任务详情面板（点击查看上下文）

1. 任务目标
- 让任务中心支持“点击任务查看详情”，避免跨页面反复跳转，提升前后端/测试理解任务上下文效率。

2. 完成内容
- 后端新增任务详情接口：`GET /api/v1/tasks/{taskId}`。
- 详情返回内容覆盖：
  - 任务字段（双语标题/描述、状态、负责人、工时、来源）。
  - 需求包上下文（标题、摘要、验收标准、影响范围、优先级、目标角色、交付状态）。
  - 项目名（若存在）。
- 详情权限：
  - `admin`、`product`、`pmo` 可查看任务详情。
  - 其他角色需满足“同角色任务”或“本人负责人”才能查看。
- 前端任务中心新增“查看详情”按钮，点击后展示详情面板（含任务与需求包上下文）。
- OpenAPI 补充任务详情接口与模型定义。

3. 涉及文件
- `backend/api-gateway/src/main/java/com/spider/apigateway/handoff/HandoffService.java`
- `backend/api-gateway/src/main/java/com/spider/apigateway/handoff/TaskCenterController.java`
- `backend/api-gateway/src/main/java/com/spider/apigateway/handoff/dto/RequirementTaskDetailResponse.java`
- `frontend/web-console/src/pages/collab/TaskCenterPage.tsx`
- `frontend/web-console/src/api/client.ts`
- `frontend/web-console/src/i18n/locales/zh-CN/common.json`
- `frontend/web-console/src/i18n/locales/en-US/common.json`
- `contracts/openapi/base.yaml`

4. 验证状态
- 前端需执行 `npm run build` 进行类型与打包验证。
- 后端当前环境无法执行 `mvn`（命令缺失），需在开发机验证编译与接口。

5. 风险/待办
- 详情中暂未落地“任务协作评论/阻塞备注/转派记录”；建议下一步补 `task_comment` 与任务时间线接口。

### 2026-03-01 #026 任务详情改为弹窗 + 角色化操作区

1. 任务目标
- 将任务详情由页内块改为弹窗展示，并在弹窗中按角色给出差异化可执行操作。

2. 完成内容
- 任务详情展示形态升级为 Modal 弹窗（遮罩、关闭按钮、移动端适配）。
- 弹窗信息结构分为三段：
  - 任务信息
  - 需求上下文
  - 角色操作
- 角色化操作策略（前端显式约束）：
  - `admin`：可直接认领并更新任意任务状态。
  - `backend/frontend/test`：仅可认领并更新“本角色任务”。
  - `product`：仅可操作 `product` 角色任务，其他任务只读。
  - `pmo`：治理只读视角（查看与协调，不直接改任务状态）。
- 列表按钮权限同步收敛，避免出现“点击后才返回403”的无效操作体验。
- 中英文本地化补齐弹窗与角色操作提示文案。

3. 涉及文件
- `frontend/web-console/src/pages/collab/TaskCenterPage.tsx`
- `frontend/web-console/src/styles.css`
- `frontend/web-console/src/i18n/locales/zh-CN/common.json`
- `frontend/web-console/src/i18n/locales/en-US/common.json`

4. 验证状态
- 前端需执行 `npm run build` 验证构建。
- 后端接口无新增，仅复用现有详情/认领/状态更新能力。

5. 风险/待办
- 当前 PMO 在弹窗内为只读治理；若后续需要“转派/催办”，建议新增 `assign` 与 `follow-up` 接口再开放 PMO 操作按钮。

### 2026-03-01 #027 任务中心分页 + 紧凑列表 + 操作区防变形

1. 任务目标
- 解决任务增多后的可用性问题：任务中心支持分页浏览；任务列表密度提升；操作按钮与状态下拉在窄宽度下不变形。

2. 完成内容
- 后端 `GET /api/v1/tasks` 升级为分页响应：
  - 新增查询参数：`page`、`size`
  - 返回结构：`items/total/page/size/hasNext`
  - 服务端分页策略：`LIMIT/OFFSET`，并限制 `size <= 100`
- 前端任务中心接入分页查询与翻页控件：
  - 新增“上一页/下一页”操作
  - 显示当前页与总条数
  - 切换视图/筛选条件自动回到第一页
- 任务列表改为紧凑卡片样式，减少单卡高度。
- 操作区样式收敛，按钮和状态下拉固定最小宽度，不随布局压缩变形。
- OpenAPI 同步更新任务中心分页契约与模型定义。

3. 涉及文件
- `backend/api-gateway/src/main/java/com/spider/apigateway/handoff/HandoffService.java`
- `backend/api-gateway/src/main/java/com/spider/apigateway/handoff/TaskCenterController.java`
- `backend/api-gateway/src/main/java/com/spider/apigateway/handoff/dto/TaskCenterPageResponse.java`
- `frontend/web-console/src/api/client.ts`
- `frontend/web-console/src/pages/collab/TaskCenterPage.tsx`
- `frontend/web-console/src/styles.css`
- `frontend/web-console/src/i18n/locales/zh-CN/common.json`
- `frontend/web-console/src/i18n/locales/en-US/common.json`
- `contracts/openapi/base.yaml`

4. 验证状态
- 前端需执行 `npm run build` 验证构建与类型。
- 后端当前环境无法执行 `mvn`（命令缺失），需在开发机验证编译和分页接口行为。

5. 风险/待办
- 当前分页为页码模式；当任务量进一步增大时，可升级为 cursor 模式以减少深页性能损耗。

### 2026-03-01 #028 T8.1 文档中心首版（文档/版本/回滚）

1. 任务目标
- 启动“多模态文档中心”分阶段落地，先完成可用基础能力：文档 CRUD、版本发布、历史查看、回滚。

2. 完成内容
- 后端新增文档中心数据模型与迁移（Flyway `V11`）：
  - `product_doc`
  - `product_doc_version`
- 后端新增文档中心接口：
  - `POST /api/v1/product-docs`
  - `GET /api/v1/product-docs`
  - `GET /api/v1/product-docs/{docId}`
  - `PUT /api/v1/product-docs/{docId}`
  - `POST /api/v1/product-docs/{docId}/publish-version`
  - `GET /api/v1/product-docs/{docId}/versions`
  - `GET /api/v1/product-docs/{docId}/versions/{versionId}`
  - `POST /api/v1/product-docs/{docId}/rollback`
- 权限策略首版：
  - `product/admin` 可创建文档。
  - `admin` 可编辑任意文档。
  - `product` 仅可编辑自己创建的文档。
- 前端新增“文档中心”页面：
  - 文档列表筛选与创建
  - 草稿编辑与保存
  - 版本发布
  - 历史版本列表与预览
  - 指定版本回滚
- 左侧菜单新增“文档中心”入口并接入路由。
- OpenAPI 同步补齐文档中心契约。
- 新增方案文档：`multimodal-doc-center-design-v0.1.md`。

3. 涉及文件
- `backend/api-gateway/src/main/resources/db/migration/V11__product_doc_center_core.sql`
- `backend/api-gateway/src/main/java/com/spider/apigateway/doc/*`
- `backend/api-gateway/src/main/java/com/spider/apigateway/exception/GlobalExceptionHandler.java`
- `backend/api-gateway/src/main/java/com/spider/apigateway/exception/ProductDoc*.java`
- `backend/api-gateway/src/main/resources/messages*.properties`
- `frontend/web-console/src/pages/collab/ProductDocCenterPage.tsx`
- `frontend/web-console/src/App.tsx`
- `frontend/web-console/src/api/client.ts`
- `frontend/web-console/src/styles.css`
- `frontend/web-console/src/i18n/locales/zh-CN/common.json`
- `frontend/web-console/src/i18n/locales/en-US/common.json`
- `contracts/openapi/base.yaml`
- `docs/multimodal-doc-center-design-v0.1.md`

4. 验证状态
- 前端已通过：`npm run build` 成功。
- 后端当前环境无法执行 `mvn`（命令缺失），需在开发机验证编译、迁移与接口联调。

5. 风险/待办
- 下一阶段（T8.2）补“文档 Chat 修订候选 + 人工确认发布”闭环。
- 下一阶段（T8.3）补图片上传、OCR识别和识别结果校对能力。

### 2026-03-01 #029 T8.2 文档中心 AI 修订候选 + 人工确认发布

1. 任务目标
- 在文档中心落地“AI 先出候选、产品确认后发布版本”的闭环，避免 AI 直接覆盖正式文档。

2. 完成内容
- 后端新增 Flyway `V12`：
  - `product_doc_chat_message`（文档 AI 会话消息）
  - `product_doc_revision`（AI 修订候选）
- 后端新增文档 AI 接口：
  - `GET /api/v1/product-docs/{docId}/ai-chat/messages`
  - `POST /api/v1/product-docs/{docId}/ai-chat/messages`
  - `GET /api/v1/product-docs/{docId}/ai-revisions`
  - `POST /api/v1/product-docs/{docId}/ai-revisions/{revisionId}/confirm`
  - `POST /api/v1/product-docs/{docId}/ai-revisions/{revisionId}/reject`
- 后端实现规则：
  - 仅文档 owner（product）或 admin 可发起 AI 修订、确认、驳回。
  - AI 修订候选状态：`pending / confirmed / rejected`。
  - 确认时自动发布 `source_type=ai_confirm` 新版本，并更新当前文档版本指针。
- 前端文档中心升级：
  - 新增 AI 修订会话区（消息流 + 输入指令）。
  - 新增候选修订列表、状态筛选、候选内容预览。
  - 新增“确认发布版本 / 驳回候选”操作。
- OpenAPI 契约补齐 AI 修订相关路径与模型。
- 中英文 i18n 补齐文档 AI 修订文案与 `pending/confirmed` 状态文案。

3. 涉及文件（核心）
- `backend/api-gateway/src/main/resources/db/migration/V12__product_doc_ai_revision.sql`
- `backend/api-gateway/src/main/java/com/spider/apigateway/doc/ProductDocService.java`
- `backend/api-gateway/src/main/java/com/spider/apigateway/doc/ProductDocController.java`
- `backend/api-gateway/src/main/java/com/spider/apigateway/doc/ProductDocRevisionEntity.java`
- `backend/api-gateway/src/main/java/com/spider/apigateway/doc/ProductDocChatMessageEntity.java`
- `backend/api-gateway/src/main/java/com/spider/apigateway/doc/dto/*`
- `backend/api-gateway/src/main/java/com/spider/apigateway/exception/GlobalExceptionHandler.java`
- `contracts/openapi/base.yaml`
- `frontend/web-console/src/pages/collab/ProductDocCenterPage.tsx`
- `frontend/web-console/src/api/client.ts`
- `frontend/web-console/src/utils/status.ts`
- `frontend/web-console/src/styles.css`
- `frontend/web-console/src/i18n/locales/en-US/common.json`
- `frontend/web-console/src/i18n/locales/zh-CN/common.json`
- `docs/multimodal-doc-center-design-v0.1.md`

4. 验证状态
- 前端本机已通过：`npm run build` 成功（`tsc -b && vite build`）。
- 后端当前环境无法执行 `mvn`（命令缺失），需在开发机验证编译、迁移与接口联调。

5. 风险/待办
- 下一阶段 T8.3 需要补文档图片上传、OCR/视觉理解和异步任务状态追踪。
- 建议为“候选确认/驳回状态机”补后端集成测试，覆盖并发确认与重复操作。

### 2026-03-01 #030 文档列表空版本 NPE 修复

1. 任务目标
- 修复创建文档后拉取文档列表时报 `INTERNAL_ERROR` 的空指针问题。

2. 完成内容
- 定位根因：文档尚未发布版本时 `currentVersionId=null`，代码对 `Map.of()` 执行 `get(null)` 触发 NPE。
- 修复 `ProductDocService.list`：增加 `currentVersionId` 空值保护，未发布版本时返回 `currentVersionNo=null`。

3. 涉及文件
- `backend/api-gateway/src/main/java/com/spider/apigateway/doc/ProductDocService.java`

4. 验证状态
- 代码修复完成；当前环境无 `mvn`，需在开发机重启后端后联调验证。

5. 风险/待办
- 建议补文档中心服务层测试，覆盖“无版本文档列表返回”场景。

### 2026-03-01 #031 文档中心 AI 区域中文文案乱码修复

1. 任务目标
- 修复文档中心 AI 区域显示 `????` 的中文文案乱码问题。

2. 完成内容
- 定位为 `zh-CN/common.json` 中 AI 新增键值被错误写入为 `?` 字符串。
- 回写 AI 区域相关中文键值（含 `statusPending/statusConfirmed`）为正确 UTF-8 中文。
- 前端构建回归通过，确认未引入类型或打包问题。

3. 涉及文件
- `frontend/web-console/src/i18n/locales/zh-CN/common.json`

4. 验证状态
- 前端本机已通过：`npm run build` 成功。

5. 风险/待办
- 后续新增中文文案建议统一走 UTF-8 写入流程，避免控制台编码导致问号替换。

### 2026-03-01 #032 文档版本预览交互修复（底部无感 -> 弹窗）

1. 任务目标
- 修复“版本历史预览按钮点击无明显反馈”的交互问题。

2. 完成内容
- 将版本预览展示方式从页面底部结果块改为 Modal 弹窗。
- 点击“预览”后立即展示弹窗，支持遮罩关闭与按钮关闭。
- 保留原接口调用逻辑，增强用户可见反馈。

3. 涉及文件
- `frontend/web-console/src/pages/collab/ProductDocCenterPage.tsx`

4. 验证状态
- 前端本机已通过：`npm run build` 成功。

5. 风险/待办
- 若后续版本内容较长，建议在弹窗内补“复制内容/下载为 Markdown”能力。

### 2026-03-01 #033 文档版本历史固定高度滚动优化

1. 任务目标
- 避免版本历史列表随数据增长无限拉长页面，改为固定高度并在列表区域内滚动。

2. 完成内容
- 版本历史列表容器增加专用类 `docVersionHistoryList`。
- 样式新增固定高度滚动：`max-height: 360px; overflow-y: auto;`。
- 仅影响文档中心“版本历史”区域，不改变其它列表布局。

3. 涉及文件
- `frontend/web-console/src/pages/collab/ProductDocCenterPage.tsx`
- `frontend/web-console/src/styles.css`

4. 验证状态
- 前端本机已通过：`npm run build` 成功。

5. 风险/待办
- 若后续你希望按屏幕高度自适应，可升级为 `max-height: calc(100vh - xxxpx)`。

### 2026-03-01 #034 文档发布流程可理解性优化（草稿/历史版本）

1. 任务目标
- 解决“用户不清楚什么时候写入历史版本”的问题，降低误操作成本。

2. 完成内容
- 文档编辑区新增流程说明：
  - 保存草稿：仅更新草稿，不写入历史版本。
  - 发布版本：将当前草稿快照写入历史版本。
- 新增草稿状态提示：`草稿有未保存改动 / 草稿已保存`。
- 发布逻辑优化：当存在未保存改动时，发布按钮会自动先保存草稿再发布版本，确保历史版本内容与编辑区一致。
- 发布按钮文案动态化：有未保存改动时显示“保存并发布版本”。

3. 涉及文件
- `frontend/web-console/src/pages/collab/ProductDocCenterPage.tsx`
- `frontend/web-console/src/styles.css`
- `frontend/web-console/src/i18n/locales/en-US/common.json`
- `frontend/web-console/src/i18n/locales/zh-CN/common.json`

4. 验证状态
- 前端本机已通过：`npm run build` 成功。

5. 风险/待办
- 后续可增加“离开页面前未保存提示”以进一步防止草稿丢失。

### 2026-03-01 #035 文档中心 AI 改写策略调整（候选模式 -> 直接改写当前草稿）

1. 任务目标
- 按协作语义优化文档 AI 流程：AI 不再作为“独立候选方案”使用，而是直接基于当前文档草稿持续改写与完善。

2. 完成内容
- 前端交互调整：
  - AI 指令执行前，若草稿有未保存改动，先自动保存草稿。
  - AI 返回后，自动将改写结果写回当前文档草稿并刷新编辑区。
  - 提示用户“AI 结果已写入当前草稿编辑区”。
- 流程说明文案更新：
  - 明确“AI 修订是直接更新当前草稿，确认无误后再发布版本”。
- UI 收敛：
  - 移除 AI 候选确认/驳回按钮，避免与“直接改写当前草稿”策略冲突。

3. 涉及文件
- `frontend/web-console/src/pages/collab/ProductDocCenterPage.tsx`
- `frontend/web-console/src/styles.css`
- `frontend/web-console/src/i18n/locales/en-US/common.json`
- `frontend/web-console/src/i18n/locales/zh-CN/common.json`

4. 验证状态
- 前端本机已通过：`npm run build` 成功。

5. 风险/待办
- 后端当前仍保留 `product_doc_revision` 记录用于审计追踪；后续可评估是否在界面上改名为“AI改写记录”以减少概念偏差。

### 2026-03-01 #036 文档中心信息架构重构（重AI、轻人力）

1. 任务目标
- 按“重 AI、轻人力”原则重构文档中心，减少人工操作路径。

2. 完成内容
- 页面结构收敛为两块：
  - 左侧：创建文档 + 文档列表
  - 右侧：AI 修订会话 + AI 改写记录
- 移除模块：
  - 文档编辑区
  - 版本历史区
  - 版本发布/回滚等人工版本操作入口（页面层）
- AI 改写流程：
  - 发送指令后，AI 结果直接写回当前文档草稿（非独立候选方案）
  - 页面提示“AI 结果已写入当前草稿编辑区”
- 文案全面改为“基于当前草稿持续改写”语义，去除候选方案导向词。

3. 涉及文件
- `frontend/web-console/src/pages/collab/ProductDocCenterPage.tsx`
- `frontend/web-console/src/styles.css`
- `frontend/web-console/src/i18n/locales/en-US/common.json`
- `frontend/web-console/src/i18n/locales/zh-CN/common.json`

4. 验证状态
- 前端本机已通过：`npm run build` 成功。

5. 风险/待办
- 后端仍保留版本接口与修订状态模型用于审计，后续可按产品决策继续收敛为“AI改写日志模型”。

### 2026-03-01 #037 全屏布局放宽（取消居中限制）

1. 任务目标
- 解决页面内容拥挤问题，提升大屏利用率。

2. 完成内容
- 取消页面容器最大宽度与居中限制，改为全宽布局。
- 增加页面左右内边距，保持信息密度同时避免贴边。
- 主工作区比例放宽：左侧导航列由 280px 调整为 320px，内容区可用宽度更大。

3. 涉及文件
- `frontend/web-console/src/styles.css`

4. 验证状态
- 前端本机已通过：`npm run build` 成功。

5. 风险/待办
- 如仍觉得拥挤，可继续按模块增加“可折叠左侧栏”与“页面密度切换（紧凑/舒展）”。

### 2026-03-01 #038 AI改写记录区简化为单一 Markdown 编辑器

1. 任务目标
- 按需求移除 AI 改写记录中的“卡片列表 + 点击预览”交互，仅保留一个 Markdown 编辑器。

2. 完成内容
- 文档中心右侧区域重构：
  - 保留 AI 修订会话。
  - AI 改写记录区改为单一 Markdown 编辑器。
- 移除功能：
  - 改写记录筛选器。
  - 卡片列表。
  - 点击查看差异/预览交互。
- 保留能力：
  - AI 改写结果继续直接写回当前草稿。
  - Markdown 编辑器支持手动微调并保存草稿。

3. 涉及文件
- `frontend/web-console/src/pages/collab/ProductDocCenterPage.tsx`
- `frontend/web-console/src/styles.css`
- `frontend/web-console/src/i18n/locales/en-US/common.json`
- `frontend/web-console/src/i18n/locales/zh-CN/common.json`

4. 验证状态
- 前端本机已通过：`npm run build` 成功。

5. 风险/待办
- 如后续希望进一步“轻人工”，可将“保存草稿”也改为自动保存（防抖）。

### 2026-03-01 #039 文档中心接入流式 AI 改写（SSE）

1. 任务目标
- 让产品文档 AI 改写体验从“请求完成后一次返回”升级为“流式输出”，并确保 AI 持续基于当前草稿改写。

2. 完成内容
- 前端文档中心改造为流式对话：
  - `改写当前草稿` 按钮改为调用 SSE 接口。
  - AI 输出按流实时写入：
    - AI 会话消息区（assistant 消息增量更新）
    - 右侧 Markdown 草稿编辑区（增量覆盖当前草稿）
  - 流结束后自动刷新文档详情与会话消息，确保前后端状态一致。
- 后端流式落库对齐：
  - 流式完成后将最终改写文档写入 AI assistant 消息，确保刷新页面后会话内容一致可追溯。
- 前端 SSE 解析增强：
  - 兼容 `\r\n` / `\n` 换行分隔。
  - 支持尾部缓冲区 flush，避免最后一段丢失。
- 协议补充：
  - OpenAPI 增加 `/api/v1/product-docs/{docId}/ai-chat/messages/stream`。

3. 涉及文件
- `frontend/web-console/src/pages/collab/ProductDocCenterPage.tsx`
- `frontend/web-console/src/api/client.ts`
- `contracts/openapi/base.yaml`

4. 验证状态
- 前端本机已通过：`npm run build` 成功（`tsc -b && vite build`）。

5. 风险/待办
- 后端 SSE 目前采用“输出整篇改写文档”的流式策略，后续可按需要追加“结构化增量（段落级）”事件类型。

### 2026-03-01 #040 SSE接口异常响应序列化修复（text/event-stream 与 JSON 冲突）

1. 任务目标
- 修复流式接口在异常场景下触发全局异常处理时，`ApiResponse` 无法按 `text/event-stream` 序列化导致的二次报错。

2. 完成内容
- 全局异常处理统一显式设置响应类型为 `application/json`：
  - `ResponseEntity.status(...).contentType(MediaType.APPLICATION_JSON).body(...)`
- 覆盖所有 `@ExceptionHandler` 返回，确保即使命中 SSE 路由也能稳定返回 JSON 错误体。

3. 涉及文件
- `backend/api-gateway/src/main/java/com/spider/apigateway/exception/GlobalExceptionHandler.java`

4. 验证状态
- 代码修复完成；当前环境缺少 `mvn`，需在开发机启动后端后验证。

5. 风险/待办
- 后续可进一步针对 SSE 接口增加“输入校验失败转 SSE error 事件”的统一策略，减少前端分支处理。

### 2026-03-01 #041 SSE stream_failed 根因修复（RequestContext 异步线程丢失）

1. 任务目标
- 修复流式改写返回 `event:error` 且内容为 `stream_failed` 的问题。

2. 根因
- `ProductDocService.streamAiMessage` 使用 `CompletableFuture.runAsync`。
- `RequestContext` 基于 `ThreadLocal`，切换线程后丢失 `actor/role/requestId`，导致权限校验等逻辑异常。
- 异常类（如 `ProductDocAccessDeniedException`）无 message，前端仅收到兜底 `stream_failed`。

3. 完成内容
- 在流式任务启动前捕获 `requestId/actor/role`，异步线程内显式恢复 `RequestContext`，结束后清理。
- SSE error 事件改为结构化 JSON（`code/detail/requestId`），并记录完整堆栈日志。
- 前端增加 SSE error JSON 解析，优先展示 `detail`。

4. 涉及文件
- `backend/api-gateway/src/main/java/com/spider/apigateway/doc/ProductDocService.java`
- `frontend/web-console/src/pages/collab/ProductDocCenterPage.tsx`

5. 验证状态
- 前端本机已通过：`npm run build` 成功（`tsc -b && vite build`）。
- 后端需在开发机重启后验证流式接口。
