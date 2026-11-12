import type { components } from './schema'
import type { components as componentsSprint3 } from './schema-sprint3'

// Nomes curtos para os tipos gerados do contrato (docs/api/openapi-sprint2.yaml).
// Nada aqui é escrito à mão: ao mudar o contrato, rode `npm run api:tipos`.
type Esquemas = components['schemas']
// Sprint 3 (docs/api/openapi-sprint3.yaml)
type EsquemasSprint3 = componentsSprint3['schemas']

export type TipoCriterio = Esquemas['TipoCriterio']
export type CategoriaAcao = Esquemas['CategoriaAcao']
export type SituacaoCandidato = Esquemas['SituacaoCandidato']
export type ResultadoAvaliacao = Esquemas['ResultadoAvaliacao']

export type CriterioRequest = Esquemas['CriterioRequest']
export type CriterioResponse = Esquemas['CriterioResponse']
export type VisitaRequest = Esquemas['VisitaRequest']
export type VisitaResponse = Esquemas['VisitaResponse']
export type AcaoRequest = Esquemas['AcaoRequest']
export type AcaoResponse = Esquemas['AcaoResponse']
export type CandidatoRequest = Esquemas['CandidatoRequest']
export type CandidatoResponse = Esquemas['CandidatoResponse']
export type PaginaCandidatos = Esquemas['PaginaCandidatos']
export type AvaliacaoRequest = Esquemas['AvaliacaoRequest']
export type RespostaAvaliacaoRequest = Esquemas['RespostaAvaliacaoRequest']
export type AvaliacaoResponse = Esquemas['AvaliacaoResponse']
export type ConsentimentoRequest = Esquemas['ConsentimentoRequest']
export type ConsentimentoResponse = Esquemas['ConsentimentoResponse']
export type ParticipacaoResponse = Esquemas['ParticipacaoResponse']
export type VagasResponse = Esquemas['VagasResponse']
export type HistoricoStatusResponse = Esquemas['HistoricoStatusResponse']

// Histórico do participante (Sprint 3, T95)
export type SituacaoNoHistorico = EsquemasSprint3['SituacaoNoHistorico']
export type HistoricoParticipanteResponse = EsquemasSprint3['HistoricoParticipanteResponse']
export type ParticipanteDoHistorico = EsquemasSprint3['ParticipanteDoHistorico']
export type ResumoDoHistorico = EsquemasSprint3['ResumoDoHistorico']
export type VisitaDoHistorico = EsquemasSprint3['VisitaDoHistorico']
export type ResultadoDoHistorico = EsquemasSprint3['ResultadoDoHistorico']

export const categoriasAcao: CategoriaAcao[] = [
  'AVALIACAO_CLINICA',
  'EXAME_LABORATORIAL',
  'EXAME_IMAGEM',
  'QUESTIONARIO',
  'MEDICACAO',
  'OUTROS',
]

export const rotulosCategoria: Record<CategoriaAcao, string> = {
  AVALIACAO_CLINICA: 'Avaliação clínica',
  EXAME_LABORATORIAL: 'Exame laboratorial',
  EXAME_IMAGEM: 'Exame de imagem',
  QUESTIONARIO: 'Questionário',
  MEDICACAO: 'Medicação',
  OUTROS: 'Outros',
}

export const situacoesCandidato: SituacaoCandidato[] = [
  'CADASTRADO',
  'EM_AVALIACAO',
  'ELEGIVEL',
  'NAO_ELEGIVEL',
  'CONSENTIDO',
  'INCLUIDO',
]

export const rotulosSituacao: Record<SituacaoCandidato, string> = {
  CADASTRADO: 'Cadastrado',
  EM_AVALIACAO: 'Em avaliação',
  ELEGIVEL: 'Elegível',
  NAO_ELEGIVEL: 'Não elegível',
  CONSENTIDO: 'Consentido',
  INCLUIDO: 'Incluído',
}
