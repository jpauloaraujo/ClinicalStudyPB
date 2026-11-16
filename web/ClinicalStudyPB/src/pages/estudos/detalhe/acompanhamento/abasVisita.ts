import type { PerfilUsuario } from '../../../../types/usuario'

export type AbaVisita = {
  // Identificador da aba na tela da visita (VisitaAgendadaPage)
  caminho: string
  rotulo: string
  perfis: PerfilUsuario[]
}

/**
 * Fonte única das abas da tela da visita (T77), no mesmo modelo de abas.ts.
 *
 * Como acrescentar uma aba (contrato dos próximos PBIs): inclua aqui `{ caminho, rotulo, perfis }` e trate o
 * novo `caminho` em `ConteudoDaAba`, dentro de VisitaAgendadaPage.tsx. Não é preciso reescrever o resto da tela.
 *  - Agora: `dados`.
 *  - T91 (PBI-21): `avaliacoes` (perfis PESQUISADOR e COORDENADOR).
 */
export const abasVisita: AbaVisita[] = [{ caminho: 'dados', rotulo: 'Dados', perfis: ['PESQUISADOR', 'COORDENADOR'] }]

export function abasVisitaDoPerfil(perfil: PerfilUsuario): AbaVisita[] {
  return abasVisita.filter((aba) => aba.perfis.includes(perfil))
}
