import { StyleSheet, Text, View } from 'react-native';
import type { ResumoComparecimento as Resumo } from '@/api/visitas';
import { calcularComparecimento } from '@/utils/comparecimento';
import { cores, espaco, raio } from './tema';

// Resumo no topo do histórico (T118): sempre com texto; o percentual é um complemento (RQ-T118-07 e 10)
export function ResumoComparecimento({ resumo }: { resumo: Resumo }) {
  const { texto, percentual } = calcularComparecimento(resumo);
  const rotulo = texto.charAt(0).toUpperCase() + texto.slice(1);
  return (
    <View style={local.caixa} accessible accessibilityLabel={rotulo} testID="resumo-comparecimento">
      <Text style={local.texto}>{texto}</Text>
      {percentual !== null ? <Text style={local.percentual}>{percentual}%</Text> : null}
    </View>
  );
}

const local = StyleSheet.create({
  caixa: {
    minHeight: 44,
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'space-between',
    gap: espaco[2],
    padding: espaco[4],
    borderRadius: raio,
    borderWidth: 1,
    borderColor: cores.borda,
    backgroundColor: cores.primariaFundo,
  },
  texto: { flexShrink: 1, fontSize: 16, fontWeight: '600', color: cores.texto },
  percentual: { fontSize: 18, fontWeight: '700', color: cores.primaria },
});
