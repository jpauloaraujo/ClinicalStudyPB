import { ActivityIndicator, StyleSheet, Text, View } from 'react-native';
import { cores, espaco } from './tema';

// Indicador de carregamento com texto acessível para leitores de tela
export function Carregando({ texto = 'Carregando…' }: { texto?: string }) {
  return (
    <View style={estilos.caixa} accessible accessibilityRole="progressbar" accessibilityLabel={texto}>
      <ActivityIndicator color={cores.primaria} />
      <Text style={estilos.texto}>{texto}</Text>
    </View>
  );
}

const estilos = StyleSheet.create({
  caixa: { alignItems: 'center', gap: espaco[2], padding: espaco[6] },
  texto: { fontSize: 16, color: cores.textoSuave },
});
