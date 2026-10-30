import { useOutletContext } from 'react-router'
import type { Estudo } from '../../../types/estudo'

// Dados que a página de detalhe repassa às abas pelo <Outlet>
export type EstudoDetalheContexto = {
  estudo: Estudo
  // Recarrega o estudo depois de uma alteração feita em uma aba (ex.: mudança de estado)
  recarregar: () => void
}

export function useEstudoDetalhe(): EstudoDetalheContexto {
  return useOutletContext<EstudoDetalheContexto>()
}
