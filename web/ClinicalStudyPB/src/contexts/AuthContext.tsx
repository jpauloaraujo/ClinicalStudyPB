import type { ReactNode } from 'react'
import { lerToken } from '../services/api'
import { AuthContext } from './authContexto'
import { SessaoProvider } from './SessaoProvider'
import { useSessao } from './sessao'

// Lê a sessão e a publica no AuthContext. O token vem do cliente HTTP (services/api.ts), a mesma chave que o
// interceptor usa para enviar o cabeçalho Authorization.
function PonteAuth({ children }: { children: ReactNode }) {
  const { usuario, carregando, entrar, sair } = useSessao()
  const token = usuario ? lerToken() : null
  return (
    <AuthContext.Provider
      value={{ usuario, token, isAuthenticated: usuario !== null, carregando, login: entrar, logout: sair }}
    >
      {children}
    </AuthContext.Provider>
  )
}

// Provedor de autenticação da aplicação: login, sessão restaurada, expiração e logout (RF02).
export function AuthProvider({ children }: { children: ReactNode }) {
  return (
    <SessaoProvider>
      <PonteAuth>{children}</PonteAuth>
    </SessaoProvider>
  )
}
