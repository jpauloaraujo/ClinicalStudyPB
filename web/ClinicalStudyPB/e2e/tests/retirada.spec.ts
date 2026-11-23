import { expect, type Browser, type Page } from '@playwright/test'
import { FichaParticipantePage } from '../pages/FichaParticipantePage'
import { ParticipantesPage } from '../pages/ParticipantesPage'
import { ApiE2E } from '../support/api'
import { capturar } from '../support/capturas'
import { caminhoSessao } from '../support/sessao'
import { nomeFicticio, sufixoUnico } from '../support/unico'
import { test } from '../fixtures'

// RF16 (T107): retirada de participante no navegador.
// Prova o fluxo completo de sucesso (marca RETIRADO, aviso de irreversibilidade e contagem de visitas futuras,
// cancelamento em cascata no histórico preservando visitas realizadas e resultados, ocultação das ações de escrita,
// aviso de participante retirado na aba Avaliações e manutenção das vagas ocupadas) e o cenário de erro de motivo
// vazio ou curto. O estado de partida é montado pela API com dados únicos e datas relativas ao servidor.

const VISITA_REALIZADA = 'Triagem'
const VISITA_FUTURA_1 = 'Retorno 15'
const VISITA_FUTURA_2 = 'Retorno 30'
const VISITA_NAO_AGENDADA = 'Retorno 60'
const ATIVIDADE = 'Pressão arterial'
const MOTIVO_VALIDO = 'Mudança de cidade com impossibilidade de retorno ao centro'

type CenarioRetirada = {
  estudoId: string
  participacaoId: string
  codigo: string
  nome: string
  dataHoje: string
  triagemAgendadaId: string
}

async function prepararParticipanteComVisitasFuturas(): Promise<CenarioRetirada> {
  const pesquisador = await ApiE2E.como('PESQUISADOR')
  const coordenador = await ApiE2E.como('COORDENADOR')
  try {
    const estudo = await pesquisador.estudoParaRetirada(`Estudo Retirada ${sufixoUnico()}`, 2)
    const nome = nomeFicticio('Participante')
    const { participacaoId, codigo } = await coordenador.participanteIncluido(estudo.estudoId, nome)

    const visitas = await coordenador.visitasDoParticipante(participacaoId)
    const dataDe = (visitaPlanejadaId: string) => {
      const item = visitas.find((v) => v.visitaPlanejadaId === visitaPlanejadaId)
      expect(item, `visita prevista ${visitaPlanejadaId} deve aparecer na agenda do participante`).toBeDefined()
      return item!.dataSugerida
    }

    const dataHoje = dataDe(estudo.triagemId)
    const triagemAgendadaId = await coordenador.agendar(participacaoId, estudo.triagemId, `${dataHoje}T09:00`)
    await coordenador.registrarDesfecho(triagemAgendadaId, { tipo: 'REALIZADA' })
    await pesquisador.registrarResultado(triagemAgendadaId, estudo.acaoId, '120/80', 'mmHg')

    await coordenador.agendar(participacaoId, estudo.retorno15Id, `${dataDe(estudo.retorno15Id)}T10:00`)
    await coordenador.agendar(participacaoId, estudo.retorno30Id, `${dataDe(estudo.retorno30Id)}T10:00`)

    return { estudoId: estudo.estudoId, participacaoId, codigo, nome, dataHoje, triagemAgendadaId }
  } finally {
    await pesquisador.encerrar()
    await coordenador.encerrar()
  }
}

async function abrirComo(browser: Browser, perfil: 'COORDENADOR' | 'PESQUISADOR') {
  // Fuso alinhado ao app.fuso-horario do servidor para que "hoje" do formulário nunca divirja perto da meia-noite
  const contexto = await browser.newContext({
    storageState: caminhoSessao(perfil),
    locale: 'pt-BR',
    timezoneId: 'America/Recife',
  })
  const pagina: Page = await contexto.newPage()
  return {
    contexto,
    pagina,
    ficha: new FichaParticipantePage(pagina),
    participantes: new ParticipantesPage(pagina),
  }
}

test.describe('retirada de participante (RF16)', () => {
  test('o coordenador registra a retirada, visitas futuras são canceladas, ações somem e as vagas permanecem ocupadas', async ({
    browser,
  }) => {
    let cenario!: CenarioRetirada
    await test.step('Partida pela API: participante com visita realizada (com resultado), duas futuras agendadas e uma não agendada', async () => {
      cenario = await prepararParticipanteComVisitasFuturas()
    })
    const { estudoId, participacaoId, codigo, nome, dataHoje, triagemAgendadaId } = cenario

    const coordenador = await abrirComo(browser, 'COORDENADOR')
    const pesquisador = await abrirComo(browser, 'PESQUISADOR')

    try {
      await test.step('Coordenador: confere 1 de 2 vagas na lista e abre a ficha do participante ativo', async () => {
        await coordenador.pagina.goto(`/estudos/${estudoId}/participantes`)
        await coordenador.participantes.expectContador(1, 2)
        await coordenador.participantes.expectParticipante(codigo, nome)
        await coordenador.participantes.expectSituacao(codigo, 'Ativo')

        await coordenador.ficha.abrirPelaLista(nome)
        await coordenador.ficha.expectCabecalho(codigo, nome)
        await coordenador.ficha.expectResumo({ realizadas: 1, ausentes: 0, canceladas: 0, pendentes: 3 })
      })

      await test.step('Coordenador: preenche a retirada e revisa irreversibilidade e quantidade de visitas futuras afetadas', async () => {
        await coordenador.ficha.abrirDialogoRetirada()
        await coordenador.ficha.preencherRetirada({
          motivo: MOTIVO_VALIDO,
          solicitante: 'PARTICIPANTE',
          dataRetirada: dataHoje,
        })
        await coordenador.ficha.continuarParaConfirmacao()
        await coordenador.ficha.expectConfirmacaoRetirada({
          visitasFuturas: 2,
          motivo: MOTIVO_VALIDO,
          solicitanteRotulo: 'Participante',
        })
        await coordenador.pagina.evaluate(() => window.scrollTo(0, 0))
        await capturar(coordenador.pagina, 'rf16-retirada-confirmacao', { fullPage: false })
      })

      await test.step('Coordenador: confirma a retirada e verifica marca RETIRADO, histórico e visitas canceladas', async () => {
        await coordenador.ficha.confirmarRetirada(2)
        await coordenador.ficha.expectRetirado()
        await coordenador.ficha.expectDadosDaRetirada({
          motivo: MOTIVO_VALIDO,
          solicitanteRotulo: 'Participante',
        })
        await coordenador.ficha.expectResumo({ realizadas: 1, ausentes: 0, canceladas: 2, pendentes: 1 })
        await coordenador.ficha.expectVisitasNaOrdem([
          VISITA_REALIZADA,
          VISITA_FUTURA_1,
          VISITA_FUTURA_2,
          VISITA_NAO_AGENDADA,
        ])
        await coordenador.ficha.expectSituacaoDaVisita(VISITA_REALIZADA, 'Realizada')
        await coordenador.ficha.expectResultado(VISITA_REALIZADA, ATIVIDADE, '120/80 mmHg')
        await coordenador.ficha.expectSituacaoDaVisita(VISITA_FUTURA_1, 'Cancelada')
        await coordenador.ficha.expectMotivoDaVisita(VISITA_FUTURA_1, 'retirada do estudo')
        await coordenador.ficha.expectSituacaoDaVisita(VISITA_FUTURA_2, 'Cancelada')
        await coordenador.ficha.expectMotivoDaVisita(VISITA_FUTURA_2, 'retirada do estudo')
        await coordenador.ficha.expectSituacaoDaVisita(VISITA_NAO_AGENDADA, 'Não agendada')
        await coordenador.pagina.evaluate(() => window.scrollTo(0, 0))
        await capturar(coordenador.pagina, 'rf16-retirada-historico')
      })

      await test.step('Estado depois da retirada: sem botões de escrita na ficha, no agendamento e na visita; Avaliações com aviso', async () => {
        await coordenador.ficha.expectSemAcoesDeEscritaNaFicha()

        await coordenador.pagina.goto(`/estudos/${estudoId}/participantes/${participacaoId}/agendamento`)
        await coordenador.ficha.expectAgendamentoBloqueadoPorRetirada(VISITA_NAO_AGENDADA)

        await coordenador.pagina.goto(
          `/estudos/${estudoId}/participantes/${participacaoId}/visitas/${triagemAgendadaId}`,
        )
        await coordenador.ficha.expectVisitaBloqueadaPorRetirada()
        await coordenador.ficha.abrirAbaDaVisita('Avaliações')
        await coordenador.ficha.expectAvaliacoesRetirado(ATIVIDADE, '120/80')

        await pesquisador.pagina.goto(
          `/estudos/${estudoId}/participantes/${participacaoId}/visitas/${triagemAgendadaId}`,
        )
        await pesquisador.ficha.expectVisitaBloqueadaPorRetirada()
        await pesquisador.ficha.abrirAbaDaVisita('Avaliações')
        await pesquisador.ficha.expectAvaliacoesRetirado(ATIVIDADE, '120/80')
      })

      await test.step('Lista de participantes: marca RETIRADO, atalho Ver histórico e vagas continuam em 1 de 2', async () => {
        await coordenador.pagina.goto(`/estudos/${estudoId}/participantes`)
        await coordenador.participantes.expectContador(1, 2)
        await coordenador.participantes.expectParticipante(codigo, nome)
        await coordenador.participantes.expectSituacao(codigo, 'Retirado')
      })
    } finally {
      await coordenador.contexto.close()
      await pesquisador.contexto.close()
    }
  })

  test('motivo vazio ou curto bloqueia a confirmação e não grava a retirada', async ({ browser }) => {
    let cenario!: CenarioRetirada
    await test.step('Partida pela API: participante ativo com visitas futuras agendadas', async () => {
      cenario = await prepararParticipanteComVisitasFuturas()
    })
    const { estudoId, participacaoId } = cenario

    const coordenador = await abrirComo(browser, 'COORDENADOR')
    try {
      await test.step('Motivo vazio bloqueia o avanço e mostra mensagem de validação', async () => {
        await coordenador.ficha.goto(estudoId, participacaoId)
        await coordenador.ficha.abrirDialogoRetirada()
        await coordenador.ficha.continuarParaConfirmacao()
        await coordenador.ficha.expectErroNoDialogoRetirada(
          'Informe o motivo da retirada, com ao menos 5 caracteres.',
        )
        await coordenador.pagina.evaluate(() => window.scrollTo(0, 0))
        await capturar(coordenador.pagina, 'rf16-retirada-erro-motivo', { fullPage: false })
      })

      await test.step('Motivo com menos de 5 caracteres também bloqueia a confirmação', async () => {
        await coordenador.ficha.preencherRetirada({ motivo: 'abc' })
        await coordenador.ficha.continuarParaConfirmacao()
        await coordenador.ficha.expectErroNoDialogoRetirada(
          'Informe o motivo da retirada, com ao menos 5 caracteres.',
        )
        await coordenador.ficha.fecharDialogoRetirada()
      })

      await test.step('Nada foi gravado: participante permanece ativo e a API responde 404 para a retirada', async () => {
        await coordenador.ficha.goto(estudoId, participacaoId)
        await coordenador.ficha.expectAtivoNaFicha()
        await coordenador.ficha.expectSituacaoDaVisita(VISITA_FUTURA_1, 'Agendada')
        await coordenador.ficha.expectSituacaoDaVisita(VISITA_FUTURA_2, 'Agendada')

        const api = await ApiE2E.como('COORDENADOR')
        try {
          expect(await api.statusDaRetirada(participacaoId)).toBe(404)
        } finally {
          await api.encerrar()
        }
      })
    } finally {
      await coordenador.contexto.close()
    }
  })
})
