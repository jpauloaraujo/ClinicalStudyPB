// Mensagens claras para cada regra que pode bloquear uma operação do acompanhamento (códigos do backend, seção 4
// das convenções da Sprint 3). Cada task acrescenta os códigos da sua fatia: este arquivo traz os do desfecho da
// visita (T83); o agendamento (T77), os resultados (T91) e a retirada (T104) entram nas suas tasks.
const MENSAGENS: Record<string, string> = {
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

/** Mensagem para o código da regra violada; sem código conhecido, cai na mensagem que o servidor enviou. */
export function mensagemDeAcompanhamento(codigo: string | undefined, mensagemDoServidor: string): string {
  return (codigo && MENSAGENS[codigo]) || mensagemDoServidor
}
