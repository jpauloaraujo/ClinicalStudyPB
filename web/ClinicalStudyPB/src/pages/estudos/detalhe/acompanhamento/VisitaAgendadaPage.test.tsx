import { screen, within } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { beforeEach, describe, expect, it, vi } from 'vitest'
import { agendamentoApi } from '../../../../api/agendamento'
import { visitasAgendadasApi } from '../../../../api/visitasAgendadas'
import { erroHttp, renderizarAba } from '../../../../test/ambiente'
import type { PerfilUsuario } from '../../../../types/usuario'
import { VisitaAgendadaPage } from './VisitaAgendadaPage'
import {
  PARTICIPACAO_ID,
  agendamentoDe,
  estudoEmPlanejamento,
  estudoEmRecrutamento,
  participanteDaAgenda,
  participanteRetiradoDaAgenda,
  visitasDoParticipante,
} from './agendamentoFixture'
import type { Estudo } from '../../../../types/estudo'

vi.mock('../../../../api/agendamento', () => ({
  agendamentoApi: {
    visitasDoParticipante: vi.fn(),
    agendar: vi.fn(),
    reagendar: vi.fn(),
    agenda: vi.fn(),
    participantesDaAgenda: vi.fn(),
  },
}))

vi.mock('../../../../api/visitasAgendadas', () => ({
  visitasAgendadasApi: {
    buscar: vi.fn(),
    registrarDesfecho: vi.fn(),
    resultados: vi.fn(),
    registrarResultado: vi.fn(),
  },
}))

const visitasApi = vi.mocked(agendamentoApi.visitasDoParticipante)
const participantesDaAgenda = vi.mocked(agendamentoApi.participantesDaAgenda)
const resultadosApi = vi.mocked(visitasAgendadasApi.resultados)
const reagendar = vi.mocked(agendamentoApi.reagendar)

function renderizar(visitaAgendadaId: string, perfil: PerfilUsuario = 'COORDENADOR', estudo: Estudo = estudoEmRecrutamento) {
  renderizarAba(<VisitaAgendadaPage />, {
    perfil,
    estudo,
    rota: 'participantes/:participacaoId/visitas/:visitaAgendadaId',
    caminho: `/participantes/${PARTICIPACAO_ID}/visitas/${visitaAgendadaId}`,
  })
}

describe('VisitaAgendadaPage (T77, T83, T91 e T104)', () => {
  beforeEach(() => {
    vi.clearAllMocks()
    visitasApi.mockResolvedValue(visitasDoParticipante)
    participantesDaAgenda.mockResolvedValue([participanteDaAgenda])
  })

  it('mostra os dados da visita: participante, visita prevista, data e hora, situação, janela e auditoria', async () => {
    renderizar('va-1')

    const dados = await screen.findByRole('region', { name: 'Dados da visita' })
    expect(visitasApi).toHaveBeenCalledWith(PARTICIPACAO_ID)
    expect(within(dados).getByText('CAND-001 · Pessoa Teta (Ficticia)')).toBeInTheDocument()
    expect(within(dados).getByText('Retorno 30 dias (dia 30, tolerância de 5 dias)')).toBeInTheDocument()
    expect(within(dados).getByText('25/11/2026 09:30')).toBeInTheDocument()
    expect(within(dados).getByText('Agendada')).toBeInTheDocument()
    expect(within(dados).getByText('20/11/2026 a 30/11/2026')).toBeInTheDocument()
    expect(within(dados).getAllByText(/Coordenador Demo \(Ficticio\) em 11\/11\/2026 10:00/)).toHaveLength(2)
  })

  it('mostra as abas Dados e Avaliações, definidas em abasVisita.ts, com Dados aberta', async () => {
    renderizar('va-1')

    const abas = await screen.findByRole('navigation', { name: 'Seções da visita' })
    const botoes = within(abas).getAllByRole('button')
    expect(botoes.map((b) => b.textContent)).toEqual(['Dados', 'Avaliações'])
    expect(botoes[0]).toHaveAttribute('aria-current', 'page')
  })

  it.each<PerfilUsuario>(['PESQUISADOR', 'COORDENADOR'])(
    'a aba Avaliações (T91) aparece para o %s e mostra os resultados da visita',
    async (perfil) => {
      resultadosApi.mockResolvedValue({
        visitaAgendadaId: 'va-0',
        situacaoVisita: 'REALIZADA',
        bloqueio: null,
        total: 1,
        registrados: 0,
        pendentes: 1,
        atividades: [{ acaoId: 'acao-1', nome: 'Aferir pressão arterial', descricao: null, resultado: null }],
      })
      renderizar('va-0', perfil)

      const abas = await screen.findByRole('navigation', { name: 'Seções da visita' })
      await userEvent.click(within(abas).getByRole('button', { name: 'Avaliações' }))

      expect(within(abas).getByRole('button', { name: 'Avaliações' })).toHaveAttribute('aria-current', 'page')
      expect(await screen.findByRole('region', { name: 'Avaliações da visita' })).toBeInTheDocument()
      expect(resultadosApi).toHaveBeenCalledWith('va-0')
      expect(screen.getByText(/0 de 1/)).toHaveTextContent('0 de 1 avaliações registradas')
      expect(screen.queryByRole('region', { name: 'Dados da visita' })).not.toBeInTheDocument()
    },
  )

  it('o coordenador tem Reagendar (ponto de encaixe AcoesDaVisita) e reagenda com a janela como limite', async () => {
    reagendar.mockResolvedValue(agendamentoDe({ dataHora: '2026-11-27T09:30:00-03:00' }))
    renderizar('va-1')

    await userEvent.click(await screen.findByRole('button', { name: 'Reagendar Retorno 30 dias' }))

    const dialogo = screen.getByRole('dialog', { name: 'Reagendar Retorno 30 dias' })
    const data = within(dialogo).getByLabelText('Data')
    expect(data).toHaveValue('2026-11-25')
    expect(data).toHaveAttribute('min', '2026-11-20')
    expect(data).toHaveAttribute('max', '2026-11-30')

    await userEvent.clear(data)
    await userEvent.type(data, '2026-11-27')
    await userEvent.click(within(dialogo).getByRole('button', { name: 'Salvar' }))

    expect(reagendar).toHaveBeenCalledWith('va-1', { dataHora: '2026-11-27T09:30' })
    // Depois de salvar o diálogo fecha e os dados são recarregados
    expect(screen.queryByRole('dialog')).not.toBeInTheDocument()
    expect(visitasApi).toHaveBeenCalledTimes(2)
  })

  it('erro de regra ao reagendar fica no diálogo, com a janela junto do campo', async () => {
    reagendar.mockRejectedValue(erroHttp(409, 'mensagem do servidor', 'DATA_FORA_DA_JANELA'))
    renderizar('va-1')
    await userEvent.click(await screen.findByRole('button', { name: 'Reagendar Retorno 30 dias' }))

    await userEvent.click(screen.getByRole('button', { name: 'Salvar' }))

    expect(await screen.findByText(/fora da janela válida desta visita \(20\/11\/2026 a 30\/11\/2026\)/)).toBeInTheDocument()
    expect(screen.getByRole('dialog')).toBeInTheDocument()
  })

  it('Esc e Cancelar fecham o diálogo sem chamar a API', async () => {
    renderizar('va-1')
    await userEvent.click(await screen.findByRole('button', { name: 'Reagendar Retorno 30 dias' }))

    await userEvent.click(screen.getByRole('button', { name: 'Cancelar' }))

    expect(screen.queryByRole('dialog')).not.toBeInTheDocument()
    expect(reagendar).not.toHaveBeenCalled()
  })

  it('o pesquisador vê os dados sem nenhuma ação', async () => {
    renderizar('va-1', 'PESQUISADOR')

    expect(await screen.findByRole('region', { name: 'Dados da visita' })).toBeInTheDocument()
    expect(screen.queryByRole('button', { name: /Reagendar/ })).not.toBeInTheDocument()
  })

  it('visita realizada mostra o desfecho registrado (T83), em leitura, e não oferece Reagendar nem Registrar desfecho', async () => {
    renderizar('va-0')

    const desfecho = await screen.findByRole('region', { name: 'Desfecho registrado' })
    expect(within(desfecho).getByText('Realizada em')).toBeInTheDocument()
    expect(within(desfecho).getByText('26/10/2026 09:10')).toBeInTheDocument()
    expect(screen.queryByRole('button', { name: /Reagendar/ })).not.toBeInTheDocument()
    expect(screen.queryByRole('button', { name: 'Registrar desfecho' })).not.toBeInTheDocument()
  })

  it('o coordenador tem Registrar desfecho (ponto de encaixe da T83) na visita agendada e o pesquisador não', async () => {
    renderizar('va-1')
    expect(await screen.findByRole('button', { name: 'Registrar desfecho' })).toBeEnabled()
    expect(screen.queryByRole('region', { name: 'Desfecho registrado' })).not.toBeInTheDocument()
  })

  it('o pesquisador não tem Registrar desfecho', async () => {
    renderizar('va-1', 'PESQUISADOR')
    await screen.findByRole('region', { name: 'Dados da visita' })
    expect(screen.queryByRole('button', { name: 'Registrar desfecho' })).not.toBeInTheDocument()
  })

  it('com o estudo fora de recrutamento e execução o Reagendar fica desabilitado', async () => {
    renderizar('va-1', 'COORDENADOR', estudoEmPlanejamento)

    expect(await screen.findByRole('button', { name: 'Reagendar Retorno 30 dias' })).toBeDisabled()
  })

  it('visita que não pertence ao participante mostra que não foi encontrada', async () => {
    renderizar('va-inexistente')

    expect(await screen.findByRole('alert')).toHaveTextContent('Visita agendada não encontrada para este participante.')
  })

  it('mostra o erro do servidor e permite tentar de novo', async () => {
    visitasApi.mockRejectedValueOnce(erroHttp(500, 'Erro interno do servidor'))
    renderizar('va-1')

    expect(await screen.findByRole('alert')).toHaveTextContent('Erro interno do servidor')
    await userEvent.click(screen.getByRole('button', { name: 'Tentar de novo' }))

    expect(await screen.findByRole('region', { name: 'Dados da visita' })).toBeInTheDocument()
  })

  it('indica o carregamento', async () => {
    visitasApi.mockReturnValue(new Promise(() => {}))
    renderizar('va-1')

    expect(await screen.findByText('Carregando visita…')).toBeInTheDocument()
  })

  it('participante retirado mostra selo Retirado e esconde Reagendar e Registrar desfecho (T104)', async () => {
    participantesDaAgenda.mockResolvedValue([participanteRetiradoDaAgenda])
    renderizar('va-1', 'COORDENADOR')

    expect(await screen.findByRole('region', { name: 'Dados da visita' })).toBeInTheDocument()
    expect(await screen.findByText('Retirado')).toHaveClass('selo--retirado')
    expect(screen.getByRole('status')).toHaveTextContent(
      'Participante retirado do estudo: as ações de edição desta visita estão indisponíveis.',
    )
    expect(screen.queryByRole('button', { name: /Reagendar/ })).not.toBeInTheDocument()
    expect(screen.queryByRole('button', { name: 'Registrar desfecho' })).not.toBeInTheDocument()
  })
})
