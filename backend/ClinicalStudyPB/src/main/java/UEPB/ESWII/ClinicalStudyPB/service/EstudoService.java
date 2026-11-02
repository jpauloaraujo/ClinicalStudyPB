package UEPB.ESWII.ClinicalStudyPB.service;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import UEPB.ESWII.ClinicalStudyPB.controller.dto.EstudoRequest;
import UEPB.ESWII.ClinicalStudyPB.domain.enums.PerfilUsuario;
import UEPB.ESWII.ClinicalStudyPB.domain.enums.StatusEstudo;
import UEPB.ESWII.ClinicalStudyPB.domain.model.Estudo;
import UEPB.ESWII.ClinicalStudyPB.domain.model.Usuario;
import UEPB.ESWII.ClinicalStudyPB.repository.CandidatoRepository;
import UEPB.ESWII.ClinicalStudyPB.repository.EstudoRepository;
import UEPB.ESWII.ClinicalStudyPB.repository.UsuarioRepository;

@Service
@Transactional
public class EstudoService {

    private final EstudoRepository estudoRepository;
    private final UsuarioRepository usuarioRepository;
    private final CandidatoRepository candidatoRepository;

    public EstudoService(EstudoRepository estudoRepository, UsuarioRepository usuarioRepository,
            CandidatoRepository candidatoRepository) {
        this.estudoRepository = estudoRepository;
        this.usuarioRepository = usuarioRepository;
        this.candidatoRepository = candidatoRepository;
    }

    @Transactional(readOnly = true)
    public List<Estudo> listarTodos() {
        return estudoRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Estudo buscarPorId(UUID id) {
        return estudoRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Estudo não encontrado: " + id));
    }

    public Estudo criar(EstudoRequest dados) {
        // RF07: o estado só muda pela máquina de estados (POST /api/estudos/{id}/transicoes)
        if (dados.status() != StatusEstudo.PLANEJAMENTO) {
            throw new ConflitoRegraException("ESTUDO_DEVE_COMECAR_EM_PLANEJAMENTO",
                    "Todo estudo começa em planejamento; use a transição de estado para avançá-lo");
        }
        Estudo estudo = new Estudo();
        preencher(estudo, dados);
        estudo.setId(null);
        return estudoRepository.save(estudo);
    }

    public Estudo atualizar(UUID id, EstudoRequest dados) {
        Estudo estudo = buscarPorId(id);
        if (dados.status() != estudo.getStatus()) {
            throw new ConflitoRegraException("ESTADO_ALTERADO_FORA_DA_TRANSICAO",
                    "O estado do estudo só muda pela transição de estado (POST /api/estudos/{id}/transicoes)");
        }
        preencher(estudo, dados);
        return estudoRepository.save(estudo);
    }

    public void excluir(UUID id) {
        Estudo estudo = buscarPorId(id);
        // A FK candidatos.estudo_id é ON DELETE RESTRICT; sem esta checagem a API responderia 500
        if (candidatoRepository.existsByEstudoId(id)) {
            throw new ConflitoRegraException("ESTUDO_COM_CANDIDATOS",
                    "Não é possível excluir: o estudo já tem candidatos cadastrados");
        }
        estudoRepository.delete(estudo);
    }

    private void preencher(Estudo estudo, EstudoRequest dados) {
        if (dados.dataInicio() != null && dados.dataFim() != null
                && dados.dataFim().isBefore(dados.dataInicio())) {
            throw new RegraNegocioException("A data final não pode ser anterior à data inicial");
        }
        Usuario responsavel = usuarioRepository.findById(dados.responsavelId())
                .orElseThrow(() -> new RecursoNaoEncontradoException(
                        "Responsável não encontrado: " + dados.responsavelId()));
        if (responsavel.getPerfil() != PerfilUsuario.PESQUISADOR) {
            throw new RegraNegocioException("O responsável pelo estudo deve ter o perfil de pesquisador");
        }
        estudo.setTitulo(dados.titulo());
        estudo.setObjetivo(dados.objetivo());
        estudo.setResponsavel(responsavel);
        estudo.setDataInicio(dados.dataInicio());
        estudo.setDataFim(dados.dataFim());
        estudo.setLimiteParticipantes(dados.limiteParticipantes());
    }
}
