import { useState, type FormEvent } from 'react'
import { Navigate, useLocation } from 'react-router'
import { PerfilSemAcessoWebError, useSessao } from '../contexts/sessao'
import { podeAcessar } from '../routes/navegacao'
import { extrairErro } from '../services/erros'

const EMAIL_VALIDO = /^[^\s@]+@[^\s@]+\.[^\s@]+$/

export function LoginPage() {
  const { usuario, entrar } = useSessao()
  const local = useLocation()
  const anterior = (local.state as { de?: string } | null)?.de

  const [email, setEmail] = useState('')
  const [senha, setSenha] = useState('')
  const [mostrarSenha, setMostrarSenha] = useState(false)
  const [erro, setErro] = useState<string | null>(null)
  const [enviando, setEnviando] = useState(false)

  // Volta para a página pedida antes do login só se o perfil puder abri-la
  if (usuario) {
    const destino = anterior && podeAcessar(anterior, usuario.perfil) ? anterior : '/inicio'
    return <Navigate to={destino} replace />
  }

  async function enviar(evento: FormEvent) {
    evento.preventDefault()
    if (!email.trim() || !senha) {
      setErro('Informe o e-mail e a senha.')
      return
    }
    if (!EMAIL_VALIDO.test(email.trim())) {
      setErro('Informe um e-mail válido.')
      return
    }

    setErro(null)
    setEnviando(true)
    try {
      await entrar(email.trim(), senha)
    } catch (falha) {
      setErro(falha instanceof PerfilSemAcessoWebError ? falha.message : extrairErro(falha).mensagem)
    } finally {
      setEnviando(false)
    }
  }

  return (
    <>
      <h1>ClinicalStudyPB</h1>
      <p>Entre com seu e-mail e senha.</p>
      <form className="formulario" onSubmit={enviar} noValidate>
        <label className="campo">
          <span>E-mail</span>
          <input
            type="email"
            autoComplete="username"
            value={email}
            onChange={(evento) => setEmail(evento.target.value)}
          />
        </label>
        <label className="campo">
          <span>Senha</span>
          <input
            type={mostrarSenha ? 'text' : 'password'}
            autoComplete="current-password"
            value={senha}
            onChange={(evento) => setSenha(evento.target.value)}
          />
        </label>
        <label className="campo campo--checkbox">
          <input type="checkbox" checked={mostrarSenha} onChange={(evento) => setMostrarSenha(evento.target.checked)} />
          <span>Mostrar senha</span>
        </label>
        {erro && (
          <p className="alerta alerta--erro" role="alert">
            {erro}
          </p>
        )}
        <button type="submit" className="botao" disabled={enviando}>
          {enviando ? 'Entrando…' : 'Entrar'}
        </button>
      </form>
    </>
  )
}
