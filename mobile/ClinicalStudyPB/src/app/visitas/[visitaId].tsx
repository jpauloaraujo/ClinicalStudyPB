import { useLocalSearchParams, useRouter } from 'expo-router';
import { StyleSheet, Text, View } from 'react-native';
import type { MinhaVisita } from '@/api/visitas';
import { EstadoVazio } from '@/components/EstadoVazio';
import { Selo } from '@/components/Selo';
import { cores, espaco } from '@/components/tema';
import { Botao, Cartao, Tela, estilos } from '@/components/ui';
import { formatarDataHora } from '@/utils/datas';
import { rotuloSituacaoVisita, tomSituacaoVisita } from '@/utils/visitas';

export const TEXTO_VISITA_NAO_ENCONTRADA = 'Visita não encontrada.';

// Lê a visita passada pela navegação (o cartão); link direto, sem dados ou dados ilegíveis resultam em null
export function lerVisita(bruto: string | string[] | undefined): MinhaVisita | null {
  const texto = Array.isArray(bruto) ? bruto[0] : bruto;
  if (!texto) return null;
  try {
    const dados = JSON.parse(texto) as Partial<MinhaVisita>;
    return dados && typeof dados.id === 'string' && typeof dados.visitaNome === 'string' ? (dados as MinhaVisita) : null;
  } catch {
    return null;
  }
}

// M07 Detalhe da visita: dados do cartão, sem endpoint novo e sem as ações do RF19 (Sprint 4) nem resultados
export default function DetalheVisitaScreen() {
  const router = useRouter();
  const { visita: bruto } = useLocalSearchParams<{ visita?: string }>();
  const visita = lerVisita(bruto);

  const voltar = () => (router.canGoBack() ? router.back() : router.replace('/visitas'));

  if (!visita) {
    return (
      <Tela titulo="Visita">
        <EstadoVazio titulo={TEXTO_VISITA_NAO_ENCONTRADA} />
        <Botao rotulo="Voltar" onPress={voltar} variante="secundario" />
      </Tela>
    );
  }

  return (
    <Tela titulo={visita.visitaNome}>
      <Cartao>
        <Selo rotulo={rotuloSituacaoVisita(visita.situacao)} tom={tomSituacaoVisita(visita.situacao)} />
        <Campo rotulo="Data e hora" valor={formatarDataHora(visita.dataHora)} />
        <Campo rotulo="Estudo" valor={visita.estudoTitulo} />
        {visita.situacao === 'CANCELADA' && visita.motivo ? <Campo rotulo="Motivo" valor={visita.motivo} /> : null}
        <View style={local.atividades}>
          <Text style={local.rotulo}>Atividades previstas</Text>
          {visita.atividades.length > 0 ? (
            visita.atividades.map((atividade, indice) => (
              <Text key={`${indice}-${atividade}`} style={estilos.texto}>
                • {atividade}
              </Text>
            ))
          ) : (
            <Text style={estilos.textoSuave}>Nenhuma atividade prevista.</Text>
          )}
        </View>
      </Cartao>
      <Botao rotulo="Voltar" onPress={voltar} variante="secundario" />
    </Tela>
  );
}

function Campo({ rotulo, valor }: { rotulo: string; valor: string }) {
  return (
    <View style={local.campo} accessible accessibilityLabel={`${rotulo}: ${valor}`}>
      <Text style={local.rotulo}>{rotulo}</Text>
      <Text style={estilos.texto}>{valor}</Text>
    </View>
  );
}

const local = StyleSheet.create({
  campo: { gap: espaco[1] },
  atividades: { gap: espaco[1] },
  rotulo: { fontSize: 14, fontWeight: '600', color: cores.textoSuave },
});
