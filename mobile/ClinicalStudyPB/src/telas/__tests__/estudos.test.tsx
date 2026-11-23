import { act, fireEvent, render, screen, waitFor } from '@testing-library/react-native';
import { ErroApi } from '@/api/erro';
import { estudosApi, type MeuEstudo, type MeuEstudoDetalhe } from '@/api/estudos';
import DetalheEstudoScreen, { TEXTO_ESTUDO_NAO_ENCONTRADO } from '@/app/estudos/[estudoId]';
import EstudosScreen, { descricaoDoEstudo, TEXTO_SEM_ESTUDOS } from '@/app/(tabs)/estudos';

// Navegação simulada: o que importa é para onde a tela manda o participante (prefixo "mock" exigido pelo Jest)
const mockPush = jest.fn();
const mockBack = jest.fn();
const mockReplace = jest.fn();
let mockPodeVoltar = true;
let mockEstudoId = 'e-1';
jest.mock('expo-router', () => ({
  useRouter: () => ({ push: mockPush, back: mockBack, replace: mockReplace, canGoBack: () => mockPodeVoltar }),
  useLocalSearchParams: () => ({ estudoId: mockEstudoId }),
}));

// O primeiro render carrega react-native e o roteador; com as suítes em paralelo passa dos 5 s padrão (F-T110-03)
jest.setTimeout(30000);

const ativo: MeuEstudo = {
  participacaoId: 'p-1',
  estudoId: 'e-1',
  titulo: 'Estudo de pressão arterial',
  resumo: 'Avaliar a eficácia de uma intervenção fictícia.',
  statusEstudo: 'EXECUCAO',
  dataInclusao: '2026-10-30',
  situacaoParticipacao: 'ATIVO',
};
const retirado: MeuEstudo = {
  participacaoId: 'p-2',
  estudoId: 'e-2',
  titulo: 'Estudo de sono',
  resumo: 'Estudo fictício sobre a qualidade do sono.',
  statusEstudo: 'RECRUTAMENTO',
  dataInclusao: '2026-09-15',
  situacaoParticipacao: 'RETIRADO',
};
const detalheAtivo: MeuEstudoDetalhe = {
  ...ativo,
  objetivo: 'Avaliar a eficácia e a segurança de uma intervenção fictícia no controle da pressão arterial em adultos.',
  dataInicio: '2026-10-01',
  dataFim: '2027-05-01',
  responsavelNome: 'Coordenador de Demonstração',
  retirada: null,
};

beforeEach(() => {
  mockPush.mockReset();
  mockBack.mockReset();
  mockReplace.mockReset();
  mockPodeVoltar = true;
  mockEstudoId = 'e-1';
});

afterEach(() => {
  jest.restoreAllMocks();
});

describe('Lista Meus estudos (T112, RQ-T112-01 a 04 e 09)', () => {
  it('mostra o carregamento enquanto a lista não chega', async () => {
    jest.spyOn(estudosApi, 'listar').mockReturnValue(new Promise(() => {}));

    await render(<EstudosScreen />);

    expect(screen.getByText('Carregando…')).toBeTruthy();
    expect(screen.queryByText(TEXTO_SEM_ESTUDOS)).toBeNull();
  });

  it('cada estudo é um cartão com título, status em português e data de inclusão dd/MM/yyyy (RQ-01)', async () => {
    jest.spyOn(estudosApi, 'listar').mockResolvedValue([ativo, retirado]);

    await render(<EstudosScreen />);

    expect(await screen.findByText('Estudo de pressão arterial')).toBeTruthy();
    expect(screen.getByText('Em execução')).toBeTruthy();
    expect(screen.getByText('Incluído em 30/10/2026')).toBeTruthy();
    expect(screen.getByText('Recrutamento')).toBeTruthy();
    expect(screen.getByText('Incluído em 15/09/2026')).toBeTruthy();
    expect(screen.getByText('Avaliar a eficácia de uma intervenção fictícia.')).toBeTruthy();
  });

  it('só o estudo com participação retirada tem o selo "Retirado" (RQ-01)', async () => {
    jest.spyOn(estudosApi, 'listar').mockResolvedValue([ativo, retirado]);

    await render(<EstudosScreen />);
    await screen.findByText('Estudo de sono');

    expect(screen.getAllByText('Retirado')).toHaveLength(1);
  });

  it('mantém a ordem recebida da API, sem reordenar no aplicativo (RQ-02)', async () => {
    jest.spyOn(estudosApi, 'listar').mockResolvedValue([retirado, ativo]);

    await render(<EstudosScreen />);
    await screen.findByText('Estudo de sono');

    const titulos = screen.getAllByText(/^Estudo de /).map((no) => no.props.children);
    expect(titulos).toEqual(['Estudo de sono', 'Estudo de pressão arterial']);
  });

  it('o cartão tem papel de botão e rótulo com título, status e data (RQ-09)', async () => {
    jest.spyOn(estudosApi, 'listar').mockResolvedValue([ativo, retirado]);

    await render(<EstudosScreen />);

    expect(await screen.findByRole('button', {
      name: 'Estudo Estudo de pressão arterial, em execução, incluído em 30/10/2026',
    })).toBeTruthy();
    expect(descricaoDoEstudo(retirado)).toBe('Estudo Estudo de sono, recrutamento, incluído em 15/09/2026, retirado');
    expect(screen.getByRole('button', { name: descricaoDoEstudo(retirado) })).toBeTruthy();
  });

  it('tocar no cartão abre o detalhe do estudo (RQ-02)', async () => {
    jest.spyOn(estudosApi, 'listar').mockResolvedValue([ativo]);

    await render(<EstudosScreen />);
    await fireEvent.press(await screen.findByRole('button', { name: descricaoDoEstudo(ativo) }));

    expect(mockPush).toHaveBeenCalledWith({ pathname: '/estudos/[estudoId]', params: { estudoId: 'e-1' } });
  });

  it('lista vazia mostra o texto do guia, e não um erro (RQ-03)', async () => {
    jest.spyOn(estudosApi, 'listar').mockResolvedValue([]);

    await render(<EstudosScreen />);

    expect(await screen.findByText('Você ainda não está incluído em nenhum estudo.')).toBeTruthy();
    expect(TEXTO_SEM_ESTUDOS).toBe('Você ainda não está incluído em nenhum estudo.');
    expect(screen.queryByText('Tentar de novo')).toBeNull();
  });

  it('erro mostra a mensagem e "Tentar de novo", que busca de novo (RQ-03)', async () => {
    const listar = jest
      .spyOn(estudosApi, 'listar')
      .mockRejectedValueOnce(new ErroApi('Não foi possível conectar.', { codigo: 'SEM_CONEXAO' }))
      .mockResolvedValue([ativo]);

    await render(<EstudosScreen />);
    expect(await screen.findByText('Não foi possível conectar.')).toBeTruthy();
    await fireEvent.press(screen.getByText('Tentar de novo'));

    expect(await screen.findByText('Estudo de pressão arterial')).toBeTruthy();
    expect(screen.queryByText('Não foi possível conectar.')).toBeNull();
    expect(listar).toHaveBeenCalledTimes(2);
  });

  async function puxarParaAtualizar() {
    const lista = screen.getByTestId('lista-estudos');
    await act(async () => {
      lista.props.refreshControl.props.onRefresh();
    });
  }

  it('puxar para atualizar recarrega a lista e o indicador termina (RQ-04)', async () => {
    const listar = jest.spyOn(estudosApi, 'listar').mockResolvedValueOnce([ativo]).mockResolvedValue([ativo, retirado]);

    await render(<EstudosScreen />);
    await screen.findByText('Estudo de pressão arterial');
    await puxarParaAtualizar();

    expect(await screen.findByText('Estudo de sono')).toBeTruthy();
    expect(listar).toHaveBeenCalledTimes(2);
    await waitFor(() => expect(screen.getByTestId('lista-estudos').props.refreshControl.props.refreshing).toBe(false));
  });

  it('atualização que falha mantém os cartões, mostra o erro e também termina o indicador (RQ-04)', async () => {
    jest
      .spyOn(estudosApi, 'listar')
      .mockResolvedValueOnce([ativo])
      .mockRejectedValue(new ErroApi('Erro 500 na API.', { status: 500 }));

    await render(<EstudosScreen />);
    await screen.findByText('Estudo de pressão arterial');
    await puxarParaAtualizar();

    expect(await screen.findByText('Erro 500 na API.')).toBeTruthy();
    expect(screen.getByText('Estudo de pressão arterial')).toBeTruthy();
    await waitFor(() => expect(screen.getByTestId('lista-estudos').props.refreshControl.props.refreshing).toBe(false));
  });

  it('não mostra nenhum dado de outro participante nem JSON cru (RQ-10)', async () => {
    jest.spyOn(estudosApi, 'listar').mockResolvedValue([ativo]);

    await render(<EstudosScreen />);
    await screen.findByText('Estudo de pressão arterial');

    expect(screen.queryByText(/participacaoId|estudoId|\{|"/)).toBeNull();
  });
});

describe('Detalhe do estudo (T112, RQ-T112-05 a 07)', () => {
  it('mostra o carregamento enquanto o detalhe não chega', async () => {
    jest.spyOn(estudosApi, 'buscar').mockReturnValue(new Promise(() => {}));

    await render(<DetalheEstudoScreen />);

    expect(screen.getByText('Carregando…')).toBeTruthy();
  });

  it('mostra título, objetivo completo, responsável, período, status e situação (RQ-05)', async () => {
    const buscar = jest.spyOn(estudosApi, 'buscar').mockResolvedValue(detalheAtivo);

    await render(<DetalheEstudoScreen />);

    expect(await screen.findByText('Estudo de pressão arterial')).toBeTruthy();
    expect(buscar).toHaveBeenCalledWith('e-1');
    expect(screen.getByText(detalheAtivo.objetivo)).toBeTruthy();
    expect(screen.getByText('Coordenador de Demonstração')).toBeTruthy();
    expect(screen.getByText('01/10/2026 a 01/05/2027')).toBeTruthy();
    expect(screen.getByText('Em execução')).toBeTruthy();
    expect(screen.getByText('Ativo')).toBeTruthy();
    expect(screen.getByText('30/10/2026')).toBeTruthy();
    expect(screen.queryByText(/Retirado em/)).toBeNull();
  });

  it('período com data ausente mostra "—" na ponta que falta e, sem as duas, o texto próprio (RQ-05)', async () => {
    jest.spyOn(estudosApi, 'buscar').mockResolvedValue({ ...detalheAtivo, dataFim: null });
    await render(<DetalheEstudoScreen />);
    expect(await screen.findByText('01/10/2026 a —')).toBeTruthy();
  });

  it('sem nenhuma das datas do período mostra "Período não informado" (RQ-05)', async () => {
    jest.spyOn(estudosApi, 'buscar').mockResolvedValue({ ...detalheAtivo, dataInicio: null, dataFim: null });
    await render(<DetalheEstudoScreen />);
    expect(await screen.findByText('Período não informado')).toBeTruthy();
  });

  it('participante retirado vê a marca "Retirado" e a data da retirada, nunca o motivo (RQ-05)', async () => {
    mockEstudoId = 'e-2';
    jest.spyOn(estudosApi, 'buscar').mockResolvedValue({
      ...detalheAtivo,
      ...retirado,
      objetivo: 'Estudo fictício sobre a qualidade do sono.',
      retirada: { dataRetirada: '2026-11-10' },
    });

    await render(<DetalheEstudoScreen />);

    expect(await screen.findByText('Retirado em 10/11/2026')).toBeTruthy();
    expect(screen.getAllByText('Retirado').length).toBeGreaterThanOrEqual(1);
    expect(screen.queryByText(/motivo/i)).toBeNull();
  });

  it('o atalho "Ver visitas deste estudo" navega para a aba Visitas com o estudoId (RQ-06)', async () => {
    jest.spyOn(estudosApi, 'buscar').mockResolvedValue(detalheAtivo);

    await render(<DetalheEstudoScreen />);
    await fireEvent.press(await screen.findByRole('button', { name: 'Ver visitas deste estudo' }));

    expect(mockPush).toHaveBeenCalledWith({ pathname: '/visitas', params: { estudoId: 'e-1' } });
  });

  it('erro mostra a mensagem e "Tentar de novo", que busca de novo (RQ-07)', async () => {
    const buscar = jest
      .spyOn(estudosApi, 'buscar')
      .mockRejectedValueOnce(new ErroApi('Erro 500 na API.', { status: 500 }))
      .mockResolvedValue(detalheAtivo);

    await render(<DetalheEstudoScreen />);
    expect(await screen.findByText('Erro 500 na API.')).toBeTruthy();
    await fireEvent.press(screen.getByText('Tentar de novo'));

    expect(await screen.findByText(detalheAtivo.objetivo)).toBeTruthy();
    expect(buscar).toHaveBeenCalledTimes(2);
  });

  it('404 mostra "Estudo não encontrado." e o botão Voltar, sem "Tentar de novo" (RQ-07)', async () => {
    jest.spyOn(estudosApi, 'buscar').mockRejectedValue(new ErroApi('Estudo não encontrado.', { status: 404 }));

    await render(<DetalheEstudoScreen />);

    expect(await screen.findByText('Estudo não encontrado.')).toBeTruthy();
    expect(TEXTO_ESTUDO_NAO_ENCONTRADO).toBe('Estudo não encontrado.');
    expect(screen.queryByText('Tentar de novo')).toBeNull();
    await fireEvent.press(screen.getByRole('button', { name: 'Voltar' }));
    expect(mockBack).toHaveBeenCalledTimes(1);
  });

  it('sem histórico de navegação, Voltar leva para a lista de estudos (RQ-07)', async () => {
    mockPodeVoltar = false;
    jest.spyOn(estudosApi, 'buscar').mockRejectedValue(new ErroApi('Estudo não encontrado.', { status: 404 }));

    await render(<DetalheEstudoScreen />);
    await fireEvent.press(await screen.findByRole('button', { name: 'Voltar' }));

    expect(mockBack).not.toHaveBeenCalled();
    expect(mockReplace).toHaveBeenCalledWith('/estudos');
  });
});
