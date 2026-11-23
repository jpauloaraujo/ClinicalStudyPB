import { useRouter } from 'expo-router';
import { useCallback, useEffect, useRef, useState } from 'react';
import { FlatList, Pressable, RefreshControl, StyleSheet, Text, View } from 'react-native';
import { mensagemDoErro } from '@/api/cliente';
import { estudosApi, type MeuEstudo } from '@/api/estudos';
import { Carregando } from '@/components/Carregando';
import { EstadoErro } from '@/components/EstadoErro';
import { EstadoVazio } from '@/components/EstadoVazio';
import { Selo } from '@/components/Selo';
import { cores, espaco, raio } from '@/components/tema';
import { estilos } from '@/components/ui';
import {
  formatarData,
  rotuloStatusEstudo,
  tomSituacaoParticipacao,
  tomStatusEstudo,
} from '@/utils/estudos';

export const TEXTO_SEM_ESTUDOS = 'Você ainda não está incluído em nenhum estudo.';

// Texto lido pelo leitor de tela; a informação nunca depende só do selo colorido (RQ-T112-09)
export function descricaoDoEstudo(estudo: MeuEstudo): string {
  const retirado = estudo.situacaoParticipacao === 'RETIRADO' ? ', retirado' : '';
  return `Estudo ${estudo.titulo}, ${rotuloStatusEstudo(estudo.statusEstudo).toLowerCase()}, incluído em ${formatarData(estudo.dataInclusao)}${retirado}`;
}

// M03 Meus estudos: lista de cartões dos estudos do participante, na ordem da API (inclusão mais recente primeiro)
export default function EstudosScreen() {
  const router = useRouter();
  const [estudos, setEstudos] = useState<MeuEstudo[] | null>(null);
  const [erro, setErro] = useState<string | null>(null);
  const [atualizando, setAtualizando] = useState(false);
  const ativo = useRef(true);

  // Só atualiza o estado em callbacks assíncronos e só enquanto a tela está montada (voltar durante a carga é seguro)
  const carregar = useCallback(() => {
    return estudosApi
      .listar()
      .then((lista) => {
        if (!ativo.current) return;
        setEstudos(lista);
        setErro(null);
      })
      .catch((falha) => {
        if (ativo.current) setErro(mensagemDoErro(falha));
      });
  }, []);

  useEffect(() => {
    ativo.current = true;
    carregar();
    return () => {
      ativo.current = false;
    };
  }, [carregar]);

  // O indicador sempre termina, também quando a atualização falha (RQ-T112-04)
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

  // O que ocupa o lugar da lista: carregando na primeira carga, erro se a primeira carga falhou, vazio se a lista veio
  // vazia. Com erro e a lista já carregada, o aviso fica no cabeçalho e aqui não entra nada.
  const estadoSemLista =
    estudos === null && erro === null ? (
      <Carregando />
    ) : estudos === null && erro !== null ? (
      <EstadoErro mensagem={erro} aoTentarDeNovo={tentarDeNovo} />
    ) : erro === null ? (
      <EstadoVazio titulo={TEXTO_SEM_ESTUDOS} />
    ) : undefined;

  return (
    <View style={estilos.tela}>
      <FlatList
        testID="lista-estudos"
        data={estudos ?? []}
        keyExtractor={(estudo) => estudo.participacaoId}
        contentContainerStyle={estilos.conteudo}
        refreshControl={<RefreshControl refreshing={atualizando} onRefresh={atualizar} />}
        ListHeaderComponent={
          <View style={local.cabecalho}>
            <Text style={estilos.titulo}>Meus estudos</Text>
            {/* Falha com a lista já na tela: o aviso fica no topo e os cartões continuam visíveis */}
            {erro && estudos !== null ? <EstadoErro mensagem={erro} aoTentarDeNovo={tentarDeNovo} /> : null}
          </View>
        }
        renderItem={({ item }) => (
          <CartaoDoEstudo estudo={item} aoAbrir={() => router.push({ pathname: '/estudos/[estudoId]', params: { estudoId: item.estudoId } })} />
        )}
        ListEmptyComponent={estadoSemLista}
      />
    </View>
  );
}

function CartaoDoEstudo({ estudo, aoAbrir }: { estudo: MeuEstudo; aoAbrir: () => void }) {
  const retirado = estudo.situacaoParticipacao === 'RETIRADO';
  return (
    <Pressable
      accessibilityRole="button"
      accessibilityLabel={descricaoDoEstudo(estudo)}
      onPress={aoAbrir}
      style={({ pressed }) => [local.cartao, pressed && { opacity: 0.7 }]}
    >
      <Text style={local.titulo}>{estudo.titulo}</Text>
      <View style={local.selos}>
        <Selo rotulo={rotuloStatusEstudo(estudo.statusEstudo)} tom={tomStatusEstudo(estudo.statusEstudo)} />
        {retirado ? <Selo rotulo="Retirado" tom={tomSituacaoParticipacao(estudo.situacaoParticipacao)} /> : null}
      </View>
      <Text style={estilos.textoSuave}>Incluído em {formatarData(estudo.dataInclusao)}</Text>
      <Text style={estilos.texto}>{estudo.resumo}</Text>
    </Pressable>
  );
}

const local = StyleSheet.create({
  cabecalho: { gap: espaco[3], marginBottom: espaco[2] },
  cartao: {
    minHeight: 44,
    backgroundColor: cores.superficie,
    borderColor: cores.borda,
    borderWidth: 1,
    borderRadius: raio,
    padding: espaco[4],
    gap: espaco[2],
  },
  titulo: { fontSize: 18, fontWeight: '700', color: cores.texto },
  selos: { flexDirection: 'row', flexWrap: 'wrap', gap: espaco[2] },
});
