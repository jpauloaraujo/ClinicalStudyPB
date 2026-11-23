import { render, screen, within } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { beforeEach, describe, expect, it, vi } from 'vitest'
import { retiradaApi } from '../../../../api/retirada'
import type { RetiradaResponse, VisitaDoHistorico } from '../../../../api/tipos'
import { erroHttp } from '../../../../test/ambiente'
import { DialogoRetirada, type ParticipanteParaRetirada } from './DialogoRetirada'
import { cancelada, participacaoId, retorno60, triagem } from './historicoFixture'

vi.mock('../../../../api/retirada', () => ({ retiradaApi: { registrar: vi.fn(), buscar: vi.fn() } }))

const registrar = vi.mocked(retiradaApi.registrar)

const participante: ParticipanteParaRetirada = {
  participacaoId,
  codigo: 'CAND-001',
  nomeFicticio: 'Pessoa Teta (Ficticia)',
}

const visitaFutura1: VisitaDoHistorico = {
  visitaPlanejadaId: 'vp-fut-1',
  nome: 'Retorno 30 dias',
  ordem: 2,
  diaPrevisto: 30,
  tolerancia: 5,
  dataSugerida: '2099-11-19',
  situacao: 'AGENDADA',
  visitaAgendadaId: 'va-fut-1',
  dataHoraAgendada: '2099-11-24T09:30:00-03:00',
  dataHoraRealizada: null,
  motivo: null,
  observacao: null,
  resultados: [],
}

const visitaFutura2: VisitaDoHistorico = {
  ...visitaFutura1,
  visitaPlanejadaId: 'vp-fut-2',
  nome: 'Retorno 60 dias',
  ordem: 3,
  diaPrevisto: 60,
  visitaAgendadaId: 'va-fut-2',
  dataHoraAgendada: '2099-12-24T10:00:00-03:00',
}

const visitaVencida: VisitaDoHistorico = {
  ...visitaFutura1,
  visitaPlanejadaId: 'vp-venc-1',
  nome: 'Retorno 15 dias',
  ordem: 2,
  diaPrevisto: 15,
  visitaAgendadaId: 'va-venc-1',
  dataHoraAgendada: '2020-01-10T09:00:00-03:00',
}

const respostaRetirada: RetiradaResponse = {
  id: 'ret-1',
  participacaoId,
  dataRetirada: '2026-11-18',
  motivo: 'Mudança de cidade',
  solicitante: 'PESQUISADOR',
  registradoPor: 'Coordenador Fictício',
  registradoEm: '2026-11-18T14:00:00-03:00',
  visitasCanceladas: 2,
}

function hojeLocal(): string {
  const agora = new Date()
  agora.setMinutes(agora.getMinutes() - agora.getTimezoneOffset())
  return agora.toISOString().slice(0, 10)
}

function abrir(visitas: VisitaDoHistorico[] = [triagem, visitaFutura1, visitaFutura2, retorno60, cancelada]) {
  const aoFechar = vi.fn()
  const aoRetirar = vi.fn()
  render(<DialogoRetirada participante={participante} visitas={visitas} aoFechar={aoFechar} aoRetirar={aoRetirar} />)
  return { aoFechar, aoRetirar }
}

describe('DialogoRetirada (T104)', () => {
  beforeEach(() => {
    vi.clearAllMocks()
    registrar.mockResolvedValue(respostaRetirada)
  })

  it('abre com título "Registrar retirada", foco no motivo, data padrão de hoje e as 3 opções de solicitante', () => {
    abrir()

    const dialogo = screen.getByRole('dialog', { name: 'Registrar retirada' })
    const campoMotivo = within(dialogo).getByLabelText('Motivo da retirada')
    expect(campoMotivo).toHaveFocus()

    const campoData = within(dialogo).getByLabelText('Data da retirada')
    const hoje = hojeLocal()
    expect(campoData).toHaveValue(hoje)
    expect(campoData).toHaveAttribute('max', hoje)

    const opcoes = within(within(dialogo).getByLabelText('Quem solicitou')).getAllByRole('option')
    expect(opcoes.map((opcao) => opcao.textContent)).toEqual(['Participante', 'Pesquisador', 'Coordenação'])
  })

  it('valida motivo curto e data futura no cliente antes de avançar para a confirmação', async () => {
    abrir()

    await userEvent.type(screen.getByLabelText('Motivo da retirada'), '  abc  ')
    await userEvent.click(screen.getByRole('button', { name: 'Continuar' }))

    expect(screen.getByText('Informe o motivo da retirada, com ao menos 5 caracteres.')).toBeInTheDocument()
    expect(screen.getByRole('dialog', { name: 'Registrar retirada' })).toBeInTheDocument()

    const campoMotivo = screen.getByLabelText(/^Motivo da retirada/)
    await userEvent.clear(campoMotivo)
    await userEvent.type(campoMotivo, 'Mudança de cidade')
    expect(screen.getByText('17/500 · mínimo de 5 caracteres')).toBeInTheDocument()

    const campoData = screen.getByLabelText('Data da retirada')
    await userEvent.clear(campoData)
    await userEvent.type(campoData, '2099-01-01')
    await userEvent.click(screen.getByRole('button', { name: 'Continuar' }))

    expect(screen.getByText('A data da retirada não pode estar no futuro.')).toBeInTheDocument()
    expect(screen.getByRole('dialog', { name: 'Registrar retirada' })).toBeInTheDocument()
    expect(registrar).not.toHaveBeenCalled()
  })

  it('mostra na confirmação o aviso de irreversibilidade, a contagem de visitas futuras e o alerta de vencidas', async () => {
    abrir([triagem, visitaFutura1, visitaFutura2, visitaVencida, retorno60])

    await userEvent.type(screen.getByLabelText('Motivo da retirada'), 'Mudança de cidade')
    await userEvent.selectOptions(screen.getByLabelText('Quem solicitou'), 'PESQUISADOR')
    const campoData = screen.getByLabelText('Data da retirada')
    await userEvent.clear(campoData)
    await userEvent.type(campoData, '2026-11-18')
    await userEvent.click(screen.getByRole('button', { name: 'Continuar' }))

    const dialogo = screen.getByRole('dialog', { name: 'Confirmar retirada do participante' })
    expect(dialogo).toHaveTextContent(/irreversível/i)
    expect(dialogo).toHaveTextContent(/RETIRADO/)
    expect(dialogo).toHaveTextContent(/2 visitas futuras agendadas serão canceladas automaticamente/)
    expect(dialogo).toHaveTextContent(/1 visita agendada vencida sem desfecho/)
    expect(dialogo).toHaveTextContent('Pesquisador')
    expect(dialogo).toHaveTextContent('18/11/2026')
    expect(dialogo).toHaveTextContent('Mudança de cidade')

    // Voltar preserva os dados preenchidos
    await userEvent.click(within(dialogo).getByRole('button', { name: 'Voltar' }))
    expect(screen.getByLabelText('Motivo da retirada')).toHaveValue('Mudança de cidade')
    expect(screen.getByLabelText('Quem solicitou')).toHaveValue('PESQUISADOR')
    expect(screen.getByLabelText('Data da retirada')).toHaveValue('2026-11-18')
  })

  it('sem visitas futuras nem vencidas informa que nenhuma visita será cancelada e omite o alerta de vencidas', async () => {
    abrir([triagem, retorno60, cancelada])

    await userEvent.type(screen.getByLabelText('Motivo da retirada'), 'Pedido do participante')
    await userEvent.click(screen.getByRole('button', { name: 'Continuar' }))

    const dialogo = screen.getByRole('dialog', { name: 'Confirmar retirada do participante' })
    expect(dialogo).toHaveTextContent('Nenhuma visita futura agendada será cancelada.')
    expect(dialogo).not.toHaveTextContent(/vencida sem desfecho/)
  })

  it('envia o corpo correto ao confirmar, notifica aoRetirar e fecha o diálogo', async () => {
    const { aoFechar, aoRetirar } = abrir()

    await userEvent.type(screen.getByLabelText('Motivo da retirada'), '  Mudança de cidade  ')
    await userEvent.selectOptions(screen.getByLabelText('Quem solicitou'), 'PESQUISADOR')
    const campoData = screen.getByLabelText('Data da retirada')
    await userEvent.clear(campoData)
    await userEvent.type(campoData, '2026-11-18')
    await userEvent.click(screen.getByRole('button', { name: 'Continuar' }))
    await userEvent.click(screen.getByRole('button', { name: 'Confirmar retirada' }))

    expect(registrar).toHaveBeenCalledTimes(1)
    expect(registrar).toHaveBeenCalledWith(participacaoId, {
      motivo: 'Mudança de cidade',
      solicitante: 'PESQUISADOR',
      dataRetirada: '2026-11-18',
    })
    expect(aoRetirar).toHaveBeenCalledWith(respostaRetirada)
    expect(aoFechar).toHaveBeenCalledTimes(1)
  })

  it('evita envio duplo enquanto a requisição está em andamento', async () => {
    let resolver!: (valor: RetiradaResponse) => void
    registrar.mockImplementation(() => new Promise<RetiradaResponse>((resolve) => { resolver = resolve }))
    const { aoFechar } = abrir()

    await userEvent.type(screen.getByLabelText('Motivo da retirada'), 'Motivo válido')
    await userEvent.click(screen.getByRole('button', { name: 'Continuar' }))

    const botaoConfirmar = screen.getByRole('button', { name: 'Confirmar retirada' })
    await userEvent.click(botaoConfirmar)

    const botaoEnviando = screen.getByRole('button', { name: 'Registrando…' })
    expect(botaoEnviando).toBeDisabled()
    expect(screen.getByRole('button', { name: 'Voltar' })).toBeDisabled()
    await userEvent.keyboard('{Escape}')
    expect(aoFechar).not.toHaveBeenCalled()
    expect(registrar).toHaveBeenCalledTimes(1)

    resolver(respostaRetirada)
    await vi.waitFor(() => expect(aoFechar).toHaveBeenCalledTimes(1))
  })

  it.each([
    ['MOTIVO_OBRIGATORIO', /ao menos 5 caracteres/],
    ['DATA_RETIRADA_FUTURA', /não pode estar no futuro/],
  ])('erro 400 %s volta ao formulário e exibe a mensagem no campo correspondente', async (codigo, mensagem) => {
    registrar.mockRejectedValue(erroHttp(400, 'Erro do servidor', codigo))
    abrir()

    await userEvent.type(screen.getByLabelText('Motivo da retirada'), 'Motivo válido')
    await userEvent.click(screen.getByRole('button', { name: 'Continuar' }))
    await userEvent.click(screen.getByRole('button', { name: 'Confirmar retirada' }))

    expect(await screen.findByRole('dialog', { name: 'Registrar retirada' })).toBeInTheDocument()
    expect(screen.getByText(mensagem)).toBeInTheDocument()
  })

  it('erro 400 VALIDACAO_CAMPOS volta ao formulário com o erro no campo enviado pelo servidor', async () => {
    registrar.mockRejectedValue(erroHttp(400, 'motivo: O motivo deve ter entre 5 e 500 caracteres', 'VALIDACAO_CAMPOS'))
    abrir()

    await userEvent.type(screen.getByLabelText('Motivo da retirada'), 'Motivo válido')
    await userEvent.click(screen.getByRole('button', { name: 'Continuar' }))
    await userEvent.click(screen.getByRole('button', { name: 'Confirmar retirada' }))

    expect(await screen.findByRole('dialog', { name: 'Registrar retirada' })).toBeInTheDocument()
    expect(screen.getByText('O motivo deve ter entre 5 e 500 caracteres')).toBeInTheDocument()
  })

  it.each([
    ['PARTICIPANTE_JA_RETIRADO', 'Este participante já foi retirado do estudo.'],
    ['ESTUDO_ENCERRADO', 'O estudo está encerrado: não é possível registrar retirada de participantes.'],
    ['REGRA_DESCONHECIDA', 'Mensagem original do servidor'],
  ])('erro 409 %s mantém o diálogo aberto e exibe o alerta traduzido', async (codigo, esperado) => {
    registrar.mockRejectedValue(erroHttp(409, 'Mensagem original do servidor', codigo))
    const { aoFechar, aoRetirar } = abrir()

    await userEvent.type(screen.getByLabelText('Motivo da retirada'), 'Motivo válido')
    await userEvent.click(screen.getByRole('button', { name: 'Continuar' }))
    await userEvent.click(screen.getByRole('button', { name: 'Confirmar retirada' }))

    expect(await screen.findByRole('alert')).toHaveTextContent(esperado)
    expect(aoFechar).not.toHaveBeenCalled()
    expect(aoRetirar).not.toHaveBeenCalled()
  })

  it('Cancelar e Escape fecham o diálogo sem chamar a API', async () => {
    const { aoFechar } = abrir()
    await userEvent.click(screen.getByRole('button', { name: 'Cancelar' }))
    expect(aoFechar).toHaveBeenCalledTimes(1)

    await userEvent.keyboard('{Escape}')
    expect(aoFechar).toHaveBeenCalledTimes(2)
    expect(registrar).not.toHaveBeenCalled()
  })
})
