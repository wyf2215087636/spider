import { useEffect, useMemo, useState } from "react";
import { useTranslation } from "react-i18next";
import {
  createProject,
  deleteProject,
  listProjects,
  listWorkspaces,
  updateProject,
  type ProjectItem,
  type WorkspaceItem
} from "../../api/client";
import { statusClass, statusLabel } from "../../utils/status";

interface ProjectPageProps {
  language: string;
}

function ProjectPage({ language }: ProjectPageProps) {
  const { t } = useTranslation();
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState("");
  const [workspaces, setWorkspaces] = useState<WorkspaceItem[]>([]);
  const [projects, setProjects] = useState<ProjectItem[]>([]);

  const [workspaceId, setWorkspaceId] = useState("");
  const [name, setName] = useState("");
  const [status, setStatus] = useState<"draft" | "active" | "archived">("draft");
  const [workspaceFilterId, setWorkspaceFilterId] = useState("");

  const [editingId, setEditingId] = useState<string | null>(null);
  const [editName, setEditName] = useState("");
  const [editStatus, setEditStatus] = useState<"draft" | "active" | "archived">("draft");

  const workspaceNameById = useMemo(() => {
    const map = new Map<string, string>();
    workspaces.forEach((item) => map.set(item.id, item.name));
    return map;
  }, [workspaces]);

  const loadWorkspaces = async () => {
    const data = await listWorkspaces(language);
    setWorkspaces(data);
    if (!workspaceId && data.length > 0) {
      setWorkspaceId(data[0].id);
    }
    if (workspaceFilterId && !data.find((item) => item.id === workspaceFilterId)) {
      setWorkspaceFilterId("");
    }
  };

  const loadProjects = async (filterWorkspaceId?: string) => {
    const data = await listProjects(language, filterWorkspaceId);
    setProjects(data);
  };

  const refreshData = async (filterWorkspaceId?: string) => {
    try {
      setLoading(true);
      setError("");
      await loadWorkspaces();
      await loadProjects(filterWorkspaceId);
    } catch (e) {
      setError(e instanceof Error ? e.message : t("errorUnknown"));
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    void refreshData(workspaceFilterId || undefined);
  }, [language, workspaceFilterId]);

  const onCreate = async () => {
    try {
      if (!workspaceId || !name.trim()) {
        setError(t("projectRequired"));
        return;
      }
      setLoading(true);
      setError("");
      await createProject(language, {
        workspaceId,
        name: name.trim(),
        status
      });
      setName("");
      setStatus("draft");
      await loadProjects(workspaceFilterId || undefined);
    } catch (e) {
      setError(e instanceof Error ? e.message : t("errorUnknown"));
    } finally {
      setLoading(false);
    }
  };

  const onStartEdit = (item: ProjectItem) => {
    setEditingId(item.id);
    setEditName(item.name);
    setEditStatus(item.status as "draft" | "active" | "archived");
  };

  const onCancelEdit = () => {
    setEditingId(null);
    setEditName("");
    setEditStatus("draft");
  };

  const onSaveEdit = async (projectId: string) => {
    try {
      if (!editName.trim()) {
        setError(t("projectRequired"));
        return;
      }
      setLoading(true);
      setError("");
      await updateProject(projectId, language, {
        name: editName.trim(),
        status: editStatus
      });
      onCancelEdit();
      await loadProjects(workspaceFilterId || undefined);
    } catch (e) {
      setError(e instanceof Error ? e.message : t("errorUnknown"));
    } finally {
      setLoading(false);
    }
  };

  const onDelete = async (projectId: string) => {
    if (!window.confirm(t("projectDeleteConfirm"))) {
      return;
    }
    try {
      setLoading(true);
      setError("");
      await deleteProject(projectId, language);
      await loadProjects(workspaceFilterId || undefined);
    } catch (e) {
      setError(e instanceof Error ? e.message : t("errorUnknown"));
    } finally {
      setLoading(false);
    }
  };

  return (
    <section className="card">
      <h2>{t("projectTitle")}</h2>
      <p>{t("projectDesc")}</p>

      <div className="formRow">
        <select value={workspaceId} onChange={(e) => setWorkspaceId(e.target.value)}>
          <option value="">{t("projectWorkspace")}</option>
          {workspaces.map((item) => (
            <option value={item.id} key={item.id}>
              {item.name}
            </option>
          ))}
        </select>
        <input value={name} onChange={(e) => setName(e.target.value)} placeholder={t("projectName")} />
        <select value={status} onChange={(e) => setStatus(e.target.value as "draft" | "active" | "archived")}>
          <option value="draft">{t("statusDraft")}</option>
          <option value="active">{t("statusActive")}</option>
          <option value="archived">{t("statusArchived")}</option>
        </select>
        <button onClick={onCreate} disabled={loading}>
          {loading ? t("loading") : t("projectCreate")}
        </button>
      </div>

      <div className="projectFilterRow">
        <select value={workspaceFilterId} onChange={(e) => setWorkspaceFilterId(e.target.value)}>
          <option value="">{t("projectFilterAll")}</option>
          {workspaces.map((item) => (
            <option value={item.id} key={item.id}>
              {item.name}
            </option>
          ))}
        </select>
      </div>

      <div className="workspaceList">
        {loading && <p>{t("loading")}</p>}
        {!loading && projects.length === 0 && <p>{t("projectEmpty")}</p>}
        {!loading &&
          projects.map((item) => (
            <article className="workspaceItem" key={item.id}>
              {editingId === item.id ? (
                <div className="projectEditForm">
                  <input value={editName} onChange={(e) => setEditName(e.target.value)} />
                  <select
                    value={editStatus}
                    onChange={(e) => setEditStatus(e.target.value as "draft" | "active" | "archived")}
                  >
                    <option value="draft">{t("statusDraft")}</option>
                    <option value="active">{t("statusActive")}</option>
                    <option value="archived">{t("statusArchived")}</option>
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
                      {t("projectWorkspaceLabel")}: {workspaceNameById.get(item.workspaceId) || item.workspaceId}
                    </p>
                  </div>
                  <div className="workspaceMeta">
                    <span className={statusClass(item.status)}>{statusLabel(item.status, t)}</span>
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

export default ProjectPage;

