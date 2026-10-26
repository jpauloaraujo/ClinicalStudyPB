import { createBrowserRouter, Navigate } from 'react-router'
import { AppLayout } from '../components/layout/AppLayout'
import { PublicLayout } from '../components/layout/PublicLayout'
import { EstudosPage } from '../pages/estudos/EstudosPage'
import { InicioPage } from '../pages/InicioPage'
import { LoginPage } from '../pages/LoginPage'
import { NaoEncontradoPage } from '../pages/NaoEncontradoPage'
import { UsuariosPage } from '../pages/usuarios/UsuariosPage'

export const router = createBrowserRouter([
  {
    element: <PublicLayout />,
    children: [{ path: '/login', element: <LoginPage /> }],
  },
  {
    path: '/',
    element: <AppLayout />,
    children: [
      { index: true, element: <Navigate to="/inicio" replace /> },
      { path: 'inicio', element: <InicioPage /> },
      // Sub-rotas (novo, :id) entram em T17 e T29
      { path: 'usuarios', children: [{ index: true, element: <UsuariosPage /> }] },
      { path: 'estudos', children: [{ index: true, element: <EstudosPage /> }] },
      { path: '*', element: <NaoEncontradoPage /> },
    ],
  },
])
