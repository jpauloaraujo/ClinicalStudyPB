// Define as situações possíveis de uma visita para o participante
export type SituacaoVisita = 'AGENDADA' | 'REALIZADA' | 'AUSENTE' | 'CANCELADA';

// Contrato da Visita do Participante retornado pelas rotas /me
export type VisitaParticipante = {
  id: string;                          // Identificador único da visita agendaad
  estudo: string;                      // Nome ou título do estudo clínico
  nomeVisita: string;                  // Nome da visita prevista (ex: Visita de Retorno 1)
  dataHora: string;                    // Data e hora com fuso horário no formato ISO 8601
  situacao: SituacaoVisita;            // Situação atual da visita
  atividadesPrevistas: string[];       // Apenas os nomes das atividades previstas (sem resultados)
  motivoCancelamento?: string;         // Obrigatório apenas se a situação for CANCELADA
};