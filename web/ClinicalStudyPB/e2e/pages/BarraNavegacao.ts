import { expect, type Page } from '@playwright/test'

// Cabeçalho da área autenticada, onde fica a ação de logout
export class BarraNavegacao {
  private readonly page: Page

  constructor(page: Page) {
    this.page = page
  }

  // Abre uma rota da aplicação direto pela URL (ex.: '/estudos')
  async abrirRota(caminho: string) {
    await this.page.goto(caminho)
  }

  async voltar() {
    await this.page.goBack()
  }

  async sair() {
    await this.page.getByRole('button', { name: 'Sair' }).click()
    await expect(this.page).toHaveURL(/\/login$/)
  }
}
