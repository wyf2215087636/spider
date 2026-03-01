import { useEffect, useState } from "react";
import { useTranslation } from "react-i18next";
import {
  createWorkspace,
  deleteWorkspace,
  listWorkspaces,
  updateWorkspace,
  type WorkspaceItem
} from "../../api/client";
import { statusClass, statusLabel } from "../../utils/status";

interface WorkspacePageProps {
  language: string;
}

function WorkspacePage({ language }: WorkspacePageProps) {
  const { t } = useTranslation();
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState("");
  const [workspaces, setWorkspaces] = useState<WorkspaceItem[]>([]);

  const [name, setName] = useState("");
  const [owner, setOwner] = useState("");
  const [defaultLanguage, setDefaultLanguage] = useState<"zh-CN" | "en-US">("zh-CN");

  const [editingId, setEditingId] = useState<string | null>(null);
  const [editName, setEditName] = useState("");
  const [editOwner, setEditOwner] = useState("");
  const [editStatus, setEditStatus] = useState("active");
  const [editLanguage, setEditLanguage] = useState<"zh-CN" | "en-US">("zh-CN");

  const loadWorkspaces = async () => {
    try {
      setLoading(true);
      setError("");
      const data = await listWorkspaces(language);
      setWorkspaces(data);
    } catch (e) {
      setError(e instanceof Error ? e.message : t("errorUnknown"));
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    void loadWorkspaces();
  }, [language]);

  const onCreate = async () => {
    try {
      if (!name.trim() || !owner.trim()) {
        setError(t("workspaceRequired"));
        return;
      }
      setLoading(true);
      setError("");
      await createWorkspace(language, {
        name: name.trim(),
        owner: owner.trim(),
        defaultLanguage
      });
      setName("");
      setOwner("");
      await loadWorkspaces();
    } catch (e) {
      setError(e instanceof Error ? e.message : t("errorUnknown"));
    } finally {
      setLoading(false);
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
        setError(t("workspaceRequired"));
        return;
      }
      setLoading(true);
      setError("");
      await updateWorkspace(workspaceId, language, {
        name: editName.trim(),
        owner: editOwner.trim(),
        status: editStatus,
        defaultLanguage: editLanguage
      });
      onCancelEdit();
      await loadWorkspaces();
    } catch (e) {
      setError(e instanceof Error ? e.message : t("errorUnknown"));
    } finally {
      setLoading(false);
    }
  };

  const onDelete = async (workspaceId: string) => {
    if (!window.confirm(t("workspaceDeleteConfirm"))) {
      return;
    }
    try {
      setLoading(true);
      setError("");
      await deleteWorkspace(workspaceId, language);
      await loadWorkspaces();
    } catch (e) {
      setError(e instanceof Error ? e.message : t("errorUnknown"));
    } finally {
      setLoading(false);
    }
  };

  return (
    <section className="card">
      <h2>{t("workspaceTitle")}</h2>
      <p>{t("workspaceDesc")}</p>

      <div className="formRow">
        <input value={name} onChange={(e) => setName(e.target.value)} placeholder={t("workspaceName")} />
        <input value={owner} onChange={(e) => setOwner(e.target.value)} placeholder={t("workspaceOwner")} />
        <select value={defaultLanguage} onChange={(e) => setDefaultLanguage(e.target.value as "zh-CN" | "en-US")}>
          <option value="zh-CN">zh-CN</option>
          <option value="en-US">en-US</option>
        </select>
        <button onClick={onCreate} disabled={loading}>
          {loading ? t("loading") : t("workspaceCreate")}
        </button>
      </div>

      <div className="workspaceList">
        {loading && <p>{t("loading")}</p>}
        {!loading && workspaces.length === 0 && <p>{t("workspaceEmpty")}</p>}
        {!loading &&
          workspaces.map((item) => (
            <article className="workspaceItem" key={item.id}>
              {editingId === item.id ? (
                <div className="workspaceEditForm">
                  <input value={editName} onChange={(e) => setEditName(e.target.value)} />
                  <input value={editOwner} onChange={(e) => setEditOwner(e.target.value)} />
                  <select value={editStatus} onChange={(e) => setEditStatus(e.target.value)}>
                    <option value="active">{t("statusActive")}</option>
                    <option value="archived">{t("statusArchived")}</option>
                  </select>
                  <select value={editLanguage} onChange={(e) => setEditLanguage(e.target.value as "zh-CN" | "en-US")}>
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
                    <span className={statusClass(item.status)}>{statusLabel(item.status, t)}</span>
                    <span>{item.defaultLanguage}</span>
                    <div className="workspaceActions">
                      <button onClick={() => onStartEdit(item)}>{t("workspaceEdit")}</button>
                      <button className="danger" onClick={() => onDelete(item.id)}>
                        {t("workspaceDelete")}
                      </button>
                    </div>
                  </div>
                </>
              )}
            </article>
          ))}
      </div>

      {error && <p className="error">{error}</p>}
    </section>
  );
}

export default WorkspacePage;
