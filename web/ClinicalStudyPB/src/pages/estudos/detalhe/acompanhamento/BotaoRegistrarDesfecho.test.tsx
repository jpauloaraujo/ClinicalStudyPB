import { screen } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { beforeEach, describe, expect, it, vi } from 'vitest'
import { visitasAgendadasApi } from '../../../../api/visitasAgendadas'
import { renderizarAba } from '../../../../test/ambiente'
import { visitaAgendadaBase } from '../../../../test/visitaAgendada'
import type { StatusEstudo } from '../../../../types/estudo'
import type { PerfilUsuario } from '../../../../types/usuario'
import { BotaoRegistrarDesfecho } from './BotaoRegistrarDesfecho'

vi.mock('../../../../api/visitasAgendadas', () => ({
  visitasAgendadasApi: { buscar: vi.fn(), registrarDesfecho: vi.fn() },
}))

const registrarDesfecho = vi.mocked(visitasAgendadasApi.registrarDesfecho)
const aoRegistrar = vi.fn()

function renderizar(opcoes: {
  perfil?: PerfilUsuario
  statusEstudo?: StatusEstudo
  situacao?: 'AGENDADA' | 'REALIZADA' | 'AUSENTE' | 'CANCELADA'
}) {
  renderizarAba(
    <BotaoRegistrarDesfecho
      visita={{
        id: visitaAgendadaBase.id,
        visitaNome: visitaAgendadaBase.visitaNome,
        participanteNome: visitaAgendadaBase.participanteNome,
        dataHora: visitaAgendadaBase.dataHora,
        situacao: opcoes.situacao ?? 'AGENDADA',
      }}
      statusEstudo={opcoes.statusEstudo ?? 'EXECUCAO'}
      aoRegistrar={aoRegistrar}
    />,
    { perfil: opcoes.perfil ?? 'COORDENADOR' },
  )
}

describe('BotaoRegistrarDesfecho (T83)', () => {
  beforeEach(() => {
    vi.clearAllMocks()
    registrarDesfecho.mockResolvedValue({ ...visitaAgendadaBase, situacao: 'AUSENTE' })
  })

  it.each<StatusEstudo>(['RECRUTAMENTO', 'EXECUCAO'])(
    'o coordenador vê o botão habilitado com a visita agendada e o estudo em %s',
    (statusEstudo) => {
      renderizar({ statusEstudo })

      expect(screen.getByRole('button', { name: 'Registrar desfecho' })).toBeEnabled()
    },
  )

  it.each<StatusEstudo>(['PLANEJAMENTO', 'ENCERRADO'])(
    'com o estudo em %s o botão fica desabilitado e a explicação aparece',
    (statusEstudo) => {
      renderizar({ statusEstudo })

      const botao = screen.getByRole('button', { name: 'Registrar desfecho' })
      expect(botao).toBeDisabled()
      expect(botao).toHaveAccessibleDescription(/recrutamento ou execução/)
    },
  )

  it('o pesquisador não vê o botão', () => {
    renderizar({ perfil: 'PESQUISADOR' })

    expect(screen.queryByRole('button', { name: 'Registrar desfecho' })).not.toBeInTheDocument()
  })

  it.each(['REALIZADA', 'AUSENTE', 'CANCELADA'] as const)(
    'visita %s é somente leitura: nenhum botão de escrita',
    (situacao) => {
      renderizar({ situacao })

      expect(screen.queryByRole('button')).not.toBeInTheDocument()
    },
  )

  it('abre o diálogo, registra e mostra a confirmação, avisando a tela de origem', async () => {
    renderizar({})

    await userEvent.click(screen.getByRole('button', { name: 'Registrar desfecho' }))
    await userEvent.click(screen.getByRole('radio', { name: 'Ausente' }))
    await userEvent.click(screen.getByRole('button', { name: 'Confirmar' }))

    expect(registrarDesfecho).toHaveBeenCalledWith('va-1', { tipo: 'AUSENTE' })
    expect(aoRegistrar).toHaveBeenCalledWith(expect.objectContaining({ situacao: 'AUSENTE' }))
    expect(await screen.findByRole('status')).toHaveTextContent('Desfecho registrado: visita ausente.')
    expect(screen.queryByRole('dialog')).not.toBeInTheDocument()
  })
})
