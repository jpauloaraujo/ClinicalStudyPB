import { Link } from 'expo-router';
import { useCallback, useEffect, useState } from 'react';
import { Text } from 'react-native';
import { mensagemDoErro } from '@/api/cliente';
import { estudosApi } from '@/api/estudos';
import { Carregando } from '@/components/Carregando';
import { EstadoErro } from '@/components/EstadoErro';
import { EstadoVazio } from '@/components/EstadoVazio';
import { Cartao, EmBreve, estilos, Tela } from '@/components/ui';
import { useSessao } from '@/contexts/sessao';

export const AVISO_SEM_ESTUDO = 'Você ainda não está incluído em nenhum estudo.';

type Situacao = { tipo: 'carregando' } | { tipo: 'erro'; mensagem: string } | { tipo: 'pronto'; total: number };

// M02 Início
export default function InicioScreen() {
  const { usuario } = useSessao();
  const [situacao, setSituacao] = useState<Situacao>({ tipo: 'carregando' });

  // Só atualiza o estado em callbacks assíncronos; o estado inicial já é "carregando"
  const buscar = useCallback(() => {
    estudosApi
      .listar()
      .then((lista) => setSituacao({ tipo: 'pronto', total: lista.length }))
      .catch((falha) => setSituacao({ tipo: 'erro', mensagem: mensagemDoErro(falha) }));
  }, []);

  useEffect(buscar, [buscar]);

  function tentarDeNovo() {
    setSituacao({ tipo: 'carregando' });
    buscar();
  }

  return (
    <Tela titulo={`Olá, ${usuario?.nome ?? ''}`}>
      <Text style={estilos.textoSuave}>Acompanhe aqui sua participação nos estudos clínicos.</Text>
      {situacao.tipo === 'carregando' && <Carregando />}
      {situacao.tipo === 'erro' && <EstadoErro mensagem={situacao.mensagem} aoTentarDeNovo={tentarDeNovo} />}
      {situacao.tipo === 'pronto' && situacao.total === 0 && <EstadoVazio titulo={AVISO_SEM_ESTUDO} />}
      {situacao.tipo === 'pronto' && situacao.total > 0 && (
        <EmBreve requisito="RF18" texto="O cartão com a sua próxima visita aparecerá aqui." />
      )}
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
