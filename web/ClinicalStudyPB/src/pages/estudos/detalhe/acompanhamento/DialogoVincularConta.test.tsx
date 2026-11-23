import { render, screen, waitFor } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { beforeEach, describe, expect, it, vi } from 'vitest'
import { contaParticipanteApi } from '../../../../api/contaParticipante'
import { erroHttp } from '../../../../test/ambiente'
import { DialogoVincularConta, MENSAGEM_SEM_CONTAS } from './DialogoVincularConta'

vi.mock('../../../../api/contaParticipante', () => ({
  contaParticipanteApi: { buscar: vi.fn(), vincular: vi.fn(), listarDisponiveis: vi.fn() },
}))

const listarDisponiveis = vi.mocked(contaParticipanteApi.listarDisponiveis)
const vincular = vi.mocked(contaParticipanteApi.vincular)
const aoFechar = vi.fn()
const aoVincular = vi.fn()

const contas = [
  { usuarioId: 'u-1', nome: 'Ana Participante (Ficticia)', email: 'ana@clinicalstudy.test' },
  { usuarioId: 'u-2', nome: 'Bruno Participante (Ficticio)', email: 'bruno@clinicalstudy.test' },
]
const vinculada = { participacaoId: 'p-1', usuarioId: 'u-2', nome: contas[1].nome, email: contas[1].email }

const botaoVincular = () => screen.getByRole('button', { name: /^Vincular$|Vinculando/ })

function renderizar() {
  render(
    <DialogoVincularConta
      participacaoId="p-1"
      participanteNome="CAND-001 · Pessoa Teta (Ficticia)"
      aoFechar={aoFechar}
      aoVincular={aoVincular}
    />,
  )
}

describe('DialogoVincularConta (T111, RQ-T111-09 a 12)', () => {
  beforeEach(() => {
    vi.clearAllMocks()
    listarDisponiveis.mockResolvedValue(contas)
    vincular.mockResolvedValue(vinculada)
  })

  it('indica o carregamento e depois lista as contas com nome e e-mail, sem nenhuma escolhida', async () => {
    renderizar()

    expect(screen.getByText('Carregando contas disponíveis…')).toBeInTheDocument()
    expect(await screen.findByRole('radio', { name: /Ana Participante/ })).not.toBeChecked()
    expect(screen.getByRole('dialog', { name: 'Vincular conta do participante' })).toHaveTextContent('CAND-001')
    expect(screen.getByText('(bruno@clinicalstudy.test)')).toBeInTheDocument()
    expect(botaoVincular()).toBeDisabled()
  })

  it('lista vazia mostra o texto do guia e não deixa vincular', async () => {
    listarDisponiveis.mockResolvedValue([])
    renderizar()

    expect(await screen.findByText(MENSAGEM_SEM_CONTAS)).toBeInTheDocument()
    expect(MENSAGEM_SEM_CONTAS).toBe(
      'Não há contas de participante disponíveis. Peça ao administrador para cadastrar uma.',
    )
    expect(screen.queryByRole('radio')).not.toBeInTheDocument()
    expect(botaoVincular()).toBeDisabled()
  })

  it('erro ao carregar mostra a mensagem e "Tentar de novo", que busca de novo', async () => {
    listarDisponiveis.mockRejectedValueOnce(erroHttp(500, 'Falha no servidor'))
    renderizar()

    expect(await screen.findByRole('alert')).toHaveTextContent('Falha no servidor')
    await userEvent.click(screen.getByRole('button', { name: 'Tentar de novo' }))

    expect(await screen.findByRole('radio', { name: /Ana Participante/ })).toBeInTheDocument()
    expect(listarDisponiveis).toHaveBeenCalledTimes(2)
  })

  it('escolher uma conta habilita "Vincular"; confirmar envia o usuarioId escolhido, avisa e fecha', async () => {
    renderizar()
    await userEvent.click(await screen.findByRole('radio', { name: /Bruno Participante/ }))
    expect(botaoVincular()).toBeEnabled()

    await userEvent.click(botaoVincular())

    await waitFor(() => expect(vincular).toHaveBeenCalledWith('p-1', 'u-2'))
    expect(aoVincular).toHaveBeenCalledWith(vinculada)
    expect(aoFechar).toHaveBeenCalledTimes(1)
  })

  it.each([
    ['CONTA_JA_VINCULADA', /já está vinculada a outro participante/],
    ['CONTA_INATIVA', /está desativada/],
    ['CONTA_NAO_E_PARTICIPANTE', /não é de um participante/],
    ['PARTICIPACAO_JA_TEM_CONTA', /já tem uma conta vinculada/],
    ['PARTICIPANTE_RETIRADO', /foi retirado do estudo/],
  ])('409 %s mostra a mensagem da regra e mantém o diálogo aberto para tentar outra conta', async (codigo, esperado) => {
    vincular.mockRejectedValueOnce(erroHttp(409, 'texto do servidor', codigo))
    renderizar()
    await userEvent.click(await screen.findByRole('radio', { name: /Ana Participante/ }))
    await userEvent.click(botaoVincular())

    expect(await screen.findByRole('alert')).toHaveTextContent(esperado)
    expect(aoVincular).not.toHaveBeenCalled()
    expect(aoFechar).not.toHaveBeenCalled()
    expect(botaoVincular()).toBeEnabled()
  })

  it('código desconhecido mostra a mensagem do servidor', async () => {
    vincular.mockRejectedValueOnce(erroHttp(409, 'Regra nova do servidor', 'REGRA_NOVA'))
    renderizar()
    await userEvent.click(await screen.findByRole('radio', { name: /Ana Participante/ }))
    await userEvent.click(botaoVincular())

    expect(await screen.findByRole('alert')).toHaveTextContent('Regra nova do servidor')
  })

  it('escolher outra conta depois do erro limpa a mensagem', async () => {
    vincular.mockRejectedValueOnce(erroHttp(409, 'x', 'CONTA_INATIVA'))
    renderizar()
    await userEvent.click(await screen.findByRole('radio', { name: /Ana Participante/ }))
    await userEvent.click(botaoVincular())
    await screen.findByRole('alert')

    await userEvent.click(screen.getByRole('radio', { name: /Bruno Participante/ }))

    expect(screen.queryByRole('alert')).not.toBeInTheDocument()
  })

  it('durante o envio mostra "Vinculando…" e não permite enviar de novo nem cancelar', async () => {
    let concluir: (conta: typeof vinculada) => void = () => {}
    vincular.mockReturnValue(new Promise((resolver) => (concluir = resolver)))
    renderizar()
    await userEvent.click(await screen.findByRole('radio', { name: /Ana Participante/ }))
    await userEvent.click(botaoVincular())

    expect(screen.getByRole('button', { name: 'Vinculando…' })).toBeDisabled()
    expect(screen.getByRole('button', { name: 'Cancelar' })).toBeDisabled()
    expect(vincular).toHaveBeenCalledTimes(1)

    concluir(vinculada)
    await waitFor(() => expect(aoFechar).toHaveBeenCalled())
  })

  it('Cancelar e Esc fecham sem vincular', async () => {
    renderizar()
    await screen.findByRole('radio', { name: /Ana Participante/ })

    await userEvent.click(screen.getByRole('button', { name: 'Cancelar' }))
    await userEvent.keyboard('{Escape}')

    expect(aoFechar).toHaveBeenCalledTimes(2)
    expect(vincular).not.toHaveBeenCalled()
  })
})
