import { expect, type Page } from '@playwright/test'

export type AbaDoEstudo = 'Resumo' | 'Critérios' | 'Visitas' | 'Candidatos' | 'Participantes'
export type EstadoDoEstudo = 'Planejamento' | 'Recrutamento' | 'Execução' | 'Encerrado'

// Página de detalhe do estudo (/estudos/:id): cabeçalho, abas e o indicador de estado com o botão de avançar (RF07)
export class EstudoDetalhePage {
  private readonly page: Page

  constructor(page: Page) {
    this.page = page
  }

  private readonly abas = () => this.page.getByRole('navigation', { name: 'Seções do estudo' })
  private readonly indicador = () => this.page.getByRole('list', { name: 'Etapas do estudo' })
  private readonly alerta = () => this.page.getByRole('alert')

  async goto(estudoId: string, aba: string = 'resumo') {
    await this.page.goto(`/estudos/${estudoId}/${aba}`)
    await expect(this.abas()).toBeVisible()
  }

  async abrirAba(aba: AbaDoEstudo) {
    await this.abas().getByRole('link', { name: aba, exact: true }).click()
  }

  async expectAbasVisiveis(abas: AbaDoEstudo[]) {
    for (const aba of abas) {
      await expect(this.abas().getByRole('link', { name: aba, exact: true })).toBeVisible()
    }
  }

  async expectAbasOcultas(abas: AbaDoEstudo[]) {
    for (const aba of abas) {
      await expect(this.abas().getByRole('link', { name: aba, exact: true })).toHaveCount(0)
    }
  }

  // O estado atual aparece no cabeçalho ("Estado atual: X") e destacado no indicador das quatro etapas
  async expectEstado(estado: EstadoDoEstudo) {
    await expect(this.page.getByText(`Estado atual: ${estado}`)).toBeVisible()
    await expect(this.indicador().locator('[aria-current="step"]')).toHaveText(estado)
  }

  async avancarPara(estado: EstadoDoEstudo) {
    await this.page.getByRole('button', { name: `Avançar para ${estado}` }).click()
    const dialogo = this.page.getByRole('dialog', { name: `Avançar para ${estado}?` })
    await expect(dialogo).toContainText('Não é possível voltar')
    await dialogo.getByRole('button', { name: 'Confirmar' }).click()
    await expect(dialogo).toHaveCount(0)
  }

  async expectSemBotaoDeAvancar() {
    await expect(this.page.getByRole('button', { name: /^Avançar para/ })).toHaveCount(0)
  }

  // Tentativa de avançar que o backend recusa (409): o diálogo fecha e o motivo aparece em um alerta
  async tentarAvancarEEsperarRecusa(estado: EstadoDoEstudo, motivo: string | RegExp) {
    await this.page.getByRole('button', { name: `Avançar para ${estado}` }).click()
    await this.page.getByRole('dialog', { name: `Avançar para ${estado}?` }).getByRole('button', { name: 'Confirmar' }).click()
    await expect(this.alerta()).toContainText(motivo)
  }
}
