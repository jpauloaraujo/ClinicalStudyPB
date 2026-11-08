import { expect, type Page } from '@playwright/test'

export type DadosUsuario = { nome: string; email: string; senha: string; perfil?: string }

// Lista e formulário de usuários (/usuarios), tela do administrador (RF01)
export class UsuariosPage {
  private readonly page: Page

  constructor(page: Page) {
    this.page = page
  }

  async goto() {
    await this.page.goto('/usuarios')
    await expect(this.page.getByRole('heading', { level: 1, name: 'Usuários' })).toBeVisible()
  }

  async cadastrar(dados: DadosUsuario) {
    await this.page.getByRole('link', { name: 'Novo usuário' }).click()
    await expect(this.page.getByRole('heading', { level: 1, name: 'Novo usuário' })).toBeVisible()
    await this.page.getByLabel('Nome').fill(dados.nome)
    await this.page.getByLabel('E-mail').fill(dados.email)
    await this.page.getByLabel('Senha', { exact: true }).fill(dados.senha)
    if (dados.perfil) {
      await this.page.getByLabel('Perfil').selectOption({ label: dados.perfil })
    }
    await this.page.getByRole('button', { name: 'Salvar' }).click()
  }

  // Depois de salvar, a aplicação volta para a listagem com o usuário novo
  async expectNaListagem(nome: string, email: string) {
    await expect(this.page).toHaveURL(/\/usuarios$/)
    const linha = this.page.getByRole('row').filter({ hasText: email })
    await expect(linha).toBeVisible()
    await expect(linha).toContainText(nome)
  }
}
