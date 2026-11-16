import type { HistoricoParticipanteResponse, VisitaDoHistorico } from '../../../../api/tipos'

// Dados de teste do histórico (T97): P1 do demo, com a Triagem realizada, o Retorno 30 agendado e o Retorno 60 sem agendamento
export const participacaoId = 'd6300000-0000-4000-8000-000000001921'

export const triagem: VisitaDoHistorico = {
  visitaPlanejadaId: 'vp-1',
  nome: 'Triagem',
  ordem: 1,
  diaPrevisto: 0,
  tolerancia: 3,
  dataSugerida: '2026-10-20',
  situacao: 'REALIZADA',
  visitaAgendadaId: 'va-1',
  dataHoraAgendada: '2026-10-20T09:00:00-03:00',
  dataHoraRealizada: '2026-10-20T09:10:00-03:00',
  motivo: null,
  observacao: 'Paciente colaborativo',
  resultados: [
    { acaoId: 'a-1', nome: 'Aferir pressão arterial', valor: '120/80', unidade: 'mmHg', observacao: 'Em repouso' },
    { acaoId: 'a-2', nome: 'Escala de dor', valor: '2', unidade: null, observacao: null },
  ],
}

export const retorno30: VisitaDoHistorico = {
  visitaPlanejadaId: 'vp-2',
  nome: 'Retorno 30 dias',
  ordem: 2,
  diaPrevisto: 30,
  tolerancia: 5,
  dataSugerida: '2026-11-19',
  situacao: 'AGENDADA',
  visitaAgendadaId: 'va-2',
  dataHoraAgendada: '2026-11-24T09:30:00-03:00',
  dataHoraRealizada: null,
  motivo: null,
  observacao: null,
  resultados: [],
}

export const retorno60: VisitaDoHistorico = {
  visitaPlanejadaId: 'vp-3',
  nome: 'Retorno 60 dias',
  ordem: 3,
  diaPrevisto: 60,
  tolerancia: 7,
  dataSugerida: '2026-12-19',
  situacao: 'NAO_AGENDADA',
  visitaAgendadaId: null,
  dataHoraAgendada: null,
  dataHoraRealizada: null,
  motivo: null,
  observacao: null,
  resultados: [],
}

export const cancelada: VisitaDoHistorico = {
  ...retorno60,
  visitaPlanejadaId: 'vp-4',
  nome: 'Retorno 90 dias',
  ordem: 4,
  diaPrevisto: 90,
  situacao: 'CANCELADA',
  visitaAgendadaId: 'va-4',
  dataHoraAgendada: '2027-01-18T10:00:00-03:00',
  motivo: 'Participante em viagem',
}

export const historicoBase: HistoricoParticipanteResponse = {
  participante: {
    participacaoId,
    candidatoId: 'c-1',
    codigo: 'CAND-001',
    nomeFicticio: 'Pessoa Teta (Ficticia)',
    dataInclusao: '2026-10-20',
  },
  resumo: { previstas: 3, realizadas: 1, ausentes: 0, canceladas: 0, agendadas: 1, naoAgendadas: 1, pendentes: 2 },
  visitas: [triagem, retorno30, retorno60],
}
