import { expect, type Locator, type Page } from '@playwright/test'

export type ResumoEsperado = { realizadas: number; ausentes: number; canceladas: number; pendentes: number }

// Ficha do participante com o histórico de visitas (RF15, T97): cartões de resumo, linha do tempo das visitas
// previstas (cartões expansíveis) e a área de ações do perfil. Também cobre o agendamento (RF12) e a tela da visita
// (RF13 e RF14), que o fluxo de acompanhamento percorre em seguida.
export class FichaParticipantePage {
  private readonly page: Page

  constructor(page: Page) {
    this.page = page
  }

  private readonly regiaoResumo = () => this.page.getByRole('region', { name: 'Resumo do histórico' })
  private readonly regiaoVisitas = () => this.page.getByRole('region', { name: 'Visitas do participante' })

  async goto(estudoId: string, participacaoId: string) {
    await this.page.goto(`/estudos/${estudoId}/participantes/${participacaoId}`)
    await expect(this.regiaoResumo()).toBeVisible()
  }

  /** Abre a ficha pela aba Participantes (link "Abrir a ficha de ..."). */
  async abrirPelaLista(nomeFicticio: string) {
    await this.page.getByRole('link', { name: `Abrir a ficha de ${nomeFicticio}` }).click()
    await expect(this.regiaoResumo()).toBeVisible()
  }

  async expectCabecalho(codigo: string, nomeFicticio: string) {
    await expect(this.page.getByRole('heading', { level: 2, name: `${codigo} · ${nomeFicticio}` })).toBeVisible()
  }

  // Cada cartão tem o número e o rótulo; confere o número do cartão pelo rótulo
  async expectResumo(esperado: ResumoEsperado) {
    const valores: [string, number][] = [
      ['Realizadas', esperado.realizadas],
      ['Ausentes', esperado.ausentes],
      ['Canceladas', esperado.canceladas],
      ['Pendentes', esperado.pendentes],
    ]
    for (const [rotulo, valor] of valores) {
      const cartao = this.regiaoResumo().getByRole('listitem').filter({ hasText: rotulo })
      await expect(cartao.locator('.cartao-resumo__valor')).toHaveText(String(valor))
    }
  }

  async expectVisitasNaOrdem(nomes: string[]) {
    await expect(this.regiaoVisitas().locator('.visita-historico__nome')).toHaveText(nomes)
  }

  private visita(nome: string): Locator {
    return this.regiaoVisitas().getByRole('listitem').filter({ has: this.page.locator('.visita-historico__nome', { hasText: nome }) })
  }

  async expectSituacaoDaVisita(nome: string, situacao: string) {
    await expect(this.visita(nome).getByRole('heading', { level: 4 })).toContainText(situacao)
  }

  async expandirVisita(nome: string) {
    const botao = this.visita(nome).getByRole('button', { name: new RegExp(nome) })
    if ((await botao.getAttribute('aria-expanded')) !== 'true') {
      await botao.click()
    }
    await expect(botao).toHaveAttribute('aria-expanded', 'true')
  }

  async expectResultado(visita: string, atividade: string, valor: string) {
    await this.expandirVisita(visita)
    const resultado = this.visita(visita).locator('.resultado-historico').filter({ hasText: atividade })
    await expect(resultado).toContainText(valor)
  }

  async abrirVisita(nome: string) {
    await this.expandirVisita(nome)
    await this.visita(nome).getByRole('link', { name: 'Abrir visita' }).click()
  }

  async expectComLinkDeAgendamento() {
    await expect(this.page.getByRole('link', { name: 'Agendar visitas' })).toBeVisible()
  }

  async expectSemAgendamento() {
    await expect(this.page.getByRole('link', { name: 'Agendar visitas' })).toHaveCount(0)
    await expect(this.page.getByRole('button', { name: /^Agendar|^Reagendar|^Registrar desfecho/ })).toHaveCount(0)
  }

  // ---- agendamento (tela do coordenador) ----

  async abrirAgendamentoPelaLista(codigo: string) {
    await this.page.getByRole('link', { name: `Agendamento de ${codigo}` }).click()
    await expect(this.page.getByRole('heading', { level: 2, name: 'Agendamento de visitas' })).toBeVisible()
  }

  /** Agenda a visita com a data e a hora sugeridas pela tela e confere a confirmação. */
  async agendarComASugestao(visita: string) {
    await this.page.getByRole('button', { name: `Agendar ${visita}` }).click()
    const formulario = this.page.getByRole('form', { name: `Agendar ${visita}` })
    await formulario.getByRole('button', { name: 'Salvar' }).click()
    await expect(this.page.getByRole('status').filter({ hasText: `${visita} agendada para` })).toBeVisible()
  }

  async expectSemBotoesDeAgendar() {
    await expect(this.page.getByRole('button', { name: /^Agendar|^Reagendar/ })).toHaveCount(0)
    await expect(this.page.getByText('Seu perfil tem acesso apenas para consulta do agendamento.')).toBeVisible()
  }

  async abrirVisitaNoAgendamento(visita: string) {
    await this.page.getByRole('link', { name: `Abrir a visita ${visita}` }).click()
    await expect(this.page.getByRole('heading', { level: 2, name: 'Visita agendada' })).toBeVisible()
  }

  // ---- tela da visita (desfecho e avaliações) ----

  async registrarRealizada() {
    await this.page.getByRole('button', { name: 'Registrar desfecho' }).click()
    const dialogo = this.page.getByRole('dialog', { name: 'Registrar desfecho da visita' })
    await dialogo.getByRole('radio', { name: 'Realizada' }).check()
    await dialogo.getByRole('button', { name: 'Confirmar' }).click()
    await expect(dialogo).toHaveCount(0)
    await expect(this.page.getByRole('status').filter({ hasText: 'Desfecho registrado: visita realizada.' })).toBeVisible()
  }

  async abrirAbaDaVisita(aba: 'Dados' | 'Avaliações') {
    await this.page.getByRole('navigation', { name: 'Seções da visita' }).getByRole('button', { name: aba }).click()
    if (aba === 'Avaliações') {
      await expect(this.page.getByRole('region', { name: 'Avaliações da visita' })).toBeVisible()
    }
  }

  async registrarResultadoDaAvaliacao(atividade: string, valor: string, unidade: string) {
    await this.page.getByLabel(`Valor (${atividade})`).fill(valor)
    await this.page.getByLabel(`Unidade (${atividade})`).fill(unidade)
    await this.page.getByRole('button', { name: `Salvar (${atividade})` }).click()
    await expect(this.page.getByRole('article', { name: atividade }).getByRole('status')).toBeVisible()
  }

  async expectAvaliacoesSomenteLeitura(atividade: string) {
    await expect(this.page.getByText('Somente o pesquisador registra resultados.')).toBeVisible()
    await expect(this.page.getByRole('button', { name: `Salvar (${atividade})` })).toHaveCount(0)
    await expect(this.page.getByLabel(`Valor (${atividade})`)).toHaveCount(0)
  }
}
