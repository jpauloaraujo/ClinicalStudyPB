import { createBrowserRouter, Navigate, type RouteObject } from 'react-router'
import { AppLayout } from '../components/layout/AppLayout'
import { PublicLayout } from '../components/layout/PublicLayout'
import { AbaProvisoria } from '../pages/estudos/detalhe/abas/AbaProvisoria'
import { AbaResumo } from '../pages/estudos/detalhe/abas/AbaResumo'
import { perfisDaAba } from '../pages/estudos/detalhe/abas'
import { EstudoDetalhePage } from '../pages/estudos/detalhe/EstudoDetalhePage'
import { EstudoFormPage } from '../pages/estudos/EstudoFormPage'
import { EstudosPage } from '../pages/estudos/EstudosPage'
import { InicioPage } from '../pages/InicioPage'
import { LoginPage } from '../pages/LoginPage'
import { NaoEncontradoPage } from '../pages/NaoEncontradoPage'
import { UsuarioFormPage } from '../pages/usuarios/UsuarioFormPage'
import { UsuariosPage } from '../pages/usuarios/UsuariosPage'
import { perfisDaRota } from './navegacao'
import { RequerAutenticacao, RequerPerfil } from './RotaProtegida'

// Exportadas para os testes montarem a mesma árvore com createMemoryRouter
export const rotas: RouteObject[] = [
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
              { path: ':estudoId/editar', element: <EstudoFormPage /> },
            ],
          },
          // Detalhe do estudo com abas como rotas aninhadas (T39); cada aba tem guard por perfil
          {
            path: ':estudoId',
            element: <EstudoDetalhePage />,
            children: [
              { index: true, element: <Navigate to="resumo" replace /> },
              {
                path: 'resumo',
                element: (
                  <RequerPerfil perfis={perfisDaAba('resumo')}>
                    <AbaResumo />
                  </RequerPerfil>
                ),
              },
              {
                path: 'criterios',
                element: (
                  <RequerPerfil perfis={perfisDaAba('criterios')}>
                    <AbaProvisoria titulo="Critérios de inclusão e exclusão" />
                  </RequerPerfil>
                ),
              },
              {
                path: 'visitas',
                element: (
                  <RequerPerfil perfis={perfisDaAba('visitas')}>
                    <AbaProvisoria titulo="Visitas previstas e atividades" />
                  </RequerPerfil>
                ),
              },
              {
                path: 'candidatos',
                element: (
                  <RequerPerfil perfis={perfisDaAba('candidatos')}>
                    <AbaProvisoria titulo="Candidatos" />
                  </RequerPerfil>
                ),
              },
              {
                path: 'participantes',
                element: (
                  <RequerPerfil perfis={perfisDaAba('participantes')}>
                    <AbaProvisoria titulo="Participantes" />
                  </RequerPerfil>
                ),
              },
            ],
          },
        ],
      },
      { path: '*', element: <NaoEncontradoPage /> },
    ],
  },
]

export const router = createBrowserRouter(rotas)
