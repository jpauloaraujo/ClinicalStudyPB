import { screen, within } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { beforeEach, describe, expect, it, vi } from 'vitest'
import { avaliacoesApi } from '../../../../api/avaliacoes'
import { candidatosApi } from '../../../../api/candidatos'
import { consentimentoApi } from '../../../../api/consentimento'
import { criteriosApi } from '../../../../api/criterios'
import { participantesApi } from '../../../../api/participantes'
import type { AvaliacaoResponse, CandidatoResponse, CriterioResponse } from '../../../../api/tipos'
import { erroHttp, estudoBase, renderizarAba } from '../../../../test/ambiente'
import type { Estudo } from '../../../../types/estudo'
import { FichaCandidatoPage } from './FichaCandidatoPage'

vi.mock('../../../../api/candidatos', () => ({
  candidatosApi: { listar: vi.fn(), buscar: vi.fn(), criar: vi.fn(), atualizar: vi.fn() },
}))
vi.mock('../../../../api/criterios', () => ({
  criteriosApi: { listar: vi.fn(), criar: vi.fn(), atualizar: vi.fn(), excluir: vi.fn() },
}))
vi.mock('../../../../api/avaliacoes', () => ({ avaliacoesApi: { historico: vi.fn(), avaliar: vi.fn() } }))
vi.mock('../../../../api/consentimento', () => ({ consentimentoApi: { buscar: vi.fn(), registrar: vi.fn() } }))
vi.mock('../../../../api/participantes', () => ({ participantesApi: { listar: vi.fn(), vagas: vi.fn(), incluir: vi.fn() } }))

const buscar = vi.mocked(candidatosApi.buscar)
const listarCriterios = vi.mocked(criteriosApi.listar)
const historico = vi.mocked(avaliacoesApi.historico)
const avaliar = vi.mocked(avaliacoesApi.avaliar)
const buscarConsentimento = vi.mocked(consentimentoApi.buscar)
const vagas = vi.mocked(participantesApi.vagas)

const estudo: Estudo = { ...estudoBase, status: 'RECRUTAMENTO' }

const candidato: CandidatoResponse = {
  id: 'cand-1', estudoId: estudo.id, codigo: 'CAND-001', nomeFicticio: 'J. S.', dataNascimento: '1990-04-12',
  contato: 'js@example.test', usuarioId: null, situacao: 'CADASTRADO', resultadoAvaliacao: null, dataAvaliacao: null,
  consentimentoRegistrado: false, dataConsentimento: null,
}

const idade: CriterioResponse = {
  id: 'c-1', estudoId: estudo.id, tipo: 'INCLUSAO', titulo: 'Idade adulta', descricao: 'Maior de 18 anos', peso: 1, determinante: true,
}
const gestante: CriterioResponse = {
  id: 'c-2', estudoId: estudo.id, tipo: 'EXCLUSAO', titulo: 'Gestante', descricao: 'Em gestação', peso: null, determinante: null,
}

function avaliacao(parcial: Partial<AvaliacaoResponse> = {}): AvaliacaoResponse {
  return {
    id: 'av-1', candidatoId: candidato.id, dataAvaliacao: '2026-11-02T10:00:00', avaliador: 'Coordenador Fictício',
    resultado: 'APROVADO', pontuacao: 1, vigente: true, respostas: [], criteriosReprovadores: [], ...parcial,
  }
}

function renderizar(estudoDaFicha = estudo) {
  renderizarAba(<FichaCandidatoPage />, {
    perfil: 'COORDENADOR',
    estudo: estudoDaFicha,
    rota: 'candidatos/:candidatoId',
    caminho: `/candidatos/${candidato.id}`,
  })
}

async function responder(criterio: string, resposta: 'Se aplica' | 'Não se aplica') {
  const grupo = await screen.findByRole('radiogroup', { name: `Resposta para ${criterio}` })
  await userEvent.click(within(grupo).getByLabelText(resposta))
}

describe('FichaCandidatoPage e SecaoAvaliacao (T52 e T55)', () => {
  beforeEach(() => {
    vi.clearAllMocks()
    buscar.mockResolvedValue(candidato)
    listarCriterios.mockResolvedValue([idade, gestante])
    historico.mockResolvedValue([])
    buscarConsentimento.mockRejectedValue(erroHttp(404, 'Consentimento não registrado para o candidato'))
    vagas.mockResolvedValue({ incluidos: 0, limite: 30, restantes: 30 })
  })

  it('mostra os dados do candidato e as três seções do funil (avaliação, consentimento e inclusão)', async () => {
    renderizar()

    expect(await screen.findByRole('heading', { level: 2, name: 'CAND-001 · J. S.' })).toBeInTheDocument()
    const dados = screen.getByRole('region', { name: 'Dados do candidato' })
    expect(within(dados).getByText('Cadastrado')).toBeInTheDocument()
    expect(within(dados).getByText('12/04/1990')).toBeInTheDocument()
    expect(within(dados).getByText('js@example.test')).toBeInTheDocument()
    expect(screen.getByRole('region', { name: 'Avaliação de elegibilidade' })).toBeInTheDocument()
    expect(await screen.findByRole('button', { name: 'Registrar' })).toBeInTheDocument()
    expect(await screen.findByRole('button', { name: 'Incluir no estudo' })).toBeDisabled()
    expect(buscar).toHaveBeenCalledWith('cand-1')
  })

  it('mostra "não encontrado" para candidato inexistente ou de outro estudo', async () => {
    buscar.mockRejectedValue(erroHttp(404, 'Candidato não encontrado: cand-1'))
    renderizar()
    expect(await screen.findByRole('heading', { level: 2, name: 'Candidato não encontrado' })).toBeInTheDocument()
    expect(screen.getByRole('link', { name: 'Voltar para candidatos' })).toBeInTheDocument()
  })

  it('trata como não encontrado um candidato de outro estudo', async () => {
    buscar.mockResolvedValue({ ...candidato, estudoId: 'outro-estudo' })
    renderizar()

    expect(await screen.findByRole('heading', { level: 2, name: 'Candidato não encontrado' })).toBeInTheDocument()
  })

  it('agrupa os critérios, explica a exclusão e deixa o Salvar desabilitado até responder todos', async () => {
    renderizar()
    await screen.findByRole('radiogroup', { name: 'Resposta para Idade adulta' })

    expect(within(screen.getByRole('region', { name: 'Critérios de inclusão' })).getByText('Idade adulta')).toBeInTheDocument()
    const exclusao = screen.getByRole('region', { name: 'Critérios de exclusão' })
    expect(within(exclusao).getByText('Gestante')).toBeInTheDocument()
    expect(exclusao).toHaveTextContent('“se aplica” reprova o candidato')
    const salvar = screen.getByRole('button', { name: 'Salvar avaliação' })
    expect(salvar).toBeDisabled()

    await responder('Idade adulta', 'Se aplica')
    expect(salvar).toBeDisabled()
    await responder('Gestante', 'Não se aplica')
    expect(salvar).toBeEnabled()
  })

  it('envia uma resposta por critério e recarrega o histórico e a ficha', async () => {
    avaliar.mockResolvedValue(avaliacao())
    renderizar()
    await responder('Idade adulta', 'Se aplica')
    await responder('Gestante', 'Não se aplica')
    await userEvent.type(screen.getByLabelText(/Observação \(Idade adulta\)/), 'Documento conferido')

    await userEvent.click(screen.getByRole('button', { name: 'Salvar avaliação' }))

    expect(avaliar).toHaveBeenCalledWith('cand-1', {
      respostas: [
        { criterioId: 'c-1', aplica: true, observacao: 'Documento conferido' },
        { criterioId: 'c-2', aplica: false, observacao: undefined },
      ],
    })
    expect(historico).toHaveBeenCalledTimes(2)
    expect(buscar).toHaveBeenCalledTimes(2)
  })

  it('destaca o resultado vigente aprovado', async () => {
    historico.mockResolvedValue([avaliacao({ pontuacao: 1 })])
    renderizar()

    const resultado = await screen.findByRole('status', { name: '' })
    expect(resultado).toHaveTextContent('Candidato elegível')
    expect(resultado).toHaveClass('resultado--aprovado')
  })

  it('mostra os critérios que levaram à inelegibilidade', async () => {
    historico.mockResolvedValue([
      avaliacao({
        resultado: 'REPROVADO', pontuacao: 0,
        criteriosReprovadores: [{ criterioId: 'c-2', tipo: 'EXCLUSAO', titulo: 'Gestante' }],
      }),
    ])
    renderizar()

    const resultado = await screen.findByText('Candidato não elegível')
    expect(resultado.closest('.resultado')).toHaveClass('resultado--reprovado')
    expect(screen.getAllByText(/Gestante/).some((e) => e.textContent?.includes('exclusão aplicada'))).toBe(true)
  })

  it('lista o histórico de avaliações marcando a vigente', async () => {
    historico.mockResolvedValue([
      avaliacao({ id: 'av-2', dataAvaliacao: '2026-11-03T09:00:00' }),
      avaliacao({ id: 'av-1', resultado: 'REPROVADO', vigente: false }),
    ])
    renderizar()

    const lista = await screen.findByRole('list', { name: 'Histórico de avaliações' })
    const itens = within(lista).getAllByRole('listitem')
    expect(itens).toHaveLength(2)
    expect(within(itens[0]).getByText('Vigente')).toBeInTheDocument()
    expect(within(itens[0]).getByText('Aprovado')).toBeInTheDocument()
    expect(within(itens[1]).getByText('Reprovado')).toBeInTheDocument()
    expect(within(itens[1]).queryByText('Vigente')).not.toBeInTheDocument()
  })

  it('mostra o erro 409 da API sem perder as respostas', async () => {
    avaliar.mockRejectedValue(erroHttp(409, 'Esta operação exige que o estudo esteja em recrutamento', 'ESTUDO_FORA_DE_RECRUTAMENTO'))
    renderizar()
    await responder('Idade adulta', 'Se aplica')
    await responder('Gestante', 'Não se aplica')

    await userEvent.click(screen.getByRole('button', { name: 'Salvar avaliação' }))

    expect(await screen.findByRole('alert')).toHaveTextContent('exige que o estudo esteja em recrutamento')
    expect(screen.getByRole('button', { name: 'Salvar avaliação' })).toBeEnabled()
  })

  it('mostra o erro 400 da API', async () => {
    avaliar.mockRejectedValue(erroHttp(400, 'A avaliação deve responder a todos os critérios do estudo'))
    renderizar()
    await responder('Idade adulta', 'Se aplica')
    await responder('Gestante', 'Não se aplica')

    await userEvent.click(screen.getByRole('button', { name: 'Salvar avaliação' }))

    expect(await screen.findByRole('alert')).toHaveTextContent('responder a todos os critérios')
  })

  it('bloqueia a avaliação quando o estudo não está em recrutamento', async () => {
    renderizar({ ...estudoBase, status: 'EXECUCAO' })
    await screen.findByRole('radiogroup', { name: 'Resposta para Idade adulta' })

    expect(screen.getByText('A avaliação só pode ser registrada com o estudo em recrutamento.')).toBeInTheDocument()
    expect(screen.getByLabelText('Se aplica', { selector: '[name="criterio-c-1"]' })).toBeDisabled()
    expect(screen.getByRole('button', { name: 'Salvar avaliação' })).toBeDisabled()
  })

  it('avisa quando o estudo não tem critérios', async () => {
    listarCriterios.mockResolvedValue([])
    renderizar()

    expect(await screen.findByText('Este estudo ainda não tem critérios para avaliar.')).toBeInTheDocument()
    expect(screen.queryByRole('button', { name: 'Salvar avaliação' })).not.toBeInTheDocument()
  })
})
