import { fireEvent, render, screen } from '@testing-library/react-native';
import { visitasApi, type HistoricoMinhasVisitas, type MinhaVisita } from '@/api/visitas';
import VisitasScreen from '@/app/(tabs)/visitas';

// T121 (RQ-T121-02): o fluxo Maestro das visitas acha os elementos por identificador, não por coordenada. Estes testes só
// garantem que os identificadores e os textos usados em `.maestro/fluxo-participante.yaml` continuam nas telas.
jest.mock('expo-router', () => ({
  useRouter: () => ({ push: jest.fn(), back: jest.fn(), replace: jest.fn(), setParams: jest.fn(), canGoBack: () => true }),
  useLocalSearchParams: () => ({}),
}));

jest.setTimeout(30000);

const proxima: MinhaVisita = {
  id: 'v-1',
  participacaoId: 'p-1',
  estudoId: 'e-1',
  estudoTitulo: 'Demo - Acompanhamento de participantes',
  visitaNome: 'Retorno 30 dias',
  dataHora: '2026-12-03T09:30:00-03:00',
  situacao: 'AGENDADA',
  dataHoraRealizada: null,
  motivo: null,
  atividades: ['Escala de dor', 'Exame físico'],
};
const realizada: MinhaVisita = {
  ...proxima,
  id: 'v-2',
  visitaNome: 'Triagem',
  dataHora: '2026-11-03T09:00:00-03:00',
  situacao: 'REALIZADA',
  dataHoraRealizada: '2026-11-03T09:10:00-03:00',
  atividades: ['Aferir pressão arterial', 'Coletar sangue'],
};
const historico: HistoricoMinhasVisitas = {
  resumo: { realizadas: 1, ausentes: 0, canceladas: 0 },
  conteudo: [realizada],
  pagina: 0,
  tamanho: 20,
  totalElementos: 1,
  totalPaginas: 1,
};

describe('Identificadores do fluxo E2E das visitas (T121, RQ-T121-01 e 02)', () => {
  afterEach(() => {
    jest.restoreAllMocks();
  });

  it('as próximas expõem o cartão da visita e o segmento do histórico', async () => {
    jest.spyOn(visitasApi, 'proximas').mockResolvedValue([proxima]);

    await render(<VisitasScreen />);

    expect(await screen.findAllByTestId('cartao-visita')).toHaveLength(1);
    expect(screen.getByTestId('segmento-historico')).toBeTruthy();
    expect(screen.getByTestId('segmento-proximas')).toBeTruthy();
    expect(screen.getByText('Retorno 30 dias')).toBeTruthy();
    expect(screen.getByText('Agendada')).toBeTruthy();
  });

  it('o histórico expõe o resumo "comparecimento: 1 de 1 visitas" e a visita realizada com o selo', async () => {
    jest.spyOn(visitasApi, 'proximas').mockResolvedValue([proxima]);
    jest.spyOn(visitasApi, 'historico').mockResolvedValue(historico);

    await render(<VisitasScreen />);
    await fireEvent.press(screen.getByTestId('segmento-historico'));

    expect(await screen.findByTestId('resumo-comparecimento')).toBeTruthy();
    expect(screen.getByText('comparecimento: 1 de 1 visitas')).toBeTruthy();
    expect(screen.getByText('Triagem')).toBeTruthy();
    expect(screen.getByText('Realizada')).toBeTruthy();
  });
});
