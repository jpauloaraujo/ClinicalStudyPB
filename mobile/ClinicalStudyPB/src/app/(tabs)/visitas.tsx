import { useMemo } from 'react';
import { StyleSheet, Text, View } from 'react-native';
import { Cartao, EmBreve, Tela, estilos } from '@/components/ui';
import { cores, espaco, raio } from '@/components/tema';
import { proximasVisitasMock } from '@/mocks/visitasMock';
import type { VisitaParticipante } from '@/types/visitas';

function formatarDataHora(isoString: string): string {
  const data = new Date(isoString);
  if (Number.isNaN(data.getTime())) {
    return isoString;
  }

  return data.toLocaleString('pt-BR', {
    day: '2-digit',
    month: '2-digit',
    year: 'numeric',
    hour: '2-digit',
    minute: '2-digit',
  });
}

export default function VisitasScreen() {
  const proximasVisitas = useMemo(() => {
    const agoraMs = Date.now();

    return proximasVisitasMock
      .filter((visita: VisitaParticipante) => {
        const timestamp = new Date(visita.dataHora).getTime();
        return (
          visita.situacao === 'AGENDADA' &&
          !Number.isNaN(timestamp) &&
          timestamp >= agoraMs
        );
      })
      .sort(
        (a, b) => new Date(a.dataHora).getTime() - new Date(b.dataHora).getTime()
      );
  }, []);

  return (
    <Tela titulo="Visitas">
      <Text style={locais.secaoTitulo}>Próximas visitas</Text>

      {proximasVisitas.length === 0 ? (
        <Cartao>
          <Text style={estilos.textoSuave}>
            Nenhuma visita agendada a partir de hoje.
          </Text>
        </Cartao>
      ) : (
        proximasVisitas.map((visita) => (
          <Cartao key={visita.id}>
            <View style={locais.cabecalho}>
              <View style={locais.badge}>
                <Text style={estilos.etiqueta}>{visita.situacao}</Text>
              </View>
              <Text style={estilos.textoSuave}>
                {formatarDataHora(visita.dataHora)}
              </Text>
            </View>

            <Text style={locais.nomeVisita}>{visita.nomeVisita}</Text>
            <Text style={estilos.textoSuave}>{visita.estudo}</Text>

            <View style={locais.atividadesBox}>
              <Text style={locais.atividadesTitulo}>Atividades previstas</Text>
              {visita.atividadesPrevistas.map((atividade, indice) => (
                <Text key={`${visita.id}-atv-${indice}`} style={estilos.texto}>
                  • {atividade}
                </Text>
              ))}
            </View>
          </Cartao>
        ))
      )}

      <EmBreve
        requisito="RF19"
        texto="Você poderá confirmar presença ou pedir reagendamento de cada visita."
      />
    </Tela>
  );
}

const locais = StyleSheet.create({
  secaoTitulo: {
    fontSize: 18,
    fontWeight: '700',
    color: cores.texto,
  },
  cabecalho: {
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'space-between',
    gap: espaco[2],
  },
  badge: {
    backgroundColor: cores.primariaFundo,
    paddingHorizontal: espaco[2],
    paddingVertical: espaco[1],
    borderRadius: raio,
  },
  nomeVisita: {
    fontSize: 18,
    fontWeight: '700',
    color: cores.texto,
  },
  atividadesBox: {
    marginTop: espaco[1],
    paddingTop: espaco[2],
    borderTopWidth: 1,
    borderTopColor: cores.borda,
    gap: espaco[1],
  },
  atividadesTitulo: {
    fontSize: 14,
    fontWeight: '600',
    color: cores.texto,
  },
});
