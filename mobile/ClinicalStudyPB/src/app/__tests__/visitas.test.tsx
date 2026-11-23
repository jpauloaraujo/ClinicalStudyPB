import { act, fireEvent, render, screen, waitFor } from '@testing-library/react-native';
import { ErroApi } from '@/api/erro';
import { visitasApi, type HistoricoMinhasVisitas, type MinhaVisita } from '@/api/visitas';
import VisitasScreen, { TEXTO_SEM_PROXIMAS } from '@/app/(tabs)/visitas';
import { TEXTO_SEM_HISTORICO, juntarPaginas } from '@/components/ListaHistoricoVisitas';
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

  it('a parte Histórico pode ser selecionada e carrega o histórico', async () => {
    jest.spyOn(visitasApi, 'proximas').mockResolvedValue([]);
    const historico = jest.spyOn(visitasApi, 'historico').mockResolvedValue(pagina([], 0, 0));

    await render(<VisitasScreen />);
    await fireEvent.press(screen.getByRole('tab', { name: 'Histórico' }));

    await waitFor(() => expect(screen.getByRole('tab', { name: 'Histórico', selected: true })).toBeTruthy());
    await waitFor(() => expect(historico).toHaveBeenCalled());
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

const realizada: MinhaVisita = {
  ...proxima,
  id: 'h-1',
  visitaNome: 'Triagem',
  dataHora: '2026-10-30T09:00:00-03:00',
  situacao: 'REALIZADA',
  dataHoraRealizada: '2026-10-30T09:10:00-03:00',
};
const ausente: MinhaVisita = { ...proxima, id: 'h-2', visitaNome: 'Retorno 15 dias', dataHora: '2026-10-20T10:00:00-03:00', situacao: 'AUSENTE' };
const cancelada: MinhaVisita = {
  ...proxima,
  id: 'h-3',
  visitaNome: 'Retorno 45 dias',
  dataHora: '2026-10-10T10:00:00-03:00',
  situacao: 'CANCELADA',
  motivo: 'retirada do estudo',
};

function pagina(
  conteudo: MinhaVisita[],
  numero = 0,
  totalPaginas = 1,
  resumo = { realizadas: 3, ausentes: 1, canceladas: 5 },
): HistoricoMinhasVisitas {
  return { resumo, conteudo, pagina: numero, tamanho: 20, totalElementos: conteudo.length, totalPaginas };
}

async function abrirHistorico() {
  await render(<VisitasScreen />);
  await fireEvent.press(screen.getByRole('tab', { name: 'Histórico' }));
}

describe('Aba Visitas: histórico de comparecimento (T118, RQ-T118-01 a 06 e 10)', () => {
  beforeEach(() => {
    jest.spyOn(visitasApi, 'proximas').mockResolvedValue([]);
  });

  it('mostra realizada, ausente e cancelada na ordem da API, com selo de texto, data e estudo (RQ-01)', async () => {
    jest.spyOn(visitasApi, 'historico').mockResolvedValue(pagina([realizada, ausente, cancelada]));

    await abrirHistorico();

    expect(await screen.findByText('Triagem')).toBeTruthy();
    expect(screen.getByText('Realizada')).toBeTruthy();
    expect(screen.getByText('Ausente')).toBeTruthy();
    expect(screen.getByText('Cancelada')).toBeTruthy();
    expect(screen.getByText('sex, 30/10 às 09:00')).toBeTruthy();
    expect(screen.getAllByText('Estudo de pressão arterial')).toHaveLength(3);
    const nomes = screen.getAllByText(/^(Triagem|Retorno )/).map((no) => no.props.children);
    expect(nomes).toEqual(['Triagem', 'Retorno 15 dias', 'Retorno 45 dias']);
  });

  it('a cancelada mostra o motivo e a realizada, a data e hora reais (RQ-02)', async () => {
    jest.spyOn(visitasApi, 'historico').mockResolvedValue(pagina([realizada, cancelada]));

    await abrirHistorico();

    expect(await screen.findByText('Motivo: retirada do estudo')).toBeTruthy();
    expect(screen.getByText('Realizada em sex, 30/10 às 09:10')).toBeTruthy();
  });

  it('o resumo vem do servidor e as canceladas não entram: "comparecimento: 3 de 4 visitas" (RQ-06)', async () => {
    jest.spyOn(visitasApi, 'historico').mockResolvedValue(pagina([realizada]));

    await abrirHistorico();

    expect(await screen.findByText('comparecimento: 3 de 4 visitas')).toBeTruthy();
    expect(screen.getByText('75%')).toBeTruthy();
    expect(screen.getByLabelText('Comparecimento: 3 de 4 visitas')).toBeTruthy();
  });

  it('sem realizadas nem ausentes mostra "Sem visitas contabilizadas ainda" (RQ-06)', async () => {
    jest
      .spyOn(visitasApi, 'historico')
      .mockResolvedValue(pagina([cancelada], 0, 1, { realizadas: 0, ausentes: 0, canceladas: 1 }));

    await abrirHistorico();

    expect(await screen.findByText('Sem visitas contabilizadas ainda')).toBeTruthy();
  });

  it('histórico vazio mostra "Nenhuma visita no histórico ainda." (RQ-04)', async () => {
    jest.spyOn(visitasApi, 'historico').mockResolvedValue(pagina([], 0, 0, { realizadas: 0, ausentes: 0, canceladas: 0 }));

    await abrirHistorico();

    expect(await screen.findByText(TEXTO_SEM_HISTORICO)).toBeTruthy();
    expect(TEXTO_SEM_HISTORICO).toBe('Nenhuma visita no histórico ainda.');
  });

  it('falha na carga mostra a mensagem e "Tentar de novo" repete a chamada (RQ-04)', async () => {
    const historico = jest
      .spyOn(visitasApi, 'historico')
      .mockRejectedValueOnce(new ErroApi('Falha no servidor.', { status: 500 }))
      .mockResolvedValue(pagina([realizada]));

    await abrirHistorico();
    expect(await screen.findByText('Falha no servidor.')).toBeTruthy();
    await fireEvent.press(screen.getByRole('button', { name: 'Tentar de novo' }));

    expect(await screen.findByText('Triagem')).toBeTruthy();
    expect(historico).toHaveBeenCalledTimes(2);
  });

  it('"Carregar mais" busca a próxima página sem duplicar itens e some na última (RQ-03)', async () => {
    const historico = jest
      .spyOn(visitasApi, 'historico')
      .mockResolvedValueOnce(pagina([realizada, ausente], 0, 2))
      .mockResolvedValueOnce(pagina([ausente, cancelada], 1, 2));

    await abrirHistorico();
    await screen.findByText('Triagem');
    await fireEvent.press(screen.getByRole('button', { name: 'Carregar mais' }));

    expect(await screen.findByText('Retorno 45 dias')).toBeTruthy();
    expect(screen.getAllByText('Retorno 15 dias')).toHaveLength(1);
    expect(historico).toHaveBeenLastCalledWith({ estudoId: undefined, pagina: 1, tamanho: 20 });
    expect(screen.queryByRole('button', { name: 'Carregar mais' })).toBeNull();
  });

  it('puxar para atualizar recarrega do início (RQ-03)', async () => {
    const historico = jest.spyOn(visitasApi, 'historico').mockResolvedValue(pagina([realizada]));

    await abrirHistorico();
    await screen.findByText('Triagem');
    historico.mockResolvedValue(pagina([realizada, ausente]));

    await act(async () => {
      screen.getByTestId('lista-historico').props.refreshControl.props.onRefresh();
    });

    expect(await screen.findByText('Retorno 15 dias')).toBeTruthy();
    expect(historico).toHaveBeenLastCalledWith({ estudoId: undefined, pagina: 0, tamanho: 20 });
  });

  it('com estudoId, o histórico também é filtrado (RQ-05)', async () => {
    mockParametros = { estudoId: 'e-1' };
    const historico = jest.spyOn(visitasApi, 'historico').mockResolvedValue(pagina([realizada]));

    await abrirHistorico();
    await screen.findByText('Triagem');

    expect(historico).toHaveBeenCalledWith({ estudoId: 'e-1', pagina: 0, tamanho: 20 });
  });

  it('o cartão do histórico tem rótulo com a situação e o motivo (RQ-10)', async () => {
    jest.spyOn(visitasApi, 'historico').mockResolvedValue(pagina([cancelada]));

    await abrirHistorico();

    expect(
      await screen.findByRole('button', {
        name: 'Visita Retorno 45 dias, sábado, 10 de outubro às 10:00, estudo Estudo de pressão arterial, cancelada, motivo: retirada do estudo',
      }),
    ).toBeTruthy();
  });

  it('juntarPaginas não repete visitas já carregadas', () => {
    expect(juntarPaginas([realizada, ausente], [ausente, cancelada]).map((v) => v.id)).toEqual(['h-1', 'h-2', 'h-3']);
  });
});

describe('Estados de carregamento e de erro das visitas (T119, RQ-T119-03 a 07)', () => {
  const SEM_CONEXAO = new ErroApi('Não foi possível conectar à API em http://10.0.2.2:8081.', { codigo: 'SEM_CONEXAO' });

  it('próximas: sem conexão mostra o texto próprio e "Tentar de novo" recupera (RQ-05)', async () => {
    const proximas = jest
      .spyOn(visitasApi, 'proximas')
      .mockRejectedValueOnce(SEM_CONEXAO)
      .mockResolvedValue([proxima]);

    await render(<VisitasScreen />);

    expect(await screen.findByText('Não foi possível conectar. Verifique sua conexão e tente de novo.')).toBeTruthy();
    expect(screen.queryByText(/10\.0\.2\.2/)).toBeNull();
    await fireEvent.press(screen.getByRole('button', { name: 'Tentar de novo' }));

    expect(await screen.findByText('Retorno 30 dias')).toBeTruthy();
    expect(proximas).toHaveBeenCalledTimes(2);
  });

  it('próximas: se a atualização falha com a lista já na tela, a lista é mantida com o aviso (RQ-05)', async () => {
    const proximas = jest.spyOn(visitasApi, 'proximas').mockResolvedValue([proxima]);

    await render(<VisitasScreen />);
    await screen.findByText('Retorno 30 dias');
    proximas.mockRejectedValue(SEM_CONEXAO);

    await act(async () => {
      screen.getByTestId('lista-proximas').props.refreshControl.props.onRefresh();
    });

    expect(await screen.findByText('Não foi possível conectar. Verifique sua conexão e tente de novo.')).toBeTruthy();
    expect(screen.getByText('Retorno 30 dias')).toBeTruthy();
  });

  it('próximas: erro 5xx sem mensagem do servidor mostra texto genérico, nunca JSON cru (RQ-04)', async () => {
    jest.spyOn(visitasApi, 'proximas').mockRejectedValue(new ErroApi('Erro 500 na API.', { status: 500 }));

    await render(<VisitasScreen />);

    expect(await screen.findByText('Não foi possível carregar agora. Tente de novo.')).toBeTruthy();
    expect(screen.getByRole('button', { name: 'Tentar de novo' })).toBeTruthy();
  });

  it('próximas: participante retirado ou sem estudo recebe lista vazia (200) e não vê erro (RQ-07)', async () => {
    jest.spyOn(visitasApi, 'proximas').mockResolvedValue([]);

    await render(<VisitasScreen />);

    expect(await screen.findByText('Nenhuma visita agendada.')).toBeTruthy();
    expect(screen.queryByRole('button', { name: 'Tentar de novo' })).toBeNull();
  });

  it('histórico: sem conexão na primeira carga mostra o texto próprio e "Tentar de novo" recupera (RQ-05)', async () => {
    jest.spyOn(visitasApi, 'proximas').mockResolvedValue([]);
    const historico = jest
      .spyOn(visitasApi, 'historico')
      .mockRejectedValueOnce(SEM_CONEXAO)
      .mockResolvedValue(pagina([realizada]));

    await abrirHistorico();
    expect(await screen.findByText('Não foi possível conectar. Verifique sua conexão e tente de novo.')).toBeTruthy();
    await fireEvent.press(screen.getByRole('button', { name: 'Tentar de novo' }));

    expect(await screen.findByText('Triagem')).toBeTruthy();
    expect(historico).toHaveBeenCalledTimes(2);
  });

  it('histórico: falha ao carregar mais mantém a lista e o aviso repete só a página que falhou (RQ-05)', async () => {
    jest.spyOn(visitasApi, 'proximas').mockResolvedValue([]);
    const historico = jest
      .spyOn(visitasApi, 'historico')
      .mockResolvedValueOnce(pagina([realizada], 0, 2))
      .mockRejectedValueOnce(SEM_CONEXAO)
      .mockResolvedValueOnce(pagina([ausente], 1, 2));

    await abrirHistorico();
    await screen.findByText('Triagem');
    await fireEvent.press(screen.getByRole('button', { name: 'Carregar mais' }));

    expect(await screen.findByText('Não foi possível conectar. Verifique sua conexão e tente de novo.')).toBeTruthy();
    expect(screen.getByText('Triagem')).toBeTruthy();
    await fireEvent.press(screen.getByRole('button', { name: 'Tentar de novo' }));

    expect(await screen.findByText('Retorno 15 dias')).toBeTruthy();
    expect(historico).toHaveBeenLastCalledWith({ estudoId: undefined, pagina: 1, tamanho: 20 });
  });

  it('histórico: o carregamento da primeira carga tem texto acessível (RQ-03)', async () => {
    jest.spyOn(visitasApi, 'proximas').mockResolvedValue([]);
    jest.spyOn(visitasApi, 'historico').mockReturnValue(new Promise(() => {}));

    await abrirHistorico();

    expect(screen.getByRole('progressbar', { name: 'Carregando…' })).toBeTruthy();
  });
});
