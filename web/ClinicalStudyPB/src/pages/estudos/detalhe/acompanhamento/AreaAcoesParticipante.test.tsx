import { screen } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { beforeEach, describe, expect, it, vi } from 'vitest'
import { contaParticipanteApi } from '../../../../api/contaParticipante'
import { estudoBase, renderizarAba } from '../../../../test/ambiente'
import { AreaAcoesParticipante } from './AreaAcoesParticipante'
import { historicoBase, historicoRetirado } from './historicoFixture'

vi.mock('../../../../api/contaParticipante', () => ({
  contaParticipanteApi: { buscar: vi.fn(), vincular: vi.fn(), listarDisponiveis: vi.fn() },
}))

const participacao = historicoBase.participante
const participacaoRetirada = historicoRetirado.participante
const contaVinculada = { participacaoId: participacao.participacaoId, usuarioId: 'u-1', nome: 'Maria', email: 'maria@clinicalstudy.test' }

describe('AreaAcoesParticipante (T97 e T104)', () => {
  it('o coordenador vê "Agendar visitas" e "Registrar retirada" habilitados para participante ativo em estudo aberto', () => {
    renderizarAba(<AreaAcoesParticipante participacao={participacao} perfil="COORDENADOR" aoAlterar={vi.fn()} />, {
      perfil: 'COORDENADOR',
      estudo: { ...estudoBase, status: 'EXECUCAO' },
    })

    expect(screen.getByRole('region', { name: 'Ações do participante' })).toBeInTheDocument()
    expect(screen.getByRole('link', { name: 'Agendar visitas' })).toHaveAttribute(
      'href',
      `/estudos/${estudoBase.id}/participantes/${participacao.participacaoId}/agendamento`,
    )
    expect(screen.getByRole('button', { name: 'Registrar retirada' })).toBeEnabled()
  })

  it('o pesquisador só lê: a área não aparece e não há botões de escrita', () => {
    renderizarAba(<AreaAcoesParticipante participacao={participacao} perfil="PESQUISADOR" aoAlterar={vi.fn()} />, {
      perfil: 'PESQUISADOR',
    })

    expect(screen.queryByRole('region', { name: 'Ações do participante' })).not.toBeInTheDocument()
    expect(screen.queryByRole('link')).not.toBeInTheDocument()
    expect(screen.queryByRole('button')).not.toBeInTheDocument()
  })

  it('clicar em "Registrar retirada" abre o diálogo de retirada (T104)', async () => {
    renderizarAba(
      <AreaAcoesParticipante
        participacao={participacao}
        perfil="COORDENADOR"
        visitas={historicoBase.visitas}
        aoAlterar={vi.fn()}
      />,
      { perfil: 'COORDENADOR', estudo: { ...estudoBase, status: 'EXECUCAO' } },
    )

    await userEvent.click(screen.getByRole('button', { name: 'Registrar retirada' }))

    expect(screen.getByRole('dialog', { name: 'Registrar retirada' })).toBeInTheDocument()
  })

  it('com o estudo encerrado o botão "Registrar retirada" fica desabilitado com explicação acessível (T104)', () => {
    renderizarAba(<AreaAcoesParticipante participacao={participacao} perfil="COORDENADOR" aoAlterar={vi.fn()} />, {
      perfil: 'COORDENADOR',
      estudo: { ...estudoBase, status: 'ENCERRADO' },
    })

    const botao = screen.getByRole('button', { name: 'Registrar retirada' })
    expect(botao).toBeDisabled()
    expect(botao).toHaveAccessibleDescription('O estudo está encerrado: não é possível registrar retirada de participantes.')
  })

  it('com o participante já retirado esconde Agendar visitas, Vincular conta e Registrar retirada (T104)', () => {
    renderizarAba(
      <AreaAcoesParticipante participacao={participacaoRetirada} perfil="COORDENADOR" conta={null} aoAlterar={vi.fn()} />,
      { perfil: 'COORDENADOR', estudo: { ...estudoBase, status: 'EXECUCAO' } },
    )

    expect(screen.queryByRole('region', { name: 'Ações do participante' })).not.toBeInTheDocument()
    expect(screen.queryByRole('link', { name: 'Agendar visitas' })).not.toBeInTheDocument()
    expect(screen.queryByRole('button', { name: 'Vincular conta' })).not.toBeInTheDocument()
    expect(screen.queryByRole('button', { name: 'Registrar retirada' })).not.toBeInTheDocument()
  })
})

describe('AreaAcoesParticipante: vincular conta (T111, RQ-T111-09)', () => {
  beforeEach(() => {
    vi.clearAllMocks()
    vi.mocked(contaParticipanteApi.listarDisponiveis).mockResolvedValue([
      { usuarioId: 'u-9', nome: 'Ana Livre', email: 'ana@clinicalstudy.test' },
    ])
    vi.mocked(contaParticipanteApi.vincular).mockResolvedValue({ ...contaVinculada, usuarioId: 'u-9', nome: 'Ana Livre' })
  })

  function area(perfil: 'COORDENADOR' | 'PESQUISADOR', conta: typeof contaVinculada | null | undefined, aoAlterar = vi.fn()) {
    renderizarAba(<AreaAcoesParticipante participacao={participacao} perfil={perfil} conta={conta} aoAlterar={aoAlterar} />, { perfil })
    return aoAlterar
  }

  it('o coordenador vê "Vincular conta" quando o participante não tem conta', () => {
    area('COORDENADOR', null)

    expect(screen.getByRole('button', { name: 'Vincular conta' })).toBeInTheDocument()
    expect(screen.getByRole('link', { name: 'Agendar visitas' })).toBeInTheDocument()
  })

  it('o botão some quando o participante já tem conta', () => {
    area('COORDENADOR', contaVinculada)

    expect(screen.queryByRole('button', { name: 'Vincular conta' })).not.toBeInTheDocument()
  })

  it('o botão não aparece enquanto não se sabe se há conta (carregando ou falha ao consultar)', () => {
    area('COORDENADOR', undefined)

    expect(screen.queryByRole('button', { name: 'Vincular conta' })).not.toBeInTheDocument()
  })

  it('o pesquisador nunca vê o botão, mesmo sem conta (a área nem é desenhada)', () => {
    area('PESQUISADOR', null)

    expect(screen.queryByRole('button', { name: 'Vincular conta' })).not.toBeInTheDocument()
    expect(screen.queryByRole('region', { name: 'Ações do participante' })).not.toBeInTheDocument()
  })

  it('o botão abre o diálogo; vincular confirma na área e pede a recarga da ficha', async () => {
    const aoAlterar = area('COORDENADOR', null)

    await userEvent.click(screen.getByRole('button', { name: 'Vincular conta' }))
    await userEvent.click(await screen.findByRole('radio', { name: /Ana Livre/ }))
    await userEvent.click(screen.getByRole('button', { name: 'Vincular' }))

    expect(await screen.findByRole('status')).toHaveTextContent('Conta de Ana Livre vinculada com sucesso.')
    expect(aoAlterar).toHaveBeenCalledTimes(1)
    expect(screen.queryByRole('dialog')).not.toBeInTheDocument()
  })

  it('cancelar o diálogo não pede recarga nem vincula', async () => {
    const aoAlterar = area('COORDENADOR', null)

    await userEvent.click(screen.getByRole('button', { name: 'Vincular conta' }))
    await userEvent.click(await screen.findByRole('button', { name: 'Cancelar' }))

    expect(screen.queryByRole('dialog')).not.toBeInTheDocument()
    expect(aoAlterar).not.toHaveBeenCalled()
    expect(contaParticipanteApi.vincular).not.toHaveBeenCalled()
  })
})
