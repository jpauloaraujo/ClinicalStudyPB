import type { PerfilUsuario } from '../types/usuario'

// Fonte única dos itens de menu; os guards de rota (T25) leem os perfis daqui
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

// Perfis de uma rota lidos do menu, para manter menu e guards consistentes
export function perfisDaRota(caminho: string): PerfilUsuario[] {
  const item = itensNavegacao.find((it) => it.caminho === caminho)
  if (!item) {
    throw new Error(`Rota sem item de navegação: ${caminho}`)
  }
  return item.perfis
}

// Verifica se o perfil pode abrir um caminho (ex.: /estudos/novo pertence a /estudos)
export function podeAcessar(caminho: string, perfil: PerfilUsuario): boolean {
  const item = itensNavegacao.find((it) => caminho === it.caminho || caminho.startsWith(`${it.caminho}/`))
  return item ? item.perfis.includes(perfil) : false
}
