import { expect, type Page } from '@playwright/test'

// Aba Candidatos (RF08): lista paginada com filtro por situação e formulário de cadastro (só o coordenador)
export class CandidatosPage {
  private readonly page: Page

  constructor(page: Page) {
    this.page = page
  }

  private readonly formulario = () => this.page.getByRole('region', { name: 'Cadastrar candidato' })
  private readonly botaoCadastrar = () => this.formulario().getByRole('button', { name: 'Cadastrar candidato' })

  async preencher(nomeFicticio: string, dataNascimento = '1990-04-12', contato?: string) {
    await this.formulario().getByLabel('Nome fictício').fill(nomeFicticio)
    await this.formulario().getByLabel('Data de nascimento').fill(dataNascimento)
    if (contato) await this.formulario().getByLabel('Contato (opcional)').fill(contato)
  }

  // Cadastra e devolve o código gerado pelo sistema (CAND-001, CAND-002...)
  async cadastrar(nomeFicticio: string, dataNascimento = '1990-04-12'): Promise<string> {
    await this.preencher(nomeFicticio, dataNascimento)
    await this.botaoCadastrar().click()
    const aviso = this.formulario().getByRole('status').filter({ hasText: /cadastrado\.$/ })
    await expect(aviso).toBeVisible()
    const codigo = /CAND-\d+/.exec((await aviso.textContent()) ?? '')?.[0]
    expect(codigo, 'o aviso de cadastro deve informar o código gerado').toBeTruthy()
    return codigo as string
  }

  async tentarCadastrar(nomeFicticio: string, dataNascimento = '1990-04-12') {
    await this.preencher(nomeFicticio, dataNascimento)
    await this.botaoCadastrar().click()
  }

  async expectErroDoServidor(mensagem: string | RegExp) {
    await expect(this.formulario().getByRole('alert')).toContainText(mensagem)
  }

  async expectNaLista(codigo: string, nomeFicticio: string) {
    const linha = this.page.getByRole('row').filter({ hasText: codigo })
    await expect(linha).toContainText(nomeFicticio)
  }

  async expectSituacao(codigo: string, situacao: string) {
    await expect(this.page.getByRole('row').filter({ hasText: codigo })).toContainText(situacao)
  }

  async abrirFicha(codigo: string) {
    await this.page.getByRole('link', { name: `Abrir a ficha de ${codigo}` }).click()
    await expect(this.page.getByRole('heading', { level: 2, name: new RegExp(`^${codigo} · `) })).toBeVisible()
  }

  async expectCadastroDesabilitado() {
    await expect(this.botaoCadastrar()).toBeDisabled()
    await expect(this.formulario()).toContainText('só podem ser cadastrados com o estudo em recrutamento')
  }

  async expectSemFormularioDeCadastro() {
    await expect(this.page.getByRole('region', { name: 'Cadastrar candidato' })).toHaveCount(0)
  }
}
