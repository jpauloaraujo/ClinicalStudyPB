import type { PerfilUsuario } from '../types/usuario'

// Fonte única dos itens de menu; reutilizar nos guards de rota (T25)
export type ItemNavegacao = {
  rotulo: string
  caminho: string
  perfis: PerfilUsuario[]
}

export const itensNavegacao: ItemNavegacao[] = [
  { rotulo: 'Início', caminho: '/inicio', perfis: ['ADMINISTRADOR', 'PESQUISADOR', 'COORDENADOR'] },
  { rotulo: 'Usuários', caminho: '/usuarios', perfis: ['ADMINISTRADOR'] },
  { rotulo: 'Estudos', caminho: '/estudos', perfis: ['PESQUISADOR', 'COORDENADOR'] },
]

export function itensDoPerfil(perfil: PerfilUsuario): ItemNavegacao[] {
  return itensNavegacao.filter((item) => item.perfis.includes(perfil))
}
