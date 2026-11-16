import { useId, useState } from 'react'
import { agendamentoApi } from '../../../../api/agendamento'
import type { SituacaoVisita } from '../../../../api/tipos'
import { useUsuarioLogado } from '../../../../contexts/sessao'
import { dataHoraParaData, dataHoraParaHora } from '../../../../utils/datas'
import { useEstudoDetalhe } from '../contexto'
import { BotaoRegistrarDesfecho } from './BotaoRegistrarDesfecho'
import { FormularioDataHora } from './FormularioDataHora'
import { interpretarErroDeAgendamento } from './mensagensAcompanhamento'
import { rotulosSituacaoVisita } from './situacaoVisita'
import { EXPLICACAO_ESTUDO_FORA_DE_ESTADO, estudoPermiteAgendar, podeReagendar } from './rotulosVisita'
import './acompanhamento.css'

/**
 * PONTO DE ENCAIXE das ações de uma visita agendada (T77). É usado na tela da visita (VisitaAgendadaPage) e em cada
 * linha da agenda (AbaAgenda); por isso quem o usa só entrega os dados da visita e é avisado quando algo muda.
 *
 * Contrato: `{ visita, aoAlterar }`.
 *  - `visita`: o mínimo que as ações precisam (ver VisitaDasAcoes). A janela é opcional: sem ela o formulário não
 *    limita o campo de data e o servidor devolve a janela no erro.
 *  - `aoAlterar`: chamado depois de uma ação concluída, para a tela recarregar os dados.
 *
 * Cada botão decide sozinho se aparece, por perfil (useUsuarioLogado) e por situação da visita. Agora há
 * **Reagendar** (T77) e **Registrar desfecho** (T83, componente BotaoRegistrarDesfecho, que decide sozinho quando
 * aparece). Quem usa este componente não muda para ganhar uma ação nova.
 */
export type VisitaDasAcoes = {
  visitaAgendadaId: string
  participacaoId: string
  situacao: SituacaoVisita
  // Data e hora atuais do agendamento (ISO com fuso, como a API devolve)
  dataHora: string
  // Nomes usados nos rótulos dos botões e no diálogo do desfecho
  participanteNome: string
  visitaNome: string
  janelaInicio?: string
  janelaFim?: string
}

type AcoesDaVisitaProps = {
  visita: VisitaDasAcoes
  aoAlterar: () => void
}

export function AcoesDaVisita({ visita, aoAlterar }: AcoesDaVisitaProps) {
  const usuario = useUsuarioLogado()
  const { estudo } = useEstudoDetalhe()
  const idDescricao = useId()
  const [reagendando, setReagendando] = useState(false)
  const [enviando, setEnviando] = useState(false)
  const [erroGeral, setErroGeral] = useState<string | null>(null)
  const [erroData, setErroData] = useState<string | null>(null)
  // Confirmação do desfecho: fica aqui porque o botão desmonta quando a visita deixa de estar agendada
  const [confirmacaoDesfecho, setConfirmacaoDesfecho] = useState<string | null>(null)

  const janela =
    visita.janelaInicio && visita.janelaFim ? { inicio: visita.janelaInicio, fim: visita.janelaFim } : undefined
  const estudoAceita = estudoPermiteAgendar(estudo.status)
  const mostrarReagendar = usuario.perfil === 'COORDENADOR' && podeReagendar(visita.situacao)

  function fechar() {
    setReagendando(false)
    setErroGeral(null)
    setErroData(null)
  }

  async function reagendar(dataHora: string) {
    setEnviando(true)
    setErroGeral(null)
    setErroData(null)
    try {
      await agendamentoApi.reagendar(visita.visitaAgendadaId, { dataHora })
      fechar()
      aoAlterar()
    } catch (falha) {
      const { geral, data } = interpretarErroDeAgendamento(falha, janela)
      setErroGeral(geral)
      setErroData(data)
    } finally {
      setEnviando(false)
    }
  }

  // Sem nenhuma ação para este perfil e situação: não desenha nada (o pesquisador e as visitas com desfecho)
  if (!mostrarReagendar) {
    return confirmacaoDesfecho ? (
      <p className="alerta alerta--ok" role="status">
        {confirmacaoDesfecho}
      </p>
    ) : null
  }

  return (
    <div className="acoes-visita">
      <button
        type="button"
        className="botao botao--secundario botao--pequeno"
        aria-label={`Reagendar ${visita.visitaNome}`}
        aria-describedby={estudoAceita ? undefined : idDescricao}
        disabled={!estudoAceita}
        title={estudoAceita ? undefined : EXPLICACAO_ESTUDO_FORA_DE_ESTADO}
        onClick={() => setReagendando(true)}
      >
        Reagendar
      </button>
      {!estudoAceita && (
        <span id={idDescricao} className="sr-only">
          {EXPLICACAO_ESTUDO_FORA_DE_ESTADO}
        </span>
      )}

      {/* RF13 (T83): o botão só aparece ao coordenador e à visita AGENDADA; depois de registrar, a tela recarrega */}
      <BotaoRegistrarDesfecho
        visita={{
          id: visita.visitaAgendadaId,
          visitaNome: visita.visitaNome,
          participanteNome: visita.participanteNome,
          dataHora: visita.dataHora,
          situacao: visita.situacao,
        }}
        statusEstudo={estudo.status}
        aoRegistrar={(atualizada) => {
          setConfirmacaoDesfecho(`Desfecho registrado: visita ${rotulosSituacaoVisita[atualizada.situacao].toLowerCase()}.`)
          aoAlterar()
        }}
      />

      {reagendando && (
        <div
          className="dialogo-fundo"
          onKeyDown={(evento) => {
            if (evento.key === 'Escape' && !enviando) fechar()
          }}
        >
          <div className="dialogo" role="dialog" aria-modal="true" aria-label={`Reagendar ${visita.visitaNome}`}>
            <h2>Reagendar {visita.visitaNome}</h2>
            {janela && (
              <p className="texto-suave">
                A nova data deve ficar dentro da janela válida da visita (campo de data limitado a ela).
              </p>
            )}
            <FormularioDataHora
              titulo={`Reagendar ${visita.visitaNome}`}
              dataInicial={dataHoraParaData(visita.dataHora)}
              horaInicial={dataHoraParaHora(visita.dataHora)}
              janela={janela}
              rotuloSalvar="Salvar"
              enviando={enviando}
              erroGeral={erroGeral}
              erroData={erroData}
              onSalvar={reagendar}
              onCancelar={fechar}
            />
          </div>
        </div>
      )}
    </div>
  )
}
