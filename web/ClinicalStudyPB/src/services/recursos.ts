import type { Estudo, EstudoDados } from '../types/estudo'
import type { Usuario, UsuarioAtualizacao, UsuarioCriacao, UsuarioSessao } from '../types/usuario'
import { api } from './api'

// Chamadas da API agrupadas por recurso; os tipos espelham os DTOs do backend

export const autenticacaoApi = {
  async entrar(email: string, senha: string): Promise<string> {
    const { data } = await api.post<{ token: string }>('/api/auth/login', { email, senha })
    return data.token
  },
  async usuarioAtual(): Promise<UsuarioSessao> {
    const { data } = await api.get<UsuarioSessao>('/api/auth/me')
    return data
  },
}

export const usuariosApi = {
  listar: async () => (await api.get<Usuario[]>('/api/usuarios')).data,
  buscar: async (id: string) => (await api.get<Usuario>(`/api/usuarios/${id}`)).data,
  criar: async (dados: UsuarioCriacao) => (await api.post<Usuario>('/api/usuarios', dados)).data,
  atualizar: async (id: string, dados: UsuarioAtualizacao) =>
    (await api.put<Usuario>(`/api/usuarios/${id}`, dados)).data,
  excluir: async (id: string) => {
    await api.delete(`/api/usuarios/${id}`)
  },
}

export const estudosApi = {
  listar: async () => (await api.get<Estudo[]>('/api/estudos')).data,
  buscar: async (id: string) => (await api.get<Estudo>(`/api/estudos/${id}`)).data,
  criar: async (dados: EstudoDados) => (await api.post<Estudo>('/api/estudos', dados)).data,
  atualizar: async (id: string, dados: EstudoDados) =>
    (await api.put<Estudo>(`/api/estudos/${id}`, dados)).data,
  excluir: async (id: string) => {
    await api.delete(`/api/estudos/${id}`)
  },
}
