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

interface ApiResponse<T> {
  code: string;
  message: string;
  data: T;
  timestamp: string;
  requestId?: string;
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
    headers: {
      "Accept-Language": language
    }
  });

  if (!response.ok) {
    return readError(response);
  }

  const payload = (await response.json()) as ApiResponse<HealthResult>;
  return payload.data;
}

export async function listWorkspaces(language: string): Promise<WorkspaceItem[]> {
  const response = await fetch("/api/v1/workspaces", {
    headers: {
      "Accept-Language": language
    }
  });
  if (!response.ok) {
    return readError(response);
  }
  const payload = (await response.json()) as ApiResponse<WorkspaceItem[]>;
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
    headers: {
      "Content-Type": "application/json",
      "Accept-Language": language
    },
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
    headers: {
      "Content-Type": "application/json",
      "Accept-Language": language
    },
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
    headers: {
      "Accept-Language": language
    }
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
    headers: {
      "Accept-Language": language
    }
  });
  if (!response.ok) {
    return readError(response);
  }
  const payload = (await response.json()) as ApiResponse<AuditLogItem[]>;
  return payload.data;
}
