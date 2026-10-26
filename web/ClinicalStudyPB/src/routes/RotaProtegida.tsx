import type { ReactNode } from 'react'
import { Navigate, Outlet, useLocation } from 'react-router'
import { useSessao } from '../contexts/sessao'
import { AcessoNegadoPage } from '../pages/AcessoNegadoPage'
import type { PerfilUsuario } from '../types/usuario'

// Guards de rota (T25). A autorização real é feita pela API (T24);
// aqui só se evita mostrar telas que o perfil não pode usar.

export function RequerAutenticacao({ children }: { children?: ReactNode }) {
  const { usuario, carregando } = useSessao()
  const local = useLocation()

  if (carregando) {
    return <p className="carregando">Carregando sessão…</p>
  }
  if (!usuario) {
    return <Navigate to="/login" replace state={{ de: local.pathname }} />
  }
  return children ?? <Outlet />
}

export function RequerPerfil({ perfis, children }: { perfis: PerfilUsuario[]; children?: ReactNode }) {
  const { usuario } = useSessao()
  if (!usuario || !perfis.includes(usuario.perfil)) {
    return <AcessoNegadoPage />
  }
  return children ?? <Outlet />
}
