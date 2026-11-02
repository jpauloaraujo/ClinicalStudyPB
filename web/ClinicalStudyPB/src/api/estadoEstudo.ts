import { api } from '../services/api'
import type { Estudo, StatusEstudo } from '../types/estudo'
import type { HistoricoStatusResponse } from './tipos'

// RF07: o cadastro do estudo (CRUD) continua em services/recursos.ts; aqui ficam o avanço de estado e o histórico
export const estadoEstudoApi = {
  avancar: async (estudoId: string, destino: StatusEstudo) =>
    (await api.post<Estudo>(`/api/estudos/${estudoId}/transicoes`, { destino })).data,
  historico: async (estudoId: string) =>
    (await api.get<HistoricoStatusResponse[]>(`/api/estudos/${estudoId}/historico-status`)).data,
}
