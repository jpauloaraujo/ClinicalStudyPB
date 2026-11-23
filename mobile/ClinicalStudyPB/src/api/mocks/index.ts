import { ErroApi } from '../erro';
import { CONTAS_MOCK, estudosMock, historicoVisitasMock, proximasVisitasMock, SENHA_DEMO } from './dados';

// EXPO_PUBLIC_USE_MOCKS=true troca a API real por respostas em memória (só desenvolvimento e testes)
export function usarMocks(): boolean {
  return process.env.EXPO_PUBLIC_USE_MOCKS === 'true';
}

// EXPO_PUBLIC_MOCK_ERRO=true faz as consultas do participante (/api/me/*) responderem 500, para exercitar o estado de erro
function provocarErro(): boolean {
  return process.env.EXPO_PUBLIC_MOCK_ERRO === 'true';
}

function erro(status: number, codigo: string, mensagem: string): ErroApi {
  return new ErroApi(mensagem, { status, codigo });
}

const ATRASO_MS = 150;

// Responde como a API real responderia; os erros usam o formato único da Sprint 2
export async function responderMock<T>(
  metodo: string,
  caminho: string,
  corpo: unknown,
  token: string | null,
): Promise<T> {
  await new Promise((resolver) => setTimeout(resolver, ATRASO_MS));
  const rota = caminho.split('?')[0];

  if (metodo === 'POST' && rota === '/api/auth/login') {
    const { email, senha } = (corpo ?? {}) as { email?: string; senha?: string };
    const conta = CONTAS_MOCK.find((item) => item.email === email?.toLowerCase());
    if (!conta || senha !== SENHA_DEMO) {
      throw erro(401, 'CREDENCIAIS_INVALIDAS', 'E-mail ou senha inválidos.');
    }
    return { token: conta.token } as T;
  }

  const conta = CONTAS_MOCK.find((item) => item.token === token);
  if (!conta) {
    throw erro(401, 'NAO_AUTENTICADO', 'Sessão expirada. Entre novamente.');
  }

  if (rota === '/api/auth/me') {
    return conta.usuario as T;
  }

  if (rota.startsWith('/api/me/')) {
    if (provocarErro()) {
      throw erro(500, 'ERRO_INTERNO', 'Erro interno simulado pelo modo de mocks.');
    }
    if (conta.usuario.perfil !== 'PARTICIPANTE') {
      throw erro(403, 'ACESSO_NEGADO', 'Acesso negado.');
    }
    if (metodo === 'GET' && rota === '/api/me/estudos') {
      // Participante sem participação vinculada: lista vazia com 200, não é erro
      if (conta.semEstudos) {
        return [] as T;
      }
      return estudosMock.map(({ objetivo, dataInicio, dataFim, responsavelNome, retirada, ...resumo }) => resumo) as T;
    }
    const detalhe = rota.match(/^\/api\/me\/estudos\/([^/]+)$/);
    if (metodo === 'GET' && detalhe) {
      const estudo = estudosMock.find((item) => item.estudoId === detalhe[1]);
      if (!estudo) {
        throw erro(404, 'RECURSO_NAO_ENCONTRADO', 'Estudo não encontrado.');
      }
      return estudo as T;
    }
    if (metodo === 'GET' && rota === '/api/me/visitas/proximas') {
      return proximasVisitasMock as T;
    }
    if (metodo === 'GET' && rota === '/api/me/visitas/historico') {
      const total = historicoVisitasMock.length;
      return {
        resumo: { realizadas: total, ausentes: 0, canceladas: 0 },
        conteudo: historicoVisitasMock,
        pagina: 0,
        tamanho: 20,
        totalElementos: total,
        totalPaginas: 1,
      } as T;
    }
  }

  throw erro(404, 'RECURSO_NAO_ENCONTRADO', `Rota inexistente no modo de mocks: ${metodo} ${rota}`);
}
