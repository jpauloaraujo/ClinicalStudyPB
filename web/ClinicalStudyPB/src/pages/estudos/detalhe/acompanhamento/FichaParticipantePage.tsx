import { useCallback, useState } from 'react'
import { Link, useParams } from 'react-router'
import { contaParticipanteApi } from '../../../../api/contaParticipante'
import { historicoApi } from '../../../../api/historico'
import { rotulosSolicitanteRetirada, type RetiradaResponse } from '../../../../api/tipos'
import { useUsuarioLogado } from '../../../../contexts/sessao'
import { useCarregamento } from '../../../../hooks/useCarregamento'
import { formatarData } from '../../../../utils/datas'
import { useEstudoDetalhe } from '../contexto'
import '../recrutamento.css'
import './acompanhamento.css'
import { AreaAcoesParticipante } from './AreaAcoesParticipante'
import { LinhaDoTempoVisitas } from './LinhaDoTempoVisitas'

function mensagemConfirmacaoRetirada(retirada: RetiradaResponse): string {
  if (retirada.visitasCanceladas === 0) {
    return 'Retirada registrada com sucesso. Nenhuma visita futura agendada precisou ser cancelada.'
  }
  return `Retirada registrada com sucesso. ${retirada.visitasCanceladas} ${
    retirada.visitasCanceladas === 1 ? 'visita futura agendada foi cancelada' : 'visitas futuras agendadas foram canceladas'
  }.`
}

// Ficha do participante (RF15, T97; RF16, T104; T111): dados, marca RETIRADO e detalhes da retirada quando houver,
// conta vinculada, cartões de resumo do histórico, linha do tempo de todas as visitas previstas e a área de ações.
export function FichaParticipantePage() {
  const { estudo } = useEstudoDetalhe()
  const { perfil } = useUsuarioLogado()
  const { participacaoId = '' } = useParams()
  const [confirmacaoRetirada, setConfirmacaoRetirada] = useState<string | null>(null)
  const carregar = useCallback(() => historicoApi.buscar(participacaoId), [participacaoId])
  const { dados: historico, erro, statusErro, carregando, recarregar } = useCarregamento(carregar)
  // T111: a conta vinculada vem de outra consulta; 'sem conta' (404) chega como null e nunca derruba a ficha
  const carregarConta = useCallback(() => contaParticipanteApi.buscar(participacaoId), [participacaoId])
  const {
    dados: contaVinculada,
    erro: erroDaConta,
    carregando: carregandoConta,
    recarregar: recarregarConta,
  } = useCarregamento(carregarConta)
  // undefined = ainda não se sabe (carregando ou falha ao consultar): a área não oferece o vínculo nesse caso
  const conta = carregandoConta || erroDaConta ? undefined : (contaVinculada ?? null)
  const recarregarTudo = useCallback(() => {
    recarregar()
    recarregarConta()
  }, [recarregar, recarregarConta])

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
  const retirado = participante.situacao === 'RETIRADO'
  const cartoes = [
    { rotulo: 'Realizadas', valor: resumo.realizadas },
    { rotulo: 'Ausentes', valor: resumo.ausentes },
    { rotulo: 'Canceladas', valor: resumo.canceladas },
    { rotulo: 'Pendentes', valor: resumo.pendentes },
  ]

  return (
    <div className="pilha">
      <div className="barra-titulo">
        <div className="titulo-com-selo">
          <h2>
            {participante.codigo} · {participante.nomeFicticio}
          </h2>
          {retirado && <span className="selo selo--retirado">Retirado</span>}
        </div>
        <Link to={`/estudos/${estudo.id}/participantes`} className="botao botao--secundario botao--pequeno">
          ← Participantes
        </Link>
      </div>
      <p className="texto-suave">Incluído em {formatarData(participante.dataInclusao)}</p>
      {conta && (
        <p className="texto-suave">
          Conta vinculada: {conta.nome} ({conta.email})
        </p>
      )}
      {conta === null && <p className="texto-suave">Nenhuma conta vinculada ao participante.</p>}
      {erroDaConta && !carregandoConta && (
        <p className="alerta alerta--erro" role="alert">
          Não foi possível consultar a conta vinculada. {erroDaConta}
        </p>
      )}

      {confirmacaoRetirada && (
        <p className="alerta alerta--ok" role="status">
          {confirmacaoRetirada}
        </p>
      )}

      {retirado && participante.retirada && (
        <section className="secao bloco-retirada" aria-label="Dados da retirada">
          <h3>Participante retirado do estudo</h3>
          <dl className="detalhes">
            <div>
              <dt>Data da retirada</dt>
              <dd>{formatarData(participante.retirada.dataRetirada)}</dd>
            </div>
            <div>
              <dt>Solicitante</dt>
              <dd>{rotulosSolicitanteRetirada[participante.retirada.solicitante]}</dd>
            </div>
            <div>
              <dt>Motivo</dt>
              <dd>{participante.retirada.motivo}</dd>
            </div>
          </dl>
        </section>
      )}

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

      <AreaAcoesParticipante
        participacao={participante}
        perfil={perfil}
        conta={conta}
        visitas={visitas}
        aoAlterar={recarregarTudo}
        aoRetirar={(resposta) => setConfirmacaoRetirada(mensagemConfirmacaoRetirada(resposta))}
      />

      <section className="secao" aria-label="Visitas do participante">
        <h3>Visitas do participante</h3>
        {visitas.length === 0 ? (
          <p className="texto-suave">Este estudo ainda não tem visitas previstas.</p>
        ) : (
          <LinhaDoTempoVisitas
            visitas={visitas}
            estudoId={estudo.id}
            participacaoId={participante.participacaoId}
            participanteRetirado={retirado}
          />
        )}
      </section>
    </div>
  )
}
