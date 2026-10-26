import type { ReactNode } from 'react'

// Rótulo + controle + mensagem de erro de validação (local ou devolvida pela API)
export function Campo({ rotulo, erro, children }: { rotulo: string; erro?: string; children: ReactNode }) {
  return (
    <label className={erro ? 'campo campo--invalido' : 'campo'}>
      <span>{rotulo}</span>
      {children}
      {erro && <small className="campo__erro">{erro}</small>}
    </label>
  )
}
