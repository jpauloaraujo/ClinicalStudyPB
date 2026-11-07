// Mensagens claras para cada regra que pode bloquear a inclusão de um participante (códigos do backend, T60)
const MENSAGENS: Record<string, string> = {
  ESTUDO_FORA_DE_RECRUTAMENTO: 'O estudo não está em recrutamento: não é possível incluir participantes.',
  CANDIDATO_JA_INCLUIDO: 'Este candidato já foi incluído no estudo.',
  CANDIDATO_NAO_ELEGIVEL: 'O candidato não está elegível: registre uma avaliação aprovada antes de incluí-lo.',
  CONSENTIMENTO_AUSENTE: 'Falta o consentimento do candidato: registre o consentimento antes de incluí-lo.',
  VAGAS_ESGOTADAS: 'Vagas esgotadas: o estudo já atingiu o limite de participantes.',
}

/** Mensagem para o código da regra violada; sem código conhecido, cai na mensagem que o servidor enviou. */
export function mensagemDeInclusao(codigo: string | undefined, mensagemDoServidor: string): string {
  return (codigo && MENSAGENS[codigo]) || mensagemDoServidor
}
