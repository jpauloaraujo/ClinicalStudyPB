import { requisitar } from './cliente';
import type { components } from './schema-sprint3';

export type MeuEstudo = components['schemas']['MeuEstudoResponse'];

// Estudos do participante logado (GET /api/me/estudos, T109). Lista vazia (200) não é erro: é o participante
// que ainda não foi incluído em nenhum estudo. A T112 acrescenta aqui o detalhe do estudo.
export const estudosApi = {
  listar: () => requisitar<MeuEstudo[]>('/api/me/estudos'),
};
