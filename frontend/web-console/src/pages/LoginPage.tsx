import { FormEvent, useState } from "react";
import { useTranslation } from "react-i18next";

interface LoginPageProps {
  loading: boolean;
  error: string;
  onSubmit: (username: string, password: string) => Promise<void>;
  onToggleLanguage: () => Promise<void>;
  languageLabel: string;
}

function LoginPage({ loading, error, onSubmit, onToggleLanguage, languageLabel }: LoginPageProps) {
  const { t } = useTranslation();
  const [username, setUsername] = useState("product");
  const [password, setPassword] = useState("spider123");

  const submit = async (event: FormEvent) => {
    event.preventDefault();
    await onSubmit(username, password);
  };

  return (
    <main className="loginPage">
      <section className="loginCard">
        <div className="loginHeader">
          <span className="loginHeaderTag">{t("loginTitle")}</span>
          <button className="secondary" type="button" onClick={onToggleLanguage}>
            {t("language")} {languageLabel}
          </button>
        </div>
        <h1>{t("loginTitle")}</h1>
        <p>{t("loginDesc")}</p>
        <form onSubmit={submit} className="loginForm">
          <input value={username} onChange={(e) => setUsername(e.target.value)} placeholder={t("loginUsername")} />
          <input
            type="password"
            value={password}
            onChange={(e) => setPassword(e.target.value)}
            placeholder={t("loginPassword")}
          />
          <button type="submit" disabled={loading}>
            {loading ? t("loading") : t("loginSubmit")}
          </button>
        </form>
        {error && <p className="error">{error}</p>}
        <p className="loginHint">{t("loginHint")}</p>
      </section>
    </main>
  );
}

export default LoginPage;
