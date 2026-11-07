import { ApiE2E } from '../support/api'
import { capturar } from '../support/capturas'
import { consentimentoPronto, inclusaoPronta, MOTIVO_CONSENTIMENTO, MOTIVO_INCLUSAO } from '../support/pendencias'
import { nomeFicticio, sufixoUnico } from '../support/unico'
import { comoPerfil, expect, test } from '../fixtures'

// Erros de regra no fluxo de recrutamento (RQ-12 da T66). A preparação do cenário é feita pela API; a regra
// violada é provada pela interface.

test.describe('regras do planejamento (pesquisador)', () => {
  test.use(comoPerfil('PESQUISADOR'))

  test('um estudo sem critério de inclusão e sem visita não avança, e o motivo aparece na tela', async ({ page, paginaEstudo }) => {
    const pesquisador = await ApiE2E.como('PESQUISADOR')
    const estudoId = await pesquisador.criarEstudo(`Estudo Regra ${sufixoUnico()}`)

    await paginaEstudo.goto(estudoId)
    await paginaEstudo.tentarAvancarEEsperarRecusa('Recrutamento', /critério de inclusão/i)
    await paginaEstudo.expectEstado('Planejamento')
    await capturar(page, 'regra-01-sem-criterio')

    await pesquisador.criarCriterio(estudoId, 'INCLUSAO', 'Idade adulta', 'Candidato com 18 anos ou mais')
    await paginaEstudo.goto(estudoId)
    await paginaEstudo.tentarAvancarEEsperarRecusa('Recrutamento', /visita/i)
    await paginaEstudo.expectEstado('Planejamento')
    await pesquisador.encerrar()
  })
})

test.describe('regras do recrutamento (coordenador)', () => {
  test.use(comoPerfil('COORDENADOR'))

  test('candidato com o mesmo nome e nascimento no estudo é recusado como duplicado', async ({ page, paginaCandidatos }) => {
    const pesquisador = await ApiE2E.como('PESQUISADOR')
    const estudoId = await pesquisador.estudoEmRecrutamento(`Estudo Duplicado ${sufixoUnico()}`)
    await pesquisador.encerrar()
    const nome = nomeFicticio('Duplicada')

    await page.goto(`/estudos/${estudoId}/candidatos`)
    await paginaCandidatos.cadastrar(nome)
    await paginaCandidatos.tentarCadastrar(nome)

    await paginaCandidatos.expectErroDoServidor(/já existe um candidato/i)
    await capturar(page, 'regra-02-candidato-duplicado')
  })

  test('com o estudo fora de recrutamento o cadastro de candidato fica desabilitado, com explicação', async ({
    page,
    paginaCandidatos,
  }) => {
    const pesquisador = await ApiE2E.como('PESQUISADOR')
    const estudoId = await pesquisador.criarEstudo(`Estudo Planejamento ${sufixoUnico()}`)
    await pesquisador.encerrar()

    await page.goto(`/estudos/${estudoId}/candidatos`)

    await paginaCandidatos.expectCadastroDesabilitado()
  })

  test('a avaliação incompleta não pode ser salva: o botão só habilita com todos os critérios respondidos', async ({
    page,
    paginaFicha,
  }) => {
    const pesquisador = await ApiE2E.como('PESQUISADOR')
    const coordenador = await ApiE2E.como('COORDENADOR')
    const estudoId = await pesquisador.estudoEmRecrutamento(`Estudo Incompleta ${sufixoUnico()}`)
    const candidatoId = await coordenador.cadastrarCandidato(estudoId, nomeFicticio('Incompleta'))
    await pesquisador.encerrar()
    await coordenador.encerrar()

    await page.goto(`/estudos/${estudoId}/candidatos/${candidatoId}`)
    await paginaFicha.expectSalvarAvaliacaoDesabilitado()
    await paginaFicha.responderCriterio('Idade adulta', true)
    await paginaFicha.expectSalvarAvaliacaoDesabilitado()
    await paginaFicha.responderCriterio('Gestante', false)
    await paginaFicha.salvarAvaliacao()

    await paginaFicha.expectResultado('Candidato elegível')
  })

  test('candidato que atende a uma exclusão é não elegível, com o critério reprovador, e a reavaliação preserva o histórico', async ({
    page,
    paginaFicha,
  }) => {
    const pesquisador = await ApiE2E.como('PESQUISADOR')
    const coordenador = await ApiE2E.como('COORDENADOR')
    const estudoId = await pesquisador.estudoEmRecrutamento(`Estudo Inelegivel ${sufixoUnico()}`)
    const candidatoId = await coordenador.cadastrarCandidato(estudoId, nomeFicticio('Inelegivel'))
    await pesquisador.encerrar()
    await coordenador.encerrar()

    await page.goto(`/estudos/${estudoId}/candidatos/${candidatoId}`)
    await paginaFicha.responderCriterio('Idade adulta', true)
    await paginaFicha.responderCriterio('Gestante', true)
    await paginaFicha.salvarAvaliacao()
    await paginaFicha.expectResultado('Candidato não elegível')
    await paginaFicha.expectCriterioReprovador('Gestante (exclusão aplicada)')
    await paginaFicha.expectSituacao('Não elegível')
    await capturar(page, 'regra-03-candidato-nao-elegivel')

    // O formulário recomeça vazio depois de salvar: a reavaliação responde a todos os critérios de novo
    await paginaFicha.expectSalvarAvaliacaoDesabilitado()
    await paginaFicha.responderCriterio('Idade adulta', true)
    await paginaFicha.responderCriterio('Gestante', false)
    await paginaFicha.salvarAvaliacao()
    await paginaFicha.expectResultado('Candidato elegível')
    await paginaFicha.expectHistorico(2)
  })

  test('candidato sem consentimento não pode ser incluído', async ({ page }) => {
    test.fixme(!inclusaoPronta(), MOTIVO_INCLUSAO)

    const pesquisador = await ApiE2E.como('PESQUISADOR')
    const coordenador = await ApiE2E.como('COORDENADOR')
    const estudoId = await pesquisador.estudoEmRecrutamento(`Estudo Sem Consentimento ${sufixoUnico()}`)
    const candidatoId = await coordenador.cadastrarCandidato(estudoId, nomeFicticio('SemConsentimento'))
    await coordenador.avaliar(estudoId, candidatoId, true)
    await pesquisador.encerrar()
    await coordenador.encerrar()

    await page.goto(`/estudos/${estudoId}/candidatos/${candidatoId}`)

    const inclusao = page.getByRole('region', { name: 'Inclusão', exact: true })
    await expect(inclusao.getByRole('button', { name: 'Incluir no estudo' })).toBeDisabled()
    await expect(inclusao).toContainText(/consentimento/i)
  })

  test('quem já consentiu vê o comprovante e não pode registrar o consentimento de novo', async ({ page, paginaFicha }) => {
    test.fixme(!consentimentoPronto(), MOTIVO_CONSENTIMENTO)

    const pesquisador = await ApiE2E.como('PESQUISADOR')
    const coordenador = await ApiE2E.como('COORDENADOR')
    const estudoId = await pesquisador.estudoEmRecrutamento(`Estudo Consent ${sufixoUnico()}`)
    const candidatoId = await coordenador.candidatoPronto(estudoId, nomeFicticio('Consentida'))
    await pesquisador.encerrar()
    await coordenador.encerrar()

    await page.goto(`/estudos/${estudoId}/candidatos/${candidatoId}`)

    await paginaFicha.expectComprovante()
    await expect(page.getByRole('region', { name: 'Consentimento', exact: true }).getByRole('button', { name: 'Registrar' })).toHaveCount(0)
  })
})
