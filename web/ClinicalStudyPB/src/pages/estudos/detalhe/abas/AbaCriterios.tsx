import { useCallback, useState, type FormEvent } from 'react'
import { criteriosApi } from '../../../../api/criterios'
import { converterErro } from '../../../../api/erros'
import type { CriterioRequest, CriterioResponse, TipoCriterio } from '../../../../api/tipos'
import { Campo } from '../../../../components/ui/Campo'
import { Confirmacao } from '../../../../components/ui/Confirmacao'
import { useUsuarioLogado } from '../../../../contexts/sessao'
import { useCarregamento } from '../../../../hooks/useCarregamento'
import { useEstudoDetalhe } from '../contexto'
import '../recrutamento.css'

type Campos = {
  tipo: TipoCriterio
  titulo: string
  descricao: string
  peso: string
  determinante: boolean
}
type Erros = Partial<Record<keyof Campos, string>>

const VAZIO: Campos = { tipo: 'INCLUSAO', titulo: '', descricao: '', peso: '1', determinante: true }

// Mesmas regras de CriterioRequest no backend
function validar(campos: Campos): Erros {
  const erros: Erros = {}
  if (!campos.titulo.trim()) erros.titulo = 'O título é obrigatório'
  else if (campos.titulo.length > 120) erros.titulo = 'O título deve ter no máximo 120 caracteres'
  const descricao = campos.descricao.trim()
  if (!descricao) erros.descricao = 'A descrição é obrigatória'
  else if (descricao.length < 5 || descricao.length > 500) erros.descricao = 'A descrição deve ter entre 5 e 500 caracteres'
  if (campos.tipo === 'INCLUSAO' && campos.peso.trim() !== '' && !(Number(campos.peso.replace(',', '.')) > 0)) {
    erros.peso = 'O peso deve ser maior que zero'
  }
  return erros
}

function paraRequisicao(campos: Campos): CriterioRequest {
  const base = { tipo: campos.tipo, titulo: campos.titulo.trim(), descricao: campos.descricao.trim() }
  if (campos.tipo === 'EXCLUSAO') return base
  return {
    ...base,
    peso: campos.peso.trim() === '' ? undefined : Number(campos.peso.replace(',', '.')),
    determinante: campos.determinante,
  }
}

function deCriterio(criterio: CriterioResponse): Campos {
  return {
    tipo: criterio.tipo,
    titulo: criterio.titulo,
    descricao: criterio.descricao,
    peso: String(criterio.peso ?? 1),
    determinante: criterio.determinante ?? true,
  }
}

type FormularioProps = {
  inicial: Campos
  edicao: boolean
  enviando: boolean
  erroGeral: string | null
  errosServidor: Erros
  onEnviar: (campos: Campos) => void
  onCancelar?: () => void
}

function FormularioCriterio({ inicial, edicao, enviando, erroGeral, errosServidor, onEnviar, onCancelar }: FormularioProps) {
  const [campos, setCampos] = useState<Campos>(inicial)
  const [errosLocais, setErrosLocais] = useState<Erros>({})
  const erros = { ...errosServidor, ...errosLocais }

  function alterar<K extends keyof Campos>(campo: K, valor: Campos[K]) {
    setCampos((atual) => ({ ...atual, [campo]: valor }))
    setErrosLocais((atual) => ({ ...atual, [campo]: undefined }))
  }

  function enviar(evento: FormEvent) {
    evento.preventDefault()
    const encontrados = validar(campos)
    setErrosLocais(encontrados)
    if (Object.keys(encontrados).length === 0) onEnviar(campos)
  }

  return (
    <form className="formulario formulario--largo" onSubmit={enviar} noValidate aria-label={edicao ? 'Editar critério' : 'Novo critério'}>
      <Campo rotulo="Tipo" erro={erros.tipo}>
        <select value={campos.tipo} disabled={edicao} onChange={(e) => alterar('tipo', e.target.value as TipoCriterio)}>
          <option value="INCLUSAO">Inclusão</option>
          <option value="EXCLUSAO">Exclusão</option>
        </select>
      </Campo>
      <Campo rotulo="Título" erro={erros.titulo}>
        <input value={campos.titulo} maxLength={120} onChange={(e) => alterar('titulo', e.target.value)} />
      </Campo>
      <Campo rotulo="Descrição" erro={erros.descricao}>
        <textarea rows={3} value={campos.descricao} maxLength={500} onChange={(e) => alterar('descricao', e.target.value)} />
      </Campo>
      {campos.tipo === 'INCLUSAO' && (
        <>
          <Campo rotulo="Peso" erro={erros.peso}>
            <input inputMode="decimal" value={campos.peso} onChange={(e) => alterar('peso', e.target.value)} />
          </Campo>
          <label className="campo campo--checkbox">
            <input type="checkbox" checked={campos.determinante} onChange={(e) => alterar('determinante', e.target.checked)} />
            <span>Determinante (deve ser atendido para o candidato ser elegível)</span>
          </label>
        </>
      )}

      {erroGeral && (
        <p className="alerta alerta--erro" role="alert">
          {erroGeral}
        </p>
      )}
      <div className="formulario__acoes">
        <button type="submit" className="botao" disabled={enviando}>
          {enviando ? 'Salvando…' : edicao ? 'Salvar alterações' : 'Adicionar critério'}
        </button>
        {onCancelar && (
          <button type="button" className="botao botao--secundario" onClick={onCancelar}>
            Cancelar
          </button>
        )}
      </div>
    </form>
  )
}

type ListaProps = {
  titulo: string
  vazio: string
  criterios: CriterioResponse[]
  editavel: boolean
  onEditar: (criterio: CriterioResponse) => void
  onExcluir: (criterio: CriterioResponse) => void
}

function ListaCriterios({ titulo, vazio, criterios, editavel, onEditar, onExcluir }: ListaProps) {
  return (
    <section className="pilha" aria-label={titulo}>
      <h3>{titulo}</h3>
      {criterios.length === 0 ? (
        <p className="texto-suave">{vazio}</p>
      ) : (
        <ul className="itens">
          {criterios.map((criterio) => (
            <li key={criterio.id} className="item">
              <div className="item__texto">
                <div className="item__titulo">
                  {criterio.titulo}
                  {criterio.tipo === 'INCLUSAO' && (
                    <>
                      <span className="etiqueta">Peso {criterio.peso}</span>
                      <span className="etiqueta">{criterio.determinante ? 'Determinante' : 'Não determinante'}</span>
                    </>
                  )}
                </div>
                <p className="item__descricao">{criterio.descricao}</p>
              </div>
              {editavel && (
                <div className="tabela__acoes">
                  <button type="button" className="botao botao--secundario botao--pequeno" onClick={() => onEditar(criterio)}>
                    Editar
                  </button>
                  <button type="button" className="botao botao--perigo botao--pequeno" onClick={() => onExcluir(criterio)}>
                    Excluir
                  </button>
                </div>
              )}
            </li>
          ))}
        </ul>
      )}
    </section>
  )
}

// RF05 (T46): critérios de inclusão e exclusão. O pesquisador edita enquanto o estudo está em
// planejamento; depois disso, e para o coordenador, a tela é somente leitura.
export function AbaCriterios() {
  const { estudo } = useEstudoDetalhe()
  const usuario = useUsuarioLogado()
  const carregar = useCallback(() => criteriosApi.listar(estudo.id), [estudo.id])
  const { dados: criterios, erro, carregando, recarregar } = useCarregamento(carregar)

  const emPlanejamento = estudo.status === 'PLANEJAMENTO'
  const editavel = emPlanejamento && usuario.perfil === 'PESQUISADOR'

  const [editando, setEditando] = useState<CriterioResponse | null>(null)
  const [excluindo, setExcluindo] = useState<CriterioResponse | null>(null)
  const [enviando, setEnviando] = useState(false)
  const [erroGeral, setErroGeral] = useState<string | null>(null)
  const [errosServidor, setErrosServidor] = useState<Erros>({})

  async function salvar(campos: Campos) {
    setEnviando(true)
    setErroGeral(null)
    setErrosServidor({})
    try {
      const dados = paraRequisicao(campos)
      if (editando) {
        await criteriosApi.atualizar(estudo.id, editando.id, dados)
        setEditando(null)
      } else {
        await criteriosApi.criar(estudo.id, dados)
      }
      recarregar()
    } catch (falha) {
      const convertido = converterErro(falha)
      setErrosServidor(convertido.campos as Erros)
      setErroGeral(Object.keys(convertido.campos).length > 0 ? null : convertido.mensagem)
    } finally {
      setEnviando(false)
    }
  }

  async function excluir() {
    if (!excluindo) return
    setEnviando(true)
    setErroGeral(null)
    try {
      await criteriosApi.excluir(estudo.id, excluindo.id)
      setExcluindo(null)
      recarregar()
    } catch (falha) {
      setExcluindo(null)
      setErroGeral(converterErro(falha).mensagem)
    } finally {
      setEnviando(false)
    }
  }

  const inclusao = (criterios ?? []).filter((c) => c.tipo === 'INCLUSAO')
  const exclusao = (criterios ?? []).filter((c) => c.tipo === 'EXCLUSAO')

  return (
    <div className="pilha">
      <h2>Critérios de inclusão e exclusão</h2>

      {!editavel && (
        <p className="alerta alerta--info" role="status">
          {usuario.perfil !== 'PESQUISADOR'
            ? 'Seu perfil tem acesso apenas para consulta dos critérios.'
            : 'O estudo não está mais em planejamento: os critérios estão somente para consulta.'}
        </p>
      )}
      {erro && (
        <p className="alerta alerta--erro" role="alert">
          {erro}
        </p>
      )}
      {carregando && <p className="carregando">Carregando critérios…</p>}

      {criterios && (
        <>
          <ListaCriterios
            titulo="Critérios de inclusão"
            vazio={
              editavel
                ? 'Adicione ao menos um critério de inclusão para avançar.'
                : 'Nenhum critério de inclusão cadastrado.'
            }
            criterios={inclusao}
            editavel={editavel}
            onEditar={setEditando}
            onExcluir={setExcluindo}
          />
          <ListaCriterios
            titulo="Critérios de exclusão"
            vazio="Nenhum critério de exclusão cadastrado."
            criterios={exclusao}
            editavel={editavel}
            onEditar={setEditando}
            onExcluir={setExcluindo}
          />
        </>
      )}

      {editavel && (
        <section className="secao" aria-label={editando ? 'Editar critério' : 'Adicionar critério'}>
          <h3>{editando ? 'Editar critério' : 'Novo critério'}</h3>
          <FormularioCriterio
            key={editando?.id ?? 'novo'}
            inicial={editando ? deCriterio(editando) : VAZIO}
            edicao={editando !== null}
            enviando={enviando}
            erroGeral={erroGeral}
            errosServidor={errosServidor}
            onEnviar={salvar}
            onCancelar={editando ? () => setEditando(null) : undefined}
          />
        </section>
      )}

      {excluindo && (
        <Confirmacao
          titulo="Excluir critério?"
          mensagem={`O critério "${excluindo.titulo}" será removido do estudo. Esta ação não pode ser desfeita.`}
          rotuloConfirmar="Excluir"
          perigosa
          processando={enviando}
          onConfirmar={excluir}
          onCancelar={() => setExcluindo(null)}
        />
      )}
    </div>
  )
}
