import { api } from '../services/api'
import type { AvaliacaoRequest, AvaliacaoResponse } from './tipos'

export const avaliacoesApi = {
  // Histórico: da avaliação vigente (a mais recente) para a mais antiga
  historico: async (candidatoId: string) =>
    (await api.get<AvaliacaoResponse[]>(`/api/candidatos/${candidatoId}/avaliacoes`)).data,
  avaliar: async (candidatoId: string, dados: AvaliacaoRequest) =>
    (await api.post<AvaliacaoResponse>(`/api/candidatos/${candidatoId}/avaliacoes`, dados)).data,
}
