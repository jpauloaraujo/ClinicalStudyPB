import { StrictMode } from 'react'
import { createRoot } from 'react-dom/client'
import { RouterProvider } from 'react-router'
import { SessaoProvider } from './contexts/SessaoProvider'
import { router } from './routes/router'
import './index.css'

createRoot(document.getElementById('root')!).render(
  <StrictMode>
    <SessaoProvider>
      <RouterProvider router={router} />
    </SessaoProvider>
  </StrictMode>,
)
