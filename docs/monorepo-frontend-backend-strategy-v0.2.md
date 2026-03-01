# AI研发协作平台 - Monorepo落地方案 v0.2

- 版本：v0.2
- 日期：2026-03-01
- 适用阶段：MVP到M2
- 决策状态：已确认（Java后端）

## 1. 决策结论

采用“前后端分离架构 + 单仓Monorepo工程”，技术栈明确为：
1. 前端：`React + TypeScript + Vite`
2. 后端：`JDK 17 + Spring Boot 3 + MyBatis-Plus`
3. 数据：`PostgreSQL 15 + Redis`
4. 迁移：`Flyway`

## 2. 仓库结构（全栈同工作空间）

```text
spider/
  frontend/
    web-console/                    # 主前端应用（Chat/看板/文档中心）
  backend/
    pom.xml                         # Maven聚合工程
    common/                         # 公共模块（响应体、异常、工具）
    api-gateway/                    # API网关（鉴权、聚合、限流）
    project-service/                # 项目与工作空间服务
    orchestration-service/          # Agent编排服务
    workflow-worker/                # 工作流执行器（Temporal Worker）
    rag-service/                    # 检索与索引服务
    tool-runtime-service/           # Git/Jira/Confluence适配层
  contracts/
    openapi/                        # OpenAPI契约文件（单一事实源）
  packages/
    frontend-sdk/                   # 由OpenAPI生成的TS SDK
    shared-schemas/                 # 共享JSON Schema
  infra/
    docker-compose/                 # 本地依赖(PostgreSQL/Redis/Temporal)
    k8s/                            # 部署清单
  scripts/
    dev/                            # 本地启动脚本
    ci/                             # CI流水线脚本
  docs/
    README.md
    mvp-prd-v0.1.md
    technical-architecture-blueprint-v0.1.md
    monorepo-frontend-backend-strategy-v0.1.md
    monorepo-frontend-backend-strategy-v0.2.md
```

## 3. 技术基线

### 3.1 前端基线
1. `React 18` + `TypeScript 5`
2. `Vite 6`
3. `React Router`
4. `TanStack Query`
5. `Ant Design`
6. `react-i18next`（中英双语）

### 3.2 后端基线
1. `Spring Boot 3.x`
2. `Spring Security`
3. `MyBatis-Plus`
4. `Flyway`
5. `PostgreSQL 15`
6. `Redis`
7. `Temporal Java SDK`（流程编排）
8. `MessageSource`（基于 `Accept-Language` 的消息国际化）

### 3.3 工程与质量
1. 前端使用 `pnpm`
2. 后端使用 `Maven` 多模块
3. 契约优先：`OpenAPI` -> 生成前端SDK
4. CI门禁：`lint + unit test + contract check`

## 4. 前后端协作规范

1. API前缀统一：`/api/v1`
2. 所有接口先更新 `contracts/openapi`，后实现代码
3. 错误结构统一：`code/message/request_id/details`
4. 长任务采用：提交任务 -> 返回 `run_id` -> 轮询或SSE获取状态
5. 前端不直连数据库，不绕过后端服务
6. 语言协商统一：前端传 `Accept-Language`，后端按语言返回消息

## 5. 数据库与ORM规范

1. ORM统一使用 `MyBatis-Plus`
2. 表结构变更必须通过 `Flyway` 脚本
3. 主键建议 `UUID`（或雪花ID，二选一并统一）
4. 审计字段统一：`created_at/created_by/updated_at/updated_by`
5. 时间字段统一存储 `UTC`，展示按用户时区转换

## 6. 环境与部署

1. 环境：`dev/test/prod`
2. 本地依赖通过 `docker compose` 一键拉起 PostgreSQL/Redis/Temporal
3. 前后端可独立发布，版本通过Git Tag关联

## 7. 版本策略

1. `v0.1` 作为历史方案保留
2. `v0.2` 作为当前执行基线
3. 后续变更新增 `v0.3+` 文档，不覆盖旧版本
