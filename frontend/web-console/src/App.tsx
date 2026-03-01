import { useEffect, useMemo, useState } from "react";
import { useTranslation } from "react-i18next";
import {
  createWorkspace,
  deleteWorkspace,
  fetchHealth,
  listAuditLogs,
  listWorkspaces,
  updateWorkspace,
  type AuditLogItem,
  type HealthResult,
  type WorkspaceItem
} from "./api/client";

function App() {
  const { t, i18n } = useTranslation();
  const [loading, setLoading] = useState(false);
  const [health, setHealth] = useState<HealthResult | null>(null);
  const [error, setError] = useState<string>("");

  const [workspaceLoading, setWorkspaceLoading] = useState(false);
  const [workspaceError, setWorkspaceError] = useState<string>("");
  const [workspaces, setWorkspaces] = useState<WorkspaceItem[]>([]);

  const [auditLoading, setAuditLoading] = useState(false);
  const [auditError, setAuditError] = useState<string>("");
  const [auditLogs, setAuditLogs] = useState<AuditLogItem[]>([]);
  const [selectedWorkspaceId, setSelectedWorkspaceId] = useState("");

  const [name, setName] = useState("");
  const [owner, setOwner] = useState("");
  const [defaultLanguage, setDefaultLanguage] = useState<"zh-CN" | "en-US">("zh-CN");

  const [editingId, setEditingId] = useState<string | null>(null);
  const [editName, setEditName] = useState("");
  const [editOwner, setEditOwner] = useState("");
  const [editStatus, setEditStatus] = useState("active");
  const [editLanguage, setEditLanguage] = useState<"zh-CN" | "en-US">("zh-CN");

  const languageLabel = useMemo(() => {
    return i18n.language === "zh-CN" ? "ZH" : "EN";
  }, [i18n.language]);

  const loadWorkspaces = async () => {
    try {
      setWorkspaceLoading(true);
      setWorkspaceError("");
      const data = await listWorkspaces(i18n.language);
      setWorkspaces(data);
      if (selectedWorkspaceId && !data.find((w) => w.id === selectedWorkspaceId)) {
        setSelectedWorkspaceId("");
      }
    } catch (e) {
      setWorkspaceError(e instanceof Error ? e.message : t("errorUnknown"));
    } finally {
      setWorkspaceLoading(false);
    }
  };

  const loadAuditLogs = async (resourceId?: string) => {
    try {
      setAuditLoading(true);
      setAuditError("");
      const data = await listAuditLogs(i18n.language, {
        resourceType: "workspace",
        resourceId: resourceId || undefined,
        limit: 20
      });
      setAuditLogs(data);
    } catch (e) {
      setAuditError(e instanceof Error ? e.message : t("errorUnknown"));
    } finally {
      setAuditLoading(false);
    }
  };

  useEffect(() => {
    void loadWorkspaces();
  }, [i18n.language]);

  useEffect(() => {
    void loadAuditLogs(selectedWorkspaceId || undefined);
  }, [i18n.language, selectedWorkspaceId]);

  const toggleLanguage = async () => {
    const next = i18n.language === "zh-CN" ? "en-US" : "zh-CN";
    await i18n.changeLanguage(next);
  };

  const checkHealth = async () => {
    try {
      setLoading(true);
      setError("");
      const data = await fetchHealth(i18n.language);
      setHealth(data);
    } catch (e) {
      setError(e instanceof Error ? e.message : t("errorUnknown"));
      setHealth(null);
    } finally {
      setLoading(false);
    }
  };

  const onCreateWorkspace = async () => {
    try {
      if (!name.trim() || !owner.trim()) {
        setWorkspaceError(t("workspaceRequired"));
        return;
      }
      setWorkspaceLoading(true);
      setWorkspaceError("");
      await createWorkspace(i18n.language, {
        name: name.trim(),
        owner: owner.trim(),
        defaultLanguage
      });
      setName("");
      setOwner("");
      await loadWorkspaces();
      await loadAuditLogs(selectedWorkspaceId || undefined);
    } catch (e) {
      setWorkspaceError(e instanceof Error ? e.message : t("errorUnknown"));
    } finally {
      setWorkspaceLoading(false);
    }
  };

  const onStartEdit = (item: WorkspaceItem) => {
    setEditingId(item.id);
    setEditName(item.name);
    setEditOwner(item.owner);
    setEditStatus(item.status);
    setEditLanguage(item.defaultLanguage);
  };

  const onCancelEdit = () => {
    setEditingId(null);
    setEditName("");
    setEditOwner("");
    setEditStatus("active");
    setEditLanguage("zh-CN");
  };

  const onSaveEdit = async (workspaceId: string) => {
    try {
      if (!editName.trim() || !editOwner.trim()) {
        setWorkspaceError(t("workspaceRequired"));
        return;
      }
      setWorkspaceLoading(true);
      setWorkspaceError("");
      await updateWorkspace(workspaceId, i18n.language, {
        name: editName.trim(),
        owner: editOwner.trim(),
        status: editStatus,
        defaultLanguage: editLanguage
      });
      onCancelEdit();
      await loadWorkspaces();
      await loadAuditLogs(selectedWorkspaceId || undefined);
    } catch (e) {
      setWorkspaceError(e instanceof Error ? e.message : t("errorUnknown"));
    } finally {
      setWorkspaceLoading(false);
    }
  };

  const onDeleteWorkspace = async (workspaceId: string) => {
    const confirmed = window.confirm(t("workspaceDeleteConfirm"));
    if (!confirmed) {
      return;
    }
    try {
      setWorkspaceLoading(true);
      setWorkspaceError("");
      await deleteWorkspace(workspaceId, i18n.language);
      await loadWorkspaces();
      await loadAuditLogs(selectedWorkspaceId || undefined);
    } catch (e) {
      setWorkspaceError(e instanceof Error ? e.message : t("errorUnknown"));
    } finally {
      setWorkspaceLoading(false);
    }
  };

  return (
    <main className="page">
      <header className="header">
        <div>
          <h1>{t("title")}</h1>
          <p>{t("subtitle")}</p>
        </div>
        <button className="secondary" onClick={toggleLanguage}>
          {t("language")} {languageLabel}
        </button>
      </header>

      <section className="card">
        <h2>{t("workspaceTitle")}</h2>
        <p>{t("workspaceDesc")}</p>

        <div className="formRow">
          <input
            value={name}
            onChange={(e) => setName(e.target.value)}
            placeholder={t("workspaceName")}
          />
          <input
            value={owner}
            onChange={(e) => setOwner(e.target.value)}
            placeholder={t("workspaceOwner")}
          />
          <select
            value={defaultLanguage}
            onChange={(e) => setDefaultLanguage(e.target.value as "zh-CN" | "en-US")}
          >
            <option value="zh-CN">zh-CN</option>
            <option value="en-US">en-US</option>
          </select>
          <button onClick={onCreateWorkspace} disabled={workspaceLoading}>
            {workspaceLoading ? t("loading") : t("workspaceCreate")}
          </button>
        </div>

        <div className="workspaceList">
          {workspaceLoading && <p>{t("loading")}</p>}
          {!workspaceLoading && workspaces.length === 0 && <p>{t("workspaceEmpty")}</p>}
          {!workspaceLoading &&
            workspaces.map((item) => (
              <article className="workspaceItem" key={item.id}>
                {editingId === item.id ? (
                  <div className="workspaceEditForm">
                    <input value={editName} onChange={(e) => setEditName(e.target.value)} />
                    <input value={editOwner} onChange={(e) => setEditOwner(e.target.value)} />
                    <select value={editStatus} onChange={(e) => setEditStatus(e.target.value)}>
                      <option value="active">active</option>
                      <option value="archived">archived</option>
                    </select>
                    <select
                      value={editLanguage}
                      onChange={(e) => setEditLanguage(e.target.value as "zh-CN" | "en-US")}
                    >
                      <option value="zh-CN">zh-CN</option>
                      <option value="en-US">en-US</option>
                    </select>
                    <div className="workspaceActions">
                      <button onClick={() => onSaveEdit(item.id)}>{t("workspaceSave")}</button>
                      <button className="ghost" onClick={onCancelEdit}>
                        {t("workspaceCancel")}
                      </button>
                    </div>
                  </div>
                ) : (
                  <>
                    <div>
                      <h3>{item.name}</h3>
                      <p>
                        {t("workspaceOwnerLabel")}: {item.owner}
                      </p>
                    </div>
                    <div className="workspaceMeta">
                      <span>{item.status}</span>
                      <span>{item.defaultLanguage}</span>
                      <div className="workspaceActions">
                        <button onClick={() => onStartEdit(item)}>{t("workspaceEdit")}</button>
                        <button className="danger" onClick={() => onDeleteWorkspace(item.id)}>
                          {t("workspaceDelete")}
                        </button>
                      </div>
                    </div>
                  </>
                )}
              </article>
            ))}
        </div>

        {workspaceError && <p className="error">{workspaceError}</p>}
      </section>

      <section className="card">
        <div className="auditHeader">
          <div>
            <h2>{t("auditTitle")}</h2>
            <p>{t("auditDesc")}</p>
          </div>
          <div className="auditFilters">
            <select
              value={selectedWorkspaceId}
              onChange={(e) => setSelectedWorkspaceId(e.target.value)}
            >
              <option value="">{t("auditFilterAll")}</option>
              {workspaces.map((w) => (
                <option value={w.id} key={w.id}>
                  {w.name}
                </option>
              ))}
            </select>
            <button className="secondary" onClick={() => loadAuditLogs(selectedWorkspaceId || undefined)}>
              {t("auditRefresh")}
            </button>
          </div>
        </div>

        <div className="auditList">
          {auditLoading && <p>{t("loading")}</p>}
          {!auditLoading && auditLogs.length === 0 && <p>{t("auditEmpty")}</p>}
          {!auditLoading &&
            auditLogs.map((item) => (
              <article className="auditItem" key={item.id}>
                <div className="auditTop">
                  <strong>{item.action}</strong>
                  <span>{item.status}</span>
                </div>
                <p>{item.details || "-"}</p>
                <div className="auditMeta">
                  <span>requestId: {item.requestId || "-"}</span>
                  <span>actor: {item.actor || "-"}</span>
                  <span>{item.createdAt || "-"}</span>
                </div>
              </article>
            ))}
        </div>

        {auditError && <p className="error">{auditError}</p>}
      </section>

      <section className="card">
        <h2>{t("healthCheck")}</h2>
        <p>{t("healthDesc")}</p>
        <button onClick={checkHealth} disabled={loading}>
          {loading ? t("loading") : t("runHealth")}
        </button>

        {health && <pre className="result">{JSON.stringify(health, null, 2)}</pre>}
        {error && <p className="error">{error}</p>}
      </section>
    </main>
  );
}

export default App;
