import { estudosApi } from '@/api/estudos';
import { tokenArmazenado } from '@/api/cliente';
import { CONTAS_MOCK } from '@/api/mocks/dados';

// Sem SecureStore nos testes: o token fica numa variável (o prefixo "mock" é exigido pelo Jest)
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

describe('estudosApi.listar (T110, RQ-T110-06)', () => {
  beforeEach(() => {
    process.env.EXPO_PUBLIC_USE_MOCKS = 'true';
  });

  afterEach(() => {
    delete process.env.EXPO_PUBLIC_USE_MOCKS;
  });

  it('lista os estudos do participante com vínculo', async () => {
    mockToken = CONTAS_MOCK[0].token;

    const lista = await estudosApi.listar();

    expect(lista.length).toBeGreaterThan(0);
    expect(lista[0]).toHaveProperty('estudoId');
  });

  it('devolve lista vazia, e não erro, para o participante sem vínculo', async () => {
    mockToken = CONTAS_MOCK.find((conta) => conta.semEstudos)!.token;

    await expect(estudosApi.listar()).resolves.toEqual([]);
  });

  it('um perfil que não é participante recebe 403 em /api/me/estudos', async () => {
    mockToken = CONTAS_MOCK.find((conta) => conta.usuario.perfil === 'COORDENADOR')!.token;

    await expect(estudosApi.listar()).rejects.toMatchObject({ status: 403 });
  });

  it('sem token válido responde 401', async () => {
    await tokenArmazenado.remover();

    await expect(estudosApi.listar()).rejects.toMatchObject({ status: 401 });
  });
});
