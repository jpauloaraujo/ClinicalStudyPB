import { createContext, useContext } from 'react'

export type AuthContextType = {
  token: string | null
  login: (token: string) => void
  logout: () => void
  isAuthenticated: boolean
}

// Contexto de autenticação simples (token JWT). Fica em arquivo próprio porque o fast refresh do Vite
// só aceita componentes nos arquivos .tsx.
export const AuthContext = createContext<AuthContextType | null>(null)

export function useAuth(): AuthContextType {
  const contexto = useContext(AuthContext)
  if (!contexto) {
    throw new Error('useAuth deve ser usado dentro de <AuthProvider>')
  }
  return contexto
}
