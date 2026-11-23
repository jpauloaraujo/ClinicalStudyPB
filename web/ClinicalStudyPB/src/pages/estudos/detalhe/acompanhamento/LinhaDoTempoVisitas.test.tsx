import { render, screen, within } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { MemoryRouter } from 'react-router'
import { describe, expect, it } from 'vitest'
import { cancelada, participacaoId, retorno30, retorno60, triagem } from './historicoFixture'
import { LinhaDoTempoVisitas } from './LinhaDoTempoVisitas'

const estudoId = 'estudo-1'

function montar(visitas = [triagem, retorno30, retorno60, cancelada], participanteRetirado = false) {
  render(
    <MemoryRouter>
      <LinhaDoTempoVisitas
        visitas={visitas}
        estudoId={estudoId}
        participacaoId={participacaoId}
        participanteRetirado={participanteRetirado}
      />
    </MemoryRouter>,
  )
}

const botaoDa = (nome: RegExp) => screen.getByRole('button', { name: nome })

describe('LinhaDoTempoVisitas (T97)', () => {
  it('mostra todas as visitas previstas na ordem recebida, com nome, dia previsto, data sugerida e selo', () => {
    montar()

    const titulos = screen.getAllByRole('heading', { level: 4 })
    expect(titulos.map((titulo) => titulo.textContent)).toEqual([
      expect.stringContaining('Triagem'),
      expect.stringContaining('Retorno 30 dias'),
      expect.stringContaining('Retorno 60 dias'),
      expect.stringContaining('Retorno 90 dias'),
    ])
    expect(titulos[0]).toHaveTextContent('Dia 0 · sugerida em 20/10/2026')
    expect(titulos[0]).toHaveTextContent('Realizada')
    expect(titulos[1]).toHaveTextContent('Agendada: 24/11/2026 09:30')
    expect(titulos[2]).toHaveTextContent('Não agendada')
    expect(titulos[3]).toHaveTextContent('Cancelada')
  })

  it('mostra a data agendada e a data realizada no cabeçalho quando existem', () => {
    montar()

    expect(botaoDa(/Triagem/)).toHaveTextContent('Agendada: 20/10/2026 09:00')
    expect(botaoDa(/Triagem/)).toHaveTextContent('Realizada: 20/10/2026 09:10')
    expect(botaoDa(/Retorno 60 dias/)).not.toHaveTextContent('Agendada:')
  })

  it('começa recolhida e, ao expandir, mostra datas, motivo, observação e os resultados com valor e unidade', async () => {
    montar()
    const botao = botaoDa(/Triagem/)
    expect(botao).toHaveAttribute('aria-expanded', 'false')
    expect(screen.queryByText('Aferir pressão arterial')).not.toBeInTheDocument()

    await userEvent.click(botao)

    expect(botao).toHaveAttribute('aria-expanded', 'true')
    expect(screen.getByText('Data e hora agendadas')).toBeInTheDocument()
    expect(screen.getByText('Paciente colaborativo')).toBeInTheDocument()
    expect(screen.getByText('Aferir pressão arterial')).toBeInTheDocument()
    expect(screen.getByText('120/80 mmHg')).toBeInTheDocument()
    expect(screen.getByText('Em repouso')).toBeInTheDocument()
    expect(screen.getByText('Escala de dor')).toBeInTheDocument()
    expect(screen.getByText('2')).toBeInTheDocument()

    await userEvent.click(botao)
    expect(botao).toHaveAttribute('aria-expanded', 'false')
    expect(screen.queryByText('Aferir pressão arterial')).not.toBeInTheDocument()
  })

  it('visita sem resultado diz "Sem resultados registrados"; a cancelada mostra o motivo', async () => {
    montar()

    await userEvent.click(botaoDa(/Retorno 30 dias/))
    expect(screen.getByText('Sem resultados registrados')).toBeInTheDocument()

    await userEvent.click(botaoDa(/Retorno 90 dias/))
    expect(screen.getByText('Participante em viagem')).toBeInTheDocument()
  })

  it('as visitas agendadas ou realizadas têm o link para a tela da visita; as outras não', async () => {
    montar()

    await userEvent.click(botaoDa(/Triagem/))
    await userEvent.click(botaoDa(/Retorno 30 dias/))
    await userEvent.click(botaoDa(/Retorno 60 dias/))
    await userEvent.click(botaoDa(/Retorno 90 dias/))

    const links = screen.getAllByRole('link', { name: 'Abrir visita' })
    expect(links.map((link) => link.getAttribute('href'))).toEqual([
      `/estudos/${estudoId}/participantes/${participacaoId}/visitas/va-1`,
      `/estudos/${estudoId}/participantes/${participacaoId}/visitas/va-2`,
    ])
  })

  it('o filtro por situação esconde as outras visitas e mostra a contagem de itens exibidos', async () => {
    montar()
    expect(screen.getByRole('status')).toHaveTextContent('Exibindo 4 de 4 visitas')

    await userEvent.selectOptions(screen.getByLabelText('Filtrar por situação'), 'Não agendada')

    expect(screen.getByRole('status')).toHaveTextContent('Exibindo 1 de 4 visitas')
    expect(screen.getAllByRole('heading', { level: 4 })).toHaveLength(1)
    expect(screen.getByRole('heading', { level: 4 })).toHaveTextContent('Retorno 60 dias')

    await userEvent.selectOptions(screen.getByLabelText('Filtrar por situação'), 'Todas')
    expect(screen.getAllByRole('heading', { level: 4 })).toHaveLength(4)
  })

  it('o filtro sem nenhuma visita da situação escolhida explica que não há itens', async () => {
    montar([triagem, retorno30])

    await userEvent.selectOptions(screen.getByLabelText('Filtrar por situação'), 'Ausente')

    expect(screen.getByText('Nenhuma visita com a situação selecionada.')).toBeInTheDocument()
    expect(screen.getByRole('status')).toHaveTextContent('Exibindo 0 de 2 visitas')
  })

  it('oferece as cinco situações no filtro', () => {
    montar()

    const opcoes = within(screen.getByLabelText('Filtrar por situação')).getAllByRole('option')
    expect(opcoes.map((opcao) => opcao.textContent)).toEqual([
      'Todas',
      'Não agendada',
      'Agendada',
      'Realizada',
      'Ausente',
      'Cancelada',
    ])
  })

  it('mostra a marca Retirado e preserva visitas e resultados quando o participante foi retirado (T104)', async () => {
    montar([triagem, cancelada], true)

    expect(screen.getByText('Retirado')).toHaveClass('selo--retirado')
    expect(screen.getByText(/Histórico preservado em modo somente leitura após a retirada do estudo/)).toBeInTheDocument()

    await userEvent.click(botaoDa(/Triagem/))
    expect(screen.getByText('Aferir pressão arterial')).toBeInTheDocument()
    expect(screen.getByText('120/80 mmHg')).toBeInTheDocument()
  })
})
