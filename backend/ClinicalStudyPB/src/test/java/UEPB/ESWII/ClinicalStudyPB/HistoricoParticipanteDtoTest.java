package UEPB.ESWII.ClinicalStudyPB;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import UEPB.ESWII.ClinicalStudyPB.controller.dto.HistoricoParticipanteResponse.ParticipanteDoHistorico;
import UEPB.ESWII.ClinicalStudyPB.controller.dto.HistoricoParticipanteResponse;
import UEPB.ESWII.ClinicalStudyPB.controller.dto.HistoricoParticipanteResponse.ResumoDoHistorico;
import UEPB.ESWII.ClinicalStudyPB.controller.dto.HistoricoParticipanteResponse.VisitaDoHistorico;
import UEPB.ESWII.ClinicalStudyPB.domain.enums.SituacaoNoHistorico;
import UEPB.ESWII.ClinicalStudyPB.domain.enums.SituacaoVisita;

/** T95: vocabulário de situações e DTOs de leitura do histórico (sem Spring, sem banco). */
class HistoricoParticipanteDtoTest {

    private static VisitaDoHistorico visita(int ordem, SituacaoNoHistorico situacao) {
        boolean agendada = situacao != SituacaoNoHistorico.NAO_AGENDADA;
        return new VisitaDoHistorico(UUID.randomUUID(), "Visita " + ordem, ordem, ordem * 30, 3,
                LocalDate.of(2026, 10, 20).plusDays(ordem * 30L), situacao, agendada ? UUID.randomUUID() : null,
                agendada ? OffsetDateTime.of(2026, 10, 20, 9, 0, 0, 0, ZoneOffset.ofHours(-3)) : null,
                null, null, null, List.of());
    }

    @Test
    @DisplayName("Cada situação de visita agendada vira a de mesmo nome e a ausência de agendamento vira NAO_AGENDADA")
    void conversaoDaSituacaoDaVisita() {
        for (SituacaoVisita situacao : SituacaoVisita.values()) {
            assertThat(SituacaoNoHistorico.de(situacao).name()).isEqualTo(situacao.name());
        }
        assertThat(SituacaoNoHistorico.de(null)).isEqualTo(SituacaoNoHistorico.NAO_AGENDADA);
    }

    @Test
    @DisplayName("O vocabulário do histórico é o da visita agendada mais NAO_AGENDADA")
    void vocabularioDoHistorico() {
        assertThat(SituacaoNoHistorico.values()).extracting(Enum::name)
                .containsExactly("NAO_AGENDADA", "AGENDADA", "REALIZADA", "AUSENTE", "CANCELADA");
    }

    @Test
    @DisplayName("Pendente é a visita não agendada ou ainda agendada; as demais já têm desfecho")
    void pendencia() {
        assertThat(SituacaoNoHistorico.NAO_AGENDADA.estaPendente()).isTrue();
        assertThat(SituacaoNoHistorico.AGENDADA.estaPendente()).isTrue();
        assertThat(SituacaoNoHistorico.REALIZADA.estaPendente()).isFalse();
        assertThat(SituacaoNoHistorico.AUSENTE.estaPendente()).isFalse();
        assertThat(SituacaoNoHistorico.CANCELADA.estaPendente()).isFalse();
    }

    @Test
    @DisplayName("O resumo conta cada situação e mantém pendentes = agendadas + naoAgendadas")
    void resumoConta() {
        ResumoDoHistorico resumo = ResumoDoHistorico.de(List.of(visita(1, SituacaoNoHistorico.REALIZADA),
                visita(2, SituacaoNoHistorico.AGENDADA), visita(3, SituacaoNoHistorico.NAO_AGENDADA),
                visita(4, SituacaoNoHistorico.AUSENTE), visita(5, SituacaoNoHistorico.CANCELADA),
                visita(6, SituacaoNoHistorico.NAO_AGENDADA)));

        assertThat(resumo.previstas()).isEqualTo(6);
        assertThat(resumo.realizadas()).isEqualTo(1);
        assertThat(resumo.ausentes()).isEqualTo(1);
        assertThat(resumo.canceladas()).isEqualTo(1);
        assertThat(resumo.agendadas()).isEqualTo(1);
        assertThat(resumo.naoAgendadas()).isEqualTo(2);
        assertThat(resumo.pendentes()).isEqualTo(resumo.agendadas() + resumo.naoAgendadas()).isEqualTo(3);
    }

    @Test
    @DisplayName("Sem visitas, o resumo é todo zero")
    void resumoVazio() {
        ResumoDoHistorico resumo = ResumoDoHistorico.de(List.of());

        assertThat(resumo).isEqualTo(new ResumoDoHistorico(0, 0, 0, 0, 0, 0, 0));
    }

    @Test
    @DisplayName("No JSON, a data e hora saem com fuso (ISO 8601) e a data pura sem hora")
    void formatoDoJson() throws Exception {
        ObjectMapper json = new ObjectMapper().registerModule(new JavaTimeModule())
                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        List<VisitaDoHistorico> visitas = List.of(visita(1, SituacaoNoHistorico.AGENDADA),
                visita(2, SituacaoNoHistorico.NAO_AGENDADA));
        HistoricoParticipanteResponse resposta = new HistoricoParticipanteResponse(
                new ParticipanteDoHistorico(UUID.randomUUID(), UUID.randomUUID(), "CAND-001",
                        "Pessoa Teta (Ficticia)", LocalDate.of(2026, 10, 20)),
                ResumoDoHistorico.de(visitas), visitas);

        JsonNode no = json.readTree(json.writeValueAsString(resposta));

        assertThat(no.at("/participante/dataInclusao").asText()).isEqualTo("2026-10-20");
        assertThat(no.at("/visitas/0/dataHoraAgendada").asText()).isEqualTo("2026-10-20T09:00:00-03:00");
        assertThat(no.at("/visitas/0/situacao").asText()).isEqualTo("AGENDADA");
        assertThat(no.at("/visitas/1/situacao").asText()).isEqualTo("NAO_AGENDADA");
        assertThat(no.at("/visitas/1/visitaAgendadaId").isNull()).isTrue();
        assertThat(no.at("/visitas/1/dataHoraAgendada").isNull()).isTrue();
        assertThat(no.at("/resumo/pendentes").asInt()).isEqualTo(2);
    }
}
