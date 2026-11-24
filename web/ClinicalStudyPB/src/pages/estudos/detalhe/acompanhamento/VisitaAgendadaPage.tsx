import { useCallback, useState } from 'react'
import { Link, useParams } from 'react-router'
import { agendamentoApi } from '../../../../api/agendamento'
import type { VisitaAgendadaResponse, VisitaDoParticipanteResponse } from '../../../../api/tipos'
import { useUsuarioLogado } from '../../../../contexts/sessao'
import { useCarregamento } from '../../../../hooks/useCarregamento'
import { formatarDataHora, formatarJanela } from '../../../../utils/datas'
import { useEstudoDetalhe } from '../contexto'
import { AbaAvaliacoesVisita } from './AbaAvaliacoesVisita'
import { AcoesDaVisita } from './AcoesDaVisita'
import { abasVisitaDoPerfil } from './abasVisita'
import { DesfechoRegistrado } from './DesfechoRegistrado'
import { SeloSituacaoVisita } from './SeloSituacaoVisita'
import '../recrutamento.css'
import './acompanhamento.css'

type DadosDaVisita = { previsao: VisitaDoParticipanteResponse; visita: VisitaAgendadaResponse }

// Conteúdo de cada aba da tela da visita. Para acrescentar uma aba, declare-a em abasVisita.ts e trate o novo caminho
// aqui, sem mexer no restante da tela.
function ConteudoDaAba({ caminho, dados }: { caminho: string; dados: DadosDaVisita }) {
  if (caminho === 'dados') {
    return (
      <>
        <DadosDaVisitaAgendada dados={dados} />
        {/* RF13 (T83): o que foi registrado, somente leitura; some enquanto a visita está AGENDADA */}
        <DesfechoRegistrado visita={dados.visita} />
      </>
    )
  }
  if (caminho === 'avaliacoes') {
    // RF14 (T91). A chave pela situação recarrega os resultados quando o desfecho muda a visita (ex.: REALIZADA)
    return <AbaAvaliacoesVisita key={dados.visita.situacao} visitaAgendadaId={dados.visita.id} />
  }
  return null
}

function DadosDaVisitaAgendada({ dados: { previsao, visita } }: { dados: DadosDaVisita }) {
  return (
    <section className="secao" aria-label="Dados da visita">
      <dl className="detalhes">
        <div>
          <dt>Participante</dt>
          <dd>
            {visita.participanteCodigo} · {visita.participanteNome}
          </dd>
        </div>
        <div>
          <dt>Visita prevista</dt>
          <dd>
            {visita.visitaNome} (dia {previsao.diaPrevisto}, tolerância de {previsao.tolerancia} dias)
          </dd>
        </div>
        <div>
          <dt>Data e hora</dt>
          <dd>{formatarDataHora(visita.dataHora)}</dd>
        </div>
        <div>
          <dt>Situação</dt>
          <dd>
            <SeloSituacaoVisita situacao={visita.situacao} />
          </dd>
        </div>
        <div>
          <dt>Janela válida</dt>
          <dd>{formatarJanela(visita.janelaInicio, visita.janelaFim)}</dd>
        </div>
        <div>
          <dt>Criada por</dt>
          <dd>
            {visita.criadoPor} em {formatarDataHora(visita.criadoEm)}
          </dd>
        </div>
        <div>
          <dt>Alterada por</dt>
          <dd>
            {visita.alteradoPor} em {formatarDataHora(visita.alteradoEm)}
          </dd>
        </div>
      </dl>
    </section>
  )
}

// RF12 (T77): tela de uma visita agendada, com os dados e o ponto de encaixe das ações (AcoesDaVisita) e das abas
// (abasVisita.ts) que os próximos PBIs completam: desfecho (T83) e avaliações (T91). A rota traz a participação, e os
// dados saem da lista de visitas dela (operação 1 do contrato), que já inclui o agendamento.
export function VisitaAgendadaPage() {
  const { participacaoId = '', visitaAgendadaId = '' } = useParams()
  const { estudo } = useEstudoDetalhe()
  const usuario = useUsuarioLogado()
  const abas = abasVisitaDoPerfil(usuario.perfil)
  const [abaAtiva, setAbaAtiva] = useState(abas[0]?.caminho ?? 'dados')

  const carregar = useCallback(() => agendamentoApi.visitasDoParticipante(participacaoId), [participacaoId])
  const carregarParticipantes = useCallback(async () => {
    try {
      return (await agendamentoApi.participantesDaAgenda(estudo.id)) ?? []
    } catch {
      return []
    }
  }, [estudo.id])
  const { dados: visitas, erro, carregando, recarregar } = useCarregamento(carregar)
  const { dados: participantes } = useCarregamento(carregarParticipantes)
  const participanteRetirado =
    participantes?.find((item) => item.participacaoId === participacaoId)?.situacaoParticipacao === 'RETIRADO'

  const previsao = visitas?.find((item) => item.agendamento?.id === visitaAgendadaId)
  const visita = previsao?.agendamento ?? null
  const rotaDoAgendamento = `/estudos/${estudo.id}/participantes/${participacaoId}/agendamento`

  return (
    <div className="pilha">
      <div className="barra-titulo">
        <div className="titulo-com-selo">
          <h2>Visita agendada</h2>
          {participanteRetirado && <span className="selo selo--retirado">Retirado</span>}
        </div>
        <Link to={rotaDoAgendamento} className="botao botao--secundario botao--pequeno">
          ← Voltar para o agendamento
        </Link>
      </div>
      {participanteRetirado && (
        <p className="alerta alerta--info" role="status">
          Participante retirado do estudo: as ações de edição desta visita estão indisponíveis.
        </p>
      )}

      {carregando && <p className="carregando">Carregando visita…</p>}
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
      {visitas && !visita && (
        <p className="alerta alerta--erro" role="alert">
          Visita agendada não encontrada para este participante.
        </p>
      )}

      {previsao && visita && (
        <>
          <div className="barra-titulo">
            <p className="texto-suave">
              {visita.visitaNome} · {visita.participanteCodigo}
            </p>
            <AcoesDaVisita
              visita={{
                visitaAgendadaId: visita.id,
                participacaoId,
                situacao: visita.situacao,
                dataHora: visita.dataHora,
                visitaNome: visita.visitaNome,
                participanteNome: visita.participanteNome,
                janelaInicio: visita.janelaInicio,
                janelaFim: visita.janelaFim,
                participanteRetirado,
              }}
              aoAlterar={recarregar}
            />
          </div>

          <nav className="abas" aria-label="Seções da visita">
            <ul>
              {abas.map((aba) => (
                <li key={aba.caminho}>
                  <button
                    type="button"
                    className="abas__link"
                    aria-current={aba.caminho === abaAtiva ? 'page' : undefined}
                    onClick={() => setAbaAtiva(aba.caminho)}
                  >
                    {aba.rotulo}
                  </button>
                </li>
              ))}
            </ul>
          </nav>

          <ConteudoDaAba caminho={abaAtiva} dados={{ previsao, visita }} />
        </>
      )}
    </div>
  )
}
