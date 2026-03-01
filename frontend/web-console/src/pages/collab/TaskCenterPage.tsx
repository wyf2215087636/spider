import { useEffect, useMemo, useState } from "react";
import { useTranslation } from "react-i18next";
import {
  claimRequirementTask,
  getTaskDetail,
  listProjects,
  listTaskCenterTasks,
  updateRequirementTaskStatus,
  type ProjectItem,
  type RequirementTaskDetailItem,
  type RequirementTaskItem,
  type TaskCenterView,
  type UserProfile
} from "../../api/client";
import { statusClass, statusLabel } from "../../utils/status";

interface TaskCenterPageProps {
  language: string;
  currentRole: UserProfile["role"];
  currentActor: string;
}

function TaskCenterPage({ language, currentRole, currentActor }: TaskCenterPageProps) {
  const { t } = useTranslation();
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState("");
  const [projects, setProjects] = useState<ProjectItem[]>([]);
  const [tasks, setTasks] = useState<RequirementTaskItem[]>([]);
  const [selectedTaskId, setSelectedTaskId] = useState("");
  const [selectedTaskDetail, setSelectedTaskDetail] = useState<RequirementTaskDetailItem | null>(null);
  const [detailLoading, setDetailLoading] = useState(false);
  const [view, setView] = useState<TaskCenterView>("pool");
  const [projectId, setProjectId] = useState("");
  const [statusFilter, setStatusFilter] = useState<"" | "todo" | "in_progress" | "done" | "blocked">("");
  const [roleFilter, setRoleFilter] = useState<"" | "product" | "backend" | "frontend" | "test">("");
  const [page, setPage] = useState(1);
  const [size] = useState(10);
  const [total, setTotal] = useState(0);
  const [hasNext, setHasNext] = useState(false);

  const canViewAll = currentRole === "pmo" || currentRole === "admin";
  const viewOptions = useMemo<TaskCenterView[]>(
    () => (canViewAll ? ["pool", "mine", "all"] : ["pool", "mine"]),
    [canViewAll]
  );

  const roleLabel = (role: "product" | "backend" | "frontend" | "test") => {
    if (role === "product") {
      return t("roleProduct");
    }
    if (role === "backend") {
      return t("roleBackend");
    }
    if (role === "frontend") {
      return t("roleFrontend");
    }
    return t("roleTest");
  };

  const taskTitle = (task: RequirementTaskItem) => {
    if (language === "zh-CN") {
      return task.titleZh || task.title || task.titleEn;
    }
    return task.titleEn || task.title || task.titleZh;
  };

  const taskDescription = (task: RequirementTaskItem) => {
    if (language === "zh-CN") {
      return task.descriptionZh || task.description || task.descriptionEn;
    }
    return task.descriptionEn || task.description || task.descriptionZh;
  };

  const projectNameMap = useMemo(() => {
    const map = new Map<string, string>();
    projects.forEach((item) => map.set(item.id, item.name));
    return map;
  }, [projects]);

  const roleOperationHint = useMemo(() => {
    if (currentRole === "admin") {
      return t("taskDetailOpsAdmin");
    }
    if (currentRole === "pmo") {
      return t("taskDetailOpsPmo");
    }
    if (currentRole === "product") {
      return t("taskDetailOpsProduct");
    }
    return t("taskDetailOpsDev");
  }, [currentRole, t]);

  const loadProjects = async () => {
    const data = await listProjects(language);
    setProjects(data);
    if (projectId && !data.find((item) => item.id === projectId)) {
      setProjectId("");
    }
  };

  const loadTasks = async () => {
    const data = await listTaskCenterTasks(language, {
      view,
      projectId: projectId || undefined,
      status: statusFilter || undefined,
      role: roleFilter || undefined,
      page,
      size
    });
    setTasks(data.items);
    setTotal(data.total);
    setHasNext(data.hasNext);
  };

  const loadTaskDetail = async (taskId: string) => {
    const detail = await getTaskDetail(taskId, language);
    setSelectedTaskDetail(detail);
  };

  const refreshData = async () => {
    try {
      setLoading(true);
      setError("");
      await loadProjects();
      await loadTasks();
    } catch (e) {
      setError(e instanceof Error ? e.message : t("errorUnknown"));
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    if (!viewOptions.includes(view)) {
      setView(viewOptions[0]);
      return;
    }
    void refreshData();
  }, [language, view, projectId, statusFilter, roleFilter, canViewAll, page, size]);

  useEffect(() => {
    if (!selectedTaskId) {
      setSelectedTaskDetail(null);
      return;
    }
    if (!tasks.find((item) => item.id === selectedTaskId)) {
      setSelectedTaskId("");
      setSelectedTaskDetail(null);
    }
  }, [tasks, selectedTaskId]);

  const onViewTaskDetail = async (taskId: string) => {
    try {
      setSelectedTaskId(taskId);
      setSelectedTaskDetail(null);
      setDetailLoading(true);
      setError("");
      await loadTaskDetail(taskId);
    } catch (e) {
      setError(e instanceof Error ? e.message : t("errorUnknown"));
      setSelectedTaskId("");
      setSelectedTaskDetail(null);
    } finally {
      setDetailLoading(false);
    }
  };

  const onCloseTaskDetail = () => {
    setSelectedTaskId("");
    setSelectedTaskDetail(null);
    setDetailLoading(false);
  };

  const canOperateTaskRole = (task: RequirementTaskItem) =>
    currentRole === "admin" || currentRole === task.role;

  const canClaim = (task: RequirementTaskItem) =>
    canOperateTaskRole(task) && (!task.assignee || task.assignee === currentActor || currentRole === "admin");

  const canShowClaim = (task: RequirementTaskItem) => canClaim(task) && task.assignee !== currentActor;

  const canUpdateStatus = (task: RequirementTaskItem) =>
    canOperateTaskRole(task) && (!task.assignee || task.assignee === currentActor || currentRole === "admin");

  const onClaim = async (taskId: string) => {
    try {
      setLoading(true);
      setError("");
      await claimRequirementTask(taskId, language);
      await loadTasks();
      if (selectedTaskId === taskId) {
        await loadTaskDetail(taskId);
      }
    } catch (e) {
      setError(e instanceof Error ? e.message : t("errorUnknown"));
    } finally {
      setLoading(false);
    }
  };

  const onUpdateTaskStatus = async (taskId: string, status: "todo" | "in_progress" | "done" | "blocked") => {
    try {
      setLoading(true);
      setError("");
      await updateRequirementTaskStatus(taskId, language, { status });
      await loadTasks();
      if (selectedTaskId === taskId) {
        await loadTaskDetail(taskId);
      }
    } catch (e) {
      setError(e instanceof Error ? e.message : t("errorUnknown"));
    } finally {
      setLoading(false);
    }
  };

  return (
    <section className="card">
      <h2>{t("taskCenterTitle")}</h2>
      <p>{t("taskCenterDesc")}</p>

      <div className="taskCenterTabs">
        {viewOptions.map((item) => (
          <button
            key={item}
            className={view === item ? "" : "secondary"}
            onClick={() => {
              setView(item);
              setPage(1);
            }}
            disabled={loading}
          >
            {item === "pool" ? t("taskCenterViewPool") : item === "mine" ? t("taskCenterViewMine") : t("taskCenterViewAll")}
          </button>
        ))}
      </div>

      <div className="taskCenterFilters">
        <select
          value={projectId}
          onChange={(e) => {
            setProjectId(e.target.value);
            setPage(1);
          }}
        >
          <option value="">{t("taskCenterProjectAll")}</option>
          {projects.map((item) => (
            <option value={item.id} key={item.id}>
              {item.name}
            </option>
          ))}
        </select>
        <select
          value={statusFilter}
          onChange={(e) => {
            setStatusFilter(e.target.value as "" | "todo" | "in_progress" | "done" | "blocked");
            setPage(1);
          }}
        >
          <option value="">{t("taskCenterStatusAll")}</option>
          <option value="todo">{t("statusTodo")}</option>
          <option value="in_progress">{t("statusInProgress")}</option>
          <option value="done">{t("statusDone")}</option>
          <option value="blocked">{t("statusBlocked")}</option>
        </select>
        {canViewAll ? (
          <select
            value={roleFilter}
            onChange={(e) => {
              setRoleFilter(e.target.value as "" | "product" | "backend" | "frontend" | "test");
              setPage(1);
            }}
          >
            <option value="">{t("taskCenterRoleAll")}</option>
            <option value="product">{t("roleProduct")}</option>
            <option value="backend">{t("roleBackend")}</option>
            <option value="frontend">{t("roleFrontend")}</option>
            <option value="test">{t("roleTest")}</option>
          </select>
        ) : null}
      </div>

      <div className="workspaceList">
        {loading && <p>{t("loading")}</p>}
        {!loading && tasks.length === 0 ? <p>{t("taskCenterEmpty")}</p> : null}
        {!loading &&
          tasks.map((task) => (
            <article className="workspaceItem taskItem" key={task.id}>
              <div>
                <h3>{taskTitle(task)}</h3>
                <p>{taskDescription(task)}</p>
                <p>
                  {t("taskCenterBelongHandoff")}: {task.handoffId}
                </p>
                <p>
                  {t("projectTitle")}: {projectNameMap.get(task.projectId) || task.projectId}
                </p>
              </div>
              <div className="workspaceMeta">
                <span className={statusClass(task.status)}>{statusLabel(task.status, t)}</span>
                <span>{roleLabel(task.role)}</span>
                <span>{t("handoffTaskEstimate")}: {task.estimateHours}</span>
                <span>{t("handoffTaskAssignee")}: {task.assignee || "-"}</span>
                <div className="workspaceActions">
                  <button className="ghost" onClick={() => onViewTaskDetail(task.id)} disabled={loading || detailLoading}>
                    {t("taskCenterViewDetail")}
                  </button>
                  <button className="secondary" onClick={() => onClaim(task.id)} disabled={loading || !canClaim(task)}>
                    {t("handoffClaimTask")}
                  </button>
                  <select
                    value={task.status}
                    disabled={loading || !canUpdateStatus(task)}
                    onChange={(e) =>
                      void onUpdateTaskStatus(
                        task.id,
                        e.target.value as "todo" | "in_progress" | "done" | "blocked"
                      )
                    }
                  >
                    <option value="todo">{t("statusTodo")}</option>
                    <option value="in_progress">{t("statusInProgress")}</option>
                    <option value="done">{t("statusDone")}</option>
                    <option value="blocked">{t("statusBlocked")}</option>
                  </select>
                </div>
              </div>
            </article>
          ))}
      </div>

      <div className="taskPagination">
        <button
          className="secondary"
          onClick={() => setPage((prev) => Math.max(1, prev - 1))}
          disabled={loading || page <= 1}
        >
          {t("taskCenterPrevPage")}
        </button>
        <span>{t("taskCenterPageInfo", { page, total })}</span>
        <button
          className="secondary"
          onClick={() => setPage((prev) => prev + 1)}
          disabled={loading || !hasNext}
        >
          {t("taskCenterNextPage")}
        </button>
      </div>

      {selectedTaskId ? (
        <div className="modalBackdrop" onClick={onCloseTaskDetail}>
          <div className="modalCard" onClick={(e) => e.stopPropagation()}>
            <div className="modalHeader">
              <h3>{t("taskDetailTitle")}</h3>
              <button className="ghost" onClick={onCloseTaskDetail}>
                {t("taskDetailClose")}
              </button>
            </div>

            {detailLoading || !selectedTaskDetail ? (
              <p>{t("loading")}</p>
            ) : (
              <div className="modalBody">
                <h4>{t("taskDetailSectionTask")}</h4>
                <p>
                  {t("taskDetailTaskTitle")}: {taskTitle(selectedTaskDetail)}
                </p>
                <p>
                  {t("taskDetailTaskRole")}: {roleLabel(selectedTaskDetail.role)}
                </p>
                <p>
                  {t("taskDetailTaskStatus")}: {statusLabel(selectedTaskDetail.status, t)}
                </p>
                <p>
                  {t("taskDetailTaskAssignee")}: {selectedTaskDetail.assignee || "-"}
                </p>
                <p>
                  {t("taskDetailTaskEstimate")}: {selectedTaskDetail.estimateHours}
                </p>
                <p>
                  {t("taskDetailTaskProject")}: {selectedTaskDetail.projectName || selectedTaskDetail.projectId}
                </p>

                <h4>{t("taskDetailSectionHandoff")}</h4>
                <p>
                  {t("taskDetailHandoffTitle")}: {selectedTaskDetail.handoffTitle}
                </p>
                <p>
                  {t("taskDetailHandoffPriority")}: {selectedTaskDetail.handoffPriority}
                </p>
                <p>
                  {t("taskDetailHandoffStatus")}: {statusLabel(selectedTaskDetail.handoffStatus, t)}
                </p>
                <p>
                  {t("taskDetailHandoffTargetRole")}: {roleLabel(selectedTaskDetail.handoffTargetRole)}
                </p>
                <p>{t("taskDetailHandoffSummary")}:</p>
                <p>{selectedTaskDetail.handoffRequirementSummary}</p>
                <p>{t("taskDetailHandoffCriteria")}:</p>
                <p>{selectedTaskDetail.handoffAcceptanceCriteria}</p>
                <p>{t("taskDetailHandoffScope")}:</p>
                <p>{selectedTaskDetail.handoffImpactScope || "-"}</p>

                <h4>{t("taskDetailSectionActions")}</h4>
                <p>{roleOperationHint}</p>
                <div className="workspaceActions">
                  {canShowClaim(selectedTaskDetail) ? (
                    <button
                      className="secondary"
                      onClick={() => onClaim(selectedTaskDetail.id)}
                      disabled={loading || detailLoading}
                    >
                      {t("handoffClaimTask")}
                    </button>
                  ) : null}
                  {canUpdateStatus(selectedTaskDetail) ? (
                    <select
                      value={selectedTaskDetail.status}
                      disabled={loading || detailLoading}
                      onChange={(e) =>
                        void onUpdateTaskStatus(
                          selectedTaskDetail.id,
                          e.target.value as "todo" | "in_progress" | "done" | "blocked"
                        )
                      }
                    >
                      <option value="todo">{t("statusTodo")}</option>
                      <option value="in_progress">{t("statusInProgress")}</option>
                      <option value="done">{t("statusDone")}</option>
                      <option value="blocked">{t("statusBlocked")}</option>
                    </select>
                  ) : null}
                  {!canShowClaim(selectedTaskDetail) && !canUpdateStatus(selectedTaskDetail) ? (
                    <span className="taskDetailReadonly">{t("taskDetailReadonly")}</span>
                  ) : null}
                </div>
              </div>
            )}
          </div>
        </div>
      ) : null}

      {error && <p className="error">{error}</p>}
    </section>
  );
}

export default TaskCenterPage;
