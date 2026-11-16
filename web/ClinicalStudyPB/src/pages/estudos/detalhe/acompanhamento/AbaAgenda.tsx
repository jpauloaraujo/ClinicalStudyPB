import { useCallback, useState } from 'react'
import { Link } from 'react-router'
import { agendamentoApi } from '../../../../api/agendamento'
import type { SituacaoVisita } from '../../../../api/tipos'
import { Campo } from '../../../../components/ui/Campo'
import { useCarregamento } from '../../../../hooks/useCarregamento'
import { formatarDataHora } from '../../../../utils/datas'
import { useEstudoDetalhe } from '../contexto'
import { AcoesDaVisita } from './AcoesDaVisita'
import { rotulosSituacaoVisita, situacoesVisita } from './rotulosVisita'
import '../recrutamento.css'
import './acompanhamento.css'

const TAMANHO_PAGINA = 20

// RF12 (T77): agenda do estudo, com as visitas de todos os participantes ordenadas por data e hora, filtro por período e
// por situação e paginação. É somente leitura para o pesquisador; o coordenador reagenda pela linha (AcoesDaVisita).
// Item de corte do planejamento (RQ-T77-15): se a capacidade faltar, esta aba é a primeira a sair; o agendamento por
// participante (AgendamentoParticipantePage) não pode ser cortado.
export function AbaAgenda() {
  const { estudo } = useEstudoDetalhe()
  const [de, setDe] = useState('')
  const [ate, setAte] = useState('')
  const [situacao, setSituacao] = useState<SituacaoVisita | ''>('')
  const [pagina, setPagina] = useState(0)

  const periodoInvalido = de !== '' && ate !== '' && de > ate
  const filtrando = de !== '' || ate !== '' || situacao !== ''

  const carregar = useCallback(
    () =>
      // Período invertido nem chega ao servidor (que responderia 400): a tela avisa e espera a correção
      periodoInvalido
        ? Promise.resolve(null)
        : agendamentoApi.agenda(estudo.id, {
            de: de || undefined,
            ate: ate || undefined,
            situacao: situacao || undefined,
            pagina,
            tamanho: TAMANHO_PAGINA,
          }),
    [estudo.id, de, ate, situacao, pagina, periodoInvalido],
  )
  const { dados: lista, erro, carregando, recarregar } = useCarregamento(carregar)

  function limparFiltros() {
    setDe('')
    setAte('')
    setSituacao('')
    setPagina(0)
  }

  return (
    <div className="pilha">
      <h2>Agenda</h2>

      <section className="pilha" aria-label="Agenda do estudo">
        <div className="filtros filtros--agenda">
          <Campo rotulo="De">
            <input
              type="date"
              value={de}
              onChange={(e) => {
                setDe(e.target.value)
                setPagina(0)
              }}
            />
          </Campo>
          <Campo rotulo="Até">
            <input
              type="date"
              value={ate}
              onChange={(e) => {
                setAte(e.target.value)
                setPagina(0)
              }}
            />
          </Campo>
          <Campo rotulo="Situação">
            <select
              value={situacao}
              onChange={(e) => {
                setSituacao(e.target.value as SituacaoVisita | '')
                setPagina(0)
              }}
            >
              <option value="">Todas</option>
              {situacoesVisita.map((s) => (
                <option key={s} value={s}>
                  {rotulosSituacaoVisita[s]}
                </option>
              ))}
            </select>
          </Campo>
          <button
            type="button"
            className="botao botao--secundario botao--pequeno"
            disabled={!filtrando}
            onClick={limparFiltros}
          >
            Limpar filtros
          </button>
        </div>

        {periodoInvalido && (
          <p className="alerta alerta--erro" role="alert">
            A data inicial não pode ser posterior à data final.
          </p>
        )}
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
        {carregando && <p className="carregando">Carregando agenda…</p>}

        {lista && lista.conteudo.length === 0 && (
          <p className="texto-suave">
            {filtrando ? 'Nenhuma visita no período.' : 'Nenhuma visita agendada neste estudo ainda.'}
          </p>
        )}
        {lista && lista.conteudo.length > 0 && (
          <>
            <div className="tabela-container">
              <table className="tabela">
                <thead>
                  <tr>
                    <th>Data e hora</th>
                    <th>Participante</th>
                    <th>Visita</th>
                    <th>Situação</th>
                    <th aria-label="Ações" />
                  </tr>
                </thead>
                <tbody>
                  {lista.conteudo.map((item) => (
                    <tr key={item.visitaAgendadaId}>
                      <td>{formatarDataHora(item.dataHora)}</td>
                      <td>
                        {item.participanteCodigo} · {item.participanteNome}
                      </td>
                      <td>{item.visitaNome}</td>
                      <td>
                        <span className="etiqueta">{rotulosSituacaoVisita[item.situacao]}</span>
                      </td>
                      <td className="tabela__acoes">
                        <Link
                          to={`/estudos/${estudo.id}/participantes/${item.participacaoId}/visitas/${item.visitaAgendadaId}`}
                          className="botao botao--secundario botao--pequeno"
                          aria-label={`Abrir a visita ${item.visitaNome} de ${item.participanteCodigo}`}
                        >
                          Abrir visita
                        </Link>
                        <AcoesDaVisita
                          visita={{
                            visitaAgendadaId: item.visitaAgendadaId,
                            participacaoId: item.participacaoId,
                            situacao: item.situacao,
                            dataHora: item.dataHora,
                            visitaNome: item.visitaNome,
                          }}
                          aoAlterar={recarregar}
                        />
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
            <nav className="paginacao" aria-label="Paginação da agenda">
              <button
                type="button"
                className="botao botao--secundario botao--pequeno"
                disabled={pagina === 0}
                onClick={() => setPagina(pagina - 1)}
              >
                Anterior
              </button>
              <span>
                Página {lista.pagina + 1} de {Math.max(1, lista.totalPaginas)} · {lista.totalElementos}{' '}
                {lista.totalElementos === 1 ? 'visita' : 'visitas'}
              </span>
              <button
                type="button"
                className="botao botao--secundario botao--pequeno"
                disabled={pagina + 1 >= lista.totalPaginas}
                onClick={() => setPagina(pagina + 1)}
              >
                Próxima
              </button>
            </nav>
          </>
        )}
      </section>
    </div>
  )
}
