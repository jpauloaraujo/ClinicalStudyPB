import { fireEvent, render, screen, waitFor } from '@testing-library/react-native';
import { ErroApi } from '@/api/erro';
import { estudosApi } from '@/api/estudos';
import InicioScreen from '@/app/(tabs)/index';
import PerfilScreen from '@/app/(tabs)/perfil';

const mockSair = jest.fn();
jest.mock('@/contexts/sessao', () => ({
  useSessao: () => ({
    usuario: { id: 'u1', nome: 'Pessoa Teta (Ficticia)', email: 'participante@clinicalstudy.test', perfil: 'PARTICIPANTE' },
    carregando: false,
    aviso: null,
    entrar: jest.fn(),
    sair: mockSair,
  }),
}));

describe('Início (T110, RQ-T110-06)', () => {
  afterEach(() => {
    jest.restoreAllMocks();
  });

  it('mostra o aviso quando o participante não está em nenhum estudo (lista vazia não é erro)', async () => {
    jest.spyOn(estudosApi, 'listar').mockResolvedValue([]);

    await render(<InicioScreen />);

    expect(await screen.findByText('Você ainda não está incluído em nenhum estudo.')).toBeTruthy();
    expect(screen.queryByText('Tentar de novo')).toBeNull();
  });

  it('não mostra o aviso quando há estudos', async () => {
    jest.spyOn(estudosApi, 'listar').mockResolvedValue([
      {
        participacaoId: 'p1',
        estudoId: 'e1',
        titulo: 'Estudo',
        resumo: 'Resumo',
        statusEstudo: 'EXECUCAO',
        dataInclusao: '2026-10-30',
        situacaoParticipacao: 'ATIVO',
      },
    ]);

    await render(<InicioScreen />);

    await waitFor(() => expect(screen.queryByLabelText('Carregando…')).toBeNull());
    expect(screen.queryByText('Você ainda não está incluído em nenhum estudo.')).toBeNull();
  });

  it('mostra o erro e tenta de novo', async () => {
    const listar = jest
      .spyOn(estudosApi, 'listar')
      .mockRejectedValueOnce(new ErroApi('Erro interno.', { status: 500 }))
      .mockResolvedValueOnce([]);

    await render(<InicioScreen />);
    expect(await screen.findByText('Erro interno.')).toBeTruthy();

    await fireEvent.press(screen.getByText('Tentar de novo'));

    expect(await screen.findByText('Você ainda não está incluído em nenhum estudo.')).toBeTruthy();
    expect(listar).toHaveBeenCalledTimes(2);
  });

  it('cumprimenta o participante pelo nome', async () => {
    jest.spyOn(estudosApi, 'listar').mockResolvedValue([]);

    await render(<InicioScreen />);

    expect(screen.getByText('Olá, Pessoa Teta (Ficticia)')).toBeTruthy();
    await screen.findByText('Você ainda não está incluído em nenhum estudo.');
  });
});

describe('Perfil (T110, RQ-T110-08)', () => {
  beforeEach(() => mockSair.mockReset());

  it('mostra nome, e-mail e o perfil Participante', async () => {
    await render(<PerfilScreen />);

    expect(screen.getByText('Pessoa Teta (Ficticia)')).toBeTruthy();
    expect(screen.getByText('participante@clinicalstudy.test')).toBeTruthy();
    expect(screen.getByText('Participante')).toBeTruthy();
  });

  it('Sair encerra a sessão', async () => {
    mockSair.mockResolvedValue(undefined);
    await render(<PerfilScreen />);

    await fireEvent.press(screen.getByText('Sair'));

    expect(mockSair).toHaveBeenCalledTimes(1);
  });

  it('Sair não deixa rejeição sem tratamento quando o armazenamento falha', async () => {
    mockSair.mockRejectedValue(new Error('SecureStore indisponível'));
    await render(<PerfilScreen />);

    await fireEvent.press(screen.getByText('Sair'));

    expect(mockSair).toHaveBeenCalledTimes(1);
  });
});
