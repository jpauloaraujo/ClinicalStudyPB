package UEPB.ESWII.ClinicalStudyPB.domain.regra;

import java.time.LocalDate;

import UEPB.ESWII.ClinicalStudyPB.domain.model.VisitaPlanejada;

public record JanelaDeAgendamento(LocalDate dataInicio, LocalDate dataFim, LocalDate dataBase) {

    // Método de instância para verificar se uma data está dentro da janela
    public boolean contem(LocalDate data) {
        return !data.isBefore(dataInicio) && !data.isAfter(dataFim);
    }

    // Factory method estático para construir a janela
    public static JanelaDeAgendamento de(VisitaPlanejada visitaPlanejada, LocalDate dataInclusao) {
        LocalDate dataBase = visitaPlanejada.dataPrevista(dataInclusao);
        return new JanelaDeAgendamento(
            dataBase.minusDays(visitaPlanejada.getTolerancia()),
            dataBase.plusDays(visitaPlanejada.getTolerancia()),
            dataBase
        );
    }
}