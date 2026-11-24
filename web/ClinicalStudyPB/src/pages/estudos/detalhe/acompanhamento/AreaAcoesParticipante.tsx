import { useId, useState, type ReactNode } from 'react'
import { Link } from 'react-router'
import type {
  ContaVinculadaResponse,
  ParticipanteDoHistorico,
  RetiradaResponse,
  VisitaDoHistorico,
} from '../../../../api/tipos'
import type { PerfilUsuario } from '../../../../types/usuario'
import { useEstudoDetalhe } from '../contexto'
import './acompanhamento.css'
import { DialogoRetirada } from './DialogoRetirada'
import { DialogoVincularConta } from './DialogoVincularConta'

/*
 * Área de ações da ficha do participante (T97 / T104 / T111): ponto de encaixe das ações por perfil e situação.
 *
 * Contrato: recebe { participacao, perfil, aoAlterar, conta, visitas?, aoRetirar? } e renderiza a lista de botões de
 * ação que o perfil pode usar sobre esta participação. Cada ação é uma entrada de `acoesDoPerfil`, com uma chave estável:
 *   - T97: "Agendar visitas" (coordenador; some se a participação estiver retirada);
 *   - T111: "Vincular conta" (coordenador; só sem conta vinculada e com participação ativa);
 *   - T104: "Registrar retirada" (coordenador; desabilitado com explicação se o estudo estiver encerrado; some se a
 *     participação já estiver retirada).
 * Ações que gravam dados chamam `aoAlterar()` ao terminar para a ficha recarregar o histórico.
 * O pesquisador só lê: para ele a lista fica vazia e a área não é desenhada (sem botões de escrita).
 */
export type PropsAreaAcoesParticipante = {
  participacao: ParticipanteDoHistorico
  perfil: PerfilUsuario
  // Pedido de recarga da ficha depois de uma ação que altera dados (usado pela T104 e pela T111)
  aoAlterar: () => void
  /*
   * Conta vinculada ao participante, já consultada pela ficha (T111): `null` = sem conta; um objeto = com conta;
   * `undefined` = ainda não se sabe (carregando ou falha ao consultar). O botão "Vincular conta" só aparece com `null`
   * e participante ativo.
   */
  conta?: ContaVinculadaResponse | null
  // Visitas do participante para contar futuras e vencidas na confirmação da retirada (T104)
  visitas?: VisitaDoHistorico[]
  // Callback opcional com a resposta da retirada (permite exibir o total de visitas canceladas na ficha)
  aoRetirar?: (retirada: RetiradaResponse) => void
}

const EXPLICACAO_ESTUDO_ENCERRADO = 'O estudo está encerrado: não é possível registrar retirada de participantes.'

type AcaoParticipante = { chave: string; elemento: ReactNode }

function acoesDoPerfil(
  { participacao, perfil, conta }: Pick<PropsAreaAcoesParticipante, 'participacao' | 'perfil' | 'conta'>,
  estudoId: string,
  estudoEncerrado: boolean,
  idExplicacaoRetirada: string,
  abrirVinculo: () => void,
  abrirRetirada: () => void,
): AcaoParticipante[] {
  const acoes: AcaoParticipante[] = []
  if (perfil === 'COORDENADOR' && participacao.situacao !== 'RETIRADO') {
    acoes.push({
      chave: 'agendar-visitas',
      elemento: (
        <Link
          to={`/estudos/${estudoId}/participantes/${participacao.participacaoId}/agendamento`}
          className="botao botao--secundario"
        >
          Agendar visitas
        </Link>
      ),
    })
    if (conta === null) {
      acoes.push({
        chave: 'vincular-conta',
        elemento: (
          <button type="button" className="botao botao--secundario" onClick={abrirVinculo}>
            Vincular conta
          </button>
        ),
      })
    }
    acoes.push({
      chave: 'registrar-retirada',
      elemento: (
        <>
          <button
            type="button"
            className="botao botao--perigo"
            disabled={estudoEncerrado}
            title={estudoEncerrado ? EXPLICACAO_ESTUDO_ENCERRADO : undefined}
            aria-describedby={estudoEncerrado ? idExplicacaoRetirada : undefined}
            onClick={abrirRetirada}
          >
            Registrar retirada
          </button>
          {estudoEncerrado && (
            <span id={idExplicacaoRetirada} className="sr-only">
              {EXPLICACAO_ESTUDO_ENCERRADO}
            </span>
          )}
        </>
      ),
    })
  }
  return acoes
}

export function AreaAcoesParticipante({
  participacao,
  perfil,
  conta,
  aoAlterar,
  visitas = [],
  aoRetirar,
}: PropsAreaAcoesParticipante) {
  const { estudo } = useEstudoDetalhe()
  const idExplicacaoRetirada = useId()
  const [vinculando, setVinculando] = useState(false)
  const [retirando, setRetirando] = useState(false)
  const [confirmacao, setConfirmacao] = useState<string | null>(null)
  const estudoEncerrado = estudo.status === 'ENCERRADO'
  const acoes = acoesDoPerfil(
    { participacao, perfil, conta },
    estudo.id,
    estudoEncerrado,
    idExplicacaoRetirada,
    () => setVinculando(true),
    () => setRetirando(true),
  )
  if (acoes.length === 0) {
    return null
  }
  return (
    <section className="secao" aria-label="Ações do participante">
      <h3>Ações</h3>
      {confirmacao && (
        <p className="alerta alerta--info" role="status">
          {confirmacao}
        </p>
      )}
      <div className="acoes-participante">
        {acoes.map((acao) => (
          <span key={acao.chave}>{acao.elemento}</span>
        ))}
      </div>
      {estudoEncerrado && <p className="texto-suave">{EXPLICACAO_ESTUDO_ENCERRADO}</p>}
      {vinculando && (
        <DialogoVincularConta
          participacaoId={participacao.participacaoId}
          participanteNome={`${participacao.codigo} · ${participacao.nomeFicticio}`}
          aoFechar={() => setVinculando(false)}
          aoVincular={(vinculada) => {
            setConfirmacao(`Conta de ${vinculada.nome} vinculada com sucesso.`)
            aoAlterar()
          }}
        />
      )}
      {retirando && (
        <DialogoRetirada
          participante={participacao}
          visitas={visitas}
          aoFechar={() => setRetirando(false)}
          aoRetirar={(resposta) => {
            aoRetirar?.(resposta)
            aoAlterar()
          }}
        />
      )}
    </section>
  )
}
