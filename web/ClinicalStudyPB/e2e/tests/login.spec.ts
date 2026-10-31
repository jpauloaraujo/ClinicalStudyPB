import { expect, perfisE2E, test } from '../fixtures'
import { credenciaisDe } from '../support/env'

test.describe('login', () => {
  for (const perfil of perfisE2E) {
    test(`entra com credenciais válidas: ${perfil}`, async ({ paginaLogin }) => {
      const { email, senha } = credenciaisDe(perfil)

      await paginaLogin.goto()
      await paginaLogin.login(email, senha)

      await paginaLogin.expectLoggedIn()
      expect(await paginaLogin.lerToken()).toBeTruthy()
    })
  }

  test('rejeita senha errada e não cria sessão', async ({ paginaLogin }) => {
    const { email } = credenciaisDe('PESQUISADOR')

    await paginaLogin.goto()
    await paginaLogin.login(email, 'senha-incorreta-123')

    await paginaLogin.expectErrorMessage('Credenciais inválidas')
    await paginaLogin.expectOnLoginPage()
    expect(await paginaLogin.lerToken()).toBeNull()
  })

  test('rejeita e-mail inexistente com a mesma mensagem, sem revelar qual campo errou', async ({ paginaLogin }) => {
    await paginaLogin.goto()
    await paginaLogin.login('nao.existe.e2e@clinicalstudy.test', 'senha-qualquer-123')

    await paginaLogin.expectErrorMessage('Credenciais inválidas')
    await paginaLogin.expectOnLoginPage()
    expect(await paginaLogin.lerToken()).toBeNull()
  })
})

test.describe('logout', () => {
  test('volta ao login, bloqueia rota protegida e o Voltar não reabre a área autenticada', async ({
    paginaLogin,
    barra,
  }) => {
    const { email, senha } = credenciaisDe('PESQUISADOR')
    await paginaLogin.goto()
    await paginaLogin.login(email, senha)
    await paginaLogin.expectLoggedIn()
    await barra.abrirRota('/estudos')

    await barra.sair()

    await paginaLogin.expectOnLoginPage()
    expect(await paginaLogin.lerToken()).toBeNull()

    await barra.abrirRota('/estudos')
    await paginaLogin.expectOnLoginPage()

    await barra.voltar()
    await paginaLogin.expectOnLoginPage()
  })
})
