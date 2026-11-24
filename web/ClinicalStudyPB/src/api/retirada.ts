import { api } from '../services/api'
import type { RetiradaRequest, RetiradaResponse } from './tipos'

// Endpoints da retirada de participante (RF16, T103/T104)
export const retiradaApi = {
  registrar: async (participacaoId: string, dados: RetiradaRequest) =>
    (await api.post<RetiradaResponse>(`/api/participacoes/${participacaoId}/retirada`, dados)).data,

  buscar: async (participacaoId: string) =>
    (await api.get<RetiradaResponse>(`/api/participacoes/${participacaoId}/retirada`)).data,
}
