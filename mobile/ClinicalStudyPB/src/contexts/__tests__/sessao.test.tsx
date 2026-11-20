import { act, renderHook, waitFor } from '@testing-library/react-native';
import type { ReactNode } from 'react';
import * as cliente from '@/api/cliente';
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
