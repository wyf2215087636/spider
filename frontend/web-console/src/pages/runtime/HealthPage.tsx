import { useState } from "react";
import { useTranslation } from "react-i18next";
import { fetchHealth, type HealthResult } from "../../api/client";

interface HealthPageProps {
  language: string;
}

function HealthPage({ language }: HealthPageProps) {
  const { t } = useTranslation();
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState("");
  const [health, setHealth] = useState<HealthResult | null>(null);

  const onCheck = async () => {
    try {
      setLoading(true);
      setError("");
      const data = await fetchHealth(language);
      setHealth(data);
    } catch (e) {
      setError(e instanceof Error ? e.message : t("errorUnknown"));
      setHealth(null);
    } finally {
      setLoading(false);
    }
  };

  return (
    <section className="card">
      <h2>{t("healthCheck")}</h2>
      <p>{t("healthDesc")}</p>
      <button onClick={onCheck} disabled={loading}>
        {loading ? t("loading") : t("runHealth")}
      </button>

      {health && <pre className="result">{JSON.stringify(health, null, 2)}</pre>}
      {error && <p className="error">{error}</p>}
    </section>
  );
}

export default HealthPage;

