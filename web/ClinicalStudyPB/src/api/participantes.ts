import { api } from '../services/api'
import type { ParticipacaoResponse, VagasResponse } from './tipos'

export const participantesApi = {
  listar: async (estudoId: string) =>
    (await api.get<ParticipacaoResponse[]>(`/api/estudos/${estudoId}/participantes`)).data,
  vagas: async (estudoId: string) =>
    (await api.get<VagasResponse>(`/api/estudos/${estudoId}/participantes/vagas`)).data,
  incluir: async (estudoId: string, candidatoId: string) =>
    (await api.post<ParticipacaoResponse>(`/api/estudos/${estudoId}/participantes`, { candidatoId })).data,
}
