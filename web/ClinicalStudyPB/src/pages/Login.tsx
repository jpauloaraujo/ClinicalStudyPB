import { useState, type FormEvent } from 'react'
import { useAuth } from '../contexts/authContexto'
import { extrairErro } from '../services/erros'
import { autenticacaoApi } from '../services/recursos'

// Tela de login ligada ao AuthContext. A tela principal da aplicação é a LoginPage (com perfis e rotas);
// esta usa o mesmo endpoint (/api/auth/login) e a mesma chave de token.
export function Login() {
  const [email, setEmail] = useState('')
  const [senha, setSenha] = useState('')
  const [erro, setErro] = useState('')
  const [enviando, setEnviando] = useState(false)
  const { login, logout, isAuthenticated } = useAuth()

  const handleLogin = async (evento: FormEvent) => {
    evento.preventDefault()
    setErro('')
    setEnviando(true)
    try {
      login(await autenticacaoApi.entrar(email.trim(), senha))
    } catch (falha) {
      setErro(extrairErro(falha).mensagem)
    } finally {
      setEnviando(false)
    }
  }

  if (isAuthenticated) {
    return (
      <div className="pilha">
        <h2>Você está autenticado!</h2>
        <button type="button" className="botao" onClick={logout}>
          Sair
        </button>
      </div>
    )
  }

  return (
    <div className="pilha">
      <h2>Login - ClinicalStudyPB</h2>
      {erro && (
        <p className="alerta alerta--erro" role="alert">
          {erro}
        </p>
      )}
      <form className="formulario" onSubmit={handleLogin}>
        <label className="campo">
          <span>E-mail</span>
          <input type="email" value={email} onChange={(e) => setEmail(e.target.value)} required />
        </label>
        <label className="campo">
          <span>Senha</span>
          <input type="password" value={senha} onChange={(e) => setSenha(e.target.value)} required />
        </label>
        <button type="submit" className="botao" disabled={enviando}>
          {enviando ? 'Entrando…' : 'Entrar'}
        </button>
      </form>
    </div>
  )
}
