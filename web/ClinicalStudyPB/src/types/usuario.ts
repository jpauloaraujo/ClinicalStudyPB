// Espelha o enum PerfilUsuario do backend
export type PerfilUsuario = 'ADMINISTRADOR' | 'PESQUISADOR' | 'COORDENADOR' | 'PARTICIPANTE'

export const perfis: PerfilUsuario[] = ['ADMINISTRADOR', 'PESQUISADOR', 'COORDENADOR', 'PARTICIPANTE']

export const rotulosPerfil: Record<PerfilUsuario, string> = {
  ADMINISTRADOR: 'Administrador',
  PESQUISADOR: 'Pesquisador',
  COORDENADOR: 'Coordenador',
  PARTICIPANTE: 'Participante',
}

// Perfis atendidos pela aplicação web; o participante usa o app mobile
export const perfisWeb: PerfilUsuario[] = ['ADMINISTRADOR', 'PESQUISADOR', 'COORDENADOR']

export type UsuarioSessao = {
  id: string
  nome: string
  email: string
  perfil: PerfilUsuario
}

// UsuarioResponse da API
export type Usuario = UsuarioSessao & {
  ativo: boolean
}

export type UsuarioCriacao = {
  nome: string
  email: string
  senha: string
  perfil: PerfilUsuario
}

export type UsuarioAtualizacao = {
  nome: string
  email: string
  senha?: string
  perfil: PerfilUsuario
  ativo: boolean
}
