import { armazenamento } from './armazenamento';
import { CODIGO_SEM_CONEXAO, ErroApi, erroDaResposta, type CorpoErroApi } from './erro';
import { responderMock, usarMocks } from './mocks';

export {
  CODIGO_PERFIL_NAO_PARTICIPANTE,
  ErroApi,
  mensagemDoErro,
  mensagemDoLogin,
  type ErroCampo,
} from './erro';

// URL da API (EXPO_PUBLIC_API_URL). No emulador Android, "localhost" é o próprio emulador: use 10.0.2.2.
export const URL_API = process.env.EXPO_PUBLIC_API_URL ?? 'http://localhost:8081';

const CHAVE_TOKEN = 'clinicalstudypb.token';
const TEMPO_LIMITE_MS = 15000;
const CAMINHO_LOGIN = '/api/auth/login';

export type PerfilUsuario = 'ADMINISTRADOR' | 'PESQUISADOR' | 'COORDENADOR' | 'PARTICIPANTE';

export type UsuarioSessao = {
  id: string;
  nome: string;
  email: string;
  perfil: PerfilUsuario;
};

export const tokenArmazenado = {
  ler: () => armazenamento.ler(CHAVE_TOKEN),
  salvar: (token: string) => armazenamento.salvar(CHAVE_TOKEN, token),
  remover: () => armazenamento.remover(CHAVE_TOKEN),
};

// Chamado quando a API responde 401 fora do login (token expirado ou inválido); o SessaoProvider o registra
let aoExpirarSessao: (() => void) | null = null;

export function definirAoExpirarSessao(callback: (() => void) | null) {
  aoExpirarSessao = callback;
}

// T119 (RQ-T119-06): 403 em /api/me/* é perfil que não é de participante; o SessaoProvider leva ao login com o aviso
let aoPerfilRecusado: (() => void) | null = null;

export function definirAoPerfilRecusado(callback: (() => void) | null) {
  aoPerfilRecusado = callback;
}

function ehConsultaDoParticipante(caminho: string): boolean {
  return caminho.startsWith('/api/me/');
}

// Requisição JSON com o token salvo; erros seguem o formato único da Sprint 2
export async function requisitar<T>(caminho: string, opcoes: RequestInit = {}): Promise<T> {
  const token = await tokenArmazenado.ler();
  const metodo = (opcoes.method ?? 'GET').toUpperCase();
  const ehLogin = caminho === CAMINHO_LOGIN;

  if (usarMocks()) {
    const corpo = typeof opcoes.body === 'string' ? (JSON.parse(opcoes.body) as unknown) : undefined;
    try {
      return await responderMock<T>(metodo, caminho, corpo, token);
    } catch (falha) {
      if (falha instanceof ErroApi && falha.status === 401 && !ehLogin) {
        aoExpirarSessao?.();
      }
      if (falha instanceof ErroApi && falha.status === 403 && ehConsultaDoParticipante(caminho)) {
        aoPerfilRecusado?.();
      }
      throw falha;
    }
  }

  const controle = new AbortController();
  const temporizador = setTimeout(() => controle.abort(), TEMPO_LIMITE_MS);
  let resposta: Response;
  try {
    resposta = await fetch(`${URL_API}${caminho}`, {
      ...opcoes,
      signal: controle.signal,
      headers: {
        'Content-Type': 'application/json',
        ...(token ? { Authorization: `Bearer ${token}` } : {}),
        ...opcoes.headers,
      },
    });
  } catch {
    throw new ErroApi(`Não foi possível conectar à API em ${URL_API}.`, { codigo: CODIGO_SEM_CONEXAO });
  } finally {
    clearTimeout(temporizador);
  }

  if (!resposta.ok) {
    if (resposta.status === 401 && !ehLogin) {
      aoExpirarSessao?.();
    }
    if (resposta.status === 403 && ehConsultaDoParticipante(caminho)) {
      aoPerfilRecusado?.();
    }
    const corpo = (await resposta.json().catch(() => null)) as CorpoErroApi | null;
    throw erroDaResposta(resposta.status, corpo);
  }
  if (resposta.status === 204) {
    return undefined as T;
  }
  return (await resposta.json()) as T;
}

export const autenticacaoApi = {
  entrar: (email: string, senha: string) =>
    requisitar<{ token: string }>(CAMINHO_LOGIN, {
      method: 'POST',
      body: JSON.stringify({ email, senha }),
    }),
  usuarioAtual: () => requisitar<UsuarioSessao>('/api/auth/me'),
};
