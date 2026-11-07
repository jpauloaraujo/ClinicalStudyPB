import { expect, type Page } from '@playwright/test'

// Aba Critérios (RF05): listas de inclusão e exclusão e o formulário de cadastro
export class CriteriosPage {
  private readonly page: Page

  constructor(page: Page) {
    this.page = page
  }

  private readonly inclusao = () => this.page.getByRole('region', { name: 'Critérios de inclusão' })
  private readonly exclusao = () => this.page.getByRole('region', { name: 'Critérios de exclusão' })

  async adicionar(tipo: 'INCLUSAO' | 'EXCLUSAO', titulo: string, descricao: string) {
    const formulario = this.page.getByRole('form', { name: 'Novo critério' })
    await formulario.getByLabel('Tipo').selectOption(tipo)
    await formulario.getByLabel('Título').fill(titulo)
    await formulario.getByLabel('Descrição').fill(descricao)
    await formulario.getByRole('button', { name: 'Adicionar critério' }).click()
    const lista = tipo === 'INCLUSAO' ? this.inclusao() : this.exclusao()
    await expect(lista.getByRole('listitem').filter({ hasText: titulo })).toBeVisible()
  }

  async expectSomenteLeitura() {
    await expect(this.page.getByRole('form')).toHaveCount(0)
    await expect(this.page.getByRole('button', { name: 'Editar' })).toHaveCount(0)
    await expect(this.page.getByRole('button', { name: 'Excluir' })).toHaveCount(0)
  }

  async expectAvisoSemInclusao() {
    await expect(this.page.getByText('Adicione ao menos um critério de inclusão para avançar.')).toBeVisible()
  }
}
