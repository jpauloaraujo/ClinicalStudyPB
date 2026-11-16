import { useId, useState } from 'react'
import { agendamentoApi } from '../../../../api/agendamento'
import type { SituacaoVisita } from '../../../../api/tipos'
import { useUsuarioLogado } from '../../../../contexts/sessao'
import { dataHoraParaData, dataHoraParaHora } from '../../../../utils/datas'
import { useEstudoDetalhe } from '../contexto'
import { FormularioDataHora } from './FormularioDataHora'
import { interpretarErroDeAgendamento } from './mensagensAcompanhamento'
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
 * **Reagendar**. A T83 acrescenta **Registrar desfecho** aqui dentro, sem mudar quem usa este componente.
 */
export type VisitaDasAcoes = {
  visitaAgendadaId: string
  participacaoId: string
  situacao: SituacaoVisita
  // Data e hora atuais do agendamento (ISO com fuso, como a API devolve)
  dataHora: string
  // Nome da visita, usado só nos rótulos acessíveis dos botões
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
  if (!mostrarReagendar) return null

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
