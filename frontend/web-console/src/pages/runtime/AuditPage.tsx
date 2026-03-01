import { useEffect, useMemo, useState } from "react";
import { useTranslation } from "react-i18next";
import {
  listAuditLogs,
  listProjectAuditFilterOptions,
  listWorkspaceAuditFilterOptions,
  type AuditLogItem,
  type ProjectAuditFilterOption,
  type WorkspaceAuditFilterOption
} from "../../api/client";
import { statusClass, statusLabel } from "../../utils/status";

type AuditResourceType = "workspace" | "project";

interface AuditPageProps {
  language: string;
}

function AuditPage({ language }: AuditPageProps) {
  const { t } = useTranslation();
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState("");
  const [logs, setLogs] = useState<AuditLogItem[]>([]);

  const [resourceType, setResourceType] = useState<AuditResourceType>("workspace");
  const [resourceId, setResourceId] = useState("");

  const [workspaceOptions, setWorkspaceOptions] = useState<WorkspaceAuditFilterOption[]>([]);
  const [projectOptions, setProjectOptions] = useState<ProjectAuditFilterOption[]>([]);

  const currentOptions = useMemo(
    () => (resourceType === "workspace" ? workspaceOptions : projectOptions),
    [resourceType, workspaceOptions, projectOptions]
  );

  const loadOptions = async (nextResourceType: AuditResourceType) => {
    if (nextResourceType === "workspace") {
      const data = await listWorkspaceAuditFilterOptions(language);
      setWorkspaceOptions(data);
      if (resourceId && !data.find((item) => item.resourceId === resourceId)) {
        setResourceId("");
      }
      return;
    }

    const data = await listProjectAuditFilterOptions(language);
    setProjectOptions(data);
    if (resourceId && !data.find((item) => item.resourceId === resourceId)) {
      setResourceId("");
    }
  };

  const loadLogs = async (nextResourceType: AuditResourceType, nextResourceId?: string) => {
    const data = await listAuditLogs(language, {
      resourceType: nextResourceType,
      resourceId: nextResourceId || undefined,
      limit: 50
    });
    setLogs(data);
  };

  const refreshData = async (nextResourceType: AuditResourceType, nextResourceId?: string) => {
    try {
      setLoading(true);
      setError("");
      await loadOptions(nextResourceType);
      await loadLogs(nextResourceType, nextResourceId);
    } catch (e) {
      setError(e instanceof Error ? e.message : t("errorUnknown"));
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    void refreshData(resourceType, resourceId || undefined);
  }, [language, resourceType, resourceId]);

  return (
    <section className="card">
      <div className="auditHeader">
        <div>
          <h2>{t("auditTitle")}</h2>
          <p>{t("auditDesc")}</p>
        </div>
        <div className="auditFilters">
          <select value={resourceType} onChange={(e) => setResourceType(e.target.value as AuditResourceType)}>
            <option value="workspace">{t("auditTypeWorkspace")}</option>
            <option value="project">{t("auditTypeProject")}</option>
          </select>
          <select value={resourceId} onChange={(e) => setResourceId(e.target.value)}>
            <option value="">{t("auditFilterAll")}</option>
            {currentOptions.map((item) => (
              <option value={item.resourceId} key={item.resourceId}>
                {item.deleted ? `${t("auditDeletedPrefix")} ` : ""}
                {item.name}
              </option>
            ))}
          </select>
          <button className="secondary" onClick={() => refreshData(resourceType, resourceId || undefined)}>
            {t("auditRefresh")}
          </button>
        </div>
      </div>

      <div className="auditList">
        {loading && <p>{t("loading")}</p>}
        {!loading && logs.length === 0 && <p>{t("auditEmpty")}</p>}
        {!loading &&
          logs.map((item) => (
            <article className="auditItem" key={item.id}>
              <div className="auditTop">
                <strong>{item.action}</strong>
                <span className={statusClass(item.status)}>{statusLabel(item.status, t)}</span>
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

      {error && <p className="error">{error}</p>}
    </section>
  );
}

export default AuditPage;

