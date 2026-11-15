import type { VisitaAgendadaResponse } from '../api/tipos'

/** Visita agendada de demonstração para os testes de componente do desfecho (RF13). */
export const visitaAgendadaBase: VisitaAgendadaResponse = {
  id: 'va-1',
  participacaoId: 'part-1',
  estudoId: '3f6c1a52-0d1e-4c8b-9a77-5b2e8d4f1c90',
  visitaPlanejadaId: 'vp-1',
  visitaNome: 'Retorno 30 dias',
  participanteCodigo: 'CAND-001',
  participanteNome: 'Pessoa Teta (Ficticia)',
  dataHora: '2026-11-16T09:30:00-03:00',
  situacao: 'AGENDADA',
  dataHoraRealizada: null,
  motivo: null,
  observacao: null,
  janelaInicio: '2026-11-11',
  janelaFim: '2026-11-21',
  criadoPor: 'Coordenador Demo (Ficticio)',
  criadoEm: '2026-11-11T10:00:00-03:00',
  alteradoPor: 'Coordenador Demo (Ficticio)',
  alteradoEm: '2026-11-11T10:00:00-03:00',
  versao: 0,
}
