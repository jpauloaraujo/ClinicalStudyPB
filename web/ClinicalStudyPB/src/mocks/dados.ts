import type {
  AvaliacaoResponse,
  CandidatoResponse,
  ConsentimentoResponse,
  CriterioResponse,
  HistoricoStatusResponse,
  ParticipacaoResponse,
  VisitaResponse,
} from '../api/tipos'
import type { Estudo } from '../types/estudo'
import type { PerfilUsuario } from '../types/usuario'

// Dados fictícios em memória usados pelo Mock Service Worker (VITE_USE_MOCKS=true) e pelos testes.
// Os identificadores seguem o formato do seed do backend (estudos aaaaaaaa... e bbbbbbbb...).

export const ESTUDO_PLANEJAMENTO = 'aaaaaaaa-aaaa-4aaa-8aaa-aaaaaaaaaaaa'
export const ESTUDO_RECRUTAMENTO = 'bbbbbbbb-bbbb-4bbb-8bbb-bbbbbbbbbbbb'

const RESPONSAVEL = {
  id: '22222222-2222-4222-8222-222222222222',
  nome: 'Pesquisador Fictício',
  email: 'pesquisador@clinicalstudy.test',
  perfil: 'PESQUISADOR',
  ativo: true,
}

export type EstadoMock = {
  estudos: Estudo[]
  criterios: CriterioResponse[]
  visitas: VisitaResponse[]
  candidatos: CandidatoResponse[]
  avaliacoes: AvaliacaoResponse[]
  consentimentos: ConsentimentoResponse[]
  participantes: ParticipacaoResponse[]
  historico: HistoricoStatusResponse[]
  sequencia: number
}

export function criarEstadoInicial(): EstadoMock {
  const estudos: Estudo[] = [
    {
      id: ESTUDO_PLANEJAMENTO,
      titulo: 'Estudo de Hipertensão (fictício) — Planejamento',
      objetivo: 'Avaliar a adesão ao protocolo de acompanhamento ambulatorial simplificado.',
      responsavel: RESPONSAVEL,
      dataInicio: '2026-11-01',
      dataFim: '2027-05-01',
      limiteParticipantes: 50,
      status: 'PLANEJAMENTO',
    },
    {
      id: ESTUDO_RECRUTAMENTO,
      titulo: 'Estudo de Diabetes (fictício) — Recrutamento',
      objetivo: 'Acompanhar o controle glicêmico de adultos com diabetes tipo 2.',
      responsavel: RESPONSAVEL,
      dataInicio: '2026-10-01',
      dataFim: '2027-04-01',
      limiteParticipantes: 2,
      status: 'RECRUTAMENTO',
    },
  ]

  const criterios: CriterioResponse[] = [
    { id: 'c0000001-0000-4000-8000-000000000001', estudoId: ESTUDO_PLANEJAMENTO, tipo: 'INCLUSAO', titulo: 'Idade adulta', descricao: 'Candidato com 18 anos ou mais', peso: 1, determinante: true },
    { id: 'c0000001-0000-4000-8000-000000000002', estudoId: ESTUDO_PLANEJAMENTO, tipo: 'EXCLUSAO', titulo: 'Gestante', descricao: 'Candidata em gestação confirmada', peso: null, determinante: null },
    { id: 'c0000002-0000-4000-8000-000000000001', estudoId: ESTUDO_RECRUTAMENTO, tipo: 'INCLUSAO', titulo: 'Diabetes tipo 2', descricao: 'Diagnóstico confirmado de diabetes tipo 2', peso: 2, determinante: true },
    { id: 'c0000002-0000-4000-8000-000000000002', estudoId: ESTUDO_RECRUTAMENTO, tipo: 'INCLUSAO', titulo: 'Atividade física regular', descricao: 'Pratica atividade física ao menos duas vezes por semana', peso: 1, determinante: false },
    { id: 'c0000002-0000-4000-8000-000000000003', estudoId: ESTUDO_RECRUTAMENTO, tipo: 'EXCLUSAO', titulo: 'Gestante', descricao: 'Candidata em gestação confirmada', peso: null, determinante: null },
  ]

  const visitas: VisitaResponse[] = [
    {
      id: 'd0000001-0000-4000-8000-000000000001', estudoId: ESTUDO_PLANEJAMENTO, nome: 'Triagem', diaPrevisto: 0, tolerancia: 0, ordem: 1,
      acoes: [{ id: 'a0000001-0000-4000-8000-000000000001', visitaId: 'd0000001-0000-4000-8000-000000000001', nome: 'Aferição de pressão', descricao: null, categoria: 'AVALIACAO_CLINICA' }],
    },
    { id: 'd0000002-0000-4000-8000-000000000001', estudoId: ESTUDO_RECRUTAMENTO, nome: 'Visita inicial', diaPrevisto: 0, tolerancia: 0, ordem: 1, acoes: [] },
    { id: 'd0000002-0000-4000-8000-000000000002', estudoId: ESTUDO_RECRUTAMENTO, nome: 'Retorno mensal', diaPrevisto: 30, tolerancia: 5, ordem: 2, acoes: [] },
  ]

  const candidatos: CandidatoResponse[] = [
    { id: 'e0000002-0000-4000-8000-000000000001', estudoId: ESTUDO_RECRUTAMENTO, codigo: 'CAND-001', nomeFicticio: 'J. S.', dataNascimento: '1985-04-12', contato: null, usuarioId: null, situacao: 'ELEGIVEL', resultadoAvaliacao: 'APROVADO', dataAvaliacao: '2026-10-28T10:00:00', consentimentoRegistrado: false, dataConsentimento: null },
    { id: 'e0000002-0000-4000-8000-000000000002', estudoId: ESTUDO_RECRUTAMENTO, codigo: 'CAND-002', nomeFicticio: 'M. L.', dataNascimento: '1992-09-03', contato: 'm.l@example.test', usuarioId: null, situacao: 'CADASTRADO', resultadoAvaliacao: null, dataAvaliacao: null, consentimentoRegistrado: false, dataConsentimento: null },
    { id: 'e0000002-0000-4000-8000-000000000003', estudoId: ESTUDO_RECRUTAMENTO, codigo: 'CAND-003', nomeFicticio: 'A. B.', dataNascimento: '1978-01-20', contato: null, usuarioId: null, situacao: 'INCLUIDO', resultadoAvaliacao: 'APROVADO', dataAvaliacao: '2026-10-27T09:00:00', consentimentoRegistrado: true, dataConsentimento: '2026-10-27T09:30:00' },
  ]

  const avaliacoes: AvaliacaoResponse[] = [
    {
      id: 'f0000001-0000-4000-8000-000000000001', candidatoId: 'e0000002-0000-4000-8000-000000000001', dataAvaliacao: '2026-10-28T10:00:00',
      avaliador: 'Coordenador Fictício', resultado: 'APROVADO', pontuacao: 3, vigente: true, criteriosReprovadores: [],
      respostas: [
        { criterioId: 'c0000002-0000-4000-8000-000000000001', tipo: 'INCLUSAO', titulo: 'Diabetes tipo 2', aplica: true, observacao: 'Exame de glicemia anexado' },
        { criterioId: 'c0000002-0000-4000-8000-000000000002', tipo: 'INCLUSAO', titulo: 'Atividade física regular', aplica: true, observacao: null },
        { criterioId: 'c0000002-0000-4000-8000-000000000003', tipo: 'EXCLUSAO', titulo: 'Gestante', aplica: false, observacao: null },
      ],
    },
  ]

  const participantes: ParticipacaoResponse[] = [
    { id: 'b0000002-0000-4000-8000-000000000001', candidatoId: 'e0000002-0000-4000-8000-000000000003', codigo: 'CAND-003', nomeFicticio: 'A. B.', estudoId: ESTUDO_RECRUTAMENTO, dataInclusao: '2026-10-27', incluidoPor: 'Coordenador Fictício' },
  ]

  const consentimentos: ConsentimentoResponse[] = [
    { id: 'g0000002-0000-4000-8000-000000000003', candidatoId: 'e0000002-0000-4000-8000-000000000003', dataHora: '2026-10-27T09:30:00', responsavel: 'Coordenador Fictício', versaoTermo: 'TCLE-SIMULADO v1' },
  ]

  const historico: HistoricoStatusResponse[] = [
    { id: 'h0000002-0000-4000-8000-000000000001', estudoId: ESTUDO_RECRUTAMENTO, estadoAnterior: 'PLANEJAMENTO', estadoNovo: 'RECRUTAMENTO', usuario: 'Pesquisador Fictício', dataHora: '2026-10-20T08:00:00' },
  ]

  return { estudos, criterios, visitas, candidatos, avaliacoes, consentimentos, participantes, historico, sequencia: 100 }
}

export const mock: { estado: EstadoMock } = { estado: criarEstadoInicial() }

const base64Url = (texto: string) => btoa(texto).replace(/\+/g, '-').replace(/\//g, '_').replace(/=+$/, '')

/**
 * Token fictício com o formato de um JWT (cabeçalho.carga.assinatura) e validade longa, para a web
 * restaurar a sessão ao recarregar como faz com o token real. A carga traz o perfil na claim "role".
 */
export function tokenMock(perfil: PerfilUsuario): string {
  const carga = { sub: `mock-${perfil.toLowerCase()}`, role: perfil, exp: Math.floor(Date.now() / 1000) + 24 * 3600 }
  return `${base64Url(JSON.stringify({ alg: 'none', typ: 'JWT' }))}.${base64Url(JSON.stringify(carga))}.mock`
}

/** Perfil guardado em um token gerado por {@link tokenMock}; null se não for um deles. */
export function perfilDoToken(token: string): PerfilUsuario | null {
  try {
    const carga = token.split('.')[1].replace(/-/g, '+').replace(/_/g, '/')
    return (JSON.parse(atob(carga)) as { role?: PerfilUsuario }).role ?? null
  } catch {
    return null
  }
}

/** Volta os dados fictícios ao estado inicial (usado entre os testes). */
export function resetarMocks() {
  mock.estado = criarEstadoInicial()
}

/** Identificador novo e único dentro da sessão do mock. */
export function novoId(prefixo: string): string {
  mock.estado.sequencia += 1
  const n = String(mock.estado.sequencia).padStart(12, '0')
  return `${prefixo}0000000-0000-4000-8000-${n}`
}
