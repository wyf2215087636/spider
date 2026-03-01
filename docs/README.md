# Docs 索引与维护规则

## 1. 当前文档

1. `mvp-prd-v0.1.md`：MVP产品需求文档
2. `technical-architecture-blueprint-v0.1.md`：技术架构蓝图
3. `technical-architecture-java-delta-v0.1.md`：技术架构Java增量说明（现行后端实现基线）
4. `monorepo-frontend-backend-strategy-v0.1.md`：前后端分离 + Monorepo落地方案（历史版）
5. `monorepo-frontend-backend-strategy-v0.2.md`：前后端分离 + Monorepo落地方案（Java后端现行版）
6. `fullstack-implementation-roadmap-v0.1.md`：全栈逐步落地路线图
7. `chat-ui-design-spec-v0.1.md`：Chat工作台UI设计规范（现行前端体验基线）
8. `i18n-globalization-strategy-v0.1.md`：国际化（中英）方案（现行语言基线）
9. `ai-development-worklog-v0.1.md`：开发留存日志（持续追加）

## 2. 新增文档规则

1. 命名规范：`<topic>-v<major>.<minor>.md`
2. 每份文档必须包含：版本、日期、适用阶段、决策状态
3. 方案更新不覆盖旧版本，新增版本文件并在本索引追加记录

## 3. 当前执行基线

1. 架构执行基线：`monorepo-frontend-backend-strategy-v0.2.md`
2. 后端实现基线：`technical-architecture-java-delta-v0.1.md`
3. 迭代执行基线：`fullstack-implementation-roadmap-v0.1.md`
4. 前端体验基线：`chat-ui-design-spec-v0.1.md`
5. 国际化基线：`i18n-globalization-strategy-v0.1.md`
6. 开发留存基线：`ai-development-worklog-v0.1.md`

## 4. 推荐主题清单（下一步）

1. `jira-delivery-template-v0.1.md`：Epic/Story/Task模板与自动化规则
2. `implementation-wbs-v0.1.md`：MVP实施WBS与人天估算
3. `api-contract-openapi-v0.1.md`：接口契约草案
4. `data-model-ddl-v0.1.md`：数据库DDL草案
5. `security-and-permission-model-v0.1.md`：权限与审计模型

## 5. 协作留存约定

1. 每次代码开发完成后，必须更新 `ai-development-worklog-v0.1.md`
2. 记录内容最少包括：目标、完成项、核心文件、验证状态、下一步
