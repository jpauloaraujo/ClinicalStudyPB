import { fireEvent, render, screen } from '@testing-library/react-native';
import type { MinhaVisita } from '@/api/visitas';
import { CartaoVisita } from '@/components/CartaoVisita';
import { ResumoComparecimento } from '@/components/ResumoComparecimento';
import { Selo } from '@/components/Selo';
import { descricaoDaVisita, rotuloSituacaoVisita, tomSituacaoVisita } from '@/utils/visitas';

jest.setTimeout(30000);

const base: MinhaVisita = {
  id: 'v-1',
  participacaoId: 'p-1',
  estudoId: 'e-1',
  estudoTitulo: 'Estudo de pressão arterial',
  visitaNome: 'Retorno 30 dias',
  dataHora: '2026-11-24T09:30:00-03:00',
  situacao: 'AGENDADA',
  dataHoraRealizada: null,
  motivo: null,
  atividades: ['Aferir pressão arterial'],
};

describe('CartaoVisita (T120, RQ-T120-13)', () => {
  it('mostra data em português, visita, estudo, atividades e o selo de texto da situação', async () => {
    await render(<CartaoVisita visita={base} aoAbrir={() => {}} />);

    expect(screen.getByText('ter, 24/11 às 09:30')).toBeTruthy();
    expect(screen.getByText('Retorno 30 dias')).toBeTruthy();
    expect(screen.getByText('Estudo de pressão arterial')).toBeTruthy();
    expect(screen.getByText('• Aferir pressão arterial')).toBeTruthy();
    expect(screen.getByText('Agendada')).toBeTruthy();
  });

  it('tem papel de botão, rótulo completo e dispara aoAbrir ao tocar', async () => {
    const aoAbrir = jest.fn();
    await render(<CartaoVisita visita={base} aoAbrir={aoAbrir} />);

    const cartao = screen.getByRole('button', {
      name: 'Visita Retorno 30 dias, terça-feira, 24 de novembro às 09:30, estudo Estudo de pressão arterial, agendada',
    });
    await fireEvent.press(cartao);

    expect(aoAbrir).toHaveBeenCalledTimes(1);
  });

  it('a cancelada mostra o motivo e a realizada, a data e hora reais', async () => {
    await render(
      <>
        <CartaoVisita visita={{ ...base, id: 'c', situacao: 'CANCELADA', motivo: 'retirada do estudo' }} aoAbrir={() => {}} />
        <CartaoVisita
          visita={{ ...base, id: 'r', situacao: 'REALIZADA', dataHoraRealizada: '2026-11-24T09:40:00-03:00' }}
          aoAbrir={() => {}}
        />
      </>,
    );

    expect(screen.getByText('Motivo: retirada do estudo')).toBeTruthy();
    expect(screen.getByText('Realizada em ter, 24/11 às 09:40')).toBeTruthy();
  });

  it('sem atividades previstas não mostra a seção de atividades', async () => {
    await render(<CartaoVisita visita={{ ...base, atividades: [] }} aoAbrir={() => {}} />);

    expect(screen.queryByText('Atividades previstas')).toBeNull();
  });
});

describe('Selo e rótulos da situação da visita (T120, RQ-T120-13)', () => {
  it.each([
    ['AGENDADA', 'Agendada', 'info'],
    ['REALIZADA', 'Realizada', 'sucesso'],
    ['AUSENTE', 'Ausente', 'aviso'],
    ['CANCELADA', 'Cancelada', 'neutro'],
  ] as const)('%s tem rótulo "%s" e tom %s', async (situacao, rotulo, tom) => {
    expect(rotuloSituacaoVisita(situacao)).toBe(rotulo);
    expect(tomSituacaoVisita(situacao)).toBe(tom);

    await render(<Selo rotulo={rotuloSituacaoVisita(situacao)} tom={tomSituacaoVisita(situacao)} />);
    expect(screen.getByText(rotulo)).toBeTruthy();
  });

  it('situação desconhecida aparece como veio, sem quebrar', () => {
    expect(rotuloSituacaoVisita('NOVA')).toBe('NOVA');
    expect(tomSituacaoVisita('NOVA')).toBe('neutro');
  });

  it('o rótulo de acessibilidade inclui a data por extenso, o estudo e a situação', () => {
    expect(descricaoDaVisita({ ...base, situacao: 'AUSENTE' })).toBe(
      'Visita Retorno 30 dias, terça-feira, 24 de novembro às 09:30, estudo Estudo de pressão arterial, ausente',
    );
  });
});

describe('ResumoComparecimento (T120, RQ-T120-12 e 13)', () => {
  it('mostra "comparecimento: 3 de 4 visitas", o percentual e o rótulo de acessibilidade', async () => {
    await render(<ResumoComparecimento resumo={{ realizadas: 3, ausentes: 1, canceladas: 5 }} />);

    expect(screen.getByText('comparecimento: 3 de 4 visitas')).toBeTruthy();
    expect(screen.getByText('75%')).toBeTruthy();
    expect(screen.getByLabelText('Comparecimento: 3 de 4 visitas')).toBeTruthy();
  });

  it('sem visitas contabilizadas mostra o texto próprio e nenhum percentual', async () => {
    await render(<ResumoComparecimento resumo={{ realizadas: 0, ausentes: 0, canceladas: 4 }} />);

    expect(screen.getByText('Sem visitas contabilizadas ainda')).toBeTruthy();
    expect(screen.queryByText(/%/)).toBeNull();
  });
});
