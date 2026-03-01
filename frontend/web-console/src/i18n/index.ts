import i18n from "i18next";
import { initReactI18next } from "react-i18next";
import zhCN from "./locales/zh-CN/common.json";
import enUS from "./locales/en-US/common.json";

const browserLang = navigator.language.toLowerCase().startsWith("zh")
  ? "zh-CN"
  : "en-US";

void i18n.use(initReactI18next).init({
  resources: {
    "zh-CN": {
      common: zhCN
    },
    "en-US": {
      common: enUS
    }
  },
  lng: browserLang,
  fallbackLng: "en-US",
  defaultNS: "common",
  interpolation: {
    escapeValue: false
  }
});

export default i18n;
