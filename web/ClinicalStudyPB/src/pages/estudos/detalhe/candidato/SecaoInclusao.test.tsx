import { screen, within } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { beforeEach, describe, expect, it, vi } from 'vitest'
import { participantesApi } from '../../../../api/participantes'
import type { CandidatoResponse } from '../../../../api/tipos'
import { erroHttp, estudoBase, renderizarAba } from '../../../../test/ambiente'
import type { Estudo } from '../../../../types/estudo'
import type { PerfilUsuario } from '../../../../types/usuario'
import { SecaoInclusao } from './SecaoInclusao'

vi.mock('../../../../api/participantes', () => ({ participantesApi: { listar: vi.fn(), vagas: vi.fn(), incluir: vi.fn() } }))

const vagas = vi.mocked(participantesApi.vagas)
const incluir = vi.mocked(participantesApi.incluir)

const estudo: Estudo = { ...estudoBase, status: 'RECRUTAMENTO', limiteParticipantes: 3 }
const pronto: CandidatoResponse = {
  id: 'cand-1', estudoId: estudo.id, codigo: 'CAND-001', nomeFicticio: 'J. S.', dataNascimento: '1990-04-12',
  contato: null, usuarioId: null, situacao: 'CONSENTIDO', resultadoAvaliacao: 'APROVADO', dataAvaliacao: '2026-11-03T10:00:00',
  consentimentoRegistrado: true, dataConsentimento: '2026-11-03T10:30:00',
}

const aoIncluir = vi.fn()

function renderizar(opcoes: { perfil?: PerfilUsuario; estudoDaFicha?: Estudo; candidatoDaFicha?: CandidatoResponse } = {}) {
  renderizarAba(<SecaoInclusao estudo={opcoes.estudoDaFicha ?? estudo} candidato={opcoes.candidatoDaFicha ?? pronto} aoIncluir={aoIncluir} />, {
    perfil: opcoes.perfil ?? 'COORDENADOR',
    estudo: opcoes.estudoDaFicha ?? estudo,
  })
}

const botao = () => screen.findByRole('button', { name: 'Incluir no estudo' })
const regiao = () => screen.getByRole('region', { name: 'Inclusão' })

describe('SecaoInclusao (T61)', () => {
  beforeEach(() => {
    vi.clearAllMocks()
    vagas.mockResolvedValue({ incluidos: 1, limite: 3, restantes: 2 })
  })

  it('com as quatro pré-condições atendidas mostra todas como atendidas e habilita o botão', async () => {
    renderizar()

    expect(await botao()).toBeEnabled()
    const lista = screen.getByRole('list', { name: 'Pré-condições da inclusão' })
    expect(within(lista).getAllByRole('listitem')).toHaveLength(4)
    expect(within(lista).getAllByText('Atendida')).toHaveLength(4)
    expect(within(lista).queryByText('Pendente')).not.toBeInTheDocument()
    expect(regiao()).toHaveTextContent('1 de 3 vagas ocupadas · 2 vagas restantes')
  })

  it.each([
    ['o estudo não está em recrutamento', { estudoDaFicha: { ...estudo, status: 'EXECUCAO' as const } }, /não está em recrutamento/],
    ['a avaliação não foi aprovada', { candidatoDaFicha: { ...pronto, situacao: 'NAO_ELEGIVEL' as const, resultadoAvaliacao: 'REPROVADO' as const } }, /avaliação de elegibilidade aprovada/],
    ['o candidato nunca foi avaliado', { candidatoDaFicha: { ...pronto, situacao: 'CADASTRADO' as const, resultadoAvaliacao: null } }, /avaliação de elegibilidade aprovada/],
    ['falta o consentimento', { candidatoDaFicha: { ...pronto, situacao: 'ELEGIVEL' as const, consentimentoRegistrado: false, dataConsentimento: null } }, /registrar o consentimento/],
  ])('mantém o botão desabilitado e explica o motivo quando %s', async (_caso, opcoes, motivo) => {
    renderizar(opcoes)

    expect(await botao()).toBeDisabled()
    expect(within(screen.getByRole('list', { name: 'Pré-condições da inclusão' })).getAllByText('Pendente')).toHaveLength(1)
    expect(regiao()).toHaveTextContent(motivo)
  })

  it('sem vaga disponível mostra "Vagas esgotadas" e desabilita o botão', async () => {
    vagas.mockResolvedValue({ incluidos: 3, limite: 3, restantes: 0 })
    renderizar()

    expect(await screen.findByText('Vagas esgotadas.')).toBeInTheDocument()
    expect(await botao()).toBeDisabled()
  })

  it('pede confirmação antes de incluir e só chama a API depois de confirmar', async () => {
    incluir.mockResolvedValue({
      id: 'p-1', candidatoId: pronto.id, codigo: 'CAND-001', nomeFicticio: 'J. S.', estudoId: estudo.id,
      dataInclusao: '2026-11-04', incluidoPor: 'Coordenadora Fictícia',
    })
    renderizar()
    await userEvent.click(await botao())

    const dialogo = screen.getByRole('dialog', { name: 'Incluir candidato no estudo?' })
    await userEvent.click(within(dialogo).getByRole('button', { name: 'Cancelar' }))
    expect(incluir).not.toHaveBeenCalled()

    await userEvent.click(await botao())
    await userEvent.click(within(screen.getByRole('dialog')).getByRole('button', { name: 'Confirmar' }))

    expect(incluir).toHaveBeenCalledWith(estudo.id, 'cand-1')
    expect(aoIncluir).toHaveBeenCalledTimes(1)
    expect(vagas).toHaveBeenCalledTimes(2)
  })

  it.each([
    ['VAGAS_ESGOTADAS', 'Já atingiu o limite', /Vagas esgotadas: o estudo já atingiu o limite de participantes/],
    ['CONSENTIMENTO_AUSENTE', 'sem consentimento', /Falta o consentimento do candidato/],
    ['CANDIDATO_NAO_ELEGIVEL', 'não elegível', /O candidato não está elegível/],
    ['CANDIDATO_JA_INCLUIDO', 'já incluído', /já foi incluído no estudo/],
    ['ESTUDO_FORA_DE_RECRUTAMENTO', 'fora de recrutamento', /não está em recrutamento: não é possível incluir/],
  ])('traduz o código %s em uma mensagem clara', async (codigo, mensagemDoServidor, esperado) => {
    incluir.mockRejectedValue(erroHttp(409, mensagemDoServidor, codigo))
    renderizar()
    await userEvent.click(await botao())
    await userEvent.click(within(screen.getByRole('dialog')).getByRole('button', { name: 'Confirmar' }))

    expect(await screen.findByRole('alert')).toHaveTextContent(esperado)
    expect(screen.queryByRole('dialog')).not.toBeInTheDocument()
    expect(aoIncluir).not.toHaveBeenCalled()
  })

  it('para um código desconhecido mostra a mensagem enviada pelo servidor', async () => {
    incluir.mockRejectedValue(erroHttp(409, 'Regra nova do servidor', 'REGRA_NOVA'))
    renderizar()
    await userEvent.click(await botao())
    await userEvent.click(within(screen.getByRole('dialog')).getByRole('button', { name: 'Confirmar' }))

    expect(await screen.findByRole('alert')).toHaveTextContent('Regra nova do servidor')
  })

  it('candidato já incluído mostra o aviso e não oferece o botão', async () => {
    renderizar({ candidatoDaFicha: { ...pronto, situacao: 'INCLUIDO' } })

    expect(await screen.findByText('Candidato incluído como participante do estudo.')).toBeInTheDocument()
    expect(screen.queryByRole('button', { name: 'Incluir no estudo' })).not.toBeInTheDocument()
  })

  it('fica somente leitura para quem não é coordenador', async () => {
    renderizar({ perfil: 'PESQUISADOR' })

    expect(await screen.findByText('Seu perfil tem acesso apenas para consulta da inclusão.')).toBeInTheDocument()
    expect(screen.queryByRole('button', { name: 'Incluir no estudo' })).not.toBeInTheDocument()
  })

  it('mostra o erro ao carregar as vagas e mantém o botão desabilitado', async () => {
    vagas.mockRejectedValue(erroHttp(500, 'Erro interno do servidor'))
    renderizar()

    expect(await screen.findByRole('alert')).toHaveTextContent('Erro interno do servidor')
    expect(await botao()).toBeDisabled()
  })
})
