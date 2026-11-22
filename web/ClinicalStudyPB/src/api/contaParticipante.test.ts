import { beforeEach, describe, expect, it, vi } from 'vitest'
import { erroHttp } from '../test/ambiente'
import { api } from '../services/api'
import { contaParticipanteApi } from './contaParticipante'

vi.mock('../services/api', () => ({ api: { get: vi.fn(), put: vi.fn() } }))

const get = vi.mocked(api.get)
const put = vi.mocked(api.put)

const conta = {
  participacaoId: 'p-1',
  usuarioId: 'u-1',
  nome: 'Maria Participante (Ficticia)',
  email: 'maria@clinicalstudy.test',
}

describe('contaParticipanteApi (T111, RQ-T111-10)', () => {
  beforeEach(() => vi.clearAllMocks())

  it('buscar devolve a conta vinculada', async () => {
    get.mockResolvedValue({ data: conta })

    await expect(contaParticipanteApi.buscar('p-1')).resolves.toEqual(conta)
    expect(get).toHaveBeenCalledWith('/api/participacoes/p-1/conta')
  })

  it('buscar trata 404 como "sem conta" (null), nunca como erro de tela', async () => {
    get.mockRejectedValue(erroHttp(404, 'Este participante não tem conta vinculada', 'RECURSO_NAO_ENCONTRADO'))

    await expect(contaParticipanteApi.buscar('p-1')).resolves.toBeNull()
  })

  it.each([[403], [500]])('buscar propaga o erro %s', async (status) => {
    get.mockRejectedValue(erroHttp(status, 'falha'))

    await expect(contaParticipanteApi.buscar('p-1')).rejects.toMatchObject({ response: { status } })
  })

  it('vincular envia só o usuarioId por PUT e devolve a conta', async () => {
    put.mockResolvedValue({ data: conta })

    await expect(contaParticipanteApi.vincular('p-1', 'u-1')).resolves.toEqual(conta)
    expect(put).toHaveBeenCalledWith('/api/participacoes/p-1/conta', { usuarioId: 'u-1' })
  })

  it('listarDisponiveis lê as contas livres', async () => {
    const lista = [{ usuarioId: 'u-2', nome: 'Ana', email: 'ana@clinicalstudy.test' }]
    get.mockResolvedValue({ data: lista })

    await expect(contaParticipanteApi.listarDisponiveis()).resolves.toEqual(lista)
    expect(get).toHaveBeenCalledWith('/api/contas-participante/disponiveis')
  })
})
