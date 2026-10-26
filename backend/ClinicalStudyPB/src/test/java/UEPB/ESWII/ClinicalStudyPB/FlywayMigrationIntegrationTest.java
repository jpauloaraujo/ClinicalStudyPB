package UEPB.ESWII.ClinicalStudyPB;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.UUID;

import org.flywaydb.core.Flyway;
import org.flywaydb.core.api.MigrationInfo;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

import UEPB.ESWII.ClinicalStudyPB.domain.enums.PerfilUsuario;
import UEPB.ESWII.ClinicalStudyPB.domain.enums.StatusEstudo;
import UEPB.ESWII.ClinicalStudyPB.domain.model.Estudo;
import UEPB.ESWII.ClinicalStudyPB.domain.model.Usuario;
import UEPB.ESWII.ClinicalStudyPB.repository.EstudoRepository;
import UEPB.ESWII.ClinicalStudyPB.repository.UsuarioRepository;

/**
 * Testes de integração das migrations iniciais do Flyway (T09).
 */
@SpringBootTest
@Transactional
class FlywayMigrationIntegrationTest {

    @Autowired
    private Flyway flyway;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private EstudoRepository estudoRepository;

    @Test
    @DisplayName("T09 - Deve aplicar com sucesso as migrations iniciais V1 e V2 do Flyway")
    void deveAplicarMigrationsIniciaisDoFlyway() {
        MigrationInfo[] aplicadas = flyway.info().applied();

        assertThat(aplicadas)
                .hasSizeGreaterThanOrEqualTo(2)
                .extracting(info -> info.getVersion().getVersion())
                .contains("1", "2");
    }

    @Test
    @DisplayName("T09 - Deve carregar os quatro perfis fictícios no seed inicial com senha em BCrypt")
    void deveCarregarUsuariosDeSeedComQuatroPerfisESenhaValida() {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

        Usuario admin = usuarioRepository.findByEmail("admin@clinicalstudy.test").orElseThrow();
        assertThat(admin.getPerfil()).isEqualTo(PerfilUsuario.ADMINISTRADOR);
        assertThat(admin.isAtivo()).isTrue();
        assertThat(encoder.matches("senha-de-teste", admin.getSenha())).isTrue();

        Usuario pesquisador = usuarioRepository.findByEmail("pesquisador@clinicalstudy.test").orElseThrow();
        assertThat(pesquisador.getPerfil()).isEqualTo(PerfilUsuario.PESQUISADOR);

        Usuario coordenador = usuarioRepository.findByEmail("coordenador@clinicalstudy.test").orElseThrow();
        assertThat(coordenador.getPerfil()).isEqualTo(PerfilUsuario.COORDENADOR);

        Usuario participante = usuarioRepository.findByEmail("participante@clinicalstudy.test").orElseThrow();
        assertThat(participante.getPerfil()).isEqualTo(PerfilUsuario.PARTICIPANTE);
    }

    @Test
    @DisplayName("T09 - Deve carregar os estudos clínicos fictícios vinculados ao pesquisador responsável")
    void deveCarregarEstudosClinicosIniciaisDeDemonstracao() {
        List<Estudo> estudos = estudoRepository.findAll();

        assertThat(estudos).hasSizeGreaterThanOrEqualTo(2);

        Estudo estudoPlanejamento = estudoRepository
                .findById(UUID.fromString("aaaaaaaa-aaaa-4aaa-8aaa-aaaaaaaaaaaa"))
                .orElseThrow();

        assertThat(estudoPlanejamento.getStatus()).isEqualTo(StatusEstudo.PLANEJAMENTO);
        assertThat(estudoPlanejamento.getLimiteParticipantes()).isEqualTo(50);
        assertThat(estudoPlanejamento.getResponsavel().getEmail()).isEqualTo("pesquisador@clinicalstudy.test");
    }
}
