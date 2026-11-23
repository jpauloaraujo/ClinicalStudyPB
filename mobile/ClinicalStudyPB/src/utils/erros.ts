import { CODIGO_SEM_CONEXAO, ErroApi, MSG_SEM_CONEXAO } from '@/api/erro';

export const MSG_FALHA_GENERICA = 'Não foi possível carregar agora. Tente de novo.';

/** Falta de conexão (ou tempo esgotado): o cliente a marca com o código SEM_CONEXAO (T108). */
export function ehSemConexao(erro: unknown): boolean {
  return erro instanceof ErroApi && erro.codigo === CODIGO_SEM_CONEXAO;
}

// Mensagem das telas de consulta (T119, RQ-T119-04 e 05): a do servidor quando existe, o texto de conexão quando a API
// não respondeu e um texto genérico no resto. Nunca JSON cru, nem a URL da API.
export function mensagemDaConsulta(erro: unknown): string {
  if (ehSemConexao(erro)) {
    return MSG_SEM_CONEXAO;
  }
  if (erro instanceof ErroApi && erro.status !== undefined) {
    // "Erro 500 na API." é o texto que o cliente monta quando o servidor não mandou mensagem
    const semMensagemDoServidor = /^Erro \d+ na API\.$/.test(erro.message);
    return erro.message && !semMensagemDoServidor && !erro.message.trim().startsWith('{') ? erro.message : MSG_FALHA_GENERICA;
  }
  return MSG_FALHA_GENERICA;
}
