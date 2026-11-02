/// <reference types="vite/client" />

interface ImportMetaEnv {
  /** URL base da API REST (ex.: http://localhost:8081). */
  readonly VITE_API_URL?: string
  /** "true" liga o Mock Service Worker (API fictícia, sem backend). Desligado por padrão. */
  readonly VITE_USE_MOCKS?: string
}

interface ImportMeta {
  readonly env: ImportMetaEnv
}
