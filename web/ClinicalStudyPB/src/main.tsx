import { StrictMode } from 'react'
import { createRoot } from 'react-dom/client'
import { RouterProvider } from 'react-router'
import { AuthProvider } from './contexts/AuthContext'
import { iniciarMocks } from './mocks/iniciar'
import { router } from './routes/router'
import './index.css'

// Com VITE_USE_MOCKS=true o Mock Service Worker liga antes da primeira renderização (T40)
void iniciarMocks().finally(() => {
  createRoot(document.getElementById('root')!).render(
    <StrictMode>
      <AuthProvider>
        <RouterProvider router={router} />
      </AuthProvider>
    </StrictMode>,
  )
})
