import { screen, within } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { afterAll, afterEach, beforeAll, beforeEach, describe, expect, it, vi } from 'vitest'
import { converterErro } from '../api/erros'
import { criteriosApi } from '../api/criterios'
import { candidatosApi } from '../api/candidatos'
import { estadoEstudoApi } from '../api/estadoEstudo'
import { participantesApi } from '../api/participantes'
import { visitasApi } from '../api/visitas'
import { AbaCandidatos } from '../pages/estudos/detalhe/abas/AbaCandidatos'
import { AbaCriterios } from '../pages/estudos/detalhe/abas/AbaCriterios'
import { AbaVisitas } from '../pages/estudos/detalhe/abas/AbaVisitas'
import { salvarToken } from '../services/api'
import { renderizarAba } from '../test/ambiente'
import { ESTUDO_PLANEJAMENTO, ESTUDO_RECRUTAMENTO, resetarMocks, tokenMock } from './dados'
import { iniciarMocks } from './iniciar'
import { server } from './server'

// T40: as mesmas telas e módulos de API, sem nenhum vi.mock, lendo e gravando no Mock Service Worker.
// Prova que cada tela funciona sobre o mock sem alteração de código (com o mock desligado, as mesmas
// chamadas vão para o backend real).

beforeAll(() => server.listen({ onUnhandledFrame: 'error' }))
afterEach(() => {
  server.resetHandlers()
  salvarToken(null)
})
afterAll(() => server.close())

const estudo = (id: string, status: 'PLANEJAMENTO' | 'RECRUTAMENTO') => ({
  id,
  titulo: 'Estudo mock',
  objetivo: 'Objetivo',
  responsavel: { id: 'r', nome: 'R', email: 'r@x.test' },
  dataInicio: null,
  dataFim: null,
  limiteParticipantes: 2,
  status,
})

describe('Mock Service Worker (T40)', () => {
  beforeEach(() => {
    resetarMocks()
    salvarToken(tokenMock('PESQUISADOR'))
  })

  it('a aba de critérios lista os dados do mock e cria um critério novo', async () => {
    renderizarAba(<AbaCriterios />, { perfil: 'PESQUISADOR', estudo: estudo(ESTUDO_PLANEJAMENTO, 'PLANEJAMENTO') })

    const inclusao = await screen.findByRole('region', { name: 'Critérios de inclusão' })
    expect(within(inclusao).getByText('Idade adulta')).toBeInTheDocument()

    await userEvent.type(screen.getByLabelText(/^Título/), 'Sem alergia')
    await userEvent.type(screen.getByLabelText(/^Descrição/), 'Sem alergia conhecida ao medicamento')
    await userEvent.click(screen.getByRole('button', { name: 'Adicionar critério' }))

    expect(await within(screen.getByRole('region', { name: 'Critérios de inclusão' })).findByText('Sem alergia')).toBeInTheDocument()
  })

  it('o mock devolve o erro de duplicidade no formato da API e a tela o mostra', async () => {
    renderizarAba(<AbaCriterios />, { perfil: 'PESQUISADOR', estudo: estudo(ESTUDO_PLANEJAMENTO, 'PLANEJAMENTO') })
    await screen.findByText('Idade adulta')

    await userEvent.type(screen.getByLabelText(/^Título/), 'idade adulta')
    await userEvent.type(screen.getByLabelText(/^Descrição/), 'Outra descrição qualquer')
    await userEvent.click(screen.getByRole('button', { name: 'Adicionar critério' }))

    expect(await screen.findByRole('alert')).toHaveTextContent('Já existe um critério de inclusão com esse título')
  })

  it('a aba de visitas lista as visitas do mock ordenadas', async () => {
    renderizarAba(<AbaVisitas />, { perfil: 'PESQUISADOR', estudo: estudo(ESTUDO_RECRUTAMENTO, 'RECRUTAMENTO') })

    const resumos = await screen.findAllByText(/^Dia \d+ · /)
    expect(resumos.map((r) => r.textContent)).toEqual(['Dia 0 · Visita inicial', 'Dia 30 · Retorno mensal'])
  })

  it('a aba de candidatos lista, filtra e cadastra no mock', async () => {
    salvarToken(tokenMock('COORDENADOR'))
    renderizarAba(<AbaCandidatos />, { perfil: 'COORDENADOR', estudo: estudo(ESTUDO_RECRUTAMENTO, 'RECRUTAMENTO') })

    expect(await screen.findByText('CAND-001')).toBeInTheDocument()
    await userEvent.selectOptions(screen.getByLabelText(/^Filtrar por situação/), 'INCLUIDO')
    expect(await screen.findByText('CAND-003')).toBeInTheDocument()
    expect(screen.queryByText('CAND-001')).not.toBeInTheDocument()

    await userEvent.selectOptions(screen.getByLabelText(/^Filtrar por situação/), '')
    await userEvent.type(screen.getByLabelText(/^Nome fictício/), 'K. L.')
    await userEvent.type(screen.getByLabelText(/^Data de nascimento/), '1995-05-05')
    await userEvent.click(screen.getByRole('button', { name: 'Cadastrar candidato' }))

    expect(await screen.findByText('Candidato CAND-004 cadastrado.')).toBeInTheDocument()
    expect(await screen.findByText('K. L.')).toBeInTheDocument()
  })

  it('cada módulo de API alcança o seu endpoint do contrato', async () => {
    salvarToken(tokenMock('COORDENADOR'))
    expect((await visitasApi.listar(ESTUDO_RECRUTAMENTO)).length).toBe(2)
    expect((await criteriosApi.listar(ESTUDO_RECRUTAMENTO)).length).toBe(3)
    expect((await candidatosApi.listar(ESTUDO_RECRUTAMENTO)).totalElementos).toBe(3)
    expect(await participantesApi.vagas(ESTUDO_RECRUTAMENTO)).toEqual({ incluidos: 1, limite: 2, restantes: 1 })
    expect((await participantesApi.listar(ESTUDO_RECRUTAMENTO)).length).toBe(1)
    expect((await estadoEstudoApi.historico(ESTUDO_RECRUTAMENTO)).length).toBe(1)
  })

  it('o fluxo de recrutamento completo funciona no mock: avaliar, consentir e incluir até esgotar as vagas', async () => {
    salvarToken(tokenMock('COORDENADOR'))
    const { avaliacoesApi } = await import('../api/avaliacoes')
    const { consentimentoApi } = await import('../api/consentimento')
    const lista = await candidatosApi.listar(ESTUDO_RECRUTAMENTO)
    const cand = lista.conteudo[0]
    const criterios = await criteriosApi.listar(ESTUDO_RECRUTAMENTO)

    await expect(participantesApi.incluir(ESTUDO_RECRUTAMENTO, cand.id).catch(converterErro)).resolves.toMatchObject({
      codigo: 'CONSENTIMENTO_AUSENTE', status: 409,
    })
    await consentimentoApi.registrar(cand.id)
    const incluido = await participantesApi.incluir(ESTUDO_RECRUTAMENTO, cand.id)
    expect(incluido.codigo).toBe('CAND-001')

    const segundo = lista.conteudo[1]
    await avaliacoesApi.avaliar(segundo.id, {
      respostas: criterios.map((c) => ({ criterioId: c.id, aplica: c.tipo === 'INCLUSAO' })),
    })
    await consentimentoApi.registrar(segundo.id)
    await expect(participantesApi.incluir(ESTUDO_RECRUTAMENTO, segundo.id).catch(converterErro)).resolves.toMatchObject({
      codigo: 'VAGAS_ESGOTADAS', status: 409,
    })
  })

  it('o mock recusa perfis sem permissão e requisições sem token', async () => {
    salvarToken(tokenMock('COORDENADOR'))
    await expect(criteriosApi.criar(ESTUDO_PLANEJAMENTO, { tipo: 'INCLUSAO', titulo: 'X', descricao: 'Descrição X' }).catch(converterErro))
      .resolves.toMatchObject({ status: 403 })
    salvarToken(null)
    await expect(criteriosApi.listar(ESTUDO_PLANEJAMENTO).catch(converterErro)).resolves.toMatchObject({ status: 401 })
  })

  it('avança o estado do estudo pelo mock respeitando as pré-condições', async () => {
    await expect(estadoEstudoApi.avancar(ESTUDO_PLANEJAMENTO, 'EXECUCAO').catch(converterErro)).resolves.toMatchObject({
      codigo: 'TRANSICAO_ESTADO_INVALIDA',
    })
    const avancado = await estadoEstudoApi.avancar(ESTUDO_PLANEJAMENTO, 'RECRUTAMENTO')
    expect(avancado.status).toBe('RECRUTAMENTO')
    expect((await estadoEstudoApi.historico(ESTUDO_PLANEJAMENTO)).at(-1)?.estadoNovo).toBe('RECRUTAMENTO')
  })
})

describe('iniciarMocks', () => {
  it('não carrega o Mock Service Worker com a variável desligada: as chamadas vão para a API real', async () => {
    vi.stubEnv('VITE_USE_MOCKS', 'false')
    const iniciar = vi.fn()
    vi.doMock('./browser', () => ({ worker: { start: iniciar } }))

    await iniciarMocks()

    expect(iniciar).not.toHaveBeenCalled()
    vi.unstubAllEnvs()
    vi.doUnmock('./browser')
  })

  it('liga o worker quando VITE_USE_MOCKS=true', async () => {
    vi.stubEnv('VITE_USE_MOCKS', 'true')
    const iniciar = vi.fn().mockResolvedValue(undefined)
    vi.doMock('./browser', () => ({ worker: { start: iniciar } }))
    vi.spyOn(console, 'info').mockImplementation(() => undefined)

    await iniciarMocks()

    expect(iniciar).toHaveBeenCalledWith({ onUnhandledFrame: 'bypass' })
    vi.unstubAllEnvs()
    vi.doUnmock('./browser')
  })
})
