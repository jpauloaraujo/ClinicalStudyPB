// Datas da API no fuso do aparelho, em português (T117, RQ-T117-08). Montado à mão com os getters locais de `Date`,
// sem `Intl` nem `toLocaleString`: o Hermes de alguns aparelhos não traz o idioma pt-BR e o resultado mudaria.

const DIAS_ABREVIADOS = ['dom', 'seg', 'ter', 'qua', 'qui', 'sex', 'sáb'];
const DIAS_POR_EXTENSO = [
  'domingo',
  'segunda-feira',
  'terça-feira',
  'quarta-feira',
  'quinta-feira',
  'sexta-feira',
  'sábado',
];
const MESES = [
  'janeiro',
  'fevereiro',
  'março',
  'abril',
  'maio',
  'junho',
  'julho',
  'agosto',
  'setembro',
  'outubro',
  'novembro',
  'dezembro',
];

export const DATA_INVALIDA = '—';

function converter(iso: string | null | undefined): Date | null {
  if (!iso) return null;
  const data = new Date(iso);
  return Number.isNaN(data.getTime()) ? null : data;
}

const dois = (n: number) => String(n).padStart(2, '0');

/** Dia da semana abreviado ("ter"), no fuso do aparelho; data inválida devolve "—". */
export function diaDaSemana(iso: string | null | undefined): string {
  const data = converter(iso);
  return data ? DIAS_ABREVIADOS[data.getDay()] : DATA_INVALIDA;
}

/** "24/11/2026", no fuso do aparelho. */
export function formatarData(iso: string | null | undefined): string {
  const data = converter(iso);
  if (!data) return DATA_INVALIDA;
  return `${dois(data.getDate())}/${dois(data.getMonth() + 1)}/${data.getFullYear()}`;
}

/** "ter, 24/11 às 09:30", no fuso do aparelho. */
export function formatarDataHora(iso: string | null | undefined): string {
  const data = converter(iso);
  if (!data) return DATA_INVALIDA;
  return `${DIAS_ABREVIADOS[data.getDay()]}, ${dois(data.getDate())}/${dois(data.getMonth() + 1)} às ${dois(data.getHours())}:${dois(data.getMinutes())}`;
}

/** "terça-feira, 24 de novembro às 09:30": a forma lida pelo leitor de tela (RQ-T117-10). */
export function formatarDataHoraPorExtenso(iso: string | null | undefined): string {
  const data = converter(iso);
  if (!data) return 'data não informada';
  return `${DIAS_POR_EXTENSO[data.getDay()]}, ${data.getDate()} de ${MESES[data.getMonth()]} às ${dois(data.getHours())}:${dois(data.getMinutes())}`;
}
