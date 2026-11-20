package UEPB.ESWII.ClinicalStudyPB.service;

import java.time.Clock;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import UEPB.ESWII.ClinicalStudyPB.controller.dto.DesfechoRequest;
import UEPB.ESWII.ClinicalStudyPB.controller.dto.VisitaAgendadaResponse;
import UEPB.ESWII.ClinicalStudyPB.domain.model.VisitaAgendada;
import UEPB.ESWII.ClinicalStudyPB.repository.VisitaAgendadaRepository;
import UEPB.ESWII.ClinicalStudyPB.service.DesfechoVisitaService.ComandoDesfecho;

/**
 * Consulta de uma visita agendada e registro do desfecho (RF13) para a camada web. Existe porque o
 * {@code spring.jpa.open-in-view} está desligado: o {@link VisitaAgendadaResponse} lê relações carregadas sob
 * demanda e por isso é montado aqui, dentro da transação. As regras ficam no {@link DesfechoVisitaService}.
 */
@Service
public class VisitaAgendadaService {

    private final VisitaAgendadaRepository visitaRepository;
    private final DesfechoVisitaService desfechoService;
    private final Clock clock;

    public VisitaAgendadaService(VisitaAgendadaRepository visitaRepository, DesfechoVisitaService desfechoService,
            Clock clock) {
        this.visitaRepository = visitaRepository;
        this.desfechoService = desfechoService;
        this.clock = clock;
    }

    @Transactional(readOnly = true)
    public VisitaAgendadaResponse buscar(UUID visitaAgendadaId) {
        VisitaAgendada visita = visitaRepository.findById(visitaAgendadaId)
                .orElseThrow(() -> new RecursoNaoEncontradoException(
                        "Visita agendada não encontrada: " + visitaAgendadaId));
        return VisitaAgendadaResponse.de(visita, clock);
    }

    @Transactional
    public VisitaAgendadaResponse registrarDesfecho(UUID visitaAgendadaId, DesfechoRequest dados, UUID usuarioId) {
        ComandoDesfecho comando = new ComandoDesfecho(dados.tipo(), dados.dataHoraRealizada(), dados.motivo(),
                dados.observacao());
        VisitaAgendada visita = desfechoService.registrar(visitaAgendadaId, comando, usuarioId);
        return VisitaAgendadaResponse.de(visita, clock);
    }
}
