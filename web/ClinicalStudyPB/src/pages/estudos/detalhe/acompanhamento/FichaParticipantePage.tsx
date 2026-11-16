import { useCallback } from 'react'
import { Link, useParams } from 'react-router'
import { historicoApi } from '../../../../api/historico'
import { useUsuarioLogado } from '../../../../contexts/sessao'
import { useCarregamento } from '../../../../hooks/useCarregamento'
import { formatarData } from '../../../../utils/datas'
import { useEstudoDetalhe } from '../contexto'
import '../recrutamento.css'
import './acompanhamento.css'
import { AreaAcoesParticipante } from './AreaAcoesParticipante'
import { LinhaDoTempoVisitas } from './LinhaDoTempoVisitas'

// Ficha do participante (RF15, T97): dados, cartões de resumo do histórico, linha do tempo de todas as visitas previstas
// e a área de ações (ponto de encaixe da retirada e do vínculo de conta). Somente leitura: a tela não grava nada, e os
// números do resumo vêm prontos do servidor (nunca são recalculados aqui).
export function FichaParticipantePage() {
  const { estudo } = useEstudoDetalhe()
  const { perfil } = useUsuarioLogado()
  const { participacaoId = '' } = useParams()
  const carregar = useCallback(() => historicoApi.buscar(participacaoId), [participacaoId])
  const { dados: historico, erro, statusErro, carregando, recarregar } = useCarregamento(carregar)

  if (carregando) {
    return <p className="carregando">Carregando histórico do participante…</p>
  }

  if (!historico) {
    const naoEncontrado = statusErro === 404 || statusErro === 400
    return (
      <div className="pilha">
        <h2>{naoEncontrado ? 'Participante não encontrado' : 'Não foi possível carregar o histórico'}</h2>
        <p className="alerta alerta--erro" role="alert">
          {naoEncontrado ? 'O participante solicitado não existe neste estudo.' : erro}
        </p>
        <p className="acoes-participante">
          {!naoEncontrado && (
            <button type="button" className="botao" onClick={recarregar}>
              Tentar de novo
            </button>
          )}
          <Link to={`/estudos/${estudo.id}/participantes`} className="botao botao--secundario">
            Voltar para participantes
          </Link>
        </p>
      </div>
    )
  }

  const { participante, resumo, visitas } = historico
  const cartoes = [
    { rotulo: 'Realizadas', valor: resumo.realizadas },
    { rotulo: 'Ausentes', valor: resumo.ausentes },
    { rotulo: 'Canceladas', valor: resumo.canceladas },
    { rotulo: 'Pendentes', valor: resumo.pendentes },
  ]

  return (
    <div className="pilha">
      <div className="barra-titulo">
        <h2>
          {participante.codigo} · {participante.nomeFicticio}
        </h2>
        <Link to={`/estudos/${estudo.id}/participantes`} className="botao botao--secundario botao--pequeno">
          ← Participantes
        </Link>
      </div>
      <p className="texto-suave">Incluído em {formatarData(participante.dataInclusao)}</p>

      <section className="secao" aria-label="Resumo do histórico">
        <h3>Resumo do histórico</h3>
        <ul className="cartoes-resumo">
          {cartoes.map((cartao) => (
            <li key={cartao.rotulo} className="cartao-resumo">
              <span className="cartao-resumo__valor">{cartao.valor}</span>
              <span className="cartao-resumo__rotulo">{cartao.rotulo}</span>
            </li>
          ))}
        </ul>
        <p className="texto-suave">
          {resumo.previstas} {resumo.previstas === 1 ? 'visita prevista' : 'visitas previstas'} no estudo
          {resumo.pendentes > 0 &&
            ` · pendentes: ${resumo.agendadas} ${resumo.agendadas === 1 ? 'agendada' : 'agendadas'} e ${resumo.naoAgendadas} não ${resumo.naoAgendadas === 1 ? 'agendada' : 'agendadas'}`}
        </p>
      </section>

      <AreaAcoesParticipante participacao={participante} perfil={perfil} aoAlterar={recarregar} />

      <section className="secao" aria-label="Visitas do participante">
        <h3>Visitas do participante</h3>
        {visitas.length === 0 ? (
          <p className="texto-suave">Este estudo ainda não tem visitas previstas.</p>
        ) : (
          <LinhaDoTempoVisitas visitas={visitas} estudoId={estudo.id} participacaoId={participante.participacaoId} />
        )}
      </section>
    </div>
  )
}
