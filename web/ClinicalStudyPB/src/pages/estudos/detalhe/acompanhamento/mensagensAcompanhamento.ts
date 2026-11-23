import { converterErro } from '../../../../api/erros'
import { formatarJanela } from '../../../../utils/datas'

// Mensagens claras para cada regra que pode bloquear uma operação do acompanhamento (códigos do backend, seção 4
// das convenções da Sprint 3). Cada task acrescenta os códigos da sua fatia: o agendamento (T77) e o desfecho da
// visita (T83) estão aqui; os resultados (T91) e a retirada (T104) entram nas suas tasks.

// ---------------------------------------------------------------------------------------------------------------
// Agendamento e reagendamento (T75/T76, tela da T77)
// ---------------------------------------------------------------------------------------------------------------

const MENSAGENS_AGENDAMENTO: Record<string, string> = {
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
  return (codigo && MENSAGENS_AGENDAMENTO[codigo]) || mensagemDoServidor
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

// ---------------------------------------------------------------------------------------------------------------
// Desfecho da visita (T80/T81, telas da T83)
// ---------------------------------------------------------------------------------------------------------------

const MENSAGENS_DESFECHO: Record<string, string> = {
  TRANSICAO_VISITA_INVALIDA: 'Esta visita já tem desfecho registrado e não pode mais ser alterada.',
  DATA_REALIZACAO_FUTURA: 'A data e a hora em que a visita foi realizada não podem estar no futuro.',
  MOTIVO_OBRIGATORIO: 'Informe o motivo do cancelamento, com ao menos 5 caracteres.',
  VISITA_AINDA_NAO_OCORREU:
    'Esta visita está agendada para uma data futura: até lá ela só pode ser cancelada.',
  VISITA_ALTERADA_CONCORRENTEMENTE:
    'Esta visita foi alterada por outra pessoa enquanto você registrava. Recarregue a página e confira a situação atual.',
  ESTUDO_FORA_DE_RECRUTAMENTO_OU_EXECUCAO:
    'O estudo não está em recrutamento ou execução: não é possível registrar o desfecho da visita.',
}

/** Mensagem para o código da regra do desfecho; sem código conhecido, cai na mensagem que o servidor enviou. */
export function mensagemDeAcompanhamento(codigo: string | undefined, mensagemDoServidor: string): string {
  return (codigo && MENSAGENS_DESFECHO[codigo]) || mensagemDoServidor
}

// ---------------------------------------------------------------------------------------------------------------
// Resultados das avaliações (T89/T90, tela da T91)
// ---------------------------------------------------------------------------------------------------------------

const MENSAGENS_RESULTADOS: Record<string, string> = {
  VISITA_NAO_REALIZADA: 'Os resultados só podem ser registrados em visitas realizadas.',
  ATIVIDADE_FORA_DA_CATEGORIA_AVALIACAO: 'Esta atividade não é da categoria avaliação e não recebe resultado.',
  ATIVIDADE_NAO_PERTENCE_A_VISITA: 'Esta atividade não pertence a esta visita.',
  ESTUDO_ENCERRADO: 'O estudo foi encerrado: os resultados não podem mais ser registrados nem editados.',
  VALOR_OBRIGATORIO: 'Informe o valor do resultado.',
}

// Erros de regra que pertencem a um campo do formulário; os demais vão no alerta da atividade
const CAMPO_DO_CODIGO_RESULTADO: Record<string, string> = {
  VALOR_OBRIGATORIO: 'valor',
}

/** Mensagem para o código da regra dos resultados; sem código conhecido, cai na mensagem que o servidor enviou. */
export function mensagemDeResultado(codigo: string | undefined, mensagemDoServidor: string): string {
  return (codigo && MENSAGENS_RESULTADOS[codigo]) || mensagemDoServidor
}

/**
 * Converte a falha ao salvar um resultado em mensagens para o formulário da atividade: erros por campo (valor,
 * unidade, observação) ficam junto do campo; erros de regra, traduzidos pelo código, vão no alerta.
 */
export function interpretarErroDeResultado(falha: unknown): { geral: string | null; campos: Record<string, string> } {
  const convertido = converterErro(falha)
  const mensagem = mensagemDeResultado(convertido.codigo, convertido.mensagem)
  const campoDoCodigo = convertido.codigo ? CAMPO_DO_CODIGO_RESULTADO[convertido.codigo] : undefined
  if (campoDoCodigo) {
    return { geral: null, campos: { ...convertido.campos, [campoDoCodigo]: mensagem } }
  }
  if (Object.keys(convertido.campos).length > 0) {
    return { geral: null, campos: convertido.campos }
  }
  return { geral: mensagem, campos: {} }
}

// ---------------------------------------------------------------------------------------------------------------
// Vínculo da conta do participante (T111, diálogo DialogoVincularConta)
// ---------------------------------------------------------------------------------------------------------------

const MENSAGENS_VINCULO: Record<string, string> = {
  CONTA_JA_VINCULADA: 'Esta conta já está vinculada a outro participante. Escolha outra conta.',
  CONTA_INATIVA: 'Esta conta está desativada. Escolha outra conta ou peça ao administrador para reativá-la.',
  CONTA_NAO_E_PARTICIPANTE: 'Esta conta não é de um participante. Escolha uma conta de perfil participante.',
  PARTICIPACAO_JA_TEM_CONTA: 'Este participante já tem uma conta vinculada.',
  PARTICIPANTE_RETIRADO: 'O participante foi retirado do estudo e não pode receber uma conta.',
}

/** Mensagem para o código da regra do vínculo; sem código conhecido, cai na mensagem que o servidor enviou. */
export function mensagemDeVinculo(codigo: string | undefined, mensagemDoServidor: string): string {
  return (codigo && MENSAGENS_VINCULO[codigo]) || mensagemDoServidor
}
