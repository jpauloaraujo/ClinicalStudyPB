import { createContext, useContext } from 'react'
import type { PerfilUsuario, UsuarioSessao } from '../types/usuario'

// Sessão provisória até a T21 (login real e AuthContext)
export type Sessao = {
  usuario: UsuarioSessao
  trocarPerfil: (perfil: PerfilUsuario) => void
}

// Usuários do seed V2__seed_initial_data
export const usuariosProvisorios: Record<Exclude<PerfilUsuario, 'PARTICIPANTE'>, UsuarioSessao> = {
  ADMINISTRADOR: {
    id: '11111111-1111-4111-8111-111111111111',
    nome: 'Administrador do Sistema',
    perfil: 'ADMINISTRADOR',
  },
  PESQUISADOR: {
    id: '22222222-2222-4222-8222-222222222222',
    nome: 'Dra. Helena Costa',
    perfil: 'PESQUISADOR',
  },
  COORDENADOR: {
    id: '33333333-3333-4333-8333-333333333333',
    nome: 'Carlos Eduardo',
    perfil: 'COORDENADOR',
  },
}

export const SessaoContext = createContext<Sessao | null>(null)

export function useSessao(): Sessao {
  const sessao = useContext(SessaoContext)
  if (!sessao) {
    throw new Error('useSessao deve ser usado dentro de <SessaoProvider>')
  }
  return sessao
}
