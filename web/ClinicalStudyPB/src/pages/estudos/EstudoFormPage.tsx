import { useEffect, useState, type FormEvent } from 'react'
import { Link, useNavigate, useParams } from 'react-router'
import { Campo } from '../../components/ui/Campo'
import { useUsuarioLogado } from '../../contexts/sessao'
import { extrairErro } from '../../services/erros'
import { estudosApi } from '../../services/recursos'
import { rotulosStatus, statusEstudo, type StatusEstudo } from '../../types/estudo'

type Campos = {
  titulo: string
  objetivo: string
  dataInicio: string
  dataFim: string
  limiteParticipantes: string
  status: StatusEstudo
}

type Erros = Partial<Record<keyof Campos, string>>

// Mesmas regras de EstudoRequest e EstudoService
function validar(campos: Campos): Erros {
  const erros: Erros = {}
  if (!campos.titulo.trim()) erros.titulo = 'O título é obrigatório'
  else if (campos.titulo.length > 180) erros.titulo = 'O título deve ter no máximo 180 caracteres'
  if (!campos.objetivo.trim()) erros.objetivo = 'O objetivo é obrigatório'
  const limite = Number(campos.limiteParticipantes)
  if (!campos.limiteParticipantes || !Number.isInteger(limite) || limite < 1) {
    erros.limiteParticipantes = 'O limite de participantes deve ser um número inteiro maior ou igual a 1'
  }
  if (campos.dataInicio && campos.dataFim && campos.dataFim < campos.dataInicio) {
    erros.dataFim = 'A data final não pode ser anterior à data inicial'
  }
  return erros
}

const VAZIO: Campos = {
  titulo: '',
  objetivo: '',
  dataInicio: '',
  dataFim: '',
  limiteParticipantes: '',
  status: 'PLANEJAMENTO',
}

// Cadastro e edição de estudo (T29 / T31 / RF04) — apenas pesquisador
export function EstudoFormPage() {
  const { estudoId: id } = useParams()
  const edicao = id !== undefined
  const usuario = useUsuarioLogado()
  const navegar = useNavigate()

  const [campos, setCampos] = useState<Campos>(VAZIO)
  // Novo estudo: o pesquisador logado é o responsável; na edição mantém o atual
  const [responsavel, setResponsavel] = useState({ id: usuario.id, nome: usuario.nome })
  const [erros, setErros] = useState<Erros>({})
  const [erroGeral, setErroGeral] = useState<string | null>(null)
  const [carregando, setCarregando] = useState(edicao)
  const [enviando, setEnviando] = useState(false)

  useEffect(() => {
    if (!id) return
    estudosApi
      .buscar(id)
      .then((estudo) => {
        setCampos({
          titulo: estudo.titulo,
          objetivo: estudo.objetivo,
          dataInicio: estudo.dataInicio ?? '',
          dataFim: estudo.dataFim ?? '',
          limiteParticipantes: String(estudo.limiteParticipantes),
          status: estudo.status,
        })
        setResponsavel({ id: estudo.responsavel.id, nome: estudo.responsavel.nome })
      })
      .catch((falha) => setErroGeral(extrairErro(falha).mensagem))
      .finally(() => setCarregando(false))
  }, [id])

  function alterar<K extends keyof Campos>(campo: K, valor: Campos[K]) {
    setCampos((atual) => ({ ...atual, [campo]: valor }))
    setErros((atual) => ({ ...atual, [campo]: undefined }))
  }

  async function enviar(evento: FormEvent) {
    evento.preventDefault()
    const encontrados = validar(campos)
    setErros(encontrados)
    setErroGeral(null)
    if (Object.keys(encontrados).length > 0) return

    const dados = {
      titulo: campos.titulo.trim(),
      objetivo: campos.objetivo.trim(),
      responsavelId: responsavel.id,
      dataInicio: campos.dataInicio || null,
      dataFim: campos.dataFim || null,
      limiteParticipantes: Number(campos.limiteParticipantes),
      status: campos.status,
    }

    setEnviando(true)
    try {
      if (id) {
        await estudosApi.atualizar(id, dados)
      } else {
        await estudosApi.criar(dados)
      }
      navegar('/estudos')
    } catch (falha) {
      // Erros de validação da API aparecem no campo; regras de negócio no topo (T31)
      const { mensagem, campo } = extrairErro(falha)
      if (campo && campo in VAZIO) {
        setErros({ [campo]: mensagem })
      } else if (mensagem.includes('data final')) {
        setErros({ dataFim: mensagem })
      } else {
        setErroGeral(mensagem)
      }
    } finally {
      setEnviando(false)
    }
  }

  if (carregando) {
    return <p className="carregando">Carregando estudo…</p>
  }

  return (
    <>
      <h1>{edicao ? 'Editar estudo' : 'Novo estudo'}</h1>
      <form className="formulario formulario--largo" onSubmit={enviar} noValidate>
        <Campo rotulo="Título" erro={erros.titulo}>
          <input value={campos.titulo} maxLength={180} onChange={(e) => alterar('titulo', e.target.value)} />
        </Campo>
        <Campo rotulo="Objetivo" erro={erros.objetivo}>
          <textarea rows={4} value={campos.objetivo} onChange={(e) => alterar('objetivo', e.target.value)} />
        </Campo>
        <Campo rotulo="Responsável">
          <input value={responsavel.nome} disabled />
        </Campo>
        <div className="formulario__linha">
          <Campo rotulo="Data de início" erro={erros.dataInicio}>
            <input type="date" value={campos.dataInicio} onChange={(e) => alterar('dataInicio', e.target.value)} />
          </Campo>
          <Campo rotulo="Data de término" erro={erros.dataFim}>
            <input type="date" value={campos.dataFim} onChange={(e) => alterar('dataFim', e.target.value)} />
          </Campo>
        </div>
        <div className="formulario__linha">
          <Campo rotulo="Limite de participantes" erro={erros.limiteParticipantes}>
            <input
              type="number"
              min={1}
              step={1}
              value={campos.limiteParticipantes}
              onChange={(e) => alterar('limiteParticipantes', e.target.value)}
            />
          </Campo>
          <Campo rotulo="Status" erro={erros.status}>
            <select value={campos.status} disabled title="O estado muda pelo botão de avançar na página do estudo" onChange={(e) => alterar('status', e.target.value as StatusEstudo)}>
              {statusEstudo.map((status) => (
                <option key={status} value={status}>
                  {rotulosStatus[status]}
                </option>
              ))}
            </select>
          </Campo>
        </div>

        {erroGeral && (
          <p className="alerta alerta--erro" role="alert">
            {erroGeral}
          </p>
        )}
        <div className="formulario__acoes">
          <button type="submit" className="botao" disabled={enviando}>
            {enviando ? 'Salvando…' : 'Salvar'}
          </button>
          <Link to="/estudos" className="botao botao--secundario">
            Cancelar
          </Link>
        </div>
      </form>
    </>
  )
}
