import { useState, type ReactNode } from 'react'
import type { PerfilUsuario } from '../types/usuario'
import { SessaoContext, usuariosProvisorios } from './sessao'

export function SessaoProvider({ children }: { children: ReactNode }) {
  const [usuario, setUsuario] = useState(usuariosProvisorios.PESQUISADOR)

  function trocarPerfil(perfil: PerfilUsuario) {
    if (perfil !== 'PARTICIPANTE') {
      setUsuario(usuariosProvisorios[perfil])
    }
  }

  return (
    <SessaoContext.Provider value={{ usuario, trocarPerfil }}>
      {children}
    </SessaoContext.Provider>
  )
}
