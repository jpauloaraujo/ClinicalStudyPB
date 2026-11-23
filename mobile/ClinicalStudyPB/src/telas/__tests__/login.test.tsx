import { act, fireEvent, render, screen, userEvent } from '@testing-library/react-native';
import { CODIGO_PERFIL_NAO_PARTICIPANTE, CODIGO_SEM_CONEXAO, ErroApi } from '@/api/erro';
import LoginScreen from '@/app/login';

// A tela é testada com o contexto simulado: o que importa aqui é o que ela mostra para cada falha
const mockEntrar = jest.fn();
let mockAviso: string | null = null;
jest.mock('@/contexts/sessao', () => ({
  useSessao: () => ({ entrar: mockEntrar, aviso: mockAviso, usuario: null, carregando: false, sair: jest.fn() }),
}));

// O primeiro render carrega react-native e expo-router; com as suítes em paralelo passa dos 5 s padrão (F-T110-03)
jest.setTimeout(30000);

const MSG_PERFIL = 'Este aplicativo é exclusivo para participantes. Use a aplicação web.';

async function preencher(email: string, senha: string) {
  await fireEvent.changeText(screen.getByLabelText('E-mail'), email);
  await fireEvent.changeText(screen.getByLabelText('Senha'), senha);
}

async function tocarEntrar() {
  await fireEvent.press(screen.getByText(/^Entrar|Entrando/));
}

describe('Tela de login (T110)', () => {
  beforeEach(() => {
    mockEntrar.mockReset();
    mockAviso = null;
  });

  it('exige e-mail e senha antes de enviar (RQ-T110-01)', async () => {
    await render(<LoginScreen />);

    await tocarEntrar();

    expect(screen.getByText('Informe o e-mail e a senha.')).toBeTruthy();
    expect(mockEntrar).not.toHaveBeenCalled();
  });

  it('exige e-mail com formato válido (RQ-T110-01)', async () => {
    await render(<LoginScreen />);
    await preencher('sem-arroba', 'senha');

    await tocarEntrar();

    expect(screen.getByText('Informe um e-mail válido.')).toBeTruthy();
    expect(mockEntrar).not.toHaveBeenCalled();
  });

  it('o erro some quando o usuário volta a digitar (RQ-T110-01)', async () => {
    await render(<LoginScreen />);
    await tocarEntrar();
    expect(screen.getByText('Informe o e-mail e a senha.')).toBeTruthy();

    await fireEvent.changeText(screen.getByLabelText('E-mail'), 'a');

    expect(screen.queryByText('Informe o e-mail e a senha.')).toBeNull();
  });

  it('envia o e-mail sem espaços nas pontas e a senha como digitada', async () => {
    mockEntrar.mockResolvedValue(undefined);
    await render(<LoginScreen />);
    await preencher('  participante@clinicalstudy.test  ', ' senha ');

    await tocarEntrar();

    expect(mockEntrar).toHaveBeenCalledWith('participante@clinicalstudy.test', ' senha ');
  });

  it('desabilita o botão durante o envio e não envia duas vezes (RQ-T110-02)', async () => {
    let concluir: () => void = () => undefined;
    mockEntrar.mockImplementation(() => new Promise<void>((resolver) => (concluir = resolver)));
    await render(<LoginScreen />);
    await preencher('participante@clinicalstudy.test', 'senha');

    // userEvent toca pelo caminho real do Pressable e não espera a promessa que o manipulador devolve
    // (entrar() só resolve em concluir()); ver F-T110-04 a F-T110-06
    const usuario = userEvent.setup();
    await usuario.press(screen.getByText('Entrar'));
    expect(screen.getByText('Entrando…')).toBeTruthy();
    expect(screen.getByRole('button', { name: 'Entrando…' })).toBeDisabled();

    // segundo toque e Enter no campo de senha durante o envio
    await usuario.press(screen.getByText('Entrando…'));
    await usuario.type(screen.getByLabelText('Senha'), 'a', { submitEditing: true });
    expect(mockEntrar).toHaveBeenCalledTimes(1);

    await act(async () => concluir());
    expect(screen.getByText('Entrar')).toBeTruthy();
  });

  it('alterna mostrar e ocultar a senha (RQ-T110-02)', async () => {
    await render(<LoginScreen />);
    expect(screen.getByLabelText('Senha').props.secureTextEntry).toBe(true);

    await fireEvent.press(screen.getByText('Mostrar senha'));

    expect(screen.getByLabelText('Senha').props.secureTextEntry).toBe(false);
    expect(screen.getByText('Ocultar senha')).toBeTruthy();
  });

  it('configura o teclado do e-mail e a ação de cada campo (RQ-T110-02)', async () => {
    await render(<LoginScreen />);

    const email = screen.getByLabelText('E-mail');
    const senha = screen.getByLabelText('Senha');

    expect(email.props.keyboardType).toBe('email-address');
    expect(email.props.autoCapitalize).toBe('none');
    expect(email.props.returnKeyType).toBe('next');
    expect(senha.props.returnKeyType).toBe('go');
  });

  it('mostra uma mensagem única para credencial inválida, sem repassar o texto da API (RQ-T110-03)', async () => {
    mockEntrar.mockRejectedValue(new ErroApi('Credenciais inválidas', { status: 401 }));
    await render(<LoginScreen />);
    await preencher('naoexiste@clinicalstudy.test', 'errada');

    await tocarEntrar();

    expect(await screen.findByText('E-mail ou senha inválidos, ou conta desativada.')).toBeTruthy();
    expect(screen.queryByText('Credenciais inválidas')).toBeNull();
  });

  it('mostra a mensagem de falha de rede sem a URL da API (RQ-T110-03)', async () => {
    mockEntrar.mockRejectedValue(new ErroApi('Não foi possível conectar à API em http://10.0.2.2:8081.', { codigo: CODIGO_SEM_CONEXAO }));
    await render(<LoginScreen />);
    await preencher('participante@clinicalstudy.test', 'senha');

    await tocarEntrar();

    expect(await screen.findByText('Não foi possível conectar. Verifique sua conexão e tente de novo.')).toBeTruthy();
    expect(screen.queryByText(/10\.0\.2\.2/)).toBeNull();
  });

  it('mostra mensagem genérica para erro inesperado, nunca JSON cru (RQ-T110-03)', async () => {
    mockEntrar.mockRejectedValue(new ErroApi('{"status":500,"erro":"NullPointerException"}', { status: 500 }));
    await render(<LoginScreen />);
    await preencher('participante@clinicalstudy.test', 'senha');

    await tocarEntrar();

    expect(await screen.findByText('Não foi possível entrar agora. Tente novamente.')).toBeTruthy();
    expect(screen.queryByText(/NullPointerException/)).toBeNull();
  });

  it.each(['ADMINISTRADOR', 'PESQUISADOR', 'COORDENADOR'])(
    'recusa o perfil %s com a mensagem da regra 8 (RQ-T110-05)',
    async () => {
      mockEntrar.mockRejectedValue(new ErroApi(MSG_PERFIL, { codigo: CODIGO_PERFIL_NAO_PARTICIPANTE }));
      await render(<LoginScreen />);
      await preencher('web@clinicalstudy.test', 'senha');

      await tocarEntrar();

      expect(await screen.findByText(MSG_PERFIL)).toBeTruthy();
      expect(screen.getByText('Entrar')).toBeTruthy();
    },
  );

  it('exibe o aviso de sessão expirada vindo do contexto (RQ-T110-09)', async () => {
    mockAviso = 'Sua sessão expirou. Entre novamente.';

    await render(<LoginScreen />);

    expect(screen.getByText('Sua sessão expirou. Entre novamente.')).toBeTruthy();
  });

  it('mantém o aviso de dados fictícios do protótipo M01 (RQ-T110-04)', async () => {
    await render(<LoginScreen />);

    expect(screen.getByText('Sistema acadêmico — utiliza apenas dados fictícios.')).toBeTruthy();
  });
});
