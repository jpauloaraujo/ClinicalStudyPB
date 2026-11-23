import { useLocalSearchParams, useRouter } from 'expo-router';
import { useCallback, useEffect, useRef, useState } from 'react';
import { FlatList, Pressable, RefreshControl, StyleSheet, Text, View } from 'react-native';
import { mensagemDoErro } from '@/api/cliente';
import { visitasApi, type MinhaVisita } from '@/api/visitas';
import { CartaoVisita } from '@/components/CartaoVisita';
import { Carregando } from '@/components/Carregando';
import { EstadoErro } from '@/components/EstadoErro';
import { EstadoVazio } from '@/components/EstadoVazio';
import { cores, espaco, raio } from '@/components/tema';
import { Botao, estilos } from '@/components/ui';

export const TEXTO_SEM_PROXIMAS = 'Nenhuma visita agendada.';

type Parte = 'proximas' | 'historico';

// M05 Visitas: seletor de duas partes. Próximas é a padrão; o histórico de comparecimento é a T118.
export default function VisitasScreen() {
  const { estudoId } = useLocalSearchParams<{ estudoId?: string }>();
  const [parte, setParte] = useState<Parte>('proximas');
  const filtro = estudoId ? estudoId : undefined;

  return (
    <View style={estilos.tela}>
      <View style={local.seletor} accessibilityRole="tablist">
        <Segmento id="proximas" rotulo="Próximas" ativo={parte === 'proximas'} aoTocar={() => setParte('proximas')} />
        <Segmento id="historico" rotulo="Histórico" ativo={parte === 'historico'} aoTocar={() => setParte('historico')} />
      </View>
      {parte === 'proximas' ? (
        <ListaProximas key={filtro ?? "todas"} estudoId={filtro} />
      ) : (
        <View style={estilos.conteudo}>
          <EstadoVazio titulo="O histórico de comparecimento chega na T118." />
        </View>
      )}
    </View>
  );
}

function Segmento({ id, rotulo, ativo, aoTocar }: { id: string; rotulo: string; ativo: boolean; aoTocar: () => void }) {
  return (
    <Pressable
      accessibilityRole="tab"
      accessibilityState={{ selected: ativo }}
      testID={`segmento-${id}`}
      onPress={aoTocar}
      style={[local.segmento, ativo && local.segmentoAtivo]}
    >
      <Text style={[local.segmentoTexto, ativo && local.segmentoTextoAtivo]}>{rotulo}</Text>
    </Pressable>
  );
}

// Próximas visitas do participante logado, na ordem da API (da mais próxima para a mais distante)
export function ListaProximas({ estudoId }: { estudoId?: string }) {
  const router = useRouter();
  const [visitas, setVisitas] = useState<MinhaVisita[] | null>(null);
  const [erro, setErro] = useState<string | null>(null);
  const [atualizando, setAtualizando] = useState(false);
  const ativo = useRef(true);

  const carregar = useCallback(() => {
    return visitasApi
      .proximas(estudoId)
      .then((lista) => {
        if (!ativo.current) return;
        setVisitas(lista);
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
    carregar();
  }, [carregar]);

  const abrir = (visita: MinhaVisita) =>
    router.push({ pathname: '/visitas/[visitaId]', params: { visitaId: visita.id, visita: JSON.stringify(visita) } });

  const estadoSemLista =
    visitas === null && erro === null ? (
      <Carregando />
    ) : visitas === null && erro !== null ? (
      <EstadoErro mensagem={erro} aoTentarDeNovo={tentarDeNovo} />
    ) : erro === null ? (
      <EstadoVazio titulo={TEXTO_SEM_PROXIMAS} />
    ) : undefined;

  return (
    <FlatList
      testID="lista-proximas"
      data={visitas ?? []}
      keyExtractor={(visita) => visita.id}
      contentContainerStyle={estilos.conteudo}
      refreshControl={<RefreshControl refreshing={atualizando} onRefresh={atualizar} />}
      ListHeaderComponent={
        <View style={local.cabecalho}>
          {estudoId ? (
            <View style={local.filtro}>
              <Text style={estilos.texto}>
                {visitas && visitas.length > 0 ? `Estudo: ${visitas[0].estudoTitulo}` : 'Filtrado por um estudo'}
              </Text>
              <Botao rotulo="Ver todas" variante="secundario" onPress={() => router.setParams({ estudoId: '' })} />
            </View>
          ) : null}
          {erro && visitas !== null ? <EstadoErro mensagem={erro} aoTentarDeNovo={tentarDeNovo} /> : null}
        </View>
      }
      renderItem={({ item }) => <CartaoVisita visita={item} aoAbrir={() => abrir(item)} />}
      ListEmptyComponent={estadoSemLista}
    />
  );
}

const local = StyleSheet.create({
  seletor: {
    flexDirection: 'row',
    margin: espaco[4],
    marginBottom: 0,
    borderWidth: 1,
    borderColor: cores.borda,
    borderRadius: raio,
    overflow: 'hidden',
    backgroundColor: cores.superficie,
  },
  segmento: { flex: 1, minHeight: 44, alignItems: 'center', justifyContent: 'center' },
  segmentoAtivo: { backgroundColor: cores.primariaFundo },
  segmentoTexto: { fontSize: 16, color: cores.textoSuave, fontWeight: '600' },
  segmentoTextoAtivo: { color: cores.primaria },
  cabecalho: { gap: espaco[3] },
  filtro: { gap: espaco[2] },
});
