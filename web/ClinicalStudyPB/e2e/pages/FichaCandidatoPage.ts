import { expect, type Locator, type Page } from '@playwright/test'

// Ficha do candidato (/estudos/:id/candidatos/:candidatoId) com as seções de avaliação (RF09), consentimento
// (RF10) e inclusão (RF11). Avaliação já existe na web (T55). Consentimento (T58) e inclusão (T61) seguem o
// contrato de nomes acessíveis definido nos documentos de design dessas tasks; veja `pendencias.ts`.
export class FichaCandidatoPage {
  private readonly page: Page

  constructor(page: Page) {
    this.page = page
  }

  private readonly avaliacao = () => this.page.getByRole('region', { name: 'Avaliação de elegibilidade', exact: true })
  private readonly consentimento = () => this.page.getByRole('region', { name: 'Consentimento', exact: true })
  private readonly inclusao = () => this.page.getByRole('region', { name: 'Inclusão', exact: true })

  private grupo(titulo: string): Locator {
    return this.avaliacao().getByRole('radiogroup', { name: `Resposta para ${titulo}` })
  }

  // ---- Avaliação de elegibilidade (T55)

  async responderCriterio(titulo: string, seAplica: boolean) {
    await this.grupo(titulo).getByLabel(seAplica ? 'Se aplica' : 'Não se aplica', { exact: true }).check()
  }

  async expectSalvarAvaliacaoDesabilitado() {
    await expect(this.avaliacao().getByRole('button', { name: 'Salvar avaliação' })).toBeDisabled()
  }

  async salvarAvaliacao() {
    const botao = this.avaliacao().getByRole('button', { name: 'Salvar avaliação' })
    await expect(botao).toBeEnabled()
    await botao.click()
  }

  async expectResultado(resultado: 'Candidato elegível' | 'Candidato não elegível') {
    await expect(this.avaliacao().getByRole('status').filter({ hasText: resultado })).toBeVisible()
  }

  async expectCriterioReprovador(texto: string) {
    await expect(this.avaliacao().getByText(texto)).toBeVisible()
  }

  async expectHistorico(quantidade: number) {
    await expect(this.avaliacao().getByRole('list', { name: 'Histórico de avaliações' }).getByRole('listitem')).toHaveCount(
      quantidade,
    )
  }

  async expectSituacao(situacao: string) {
    await expect(this.page.getByRole('region', { name: 'Dados do candidato' })).toContainText(situacao)
  }

  // ---- Consentimento (T58): nomes acessíveis esperados
  //   região "Consentimento"; caixa de confirmação (nome contém "confirm"); botão "Registrar"; comprovante com
  //   a palavra "Comprovante", a versão do termo e o responsável.

  async registrarConsentimento() {
    const caixa = this.consentimento().getByRole('checkbox', { name: /confirm/i })
    await caixa.check()
    await this.consentimento().getByRole('button', { name: 'Registrar' }).click()
  }

  async expectComprovante(versaoTermo = 'TCLE-SIMULADO v1') {
    await expect(this.consentimento().getByText(/Comprovante/i)).toBeVisible()
    await expect(this.consentimento()).toContainText(versaoTermo)
  }

  // ---- Inclusão (T61): nomes acessíveis esperados
  //   região "Inclusão"; as quatro pré-condições em uma lista; botão "Incluir no estudo"; diálogo de confirmação
  //   com o botão "Confirmar".

  async expectPreCondicoesAtendidas() {
    await expect(this.inclusao().getByRole('button', { name: 'Incluir no estudo' })).toBeEnabled()
  }

  async incluirNoEstudo() {
    await this.inclusao().getByRole('button', { name: 'Incluir no estudo' }).click()
    await this.page.getByRole('dialog').getByRole('button', { name: 'Confirmar' }).click()
  }

  async expectIncluido() {
    await expect(this.inclusao()).toContainText('incluído')
    await this.expectSituacao('Incluído')
  }

  // Sem vaga, a pré-condição "Vaga disponível" fica pendente com o texto "Vagas esgotadas" e o botão desabilitado
  async expectInclusaoBloqueadaPorVagas() {
    await expect(this.inclusao()).toContainText(/vagas esgotadas/i)
    await expect(this.inclusao().getByRole('button', { name: 'Incluir no estudo' })).toBeDisabled()
  }

  async expectInclusaoRecusada(mensagem: string | RegExp) {
    await expect(this.inclusao().getByRole('alert')).toContainText(mensagem)
  }
}
