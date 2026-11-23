import { useState, type ReactNode } from 'react'
import { Link } from 'react-router'
import type { ContaVinculadaResponse, ParticipanteDoHistorico } from '../../../../api/tipos'
import type { PerfilUsuario } from '../../../../types/usuario'
import { useEstudoDetalhe } from '../contexto'
import './acompanhamento.css'
import { DialogoVincularConta } from './DialogoVincularConta'

/*
 * Área de ações da ficha do participante (T97): ponto de encaixe das ações por perfil e situação.
 *
 * Contrato: recebe { participacao, perfil, aoAlterar, conta } e renderiza a lista de botões de ação que o perfil pode
 * usar sobre esta participação. Cada ação é uma entrada de `acoesDoPerfil`, com uma chave estável. Quem entrega uma ação
 * nova só acrescenta uma entrada a essa lista (e, se for um diálogo, o próprio componente do diálogo), sem reescrever a
 * ficha:
 *   - T104 acrescenta "Registrar retirada" (coordenador; some se a participação já estiver retirada);
 *   - T111 acrescentou "Vincular conta" (coordenador; só sem conta vinculada).
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
   * `undefined` = ainda não se sabe (carregando ou falha ao consultar). O botão "Vincular conta" só aparece com `null`,
   * para nunca oferecer o vínculo a quem pode já ter conta. A situação "ativo" é presumida até a retirada (T104):
   * o servidor recusa o vínculo de participante retirado com 409 de qualquer forma.
   */
  conta?: ContaVinculadaResponse | null
}

type AcaoParticipante = { chave: string; elemento: ReactNode }

function acoesDoPerfil(
  { participacao, perfil, conta }: Pick<PropsAreaAcoesParticipante, 'participacao' | 'perfil' | 'conta'>,
  estudoId: string,
  abrirVinculo: () => void,
): AcaoParticipante[] {
  const acoes: AcaoParticipante[] = []
  if (perfil === 'COORDENADOR') {
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
    // TODO(T104): esconder também quando a participação estiver retirada (hoje toda participação é ativa)
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
  }
  return acoes
}

export function AreaAcoesParticipante({ participacao, perfil, conta, aoAlterar }: PropsAreaAcoesParticipante) {
  const { estudo } = useEstudoDetalhe()
  const [vinculando, setVinculando] = useState(false)
  const [confirmacao, setConfirmacao] = useState<string | null>(null)
  const acoes = acoesDoPerfil({ participacao, perfil, conta }, estudo.id, () => setVinculando(true))
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
    </section>
  )
}
