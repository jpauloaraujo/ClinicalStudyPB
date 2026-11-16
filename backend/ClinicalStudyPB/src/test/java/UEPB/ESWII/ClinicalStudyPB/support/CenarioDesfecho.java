package UEPB.ESWII.ClinicalStudyPB.support;

import static UEPB.ESWII.ClinicalStudyPB.support.CenarioRecrutamento.avaliarAprovado;
import static UEPB.ESWII.ClinicalStudyPB.support.CenarioRecrutamento.consentir;
import static UEPB.ESWII.ClinicalStudyPB.support.CenarioRecrutamento.criarCandidato;
import static UEPB.ESWII.ClinicalStudyPB.support.CenarioRecrutamento.enviar;
import static UEPB.ESWII.ClinicalStudyPB.support.CenarioRecrutamento.estudoEmRecrutamento;
import static UEPB.ESWII.ClinicalStudyPB.support.CenarioRecrutamento.id;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.UUID;

import jakarta.persistence.EntityManager;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;

import com.jayway.jsonpath.JsonPath;

/**
 * Prepara, para os testes do desfecho (T85), um estudo em recrutamento com uma visita prevista (a "Triagem", dia 0),
 * participantes incluídos pela API e visitas agendadas em datas escolhidas pelo teste. O estudo, o candidato e a
 * inclusão passam pela API; a visita agendada é inserida por SQL (o endpoint de agendamento é do PBI-19). As datas
 * são sempre relativas a hoje, nunca fixas.
 */
public final class CenarioDesfecho {

    private static final String COORDENADOR_EMAIL = "coordenador@clinicalstudy.test";

    /** Estudo em recrutamento e o id (texto) da sua única visita prevista. */
    public record EstudoComVisita(String estudoId, String visitaPlanejadaId) {
    }

    private CenarioDesfecho() {
    }

    public static EstudoComVisita estudoComVisita(MockMvc mvc, String tokenPesquisador, int limite) throws Exception {
        String estudo = estudoEmRecrutamento(mvc, tokenPesquisador, limite);
        String visitas = enviar(mvc, "GET", "/api/estudos/" + estudo + "/visitas", tokenPesquisador, null)
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        return new EstudoComVisita(estudo, JsonPath.read(visitas, "$[0].id"));
    }

    /** Inclui um candidato novo no estudo (avaliado e consentido) e devolve o id da participação. */
    public static String participanteIncluido(MockMvc mvc, String tokenCoordenador, String estudoId, String nome)
            throws Exception {
        String candidato = criarCandidato(mvc, tokenCoordenador, estudoId, nome);
        avaliarAprovado(mvc, tokenCoordenador, candidato, estudoId);
        consentir(mvc, tokenCoordenador, candidato).andExpect(status().isCreated());
        return id(enviar(mvc, "POST", "/api/estudos/" + estudoId + "/participantes", tokenCoordenador,
                "{\"candidatoId\":\"" + candidato + "\"}").andExpect(status().isCreated()));
    }

    /**
     * Inclui um participante novo e insere uma visita agendada ({@code AGENDADA}) para ele, devolvendo o id da
     * visita agendada. O {@code DesfechoVisitaService} só compara o <b>dia</b> agendado com hoje: use ontem para uma
     * visita que já pode ter desfecho e daqui a alguns dias para uma visita futura. Uma visita por participante,
     * porque o par (participação, visita prevista) é único.
     */
    public static String visitaAgendada(MockMvc mvc, JdbcTemplate jdbc, String tokenCoordenador, EstudoComVisita estudo,
            LocalDateTime dataHoraAgendada) throws Exception {
        return visitaAgendada(mvc, jdbc, null, tokenCoordenador, estudo, dataHoraAgendada);
    }

    /**
     * O mesmo que o método acima, para testes que rodam <b>dentro de uma transação</b> ({@code @Transactional}): o
     * MockMvc usa a transação do teste, então o JPA só guarda a participação em memória e não a grava antes do
     * {@code INSERT} por SQL da visita, que o banco recusa pela chave estrangeira. Passando o {@code EntityManager}
     * do teste, a participação é gravada (flush) antes. Testes sem transação passam {@code null} (ou usam o método
     * acima), pois lá cada requisição já grava de verdade.
     */
    public static String visitaAgendada(MockMvc mvc, JdbcTemplate jdbc, EntityManager entityManager,
            String tokenCoordenador, EstudoComVisita estudo, LocalDateTime dataHoraAgendada) throws Exception {
        String participacao = participanteIncluido(mvc, tokenCoordenador, estudo.estudoId(),
                "Candidato Desfecho " + UUID.randomUUID().toString().substring(0, 8));
        if (entityManager != null) {
            entityManager.flush();
        }
        return agendada(jdbc, participacao, estudo.visitaPlanejadaId(), dataHoraAgendada);
    }

    /** Insere por SQL a visita agendada de uma participação e devolve o id. O autor é o coordenador do seed. */
    public static String agendada(JdbcTemplate jdbc, String participacaoId, String visitaPlanejadaId,
            LocalDateTime dataHoraAgendada) {
        String usuario = jdbc.queryForObject("SELECT CAST(id AS VARCHAR(36)) FROM usuarios WHERE email = ?",
                String.class, COORDENADOR_EMAIL);
        String id = UUID.randomUUID().toString();
        Timestamp agora = Timestamp.valueOf(LocalDateTime.now());
        jdbc.update("INSERT INTO visitas_agendadas (id, participacao_id, visita_planejada_id, data_hora_agendada, "
                + "situacao, versao, criado_por_id, criado_em, alterado_por_id, alterado_em) "
                + "VALUES (CAST(? AS UUID), CAST(? AS UUID), CAST(? AS UUID), ?, 'AGENDADA', 0, CAST(? AS UUID), ?, "
                + "CAST(? AS UUID), ?)",
                id, participacaoId, visitaPlanejadaId, Timestamp.valueOf(dataHoraAgendada), usuario, agora, usuario,
                agora);
        return id;
    }
}
