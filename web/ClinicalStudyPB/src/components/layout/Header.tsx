import { Link } from 'react-router'
import { useSessao, usuariosProvisorios } from '../../contexts/sessao'
import { rotulosPerfil, type PerfilUsuario } from '../../types/usuario'

type HeaderProps = {
  menuAberto: boolean
  onAlternarMenu: () => void
}

const perfisProvisorios = Object.keys(usuariosProvisorios) as PerfilUsuario[]

export function Header({ menuAberto, onAlternarMenu }: HeaderProps) {
  const { usuario, trocarPerfil } = useSessao()

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
        {/* Seletor provisório para conferir o menu por perfil; substituir pelo AuthContext na T21 */}
        {import.meta.env.DEV && (
          <label className="header__perfil-dev">
            <span>Perfil (dev)</span>
            <select
              value={usuario.perfil}
              onChange={(evento) => trocarPerfil(evento.target.value as PerfilUsuario)}
            >
              {perfisProvisorios.map((perfil) => (
                <option key={perfil} value={perfil}>
                  {rotulosPerfil[perfil]}
                </option>
              ))}
            </select>
          </label>
        )}
        <span className="header__nome">
          {usuario.nome} · <strong>{rotulosPerfil[usuario.perfil]}</strong>
        </span>
        {/* Logout implementado na T21 */}
        <button type="button" className="botao botao--secundario" disabled title="Disponível após a T21">
          Sair
        </button>
      </div>
    </header>
  )
}
