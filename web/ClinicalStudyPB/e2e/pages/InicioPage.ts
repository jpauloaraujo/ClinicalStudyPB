import { expect, type Page } from '@playwright/test'

// Página inicial da área autenticada (/inicio)
export class InicioPage {
  private readonly page: Page

  constructor(page: Page) {
    this.page = page
  }

  async goto() {
    await this.page.goto('/inicio')
  }

  async expectGreeting() {
    await expect(this.page.getByRole('heading', { level: 1 })).toContainText('Olá')
  }
}
