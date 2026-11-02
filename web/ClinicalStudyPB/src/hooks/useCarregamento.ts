import { useCallback, useEffect, useState } from 'react'
import { extrairErro } from '../services/erros'

type Estado<T> = {
  dados: T | null
  erro: string | null
  // Status HTTP do erro, para a tela distinguir, por exemplo, registro inexistente (404)
  statusErro: number | null
  carregando: boolean
}

// Carrega dados da API ao montar a tela e permite recarregar após uma alteração
export function useCarregamento<T>(carregar: () => Promise<T>) {
  const [estado, setEstado] = useState<Estado<T>>({ dados: null, erro: null, statusErro: null, carregando: true })
  const [versao, setVersao] = useState(0)

  useEffect(() => {
    let ativo = true
    carregar()
      .then((dados) => ativo && setEstado({ dados, erro: null, statusErro: null, carregando: false }))
      .catch((falha) => {
        if (!ativo) return
        const { mensagem, status } = extrairErro(falha)
        setEstado({ dados: null, erro: mensagem, statusErro: status ?? null, carregando: false })
      })
    return () => {
      ativo = false
    }
  }, [carregar, versao])

  const recarregar = useCallback(() => setVersao((v) => v + 1), [])

  return { ...estado, recarregar }
}
