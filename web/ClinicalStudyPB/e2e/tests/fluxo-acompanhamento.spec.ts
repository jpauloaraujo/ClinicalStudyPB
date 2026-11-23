import { expect as esperar, type Browser, type Page } from '@playwright/test'
import { FichaParticipantePage } from '../pages/FichaParticipantePage'
import { ApiE2E } from '../support/api'
import { capturar } from '../support/capturas'
import { caminhoSessao } from '../support/sessao'
import { nomeFicticio, sufixoUnico } from '../support/unico'
import { test } from '../fixtures'

// Fluxo de acompanhamento completo (T99, RF12 a RF15), com dois perfis em dois contextos de navegador:
// o coordenador agenda a visita e registra que foi realizada; o pesquisador registra o resultado da avaliação; os
// dois conferem o histórico na ficha do participante. O estado inicial é montado pela API com dados únicos por
// execução (estudo, candidato e atividade novos), então o spec pode ser repetido no mesmo banco.

const VISITA = 'Triagem'
const ATIVIDADE = 'Pressão arterial'

type Cenario = { estudoId: string; participacaoId: string; codigo: string; nome: string }

async function prepararCenario(): Promise<Cenario> {
  const pesquisador = await ApiE2E.como('PESQUISADOR')
  const coordenador = await ApiE2E.como('COORDENADOR')
  try {
    const { estudoId } = await pesquisador.estudoParaAcompanhamento(`Estudo Acompanhamento ${sufixoUnico()}`)
    const nome = nomeFicticio('Participante')
    const { participacaoId, codigo } = await coordenador.participanteIncluido(estudoId, nome)
    return { estudoId, participacaoId, codigo, nome }
  } finally {
    await pesquisador.encerrar()
    await coordenador.encerrar()
  }
}

async function abrirComo(browser: Browser, perfil: 'COORDENADOR' | 'PESQUISADOR') {
  const contexto = await browser.newContext({ storageState: caminhoSessao(perfil), locale: 'pt-BR' })
  const pagina: Page = await contexto.newPage()
  return { contexto, pagina, ficha: new FichaParticipantePage(pagina) }
}

test.describe('fluxo de acompanhamento completo (RF12 a RF15)', () => {
  test('o coordenador agenda e registra a realização, o pesquisador registra o resultado e o histórico mostra tudo', async ({
    browser,
  }) => {
    let cenario!: Cenario
    await test.step('Partida pela API: estudo em recrutamento, participante incluído e visita prevista com avaliação', async () => {
      cenario = await prepararCenario()
    })
    const { estudoId, participacaoId, codigo, nome } = cenario

    const coordenador = await abrirComo(browser, 'COORDENADOR')
    const pesquisador = await abrirComo(browser, 'PESQUISADOR')

    await test.step('Coordenador: antes de agendar, a ficha mostra a visita como não agendada e 1 pendente', async () => {
      await coordenador.ficha.goto(estudoId, participacaoId)
      await coordenador.ficha.expectResumo({ realizadas: 0, ausentes: 0, canceladas: 0, pendentes: 1 })
      await coordenador.ficha.expectSituacaoDaVisita(VISITA, 'Não agendada')
    })

    await test.step('Coordenador: abre Participantes, entra no agendamento e agenda a visita', async () => {
      await coordenador.pagina.goto(`/estudos/${estudoId}/participantes`)
      await coordenador.ficha.abrirAgendamentoPelaLista(codigo)
      await coordenador.ficha.agendarComASugestao(VISITA)
      await capturar(coordenador.pagina, 'fluxo-acompanhamento-01-visita-agendada')
    })

    await test.step('Coordenador: abre a visita e registra que foi realizada', async () => {
      await coordenador.ficha.abrirVisitaNoAgendamento(VISITA)
      await coordenador.ficha.registrarRealizada()
      await capturar(coordenador.pagina, 'fluxo-acompanhamento-02-visita-realizada')
    })

    await test.step('Pesquisador: não tem botões de agendamento nem de desfecho', async () => {
      await pesquisador.pagina.goto(`/estudos/${estudoId}/participantes/${participacaoId}/agendamento`)
      await pesquisador.ficha.expectSemBotoesDeAgendar()
      await pesquisador.ficha.goto(estudoId, participacaoId)
      await pesquisador.ficha.expectSemAgendamento()
    })

    await test.step('Pesquisador: abre a visita realizada, aba Avaliações, e registra o resultado', async () => {
      await pesquisador.ficha.abrirVisita(VISITA)
      await pesquisador.ficha.abrirAbaDaVisita('Avaliações')
      await pesquisador.ficha.registrarResultadoDaAvaliacao(ATIVIDADE, '120/80', 'mmHg')
      await capturar(pesquisador.pagina, 'fluxo-acompanhamento-03-resultado-registrado')
    })

    await test.step('Os dois perfis veem no histórico a visita realizada com o resultado e o resumo correto', async () => {
      for (const { ficha, pagina } of [coordenador, pesquisador]) {
        await ficha.goto(estudoId, participacaoId)
        await ficha.expectCabecalho(codigo, nome)
        await ficha.expectResumo({ realizadas: 1, ausentes: 0, canceladas: 0, pendentes: 0 })
        await ficha.expectVisitasNaOrdem([VISITA])
        await ficha.expectSituacaoDaVisita(VISITA, 'Realizada')
        await ficha.expectResultado(VISITA, ATIVIDADE, '120/80 mmHg')
        await esperar(pagina.getByRole('region', { name: 'Resumo do histórico' })).toBeVisible()
      }
      await capturar(coordenador.pagina, 'rf15-historico')
      await coordenador.pagina
        .getByRole('region', { name: 'Resumo do histórico' })
        .screenshot({ path: 'test-results/capturas/rf15-historico-resumo.png' })
      await capturar(pesquisador.pagina, 'fluxo-acompanhamento-04-historico-pesquisador')
    })

    await test.step('Coordenador: vê o resultado só para leitura na aba Avaliações; pesquisador não tem agendamento', async () => {
      await coordenador.ficha.goto(estudoId, participacaoId)
      await coordenador.ficha.expectComLinkDeAgendamento()
      await coordenador.ficha.abrirVisita(VISITA)
      await coordenador.ficha.abrirAbaDaVisita('Avaliações')
      await coordenador.ficha.expectAvaliacoesSomenteLeitura(ATIVIDADE)
      await esperar(coordenador.pagina.getByRole('article', { name: ATIVIDADE })).toContainText('120/80')
      await capturar(coordenador.pagina, 'fluxo-acompanhamento-05-avaliacao-somente-leitura')
    })

    await coordenador.contexto.close()
    await pesquisador.contexto.close()
  })
})
