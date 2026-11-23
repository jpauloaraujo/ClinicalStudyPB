import { act, renderHook, waitFor } from '@testing-library/react-native';
import type { ReactNode } from 'react';
import * as cliente from '@/api/cliente';
import { CONTAS_MOCK } from '@/api/mocks/dados';
import { SessaoProvider, useSessao } from '@/contexts/sessao';

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

const EMAIL = 'participante@clinicalstudy.test';
const SENHA = 'senha-de-teste';

function Envoltorio({ children }: { children: ReactNode }) {
  return <SessaoProvider>{children}</SessaoProvider>;
}

// Captura a falha dentro do act: rejeitar fora dele deixa o ambiente do React instável nos testes seguintes
async function tentarEntrar(entrar: () => Promise<void>): Promise<Error | null> {
  let falha: Error | null = null;
  await act(async () => {
    try {
      await entrar();
    } catch (erro) {
      falha = erro as Error;
    }
  });
  return falha;
}

async function montar() {
  const resultado = await renderHook(() => useSessao(), { wrapper: Envoltorio });
  await waitFor(() => expect(resultado.result.current?.carregando).toBe(false));
  return resultado;
}

describe('SessaoProvider', () => {
  beforeEach(() => {
    mockToken = null;
    process.env.EXPO_PUBLIC_USE_MOCKS = 'true';
    delete process.env.EXPO_PUBLIC_MOCK_ERRO;
  });

  afterEach(() => {
    delete process.env.EXPO_PUBLIC_USE_MOCKS;
    jest.restoreAllMocks();
  });

  it('começa sem usuário quando não há token salvo', async () => {
    const { result } = await montar();

    expect(result.current.usuario).toBeNull();
  });

  it('restaura a sessão do participante a partir do token salvo', async () => {
    mockToken = 'token-de-mock';

    const { result } = await montar();

    expect(result.current.usuario?.perfil).toBe('PARTICIPANTE');
    expect(result.current.usuario?.email).toBe(EMAIL);
  });

  it('descarta o token de quem não é participante na restauração (RF03)', async () => {
    mockToken = 'token-de-mock';
    jest.spyOn(cliente.autenticacaoApi, 'usuarioAtual').mockResolvedValue({
      id: 'u1',
      nome: 'Pesquisador',
      email: 'pesquisador@clinicalstudy.test',
      perfil: 'PESQUISADOR',
    });

    const { result } = await montar();

    expect(result.current.usuario).toBeNull();
    expect(mockToken).toBeNull();
  });

  it('descarta o token rejeitado pela API (401) na restauração', async () => {
    mockToken = 'token-expirado';

    const { result } = await montar();

    expect(result.current.usuario).toBeNull();
    expect(mockToken).toBeNull();
  });

  it('entra com o participante de demonstração e guarda o token', async () => {
    const { result } = await montar();

    await act(async () => {
      await result.current.entrar(EMAIL, SENHA);
    });

    expect(result.current.usuario?.nome).toBe('Pessoa Teta (Ficticia)');
    expect(mockToken).toBe('token-de-mock');
  });

  it('recusa credenciais inválidas com a mensagem da API e não abre sessão', async () => {
    const { result } = await montar();

    const falha = await tentarEntrar(() => result.current.entrar(EMAIL, 'errada'));

    expect(falha?.message).toBe('E-mail ou senha inválidos.');

    expect(result.current.usuario).toBeNull();
    expect(mockToken).toBeNull();
  });

  it('recusa perfil diferente de participante e descarta o token (RF03)', async () => {
    jest.spyOn(cliente.autenticacaoApi, 'usuarioAtual').mockResolvedValue({
      id: 'u1',
      nome: 'Coordenador',
      email: 'coordenador@clinicalstudy.test',
      perfil: 'COORDENADOR',
    });
    const { result } = await montar();

    const falha = await tentarEntrar(() => result.current.entrar(EMAIL, SENHA));

    expect(falha?.message).toBe('Este aplicativo é exclusivo para participantes. Use a aplicação web.');

    expect(result.current.usuario).toBeNull();
    expect(mockToken).toBeNull();
  });

  it('um 401 fora do login limpa a sessão e o token (volta ao login)', async () => {
    const { result } = await montar();
    await act(async () => {
      await result.current.entrar(EMAIL, SENHA);
    });
    expect(result.current.usuario).not.toBeNull();

    mockToken = 'token-expirado';
    await act(async () => {
      await cliente.requisitar('/api/me/estudos').catch(() => undefined);
    });

    await waitFor(() => expect(result.current.usuario).toBeNull());
    expect(mockToken).toBeNull();
  });

  it('sair descarta o token e o usuário', async () => {
    const { result } = await montar();
    await act(async () => {
      await result.current.entrar(EMAIL, SENHA);
    });

    await act(async () => {
      await result.current.sair();
    });

    expect(result.current.usuario).toBeNull();
    expect(mockToken).toBeNull();
  });

  // ---- T110 ----

  it.each([
    ['ADMINISTRADOR', 'administrador@clinicalstudy.test'],
    ['PESQUISADOR', 'pesquisador@clinicalstudy.test'],
    ['COORDENADOR', 'coordenador@clinicalstudy.test'],
  ])('T110 · recusa o perfil %s com o código próprio e não deixa token (RQ-T110-05)', async (_perfil, email) => {
    const { result } = await montar();

    const falha = (await tentarEntrar(() => result.current.entrar(email, SENHA))) as cliente.ErroApi | null;

    expect(falha?.codigo).toBe(cliente.CODIGO_PERFIL_NAO_PARTICIPANTE);
    expect(falha?.message).toBe('Este aplicativo é exclusivo para participantes. Use a aplicação web.');
    expect(result.current.usuario).toBeNull();
    expect(mockToken).toBeNull();
  });

  it.each([403, 404])('T110 · restauração com %s da API descarta o token (RQ-T110-07)', async (status) => {
    mockToken = 'token-rejeitado';
    jest.spyOn(cliente.autenticacaoApi, 'usuarioAtual').mockRejectedValue(new cliente.ErroApi('rejeitado', { status }));

    const { result } = await montar();

    expect(result.current.usuario).toBeNull();
    expect(mockToken).toBeNull();
  });

  it('T110 · restauração com a API fora do ar mantém o token (RQ-T110-07)', async () => {
    mockToken = 'token-valido';
    jest
      .spyOn(cliente.autenticacaoApi, 'usuarioAtual')
      .mockRejectedValue(new cliente.ErroApi('sem conexão', { codigo: 'SEM_CONEXAO' }));

    const { result } = await montar();

    expect(result.current.usuario).toBeNull();
    expect(mockToken).toBe('token-valido');
  });

  it('T110 · 401 com sessão aberta define o aviso de sessão expirada (RQ-T110-09)', async () => {
    const { result } = await montar();
    await act(async () => {
      await result.current.entrar(EMAIL, SENHA);
    });
    expect(result.current.aviso).toBeNull();

    mockToken = 'token-expirado';
    await act(async () => {
      await cliente.requisitar('/api/me/estudos').catch(() => undefined);
    });

    await waitFor(() => expect(result.current.usuario).toBeNull());
    expect(result.current.aviso).toBe('Sua sessão expirou. Entre novamente.');
  });

  it('T119 · 403 em /api/me/* com sessão aberta volta ao login com o aviso do app exclusivo (RQ-T119-06)', async () => {
    const { result } = await montar();
    await act(async () => {
      await result.current.entrar(EMAIL, SENHA);
    });
    expect(result.current.usuario).not.toBeNull();

    // O servidor passa a ver a conta como de outro perfil: /api/me/* responde 403
    const conta = CONTAS_MOCK.find((item) => item.email === EMAIL)!;
    const perfilOriginal = conta.usuario.perfil;
    conta.usuario.perfil = 'COORDENADOR';
    try {
      await act(async () => {
        await cliente.requisitar('/api/me/estudos').catch(() => undefined);
      });
    } finally {
      conta.usuario.perfil = perfilOriginal;
    }

    await waitFor(() => expect(result.current.usuario).toBeNull());
    expect(result.current.aviso).toBe('Este aplicativo é exclusivo para participantes. Use a aplicação web.');
    expect(mockToken).toBeNull();
  });

  it('T110 · 401 na restauração (sem sessão aberta) não mostra aviso', async () => {
    mockToken = 'token-expirado';

    const { result } = await montar();

    expect(result.current.usuario).toBeNull();
    expect(result.current.aviso).toBeNull();
  });

  it('T110 · entrar limpa o aviso anterior', async () => {
    const { result } = await montar();
    await act(async () => {
      await result.current.entrar(EMAIL, SENHA);
    });
    mockToken = 'token-expirado';
    await act(async () => {
      await cliente.requisitar('/api/me/estudos').catch(() => undefined);
    });
    await waitFor(() => expect(result.current.aviso).not.toBeNull());

    await act(async () => {
      await result.current.entrar(EMAIL, SENHA);
    });

    expect(result.current.aviso).toBeNull();
    expect(result.current.usuario).not.toBeNull();
  });

  it('T110 · senha errada no login não vira "sessão expirada" (401 do próprio login)', async () => {
    const { result } = await montar();

    const falha = (await tentarEntrar(() => result.current.entrar(EMAIL, 'errada'))) as cliente.ErroApi | null;

    expect(falha?.status).toBe(401);
    expect(result.current.aviso).toBeNull();
  });

  it('T110 · 401 de /api/auth/me durante o login é falha de credencial, não sessão expirada', async () => {
    const { result } = await montar();
    jest
      .spyOn(cliente.autenticacaoApi, 'usuarioAtual')
      .mockImplementation(() => cliente.requisitar('/api/auth/me').then(() => ({}) as never));
    mockToken = null;
    jest.spyOn(cliente.autenticacaoApi, 'entrar').mockImplementation(async () => ({ token: 'token-invalido' }));

    const falha = (await tentarEntrar(() => result.current.entrar(EMAIL, SENHA))) as cliente.ErroApi | null;

    expect(falha?.status).toBe(401);
    expect(result.current.aviso).toBeNull();
    expect(mockToken).toBeNull();
  });

  it('T110 · sair limpa a sessão mesmo se o armazenamento falhar (RQ-T110-08)', async () => {
    const { result } = await montar();
    await act(async () => {
      await result.current.entrar(EMAIL, SENHA);
    });
    jest.spyOn(cliente.tokenArmazenado, 'remover').mockRejectedValue(new Error('SecureStore indisponível'));

    await act(async () => {
      await result.current.sair().catch(() => undefined);
    });

    expect(result.current.usuario).toBeNull();
  });

  it('useSessao exige o SessaoProvider', async () => {
    const erroConsole = jest.spyOn(console, 'error').mockImplementation(() => undefined);

    await expect(renderHook(() => useSessao())).rejects.toThrow('useSessao deve ser usado dentro de <SessaoProvider>');

    erroConsole.mockRestore();
  });

  it('remove o callback de sessão expirada ao desmontar o provider', async () => {
    const espiao = jest.spyOn(cliente, 'definirAoExpirarSessao');
    const { unmount } = await montar();

    await unmount();

    expect(espiao).toHaveBeenLastCalledWith(null);
  });
});
