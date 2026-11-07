import { useCallback } from 'react'
import { participantesApi } from '../../../../api/participantes'
import { useCarregamento } from '../../../../hooks/useCarregamento'
import { formatarData } from '../../../../utils/datas'
import { useEstudoDetalhe } from '../contexto'
import '../recrutamento.css'

// RF11 (T61): participantes incluídos no estudo e o contador de vagas. A aba é recarregada sempre que é aberta,
// então mostra a inclusão feita há pouco na ficha do candidato.
export function AbaParticipantes() {
  const { estudo } = useEstudoDetalhe()
  const carregarLista = useCallback(() => participantesApi.listar(estudo.id), [estudo.id])
  const carregarVagas = useCallback(() => participantesApi.vagas(estudo.id), [estudo.id])
  const lista = useCarregamento(carregarLista)
  const vagas = useCarregamento(carregarVagas)

  const erro = lista.erro ?? vagas.erro
  const participantes = lista.dados

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
              </tr>
            </thead>
            <tbody>
              {participantes.map((participante) => (
                <tr key={participante.id}>
                  <td>{participante.codigo}</td>
                  <td>{participante.nomeFicticio}</td>
                  <td>{formatarData(participante.dataInclusao)}</td>
                  <td>{participante.incluidoPor}</td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}
    </div>
  )
}
