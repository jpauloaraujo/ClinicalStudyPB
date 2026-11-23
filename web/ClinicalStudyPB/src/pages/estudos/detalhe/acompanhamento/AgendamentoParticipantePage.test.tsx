import { screen, within } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { beforeEach, describe, expect, it, vi } from 'vitest'
import { agendamentoApi } from '../../../../api/agendamento'
import { erroHttp, renderizarAba } from '../../../../test/ambiente'
import type { PerfilUsuario } from '../../../../types/usuario'
import type { Estudo } from '../../../../types/estudo'
import { AgendamentoParticipantePage } from './AgendamentoParticipantePage'
import {
  PARTICIPACAO_ID,
  agendamentoDe,
  estudoEmPlanejamento,
  estudoEmRecrutamento,
  participanteDaAgenda,
  retornoAgendado,
  retornoSemAgendamento,
  visitasDoParticipante,
} from './agendamentoFixture'

vi.mock('../../../../api/agendamento', () => ({
  agendamentoApi: {
    visitasDoParticipante: vi.fn(),
    agendar: vi.fn(),
    reagendar: vi.fn(),
    agenda: vi.fn(),
    participantesDaAgenda: vi.fn(),
  },
}))

const visitasDoParticipanteApi = vi.mocked(agendamentoApi.visitasDoParticipante)
const participantesDaAgenda = vi.mocked(agendamentoApi.participantesDaAgenda)
const agendar = vi.mocked(agendamentoApi.agendar)
const reagendar = vi.mocked(agendamentoApi.reagendar)

function renderizar(perfil: PerfilUsuario = 'COORDENADOR', estudo: Estudo = estudoEmRecrutamento) {
  renderizarAba(<AgendamentoParticipantePage />, {
    perfil,
    estudo,
    rota: 'participantes/:participacaoId/agendamento',
    caminho: `/participantes/${PARTICIPACAO_ID}/agendamento`,
  })
}

const linha = (nome: string) => screen.findByRole('row', { name: new RegExp(nome) })

describe('AgendamentoParticipantePage (T77)', () => {
  beforeEach(() => {
    vi.clearAllMocks()
    visitasDoParticipanteApi.mockResolvedValue(visitasDoParticipante)
    participantesDaAgenda.mockResolvedValue([participanteDaAgenda])
  })

  it('lista todas as visitas previstas com janela, data sugerida e a situação em texto', async () => {
    renderizar()

    const regiao = await screen.findByRole('region', { name: 'Visitas do participante' })
    expect(visitasDoParticipanteApi).toHaveBeenCalledWith(PARTICIPACAO_ID)
    expect(within(regiao).getAllByRole('row')).toHaveLength(4)

    const triagem = await linha('Triagem')
    expect(within(triagem).getByText('Dia 0')).toBeInTheDocument()
    expect(within(triagem).getByText('± 3 dias')).toBeInTheDocument()
    expect(within(triagem).getByText('23/10/2026 a 29/10/2026')).toBeInTheDocument()
    expect(within(triagem).getByText('26/10/2026', { selector: 'td' })).toBeInTheDocument()
    expect(within(triagem).getByText('Realizada')).toBeInTheDocument()

    const retorno30 = await linha('Retorno 30 dias')
    expect(within(retorno30).getByText('Agendada')).toBeInTheDocument()
    expect(within(retorno30).getByText('25/11/2026 09:30')).toBeInTheDocument()

    const retorno60 = await linha('Retorno 60 dias')
    expect(within(retorno60).getByText('Não agendada')).toBeInTheDocument()
    expect(within(retorno60).getByText('18/12/2026 a 01/01/2027')).toBeInTheDocument()
    expect(screen.getByText(/CAND-001 · Pessoa Teta \(Ficticia\) · 2 de 3 visitas agendadas/)).toBeInTheDocument()
  })

  it('o coordenador agenda com a data sugerida e a hora padrão já preenchidas e a janela como limite do campo', async () => {
    agendar.mockResolvedValue(agendamentoDe({ id: 'va-9', dataHora: '2026-12-25T09:00:00-03:00' }))
    renderizar()

    await userEvent.click(await screen.findByRole('button', { name: 'Agendar Retorno 60 dias' }))

    const formulario = screen.getByRole('form', { name: 'Agendar Retorno 60 dias' })
    const data = within(formulario).getByLabelText(/^Data/)
    expect(data).toHaveValue('2026-12-25')
    expect(data).toHaveAttribute('min', '2026-12-18')
    expect(data).toHaveAttribute('max', '2027-01-01')
    expect(within(formulario).getByLabelText('Hora')).toHaveValue('09:00')
    expect(screen.getByText(/Janela válida: 18\/12\/2026 a 01\/01\/2027/)).toBeInTheDocument()

    await userEvent.click(within(formulario).getByRole('button', { name: 'Salvar' }))

    expect(agendar).toHaveBeenCalledWith(PARTICIPACAO_ID, {
      visitaPlanejadaId: retornoSemAgendamento.visitaPlanejadaId,
      dataHora: '2026-12-25T09:00',
    })
    expect(await screen.findByText('Retorno 60 dias agendada para 25/12/2026 09:00.')).toBeInTheDocument()
    // A lista e o progresso são recarregados depois de salvar
    expect(visitasDoParticipanteApi).toHaveBeenCalledTimes(2)
    expect(participantesDaAgenda).toHaveBeenCalledTimes(2)
    expect(screen.queryByRole('form', { name: 'Agendar Retorno 60 dias' })).not.toBeInTheDocument()
  })

  it('o coordenador reagenda a visita agendada com a data atual já preenchida', async () => {
    reagendar.mockResolvedValue(agendamentoDe({ dataHora: '2026-11-27T10:15:00-03:00' }))
    renderizar()

    await userEvent.click(await screen.findByRole('button', { name: 'Reagendar Retorno 30 dias' }))

    const formulario = screen.getByRole('form', { name: 'Reagendar Retorno 30 dias' })
    expect(within(formulario).getByLabelText(/^Data/)).toHaveValue('2026-11-25')
    expect(within(formulario).getByLabelText('Hora')).toHaveValue('09:30')

    await userEvent.clear(within(formulario).getByLabelText('Hora'))
    await userEvent.type(within(formulario).getByLabelText('Hora'), '10:15')
    await userEvent.clear(within(formulario).getByLabelText(/^Data/))
    await userEvent.type(within(formulario).getByLabelText(/^Data/), '2026-11-27')
    await userEvent.click(within(formulario).getByRole('button', { name: 'Salvar' }))

    expect(reagendar).toHaveBeenCalledWith('va-1', { dataHora: '2026-11-27T10:15' })
    expect(await screen.findByText('Retorno 30 dias reagendada para 27/11/2026 10:15.')).toBeInTheDocument()
  })

  it('DATA_FORA_DA_JANELA mostra a janela válida junto do campo de data e põe o foco nele', async () => {
    agendar.mockRejectedValue(erroHttp(409, 'Data fora da janela de 18/12/2026 a 01/01/2027', 'DATA_FORA_DA_JANELA'))
    renderizar()
    await userEvent.click(await screen.findByRole('button', { name: 'Agendar Retorno 60 dias' }))

    await userEvent.click(screen.getByRole('button', { name: 'Salvar' }))

    expect(
      await screen.findByText(/fora da janela válida desta visita \(18\/12\/2026 a 01\/01\/2027\)/),
    ).toBeInTheDocument()
    expect(screen.getByLabelText(/^Data/)).toHaveFocus()
    // O botão Salvar volta a ficar disponível para uma nova tentativa
    expect(screen.getByRole('button', { name: 'Salvar' })).toBeEnabled()
  })

  it.each([
    [
      'ESTUDO_FORA_DE_RECRUTAMENTO_OU_EXECUCAO',
      409,
      'O estudo não está em recrutamento nem em execução: não é possível agendar nem reagendar visitas.',
    ],
    ['VISITA_JA_AGENDADA', 409, 'Esta visita já está agendada para o participante. Use Reagendar para mudar a data.'],
    ['PARTICIPANTE_RETIRADO', 409, 'O participante não está ativo no estudo: não é possível agendar visitas.'],
  ])('traduz o código %s no alerta do formulário', async (codigo, status, texto) => {
    agendar.mockRejectedValue(erroHttp(status, 'mensagem do servidor', codigo))
    renderizar()
    await userEvent.click(await screen.findByRole('button', { name: 'Agendar Retorno 60 dias' }))

    await userEvent.click(screen.getByRole('button', { name: 'Salvar' }))

    expect(await screen.findByRole('alert')).toHaveTextContent(texto)
  })

  it('DATA_NO_PASSADO aparece junto do campo de data', async () => {
    agendar.mockRejectedValue(erroHttp(400, 'mensagem do servidor', 'DATA_NO_PASSADO'))
    renderizar()
    await userEvent.click(await screen.findByRole('button', { name: 'Agendar Retorno 60 dias' }))

    await userEvent.click(screen.getByRole('button', { name: 'Salvar' }))

    expect(await screen.findByText('A data da visita não pode ser anterior a hoje.')).toBeInTheDocument()
    expect(screen.getByLabelText(/^Data/)).toHaveFocus()
  })

  it('VISITA_NAO_ESTA_AGENDADA é traduzido ao reagendar', async () => {
    reagendar.mockRejectedValue(erroHttp(409, 'mensagem do servidor', 'VISITA_NAO_ESTA_AGENDADA'))
    renderizar()
    await userEvent.click(await screen.findByRole('button', { name: 'Reagendar Retorno 30 dias' }))

    await userEvent.click(screen.getByRole('button', { name: 'Salvar' }))

    expect(await screen.findByRole('alert')).toHaveTextContent('Só é possível reagendar uma visita que ainda está agendada.')
  })

  it('para um código desconhecido mostra a mensagem do servidor', async () => {
    agendar.mockRejectedValue(erroHttp(409, 'Regra nova do servidor', 'CODIGO_QUE_A_TELA_NAO_CONHECE'))
    renderizar()
    await userEvent.click(await screen.findByRole('button', { name: 'Agendar Retorno 60 dias' }))

    await userEvent.click(screen.getByRole('button', { name: 'Salvar' }))

    expect(await screen.findByRole('alert')).toHaveTextContent('Regra nova do servidor')
  })

  it('sem data o formulário avisa, põe o foco no campo e não chama a API', async () => {
    renderizar()
    await userEvent.click(await screen.findByRole('button', { name: 'Agendar Retorno 60 dias' }))

    await userEvent.clear(screen.getByLabelText(/^Data/))
    await userEvent.click(screen.getByRole('button', { name: 'Salvar' }))

    expect(await screen.findByText('A data é obrigatória')).toBeInTheDocument()
    expect(screen.getByLabelText(/^Data/)).toHaveFocus()
    expect(agendar).not.toHaveBeenCalled()
  })

  it('desabilita o botão enquanto envia', async () => {
    agendar.mockReturnValue(new Promise(() => {}))
    renderizar()
    await userEvent.click(await screen.findByRole('button', { name: 'Agendar Retorno 60 dias' }))

    await userEvent.click(screen.getByRole('button', { name: 'Salvar' }))

    expect(await screen.findByRole('button', { name: 'Salvando…' })).toBeDisabled()
  })

  it('o pesquisador vê tudo em leitura, sem botões de agendar nem reagendar', async () => {
    renderizar('PESQUISADOR')

    expect(await linha('Retorno 30 dias')).toBeInTheDocument()
    expect(screen.queryByRole('button', { name: /Agendar|Reagendar/ })).not.toBeInTheDocument()
    expect(screen.getByText('Seu perfil tem acesso apenas para consulta do agendamento.')).toBeInTheDocument()
    expect(screen.getByRole('link', { name: 'Abrir a visita Retorno 30 dias' })).toHaveAttribute(
      'href',
      `/estudos/${estudoEmRecrutamento.id}/participantes/${PARTICIPACAO_ID}/visitas/va-1`,
    )
  })

  it('visita com desfecho aparece sem botões de escrita e com a data real', async () => {
    renderizar()

    const triagem = await linha('Triagem')
    expect(within(triagem).getByText('Realizada em 26/10/2026 09:10')).toBeInTheDocument()
    expect(within(triagem).queryByRole('button')).not.toBeInTheDocument()
    expect(within(triagem).getByRole('link', { name: 'Abrir a visita Triagem' })).toBeInTheDocument()
  })

  it('com o estudo fora de recrutamento e execução desabilita os botões e explica', async () => {
    renderizar('COORDENADOR', estudoEmPlanejamento)

    const agendarVisita = await screen.findByRole('button', { name: 'Agendar Retorno 60 dias' })
    expect(agendarVisita).toBeDisabled()
    expect(screen.getByRole('button', { name: 'Reagendar Retorno 30 dias' })).toBeDisabled()
    expect(screen.getByRole('status')).toHaveTextContent('O estudo não está em recrutamento nem em execução')
  })

  it('indica o carregamento enquanto as visitas não chegam', async () => {
    visitasDoParticipanteApi.mockReturnValue(new Promise(() => {}))
    renderizar()

    expect(await screen.findByText('Carregando visitas…')).toBeInTheDocument()
  })

  it('mostra o erro do servidor e permite tentar de novo', async () => {
    visitasDoParticipanteApi.mockRejectedValueOnce(erroHttp(500, 'Erro interno do servidor'))
    renderizar()

    expect(await screen.findByRole('alert')).toHaveTextContent('Erro interno do servidor')
    visitasDoParticipanteApi.mockResolvedValue([retornoAgendado])
    await userEvent.click(screen.getByRole('button', { name: 'Tentar de novo' }))

    expect(await linha('Retorno 30 dias')).toBeInTheDocument()
    expect(screen.queryByRole('alert')).not.toBeInTheDocument()
  })

  it('estudo sem visitas previstas mostra um texto explicativo', async () => {
    visitasDoParticipanteApi.mockResolvedValue([])
    renderizar()

    expect(await screen.findByText(/Este estudo ainda não tem visitas previstas/)).toBeInTheDocument()
    expect(screen.queryByRole('table')).not.toBeInTheDocument()
  })
})
