import { api } from '../services/api'
import type {
  PaginaAgenda,
  ParticipanteAgendaResponse,
  SituacaoVisita,
  VisitaAgendadaRequest,
  VisitaAgendadaResponse,
  VisitaDoParticipanteResponse,
  VisitaReagendamentoRequest,
} from './tipos'

// Filtros da agenda do estudo: `de` e `ate` são datas (aaaa-mm-dd) inclusivas; a página começa em 0
export type FiltroAgenda = {
  de?: string
  ate?: string
  situacao?: SituacaoVisita
  pagina?: number
  tamanho?: number
}

// Agendamento de visitas (RF12, T76). Todas as telas do agendamento chamam a API por aqui.
export const agendamentoApi = {
  visitasDoParticipante: async (participacaoId: string) =>
    (await api.get<VisitaDoParticipanteResponse[]>(`/api/participacoes/${participacaoId}/visitas`)).data,
  agendar: async (participacaoId: string, dados: VisitaAgendadaRequest) =>
    (await api.post<VisitaAgendadaResponse>(`/api/participacoes/${participacaoId}/visitas-agendadas`, dados)).data,
  reagendar: async (visitaAgendadaId: string, dados: VisitaReagendamentoRequest) =>
    (await api.put<VisitaAgendadaResponse>(`/api/visitas-agendadas/${visitaAgendadaId}`, dados)).data,
  agenda: async (estudoId: string, filtro: FiltroAgenda = {}) =>
    (await api.get<PaginaAgenda>(`/api/estudos/${estudoId}/agenda`, { params: filtro })).data,
  participantesDaAgenda: async (estudoId: string) =>
    (await api.get<ParticipanteAgendaResponse[]>(`/api/estudos/${estudoId}/agenda/participantes`)).data,
}
