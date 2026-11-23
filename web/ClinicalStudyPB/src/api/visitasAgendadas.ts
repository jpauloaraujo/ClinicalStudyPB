import { api } from '../services/api'
import type {
  DesfechoRequest,
  ResultadoAtividadeResponse,
  ResultadoRequest,
  ResultadosDaVisitaResponse,
  VisitaAgendadaResponse,
} from './tipos'

// Visita agendada, seu desfecho (RF13) e os resultados das avaliações (RF14, T91), contrato da Sprint 3.
export const visitasAgendadasApi = {
  buscar: async (visitaAgendadaId: string) =>
    (await api.get<VisitaAgendadaResponse>(`/api/visitas-agendadas/${visitaAgendadaId}`)).data,
  registrarDesfecho: async (visitaAgendadaId: string, dados: DesfechoRequest) =>
    (await api.post<VisitaAgendadaResponse>(`/api/visitas-agendadas/${visitaAgendadaId}/desfecho`, dados)).data,
  // Operação 8: atividades de avaliação da visita, com o resultado de cada uma (nulo = pendente) e o bloqueio
  resultados: async (visitaAgendadaId: string) =>
    (await api.get<ResultadosDaVisitaResponse>(`/api/visitas-agendadas/${visitaAgendadaId}/resultados`)).data,
  // Operação 9: registra (201) ou edita (200) o resultado de uma atividade; só o pesquisador
  registrarResultado: async (visitaAgendadaId: string, acaoId: string, corpo: ResultadoRequest) =>
    (
      await api.put<ResultadoAtividadeResponse>(
        `/api/visitas-agendadas/${visitaAgendadaId}/resultados/${acaoId}`,
        corpo,
      )
    ).data,
}
