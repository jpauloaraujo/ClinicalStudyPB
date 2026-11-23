import type { TomSelo } from '@/components/Selo';
import type { MinhaVisita } from '@/api/visitas';
import { formatarDataHoraPorExtenso } from './datas';

// Rótulos e tons da situação da visita (T117 e T118). Valor desconhecido aparece como veio, sem quebrar a tela.
const ROTULO: Record<string, string> = {
  AGENDADA: 'Agendada',
  REALIZADA: 'Realizada',
  AUSENTE: 'Ausente',
  CANCELADA: 'Cancelada',
};
const TOM: Record<string, TomSelo> = {
  AGENDADA: 'info',
  REALIZADA: 'sucesso',
  AUSENTE: 'aviso',
  CANCELADA: 'neutro',
};

export function rotuloSituacaoVisita(situacao: string): string {
  return ROTULO[situacao] ?? situacao;
}

export function tomSituacaoVisita(situacao: string): TomSelo {
  return TOM[situacao] ?? 'neutro';
}

/** Texto lido pelo leitor de tela; a situação nunca depende só da cor do selo (RQ-T117-10). */
export function descricaoDaVisita(visita: MinhaVisita): string {
  const partes = [
    `Visita ${visita.visitaNome}`,
    formatarDataHoraPorExtenso(visita.dataHora),
    `estudo ${visita.estudoTitulo}`,
    rotuloSituacaoVisita(visita.situacao).toLowerCase(),
  ];
  if (visita.situacao === 'CANCELADA' && visita.motivo) {
    partes.push(`motivo: ${visita.motivo}`);
  }
  return partes.join(', ');
}
