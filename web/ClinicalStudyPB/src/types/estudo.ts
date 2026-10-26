// Espelha o enum StatusEstudo do backend
export type StatusEstudo = 'PLANEJAMENTO' | 'RECRUTAMENTO' | 'EXECUCAO' | 'ENCERRADO'

export const statusEstudo: StatusEstudo[] = ['PLANEJAMENTO', 'RECRUTAMENTO', 'EXECUCAO', 'ENCERRADO']

export const rotulosStatus: Record<StatusEstudo, string> = {
  PLANEJAMENTO: 'Planejamento',
  RECRUTAMENTO: 'Recrutamento',
  EXECUCAO: 'Execução',
  ENCERRADO: 'Encerrado',
}

// Entidade Estudo devolvida pela API (o responsável vem sem a senha)
export type Estudo = {
  id: string
  titulo: string
  objetivo: string
  responsavel: { id: string; nome: string; email: string }
  dataInicio: string | null
  dataFim: string | null
  limiteParticipantes: number
  status: StatusEstudo
}

// EstudoRequest da API
export type EstudoDados = {
  titulo: string
  objetivo: string
  responsavelId: string
  dataInicio: string | null
  dataFim: string | null
  limiteParticipantes: number
  status: StatusEstudo
}
