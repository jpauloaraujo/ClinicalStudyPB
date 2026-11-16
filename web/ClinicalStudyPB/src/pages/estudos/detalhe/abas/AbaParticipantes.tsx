import { useCallback } from 'react'
import { Link } from 'react-router'
import { agendamentoApi } from '../../../../api/agendamento'
import { participantesApi } from '../../../../api/participantes'
import { useUsuarioLogado } from '../../../../contexts/sessao'
import { useCarregamento } from '../../../../hooks/useCarregamento'
import { formatarData } from '../../../../utils/datas'
import { useEstudoDetalhe } from '../contexto'
import '../recrutamento.css'

// RF11 (T61) e RF12 (T77): participantes incluídos no estudo, o contador de vagas e o progresso de agendamento de cada
// um ("X de N visitas agendadas"). A aba é recarregada sempre que é aberta, então mostra a inclusão feita há pouco na
// ficha do candidato. O coordenador abre o agendamento do participante; o pesquisador só consulta (somente leitura).
export function AbaParticipantes() {
  const { estudo } = useEstudoDetalhe()
  const usuario = useUsuarioLogado()
  const ehCoordenador = usuario.perfil === 'COORDENADOR'
  const carregarLista = useCallback(() => participantesApi.listar(estudo.id), [estudo.id])
  const carregarVagas = useCallback(() => participantesApi.vagas(estudo.id), [estudo.id])
  const carregarProgresso = useCallback(() => agendamentoApi.participantesDaAgenda(estudo.id), [estudo.id])
  const lista = useCarregamento(carregarLista)
  const vagas = useCarregamento(carregarVagas)
  const progresso = useCarregamento(carregarProgresso)

  const erro = lista.erro ?? vagas.erro ?? progresso.erro
  const participantes = lista.dados
  const progressoPorParticipacao = new Map((progresso.dados ?? []).map((item) => [item.participacaoId, item]))

  return (
    <div className="pilha">
      <div className="barra-titulo">
        <h2>Participantes</h2>
        {vagas.dados && (
          <span className="etiqueta">
            {vagas.dados.incluidos} de {vagas.dados.limite} vagas
          </span>
        )}
      </div>

      {erro && (
        <p className="alerta alerta--erro" role="alert">
          {erro}
        </p>
      )}
      {lista.carregando && <p className="carregando">Carregando participantes…</p>}

      {participantes && participantes.length === 0 && (
        <p className="texto-suave">Nenhum participante incluído ainda. A inclusão é feita na ficha do candidato.</p>
      )}
      {participantes && participantes.length > 0 && (
        <div className="tabela-container">
          <table className="tabela tabela--candidatos">
            <thead>
              <tr>
                <th>Código</th>
                <th>Nome fictício</th>
                <th>Data de inclusão</th>
                <th>Incluído por</th>
                <th>Visitas</th>
                <th aria-label="Agendamento" />
              </tr>
            </thead>
            <tbody>
              {participantes.map((participante) => {
                const andamento = progressoPorParticipacao.get(participante.id)
                const rotaDoAgendamento = `/estudos/${estudo.id}/participantes/${participante.id}/agendamento`
                return (
                  <tr key={participante.id}>
                    <td>{participante.codigo}</td>
                    <td>{participante.nomeFicticio}</td>
                    <td>{formatarData(participante.dataInclusao)}</td>
                    <td>{participante.incluidoPor}</td>
                    <td>
                      {andamento ? `${andamento.visitasAgendadas} de ${andamento.visitasPrevistas} visitas agendadas` : '—'}
                    </td>
                    <td className="tabela__acoes">
                      <Link
                        to={rotaDoAgendamento}
                        className={ehCoordenador ? 'botao botao--pequeno' : 'botao botao--secundario botao--pequeno'}
                        aria-label={`${ehCoordenador ? 'Agendamento' : 'Ver agendamento'} de ${participante.codigo}`}
                      >
                        {ehCoordenador ? 'Agendamento' : 'Ver agendamento'}
                      </Link>
                    </td>
                  </tr>
                )
              })}
            </tbody>
          </table>
        </div>
      )}
      {!ehCoordenador && (
        <p className="texto-suave">Seu perfil tem acesso apenas para consulta dos participantes e da agenda.</p>
      )}
    </div>
  )
}
