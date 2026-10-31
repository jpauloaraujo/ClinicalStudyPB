import { expect, type Page } from '@playwright/test'
import { CHAVE_TOKEN } from '../support/sessao'

// Tela de login (/login). Os testes só chamam estes métodos, sem seletores soltos.
export class LoginPage {
  private readonly page: Page

  constructor(page: Page) {
    this.page = page
  }

  private readonly campoEmail = () => this.page.getByLabel('E-mail')
  private readonly campoSenha = () => this.page.getByLabel('Senha', { exact: true })
  private readonly botaoEntrar = () => this.page.getByRole('button', { name: 'Entrar' })
  private readonly alertaErro = () => this.page.getByRole('alert')

  async goto() {
    await this.page.goto('/login')
    await expect(this.botaoEntrar()).toBeVisible()
  }

  async login(email: string, senha: string) {
    await this.campoEmail().fill(email)
    await this.campoSenha().fill(senha)
    await this.botaoEntrar().click()
  }

  // Entrou: foi levado à área autenticada (/inicio) e o botão "Sair" está visível
  async expectLoggedIn() {
    await expect(this.page).toHaveURL(/\/inicio$/)
    await expect(this.page.getByRole('button', { name: 'Sair' })).toBeVisible()
  }

  // Continua na tela de login, sem entrar
  async expectOnLoginPage() {
    await expect(this.page).toHaveURL(/\/login$/)
    await expect(this.botaoEntrar()).toBeVisible()
  }

  async expectErrorMessage(mensagem: string | RegExp) {
    await expect(this.alertaErro()).toHaveText(mensagem)
  }

  // Estado de autenticação guardado pelo front (token no localStorage)
  async lerToken(): Promise<string | null> {
    return this.page.evaluate((chave) => localStorage.getItem(chave), CHAVE_TOKEN)
  }
}
