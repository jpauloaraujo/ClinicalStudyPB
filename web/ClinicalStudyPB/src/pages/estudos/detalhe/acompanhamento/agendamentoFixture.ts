import type {
  AgendaItemResponse,
  ParticipanteAgendaResponse,
  VisitaAgendadaResponse,
  VisitaDoParticipanteResponse,
} from '../../../../api/tipos'
import { estudoBase } from '../../../../test/ambiente'
import type { Estudo } from '../../../../types/estudo'

// Dados fictícios dos testes das telas de agendamento (T77)

export const estudoEmRecrutamento: Estudo = { ...estudoBase, status: 'RECRUTAMENTO' }
export const estudoEmPlanejamento: Estudo = { ...estudoBase, status: 'PLANEJAMENTO' }

export const PARTICIPACAO_ID = 'd6300000-0000-4000-8000-000000001921'

export function agendamentoDe(sobrescrever: Partial<VisitaAgendadaResponse> = {}): VisitaAgendadaResponse {
  return {
    id: 'va-1',
    participacaoId: PARTICIPACAO_ID,
    estudoId: estudoBase.id,
    visitaPlanejadaId: 'vp-2',
    visitaNome: 'Retorno 30 dias',
    participanteCodigo: 'CAND-001',
    participanteNome: 'Pessoa Teta (Ficticia)',
    dataHora: '2026-11-25T09:30:00-03:00',
    situacao: 'AGENDADA',
    dataHoraRealizada: null,
    motivo: null,
    observacao: null,
    janelaInicio: '2026-11-20',
    janelaFim: '2026-11-30',
    criadoPor: 'Coordenador Demo (Ficticio)',
    criadoEm: '2026-11-11T10:00:00-03:00',
    alteradoPor: 'Coordenador Demo (Ficticio)',
    alteradoEm: '2026-11-11T10:00:00-03:00',
    versao: 0,
    ...sobrescrever,
  }
}

// Triagem (realizada), Retorno 30 dias (agendada) e Retorno 60 dias (sem agendamento), como no estudo de demonstração
export const triagemRealizada: VisitaDoParticipanteResponse = {
  visitaPlanejadaId: 'vp-1',
  nome: 'Triagem',
  ordem: 1,
  diaPrevisto: 0,
  tolerancia: 3,
  dataSugerida: '2026-10-26',
  janelaInicio: '2026-10-23',
  janelaFim: '2026-10-29',
  agendamento: agendamentoDe({
    id: 'va-0',
    visitaPlanejadaId: 'vp-1',
    visitaNome: 'Triagem',
    dataHora: '2026-10-26T09:00:00-03:00',
    situacao: 'REALIZADA',
    dataHoraRealizada: '2026-10-26T09:10:00-03:00',
    janelaInicio: '2026-10-23',
    janelaFim: '2026-10-29',
  }),
}

export const retornoAgendado: VisitaDoParticipanteResponse = {
  visitaPlanejadaId: 'vp-2',
  nome: 'Retorno 30 dias',
  ordem: 2,
  diaPrevisto: 30,
  tolerancia: 5,
  dataSugerida: '2026-11-25',
  janelaInicio: '2026-11-20',
  janelaFim: '2026-11-30',
  agendamento: agendamentoDe(),
}

export const retornoSemAgendamento: VisitaDoParticipanteResponse = {
  visitaPlanejadaId: 'vp-3',
  nome: 'Retorno 60 dias',
  ordem: 3,
  diaPrevisto: 60,
  tolerancia: 7,
  dataSugerida: '2026-12-25',
  janelaInicio: '2026-12-18',
  janelaFim: '2027-01-01',
  agendamento: null,
}

export const visitasDoParticipante = [triagemRealizada, retornoAgendado, retornoSemAgendamento]

export const participanteDaAgenda: ParticipanteAgendaResponse = {
  participacaoId: PARTICIPACAO_ID,
  candidatoId: 'cand-1',
  codigo: 'CAND-001',
  nomeFicticio: 'Pessoa Teta (Ficticia)',
  dataInclusao: '2026-10-26',
  visitasPrevistas: 3,
  visitasAgendadas: 2,
  situacaoParticipacao: 'ATIVO',
}

export const participanteRetiradoDaAgenda: ParticipanteAgendaResponse = {
  ...participanteDaAgenda,
  situacaoParticipacao: 'RETIRADO',
}

export function itemDaAgenda(sobrescrever: Partial<AgendaItemResponse> = {}): AgendaItemResponse {
  return {
    visitaAgendadaId: 'va-1',
    participacaoId: PARTICIPACAO_ID,
    participanteCodigo: 'CAND-001',
    participanteNome: 'Pessoa Teta (Ficticia)',
    visitaPlanejadaId: 'vp-2',
    visitaNome: 'Retorno 30 dias',
    dataHora: '2026-11-25T09:30:00-03:00',
    situacao: 'AGENDADA',
    ...sobrescrever,
  }
}
