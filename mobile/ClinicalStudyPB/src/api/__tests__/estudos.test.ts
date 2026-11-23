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

describe('estudosApi.buscar (T112, RQ-T112-05 e 07)', () => {
  beforeEach(() => {
    process.env.EXPO_PUBLIC_USE_MOCKS = 'true';
  });

  afterEach(() => {
    delete process.env.EXPO_PUBLIC_USE_MOCKS;
  });

  it('devolve o detalhe com objetivo completo, responsável e período', async () => {
    mockToken = CONTAS_MOCK[0].token;
    const [primeiro] = await estudosApi.listar();

    const detalhe = await estudosApi.buscar(primeiro.estudoId);

    expect(detalhe.estudoId).toBe(primeiro.estudoId);
    expect(detalhe.objetivo.length).toBeGreaterThan(160);
    expect(detalhe.responsavelNome).toBeTruthy();
    expect(detalhe).toHaveProperty('dataInicio');
    expect(detalhe).toHaveProperty('dataFim');
  });

  it('traz a data da retirada do estudo retirado e nunca o motivo', async () => {
    mockToken = CONTAS_MOCK[0].token;
    const lista = await estudosApi.listar();
    const retirado = lista.find((estudo) => estudo.situacaoParticipacao === 'RETIRADO')!;

    const detalhe = await estudosApi.buscar(retirado.estudoId);

    expect(detalhe.retirada?.dataRetirada).toBeTruthy();
    expect(JSON.stringify(detalhe)).not.toMatch(/motivo/i);
  });

  it('estudo desconhecido rejeita com ErroApi de status 404', async () => {
    mockToken = CONTAS_MOCK[0].token;

    await expect(estudosApi.buscar('d6300000-0000-4000-8000-00000000ffff')).rejects.toMatchObject({ status: 404 });
  });

  it('a conta sem vínculo recebe 404 mesmo para um id que existe (não revela o dado de outro participante)', async () => {
    mockToken = CONTAS_MOCK[0].token;
    const [primeiro] = await estudosApi.listar();
    mockToken = CONTAS_MOCK.find((conta) => conta.semEstudos)!.token;

    await expect(estudosApi.buscar(primeiro.estudoId)).rejects.toMatchObject({ status: 404 });
  });

  it('um perfil que não é participante recebe 403', async () => {
    mockToken = CONTAS_MOCK.find((conta) => conta.usuario.perfil === 'COORDENADOR')!.token;

    await expect(estudosApi.buscar('d6300000-0000-4000-8000-000000002421')).rejects.toMatchObject({ status: 403 });
  });
});
