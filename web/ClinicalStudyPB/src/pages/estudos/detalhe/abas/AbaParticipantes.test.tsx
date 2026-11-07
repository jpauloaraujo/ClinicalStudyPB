import { screen, within } from '@testing-library/react'
import { beforeEach, describe, expect, it, vi } from 'vitest'
import { participantesApi } from '../../../../api/participantes'
import type { ParticipacaoResponse } from '../../../../api/tipos'
import { erroHttp, estudoBase, renderizarAba } from '../../../../test/ambiente'
import { AbaParticipantes } from './AbaParticipantes'

vi.mock('../../../../api/participantes', () => ({ participantesApi: { listar: vi.fn(), vagas: vi.fn(), incluir: vi.fn() } }))

const listar = vi.mocked(participantesApi.listar)
const vagas = vi.mocked(participantesApi.vagas)

const primeiro: ParticipacaoResponse = {
  id: 'p-1', candidatoId: 'c-1', codigo: 'CAND-001', nomeFicticio: 'J. S.', estudoId: estudoBase.id,
  dataInclusao: '2026-11-04', incluidoPor: 'Coordenadora Fictícia',
}
const segundo: ParticipacaoResponse = { ...primeiro, id: 'p-2', candidatoId: 'c-2', codigo: 'CAND-002', nomeFicticio: 'M. P.' }

describe('AbaParticipantes (T61)', () => {
  beforeEach(() => {
    vi.clearAllMocks()
    listar.mockResolvedValue([primeiro, segundo])
    vagas.mockResolvedValue({ incluidos: 2, limite: 30, restantes: 28 })
  })

  it('lista os participantes com código, nome, data de inclusão e quem incluiu, e o contador "X de N vagas"', async () => {
    renderizarAba(<AbaParticipantes />, { perfil: 'COORDENADOR' })

    expect(await screen.findByText('2 de 30 vagas')).toBeInTheDocument()
    const linha = screen.getByRole('row', { name: /CAND-001/ })
    expect(within(linha).getByText('J. S.')).toBeInTheDocument()
    expect(within(linha).getByText('04/11/2026')).toBeInTheDocument()
    expect(within(linha).getByText('Coordenadora Fictícia')).toBeInTheDocument()
    expect(screen.getByRole('row', { name: /CAND-002/ })).toBeInTheDocument()
    expect(listar).toHaveBeenCalledWith(estudoBase.id)
  })

  it('sem participantes explica que a inclusão é feita na ficha do candidato', async () => {
    listar.mockResolvedValue([])
    vagas.mockResolvedValue({ incluidos: 0, limite: 30, restantes: 30 })
    renderizarAba(<AbaParticipantes />, { perfil: 'COORDENADOR' })

    expect(await screen.findByText(/Nenhum participante incluído ainda/)).toBeInTheDocument()
    expect(await screen.findByText('0 de 30 vagas')).toBeInTheDocument()
    expect(screen.queryByRole('table')).not.toBeInTheDocument()
  })

  it('mostra o erro do servidor', async () => {
    listar.mockRejectedValue(erroHttp(500, 'Erro interno do servidor'))
    renderizarAba(<AbaParticipantes />, { perfil: 'COORDENADOR' })

    expect(await screen.findByRole('alert')).toHaveTextContent('Erro interno do servidor')
    expect(screen.queryByRole('table')).not.toBeInTheDocument()
  })

  it('mostra o erro de acesso negado', async () => {
    vagas.mockRejectedValue(erroHttp(403, 'Acesso negado para o seu perfil'))
    renderizarAba(<AbaParticipantes />, { perfil: 'COORDENADOR' })

    expect(await screen.findByRole('alert')).toHaveTextContent('Acesso negado para o seu perfil')
  })

  it('indica o carregamento enquanto a lista não chega', async () => {
    listar.mockReturnValue(new Promise(() => {}))
    renderizarAba(<AbaParticipantes />, { perfil: 'COORDENADOR' })

    expect(await screen.findByText('Carregando participantes…')).toBeInTheDocument()
  })
})
