import type { VisitaAgendadaResponse } from '../../../../api/tipos'
import { SeloSituacaoVisita } from './SeloSituacaoVisita'
import './acompanhamento.css'

function formatarDataHora(valor: string): string {
  const data = new Date(valor)
  return Number.isNaN(data.getTime()) ? valor : data.toLocaleString('pt-BR')
}

// O que foi registrado em uma visita com desfecho (RF13, T83): somente leitura, sem botão de escrita. Para a
// visita ainda AGENDADA não mostra nada: ela é tratada pelas ações da visita.
export function DesfechoRegistrado({ visita }: { visita: VisitaAgendadaResponse }) {
  if (visita.situacao === 'AGENDADA') return null

  return (
    <section className="secao pilha" aria-label="Desfecho registrado">
      <h3>Desfecho registrado</h3>
      <dl className="resumo-estudo">
        <dt>Situação</dt>
        <dd>
          <SeloSituacaoVisita situacao={visita.situacao} />
        </dd>
        {visita.situacao === 'REALIZADA' && visita.dataHoraRealizada && (
          <>
            <dt>Realizada em</dt>
            <dd>{formatarDataHora(visita.dataHoraRealizada)}</dd>
          </>
        )}
        {visita.situacao === 'CANCELADA' && visita.motivo && (
          <>
            <dt>Motivo do cancelamento</dt>
            <dd>{visita.motivo}</dd>
          </>
        )}
        {visita.observacao && (
          <>
            <dt>Observação</dt>
            <dd>{visita.observacao}</dd>
          </>
        )}
        <dt>Registrado por</dt>
        <dd>
          {visita.alteradoPor} em {formatarDataHora(visita.alteradoEm)}
        </dd>
      </dl>
    </section>
  )
}
