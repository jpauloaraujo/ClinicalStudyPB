package UEPB.ESWII.ClinicalStudyPB.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import UEPB.ESWII.ClinicalStudyPB.controller.dto.MeuEstudoDetalheResponse;
import UEPB.ESWII.ClinicalStudyPB.controller.dto.MeuEstudoResponse;
import UEPB.ESWII.ClinicalStudyPB.domain.enums.SituacaoParticipacao;
import UEPB.ESWII.ClinicalStudyPB.domain.enums.StatusEstudo;
import UEPB.ESWII.ClinicalStudyPB.domain.model.Estudo;
import UEPB.ESWII.ClinicalStudyPB.domain.model.Participacao;
import UEPB.ESWII.ClinicalStudyPB.domain.model.Usuario;

/**
 * T113 (RQ-T113-01, 02, 03 e 05): {@link MeusEstudosService} isolado, com o {@link AcessoParticipante} simulado.
 * Cobre o que o banco não permite montar desde a T111 (uma conta tem um único candidato, logo uma única participação):
 * conta com <b>dois estudos</b> e a ordem recebida.
 */
class MeusEstudosServiceTest {

    private static final UUID USUARIO = UUID.randomUUID();

    private AcessoParticipante acesso;
    private MeusEstudosService servico;

    @BeforeEach
    void preparar() {
        acesso = mock(AcessoParticipante.class);
        servico = new MeusEstudosService(acesso);
    }

    private static Participacao participacao(String titulo, String objetivo, StatusEstudo status, LocalDate inclusao) {
        Usuario responsavel = new Usuario();
        responsavel.setNome("Dra. Responsavel (Ficticia)");
        Estudo estudo = new Estudo();
        estudo.setId(UUID.randomUUID());
        estudo.setTitulo(titulo);
        estudo.setObjetivo(objetivo);
        estudo.setStatus(status);
        estudo.setResponsavel(responsavel);
        estudo.setDataInicio(LocalDate.of(2026, 11, 1));
        estudo.setDataFim(LocalDate.of(2027, 5, 1));
        Participacao participacao = new Participacao();
        participacao.setId(UUID.randomUUID());
        participacao.setEstudo(estudo);
        participacao.setDataInclusao(inclusao);
        return participacao;
    }

    @Test
    @DisplayName("RQ-02: conta com dois estudos vê os dois, na ordem em que o acesso os entrega (mais recente primeiro)")
    void doisEstudosMantemAOrdem() {
        Participacao recente = participacao("Estudo recente", "Objetivo A.", StatusEstudo.EXECUCAO,
                LocalDate.of(2026, 11, 10));
        Participacao antiga = participacao("Estudo antigo", "Objetivo B.", StatusEstudo.RECRUTAMENTO,
                LocalDate.of(2026, 10, 1));
        when(acesso.participacoesDe(USUARIO)).thenReturn(List.of(recente, antiga));

        List<MeuEstudoResponse> lista = servico.listar(USUARIO);

        assertThat(lista).extracting(MeuEstudoResponse::titulo).containsExactly("Estudo recente", "Estudo antigo");
        assertThat(lista).extracting(MeuEstudoResponse::dataInclusao)
                .containsExactly(LocalDate.of(2026, 11, 10), LocalDate.of(2026, 10, 1));
        assertThat(lista).extracting(MeuEstudoResponse::participacaoId)
                .containsExactly(recente.getId(), antiga.getId());
        assertThat(lista).extracting(MeuEstudoResponse::situacaoParticipacao)
                .containsOnly(SituacaoParticipacao.ATIVO);
    }

    @Test
    @DisplayName("RQ-02: conta sem participação recebe lista vazia")
    void semParticipacaoListaVazia() {
        when(acesso.participacoesDe(USUARIO)).thenReturn(List.of());

        assertThat(servico.listar(USUARIO)).isEmpty();
    }

    @Test
    @DisplayName("RQ-05: o resumo da lista é cortado em 160 caracteres e o detalhe traz o objetivo inteiro")
    void resumoCortadoEObjetivoCompleto() {
        String objetivo = "Objetivo longo. ".repeat(20);
        Participacao participacao = participacao("Estudo longo", objetivo, StatusEstudo.EXECUCAO,
                LocalDate.of(2026, 11, 1));
        UUID estudoId = participacao.getEstudo().getId();
        when(acesso.participacoesDe(USUARIO)).thenReturn(List.of(participacao));
        when(acesso.participacaoDoEstudo(USUARIO, estudoId)).thenReturn(participacao);

        assertThat(servico.listar(USUARIO).get(0).resumo()).hasSizeLessThanOrEqualTo(160).endsWith("…");
        MeuEstudoDetalheResponse detalhe = servico.detalhar(USUARIO, estudoId);
        assertThat(detalhe.objetivo()).isEqualTo(objetivo);
        assertThat(detalhe.resumo()).hasSizeLessThanOrEqualTo(160);
        assertThat(detalhe.responsavelNome()).isEqualTo("Dra. Responsavel (Ficticia)");
        assertThat(detalhe.retirada()).as("sem retirada (TODO T101), nunca com motivo").isNull();
        assertThat(detalhe.situacaoParticipacao()).isEqualTo(SituacaoParticipacao.ATIVO);
    }

    @Test
    @DisplayName("RQ-03: estudo que não é da conta propaga o 404 do acesso, sem consultar mais nada")
    void detalheDeEstudoAlheioPropaga404() {
        UUID estudoAlheio = UUID.randomUUID();
        when(acesso.participacaoDoEstudo(USUARIO, estudoAlheio)).thenThrow(new RecursoNaoEncontradoException("x"));

        assertThatThrownBy(() -> servico.detalhar(USUARIO, estudoAlheio))
                .isInstanceOf(RecursoNaoEncontradoException.class);
    }
}
