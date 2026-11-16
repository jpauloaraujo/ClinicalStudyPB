import { rotulosSituacaoVisita, type SituacaoDoSelo } from './situacaoVisita'
import './acompanhamento.css'

// Rótulo em texto e cor da situação de uma visita. O texto sempre aparece para a informação não depender só da
// cor. Ponto de encaixe compartilhado: agenda e ficha de agendamento (PBI-19), histórico (T97) e app (T118).
export function SeloSituacaoVisita({ situacao }: { situacao: SituacaoDoSelo }) {
  return <span className={`selo selo--${situacao.toLowerCase()}`}>{rotulosSituacaoVisita[situacao]}</span>
}
