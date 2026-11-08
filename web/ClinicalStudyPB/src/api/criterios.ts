import { api } from '../services/api'
import type { CriterioRequest, CriterioResponse } from './tipos'

export const criteriosApi = {
  listar: async (estudoId: string) =>
    (await api.get<CriterioResponse[]>(`/api/estudos/${estudoId}/criterios`)).data,
  criar: async (estudoId: string, dados: CriterioRequest) =>
    (await api.post<CriterioResponse>(`/api/estudos/${estudoId}/criterios`, dados)).data,
  atualizar: async (estudoId: string, id: string, dados: CriterioRequest) =>
    (await api.put<CriterioResponse>(`/api/estudos/${estudoId}/criterios/${id}`, dados)).data,
  excluir: async (estudoId: string, id: string) => {
    await api.delete(`/api/estudos/${estudoId}/criterios/${id}`)
  },
}
