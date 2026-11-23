import { requisitar } from './cliente';
import type { components } from './schema-sprint3';

export type MeuEstudo = components['schemas']['MeuEstudoResponse'];
export type MeuEstudoDetalhe = components['schemas']['MeuEstudoDetalheResponse'];

// Estudos do participante logado (T109). Lista vazia (200) não é erro: é o participante que ainda não foi incluído em
// nenhum estudo. O detalhe devolve 404 quando o estudo não existe ou não é da conta, sem distinguir os dois casos.
export const estudosApi = {
  listar: () => requisitar<MeuEstudo[]>('/api/me/estudos'),
  buscar: (estudoId: string) => requisitar<MeuEstudoDetalhe>(`/api/me/estudos/${encodeURIComponent(estudoId)}`),
};
