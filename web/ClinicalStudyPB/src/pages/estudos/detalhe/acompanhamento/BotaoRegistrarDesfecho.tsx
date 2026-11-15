import { useId, useState } from 'react'
import type { VisitaAgendadaResponse, SituacaoVisita } from '../../../../api/tipos'
import { useUsuarioLogado } from '../../../../contexts/sessao'
import type { StatusEstudo } from '../../../../types/estudo'
import { DialogoDesfecho, type VisitaParaDesfecho } from './DialogoDesfecho'
import { rotulosSituacaoVisita } from './situacaoVisita'

type BotaoRegistrarDesfechoProps = {
  visita: VisitaParaDesfecho & { situacao: SituacaoVisita }
  statusEstudo: StatusEstudo
  // Chamado com a visita atualizada, para a tela de origem recarregar a visita e a lista
  aoRegistrar: (visita: VisitaAgendadaResponse) => void
}

// Ponto de encaixe do RF13 (T83) nas ações da visita: o `AcoesDaVisita` da T77 só renderiza este componente
// (na tela da visita e em cada linha da agenda), sem conhecer as regras do desfecho.
//  - só o coordenador vê o botão (o pesquisador não);
//  - só a visita AGENDADA tem o botão: visita com desfecho é somente leitura e não mostra nada aqui;
//  - com o estudo fora de recrutamento e execução o botão fica desabilitado, com a explicação.
export function BotaoRegistrarDesfecho({ visita, statusEstudo, aoRegistrar }: BotaoRegistrarDesfechoProps) {
  const usuario = useUsuarioLogado()
  const idExplicacao = useId()
  const [aberto, setAberto] = useState(false)
  const [confirmacao, setConfirmacao] = useState<string | null>(null)

  if (usuario.perfil !== 'COORDENADOR') return null

  const estudoPermite = statusEstudo === 'RECRUTAMENTO' || statusEstudo === 'EXECUCAO'
  const podeRegistrar = visita.situacao === 'AGENDADA'

  return (
    <>
      {podeRegistrar && (
        <>
          <button
            type="button"
            className="botao"
            disabled={!estudoPermite}
            aria-describedby={estudoPermite ? undefined : idExplicacao}
            onClick={() => {
              setConfirmacao(null)
              setAberto(true)
            }}
          >
            Registrar desfecho
          </button>
          {!estudoPermite && (
            <small id={idExplicacao} className="desfecho__aviso">
              O desfecho só pode ser registrado com o estudo em recrutamento ou execução.
            </small>
          )}
        </>
      )}

      {confirmacao && (
        <p className="alerta alerta--ok" role="status">
          {confirmacao}
        </p>
      )}

      {aberto && (
        <DialogoDesfecho
          visita={visita}
          aoFechar={() => setAberto(false)}
          aoRegistrar={(atualizada) => {
            setConfirmacao(`Desfecho registrado: visita ${rotulosSituacaoVisita[atualizada.situacao].toLowerCase()}.`)
            aoRegistrar(atualizada)
          }}
        />
      )}
    </>
  )
}
