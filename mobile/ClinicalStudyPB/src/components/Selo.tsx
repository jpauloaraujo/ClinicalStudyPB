import { StyleSheet, Text, View } from 'react-native';
import { cores, espaco } from './tema';

export type TomSelo = 'neutro' | 'info' | 'sucesso' | 'aviso' | 'perigo';

const TONS: Record<TomSelo, { texto: string; fundo: string }> = {
  neutro: { texto: cores.textoSuave, fundo: cores.borda },
  info: { texto: cores.primaria, fundo: cores.primariaFundo },
  sucesso: { texto: cores.sucesso, fundo: cores.sucessoFundo },
  aviso: { texto: cores.aviso, fundo: cores.avisoFundo },
  perigo: { texto: cores.perigo, fundo: cores.perigoFundo },
};

// Rótulo curto em texto com cor: a informação nunca depende só da cor
export function Selo({ rotulo, tom = 'neutro' }: { rotulo: string; tom?: TomSelo }) {
  const { texto, fundo } = TONS[tom];
  return (
    <View style={[estilos.selo, { backgroundColor: fundo, borderColor: texto }]} accessibilityLabel={rotulo}>
      <Text style={[estilos.texto, { color: texto }]}>{rotulo}</Text>
    </View>
  );
}

const estilos = StyleSheet.create({
  selo: {
    alignSelf: 'flex-start',
    borderWidth: 1,
    borderRadius: 999,
    paddingHorizontal: espaco[3],
    paddingVertical: espaco[1],
  },
  texto: { fontSize: 13, fontWeight: '600' },
});
