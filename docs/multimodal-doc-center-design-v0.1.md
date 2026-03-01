# 多模态文档中心设计方案 v0.2

- 版本：v0.2
- 日期：2026-03-01
- 适用阶段：MVP 迭代（T8.1 - T8.3）
- 决策状态：进行中

## 1. 目标

1. 在平台内完成产品文档编写、AI 辅助改稿、版本管理、回滚。
2. AI 生成结果必须“先候选、后确认”，确认后才进入正式版本。
3. 支持像 Git 一样查看历史版本，并可回滚到任意历史版本（回滚生成新版本，不覆盖历史）。
4. 为后续图片/流程图等多模态理解能力预留数据结构和异步处理链路。

## 2. 分阶段落地

### T8.1 已落地（文档中心基础版）

1. 文档主表 + 版本快照表：`product_doc`、`product_doc_version`。
2. 文档 CRUD：创建、列表、详情、草稿更新。
3. 版本管理：发布版本、查看历史、查看指定版本。
4. 回滚：指定历史版本回滚为新版本。

### T8.2 已落地（AI 修订候选 + 人工确认）

1. 新增 AI 会话消息表：`product_doc_chat_message`。
2. 新增 AI 修订候选表：`product_doc_revision`。
3. 新增流程：
- 产品输入改稿指令。
- AI 生成候选修订（不直接覆盖正式版本）。
- 产品确认后发布为 `ai_confirm` 新版本。
- 可驳回候选并保留留痕。
4. 前端文档中心新增“AI 修订会话 + 候选列表 + 预览 + 确认/驳回”。

### T8.3 规划中（多模态能力）

1. 图片/附件上传能力：
- `product_doc_asset`（文档资源）
- `product_doc_asset_analysis`（识别结果）
2. 异步识别流水线：上传 -> OCR/图像理解 -> 结果校验 -> 入库。
3. AI 改稿提示词注入图像识别结果，支持“图文混合文档”改稿。

## 3. 核心流程

1. 文档草稿编辑：持续保存 `draft_content`。
2. 手工发布版本：`draft_content` -> `product_doc_version` 快照。
3. AI 候选修订：
- 指令进入 `product_doc_chat_message`。
- AI 返回候选内容，写入 `product_doc_revision(status=pending)`。
4. 确认候选：
- 候选内容写回草稿。
- 自动发布新版本（`source_type=ai_confirm`）。
- 候选状态改为 `confirmed`。
5. 驳回候选：候选状态改为 `rejected`，写入会话留痕。

## 4. 权限策略（当前）

1. 创建文档：`product/admin`。
2. 编辑草稿、AI 修订、确认/驳回、发布、回滚：
- `admin` 可操作全部文档。
- `product` 仅可操作自己创建的文档。
3. 其他角色：只读文档与版本。

## 5. 数据模型总览

1. `product_doc`
- 文档主记录（草稿、负责人、当前版本指针、状态）。
2. `product_doc_version`
- 不可变历史版本快照。
3. `product_doc_chat_message`
- 文档改稿会话消息（user/assistant/system）。
4. `product_doc_revision`
- AI 修订候选及其确认状态。

## 6. API 总览（当前）

1. 文档基础：
- `POST /api/v1/product-docs`
- `GET /api/v1/product-docs`
- `GET /api/v1/product-docs/{docId}`
- `PUT /api/v1/product-docs/{docId}`
2. 版本管理：
- `POST /api/v1/product-docs/{docId}/publish-version`
- `GET /api/v1/product-docs/{docId}/versions`
- `GET /api/v1/product-docs/{docId}/versions/{versionId}`
- `POST /api/v1/product-docs/{docId}/rollback`
3. AI 修订：
- `GET /api/v1/product-docs/{docId}/ai-chat/messages`
- `POST /api/v1/product-docs/{docId}/ai-chat/messages`
- `GET /api/v1/product-docs/{docId}/ai-revisions`
- `POST /api/v1/product-docs/{docId}/ai-revisions/{revisionId}/confirm`
- `POST /api/v1/product-docs/{docId}/ai-revisions/{revisionId}/reject`

## 7. 下一步建议

1. T8.3：完成附件上传与 OCR/图像识别异步链路。
2. T8.4：支持“从指定文档版本一键生成需求包并进入交付中心”。
3. T8.5：版本 Diff、评审评论、质量评分与发布门禁。
