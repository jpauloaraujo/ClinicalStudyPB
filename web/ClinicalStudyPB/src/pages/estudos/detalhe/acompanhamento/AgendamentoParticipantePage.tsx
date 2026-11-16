import { useCallback, useState } from 'react'
import { Link, useParams } from 'react-router'
import { agendamentoApi } from '../../../../api/agendamento'
import type { VisitaDoParticipanteResponse } from '../../../../api/tipos'
import { useUsuarioLogado } from '../../../../contexts/sessao'
import { useCarregamento } from '../../../../hooks/useCarregamento'
import { dataHoraParaData, dataHoraParaHora, formatarData, formatarDataHora, formatarJanela } from '../../../../utils/datas'
import { useEstudoDetalhe } from '../contexto'
import { FormularioDataHora } from './FormularioDataHora'
import { interpretarErroDeAgendamento } from './mensagensAcompanhamento'
import {
  EXPLICACAO_ESTUDO_FORA_DE_ESTADO,
  ROTULO_NAO_AGENDADA,
  estudoPermiteAgendar,
  podeReagendar,
  rotulosSituacaoVisita,
} from './rotulosVisita'
import '../recrutamento.css'
import './acompanhamento.css'

// Hora padrão do formulário de agendamento quando a visita ainda não tem horário
const HORA_PADRAO = '09:00'

type Edicao = { modo: 'agendar' | 'reagendar'; visita: VisitaDoParticipanteResponse }

// RF12 (T77): ficha de agendamento do participante. Lista todas as visitas previstas do estudo com a janela válida e a
// data sugerida, e deixa o coordenador agendar e reagendar. O pesquisador vê tudo em leitura. Quem decide as regras
// (janela, data passada, estado do estudo) é o backend; a tela só mostra o resultado em mensagens claras.
export function AgendamentoParticipantePage() {
  const { participacaoId = '' } = useParams()
  const { estudo } = useEstudoDetalhe()
  const usuario = useUsuarioLogado()
  const ehCoordenador = usuario.perfil === 'COORDENADOR'
  const estudoAceita = estudoPermiteAgendar(estudo.status)

  const carregarVisitas = useCallback(() => agendamentoApi.visitasDoParticipante(participacaoId), [participacaoId])
  const carregarParticipantes = useCallback(() => agendamentoApi.participantesDaAgenda(estudo.id), [estudo.id])
  const visitas = useCarregamento(carregarVisitas)
  const participantes = useCarregamento(carregarParticipantes)
  const participante = participantes.dados?.find((item) => item.participacaoId === participacaoId)

  const [edicao, setEdicao] = useState<Edicao | null>(null)
  const [enviando, setEnviando] = useState(false)
  const [erroGeral, setErroGeral] = useState<string | null>(null)
  const [erroData, setErroData] = useState<string | null>(null)
  const [confirmacao, setConfirmacao] = useState<string | null>(null)

  function abrir(modo: Edicao['modo'], visita: VisitaDoParticipanteResponse) {
    setEdicao({ modo, visita })
    setErroGeral(null)
    setErroData(null)
    setConfirmacao(null)
  }

  function fechar() {
    setEdicao(null)
    setErroGeral(null)
    setErroData(null)
  }

  async function salvar(dataHora: string) {
    if (!edicao) return
    const { modo, visita } = edicao
    setEnviando(true)
    setErroGeral(null)
    setErroData(null)
    try {
      const salvo =
        modo === 'agendar'
          ? await agendamentoApi.agendar(participacaoId, { visitaPlanejadaId: visita.visitaPlanejadaId, dataHora })
          : await agendamentoApi.reagendar(visita.agendamento!.id, { dataHora })
      setEdicao(null)
      setConfirmacao(
        `${visita.nome} ${modo === 'agendar' ? 'agendada' : 'reagendada'} para ${formatarDataHora(salvo.dataHora)}.`,
      )
      visitas.recarregar()
      participantes.recarregar()
    } catch (falha) {
      const { geral, data } = interpretarErroDeAgendamento(falha, { inicio: visita.janelaInicio, fim: visita.janelaFim })
      setErroGeral(geral)
      setErroData(data)
    } finally {
      setEnviando(false)
    }
  }

  const lista = visitas.dados

  return (
    <div className="pilha">
      <div className="barra-titulo">
        <h2>Agendamento de visitas</h2>
        <Link to={`/estudos/${estudo.id}/participantes`} className="botao botao--secundario botao--pequeno">
          ← Voltar para os participantes
        </Link>
      </div>
      <p className="texto-suave">
        {participante ? `${participante.codigo} · ${participante.nomeFicticio}` : 'Participante do estudo'}
        {participante && ` · ${participante.visitasAgendadas} de ${participante.visitasPrevistas} visitas agendadas`}
      </p>

      {!estudoAceita && (
        <p className="alerta alerta--info" role="status">
          {EXPLICACAO_ESTUDO_FORA_DE_ESTADO}
        </p>
      )}
      {!ehCoordenador && (
        <p className="alerta alerta--info" role="status">
          Seu perfil tem acesso apenas para consulta do agendamento.
        </p>
      )}
      {confirmacao && (
        <p className="alerta alerta--ok" role="status">
          {confirmacao}
        </p>
      )}

      <section className="secao pilha" aria-label="Visitas do participante">
        <h3>Visitas do participante</h3>

        {visitas.carregando && <p className="carregando">Carregando visitas…</p>}
        {visitas.erro && (
          <div className="pilha">
            <p className="alerta alerta--erro" role="alert">
              {visitas.erro}
            </p>
            <div>
              <button type="button" className="botao botao--secundario botao--pequeno" onClick={visitas.recarregar}>
                Tentar de novo
              </button>
            </div>
          </div>
        )}
        {lista && lista.length === 0 && (
          <p className="texto-suave">
            Este estudo ainda não tem visitas previstas. Cadastre as visitas na aba Visitas para poder agendá-las.
          </p>
        )}
        {lista && lista.length > 0 && (
          <div className="tabela-container">
            <table className="tabela">
              <thead>
                <tr>
                  <th>Visita</th>
                  <th>Dia previsto</th>
                  <th>Tolerância</th>
                  <th>Janela válida</th>
                  <th>Data sugerida</th>
                  <th>Situação</th>
                  <th>Data e hora</th>
                  <th aria-label="Ações" />
                </tr>
              </thead>
              <tbody>
                {lista.map((visita) => {
                  const agendamento = visita.agendamento
                  return (
                    <tr key={visita.visitaPlanejadaId}>
                      <td>{visita.nome}</td>
                      <td>Dia {visita.diaPrevisto}</td>
                      <td>± {visita.tolerancia} dias</td>
                      <td>{formatarJanela(visita.janelaInicio, visita.janelaFim)}</td>
                      <td>{formatarData(visita.dataSugerida)}</td>
                      <td>
                        <span className="etiqueta">
                          {agendamento ? rotulosSituacaoVisita[agendamento.situacao] : ROTULO_NAO_AGENDADA}
                        </span>
                      </td>
                      <td>
                        {agendamento ? formatarDataHora(agendamento.dataHora) : '—'}
                        {agendamento?.dataHoraRealizada && (
                          <small className="linha-extra">Realizada em {formatarDataHora(agendamento.dataHoraRealizada)}</small>
                        )}
                      </td>
                      <td className="tabela__acoes">
                        {agendamento && (
                          <Link
                            to={`/estudos/${estudo.id}/participantes/${participacaoId}/visitas/${agendamento.id}`}
                            className="botao botao--secundario botao--pequeno"
                            aria-label={`Abrir a visita ${visita.nome}`}
                          >
                            Abrir visita
                          </Link>
                        )}
                        {ehCoordenador && !agendamento && (
                          <button
                            type="button"
                            className="botao botao--pequeno"
                            aria-label={`Agendar ${visita.nome}`}
                            disabled={!estudoAceita}
                            title={estudoAceita ? undefined : EXPLICACAO_ESTUDO_FORA_DE_ESTADO}
                            onClick={() => abrir('agendar', visita)}
                          >
                            Agendar
                          </button>
                        )}
                        {ehCoordenador && agendamento && podeReagendar(agendamento.situacao) && (
                          <button
                            type="button"
                            className="botao botao--secundario botao--pequeno"
                            aria-label={`Reagendar ${visita.nome}`}
                            disabled={!estudoAceita}
                            title={estudoAceita ? undefined : EXPLICACAO_ESTUDO_FORA_DE_ESTADO}
                            onClick={() => abrir('reagendar', visita)}
                          >
                            Reagendar
                          </button>
                        )}
                      </td>
                    </tr>
                  )
                })}
              </tbody>
            </table>
          </div>
        )}
      </section>

      {edicao && (
        <section className="secao pilha" aria-label={`${edicao.modo === 'agendar' ? 'Agendar' : 'Reagendar'} visita`}>
          <h3>
            {edicao.modo === 'agendar' ? 'Agendar' : 'Reagendar'} {edicao.visita.nome}
          </h3>
          <p className="texto-suave">
            Janela válida: {formatarJanela(edicao.visita.janelaInicio, edicao.visita.janelaFim)}.{' '}
            {edicao.modo === 'agendar'
              ? 'A data e a hora já vêm preenchidas com a sugestão; ajuste se precisar.'
              : 'A data e a hora atuais já vêm preenchidas.'}
          </p>
          <FormularioDataHora
            key={`${edicao.modo}-${edicao.visita.visitaPlanejadaId}`}
            titulo={`${edicao.modo === 'agendar' ? 'Agendar' : 'Reagendar'} ${edicao.visita.nome}`}
            dataInicial={
              edicao.modo === 'agendar'
                ? edicao.visita.dataSugerida
                : dataHoraParaData(edicao.visita.agendamento!.dataHora)
            }
            horaInicial={
              edicao.modo === 'agendar' ? HORA_PADRAO : dataHoraParaHora(edicao.visita.agendamento!.dataHora)
            }
            janela={{ inicio: edicao.visita.janelaInicio, fim: edicao.visita.janelaFim }}
            rotuloSalvar="Salvar"
            enviando={enviando}
            erroGeral={erroGeral}
            erroData={erroData}
            onSalvar={salvar}
            onCancelar={fechar}
          />
        </section>
      )}
    </div>
  )
}
