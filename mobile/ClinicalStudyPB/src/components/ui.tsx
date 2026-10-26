import type { ReactNode } from 'react';
import { Pressable, ScrollView, StyleSheet, Text, View } from 'react-native';
import { cores, espaco, raio } from './tema';

export function Tela({ titulo, children }: { titulo: string; children: ReactNode }) {
  return (
    <ScrollView style={estilos.tela} contentContainerStyle={estilos.conteudo}>
      <Text style={estilos.titulo}>{titulo}</Text>
      {children}
    </ScrollView>
  );
}

export function Cartao({ children }: { children: ReactNode }) {
  return <View style={estilos.cartao}>{children}</View>;
}

type BotaoProps = {
  rotulo: string;
  onPress: () => void;
  variante?: 'primario' | 'secundario' | 'perigo';
  desabilitado?: boolean;
};

export function Botao({ rotulo, onPress, variante = 'primario', desabilitado }: BotaoProps) {
  return (
    <Pressable
      accessibilityRole="button"
      onPress={onPress}
      disabled={desabilitado}
      style={({ pressed }) => [
        estilos.botao,
        variante !== 'primario' && estilos.botaoContorno,
        variante === 'perigo' && { borderColor: cores.perigo },
        (pressed || desabilitado) && { opacity: 0.6 },
      ]}
    >
      <Text
        style={[
          estilos.botaoTexto,
          variante === 'secundario' && { color: cores.primaria },
          variante === 'perigo' && { color: cores.perigo },
        ]}
      >
        {rotulo}
      </Text>
    </Pressable>
  );
}

// Aviso usado nas telas cujo conteúdo depende de requisitos de sprints futuras
export function EmBreve({ requisito, texto }: { requisito: string; texto: string }) {
  return (
    <Cartao>
      <Text style={estilos.etiqueta}>{requisito} · próximas sprints</Text>
      <Text style={estilos.textoSuave}>{texto}</Text>
    </Cartao>
  );
}

export const estilos = StyleSheet.create({
  tela: { flex: 1, backgroundColor: cores.fundo },
  conteudo: { padding: espaco[4], gap: espaco[4] },
  titulo: { fontSize: 24, fontWeight: '700', color: cores.texto },
  cartao: {
    backgroundColor: cores.superficie,
    borderColor: cores.borda,
    borderWidth: 1,
    borderRadius: raio,
    padding: espaco[4],
    gap: espaco[2],
  },
  botao: {
    minHeight: 44,
    paddingHorizontal: espaco[4],
    borderRadius: raio,
    borderWidth: 1,
    borderColor: cores.primaria,
    backgroundColor: cores.primaria,
    alignItems: 'center',
    justifyContent: 'center',
  },
  botaoContorno: { backgroundColor: 'transparent' },
  botaoTexto: { color: '#fff', fontSize: 16, fontWeight: '600' },
  texto: { fontSize: 16, color: cores.texto },
  textoSuave: { fontSize: 16, color: cores.textoSuave },
  etiqueta: { fontSize: 13, fontWeight: '600', color: cores.primaria },
  campo: {
    minHeight: 44,
    borderWidth: 1,
    borderColor: cores.borda,
    borderRadius: raio,
    backgroundColor: cores.superficie,
    paddingHorizontal: espaco[3],
    fontSize: 16,
    color: cores.texto,
  },
  erro: {
    color: cores.perigo,
    backgroundColor: cores.perigoFundo,
    borderColor: cores.perigo,
    borderWidth: 1,
    borderRadius: raio,
    padding: espaco[3],
    fontSize: 15,
  },
});
