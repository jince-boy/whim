/// <reference types="vite/client" />

interface ImportMetaEnv {
  readonly VITE_APP_NAME: string
  readonly VITE_APP_TITLE: string
  readonly VITE_APP_FOOTER_TEXT: string
  readonly VITE_API_BASE_URL?: string
}
