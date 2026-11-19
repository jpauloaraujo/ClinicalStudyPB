import { VisitaParticipante } from '../types/visitas';

// Próximas visitas (Apenas AGENDADA, de hoje em diante, ordenadas da mais próxima para a mais distante)
export const proximasVisitasMock: VisitaParticipante[] = [
  {
    id: "v-01",
    estudo: "Estudo Clínico Fase II - Cardiologia",
    nomeVisita: "Visita de Acompanhamento 30 dias",
    dataHora: "2026-11-24T09:30:00-03:00",
    situacao: "AGENDADA",
    atividadesPrevistas: ["Exame de Sangue", "Avaliação de Sintomas", "Eletrocardiograma"]
  },
  {
    id: "v-02",
    estudo: "Estudo Clínico Fase II - Cardiologia",
    nomeVisita: "Visita de Acompanhamento 60 dias",
    dataHora: "2026-12-15T14:00:00-03:00",
    situacao: "AGENDADA",
    atividadesPrevistas: ["Consulta Médica", "Entrega de Diário"]
  }
];

// Histórico de comparecimento (REALIZADA, AUSENTE e CANCELADA, ordenadas da mais recente para a mais antiga)
export const historicoVisitasMock: VisitaParticipante[] = [
  {
    id: "v-prev-01",
    estudo: "Estudo Clínico Fase II - Cardiologia",
    nomeVisita: "Visita Inicial / Triagem",
    dataHora: "2026-10-10T10:00:00-03:00",
    situacao: "REALIZADA",
    atividadesPrevistas: ["Termo de Consentimento", "Exame Físico"]
  },
  {
    id: "v-prev-02",
    estudo: "Estudo Clínico Fase II - Cardiologia",
    nomeVisita: "Visita de Retorno 15 dias",
    dataHora: "2026-10-25T11:00:00-03:00",
    situacao: "CANCELADA",
    atividadesPrevistas: ["Avaliação de Rotina"],
    motivoCancelamento: "Remanejamento de agenda a pedido do centro"
  }
];