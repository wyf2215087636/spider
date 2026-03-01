# AI研发协作平台 - 国际化（中英）方案 v0.1

- 版本：v0.1
- 日期：2026-03-01
- 适用阶段：MVP到M2
- 决策状态：执行基线

## 1. 目标范围

首期支持 `zh-CN` 和 `en-US` 双语能力，覆盖：
1. 前端界面文案
2. 系统状态与错误提示
3. Agent输出模板（可选双语）
4. 自动生成文档（支持中/英文模板）

## 2. 语言策略

1. 默认语言：`zh-CN`
2. 用户可在个人设置切换语言
3. Workspace可配置默认语言（新成员默认继承）
4. URL可带 `?lang=zh-CN|en-US` 覆盖当前会话语言

## 3. 前端国际化实现

1. 技术建议：`react-i18next`
2. 目录建议：
- `frontend/web-console/src/i18n/index.ts`
- `frontend/web-console/src/i18n/locales/zh-CN/*.json`
- `frontend/web-console/src/i18n/locales/en-US/*.json`
3. Key命名：`page.module.section.key`
4. 禁止硬编码文案，所有UI文案走 `t('...')`
5. 日期时间按语言+时区格式化展示

## 4. 后端国际化实现

1. 技术建议：`Spring MessageSource`
2. 错误码和消息分离：接口返回 `code` + 本地化 `message`
3. 支持 `Accept-Language` 请求头
4. 消息资源建议：
- `messages_zh_CN.properties`
- `messages_en_US.properties`

## 5. Agent与文档双语策略

1. Agent输出支持参数：`output_language`
2. 文档模板按语言区分：
- `prd.template.zh-CN.md`
- `prd.template.en-US.md`
3. 审批/审计日志可保留原文 + 目标语言摘要

## 6. 状态文案标准（中英）

统一状态码，不同语言渲染文本：
1. `draft`：`草稿` / `Draft`
2. `in_progress`：`进行中` / `In Progress`
3. `blocked`：`阻塞` / `Blocked`
4. `in_review`：`评审中` / `In Review`
5. `done`：`已完成` / `Done`
6. `failed`：`失败` / `Failed`

## 7. 验收标准

1. 语言切换后页面文案100%切换，无硬编码残留
2. 同一错误码可返回中英文消息
3. Chat快捷模板支持中英文
4. 任务状态、风险状态、审批状态可中英切换

## 8. 风险与控制

1. 文案分叉风险：建立术语表（Glossary）统一翻译
2. 版本不同步风险：新增文案必须在两种语言补齐
3. 布局溢出风险：英文长文案需预留自适应空间
