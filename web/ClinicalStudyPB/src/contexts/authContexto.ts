import { createContext, useContext } from 'react'
import type { UsuarioSessao } from '../types/usuario'

// AuthContext (T21): a porta de entrada da autenticação para as telas. Expõe o usuário logado (nome e perfil),
// o token, o estado da sessão e as operações de login e logout. Quem implementa a sessão de verdade (token no
// cliente HTTP, restauração ao abrir a página, expiração) é o SessaoProvider; o AuthProvider o empacota.
export type AuthContextType = {
  usuario: UsuarioSessao | null
  token: string | null
  isAuthenticated: boolean
  // true enquanto a sessão salva é restaurada ao abrir a página
  carregando: boolean
  login: (email: string, senha: string) => Promise<void>
  logout: () => void
}

export const AuthContext = createContext<AuthContextType | null>(null)

export function useAuth(): AuthContextType {
  const contexto = useContext(AuthContext)
  if (!contexto) {
    throw new Error('useAuth deve ser usado dentro de <AuthProvider>')
  }
  return contexto
}
