import react, { reactCompilerPreset } from '@vitejs/plugin-react'
import babel from '@rolldown/plugin-babel'
import { configDefaults, defineConfig } from 'vitest/config'

// https://vite.dev/config/
export default defineConfig({
  plugins: [
    react(),
    babel({ presets: [reactCompilerPreset()] })
  ],
  // Testes de componente (Vitest + Testing Library)
  test: {
    environment: 'jsdom',
    setupFiles: ['./src/test/setup.ts'],
    // Os testes E2E (Playwright) rodam com `npm run test:e2e`, não no Vitest
    exclude: [...configDefaults.exclude, 'e2e/**'],
  },
})
