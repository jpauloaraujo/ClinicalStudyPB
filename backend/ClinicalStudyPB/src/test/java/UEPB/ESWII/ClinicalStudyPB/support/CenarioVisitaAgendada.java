package UEPB.ESWII.ClinicalStudyPB.support;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

import org.springframework.jdbc.core.JdbcTemplate;

/**
 * Cria, direto no banco, o mínimo para testar a visita agendada (T73): um candidato incluído no estudo de
 * demonstração e duas visitas previstas. Use dentro de um teste {@code @Transactional}, que desfaz tudo no fim.
 */
public final class CenarioVisitaAgendada {

    private static final String ESTUDO = "aaaaaaaa-aaaa-4aaa-8aaa-aaaaaaaaaaaa";
    private static final String COORDENADOR = "22222222-2222-4222-8222-222222222222";

    public record Ids(UUID participacao, UUID visita, UUID outraVisita, UUID usuario) {
    }

    private CenarioVisitaAgendada() {
    }

    public static Ids criar(JdbcTemplate jdbc) {
        UUID candidato = UUID.randomUUID();
        UUID participacao = UUID.randomUUID();
        UUID visita = UUID.randomUUID();
        UUID outraVisita = UUID.randomUUID();
        int dia = 1000 + ThreadLocalRandom.current().nextInt(100000);

        jdbc.update("INSERT INTO candidatos (id, estudo_id, codigo, nome_ficticio, data_nascimento, situacao) "
                + "VALUES (CAST(? AS UUID), CAST(? AS UUID), ?, ?, DATE '1990-01-01', 'INCLUIDO')",
                candidato.toString(), ESTUDO, "T73-" + candidato.toString().substring(0, 8), "Candidato T73 " + candidato);
        jdbc.update("INSERT INTO participacoes (id, candidato_id, estudo_id, data_inclusao, incluido_por_id) "
                + "VALUES (CAST(? AS UUID), CAST(? AS UUID), CAST(? AS UUID), CURRENT_DATE, CAST(? AS UUID))",
                participacao.toString(), candidato.toString(), ESTUDO, COORDENADOR);
        inserirVisitaPlanejada(jdbc, visita, "Visita T73 A", dia, 1);
        inserirVisitaPlanejada(jdbc, outraVisita, "Visita T73 B", dia + 1, 2);
        return new Ids(participacao, visita, outraVisita, UUID.fromString(COORDENADOR));
    }

    private static void inserirVisitaPlanejada(JdbcTemplate jdbc, UUID id, String nome, int dia, int ordem) {
        jdbc.update("INSERT INTO visitas_planejadas (id, estudo_id, nome, dia_previsto, tolerancia, ordem) "
                + "VALUES (CAST(? AS UUID), CAST(? AS UUID), ?, ?, 3, ?)", id.toString(), ESTUDO, nome, dia, ordem);
    }

    /** Insere uma visita agendada por SQL, sem passar pela entidade, para provar o que o banco recusa. */
    public static void inserirVisitaAgendada(JdbcTemplate jdbc, Ids ids, UUID visita, String situacao,
            LocalDateTime dataHoraRealizada, String motivo) {
        Timestamp agora = Timestamp.valueOf(LocalDateTime.now());
        jdbc.update("INSERT INTO visitas_agendadas (id, participacao_id, visita_planejada_id, data_hora_agendada, "
                + "situacao, data_hora_realizada, motivo, versao, criado_por_id, criado_em, alterado_por_id, alterado_em) "
                + "VALUES (CAST(? AS UUID), CAST(? AS UUID), CAST(? AS UUID), ?, ?, ?, ?, 0, CAST(? AS UUID), ?, "
                + "CAST(? AS UUID), ?)",
                UUID.randomUUID().toString(), ids.participacao().toString(), visita.toString(), agora, situacao,
                dataHoraRealizada == null ? null : Timestamp.valueOf(dataHoraRealizada), motivo,
                ids.usuario().toString(), agora, ids.usuario().toString(), agora);
    }
}
