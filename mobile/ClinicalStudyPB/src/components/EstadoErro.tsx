import { StyleSheet, Text, View } from 'react-native';
import { Botao } from './ui';
import { cores, espaco, raio } from './tema';

type Props = { mensagem: string; aoTentarDeNovo?: () => void };

// Falha ao carregar: mensagem da API e, quando houver, o botão para repetir a chamada
export function EstadoErro({ mensagem, aoTentarDeNovo }: Props) {
  return (
    <View style={estilos.caixa} accessibilityRole="alert">
      <Text style={estilos.mensagem}>{mensagem}</Text>
      {aoTentarDeNovo ? <Botao rotulo="Tentar de novo" onPress={aoTentarDeNovo} variante="secundario" /> : null}
    </View>
  );
}

const estilos = StyleSheet.create({
  caixa: {
    gap: espaco[3],
    padding: espaco[4],
    borderRadius: raio,
    borderWidth: 1,
    borderColor: cores.perigo,
    backgroundColor: cores.perigoFundo,
  },
  mensagem: { fontSize: 16, color: cores.perigo },
});
