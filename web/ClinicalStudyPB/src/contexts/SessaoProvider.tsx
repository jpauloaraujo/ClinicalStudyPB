import { useCallback, useEffect, useState, type ReactNode } from 'react'
import { definirAoExpirarSessao, lerToken, salvarToken } from '../services/api'
import { extrairErro } from '../services/erros'
import { autenticacaoApi } from '../services/recursos'
import { perfisWeb, type UsuarioSessao } from '../types/usuario'
import { PerfilSemAcessoWebError, SessaoContext, tokenExpirado } from './sessao'

export function SessaoProvider({ children }: { children: ReactNode }) {
  const [usuario, setUsuario] = useState<UsuarioSessao | null>(null)
  const [carregando, setCarregando] = useState(() => {
    const token = lerToken()
    return token !== null && !tokenExpirado(token)
  })

  const sair = useCallback(() => {
    salvarToken(null)
    setUsuario(null)
  }, [])

  // Restaura a sessão salva ao abrir a página
  useEffect(() => {
    const token = lerToken()
    if (!token || tokenExpirado(token)) {
      salvarToken(null)
      return
    }
    autenticacaoApi
      .usuarioAtual()
      .then((dados) => {
        if (perfisWeb.includes(dados.perfil)) {
          setUsuario(dados)
        } else {
          salvarToken(null)
        }
      })
      .catch((erro) => {
        // Só descarta o token se a API o rejeitou; com a API fora do ar ele é mantido
        // e a restauração é tentada de novo na próxima abertura
        const status = extrairErro(erro).status
        if (status === 401 || status === 403 || status === 404) {
          salvarToken(null)
        }
      })
      .finally(() => setCarregando(false))
  }, [])

  useEffect(() => {
    definirAoExpirarSessao(sair)
    return () => definirAoExpirarSessao(null)
  }, [sair])

  const entrar = useCallback(async (email: string, senha: string) => {
    salvarToken(await autenticacaoApi.entrar(email, senha))
    try {
      const dados = await autenticacaoApi.usuarioAtual()
      if (!perfisWeb.includes(dados.perfil)) {
        throw new PerfilSemAcessoWebError()
      }
      setUsuario(dados)
    } catch (erro) {
      salvarToken(null)
      throw erro
    }
  }, [])

  return (
    <SessaoContext.Provider value={{ usuario, carregando, entrar, sair }}>
      {children}
    </SessaoContext.Provider>
  )
}
