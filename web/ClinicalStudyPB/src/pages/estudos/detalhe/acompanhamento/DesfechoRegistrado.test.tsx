import { render, screen, within } from '@testing-library/react'
import { describe, expect, it } from 'vitest'
import { visitaAgendadaBase } from '../../../../test/visitaAgendada'
import { DesfechoRegistrado } from './DesfechoRegistrado'

describe('DesfechoRegistrado (T83)', () => {
  it('visita agendada não mostra nada: ela ainda não tem desfecho', () => {
    const { container } = render(<DesfechoRegistrado visita={visitaAgendadaBase} />)

    expect(container).toBeEmptyDOMElement()
  })

  it('realizada mostra a situação, a data real, a observação e quem registrou, sem botões', () => {
    render(
      <DesfechoRegistrado
        visita={{
          ...visitaAgendadaBase,
          situacao: 'REALIZADA',
          dataHoraRealizada: '2026-11-16T09:40:00-03:00',
          observacao: 'Chegou no horário',
          alteradoPor: 'Coordenadora Fictícia',
        }}
      />,
    )

    const regiao = screen.getByRole('region', { name: 'Desfecho registrado' })
    expect(within(regiao).getByText('Realizada', { selector: '.selo' })).toBeInTheDocument()
    expect(within(regiao).getByText('Realizada em')).toBeInTheDocument()
    expect(within(regiao).getByText('Chegou no horário')).toBeInTheDocument()
    expect(within(regiao).getByText(/Coordenadora Fictícia/)).toBeInTheDocument()
    expect(within(regiao).queryByRole('button')).not.toBeInTheDocument()
  })

  it('cancelada mostra o motivo e não mostra data real', () => {
    render(
      <DesfechoRegistrado
        visita={{ ...visitaAgendadaBase, situacao: 'CANCELADA', motivo: 'Participante em viagem' }}
      />,
    )

    expect(screen.getByText('Motivo do cancelamento')).toBeInTheDocument()
    expect(screen.getByText('Participante em viagem')).toBeInTheDocument()
    expect(screen.queryByText('Realizada em')).not.toBeInTheDocument()
  })

  it('ausente mostra a observação e não mostra motivo nem data real', () => {
    render(
      <DesfechoRegistrado visita={{ ...visitaAgendadaBase, situacao: 'AUSENTE', observacao: 'Não atendeu' }} />,
    )

    expect(screen.getByText('Não atendeu')).toBeInTheDocument()
    expect(screen.queryByText('Motivo do cancelamento')).not.toBeInTheDocument()
    expect(screen.queryByText('Realizada em')).not.toBeInTheDocument()
  })
})
