// Liga o Mock Service Worker quando VITE_USE_MOCKS=true; com a variável desligada (padrão) nada é
// carregado e as chamadas vão para o backend real. O import dinâmico mantém o MSW fora do bundle normal.
export async function iniciarMocks(): Promise<void> {
  if (import.meta.env.VITE_USE_MOCKS !== 'true') return
  const { worker } = await import('./browser')
  await worker.start({ onUnhandledFrame: 'bypass' })
  console.info('[mocks] Mock Service Worker ligado: a API é fictícia (VITE_USE_MOCKS=true)')
}
