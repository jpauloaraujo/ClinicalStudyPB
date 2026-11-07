import type { Page } from '@playwright/test'

// Capturas nomeadas dos passos-chave (RQ-15 da T66), salvas em test-results/capturas/ para o README e para a
// apresentação. No CI a pasta test-results é enviada como artefato.
export async function capturar(page: Page, nome: string): Promise<void> {
  await page.screenshot({ path: `test-results/capturas/${nome}.png`, fullPage: true })
}
