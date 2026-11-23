import { definirAoExpirarSessao, definirAoPerfilRecusado } from '@/api/cliente';
import { ErroApi } from '@/api/erro';
import { TOKEN_MOCK } from '@/api/mocks/dados';
import { visitasApi } from '@/api/visitas';

let mockToken: string | null = null;
jest.mock('@/api/armazenamento', () => ({
  armazenamento: {
    ler: jest.fn(async () => mockToken),
    salvar: jest.fn(async () => undefined),
    remover: jest.fn(async () => undefined),
  },
}));

function respostaJson(status: number, corpo: unknown): Response {
  return { ok: status >= 200 && status < 300, status, json: async () => corpo } as Response;
}

describe('visitasApi com a API real (T119, RQ-T119-01 e 02)', () => {
  const fetchSimulado = jest.fn();

  beforeEach(() => {
    mockToken = 'token-de-teste';
    fetchSimulado.mockReset();
    globalThis.fetch = fetchSimulado as unknown as typeof fetch;
    delete process.env.EXPO_PUBLIC_USE_MOCKS;
  });

  afterEach(() => {
    definirAoExpirarSessao(null);
    definirAoPerfilRecusado(null);
  });

  it('proximas chama /api/me/visitas/proximas com o token, sem id de participante', async () => {
    fetchSimulado.mockResolvedValue(respostaJson(200, []));

    await visitasApi.proximas();

    const [url, opcoes] = fetchSimulado.mock.calls[0];
    expect(String(url)).toMatch(/\/api\/me\/visitas\/proximas$/);
    expect(opcoes.headers.Authorization).toBe('Bearer token-de-teste');
  });

  it('proximas leva o estudoId como parâmetro de consulta', async () => {
    fetchSimulado.mockResolvedValue(respostaJson(200, []));

    await visitasApi.proximas('e-1');

    expect(String(fetchSimulado.mock.calls[0][0])).toMatch(/\/api\/me\/visitas\/proximas\?estudoId=e-1$/);
  });

  it('historico chama /api/me/visitas/historico com pagina e tamanho (e estudoId, quando há)', async () => {
    fetchSimulado.mockResolvedValue(respostaJson(200, { resumo: {}, conteudo: [] }));

    await visitasApi.historico({ estudoId: 'e-1', pagina: 2, tamanho: 10 });
    await visitasApi.historico();

    expect(String(fetchSimulado.mock.calls[0][0])).toMatch(
      /\/api\/me\/visitas\/historico\?estudoId=e-1&pagina=2&tamanho=10$/,
    );
    expect(String(fetchSimulado.mock.calls[1][0])).toMatch(/\/api\/me\/visitas\/historico\?pagina=0&tamanho=20$/);
  });

  it('falha de rede vira ErroApi SEM_CONEXAO, sem expor a URL da API na tela', async () => {
    fetchSimulado.mockRejectedValue(new TypeError('Network request failed'));

    await expect(visitasApi.proximas()).rejects.toMatchObject({ codigo: 'SEM_CONEXAO' });
  });

  it('401 aciona a expiração da sessão (volta ao login)', async () => {
    const aoExpirar = jest.fn();
    definirAoExpirarSessao(aoExpirar);
    fetchSimulado.mockResolvedValue(respostaJson(401, { mensagem: 'Não autenticado.' }));

    await expect(visitasApi.proximas()).rejects.toBeInstanceOf(ErroApi);

    expect(aoExpirar).toHaveBeenCalledTimes(1);
  });

  it('403 em /api/me aciona o aviso de app exclusivo para participantes', async () => {
    const aoRecusar = jest.fn();
    definirAoPerfilRecusado(aoRecusar);
    fetchSimulado.mockResolvedValue(respostaJson(403, { mensagem: 'Acesso negado.' }));

    await expect(visitasApi.historico()).rejects.toMatchObject({ status: 403 });

    expect(aoRecusar).toHaveBeenCalledTimes(1);
  });

  it('sem EXPO_PUBLIC_USE_MOCKS nenhuma chamada usa os mocks: sempre vai ao fetch', async () => {
    fetchSimulado.mockResolvedValue(respostaJson(200, []));

    await visitasApi.proximas();

    expect(fetchSimulado).toHaveBeenCalledTimes(1);
  });

  it('com EXPO_PUBLIC_USE_MOCKS=true responde com os mocks e não chama a rede', async () => {
    process.env.EXPO_PUBLIC_USE_MOCKS = 'true';
    mockToken = TOKEN_MOCK;

    const proximas = await visitasApi.proximas();
    const historico = await visitasApi.historico();

    expect(fetchSimulado).not.toHaveBeenCalled();
    expect(proximas.length).toBeGreaterThan(0);
    expect(historico.conteudo.length).toBeGreaterThan(0);
    expect(historico.resumo).toEqual(expect.objectContaining({ realizadas: expect.any(Number) }));
    delete process.env.EXPO_PUBLIC_USE_MOCKS;
  });
});
