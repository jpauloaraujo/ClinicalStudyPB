import { expect, type Page } from '@playwright/test'

export type DadosEstudo = {
  titulo: string
  objetivo: string
  dataInicio?: string
  dataFim?: string
  limite: number
}

// Lista e formulário de estudos (/estudos), RF04. O responsável é o pesquisador logado.
export class EstudosPage {
  private readonly page: Page

  constructor(page: Page) {
    this.page = page
  }

  async goto() {
    await this.page.goto('/estudos')
    await expect(this.page.getByRole('heading', { level: 1, name: 'Estudos clínicos' })).toBeVisible()
  }

  async abrirFormularioNovo() {
    await this.page.getByRole('link', { name: 'Novo estudo' }).click()
    await expect(this.page.getByRole('heading', { level: 1, name: 'Novo estudo' })).toBeVisible()
  }

  async preencher(dados: Partial<DadosEstudo>) {
    if (dados.titulo !== undefined) await this.page.getByLabel('Título').fill(dados.titulo)
    if (dados.objetivo !== undefined) await this.page.getByLabel('Objetivo').fill(dados.objetivo)
    if (dados.dataInicio) await this.page.getByLabel('Data de início').fill(dados.dataInicio)
    if (dados.dataFim) await this.page.getByLabel('Data de término').fill(dados.dataFim)
    if (dados.limite !== undefined) await this.page.getByLabel('Limite de participantes').fill(String(dados.limite))
  }

  async salvar() {
    await this.page.getByRole('button', { name: 'Salvar' }).click()
  }

  // Cadastro completo; ao salvar a aplicação volta para a listagem
  async cadastrar(dados: DadosEstudo) {
    await this.goto()
    await this.abrirFormularioNovo()
    await this.preencher(dados)
    await this.salvar()
    await this.expectNaLista(dados.titulo)
  }

  async expectNaLista(titulo: string) {
    await expect(this.page).toHaveURL(/\/estudos$/)
    await expect(this.page.getByRole('link', { name: titulo, exact: true })).toBeVisible()
  }

  async abrir(titulo: string) {
    await this.page.getByRole('link', { name: titulo, exact: true }).click()
    await expect(this.page.getByRole('heading', { level: 1, name: titulo })).toBeVisible()
  }

  async expectErroDeCampo(mensagem: string) {
    await expect(this.page.getByText(mensagem)).toBeVisible()
  }

  async expectNaoVeBotaoNovo() {
    await expect(this.page.getByRole('link', { name: 'Novo estudo' })).toHaveCount(0)
  }
}
