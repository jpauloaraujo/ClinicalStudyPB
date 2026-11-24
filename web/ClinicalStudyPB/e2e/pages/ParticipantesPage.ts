import { expect, type Page } from '@playwright/test'

// Aba Participantes (T61): lista dos incluídos e o contador "X de N vagas". Nomes acessíveis esperados: tabela
// com o código e o nome fictício de cada participante e um texto no formato "1 de 5 vagas".
export class ParticipantesPage {
  private readonly page: Page

  constructor(page: Page) {
    this.page = page
  }

  async expectContador(incluidos: number, limite: number) {
    await expect(this.page.getByText(new RegExp(`\\b${incluidos} de ${limite} vagas`))).toBeVisible()
  }

  async expectParticipante(codigo: string, nomeFicticio: string) {
    const linha = this.page.getByRole('row').filter({ hasText: codigo })
    await expect(linha).toContainText(nomeFicticio)
  }

  async expectSituacao(codigo: string, situacao: 'Ativo' | 'Retirado') {
    const linha = this.page.getByRole('row').filter({ hasText: codigo })
    await expect(linha.locator('.selo')).toHaveText(situacao)
    if (situacao === 'Retirado') {
      await expect(linha.getByRole('link', { name: `Ver histórico de ${codigo}` })).toBeVisible()
      await expect(linha.getByRole('link', { name: `Agendamento de ${codigo}` })).toHaveCount(0)
    }
  }
}
