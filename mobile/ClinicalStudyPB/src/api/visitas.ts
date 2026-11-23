import { requisitar } from './cliente';
import type { components } from './schema-sprint3';

export type MinhaVisita = components['schemas']['MinhaVisitaResponse'];
export type ResumoComparecimento = components['schemas']['ResumoComparecimentoResponse'];
export type HistoricoMinhasVisitas = components['schemas']['HistoricoMinhasVisitasResponse'];

export const TAMANHO_PAGINA_HISTORICO = 20;

export type FiltroHistorico = { estudoId?: string; pagina?: number; tamanho?: number };

function consulta(parametros: Record<string, string | number | undefined>): string {
  const partes = Object.entries(parametros)
    .filter(([, valor]) => valor !== undefined && valor !== '')
    .map(([chave, valor]) => `${chave}=${encodeURIComponent(String(valor))}`);
  return partes.length > 0 ? `?${partes.join('&')}` : '';
}

// Visitas do participante logado (T116). A conta vem do token: nenhuma chamada leva id de participante. `estudoId`
// de outro participante devolve lista vazia (200), não erro.
export const visitasApi = {
  proximas: (estudoId?: string) => requisitar<MinhaVisita[]>(`/api/me/visitas/proximas${consulta({ estudoId })}`),
  historico: ({ estudoId, pagina = 0, tamanho = TAMANHO_PAGINA_HISTORICO }: FiltroHistorico = {}) =>
    requisitar<HistoricoMinhasVisitas>(`/api/me/visitas/historico${consulta({ estudoId, pagina, tamanho })}`),
};
