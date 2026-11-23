import type { ResumoComparecimento } from '@/api/visitas';

export const TEXTO_SEM_VISITAS_CONTABILIZADAS = 'Sem visitas contabilizadas ainda';

export type Comparecimento = {
  /** Visitas a que o participante compareceu (realizadas). */
  compareceu: number;
  /** Visitas contabilizadas: realizadas mais ausentes. As canceladas não entram (não foram falta do participante). */
  contabilizadas: number;
  /** Percentual inteiro, ou null quando não há visita contabilizada (nunca divide por zero). */
  percentual: number | null;
  /** "comparecimento: 3 de 4 visitas" ou "Sem visitas contabilizadas ainda". */
  texto: string;
};

// Resumo do histórico (T118, RQ-T118-06 e 08): calculado a partir do `resumo` do servidor, que não depende da página
// carregada. 3 realizadas, 1 ausente e 5 canceladas resultam em "3 de 4".
export function calcularComparecimento(resumo: Pick<ResumoComparecimento, 'realizadas' | 'ausentes'>): Comparecimento {
  const compareceu = Math.max(0, resumo.realizadas);
  const contabilizadas = compareceu + Math.max(0, resumo.ausentes);
  if (contabilizadas === 0) {
    return { compareceu, contabilizadas, percentual: null, texto: TEXTO_SEM_VISITAS_CONTABILIZADAS };
  }
  return {
    compareceu,
    contabilizadas,
    percentual: Math.round((compareceu / contabilizadas) * 100),
    texto: `comparecimento: ${compareceu} de ${contabilizadas} visitas`,
  };
}
