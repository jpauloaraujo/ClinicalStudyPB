package UEPB.ESWII.ClinicalStudyPB.service;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import UEPB.ESWII.ClinicalStudyPB.controller.dto.EstudoRequest;
import UEPB.ESWII.ClinicalStudyPB.domain.model.Estudo;
import UEPB.ESWII.ClinicalStudyPB.domain.model.Usuario;
import UEPB.ESWII.ClinicalStudyPB.repository.EstudoRepository;
import UEPB.ESWII.ClinicalStudyPB.repository.UsuarioRepository;

@Service
@Transactional
public class EstudoService {

    private final EstudoRepository estudoRepository;
    private final UsuarioRepository usuarioRepository;

    public EstudoService(EstudoRepository estudoRepository, UsuarioRepository usuarioRepository) {
        this.estudoRepository = estudoRepository;
        this.usuarioRepository = usuarioRepository;
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
        Estudo estudo = new Estudo();
        preencher(estudo, dados);
        estudo.setId(null);
        return estudoRepository.save(estudo);
    }

    public Estudo atualizar(UUID id, EstudoRequest dados) {
        Estudo estudo = buscarPorId(id);
        preencher(estudo, dados);
        return estudoRepository.save(estudo);
    }

    public void excluir(UUID id) {
        estudoRepository.delete(buscarPorId(id));
    }

    private void preencher(Estudo estudo, EstudoRequest dados) {
        if (dados.dataInicio() != null && dados.dataFim() != null
                && dados.dataFim().isBefore(dados.dataInicio())) {
            throw new RegraNegocioException("A data final não pode ser anterior à data inicial");
        }
        Usuario responsavel = usuarioRepository.findById(dados.responsavelId())
                .orElseThrow(() -> new RecursoNaoEncontradoException(
                        "Responsável não encontrado: " + dados.responsavelId()));
        estudo.setTitulo(dados.titulo());
        estudo.setObjetivo(dados.objetivo());
        estudo.setResponsavel(responsavel);
        estudo.setDataInicio(dados.dataInicio());
        estudo.setDataFim(dados.dataFim());
        estudo.setLimiteParticipantes(dados.limiteParticipantes());
        estudo.setStatus(dados.status());
    }
}
