import { createContext, useContext } from 'react'
import type { UsuarioSessao } from '../types/usuario'

// Sessão autenticada (T21): token JWT guardado pelo cliente da API e dados de /api/auth/me
export type Sessao = {
  usuario: UsuarioSessao | null
  // true enquanto a sessão salva é restaurada ao abrir a página
  carregando: boolean
  entrar: (email: string, senha: string) => Promise<void>
  sair: () => void
}

export class PerfilSemAcessoWebError extends Error {
  constructor() {
    super('O perfil Participante utiliza o aplicativo mobile.')
  }
}

export const SessaoContext = createContext<Sessao | null>(null)

export function useSessao(): Sessao {
  const sessao = useContext(SessaoContext)
  if (!sessao) {
    throw new Error('useSessao deve ser usado dentro de <SessaoProvider>')
  }
  return sessao
}

// Para telas dentro de <RequerAutenticacao>, onde o usuário sempre existe
export function useUsuarioLogado(): UsuarioSessao {
  const { usuario } = useSessao()
  if (!usuario) {
    throw new Error('useUsuarioLogado deve ser usado em rotas protegidas')
  }
  return usuario
}

// Lê a claim exp do JWT para descartar tokens vencidos antes de chamar a API
export function tokenExpirado(token: string): boolean {
  try {
    const carga = token.split('.')[1].replace(/-/g, '+').replace(/_/g, '/')
    const { exp } = JSON.parse(atob(carga)) as { exp?: number }
    return typeof exp === 'number' && exp * 1000 <= Date.now()
  } catch {
    return true
  }
}
