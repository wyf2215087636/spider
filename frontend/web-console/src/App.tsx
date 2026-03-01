import { useEffect, useMemo, useState } from "react";
import { useTranslation } from "react-i18next";
import { Navigate, NavLink, Route, Routes } from "react-router-dom";
import { login, logout, me, readAuthToken, writeAuthToken, type UserProfile } from "./api/client";
import HandoffCenterPage from "./pages/collab/HandoffCenterPage";
import ProjectPage from "./pages/collab/ProjectPage";
import ProductDocCenterPage from "./pages/collab/ProductDocCenterPage";
import TaskCenterPage from "./pages/collab/TaskCenterPage";
import WorkspacePage from "./pages/collab/WorkspacePage";
import LoginPage from "./pages/LoginPage";
import AuditPage from "./pages/runtime/AuditPage";
import HealthPage from "./pages/runtime/HealthPage";

interface MenuItem {
  key: string;
  labelKey: string;
  to: string;
}

interface MenuGroup {
  key: string;
  labelKey: string;
  items: MenuItem[];
}

function SidebarMenu({
  groups,
  collapsedKeys,
  onToggle,
  t
}: {
  groups: MenuGroup[];
  collapsedKeys: Set<string>;
  onToggle: (key: string) => void;
  t: (key: string) => string;
}) {
  return (
    <ul className="menuLevel menuLevel1">
      {groups.map((group) => {
        const collapsed = collapsedKeys.has(group.key);
        return (
          <li key={group.key} className="menuItem">
            <button className="menuGroupToggle" type="button" onClick={() => onToggle(group.key)}>
              <span className="menuGroupTitle">{t(group.labelKey)}</span>
              <span className="menuGroupArrow">{collapsed ? "+" : "-"}</span>
            </button>
            {!collapsed ? (
              <ul className="menuLevel menuLevel2">
                {group.items.map((item) => (
                  <li key={item.key} className="menuItem">
                    <NavLink
                      to={item.to}
                      className={({ isActive }) => (isActive ? "menuLink active" : "menuLink")}
                    >
                      {t(item.labelKey)}
                    </NavLink>
                  </li>
                ))}
              </ul>
            ) : null}
          </li>
        );
      })}
    </ul>
  );
}

function App() {
  const { t, i18n } = useTranslation();

  const languageLabel = useMemo(() => (i18n.language === "zh-CN" ? "ZH" : "EN"), [i18n.language]);
  const [authLoading, setAuthLoading] = useState(true);
  const [authError, setAuthError] = useState("");
  const [user, setUser] = useState<UserProfile | null>(null);
  const menuGroups = useMemo<MenuGroup[]>(
    () => {
      if (!user) {
        return [];
      }

      const allItems = {
        workspace: { key: "workspace-list", labelKey: "menuWorkspace", to: "/workspaces" } satisfies MenuItem,
        project: { key: "project-list", labelKey: "menuProject", to: "/projects" } satisfies MenuItem,
        docCenter: { key: "doc-center", labelKey: "menuDocCenter", to: "/product-docs" } satisfies MenuItem,
        handoff: { key: "handoff-center", labelKey: "menuHandoffCenter", to: "/handoffs" } satisfies MenuItem,
        tasks: { key: "task-center", labelKey: "menuTaskCenter", to: "/tasks" } satisfies MenuItem,
        audit: { key: "audit-logs", labelKey: "menuAudit", to: "/audit" } satisfies MenuItem,
        health: { key: "system-health", labelKey: "menuHealth", to: "/health" } satisfies MenuItem
      };

      if (user.role === "admin") {
        return [
          { key: "collab", labelKey: "menuGroupCollab", items: [allItems.workspace, allItems.project, allItems.docCenter, allItems.handoff, allItems.tasks] },
          { key: "runtime", labelKey: "menuGroupRuntime", items: [allItems.audit, allItems.health] }
        ];
      }
      if (user.role === "product") {
        return [
          { key: "collab", labelKey: "menuGroupCollab", items: [allItems.workspace, allItems.project, allItems.docCenter, allItems.handoff, allItems.tasks] },
          { key: "runtime", labelKey: "menuGroupRuntime", items: [allItems.health] }
        ];
      }
      if (user.role === "pmo") {
        return [
          { key: "collab", labelKey: "menuGroupCollab", items: [allItems.project, allItems.docCenter, allItems.handoff, allItems.tasks] },
          { key: "runtime", labelKey: "menuGroupRuntime", items: [allItems.audit, allItems.health] }
        ];
      }
      return [
        { key: "collab", labelKey: "menuGroupCollab", items: [allItems.docCenter, allItems.handoff, allItems.tasks] },
        { key: "runtime", labelKey: "menuGroupRuntime", items: [allItems.health] }
      ];
    },
    [user]
  );
  const visiblePaths = useMemo(
    () => new Set(menuGroups.flatMap((group) => group.items.map((item) => item.to))),
    [menuGroups]
  );
  const defaultPath = useMemo(
    () => menuGroups.flatMap((group) => group.items.map((item) => item.to))[0] || "/health",
    [menuGroups]
  );
  const [collapsedKeys, setCollapsedKeys] = useState<Set<string>>(new Set());

  useEffect(() => {
    const token = readAuthToken();
    if (!token) {
      setAuthLoading(false);
      setUser(null);
      return;
    }
    void (async () => {
      try {
        const profile = await me(i18n.language);
        setUser(profile);
        setAuthError("");
      } catch {
        writeAuthToken("");
        setUser(null);
      } finally {
        setAuthLoading(false);
      }
    })();
  }, [i18n.language]);

  const toggleLanguage = async () => {
    const next = i18n.language === "zh-CN" ? "en-US" : "zh-CN";
    await i18n.changeLanguage(next);
  };

  const onLogin = async (username: string, password: string) => {
    try {
      setAuthLoading(true);
      setAuthError("");
      const result = await login(i18n.language, { username, password });
      writeAuthToken(result.token);
      setUser(result.user);
    } catch (e) {
      setAuthError(e instanceof Error ? e.message : t("errorUnknown"));
      setUser(null);
    } finally {
      setAuthLoading(false);
    }
  };

  const onLogout = async () => {
    try {
      await logout(i18n.language);
    } catch {
      // ignore logout errors and clear local state anyway
    }
    writeAuthToken("");
    setUser(null);
  };

  const toggleGroup = (key: string) => {
    setCollapsedKeys((prev) => {
      const next = new Set(prev);
      if (next.has(key)) {
        next.delete(key);
      } else {
        next.add(key);
      }
      return next;
    });
  };

  if (!user) {
    return (
      <LoginPage
        loading={authLoading}
        error={authError}
        onSubmit={onLogin}
        onToggleLanguage={toggleLanguage}
        languageLabel={languageLabel}
      />
    );
  }

  return (
    <main className="page">
      <header className="header">
        <div>
          <h1>{t("title")}</h1>
          <p>{t("subtitle")}</p>
        </div>
        <div className="headerActions">
          <div className="actorLabel">{user.displayName} ({user.role})</div>
          <button className="ghost" onClick={onLogout}>
            {t("logout")}
          </button>
          <button className="secondary" onClick={toggleLanguage}>
            {t("language")} {languageLabel}
          </button>
        </div>
      </header>

      <div className="workspaceLayout">
        <aside className="sidebar">
          <div className="sidebarTitle">{t("menuTitle")}</div>
          <SidebarMenu groups={menuGroups} collapsedKeys={collapsedKeys} onToggle={toggleGroup} t={t} />
        </aside>

        <section className="contentPane">
          <Routes>
            <Route path="/" element={<Navigate to={defaultPath} replace />} />
            <Route
              path="/workspaces"
              element={visiblePaths.has("/workspaces") ? <WorkspacePage language={i18n.language} /> : <Navigate to={defaultPath} replace />}
            />
            <Route
              path="/projects"
              element={visiblePaths.has("/projects") ? <ProjectPage language={i18n.language} /> : <Navigate to={defaultPath} replace />}
            />
            <Route
              path="/handoffs"
              element={
                visiblePaths.has("/handoffs") ? (
                  <HandoffCenterPage language={i18n.language} currentRole={user.role} />
                ) : (
                  <Navigate to={defaultPath} replace />
                )
              }
            />
            <Route
              path="/product-docs"
              element={
                visiblePaths.has("/product-docs") ? (
                  <ProductDocCenterPage language={i18n.language} currentRole={user.role} currentActor={user.username} />
                ) : (
                  <Navigate to={defaultPath} replace />
                )
              }
            />
            <Route
              path="/tasks"
              element={
                visiblePaths.has("/tasks") ? (
                  <TaskCenterPage language={i18n.language} currentRole={user.role} currentActor={user.username} />
                ) : (
                  <Navigate to={defaultPath} replace />
                )
              }
            />
            <Route
              path="/audit"
              element={visiblePaths.has("/audit") ? <AuditPage language={i18n.language} /> : <Navigate to={defaultPath} replace />}
            />
            <Route
              path="/health"
              element={visiblePaths.has("/health") ? <HealthPage language={i18n.language} /> : <Navigate to={defaultPath} replace />}
            />
            <Route path="*" element={<Navigate to={defaultPath} replace />} />
          </Routes>
        </section>
      </div>
    </main>
  );
}

export default App;
