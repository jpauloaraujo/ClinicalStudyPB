import { Link } from 'react-router'

export function AcessoNegadoPage() {
  return (
    <>
      <h1>Acesso negado</h1>
      <p>Seu perfil não tem permissão para acessar esta página.</p>
      <Link to="/inicio">Voltar para o início</Link>
    </>
  )
}
