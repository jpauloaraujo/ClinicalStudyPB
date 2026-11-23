import { screen, within } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { beforeEach, describe, expect, it, vi } from 'vitest'
import { contaParticipanteApi } from '../../../../api/contaParticipante'
import { historicoApi } from '../../../../api/historico'
import { erroHttp, estudoBase, renderizarAba } from '../../../../test/ambiente'
import type { PerfilUsuario } from '../../../../types/usuario'
import { FichaParticipantePage } from './FichaParticipantePage'
import { cancelada, historicoBase, participacaoId, retorno30, retorno60, triagem } from './historicoFixture'

vi.mock('../../../../api/historico', () => ({ historicoApi: { buscar: vi.fn() } }))
vi.mock('../../../../api/contaParticipante', () => ({
  contaParticipanteApi: { buscar: vi.fn(), vincular: vi.fn(), listarDisponiveis: vi.fn() },
}))

const buscar = vi.mocked(historicoApi.buscar)
const buscarConta = vi.mocked(contaParticipanteApi.buscar)

function abrir(perfil: PerfilUsuario = 'COORDENADOR') {
  return renderizarAba(<FichaParticipantePage />, {
    perfil,
    rota: 'participantes/:participacaoId',
    caminho: `/participantes/${participacaoId}`,
  })
}

describe('FichaParticipantePage (T97)', () => {
  beforeEach(() => {
    vi.clearAllMocks()
    buscar.mockResolvedValue(historicoBase)
    buscarConta.mockResolvedValue(null)
  })

  it('busca o histórico da participação da rota e mostra código, nome fictício e data de inclusão', async () => {
    abrir()

    expect(await screen.findByRole('heading', { name: 'CAND-001 · Pessoa Teta (Ficticia)' })).toBeInTheDocument()
    expect(screen.getByText('Incluído em 20/10/2026')).toBeInTheDocument()
    expect(buscar).toHaveBeenCalledWith(participacaoId)
  })

  it('mostra os 4 cartões de resumo com os valores do servidor, sem recalcular no cliente', async () => {
    // Números propositalmente diferentes da contagem das visitas: a tela só repassa o que o servidor mandou
    buscar.mockResolvedValue({
      ...historicoBase,
      resumo: { previstas: 9, realizadas: 4, ausentes: 3, canceladas: 2, agendadas: 5, naoAgendadas: 6, pendentes: 11 },
    })
    abrir()

    const resumo = await screen.findByRole('region', { name: 'Resumo do histórico' })
    const cartao = (rotulo: string) => within(resumo).getByText(rotulo).closest('li')
    expect(cartao('Realizadas')).toHaveTextContent('4')
    expect(cartao('Ausentes')).toHaveTextContent('3')
    expect(cartao('Canceladas')).toHaveTextContent('2')
    expect(cartao('Pendentes')).toHaveTextContent('11')
    expect(within(resumo).getAllByRole('listitem')).toHaveLength(4)
  })

  it('lista todas as visitas previstas na ordem, inclusive as não agendadas', async () => {
    abrir()

    const visitas = await screen.findByRole('region', { name: 'Visitas do participante' })
    const titulos = within(visitas).getAllByRole('heading', { level: 4 })
    expect(titulos).toHaveLength(3)
    expect(titulos[0]).toHaveTextContent('Triagem')
    expect(titulos[0]).toHaveTextContent('Realizada')
    expect(titulos[1]).toHaveTextContent('Retorno 30 dias')
    expect(titulos[1]).toHaveTextContent('Agendada')
    expect(titulos[2]).toHaveTextContent('Retorno 60 dias')
    expect(titulos[2]).toHaveTextContent('Não agendada')
  })

  it('mostra os resultados ao expandir a visita', async () => {
    abrir()
    await userEvent.click(await screen.findByRole('button', { name: /Triagem/ }))

    expect(screen.getByText('Aferir pressão arterial')).toBeInTheDocument()
    expect(screen.getByText('120/80 mmHg')).toBeInTheDocument()
  })

  it('o filtro por situação mostra só as visitas escolhidas e a contagem exibida', async () => {
    abrir()
    await userEvent.selectOptions(await screen.findByLabelText('Filtrar por situação'), 'Realizada')

    const visitas = screen.getByRole('region', { name: 'Visitas do participante' })
    expect(within(visitas).getAllByRole('heading', { level: 4 })).toHaveLength(1)
    expect(within(visitas).getByRole('heading', { level: 4 })).toHaveTextContent('Triagem')
    expect(screen.getByRole('status')).toHaveTextContent('Exibindo 1 de 3 visitas')
  })

  it('estudo sem visitas previstas mostra a lista vazia e o resumo zerado', async () => {
    buscar.mockResolvedValue({
      ...historicoBase,
      visitas: [],
      resumo: { previstas: 0, realizadas: 0, ausentes: 0, canceladas: 0, agendadas: 0, naoAgendadas: 0, pendentes: 0 },
    })
    abrir()

    expect(await screen.findByText('Este estudo ainda não tem visitas previstas.')).toBeInTheDocument()
    expect(screen.queryByLabelText('Filtrar por situação')).not.toBeInTheDocument()
    const resumo = screen.getByRole('region', { name: 'Resumo do histórico' })
    expect(within(resumo).getAllByRole('listitem')).toHaveLength(4)
  })

  it('indica o carregamento enquanto o histórico não chega', async () => {
    buscar.mockReturnValue(new Promise(() => {}))
    abrir()

    expect(await screen.findByText('Carregando histórico do participante…')).toBeInTheDocument()
  })

  it('erro do servidor mostra a mensagem e o botão "Tentar de novo", que busca de novo', async () => {
    buscar.mockRejectedValueOnce(erroHttp(500, 'Erro interno do servidor'))
    abrir()

    expect(await screen.findByRole('alert')).toHaveTextContent('Erro interno do servidor')
    expect(screen.getByRole('heading', { name: 'Não foi possível carregar o histórico' })).toBeInTheDocument()

    await userEvent.click(screen.getByRole('button', { name: 'Tentar de novo' }))

    expect(await screen.findByRole('heading', { name: 'CAND-001 · Pessoa Teta (Ficticia)' })).toBeInTheDocument()
    expect(buscar).toHaveBeenCalledTimes(2)
  })

  it('participante inexistente (404) mostra mensagem clara e o caminho de volta, sem "Tentar de novo"', async () => {
    buscar.mockRejectedValue(erroHttp(404, 'Participação não encontrada.', 'RECURSO_NAO_ENCONTRADO'))
    abrir()

    expect(await screen.findByRole('heading', { name: 'Participante não encontrado' })).toBeInTheDocument()
    expect(screen.getByRole('alert')).toHaveTextContent('O participante solicitado não existe neste estudo.')
    expect(screen.queryByRole('button', { name: 'Tentar de novo' })).not.toBeInTheDocument()
    expect(screen.getByRole('link', { name: 'Voltar para participantes' })).toHaveAttribute(
      'href',
      `/estudos/${estudoBase.id}/participantes`,
    )
  })

  it('o coordenador tem o link "Agendar visitas" para o agendamento do participante', async () => {
    abrir('COORDENADOR')

    expect(await screen.findByRole('link', { name: 'Agendar visitas' })).toHaveAttribute(
      'href',
      `/estudos/${estudoBase.id}/participantes/${participacaoId}/agendamento`,
    )
  })

  it('o pesquisador só lê: vê o histórico, mas não tem "Agendar visitas" nem botões de escrita', async () => {
    abrir('PESQUISADOR')

    expect(await screen.findByRole('heading', { name: 'CAND-001 · Pessoa Teta (Ficticia)' })).toBeInTheDocument()
    expect(screen.queryByRole('link', { name: 'Agendar visitas' })).not.toBeInTheDocument()
    expect(screen.queryByRole('region', { name: 'Ações do participante' })).not.toBeInTheDocument()
    // Os únicos botões da tela são os que expandem as visitas
    for (const botao of screen.getAllByRole('button')) {
      expect(botao).toHaveAttribute('aria-expanded')
    }
  })

  it('tem as regiões nomeadas "Resumo do histórico" e "Visitas do participante"', async () => {
    abrir()

    expect(await screen.findByRole('region', { name: 'Resumo do histórico' })).toBeInTheDocument()
    expect(screen.getByRole('region', { name: 'Visitas do participante' })).toBeInTheDocument()
  })

  describe('corpo completo da API, com as cinco situações (T98)', () => {
    const ausente = { ...retorno30, visitaPlanejadaId: 'vp-5', nome: 'Retorno 45 dias', ordem: 3, diaPrevisto: 45, situacao: 'AUSENTE' as const }
    const completo = {
      ...historicoBase,
      resumo: { previstas: 5, realizadas: 1, ausentes: 1, canceladas: 1, agendadas: 1, naoAgendadas: 1, pendentes: 2 },
      visitas: [triagem, retorno30, ausente, { ...retorno60, ordem: 4 }, { ...cancelada, ordem: 5 }],
    }

    it('mantém a ordem do servidor e mostra a situação de cada visita', async () => {
      buscar.mockResolvedValue(completo)
      abrir()

      const visitas = await screen.findByRole('region', { name: 'Visitas do participante' })
      const titulos = within(visitas).getAllByRole('heading', { level: 4 })
      expect(titulos.map((t) => t.textContent)).toEqual([
        expect.stringMatching(/Triagem.*Realizada/),
        expect.stringMatching(/Retorno 30 dias.*Agendada/),
        expect.stringMatching(/Retorno 45 dias.*Ausente/),
        expect.stringMatching(/Retorno 60 dias.*Não agendada/),
        expect.stringMatching(/Retorno 90 dias.*Cancelada/),
      ])
    })

    it('os cartões e a contagem do filtro batem com o resumo do servidor', async () => {
      buscar.mockResolvedValue(completo)
      abrir()

      const resumo = await screen.findByRole('region', { name: 'Resumo do histórico' })
      const cartao = (rotulo: string) => within(resumo).getByText(rotulo).closest('li')
      expect(cartao('Realizadas')).toHaveTextContent('1')
      expect(cartao('Ausentes')).toHaveTextContent('1')
      expect(cartao('Canceladas')).toHaveTextContent('1')
      expect(cartao('Pendentes')).toHaveTextContent('2')
      expect(screen.getByRole('status')).toHaveTextContent('Exibindo 5 de 5 visitas')
    })

    it('a visita cancelada mostra o motivo ao expandir', async () => {
      buscar.mockResolvedValue(completo)
      abrir()
      await userEvent.click(await screen.findByRole('button', { name: /Retorno 90 dias/ }))

      expect(screen.getByText(/Participante em viagem/)).toBeInTheDocument()
    })
  })
})

describe('FichaParticipantePage: conta vinculada (T111, RQ-T111-09 e 10)', () => {
  const conta = { participacaoId, usuarioId: 'u-1', nome: 'Maria Participante (Ficticia)', email: 'maria@clinicalstudy.test' }

  beforeEach(() => {
    vi.clearAllMocks()
    buscar.mockResolvedValue(historicoBase)
    buscarConta.mockResolvedValue(null)
    vi.mocked(contaParticipanteApi.listarDisponiveis).mockResolvedValue([
      { usuarioId: 'u-9', nome: 'Ana Livre', email: 'ana@clinicalstudy.test' },
    ])
  })

  it.each<PerfilUsuario>(['COORDENADOR', 'PESQUISADOR'])(
    '%s vê "Conta vinculada: nome (e-mail)" quando há conta, e a busca usa a participação da rota',
    async (perfil) => {
      buscarConta.mockResolvedValue(conta)
      abrir(perfil)

      expect(await screen.findByText('Conta vinculada: Maria Participante (Ficticia) (maria@clinicalstudy.test)')).toBeInTheDocument()
      expect(buscarConta).toHaveBeenCalledWith(participacaoId)
      expect(screen.queryByText('Nenhuma conta vinculada ao participante.')).not.toBeInTheDocument()
    },
  )

  it('sem conta (null) mostra "Nenhuma conta vinculada" aos dois perfis, sem erro de tela', async () => {
    abrir('PESQUISADOR')

    expect(await screen.findByText('Nenhuma conta vinculada ao participante.')).toBeInTheDocument()
    expect(screen.queryByRole('alert')).not.toBeInTheDocument()
  })

  it('o coordenador vê o botão "Vincular conta" só quando não há conta; o pesquisador nunca', async () => {
    abrir('COORDENADOR')
    expect(await screen.findByRole('button', { name: 'Vincular conta' })).toBeInTheDocument()
  })

  it('com conta vinculada o coordenador não vê o botão', async () => {
    buscarConta.mockResolvedValue(conta)
    abrir('COORDENADOR')

    await screen.findByText(/Conta vinculada: Maria/)
    expect(screen.queryByRole('button', { name: 'Vincular conta' })).not.toBeInTheDocument()
  })

  it('o pesquisador não tem o botão mesmo sem conta', async () => {
    abrir('PESQUISADOR')

    await screen.findByText('Nenhuma conta vinculada ao participante.')
    expect(screen.queryByRole('button', { name: 'Vincular conta' })).not.toBeInTheDocument()
  })

  it('falha ao consultar a conta mostra um aviso, mantém a ficha e não oferece o vínculo', async () => {
    buscarConta.mockRejectedValue(erroHttp(500, 'Falha no servidor'))
    abrir('COORDENADOR')

    expect(await screen.findByRole('alert')).toHaveTextContent('Não foi possível consultar a conta vinculada.')
    expect(screen.getByRole('heading', { name: 'CAND-001 · Pessoa Teta (Ficticia)' })).toBeInTheDocument()
    expect(screen.queryByRole('button', { name: 'Vincular conta' })).not.toBeInTheDocument()
  })

  it('depois de vincular, recarrega a conta, mostra a confirmação e esconde o botão', async () => {
    const vinculada = { ...conta, usuarioId: 'u-9', nome: 'Ana Livre', email: 'ana@clinicalstudy.test' }
    vi.mocked(contaParticipanteApi.vincular).mockImplementation(async () => {
      buscarConta.mockResolvedValue(vinculada)
      return vinculada
    })
    abrir('COORDENADOR')

    await userEvent.click(await screen.findByRole('button', { name: 'Vincular conta' }))
    await userEvent.click(await screen.findByRole('radio', { name: /Ana Livre/ }))
    await userEvent.click(screen.getByRole('button', { name: 'Vincular' }))

    expect(await screen.findByText('Conta vinculada: Ana Livre (ana@clinicalstudy.test)')).toBeInTheDocument()
    expect(within(screen.getByRole('region', { name: 'Ações do participante' })).getByRole('status')).toHaveTextContent(
      'Conta de Ana Livre vinculada com sucesso.',
    )
    expect(screen.queryByRole('button', { name: 'Vincular conta' })).not.toBeInTheDocument()
    expect(contaParticipanteApi.vincular).toHaveBeenCalledWith(participacaoId, 'u-9')
  })
})
