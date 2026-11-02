import type { PerfilUsuario } from '../../../types/usuario'

export type AbaEstudo = {
  // Segmento da rota aninhada em /estudos/:estudoId
  caminho: string
  rotulo: string
  perfis: PerfilUsuario[]
}

// Fonte única das abas do detalhe do estudo (T39): a barra de abas e os guards
// das rotas aninhadas leem os perfis daqui, como o menu faz em navegacao.ts
export const abasEstudo: AbaEstudo[] = [
  { caminho: 'resumo', rotulo: 'Resumo', perfis: ['PESQUISADOR', 'COORDENADOR'] },
  { caminho: 'criterios', rotulo: 'Critérios', perfis: ['PESQUISADOR'] },
  { caminho: 'visitas', rotulo: 'Visitas', perfis: ['PESQUISADOR'] },
  { caminho: 'candidatos', rotulo: 'Candidatos', perfis: ['COORDENADOR'] },
  { caminho: 'participantes', rotulo: 'Participantes', perfis: ['COORDENADOR'] },
]

export function abasDoPerfil(perfil: PerfilUsuario): AbaEstudo[] {
  return abasEstudo.filter((aba) => aba.perfis.includes(perfil))
}

export function perfisDaAba(caminho: string): PerfilUsuario[] {
  const aba = abasEstudo.find((it) => it.caminho === caminho)
  if (!aba) {
    throw new Error(`Aba de estudo inexistente: ${caminho}`)
  }
  return aba.perfis
}
