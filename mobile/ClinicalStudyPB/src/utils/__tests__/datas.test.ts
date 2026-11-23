
import {
  DATA_INVALIDA,
  diaDaSemana,
  formatarData,
  formatarDataHora,
  formatarDataHoraPorExtenso,
} from '@/utils/datas';

describe('datas no fuso do aparelho (T117)', () => {
  it('data com deslocamento -03:00 vira "ter, 24/11 às 09:30"', () => {
    expect(formatarDataHora('2026-11-24T09:30:00-03:00')).toBe('ter, 24/11 às 09:30');
  });

  it('data com Z é convertida para o fuso do aparelho (UTC-3)', () => {
    expect(formatarDataHora('2026-11-24T12:30:00Z')).toBe('ter, 24/11 às 09:30');
  });

  it('a virada do dia no fuso do aparelho muda a data', () => {
    expect(formatarDataHora('2026-11-25T01:15:00Z')).toBe('ter, 24/11 às 22:15');
  });

  it('formatarData devolve dd/MM/aaaa', () => {
    expect(formatarData('2026-11-24T09:30:00-03:00')).toBe('24/11/2026');
    expect(formatarData('2026-11-24T02:00:00Z')).toBe('23/11/2026');
  });

  it('diaDaSemana é abreviado em português', () => {
    expect(diaDaSemana('2026-11-22T10:00:00-03:00')).toBe('dom');
    expect(diaDaSemana('2026-11-28T10:00:00-03:00')).toBe('sáb');
  });

  it('formato por extenso para o leitor de tela', () => {
    expect(formatarDataHoraPorExtenso('2026-11-24T09:30:00-03:00')).toBe('terça-feira, 24 de novembro às 09:30');
  });

  it.each([undefined, null, '', 'não é data'])('data inválida (%p) devolve "—" sem quebrar', (valor) => {
    expect(formatarDataHora(valor)).toBe(DATA_INVALIDA);
    expect(formatarData(valor)).toBe(DATA_INVALIDA);
    expect(diaDaSemana(valor)).toBe(DATA_INVALIDA);
    expect(formatarDataHoraPorExtenso(valor)).toBe('data não informada');
  });
});
