package UEPB.ESWII.ClinicalStudyPB.service;

import java.util.UUID;

import org.springframework.stereotype.Component;

import UEPB.ESWII.ClinicalStudyPB.domain.enums.StatusEstudo;
import UEPB.ESWII.ClinicalStudyPB.domain.model.Estudo;
import UEPB.ESWII.ClinicalStudyPB.repository.EstudoRepository;

/**
 * Busca de estudo e verificação do estado exigido por cada operação, compartilhadas pelos serviços
 * de critérios, visitas, candidatos, avaliação, consentimento e inclusão.
 */
@Component
public class EstudoAcesso {

    private final EstudoRepository estudoRepository;

    public EstudoAcesso(EstudoRepository estudoRepository) {
        this.estudoRepository = estudoRepository;
    }

    public Estudo buscar(UUID id) {
        return estudoRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Estudo não encontrado: " + id));
    }

    /** Carrega o estudo com bloqueio de escrita até o fim da transação (ver {@link EstudoRepository#buscarComBloqueio}). */
    public Estudo buscarComBloqueio(UUID id) {
        return estudoRepository.buscarComBloqueio(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Estudo não encontrado: " + id));
    }

    /** Critérios e visitas só podem ser alterados com o estudo em planejamento. */
    public void exigirPlanejamento(Estudo estudo) {
        if (estudo.getStatus() != StatusEstudo.PLANEJAMENTO) {
            throw new ConflitoRegraException("ESTUDO_FORA_DE_PLANEJAMENTO",
                    "Critérios e visitas só podem ser alterados com o estudo em planejamento");
        }
    }

    /** Candidatos, avaliações, consentimentos e inclusões só existem com o estudo em recrutamento. */
    public void exigirRecrutamento(Estudo estudo) {
        if (estudo.getStatus() != StatusEstudo.RECRUTAMENTO) {
            throw new ConflitoRegraException("ESTUDO_FORA_DE_RECRUTAMENTO",
                    "Esta operação exige que o estudo esteja em recrutamento");
        }
    }
}
