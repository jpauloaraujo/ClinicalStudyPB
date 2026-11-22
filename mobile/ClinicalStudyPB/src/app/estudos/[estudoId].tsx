import { useLocalSearchParams, useRouter } from 'expo-router';
import { useCallback, useEffect, useRef, useState } from 'react';
import { StyleSheet, Text, View } from 'react-native';
import { ErroApi, mensagemDoErro } from '@/api/cliente';
import { estudosApi, type MeuEstudoDetalhe } from '@/api/estudos';
import { Carregando } from '@/components/Carregando';
import { EstadoErro } from '@/components/EstadoErro';
import { Selo } from '@/components/Selo';
import { cores, espaco } from '@/components/tema';
import { Botao, Cartao, estilos, Tela } from '@/components/ui';
import {
  formatarData,
  formatarPeriodo,
  rotuloSituacaoParticipacao,
  rotuloStatusEstudo,
  tomSituacaoParticipacao,
  tomStatusEstudo,
} from '@/utils/estudos';

export const TEXTO_ESTUDO_NAO_ENCONTRADO = 'Estudo não encontrado.';

type Situacao =
  | { tipo: 'carregando' }
  | { tipo: 'nao-encontrado' }
  | { tipo: 'erro'; mensagem: string }
  | { tipo: 'pronto'; estudo: MeuEstudoDetalhe };

// M04 Detalhe do estudo. O cabeçalho "Estudo" e o botão Voltar vêm do _layout.tsx (T108). Estudo inexistente e estudo
// de outro participante dão o mesmo 404 no servidor, e a tela mostra o mesmo texto, sem travar o aplicativo.
export default function DetalheEstudoScreen() {
  const { estudoId } = useLocalSearchParams<{ estudoId: string }>();
  const router = useRouter();
  const [situacao, setSituacao] = useState<Situacao>({ tipo: 'carregando' });
  const ativo = useRef(true);

  // Só atualiza o estado em callbacks assíncronos e só com a tela montada
  const buscar = useCallback(() => {
    estudosApi
      .buscar(String(estudoId))
      .then((estudo) => {
        if (ativo.current) setSituacao({ tipo: 'pronto', estudo });
      })
      .catch((falha) => {
        if (!ativo.current) return;
        if (falha instanceof ErroApi && falha.status === 404) {
          setSituacao({ tipo: 'nao-encontrado' });
        } else {
          setSituacao({ tipo: 'erro', mensagem: mensagemDoErro(falha) });
        }
      });
  }, [estudoId]);

  useEffect(() => {
    ativo.current = true;
    buscar();
    return () => {
      ativo.current = false;
    };
  }, [buscar]);

  function tentarDeNovo() {
    setSituacao({ tipo: 'carregando' });
    buscar();
  }

  function voltar() {
    if (router.canGoBack()) {
      router.back();
    } else {
      router.replace('/estudos');
    }
  }

  if (situacao.tipo === 'carregando') {
    return (
      <Tela titulo="Estudo">
        <Carregando />
      </Tela>
    );
  }

  if (situacao.tipo === 'nao-encontrado') {
    return (
      <Tela titulo="Estudo">
        <Cartao>
          <Text style={estilos.texto}>{TEXTO_ESTUDO_NAO_ENCONTRADO}</Text>
          <Botao rotulo="Voltar" onPress={voltar} variante="secundario" />
        </Cartao>
      </Tela>
    );
  }

  if (situacao.tipo === 'erro') {
    return (
      <Tela titulo="Estudo">
        <EstadoErro mensagem={situacao.mensagem} aoTentarDeNovo={tentarDeNovo} />
      </Tela>
    );
  }

  const { estudo } = situacao;
  const retirado = estudo.situacaoParticipacao === 'RETIRADO';
  return (
    <Tela titulo={estudo.titulo}>
      <View style={local.selos}>
        <Selo rotulo={rotuloStatusEstudo(estudo.statusEstudo)} tom={tomStatusEstudo(estudo.statusEstudo)} />
        <Selo
          rotulo={rotuloSituacaoParticipacao(estudo.situacaoParticipacao)}
          tom={tomSituacaoParticipacao(estudo.situacaoParticipacao)}
        />
      </View>

      {retirado && estudo.retirada ? (
        <Cartao>
          <Text style={estilos.etiqueta}>Retirado</Text>
          <Text style={estilos.texto}>Retirado em {formatarData(estudo.retirada.dataRetirada)}</Text>
        </Cartao>
      ) : null}

      <Cartao>
        <Text style={local.rotulo}>Objetivo</Text>
        <Text style={estilos.texto}>{estudo.objetivo}</Text>
        <Text style={local.rotulo}>Responsável</Text>
        <Text style={estilos.texto}>{estudo.responsavelNome}</Text>
        <Text style={local.rotulo}>Período</Text>
        <Text style={estilos.texto}>{formatarPeriodo(estudo.dataInicio, estudo.dataFim)}</Text>
        <Text style={local.rotulo}>Incluído em</Text>
        <Text style={estilos.texto}>{formatarData(estudo.dataInclusao)}</Text>
      </Cartao>

      <Botao
        rotulo="Ver visitas deste estudo"
        onPress={() => router.push({ pathname: '/visitas', params: { estudoId: estudo.estudoId } })}
      />
    </Tela>
  );
}

const local = StyleSheet.create({
  selos: { flexDirection: 'row', flexWrap: 'wrap', gap: espaco[2] },
  rotulo: { fontSize: 13, fontWeight: '600', color: cores.textoSuave, marginTop: espaco[2] },
});
