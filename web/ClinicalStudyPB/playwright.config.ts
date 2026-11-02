import { defineConfig, devices } from '@playwright/test'

// Variáveis do ambiente de teste: .env.e2e (local) ou variáveis já definidas (CI).
// loadEnvFile não sobrescreve variáveis que já existem.
try {
  process.loadEnvFile('.env.e2e')
} catch {
  // Sem .env.e2e: valem as variáveis do ambiente; as que faltarem são apontadas no setup
}

const noCI = Boolean(process.env.CI)

export default defineConfig({
  testDir: './e2e',
  // Os testes de componente (Vitest) ficam em src/ e não entram aqui
  testMatch: '**/*.{setup,spec}.ts',
  outputDir: 'test-results',
  timeout: 30_000,
  expect: { timeout: 5_000 },
  fullyParallel: true,
  forbidOnly: noCI,
  retries: noCI ? 2 : 0,
  workers: noCI ? 2 : undefined,
  reporter: noCI
    ? [['list'], ['html', { open: 'never' }]]
    : [['list'], ['html', { open: 'on-failure' }]],
  use: {
    baseURL: process.env.E2E_BASE_URL,
    locale: 'pt-BR',
    trace: 'on-first-retry',
    screenshot: 'only-on-failure',
    video: 'retain-on-failure',
  },
  projects: [
    // Cria os usuários de teste e salva a sessão de cada perfil em e2e/.auth/ a cada execução
    { name: 'setup', testMatch: '**/*.setup.ts' },
    {
      name: 'chromium',
      testMatch: '**/*.spec.ts',
      use: { ...devices['Desktop Chrome'] },
      dependencies: ['setup'],
    },
  ],
})
