import { useCallback, useEffect, useMemo, useState } from 'react';
import {
  ActivityIndicator,
  Pressable,
  StyleSheet,
  Text,
  View,
} from 'react-native';
import { Cartao, EmBreve, Tela, estilos } from '@/components/ui';
import { cores, espaco, raio } from '@/components/tema';
import {
  buscarHistoricoVisitasApi,
  buscarProximasVisitasApi,
} from '@/api/visitasApi';
import type { SituacaoVisita, VisitaParticipante } from '@/types/visitas';

const SITUACOES_HISTORICO: SituacaoVisita[] = [
  'REALIZADA',
  'AUSENTE',
  'CANCELADA',
];

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
  const [proximasBrutas, setProximasBrutas] = useState<VisitaParticipante[]>([]);
  const [historicoBruto, setHistoricoBruto] = useState<VisitaParticipante[]>([]);
  const [carregando, setCarregando] = useState<boolean>(true);
  const [erro, setErro] = useState<string | null>(null);

  const carregarVisitas = useCallback(async () => {
    setCarregando(true);
    setErro(null);

    try {
      const [proximasResp, historicoResp] = await Promise.all([
        buscarProximasVisitasApi(),
        buscarHistoricoVisitasApi(),
      ]);

      setProximasBrutas(proximasResp);
      setHistoricoBruto(historicoResp);
    } catch (err) {
      const mensagem =
        err instanceof Error && err.message
          ? err.message
          : 'Não foi possível carregar suas visitas no momento.';
      setErro(mensagem);
    } finally {
      setCarregando(false);
    }
  }, []);

  useEffect(() => {
    carregarVisitas();
  }, [carregarVisitas]);

  const proximasVisitas = useMemo(() => {
    const agoraMs = Date.now();

    return proximasBrutas
      .filter((visita) => {
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
  }, [proximasBrutas]);

  const historicoVisitas = useMemo(() => {
    return historicoBruto
      .filter((visita) => SITUACOES_HISTORICO.includes(visita.situacao))
      .sort(
        (a, b) => new Date(b.dataHora).getTime() - new Date(a.dataHora).getTime()
      );
  }, [historicoBruto]);

  return (
    <Tela titulo="Visitas">
      {carregando ? (
        <Cartao>
          <View style={locais.carregandoBox}>
            <ActivityIndicator size="small" color={cores.primaria} />
            <Text style={estilos.textoSuave}>Carregando visitas...</Text>
          </View>
        </Cartao>
      ) : erro ? (
        <Cartao>
          <Text style={locais.erroTexto}>{erro}</Text>
          <Pressable style={locais.botaoTentar} onPress={carregarVisitas}>
            <Text style={locais.botaoTentarTexto}>Tentar novamente</Text>
          </Pressable>
        </Cartao>
      ) : (
        <>
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
                  <Text style={locais.atividadesTitulo}>
                    Atividades previstas
                  </Text>
                  {visita.atividadesPrevistas.map((atividade, indice) => (
                    <Text
                      key={`${visita.id}-atv-${indice}`}
                      style={estilos.texto}
                    >
                      • {atividade}
                    </Text>
                  ))}
                </View>
              </Cartao>
            ))
          )}

          <Text style={locais.secaoTitulo}>Histórico de comparecimento</Text>

          {historicoVisitas.length === 0 ? (
            <Cartao>
              <Text style={estilos.textoSuave}>
                Nenhuma visita registrada no histórico até o momento.
              </Text>
            </Cartao>
          ) : (
            historicoVisitas.map((visita) => {
              const isCancelada = visita.situacao === 'CANCELADA';

              return (
                <Cartao key={visita.id}>
                  <View style={locais.cabecalho}>
                    <View
                      style={[
                        locais.badge,
                        isCancelada && locais.badgeCancelada,
                      ]}
                    >
                      <Text
                        style={[
                          estilos.etiqueta,
                          isCancelada && locais.etiquetaCancelada,
                        ]}
                      >
                        {visita.situacao}
                      </Text>
                    </View>
                    <Text style={estilos.textoSuave}>
                      {formatarDataHora(visita.dataHora)}
                    </Text>
                  </View>

                  <Text style={locais.nomeVisita}>{visita.nomeVisita}</Text>
                  <Text style={estilos.textoSuave}>{visita.estudo}</Text>

                  {visita.motivoCancelamento ? (
                    <View style={locais.motivoBox}>
                      <Text style={locais.motivoTitulo}>
                        Motivo do cancelamento
                      </Text>
                      <Text style={estilos.texto}>
                        {visita.motivoCancelamento}
                      </Text>
                    </View>
                  ) : null}

                  <View style={locais.atividadesBox}>
                    <Text style={locais.atividadesTitulo}>
                      Atividades previstas
                    </Text>
                    {visita.atividadesPrevistas.map((atividade, indice) => (
                      <Text
                        key={`${visita.id}-hist-atv-${indice}`}
                        style={estilos.texto}
                      >
                        • {atividade}
                      </Text>
                    ))}
                  </View>
                </Cartao>
              );
            })
          )}
        </>
      )}

      <EmBreve
        requisito="RF19"
        texto="Você poderá confirmar presença ou pedir reagendamento de cada visita."
      />
    </Tela>
  );
}

const locais = StyleSheet.create({
  carregandoBox: {
    flexDirection: 'row',
    alignItems: 'center',
    gap: espaco[2],
  },
  erroTexto: {
    fontSize: 14,
    color: cores.perigo,
    fontWeight: '600',
  },
  botaoTentar: {
    alignSelf: 'flex-start',
    marginTop: espaco[2],
    backgroundColor: cores.primaria,
    paddingHorizontal: espaco[3],
    paddingVertical: espaco[2],
    borderRadius: raio,
  },
  botaoTentarTexto: {
    color: '#FFFFFF',
    fontWeight: '600',
    fontSize: 14,
  },
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
  badgeCancelada: {
    backgroundColor: cores.perigoFundo,
  },
  etiquetaCancelada: {
    color: cores.perigo,
  },
  nomeVisita: {
    fontSize: 18,
    fontWeight: '700',
    color: cores.texto,
  },
  motivoBox: {
    backgroundColor: cores.perigoFundo,
    borderColor: cores.perigo,
    borderWidth: 1,
    borderRadius: raio,
    padding: espaco[3],
    gap: espaco[1],
  },
  motivoTitulo: {
    fontSize: 13,
    fontWeight: '700',
    color: cores.perigo,
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