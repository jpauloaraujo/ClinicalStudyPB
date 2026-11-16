import { converterErro } from '../../../../api/erros'
import { formatarJanela } from '../../../../utils/datas'

// Mensagens claras para as regras do agendamento (códigos do backend, T75/T76), no mesmo padrão de mensagensInclusao.ts.
// Os desfechos da visita (T83) acrescentam seus códigos aqui.
const MENSAGENS: Record<string, string> = {
  ESTUDO_FORA_DE_RECRUTAMENTO_OU_EXECUCAO:
    'O estudo não está em recrutamento nem em execução: não é possível agendar nem reagendar visitas.',
  VISITA_JA_AGENDADA: 'Esta visita já está agendada para o participante. Use Reagendar para mudar a data.',
  DATA_NO_PASSADO: 'A data da visita não pode ser anterior a hoje.',
  VISITA_NAO_ESTA_AGENDADA: 'Só é possível reagendar uma visita que ainda está agendada.',
  PARTICIPANTE_RETIRADO: 'O participante não está ativo no estudo: não é possível agendar visitas.',
}

export type JanelaDaVisita = { inicio: string; fim: string }

/**
 * Mensagem para o código da regra violada. Em DATA_FORA_DA_JANELA mostra a janela válida (quando a tela a conhece);
 * sem código conhecido, cai na mensagem que o servidor enviou.
 */
export function mensagemDeAgendamento(
  codigo: string | undefined,
  mensagemDoServidor: string,
  janela?: JanelaDaVisita,
): string {
  if (codigo === 'DATA_FORA_DA_JANELA' && janela) {
    return `A data está fora da janela válida desta visita (${formatarJanela(janela.inicio, janela.fim)}). Escolha uma data dentro dela.`
  }
  return (codigo && MENSAGENS[codigo]) || mensagemDoServidor
}

/**
 * Converte a falha da API em mensagens para o formulário de data e hora: o que pertence à data (fora da janela, no
 * passado, erro por campo) aparece junto do campo; o restante vai para o alerta do formulário.
 */
export function interpretarErroDeAgendamento(
  falha: unknown,
  janela?: JanelaDaVisita,
): { geral: string | null; data: string | null } {
  const convertido = converterErro(falha)
  const mensagem = mensagemDeAgendamento(convertido.codigo, convertido.mensagem, janela)
  if (convertido.codigo === 'DATA_FORA_DA_JANELA' || convertido.codigo === 'DATA_NO_PASSADO') {
    return { geral: null, data: mensagem }
  }
  const erroDoCampo = convertido.campos.dataHora
  if (erroDoCampo) return { geral: null, data: erroDoCampo }
  return { geral: mensagem, data: null }
}
