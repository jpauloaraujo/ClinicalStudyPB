package UEPB.ESWII.ClinicalStudyPB;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDate;
import java.time.LocalDateTime;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;

import UEPB.ESWII.ClinicalStudyPB.domain.enums.SituacaoVisita;
import UEPB.ESWII.ClinicalStudyPB.domain.model.VisitaAgendada;
import UEPB.ESWII.ClinicalStudyPB.repository.ParticipacaoRepository;
import UEPB.ESWII.ClinicalStudyPB.repository.UsuarioRepository;
import UEPB.ESWII.ClinicalStudyPB.repository.VisitaAgendadaRepository;
import UEPB.ESWII.ClinicalStudyPB.repository.VisitaPlanejadaRepository;
import UEPB.ESWII.ClinicalStudyPB.support.CenarioVisitaAgendada;
import UEPB.ESWII.ClinicalStudyPB.support.CenarioVisitaAgendada.Ids;

/**
 * T73: persistência da visita agendada (entidade, repositório, UNIQUE e controle de versão otimista).
 */
@SpringBootTest
@Transactional
class VisitaAgendadaRepositoryTest {

    @Autowired
    private VisitaAgendadaRepository repository;
    @Autowired
    private ParticipacaoRepository participacoes;
    @Autowired
    private VisitaPlanejadaRepository visitas;
    @Autowired
    private UsuarioRepository usuarios;
    @Autowired
    private JdbcTemplate jdbc;
    private Ids ids;

    @BeforeEach
    void preparar() {
        ids = CenarioVisitaAgendada.criar(jdbc);
    }

    private VisitaAgendada nova(java.util.UUID visita) {
        VisitaAgendada v = new VisitaAgendada();
        v.setParticipacao(participacoes.getReferenceById(ids.participacao()));
        v.setVisitaPlanejada(visitas.getReferenceById(visita));
        v.setDataHoraAgendada(LocalDateTime.now().plusDays(3));
        v.setCriadoPor(usuarios.getReferenceById(ids.usuario()));
        return v;
    }

    @Test
    @DisplayName("Salva com situação AGENDADA por padrão, datas preenchidas e quem criou e alterou")
    void salvaComPadroes() {
        VisitaAgendada salva = repository.saveAndFlush(nova(ids.visita()));

        assertThat(salva.getId()).isNotNull();
        assertThat(salva.getSituacao()).isEqualTo(SituacaoVisita.AGENDADA);
        assertThat(salva.estaAgendada()).isTrue();
        assertThat(salva.getVersao()).isZero();
        assertThat(salva.getCriadoEm()).isNotNull();
        assertThat(salva.getAlteradoEm()).isNotNull();
        assertThat(salva.getAlteradoPor().getId()).isEqualTo(ids.usuario());
        assertThat(salva.dataAgendada()).isEqualTo(LocalDate.now().plusDays(3));
    }

    @Test
    @DisplayName("Os métodos do repositório encontram, contam e ordenam pela ordem da visita prevista")
    void consultasDoRepositorio() {
        repository.saveAndFlush(nova(ids.outraVisita()));
        repository.saveAndFlush(nova(ids.visita()));

        assertThat(repository.existsByParticipacaoIdAndVisitaPlanejadaId(ids.participacao(), ids.visita())).isTrue();
        assertThat(repository.findByParticipacaoIdAndVisitaPlanejadaId(ids.participacao(), ids.outraVisita()))
                .isPresent();
        assertThat(repository.countByParticipacaoId(ids.participacao())).isEqualTo(2);
        assertThat(repository.findByParticipacaoIdOrderByVisitaPlanejadaOrdemAsc(ids.participacao()))
                .extracting(v -> v.getVisitaPlanejada().getId())
                .containsExactly(ids.visita(), ids.outraVisita());
    }

    @Test
    @DisplayName("O segundo agendamento do mesmo par participação e visita prevista dispara DataIntegrityViolationException")
    void recusaDuplicidade() {
        repository.saveAndFlush(nova(ids.visita()));

        assertThatThrownBy(() -> repository.saveAndFlush(nova(ids.visita())))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    @DisplayName("@Version incrementa ao alterar a visita")
    void versaoIncrementa() {
        VisitaAgendada salva = repository.saveAndFlush(nova(ids.visita()));
        assertThat(salva.getVersao()).isZero();

        salva.setSituacao(SituacaoVisita.AUSENTE);
        repository.saveAndFlush(salva);

        assertThat(salva.getVersao()).isEqualTo(1);
    }

    @Test
    @DisplayName("Duas alterações a partir da mesma versão: a segunda é recusada pelo controle otimista")
    void versaoRecusaAlteracaoDefasada() {
        VisitaAgendada salva = repository.saveAndFlush(nova(ids.visita()));
        jdbc.update("UPDATE visitas_agendadas SET versao = versao + 1 WHERE id = CAST(? AS UUID)",
                salva.getId().toString());

        salva.setSituacao(SituacaoVisita.AUSENTE);

        assertThatThrownBy(() -> repository.saveAndFlush(salva))
                .isInstanceOf(org.springframework.orm.ObjectOptimisticLockingFailureException.class);
    }
}
