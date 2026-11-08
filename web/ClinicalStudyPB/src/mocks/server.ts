import { setupServer } from 'msw/node'
import { handlers } from './handlers'

// Servidor do MSW para os testes (Vitest): as mesmas respostas fictícias do navegador
export const server = setupServer(...handlers)
