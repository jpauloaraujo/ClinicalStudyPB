// Formato único de erro da Sprint 2: { status, codigoRegra, mensagem, errosCampo[{campo, erro}], erro, codigo }
export type ErroCampo = { campo: string; erro: string };

export type CorpoErroApi = {
  status?: number;
  codigoRegra?: string;
  codigo?: string;
  mensagem?: string;
  erro?: string;
  errosCampo?: { campo?: string; erro?: string }[];
};

export const CODIGO_SEM_CONEXAO = 'SEM_CONEXAO';

type OpcoesErroApi = { status?: number; codigo?: string; errosCampo?: ErroCampo[] };

export class ErroApi extends Error {
  status?: number;
  codigo?: string;
  errosCampo: ErroCampo[];

  constructor(mensagem: string, opcoes: OpcoesErroApi = {}) {
    super(mensagem);
    this.name = 'ErroApi';
    this.status = opcoes.status;
    this.codigo = opcoes.codigo;
    this.errosCampo = opcoes.errosCampo ?? [];
  }
}

// Converte o corpo de erro da API (ou a ausência dele) em ErroApi
export function erroDaResposta(status: number, corpo: CorpoErroApi | null): ErroApi {
  const errosCampo = (corpo?.errosCampo ?? [])
    .filter((item) => item.campo && item.erro)
    .map((item) => ({ campo: item.campo as string, erro: item.erro as string }));
  return new ErroApi(corpo?.mensagem ?? corpo?.erro ?? `Erro ${status} na API.`, {
    status,
    codigo: corpo?.codigoRegra ?? corpo?.codigo,
    errosCampo,
  });
}

// T110: recusa do perfil que não é participante (regra 8); o login a distingue pelo código, nunca pelo texto
export const CODIGO_PERFIL_NAO_PARTICIPANTE = 'PERFIL_NAO_PARTICIPANTE';

export const MSG_PERFIL_NAO_PARTICIPANTE = 'Este aplicativo é exclusivo para participantes. Use a aplicação web.';
export const MSG_CREDENCIAL_INVALIDA = 'E-mail ou senha inválidos, ou conta desativada.';
export const MSG_SEM_CONEXAO = 'Não foi possível conectar. Verifique sua conexão e tente de novo.';
export const MSG_LOGIN_GENERICA = 'Não foi possível entrar agora. Tente novamente.';

// T110 (RQ-T110-03 e 05): mensagem do login. A API responde o mesmo 401 para e-mail inexistente, senha errada
// e conta desativada, então os três casos têm um texto só; nunca devolve JSON cru nem a URL da API.
export function mensagemDoLogin(erro: unknown): string {
  if (erro instanceof ErroApi) {
    if (erro.codigo === CODIGO_PERFIL_NAO_PARTICIPANTE) {
      return MSG_PERFIL_NAO_PARTICIPANTE;
    }
    if (erro.status === 401) {
      return MSG_CREDENCIAL_INVALIDA;
    }
    if (erro.codigo === CODIGO_SEM_CONEXAO) {
      return MSG_SEM_CONEXAO;
    }
  }
  return MSG_LOGIN_GENERICA;
}

// Mensagem para exibir nas telas: ErroApi, falha de rede ou erro inesperado
export function mensagemDoErro(erro: unknown): string {
  if (erro instanceof ErroApi) {
    return erro.message;
  }
  if (erro instanceof Error && erro.message) {
    return erro.message;
  }
  return 'Erro inesperado. Tente novamente.';
}
