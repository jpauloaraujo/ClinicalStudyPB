import { createContext, useCallback, useContext, useEffect, useState, type ReactNode } from 'react';
import { autenticacaoApi, ErroApi, tokenArmazenado, type UsuarioSessao } from '@/api/cliente';

type Sessao = {
  usuario: UsuarioSessao | null;
  carregando: boolean;
  entrar: (email: string, senha: string) => Promise<void>;
  sair: () => Promise<void>;
};

const SessaoContext = createContext<Sessao | null>(null);

// O app mobile atende somente o perfil Participante (matriz RBAC, docs/arquitetura.md)
export function SessaoProvider({ children }: { children: ReactNode }) {
  const [usuario, setUsuario] = useState<UsuarioSessao | null>(null);
  const [carregando, setCarregando] = useState(true);

  useEffect(() => {
    tokenArmazenado
      .ler()
      .then((token) => (token ? autenticacaoApi.usuarioAtual() : null))
      .then((dados) => setUsuario(dados?.perfil === 'PARTICIPANTE' ? dados : null))
      .catch(() => tokenArmazenado.remover())
      .finally(() => setCarregando(false));
  }, []);

  const entrar = useCallback(async (email: string, senha: string) => {
    const { token } = await autenticacaoApi.entrar(email, senha);
    await tokenArmazenado.salvar(token);
    try {
      const dados = await autenticacaoApi.usuarioAtual();
      if (dados.perfil !== 'PARTICIPANTE') {
        throw new ErroApi('Este aplicativo é exclusivo para participantes. Use a aplicação web.');
      }
      setUsuario(dados);
    } catch (erro) {
      await tokenArmazenado.remover();
      throw erro;
    }
  }, []);

  // Logout stateless: o token é descartado no dispositivo
  const sair = useCallback(async () => {
    await tokenArmazenado.remover();
    setUsuario(null);
  }, []);

  return (
    <SessaoContext.Provider value={{ usuario, carregando, entrar, sair }}>{children}</SessaoContext.Provider>
  );
}

export function useSessao(): Sessao {
  const sessao = useContext(SessaoContext);
  if (!sessao) {
    throw new Error('useSessao deve ser usado dentro de <SessaoProvider>');
  }
  return sessao;
}
