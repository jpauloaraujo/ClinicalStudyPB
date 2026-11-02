import { useCallback, useState, type FormEvent } from 'react'
import { converterErro } from '../../../../api/erros'
import {
  categoriasAcao,
  rotulosCategoria,
  type AcaoResponse,
  type CategoriaAcao,
  type VisitaResponse,
} from '../../../../api/tipos'
import { acoesApi, visitasApi } from '../../../../api/visitas'
import { Campo } from '../../../../components/ui/Campo'
import { Confirmacao } from '../../../../components/ui/Confirmacao'
import { useUsuarioLogado } from '../../../../contexts/sessao'
import { useCarregamento } from '../../../../hooks/useCarregamento'
import { useEstudoDetalhe } from '../contexto'
import '../recrutamento.css'

type CamposVisita = { nome: string; diaPrevisto: string; tolerancia: string }
type ErrosVisita = Partial<Record<keyof CamposVisita, string>>
type CamposAcao = { nome: string; descricao: string; categoria: CategoriaAcao | '' }
type ErrosAcao = Partial<Record<keyof CamposAcao, string>>

function inteiro(texto: string): number {
  return texto.trim() === '' ? Number.NaN : Number(texto)
}

// Mesmas regras de VisitaRequest no backend
function validarVisita(campos: CamposVisita): ErrosVisita {
  const erros: ErrosVisita = {}
  if (!campos.nome.trim()) erros.nome = 'O nome é obrigatório'
  else if (campos.nome.length > 100) erros.nome = 'O nome deve ter no máximo 100 caracteres'
  const dia = inteiro(campos.diaPrevisto)
  if (Number.isNaN(dia)) erros.diaPrevisto = 'O dia previsto é obrigatório'
  else if (!Number.isInteger(dia) || dia < 0) erros.diaPrevisto = 'O dia previsto deve ser um inteiro maior ou igual a zero'
  const tolerancia = inteiro(campos.tolerancia)
  if (Number.isNaN(tolerancia)) erros.tolerancia = 'A tolerância é obrigatória'
  else if (!Number.isInteger(tolerancia) || tolerancia < 0 || tolerancia > 30) erros.tolerancia = 'A tolerância deve ficar entre 0 e 30 dias'
  return erros
}

function validarAcao(campos: CamposAcao): ErrosAcao {
  const erros: ErrosAcao = {}
  if (!campos.nome.trim()) erros.nome = 'O nome é obrigatório'
  else if (campos.nome.length > 100) erros.nome = 'O nome deve ter no máximo 100 caracteres'
  if (campos.descricao.length > 500) erros.descricao = 'A descrição deve ter no máximo 500 caracteres'
  if (!campos.categoria) erros.categoria = 'A categoria é obrigatória'
  return erros
}

type FormVisitaProps = {
  inicial: CamposVisita
  edicao: boolean
  enviando: boolean
  erroGeral: string | null
  errosServidor: ErrosVisita
  onEnviar: (campos: CamposVisita) => void
  onCancelar?: () => void
}

function FormularioVisita({ inicial, edicao, enviando, erroGeral, errosServidor, onEnviar, onCancelar }: FormVisitaProps) {
  const [campos, setCampos] = useState(inicial)
  const [errosLocais, setErrosLocais] = useState<ErrosVisita>({})
  const erros = { ...errosServidor, ...errosLocais }

  function alterar(campo: keyof CamposVisita, valor: string) {
    setCampos((atual) => ({ ...atual, [campo]: valor }))
    setErrosLocais((atual) => ({ ...atual, [campo]: undefined }))
  }

  function enviar(evento: FormEvent) {
    evento.preventDefault()
    const encontrados = validarVisita(campos)
    setErrosLocais(encontrados)
    if (Object.keys(encontrados).length === 0) onEnviar(campos)
  }

  return (
    <form className="formulario formulario--largo" onSubmit={enviar} noValidate aria-label={edicao ? 'Editar visita' : 'Nova visita'}>
      <Campo rotulo="Nome da visita" erro={erros.nome}>
        <input value={campos.nome} maxLength={100} onChange={(e) => alterar('nome', e.target.value)} />
      </Campo>
      <div className="formulario__linha">
        <Campo rotulo="Dia previsto (a partir da inclusão)" erro={erros.diaPrevisto}>
          <input inputMode="numeric" value={campos.diaPrevisto} onChange={(e) => alterar('diaPrevisto', e.target.value)} />
        </Campo>
        <Campo rotulo="Tolerância (dias, de 0 a 30)" erro={erros.tolerancia}>
          <input inputMode="numeric" value={campos.tolerancia} onChange={(e) => alterar('tolerancia', e.target.value)} />
        </Campo>
      </div>
      {erroGeral && (
        <p className="alerta alerta--erro" role="alert">
          {erroGeral}
        </p>
      )}
      <div className="formulario__acoes">
        <button type="submit" className="botao" disabled={enviando}>
          {enviando ? 'Salvando…' : edicao ? 'Salvar visita' : 'Adicionar visita'}
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

type FormAcaoProps = {
  inicial: CamposAcao
  edicao: boolean
  enviando: boolean
  erroGeral: string | null
  errosServidor: ErrosAcao
  nomeVisita: string
  onEnviar: (campos: CamposAcao) => void
  onCancelar?: () => void
}

function FormularioAcao({ inicial, edicao, enviando, erroGeral, errosServidor, nomeVisita, onEnviar, onCancelar }: FormAcaoProps) {
  const [campos, setCampos] = useState(inicial)
  const [errosLocais, setErrosLocais] = useState<ErrosAcao>({})
  const erros = { ...errosServidor, ...errosLocais }

  function alterar(campo: keyof CamposAcao, valor: string) {
    setCampos((atual) => ({ ...atual, [campo]: valor }))
    setErrosLocais((atual) => ({ ...atual, [campo]: undefined }))
  }

  function enviar(evento: FormEvent) {
    evento.preventDefault()
    const encontrados = validarAcao(campos)
    setErrosLocais(encontrados)
    if (Object.keys(encontrados).length === 0) onEnviar(campos)
  }

  return (
    <form
      className="formulario"
      onSubmit={enviar}
      noValidate
      aria-label={edicao ? `Editar atividade da visita ${nomeVisita}` : `Nova atividade da visita ${nomeVisita}`}
    >
      <Campo rotulo="Nome da atividade" erro={erros.nome}>
        <input value={campos.nome} maxLength={100} onChange={(e) => alterar('nome', e.target.value)} />
      </Campo>
      <Campo rotulo="Descrição da atividade" erro={erros.descricao}>
        <input value={campos.descricao} maxLength={500} onChange={(e) => alterar('descricao', e.target.value)} />
      </Campo>
      <Campo rotulo="Categoria" erro={erros.categoria}>
        <select value={campos.categoria} onChange={(e) => alterar('categoria', e.target.value)}>
          <option value="">Selecione…</option>
          {categoriasAcao.map((categoria) => (
            <option key={categoria} value={categoria}>
              {rotulosCategoria[categoria]}
            </option>
          ))}
        </select>
      </Campo>
      {erroGeral && (
        <p className="alerta alerta--erro" role="alert">
          {erroGeral}
        </p>
      )}
      <div className="formulario__acoes">
        <button type="submit" className="botao botao--secundario" disabled={enviando}>
          {edicao ? 'Salvar atividade' : 'Adicionar atividade'}
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

type CartaoProps = {
  visita: VisitaResponse
  editavel: boolean
  aoMudar: () => void
  aoEditar: (visita: VisitaResponse) => void
  aoExcluir: (visita: VisitaResponse) => void
}

function CartaoVisita({ visita, editavel, aoMudar, aoEditar, aoExcluir }: CartaoProps) {
  const [editandoAcao, setEditandoAcao] = useState<AcaoResponse | null>(null)
  const [enviando, setEnviando] = useState(false)
  const [erroGeral, setErroGeral] = useState<string | null>(null)
  const [errosServidor, setErrosServidor] = useState<ErrosAcao>({})

  async function salvarAcao(campos: CamposAcao) {
    setEnviando(true)
    setErroGeral(null)
    setErrosServidor({})
    try {
      const dados = {
        nome: campos.nome.trim(),
        descricao: campos.descricao.trim() || undefined,
        categoria: campos.categoria as CategoriaAcao,
      }
      if (editandoAcao) {
        await acoesApi.atualizar(visita.id, editandoAcao.id, dados)
        setEditandoAcao(null)
      } else {
        await acoesApi.criar(visita.id, dados)
      }
      aoMudar()
    } catch (falha) {
      const convertido = converterErro(falha)
      setErrosServidor(convertido.campos as ErrosAcao)
      setErroGeral(Object.keys(convertido.campos).length > 0 ? null : convertido.mensagem)
    } finally {
      setEnviando(false)
    }
  }

  async function excluirAcao(acao: AcaoResponse) {
    setErroGeral(null)
    try {
      await acoesApi.excluir(visita.id, acao.id)
      aoMudar()
    } catch (falha) {
      setErroGeral(converterErro(falha).mensagem)
    }
  }

  return (
    <details className="cartao-visita">
      <summary>
        <span>
          Dia {visita.diaPrevisto} · {visita.nome}
        </span>
        <span className="etiqueta">± {visita.tolerancia} dias</span>
        <span className="etiqueta">
          {visita.acoes.length} {visita.acoes.length === 1 ? 'atividade' : 'atividades'}
        </span>
      </summary>
      <div className="cartao-visita__corpo">
        {visita.acoes.length === 0 ? (
          <p className="texto-suave">Nenhuma atividade cadastrada nesta visita.</p>
        ) : (
          <ul className="itens">
            {visita.acoes.map((acao) => (
              <li key={acao.id} className="item">
                <div className="item__texto">
                  <div className="item__titulo">
                    {acao.nome}
                    <span className="etiqueta">{rotulosCategoria[acao.categoria]}</span>
                  </div>
                  {acao.descricao && <p className="item__descricao">{acao.descricao}</p>}
                </div>
                {editavel && (
                  <div className="tabela__acoes">
                    <button type="button" className="botao botao--secundario botao--pequeno" onClick={() => setEditandoAcao(acao)}>
                      Editar atividade
                    </button>
                    <button type="button" className="botao botao--perigo botao--pequeno" onClick={() => excluirAcao(acao)}>
                      Excluir atividade
                    </button>
                  </div>
                )}
              </li>
            ))}
          </ul>
        )}

        {editavel && (
          <>
            <FormularioAcao
              key={editandoAcao?.id ?? 'nova'}
              inicial={
                editandoAcao
                  ? { nome: editandoAcao.nome, descricao: editandoAcao.descricao ?? '', categoria: editandoAcao.categoria }
                  : { nome: '', descricao: '', categoria: '' }
              }
              edicao={editandoAcao !== null}
              enviando={enviando}
              erroGeral={erroGeral}
              errosServidor={errosServidor}
              nomeVisita={visita.nome}
              onEnviar={salvarAcao}
              onCancelar={editandoAcao ? () => setEditandoAcao(null) : undefined}
            />
            <div className="tabela__acoes">
              <button type="button" className="botao botao--secundario botao--pequeno" onClick={() => aoEditar(visita)}>
                Editar visita {visita.nome}
              </button>
              <button type="button" className="botao botao--perigo botao--pequeno" onClick={() => aoExcluir(visita)}>
                Excluir visita {visita.nome}
              </button>
            </div>
          </>
        )}
      </div>
    </details>
  )
}

// RF06 (T49): visitas planejadas ordenadas pelo dia previsto, cada uma com suas atividades. Editável
// pelo pesquisador enquanto o estudo está em planejamento; fora disso, e para o coordenador, só leitura.
export function AbaVisitas() {
  const { estudo } = useEstudoDetalhe()
  const usuario = useUsuarioLogado()
  const carregar = useCallback(() => visitasApi.listar(estudo.id), [estudo.id])
  const { dados: visitas, erro, carregando, recarregar } = useCarregamento(carregar)

  const editavel = estudo.status === 'PLANEJAMENTO' && usuario.perfil === 'PESQUISADOR'

  const [editando, setEditando] = useState<VisitaResponse | null>(null)
  const [excluindo, setExcluindo] = useState<VisitaResponse | null>(null)
  const [enviando, setEnviando] = useState(false)
  const [erroGeral, setErroGeral] = useState<string | null>(null)
  const [errosServidor, setErrosServidor] = useState<ErrosVisita>({})

  async function salvar(campos: CamposVisita) {
    setEnviando(true)
    setErroGeral(null)
    setErrosServidor({})
    try {
      const dados = { nome: campos.nome.trim(), diaPrevisto: Number(campos.diaPrevisto), tolerancia: Number(campos.tolerancia) }
      if (editando) {
        await visitasApi.atualizar(estudo.id, editando.id, dados)
        setEditando(null)
      } else {
        await visitasApi.criar(estudo.id, dados)
      }
      recarregar()
    } catch (falha) {
      const convertido = converterErro(falha)
      setErrosServidor(convertido.campos as ErrosVisita)
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
      await visitasApi.excluir(estudo.id, excluindo.id)
      setExcluindo(null)
      recarregar()
    } catch (falha) {
      setExcluindo(null)
      setErroGeral(converterErro(falha).mensagem)
    } finally {
      setEnviando(false)
    }
  }

  return (
    <div className="pilha">
      <h2>Visitas previstas e atividades</h2>

      {!editavel && (
        <p className="alerta alerta--info" role="status">
          {usuario.perfil !== 'PESQUISADOR'
            ? 'Seu perfil tem acesso apenas para consulta das visitas.'
            : 'O estudo não está mais em planejamento: as visitas estão somente para consulta.'}
        </p>
      )}
      {erro && (
        <p className="alerta alerta--erro" role="alert">
          {erro}
        </p>
      )}
      {carregando && <p className="carregando">Carregando visitas…</p>}

      {visitas && visitas.length === 0 && (
        <p className="texto-suave">
          {editavel
            ? 'Nenhuma visita prevista. Cadastre ao menos uma visita para avançar o estudo.'
            : 'Nenhuma visita prevista cadastrada.'}
        </p>
      )}
      {visitas && visitas.length > 0 && (
        <div className="pilha" aria-label="Visitas ordenadas pelo dia previsto">
          {visitas.map((visita) => (
            <CartaoVisita
              key={visita.id}
              visita={visita}
              editavel={editavel}
              aoMudar={recarregar}
              aoEditar={setEditando}
              aoExcluir={setExcluindo}
            />
          ))}
        </div>
      )}

      {editavel && (
        <section className="secao" aria-label={editando ? 'Editar visita' : 'Adicionar visita'}>
          <h3>{editando ? `Editar visita ${editando.nome}` : 'Nova visita'}</h3>
          <FormularioVisita
            key={editando?.id ?? 'nova'}
            inicial={
              editando
                ? { nome: editando.nome, diaPrevisto: String(editando.diaPrevisto), tolerancia: String(editando.tolerancia) }
                : { nome: '', diaPrevisto: '', tolerancia: '0' }
            }
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
          titulo="Excluir visita?"
          mensagem={`A visita "${excluindo.nome}" e todas as suas atividades serão removidas. Esta ação não pode ser desfeita.`}
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
