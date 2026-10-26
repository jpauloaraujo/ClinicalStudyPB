import { Link, useNavigate } from 'react-router'
import { useSessao, useUsuarioLogado } from '../../contexts/sessao'
import { rotulosPerfil } from '../../types/usuario'

type HeaderProps = {
  menuAberto: boolean
  onAlternarMenu: () => void
}

export function Header({ menuAberto, onAlternarMenu }: HeaderProps) {
  const usuario = useUsuarioLogado()
  const { sair } = useSessao()
  const navegar = useNavigate()

  // Logout stateless: o cliente descarta o token (ver README, seção de autenticação)
  function encerrarSessao() {
    sair()
    navegar('/login', { replace: true })
  }

  return (
    <header className="header">
      <button
        type="button"
        className="header__menu"
        aria-label={menuAberto ? 'Fechar menu' : 'Abrir menu'}
        aria-expanded={menuAberto}
        aria-controls="menu-principal"
        onClick={onAlternarMenu}
      >
        <span aria-hidden="true">☰</span>
      </button>

      <Link to="/inicio" className="header__marca">
        ClinicalStudyPB
      </Link>

      <div className="header__usuario">
        <span className="header__nome">
          {usuario.nome} · <strong>{rotulosPerfil[usuario.perfil]}</strong>
        </span>
        <button type="button" className="botao botao--secundario" onClick={encerrarSessao}>
          Sair
        </button>
      </div>
    </header>
  )
}
