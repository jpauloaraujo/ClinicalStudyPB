import { useEffect, useId, useRef, useState, type FormEvent } from 'react'
import { visitasAgendadasApi } from '../../../../api/visitasAgendadas'
import { converterErro } from '../../../../api/erros'
import type { DesfechoRequest, TipoDesfecho, VisitaAgendadaResponse } from '../../../../api/tipos'
import { Campo } from '../../../../components/ui/Campo'
import { mensagemDeAcompanhamento } from './mensagensAcompanhamento'
import './acompanhamento.css'

/** O mínimo que o diálogo precisa saber da visita: a agenda e a ficha do participante montam isto. */
export type VisitaParaDesfecho = {
  id: string
  visitaNome: string
  participanteNome: string
  dataHora: string
}

type DialogoDesfechoProps = {
  visita: VisitaParaDesfecho
  aoFechar: () => void
  // Chamado com a visita atualizada, para a tela de origem (agenda ou ficha) recarregar e confirmar
  aoRegistrar: (visita: VisitaAgendadaResponse) => void
}

export const MOTIVO_MINIMO = 5
export const TEXTO_MAXIMO = 500

const OPCOES: { tipo: TipoDesfecho; rotulo: string }[] = [
  { tipo: 'REALIZADA', rotulo: 'Realizada' },
  { tipo: 'AUSENTE', rotulo: 'Ausente' },
  { tipo: 'CANCELADA', rotulo: 'Cancelada' },
]

// Erros de regra que a tela mostra junto do campo a que se referem; os demais vão no alerta do diálogo
const CAMPO_DO_CODIGO: Record<string, string> = {
  MOTIVO_OBRIGATORIO: 'motivo',
  DATA_REALIZACAO_FUTURA: 'dataHoraRealizada',
}

type FalhaDoRegistro = { mensagem: string; campos: Record<string, string> }

// Valor de um <input type="datetime-local"> para o instante atual (horário local, sem fuso)
function agoraParaCampo(): string {
  const agora = new Date()
  agora.setMinutes(agora.getMinutes() - agora.getTimezoneOffset())
  return agora.toISOString().slice(0, 16)
}

function formatarDataHora(valor: string): string {
  const data = new Date(valor)
  return Number.isNaN(data.getTime()) ? valor : data.toLocaleString('pt-BR')
}

// RF13 (T83): diálogo de registro do desfecho de uma visita agendada, com as três opções. A realizada guarda a
// data e hora reais (padrão agora, nunca futuras), a ausente aceita observação e a cancelada exige motivo.
export function DialogoDesfecho({ visita, aoFechar, aoRegistrar }: DialogoDesfechoProps) {
  const idTitulo = useId()
  const primeiraOpcao = useRef<HTMLInputElement>(null)
  const [tipo, setTipo] = useState<TipoDesfecho>('REALIZADA')
  const [dataHora, setDataHora] = useState(agoraParaCampo)
  const [dataAlterada, setDataAlterada] = useState(false)
  const [observacao, setObservacao] = useState('')
  const [motivo, setMotivo] = useState('')
  const [enviando, setEnviando] = useState(false)
  const [falha, setFalha] = useState<FalhaDoRegistro | null>(null)

  useEffect(() => {
    primeiraOpcao.current?.focus()
  }, [])

  const dataFutura = tipo === 'REALIZADA' && dataHora > agoraParaCampo()
  const erroLocalDaData =
    tipo !== 'REALIZADA'
      ? undefined
      : dataHora === ''
        ? 'Informe a data e a hora em que a visita foi realizada.'
        : dataFutura
          ? 'A data e a hora não podem estar no futuro.'
          : undefined
  const motivoCurto = tipo === 'CANCELADA' && motivo.trim().length < MOTIVO_MINIMO
  const incompleto = motivoCurto || erroLocalDaData !== undefined

  function montarCorpo(): DesfechoRequest {
    const obs = observacao.trim()
    if (tipo === 'REALIZADA') {
      // Sem edição manual, o servidor usa o instante atual (evita divergência de fuso entre navegador e API)
      return {
        tipo,
        ...(dataAlterada ? { dataHoraRealizada: `${dataHora}:00` } : {}),
        ...(obs ? { observacao: obs } : {}),
      }
    }
    if (tipo === 'AUSENTE') return { tipo, ...(obs ? { observacao: obs } : {}) }
    return { tipo, motivo: motivo.trim() }
  }

  async function confirmar(evento: FormEvent) {
    evento.preventDefault()
    if (incompleto || enviando) return
    setEnviando(true)
    setFalha(null)
    try {
      const atualizada = await visitasAgendadasApi.registrarDesfecho(visita.id, montarCorpo())
      aoRegistrar(atualizada)
      aoFechar()
    } catch (erro) {
      const convertido = converterErro(erro)
      const campos = { ...convertido.campos }
      const mensagem = mensagemDeAcompanhamento(convertido.codigo, convertido.mensagem)
      const campoDaRegra = convertido.codigo ? CAMPO_DO_CODIGO[convertido.codigo] : undefined
      if (campoDaRegra) campos[campoDaRegra] ??= mensagem
      setFalha({ mensagem: campoDaRegra ? '' : mensagem, campos })
    } finally {
      setEnviando(false)
    }
  }

  return (
    <div
      className="dialogo-fundo"
      onKeyDown={(evento) => {
        if (evento.key === 'Escape' && !enviando) aoFechar()
      }}
    >
      <form
        className="dialogo dialogo--desfecho"
        role="dialog"
        aria-modal="true"
        aria-labelledby={idTitulo}
        onSubmit={confirmar}
        noValidate
      >
        <div>
          <h2 id={idTitulo}>Registrar desfecho da visita</h2>
          <p className="desfecho__aviso">
            {visita.visitaNome} · {visita.participanteNome} · agendada para {formatarDataHora(visita.dataHora)}. O
            desfecho é final: depois de registrado, não pode ser desfeito.
          </p>
        </div>

        <fieldset className="desfecho__opcoes">
          <legend>Desfecho</legend>
          {OPCOES.map((opcao, indice) => (
            <label key={opcao.tipo} className="desfecho__opcao">
              <input
                ref={indice === 0 ? primeiraOpcao : undefined}
                type="radio"
                name="tipoDesfecho"
                value={opcao.tipo}
                checked={tipo === opcao.tipo}
                onChange={() => {
                  setTipo(opcao.tipo)
                  setFalha(null)
                }}
              />
              <span>{opcao.rotulo}</span>
            </label>
          ))}
        </fieldset>

        {tipo === 'REALIZADA' && (
          <Campo rotulo="Data e hora em que a visita foi realizada" erro={erroLocalDaData ?? falha?.campos.dataHoraRealizada}>
            <input
              type="datetime-local"
              value={dataHora}
              max={agoraParaCampo()}
              onChange={(e) => {
                setDataHora(e.target.value)
                setDataAlterada(true)
              }}
            />
          </Campo>
        )}

        {tipo === 'CANCELADA' ? (
          <div>
            <Campo rotulo="Motivo do cancelamento" erro={falha?.campos.motivo}>
              <textarea
                value={motivo}
                maxLength={TEXTO_MAXIMO}
                rows={3}
                aria-required="true"
                onChange={(e) => setMotivo(e.target.value)}
              />
            </Campo>
            <span className="desfecho__contador" aria-live="polite">
              {motivo.length}/{TEXTO_MAXIMO} · mínimo de {MOTIVO_MINIMO} caracteres
            </span>
          </div>
        ) : (
          <Campo rotulo="Observação (opcional)" erro={falha?.campos.observacao}>
            <textarea value={observacao} maxLength={TEXTO_MAXIMO} rows={3} onChange={(e) => setObservacao(e.target.value)} />
          </Campo>
        )}

        {falha?.mensagem && (
          <p className="alerta alerta--erro" role="alert">
            {falha.mensagem}
          </p>
        )}

        <div className="formulario__acoes">
          <button type="submit" className="botao" disabled={incompleto || enviando}>
            {enviando ? 'Registrando…' : 'Confirmar'}
          </button>
          <button type="button" className="botao botao--secundario" onClick={aoFechar} disabled={enviando}>
            Cancelar
          </button>
        </div>
      </form>
    </div>
  )
}
