import { useEffect, useState, type FormEvent } from 'react'
import { Link, useNavigate, useParams } from 'react-router'
import { Campo } from '../../components/ui/Campo'
import { useUsuarioLogado } from '../../contexts/sessao'
import { extrairErro } from '../../services/erros'
import { usuariosApi } from '../../services/recursos'
import { perfis, rotulosPerfil, type PerfilUsuario } from '../../types/usuario'

type Campos = {
  nome: string
  email: string
  senha: string
  perfil: PerfilUsuario
  ativo: boolean
}

type Erros = Partial<Record<keyof Campos, string>>

const EMAIL_VALIDO = /^[^\s@]+@[^\s@]+\.[^\s@]+$/

// Mesmas regras de UsuarioCriacaoRequest / UsuarioAtualizacaoRequest
function validar(campos: Campos, edicao: boolean): Erros {
  const erros: Erros = {}
  if (!campos.nome.trim()) erros.nome = 'O nome é obrigatório'
  else if (campos.nome.length > 120) erros.nome = 'O nome deve ter no máximo 120 caracteres'
  if (!campos.email.trim()) erros.email = 'O e-mail é obrigatório'
  else if (!EMAIL_VALIDO.test(campos.email.trim())) erros.email = 'Informe um e-mail válido'
  else if (campos.email.length > 180) erros.email = 'O e-mail deve ter no máximo 180 caracteres'
  if (!edicao && !campos.senha) erros.senha = 'A senha é obrigatória'
  else if (campos.senha && (campos.senha.length < 8 || campos.senha.length > 100)) {
    erros.senha = 'A senha deve ter entre 8 e 100 caracteres'
  }
  return erros
}

const VAZIO: Campos = { nome: '', email: '', senha: '', perfil: 'PESQUISADOR', ativo: true }

// Cadastro e edição de usuário (T17 / RF01)
export function UsuarioFormPage() {
  const { id } = useParams()
  const logado = useUsuarioLogado()
  const propriaConta = id !== undefined && id === logado.id
  const edicao = id !== undefined
  const navegar = useNavigate()

  const [campos, setCampos] = useState<Campos>(VAZIO)
  const [erros, setErros] = useState<Erros>({})
  const [erroGeral, setErroGeral] = useState<string | null>(null)
  const [carregando, setCarregando] = useState(edicao)
  const [enviando, setEnviando] = useState(false)

  useEffect(() => {
    if (!id) return
    usuariosApi
      .buscar(id)
      .then((u) => setCampos({ nome: u.nome, email: u.email, senha: '', perfil: u.perfil, ativo: u.ativo }))
      .catch((falha) => setErroGeral(extrairErro(falha).mensagem))
      .finally(() => setCarregando(false))
  }, [id])

  function alterar<K extends keyof Campos>(campo: K, valor: Campos[K]) {
    setCampos((atual) => ({ ...atual, [campo]: valor }))
    setErros((atual) => ({ ...atual, [campo]: undefined }))
  }

  async function enviar(evento: FormEvent) {
    evento.preventDefault()
    const encontrados = validar(campos, edicao)
    setErros(encontrados)
    setErroGeral(null)
    if (Object.keys(encontrados).length > 0) return

    setEnviando(true)
    try {
      const base = { nome: campos.nome.trim(), email: campos.email.trim(), perfil: campos.perfil }
      if (id) {
        await usuariosApi.atualizar(id, { ...base, ativo: campos.ativo, senha: campos.senha || undefined })
      } else {
        await usuariosApi.criar({ ...base, senha: campos.senha })
      }
      navegar('/usuarios')
    } catch (falha) {
      const { mensagem, campo } = extrairErro(falha)
      if (campo && campo in VAZIO) {
        setErros({ [campo]: mensagem })
      } else {
        setErroGeral(mensagem)
      }
    } finally {
      setEnviando(false)
    }
  }

  if (carregando) {
    return <p className="carregando">Carregando usuário…</p>
  }

  return (
    <>
      <h1>{edicao ? 'Editar usuário' : 'Novo usuário'}</h1>
      <form className="formulario formulario--largo" onSubmit={enviar} noValidate>
        <Campo rotulo="Nome" erro={erros.nome}>
          <input value={campos.nome} maxLength={120} onChange={(e) => alterar('nome', e.target.value)} />
        </Campo>
        <Campo rotulo="E-mail" erro={erros.email}>
          <input type="email" value={campos.email} onChange={(e) => alterar('email', e.target.value)} />
        </Campo>
        <Campo rotulo={edicao ? 'Nova senha (opcional)' : 'Senha'} erro={erros.senha}>
          <input
            type="password"
            autoComplete="new-password"
            value={campos.senha}
            onChange={(e) => alterar('senha', e.target.value)}
          />
        </Campo>
        <Campo rotulo="Perfil" erro={erros.perfil}>
          <select
            value={campos.perfil}
            disabled={propriaConta}
            title={propriaConta ? 'Você não pode alterar o perfil da própria conta' : undefined}
            onChange={(e) => alterar('perfil', e.target.value as PerfilUsuario)}>
            {perfis.map((perfil) => (
              <option key={perfil} value={perfil}>
                {rotulosPerfil[perfil]}
              </option>
            ))}
          </select>
        </Campo>
        {edicao && (
          <label className="campo campo--checkbox">
            <input
              type="checkbox"
              checked={campos.ativo}
              disabled={propriaConta}
              onChange={(e) => alterar('ativo', e.target.checked)}
            />
            <span>Conta ativa</span>
          </label>
        )}

        {erroGeral && (
          <p className="alerta alerta--erro" role="alert">
            {erroGeral}
          </p>
        )}
        <div className="formulario__acoes">
          <button type="submit" className="botao" disabled={enviando}>
            {enviando ? 'Salvando…' : 'Salvar'}
          </button>
          <Link to="/usuarios" className="botao botao--secundario">
            Cancelar
          </Link>
        </div>
      </form>
    </>
  )
}
