import { useRouter } from 'expo-router';
import { useCallback, useEffect, useRef, useState } from 'react';
import { FlatList, RefreshControl, StyleSheet, Text, View } from 'react-native';
import { mensagemDoErro } from '@/api/cliente';
import { TAMANHO_PAGINA_HISTORICO, visitasApi, type HistoricoMinhasVisitas, type MinhaVisita } from '@/api/visitas';
import { Botao, estilos } from './ui';
import { CartaoVisita } from './CartaoVisita';
import { Carregando } from './Carregando';
import { EstadoErro } from './EstadoErro';
import { EstadoVazio } from './EstadoVazio';
import { ResumoComparecimento } from './ResumoComparecimento';
import { espaco } from './tema';

export const TEXTO_SEM_HISTORICO = 'Nenhuma visita no histórico ainda.';

// Junta a próxima página sem repetir visitas (a lista pode ter mudado entre uma chamada e outra)
export function juntarPaginas(atuais: MinhaVisita[], novas: MinhaVisita[]): MinhaVisita[] {
  const ids = new Set(atuais.map((visita) => visita.id));
  return [...atuais, ...novas.filter((visita) => !ids.has(visita.id))];
}

// Histórico de comparecimento (M06): realizadas, ausentes e canceladas, da mais recente para a mais antiga, paginado
export function ListaHistoricoVisitas({ estudoId }: { estudoId?: string }) {
  const router = useRouter();
  const [itens, setItens] = useState<MinhaVisita[] | null>(null);
  const [resumo, setResumo] = useState<HistoricoMinhasVisitas['resumo'] | null>(null);
  const [pagina, setPagina] = useState(0);
  const [totalPaginas, setTotalPaginas] = useState(0);
  const [erro, setErro] = useState<string | null>(null);
  const [atualizando, setAtualizando] = useState(false);
  const [carregandoMais, setCarregandoMais] = useState(false);
  const ativo = useRef(true);

  // Primeira página: substitui o que havia (carga inicial e pull-to-refresh recarregam do início)
  const carregar = useCallback(() => {
    return visitasApi
      .historico({ estudoId, pagina: 0, tamanho: TAMANHO_PAGINA_HISTORICO })
      .then((resposta) => {
        if (!ativo.current) return;
        setItens(resposta.conteudo);
        setResumo(resposta.resumo);
        setPagina(resposta.pagina);
        setTotalPaginas(resposta.totalPaginas);
        setErro(null);
      })
      .catch((falha) => {
        if (ativo.current) setErro(mensagemDoErro(falha));
      });
  }, [estudoId]);

  useEffect(() => {
    ativo.current = true;
    carregar();
    return () => {
      ativo.current = false;
    };
  }, [carregar]);

  const atualizar = useCallback(() => {
    setAtualizando(true);
    carregar().finally(() => {
      if (ativo.current) setAtualizando(false);
    });
  }, [carregar]);

  const tentarDeNovo = useCallback(() => {
    setErro(null);
    carregar();
  }, [carregar]);

  const carregarMais = useCallback(() => {
    if (carregandoMais) return;
    setCarregandoMais(true);
    visitasApi
      .historico({ estudoId, pagina: pagina + 1, tamanho: TAMANHO_PAGINA_HISTORICO })
      .then((resposta) => {
        if (!ativo.current) return;
        setItens((atuais) => juntarPaginas(atuais ?? [], resposta.conteudo));
        setResumo(resposta.resumo);
        setPagina(resposta.pagina);
        setTotalPaginas(resposta.totalPaginas);
        setErro(null);
      })
      .catch((falha) => {
        if (ativo.current) setErro(mensagemDoErro(falha));
      })
      .finally(() => {
        if (ativo.current) setCarregandoMais(false);
      });
  }, [carregandoMais, estudoId, pagina]);

  const abrir = (visita: MinhaVisita) =>
    router.push({ pathname: '/visitas/[visitaId]', params: { visitaId: visita.id, visita: JSON.stringify(visita) } });

  const temMais = itens !== null && pagina + 1 < totalPaginas;

  const estadoSemLista =
    itens === null && erro === null ? (
      <Carregando />
    ) : itens === null && erro !== null ? (
      <EstadoErro mensagem={erro} aoTentarDeNovo={tentarDeNovo} />
    ) : erro === null ? (
      <EstadoVazio titulo={TEXTO_SEM_HISTORICO} />
    ) : undefined;

  return (
    <FlatList
      testID="lista-historico"
      data={itens ?? []}
      keyExtractor={(visita) => visita.id}
      contentContainerStyle={estilos.conteudo}
      refreshControl={<RefreshControl refreshing={atualizando} onRefresh={atualizar} />}
      ListHeaderComponent={
        <View style={local.cabecalho}>
          {resumo ? <ResumoComparecimento resumo={resumo} /> : null}
          {erro && itens !== null ? <EstadoErro mensagem={erro} aoTentarDeNovo={temMais ? carregarMais : tentarDeNovo} /> : null}
        </View>
      }
      renderItem={({ item }) => <CartaoVisita visita={item} aoAbrir={() => abrir(item)} />}
      ListEmptyComponent={estadoSemLista}
      ListFooterComponent={
        carregandoMais ? (
          <Text style={estilos.textoSuave} accessibilityRole="progressbar">
            Carregando mais…
          </Text>
        ) : temMais ? (
          <Botao rotulo="Carregar mais" variante="secundario" onPress={carregarMais} testID="carregar-mais" />
        ) : undefined
      }
    />
  );
}

const local = StyleSheet.create({
  cabecalho: { gap: espaco[3] },
});
