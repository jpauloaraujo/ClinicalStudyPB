import { definirAoExpirarSessao, requisitar } from '@/api/cliente';
import { ErroApi, mensagemDoErro } from '@/api/erro';

// Sem SecureStore nos testes: o token fica numa variável
let mockToken: string | null = null;
jest.mock('@/api/armazenamento', () => ({
  armazenamento: {
    ler: jest.fn(async () => mockToken),
    salvar: jest.fn(async (_chave: string, valor: string) => {
      mockToken = valor;
    }),
    remover: jest.fn(async () => {
      mockToken = null;
    }),
  },
}));

// Espera que a chamada falhe e devolve o erro tipado
async function falhaDe(promessa: Promise<unknown>): Promise<ErroApi> {
  try {
    await promessa;
  } catch (erro) {
    return erro as ErroApi;
  }
  throw new Error('A chamada deveria ter falhado.');
}

function respostaJson(status: number, corpo: unknown): Response {
  return { ok: status >= 200 && status < 300, status, json: async () => corpo } as Response;
}

describe('cliente HTTP', () => {
  const fetchSimulado = jest.fn();

  beforeEach(() => {
    mockToken = 'token-de-teste';
    fetchSimulado.mockReset();
    globalThis.fetch = fetchSimulado as unknown as typeof fetch;
    delete process.env.EXPO_PUBLIC_USE_MOCKS;
  });

  afterEach(() => definirAoExpirarSessao(null));

  it('converte o corpo de erro da Sprint 2 em ErroApi', async () => {
    fetchSimulado.mockResolvedValue(
      respostaJson(400, {
        status: 400,
        codigoRegra: 'VALIDACAO_CAMPOS',
        mensagem: 'Dados inválidos.',
        errosCampo: [{ campo: 'motivo', erro: 'deve ter entre 5 e 500 caracteres' }],
        erro: 'Dados inválidos.',
        codigo: 'VALIDACAO_CAMPOS',
      }),
    );

    const falha = await falhaDe(requisitar('/api/qualquer'));

    expect(falha).toBeInstanceOf(ErroApi);
    expect(falha.status).toBe(400);
    expect(falha.codigo).toBe('VALIDACAO_CAMPOS');
    expect(falha.message).toBe('Dados inválidos.');
    expect(falha.errosCampo).toEqual([{ campo: 'motivo', erro: 'deve ter entre 5 e 500 caracteres' }]);
  });

  it('usa a mensagem padrão quando o corpo de erro não é JSON', async () => {
    fetchSimulado.mockResolvedValue({
      ok: false,
      status: 503,
      json: async () => {
        throw new Error('sem corpo');
      },
    });

    const falha = await falhaDe(requisitar('/api/qualquer'));

    expect(falha.message).toBe('Erro 503 na API.');
    expect(falha.status).toBe(503);
  });

  it('trata falha de rede com o código SEM_CONEXAO', async () => {
    fetchSimulado.mockRejectedValue(new TypeError('Network request failed'));

    const falha = await falhaDe(requisitar('/api/qualquer'));

    expect(falha).toBeInstanceOf(ErroApi);
    expect(falha.codigo).toBe('SEM_CONEXAO');
    expect(falha.message).toMatch(/^Não foi possível conectar à API em /);
  });

  it('envia o token e chama o callback de sessão expirada no 401', async () => {
    const aoExpirar = jest.fn();
    definirAoExpirarSessao(aoExpirar);
    fetchSimulado.mockResolvedValue(respostaJson(401, { mensagem: 'Sessão expirada.' }));

    const falha = await falhaDe(requisitar('/api/me/estudos'));

    expect(fetchSimulado.mock.calls[0][1].headers.Authorization).toBe('Bearer token-de-teste');
    expect(aoExpirar).toHaveBeenCalledTimes(1);
    expect(falha.status).toBe(401);
  });

  it('não chama o callback quando o 401 vem do login', async () => {
    const aoExpirar = jest.fn();
    definirAoExpirarSessao(aoExpirar);
    fetchSimulado.mockResolvedValue(respostaJson(401, { mensagem: 'E-mail ou senha inválidos.' }));

    const falha = await falhaDe(requisitar('/api/auth/login', { method: 'POST', body: '{}' }));

    expect(aoExpirar).not.toHaveBeenCalled();
    expect(falha.message).toBe('E-mail ou senha inválidos.');
  });

  it('devolve o corpo JSON de uma resposta 200', async () => {
    fetchSimulado.mockResolvedValue(respostaJson(200, [{ titulo: 'Estudo' }]));

    await expect(requisitar('/api/me/estudos')).resolves.toEqual([{ titulo: 'Estudo' }]);
  });

  it('devolve undefined em resposta 204 sem corpo', async () => {
    fetchSimulado.mockResolvedValue({ ok: true, status: 204, json: async () => Promise.reject(new Error('sem corpo')) });

    await expect(requisitar('/api/qualquer', { method: 'DELETE' })).resolves.toBeUndefined();
  });

  it('envia Content-Type JSON e aborta a chamada após 15 s (timeout)', async () => {
    jest.useFakeTimers();
    try {
      fetchSimulado.mockImplementation(
        (_url: string, opcoes: RequestInit) =>
          new Promise((_resolver, rejeitar) => {
            opcoes.signal?.addEventListener('abort', () => rejeitar(new Error('abortado')));
          }),
      );

      const pendente = falhaDe(requisitar('/api/me/estudos'));
      await jest.advanceTimersByTimeAsync(15000);
      const falha = await pendente;

      expect(fetchSimulado.mock.calls[0][1].headers['Content-Type']).toBe('application/json');
      expect(fetchSimulado.mock.calls[0][1].signal.aborted).toBe(true);
      expect(falha.codigo).toBe('SEM_CONEXAO');
    } finally {
      jest.useRealTimers();
    }
  });

  it('mensagemDoErro cobre ErroApi, erro comum e valor inesperado', () => {
    expect(mensagemDoErro(new ErroApi('Falhou.', { status: 500 }))).toBe('Falhou.');
    expect(mensagemDoErro(new Error('Outro erro.'))).toBe('Outro erro.');
    expect(mensagemDoErro('texto')).toBe('Erro inesperado. Tente novamente.');
  });
});

describe('modo de mocks', () => {
  beforeEach(() => {
    process.env.EXPO_PUBLIC_USE_MOCKS = 'true';
    delete process.env.EXPO_PUBLIC_MOCK_ERRO;
    mockToken = null;
  });

  afterEach(() => {
    delete process.env.EXPO_PUBLIC_USE_MOCKS;
    delete process.env.EXPO_PUBLIC_MOCK_ERRO;
    definirAoExpirarSessao(null);
  });

  it('responde login e consulta do participante sem chamar a rede', async () => {
    const fetchSimulado = jest.fn();
    globalThis.fetch = fetchSimulado as unknown as typeof fetch;

    const { token } = await requisitar<{ token: string }>('/api/auth/login', {
      method: 'POST',
      body: JSON.stringify({ email: 'participante@clinicalstudy.test', senha: 'senha-de-teste' }),
    });
    mockToken = token;
    const estudos = await requisitar<unknown[]>('/api/me/estudos');

    expect(estudos.length).toBeGreaterThan(0);
    expect(fetchSimulado).not.toHaveBeenCalled();
  });

  it('provoca erro 500 nas consultas do participante com EXPO_PUBLIC_MOCK_ERRO', async () => {
    process.env.EXPO_PUBLIC_MOCK_ERRO = 'true';
    mockToken = 'token-de-mock';

    const falha = await falhaDe(requisitar('/api/me/estudos'));

    expect(falha.status).toBe(500);
  });

  it('rejeita credenciais inválidas e token ausente', async () => {
    const aoExpirar = jest.fn();
    definirAoExpirarSessao(aoExpirar);

    const login = await falhaDe(requisitar('/api/auth/login', {
      method: 'POST',
      body: JSON.stringify({ email: 'x@x.com', senha: 'errada' }),
    }));
    const semToken = await falhaDe(requisitar('/api/me/estudos'));

    expect(login.status).toBe(401);
    expect(semToken.status).toBe(401);
    expect(aoExpirar).toHaveBeenCalledTimes(1);
  });
});
