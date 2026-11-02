import { setupWorker } from 'msw/browser'
import { handlers } from './handlers'

// Service worker do MSW, usado só quando VITE_USE_MOCKS=true
export const worker = setupWorker(...handlers)
