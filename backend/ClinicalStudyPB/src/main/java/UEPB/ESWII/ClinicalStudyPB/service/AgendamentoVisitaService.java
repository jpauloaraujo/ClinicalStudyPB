package UEPB.ESWII.ClinicalStudyPB.service;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Objects;

import UEPB.ESWII.ClinicalStudyPB.domain.enums.SituacaoVisita;
import UEPB.ESWII.ClinicalStudyPB.domain.enums.StatusEstudo;
import UEPB.ESWII.ClinicalStudyPB.domain.model.Candidato;
import UEPB.ESWII.ClinicalStudyPB.domain.model.Participacao;
import UEPB.ESWII.ClinicalStudyPB.domain.model.Usuario;
import UEPB.ESWII.ClinicalStudyPB.domain.model.VisitaAgendada;
import UEPB.ESWII.ClinicalStudyPB.domain.model.VisitaPlanejada;
import UEPB.ESWII.ClinicalStudyPB.domain.regra.JanelaDeAgendamento;
import UEPB.ESWII.ClinicalStudyPB.repository.VisitaAgendadaRepository;

public class AgendamentoVisitaService {

    private final VisitaAgendadaRepository visitaRepository;
    private final Clock clock;

    public AgendamentoVisitaService(VisitaAgendadaRepository visitaRepository, Clock clock) {
        this.visitaRepository = Objects.requireNonNull(visitaRepository);
        this.clock = Objects.requireNonNull(clock);
    }

    public VisitaAgendada agendarVisita(Participacao participacao, VisitaPlanejada visitaPlanejada,
            LocalDate dataAgendada, Usuario usuario) {
        validarRegrasDeAgendamento(participacao, visitaPlanejada, dataAgendada);

        if (visitaRepository.existsByParticipacaoIdAndVisitaPlanejadaId(
                participacao.getId(), visitaPlanejada.getId())) {
            throw conflito("AGENDAMENTO_DUPLICADO",
                    "Já existe um agendamento para esta visita prevista.");
        }

        LocalDateTime agora = LocalDateTime.now(clock);
        VisitaAgendada visita = new VisitaAgendada();
        visita.setParticipacao(participacao);
        visita.setVisitaPlanejada(visitaPlanejada);
        visita.setDataHoraAgendada(dataAgendada.atStartOfDay());
        visita.setSituacao(SituacaoVisita.AGENDADA);
        visita.setCriadoPor(usuario);
        visita.setCriadoEm(agora);
        visita.setAlteradoPor(usuario);
        visita.setAlteradoEm(agora);
        return visitaRepository.save(visita);
    }

    public VisitaAgendada reagendarVisita(VisitaAgendada visitaAgendada, LocalDate novaData, Usuario usuario) {
        if (!visitaAgendada.estaAgendada()) {
            throw conflito("REAGENDAMENTO_INVALIDO",
                    "Apenas visitas com situação AGENDADA podem ser reagendadas.");
        }

        validarRegrasDeAgendamento(visitaAgendada.getParticipacao(),
                visitaAgendada.getVisitaPlanejada(), novaData);

        visitaAgendada.setDataHoraAgendada(novaData.atStartOfDay());
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
            LocalDate dataDesejada) {
        Candidato candidato = participacao.getCandidato();
        if (candidato == null || !candidato.estaIncluido()) {
            throw conflito("PARTICIPANTE_RETIRADO", "O participante não está ativo no estudo.");
        }

        StatusEstudo status = participacao.getEstudo().getStatus();
        if (status != StatusEstudo.RECRUTAMENTO && status != StatusEstudo.EXECUCAO) {
            throw conflito("ESTUDO_INVALIDO", "O estudo não está em recrutamento ou execução.");
        }

        if (visitaPlanejada.getEstudo().getId() == null
                || !visitaPlanejada.getEstudo().getId().equals(participacao.getEstudo().getId())) {
            throw conflito("VISITA_INCOMPATIVEL",
                    "A visita prevista não pertence ao estudo do participante.");
        }

        LocalDate hoje = LocalDate.now(clock);
        if (dataDesejada.isBefore(hoje)) {
            throw conflito("DATA_PASSADA", "A data agendada não pode estar no passado.");
        }

        JanelaDeAgendamento janela = calcularJanelaSugerida(participacao, visitaPlanejada);
        if (dataDesejada.isBefore(janela.dataInicio()) || dataDesejada.isAfter(janela.dataFim())) {
            throw conflito("FORA_DA_JANELA",
                    String.format("A data %s está fora da janela permitida (%s a %s).",
                            dataDesejada, janela.dataInicio(), janela.dataFim()));
        }
    }

    private static ConflitoRegraException conflito(String codigo, String mensagem) {
        return new ConflitoRegraException(codigo, mensagem);
    }
}