import { StyleSheet, Text, View } from 'react-native';
import { cores, espaco } from './tema';

type Props = { titulo: string; texto?: string; icone?: string };

// Lista ou consulta sem resultados
export function EstadoVazio({ titulo, texto, icone = '○' }: Props) {
  return (
    <View style={estilos.caixa} accessibilityRole="summary">
      <Text style={estilos.icone} accessibilityElementsHidden importantForAccessibility="no">
        {icone}
      </Text>
      <Text style={estilos.titulo}>{titulo}</Text>
      {texto ? <Text style={estilos.texto}>{texto}</Text> : null}
    </View>
  );
}

const estilos = StyleSheet.create({
  caixa: { alignItems: 'center', gap: espaco[2], padding: espaco[6] },
  icone: { fontSize: 32, color: cores.textoSuave },
  titulo: { fontSize: 18, fontWeight: '600', color: cores.texto, textAlign: 'center' },
  texto: { fontSize: 16, color: cores.textoSuave, textAlign: 'center' },
});
