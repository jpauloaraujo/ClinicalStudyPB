import { useId, useState } from 'react'
import { Link } from 'react-router'
import type { SituacaoNoHistorico, VisitaDoHistorico } from '../../../../api/tipos'
import { formatarData, formatarDataHora } from '../../../../utils/datas'
import './acompanhamento.css'
import { SeloSituacaoVisita } from './SeloSituacaoVisita'
import { rotulosSituacaoVisita, situacoesDoHistorico } from './situacaoVisita'

type Props = {
  visitas: VisitaDoHistorico[]
  estudoId: string
  participacaoId: string
}

type Filtro = SituacaoNoHistorico | 'TODAS'

// Linha do tempo das visitas previstas do participante (RF15, T97): todas, na ordem recebida do servidor (dia previsto),
// cada uma expansível para mostrar datas, motivo, observação e resultados. O filtro por situação só esconde linhas;
// não recalcula nada: o resumo do servidor continua valendo para o total.
export function LinhaDoTempoVisitas({ visitas, estudoId, participacaoId }: Props) {
  const [filtro, setFiltro] = useState<Filtro>('TODAS')
  const [abertas, setAbertas] = useState<ReadonlySet<string>>(new Set())
  const idFiltro = useId()
  const idBase = useId()

  const exibidas = filtro === 'TODAS' ? visitas : visitas.filter((visita) => visita.situacao === filtro)

  function alternar(visitaPlanejadaId: string) {
    setAbertas((atuais) => {
      const proximas = new Set(atuais)
      if (!proximas.delete(visitaPlanejadaId)) {
        proximas.add(visitaPlanejadaId)
      }
      return proximas
    })
  }

  return (
    <div>
      <div className="filtro-visitas">
        <label htmlFor={idFiltro}>Filtrar por situação</label>
        <select id={idFiltro} value={filtro} onChange={(evento) => setFiltro(evento.target.value as Filtro)}>
          <option value="TODAS">Todas</option>
          {situacoesDoHistorico.map((situacao) => (
            <option key={situacao} value={situacao}>
              {rotulosSituacaoVisita[situacao]}
            </option>
          ))}
        </select>
        <span className="texto-suave" role="status">
          Exibindo {exibidas.length} de {visitas.length} {visitas.length === 1 ? 'visita' : 'visitas'}
        </span>
      </div>

      {exibidas.length === 0 ? (
        <p className="texto-suave">Nenhuma visita com a situação selecionada.</p>
      ) : (
        <ol className="linha-do-tempo">
          {exibidas.map((visita) => {
            const aberta = abertas.has(visita.visitaPlanejadaId)
            const idCorpo = `${idBase}-${visita.visitaPlanejadaId}`
            const podeAbrirVisita =
              !!visita.visitaAgendadaId && (visita.situacao === 'AGENDADA' || visita.situacao === 'REALIZADA')
            return (
              <li key={visita.visitaPlanejadaId} className="visita-historico">
                <h4 className="visita-historico__titulo">
                  <button
                    type="button"
                    className="visita-historico__botao"
                    aria-expanded={aberta}
                    aria-controls={idCorpo}
                    onClick={() => alternar(visita.visitaPlanejadaId)}
                  >
                    <span className="visita-historico__nome">{visita.nome}</span>
                    <SeloSituacaoVisita situacao={visita.situacao} />
                    <span className="visita-historico__meta">
                      Dia {visita.diaPrevisto} · sugerida em {formatarData(visita.dataSugerida)}
                    </span>
                    {visita.dataHoraAgendada && (
                      <span className="visita-historico__meta">Agendada: {formatarDataHora(visita.dataHoraAgendada)}</span>
                    )}
                    {visita.dataHoraRealizada && (
                      <span className="visita-historico__meta">Realizada: {formatarDataHora(visita.dataHoraRealizada)}</span>
                    )}
                  </button>
                </h4>

                {aberta && (
                  <div id={idCorpo} className="visita-historico__corpo">
                    <dl className="resumo-estudo">
                      <dt>Data e hora agendadas</dt>
                      <dd>{formatarDataHora(visita.dataHoraAgendada)}</dd>
                      <dt>Data e hora realizadas</dt>
                      <dd>{formatarDataHora(visita.dataHoraRealizada)}</dd>
                      <dt>Motivo</dt>
                      <dd>{visita.motivo || '—'}</dd>
                      <dt>Observação</dt>
                      <dd>{visita.observacao || '—'}</dd>
                    </dl>

                    <div>
                      <h5>Resultados</h5>
                      {visita.resultados.length === 0 ? (
                        <p className="texto-suave">Sem resultados registrados</p>
                      ) : (
                        <ul className="resultados-historico">
                          {visita.resultados.map((resultado) => (
                            <li key={resultado.acaoId} className="resultado-historico">
                              <span className="resultado-historico__nome">{resultado.nome}</span>
                              <span>
                                {resultado.valor}
                                {resultado.unidade ? ` ${resultado.unidade}` : ''}
                              </span>
                              {resultado.observacao && <span className="texto-suave">{resultado.observacao}</span>}
                            </li>
                          ))}
                        </ul>
                      )}
                    </div>

                    {podeAbrirVisita && (
                      <p>
                        <Link
                          to={`/estudos/${estudoId}/participantes/${participacaoId}/visitas/${visita.visitaAgendadaId}`}
                          className="botao botao--secundario botao--pequeno"
                        >
                          Abrir visita
                        </Link>
                      </p>
                    )}
                  </div>
                )}
              </li>
            )
          })}
        </ol>
      )}
    </div>
  )
}
