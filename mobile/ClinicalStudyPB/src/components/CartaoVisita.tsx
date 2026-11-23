import { Pressable, StyleSheet, Text, View } from 'react-native';
import type { MinhaVisita } from '@/api/visitas';
import { formatarDataHora } from '@/utils/datas';
import { descricaoDaVisita, rotuloSituacaoVisita, tomSituacaoVisita } from '@/utils/visitas';
import { Selo } from './Selo';
import { cores, espaco, raio } from './tema';
import { estilos } from './ui';

type Props = { visita: MinhaVisita; aoAbrir: () => void };

// Cartão da visita, reutilizado nas próximas (T117) e no histórico (T118). Alvo de toque de pelo menos 44 pontos.
export function CartaoVisita({ visita, aoAbrir }: Props) {
  return (
    <Pressable
      accessibilityRole="button"
      accessibilityLabel={descricaoDaVisita(visita)}
      testID="cartao-visita"
      onPress={aoAbrir}
      style={({ pressed }) => [local.cartao, pressed && { opacity: 0.7 }]}
    >
      <View style={local.cabecalho}>
        <Text style={local.dataHora}>{formatarDataHora(visita.dataHora)}</Text>
        <Selo rotulo={rotuloSituacaoVisita(visita.situacao)} tom={tomSituacaoVisita(visita.situacao)} />
      </View>
      <Text style={local.nome}>{visita.visitaNome}</Text>
      <Text style={estilos.textoSuave}>{visita.estudoTitulo}</Text>
      {visita.situacao === 'REALIZADA' && visita.dataHoraRealizada ? (
        <Text style={estilos.texto}>Realizada em {formatarDataHora(visita.dataHoraRealizada)}</Text>
      ) : null}
      {visita.situacao === 'CANCELADA' && visita.motivo ? <Text style={estilos.texto}>Motivo: {visita.motivo}</Text> : null}
      {visita.atividades.length > 0 ? (
        <View style={local.atividades}>
          <Text style={local.atividadesTitulo}>Atividades previstas</Text>
          {visita.atividades.map((atividade, indice) => (
            <Text key={`${visita.id}-${indice}`} style={estilos.texto}>
              • {atividade}
            </Text>
          ))}
        </View>
      ) : null}
    </Pressable>
  );
}

const local = StyleSheet.create({
  cartao: {
    minHeight: 44,
    backgroundColor: cores.superficie,
    borderColor: cores.borda,
    borderWidth: 1,
    borderRadius: raio,
    padding: espaco[4],
    gap: espaco[2],
  },
  cabecalho: { flexDirection: 'row', alignItems: 'center', justifyContent: 'space-between', gap: espaco[2] },
  dataHora: { fontSize: 16, fontWeight: '600', color: cores.primaria },
  nome: { fontSize: 18, fontWeight: '700', color: cores.texto },
  atividades: { gap: espaco[1], paddingTop: espaco[2], borderTopWidth: 1, borderTopColor: cores.borda },
  atividadesTitulo: { fontSize: 14, fontWeight: '600', color: cores.texto },
});
