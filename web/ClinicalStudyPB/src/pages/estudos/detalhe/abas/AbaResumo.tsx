import { formatarData } from '../../../../utils/datas'
import { useEstudoDetalhe } from '../contexto'

// Aba Resumo (T39): dados cadastrais do estudo vindos de GET /api/estudos/{id}
export function AbaResumo() {
  const { estudo } = useEstudoDetalhe()

  return (
    <>
      <h2>Resumo</h2>
      <dl className="resumo-estudo">
        <dt>Objetivo</dt>
        <dd>{estudo.objetivo}</dd>
        <dt>Responsável</dt>
        <dd>{estudo.responsavel.nome}</dd>
        <dt>Período</dt>
        <dd>
          {formatarData(estudo.dataInicio)} a {formatarData(estudo.dataFim)}
        </dd>
        <dt>Limite de participantes</dt>
        <dd>{estudo.limiteParticipantes}</dd>
      </dl>
    </>
  )
}
