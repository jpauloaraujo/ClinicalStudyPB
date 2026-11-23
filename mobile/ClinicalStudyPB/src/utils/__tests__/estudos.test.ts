import {
  formatarData,
  formatarPeriodo,
  rotuloSituacaoParticipacao,
  rotuloStatusEstudo,
  tomSituacaoParticipacao,
  tomStatusEstudo,
} from '@/utils/estudos';

describe('utils/estudos (T112, RQ-T112-01, 05 e 10)', () => {
  it.each([
    ['PLANEJAMENTO', 'Planejamento', 'neutro'],
    ['RECRUTAMENTO', 'Recrutamento', 'info'],
    ['EXECUCAO', 'Em execução', 'sucesso'],
    ['ENCERRADO', 'Encerrado', 'neutro'],
  ])('status %s vira "%s" no tom %s', (status, rotulo, tom) => {
    expect(rotuloStatusEstudo(status)).toBe(rotulo);
    expect(tomStatusEstudo(status)).toBe(tom);
  });

  it('status desconhecido aparece como veio, em tom neutro, sem quebrar', () => {
    expect(rotuloStatusEstudo('SUSPENSO')).toBe('SUSPENSO');
    expect(tomStatusEstudo('SUSPENSO')).toBe('neutro');
  });

  it.each([
    ['ATIVO', 'Ativo', 'sucesso'],
    ['RETIRADO', 'Retirado', 'aviso'],
  ])('situação %s vira "%s" no tom %s', (situacao, rotulo, tom) => {
    expect(rotuloSituacaoParticipacao(situacao)).toBe(rotulo);
    expect(tomSituacaoParticipacao(situacao)).toBe(tom);
  });

  it('situação desconhecida aparece como veio', () => {
    expect(rotuloSituacaoParticipacao('SUSPENSO')).toBe('SUSPENSO');
    expect(tomSituacaoParticipacao('SUSPENSO')).toBe('neutro');
  });

  describe('formatarData', () => {
    it('converte aaaa-mm-dd para dd/MM/yyyy mantendo os zeros à esquerda', () => {
      expect(formatarData('2026-10-30')).toBe('30/10/2026');
      expect(formatarData('2026-01-05')).toBe('05/01/2026');
    });

    it('ausente vira "—" e texto fora do formato volta como veio', () => {
      expect(formatarData(null)).toBe('—');
      expect(formatarData(undefined)).toBe('—');
      expect(formatarData('')).toBe('—');
      expect(formatarData('30/10/2026')).toBe('30/10/2026');
    });

    it('aceita data com hora e não depende do fuso do aparelho (nada de new Date)', () => {
      expect(formatarData('2026-10-30T23:30:00-03:00')).toBe('30/10/2026');
      expect(formatarData('2026-12-31')).toBe('31/12/2026');
      expect(formatarData('2026-01-01')).toBe('01/01/2026');
    });
  });

  describe('formatarPeriodo', () => {
    it('período completo', () => {
      expect(formatarPeriodo('2026-10-01', '2027-05-01')).toBe('01/10/2026 a 01/05/2027');
    });

    it('só o início ou só o fim mostram "—" na ponta que falta', () => {
      expect(formatarPeriodo('2026-10-01', null)).toBe('01/10/2026 a —');
      expect(formatarPeriodo(undefined, '2027-05-01')).toBe('— a 01/05/2027');
    });

    it('sem nenhuma das duas datas mostra texto próprio', () => {
      expect(formatarPeriodo(null, null)).toBe('Período não informado');
      expect(formatarPeriodo(undefined, undefined)).toBe('Período não informado');
    });
  });
});
