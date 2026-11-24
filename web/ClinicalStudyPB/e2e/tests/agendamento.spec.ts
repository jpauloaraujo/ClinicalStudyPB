import { expect as esperar } from '@playwright/test'
import { ApiE2E } from '../support/api'
import { capturar } from '../support/capturas'
import { nomeFicticio, sufixoUnico } from '../support/unico'
import { comoPerfil, test } from '../fixtures'

// Agendamento de visitas (RF12, T79): o coordenador agenda com a data sugerida, vê a visita na agenda do estudo e
// reagenda; uma data fora da janela é recusada com a janela válida na mensagem; o pesquisador só consulta. O estado de
// partida (estudo em recrutamento, participante incluído hoje, triagem no dia 0 e retorno no dia 5 ± 2) é montado pela
// API com dados únicos por execução; o fluxo testado é feito pela interface.

const VISITA = 'Retorno'

/** aaaa-mm-dd da data local daqui a `dias` dias (o participante é incluído hoje, então a janela parte de hoje). */
function dia(dias: number): string {
  const data = new Date()
  data.setDate(data.getDate() + dias)
  const dois = (n: number) => String(n).padStart(2, '0')
  return `${data.getFullYear()}-${dois(data.getMonth() + 1)}-${dois(data.getDate())}`
}

/** dd/mm/aaaa, como a tela mostra. */
function ptBr(isoData: string): string {
  const [ano, mes, diaDoMes] = isoData.split('-')
  return `${diaDoMes}/${mes}/${ano}`
}

// Retorno: dia previsto 5, tolerância 2, participante incluído hoje -> janela de hoje + 3 a hoje + 7, sugestão hoje + 5
const JANELA = `${ptBr(dia(3))} a ${ptBr(dia(7))}`

type Cenario = { estudoId: string; participacaoId: string; codigo: string; nome: string }

async function prepararCenario(): Promise<Cenario> {
  const pesquisador = await ApiE2E.como('PESQUISADOR')
  const coordenador = await ApiE2E.como('COORDENADOR')
  try {
    const { estudoId } = await pesquisador.estudoParaAgendamento(`Estudo Agendamento ${sufixoUnico()}`)
    const nome = nomeFicticio('Participante')
    const { participacaoId, codigo } = await coordenador.participanteIncluido(estudoId, nome)
    return { estudoId, participacaoId, codigo, nome }
  } finally {
    await pesquisador.encerrar()
    await coordenador.encerrar()
  }
}

test.describe('coordenador agenda visitas (RF12)', () => {
  test.use(comoPerfil('COORDENADOR'))

  test('agenda com a data sugerida, vê a visita na agenda do estudo e reagenda para outro dia da janela', async ({
    page,
    paginaAgendamento,
    paginaAgenda,
  }) => {
    let cenario!: Cenario
    await test.step('Partida pela API: estudo em recrutamento, participante incluído e visita prevista com janela futura', async () => {
      cenario = await prepararCenario()
    })
    const { estudoId, codigo } = cenario

    await test.step('Abre Participantes, entra no agendamento e vê a janela e a data sugerida', async () => {
      await paginaAgendamento.abrirPelaLista(estudoId, codigo)
      await paginaAgendamento.expectJanelaESugestao(VISITA, JANELA, ptBr(dia(5)))
      await paginaAgendamento.expectSituacao(VISITA, 'Não agendada')
    })

    await test.step('Agenda com a data sugerida e confere o rótulo "Agendada"', async () => {
      const confirmacao = await paginaAgendamento.agendarComASugestao(VISITA)
      esperar(confirmacao).toContain(ptBr(dia(5)))
      await paginaAgendamento.expectSituacao(VISITA, 'Agendada')
      await paginaAgendamento.rolarParaOTopo()
      await capturar(page, 'rf12-agendamento-sucesso')
    })

    await test.step('A visita aparece na agenda do estudo', async () => {
      await paginaAgenda.goto(estudoId)
      await paginaAgenda.expectVisita(VISITA, codigo)
      await paginaAgenda.expectDataHora(codigo, ptBr(dia(5)))
      await paginaAgenda.rolarParaOTopo()
      await capturar(page, 'rf12-agenda')
    })

    await test.step('Reagenda para outro dia da janela e confere a nova data', async () => {
      await paginaAgendamento.abrirPelaLista(estudoId, codigo)
      const confirmacao = await paginaAgendamento.reagendarPara(VISITA, dia(7))
      esperar(confirmacao).toContain(ptBr(dia(7)))
      await paginaAgendamento.expectSituacao(VISITA, 'Agendada')
      await paginaAgenda.goto(estudoId)
      await paginaAgenda.expectDataHora(codigo, ptBr(dia(7)))
    })
  })

  test('recusa a data fora da janela, mostra a janela válida e não grava nada', async ({
    page,
    paginaAgendamento,
    paginaAgenda,
  }) => {
    const { estudoId, participacaoId, codigo } = await prepararCenario()

    await paginaAgendamento.goto(estudoId, participacaoId)
    await paginaAgendamento.tentarAgendarForaDaJanela(VISITA, dia(10), JANELA)
    await paginaAgendamento.rolarParaOTopo()
    await capturar(page, 'rf12-agendamento-erro-janela')

    await paginaAgendamento.cancelarFormulario()
    await paginaAgendamento.expectSituacao(VISITA, 'Não agendada')
    await paginaAgenda.goto(estudoId)
    await esperar(page.getByRole('link', { name: `Abrir a visita ${VISITA} de ${codigo}` })).toHaveCount(0)
  })
})

test.describe('pesquisador consulta o agendamento (RF12)', () => {
  test.use(comoPerfil('PESQUISADOR'))

  test('abre Participantes e Agenda em leitura, sem os botões Agendar e Reagendar', async ({
    page,
    paginaAgendamento,
    paginaAgenda,
    paginaEstudo,
  }) => {
    const { estudoId, participacaoId, codigo } = await prepararCenario()
    const coordenador = await ApiE2E.como('COORDENADOR')
    try {
      const visitas = await coordenador.visitasDoParticipante(participacaoId)
      const retorno = visitas.find((visita) => visita.nome === VISITA)!
      await coordenador.agendar(participacaoId, retorno.visitaPlanejadaId, `${dia(5)}T09:00`)
    } finally {
      await coordenador.encerrar()
    }

    await paginaEstudo.goto(estudoId, 'participantes')
    await paginaEstudo.expectAbasVisiveis(['Participantes', 'Agenda'])

    await paginaAgendamento.abrirPelaLista(estudoId, codigo)
    await paginaAgendamento.expectSituacao(VISITA, 'Agendada')
    await paginaAgendamento.expectSomenteLeitura()

    await paginaAgenda.goto(estudoId)
    await paginaAgenda.expectVisita(VISITA, codigo)
    await paginaAgenda.expectSemBotoesDeAgendar()
    await esperar(page.getByRole('region', { name: 'Agenda do estudo' })).toBeVisible()
  })
})
