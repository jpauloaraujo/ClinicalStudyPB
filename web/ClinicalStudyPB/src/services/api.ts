import axios from 'axios'

// Cliente HTTP único da aplicação (T14); a URL vem de VITE_API_URL
export const URL_API = import.meta.env.VITE_API_URL ?? 'http://localhost:8081'

export const api = axios.create({
  baseURL: URL_API,
  headers: { 'Content-Type': 'application/json' },
  timeout: 15000,
})

const CHAVE_TOKEN = 'clinicalstudypb.token'

export function lerToken(): string | null {
  try {
    return localStorage.getItem(CHAVE_TOKEN)
  } catch {
    return null
  }
}

export function salvarToken(token: string | null) {
  try {
    if (token) {
      localStorage.setItem(CHAVE_TOKEN, token)
    } else {
      localStorage.removeItem(CHAVE_TOKEN)
    }
  } catch {
    // Armazenamento indisponível: a sessão dura só enquanto a página estiver aberta
  }
}

api.interceptors.request.use((config) => {
  const token = lerToken()
  if (token) {
    config.headers.Authorization = `Bearer ${token}`
  }
  return config
})

// Chamado quando a API responde 401 fora do login (token expirado ou inválido)
let aoExpirarSessao: (() => void) | null = null

export function definirAoExpirarSessao(callback: (() => void) | null) {
  aoExpirarSessao = callback
}

api.interceptors.response.use(
  (resposta) => resposta,
  (erro) => {
    const ehLogin = erro.config?.url?.endsWith('/api/auth/login')
    if (erro.response?.status === 401 && !ehLogin) {
      aoExpirarSessao?.()
    }
    return Promise.reject(erro)
  },
)
