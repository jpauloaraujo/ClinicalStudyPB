import { test as base } from '@playwright/test'
import { BarraNavegacao } from '../pages/BarraNavegacao'
import { InicioPage } from '../pages/InicioPage'
import { LoginPage } from '../pages/LoginPage'
import type { PerfilE2E } from '../support/env'
import { caminhoSessao } from '../support/sessao'

export { expect } from '@playwright/test'
export { perfisE2E, type PerfilE2E } from '../support/env'

type Fixtures = {
  paginaLogin: LoginPage
  paginaInicio: InicioPage
  barra: BarraNavegacao
}

// `test` da aplicação: acrescenta os page objects. Novos page objects entram aqui.
export const test = base.extend<Fixtures>({
  paginaLogin: async ({ page }, usar) => {
    await usar(new LoginPage(page))
  },
  paginaInicio: async ({ page }, usar) => {
    await usar(new InicioPage(page))
  },
  barra: async ({ page }, usar) => {
    await usar(new BarraNavegacao(page))
  },
})

// Escolhe o perfil do arquivo de teste (já autenticado pela sessão salva no setup):
//   test.use(comoPerfil('PESQUISADOR'))
export function comoPerfil(perfil: PerfilE2E) {
  return { storageState: caminhoSessao(perfil) }
}
