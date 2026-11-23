package UEPB.ESWII.ClinicalStudyPB.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
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
import UEPB.ESWII.ClinicalStudyPB.domain.model.RetiradaParticipacao;
import UEPB.ESWII.ClinicalStudyPB.domain.model.Usuario;
import UEPB.ESWII.ClinicalStudyPB.repository.RetiradaParticipacaoRepository;

/**
 * T113 (RQ-T113-01, 02, 03 e 05): {@link MeusEstudosService} isolado, com o {@link AcessoParticipante} simulado.
 * Cobre o que o banco não permite montar desde a T111 (uma conta tem um único candidato, logo uma única participação):
 * conta com <b>dois estudos</b> e a ordem recebida.
 */
class MeusEstudosServiceTest {

    private static final UUID USUARIO = UUID.randomUUID();

    private AcessoParticipante acesso;
    private RetiradaParticipacaoRepository retiradas;
    private MeusEstudosService servico;

    @BeforeEach
    void preparar() {
        acesso = mock(AcessoParticipante.class);
        retiradas = mock(RetiradaParticipacaoRepository.class);
        servico = new MeusEstudosService(acesso, retiradas);
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
        assertThat(detalhe.retirada()).as("participação ativa: sem retirada").isNull();
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

    @Test
    @DisplayName("T101: participação retirada vem RETIRADO e o detalhe traz só a data da retirada, nunca o motivo")
    void participacaoRetiradaTrazSituacaoEDataSemMotivo() {
        Participacao participacao = participacao("Estudo retirado", "Objetivo.", StatusEstudo.EXECUCAO,
                LocalDate.of(2026, 10, 1));
        participacao.retirar();
        RetiradaParticipacao retirada = new RetiradaParticipacao();
        retirada.setDataRetirada(LocalDate.of(2026, 11, 18));
        retirada.setMotivo("Motivo sigiloso");
        UUID estudoId = participacao.getEstudo().getId();
        when(acesso.participacoesDe(USUARIO)).thenReturn(List.of(participacao));
        when(acesso.participacaoDoEstudo(USUARIO, estudoId)).thenReturn(participacao);
        when(retiradas.findByParticipacaoId(participacao.getId())).thenReturn(Optional.of(retirada));

        assertThat(servico.listar(USUARIO).get(0).situacaoParticipacao()).isEqualTo(SituacaoParticipacao.RETIRADO);
        MeuEstudoDetalheResponse detalhe = servico.detalhar(USUARIO, estudoId);

        assertThat(detalhe.situacaoParticipacao()).isEqualTo(SituacaoParticipacao.RETIRADO);
        assertThat(detalhe.retirada()).isNotNull();
        assertThat(detalhe.retirada().dataRetirada()).isEqualTo(LocalDate.of(2026, 11, 18));
        assertThat(detalhe.retirada().toString()).doesNotContain("sigiloso");
    }

    @Test
    @DisplayName("T101: participação retirada sem registro de retirada devolve retirada nula (nunca erro)")
    void retiradaSemRegistroDevolveNulo() {
        Participacao participacao = participacao("Estudo sem registro", "Objetivo.", StatusEstudo.EXECUCAO,
                LocalDate.of(2026, 10, 1));
        participacao.retirar();
        UUID estudoId = participacao.getEstudo().getId();
        when(acesso.participacaoDoEstudo(USUARIO, estudoId)).thenReturn(participacao);
        when(retiradas.findByParticipacaoId(participacao.getId())).thenReturn(Optional.empty());

        assertThat(servico.detalhar(USUARIO, estudoId).retirada()).isNull();
    }
}
