import { NavLink } from 'react-router'
import { useSessao } from '../../contexts/sessao'
import { itensDoPerfil } from '../../routes/navegacao'

type SidebarProps = {
  aberto: boolean
  onNavegar: () => void
}

export function Sidebar({ aberto, onNavegar }: SidebarProps) {
  const { usuario } = useSessao()

  return (
    <nav
      id="menu-principal"
      aria-label="Menu principal"
      className={aberto ? 'sidebar sidebar--aberta' : 'sidebar'}
    >
      <ul>
        {itensDoPerfil(usuario.perfil).map((item) => (
          <li key={item.caminho}>
            <NavLink to={item.caminho} className="sidebar__link" onClick={onNavegar}>
              {item.rotulo}
            </NavLink>
          </li>
        ))}
      </ul>
    </nav>
  )
}
