import { api } from '../services/api'
import type { ConsentimentoRequest, ConsentimentoResponse } from './tipos'

export const consentimentoApi = {
  // 404 enquanto o consentimento não foi registrado
  buscar: async (candidatoId: string) =>
    (await api.get<ConsentimentoResponse>(`/api/candidatos/${candidatoId}/consentimento`)).data,
  registrar: async (candidatoId: string, dados: ConsentimentoRequest = {}) =>
    (await api.post<ConsentimentoResponse>(`/api/candidatos/${candidatoId}/consentimento`, dados)).data,
}
