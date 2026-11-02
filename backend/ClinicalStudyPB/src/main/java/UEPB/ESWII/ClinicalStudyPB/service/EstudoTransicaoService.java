package UEPB.ESWII.ClinicalStudyPB.service;

import java.time.LocalDateTime;
import java.util.UUID;

import org.hibernate.Hibernate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import UEPB.ESWII.ClinicalStudyPB.domain.enums.StatusEstudo;
import UEPB.ESWII.ClinicalStudyPB.domain.model.Estudo;
import UEPB.ESWII.ClinicalStudyPB.domain.model.HistoricoStatusEstudo;
import UEPB.ESWII.ClinicalStudyPB.domain.model.Usuario;
import UEPB.ESWII.ClinicalStudyPB.domain.regra.MaquinaEstadosEstudo;
import UEPB.ESWII.ClinicalStudyPB.repository.CriterioRepository;
import UEPB.ESWII.ClinicalStudyPB.repository.EstudoRepository;
import UEPB.ESWII.ClinicalStudyPB.repository.HistoricoStatusEstudoRepository;
import UEPB.ESWII.ClinicalStudyPB.repository.VisitaPlanejadaRepository;

/**
 * RF07: avança o estado de um estudo seguindo a {@link MaquinaEstadosEstudo} e registra cada
 * mudança (estado anterior, novo estado, usuário e data) em {@code historico_status_estudo}.
 *
 * <p>O endpoint de transição (T42) só precisa chamar {@link #avancar}: a autorização por perfil
 * (só o pesquisador avança) fica no controller, e as regras de negócio ficam aqui e na máquina.</p>
 */
@Service
@Transactional
public class EstudoTransicaoService {

    private final EstudoAcesso estudoAcesso;
    private final EstudoRepository estudoRepository;
    private final CriterioRepository criterioRepository;
    private final VisitaPlanejadaRepository visitaRepository;
    private final HistoricoStatusEstudoRepository historicoRepository;
    private final UsuarioAutenticado usuarioAutenticado;

    public EstudoTransicaoService(EstudoAcesso estudoAcesso, EstudoRepository estudoRepository,
            CriterioRepository criterioRepository, VisitaPlanejadaRepository visitaRepository,
            HistoricoStatusEstudoRepository historicoRepository, UsuarioAutenticado usuarioAutenticado) {
        this.estudoAcesso = estudoAcesso;
        this.estudoRepository = estudoRepository;
        this.criterioRepository = criterioRepository;
        this.visitaRepository = visitaRepository;
        this.historicoRepository = historicoRepository;
        this.usuarioAutenticado = usuarioAutenticado;
    }

    /**
     * Muda o estudo para {@code destino}, que precisa ser o estado seguinte ao atual.
     *
     * @param usuarioId quem pediu a mudança (claim {@code sub} do token); fica no histórico
     * @return o estudo já no novo estado, com o responsável carregado para a resposta da API
     * @throws RecursoNaoEncontradoException estudo (ou usuário) inexistente (404)
     * @throws ConflitoRegraException        transição fora da sequência ou pré-condição não atendida (409)
     */
    public Estudo avancar(UUID estudoId, StatusEstudo destino, UUID usuarioId) {
        // Com o estudo bloqueado, dois avanços simultâneos são atendidos um de cada vez: o segundo
        // já encontra o novo estado e é recusado como transição inválida
        Estudo estudo = estudoAcesso.buscarComBloqueio(estudoId);
        StatusEstudo origem = estudo.getStatus();

        MaquinaEstadosEstudo.validar(origem, destino, criterioRepository.contarInclusaoPorEstudo(estudoId),
                visitaRepository.countByEstudoId(estudoId));
        Usuario usuario = usuarioAutenticado.buscar(usuarioId);

        estudo.setStatus(destino);
        Estudo salvo = estudoRepository.save(estudo);

        HistoricoStatusEstudo registro = new HistoricoStatusEstudo();
        registro.setEstudo(salvo);
        registro.setEstadoAnterior(origem);
        registro.setEstadoNovo(destino);
        registro.setUsuario(usuario);
        registro.setDataHora(LocalDateTime.now());
        historicoRepository.save(registro);

        // O responsável é lazy e a sessão fecha ao sair daqui (open-in-view desligado)
        Hibernate.initialize(salvo.getResponsavel());
        return salvo;
    }
}
