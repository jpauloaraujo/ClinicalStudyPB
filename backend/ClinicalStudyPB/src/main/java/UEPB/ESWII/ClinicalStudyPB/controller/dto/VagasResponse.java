package UEPB.ESWII.ClinicalStudyPB.controller.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Resumo das vagas de um estudo")
public record VagasResponse(long incluidos, int limite, long restantes) {

    public static VagasResponse de(long incluidos, int limite) {
        return new VagasResponse(incluidos, limite, Math.max(0, limite - incluidos));
    }
}
