import { screen, within } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { beforeEach, describe, expect, it, vi } from 'vitest'
import { candidatosApi } from '../../../../api/candidatos'
import type { CandidatoResponse, PaginaCandidatos } from '../../../../api/tipos'
import { erroHttp, estudoBase, renderizarAba } from '../../../../test/ambiente'
import { AbaCandidatos } from './AbaCandidatos'

vi.mock('../../../../api/candidatos', () => ({
  candidatosApi: { listar: vi.fn(), buscar: vi.fn(), criar: vi.fn(), atualizar: vi.fn() },
}))

const listar = vi.mocked(candidatosApi.listar)
const criar = vi.mocked(candidatosApi.criar)

const estudoEmRecrutamento = { ...estudoBase, status: 'RECRUTAMENTO' as const }

function candidato(codigo: string, parcial: Partial<CandidatoResponse> = {}): CandidatoResponse {
  return {
    id: `id-${codigo}`, estudoId: estudoBase.id, codigo, nomeFicticio: `Nome ${codigo}`, dataNascimento: '1990-04-12',
    contato: null, usuarioId: null, situacao: 'CADASTRADO', resultadoAvaliacao: null, dataAvaliacao: null,
    consentimentoRegistrado: false, dataConsentimento: null, ...parcial,
  }
}

function pagina(conteudo: CandidatoResponse[], extra: Partial<PaginaCandidatos> = {}): PaginaCandidatos {
  return { conteudo, pagina: 0, tamanho: 10, totalElementos: conteudo.length, totalPaginas: 1, ...extra }
}

async function preencherEEnviar(nome: string, nascimento: string) {
  await userEvent.type(screen.getByLabelText(/^Nome fictício/), nome)
  if (nascimento) await userEvent.type(screen.getByLabelText(/^Data de nascimento/), nascimento)
  await userEvent.click(screen.getByRole('button', { name: 'Cadastrar candidato' }))
}

describe('AbaCandidatos (T52)', () => {
  beforeEach(() => {
    vi.clearAllMocks()
    listar.mockResolvedValue(
      pagina([
        candidato('CAND-001', { situacao: 'ELEGIVEL', resultadoAvaliacao: 'APROVADO' }),
        candidato('CAND-002', { situacao: 'CONSENTIDO', resultadoAvaliacao: 'APROVADO', consentimentoRegistrado: true }),
        candidato('CAND-003', { situacao: 'NAO_ELEGIVEL', resultadoAvaliacao: 'REPROVADO' }),
        candidato('CAND-004'),
      ]),
    )
  })

  it('lista os candidatos com situação, elegibilidade e consentimento', async () => {
    renderizarAba(<AbaCandidatos />, { perfil: 'COORDENADOR', estudo: estudoEmRecrutamento })

    const linha1 = (await screen.findByText('CAND-001')).closest('tr')!
    expect(within(linha1).getByText('Elegível', { selector: '.etiqueta' })).toBeInTheDocument()
    expect(within(linha1).getByText('Pendente')).toBeInTheDocument()
    const linha2 = screen.getByText('CAND-002').closest('tr')!
    expect(within(linha2).getByText('Registrado')).toBeInTheDocument()
    expect(within(screen.getByText('CAND-003').closest('tr')!).getByText('Não elegível', { selector: 'td' })).toBeInTheDocument()
    expect(within(screen.getByText('CAND-004').closest('tr')!).getByText('Não avaliado')).toBeInTheDocument()
    expect(listar).toHaveBeenCalledWith(estudoBase.id, { situacao: undefined, pagina: 0, tamanho: 10 })
  })

  it('tem um link para a ficha de cada candidato', async () => {
    renderizarAba(<AbaCandidatos />, { perfil: 'COORDENADOR', estudo: estudoEmRecrutamento })

    const link = await screen.findByRole('link', { name: 'Abrir a ficha de CAND-002' })
    expect(link).toHaveAttribute('href', '/id-CAND-002')
  })

  it('filtra por situação e volta para a primeira página', async () => {
    renderizarAba(<AbaCandidatos />, { perfil: 'COORDENADOR', estudo: estudoEmRecrutamento })
    await screen.findByText('CAND-001')

    await userEvent.selectOptions(screen.getByLabelText(/^Filtrar por situação/), 'CONSENTIDO')

    expect(listar).toHaveBeenLastCalledWith(estudoBase.id, { situacao: 'CONSENTIDO', pagina: 0, tamanho: 10 })
  })

  it('avisa quando o filtro não encontra candidatos', async () => {
    listar.mockResolvedValue(pagina([]))
    renderizarAba(<AbaCandidatos />, { perfil: 'COORDENADOR', estudo: estudoEmRecrutamento })

    expect(await screen.findByText('Nenhum candidato cadastrado.')).toBeInTheDocument()
    await userEvent.selectOptions(screen.getByLabelText(/^Filtrar por situação/), 'INCLUIDO')
    expect(await screen.findByText('Nenhum candidato com essa situação.')).toBeInTheDocument()
  })

  it('navega entre as páginas', async () => {
    listar.mockResolvedValue(pagina([candidato('CAND-001')], { totalElementos: 12, totalPaginas: 2 }))
    renderizarAba(<AbaCandidatos />, { perfil: 'COORDENADOR', estudo: estudoEmRecrutamento })
    await screen.findByText('CAND-001')

    expect(screen.getByText(/Página 1 de 2 · 12 candidatos/)).toBeInTheDocument()
    expect(screen.getByRole('button', { name: 'Anterior' })).toBeDisabled()
    await userEvent.click(screen.getByRole('button', { name: 'Próxima' }))

    expect(listar).toHaveBeenLastCalledWith(estudoBase.id, { situacao: undefined, pagina: 1, tamanho: 10 })
  })

  it('valida o cadastro: nome obrigatório e nascimento no passado', async () => {
    renderizarAba(<AbaCandidatos />, { perfil: 'COORDENADOR', estudo: estudoEmRecrutamento })
    await screen.findByText('CAND-001')

    await userEvent.click(screen.getByRole('button', { name: 'Cadastrar candidato' }))
    expect(screen.getByText('O nome fictício é obrigatório')).toBeInTheDocument()
    expect(screen.getByText('A data de nascimento é obrigatória')).toBeInTheDocument()

    await preencherEEnviar('J. S.', '2999-01-01')
    expect(screen.getByText('A data de nascimento deve estar no passado')).toBeInTheDocument()
    expect(criar).not.toHaveBeenCalled()
  })

  it('cadastra o candidato, mostra o código gerado e recarrega a lista', async () => {
    criar.mockResolvedValue(candidato('CAND-005'))
    renderizarAba(<AbaCandidatos />, { perfil: 'COORDENADOR', estudo: estudoEmRecrutamento })
    await screen.findByText('CAND-001')

    await preencherEEnviar('  J. S.  ', '1990-04-12')

    expect(criar).toHaveBeenCalledWith(estudoBase.id, { nomeFicticio: 'J. S.', dataNascimento: '1990-04-12', contato: undefined })
    expect(await screen.findByText('Candidato CAND-005 cadastrado.')).toBeInTheDocument()
    expect(listar).toHaveBeenCalledTimes(2)
    expect(screen.getByLabelText(/^Nome fictício/)).toHaveValue('')
  })

  it('mostra o erro de duplicidade vindo do servidor', async () => {
    criar.mockRejectedValue(erroHttp(409, 'Já existe um candidato com esse nome fictício e data de nascimento neste estudo', 'CANDIDATO_DUPLICADO'))
    renderizarAba(<AbaCandidatos />, { perfil: 'COORDENADOR', estudo: estudoEmRecrutamento })
    await screen.findByText('CAND-001')

    await preencherEEnviar('J. S.', '1990-04-12')

    expect(await screen.findByRole('alert')).toHaveTextContent('Já existe um candidato com esse nome fictício')
  })

  it('desabilita o cadastro, explicando o motivo, quando o estudo não está em recrutamento', async () => {
    renderizarAba(<AbaCandidatos />, { perfil: 'COORDENADOR', estudo: { ...estudoBase, status: 'PLANEJAMENTO' } })
    await screen.findByText('CAND-001')

    expect(screen.getByRole('button', { name: 'Cadastrar candidato' })).toBeDisabled()
    expect(screen.getByRole('status')).toHaveTextContent('só podem ser cadastrados com o estudo em recrutamento')
  })

  it('não mostra o cadastro ao pesquisador', async () => {
    renderizarAba(<AbaCandidatos />, { perfil: 'PESQUISADOR', estudo: estudoEmRecrutamento })

    expect(await screen.findByText('CAND-001')).toBeInTheDocument()
    expect(screen.queryByRole('button', { name: 'Cadastrar candidato' })).not.toBeInTheDocument()
    expect(screen.getByText(/apenas para consulta dos candidatos/)).toBeInTheDocument()
  })

  it('mostra o erro de carregamento', async () => {
    listar.mockRejectedValue(erroHttp(403, 'Acesso negado para o seu perfil'))
    renderizarAba(<AbaCandidatos />, { perfil: 'COORDENADOR', estudo: estudoEmRecrutamento })

    expect(await screen.findByRole('alert')).toHaveTextContent('Acesso negado para o seu perfil')
  })
})
