import { responderMock, usarMocks } from '@/api/mocks';
import { estudosMock, TOKEN_MOCK } from '@/api/mocks/dados';
import type { ErroApi } from '@/api/erro';

async function falhaDe(promessa: Promise<unknown>): Promise<ErroApi> {
  try {
    await promessa;
  } catch (erro) {
    return erro as ErroApi;
  }
  throw new Error('A chamada deveria ter falhado.');
}

describe('modo de mocks (respostas no formato do contrato)', () => {
  beforeEach(() => {
    delete process.env.EXPO_PUBLIC_MOCK_ERRO;
  });

  afterEach(() => {
    delete process.env.EXPO_PUBLIC_USE_MOCKS;
    delete process.env.EXPO_PUBLIC_MOCK_ERRO;
  });

  it('só liga com EXPO_PUBLIC_USE_MOCKS=true', () => {
    expect(usarMocks()).toBe(false);
    process.env.EXPO_PUBLIC_USE_MOCKS = 'false';
    expect(usarMocks()).toBe(false);
    process.env.EXPO_PUBLIC_USE_MOCKS = 'true';
    expect(usarMocks()).toBe(true);
  });

  it('lista os estudos sem os campos exclusivos do detalhe', async () => {
    const lista = await responderMock<Record<string, unknown>[]>('GET', '/api/me/estudos', undefined, TOKEN_MOCK);

    expect(lista).toHaveLength(estudosMock.length);
    expect(Object.keys(lista[0]).sort()).toEqual(
      ['dataInclusao', 'estudoId', 'participacaoId', 'resumo', 'situacaoParticipacao', 'statusEstudo', 'titulo'].sort(),
    );
    expect(String(lista[0].resumo).length).toBeLessThanOrEqual(160);
  });

  it('devolve o detalhe do estudo e a retirada sem expor o motivo', async () => {
    const retirado = estudosMock.find((e) => e.situacaoParticipacao === 'RETIRADO')!;

    const detalhe = await responderMock<typeof retirado>('GET', `/api/me/estudos/${retirado.estudoId}`, undefined, TOKEN_MOCK);

    expect(detalhe.retirada).toEqual({ dataRetirada: expect.any(String) });
    expect(detalhe.retirada).not.toHaveProperty('motivo');
  });

  it('responde 404 para estudo que não é do participante', async () => {
    const falha = await falhaDe(responderMock('GET', '/api/me/estudos/inexistente', undefined, TOKEN_MOCK));

    expect(falha.status).toBe(404);
    expect(falha.codigo).toBe('RECURSO_NAO_ENCONTRADO');
  });

  it('devolve próximas visitas e histórico paginado com resumo', async () => {
    const proximas = await responderMock<unknown[]>('GET', '/api/me/visitas/proximas?estudoId=x', undefined, TOKEN_MOCK);
    const historico = await responderMock<{ resumo: Record<string, number>; conteudo: unknown[]; totalPaginas: number }>(
      'GET',
      '/api/me/visitas/historico',
      undefined,
      TOKEN_MOCK,
    );

    expect(proximas.length).toBeGreaterThan(0);
    expect(historico.resumo).toEqual({ realizadas: historico.conteudo.length, ausentes: 0, canceladas: 0 });
    expect(historico.totalPaginas).toBe(1);
  });

  it('nunca expõe resultados nas visitas do participante (RF18)', async () => {
    const proximas = await responderMock<Record<string, unknown>[]>('GET', '/api/me/visitas/proximas', undefined, TOKEN_MOCK);

    expect(Object.keys(proximas[0])).not.toContain('resultados');
  });

  it('responde 500 em /api/me/* com EXPO_PUBLIC_MOCK_ERRO, mas não no /api/auth/me', async () => {
    process.env.EXPO_PUBLIC_MOCK_ERRO = 'true';

    const falha = await falhaDe(responderMock('GET', '/api/me/visitas/proximas', undefined, TOKEN_MOCK));
    const usuario = await responderMock<{ perfil: string }>('GET', '/api/auth/me', undefined, TOKEN_MOCK);

    expect(falha.status).toBe(500);
    expect(usuario.perfil).toBe('PARTICIPANTE');
  });

  it('responde 404 para rota fora do mock', async () => {
    const falha = await falhaDe(responderMock('GET', '/api/nao-existe', undefined, TOKEN_MOCK));

    expect(falha.status).toBe(404);
  });
});
