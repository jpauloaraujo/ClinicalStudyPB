// Espelha o enum PerfilUsuario do backend
export type PerfilUsuario = 'ADMINISTRADOR' | 'PESQUISADOR' | 'COORDENADOR' | 'PARTICIPANTE'

export const rotulosPerfil: Record<PerfilUsuario, string> = {
  ADMINISTRADOR: 'Administrador',
  PESQUISADOR: 'Pesquisador',
  COORDENADOR: 'Coordenador',
  PARTICIPANTE: 'Participante',
}

export type UsuarioSessao = {
  id: string
  nome: string
  perfil: PerfilUsuario
}
