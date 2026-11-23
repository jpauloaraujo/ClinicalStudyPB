package UEPB.ESWII.ClinicalStudyPB.service;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Objects;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import UEPB.ESWII.ClinicalStudyPB.domain.enums.SituacaoVisita;
import UEPB.ESWII.ClinicalStudyPB.domain.enums.StatusEstudo;
import UEPB.ESWII.ClinicalStudyPB.domain.model.Candidato;
import UEPB.ESWII.ClinicalStudyPB.domain.model.Participacao;
import UEPB.ESWII.ClinicalStudyPB.domain.model.Usuario;
import UEPB.ESWII.ClinicalStudyPB.domain.model.VisitaAgendada;
import UEPB.ESWII.ClinicalStudyPB.domain.model.VisitaPlanejada;
import UEPB.ESWII.ClinicalStudyPB.domain.regra.JanelaDeAgendamento;
import UEPB.ESWII.ClinicalStudyPB.repository.VisitaAgendadaRepository;

@Service
public class AgendamentoVisitaService {

    private final VisitaAgendadaRepository visitaRepository;
    private final Clock clock;
    private static final java.time.format.DateTimeFormatter FORMATADOR_DATA =
            java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy");

    public AgendamentoVisitaService(VisitaAgendadaRepository visitaRepository, Clock clock) {
        this.visitaRepository = Objects.requireNonNull(visitaRepository);
        this.clock = Objects.requireNonNull(clock);
    }

    public VisitaAgendada agendarVisita(Participacao participacao, VisitaPlanejada visitaPlanejada,
            LocalDateTime dataAgendada, Usuario usuario) {
        validarRegrasDeAgendamento(participacao, visitaPlanejada, dataAgendada);

        if (visitaRepository.existsByParticipacaoIdAndVisitaPlanejadaId(
                participacao.getId(), visitaPlanejada.getId())) {
            throw conflito("VISITA_JA_AGENDADA",
                    "Já existe um agendamento para esta visita prevista.");
        }

        LocalDateTime agora = LocalDateTime.now(clock);
        VisitaAgendada visita = new VisitaAgendada();
        visita.setParticipacao(participacao);
        visita.setVisitaPlanejada(visitaPlanejada);
        visita.setDataHoraAgendada(dataAgendada);
        visita.setSituacao(SituacaoVisita.AGENDADA);
        visita.setCriadoPor(usuario);
        visita.setCriadoEm(agora);
        visita.setAlteradoPor(usuario);
        visita.setAlteradoEm(agora);
        try {
            return visitaRepository.saveAndFlush(visita);
        } catch (DataIntegrityViolationException ex) {
            throw conflito("VISITA_JA_AGENDADA", "Já existe um agendamento para esta visita prevista.");
        }
    }

    public VisitaAgendada reagendarVisita(VisitaAgendada visitaAgendada, LocalDateTime novaData, Usuario usuario) {
        validarEstadoDoEstudo(visitaAgendada.getParticipacao());
        exigirParticipacaoAtiva(visitaAgendada.getParticipacao());
        if (!visitaAgendada.estaAgendada()) {
            throw conflito("VISITA_NAO_ESTA_AGENDADA",
                    "Apenas visitas com situação AGENDADA podem ser reagendadas.");
        }

        validarRegrasDeAgendamento(visitaAgendada.getParticipacao(),
                visitaAgendada.getVisitaPlanejada(), novaData);

        visitaAgendada.setDataHoraAgendada(novaData);
        visitaAgendada.setAlteradoPor(usuario);
        visitaAgendada.setAlteradoEm(LocalDateTime.now(clock));
        return visitaRepository.save(visitaAgendada);
    }

    public JanelaDeAgendamento calcularJanelaSugerida(Participacao participacao,
            VisitaPlanejada visitaPlanejada) {
        LocalDate dataBase = visitaPlanejada.dataPrevista(participacao.getDataInclusao());
        return new JanelaDeAgendamento(
                dataBase.minusDays(visitaPlanejada.getTolerancia()),
                dataBase.plusDays(visitaPlanejada.getTolerancia()),
                dataBase);
    }

    private void validarRegrasDeAgendamento(Participacao participacao, VisitaPlanejada visitaPlanejada,
            LocalDateTime dataDesejada) {
        Candidato candidato = participacao.getCandidato();
        if (candidato == null || !candidato.estaIncluido()) {
            throw conflito("PARTICIPANTE_RETIRADO", "O participante não está ativo no estudo.");
        }

        validarEstadoDoEstudo(participacao);
        exigirParticipacaoAtiva(participacao);

        if (visitaPlanejada.getEstudo().getId() == null
                || !visitaPlanejada.getEstudo().getId().equals(participacao.getEstudo().getId())) {
            throw new RecursoNaoEncontradoException(
                    "Visita prevista não encontrada para a participação: " + visitaPlanejada.getId());
        }

        LocalDate hoje = LocalDate.now(clock);
        if (dataDesejada.toLocalDate().isBefore(hoje)) {
            throw new ValidacaoRegraException("DATA_NO_PASSADO", "A data agendada não pode estar no passado.");
        }

        JanelaDeAgendamento janela = JanelaDeAgendamento.de(visitaPlanejada, participacao.getDataInclusao());

        if (!janela.contem(dataDesejada.toLocalDate())) {
            String inicioFormatado = janela.dataInicio().format(FORMATADOR_DATA);
            String fimFormatado = janela.dataFim().format(FORMATADOR_DATA);
            throw conflito("DATA_FORA_DA_JANELA",
                    String.format("Data fora da janela de %s a %s", inicioFormatado, fimFormatado));
        }
    }

    void validarEstadoDoEstudo(Participacao participacao) {
        StatusEstudo status = participacao.getEstudo().getStatus();
        if (status != StatusEstudo.RECRUTAMENTO && status != StatusEstudo.EXECUCAO) {
            throw conflito("ESTUDO_FORA_DE_RECRUTAMENTO_OU_EXECUCAO",
                    "O estudo não está em recrutamento ou execução.");
        }
    }

    /** Regra 6 (T102): participante retirado não pode receber novos agendamentos nem reagendamentos. */
    void exigirParticipacaoAtiva(Participacao participacao) {
        if (participacao.estaRetirada()) {
            throw conflito("PARTICIPANTE_RETIRADO",
                    "O participante foi retirado do estudo; não é possível alterar o acompanhamento.");
        }
    }

    private static ConflitoRegraException conflito(String codigo, String mensagem) {
        return new ConflitoRegraException(codigo, mensagem);
    }
}