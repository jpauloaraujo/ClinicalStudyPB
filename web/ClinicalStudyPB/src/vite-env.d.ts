/// <reference types="vite/client" />

interface ImportMetaEnv {
  /** URL base da API REST (ex.: http://localhost:8081). */
  readonly VITE_API_URL?: string
}

interface ImportMeta {
  readonly env: ImportMetaEnv
}
