import { api } from '../services/api'
import type { CandidatoRequest, CandidatoResponse, PaginaCandidatos, SituacaoCandidato } from './tipos'

export type FiltroCandidatos = {
  situacao?: SituacaoCandidato
  pagina?: number
  tamanho?: number
}

export const candidatosApi = {
  listar: async (estudoId: string, filtro: FiltroCandidatos = {}) =>
    (await api.get<PaginaCandidatos>(`/api/estudos/${estudoId}/candidatos`, { params: filtro })).data,
  buscar: async (id: string) => (await api.get<CandidatoResponse>(`/api/candidatos/${id}`)).data,
  criar: async (estudoId: string, dados: CandidatoRequest) =>
    (await api.post<CandidatoResponse>(`/api/estudos/${estudoId}/candidatos`, dados)).data,
  atualizar: async (id: string, dados: CandidatoRequest) =>
    (await api.put<CandidatoResponse>(`/api/candidatos/${id}`, dados)).data,
}
