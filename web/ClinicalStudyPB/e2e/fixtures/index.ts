import { test as base } from '@playwright/test'
import { BarraNavegacao } from '../pages/BarraNavegacao'
import { CandidatosPage } from '../pages/CandidatosPage'
import { CriteriosPage } from '../pages/CriteriosPage'
import { EstudoDetalhePage } from '../pages/EstudoDetalhePage'
import { EstudosPage } from '../pages/EstudosPage'
import { FichaCandidatoPage } from '../pages/FichaCandidatoPage'
import { InicioPage } from '../pages/InicioPage'
import { LoginPage } from '../pages/LoginPage'
import { ParticipantesPage } from '../pages/ParticipantesPage'
import { UsuariosPage } from '../pages/UsuariosPage'
import { VisitasPage } from '../pages/VisitasPage'
import type { PerfilE2E } from '../support/env'
import { caminhoSessao } from '../support/sessao'

export { expect } from '@playwright/test'
export { perfisE2E, type PerfilE2E } from '../support/env'

type Fixtures = {
  paginaLogin: LoginPage
  paginaInicio: InicioPage
  barra: BarraNavegacao
  paginaUsuarios: UsuariosPage
  paginaEstudos: EstudosPage
  paginaEstudo: EstudoDetalhePage
  paginaCriterios: CriteriosPage
  paginaVisitas: VisitasPage
  paginaCandidatos: CandidatosPage
  paginaFicha: FichaCandidatoPage
  paginaParticipantes: ParticipantesPage
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
  paginaUsuarios: async ({ page }, usar) => {
    await usar(new UsuariosPage(page))
  },
  paginaEstudos: async ({ page }, usar) => {
    await usar(new EstudosPage(page))
  },
  paginaEstudo: async ({ page }, usar) => {
    await usar(new EstudoDetalhePage(page))
  },
  paginaCriterios: async ({ page }, usar) => {
    await usar(new CriteriosPage(page))
  },
  paginaVisitas: async ({ page }, usar) => {
    await usar(new VisitasPage(page))
  },
  paginaCandidatos: async ({ page }, usar) => {
    await usar(new CandidatosPage(page))
  },
  paginaFicha: async ({ page }, usar) => {
    await usar(new FichaCandidatoPage(page))
  },
  paginaParticipantes: async ({ page }, usar) => {
    await usar(new ParticipantesPage(page))
  },
})

// Escolhe o perfil do arquivo de teste (já autenticado pela sessão salva no setup):
//   test.use(comoPerfil('PESQUISADOR'))
export function comoPerfil(perfil: PerfilE2E) {
  return { storageState: caminhoSessao(perfil) }
}
