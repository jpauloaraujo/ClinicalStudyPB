import { screen, within } from '@testing-library/react'
import { beforeEach, describe, expect, it, vi } from 'vitest'
import { agendamentoApi } from '../../../../api/agendamento'
import { participantesApi } from '../../../../api/participantes'
import type { ParticipacaoResponse } from '../../../../api/tipos'
import { erroHttp, estudoBase, renderizarAba } from '../../../../test/ambiente'
import { AbaParticipantes } from './AbaParticipantes'

vi.mock('../../../../api/participantes', () => ({ participantesApi: { listar: vi.fn(), vagas: vi.fn(), incluir: vi.fn() } }))
vi.mock('../../../../api/agendamento', () => ({
  agendamentoApi: {
    visitasDoParticipante: vi.fn(),
    agendar: vi.fn(),
    reagendar: vi.fn(),
    agenda: vi.fn(),
    participantesDaAgenda: vi.fn(),
  },
}))

const listar = vi.mocked(participantesApi.listar)
const vagas = vi.mocked(participantesApi.vagas)
const progresso = vi.mocked(agendamentoApi.participantesDaAgenda)

const primeiro: ParticipacaoResponse = {
  id: 'p-1', candidatoId: 'c-1', codigo: 'CAND-001', nomeFicticio: 'J. S.', estudoId: estudoBase.id,
  dataInclusao: '2026-11-04', incluidoPor: 'Coordenadora Fictícia', situacao: 'ATIVO',
}
const segundo: ParticipacaoResponse = { ...primeiro, id: 'p-2', candidatoId: 'c-2', codigo: 'CAND-002', nomeFicticio: 'M. P.' }

describe('AbaParticipantes (T61, T77 e T104)', () => {
  beforeEach(() => {
    vi.clearAllMocks()
    listar.mockResolvedValue([primeiro, segundo])
    vagas.mockResolvedValue({ incluidos: 2, limite: 30, restantes: 28 })
    progresso.mockResolvedValue([
      { participacaoId: 'p-1', candidatoId: 'c-1', codigo: 'CAND-001', nomeFicticio: 'J. S.', dataInclusao: '2026-11-04', visitasPrevistas: 3, visitasAgendadas: 2, situacaoParticipacao: 'ATIVO' },
      { participacaoId: 'p-2', candidatoId: 'c-2', codigo: 'CAND-002', nomeFicticio: 'M. P.', dataInclusao: '2026-11-04', visitasPrevistas: 3, visitasAgendadas: 0, situacaoParticipacao: 'ATIVO' },
    ])
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

  it('mostra o progresso "X de N visitas agendadas" de cada participante (T77)', async () => {
    renderizarAba(<AbaParticipantes />, { perfil: 'COORDENADOR' })

    expect(await within(await screen.findByRole('row', { name: /CAND-001/ })).findByText('2 de 3 visitas agendadas')).toBeInTheDocument()
    expect(within(screen.getByRole('row', { name: /CAND-002/ })).getByText('0 de 3 visitas agendadas')).toBeInTheDocument()
    expect(progresso).toHaveBeenCalledWith(estudoBase.id)
  })

  it('o coordenador tem o botão Agendamento de cada participante', async () => {
    renderizarAba(<AbaParticipantes />, { perfil: 'COORDENADOR' })

    const link = await screen.findByRole('link', { name: 'Agendamento de CAND-001' })
    expect(link).toHaveTextContent('Agendamento')
    expect(link).toHaveAttribute('href', `/estudos/${estudoBase.id}/participantes/p-1/agendamento`)
    expect(screen.queryByText('Seu perfil tem acesso apenas para consulta dos participantes e da agenda.')).not.toBeInTheDocument()
  })

  it('o pesquisador vê a lista e o progresso, sem o botão Agendamento (somente leitura)', async () => {
    renderizarAba(<AbaParticipantes />, { perfil: 'PESQUISADOR' })

    expect(await screen.findByText('2 de 3 visitas agendadas')).toBeInTheDocument()
    expect(screen.queryByRole('link', { name: 'Agendamento de CAND-001' })).not.toBeInTheDocument()
    expect(screen.getByRole('link', { name: 'Ver agendamento de CAND-001' })).toBeInTheDocument()
    expect(screen.getByText('Seu perfil tem acesso apenas para consulta dos participantes e da agenda.')).toBeInTheDocument()
  })

  it('o nome de cada participante abre a ficha com o histórico (T97), para o coordenador e para o pesquisador', async () => {
    renderizarAba(<AbaParticipantes />, { perfil: 'PESQUISADOR' })

    const linha = await screen.findByRole('row', { name: /CAND-002/ })
    expect(within(linha).getByRole('link', { name: 'Abrir a ficha de M. P.' })).toHaveAttribute(
      'href',
      `/estudos/${estudoBase.id}/participantes/p-2`,
    )
    expect(screen.getByRole('link', { name: 'Abrir a ficha de J. S.' })).toHaveAttribute(
      'href',
      `/estudos/${estudoBase.id}/participantes/p-1`,
    )
  })

  it('sem participantes explica que a inclusão é feita na ficha do candidato', async () => {
    listar.mockResolvedValue([])
    vagas.mockResolvedValue({ incluidos: 0, limite: 30, restantes: 30 })
    progresso.mockResolvedValue([])
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

  it('se só o progresso falhar, a lista continua visível com um traço no lugar do progresso', async () => {
    progresso.mockRejectedValue(erroHttp(500, 'Falha na agenda'))
    renderizarAba(<AbaParticipantes />, { perfil: 'COORDENADOR' })

    expect(await screen.findByRole('alert')).toHaveTextContent('Falha na agenda')
    expect(within(await screen.findByRole('row', { name: /CAND-001/ })).getByText('—')).toBeInTheDocument()
  })

  it('indica o carregamento enquanto a lista não chega', async () => {
    listar.mockReturnValue(new Promise(() => {}))
    renderizarAba(<AbaParticipantes />, { perfil: 'COORDENADOR' })

    expect(await screen.findByText('Carregando participantes…')).toBeInTheDocument()
  })

  it('mostra a marca Retirado na lista e substitui Agendamento por Ver histórico (T104)', async () => {
    const retirado: ParticipacaoResponse = { ...segundo, situacao: 'RETIRADO' }
    listar.mockResolvedValue([primeiro, retirado])
    progresso.mockResolvedValue([
      { participacaoId: 'p-1', candidatoId: 'c-1', codigo: 'CAND-001', nomeFicticio: 'J. S.', dataInclusao: '2026-11-04', visitasPrevistas: 3, visitasAgendadas: 2, situacaoParticipacao: 'ATIVO' },
      { participacaoId: 'p-2', candidatoId: 'c-2', codigo: 'CAND-002', nomeFicticio: 'M. P.', dataInclusao: '2026-11-04', visitasPrevistas: 3, visitasAgendadas: 1, situacaoParticipacao: 'RETIRADO' },
    ])
    renderizarAba(<AbaParticipantes />, { perfil: 'COORDENADOR' })

    const linhaAtivo = await screen.findByRole('row', { name: /CAND-001/ })
    expect(within(linhaAtivo).getByText('Ativo')).toHaveClass('selo--ativo')
    expect(within(linhaAtivo).getByRole('link', { name: 'Agendamento de CAND-001' })).toBeInTheDocument()

    const linhaRetirado = screen.getByRole('row', { name: /CAND-002/ })
    expect(within(linhaRetirado).getByText('Retirado')).toHaveClass('selo--retirado')
    expect(within(linhaRetirado).queryByRole('link', { name: /Agendamento/ })).not.toBeInTheDocument()
    expect(within(linhaRetirado).getByRole('link', { name: 'Ver histórico de CAND-002' })).toHaveAttribute(
      'href',
      `/estudos/${estudoBase.id}/participantes/p-2`,
    )
  })
})
