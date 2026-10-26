import { Text } from 'react-native';
import { Botao, Cartao, estilos, Tela } from '@/components/ui';
import { useSessao } from '@/contexts/sessao';

// M11 Perfil — RF02 (encerrar sessão)
export default function PerfilScreen() {
  const { usuario, sair } = useSessao();

  return (
    <Tela titulo="Perfil">
      <Cartao>
        <Text style={estilos.texto}>{usuario?.nome}</Text>
        <Text style={estilos.textoSuave}>{usuario?.email}</Text>
        <Text style={estilos.etiqueta}>Participante</Text>
      </Cartao>
      <Botao rotulo="Sair" variante="perigo" onPress={sair} />
    </Tela>
  );
}
