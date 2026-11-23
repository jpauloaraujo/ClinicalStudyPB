import { expect, type Locator, type Page } from '@playwright/test'

export type SeloResultado = 'registrado' | 'pendente'

// Tela de uma visita agendada (RF12, T77) e a aba Avaliações (RF14, T91): o pesquisador registra e edita o resultado
// fictício de cada atividade de avaliação de uma visita REALIZADA; o coordenador consulta somente leitura.
export class VisitaAgendadaPage {
  private readonly page: Page

  constructor(page: Page) {
    this.page = page
  }

  private readonly regiaoAvaliacoes = () => this.page.getByRole('region', { name: 'Avaliações da visita' })
  private readonly atividade = (nome: string): Locator =>
    this.regiaoAvaliacoes().getByRole('article', { name: nome })
  private readonly campoValor = (atividade: string) => this.page.getByLabel(`Valor (${atividade})`)
  private readonly campoUnidade = (atividade: string) => this.page.getByLabel(`Unidade (${atividade})`)
  private readonly botaoSalvar = (atividade: string) =>
    this.page.getByRole('button', { name: `Salvar (${atividade})` })

  private async expectTelaDaVisita() {
    await expect(this.page.getByRole('heading', { level: 2, name: 'Visita agendada' })).toBeVisible()
  }

  async goto(estudoId: string, participacaoId: string, visitaAgendadaId: string) {
    await this.page.goto(`/estudos/${estudoId}/participantes/${participacaoId}/visitas/${visitaAgendadaId}`)
    await this.expectTelaDaVisita()
  }

  /** Abre a visita pela aba Agenda do estudo (link "Abrir a visita <visita> de <código>"). */
  async abrirPelaAgenda(estudoId: string, visita: string, codigo: string) {
    await this.page.goto(`/estudos/${estudoId}/agenda`)
    await expect(this.page.getByRole('region', { name: 'Agenda do estudo' })).toBeVisible()
    await this.page.getByRole('link', { name: `Abrir a visita ${visita} de ${codigo}` }).click()
    await this.expectTelaDaVisita()
  }

  /**
   * Volta a página (e qualquer área rolada) ao topo. Usado antes das capturas de página inteira: com a página rolada,
   * o cabeçalho fixo aparece no meio da imagem.
   */
  async rolarParaOTopo() {
    await this.page.evaluate(() => {
      window.scrollTo(0, 0)
      document.querySelectorAll<HTMLElement>('*').forEach((elemento) => {
        if (elemento.scrollTop > 0) elemento.scrollTop = 0
      })
    })
  }

  async abrirAbaAvaliacoes() {
    await this.page.getByRole('navigation', { name: 'Seções da visita' }).getByRole('button', { name: 'Avaliações' }).click()
    await expect(this.regiaoAvaliacoes()).toBeVisible()
  }

  // ---- conferências ----

  async expectContador(registrados: number, total: number) {
    await expect(this.regiaoAvaliacoes().locator('.avaliacoes__contador')).toHaveText(
      `${registrados} de ${total} avaliações registradas`,
    )
  }

  /** Só as atividades da categoria avaliação aparecem, na ordem do nome. */
  async expectAtividades(nomes: string[]) {
    await expect(this.regiaoAvaliacoes().getByRole('article').getByRole('heading', { level: 3 })).toHaveText(nomes)
  }

  async expectSelo(atividade: string, selo: SeloResultado) {
    await expect(this.atividade(atividade).locator('.selo')).toHaveText(selo)
  }

  async expectValorNoCampo(atividade: string, valor: string, unidade?: string) {
    await expect(this.campoValor(atividade)).toHaveValue(valor)
    if (unidade !== undefined) {
      await expect(this.campoUnidade(atividade)).toHaveValue(unidade)
    }
  }

  async expectUltimaAlteracao(atividade: string) {
    await expect(this.atividade(atividade).locator('.avaliacao__autoria')).toContainText('Última alteração por')
  }

  // ---- registro e edição (pesquisador) ----

  /** Preenche valor (e unidade, se informada), salva e espera a confirmação "Resultado salvo.". */
  async salvarResultado(atividade: string, valor: string, unidade?: string) {
    await this.campoValor(atividade).fill(valor)
    if (unidade !== undefined) {
      await this.campoUnidade(atividade).fill(unidade)
    }
    await this.botaoSalvar(atividade).click()
    await expect(this.atividade(atividade).getByRole('status')).toHaveText('Resultado salvo.')
  }

  /**
   * Deixa o valor vazio (ou só com espaços) e tenta salvar com Enter no campo: a tela mostra o erro, o botão Salvar
   * fica desabilitado e nenhuma confirmação aparece.
   */
  async tentarSalvarComValorVazio(atividade: string, valorEmBranco = '') {
    await this.campoValor(atividade).fill(valorEmBranco)
    await this.campoValor(atividade).press('Enter')
  }

  async expectErroDeValorObrigatorio(atividade: string) {
    await expect(this.atividade(atividade).getByText('Informe o valor do resultado.')).toBeVisible()
    await expect(this.botaoSalvar(atividade)).toBeDisabled()
    await expect(this.atividade(atividade).getByRole('status')).toHaveCount(0)
  }

  // ---- leitura (coordenador) ----

  async expectSomenteLeitura() {
    await expect(this.regiaoAvaliacoes().getByText('Somente o pesquisador registra resultados.')).toBeVisible()
    await expect(this.regiaoAvaliacoes().getByRole('button', { name: /^Salvar/ })).toHaveCount(0)
    await expect(this.regiaoAvaliacoes().getByRole('textbox')).toHaveCount(0)
  }

  async expectResultadoSomenteLeitura(atividade: string, valor: string, unidade: string) {
    const cartao = this.atividade(atividade)
    await expect(cartao.locator('dd').first()).toHaveText(valor)
    await expect(cartao).toContainText(unidade)
    await expect(cartao.locator('.selo')).toHaveText('registrado')
  }

  async expectSemResultadoSomenteLeitura(atividade: string) {
    await expect(this.atividade(atividade)).toContainText('Sem resultado registrado.')
    await expect(this.atividade(atividade).locator('.selo')).toHaveText('pendente')
  }
}
