import { fireEvent, render, screen } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { beforeEach, describe, expect, it, vi } from 'vitest'
import { visitasAgendadasApi } from '../../../../api/visitasAgendadas'
import { erroHttp } from '../../../../test/ambiente'
import { visitaAgendadaBase } from '../../../../test/visitaAgendada'
import { DialogoDesfecho } from './DialogoDesfecho'

vi.mock('../../../../api/visitasAgendadas', () => ({
  visitasAgendadasApi: { buscar: vi.fn(), registrarDesfecho: vi.fn() },
}))

const registrarDesfecho = vi.mocked(visitasAgendadasApi.registrarDesfecho)
const aoFechar = vi.fn()
const aoRegistrar = vi.fn()

const visita = {
  id: visitaAgendadaBase.id,
  visitaNome: visitaAgendadaBase.visitaNome,
  participanteNome: visitaAgendadaBase.participanteNome,
  dataHora: visitaAgendadaBase.dataHora,
}

const botaoConfirmar = () => screen.getByRole('button', { name: /Confirmar|Registrando/ })

function renderizar() {
  render(<DialogoDesfecho visita={visita} aoFechar={aoFechar} aoRegistrar={aoRegistrar} />)
}

describe('DialogoDesfecho (T83)', () => {
  beforeEach(() => {
    vi.clearAllMocks()
    registrarDesfecho.mockResolvedValue({ ...visitaAgendadaBase, situacao: 'REALIZADA' })
  })

  it('oferece as três opções, com a realizada marcada, e avisa que o desfecho é final', () => {
    renderizar()

    const dialogo = screen.getByRole('dialog', { name: 'Registrar desfecho da visita' })
    expect(dialogo).toHaveTextContent('não pode ser desfeito')
    expect(screen.getByRole('radio', { name: 'Realizada' })).toBeChecked()
    expect(screen.getByRole('radio', { name: 'Ausente' })).not.toBeChecked()
    expect(screen.getByRole('radio', { name: 'Cancelada' })).not.toBeChecked()
  })

  it('realizada sem editar a data envia só o tipo (o servidor usa o instante atual) e fecha o diálogo', async () => {
    renderizar()

    await userEvent.click(botaoConfirmar())

    expect(registrarDesfecho).toHaveBeenCalledWith('va-1', { tipo: 'REALIZADA' })
    expect(aoRegistrar).toHaveBeenCalledWith(expect.objectContaining({ situacao: 'REALIZADA' }))
    expect(aoFechar).toHaveBeenCalledTimes(1)
  })

  it('realizada com data editada e observação envia os dois', async () => {
    renderizar()
    fireEvent.change(screen.getByLabelText('Data e hora em que a visita foi realizada'), {
      target: { value: '2026-11-10T09:15' },
    })
    await userEvent.type(screen.getByLabelText('Observação (opcional)'), '  Chegou no horário ')

    await userEvent.click(botaoConfirmar())

    expect(registrarDesfecho).toHaveBeenCalledWith('va-1', {
      tipo: 'REALIZADA',
      dataHoraRealizada: '2026-11-10T09:15:00',
      observacao: 'Chegou no horário',
    })
  })

  it('ausente envia a observação e não mostra o campo de data', async () => {
    renderizar()
    await userEvent.click(screen.getByRole('radio', { name: 'Ausente' }))
    expect(screen.queryByLabelText('Data e hora em que a visita foi realizada')).not.toBeInTheDocument()
    await userEvent.type(screen.getByLabelText('Observação (opcional)'), 'Não atendeu as ligações')

    await userEvent.click(botaoConfirmar())

    expect(registrarDesfecho).toHaveBeenCalledWith('va-1', { tipo: 'AUSENTE', observacao: 'Não atendeu as ligações' })
  })

  it('ausente sem observação envia só o tipo', async () => {
    renderizar()
    await userEvent.click(screen.getByRole('radio', { name: 'Ausente' }))

    await userEvent.click(botaoConfirmar())

    expect(registrarDesfecho).toHaveBeenCalledWith('va-1', { tipo: 'AUSENTE' })
  })

  it('cancelada exige motivo: desabilitado com 4 caracteres, habilitado com 5, e o contador acompanha', async () => {
    renderizar()
    await userEvent.click(screen.getByRole('radio', { name: 'Cancelada' }))
    const motivo = screen.getByLabelText('Motivo do cancelamento')

    expect(botaoConfirmar()).toBeDisabled()
    await userEvent.type(motivo, 'abcd')
    expect(botaoConfirmar()).toBeDisabled()
    expect(screen.getByText(/4\/500/)).toBeInTheDocument()

    await userEvent.type(motivo, 'e')
    expect(botaoConfirmar()).toBeEnabled()
    expect(screen.getByText(/5\/500/)).toBeInTheDocument()
  })

  it('motivo só com espaços não habilita o botão', async () => {
    renderizar()
    await userEvent.click(screen.getByRole('radio', { name: 'Cancelada' }))

    await userEvent.type(screen.getByLabelText('Motivo do cancelamento'), '        ')

    expect(botaoConfirmar()).toBeDisabled()
  })

  it('cancelada envia o motivo aparado, sem data nem observação', async () => {
    renderizar()
    await userEvent.click(screen.getByRole('radio', { name: 'Cancelada' }))
    await userEvent.type(screen.getByLabelText('Motivo do cancelamento'), '  Participante em viagem ')

    await userEvent.click(botaoConfirmar())

    expect(registrarDesfecho).toHaveBeenCalledWith('va-1', { tipo: 'CANCELADA', motivo: 'Participante em viagem' })
  })

  it('limita o motivo a 500 caracteres', async () => {
    renderizar()
    await userEvent.click(screen.getByRole('radio', { name: 'Cancelada' }))

    expect(screen.getByLabelText('Motivo do cancelamento')).toHaveAttribute('maxlength', '500')
  })

  it('bloqueia data e hora futuras sem chamar a API', async () => {
    renderizar()
    fireEvent.change(screen.getByLabelText('Data e hora em que a visita foi realizada'), {
      target: { value: '2999-01-01T10:00' },
    })

    expect(screen.getByText('A data e a hora não podem estar no futuro.')).toBeInTheDocument()
    expect(botaoConfirmar()).toBeDisabled()
    await userEvent.click(botaoConfirmar())
    expect(registrarDesfecho).not.toHaveBeenCalled()
  })

  it('Cancelar fecha sem chamar a API', async () => {
    renderizar()

    await userEvent.click(screen.getByRole('button', { name: 'Cancelar' }))

    expect(aoFechar).toHaveBeenCalledTimes(1)
    expect(registrarDesfecho).not.toHaveBeenCalled()
  })

  it('Esc fecha o diálogo', async () => {
    renderizar()

    await userEvent.keyboard('{Escape}')

    expect(aoFechar).toHaveBeenCalledTimes(1)
  })

  it('não envia duas vezes: durante o envio o botão fica desabilitado', async () => {
    let concluir: (valor: never) => void = () => {}
    registrarDesfecho.mockReturnValue(new Promise((resolver) => (concluir = resolver as typeof concluir)))
    renderizar()

    await userEvent.click(botaoConfirmar())
    await userEvent.click(botaoConfirmar())

    expect(registrarDesfecho).toHaveBeenCalledTimes(1)
    expect(screen.getByRole('button', { name: 'Registrando…' })).toBeDisabled()
    expect(screen.getByRole('button', { name: 'Cancelar' })).toBeDisabled()
    concluir(undefined as never)
  })

  it.each([
    ['TRANSICAO_VISITA_INVALIDA', 'já tem desfecho registrado'],
    ['VISITA_AINDA_NAO_OCORREU', 'só pode ser cancelada'],
    ['VISITA_ALTERADA_CONCORRENTEMENTE', 'Recarregue a página'],
    ['ESTUDO_FORA_DE_RECRUTAMENTO_OU_EXECUCAO', 'não está em recrutamento ou execução'],
  ])('traduz o erro %s e mantém o diálogo aberto', async (codigo, trecho) => {
    registrarDesfecho.mockRejectedValue(erroHttp(409, 'mensagem do servidor', codigo))
    renderizar()

    await userEvent.click(botaoConfirmar())

    expect(await screen.findByRole('alert')).toHaveTextContent(trecho)
    expect(aoRegistrar).not.toHaveBeenCalled()
    expect(aoFechar).not.toHaveBeenCalled()
    expect(botaoConfirmar()).toBeEnabled()
  })

  it('código desconhecido mostra a mensagem do servidor', async () => {
    registrarDesfecho.mockRejectedValue(erroHttp(409, 'Regra nova do servidor', 'CODIGO_NOVO'))
    renderizar()

    await userEvent.click(botaoConfirmar())

    expect(await screen.findByRole('alert')).toHaveTextContent('Regra nova do servidor')
  })

  it('MOTIVO_OBRIGATORIO do servidor aparece junto do campo do motivo', async () => {
    registrarDesfecho.mockRejectedValue(erroHttp(400, 'mensagem do servidor', 'MOTIVO_OBRIGATORIO'))
    renderizar()
    await userEvent.click(screen.getByRole('radio', { name: 'Cancelada' }))
    await userEvent.type(screen.getByLabelText('Motivo do cancelamento'), 'Viagem')

    await userEvent.click(botaoConfirmar())

    expect(await screen.findByText('Informe o motivo do cancelamento, com ao menos 5 caracteres.')).toBeInTheDocument()
    expect(screen.queryByRole('alert')).not.toBeInTheDocument()
  })

  it('DATA_REALIZACAO_FUTURA do servidor aparece junto do campo da data', async () => {
    registrarDesfecho.mockRejectedValue(erroHttp(400, 'mensagem do servidor', 'DATA_REALIZACAO_FUTURA'))
    renderizar()
    fireEvent.change(screen.getByLabelText('Data e hora em que a visita foi realizada'), {
      target: { value: '2026-11-10T09:15' },
    })

    await userEvent.click(botaoConfirmar())

    expect(
      await screen.findByText('A data e a hora em que a visita foi realizada não podem estar no futuro.'),
    ).toBeInTheDocument()
  })
})
