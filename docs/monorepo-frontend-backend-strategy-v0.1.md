# AI研发协作平台 - Monorepo落地方案 v0.1

- 版本：v0.1
- 日期：2026-03-01
- 决策状态：建议采用

## 1. 决策结论

采用“前后端分离架构 + 单仓Monorepo工程”：
1. 架构分离：前端独立部署，后端服务独立部署。
2. 工程同仓：`apps + services + packages` 统一管理。

## 2. 选择理由

1. 满足平台型系统对安全、权限、审计、扩展的要求。
2. 前后端可以独立发布，减少互相阻塞。
3. 共享类型和接口契约，降低协作成本和联调错误。
4. 便于后续拆分微服务，不需要重做工程结构。

## 3. 仓库结构模板

```text
spider/
  apps/
    web/                        # 前端控制台（Chat/看板/文档中心）
    api-gateway/                # API网关（鉴权、聚合、限流）
  services/
    orchestrator/               # Agent编排服务
    workflow-worker/            # 工作流执行器（Temporal Worker）
    rag-service/                # 检索与知识索引服务
    tool-runtime/               # Git/Jira/Docs/CI工具适配层
  packages/
    ui-kit/                     # 前端共享组件
    shared-types/               # 共享类型（由OpenAPI生成）
    eslint-config/              # 统一Lint规则
    tsconfig/                   # 统一TS配置
    sdk/                        # 前端调用后端SDK
  infra/
    docker/                     # 本地开发编排
    k8s/                        # K8s部署清单
    terraform/                  # 云资源定义（可选）
  scripts/
    dev/                        # 开发辅助脚本
    ci/                         # CI脚本
  docs/
    mvp-prd-v0.1.md
    technical-architecture-blueprint-v0.1.md
    monorepo-frontend-backend-strategy-v0.1.md
```

## 4. 技术栈建议（MVP）

### 4.1 前端（apps/web）
1. `React + TypeScript + Vite`
2. `React Router`
3. `TanStack Query`（服务端状态管理）
4. `Ant Design`（管理后台提速）
5. `ECharts`（项目健康度与风险图表）

### 4.2 后端（apps/services）
1. `NestJS`（API Gateway + 业务服务）
2. `Temporal`（流程编排）
3. `PostgreSQL + Redis`
4. `pgvector`（MVP检索）

### 4.3 工程与质量
1. `pnpm workspace + Turborepo`
2. `ESLint + Prettier + Commitlint + Husky`
3. `Vitest`（单测）+ `Playwright`（端到端）
4. `OpenAPI` 作为接口契约单一事实源

## 5. 前后端通信规范

1. API统一前缀：`/api/v1`
2. 契约优先：先维护 `OpenAPI`，再生成 `shared-types` 与 `sdk`
3. 错误码统一：`code/message/request_id/details`
4. 鉴权统一：`OIDC + JWT`，按 Workspace 做数据隔离
5. 长任务接口采用“提交任务 + 轮询/推送状态”模式

## 6. 发布与环境策略

1. 环境：`dev / test / prod` 三套隔离
2. 发布节奏：
   - 前端：按需快速发布
   - 后端：按服务独立发布
3. CI流程：
   - PR触发：lint + test + contract check
   - 主干合并：构建镜像 + 自动部署到test

## 7. 里程碑执行建议（4周起步）

1. 第1周：Monorepo骨架、基础CI、鉴权与健康检查接口。
2. 第2周：前端控制台框架 + API Gateway + OpenAPI打通。
3. 第3周：需求拆解链路最小闭环（UI -> API -> Workflow）。
4. 第4周：任务同步与风险看板首版上线（试点团队内测）。

## 8. 设计约束

1. 所有跨服务DTO必须来自 `packages/shared-types`。
2. 禁止前端直接拼接后端内部字段，统一走 `packages/sdk`。
3. 工具调用必须落审计日志，不允许绕过 `tool-runtime`。
4. 任何新增方案文档统一放在 `docs/`，并更新 `docs/README.md`。

## 9. 备选方案与取舍

1. 方案A：单体同进程（前后端不分离）
   - 优点：初期上手快
   - 风险：后期扩展和权限治理成本高
2. 方案B：多仓库拆分
   - 优点：边界清晰
   - 风险：契约同步和跨团队协作成本更高
3. 结论：当前阶段优先“分离架构 + 单仓Monorepo”。
