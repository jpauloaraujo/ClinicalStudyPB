import type { TomSelo } from '@/components/Selo';

// Rótulos em português do estado do estudo e da situação da participação (T112, RQ-T112-01 e 05). Valor que o
// aplicativo ainda não conhece (backend mais novo) aparece como veio, em vez de quebrar a tela.

const ROTULO_STATUS: Record<string, string> = {
  PLANEJAMENTO: 'Planejamento',
  RECRUTAMENTO: 'Recrutamento',
  EXECUCAO: 'Em execução',
  ENCERRADO: 'Encerrado',
};

const TOM_STATUS: Record<string, TomSelo> = {
  PLANEJAMENTO: 'neutro',
  RECRUTAMENTO: 'info',
  EXECUCAO: 'sucesso',
  ENCERRADO: 'neutro',
};

const ROTULO_SITUACAO: Record<string, string> = {
  ATIVO: 'Ativo',
  RETIRADO: 'Retirado',
};

const TOM_SITUACAO: Record<string, TomSelo> = {
  ATIVO: 'sucesso',
  RETIRADO: 'aviso',
};

export function rotuloStatusEstudo(status: string): string {
  return ROTULO_STATUS[status] ?? status;
}

export function tomStatusEstudo(status: string): TomSelo {
  return TOM_STATUS[status] ?? 'neutro';
}

export function rotuloSituacaoParticipacao(situacao: string): string {
  return ROTULO_SITUACAO[situacao] ?? situacao;
}

export function tomSituacaoParticipacao(situacao: string): TomSelo {
  return TOM_SITUACAO[situacao] ?? 'neutro';
}

/**
 * Converte a data da API (`aaaa-mm-dd`) para `dd/MM/yyyy`. Divide o texto em vez de usar `new Date(...)`: uma data só
 * com dia é lida como UTC e, em fuso negativo, mostraria o dia anterior (mesma classe de erro do fuso da inclusão, #41).
 */
export function formatarData(data: string | null | undefined): string {
  if (!data) return '—';
  const partes = /^(\d{4})-(\d{2})-(\d{2})/.exec(data);
  if (!partes) return data;
  const [, ano, mes, dia] = partes;
  return `${dia}/${mes}/${ano}`;
}

/** Período do estudo: "01/10/2026 a 01/05/2027"; uma ponta ausente vira "—" e as duas ausentes, texto próprio. */
export function formatarPeriodo(inicio: string | null | undefined, fim: string | null | undefined): string {
  if (!inicio && !fim) return 'Período não informado';
  return `${formatarData(inicio)} a ${formatarData(fim)}`;
}
