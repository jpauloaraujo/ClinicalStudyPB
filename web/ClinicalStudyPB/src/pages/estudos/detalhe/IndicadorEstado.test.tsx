import { render, screen, within } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { beforeEach, describe, expect, it, vi } from 'vitest'
import { estadoEstudoApi } from '../../../api/estadoEstudo'
import { erroHttp, estudoBase } from '../../../test/ambiente'
import type { StatusEstudo } from '../../../types/estudo'
import type { PerfilUsuario } from '../../../types/usuario'
import { IndicadorEstado } from './IndicadorEstado'

vi.mock('../../../api/estadoEstudo', () => ({ estadoEstudoApi: { avancar: vi.fn(), historico: vi.fn() } }))

const avancar = vi.mocked(estadoEstudoApi.avancar)

function renderizar(perfil: PerfilUsuario, status: StatusEstudo = 'PLANEJAMENTO') {
  const aoAvancar = vi.fn()
  render(<IndicadorEstado estudo={{ ...estudoBase, status }} perfil={perfil} aoAvancar={aoAvancar} />)
  return { aoAvancar }
}

describe('IndicadorEstado (T43)', () => {
  beforeEach(() => {
    vi.clearAllMocks()
  })

  it('mostra os quatro estados e destaca o atual', () => {
    renderizar('PESQUISADOR', 'RECRUTAMENTO')

    const etapas = within(screen.getByRole('list', { name: 'Etapas do estudo' })).getAllByRole('listitem')
    expect(etapas.map((e) => e.textContent)).toEqual(['Planejamento', 'Recrutamento', 'Execução', 'Encerrado'])
    expect(etapas[1]).toHaveAttribute('aria-current', 'step')
    expect(etapas[0]).not.toHaveAttribute('aria-current')
  })

  it('mostra o botão de avançar só ao pesquisador, com o nome do próximo estado', () => {
    renderizar('PESQUISADOR')

    expect(screen.getByRole('button', { name: 'Avançar para Recrutamento' })).toBeInTheDocument()
  })

  it.each<PerfilUsuario>(['COORDENADOR', 'ADMINISTRADOR'])('oculta o botão de avançar para o %s', (perfil) => {
    renderizar(perfil)

    expect(screen.queryByRole('button', { name: /Avançar para/ })).not.toBeInTheDocument()
  })

  it('oculta o botão quando não existe próximo estado', () => {
    renderizar('PESQUISADOR', 'ENCERRADO')

    expect(screen.queryByRole('button', { name: /Avançar para/ })).not.toBeInTheDocument()
  })

  it('pede confirmação avisando que não há volta e só avança depois de confirmar', async () => {
    avancar.mockResolvedValue({ ...estudoBase, status: 'RECRUTAMENTO' })
    const { aoAvancar } = renderizar('PESQUISADOR')

    await userEvent.click(screen.getByRole('button', { name: 'Avançar para Recrutamento' }))
    const dialogo = screen.getByRole('dialog', { name: 'Avançar para Recrutamento?' })
    expect(dialogo).toHaveTextContent('Não é possível voltar ao estado anterior')
    expect(avancar).not.toHaveBeenCalled()

    await userEvent.click(within(dialogo).getByRole('button', { name: 'Confirmar' }))

    expect(avancar).toHaveBeenCalledWith(estudoBase.id, 'RECRUTAMENTO')
    expect(aoAvancar).toHaveBeenCalledTimes(1)
    expect(screen.queryByRole('dialog')).not.toBeInTheDocument()
  })

  it('cancelar fecha o diálogo sem avançar', async () => {
    const { aoAvancar } = renderizar('PESQUISADOR')

    await userEvent.click(screen.getByRole('button', { name: 'Avançar para Recrutamento' }))
    await userEvent.click(screen.getByRole('button', { name: 'Cancelar' }))

    expect(screen.queryByRole('dialog')).not.toBeInTheDocument()
    expect(avancar).not.toHaveBeenCalled()
    expect(aoAvancar).not.toHaveBeenCalled()
  })

  it('mostra no alerta a regra violada quando a API responde 409', async () => {
    avancar.mockRejectedValue(erroHttp(409, 'Adicione ao menos um critério de inclusão para avançar', 'ESTUDO_SEM_CRITERIO_INCLUSAO'))
    const { aoAvancar } = renderizar('PESQUISADOR')

    await userEvent.click(screen.getByRole('button', { name: 'Avançar para Recrutamento' }))
    await userEvent.click(screen.getByRole('button', { name: 'Confirmar' }))

    expect(await screen.findByRole('alert')).toHaveTextContent('Adicione ao menos um critério de inclusão para avançar')
    expect(aoAvancar).not.toHaveBeenCalled()
    expect(screen.queryByRole('dialog')).not.toBeInTheDocument()
  })
})
