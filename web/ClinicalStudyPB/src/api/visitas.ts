import { api } from '../services/api'
import type { AcaoRequest, AcaoResponse, VisitaRequest, VisitaResponse } from './tipos'

export const visitasApi = {
  listar: async (estudoId: string) => (await api.get<VisitaResponse[]>(`/api/estudos/${estudoId}/visitas`)).data,
  criar: async (estudoId: string, dados: VisitaRequest) =>
    (await api.post<VisitaResponse>(`/api/estudos/${estudoId}/visitas`, dados)).data,
  atualizar: async (estudoId: string, id: string, dados: VisitaRequest) =>
    (await api.put<VisitaResponse>(`/api/estudos/${estudoId}/visitas/${id}`, dados)).data,
  excluir: async (estudoId: string, id: string) => {
    await api.delete(`/api/estudos/${estudoId}/visitas/${id}`)
  },
}

export const acoesApi = {
  criar: async (visitaId: string, dados: AcaoRequest) =>
    (await api.post<AcaoResponse>(`/api/visitas/${visitaId}/acoes`, dados)).data,
  atualizar: async (visitaId: string, id: string, dados: AcaoRequest) =>
    (await api.put<AcaoResponse>(`/api/visitas/${visitaId}/acoes/${id}`, dados)).data,
  excluir: async (visitaId: string, id: string) => {
    await api.delete(`/api/visitas/${visitaId}/acoes/${id}`)
  },
}
