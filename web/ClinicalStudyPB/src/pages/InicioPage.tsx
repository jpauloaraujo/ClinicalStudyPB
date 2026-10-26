import { Link } from 'react-router'
import { useSessao } from '../contexts/sessao'
import { itensDoPerfil } from '../routes/navegacao'

export function InicioPage() {
  const { usuario } = useSessao()
  const atalhos = itensDoPerfil(usuario.perfil).filter((item) => item.caminho !== '/inicio')

  return (
    <>
      <h1>Olá, {usuario.nome}</h1>
      <p>Bem-vindo ao Sistema de Gestão de Estudos Clínicos.</p>
      <ul className="atalhos">
        {atalhos.map((item) => (
          <li key={item.caminho}>
            <Link to={item.caminho} className="atalhos__cartao">
              {item.rotulo}
            </Link>
          </li>
        ))}
      </ul>
    </>
  )
}
