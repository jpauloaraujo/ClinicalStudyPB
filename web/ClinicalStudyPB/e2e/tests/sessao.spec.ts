import { comoPerfil, perfisE2E, test } from '../fixtures'

// Rota protegida aberta já autenticado, sem passar pela tela de login
for (const perfil of perfisE2E) {
  test.describe(`sessão reaproveitada: ${perfil}`, () => {
    test.use(comoPerfil(perfil))

    test('abre a área autenticada sem passar pelo login', async ({ paginaInicio, paginaLogin }) => {
      await paginaInicio.goto()
      await paginaLogin.expectLoggedIn()
      await paginaInicio.expectGreeting()
    })
  })
}
