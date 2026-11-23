import type { SituacaoVisita } from '../../../../api/tipos'
import type { StatusEstudo } from '../../../../types/estudo'

// Os rótulos e o selo das situações da visita ficam em situacaoVisita.ts e SeloSituacaoVisita.tsx (T83)
export const situacoesVisita: SituacaoVisita[] = ['AGENDADA', 'REALIZADA', 'AUSENTE', 'CANCELADA']

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
