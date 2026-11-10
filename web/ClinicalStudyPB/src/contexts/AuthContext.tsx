import { useState, type ReactNode } from 'react'
import { lerToken, salvarToken } from '../services/api'
import { AuthContext } from './authContexto'

// Guarda o token JWT na mesma chave do cliente HTTP (services/api.ts), para que o interceptor
// envie o cabeçalho Authorization sem nenhuma configuração extra.
export function AuthProvider({ children }: { children: ReactNode }) {
  const [token, setToken] = useState<string | null>(() => lerToken())

  const login = (novoToken: string) => {
    salvarToken(novoToken)
    setToken(novoToken)
  }

  const logout = () => {
    salvarToken(null)
    setToken(null)
  }

  return <AuthContext.Provider value={{ token, login, logout, isAuthenticated: !!token }}>{children}</AuthContext.Provider>
}
