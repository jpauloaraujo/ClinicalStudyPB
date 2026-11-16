import type { SituacaoVisita } from '../../../../api/tipos'

// "Não agendada" não é uma situação da visita agendada: é a visita prevista que ainda não tem agendamento
// (histórico do participante, T97).
export type SituacaoDoSelo = SituacaoVisita | 'NAO_AGENDADA'

export const rotulosSituacaoVisita: Record<SituacaoDoSelo, string> = {
  AGENDADA: 'Agendada',
  REALIZADA: 'Realizada',
  AUSENTE: 'Ausente',
  CANCELADA: 'Cancelada',
  NAO_AGENDADA: 'Não agendada',
}

// Situações na ordem do filtro do histórico do participante (T97): primeiro as visitas ainda sem agendamento
export const situacoesDoHistorico: SituacaoDoSelo[] = ['NAO_AGENDADA', 'AGENDADA', 'REALIZADA', 'AUSENTE', 'CANCELADA']
