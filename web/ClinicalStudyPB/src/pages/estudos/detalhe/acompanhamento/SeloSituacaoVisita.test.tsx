import { render, screen } from '@testing-library/react'
import { describe, expect, it } from 'vitest'
import { SeloSituacaoVisita } from './SeloSituacaoVisita'
import type { SituacaoDoSelo } from './situacaoVisita'

describe('SeloSituacaoVisita (T83)', () => {
  it.each<[SituacaoDoSelo, string]>([
    ['AGENDADA', 'Agendada'],
    ['REALIZADA', 'Realizada'],
    ['AUSENTE', 'Ausente'],
    ['CANCELADA', 'Cancelada'],
    ['NAO_AGENDADA', 'Não agendada'],
  ])('mostra o texto da situação %s, para não depender só da cor', (situacao, rotulo) => {
    render(<SeloSituacaoVisita situacao={situacao} />)

    expect(screen.getByText(rotulo)).toBeInTheDocument()
  })

  it('usa uma classe de cor diferente para cada situação', () => {
    const situacoes: SituacaoDoSelo[] = ['AGENDADA', 'REALIZADA', 'AUSENTE', 'CANCELADA', 'NAO_AGENDADA']
    const classes = situacoes.map((situacao) => {
      const { container, unmount } = render(<SeloSituacaoVisita situacao={situacao} />)
      const classe = container.firstElementChild!.className
      unmount()
      return classe
    })

    expect(new Set(classes).size).toBe(5)
    classes.forEach((classe) => expect(classe).toContain('selo'))
  })
})
