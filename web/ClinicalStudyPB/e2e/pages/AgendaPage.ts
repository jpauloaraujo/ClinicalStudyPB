import { expect, type Page } from '@playwright/test'

// Aba Agenda do estudo (RF12, T77): visitas agendadas de todos os participantes, ordenadas por data, com filtros e
// paginação. Região "Agenda do estudo"; cada linha tem o link "Abrir a visita <visita> de <código>".
export class AgendaPage {
  private readonly page: Page

  constructor(page: Page) {
    this.page = page
  }

  private readonly regiao = () => this.page.getByRole('region', { name: 'Agenda do estudo' })

  async goto(estudoId: string) {
    await this.page.goto(`/estudos/${estudoId}/agenda`)
    await expect(this.regiao()).toBeVisible()
  }

  /** Volta a página (e qualquer área rolada) ao topo: com a página rolada, o cabeçalho fixo cai no meio da captura. */
  async rolarParaOTopo() {
    await this.page.evaluate(() => {
      window.scrollTo(0, 0)
      document.querySelectorAll<HTMLElement>('*').forEach((elemento) => {
        if (elemento.scrollTop > 0) elemento.scrollTop = 0
      })
    })
  }

  /** A visita do participante aparece na agenda, com o link para abri-la. */
  async expectVisita(visita: string, codigo: string) {
    await expect(this.regiao().getByRole('link', { name: `Abrir a visita ${visita} de ${codigo}` })).toBeVisible()
  }

  async expectDataHora(codigo: string, dataHora: string) {
    await expect(this.regiao().getByRole('row').filter({ hasText: codigo })).toContainText(dataHora)
  }

  /** A agenda é só de leitura: nenhum botão de agendar ou reagendar. */
  async expectSemBotoesDeAgendar() {
    await expect(this.page.getByRole('button', { name: /^Agendar|^Reagendar/ })).toHaveCount(0)
  }
}
