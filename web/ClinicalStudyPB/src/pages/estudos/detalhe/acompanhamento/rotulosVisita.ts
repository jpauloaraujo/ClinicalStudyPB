import type { SituacaoVisita } from '../../../../api/tipos'
import type { StatusEstudo } from '../../../../types/estudo'

// Rótulos de texto das situações da visita agendada. A T83 troca o texto pelo SeloSituacaoVisita (rótulo + cor).
export const situacoesVisita: SituacaoVisita[] = ['AGENDADA', 'REALIZADA', 'AUSENTE', 'CANCELADA']

export const rotulosSituacaoVisita: Record<SituacaoVisita, string> = {
  AGENDADA: 'Agendada',
  REALIZADA: 'Realizada',
  AUSENTE: 'Ausente',
  CANCELADA: 'Cancelada',
}

// Visita prevista que ainda não tem agendamento
export const ROTULO_NAO_AGENDADA = 'Não agendada'

// Só as visitas ainda AGENDADAS aceitam alteração de data; as demais já têm desfecho
export function podeReagendar(situacao: SituacaoVisita): boolean {
  return situacao === 'AGENDADA'
}

// Agendar e reagendar só valem com o estudo em recrutamento ou execução (regra 1, código
// ESTUDO_FORA_DE_RECRUTAMENTO_OU_EXECUCAO); a tela desabilita os botões e explica, e o servidor continua decidindo
export function estudoPermiteAgendar(status: StatusEstudo): boolean {
  return status === 'RECRUTAMENTO' || status === 'EXECUCAO'
}

export const EXPLICACAO_ESTUDO_FORA_DE_ESTADO =
  'O estudo não está em recrutamento nem em execução: não é possível agendar nem reagendar visitas.'
