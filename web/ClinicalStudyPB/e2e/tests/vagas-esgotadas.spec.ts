import { CandidatosPage } from '../pages/CandidatosPage'
import { EstudoDetalhePage } from '../pages/EstudoDetalhePage'
import { FichaCandidatoPage } from '../pages/FichaCandidatoPage'
import { ParticipantesPage } from '../pages/ParticipantesPage'
import { ApiE2E } from '../support/api'
import { capturar } from '../support/capturas'
import { inclusaoPronta, MOTIVO_INCLUSAO } from '../support/pendencias'
import { nomeFicticio, sufixoUnico } from '../support/unico'
import { comoPerfil, expect, test } from '../fixtures'

// Erro de regra do RF11 (RQ-08 da T66): com o estudo de limite 1, a segunda inclusão é bloqueada com a
// mensagem de vagas esgotadas. O estudo, os candidatos elegíveis e o consentimento são preparados pela API
// (helpers de support/api.ts); a inclusão e o bloqueio são provados pela interface.

test.describe('limite de vagas', () => {
  test.use(comoPerfil('COORDENADOR'))

  test('com uma vaga, o primeiro candidato é incluído ("1 de 1") e o segundo é bloqueado por vagas esgotadas', async ({ page }) => {
    test.fixme(!inclusaoPronta(), MOTIVO_INCLUSAO)

    const pesquisador = await ApiE2E.como('PESQUISADOR')
    const coordenador = await ApiE2E.como('COORDENADOR')
    const titulo = `Estudo Vagas ${sufixoUnico()}`
    const estudoId = await pesquisador.estudoEmRecrutamento(titulo, 1)
    const nomeSegundo = nomeFicticio('Segundo')
    const idPrimeiro = await coordenador.candidatoPronto(estudoId, nomeFicticio('Primeiro'))
    const idSegundo = await coordenador.candidatoPronto(estudoId, nomeSegundo)
    await pesquisador.encerrar()
    await coordenador.encerrar()

    const ficha = new FichaCandidatoPage(page)
    const participantes = new ParticipantesPage(page)
    const estudo = new EstudoDetalhePage(page)
    const candidatos = new CandidatosPage(page)

    await test.step('O primeiro candidato é incluído e o contador mostra "1 de 1"', async () => {
      await page.goto(`/estudos/${estudoId}/candidatos/${idPrimeiro}`)
      await ficha.expectPreCondicoesAtendidas()
      await ficha.incluirNoEstudo()
      await ficha.expectIncluido()
      await estudo.abrirAba('Participantes')
      await participantes.expectContador(1, 1)
      await capturar(page, 'vagas-01-uma-de-uma')
    })

    await test.step('O segundo candidato, elegível e com consentimento, não pode ser incluído: vagas esgotadas', async () => {
      await page.goto(`/estudos/${estudoId}/candidatos/${idSegundo}`)
      await ficha.expectInclusaoBloqueadaPorVagas()
      await capturar(page, 'vagas-02-vagas-esgotadas')
    })

    await test.step('A situação do segundo candidato não mudou e o contador continua "1 de 1"', async () => {
      await page.goto(`/estudos/${estudoId}/candidatos`)
      await candidatos.expectSituacao('CAND-002', 'Consentido')
      await estudo.abrirAba('Participantes')
      await participantes.expectContador(1, 1)
      await expect(page.getByRole('row').filter({ hasText: nomeSegundo })).toHaveCount(0)
    })
  })
})
