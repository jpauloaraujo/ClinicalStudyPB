package UEPB.ESWII.ClinicalStudyPB.controller.dto;

import java.time.LocalDate;

import io.swagger.v3.oas.annotations.media.Schema;

/** Dados da retirada que o app pode ver: só a data. O motivo da retirada nunca é exposto (T109, RQ-02). */
@Schema(description = "Retirada visível ao app: só a data; o motivo não é exposto")
public record RetiradaDoMeuEstudo(LocalDate dataRetirada) {
}
