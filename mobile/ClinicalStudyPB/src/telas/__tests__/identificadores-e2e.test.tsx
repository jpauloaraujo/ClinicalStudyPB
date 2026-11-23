import { render, screen } from '@testing-library/react-native';
import { estudosApi, type MeuEstudo } from '@/api/estudos';
import EstudosScreen from '@/app/(tabs)/estudos';
import PerfilScreen from '@/app/(tabs)/perfil';
import LoginScreen from '@/app/login';

// T114 (RQ-T114-02): os fluxos Maestro achar os elementos por identificador, não por coordenada. Estes testes só
// garantem que os identificadores continuam nas telas; o comportamento delas é testado nos outros arquivos.
jest.mock('expo-router', () => ({
  useRouter: () => ({ push: jest.fn(), back: jest.fn(), replace: jest.fn(), canGoBack: () => true }),
}));
jest.mock('@/contexts/sessao', () => ({
  useSessao: () => ({
    usuario: { id: 'u1', nome: 'Pessoa Teta (Ficticia)', email: 'participante@clinicalstudy.test', perfil: 'PARTICIPANTE' },
    carregando: false,
    aviso: null,
    entrar: jest.fn(),
    sair: jest.fn(),
  }),
}));

// O primeiro render carrega react-native e o roteador; com as suítes em paralelo passa dos 5 s padrão (F-T110-03)
jest.setTimeout(30000);

const estudo: MeuEstudo = {
  participacaoId: 'p-1',
  estudoId: 'e-1',
  titulo: 'Estudo de demonstração',
  resumo: 'Resumo fictício.',
  statusEstudo: 'EXECUCAO',
  dataInclusao: '2026-10-30',
  situacaoParticipacao: 'ATIVO',
};

describe('Identificadores dos fluxos E2E (T114, RQ-T114-02)', () => {
  afterEach(() => {
    jest.restoreAllMocks();
  });

  it('o login expõe os campos e o botão Entrar', async () => {
    await render(<LoginScreen />);

    expect(screen.getByTestId('campo-email')).toBeTruthy();
    expect(screen.getByTestId('campo-senha')).toBeTruthy();
    expect(screen.getByTestId('botao-entrar')).toBeTruthy();
  });

  it('a lista expõe um cartão por estudo', async () => {
    jest.spyOn(estudosApi, 'listar').mockResolvedValue([estudo, { ...estudo, participacaoId: 'p-2', estudoId: 'e-2' }]);

    await render(<EstudosScreen />);

    expect(await screen.findAllByTestId('cartao-estudo')).toHaveLength(2);
  });

  it('o Perfil expõe o botão Sair', async () => {
    await render(<PerfilScreen />);

    expect(screen.getByTestId('botao-sair')).toBeTruthy();
  });
});
