import { render, screen, within } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { AxiosError, AxiosHeaders, type AxiosResponse } from 'axios'
import { createMemoryRouter, RouterProvider } from 'react-router'
import { beforeEach, describe, expect, it, vi } from 'vitest'
import { SessaoContext, type Sessao } from '../../../contexts/sessao'
import { rotas } from '../../../routes/router'
import { estudosApi } from '../../../services/recursos'
import type { Estudo } from '../../../types/estudo'
import type { PerfilUsuario } from '../../../types/usuario'

// A API é substituída por dublês; as rotas, os guards e o layout são os reais
vi.mock('../../../services/recursos', () => ({
  autenticacaoApi: {},
  usuariosApi: {},
  estudosApi: { listar: vi.fn(), buscar: vi.fn() },
}))

const buscar = vi.mocked(estudosApi.buscar)
const listar = vi.mocked(estudosApi.listar)

const estudo: Estudo = {
  id: '3f6c1a52-0d1e-4c8b-9a77-5b2e8d4f1c90',
  titulo: 'Estudo de Hipertensão Leve',
  objetivo: 'Avaliar a adesão ao tratamento em adultos com hipertensão leve.',
  responsavel: { id: 'u-pesq', nome: 'Ana Pesquisadora', email: 'ana@clinicalstudy.test' },
  dataInicio: '2026-11-01',
  dataFim: '2027-03-31',
  limiteParticipantes: 30,
  status: 'PLANEJAMENTO',
}

function erroHttp(status: number, mensagem: string): AxiosError {
  const resposta = {
    status,
    statusText: '',
    data: { erro: mensagem },
    headers: {},
    config: { headers: new AxiosHeaders() },
  } as AxiosResponse
  return new AxiosError(mensagem, 'ERR_BAD_REQUEST', undefined, undefined, resposta)
}

function renderizar(perfil: PerfilUsuario, caminho: string) {
  const sessao: Sessao = {
    usuario: { id: 'u-1', nome: 'Usuário de Teste', email: 'teste@clinicalstudy.test', perfil },
    carregando: false,
    entrar: vi.fn(),
    sair: vi.fn(),
  }
  const router = createMemoryRouter(rotas, { initialEntries: [caminho] })
  render(
    <SessaoContext.Provider value={sessao}>
      <RouterProvider router={router} />
    </SessaoContext.Provider>,
  )
  return router
}

function rotulosDasAbas(): string[] {
  const abas = screen.getByRole('navigation', { name: 'Seções do estudo' })
  return within(abas)
    .getAllByRole('link')
    .map((link) => link.textContent ?? '')
}

describe('EstudoDetalhePage', () => {
  beforeEach(() => {
    vi.clearAllMocks()
    buscar.mockResolvedValue(estudo)
    listar.mockResolvedValue([estudo])
  })

  it('mostra o título, o estado atual e abre a aba Resumo por padrão', async () => {
    const router = renderizar('PESQUISADOR', `/estudos/${estudo.id}`)

    expect(await screen.findByRole('heading', { level: 1, name: estudo.titulo })).toBeInTheDocument()
    expect(screen.getByText(/Estado atual/)).toHaveTextContent('Estado atual: Planejamento')
    // O índice /estudos/:estudoId redireciona para a aba Resumo
    expect(await screen.findByRole('heading', { level: 2, name: 'Resumo' })).toBeInTheDocument()
    expect(screen.getByText(estudo.objetivo)).toBeInTheDocument()
    expect(router.state.location.pathname).toBe(`/estudos/${estudo.id}/resumo`)
    expect(buscar).toHaveBeenCalledWith(estudo.id)
  })

  it('o botão de voltar leva à listagem de estudos', async () => {
    const router = renderizar('COORDENADOR', `/estudos/${estudo.id}/resumo`)

    await userEvent.click(await screen.findByRole('link', { name: 'Voltar para a lista de estudos' }))

    expect(router.state.location.pathname).toBe('/estudos')
  })

  it('mostra ao pesquisador as abas Resumo, Critérios e Visitas', async () => {
    renderizar('PESQUISADOR', `/estudos/${estudo.id}`)
    await screen.findByRole('heading', { level: 1, name: estudo.titulo })

    expect(rotulosDasAbas()).toEqual(['Resumo', 'Critérios', 'Visitas'])
  })

  it('mostra ao coordenador as abas Resumo, Candidatos e Participantes', async () => {
    renderizar('COORDENADOR', `/estudos/${estudo.id}`)
    await screen.findByRole('heading', { level: 1, name: estudo.titulo })

    expect(rotulosDasAbas()).toEqual(['Resumo', 'Candidatos', 'Participantes'])
    expect(screen.queryByRole('link', { name: 'Editar dados' })).not.toBeInTheDocument()
  })

  it('navega entre as abas e marca a aba atual', async () => {
    const router = renderizar('PESQUISADOR', `/estudos/${estudo.id}`)
    await screen.findByRole('heading', { level: 1, name: estudo.titulo })

    await userEvent.click(screen.getByRole('link', { name: 'Visitas' }))

    expect(router.state.location.pathname).toBe(`/estudos/${estudo.id}/visitas`)
    expect(screen.getByRole('heading', { level: 2, name: 'Visitas previstas e atividades' })).toBeInTheDocument()
    expect(screen.getByRole('link', { name: 'Visitas' })).toHaveAttribute('aria-current', 'page')
  })

  it('bloqueia a aba de outro perfil acessada direto pela URL', async () => {
    renderizar('COORDENADOR', `/estudos/${estudo.id}/criterios`)

    expect(await screen.findByRole('heading', { name: 'Acesso negado' })).toBeInTheDocument()
  })

  it('mostra erro quando o estudo não existe', async () => {
    buscar.mockRejectedValue(erroHttp(404, `Estudo não encontrado: ${estudo.id}`))
    renderizar('PESQUISADOR', `/estudos/${estudo.id}`)

    expect(await screen.findByRole('heading', { level: 1, name: 'Estudo não encontrado' })).toBeInTheDocument()
    expect(screen.getByRole('alert')).toHaveTextContent('O estudo solicitado não existe ou foi excluído.')
    expect(screen.queryByRole('navigation', { name: 'Seções do estudo' })).not.toBeInTheDocument()
    expect(screen.getByRole('link', { name: 'Voltar para estudos' })).toHaveAttribute('href', '/estudos')
  })

  it('mostra a mensagem da API quando a falha não é de estudo inexistente', async () => {
    buscar.mockRejectedValue(new AxiosError('Network Error', 'ERR_NETWORK'))
    renderizar('PESQUISADOR', `/estudos/${estudo.id}`)

    expect(await screen.findByRole('heading', { level: 1, name: 'Não foi possível carregar o estudo' })).toBeInTheDocument()
    expect(screen.getByRole('alert')).toHaveTextContent('Não foi possível conectar à API')
  })

  it('a listagem de estudos abre a página de detalhe', async () => {
    const router = renderizar('COORDENADOR', '/estudos')

    await userEvent.click(await screen.findByRole('link', { name: estudo.titulo }))

    expect(await screen.findByRole('heading', { level: 1, name: estudo.titulo })).toBeInTheDocument()
    expect(router.state.location.pathname).toBe(`/estudos/${estudo.id}/resumo`)
  })
})
