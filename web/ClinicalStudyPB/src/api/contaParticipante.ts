import { isAxiosError } from 'axios'
import { api } from '../services/api'
import type { ContaParticipanteResponse, ContaVinculadaResponse } from './tipos'

// Vínculo da conta do participante (RF17, T111): o coordenador liga uma conta de perfil PARTICIPANTE ao participante
// do estudo; pesquisador e coordenador consultam a conta vinculada. Não há desvincular nesta Sprint.
export const contaParticipanteApi = {
  // 404 quer dizer "este participante ainda não tem conta": é um estado normal da ficha, não um erro de tela
  buscar: async (participacaoId: string): Promise<ContaVinculadaResponse | null> => {
    try {
      return (await api.get<ContaVinculadaResponse>(`/api/participacoes/${participacaoId}/conta`)).data
    } catch (falha) {
      if (isAxiosError(falha) && falha.response?.status === 404) return null
      throw falha
    }
  },
  vincular: async (participacaoId: string, usuarioId: string) =>
    (await api.put<ContaVinculadaResponse>(`/api/participacoes/${participacaoId}/conta`, { usuarioId })).data,
  listarDisponiveis: async () =>
    (await api.get<ContaParticipanteResponse[]>('/api/contas-participante/disponiveis')).data,
}
