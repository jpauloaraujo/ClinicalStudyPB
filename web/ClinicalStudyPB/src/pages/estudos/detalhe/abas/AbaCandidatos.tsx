import { useCallback, useState, type FormEvent } from 'react'
import { Link } from 'react-router'
import { candidatosApi } from '../../../../api/candidatos'
import { converterErro } from '../../../../api/erros'
import { rotulosSituacao, situacoesCandidato, type SituacaoCandidato } from '../../../../api/tipos'
import { Campo } from '../../../../components/ui/Campo'
import { useUsuarioLogado } from '../../../../contexts/sessao'
import { useCarregamento } from '../../../../hooks/useCarregamento'
import { formatarData } from '../../../../utils/datas'
import { useEstudoDetalhe } from '../contexto'
import '../recrutamento.css'

const TAMANHO_PAGINA = 10

type Campos = { nomeFicticio: string; dataNascimento: string; contato: string }
type Erros = Partial<Record<keyof Campos, string>>

function hojeISO(): string {
  return new Date().toISOString().slice(0, 10)
}

// Mesmas regras de CandidatoRequest no backend
function validar(campos: Campos): Erros {
  const erros: Erros = {}
  if (!campos.nomeFicticio.trim()) erros.nomeFicticio = 'O nome fictício é obrigatório'
  else if (campos.nomeFicticio.length > 120) erros.nomeFicticio = 'O nome fictício deve ter no máximo 120 caracteres'
  if (!campos.dataNascimento) erros.dataNascimento = 'A data de nascimento é obrigatória'
  else if (campos.dataNascimento >= hojeISO()) erros.dataNascimento = 'A data de nascimento deve estar no passado'
  if (campos.contato.length > 180) erros.contato = 'O contato deve ter no máximo 180 caracteres'
  return erros
}

// RF08 (T52): lista paginada de candidatos do estudo, com filtro por situação, e cadastro pelo
// coordenador. O cadastro só vale com o estudo em recrutamento; fora dele o botão fica desabilitado.
export function AbaCandidatos() {
  const { estudo } = useEstudoDetalhe()
  const usuario = useUsuarioLogado()
  const podeCadastrar = usuario.perfil === 'COORDENADOR'
  const emRecrutamento = estudo.status === 'RECRUTAMENTO'

  const [situacao, setSituacao] = useState<SituacaoCandidato | ''>('')
  const [pagina, setPagina] = useState(0)
  const carregar = useCallback(
    () =>
      candidatosApi.listar(estudo.id, {
        situacao: situacao || undefined,
        pagina,
        tamanho: TAMANHO_PAGINA,
      }),
    [estudo.id, situacao, pagina],
  )
  const { dados: lista, erro, carregando, recarregar } = useCarregamento(carregar)

  const [campos, setCampos] = useState<Campos>({ nomeFicticio: '', dataNascimento: '', contato: '' })
  const [errosLocais, setErrosLocais] = useState<Erros>({})
  const [errosServidor, setErrosServidor] = useState<Erros>({})
  const [erroGeral, setErroGeral] = useState<string | null>(null)
  const [enviando, setEnviando] = useState(false)
  const [cadastrado, setCadastrado] = useState<string | null>(null)
  const erros = { ...errosServidor, ...errosLocais }

  function alterar(campo: keyof Campos, valor: string) {
    setCampos((atual) => ({ ...atual, [campo]: valor }))
    setErrosLocais((atual) => ({ ...atual, [campo]: undefined }))
  }

  async function cadastrar(evento: FormEvent) {
    evento.preventDefault()
    const encontrados = validar(campos)
    setErrosLocais(encontrados)
    setErrosServidor({})
    setErroGeral(null)
    setCadastrado(null)
    if (Object.keys(encontrados).length > 0) return

    setEnviando(true)
    try {
      const criado = await candidatosApi.criar(estudo.id, {
        nomeFicticio: campos.nomeFicticio.trim(),
        dataNascimento: campos.dataNascimento,
        contato: campos.contato.trim() || undefined,
      })
      setCampos({ nomeFicticio: '', dataNascimento: '', contato: '' })
      setCadastrado(`Candidato ${criado.codigo} cadastrado.`)
      setPagina(0)
      recarregar()
    } catch (falha) {
      const convertido = converterErro(falha)
      setErrosServidor(convertido.campos as Erros)
      // Duplicidade (409) e demais regras não pertencem a um campo: vão para o alerta
      setErroGeral(Object.keys(convertido.campos).length > 0 ? null : convertido.mensagem)
    } finally {
      setEnviando(false)
    }
  }

  return (
    <div className="pilha">
      <h2>Candidatos</h2>

      {erro && (
        <p className="alerta alerta--erro" role="alert">
          {erro}
        </p>
      )}

      <div className="filtros">
        <Campo rotulo="Filtrar por situação">
          <select
            value={situacao}
            onChange={(e) => {
              setSituacao(e.target.value as SituacaoCandidato | '')
              setPagina(0)
            }}
          >
            <option value="">Todas</option>
            {situacoesCandidato.map((s) => (
              <option key={s} value={s}>
                {rotulosSituacao[s]}
              </option>
            ))}
          </select>
        </Campo>
      </div>

      {carregando && <p className="carregando">Carregando candidatos…</p>}
      {lista && lista.conteudo.length === 0 && (
        <p>{situacao ? 'Nenhum candidato com essa situação.' : 'Nenhum candidato cadastrado.'}</p>
      )}
      {lista && lista.conteudo.length > 0 && (
        <>
          <div className="tabela-container">
            <table className="tabela tabela--candidatos">
              <thead>
                <tr>
                  <th>Código</th>
                  <th>Nome fictício</th>
                  <th>Nascimento</th>
                  <th>Situação</th>
                  <th>Elegibilidade</th>
                  <th>Consentimento</th>
                  <th aria-label="Ficha" />
                </tr>
              </thead>
              <tbody>
                {lista.conteudo.map((candidato) => (
                  <tr key={candidato.id}>
                    <td>{candidato.codigo}</td>
                    <td>{candidato.nomeFicticio}</td>
                    <td>{formatarData(candidato.dataNascimento)}</td>
                    <td>
                      <span className="etiqueta">{rotulosSituacao[candidato.situacao]}</span>
                    </td>
                    <td>
                      {candidato.resultadoAvaliacao === 'APROVADO'
                        ? 'Elegível'
                        : candidato.resultadoAvaliacao === 'REPROVADO'
                          ? 'Não elegível'
                          : 'Não avaliado'}
                    </td>
                    <td>{candidato.consentimentoRegistrado ? 'Registrado' : 'Pendente'}</td>
                    <td className="tabela__acoes">
                      <Link
                        to={candidato.id}
                        className="botao botao--secundario botao--pequeno"
                        aria-label={`Abrir a ficha de ${candidato.codigo}`}
                      >
                        Abrir ficha
                      </Link>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
          <nav className="paginacao" aria-label="Paginação dos candidatos">
            <button type="button" className="botao botao--secundario botao--pequeno" disabled={pagina === 0} onClick={() => setPagina(pagina - 1)}>
              Anterior
            </button>
            <span>
              Página {lista.pagina + 1} de {Math.max(1, lista.totalPaginas)} · {lista.totalElementos}{' '}
              {lista.totalElementos === 1 ? 'candidato' : 'candidatos'}
            </span>
            <button
              type="button"
              className="botao botao--secundario botao--pequeno"
              disabled={pagina + 1 >= lista.totalPaginas}
              onClick={() => setPagina(pagina + 1)}
            >
              Próxima
            </button>
          </nav>
        </>
      )}

      {podeCadastrar ? (
        <section className="secao" aria-label="Cadastrar candidato">
          <h3>Cadastrar candidato</h3>
          <p className="texto-suave">Use somente dados fictícios: o sistema gera o código e não guarda o nome civil.</p>
          {!emRecrutamento && (
            <p className="alerta alerta--info" role="status">
              Candidatos só podem ser cadastrados com o estudo em recrutamento.
            </p>
          )}
          <form className="formulario formulario--largo" onSubmit={cadastrar} noValidate>
            <Campo rotulo="Nome fictício" erro={erros.nomeFicticio}>
              <input value={campos.nomeFicticio} maxLength={120} onChange={(e) => alterar('nomeFicticio', e.target.value)} />
            </Campo>
            <Campo rotulo="Data de nascimento" erro={erros.dataNascimento}>
              <input type="date" value={campos.dataNascimento} onChange={(e) => alterar('dataNascimento', e.target.value)} />
            </Campo>
            <Campo rotulo="Contato (opcional)" erro={erros.contato}>
              <input value={campos.contato} maxLength={180} onChange={(e) => alterar('contato', e.target.value)} />
            </Campo>
            {erroGeral && (
              <p className="alerta alerta--erro" role="alert">
                {erroGeral}
              </p>
            )}
            {cadastrado && (
              <p className="alerta alerta--ok" role="status">
                {cadastrado}
              </p>
            )}
            <div className="formulario__acoes">
              <button type="submit" className="botao" disabled={!emRecrutamento || enviando}>
                {enviando ? 'Cadastrando…' : 'Cadastrar candidato'}
              </button>
            </div>
          </form>
        </section>
      ) : (
        <p className="texto-suave">Seu perfil tem acesso apenas para consulta dos candidatos.</p>
      )}
    </div>
  )
}
