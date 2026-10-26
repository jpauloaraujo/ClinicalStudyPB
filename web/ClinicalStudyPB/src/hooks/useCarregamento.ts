import { useCallback, useEffect, useState } from 'react'
import { extrairErro } from '../services/erros'

type Estado<T> = {
  dados: T | null
  erro: string | null
  carregando: boolean
}

// Carrega dados da API ao montar a tela e permite recarregar após uma alteração
export function useCarregamento<T>(carregar: () => Promise<T>) {
  const [estado, setEstado] = useState<Estado<T>>({ dados: null, erro: null, carregando: true })
  const [versao, setVersao] = useState(0)

  useEffect(() => {
    let ativo = true
    carregar()
      .then((dados) => ativo && setEstado({ dados, erro: null, carregando: false }))
      .catch((falha) => ativo && setEstado({ dados: null, erro: extrairErro(falha).mensagem, carregando: false }))
    return () => {
      ativo = false
    }
  }, [carregar, versao])

  const recarregar = useCallback(() => setVersao((v) => v + 1), [])

  return { ...estado, recarregar }
}
