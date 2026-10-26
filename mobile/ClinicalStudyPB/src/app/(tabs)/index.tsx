import { Link } from 'expo-router';
import { Text } from 'react-native';
import { Cartao, EmBreve, estilos, Tela } from '@/components/ui';
import { useSessao } from '@/contexts/sessao';

// M02 Início
export default function InicioScreen() {
  const { usuario } = useSessao();

  return (
    <Tela titulo={`Olá, ${usuario?.nome ?? ''}`}>
      <Text style={estilos.textoSuave}>Acompanhe aqui sua participação nos estudos clínicos.</Text>
      <EmBreve requisito="RF18" texto="O cartão com a sua próxima visita aparecerá aqui." />
      <Cartao>
        <Link href="/estudos" style={estilos.etiqueta}>
          Ver meus estudos →
        </Link>
        <Link href="/visitas" style={estilos.etiqueta}>
          Ver minhas visitas →
        </Link>
      </Cartao>
    </Tela>
  );
}
