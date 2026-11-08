import { expect, type Page } from '@playwright/test'

// Aba Visitas (RF06): cartões expansíveis ordenados pelo dia previsto, com as atividades de cada visita
export class VisitasPage {
  private readonly page: Page

  constructor(page: Page) {
    this.page = page
  }

  async adicionarVisita(nome: string, diaPrevisto: number, tolerancia = 0) {
    const formulario = this.page.getByRole('form', { name: 'Nova visita' })
    await formulario.getByLabel(/^Nome da visita/).fill(nome)
    await formulario.getByLabel(/^Dia previsto/).fill(String(diaPrevisto))
    await formulario.getByLabel(/^Tolerância/).fill(String(tolerancia))
    await formulario.getByRole('button', { name: 'Adicionar visita' }).click()
    await expect(this.page.getByText(`Dia ${diaPrevisto} · ${nome}`, { exact: true })).toBeVisible()
  }

  // As atividades ficam dentro do cartão da visita, que começa fechado
  async adicionarAtividade(nomeVisita: string, nome: string, categoria: string) {
    const cartao = this.page.locator('details').filter({ has: this.page.locator('summary', { hasText: nomeVisita }) })
    await cartao.locator('summary').click()
    const formulario = this.page.getByRole('form', { name: `Nova atividade da visita ${nomeVisita}` })
    await formulario.getByLabel(/^Nome da atividade/).fill(nome)
    await formulario.getByLabel(/^Categoria/).selectOption({ label: categoria })
    await formulario.getByRole('button', { name: 'Adicionar atividade' }).click()
    // Depois de salvar a lista é recarregada e o cartão pode voltar fechado: reabre até a atividade aparecer
    await expect(async () => {
      if (!(await cartao.evaluate((el) => (el as HTMLDetailsElement).open))) {
        await cartao.locator('summary').click()
      }
      await expect(cartao.getByRole('listitem').filter({ hasText: nome })).toBeVisible({ timeout: 1_000 })
    }).toPass()
  }

  async expectSomenteLeitura() {
    await expect(this.page.getByRole('form')).toHaveCount(0)
    await expect(this.page.getByRole('button', { name: /^Excluir/ })).toHaveCount(0)
  }

  async expectVisitasNaOrdem(rotulos: string[]) {
    await expect(this.page.getByText(/^Dia \d+ · /)).toHaveText(rotulos)
  }
}
