import { expect, type Browser } from '@playwright/test'
import { VisitaAgendadaPage } from '../pages/VisitaAgendadaPage'
import { ApiE2E } from '../support/api'
import { capturar } from '../support/capturas'
import { caminhoSessao } from '../support/sessao'
import { nomeFicticio, sufixoUnico } from '../support/unico'
import { test } from '../fixtures'

// RF14 (T93): resultados fictícios das avaliações no navegador.
// Sucesso: o pesquisador abre a visita realizada pela agenda, registra o resultado de uma avaliação, vê o selo
// "registrado" e o contador e depois edita o valor; o coordenador abre a mesma aba e vê tudo somente leitura.
// Erro: valor vazio (ou só com espaços) mostra o erro, o Salvar fica desabilitado e nada é gravado.
// O estado de partida (visita REALIZADA com duas avaliações e uma atividade de outra categoria) é montado pela API,
// com dados únicos por execução e a data sugerida pelo servidor, então o spec não depende do dia em que roda.

const VISITA = 'Triagem'
const PRESSAO = 'Pressão arterial'
const DOR = 'Escala de dor'
// Ordem do nome; a "Coleta de sangue" (exame laboratorial) não é avaliação e não aparece na aba
const AVALIACOES = [DOR, PRESSAO]

type Cenario = {
  estudoId: string
  participacaoId: string
  codigo: string
  visitaAgendadaId: string
  pressaoId: string
  dorId: string
}

async function prepararVisitaRealizada(opcoes?: { resultadoDaPressao?: [string, string] }): Promise<Cenario> {
  const pesquisador = await ApiE2E.como('PESQUISADOR')
  const coordenador = await ApiE2E.como('COORDENADOR')
  try {
    const estudo = await pesquisador.estudoParaResultados(`Estudo Resultados ${sufixoUnico()}`)
    const { participacaoId, codigo } = await coordenador.participanteIncluido(estudo.estudoId, nomeFicticio('Participante'))
    const visitaAgendadaId = await coordenador.visitaRealizada(participacaoId, estudo.triagemId)
    if (opcoes?.resultadoDaPressao) {
      const [valor, unidade] = opcoes.resultadoDaPressao
      await pesquisador.registrarResultado(visitaAgendadaId, estudo.pressaoId, valor, unidade)
    }
    return {
      estudoId: estudo.estudoId,
      participacaoId,
      codigo,
      visitaAgendadaId,
      pressaoId: estudo.pressaoId,
      dorId: estudo.dorId,
    }
  } finally {
    await pesquisador.encerrar()
    await coordenador.encerrar()
  }
}

/** Resultados gravados no servidor, consultados pela API como pesquisador. */
async function resultadosNoServidor(visitaAgendadaId: string) {
  const api = await ApiE2E.como('PESQUISADOR')
  try {
    return await api.resultados(visitaAgendadaId)
  } finally {
    await api.encerrar()
  }
}

async function abrirComo(browser: Browser, perfil: 'COORDENADOR' | 'PESQUISADOR') {
  const contexto = await browser.newContext({ storageState: caminhoSessao(perfil), locale: 'pt-BR' })
  const pagina = await contexto.newPage()
  return { contexto, pagina, visita: new VisitaAgendadaPage(pagina) }
}

test.describe('resultados das avaliações da visita (RF14)', () => {
  test('o pesquisador registra e edita o resultado pela agenda e o coordenador vê a mesma aba somente leitura', async ({
    browser,
  }) => {
    let cenario!: Cenario
    await test.step('Partida pela API: participante incluído e Triagem REALIZADA com duas avaliações', async () => {
      cenario = await prepararVisitaRealizada()
    })
    const { estudoId, participacaoId, codigo, visitaAgendadaId, pressaoId } = cenario

    const pesquisador = await abrirComo(browser, 'PESQUISADOR')
    const coordenador = await abrirComo(browser, 'COORDENADOR')

    await test.step('Pesquisador: abre a visita realizada pela agenda e a aba Avaliações', async () => {
      await pesquisador.visita.abrirPelaAgenda(estudoId, VISITA, codigo)
      await pesquisador.visita.abrirAbaAvaliacoes()
      await pesquisador.visita.expectAtividades(AVALIACOES)
      await pesquisador.visita.expectContador(0, 2)
      await pesquisador.visita.expectSelo(PRESSAO, 'pendente')
      await pesquisador.visita.expectSelo(DOR, 'pendente')
    })

    await test.step('Pesquisador: registra valor e unidade; selo "registrado" e contador "1 de 2"', async () => {
      await pesquisador.visita.salvarResultado(PRESSAO, '120/80', 'mmHg')
      await pesquisador.visita.expectSelo(PRESSAO, 'registrado')
      await pesquisador.visita.expectSelo(DOR, 'pendente')
      await pesquisador.visita.expectContador(1, 2)
      await pesquisador.visita.expectUltimaAlteracao(PRESSAO)
      await capturar(pesquisador.pagina, 'rf14-resultado-registrado')
    })

    await test.step('Pesquisador: edita o valor e a alteração fica gravada', async () => {
      await pesquisador.visita.salvarResultado(PRESSAO, '130/85')
      await pesquisador.visita.expectContador(1, 2)
      await capturar(pesquisador.pagina, 'rf14-resultado-editado')

      // Recarregada a tela, o valor editado continua lá, e o servidor confirma: um resultado, com o valor novo
      await pesquisador.visita.goto(estudoId, participacaoId, visitaAgendadaId)
      await pesquisador.visita.abrirAbaAvaliacoes()
      await pesquisador.visita.expectValorNoCampo(PRESSAO, '130/85', 'mmHg')
      await pesquisador.visita.expectSelo(PRESSAO, 'registrado')
      await pesquisador.visita.expectContador(1, 2)
      const gravado = await resultadosNoServidor(visitaAgendadaId)
      expect(gravado.registrados).toBe(1)
      expect(gravado.atividades.find((a) => a.acaoId === pressaoId)?.resultado).toMatchObject({
        valor: '130/85',
        unidade: 'mmHg',
      })
    })

    await test.step('Coordenador: abre a mesma visita pela agenda e vê os resultados somente leitura', async () => {
      await coordenador.visita.abrirPelaAgenda(estudoId, VISITA, codigo)
      await coordenador.visita.abrirAbaAvaliacoes()
      await coordenador.visita.expectSomenteLeitura()
      await coordenador.visita.expectAtividades(AVALIACOES)
      await coordenador.visita.expectContador(1, 2)
      await coordenador.visita.expectResultadoSomenteLeitura(PRESSAO, '130/85', 'mmHg')
      await coordenador.visita.expectSemResultadoSomenteLeitura(DOR)
      await capturar(coordenador.pagina, 'rf14-resultado-leitura-coordenador')
    })

    await pesquisador.contexto.close()
    await coordenador.contexto.close()
  })

  test('valor vazio ou só com espaços mostra o erro e nada é gravado', async ({ browser }) => {
    let cenario!: Cenario
    await test.step('Partida pela API: Triagem REALIZADA com a pressão já registrada (120/80 mmHg)', async () => {
      cenario = await prepararVisitaRealizada({ resultadoDaPressao: ['120/80', 'mmHg'] })
    })
    const { estudoId, participacaoId, visitaAgendadaId, pressaoId, dorId } = cenario

    const pesquisador = await abrirComo(browser, 'PESQUISADOR')

    await test.step('Pesquisador: abre a aba Avaliações da visita', async () => {
      await pesquisador.visita.goto(estudoId, participacaoId, visitaAgendadaId)
      await pesquisador.visita.abrirAbaAvaliacoes()
      await pesquisador.visita.expectContador(1, 2)
    })

    await test.step('Avaliação pendente com valor só de espaços: erro, Salvar desabilitado e nada salvo', async () => {
      await pesquisador.visita.tentarSalvarComValorVazio(DOR, '   ')
      await pesquisador.visita.expectErroDeValorObrigatorio(DOR)
      await pesquisador.visita.expectSelo(DOR, 'pendente')
    })

    await test.step('Avaliação já registrada com o valor apagado: erro e o valor anterior continua gravado', async () => {
      await pesquisador.visita.tentarSalvarComValorVazio(PRESSAO)
      await pesquisador.visita.expectErroDeValorObrigatorio(PRESSAO)
      await pesquisador.visita.expectContador(1, 2)
      await capturar(pesquisador.pagina, 'rf14-resultado-erro-valor')
    })

    await test.step('Nada foi gravado: a tela recarregada e o servidor mostram o mesmo estado de antes', async () => {
      await pesquisador.visita.goto(estudoId, participacaoId, visitaAgendadaId)
      await pesquisador.visita.abrirAbaAvaliacoes()
      await pesquisador.visita.expectValorNoCampo(PRESSAO, '120/80', 'mmHg')
      await pesquisador.visita.expectSelo(DOR, 'pendente')
      await pesquisador.visita.expectContador(1, 2)

      const gravado = await resultadosNoServidor(visitaAgendadaId)
      expect(gravado.registrados).toBe(1)
      expect(gravado.atividades.find((a) => a.acaoId === pressaoId)?.resultado).toMatchObject({ valor: '120/80' })
      expect(gravado.atividades.find((a) => a.acaoId === dorId)?.resultado).toBeNull()
    })

    await pesquisador.contexto.close()
  })
})
