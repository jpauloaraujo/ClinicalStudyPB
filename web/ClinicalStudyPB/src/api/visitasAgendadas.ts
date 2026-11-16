import { api } from '../services/api'
import type { DesfechoRequest, VisitaAgendadaResponse } from './tipos'

// Visita agendada e seu desfecho (RF13, contrato da Sprint 3). Os resultados das avaliações entram na T91.
export const visitasAgendadasApi = {
  buscar: async (visitaAgendadaId: string) =>
    (await api.get<VisitaAgendadaResponse>(`/api/visitas-agendadas/${visitaAgendadaId}`)).data,
  registrarDesfecho: async (visitaAgendadaId: string, dados: DesfechoRequest) =>
    (await api.post<VisitaAgendadaResponse>(`/api/visitas-agendadas/${visitaAgendadaId}/desfecho`, dados)).data,
}
