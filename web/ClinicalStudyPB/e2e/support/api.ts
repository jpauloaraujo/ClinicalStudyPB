import fs from 'node:fs/promises'
import { expect, request, type APIRequestContext } from '@playwright/test'
import { urlApi, type PerfilE2E } from './env'
import { caminhoSessao, CHAVE_TOKEN } from './sessao'

// Helpers que PREPARAM estado pela API usando a sessão de cada perfil salva no setup (T65). Servem para montar
// o cenário de um teste (por exemplo, um estudo já em recrutamento), nunca para substituir o passo que o teste
// quer provar pela interface.

type Corpo = Record<string, unknown>

async function lerToken(perfil: PerfilE2E): Promise<string> {
  const estado = JSON.parse(await fs.readFile(caminhoSessao(perfil), 'utf-8')) as {
    origins: { localStorage: { name: string; value: string }[] }[]
  }
  const token = estado.origins.flatMap((o) => o.localStorage).find((i) => i.name === CHAVE_TOKEN)?.value
  if (!token) {
    throw new Error(`Sessão salva de ${perfil} sem token; rode o projeto "setup" (npm run test:e2e).`)
  }
  return token
}

export class ApiE2E {
  private readonly contexto: APIRequestContext
  readonly usuarioId: string

  private constructor(contexto: APIRequestContext, usuarioId: string) {
    this.contexto = contexto
    this.usuarioId = usuarioId
  }

  static async como(perfil: PerfilE2E): Promise<ApiE2E> {
    const contexto = await request.newContext({
      baseURL: urlApi(),
      extraHTTPHeaders: { Authorization: `Bearer ${await lerToken(perfil)}` },
    })
    const eu = await contexto.get('/api/auth/me')
    expect(eu.ok(), `não foi possível identificar o usuário ${perfil} (HTTP ${eu.status()})`).toBeTruthy()
    return new ApiE2E(contexto, ((await eu.json()) as { id: string }).id)
  }

  async encerrar() {
    await this.contexto.dispose()
  }

  private async chamar(metodo: 'post' | 'get', url: string, dados?: Corpo, esperado = [200, 201]) {
    const resposta = metodo === 'post' ? await this.contexto.post(url, { data: dados }) : await this.contexto.get(url)
    expect(esperado, `${metodo.toUpperCase()} ${url} respondeu HTTP ${resposta.status()}: ${await resposta.text()}`)
      .toContain(resposta.status())
    return resposta
  }

  async criarEstudo(titulo: string, limite = 5): Promise<string> {
    const resposta = await this.chamar('post', '/api/estudos', {
      titulo,
      objetivo: 'Objetivo fictício criado pelo teste E2E.',
      responsavelId: this.usuarioId,
      limiteParticipantes: limite,
      status: 'PLANEJAMENTO',
    })
    return ((await resposta.json()) as { id: string }).id
  }

  async criarCriterio(estudoId: string, tipo: 'INCLUSAO' | 'EXCLUSAO', titulo: string, descricao: string) {
    const resposta = await this.chamar('post', `/api/estudos/${estudoId}/criterios`, { tipo, titulo, descricao })
    return ((await resposta.json()) as { id: string }).id
  }

  async criarVisita(estudoId: string, nome: string, diaPrevisto: number, tolerancia = 0) {
    const resposta = await this.chamar('post', `/api/estudos/${estudoId}/visitas`, { nome, diaPrevisto, tolerancia })
    return ((await resposta.json()) as { id: string }).id
  }

  async avancar(estudoId: string, destino: 'RECRUTAMENTO' | 'EXECUCAO' | 'ENCERRADO') {
    await this.chamar('post', `/api/estudos/${estudoId}/transicoes`, { destino })
  }

  /** Estudo com um critério de inclusão, um de exclusão e uma visita, já em recrutamento. */
  async estudoEmRecrutamento(titulo: string, limite = 5): Promise<string> {
    const estudoId = await this.criarEstudo(titulo, limite)
    await this.criarCriterio(estudoId, 'INCLUSAO', 'Idade adulta', 'Candidato com 18 anos ou mais')
    await this.criarCriterio(estudoId, 'EXCLUSAO', 'Gestante', 'Candidata em gestação confirmada')
    await this.criarVisita(estudoId, 'Triagem', 0)
    await this.avancar(estudoId, 'RECRUTAMENTO')
    return estudoId
  }

  async cadastrarCandidato(estudoId: string, nomeFicticio: string): Promise<string> {
    const resposta = await this.chamar('post', `/api/estudos/${estudoId}/candidatos`, {
      nomeFicticio,
      dataNascimento: '1990-04-12',
    })
    return ((await resposta.json()) as { id: string }).id
  }

  /** Avalia o candidato como elegível (inclusões "se aplica", exclusões "não se aplica") ou como não elegível. */
  async avaliar(estudoId: string, candidatoId: string, elegivel = true) {
    const criterios = (await (await this.chamar('get', `/api/estudos/${estudoId}/criterios`)).json()) as {
      id: string
      tipo: string
    }[]
    const respostas = criterios.map((c) => ({
      criterioId: c.id,
      aplica: elegivel ? c.tipo === 'INCLUSAO' : c.tipo === 'EXCLUSAO',
    }))
    await this.chamar('post', `/api/candidatos/${candidatoId}/avaliacoes`, { respostas })
  }

  async consentir(candidatoId: string) {
    await this.chamar('post', `/api/candidatos/${candidatoId}/consentimento`, {})
  }

  async incluir(estudoId: string, candidatoId: string) {
    await this.chamar('post', `/api/estudos/${estudoId}/participantes`, { candidatoId })
  }

  /** (Como coordenador) candidato pronto para ser incluído: cadastrado, elegível e com consentimento. */
  async candidatoPronto(estudoId: string, nomeFicticio: string): Promise<string> {
    const id = await this.cadastrarCandidato(estudoId, nomeFicticio)
    await this.avaliar(estudoId, id, true)
    await this.consentir(id)
    return id
  }
}
