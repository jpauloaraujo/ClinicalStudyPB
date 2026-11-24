import { createContext, useCallback, useContext, useEffect, useRef, useState, type ReactNode } from 'react';
import {
  autenticacaoApi,
  definirAoExpirarSessao,
  definirAoPerfilRecusado,
  ErroApi,
  tokenArmazenado,
  type UsuarioSessao,
} from '@/api/cliente';
import { CODIGO_PERFIL_NAO_PARTICIPANTE, MSG_PERFIL_NAO_PARTICIPANTE } from '@/api/erro';

export const MSG_SESSAO_EXPIRADA = 'Sua sessão expirou. Entre novamente.';

type Sessao = {
  usuario: UsuarioSessao | null;
  carregando: boolean;
  // Aviso exibido no login depois que uma sessão aberta expirou (RQ-T110-09); null nos demais casos
  aviso: string | null;
  entrar: (email: string, senha: string) => Promise<void>;
  sair: () => Promise<void>;
};

const SessaoContext = createContext<Sessao | null>(null);

// O app mobile atende somente o perfil Participante (matriz RBAC, docs/arquitetura.md)
export function SessaoProvider({ children }: { children: ReactNode }) {
  const [usuario, setUsuarioEstado] = useState<UsuarioSessao | null>(null);
  const [carregando, setCarregando] = useState(true);
  const [aviso, setAviso] = useState<string | null>(null);
  // O callback de expiração é registrado uma vez; o ref evita que ele enxergue um usuário antigo
  const usuarioRef = useRef<UsuarioSessao | null>(null);
  // Durante o login, um 401 de /api/auth/me é falha de credencial, não "sessão expirada"
  const entrandoRef = useRef(false);

  const definirUsuario = useCallback((dados: UsuarioSessao | null) => {
    usuarioRef.current = dados;
    setUsuarioEstado(dados);
  }, []);

  useEffect(() => {
    tokenArmazenado
      .ler()
      .then((token) => (token ? autenticacaoApi.usuarioAtual() : null))
      .then(async (dados) => {
        if (dados?.perfil === 'PARTICIPANTE') {
          definirUsuario(dados);
        } else if (dados) {
          await tokenArmazenado.remover();
        }
      })
      .catch(async (erro) => {
        // Só descarta o token se a API o rejeitou; com a API fora do ar ele é mantido
        // e a restauração é tentada de novo na próxima abertura
        const status = erro instanceof ErroApi ? erro.status : undefined;
        if (status === 401 || status === 403 || status === 404) {
          await tokenArmazenado.remover();
        }
      })
      .finally(() => setCarregando(false));
  }, [definirUsuario]);

  // 401 fora do login (token expirado ou inválido): limpa o token e a sessão; o Stack.Protected volta ao login.
  // O aviso só aparece se havia um usuário em sessão (na restauração com token inválido não há o que avisar)
  useEffect(() => {
    definirAoExpirarSessao(() => {
      if (entrandoRef.current) {
        return;
      }
      tokenArmazenado.remover().catch(() => undefined);
      if (usuarioRef.current) {
        setAviso(MSG_SESSAO_EXPIRADA);
      }
      definirUsuario(null);
    });
    return () => definirAoExpirarSessao(null);
  }, [definirUsuario]);

  // 403 em /api/me/* (RQ-T119-06): o perfil não é de participante; volta ao login com a mensagem do app exclusivo
  useEffect(() => {
    definirAoPerfilRecusado(() => {
      if (entrandoRef.current) {
        return;
      }
      tokenArmazenado.remover().catch(() => undefined);
      if (usuarioRef.current) {
        setAviso(MSG_PERFIL_NAO_PARTICIPANTE);
      }
      definirUsuario(null);
    });
    return () => definirAoPerfilRecusado(null);
  }, [definirUsuario]);

  const entrar = useCallback(
    async (email: string, senha: string) => {
      setAviso(null);
      entrandoRef.current = true;
      try {
        const { token } = await autenticacaoApi.entrar(email, senha);
        await tokenArmazenado.salvar(token);
        try {
          const dados = await autenticacaoApi.usuarioAtual();
          if (dados.perfil !== 'PARTICIPANTE') {
            throw new ErroApi(MSG_PERFIL_NAO_PARTICIPANTE, { codigo: CODIGO_PERFIL_NAO_PARTICIPANTE });
          }
          definirUsuario(dados);
        } catch (erro) {
          await tokenArmazenado.remover();
          throw erro;
        }
      } finally {
        entrandoRef.current = false;
      }
    },
    [definirUsuario],
  );

  // Logout stateless: o token é descartado no dispositivo. A sessão fecha mesmo se o armazenamento falhar
  const sair = useCallback(async () => {
    setAviso(null);
    try {
      await tokenArmazenado.remover();
    } finally {
      definirUsuario(null);
    }
  }, [definirUsuario]);

  return (
    <SessaoContext.Provider value={{ usuario, carregando, aviso, entrar, sair }}>{children}</SessaoContext.Provider>
  );
}

export function useSessao(): Sessao {
  const sessao = useContext(SessaoContext);
  if (!sessao) {
    throw new Error('useSessao deve ser usado dentro de <SessaoProvider>');
  }
  return sessao;
}
