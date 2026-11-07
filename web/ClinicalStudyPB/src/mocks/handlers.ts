import { http, HttpResponse } from 'msw'
import type {
  AcaoRequest,
  AvaliacaoRequest,
  AvaliacaoResponse,
  CandidatoRequest,
  CandidatoResponse,
  CriterioRequest,
  CriterioResponse,
  SituacaoCandidato,
  VisitaRequest,
  VisitaResponse,
} from '../api/tipos'
import type { Estudo, StatusEstudo } from '../types/estudo'
import type { PerfilUsuario } from '../types/usuario'
import { mock, novoId, perfilDoToken, tokenMock } from './dados'

// Respostas fictícias para cada endpoint do contrato (docs/api/openapi-sprint2.yaml), mais o login e a
// consulta de estudos, para o app inteiro rodar sem backend. Reproduzem as regras principais da API
// (estado exigido, duplicidade, validação, permissões), com o mesmo formato de erro.

const SEQUENCIA_ESTADOS: StatusEstudo[] = ['PLANEJAMENTO', 'RECRUTAMENTO', 'EXECUCAO', 'ENCERRADO']
const PERFIS_LEITURA: PerfilUsuario[] = ['PESQUISADOR', 'COORDENADOR']
const PERFIL_PLANEJAMENTO: PerfilUsuario[] = ['PESQUISADOR']
const PERFIL_RECRUTAMENTO: PerfilUsuario[] = ['COORDENADOR']

// A web chama a API em outra origem (porta 8081); no navegador o service worker responde antes da rede,
// mas no jsdom dos testes há pré-requisição (OPTIONS) e checagem de CORS, então o mock também as atende
const CORS = {
  'Access-Control-Allow-Origin': '*',
  'Access-Control-Allow-Methods': 'GET, POST, PUT, DELETE, OPTIONS',
  'Access-Control-Allow-Headers': 'Authorization, Content-Type, Accept',
}

const json = (corpo: unknown, status = 200) => HttpResponse.json(corpo as never, { status, headers: CORS })
const semConteudo = () => new HttpResponse(null, { status: 204, headers: CORS })
const erro = (status: number, mensagem: string, codigo?: string) => json({ erro: mensagem, codigo }, status)
const conflito = (codigo: string, mensagem: string) => erro(409, mensagem, codigo)
const naoEncontrado = (o: string, id: string) => erro(404, `${o} não encontrado: ${id}`)

function perfilDe(request: Request): PerfilUsuario | null {
  return perfilDoToken(request.headers.get('Authorization')?.replace('Bearer ', '') ?? '')
}

// Devolve a resposta de erro (401 ou 403) quando o perfil não pode, ou null quando pode
function negar(request: Request, permitidos: PerfilUsuario[]) {
  const perfil = perfilDe(request)
  if (!perfil) return erro(401, 'Não autenticado')
  if (!permitidos.includes(perfil)) return erro(403, 'Acesso negado para o seu perfil')
  return null
}

const estudoDe = (id: string) => mock.estado.estudos.find((e) => e.id === id)
const candidatoDe = (id: string) => mock.estado.candidatos.find((c) => c.id === id)

function validar(campo: string, mensagem: string) {
  return erro(400, `${campo}: ${mensagem}`)
}

function recalcularOrdem(estudoId: string) {
  mock.estado.visitas
    .filter((v) => v.estudoId === estudoId)
    .sort((a, b) => a.diaPrevisto - b.diaPrevisto)
    .forEach((v, i) => (v.ordem = i + 1))
}

function visitasOrdenadas(estudoId: string) {
  return mock.estado.visitas.filter((v) => v.estudoId === estudoId).sort((a, b) => a.ordem - b.ordem)
}

function resultadoDaAvaliacao(estudoId: string, respostas: AvaliacaoRequest['respostas']) {
  const criterios = mock.estado.criterios.filter((c) => c.estudoId === estudoId)
  const reprovadores: AvaliacaoResponse['criteriosReprovadores'] = []
  let pontuacao = 0
  for (const criterio of criterios) {
    const aplica = respostas.find((r) => r.criterioId === criterio.id)?.aplica ?? false
    if (criterio.tipo === 'INCLUSAO') {
      if (aplica) pontuacao += criterio.peso ?? 0
      else if (criterio.determinante) reprovadores.push({ criterioId: criterio.id, tipo: criterio.tipo, titulo: criterio.titulo })
    } else if (aplica) {
      reprovadores.push({ criterioId: criterio.id, tipo: criterio.tipo, titulo: criterio.titulo })
    }
  }
  return { pontuacao, reprovadores }
}

export const handlers = [
  http.options('*', () => semConteudo()),

  // ---- Autenticação e estudos (Sprint 1), só o necessário para navegar até as telas novas
  http.post('*/api/auth/login', async ({ request }) => {
    const { email, senha } = (await request.json()) as { email: string; senha: string }
    const perfil = (['ADMINISTRADOR', 'PESQUISADOR', 'COORDENADOR'] as const).find((p) =>
      email.toLowerCase().startsWith(p.toLowerCase().replace('administrador', 'admin')),
    )
    if (!perfil || !senha || senha === 'errada') return erro(401, 'Credenciais inválidas')
    return json({ token: tokenMock(perfil), tipo: 'Bearer' })
  }),
  http.get('*/api/auth/me', ({ request }) => {
    const perfil = perfilDe(request)
    if (!perfil) return erro(401, 'Não autenticado')
    return json({ id: `mock-${perfil}`, nome: `${perfil} (mock)`, email: `${perfil.toLowerCase()}@clinicalstudy.test`, perfil })
  }),
  http.get('*/api/estudos', ({ request }) => negar(request, PERFIS_LEITURA) ?? json(mock.estado.estudos)),
  http.get('*/api/estudos/:estudoId', ({ request, params }) => {
    const negado = negar(request, PERFIS_LEITURA)
    if (negado) return negado
    const estudo = estudoDe(params.estudoId as string)
    return estudo ? json(estudo) : naoEncontrado('Estudo', params.estudoId as string)
  }),

  // ---- RF07: estado do estudo
  http.post('*/api/estudos/:estudoId/transicoes', async ({ request, params }) => {
    const negado = negar(request, PERFIL_PLANEJAMENTO)
    if (negado) return negado
    const estudo = estudoDe(params.estudoId as string)
    if (!estudo) return naoEncontrado('Estudo', params.estudoId as string)
    const { destino } = (await request.json()) as { destino?: StatusEstudo }
    if (!destino) return validar('destino', 'O estado de destino é obrigatório')

    const proximo = SEQUENCIA_ESTADOS[SEQUENCIA_ESTADOS.indexOf(estudo.status) + 1]
    if (destino !== proximo) {
      return conflito('TRANSICAO_ESTADO_INVALIDA', `O estudo não pode ir de ${estudo.status} para ${destino}`)
    }
    if (estudo.status === 'PLANEJAMENTO') {
      if (!mock.estado.criterios.some((c) => c.estudoId === estudo.id && c.tipo === 'INCLUSAO')) {
        return conflito('ESTUDO_SEM_CRITERIO_INCLUSAO', 'Adicione ao menos um critério de inclusão para avançar')
      }
      if (!mock.estado.visitas.some((v) => v.estudoId === estudo.id)) {
        return conflito('ESTUDO_SEM_VISITA_PREVISTA', 'Cadastre ao menos uma visita prevista para avançar')
      }
    }
    mock.estado.historico.push({
      id: novoId('h'), estudoId: estudo.id, estadoAnterior: estudo.status, estadoNovo: destino,
      usuario: 'Pesquisador Fictício', dataHora: new Date().toISOString().slice(0, 19),
    })
    estudo.status = destino
    return json(estudo satisfies Estudo)
  }),
  http.get('*/api/estudos/:estudoId/historico-status', ({ request, params }) => {
    const negado = negar(request, PERFIS_LEITURA)
    if (negado) return negado
    if (!estudoDe(params.estudoId as string)) return naoEncontrado('Estudo', params.estudoId as string)
    return json(mock.estado.historico.filter((h) => h.estudoId === params.estudoId))
  }),

  // ---- RF05: critérios
  http.get('*/api/estudos/:estudoId/criterios', ({ request, params }) => {
    const negado = negar(request, PERFIS_LEITURA)
    if (negado) return negado
    if (!estudoDe(params.estudoId as string)) return naoEncontrado('Estudo', params.estudoId as string)
    return json(mock.estado.criterios.filter((c) => c.estudoId === params.estudoId).sort((a, b) => a.titulo.localeCompare(b.titulo)))
  }),
  http.post('*/api/estudos/:estudoId/criterios', async ({ request, params }) => {
    const negado = negar(request, PERFIL_PLANEJAMENTO)
    if (negado) return negado
    const estudo = estudoDe(params.estudoId as string)
    if (!estudo) return naoEncontrado('Estudo', params.estudoId as string)
    const dados = (await request.json()) as CriterioRequest
    const invalido = validarCriterio(dados)
    if (invalido) return invalido
    if (estudo.status !== 'PLANEJAMENTO') {
      return conflito('ESTUDO_FORA_DE_PLANEJAMENTO', 'Critérios e visitas só podem ser alterados com o estudo em planejamento')
    }
    const repetido = repeticaoDeCriterio(estudo.id, dados)
    if (repetido) return repetido
    const criado: CriterioResponse = {
      id: novoId('c'), estudoId: estudo.id, tipo: dados.tipo, titulo: tituloDoCriterio(dados), descricao: dados.descricao.trim(),
      peso: dados.tipo === 'INCLUSAO' ? (dados.peso ?? 1) : null,
      determinante: dados.tipo === 'INCLUSAO' ? (dados.determinante ?? true) : null,
    }
    mock.estado.criterios.push(criado)
    return json(criado, 201)
  }),
  http.put('*/api/estudos/:estudoId/criterios/:id', async ({ request, params }) => {
    const negado = negar(request, PERFIL_PLANEJAMENTO)
    if (negado) return negado
    const criterio = mock.estado.criterios.find((c) => c.id === params.id && c.estudoId === params.estudoId)
    const estudo = estudoDe(params.estudoId as string)
    if (!criterio || !estudo) return naoEncontrado('Critério', params.id as string)
    const dados = (await request.json()) as CriterioRequest
    const invalido = validarCriterio(dados)
    if (invalido) return invalido
    if (estudo.status !== 'PLANEJAMENTO') {
      return conflito('ESTUDO_FORA_DE_PLANEJAMENTO', 'Critérios e visitas só podem ser alterados com o estudo em planejamento')
    }
    if (criterio.tipo !== dados.tipo) return erro(400, 'O tipo de um critério não pode ser alterado; exclua e crie outro')
    const repetido = repeticaoDeCriterio(estudo.id, dados, criterio.id)
    if (repetido) return repetido
    criterio.titulo = tituloDoCriterio(dados)
    criterio.descricao = dados.descricao.trim()
    if (criterio.tipo === 'INCLUSAO') {
      criterio.peso = dados.peso ?? 1
      criterio.determinante = dados.determinante ?? true
    }
    return json(criterio)
  }),
  http.delete('*/api/estudos/:estudoId/criterios/:id', ({ request, params }) => {
    const negado = negar(request, PERFIL_PLANEJAMENTO)
    if (negado) return negado
    const estudo = estudoDe(params.estudoId as string)
    const indice = mock.estado.criterios.findIndex((c) => c.id === params.id && c.estudoId === params.estudoId)
    if (indice < 0 || !estudo) return naoEncontrado('Critério', params.id as string)
    if (estudo.status !== 'PLANEJAMENTO') {
      return conflito('ESTUDO_FORA_DE_PLANEJAMENTO', 'Critérios e visitas só podem ser alterados com o estudo em planejamento')
    }
    mock.estado.criterios.splice(indice, 1)
    return semConteudo()
  }),

  // ---- RF06: visitas e ações
  http.get('*/api/estudos/:estudoId/visitas', ({ request, params }) => {
    const negado = negar(request, PERFIS_LEITURA)
    if (negado) return negado
    if (!estudoDe(params.estudoId as string)) return naoEncontrado('Estudo', params.estudoId as string)
    return json(visitasOrdenadas(params.estudoId as string))
  }),
  http.post('*/api/estudos/:estudoId/visitas', async ({ request, params }) => {
    const negado = negar(request, PERFIL_PLANEJAMENTO)
    if (negado) return negado
    const estudo = estudoDe(params.estudoId as string)
    if (!estudo) return naoEncontrado('Estudo', params.estudoId as string)
    const dados = (await request.json()) as VisitaRequest
    const invalido = validarVisita(dados)
    if (invalido) return invalido
    if (estudo.status !== 'PLANEJAMENTO') {
      return conflito('ESTUDO_FORA_DE_PLANEJAMENTO', 'Critérios e visitas só podem ser alterados com o estudo em planejamento')
    }
    if (mock.estado.visitas.some((v) => v.estudoId === estudo.id && v.diaPrevisto === dados.diaPrevisto)) {
      return conflito('VISITA_DIA_DUPLICADO', 'Já existe uma visita prevista para esse dia neste estudo')
    }
    const criada: VisitaResponse = { id: novoId('d'), estudoId: estudo.id, nome: dados.nome.trim(), diaPrevisto: dados.diaPrevisto, tolerancia: dados.tolerancia, ordem: 99, acoes: [] }
    mock.estado.visitas.push(criada)
    recalcularOrdem(estudo.id)
    return json(criada, 201)
  }),
  http.put('*/api/estudos/:estudoId/visitas/:id', async ({ request, params }) => {
    const negado = negar(request, PERFIL_PLANEJAMENTO)
    if (negado) return negado
    const visita = mock.estado.visitas.find((v) => v.id === params.id && v.estudoId === params.estudoId)
    const estudo = estudoDe(params.estudoId as string)
    if (!visita || !estudo) return naoEncontrado('Visita', params.id as string)
    const dados = (await request.json()) as VisitaRequest
    const invalido = validarVisita(dados)
    if (invalido) return invalido
    if (estudo.status !== 'PLANEJAMENTO') {
      return conflito('ESTUDO_FORA_DE_PLANEJAMENTO', 'Critérios e visitas só podem ser alterados com o estudo em planejamento')
    }
    if (mock.estado.visitas.some((v) => v.estudoId === estudo.id && v.id !== visita.id && v.diaPrevisto === dados.diaPrevisto)) {
      return conflito('VISITA_DIA_DUPLICADO', 'Já existe uma visita prevista para esse dia neste estudo')
    }
    Object.assign(visita, { nome: dados.nome.trim(), diaPrevisto: dados.diaPrevisto, tolerancia: dados.tolerancia })
    recalcularOrdem(estudo.id)
    return json(visita)
  }),
  http.delete('*/api/estudos/:estudoId/visitas/:id', ({ request, params }) => {
    const negado = negar(request, PERFIL_PLANEJAMENTO)
    if (negado) return negado
    const estudo = estudoDe(params.estudoId as string)
    const indice = mock.estado.visitas.findIndex((v) => v.id === params.id && v.estudoId === params.estudoId)
    if (indice < 0 || !estudo) return naoEncontrado('Visita', params.id as string)
    if (estudo.status !== 'PLANEJAMENTO') {
      return conflito('ESTUDO_FORA_DE_PLANEJAMENTO', 'Critérios e visitas só podem ser alterados com o estudo em planejamento')
    }
    mock.estado.visitas.splice(indice, 1)
    recalcularOrdem(estudo.id)
    return semConteudo()
  }),
  http.post('*/api/visitas/:visitaId/acoes', async ({ request, params }) => {
    const negado = negar(request, PERFIL_PLANEJAMENTO)
    if (negado) return negado
    const visita = mock.estado.visitas.find((v) => v.id === params.visitaId)
    if (!visita) return naoEncontrado('Visita', params.visitaId as string)
    const dados = (await request.json()) as AcaoRequest
    const invalido = validarAcao(dados)
    if (invalido) return invalido
    if (estudoDe(visita.estudoId)?.status !== 'PLANEJAMENTO') {
      return conflito('ESTUDO_FORA_DE_PLANEJAMENTO', 'Critérios e visitas só podem ser alterados com o estudo em planejamento')
    }
    const criada = { id: novoId('a'), visitaId: visita.id, nome: dados.nome.trim(), descricao: dados.descricao?.trim() || null, categoria: dados.categoria }
    visita.acoes.push(criada)
    return json(criada, 201)
  }),
  http.put('*/api/visitas/:visitaId/acoes/:id', async ({ request, params }) => {
    const negado = negar(request, PERFIL_PLANEJAMENTO)
    if (negado) return negado
    const visita = mock.estado.visitas.find((v) => v.id === params.visitaId)
    const acao = visita?.acoes.find((a) => a.id === params.id)
    if (!visita || !acao) return naoEncontrado('Ação', params.id as string)
    const dados = (await request.json()) as AcaoRequest
    const invalido = validarAcao(dados)
    if (invalido) return invalido
    if (estudoDe(visita.estudoId)?.status !== 'PLANEJAMENTO') {
      return conflito('ESTUDO_FORA_DE_PLANEJAMENTO', 'Critérios e visitas só podem ser alterados com o estudo em planejamento')
    }
    Object.assign(acao, { nome: dados.nome.trim(), descricao: dados.descricao?.trim() || null, categoria: dados.categoria })
    return json(acao)
  }),
  http.delete('*/api/visitas/:visitaId/acoes/:id', ({ request, params }) => {
    const negado = negar(request, PERFIL_PLANEJAMENTO)
    if (negado) return negado
    const visita = mock.estado.visitas.find((v) => v.id === params.visitaId)
    const indice = visita?.acoes.findIndex((a) => a.id === params.id) ?? -1
    if (!visita || indice < 0) return naoEncontrado('Ação', params.id as string)
    if (estudoDe(visita.estudoId)?.status !== 'PLANEJAMENTO') {
      return conflito('ESTUDO_FORA_DE_PLANEJAMENTO', 'Critérios e visitas só podem ser alterados com o estudo em planejamento')
    }
    visita.acoes.splice(indice, 1)
    return semConteudo()
  }),

  // ---- RF08: candidatos
  http.get('*/api/estudos/:estudoId/candidatos', ({ request, params }) => {
    const negado = negar(request, PERFIS_LEITURA)
    if (negado) return negado
    if (!estudoDe(params.estudoId as string)) return naoEncontrado('Estudo', params.estudoId as string)
    const url = new URL(request.url)
    const situacao = url.searchParams.get('situacao') as SituacaoCandidato | null
    const pagina = Number(url.searchParams.get('pagina') ?? 0)
    const tamanho = Math.min(Number(url.searchParams.get('tamanho') ?? 20), 100)
    const todos = mock.estado.candidatos
      .filter((c) => c.estudoId === params.estudoId && (!situacao || c.situacao === situacao))
      .sort((a, b) => a.codigo.localeCompare(b.codigo))
    return json({
      conteudo: todos.slice(pagina * tamanho, pagina * tamanho + tamanho), pagina, tamanho,
      totalElementos: todos.length, totalPaginas: Math.ceil(todos.length / tamanho),
    })
  }),
  http.post('*/api/estudos/:estudoId/candidatos', async ({ request, params }) => {
    const negado = negar(request, PERFIL_RECRUTAMENTO)
    if (negado) return negado
    const estudo = estudoDe(params.estudoId as string)
    if (!estudo) return naoEncontrado('Estudo', params.estudoId as string)
    const dados = (await request.json()) as CandidatoRequest
    const invalido = validarCandidato(dados)
    if (invalido) return invalido
    if (estudo.status !== 'RECRUTAMENTO') {
      return conflito('ESTUDO_FORA_DE_RECRUTAMENTO', 'Esta operação exige que o estudo esteja em recrutamento')
    }
    if (candidatoRepetido(estudo.id, dados)) {
      return conflito('CANDIDATO_DUPLICADO', 'Já existe um candidato com esse nome fictício e data de nascimento neste estudo')
    }
    const total = mock.estado.candidatos.filter((c) => c.estudoId === estudo.id).length
    const criado: CandidatoResponse = {
      id: novoId('e'), estudoId: estudo.id, codigo: `CAND-${String(total + 1).padStart(3, '0')}`, nomeFicticio: dados.nomeFicticio.trim(),
      dataNascimento: dados.dataNascimento, contato: dados.contato?.trim() || null, usuarioId: null, situacao: 'CADASTRADO',
      resultadoAvaliacao: null, dataAvaliacao: null, consentimentoRegistrado: false, dataConsentimento: null,
    }
    mock.estado.candidatos.push(criado)
    return json(criado, 201)
  }),
  http.get('*/api/candidatos/:id', ({ request, params }) => {
    const negado = negar(request, PERFIS_LEITURA)
    if (negado) return negado
    const candidato = candidatoDe(params.id as string)
    return candidato ? json(candidato) : naoEncontrado('Candidato', params.id as string)
  }),
  http.put('*/api/candidatos/:id', async ({ request, params }) => {
    const negado = negar(request, PERFIL_RECRUTAMENTO)
    if (negado) return negado
    const candidato = candidatoDe(params.id as string)
    if (!candidato) return naoEncontrado('Candidato', params.id as string)
    const dados = (await request.json()) as CandidatoRequest
    const invalido = validarCandidato(dados)
    if (invalido) return invalido
    if (candidato.situacao === 'INCLUIDO') {
      return conflito('CANDIDATO_JA_INCLUIDO', 'O candidato já foi incluído no estudo e não pode mais ser editado')
    }
    if (candidatoRepetido(candidato.estudoId, dados, candidato.id)) {
      return conflito('CANDIDATO_DUPLICADO', 'Já existe um candidato com esse nome fictício e data de nascimento neste estudo')
    }
    Object.assign(candidato, { nomeFicticio: dados.nomeFicticio.trim(), dataNascimento: dados.dataNascimento, contato: dados.contato?.trim() || null })
    return json(candidato)
  }),

  // ---- RF09: avaliação de elegibilidade
  http.get('*/api/candidatos/:candidatoId/avaliacoes', ({ request, params }) => {
    const negado = negar(request, PERFIS_LEITURA)
    if (negado) return negado
    if (!candidatoDe(params.candidatoId as string)) return naoEncontrado('Candidato', params.candidatoId as string)
    return json(mock.estado.avaliacoes.filter((a) => a.candidatoId === params.candidatoId).reverse())
  }),
  http.post('*/api/candidatos/:candidatoId/avaliacoes', async ({ request, params }) => {
    const negado = negar(request, PERFIL_RECRUTAMENTO)
    if (negado) return negado
    const candidato = candidatoDe(params.candidatoId as string)
    if (!candidato) return naoEncontrado('Candidato', params.candidatoId as string)
    const estudo = estudoDe(candidato.estudoId)
    if (estudo?.status !== 'RECRUTAMENTO') {
      return conflito('ESTUDO_FORA_DE_RECRUTAMENTO', 'Esta operação exige que o estudo esteja em recrutamento')
    }
    if (candidato.situacao === 'INCLUIDO') {
      return conflito('CANDIDATO_JA_INCLUIDO', 'O candidato já foi incluído no estudo e não pode ser reavaliado')
    }
    const { respostas } = (await request.json()) as AvaliacaoRequest
    const criterios = mock.estado.criterios.filter((c) => c.estudoId === candidato.estudoId)
    const ids = respostas.map((r) => r.criterioId)
    if (new Set(ids).size !== ids.length) return erro(400, 'Há mais de uma resposta para o mesmo critério')
    if (ids.some((id) => !criterios.some((c) => c.id === id))) return erro(400, 'Há resposta de um critério que não pertence a este estudo')
    if (ids.length !== criterios.length) return erro(400, 'A avaliação deve responder a todos os critérios do estudo')

    const { pontuacao, reprovadores } = resultadoDaAvaliacao(candidato.estudoId, respostas)
    mock.estado.avaliacoes.filter((a) => a.candidatoId === candidato.id).forEach((a) => (a.vigente = false))
    const aprovado = reprovadores.length === 0
    const criada: AvaliacaoResponse = {
      id: novoId('f'), candidatoId: candidato.id, dataAvaliacao: new Date().toISOString().slice(0, 19), avaliador: 'Coordenador Fictício',
      resultado: aprovado ? 'APROVADO' : 'REPROVADO', pontuacao, vigente: true, criteriosReprovadores: reprovadores,
      respostas: respostas.map((r) => {
        const criterio = criterios.find((c) => c.id === r.criterioId)!
        return { criterioId: criterio.id, tipo: criterio.tipo, titulo: criterio.titulo, aplica: r.aplica, observacao: r.observacao?.trim() || null }
      }),
    }
    mock.estado.avaliacoes.push(criada)
    candidato.resultadoAvaliacao = criada.resultado
    candidato.dataAvaliacao = criada.dataAvaliacao
    candidato.situacao = !aprovado ? 'NAO_ELEGIVEL' : candidato.consentimentoRegistrado ? 'CONSENTIDO' : 'ELEGIVEL'
    return json(criada, 201)
  }),

  // ---- RF10: consentimento
  http.get('*/api/candidatos/:candidatoId/consentimento', ({ request, params }) => {
    const negado = negar(request, PERFIS_LEITURA)
    if (negado) return negado
    const consentimento = mock.estado.consentimentos.find((c) => c.candidatoId === params.candidatoId)
    return consentimento ? json(consentimento) : erro(404, 'Consentimento não registrado para o candidato')
  }),
  http.post('*/api/candidatos/:candidatoId/consentimento', async ({ request, params }) => {
    const negado = negar(request, PERFIL_RECRUTAMENTO)
    if (negado) return negado
    const candidato = candidatoDe(params.candidatoId as string)
    if (!candidato) return naoEncontrado('Candidato', params.candidatoId as string)
    if (estudoDe(candidato.estudoId)?.status !== 'RECRUTAMENTO') {
      return conflito('ESTUDO_FORA_DE_RECRUTAMENTO', 'Esta operação exige que o estudo esteja em recrutamento')
    }
    if (candidato.situacao === 'INCLUIDO') return conflito('CANDIDATO_JA_INCLUIDO', 'O candidato já foi incluído no estudo')
    if (mock.estado.consentimentos.some((c) => c.candidatoId === candidato.id)) {
      return conflito('CONSENTIMENTO_DUPLICADO', 'O consentimento deste candidato já foi registrado')
    }
    const dados = ((await request.json().catch(() => ({}))) ?? {}) as { dataHora?: string; versaoTermo?: string }
    const agora = new Date().toISOString().slice(0, 19)
    if (dados.dataHora && dados.dataHora > agora) return erro(400, 'A data e hora do consentimento não pode ser futura')
    const criado = {
      id: novoId('g'), candidatoId: candidato.id, dataHora: dados.dataHora ?? agora, responsavel: 'Coordenador Fictício',
      versaoTermo: dados.versaoTermo?.trim() || 'TCLE-SIMULADO v1',
    }
    mock.estado.consentimentos.push(criado)
    candidato.consentimentoRegistrado = true
    candidato.dataConsentimento = criado.dataHora
    if (candidato.situacao === 'ELEGIVEL') candidato.situacao = 'CONSENTIDO'
    return json(criado, 201)
  }),

  // ---- RF11: participantes
  http.get('*/api/estudos/:estudoId/participantes', ({ request, params }) => {
    const negado = negar(request, PERFIS_LEITURA)
    if (negado) return negado
    if (!estudoDe(params.estudoId as string)) return naoEncontrado('Estudo', params.estudoId as string)
    return json(mock.estado.participantes.filter((p) => p.estudoId === params.estudoId))
  }),
  http.get('*/api/estudos/:estudoId/participantes/vagas', ({ request, params }) => {
    const negado = negar(request, PERFIS_LEITURA)
    if (negado) return negado
    const estudo = estudoDe(params.estudoId as string)
    if (!estudo) return naoEncontrado('Estudo', params.estudoId as string)
    const incluidos = mock.estado.participantes.filter((p) => p.estudoId === estudo.id).length
    return json({ incluidos, limite: estudo.limiteParticipantes, restantes: Math.max(0, estudo.limiteParticipantes - incluidos) })
  }),
  http.post('*/api/estudos/:estudoId/participantes', async ({ request, params }) => {
    const negado = negar(request, PERFIL_RECRUTAMENTO)
    if (negado) return negado
    const estudo = estudoDe(params.estudoId as string)
    if (!estudo) return naoEncontrado('Estudo', params.estudoId as string)
    const { candidatoId } = (await request.json()) as { candidatoId?: string }
    if (!candidatoId) return validar('candidatoId', 'O candidato é obrigatório')
    if (estudo.status !== 'RECRUTAMENTO') {
      return conflito('ESTUDO_FORA_DE_RECRUTAMENTO', 'Esta operação exige que o estudo esteja em recrutamento')
    }
    const candidato = candidatoDe(candidatoId)
    if (!candidato || candidato.estudoId !== estudo.id) return naoEncontrado('Candidato', candidatoId)
    if (candidato.situacao === 'INCLUIDO') return conflito('CANDIDATO_JA_INCLUIDO', 'O candidato já foi incluído no estudo')
    if (candidato.resultadoAvaliacao !== 'APROVADO') {
      return conflito('CANDIDATO_NAO_ELEGIVEL', 'O candidato precisa ter uma avaliação de elegibilidade aprovada para ser incluído')
    }
    if (!candidato.consentimentoRegistrado) {
      return conflito('CONSENTIMENTO_AUSENTE', 'O candidato precisa ter o consentimento registrado para ser incluído')
    }
    if (mock.estado.participantes.filter((p) => p.estudoId === estudo.id).length >= estudo.limiteParticipantes) {
      return conflito('VAGAS_ESGOTADAS', `Não há vagas disponíveis: o limite de ${estudo.limiteParticipantes} participantes do estudo foi atingido`)
    }
    const criado = {
      id: novoId('b'), candidatoId: candidato.id, codigo: candidato.codigo, nomeFicticio: candidato.nomeFicticio, estudoId: estudo.id,
      dataInclusao: new Date().toISOString().slice(0, 10), incluidoPor: 'Coordenador Fictício',
    }
    mock.estado.participantes.push(criado)
    candidato.situacao = 'INCLUIDO'
    return json(criado, 201)
  }),
]

// ---- validações e regras compartilhadas pelos handlers

function validarCriterio(d: CriterioRequest) {
  if (!d.titulo?.trim()) return validar('titulo', 'O título é obrigatório')
  if (d.titulo.length > 120) return validar('titulo', 'O título deve ter no máximo 120 caracteres')
  if (!d.descricao?.trim()) return validar('descricao', 'A descrição é obrigatória')
  if (d.descricao.trim().length < 5 || d.descricao.length > 500) return validar('descricao', 'A descrição deve ter entre 5 e 500 caracteres')
  if (d.peso != null && d.peso <= 0) return validar('peso', 'O peso deve ser maior que zero')
  return null
}

// O título é opcional na API: ausente ou em branco, vem dos primeiros 120 caracteres da descrição
function tituloDoCriterio(d: CriterioRequest): string {
  const titulo = d.titulo?.trim()
  return titulo ? titulo : d.descricao.trim().slice(0, 120)
}

function repeticaoDeCriterio(estudoId: string, d: CriterioRequest, ignorarId?: string) {
  const outros = mock.estado.criterios.filter((c) => c.estudoId === estudoId && c.tipo === d.tipo && c.id !== ignorarId)
  const tipo = d.tipo === 'INCLUSAO' ? 'inclusão' : 'exclusão'
  if (outros.some((c) => c.titulo.toLowerCase() === tituloDoCriterio(d).toLowerCase())) {
    return conflito('CRITERIO_DUPLICADO', `Já existe um critério de ${tipo} com esse título neste estudo`)
  }
  if (outros.some((c) => c.descricao.toLowerCase() === d.descricao.trim().toLowerCase())) {
    return conflito('CRITERIO_DUPLICADO', `Já existe um critério de ${tipo} com essa descrição neste estudo`)
  }
  return null
}

function validarVisita(d: VisitaRequest) {
  if (!d.nome?.trim()) return validar('nome', 'O nome é obrigatório')
  if (d.nome.length > 100) return validar('nome', 'O nome deve ter no máximo 100 caracteres')
  if (d.diaPrevisto == null || d.diaPrevisto < 0) return validar('diaPrevisto', 'O dia previsto deve ser maior ou igual a zero')
  if (d.tolerancia == null || d.tolerancia < 0 || d.tolerancia > 30) return validar('tolerancia', 'A tolerância deve ficar entre 0 e 30 dias')
  return null
}

function validarAcao(d: AcaoRequest) {
  if (!d.nome?.trim()) return validar('nome', 'O nome é obrigatório')
  if (!d.categoria) return validar('categoria', 'A categoria é obrigatória')
  return null
}

function validarCandidato(d: CandidatoRequest) {
  if (!d.nomeFicticio?.trim()) return validar('nomeFicticio', 'O nome fictício é obrigatório')
  if (!d.dataNascimento) return validar('dataNascimento', 'A data de nascimento é obrigatória')
  if (d.dataNascimento >= new Date().toISOString().slice(0, 10)) return validar('dataNascimento', 'A data de nascimento deve estar no passado')
  return null
}

function candidatoRepetido(estudoId: string, d: CandidatoRequest, ignorarId?: string) {
  return mock.estado.candidatos.some(
    (c) => c.estudoId === estudoId && c.id !== ignorarId && c.nomeFicticio.toLowerCase() === d.nomeFicticio.trim().toLowerCase() && c.dataNascimento === d.dataNascimento,
  )
}
