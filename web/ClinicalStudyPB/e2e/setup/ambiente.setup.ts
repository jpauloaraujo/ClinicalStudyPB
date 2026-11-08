import { expect, request, test as setup } from '@playwright/test'
import { exigirVariaveis, urlApi, urlBase } from '../support/env'

// RQ-01 da T66: antes de qualquer teste de fluxo, confirma que o ambiente é o real. O backend responde, a web
// abre e o Mock Service Worker NÃO está ativo (os testes E2E nunca devem rodar contra respostas fictícias).
setup('o ambiente responde e a web não usa mocks', async ({ page }) => {
  exigirVariaveis()

  const api = await request.newContext({ baseURL: urlApi() })
  const documentacao = await api.get('/v3/api-docs')
  expect(documentacao.ok(), `o backend não respondeu em ${urlApi()} (HTTP ${documentacao.status()})`).toBeTruthy()
  await api.dispose()

  const resposta = await page.goto(urlBase())
  expect(resposta?.ok(), `a web não respondeu em ${urlBase()}`).toBeTruthy()
  const registros = await page.evaluate(async () => (await navigator.serviceWorker?.getRegistrations?.())?.length ?? 0)
  expect(registros, 'há um Service Worker (MSW) ativo: desligue VITE_USE_MOCKS; o E2E usa a API real').toBe(0)
})
