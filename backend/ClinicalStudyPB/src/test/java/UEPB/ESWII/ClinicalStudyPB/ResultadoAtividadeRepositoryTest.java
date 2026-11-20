package UEPB.ESWII.ClinicalStudyPB;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDateTime;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;

import UEPB.ESWII.ClinicalStudyPB.domain.model.ResultadoAtividade;
import UEPB.ESWII.ClinicalStudyPB.repository.AcaoRepository;
import UEPB.ESWII.ClinicalStudyPB.repository.ResultadoAtividadeRepository;
import UEPB.ESWII.ClinicalStudyPB.repository.UsuarioRepository;
import UEPB.ESWII.ClinicalStudyPB.repository.VisitaAgendadaRepository;
import UEPB.ESWII.ClinicalStudyPB.support.CenarioVisitaAgendada;
import UEPB.ESWII.ClinicalStudyPB.support.CenarioVisitaAgendada.Ids;

/**
 * T87: persistência do resultado fictício de uma atividade de avaliação (entidade, repositório e UNIQUE).
 */
@SpringBootTest
@Transactional
class ResultadoAtividadeRepositoryTest {

    /** Pesquisador do seed (V2). */
    private static final UUID PESQUISADOR = UUID.fromString("33333333-3333-4333-8333-333333333333");

    @Autowired
    private ResultadoAtividadeRepository repository;
    @Autowired
    private VisitaAgendadaRepository visitasAgendadas;
    @Autowired
    private AcaoRepository acoes;
    @Autowired
    private UsuarioRepository usuarios;
    @Autowired
    private JdbcTemplate jdbc;

    private Ids ids;
    private UUID visitaAgendada;
    private UUID pressao;
    private UUID dor;

    @BeforeEach
    void preparar() {
        ids = CenarioVisitaAgendada.criar(jdbc);
        visitaAgendada = CenarioVisitaAgendada.inserirVisitaAgendada(jdbc, ids, ids.visita(), "REALIZADA",
                LocalDateTime.now().minusHours(1), null);
        pressao = CenarioVisitaAgendada.inserirAcao(jdbc, ids.visita(), "Aferir pressão arterial", "AVALIACAO_CLINICA");
        dor = CenarioVisitaAgendada.inserirAcao(jdbc, ids.visita(), "Escala de dor", "AVALIACAO_CLINICA");
    }

    private ResultadoAtividade novo(UUID acao, String valor) {
        ResultadoAtividade r = new ResultadoAtividade();
        r.setVisitaAgendada(visitasAgendadas.getReferenceById(visitaAgendada));
        r.setAcao(acoes.getReferenceById(acao));
        r.setValor(valor);
        r.setRegistradoPor(usuarios.getReferenceById(PESQUISADOR));
        return r;
    }

    @Test
    @DisplayName("Salva o resultado com valor, unidade, observação, autor e datas de registro e alteração")
    void salvaComAutorEDatas() {
        ResultadoAtividade r = novo(pressao, "120/80");
        r.setUnidade("mmHg");
        r.setObservacao("Sentado, após 5 minutos");

        ResultadoAtividade salvo = repository.saveAndFlush(r);

        assertThat(salvo.getId()).isNotNull();
        assertThat(salvo.getValor()).isEqualTo("120/80");
        assertThat(salvo.getUnidade()).isEqualTo("mmHg");
        assertThat(salvo.getObservacao()).isEqualTo("Sentado, após 5 minutos");
        assertThat(salvo.getRegistradoEm()).isNotNull();
        assertThat(salvo.getAlteradoEm()).isEqualTo(salvo.getRegistradoEm());
        assertThat(salvo.getAlteradoPor().getId()).isEqualTo(PESQUISADOR);
    }

    @Test
    @DisplayName("Unidade e observação são opcionais")
    void unidadeEObservacaoOpcionais() {
        ResultadoAtividade salvo = repository.saveAndFlush(novo(dor, "3"));

        assertThat(salvo.getUnidade()).isNull();
        assertThat(salvo.getObservacao()).isNull();
    }

    @Test
    @DisplayName("Busca por visita agendada, por visita e atividade, e conta os resultados da visita")
    void consultasDoRepositorio() {
        repository.saveAndFlush(novo(pressao, "120/80"));
        repository.saveAndFlush(novo(dor, "3"));

        assertThat(repository.findByVisitaAgendadaId(visitaAgendada))
                .extracting(r -> r.getAcao().getId())
                .containsExactlyInAnyOrder(pressao, dor);
        assertThat(repository.findByVisitaAgendadaIdAndAcaoId(visitaAgendada, pressao))
                .get().extracting(ResultadoAtividade::getValor).isEqualTo("120/80");
        assertThat(repository.findByVisitaAgendadaIdAndAcaoId(visitaAgendada, UUID.randomUUID())).isEmpty();
        assertThat(repository.countByVisitaAgendadaId(visitaAgendada)).isEqualTo(2);
        assertThat(repository.countByVisitaAgendadaId(UUID.randomUUID())).isZero();
    }

    @Test
    @DisplayName("Editar atualiza valor e data de alteração, mantendo a data de registro")
    void edicaoAtualizaAlteracao() {
        ResultadoAtividade salvo = repository.saveAndFlush(novo(pressao, "120/80"));
        LocalDateTime registradoEm = salvo.getRegistradoEm();

        salvo.setValor("130/85");
        repository.saveAndFlush(salvo);

        assertThat(salvo.getValor()).isEqualTo("130/85");
        assertThat(salvo.getRegistradoEm()).isEqualTo(registradoEm);
        assertThat(salvo.getAlteradoEm()).isAfterOrEqualTo(registradoEm);
    }

    @Test
    @DisplayName("O segundo resultado da mesma atividade na mesma visita dispara DataIntegrityViolationException")
    void recusaDuplicidade() {
        repository.saveAndFlush(novo(pressao, "120/80"));

        assertThatThrownBy(() -> repository.saveAndFlush(novo(pressao, "130/85")))
                .isInstanceOf(DataIntegrityViolationException.class);
    }
}
