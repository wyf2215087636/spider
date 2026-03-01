import { useEffect, useMemo, useState } from "react";
import { useTranslation } from "react-i18next";
import {
  claimRequirementTask,
  createChatSession,
  generateAiDraft,
  generateAiRequirementTasks,
  listChatMessages,
  listChatSessions,
  listProjects,
  listRequirementHandoffs,
  listRequirementTasks,
  publishChatSession,
  sendChatMessage,
  transitionRequirementHandoff,
  updateRequirementTaskStatus,
  type AiDraftItem,
  type AiTaskPlanItem,
  type ChatMessageItem,
  type ChatSessionItem,
  type ProjectItem,
  type RequirementHandoffItem,
  type RequirementTaskItem
} from "../../api/client";
import { statusClass, statusLabel } from "../../utils/status";

interface HandoffCenterPageProps {
  language: string;
  currentRole: "product" | "backend" | "frontend" | "test" | "pmo" | "admin";
}

type HandoffAction =
  | "submit_review"
  | "publish"
  | "accept"
  | "start_dev"
  | "start_test"
  | "complete"
  | "reject"
  | "reopen";

function HandoffCenterPage({ language, currentRole }: HandoffCenterPageProps) {
  const { t } = useTranslation();
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState("");

  const [projects, setProjects] = useState<ProjectItem[]>([]);
  const [projectId, setProjectId] = useState("");

  const [sessionTitle, setSessionTitle] = useState("");
  const [sessions, setSessions] = useState<ChatSessionItem[]>([]);
  const [selectedSessionId, setSelectedSessionId] = useState("");
  const [messages, setMessages] = useState<ChatMessageItem[]>([]);
  const [chatInput, setChatInput] = useState("");

  const [handoffTitle, setHandoffTitle] = useState("");
  const [handoffSummary, setHandoffSummary] = useState("");
  const [handoffCriteria, setHandoffCriteria] = useState("");
  const [handoffScope, setHandoffScope] = useState("");
  const [handoffPriority, setHandoffPriority] = useState<"P0" | "P1" | "P2" | "P3">("P2");
  const [handoffTargetRole, setHandoffTargetRole] = useState<"product" | "backend" | "frontend" | "test">("backend");
  const [aiDraft, setAiDraft] = useState<AiDraftItem | null>(null);

  const [handoffFilterRole, setHandoffFilterRole] = useState("");
  const [handoffFilterStatus, setHandoffFilterStatus] = useState("");
  const [handoffs, setHandoffs] = useState<RequirementHandoffItem[]>([]);
  const [selectedHandoffId, setSelectedHandoffId] = useState("");

  const [tasks, setTasks] = useState<RequirementTaskItem[]>([]);
  const [taskPlan, setTaskPlan] = useState<AiTaskPlanItem | null>(null);

  const selectedSession = useMemo(
    () => sessions.find((item) => item.id === selectedSessionId) || null,
    [selectedSessionId, sessions]
  );

  const selectedHandoff = useMemo(
    () => handoffs.find((item) => item.id === selectedHandoffId) || null,
    [handoffs, selectedHandoffId]
  );

  const canManageSessions = currentRole === "product" || currentRole === "admin";
  const canGenerateTasks = currentRole === "pmo" || currentRole === "admin";

  const roleLabel = (
    role: "product" | "backend" | "frontend" | "test" | "pmo" | "admin"
  ) => {
    if (role === "product") {
      return t("roleProduct");
    }
    if (role === "backend") {
      return t("roleBackend");
    }
    if (role === "frontend") {
      return t("roleFrontend");
    }
    if (role === "pmo") {
      return t("rolePmo");
    }
    if (role === "admin") {
      return t("roleAdmin");
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

  const actionLabel = (action: HandoffAction) => {
    const keyMap: Record<HandoffAction, string> = {
      submit_review: "handoffActionSubmitReview",
      publish: "handoffActionPublish",
      accept: "handoffActionAccept",
      start_dev: "handoffActionStartDev",
      start_test: "handoffActionStartTest",
      complete: "handoffActionComplete",
      reject: "handoffActionReject",
      reopen: "handoffActionReopen"
    };
    return t(keyMap[action]);
  };

  const getAvailableActions = (handoff: RequirementHandoffItem): HandoffAction[] => {
    const role = currentRole;
    if (handoff.status === "draft" && (role === "product" || role === "admin")) {
      return ["submit_review", "reject"];
    }
    if (handoff.status === "in_review" && (role === "product" || role === "admin")) {
      return ["publish", "reject"];
    }
    if (handoff.status === "published" && (role === handoff.targetRole || role === "admin")) {
      return ["accept", "reject"];
    }
    if (handoff.status === "accepted" && (role === "backend" || role === "frontend" || role === "admin")) {
      return ["start_dev", "reject"];
    }
    if (handoff.status === "in_development" && (role === "test" || role === "admin")) {
      return ["start_test", "reject"];
    }
    if (handoff.status === "in_testing" && (role === "test" || role === "product" || role === "admin")) {
      return ["complete", "reject"];
    }
    if (handoff.status === "rejected" && (role === "product" || role === "admin")) {
      return ["reopen"];
    }
    return [];
  };

  const loadProjects = async () => {
    const data = await listProjects(language);
    setProjects(data);
    if (!projectId && data.length > 0) {
      setProjectId(data[0].id);
    }
    if (projectId && !data.find((item) => item.id === projectId)) {
      setProjectId("");
    }
  };

  const loadSessions = async (nextProjectId?: string) => {
    const data = await listChatSessions(language, { projectId: nextProjectId || undefined });
    setSessions(data);
    if (selectedSessionId && !data.find((item) => item.id === selectedSessionId)) {
      setSelectedSessionId("");
      setMessages([]);
      setAiDraft(null);
    }
  };

  const loadMessages = async (sessionId?: string) => {
    const id = sessionId || selectedSessionId;
    if (!id) {
      setMessages([]);
      return;
    }
    const data = await listChatMessages(id, language);
    setMessages(data);
  };

  const loadHandoffs = async (nextProjectId?: string, roleFilter?: string, statusFilter?: string) => {
    const data = await listRequirementHandoffs(language, {
      projectId: nextProjectId || undefined,
      targetRole: roleFilter || undefined,
      status: statusFilter || undefined
    });
    setHandoffs(data);
    if (selectedHandoffId && !data.find((item) => item.id === selectedHandoffId)) {
      setSelectedHandoffId("");
      setTasks([]);
      setTaskPlan(null);
    }
  };

  const loadTasks = async (handoffId?: string) => {
    const id = handoffId || selectedHandoffId;
    if (!id) {
      setTasks([]);
      return;
    }
    const data = await listRequirementTasks(id, language);
    setTasks(data);
  };

  const refreshData = async (
    nextProjectId?: string,
    roleFilter?: string,
    statusFilter?: string
  ) => {
    try {
      setLoading(true);
      setError("");
      await loadProjects();
      await loadSessions(nextProjectId);
      await loadHandoffs(nextProjectId, roleFilter, statusFilter);
    } catch (e) {
      setError(e instanceof Error ? e.message : t("errorUnknown"));
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    void refreshData(projectId || undefined, handoffFilterRole || undefined, handoffFilterStatus || undefined);
  }, [language, projectId, handoffFilterRole, handoffFilterStatus]);

  useEffect(() => {
    void loadMessages();
  }, [selectedSessionId, language]);

  useEffect(() => {
    void loadTasks();
  }, [selectedHandoffId, language]);

  const onCreateSession = async () => {
    try {
      if (!projectId || !sessionTitle.trim()) {
        setError(t("handoffSessionRequired"));
        return;
      }
      setLoading(true);
      setError("");
      const created = await createChatSession(language, {
        projectId,
        title: sessionTitle.trim()
      });
      setSessionTitle("");
      setSelectedSessionId(created.id);
      await loadSessions(projectId);
      await loadMessages(created.id);
    } catch (e) {
      setError(e instanceof Error ? e.message : t("errorUnknown"));
    } finally {
      setLoading(false);
    }
  };

  const onUseSession = async (session: ChatSessionItem) => {
    setSelectedSessionId(session.id);
    setAiDraft(null);
    if (!handoffTitle.trim()) {
      setHandoffTitle(session.title);
    }
    await loadMessages(session.id);
  };

  const onSendMessage = async () => {
    try {
      if (!selectedSessionId || !chatInput.trim()) {
        setError(t("handoffChatRequired"));
        return;
      }
      setLoading(true);
      setError("");
      await sendChatMessage(selectedSessionId, language, { content: chatInput.trim() });
      setChatInput("");
      await loadMessages(selectedSessionId);
    } catch (e) {
      setError(e instanceof Error ? e.message : t("errorUnknown"));
    } finally {
      setLoading(false);
    }
  };

  const onGenerateDraft = async () => {
    try {
      if (!selectedSessionId) {
        setError(t("handoffNoSession"));
        return;
      }
      setLoading(true);
      setError("");
      const draft = await generateAiDraft(selectedSessionId, language, {
        requirementInput: handoffSummary.trim() || chatInput.trim() || undefined,
        priority: handoffPriority,
        targetRole: handoffTargetRole
      });
      setAiDraft(draft);
      setHandoffTitle(draft.title);
      setHandoffSummary(draft.requirementSummary);
      setHandoffCriteria(draft.acceptanceCriteria);
      setHandoffScope(draft.impactScope);
      setHandoffPriority(draft.priority);
      setHandoffTargetRole(draft.targetRole);
      await loadMessages(selectedSessionId);
    } catch (e) {
      setError(e instanceof Error ? e.message : t("errorUnknown"));
    } finally {
      setLoading(false);
    }
  };

  const onPublish = async () => {
    try {
      if (!selectedSessionId || !handoffTitle.trim() || !handoffSummary.trim() || !handoffCriteria.trim()) {
        setError(t("handoffPublishRequired"));
        return;
      }
      setLoading(true);
      setError("");
      const created = await publishChatSession(selectedSessionId, language, {
        title: handoffTitle.trim(),
        requirementSummary: handoffSummary.trim(),
        acceptanceCriteria: handoffCriteria.trim(),
        impactScope: handoffScope.trim() || undefined,
        priority: handoffPriority,
        targetRole: handoffTargetRole
      });
      setHandoffSummary("");
      setHandoffCriteria("");
      setHandoffScope("");
      setAiDraft(null);
      setSelectedHandoffId(created.id);
      await loadHandoffs(projectId || undefined, handoffFilterRole || undefined, handoffFilterStatus || undefined);
      await loadTasks(created.id);
    } catch (e) {
      setError(e instanceof Error ? e.message : t("errorUnknown"));
    } finally {
      setLoading(false);
    }
  };

  const onTransition = async (handoffId: string, action: HandoffAction) => {
    try {
      setLoading(true);
      setError("");
      await transitionRequirementHandoff(handoffId, language, { action });
      await loadHandoffs(projectId || undefined, handoffFilterRole || undefined, handoffFilterStatus || undefined);
    } catch (e) {
      setError(e instanceof Error ? e.message : t("errorUnknown"));
    } finally {
      setLoading(false);
    }
  };

  const onGenerateTasks = async (handoffId: string) => {
    try {
      setLoading(true);
      setError("");
      const plan = await generateAiRequirementTasks(handoffId, language);
      setTaskPlan(plan);
      setTasks(plan.tasks);
      setSelectedHandoffId(handoffId);
    } catch (e) {
      setError(e instanceof Error ? e.message : t("errorUnknown"));
    } finally {
      setLoading(false);
    }
  };

  const onClaimTask = async (taskId: string) => {
    try {
      setLoading(true);
      setError("");
      await claimRequirementTask(taskId, language);
      await loadTasks();
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
    } catch (e) {
      setError(e instanceof Error ? e.message : t("errorUnknown"));
    } finally {
      setLoading(false);
    }
  };

  return (
    <section className="card">
      <h2>{t("handoffTitle")}</h2>
      <p>{t("handoffDesc")}</p>

      <div className="formRow">
        <select value={projectId} onChange={(e) => setProjectId(e.target.value)}>
          <option value="">{t("handoffProject")}</option>
          {projects.map((item) => (
            <option value={item.id} key={item.id}>
              {item.name}
            </option>
          ))}
        </select>
        <input value={roleLabel(currentRole)} readOnly />
        {canManageSessions ? (
          <>
            <input
              value={sessionTitle}
              onChange={(e) => setSessionTitle(e.target.value)}
              placeholder={t("handoffSessionTitle")}
            />
            <button onClick={onCreateSession} disabled={loading}>
              {loading ? t("loading") : t("handoffCreateSession")}
            </button>
          </>
        ) : null}
      </div>

      {canManageSessions ? (
        <div className="workspaceList">
          {!loading && sessions.length === 0 && <p>{t("handoffSessionEmpty")}</p>}
          {sessions.map((item) => (
            <article className="workspaceItem" key={item.id}>
              <div>
                <h3>{item.title}</h3>
                <p>
                  {t("handoffSessionOwner")}: {item.ownerActor} | {t("handoffSessionRole")}: {roleLabel(item.role)}
                </p>
              </div>
              <div className="workspaceActions">
                <button className="secondary" onClick={() => void onUseSession(item)}>
                  {selectedSessionId === item.id ? t("handoffSessionSelected") : t("handoffUseSession")}
                </button>
              </div>
            </article>
          ))}
        </div>
      ) : null}

      {canManageSessions ? (
        <div className="chatPanel">
          <h3>{t("handoffChatTitle")}</h3>
          <p>{t("handoffChatDesc")}</p>
          <div className="chatMessages">
            {!selectedSessionId ? <p>{t("handoffNoSession")}</p> : null}
            {selectedSessionId && messages.length === 0 ? <p>{t("handoffChatEmpty")}</p> : null}
            {messages.map((item) => (
              <article key={item.id} className={`chatMessage chatMessage-${item.role}`}>
                <div className="chatMeta">
                  <span className="chatRole">{item.role}</span>
                  <span>{item.createdBy}</span>
                </div>
                <p>{item.content}</p>
              </article>
            ))}
          </div>
          <div className="chatComposer">
            <textarea
              className="textArea"
              value={chatInput}
              onChange={(e) => setChatInput(e.target.value)}
              placeholder={t("handoffChatInput")}
            />
            <button onClick={onSendMessage} disabled={loading || !selectedSessionId}>
              {loading ? t("loading") : t("handoffSend")}
            </button>
          </div>
        </div>
      ) : null}

      {canManageSessions ? (
        <div className="handoffForm">
          <h3>{t("handoffPublishTitle")}</h3>
          <p>
            {t("handoffSelectedSession")}: {selectedSession ? selectedSession.title : t("handoffNoSession")}
          </p>
          <div className="formRow">
            <input value={handoffTitle} onChange={(e) => setHandoffTitle(e.target.value)} placeholder={t("handoffPkgTitle")} />
            <select
              value={handoffTargetRole}
              onChange={(e) => setHandoffTargetRole(e.target.value as "product" | "backend" | "frontend" | "test")}
            >
              <option value="backend">{t("roleBackend")}</option>
              <option value="frontend">{t("roleFrontend")}</option>
              <option value="test">{t("roleTest")}</option>
              <option value="product">{t("roleProduct")}</option>
            </select>
            <select value={handoffPriority} onChange={(e) => setHandoffPriority(e.target.value as "P0" | "P1" | "P2" | "P3")}>
              <option value="P0">P0</option>
              <option value="P1">P1</option>
              <option value="P2">P2</option>
              <option value="P3">P3</option>
            </select>
            <button className="secondary" onClick={onGenerateDraft} disabled={loading || !selectedSessionId}>
              {loading ? t("loading") : t("handoffGenerateDraft")}
            </button>
          </div>
          <textarea
            className="textArea"
            value={handoffSummary}
            onChange={(e) => setHandoffSummary(e.target.value)}
            placeholder={t("handoffSummary")}
          />
          <textarea
            className="textArea"
            value={handoffCriteria}
            onChange={(e) => setHandoffCriteria(e.target.value)}
            placeholder={t("handoffCriteria")}
          />
          <textarea
            className="textArea"
            value={handoffScope}
            onChange={(e) => setHandoffScope(e.target.value)}
            placeholder={t("handoffScope")}
          />
          <button onClick={onPublish} disabled={loading}>
            {loading ? t("loading") : t("handoffPublish")}
          </button>
        </div>
      ) : null}

      {aiDraft ? (
        <div className="result">
          <h3>{t("handoffDraftResult")}</h3>
          <p>
            {t("handoffModelLabel")}: {aiDraft.provider}/{aiDraft.model}
          </p>
          <p>{aiDraft.aiReply}</p>
          <p>{t("handoffImpactedFiles")}: {aiDraft.impactedFiles.join(", ") || "-"}</p>
          <p>{t("handoffSuggestedTasks")}: {aiDraft.suggestedTasks.join(" | ") || "-"}</p>
          <p>{t("handoffRiskHints")}: {aiDraft.riskHints.join(" | ") || "-"}</p>
          <p>{t("handoffTestHints")}: {aiDraft.testHints.join(" | ") || "-"}</p>
        </div>
      ) : null}

      <div className="handoffFilterRow">
        <select value={handoffFilterRole} onChange={(e) => setHandoffFilterRole(e.target.value)}>
          <option value="">{t("handoffFilterRoleAll")}</option>
          <option value="product">{t("roleProduct")}</option>
          <option value="backend">{t("roleBackend")}</option>
          <option value="frontend">{t("roleFrontend")}</option>
          <option value="test">{t("roleTest")}</option>
        </select>
        <select value={handoffFilterStatus} onChange={(e) => setHandoffFilterStatus(e.target.value)}>
          <option value="">{t("handoffFilterStatusAll")}</option>
          <option value="draft">{t("statusDraft")}</option>
          <option value="in_review">{t("statusInReview")}</option>
          <option value="published">{t("statusPublished")}</option>
          <option value="accepted">{t("statusAccepted")}</option>
          <option value="in_development">{t("statusInDevelopment")}</option>
          <option value="in_testing">{t("statusInTesting")}</option>
          <option value="done">{t("statusDone")}</option>
          <option value="rejected">{t("statusRejected")}</option>
        </select>
      </div>

      <div className="workspaceList">
        {!loading && handoffs.length === 0 && <p>{t("handoffEmpty")}</p>}
        {handoffs.map((item) => (
          <article className="workspaceItem" key={item.id}>
            <div>
              <h3>{item.title}</h3>
              <p>v{item.version} | {t("handoffTargetRole")}: {roleLabel(item.targetRole)}</p>
              <p>{item.requirementSummary}</p>
            </div>
            <div className="workspaceMeta">
              <span className={statusClass(item.status)}>{statusLabel(item.status, t)}</span>
              <span>{item.priority}</span>
              <div className="workspaceActions">
                <button className="secondary" onClick={() => setSelectedHandoffId(item.id)} disabled={loading}>
                  {t("handoffTasksTitle")}
                </button>
                {getAvailableActions(item).map((action) => (
                  <button
                    key={action}
                    onClick={() => onTransition(item.id, action)}
                    disabled={loading}
                  >
                    {actionLabel(action)}
                  </button>
                ))}
              </div>
            </div>
          </article>
        ))}
      </div>

      {selectedHandoff ? (
        <div className="result">
          <h3>{t("handoffTasksTitle")}</h3>
          <p>{t("handoffTasksDesc")}</p>
          <p>
            {selectedHandoff.title} | {statusLabel(selectedHandoff.status, t)}
          </p>
          {canGenerateTasks ? (
            <button onClick={() => onGenerateTasks(selectedHandoff.id)} disabled={loading}>
              {loading ? t("loading") : t("handoffGenerateTasks")}
            </button>
          ) : null}
          <div className="workspaceList">
            {tasks.length === 0 ? <p>{t("handoffTasksEmpty")}</p> : null}
            {tasks.map((task) => (
              <article className="workspaceItem" key={task.id}>
                <div>
                  <h3>{taskTitle(task)}</h3>
                  <p>{taskDescription(task)}</p>
                  <p>
                    {roleLabel(task.role)} | {t("handoffTaskEstimate")}: {task.estimateHours}
                  </p>
                </div>
                <div className="workspaceMeta">
                  <span className={statusClass(task.status)}>{statusLabel(task.status, t)}</span>
                  <span>{t("handoffTaskAssignee")}: {task.assignee || "-"}</span>
                  <div className="workspaceActions">
                    <button className="secondary" onClick={() => onClaimTask(task.id)} disabled={loading}>
                      {t("handoffClaimTask")}
                    </button>
                    <select
                      value={task.status}
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
          {taskPlan ? (
            <div>
              <p>
                {t("handoffModelLabel")}: {taskPlan.provider}/{taskPlan.model}
              </p>
              <p>{taskPlan.rationale}</p>
              <p>{t("handoffImpactedFiles")}: {taskPlan.impactedFiles.join(", ") || "-"}</p>
              <p>{t("handoffRiskHints")}: {taskPlan.riskHints.join(" | ") || "-"}</p>
              <p>{t("handoffTestHints")}: {taskPlan.testHints.join(" | ") || "-"}</p>
            </div>
          ) : null}
        </div>
      ) : null}

      {error && <p className="error">{error}</p>}
    </section>
  );
}

export default HandoffCenterPage;
