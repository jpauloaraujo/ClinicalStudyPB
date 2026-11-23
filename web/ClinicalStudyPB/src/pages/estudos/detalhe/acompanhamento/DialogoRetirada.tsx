import { useEffect, useId, useRef, useState, type FormEvent } from 'react'
import { converterErro } from '../../../../api/erros'
import { retiradaApi } from '../../../../api/retirada'
import {
  rotulosSolicitanteRetirada,
  solicitantesRetirada,
  type RetiradaResponse,
  type SolicitanteRetirada,
  type VisitaDoHistorico,
} from '../../../../api/tipos'
import { Campo } from '../../../../components/ui/Campo'
import { formatarData } from '../../../../utils/datas'
import { mensagemDeRetirada } from './mensagensAcompanhamento'
import './acompanhamento.css'

export const MOTIVO_RETIRADA_MINIMO = 5
export const MOTIVO_RETIRADA_MAXIMO = 500

export type ParticipanteParaRetirada = {
  participacaoId: string
  codigo: string
  nomeFicticio: string
}

type DialogoRetiradaProps = {
  participante: ParticipanteParaRetirada
  visitas: VisitaDoHistorico[]
  aoFechar: () => void
  aoRetirar: (retirada: RetiradaResponse) => void
}

type FalhaRetirada = { mensagem: string; campos: Record<string, string> }

const CAMPO_DO_CODIGO: Record<string, string> = {
  MOTIVO_OBRIGATORIO: 'motivo',
  DATA_RETIRADA_FUTURA: 'dataRetirada',
}

// Data local de hoje no formato yyyy-MM-dd (para o valor padrão e max do <input type="date">)
function hojeParaCampo(): string {
  const agora = new Date()
  agora.setMinutes(agora.getMinutes() - agora.getTimezoneOffset())
  return agora.toISOString().slice(0, 10)
}

function contarVisitasAgendadas(visitas: VisitaDoHistorico[], agoraMs: number) {
  let futuras = 0
  let vencidas = 0
  for (const visita of visitas) {
    if (visita.situacao !== 'AGENDADA' || !visita.dataHoraAgendada) continue
    const instante = new Date(visita.dataHoraAgendada).getTime()
    if (Number.isNaN(instante)) continue
    if (instante > agoraMs) {
      futuras += 1
    } else {
      vencidas += 1
    }
  }
  return { futuras, vencidas }
}

// RF16 (T104): diálogo em duas etapas para registrar a retirada de um participante.
// Etapa 1: motivo (5 a 500 caracteres), quem solicitou e data (padrão hoje, sem data futura).
// Etapa 2: confirmação explícita da irreversibilidade, quantidade de visitas futuras afetadas e aviso de vencidas.
export function DialogoRetirada({ participante, visitas, aoFechar, aoRetirar }: DialogoRetiradaProps) {
  const idTitulo = useId()
  const campoMotivo = useRef<HTMLTextAreaElement>(null)
  const [hoje] = useState(hojeParaCampo)
  const [instanteAbertura] = useState(() => Date.now())

  const [etapa, setEtapa] = useState<'formulario' | 'confirmacao'>('formulario')
  const [motivo, setMotivo] = useState('')
  const [solicitante, setSolicitante] = useState<SolicitanteRetirada>('PARTICIPANTE')
  const [dataRetirada, setDataRetirada] = useState(hoje)
  const [tentouAvancar, setTentouAvancar] = useState(false)
  const [enviando, setEnviando] = useState(false)
  const [falha, setFalha] = useState<FalhaRetirada | null>(null)

  useEffect(() => {
    if (etapa === 'formulario') {
      campoMotivo.current?.focus()
    }
  }, [etapa])

  const { futuras: visitasFuturas, vencidas: visitasVencidas } = contarVisitasAgendadas(visitas, instanteAbertura)

  const motivoLimpo = motivo.trim()
  const erroLocalMotivo =
    motivoLimpo.length < MOTIVO_RETIRADA_MINIMO
      ? `Informe o motivo da retirada, com ao menos ${MOTIVO_RETIRADA_MINIMO} caracteres.`
      : undefined
  const erroLocalData =
    dataRetirada === ''
      ? 'A data da retirada é obrigatória.'
      : dataRetirada > hoje
        ? 'A data da retirada não pode estar no futuro.'
        : undefined

  function avancarParaConfirmacao(evento: FormEvent) {
    evento.preventDefault()
    setTentouAvancar(true)
    setFalha(null)
    if (erroLocalMotivo || erroLocalData) {
      return
    }
    setEtapa('confirmacao')
  }

  async function confirmarRetirada() {
    if (enviando) return
    setEnviando(true)
    setFalha(null)
    try {
      const resposta = await retiradaApi.registrar(participante.participacaoId, {
        motivo: motivoLimpo,
        solicitante,
        dataRetirada,
      })
      aoRetirar(resposta)
      aoFechar()
    } catch (erro) {
      const convertido = converterErro(erro)
      const campos = { ...convertido.campos }
      const mensagem = mensagemDeRetirada(convertido.codigo, convertido.mensagem)
      const campoDaRegra = convertido.codigo ? CAMPO_DO_CODIGO[convertido.codigo] : undefined
      if (campoDaRegra) campos[campoDaRegra] ??= mensagem

      const temErroDeCampo = Object.keys(campos).length > 0
      setFalha({ mensagem: temErroDeCampo ? '' : mensagem, campos })
      if (temErroDeCampo) {
        setEtapa('formulario')
      }
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
      {etapa === 'formulario' ? (
        <form
          className="dialogo dialogo--retirada"
          role="dialog"
          aria-modal="true"
          aria-labelledby={idTitulo}
          onSubmit={avancarParaConfirmacao}
          noValidate
        >
          <div>
            <h2 id={idTitulo}>Registrar retirada</h2>
            <p className="retirada__aviso">
              {participante.codigo} · {participante.nomeFicticio}. Informe os dados da retirada; na etapa seguinte você
              poderá revisar as visitas afetadas antes de confirmar.
            </p>
          </div>

          <div>
            <Campo
              rotulo="Motivo da retirada"
              erro={(tentouAvancar ? erroLocalMotivo : undefined) ?? falha?.campos.motivo}
            >
              <textarea
                ref={campoMotivo}
                value={motivo}
                maxLength={MOTIVO_RETIRADA_MAXIMO}
                rows={3}
                aria-required="true"
                onChange={(e) => {
                  setMotivo(e.target.value)
                  if (falha?.campos.motivo) setFalha(null)
                }}
              />
            </Campo>
            <span className="desfecho__contador" aria-live="polite">
              {motivo.length}/{MOTIVO_RETIRADA_MAXIMO} · mínimo de {MOTIVO_RETIRADA_MINIMO} caracteres
            </span>
          </div>

          <Campo rotulo="Quem solicitou" erro={falha?.campos.solicitante}>
            <select
              value={solicitante}
              onChange={(e) => setSolicitante(e.target.value as SolicitanteRetirada)}
            >
              {solicitantesRetirada.map((opcao) => (
                <option key={opcao} value={opcao}>
                  {rotulosSolicitanteRetirada[opcao]}
                </option>
              ))}
            </select>
          </Campo>

          <Campo
            rotulo="Data da retirada"
            erro={(tentouAvancar ? erroLocalData : undefined) ?? falha?.campos.dataRetirada}
          >
            <input
              type="date"
              value={dataRetirada}
              max={hoje}
              aria-required="true"
              onChange={(e) => {
                setDataRetirada(e.target.value)
                if (falha?.campos.dataRetirada) setFalha(null)
              }}
            />
          </Campo>

          {falha?.mensagem && (
            <p className="alerta alerta--erro" role="alert">
              {falha.mensagem}
            </p>
          )}

          <div className="formulario__acoes">
            <button type="submit" className="botao botao--perigo">
              Continuar
            </button>
            <button type="button" className="botao botao--secundario" onClick={aoFechar}>
              Cancelar
            </button>
          </div>
        </form>
      ) : (
        <div
          className="dialogo dialogo--retirada"
          role="dialog"
          aria-modal="true"
          aria-labelledby={idTitulo}
        >
          <div>
            <h2 id={idTitulo}>Confirmar retirada do participante</h2>
            <p className="retirada__aviso">
              {participante.codigo} · {participante.nomeFicticio}
            </p>
          </div>

          <p className="alerta alerta--erro" role="status">
            <strong>Esta retirada é irreversível:</strong> o participante passará para <strong>RETIRADO</strong>, não
            poderá ser reativado e a vaga continuará ocupada.
          </p>

          <p className="alerta alerta--info" role="status">
            {visitasFuturas === 0
              ? 'Nenhuma visita futura agendada será cancelada.'
              : `${visitasFuturas} ${visitasFuturas === 1 ? 'visita futura agendada será cancelada' : 'visitas futuras agendadas serão canceladas'} automaticamente com o motivo "retirada do estudo".`}
          </p>

          {visitasVencidas > 0 && (
            <p className="alerta retirada__alerta-vencidas" role="status">
              Atenção: há {visitasVencidas}{' '}
              {visitasVencidas === 1 ? 'visita agendada vencida sem desfecho' : 'visitas agendadas vencidas sem desfecho'}.
              Elas não são canceladas automaticamente e o registro de desfecho ficará bloqueado após a retirada;
              recomenda-se registrar o desfecho antes de confirmar.
            </p>
          )}

          <dl className="detalhes">
            <div>
              <dt>Solicitante</dt>
              <dd>{rotulosSolicitanteRetirada[solicitante]}</dd>
            </div>
            <div>
              <dt>Data da retirada</dt>
              <dd>{formatarData(dataRetirada)}</dd>
            </div>
            <div>
              <dt>Motivo</dt>
              <dd>{motivoLimpo}</dd>
            </div>
          </dl>

          {falha?.mensagem && (
            <p className="alerta alerta--erro" role="alert">
              {falha.mensagem}
            </p>
          )}

          <div className="formulario__acoes">
            <button
              type="button"
              className="botao botao--perigo"
              disabled={enviando}
              onClick={confirmarRetirada}
            >
              {enviando ? 'Registrando…' : 'Confirmar retirada'}
            </button>
            <button
              type="button"
              className="botao botao--secundario"
              disabled={enviando}
              onClick={() => {
                setFalha(null)
                setEtapa('formulario')
              }}
            >
              Voltar
            </button>
          </div>
        </div>
      )}
    </div>
  )
}
