# AI研发协作平台 - 技术架构蓝图 Java增量说明 v0.1

- 版本：v0.1
- 日期：2026-03-01
- 适用阶段：MVP到M2
- 决策状态：已确认
- 关联文档：`technical-architecture-blueprint-v0.1.md`

## 1. 目的

本文件用于明确替换 `technical-architecture-blueprint-v0.1.md` 中的后端技术实现基线，避免与当前Java方案冲突。

## 2. 变更范围

### 2.1 后端技术选型替换

将原“Node.js(NestJS) 或 Go(Fiber)”替换为：
1. `JDK 17 + Spring Boot 3`
2. `MyBatis-Plus`
3. `PostgreSQL 15 + Redis`
4. `Temporal Java SDK`
5. `Flyway`

### 2.2 服务模块命名建议

1. `api-gateway`：统一入口、鉴权、限流、追踪ID
2. `project-service`：Workspace/Project/Requirement核心域
3. `orchestration-service`：Agent编排与策略路由
4. `workflow-worker`：流程执行与补偿重试
5. `rag-service`：知识索引与检索
6. `tool-runtime-service`：Git/Jira/Confluence/CI适配

### 2.3 数据访问规范

1. 领域表访问统一通过 `MyBatis-Plus` Mapper
2. 复杂查询可使用XML或注解SQL，但必须保留可测试性
3. 所有DDL变更通过 `Flyway` 版本脚本管理

## 3. API契约与前后端联调规则

1. OpenAPI是接口单一事实源
2. 前端SDK由OpenAPI自动生成
3. 接口变更必须同时提交：`OpenAPI + 后端实现 + 前端适配`
4. CI增加契约一致性校验

## 4. 可观测性与治理补充

1. 指标：Micrometer -> Prometheus -> Grafana
2. 链路追踪：OpenTelemetry
3. 日志：结构化日志 + request_id 全链路透传
4. 审计：工具调用与关键AI决策强制落库

## 5. 当前执行基线

1. 架构总览仍以 `technical-architecture-blueprint-v0.1.md` 为主
2. 后端实现细节以本文件 + `monorepo-frontend-backend-strategy-v0.2.md` 为准
