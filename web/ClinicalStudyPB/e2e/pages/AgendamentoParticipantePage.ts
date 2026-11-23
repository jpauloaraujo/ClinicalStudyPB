import { expect, type Locator, type Page } from '@playwright/test'

// Ficha de agendamento do participante (RF12, T77): lista as visitas previstas com a janela válida e a data sugerida;
// o coordenador agenda e reagenda, o pesquisador só consulta. Nomes acessíveis esperados: título "Agendamento de
// visitas", botões "Agendar <visita>" e "Reagendar <visita>", formulário com o nome do botão e campo "Data".
export class AgendamentoParticipantePage {
  private readonly page: Page

  constructor(page: Page) {
    this.page = page
  }

  private readonly linha = (visita: string): Locator =>
    this.page.getByRole('region', { name: 'Visitas do participante' }).getByRole('row').filter({ hasText: visita })

  async goto(estudoId: string, participacaoId: string) {
    await this.page.goto(`/estudos/${estudoId}/participantes/${participacaoId}/agendamento`)
    await this.expectTela()
  }

  /** Abre a ficha de agendamento pela aba Participantes (link "Agendamento de <código>"; para o pesquisador, "Ver agendamento de <código>"). */
  async abrirPelaLista(estudoId: string, codigo: string) {
    await this.page.goto(`/estudos/${estudoId}/participantes`)
    await this.page.getByRole('link', { name: new RegExp(`^(Ver agendamento|Agendamento) de ${codigo}$`) }).click()
    await this.expectTela()
  }

  private async expectTela() {
    await expect(this.page.getByRole('heading', { level: 2, name: 'Agendamento de visitas' })).toBeVisible()
  }

  /** A linha da visita mostra a janela válida ("dd/mm/aaaa a dd/mm/aaaa") e a data sugerida (dd/mm/aaaa). */
  async expectJanelaESugestao(visita: string, janela: string, dataSugerida: string) {
    const linha = this.linha(visita)
    await expect(linha).toContainText(janela)
    await expect(linha).toContainText(dataSugerida)
  }

  async expectSituacao(visita: string, situacao: string) {
    await expect(this.linha(visita)).toContainText(situacao)
  }

  /** Agenda com a data e a hora sugeridas pela tela e confere a confirmação; devolve o texto da confirmação. */
  async agendarComASugestao(visita: string): Promise<string> {
    await this.page.getByRole('button', { name: `Agendar ${visita}` }).click()
    const formulario = this.page.getByRole('form', { name: `Agendar ${visita}` })
    await expect(formulario.getByLabel('Data')).toHaveValue(/^\d{4}-\d{2}-\d{2}$/)
    await formulario.getByRole('button', { name: 'Salvar' }).click()
    const confirmacao = this.page.getByRole('status').filter({ hasText: `${visita} agendada para` })
    await expect(confirmacao).toBeVisible()
    return (await confirmacao.textContent()) ?? ''
  }

  /** Reagenda para a data (aaaa-mm-dd) e confere a confirmação; devolve o texto da confirmação. */
  async reagendarPara(visita: string, data: string): Promise<string> {
    await this.page.getByRole('button', { name: `Reagendar ${visita}` }).click()
    const formulario = this.page.getByRole('form', { name: `Reagendar ${visita}` })
    await formulario.getByLabel('Data').fill(data)
    await formulario.getByRole('button', { name: 'Salvar' }).click()
    const confirmacao = this.page.getByRole('status').filter({ hasText: `${visita} reagendada para` })
    await expect(confirmacao).toBeVisible()
    return (await confirmacao.textContent()) ?? ''
  }

  /** Tenta agendar numa data (aaaa-mm-dd) fora da janela: a mensagem de erro traz a janela válida e nada é gravado. */
  async tentarAgendarForaDaJanela(visita: string, data: string, janela: string) {
    await this.page.getByRole('button', { name: `Agendar ${visita}` }).click()
    const formulario = this.page.getByRole('form', { name: `Agendar ${visita}` })
    await formulario.getByLabel('Data').fill(data)
    await formulario.getByRole('button', { name: 'Salvar' }).click()
    await expect(formulario.getByText(`fora da janela válida desta visita (${janela})`)).toBeVisible()
    await expect(this.page.getByRole('status').filter({ hasText: `${visita} agendada para` })).toHaveCount(0)
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

  async cancelarFormulario() {
    await this.page.getByRole('button', { name: 'Cancelar' }).click()
  }

  /** Leitura somente: nenhum botão Agendar ou Reagendar e o aviso do perfil. */
  async expectSomenteLeitura() {
    await expect(this.page.getByRole('button', { name: /^Agendar|^Reagendar/ })).toHaveCount(0)
    await expect(this.page.getByText('Seu perfil tem acesso apenas para consulta do agendamento.')).toBeVisible()
  }
}
