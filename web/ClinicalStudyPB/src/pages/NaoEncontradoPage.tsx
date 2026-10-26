import { Link } from 'react-router'

export function NaoEncontradoPage() {
  return (
    <>
      <h1>Página não encontrada</h1>
      <p>O endereço acessado não existe.</p>
      <Link to="/inicio">Voltar para o início</Link>
    </>
  )
}
