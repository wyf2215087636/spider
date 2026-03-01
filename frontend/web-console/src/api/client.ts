export interface HealthResult {
  status: string;
  serverTime: string;
  message: string;
}

export interface WorkspaceItem {
  id: string;
  name: string;
  owner: string;
  status: string;
  defaultLanguage: "zh-CN" | "en-US";
  createdAt: string;
  updatedAt: string;
}

export interface ProjectItem {
  id: string;
  workspaceId: string;
  name: string;
  status: string;
  createdAt: string;
  updatedAt: string;
}

export interface ProductDocItem {
  id: string;
  projectId: string;
  title: string;
  status: "draft" | "active" | "archived";
  ownerActor: string;
  currentVersionId: string | null;
  currentVersionNo: number | null;
  createdAt: string;
  updatedAt: string;
}

export interface ProductDocVersionItem {
  id: string;
  docId: string;
  versionNo: number;
  parentVersionId: string | null;
  content: string;
  changeSummary: string;
  sourceType: "manual" | "ai_confirm" | "rollback";
  createdBy: string;
  createdAt: string;
}

export interface ProductDocDetailItem extends ProductDocItem {
  draftContent: string;
  currentVersion: ProductDocVersionItem | null;
}

export interface ProductDocAiMessageItem {
  id: string;
  docId: string;
  role: "user" | "assistant" | "system";
  content: string;
  createdBy: string;
  createdAt: string;
}

export interface ProductDocRevisionItem {
  id: string;
  docId: string;
  sourceVersionId: string | null;
  baseContent: string;
  candidateContent: string;
  instruction: string;
  changeSummary: string;
  status: "pending" | "confirmed" | "rejected";
  modelProvider: string | null;
  modelName: string | null;
  createdBy: string;
  confirmedBy: string | null;
  confirmedAt: string | null;
  createdAt: string;
  updatedAt: string;
}

export interface ProductDocAiGenerateItem {
  assistantMessage: ProductDocAiMessageItem;
  revision: ProductDocRevisionItem;
}

export interface SseEventPayload {
  event: string;
  data: string;
}

export interface AuditLogItem {
  id: string;
  requestId: string;
  actor: string;
  action: string;
  resourceType: string;
  resourceId: string;
  status: string;
  details: string;
  createdAt: string;
}

export interface WorkspaceAuditFilterOption {
  resourceId: string;
  name: string;
  deleted: boolean;
}

export interface ProjectAuditFilterOption {
  resourceId: string;
  name: string;
  deleted: boolean;
}

export interface ChatSessionItem {
  id: string;
  projectId: string;
  ownerActor: string;
  role: "product" | "backend" | "frontend" | "test";
  title: string;
  status: "active" | "archived";
  createdAt: string;
  updatedAt: string;
}

export interface ChatMessageItem {
  id: string;
  sessionId: string;
  role: "user" | "assistant" | "system";
  content: string;
  createdBy: string;
  createdAt: string;
}

export interface AiDraftItem {
  title: string;
  requirementSummary: string;
  acceptanceCriteria: string;
  impactScope: string;
  priority: "P0" | "P1" | "P2" | "P3";
  targetRole: "product" | "backend" | "frontend" | "test";
  suggestedTasks: string[];
  impactedFiles: string[];
  riskHints: string[];
  testHints: string[];
  aiReply: string;
  provider: string;
  model: string;
}

export interface RequirementHandoffItem {
  id: string;
  projectId: string;
  sourceSessionId: string;
  version: number;
  title: string;
  requirementSummary: string;
  acceptanceCriteria: string;
  impactScope: string;
  priority: "P0" | "P1" | "P2" | "P3";
  targetRole: "product" | "backend" | "frontend" | "test";
  status:
    | "draft"
    | "in_review"
    | "published"
    | "accepted"
    | "in_development"
    | "in_testing"
    | "done"
    | "rejected";
  publishedBy: string;
  publishedAt: string;
  acceptedBy: string | null;
  acceptedAt: string | null;
  createdAt: string;
  updatedAt: string;
}

export interface RequirementTaskItem {
  id: string;
  handoffId: string;
  projectId: string;
  role: "product" | "backend" | "frontend" | "test";
  title: string;
  titleZh: string;
  titleEn: string;
  description: string;
  descriptionZh: string;
  descriptionEn: string;
  estimateHours: number;
  status: "todo" | "in_progress" | "done" | "blocked";
  assignee: string | null;
  source: "ai" | "manual";
  sortOrder: number;
  createdAt: string;
  updatedAt: string;
}

export type TaskCenterView = "pool" | "mine" | "all";

export interface TaskCenterPageResult {
  items: RequirementTaskItem[];
  total: number;
  page: number;
  size: number;
  hasNext: boolean;
}

export interface RequirementTaskDetailItem extends RequirementTaskItem {
  projectName: string | null;
  handoffTitle: string;
  handoffRequirementSummary: string;
  handoffAcceptanceCriteria: string;
  handoffImpactScope: string | null;
  handoffPriority: "P0" | "P1" | "P2" | "P3";
  handoffTargetRole: "product" | "backend" | "frontend" | "test";
  handoffStatus:
    | "draft"
    | "in_review"
    | "published"
    | "accepted"
    | "in_development"
    | "in_testing"
    | "done"
    | "rejected";
}

export interface AiTaskPlanItem {
  handoffId: string;
  tasks: RequirementTaskItem[];
  impactedFiles: string[];
  riskHints: string[];
  testHints: string[];
  provider: string;
  model: string;
  rationale: string;
}

export interface UserProfile {
  userId: string;
  username: string;
  displayName: string;
  role: "product" | "backend" | "frontend" | "test" | "pmo" | "admin";
}

export interface LoginResult {
  token: string;
  user: UserProfile;
}

interface ApiResponse<T> {
  code: string;
  message: string;
  data: T;
  timestamp: string;
  requestId?: string;
}

const TOKEN_STORAGE_KEY = "spider.auth.token";

export function readAuthToken(): string {
  if (typeof window === "undefined") {
    return "";
  }
  const raw = window.localStorage.getItem(TOKEN_STORAGE_KEY);
  return raw && raw.trim() ? raw.trim() : "";
}

export function writeAuthToken(token: string): void {
  if (typeof window === "undefined") {
    return;
  }
  const finalValue = token.trim();
  if (finalValue) {
    window.localStorage.setItem(TOKEN_STORAGE_KEY, finalValue);
  } else {
    window.localStorage.removeItem(TOKEN_STORAGE_KEY);
  }
}

function buildHeaders(
  language: string,
  withJson: boolean = false,
  withAuth: boolean = true
): HeadersInit {
  const headers: Record<string, string> = {
    "Accept-Language": language
  };
  if (withAuth) {
    const token = readAuthToken();
    if (token) {
      headers.Authorization = `Bearer ${token}`;
    }
  }
  if (withJson) {
    headers["Content-Type"] = "application/json";
  }
  return headers;
}

async function readError(response: Response): Promise<never> {
  try {
    const payload = (await response.json()) as ApiResponse<unknown>;
    const msg = payload.message || `HTTP ${response.status}`;
    const withReqId = payload.requestId ? `${msg} (requestId: ${payload.requestId})` : msg;
    // Keep a structured error print for faster debugging during development.
    console.error("API_ERROR", {
      status: response.status,
      code: payload.code,
      message: payload.message,
      requestId: payload.requestId
    });
    throw new Error(withReqId);
  } catch (e) {
    if (e instanceof Error) {
      throw e;
    }
    throw new Error(`HTTP ${response.status}`);
  }
}

export async function fetchHealth(language: string): Promise<HealthResult> {
  const response = await fetch("/api/v1/health", {
    headers: buildHeaders(language, false, false)
  });

  if (!response.ok) {
    return readError(response);
  }

  const payload = (await response.json()) as ApiResponse<HealthResult>;
  return payload.data;
}

export async function listWorkspaces(language: string): Promise<WorkspaceItem[]> {
  const response = await fetch("/api/v1/workspaces", {
    headers: buildHeaders(language)
  });
  if (!response.ok) {
    return readError(response);
  }
  const payload = (await response.json()) as ApiResponse<WorkspaceItem[]>;
  return payload.data;
}

export async function listProjects(
  language: string,
  workspaceId?: string
): Promise<ProjectItem[]> {
  const query = new URLSearchParams();
  if (workspaceId) {
    query.set("workspaceId", workspaceId);
  }
  const suffix = query.toString() ? `?${query.toString()}` : "";
  const response = await fetch(`/api/v1/projects${suffix}`, {
    headers: buildHeaders(language)
  });
  if (!response.ok) {
    return readError(response);
  }
  const payload = (await response.json()) as ApiResponse<ProjectItem[]>;
  return payload.data;
}

export async function createProject(
  language: string,
  body: {
    workspaceId: string;
    name: string;
    status: "draft" | "active" | "archived";
  }
): Promise<ProjectItem> {
  const response = await fetch("/api/v1/projects", {
    method: "POST",
    headers: buildHeaders(language, true),
    body: JSON.stringify(body)
  });
  if (!response.ok) {
    return readError(response);
  }
  const payload = (await response.json()) as ApiResponse<ProjectItem>;
  return payload.data;
}

export async function updateProject(
  projectId: string,
  language: string,
  body: {
    name: string;
    status: "draft" | "active" | "archived";
  }
): Promise<ProjectItem> {
  const response = await fetch(`/api/v1/projects/${projectId}`, {
    method: "PUT",
    headers: buildHeaders(language, true),
    body: JSON.stringify(body)
  });
  if (!response.ok) {
    return readError(response);
  }
  const payload = (await response.json()) as ApiResponse<ProjectItem>;
  return payload.data;
}

export async function deleteProject(projectId: string, language: string): Promise<void> {
  const response = await fetch(`/api/v1/projects/${projectId}`, {
    method: "DELETE",
    headers: buildHeaders(language)
  });
  if (!response.ok) {
    return readError(response);
  }
}

export async function createProductDoc(
  language: string,
  body: {
    projectId: string;
    title: string;
    initialContent?: string;
  }
): Promise<ProductDocDetailItem> {
  const response = await fetch("/api/v1/product-docs", {
    method: "POST",
    headers: buildHeaders(language, true),
    body: JSON.stringify(body)
  });
  if (!response.ok) {
    return readError(response);
  }
  const payload = (await response.json()) as ApiResponse<ProductDocDetailItem>;
  return payload.data;
}

export async function listProductDocs(
  language: string,
  params?: { projectId?: string }
): Promise<ProductDocItem[]> {
  const query = new URLSearchParams();
  if (params?.projectId) {
    query.set("projectId", params.projectId);
  }
  const suffix = query.toString() ? `?${query.toString()}` : "";
  const response = await fetch(`/api/v1/product-docs${suffix}`, {
    headers: buildHeaders(language)
  });
  if (!response.ok) {
    return readError(response);
  }
  const payload = (await response.json()) as ApiResponse<ProductDocItem[]>;
  return payload.data;
}

export async function getProductDoc(
  docId: string,
  language: string
): Promise<ProductDocDetailItem> {
  const response = await fetch(`/api/v1/product-docs/${docId}`, {
    headers: buildHeaders(language)
  });
  if (!response.ok) {
    return readError(response);
  }
  const payload = (await response.json()) as ApiResponse<ProductDocDetailItem>;
  return payload.data;
}

export async function updateProductDoc(
  docId: string,
  language: string,
  body: {
    title: string;
    draftContent: string;
    status: "draft" | "active" | "archived";
  }
): Promise<ProductDocDetailItem> {
  const response = await fetch(`/api/v1/product-docs/${docId}`, {
    method: "PUT",
    headers: buildHeaders(language, true),
    body: JSON.stringify(body)
  });
  if (!response.ok) {
    return readError(response);
  }
  const payload = (await response.json()) as ApiResponse<ProductDocDetailItem>;
  return payload.data;
}

export async function publishProductDocVersion(
  docId: string,
  language: string,
  body?: {
    changeSummary?: string;
    sourceType?: "manual" | "ai_confirm";
  }
): Promise<ProductDocDetailItem> {
  const response = await fetch(`/api/v1/product-docs/${docId}/publish-version`, {
    method: "POST",
    headers: buildHeaders(language, true),
    body: JSON.stringify(body || {})
  });
  if (!response.ok) {
    return readError(response);
  }
  const payload = (await response.json()) as ApiResponse<ProductDocDetailItem>;
  return payload.data;
}

export async function listProductDocVersions(
  docId: string,
  language: string
): Promise<ProductDocVersionItem[]> {
  const response = await fetch(`/api/v1/product-docs/${docId}/versions`, {
    headers: buildHeaders(language)
  });
  if (!response.ok) {
    return readError(response);
  }
  const payload = (await response.json()) as ApiResponse<ProductDocVersionItem[]>;
  return payload.data;
}

export async function getProductDocVersion(
  docId: string,
  versionId: string,
  language: string
): Promise<ProductDocVersionItem> {
  const response = await fetch(`/api/v1/product-docs/${docId}/versions/${versionId}`, {
    headers: buildHeaders(language)
  });
  if (!response.ok) {
    return readError(response);
  }
  const payload = (await response.json()) as ApiResponse<ProductDocVersionItem>;
  return payload.data;
}

export async function rollbackProductDoc(
  docId: string,
  language: string,
  body: {
    targetVersionId: string;
    changeSummary?: string;
  }
): Promise<ProductDocDetailItem> {
  const response = await fetch(`/api/v1/product-docs/${docId}/rollback`, {
    method: "POST",
    headers: buildHeaders(language, true),
    body: JSON.stringify(body)
  });
  if (!response.ok) {
    return readError(response);
  }
  const payload = (await response.json()) as ApiResponse<ProductDocDetailItem>;
  return payload.data;
}

export async function listProductDocAiMessages(
  docId: string,
  language: string
): Promise<ProductDocAiMessageItem[]> {
  const response = await fetch(`/api/v1/product-docs/${docId}/ai-chat/messages`, {
    headers: buildHeaders(language)
  });
  if (!response.ok) {
    return readError(response);
  }
  const payload = (await response.json()) as ApiResponse<ProductDocAiMessageItem[]>;
  return payload.data;
}

export async function sendProductDocAiMessage(
  docId: string,
  language: string,
  body: { content: string }
): Promise<ProductDocAiGenerateItem> {
  const response = await fetch(`/api/v1/product-docs/${docId}/ai-chat/messages`, {
    method: "POST",
    headers: buildHeaders(language, true),
    body: JSON.stringify(body)
  });
  if (!response.ok) {
    return readError(response);
  }
  const payload = (await response.json()) as ApiResponse<ProductDocAiGenerateItem>;
  return payload.data;
}

export async function streamProductDocAiMessage(
  docId: string,
  language: string,
  body: { content: string },
  onEvent: (payload: SseEventPayload) => void
): Promise<void> {
  const response = await fetch(`/api/v1/product-docs/${docId}/ai-chat/messages/stream`, {
    method: "POST",
    headers: buildHeaders(language, true),
    body: JSON.stringify(body)
  });
  if (!response.ok) {
    return readError(response);
  }
  if (!response.body) {
    throw new Error("Streaming is not available in current environment");
  }

  const reader = response.body.getReader();
  const decoder = new TextDecoder("utf-8");
  let buffer = "";

  const emitBlock = (block: string) => {
    if (!block.trim()) {
      return;
    }
    let event = "message";
    const dataLines: string[] = [];
    const lines = block.split("\n");
    lines.forEach((line) => {
      if (line.startsWith("event:")) {
        event = line.slice("event:".length).trim();
      } else if (line.startsWith("data:")) {
        dataLines.push(line.slice("data:".length).trimStart());
      }
    });
    onEvent({ event, data: dataLines.join("\n") });
  };

  const normalizeChunk = (text: string) => text.replace(/\r\n/g, "\n").replace(/\r/g, "\n");

  while (true) {
    const chunk = await reader.read();
    if (chunk.done) {
      break;
    }
    buffer += normalizeChunk(decoder.decode(chunk.value, { stream: true }));
    let separatorIndex = buffer.indexOf("\n\n");
    while (separatorIndex >= 0) {
      const block = buffer.slice(0, separatorIndex);
      buffer = buffer.slice(separatorIndex + 2);
      emitBlock(block);
      separatorIndex = buffer.indexOf("\n\n");
    }
  }

  buffer += normalizeChunk(decoder.decode());
  if (buffer.trim()) {
    emitBlock(buffer);
  }
}

export async function listProductDocRevisions(
  docId: string,
  language: string,
  params?: { status?: "pending" | "confirmed" | "rejected" }
): Promise<ProductDocRevisionItem[]> {
  const query = new URLSearchParams();
  if (params?.status) {
    query.set("status", params.status);
  }
  const suffix = query.toString() ? `?${query.toString()}` : "";
  const response = await fetch(`/api/v1/product-docs/${docId}/ai-revisions${suffix}`, {
    headers: buildHeaders(language)
  });
  if (!response.ok) {
    return readError(response);
  }
  const payload = (await response.json()) as ApiResponse<ProductDocRevisionItem[]>;
  return payload.data;
}

export async function confirmProductDocRevision(
  docId: string,
  revisionId: string,
  language: string,
  body?: { changeSummary?: string }
): Promise<ProductDocDetailItem> {
  const response = await fetch(`/api/v1/product-docs/${docId}/ai-revisions/${revisionId}/confirm`, {
    method: "POST",
    headers: buildHeaders(language, true),
    body: JSON.stringify(body || {})
  });
  if (!response.ok) {
    return readError(response);
  }
  const payload = (await response.json()) as ApiResponse<ProductDocDetailItem>;
  return payload.data;
}

export async function rejectProductDocRevision(
  docId: string,
  revisionId: string,
  language: string,
  body?: { reason?: string }
): Promise<ProductDocRevisionItem> {
  const response = await fetch(`/api/v1/product-docs/${docId}/ai-revisions/${revisionId}/reject`, {
    method: "POST",
    headers: buildHeaders(language, true),
    body: JSON.stringify(body || {})
  });
  if (!response.ok) {
    return readError(response);
  }
  const payload = (await response.json()) as ApiResponse<ProductDocRevisionItem>;
  return payload.data;
}

export async function createWorkspace(
  language: string,
  body: {
    name: string;
    owner: string;
    defaultLanguage: "zh-CN" | "en-US";
  }
): Promise<WorkspaceItem> {
  const response = await fetch("/api/v1/workspaces", {
    method: "POST",
    headers: buildHeaders(language, true),
    body: JSON.stringify(body)
  });
  if (!response.ok) {
    return readError(response);
  }
  const payload = (await response.json()) as ApiResponse<WorkspaceItem>;
  return payload.data;
}

export async function updateWorkspace(
  workspaceId: string,
  language: string,
  body: {
    name: string;
    owner: string;
    status: string;
    defaultLanguage: "zh-CN" | "en-US";
  }
): Promise<WorkspaceItem> {
  const response = await fetch(`/api/v1/workspaces/${workspaceId}`, {
    method: "PUT",
    headers: buildHeaders(language, true),
    body: JSON.stringify(body)
  });
  if (!response.ok) {
    return readError(response);
  }
  const payload = (await response.json()) as ApiResponse<WorkspaceItem>;
  return payload.data;
}

export async function deleteWorkspace(
  workspaceId: string,
  language: string
): Promise<void> {
  const response = await fetch(`/api/v1/workspaces/${workspaceId}`, {
    method: "DELETE",
    headers: buildHeaders(language)
  });
  if (!response.ok) {
    return readError(response);
  }
}

export async function listAuditLogs(
  language: string,
  params?: { resourceType?: string; resourceId?: string; limit?: number }
): Promise<AuditLogItem[]> {
  const query = new URLSearchParams();
  if (params?.resourceType) {
    query.set("resourceType", params.resourceType);
  }
  if (params?.resourceId) {
    query.set("resourceId", params.resourceId);
  }
  if (params?.limit) {
    query.set("limit", String(params.limit));
  }

  const suffix = query.toString() ? `?${query.toString()}` : "";
  const response = await fetch(`/api/v1/audit-logs${suffix}`, {
    headers: buildHeaders(language)
  });
  if (!response.ok) {
    return readError(response);
  }
  const payload = (await response.json()) as ApiResponse<AuditLogItem[]>;
  return payload.data;
}

export async function listWorkspaceAuditFilterOptions(
  language: string
): Promise<WorkspaceAuditFilterOption[]> {
  const response = await fetch("/api/v1/audit-logs/workspace-options", {
    headers: buildHeaders(language)
  });
  if (!response.ok) {
    return readError(response);
  }
  const payload = (await response.json()) as ApiResponse<WorkspaceAuditFilterOption[]>;
  return payload.data;
}

export async function listProjectAuditFilterOptions(
  language: string
): Promise<ProjectAuditFilterOption[]> {
  const response = await fetch("/api/v1/audit-logs/project-options", {
    headers: buildHeaders(language)
  });
  if (!response.ok) {
    return readError(response);
  }
  const payload = (await response.json()) as ApiResponse<ProjectAuditFilterOption[]>;
  return payload.data;
}

export async function createChatSession(
  language: string,
  body: {
    projectId: string;
    title: string;
  }
): Promise<ChatSessionItem> {
  const response = await fetch("/api/v1/chat-sessions", {
    method: "POST",
    headers: buildHeaders(language, true),
    body: JSON.stringify(body)
  });
  if (!response.ok) {
    return readError(response);
  }
  const payload = (await response.json()) as ApiResponse<ChatSessionItem>;
  return payload.data;
}

export async function listChatSessions(
  language: string,
  params?: { projectId?: string; role?: string }
): Promise<ChatSessionItem[]> {
  const query = new URLSearchParams();
  if (params?.projectId) {
    query.set("projectId", params.projectId);
  }
  if (params?.role) {
    query.set("role", params.role);
  }
  const suffix = query.toString() ? `?${query.toString()}` : "";
  const response = await fetch(`/api/v1/chat-sessions${suffix}`, {
    headers: buildHeaders(language)
  });
  if (!response.ok) {
    return readError(response);
  }
  const payload = (await response.json()) as ApiResponse<ChatSessionItem[]>;
  return payload.data;
}

export async function listChatMessages(
  sessionId: string,
  language: string
): Promise<ChatMessageItem[]> {
  const response = await fetch(`/api/v1/chat-sessions/${sessionId}/messages`, {
    headers: buildHeaders(language)
  });
  if (!response.ok) {
    return readError(response);
  }
  const payload = (await response.json()) as ApiResponse<ChatMessageItem[]>;
  return payload.data;
}

export async function sendChatMessage(
  sessionId: string,
  language: string,
  body: { content: string }
): Promise<ChatMessageItem> {
  const response = await fetch(`/api/v1/chat-sessions/${sessionId}/messages`, {
    method: "POST",
    headers: buildHeaders(language, true),
    body: JSON.stringify(body)
  });
  if (!response.ok) {
    return readError(response);
  }
  const payload = (await response.json()) as ApiResponse<ChatMessageItem>;
  return payload.data;
}

export async function generateAiDraft(
  sessionId: string,
  language: string,
  body: {
    requirementInput?: string;
    priority?: "P0" | "P1" | "P2" | "P3";
    targetRole?: "product" | "backend" | "frontend" | "test";
  }
): Promise<AiDraftItem> {
  const response = await fetch(`/api/v1/chat-sessions/${sessionId}/ai-draft`, {
    method: "POST",
    headers: buildHeaders(language, true),
    body: JSON.stringify(body)
  });
  if (!response.ok) {
    return readError(response);
  }
  const payload = (await response.json()) as ApiResponse<AiDraftItem>;
  return payload.data;
}

export async function publishChatSession(
  sessionId: string,
  language: string,
  body: {
    title: string;
    requirementSummary: string;
    acceptanceCriteria: string;
    impactScope?: string;
    priority: "P0" | "P1" | "P2" | "P3";
    targetRole: "product" | "backend" | "frontend" | "test";
  }
): Promise<RequirementHandoffItem> {
  const response = await fetch(`/api/v1/chat-sessions/${sessionId}/publish`, {
    method: "POST",
    headers: buildHeaders(language, true),
    body: JSON.stringify(body)
  });
  if (!response.ok) {
    return readError(response);
  }
  const payload = (await response.json()) as ApiResponse<RequirementHandoffItem>;
  return payload.data;
}

export async function listRequirementHandoffs(
  language: string,
  params?: { projectId?: string; targetRole?: string; status?: string }
): Promise<RequirementHandoffItem[]> {
  const query = new URLSearchParams();
  if (params?.projectId) {
    query.set("projectId", params.projectId);
  }
  if (params?.targetRole) {
    query.set("targetRole", params.targetRole);
  }
  if (params?.status) {
    query.set("status", params.status);
  }
  const suffix = query.toString() ? `?${query.toString()}` : "";
  const response = await fetch(`/api/v1/requirement-handoffs${suffix}`, {
    headers: buildHeaders(language)
  });
  if (!response.ok) {
    return readError(response);
  }
  const payload = (await response.json()) as ApiResponse<RequirementHandoffItem[]>;
  return payload.data;
}

export async function acceptRequirementHandoff(
  handoffId: string,
  language: string
): Promise<RequirementHandoffItem> {
  const response = await fetch(`/api/v1/requirement-handoffs/${handoffId}/accept`, {
    method: "POST",
    headers: buildHeaders(language)
  });
  if (!response.ok) {
    return readError(response);
  }
  const payload = (await response.json()) as ApiResponse<RequirementHandoffItem>;
  return payload.data;
}

export async function transitionRequirementHandoff(
  handoffId: string,
  language: string,
  body: {
    action: "submit_review" | "publish" | "accept" | "start_dev" | "start_test" | "complete" | "reject" | "reopen";
  }
): Promise<RequirementHandoffItem> {
  const response = await fetch(`/api/v1/requirement-handoffs/${handoffId}/transition`, {
    method: "POST",
    headers: buildHeaders(language, true),
    body: JSON.stringify(body)
  });
  if (!response.ok) {
    return readError(response);
  }
  const payload = (await response.json()) as ApiResponse<RequirementHandoffItem>;
  return payload.data;
}

export async function listRequirementTasks(
  handoffId: string,
  language: string
): Promise<RequirementTaskItem[]> {
  const response = await fetch(`/api/v1/requirement-handoffs/${handoffId}/tasks`, {
    headers: buildHeaders(language)
  });
  if (!response.ok) {
    return readError(response);
  }
  const payload = (await response.json()) as ApiResponse<RequirementTaskItem[]>;
  return payload.data;
}

export async function generateAiRequirementTasks(
  handoffId: string,
  language: string
): Promise<AiTaskPlanItem> {
  const response = await fetch(`/api/v1/requirement-handoffs/${handoffId}/tasks/ai-generate`, {
    method: "POST",
    headers: buildHeaders(language)
  });
  if (!response.ok) {
    return readError(response);
  }
  const payload = (await response.json()) as ApiResponse<AiTaskPlanItem>;
  return payload.data;
}

export async function claimRequirementTask(
  taskId: string,
  language: string
): Promise<RequirementTaskItem> {
  const response = await fetch(`/api/v1/requirement-handoffs/tasks/${taskId}/claim`, {
    method: "POST",
    headers: buildHeaders(language)
  });
  if (!response.ok) {
    return readError(response);
  }
  const payload = (await response.json()) as ApiResponse<RequirementTaskItem>;
  return payload.data;
}

export async function updateRequirementTaskStatus(
  taskId: string,
  language: string,
  body: { status: "todo" | "in_progress" | "done" | "blocked" }
): Promise<RequirementTaskItem> {
  const response = await fetch(`/api/v1/requirement-handoffs/tasks/${taskId}/status`, {
    method: "PUT",
    headers: buildHeaders(language, true),
    body: JSON.stringify(body)
  });
  if (!response.ok) {
    return readError(response);
  }
  const payload = (await response.json()) as ApiResponse<RequirementTaskItem>;
  return payload.data;
}

export async function listTaskCenterTasks(
  language: string,
  params?: {
    view?: TaskCenterView;
    projectId?: string;
    status?: "todo" | "in_progress" | "done" | "blocked";
    role?: "product" | "backend" | "frontend" | "test";
    page?: number;
    size?: number;
  }
): Promise<TaskCenterPageResult> {
  const query = new URLSearchParams();
  if (params?.view) {
    query.set("view", params.view);
  }
  if (params?.projectId) {
    query.set("projectId", params.projectId);
  }
  if (params?.status) {
    query.set("status", params.status);
  }
  if (params?.role) {
    query.set("role", params.role);
  }
  if (params?.page) {
    query.set("page", String(params.page));
  }
  if (params?.size) {
    query.set("size", String(params.size));
  }
  const suffix = query.toString() ? `?${query.toString()}` : "";
  const response = await fetch(`/api/v1/tasks${suffix}`, {
    headers: buildHeaders(language)
  });
  if (!response.ok) {
    return readError(response);
  }
  const payload = (await response.json()) as ApiResponse<TaskCenterPageResult>;
  return payload.data;
}

export async function getTaskDetail(
  taskId: string,
  language: string
): Promise<RequirementTaskDetailItem> {
  const response = await fetch(`/api/v1/tasks/${taskId}`, {
    headers: buildHeaders(language)
  });
  if (!response.ok) {
    return readError(response);
  }
  const payload = (await response.json()) as ApiResponse<RequirementTaskDetailItem>;
  return payload.data;
}

export async function login(
  language: string,
  body: { username: string; password: string }
): Promise<LoginResult> {
  const response = await fetch("/api/v1/auth/login", {
    method: "POST",
    headers: buildHeaders(language, true, false),
    body: JSON.stringify(body)
  });
  if (!response.ok) {
    return readError(response);
  }
  const payload = (await response.json()) as ApiResponse<LoginResult>;
  return payload.data;
}

export async function me(language: string): Promise<UserProfile> {
  const response = await fetch("/api/v1/auth/me", {
    headers: buildHeaders(language)
  });
  if (!response.ok) {
    return readError(response);
  }
  const payload = (await response.json()) as ApiResponse<UserProfile>;
  return payload.data;
}

export async function logout(language: string): Promise<void> {
  const response = await fetch("/api/v1/auth/logout", {
    method: "POST",
    headers: buildHeaders(language)
  });
  if (!response.ok) {
    return readError(response);
  }
}
