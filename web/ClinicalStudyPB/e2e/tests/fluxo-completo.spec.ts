import type { Browser, Page } from '@playwright/test'
import { CandidatosPage } from '../pages/CandidatosPage'
import { CriteriosPage } from '../pages/CriteriosPage'
import { EstudoDetalhePage } from '../pages/EstudoDetalhePage'
import { EstudosPage } from '../pages/EstudosPage'
import { FichaCandidatoPage } from '../pages/FichaCandidatoPage'
import { ParticipantesPage } from '../pages/ParticipantesPage'
import { VisitasPage } from '../pages/VisitasPage'
import { capturar } from '../support/capturas'
import { fluxoCompletoPronto, MOTIVO_FLUXO_COMPLETO } from '../support/pendencias'
import { caminhoSessao } from '../support/sessao'
import { nomeFicticio, sufixoUnico } from '../support/unico'
import { comoPerfil, expect, test } from '../fixtures'

// Fluxo completo da Release 1 (RQ-05 a RQ-07 da T66), com dois perfis em dois contextos de navegador:
// o pesquisador planeja o estudo e abre o recrutamento; o coordenador cadastra, avalia, registra o consentimento
// e inclui o candidato. Tudo pela interface, contra o backend real.

const LIMITE = 3

type PaginasDoPesquisador = {
  page: Page
  paginaEstudos: EstudosPage
  paginaEstudo: EstudoDetalhePage
  paginaCriterios: CriteriosPage
  paginaVisitas: VisitasPage
}

async function pesquisadorPlaneja({ page, paginaEstudos, paginaEstudo, paginaCriterios, paginaVisitas }: PaginasDoPesquisador) {
  const titulo = `Estudo Fluxo ${sufixoUnico()}`

  await test.step('O pesquisador cadastra o estudo com limite de participantes maior que 1', async () => {
    await paginaEstudos.cadastrar({
      titulo,
      objetivo: 'Avaliar um protocolo fictício no fluxo completo da Release 1.',
      dataInicio: '2026-11-10',
      dataFim: '2027-05-10',
      limite: LIMITE,
    })
    await paginaEstudos.abrir(titulo)
    await paginaEstudo.expectEstado('Planejamento')
  })

  await test.step('Define um critério de inclusão e um de exclusão', async () => {
    await paginaEstudo.abrirAba('Critérios')
    await paginaCriterios.adicionar('INCLUSAO', 'Idade adulta', 'Candidato com 18 anos ou mais')
    await paginaCriterios.adicionar('EXCLUSAO', 'Gestante', 'Candidata em gestação confirmada')
  })

  await test.step('Define uma visita prevista com uma atividade', async () => {
    await paginaEstudo.abrirAba('Visitas')
    await paginaVisitas.adicionarVisita('Triagem', 0)
    await paginaVisitas.adicionarAtividade('Triagem', 'Hemograma completo', 'Exame laboratorial')
  })

  await test.step('Avança para recrutamento com a confirmação e as telas de planejamento ficam somente leitura', async () => {
    await paginaEstudo.avancarPara('Recrutamento')
    await paginaEstudo.expectEstado('Recrutamento')
    await capturar(page, 'fluxo-01-estudo-em-recrutamento')
    await paginaVisitas.expectSomenteLeitura()
    await paginaEstudo.abrirAba('Critérios')
    await paginaCriterios.expectSomenteLeitura()
  })

  return { titulo }
}

async function coordenadorCadastraEAvalia(browser: Browser, titulo: string) {
  const contexto = await browser.newContext({ storageState: caminhoSessao('COORDENADOR') })
  const pagina = await contexto.newPage()
  const estudos = new EstudosPage(pagina)
  const estudo = new EstudoDetalhePage(pagina)
  const candidatos = new CandidatosPage(pagina)
  const ficha = new FichaCandidatoPage(pagina)
  const nome = nomeFicticio('Candidata')
  let codigo = ''

  await test.step('O coordenador abre o estudo e cadastra um candidato (o código é gerado)', async () => {
    await estudos.goto()
    await estudos.abrir(titulo)
    await estudo.expectSemBotaoDeAvancar()
    await estudo.abrirAba('Candidatos')
    codigo = await candidatos.cadastrar(nome)
    expect(codigo).toMatch(/^CAND-\d{3,}$/)
    await candidatos.expectNaLista(codigo, nome)
    await capturar(pagina, 'fluxo-02-candidato-cadastrado')
  })

  await test.step('Registra a avaliação respondendo a todos os critérios: candidato elegível', async () => {
    await candidatos.abrirFicha(codigo)
    await ficha.expectSalvarAvaliacaoDesabilitado()
    await ficha.responderCriterio('Idade adulta', true)
    await ficha.responderCriterio('Gestante', false)
    await ficha.salvarAvaliacao()
    await ficha.expectResultado('Candidato elegível')
    await ficha.expectHistorico(1)
    await ficha.expectSituacao('Elegível')
    await capturar(pagina, 'fluxo-03-candidato-elegivel')
  })

  return { contexto, pagina, codigo, nome, ficha, estudo, candidatos }
}

test.describe('fluxo completo da Release 1', () => {
  test.use(comoPerfil('PESQUISADOR'))

  test('o pesquisador planeja e abre o recrutamento; o coordenador cadastra o candidato e o avalia como elegível', async ({
    browser,
    page,
    paginaEstudos,
    paginaEstudo,
    paginaCriterios,
    paginaVisitas,
  }) => {
    const { titulo } = await pesquisadorPlaneja({ page, paginaEstudos, paginaEstudo, paginaCriterios, paginaVisitas })
    const coordenador = await coordenadorCadastraEAvalia(browser, titulo)
    await coordenador.contexto.close()
  })

  test('fluxo completo: consentimento, inclusão, participante na lista e contador de vagas', async ({
    browser,
    page,
    paginaEstudos,
    paginaEstudo,
    paginaCriterios,
    paginaVisitas,
  }) => {
    test.fixme(!fluxoCompletoPronto(), MOTIVO_FLUXO_COMPLETO)

    const { titulo } = await pesquisadorPlaneja({ page, paginaEstudos, paginaEstudo, paginaCriterios, paginaVisitas })
    const { contexto, pagina, codigo, nome, ficha, estudo } = await coordenadorCadastraEAvalia(browser, titulo)

    await test.step('Registra o consentimento simulado e vê o comprovante', async () => {
      await ficha.registrarConsentimento()
      await ficha.expectComprovante()
      await ficha.expectSituacao('Consentido')
      await capturar(pagina, 'fluxo-04-consentimento-registrado')
    })

    await test.step('As quatro pré-condições estão atendidas e a inclusão é confirmada', async () => {
      await ficha.expectPreCondicoesAtendidas()
      await ficha.incluirNoEstudo()
      await ficha.expectIncluido()
    })

    await test.step('O participante aparece na aba Participantes e o contador passa de "0 de N" para "1 de N"', async () => {
      await estudo.abrirAba('Participantes')
      const participantes = new ParticipantesPage(pagina)
      await participantes.expectParticipante(codigo, nome)
      await participantes.expectContador(1, LIMITE)
      await capturar(pagina, 'fluxo-05-participante-incluido')
    })

    await contexto.close()
  })
})
