import { useEffect, useMemo, useState } from "react";
import { useTranslation } from "react-i18next";
import {
  createProductDoc,
  getProductDoc,
  listProductDocAiMessages,
  listProductDocs,
  listProjects,
  streamProductDocAiMessage,
  updateProductDoc,
  type ProductDocAiMessageItem,
  type ProductDocDetailItem,
  type ProductDocItem,
  type ProjectItem,
  type UserProfile
} from "../../api/client";
import { statusClass, statusLabel } from "../../utils/status";

interface ProductDocCenterPageProps {
  language: string;
  currentRole: UserProfile["role"];
  currentActor: string;
}

function ProductDocCenterPage({ language, currentRole, currentActor }: ProductDocCenterPageProps) {
  const { t } = useTranslation();
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState("");
  const [notice, setNotice] = useState("");

  const [projects, setProjects] = useState<ProjectItem[]>([]);
  const [projectId, setProjectId] = useState("");

  const [docs, setDocs] = useState<ProductDocItem[]>([]);
  const [selectedDocId, setSelectedDocId] = useState("");
  const [docDetail, setDocDetail] = useState<ProductDocDetailItem | null>(null);

  const [createTitle, setCreateTitle] = useState("");
  const [createContent, setCreateContent] = useState("");

  const [aiMessages, setAiMessages] = useState<ProductDocAiMessageItem[]>([]);
  const [aiInstruction, setAiInstruction] = useState("");
  const [draftEditorContent, setDraftEditorContent] = useState("");
  const [draftDirty, setDraftDirty] = useState(false);
  const [aiStreaming, setAiStreaming] = useState(false);

  const canCreate = currentRole === "product" || currentRole === "admin";
  const canEditSelected =
    !!docDetail &&
    (currentRole === "admin" || (currentRole === "product" && docDetail.ownerActor === currentActor));

  const projectNameMap = useMemo(() => {
    const map = new Map<string, string>();
    projects.forEach((item) => map.set(item.id, item.name));
    return map;
  }, [projects]);

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

  const loadDocs = async (nextProjectId?: string) => {
    const data = await listProductDocs(language, {
      projectId: nextProjectId || undefined
    });
    setDocs(data);
    if (selectedDocId && !data.find((item) => item.id === selectedDocId)) {
      setSelectedDocId("");
      setDocDetail(null);
      setAiMessages([]);
      setDraftEditorContent("");
      setDraftDirty(false);
    }
  };

  const loadAiMessages = async (docId: string) => {
    const list = await listProductDocAiMessages(docId, language);
    setAiMessages(list);
  };

  const loadDocContext = async (docId: string) => {
    const detail = await getProductDoc(docId, language);
    setDocDetail(detail);
    setDraftEditorContent(detail.draftContent || "");
    setDraftDirty(false);
    await loadAiMessages(docId);
  };

  useEffect(() => {
    void (async () => {
      try {
        setLoading(true);
        setError("");
        setNotice("");
        await loadProjects();
        await loadDocs(projectId || undefined);
      } catch (e) {
        setError(e instanceof Error ? e.message : t("errorUnknown"));
      } finally {
        setLoading(false);
      }
    })();
  }, [language, projectId, t]);

  useEffect(() => {
    if (!selectedDocId) {
      return;
    }
    void (async () => {
      try {
        setLoading(true);
        setError("");
        setNotice("");
        await loadDocContext(selectedDocId);
      } catch (e) {
        setError(e instanceof Error ? e.message : t("errorUnknown"));
      } finally {
        setLoading(false);
      }
    })();
  }, [selectedDocId, language, t]);

  const onCreateDoc = async () => {
    if (!projectId || !createTitle.trim()) {
      setError(t("docCreateRequired"));
      return;
    }
    try {
      setLoading(true);
      setError("");
      setNotice("");
      const created = await createProductDoc(language, {
        projectId,
        title: createTitle.trim(),
        initialContent: createContent.trim() || undefined
      });
      setCreateTitle("");
      setCreateContent("");
      await loadDocs(projectId);
      setSelectedDocId(created.id);
    } catch (e) {
      setError(e instanceof Error ? e.message : t("errorUnknown"));
    } finally {
      setLoading(false);
    }
  };

  const onSaveDraft = async () => {
    if (!docDetail || !canEditSelected) {
      return;
    }
    try {
      setLoading(true);
      setError("");
      setNotice("");
      const updated = await updateProductDoc(docDetail.id, language, {
        title: docDetail.title,
        draftContent: draftEditorContent,
        status: docDetail.status
      });
      setDocDetail(updated);
      setDraftEditorContent(updated.draftContent || "");
      setDraftDirty(false);
      setNotice(t("docDraftSaved"));
      await loadDocs(projectId || undefined);
    } catch (e) {
      setError(e instanceof Error ? e.message : t("errorUnknown"));
    } finally {
      setLoading(false);
    }
  };

  const onSendAiInstruction = async () => {
    if (!docDetail) {
      return;
    }
    if (!aiInstruction.trim()) {
      setError(t("docAiInstructionRequired"));
      return;
    }

    try {
      setAiStreaming(true);
      setError("");
      setNotice("");
      const docId = docDetail.id;
      const instruction = aiInstruction.trim();

      if (draftDirty) {
        const latestDraft = await updateProductDoc(docId, language, {
          title: docDetail.title,
          draftContent: draftEditorContent,
          status: docDetail.status
        });
        setDocDetail(latestDraft);
      }

      const userMessage: ProductDocAiMessageItem = {
        id: `local-user-${Date.now()}`,
        docId,
        role: "user",
        content: instruction,
        createdBy: currentActor,
        createdAt: new Date().toISOString()
      };
      const assistantMessageId = `local-assistant-${Date.now()}`;
      const assistantMessage: ProductDocAiMessageItem = {
        id: assistantMessageId,
        docId,
        role: "assistant",
        content: "",
        createdBy: "ai-agent",
        createdAt: new Date().toISOString()
      };
      setAiMessages((prev) => [...prev, userMessage, assistantMessage]);
      setAiInstruction("");

      let streamedDraft = "";
      let streamError = "";

      await streamProductDocAiMessage(
        docId,
        language,
        { content: instruction },
        (payload) => {
          if (payload.event === "delta") {
            streamedDraft += payload.data;
            setDraftEditorContent(streamedDraft);
            setDraftDirty(true);
            setAiMessages((prev) =>
              prev.map((item) =>
                item.id === assistantMessageId ? { ...item, content: streamedDraft } : item
              )
            );
            return;
          }

          if (payload.event === "error") {
            try {
              const parsed = JSON.parse(payload.data) as { detail?: string };
              streamError = parsed.detail || payload.data || t("errorUnknown");
            } catch (_e) {
              streamError = payload.data || t("errorUnknown");
            }
          }
        }
      );

      if (streamError) {
        throw new Error(streamError);
      }

      const latest = await getProductDoc(docId, language);
      setDocDetail(latest);
      setDraftEditorContent(latest.draftContent || "");
      setDraftDirty(false);
      setNotice(t("docAiAppliedToDraft"));

      await loadDocs(projectId || undefined);
      await loadAiMessages(docId);
    } catch (e) {
      setError(e instanceof Error ? e.message : t("errorUnknown"));
      if (docDetail) {
        await loadAiMessages(docDetail.id).catch(() => undefined);
      }
    } finally {
      setAiStreaming(false);
    }
  };

  const disableAiAction = loading || aiStreaming;

  return (
    <section className="card">
      <h2>{t("docCenterTitle")}</h2>
      <p>{t("docCenterDesc")}</p>

      <div className="docLayout docLayoutAiOnly">
        <aside className="docListPane">
          <div className="docFilterRow">
            <select value={projectId} onChange={(e) => setProjectId(e.target.value)}>
              <option value="">{t("docProjectAll")}</option>
              {projects.map((project) => (
                <option value={project.id} key={project.id}>
                  {project.name}
                </option>
              ))}
            </select>
          </div>

          {canCreate ? (
            <div className="docCreateCard">
              <input
                value={createTitle}
                onChange={(e) => setCreateTitle(e.target.value)}
                placeholder={t("docCreateTitle")}
              />
              <textarea
                className="textArea"
                value={createContent}
                onChange={(e) => setCreateContent(e.target.value)}
                placeholder={t("docCreateInitial")}
              />
              <button onClick={onCreateDoc} disabled={disableAiAction}>
                {loading ? t("loading") : t("docCreate")}
              </button>
            </div>
          ) : null}

          <div className="workspaceList">
            {!loading && docs.length === 0 ? <p>{t("docEmpty")}</p> : null}
            {docs.map((doc) => (
              <article
                className={`workspaceItem docListItem ${selectedDocId === doc.id ? "docListItemActive" : ""}`}
                key={doc.id}
              >
                <div>
                  <h3>{doc.title}</h3>
                  <p>{projectNameMap.get(doc.projectId) || doc.projectId}</p>
                  <p>
                    {t("docVersionLabel")}: {doc.currentVersionNo ? `v${doc.currentVersionNo}` : "-"}
                  </p>
                </div>
                <div className="workspaceMeta">
                  <span className={statusClass(doc.status)}>{statusLabel(doc.status, t)}</span>
                  <button className="secondary" onClick={() => setSelectedDocId(doc.id)} disabled={disableAiAction}>
                    {t("docOpen")}
                  </button>
                </div>
              </article>
            ))}
          </div>
        </aside>

        <section className="docAiMainPane">
          {!selectedDocId || !docDetail ? (
            <p>{t("docSelectHint")}</p>
          ) : (
            <>
              <div className="docAiDocHeader">
                <h3>{docDetail.title}</h3>
                <span>
                  {t("docOwnerLabel")}: {docDetail.ownerActor}
                </span>
              </div>
              <p className="docAiMainDesc">{t("docFlowHintAi")}</p>
              {notice ? <p className="docDraftApplied">{notice}</p> : null}

              <div className="docAiLayout">
                <section className="docAiChatPane chatPanel">
                  <h3>{t("docAiTitle")}</h3>
                  <p>{t("docAiDesc")}</p>
                  <div className="chatMessages docAiMessages">
                    {aiMessages.length === 0 ? <p>{t("docAiEmpty")}</p> : null}
                    {aiMessages.map((message) => (
                      <article key={message.id} className={`chatMessage chatMessage-${message.role}`}>
                        <div className="chatMeta">
                          <span className="chatRole">{message.role}</span>
                          <span>{message.createdAt}</span>
                        </div>
                        <p>{message.content}</p>
                      </article>
                    ))}
                  </div>
                  {canEditSelected ? (
                    <div className="chatComposer">
                      <textarea
                        className="textArea"
                        value={aiInstruction}
                        onChange={(e) => setAiInstruction(e.target.value)}
                        placeholder={t("docAiInstructionPlaceholder")}
                      />
                      <button onClick={onSendAiInstruction} disabled={disableAiAction || !aiInstruction.trim()}>
                        {aiStreaming ? t("loading") : t("docAiGenerate")}
                      </button>
                    </div>
                  ) : (
                    <p className="taskDetailReadonly">{t("docAiReadonlyHint")}</p>
                  )}
                </section>

                <section className="docAiRecordPane">
                  <div className="docAiRecordHeader">
                    <h3>{t("docAiRevisionTitle")}</h3>
                    <span className={draftDirty ? "docDraftDirty" : "docDraftClean"}>
                      {draftDirty ? t("docDraftUnsaved") : t("docDraftSaved")}
                    </span>
                  </div>
                  <p>{t("docAiRecordDesc")}</p>
                  <textarea
                    className="textArea docAiMarkdownEditor"
                    value={draftEditorContent}
                    onChange={(e) => {
                      setDraftEditorContent(e.target.value);
                      setDraftDirty(true);
                    }}
                    placeholder={t("docAiMarkdownPlaceholder")}
                  />
                  <div className="docAiRecordActions">
                    <button onClick={onSaveDraft} disabled={disableAiAction || !draftDirty || !canEditSelected}>
                      {t("docAiSaveDraft")}
                    </button>
                  </div>
                </section>
              </div>
            </>
          )}
        </section>
      </div>

      {error && <p className="error">{error}</p>}
    </section>
  );
}

export default ProductDocCenterPage;
