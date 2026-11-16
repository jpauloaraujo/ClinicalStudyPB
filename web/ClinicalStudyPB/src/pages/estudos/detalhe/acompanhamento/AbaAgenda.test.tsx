import { screen, within } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { beforeEach, describe, expect, it, vi } from 'vitest'
import { agendamentoApi } from '../../../../api/agendamento'
import type { AgendaItemResponse, PaginaAgenda } from '../../../../api/tipos'
import { erroHttp, renderizarAba } from '../../../../test/ambiente'
import type { PerfilUsuario } from '../../../../types/usuario'
import { AbaAgenda } from './AbaAgenda'
import { estudoEmRecrutamento, itemDaAgenda } from './agendamentoFixture'

vi.mock('../../../../api/agendamento', () => ({
  agendamentoApi: {
    visitasDoParticipante: vi.fn(),
    agendar: vi.fn(),
    reagendar: vi.fn(),
    agenda: vi.fn(),
    participantesDaAgenda: vi.fn(),
  },
}))

const agenda = vi.mocked(agendamentoApi.agenda)

function pagina(conteudo: AgendaItemResponse[], extra: Partial<PaginaAgenda> = {}): PaginaAgenda {
  return { conteudo, pagina: 0, tamanho: 20, totalElementos: conteudo.length, totalPaginas: conteudo.length ? 1 : 0, ...extra }
}

const primeiro = itemDaAgenda()
const segundo = itemDaAgenda({
  visitaAgendadaId: 'va-2',
  participanteCodigo: 'CAND-002',
  participanteNome: 'Pessoa Iota (Ficticia)',
  visitaNome: 'Triagem',
  dataHora: '2026-11-26T15:00:00-03:00',
  situacao: 'REALIZADA',
})

function renderizar(perfil: PerfilUsuario = 'COORDENADOR') {
  renderizarAba(<AbaAgenda />, { perfil, estudo: estudoEmRecrutamento })
}

describe('AbaAgenda (T77)', () => {
  beforeEach(() => {
    vi.clearAllMocks()
    agenda.mockResolvedValue(pagina([primeiro, segundo]))
  })

  it('lista a agenda do estudo com data e hora, participante, visita e situação', async () => {
    renderizar()

    const regiao = await screen.findByRole('region', { name: 'Agenda do estudo' })
    expect(agenda).toHaveBeenCalledWith(estudoEmRecrutamento.id, {
      de: undefined, ate: undefined, situacao: undefined, pagina: 0, tamanho: 20,
    })
    const linha1 = within(regiao).getByRole('row', { name: /CAND-001/ })
    expect(within(linha1).getByText('25/11/2026 09:30')).toBeInTheDocument()
    expect(within(linha1).getByText('CAND-001 · Pessoa Teta (Ficticia)')).toBeInTheDocument()
    expect(within(linha1).getByText('Retorno 30 dias')).toBeInTheDocument()
    expect(within(linha1).getByText('Agendada')).toBeInTheDocument()
    expect(within(regiao).getByRole('row', { name: /CAND-002/ })).toBeInTheDocument()
    expect(screen.getByText('Página 1 de 1 · 2 visitas')).toBeInTheDocument()
  })

  it('cada linha tem o link para a tela da visita', async () => {
    renderizar()

    const link = await screen.findByRole('link', { name: 'Abrir a visita Retorno 30 dias de CAND-001' })
    expect(link).toHaveAttribute(
      'href',
      `/estudos/${estudoEmRecrutamento.id}/participantes/${primeiro.participacaoId}/visitas/va-1`,
    )
  })

  it('o coordenador vê Reagendar só nas visitas agendadas (ponto de encaixe AcoesDaVisita)', async () => {
    renderizar()

    const agendada = await screen.findByRole('row', { name: /CAND-001/ })
    expect(within(agendada).getByRole('button', { name: 'Reagendar Retorno 30 dias' })).toBeInTheDocument()
    const realizada = screen.getByRole('row', { name: /CAND-002/ })
    expect(within(realizada).queryByRole('button')).not.toBeInTheDocument()
  })

  it('o pesquisador vê a agenda em leitura, sem botões de escrita', async () => {
    renderizar('PESQUISADOR')

    expect(await screen.findByRole('row', { name: /CAND-001/ })).toBeInTheDocument()
    expect(screen.queryByRole('button', { name: /Reagendar/ })).not.toBeInTheDocument()
  })

  it('filtra por período e situação, e limpar os filtros restaura a lista', async () => {
    renderizar()
    await screen.findByRole('row', { name: /CAND-001/ })

    await userEvent.type(screen.getByLabelText('De'), '2026-11-25')
    await userEvent.type(screen.getByLabelText('Até'), '2026-11-30')
    await userEvent.selectOptions(screen.getByLabelText('Situação'), 'AGENDADA')

    expect(agenda).toHaveBeenLastCalledWith(estudoEmRecrutamento.id, {
      de: '2026-11-25', ate: '2026-11-30', situacao: 'AGENDADA', pagina: 0, tamanho: 20,
    })

    await userEvent.click(screen.getByRole('button', { name: 'Limpar filtros' }))

    expect(screen.getByLabelText('De')).toHaveValue('')
    expect(screen.getByLabelText('Até')).toHaveValue('')
    expect(screen.getByLabelText('Situação')).toHaveValue('')
    expect(agenda).toHaveBeenLastCalledWith(estudoEmRecrutamento.id, {
      de: undefined, ate: undefined, situacao: undefined, pagina: 0, tamanho: 20,
    })
  })

  it('período invertido avisa e não chama o servidor', async () => {
    renderizar()
    await screen.findByRole('row', { name: /CAND-001/ })
    const chamadas = agenda.mock.calls.length

    await userEvent.type(screen.getByLabelText('De'), '2026-11-30')
    await userEvent.type(screen.getByLabelText('Até'), '2026-11-01')

    expect(await screen.findByRole('alert')).toHaveTextContent('A data inicial não pode ser posterior à data final.')
    // Só as chamadas feitas antes de o período ficar inválido (De sozinho é válido)
    expect(agenda.mock.calls.length).toBe(chamadas + 1)
  })

  it('pagina: Próxima e Anterior pedem a página certa', async () => {
    agenda.mockResolvedValue(pagina([primeiro], { totalElementos: 25, totalPaginas: 2 }))
    renderizar()

    expect(await screen.findByText('Página 1 de 2 · 25 visitas')).toBeInTheDocument()
    expect(screen.getByRole('button', { name: 'Anterior' })).toBeDisabled()

    agenda.mockResolvedValue(pagina([segundo], { pagina: 1, totalElementos: 25, totalPaginas: 2 }))
    await userEvent.click(screen.getByRole('button', { name: 'Próxima' }))

    expect(await screen.findByText('Página 2 de 2 · 25 visitas')).toBeInTheDocument()
    expect(agenda).toHaveBeenLastCalledWith(estudoEmRecrutamento.id, expect.objectContaining({ pagina: 1, tamanho: 20 }))
    expect(screen.getByRole('button', { name: 'Próxima' })).toBeDisabled()
  })

  it('lista vazia sem filtros e com filtros mostram textos claros', async () => {
    agenda.mockResolvedValue(pagina([]))
    renderizar()

    expect(await screen.findByText('Nenhuma visita agendada neste estudo ainda.')).toBeInTheDocument()
    expect(screen.queryByRole('table')).not.toBeInTheDocument()

    await userEvent.selectOptions(screen.getByLabelText('Situação'), 'CANCELADA')

    expect(await screen.findByText('Nenhuma visita no período.')).toBeInTheDocument()
  })

  it('mostra o erro do servidor e permite tentar de novo', async () => {
    agenda.mockRejectedValueOnce(erroHttp(500, 'Erro interno do servidor'))
    renderizar()

    expect(await screen.findByRole('alert')).toHaveTextContent('Erro interno do servidor')
    await userEvent.click(screen.getByRole('button', { name: 'Tentar de novo' }))

    expect(await screen.findByRole('row', { name: /CAND-001/ })).toBeInTheDocument()
  })

  it('indica o carregamento enquanto a agenda não chega', async () => {
    agenda.mockReturnValue(new Promise(() => {}))
    renderizar()

    expect(await screen.findByText('Carregando agenda…')).toBeInTheDocument()
  })
})
