import { createBrowserRouter, Navigate } from 'react-router'
import { AppLayout } from '../components/layout/AppLayout'
import { PublicLayout } from '../components/layout/PublicLayout'
import { EstudoFormPage } from '../pages/estudos/EstudoFormPage'
import { EstudosPage } from '../pages/estudos/EstudosPage'
import { InicioPage } from '../pages/InicioPage'
import { LoginPage } from '../pages/LoginPage'
import { NaoEncontradoPage } from '../pages/NaoEncontradoPage'
import { UsuarioFormPage } from '../pages/usuarios/UsuarioFormPage'
import { UsuariosPage } from '../pages/usuarios/UsuariosPage'
import { perfisDaRota } from './navegacao'
import { RequerAutenticacao, RequerPerfil } from './RotaProtegida'

export const router = createBrowserRouter([
  {
    element: <PublicLayout />,
    children: [{ path: '/login', element: <LoginPage /> }],
  },
  {
    path: '/',
    element: (
      <RequerAutenticacao>
        <AppLayout />
      </RequerAutenticacao>
    ),
    children: [
      { index: true, element: <Navigate to="/inicio" replace /> },
      { path: 'inicio', element: <InicioPage /> },
      {
        path: 'usuarios',
        element: <RequerPerfil perfis={perfisDaRota('/usuarios')} />,
        children: [
          { index: true, element: <UsuariosPage /> },
          { path: 'novo', element: <UsuarioFormPage /> },
          { path: ':id', element: <UsuarioFormPage /> },
        ],
      },
      {
        path: 'estudos',
        element: <RequerPerfil perfis={perfisDaRota('/estudos')} />,
        children: [
          { index: true, element: <EstudosPage /> },
          // Cadastro e edição só para o pesquisador (matriz RBAC, RF04)
          {
            element: <RequerPerfil perfis={['PESQUISADOR']} />,
            children: [
              { path: 'novo', element: <EstudoFormPage /> },
              { path: ':id', element: <EstudoFormPage /> },
            ],
          },
        ],
      },
      { path: '*', element: <NaoEncontradoPage /> },
    ],
  },
])
