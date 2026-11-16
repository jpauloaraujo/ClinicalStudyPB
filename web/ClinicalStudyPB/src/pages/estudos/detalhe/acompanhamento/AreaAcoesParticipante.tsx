import type { ReactNode } from 'react'
import { Link } from 'react-router'
import type { ParticipanteDoHistorico } from '../../../../api/tipos'
import type { PerfilUsuario } from '../../../../types/usuario'
import { useEstudoDetalhe } from '../contexto'
import './acompanhamento.css'

/*
 * Área de ações da ficha do participante (T97): ponto de encaixe das ações por perfil e situação.
 *
 * Contrato: recebe { participacao, perfil, aoAlterar } e renderiza a lista de botões de ação que o perfil pode usar
 * sobre esta participação. Cada ação é uma entrada de `acoesDoPerfil`, com uma chave estável. Quem entrega uma ação nova
 * só acrescenta uma entrada a essa lista (e, se for um diálogo, o próprio componente do diálogo), sem reescrever a ficha:
 *   - T104 acrescenta "Registrar retirada" (coordenador; some se a participação já estiver retirada);
 *   - T111 acrescenta "Vincular conta" (coordenador).
 * Ações que gravam dados chamam `aoAlterar()` ao terminar para a ficha recarregar o histórico.
 * O pesquisador só lê: para ele a lista fica vazia e a área não é desenhada (sem botões de escrita).
 */
export type PropsAreaAcoesParticipante = {
  participacao: ParticipanteDoHistorico
  perfil: PerfilUsuario
  // Pedido de recarga da ficha depois de uma ação que altera dados (usado pela T104 e pela T111)
  aoAlterar: () => void
}

type AcaoParticipante = { chave: string; elemento: ReactNode }

function acoesDoPerfil(
  { participacao, perfil }: Pick<PropsAreaAcoesParticipante, 'participacao' | 'perfil'>,
  estudoId: string,
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
  }
  return acoes
}

export function AreaAcoesParticipante({ participacao, perfil }: PropsAreaAcoesParticipante) {
  const { estudo } = useEstudoDetalhe()
  const acoes = acoesDoPerfil({ participacao, perfil }, estudo.id)
  if (acoes.length === 0) {
    return null
  }
  return (
    <section className="secao" aria-label="Ações do participante">
      <h3>Ações</h3>
      <div className="acoes-participante">
        {acoes.map((acao) => (
          <span key={acao.chave}>{acao.elemento}</span>
        ))}
      </div>
    </section>
  )
}
