import { ApiE2E } from '../support/api'
import { sufixoUnico } from '../support/unico'
import { comoPerfil, expect, test } from '../fixtures'

// Controle de acesso por perfil na web (RQ-13 da T66, RF03). O backend também recusa (403); aqui se prova o
// que cada perfil enxerga.

test.describe('pesquisador', () => {
  test.use(comoPerfil('PESQUISADOR'))

  test('vê Critérios e Visitas e o botão de avançar, mas não as abas de recrutamento', async ({ page, paginaEstudo }) => {
    const api = await ApiE2E.como('PESQUISADOR')
    const estudoId = await api.estudoEmRecrutamento(`Estudo Acesso P ${sufixoUnico()}`)
    await api.encerrar()

    await paginaEstudo.goto(estudoId)

    await paginaEstudo.expectAbasVisiveis(['Resumo', 'Critérios', 'Visitas'])
    await paginaEstudo.expectAbasOcultas(['Candidatos', 'Participantes'])
    await expect(page.getByRole('button', { name: 'Avançar para Execução' })).toBeVisible()
  })

  test('digitar a URL de Candidatos mostra acesso negado', async ({ page }) => {
    const api = await ApiE2E.como('PESQUISADOR')
    const estudoId = await api.estudoEmRecrutamento(`Estudo Acesso P2 ${sufixoUnico()}`)
    await api.encerrar()

    await page.goto(`/estudos/${estudoId}/candidatos`)

    await expect(page.getByText(/acesso negado/i).first()).toBeVisible()
  })
})

test.describe('coordenador', () => {
  test.use(comoPerfil('COORDENADOR'))

  test('vê Candidatos e Participantes, mas não Critérios, Visitas nem o botão de avançar o estado', async ({
    paginaEstudo,
    paginaEstudos,
  }) => {
    const api = await ApiE2E.como('PESQUISADOR')
    const estudoId = await api.estudoEmRecrutamento(`Estudo Acesso C ${sufixoUnico()}`)
    await api.encerrar()

    await paginaEstudos.goto()
    await paginaEstudos.expectNaoVeBotaoNovo()
    await paginaEstudo.goto(estudoId)

    await paginaEstudo.expectAbasVisiveis(['Resumo', 'Candidatos', 'Participantes'])
    await paginaEstudo.expectAbasOcultas(['Critérios', 'Visitas'])
    await paginaEstudo.expectSemBotaoDeAvancar()
  })
})

test.describe('administrador', () => {
  test.use(comoPerfil('ADMINISTRADOR'))

  test('não acessa os estudos: o menu só tem Início e Usuários e a URL de estudos mostra acesso negado', async ({ page }) => {
    await page.goto('/inicio')
    const menu = page.getByRole('navigation', { name: 'Menu principal' })
    await expect(menu.getByRole('link', { name: 'Usuários' })).toBeVisible()
    await expect(menu.getByRole('link', { name: 'Estudos' })).toHaveCount(0)

    await page.goto('/estudos')

    await expect(page.getByText(/acesso negado/i).first()).toBeVisible()
  })
})
