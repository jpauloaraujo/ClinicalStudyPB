import { calcularComparecimento, TEXTO_SEM_VISITAS_CONTABILIZADAS } from '@/utils/comparecimento';

describe('resumo de comparecimento (T118, RQ-T118-06 e 08)', () => {
  it('3 realizadas, 1 ausente e 5 canceladas resultam em "3 de 4" (as canceladas não entram)', () => {
    const resumo = calcularComparecimento({ realizadas: 3, ausentes: 1 });

    expect(resumo.texto).toBe('comparecimento: 3 de 4 visitas');
    expect(resumo.compareceu).toBe(3);
    expect(resumo.contabilizadas).toBe(4);
    expect(resumo.percentual).toBe(75);
  });

  it('0 realizadas e 0 ausentes resultam em "sem visitas contabilizadas", sem dividir por zero', () => {
    const resumo = calcularComparecimento({ realizadas: 0, ausentes: 0 });

    expect(resumo.texto).toBe(TEXTO_SEM_VISITAS_CONTABILIZADAS);
    expect(resumo.texto).toBe('Sem visitas contabilizadas ainda');
    expect(resumo.percentual).toBeNull();
  });

  it('só ausentes: 0 de N, 0%', () => {
    const resumo = calcularComparecimento({ realizadas: 0, ausentes: 2 });

    expect(resumo.texto).toBe('comparecimento: 0 de 2 visitas');
    expect(resumo.percentual).toBe(0);
  });

  it('só realizadas: 100%', () => {
    expect(calcularComparecimento({ realizadas: 2, ausentes: 0 }).percentual).toBe(100);
  });

  it('arredonda o percentual para inteiro', () => {
    expect(calcularComparecimento({ realizadas: 1, ausentes: 2 }).percentual).toBe(33);
    expect(calcularComparecimento({ realizadas: 2, ausentes: 1 }).percentual).toBe(67);
  });
});
