package UEPB.ESWII.ClinicalStudyPB.service;

import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import UEPB.ESWII.ClinicalStudyPB.controller.dto.ConsentimentoRequest;
import UEPB.ESWII.ClinicalStudyPB.controller.dto.ConsentimentoResponse;
import UEPB.ESWII.ClinicalStudyPB.domain.enums.SituacaoCandidato;
import UEPB.ESWII.ClinicalStudyPB.domain.model.Candidato;
import UEPB.ESWII.ClinicalStudyPB.domain.model.Consentimento;
import UEPB.ESWII.ClinicalStudyPB.repository.ConsentimentoRepository;

/**
 * RF10: consentimento simulado (TCLE). É registrado independentemente da avaliação de elegibilidade:
 * a inclusão é que exige os dois.
 */
@Service
@Transactional
public class ConsentimentoService {

    private final ConsentimentoRepository consentimentoRepository;
    private final CandidatoService candidatoService;
    private final EstudoAcesso estudoAcesso;
    private final UsuarioAutenticado usuarioAutenticado;
    private final String versaoPadraoDoTermo;

    public ConsentimentoService(ConsentimentoRepository consentimentoRepository, CandidatoService candidatoService,
            EstudoAcesso estudoAcesso, UsuarioAutenticado usuarioAutenticado,
            @Value("${app.consentimento.versao-termo:TCLE-SIMULADO v1}") String versaoPadraoDoTermo) {
        this.consentimentoRepository = consentimentoRepository;
        this.candidatoService = candidatoService;
        this.estudoAcesso = estudoAcesso;
        this.usuarioAutenticado = usuarioAutenticado;
        this.versaoPadraoDoTermo = versaoPadraoDoTermo;
    }

    public ConsentimentoResponse registrar(UUID candidatoId, ConsentimentoRequest dados, UUID responsavelId) {
        Candidato candidato = candidatoService.buscarEntidade(candidatoId);
        estudoAcesso.exigirRecrutamento(candidato.getEstudo());
        if (candidato.estaIncluido()) {
            throw new ConflitoRegraException("CANDIDATO_JA_INCLUIDO",
                    "O candidato já foi incluído no estudo; o consentimento não pode mais ser registrado");
        }
        if (consentimentoRepository.existsByCandidatoId(candidatoId)) {
            throw new ConflitoRegraException("CONSENTIMENTO_DUPLICADO",
                    "O consentimento deste candidato já foi registrado");
        }

        LocalDateTime agora = LocalDateTime.now();
        LocalDateTime dataHora = dados.dataHora() == null ? agora : dados.dataHora();
        if (dataHora.isAfter(agora)) {
            throw new RegraNegocioException("A data e hora do consentimento não pode ser futura");
        }

        Consentimento consentimento = new Consentimento();
        consentimento.setCandidato(candidato);
        consentimento.setResponsavel(usuarioAutenticado.buscar(responsavelId));
        consentimento.setDataHora(dataHora);
        consentimento.setVersaoTermo(dados.versaoTermo() == null || dados.versaoTermo().isBlank()
                ? versaoPadraoDoTermo : dados.versaoTermo().trim());
        consentimentoRepository.save(consentimento);

        if (candidato.getSituacao() == SituacaoCandidato.ELEGIVEL) {
            candidato.setSituacao(SituacaoCandidato.CONSENTIDO);
        }
        return ConsentimentoResponse.de(consentimento);
    }

    @Transactional(readOnly = true)
    public ConsentimentoResponse buscar(UUID candidatoId) {
        candidatoService.buscarEntidade(candidatoId);
        return consentimentoRepository.findByCandidatoId(candidatoId)
                .map(ConsentimentoResponse::de)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Consentimento não registrado para o candidato"));
    }
}
