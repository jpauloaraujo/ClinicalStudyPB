package UEPB.ESWII.ClinicalStudyPB;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.lang.reflect.Method;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;

import UEPB.ESWII.ClinicalStudyPB.domain.enums.SituacaoParticipacao;
import UEPB.ESWII.ClinicalStudyPB.domain.enums.SolicitanteRetirada;
import UEPB.ESWII.ClinicalStudyPB.domain.model.Participacao;
import UEPB.ESWII.ClinicalStudyPB.domain.model.RetiradaParticipacao;
import UEPB.ESWII.ClinicalStudyPB.repository.ParticipacaoRepository;
import UEPB.ESWII.ClinicalStudyPB.repository.RetiradaParticipacaoRepository;
import UEPB.ESWII.ClinicalStudyPB.repository.UsuarioRepository;
import UEPB.ESWII.ClinicalStudyPB.support.CenarioVisitaAgendada;
import UEPB.ESWII.ClinicalStudyPB.support.CenarioVisitaAgendada.Ids;

/**
 * T101: persistência da retirada de participante, unicidade por participação e irreversibilidade da situação.
 */
@SpringBootTest
@Transactional
class RetiradaParticipacaoRepositoryTest {

    @Autowired
    private RetiradaParticipacaoRepository repository;
    @Autowired
    private ParticipacaoRepository participacoes;
    @Autowired
    private UsuarioRepository usuarios;
    @Autowired
    private JdbcTemplate jdbc;

    private Ids ids;

    @BeforeEach
    void preparar() {
        ids = CenarioVisitaAgendada.criar(jdbc);
    }

    private RetiradaParticipacao nova(SolicitanteRetirada solicitante, String motivo) {
        RetiradaParticipacao retirada = new RetiradaParticipacao();
        retirada.setParticipacao(participacoes.getReferenceById(ids.participacao()));
        retirada.setMotivo(motivo);
        retirada.setSolicitante(solicitante);
        retirada.setDataRetirada(LocalDate.of(2026, 11, 18));
        retirada.setRegistradoPor(usuarios.getReferenceById(ids.usuario()));
        return retirada;
    }

    @Test
    @DisplayName("Salva a retirada com motivo, solicitante, data, autor e instante do registro")
    void salvaRetiradaComAuditoria() {
        RetiradaParticipacao salva = repository.saveAndFlush(nova(SolicitanteRetirada.PARTICIPANTE, "Mudança de cidade"));

        assertThat(salva.getId()).isNotNull();
        assertThat(salva.getParticipacao().getId()).isEqualTo(ids.participacao());
        assertThat(salva.getMotivo()).isEqualTo("Mudança de cidade");
        assertThat(salva.getSolicitante()).isEqualTo(SolicitanteRetirada.PARTICIPANTE);
        assertThat(salva.getDataRetirada()).isEqualTo(LocalDate.of(2026, 11, 18));
        assertThat(salva.getRegistradoPor().getId()).isEqualTo(ids.usuario());
        assertThat(salva.getRegistradoEm()).isNotNull();
    }

    @Test
    @DisplayName("findByParticipacaoId e existsByParticipacaoId encontram a retirada gravada e devolvem vazio para outra")
    void consultasPorParticipacao() {
        assertThat(repository.existsByParticipacaoId(ids.participacao())).isFalse();
        assertThat(repository.findByParticipacaoId(ids.participacao())).isEmpty();

        repository.saveAndFlush(nova(SolicitanteRetirada.COORDENACAO, "Solicitação formal da coordenação"));

        assertThat(repository.existsByParticipacaoId(ids.participacao())).isTrue();
        assertThat(repository.findByParticipacaoId(ids.participacao()))
                .get()
                .extracting(RetiradaParticipacao::getSolicitante, RetiradaParticipacao::getMotivo)
                .containsExactly(SolicitanteRetirada.COORDENACAO, "Solicitação formal da coordenação");
        assertThat(repository.existsByParticipacaoId(UUID.randomUUID())).isFalse();
    }

    @Test
    @DisplayName("A segunda retirada da mesma participação dispara DataIntegrityViolationException (UNIQUE)")
    void recusaSegundaRetiradaDaMesmaParticipacao() {
        repository.saveAndFlush(nova(SolicitanteRetirada.PARTICIPANTE, "Primeira retirada"));

        assertThatThrownBy(() -> repository.saveAndFlush(nova(SolicitanteRetirada.PESQUISADOR, "Segunda retirada")))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    @DisplayName("Participacao nasce ATIVO, passa para RETIRADO por retirar() e não expõe setSituacao nem reativar")
    void situacaoDaParticipacaoEIrreversivel() {
        Participacao participacao = participacoes.findById(ids.participacao()).orElseThrow();
        assertThat(participacao.getSituacao()).isEqualTo(SituacaoParticipacao.ATIVO);
        assertThat(participacao.estaRetirada()).isFalse();

        participacao.retirar();
        participacoes.saveAndFlush(participacao);

        Participacao recarregada = participacoes.findById(ids.participacao()).orElseThrow();
        assertThat(recarregada.getSituacao()).isEqualTo(SituacaoParticipacao.RETIRADO);
        assertThat(recarregada.estaRetirada()).isTrue();

        assertThat(Arrays.stream(Participacao.class.getMethods()).map(Method::getName))
                .doesNotContain("setSituacao", "reativar");
    }
}
