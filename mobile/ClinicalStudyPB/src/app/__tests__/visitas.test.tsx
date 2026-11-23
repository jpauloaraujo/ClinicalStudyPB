import { act, fireEvent, render, screen, waitFor } from '@testing-library/react-native';
import { ErroApi } from '@/api/erro';
import { visitasApi, type MinhaVisita } from '@/api/visitas';
import VisitasScreen, { TEXTO_SEM_PROXIMAS } from '@/app/(tabs)/visitas';
import DetalheVisitaScreen, { TEXTO_VISITA_NAO_ENCONTRADA, lerVisita } from '@/app/visitas/[visitaId]';
import { descricaoDaVisita } from '@/utils/visitas';

// Navegação simulada: o que importa é para onde a tela manda o participante (prefixo "mock" exigido pelo Jest)
const mockPush = jest.fn();
const mockBack = jest.fn();
const mockReplace = jest.fn();
const mockSetParams = jest.fn();
let mockParametros: Record<string, string | undefined> = {};
jest.mock('expo-router', () => ({
  useRouter: () => ({ push: mockPush, back: mockBack, replace: mockReplace, setParams: mockSetParams, canGoBack: () => true }),
  useLocalSearchParams: () => mockParametros,
}));

jest.setTimeout(30000);

const proxima: MinhaVisita = {
  id: 'v-1',
  participacaoId: 'p-1',
  estudoId: 'e-1',
  estudoTitulo: 'Estudo de pressão arterial',
  visitaNome: 'Retorno 30 dias',
  dataHora: '2026-11-24T09:30:00-03:00',
  situacao: 'AGENDADA',
  dataHoraRealizada: null,
  motivo: null,
  atividades: ['Aferir pressão arterial', 'Coletar sangue'],
};
const outra: MinhaVisita = {
  ...proxima,
  id: 'v-2',
  visitaNome: 'Retorno 60 dias',
  dataHora: '2026-12-22T14:00:00-03:00',
  atividades: [],
};

beforeEach(() => {
  mockPush.mockReset();
  mockBack.mockReset();
  mockReplace.mockReset();
  mockSetParams.mockReset();
  mockParametros = {};
});

afterEach(() => {
  jest.restoreAllMocks();
});

describe('Aba Visitas: próximas (T117, RQ-T117-01 a 05 e 10)', () => {
  it('Próximas vem selecionada por padrão e o seletor tem as duas partes (RQ-01)', async () => {
    jest.spyOn(visitasApi, 'proximas').mockResolvedValue([proxima]);

    await render(<VisitasScreen />);
    await screen.findByText('Retorno 30 dias');

    expect(screen.getByRole('tab', { name: 'Próximas', selected: true })).toBeTruthy();
    expect(screen.getByRole('tab', { name: 'Histórico' })).toBeTruthy();
  });

  it('o cartão mostra data em português, visita, estudo, atividades e selo de texto, na ordem da API (RQ-01)', async () => {
    jest.spyOn(visitasApi, 'proximas').mockResolvedValue([proxima, outra]);

    await render(<VisitasScreen />);

    expect(await screen.findByText('ter, 24/11 às 09:30')).toBeTruthy();
    expect(screen.getAllByText('Estudo de pressão arterial')).toHaveLength(2);
    expect(screen.getByText('• Aferir pressão arterial')).toBeTruthy();
    expect(screen.getByText('• Coletar sangue')).toBeTruthy();
    expect(screen.getAllByText('Agendada')).toHaveLength(2);
    const nomes = screen.getAllByText(/^Retorno /).map((no) => no.props.children);
    expect(nomes).toEqual(['Retorno 30 dias', 'Retorno 60 dias']);
  });

  it('o cartão tem papel de botão e rótulo completo (RQ-10)', async () => {
    jest.spyOn(visitasApi, 'proximas').mockResolvedValue([proxima]);

    await render(<VisitasScreen />);

    expect(descricaoDaVisita(proxima)).toBe(
      'Visita Retorno 30 dias, terça-feira, 24 de novembro às 09:30, estudo Estudo de pressão arterial, agendada',
    );
    expect(await screen.findByRole('button', { name: descricaoDaVisita(proxima) })).toBeTruthy();
  });

  it('tocar no cartão abre o detalhe com os dados da visita (RQ-02)', async () => {
    jest.spyOn(visitasApi, 'proximas').mockResolvedValue([proxima]);

    await render(<VisitasScreen />);
    await fireEvent.press(await screen.findByRole('button', { name: descricaoDaVisita(proxima) }));

    expect(mockPush).toHaveBeenCalledWith({
      pathname: '/visitas/[visitaId]',
      params: { visitaId: 'v-1', visita: JSON.stringify(proxima) },
    });
  });

  it('lista vazia mostra "Nenhuma visita agendada." (RQ-03)', async () => {
    jest.spyOn(visitasApi, 'proximas').mockResolvedValue([]);

    await render(<VisitasScreen />);

    expect(await screen.findByText(TEXTO_SEM_PROXIMAS)).toBeTruthy();
    expect(TEXTO_SEM_PROXIMAS).toBe('Nenhuma visita agendada.');
  });

  it('mostra o carregamento enquanto a lista não chega (RQ-03)', async () => {
    jest.spyOn(visitasApi, 'proximas').mockReturnValue(new Promise(() => {}));

    await render(<VisitasScreen />);

    expect(screen.getByText('Carregando…')).toBeTruthy();
  });

  it('falha na carga mostra a mensagem e "Tentar de novo" repete a chamada (RQ-03)', async () => {
    const proximas = jest
      .spyOn(visitasApi, 'proximas')
      .mockRejectedValueOnce(new ErroApi('Falha no servidor.', { status: 500 }))
      .mockResolvedValue([proxima]);

    await render(<VisitasScreen />);
    expect(await screen.findByText('Falha no servidor.')).toBeTruthy();

    await fireEvent.press(screen.getByRole('button', { name: 'Tentar de novo' }));

    expect(await screen.findByText('Retorno 30 dias')).toBeTruthy();
    expect(proximas).toHaveBeenCalledTimes(2);
  });

  it('puxar para atualizar recarrega a lista (RQ-04)', async () => {
    const proximas = jest.spyOn(visitasApi, 'proximas').mockResolvedValue([proxima]);

    await render(<VisitasScreen />);
    await screen.findByText('Retorno 30 dias');
    proximas.mockResolvedValue([proxima, outra]);

    const lista = screen.getByTestId('lista-proximas');
    await act(async () => {
      lista.props.refreshControl.props.onRefresh();
    });

    expect(await screen.findByText('Retorno 60 dias')).toBeTruthy();
    expect(proximas).toHaveBeenCalledTimes(2);
  });

  it('com estudoId, filtra a chamada, mostra o estudo no topo e oferece ver todas (RQ-05)', async () => {
    mockParametros = { estudoId: 'e-1' };
    const proximas = jest.spyOn(visitasApi, 'proximas').mockResolvedValue([proxima]);

    await render(<VisitasScreen />);

    expect(await screen.findByText('Estudo: Estudo de pressão arterial')).toBeTruthy();
    expect(proximas).toHaveBeenCalledWith('e-1');

    await fireEvent.press(screen.getByRole('button', { name: 'Ver todas' }));
    expect(mockSetParams).toHaveBeenCalledWith({ estudoId: '' });
  });

  it('sem estudoId a chamada não leva filtro', async () => {
    const proximas = jest.spyOn(visitasApi, 'proximas').mockResolvedValue([]);

    await render(<VisitasScreen />);
    await screen.findByText(TEXTO_SEM_PROXIMAS);

    expect(proximas).toHaveBeenCalledWith(undefined);
    expect(screen.queryByRole('button', { name: 'Ver todas' })).toBeNull();
  });

  it('a parte Histórico pode ser selecionada', async () => {
    jest.spyOn(visitasApi, 'proximas').mockResolvedValue([]);

    await render(<VisitasScreen />);
    await fireEvent.press(screen.getByRole('tab', { name: 'Histórico' }));

    await waitFor(() => expect(screen.getByRole('tab', { name: 'Histórico', selected: true })).toBeTruthy());
  });
});

describe('Detalhe da visita (T117, RQ-T117-06 e 07)', () => {
  it('mostra data, nome, estudo, atividades e a situação em selo de texto (RQ-06)', async () => {
    mockParametros = { visitaId: 'v-1', visita: JSON.stringify(proxima) };

    await render(<DetalheVisitaScreen />);

    expect(screen.getByText('Retorno 30 dias')).toBeTruthy();
    expect(screen.getByText('ter, 24/11 às 09:30')).toBeTruthy();
    expect(screen.getByText('Estudo de pressão arterial')).toBeTruthy();
    expect(screen.getByText('• Aferir pressão arterial')).toBeTruthy();
    expect(screen.getByText('Agendada')).toBeTruthy();
  });

  it('não tem os botões de confirmar presença e reagendar (RF19, Sprint 4) (RQ-06)', async () => {
    mockParametros = { visitaId: 'v-1', visita: JSON.stringify(proxima) };

    await render(<DetalheVisitaScreen />);

    expect(screen.queryByText(/confirmar presença/i)).toBeNull();
    expect(screen.queryByText(/reagend/i)).toBeNull();
  });

  it('aberta sem os dados (link direto) mostra "Visita não encontrada." e o botão de voltar (RQ-07)', async () => {
    mockParametros = { visitaId: 'v-1' };

    await render(<DetalheVisitaScreen />);

    expect(screen.getByText(TEXTO_VISITA_NAO_ENCONTRADA)).toBeTruthy();
    await fireEvent.press(screen.getByRole('button', { name: 'Voltar' }));
    expect(mockBack).toHaveBeenCalled();
  });

  it.each([undefined, '', 'não é json', '{"x":1}'])('lerVisita(%p) devolve null sem quebrar', (bruto) => {
    expect(lerVisita(bruto)).toBeNull();
  });
});
