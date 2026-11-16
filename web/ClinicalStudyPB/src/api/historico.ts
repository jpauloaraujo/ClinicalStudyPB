import { api } from '../services/api'
import type { HistoricoParticipanteResponse } from './tipos'

// Histórico de um participante (RF15, T96): somente leitura, para pesquisador e coordenador
export const historicoApi = {
  buscar: async (participacaoId: string) =>
    (await api.get<HistoricoParticipanteResponse>(`/api/participacoes/${participacaoId}/historico`)).data,
}
