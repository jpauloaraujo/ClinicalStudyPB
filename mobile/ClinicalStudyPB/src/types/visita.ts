export type SituacaoVisita = 'AGENDADA' | 'REALIZADA' | 'AUSENTE' | 'CANCELADA';

export type VisitaParticipante = {
  id: string;
  estudo: string;
  nomeVisita: string;
  dataHora: string; // Formato ISO 8601
  situacao: SituacaoVisita;
  atividadesPrevistas: string[]; // Apenas nomes das atividades, sem resultados
  motivoCancelamento?: string;   // Obrigatório apenas se cancelada
};