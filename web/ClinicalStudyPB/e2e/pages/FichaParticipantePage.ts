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

  // ---- retirada de participante (RF16, T104/T107) ----

  private readonly dialogoFormularioRetirada = () => this.page.getByRole('dialog', { name: 'Registrar retirada' })
  private readonly dialogoConfirmacaoRetirada = () =>
    this.page.getByRole('dialog', { name: 'Confirmar retirada do participante' })
  private readonly regiaoDadosRetirada = () => this.page.getByRole('region', { name: 'Dados da retirada' })

  async abrirDialogoRetirada() {
    await this.page.getByRole('button', { name: 'Registrar retirada' }).click()
    await expect(this.dialogoFormularioRetirada()).toBeVisible()
  }

  async preencherRetirada(dados: {
    motivo?: string
    solicitante?: 'PARTICIPANTE' | 'PESQUISADOR' | 'COORDENACAO'
    dataRetirada?: string
  }) {
    const dialogo = this.dialogoFormularioRetirada()
    if (dados.motivo !== undefined) {
      await dialogo.getByLabel(/^Motivo da retirada/).fill(dados.motivo)
    }
    if (dados.solicitante !== undefined) {
      await dialogo.getByLabel(/^Quem solicitou/).selectOption(dados.solicitante)
    }
    if (dados.dataRetirada !== undefined) {
      await dialogo.getByLabel(/^Data da retirada/).fill(dados.dataRetirada)
    }
  }

  async continuarParaConfirmacao() {
    await this.dialogoFormularioRetirada().getByRole('button', { name: 'Continuar' }).click()
  }

  async fecharDialogoRetirada() {
    await this.dialogoFormularioRetirada().getByRole('button', { name: 'Cancelar' }).click()
    await expect(this.dialogoFormularioRetirada()).toHaveCount(0)
  }

  async expectErroNoDialogoRetirada(mensagem: string) {
    await expect(this.dialogoFormularioRetirada()).toBeVisible()
    await expect(this.dialogoConfirmacaoRetirada()).toHaveCount(0)
    await expect(this.dialogoFormularioRetirada().getByText(mensagem)).toBeVisible()
  }

  async expectConfirmacaoRetirada(esperado: {
    visitasFuturas: number
    motivo: string
    solicitanteRotulo: string
  }) {
    const dialogo = this.dialogoConfirmacaoRetirada()
    await expect(dialogo).toBeVisible()
    await expect(dialogo).toContainText('Esta retirada é irreversível')
    await expect(dialogo).toContainText('RETIRADO')
    const fraseVisitas =
      esperado.visitasFuturas === 0
        ? 'Nenhuma visita futura agendada será cancelada.'
        : esperado.visitasFuturas === 1
          ? '1 visita futura agendada será cancelada'
          : `${esperado.visitasFuturas} visitas futuras agendadas serão canceladas`
    await expect(dialogo).toContainText(fraseVisitas)
    await expect(dialogo).toContainText(esperado.solicitanteRotulo)
    await expect(dialogo).toContainText(esperado.motivo)
  }

  async confirmarRetirada(visitasCanceladas: number) {
    const dialogo = this.dialogoConfirmacaoRetirada()
    await dialogo.getByRole('button', { name: 'Confirmar retirada' }).click()
    await expect(dialogo).toHaveCount(0)
    const trecho =
      visitasCanceladas === 0
        ? 'Nenhuma visita futura agendada precisou ser cancelada.'
        : visitasCanceladas === 1
          ? '1 visita futura agendada foi cancelada.'
          : `${visitasCanceladas} visitas futuras agendadas foram canceladas.`
    await expect(this.page.getByRole('status').filter({ hasText: `Retirada registrada com sucesso. ${trecho}` })).toBeVisible()
  }

  async expectRetirado() {
    await expect(this.page.locator('.titulo-com-selo .selo--retirado')).toHaveText('Retirado')
    await expect(this.regiaoDadosRetirada()).toBeVisible()
  }

  async expectAtivoNaFicha() {
    await expect(this.regiaoDadosRetirada()).toHaveCount(0)
    await expect(this.page.locator('.titulo-com-selo .selo--retirado')).toHaveCount(0)
    await expect(this.page.getByRole('button', { name: 'Registrar retirada' })).toBeVisible()
  }

  async expectDadosDaRetirada(esperado: { motivo: string; solicitanteRotulo: string }) {
    const regiao = this.regiaoDadosRetirada()
    await expect(regiao).toContainText('Participante retirado do estudo')
    await expect(regiao).toContainText(esperado.solicitanteRotulo)
    await expect(regiao).toContainText(esperado.motivo)
  }

  async expectMotivoDaVisita(nome: string, motivo: string) {
    await this.expandirVisita(nome)
    await expect(this.visita(nome).locator('.visita-historico__corpo')).toContainText(motivo)
  }

  async expectSemAcoesDeEscritaNaFicha() {
    await expect(this.page.getByRole('region', { name: 'Ações do participante' })).toHaveCount(0)
    await expect(this.page.getByRole('link', { name: 'Agendar visitas' })).toHaveCount(0)
    await expect(this.page.getByRole('button', { name: 'Registrar retirada' })).toHaveCount(0)
    await expect(this.page.getByRole('button', { name: 'Vincular conta' })).toHaveCount(0)
  }

  async expectAgendamentoBloqueadoPorRetirada(visitaEsperada: string) {
    await expect(
      this.page.getByRole('status').filter({
        hasText: 'Participante retirado do estudo: não é possível agendar nem reagendar visitas.',
      }),
    ).toBeVisible()
    await expect(this.page.locator('.titulo-com-selo .selo--retirado')).toHaveText('Retirado')
    await expect(this.page.getByRole('cell', { name: visitaEsperada })).toBeVisible()
    await expect(this.page.getByRole('button', { name: /^Agendar|^Reagendar/ })).toHaveCount(0)
  }

  async expectVisitaBloqueadaPorRetirada() {
    await expect(
      this.page.getByRole('status').filter({
        hasText: 'Participante retirado do estudo: as ações de edição desta visita estão indisponíveis.',
      }),
    ).toBeVisible()
    await expect(this.page.locator('.titulo-com-selo .selo--retirado')).toHaveText('Retirado')
    await expect(this.page.getByRole('button', { name: /^Reagendar|^Registrar desfecho/ })).toHaveCount(0)
  }

  async expectAvaliacoesRetirado(atividade: string, valorPreservado: string) {
    const aviso = this.page.getByRole('status').filter({
      hasText:
        'O participante foi retirado do estudo: os resultados ficam disponíveis somente para consulta e não podem mais ser registrados nem editados.',
    })
    await expect(aviso).toBeVisible()
    await expect(aviso.locator('.selo--retirado')).toHaveText('Retirado')
    await expect(this.page.getByRole('button', { name: `Salvar (${atividade})` })).toHaveCount(0)
    await expect(this.page.getByLabel(`Valor (${atividade})`)).toHaveCount(0)
    await expect(this.page.getByRole('article', { name: atividade })).toContainText(valorPreservado)
  }
}
