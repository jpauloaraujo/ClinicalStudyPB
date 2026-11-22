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
