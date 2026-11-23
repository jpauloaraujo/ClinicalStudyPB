import { Stack } from 'expo-router';
import { StatusBar } from 'expo-status-bar';
import { ActivityIndicator, View } from 'react-native';
import { cores } from '@/components/tema';
import { SessaoProvider, useSessao } from '@/contexts/sessao';

// Telas fora das abas (detalhe do estudo e da visita): cabeçalho com título e botão voltar em português
function telaInterna(titulo: string) {
  return {
    headerShown: true,
    title: titulo,
    headerBackTitle: 'Voltar',
    headerTintColor: cores.primaria,
    headerTitleStyle: { color: cores.texto },
  };
}

// Navegação raiz (T12): login fora das abas; abas só com sessão de participante
function Navegacao() {
  const { usuario, carregando } = useSessao();

  if (carregando) {
    return (
      <View style={{ flex: 1, alignItems: 'center', justifyContent: 'center', backgroundColor: cores.fundo }}>
        <ActivityIndicator color={cores.primaria} />
      </View>
    );
  }

  return (
    <Stack screenOptions={{ headerShown: false }}>
      <Stack.Protected guard={!usuario}>
        <Stack.Screen name="login" />
      </Stack.Protected>
      <Stack.Protected guard={!!usuario}>
        <Stack.Screen name="(tabs)" />
        <Stack.Screen name="estudos/[estudoId]" options={telaInterna('Estudo')} />
        <Stack.Screen name="visitas/[visitaId]" options={telaInterna('Visita')} />
      </Stack.Protected>
    </Stack>
  );
}

export default function RootLayout() {
  return (
    <SessaoProvider>
      <StatusBar style="dark" />
      <Navegacao />
    </SessaoProvider>
  );
}
