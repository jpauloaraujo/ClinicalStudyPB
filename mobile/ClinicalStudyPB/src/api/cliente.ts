import { armazenamento } from './armazenamento';

// URL da API (EXPO_PUBLIC_API_URL). No emulador Android, "localhost" é o próprio emulador: use 10.0.2.2.
export const URL_API = process.env.EXPO_PUBLIC_API_URL ?? 'http://localhost:8081';

const CHAVE_TOKEN = 'clinicalstudypb.token';

export type PerfilUsuario = 'ADMINISTRADOR' | 'PESQUISADOR' | 'COORDENADOR' | 'PARTICIPANTE';

export type UsuarioSessao = {
  id: string;
  nome: string;
  email: string;
  perfil: PerfilUsuario;
};

export class ErroApi extends Error {
  status?: number;

  constructor(mensagem: string, status?: number) {
    super(mensagem);
    this.status = status;
  }
}

export const tokenArmazenado = {
  ler: () => armazenamento.ler(CHAVE_TOKEN),
  salvar: (token: string) => armazenamento.salvar(CHAVE_TOKEN, token),
  remover: () => armazenamento.remover(CHAVE_TOKEN),
};

// Requisição JSON com o token salvo; erros seguem o formato {"erro": "..."} da API
async function requisitar<T>(caminho: string, opcoes: RequestInit = {}): Promise<T> {
  const token = await tokenArmazenado.ler();
  let resposta: Response;
  try {
    resposta = await fetch(`${URL_API}${caminho}`, {
      ...opcoes,
      headers: {
        'Content-Type': 'application/json',
        ...(token ? { Authorization: `Bearer ${token}` } : {}),
        ...opcoes.headers,
      },
    });
  } catch {
    throw new ErroApi(`Não foi possível conectar à API em ${URL_API}.`);
  }

  if (!resposta.ok) {
    const corpo = (await resposta.json().catch(() => null)) as { erro?: string } | null;
    throw new ErroApi(corpo?.erro ?? `Erro ${resposta.status} na API.`, resposta.status);
  }
  return (await resposta.json()) as T;
}

export const autenticacaoApi = {
  entrar: (email: string, senha: string) =>
    requisitar<{ token: string }>('/api/auth/login', {
      method: 'POST',
      body: JSON.stringify({ email, senha }),
    }),
  usuarioAtual: () => requisitar<UsuarioSessao>('/api/auth/me'),
};
