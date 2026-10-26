import { Stack } from 'expo-router';
import { StatusBar } from 'expo-status-bar';
import { ActivityIndicator, View } from 'react-native';
import { cores } from '@/components/tema';
import { SessaoProvider, useSessao } from '@/contexts/sessao';

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
