import { screen } from '@testing-library/react'
import { describe, expect, it, vi } from 'vitest'
import { estudoBase, renderizarAba } from '../../../../test/ambiente'
import { AreaAcoesParticipante } from './AreaAcoesParticipante'
import { historicoBase } from './historicoFixture'

const participacao = historicoBase.participante

describe('AreaAcoesParticipante (T97)', () => {
  it('o coordenador vê o link "Agendar visitas" para a tela de agendamento do participante', () => {
    renderizarAba(<AreaAcoesParticipante participacao={participacao} perfil="COORDENADOR" aoAlterar={vi.fn()} />, {
      perfil: 'COORDENADOR',
    })

    expect(screen.getByRole('region', { name: 'Ações do participante' })).toBeInTheDocument()
    expect(screen.getByRole('link', { name: 'Agendar visitas' })).toHaveAttribute(
      'href',
      `/estudos/${estudoBase.id}/participantes/${participacao.participacaoId}/agendamento`,
    )
  })

  it('o pesquisador só lê: a área não aparece e não há botões de escrita', () => {
    renderizarAba(<AreaAcoesParticipante participacao={participacao} perfil="PESQUISADOR" aoAlterar={vi.fn()} />, {
      perfil: 'PESQUISADOR',
    })

    expect(screen.queryByRole('region', { name: 'Ações do participante' })).not.toBeInTheDocument()
    expect(screen.queryByRole('link')).not.toBeInTheDocument()
    expect(screen.queryByRole('button')).not.toBeInTheDocument()
  })

  it('nesta task só existe a ação de agendar: nada de retirada nem de vínculo de conta ainda', () => {
    renderizarAba(<AreaAcoesParticipante participacao={participacao} perfil="COORDENADOR" aoAlterar={vi.fn()} />, {
      perfil: 'COORDENADOR',
    })

    expect(screen.getAllByRole('link')).toHaveLength(1)
    expect(screen.queryByRole('button', { name: /retirada|vincular/i })).not.toBeInTheDocument()
  })
})
