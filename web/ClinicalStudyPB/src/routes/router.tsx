import { createBrowserRouter, Navigate, type RouteObject } from 'react-router'
import { AppLayout } from '../components/layout/AppLayout'
import { PublicLayout } from '../components/layout/PublicLayout'
import { AbaCandidatos } from '../pages/estudos/detalhe/abas/AbaCandidatos'
import { AbaCriterios } from '../pages/estudos/detalhe/abas/AbaCriterios'
import { AbaParticipantes } from '../pages/estudos/detalhe/abas/AbaParticipantes'
import { AbaResumo } from '../pages/estudos/detalhe/abas/AbaResumo'
import { AbaVisitas } from '../pages/estudos/detalhe/abas/AbaVisitas'
import { FichaCandidatoPage } from '../pages/estudos/detalhe/candidato/FichaCandidatoPage'
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
                    <AbaCriterios />
                  </RequerPerfil>
                ),
              },
              {
                path: 'visitas',
                element: (
                  <RequerPerfil perfis={perfisDaAba('visitas')}>
                    <AbaVisitas />
                  </RequerPerfil>
                ),
              },
              {
                path: 'candidatos',
                element: (
                  <RequerPerfil perfis={perfisDaAba('candidatos')}>
                    <AbaCandidatos />
                  </RequerPerfil>
                ),
              },
              // Ficha do candidato (T52); recebe as seções de avaliação, consentimento e inclusão
              {
                path: 'candidatos/:candidatoId',
                element: (
                  <RequerPerfil perfis={perfisDaAba('candidatos')}>
                    <FichaCandidatoPage />
                  </RequerPerfil>
                ),
              },
              {
                path: 'participantes',
                element: (
                  <RequerPerfil perfis={perfisDaAba('participantes')}>
                    <AbaParticipantes />
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
