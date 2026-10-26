import { Tabs } from 'expo-router';
import { Text, type ColorValue } from 'react-native';
import { cores } from '@/components/tema';

function Icone({ simbolo, color }: { simbolo: string; color: ColorValue }) {
  return <Text style={{ fontSize: 20, color }}>{simbolo}</Text>;
}

// Abas do participante (docs/prototipos-mobile.md, seção 4.1): Início · Estudos · Visitas · Perfil
export default function TabsLayout() {
  return (
    <Tabs
      screenOptions={{
        tabBarActiveTintColor: cores.primaria,
        tabBarInactiveTintColor: cores.textoSuave,
        headerTitleStyle: { color: cores.texto },
      }}
    >
      <Tabs.Screen
        name="index"
        options={{ title: 'Início', tabBarIcon: ({ color }) => <Icone simbolo="⌂" color={color} /> }}
      />
      <Tabs.Screen
        name="estudos"
        options={{ title: 'Estudos', tabBarIcon: ({ color }) => <Icone simbolo="▤" color={color} /> }}
      />
      <Tabs.Screen
        name="visitas"
        options={{ title: 'Visitas', tabBarIcon: ({ color }) => <Icone simbolo="◷" color={color} /> }}
      />
      <Tabs.Screen
        name="perfil"
        options={{ title: 'Perfil', tabBarIcon: ({ color }) => <Icone simbolo="◉" color={color} /> }}
      />
    </Tabs>
  );
}
