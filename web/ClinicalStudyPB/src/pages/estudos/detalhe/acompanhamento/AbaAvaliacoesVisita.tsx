import { useCallback, useMemo, useState, type FormEvent } from 'react'
import { visitasAgendadasApi } from '../../../../api/visitasAgendadas'
import type {
  AtividadeComResultado,
  BloqueioResultados,
  ResultadoAtividadeResponse,
} from '../../../../api/tipos'
import { Campo } from '../../../../components/ui/Campo'
import { useUsuarioLogado } from '../../../../contexts/sessao'
import { useCarregamento } from '../../../../hooks/useCarregamento'
import { formatarDataHora } from '../../../../utils/datas'
import { interpretarErroDeResultado } from './mensagensAcompanhamento'
import './acompanhamento.css'

// Limites do contrato (ResultadoRequest), conferidos também no cliente
export const VALOR_MAXIMO = 200
export const UNIDADE_MAXIMA = 30
export const OBSERVACAO_MAXIMA = 500

// Como cada atividade é mostrada: formulário editável, formulário desabilitado (visita não realizada) ou texto
type ModoAtividade = 'edicao' | 'desabilitado' | 'leitura'

const AVISOS_DO_BLOQUEIO: Partial<Record<BloqueioResultados, string>> = {
  VISITA_NAO_REALIZADA: 'Os resultados só podem ser registrados em visitas realizadas.',
  ESTUDO_ENCERRADO:
    'O estudo foi encerrado: os resultados ficam disponíveis somente para consulta e não podem mais ser registrados nem editados.',
  // PARTICIPANTE_RETIRADO: aviso próprio na T104
}

function modoDaAba(perfil: string, bloqueio: BloqueioResultados | null): ModoAtividade {
  if (perfil !== 'PESQUISADOR') return 'leitura'
  if (bloqueio === 'VISITA_NAO_REALIZADA') return 'desabilitado'
  if (bloqueio) return 'leitura'
  return 'edicao'
}

// RF14 (T91): aba Avaliações da tela da visita. O pesquisador registra e edita, uma atividade por vez, o resultado
// fictício de cada atividade da categoria avaliação de uma visita REALIZADA; o coordenador consulta somente leitura.
// O servidor diz pelo `bloqueio` quando a visita não aceita registro (visita não realizada, estudo encerrado).
export function AbaAvaliacoesVisita({ visitaAgendadaId }: { visitaAgendadaId: string }) {
  const usuario = useUsuarioLogado()
  const carregar = useCallback(() => visitasAgendadasApi.resultados(visitaAgendadaId), [visitaAgendadaId])
  const { dados, erro, carregando, recarregar } = useCarregamento(carregar)

  // Resultados salvos nesta tela sobre o último carregamento (`base`): cada salvamento atualiza só a sua atividade
  // (selo, autoria e contador), sem recarregar a aba inteira; um novo carregamento os descarta
  const [salvos, setSalvos] = useState<{ base: unknown; mapa: Record<string, ResultadoAtividadeResponse> }>({
    base: null,
    mapa: {},
  })

  const aoSalvar = useCallback(
    (resultado: ResultadoAtividadeResponse) => {
      setSalvos((atuais) => ({
        base: dados,
        mapa: { ...(atuais.base === dados ? atuais.mapa : {}), [resultado.acaoId]: resultado },
      }))
    },
    [dados],
  )

  const atividades = useMemo<AtividadeComResultado[]>(() => {
    const mapa = salvos.base === dados ? salvos.mapa : {}
    return (dados?.atividades ?? []).map((atividade) =>
      mapa[atividade.acaoId] ? { ...atividade, resultado: mapa[atividade.acaoId] } : atividade,
    )
  }, [dados, salvos])

  const modo = modoDaAba(usuario.perfil, dados?.bloqueio ?? null)
  const avisoDoBloqueio = dados?.bloqueio ? AVISOS_DO_BLOQUEIO[dados.bloqueio] : undefined
  const registrados = atividades.filter((atividade) => atividade.resultado !== null).length
  const total = dados ? Math.max(dados.total, atividades.length) : 0

  return (
    <section className="secao pilha" aria-label="Avaliações da visita">
      {carregando && <p className="carregando">Carregando avaliações…</p>}
      {erro && (
        <div className="pilha">
          <p className="alerta alerta--erro" role="alert">
            {erro}
          </p>
          <div>
            <button type="button" className="botao botao--secundario botao--pequeno" onClick={recarregar}>
              Tentar de novo
            </button>
          </div>
        </div>
      )}

      {dados && !carregando && (
        <>
          {total === 0 ? (
            <p className="texto-suave">Esta visita não tem avaliações previstas.</p>
          ) : (
            <p className="avaliacoes__contador" aria-live="polite">
              {registrados} de {total} avaliações registradas
            </p>
          )}

          {usuario.perfil !== 'PESQUISADOR' && (
            <p className="alerta alerta--info">Somente o pesquisador registra resultados.</p>
          )}
          {avisoDoBloqueio && <p className="alerta alerta--info">{avisoDoBloqueio}</p>}

          {atividades.length > 0 && (
            <ul className="avaliacoes">
              {atividades.map((atividade) => (
                <li key={atividade.acaoId} className="avaliacao">
                  <AtividadeDeAvaliacao
                    visitaAgendadaId={visitaAgendadaId}
                    atividade={atividade}
                    modo={modo}
                    aoSalvar={aoSalvar}
                  />
                </li>
              ))}
            </ul>
          )}
        </>
      )}
    </section>
  )
}

function SeloResultado({ registrado }: { registrado: boolean }) {
  return (
    <span className={registrado ? 'selo selo--registrado' : 'selo selo--pendente'}>
      {registrado ? 'registrado' : 'pendente'}
    </span>
  )
}

function UltimaAlteracao({ resultado }: { resultado: ResultadoAtividadeResponse | null }) {
  if (!resultado) return null
  return (
    <p className="texto-suave avaliacao__autoria">
      Última alteração por {resultado.alteradoPor} em {formatarDataHora(resultado.alteradoEm)}
    </p>
  )
}

type PropsAtividade = {
  visitaAgendadaId: string
  atividade: AtividadeComResultado
  modo: ModoAtividade
  aoSalvar: (resultado: ResultadoAtividadeResponse) => void
}

function AtividadeDeAvaliacao({ visitaAgendadaId, atividade, modo, aoSalvar }: PropsAtividade) {
  const registrado = atividade.resultado !== null
  return (
    <article className="pilha" aria-label={atividade.nome}>
      <div className="avaliacao__cabecalho">
        <h3>{atividade.nome}</h3>
        <SeloResultado registrado={registrado} />
      </div>
      {atividade.descricao && <p className="texto-suave">{atividade.descricao}</p>}

      {modo === 'leitura' ? (
        <ResultadoSomenteLeitura atividade={atividade} />
      ) : (
        <FormularioResultado
          visitaAgendadaId={visitaAgendadaId}
          atividade={atividade}
          desabilitado={modo === 'desabilitado'}
          aoSalvar={aoSalvar}
        />
      )}
      <UltimaAlteracao resultado={atividade.resultado} />
    </article>
  )
}

function ResultadoSomenteLeitura({ atividade }: { atividade: AtividadeComResultado }) {
  const { resultado } = atividade
  if (!resultado) return <p className="texto-suave">Sem resultado registrado.</p>
  return (
    <dl className="detalhes">
      <div>
        <dt>Valor</dt>
        <dd>{resultado.valor}</dd>
      </div>
      <div>
        <dt>Unidade</dt>
        <dd>{resultado.unidade || '—'}</dd>
      </div>
      <div>
        <dt>Observação</dt>
        <dd>{resultado.observacao || '—'}</dd>
      </div>
    </dl>
  )
}

type Valores = { valor: string; unidade: string; observacao: string }

function valoresIniciais(resultado: ResultadoAtividadeResponse | null): Valores {
  return {
    valor: resultado?.valor ?? '',
    unidade: resultado?.unidade ?? '',
    observacao: resultado?.observacao ?? '',
  }
}

function validar(valores: Valores): Partial<Record<keyof Valores, string>> {
  const erros: Partial<Record<keyof Valores, string>> = {}
  const valor = valores.valor.trim()
  if (valor.length === 0) erros.valor = 'Informe o valor do resultado.'
  else if (valor.length > VALOR_MAXIMO) erros.valor = `O valor deve ter no máximo ${VALOR_MAXIMO} caracteres.`
  if (valores.unidade.trim().length > UNIDADE_MAXIMA) {
    erros.unidade = `A unidade deve ter no máximo ${UNIDADE_MAXIMA} caracteres.`
  }
  if (valores.observacao.trim().length > OBSERVACAO_MAXIMA) {
    erros.observacao = `A observação deve ter no máximo ${OBSERVACAO_MAXIMA} caracteres.`
  }
  return erros
}

function textoOuNulo(texto: string): string | null {
  const limpo = texto.trim()
  return limpo === '' ? null : limpo
}

type PropsFormulario = {
  visitaAgendadaId: string
  atividade: AtividadeComResultado
  desabilitado: boolean
  aoSalvar: (resultado: ResultadoAtividadeResponse) => void
}

function FormularioResultado({ visitaAgendadaId, atividade, desabilitado, aoSalvar }: PropsFormulario) {
  // Valores do último resultado salvo: o botão só habilita quando algo mudou em relação a eles
  const [iniciais, setIniciais] = useState(() => valoresIniciais(atividade.resultado))
  const [valores, setValores] = useState<Valores>(iniciais)
  const [tocados, setTocados] = useState<Partial<Record<keyof Valores, boolean>>>({})
  const [enviando, setEnviando] = useState(false)
  const [erroGeral, setErroGeral] = useState<string | null>(null)
  const [errosDoServidor, setErrosDoServidor] = useState<Record<string, string>>({})
  const [salvo, setSalvo] = useState(false)

  const errosLocais = validar(valores)
  const alterado =
    valores.valor !== iniciais.valor ||
    valores.unidade !== iniciais.unidade ||
    valores.observacao !== iniciais.observacao
  const invalido = Object.keys(errosLocais).length > 0
  const nome = atividade.nome

  function alterar(campo: keyof Valores, texto: string) {
    setValores((atuais) => ({ ...atuais, [campo]: texto }))
    setTocados((atuais) => ({ ...atuais, [campo]: true }))
    setErrosDoServidor((atuais) => {
      const restantes = { ...atuais }
      delete restantes[campo]
      return restantes
    })
    setErroGeral(null)
    setSalvo(false)
  }

  function erroDoCampo(campo: keyof Valores): string | undefined {
    return errosDoServidor[campo] ?? (tocados[campo] ? errosLocais[campo] : undefined)
  }

  async function salvar(evento: FormEvent) {
    evento.preventDefault()
    if (desabilitado || enviando || !alterado || invalido) return
    setEnviando(true)
    setErroGeral(null)
    setErrosDoServidor({})
    try {
      const resultado = await visitasAgendadasApi.registrarResultado(visitaAgendadaId, atividade.acaoId, {
        valor: valores.valor.trim(),
        unidade: textoOuNulo(valores.unidade),
        observacao: textoOuNulo(valores.observacao),
      })
      const salvos = valoresIniciais(resultado)
      setIniciais(salvos)
      setValores(salvos)
      setTocados({})
      setSalvo(true)
      setEnviando(false)
      aoSalvar(resultado)
    } catch (falha) {
      const { geral, campos } = interpretarErroDeResultado(falha)
      setErroGeral(geral)
      setErrosDoServidor(campos)
      setEnviando(false)
    }
  }

  return (
    <form className="pilha" onSubmit={salvar} noValidate>
      <div className="avaliacao__campos">
        <Campo rotulo="Valor" erro={erroDoCampo('valor')}>
          <input
            aria-label={`Valor (${nome})`}
            value={valores.valor}
            disabled={desabilitado}
            required
            onChange={(e) => alterar('valor', e.target.value)}
          />
          <span className="avaliacao__contador">
            {valores.valor.length}/{VALOR_MAXIMO}
          </span>
        </Campo>
        <Campo rotulo="Unidade" erro={erroDoCampo('unidade')}>
          <input
            aria-label={`Unidade (${nome})`}
            value={valores.unidade}
            disabled={desabilitado}
            onChange={(e) => alterar('unidade', e.target.value)}
          />
          <span className="avaliacao__contador">
            {valores.unidade.length}/{UNIDADE_MAXIMA}
          </span>
        </Campo>
      </div>
      <Campo rotulo="Observação" erro={erroDoCampo('observacao')}>
        <textarea
          aria-label={`Observação (${nome})`}
          rows={2}
          value={valores.observacao}
          disabled={desabilitado}
          onChange={(e) => alterar('observacao', e.target.value)}
        />
        <span className="avaliacao__contador">
          {valores.observacao.length}/{OBSERVACAO_MAXIMA}
        </span>
      </Campo>

      {erroGeral && (
        <p className="alerta alerta--erro" role="alert">
          {erroGeral}
        </p>
      )}
      {salvo && (
        <p className="alerta alerta--ok" role="status">
          Resultado salvo.
        </p>
      )}

      <div>
        <button
          type="submit"
          className="botao botao--pequeno"
          aria-label={`Salvar (${nome})`}
          disabled={desabilitado || enviando || !alterado || invalido}
        >
          {enviando ? 'Salvando…' : 'Salvar'}
        </button>
      </div>
    </form>
  )
}
