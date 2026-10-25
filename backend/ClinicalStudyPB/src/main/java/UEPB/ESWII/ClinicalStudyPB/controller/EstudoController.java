package UEPB.ESWII.ClinicalStudyPB.controller;

import java.net.URI;
import java.util.List;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import UEPB.ESWII.ClinicalStudyPB.controller.dto.EstudoRequest;
import UEPB.ESWII.ClinicalStudyPB.domain.model.Estudo;
import UEPB.ESWII.ClinicalStudyPB.service.EstudoService;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/estudos")
public class EstudoController {

    private final EstudoService estudoService;

    public EstudoController(EstudoService estudoService) {
        this.estudoService = estudoService;
    }

    @GetMapping
    public List<Estudo> listarTodos() {
        return estudoService.listarTodos();
    }

    @GetMapping("/{id}")
    public Estudo buscarPorId(@PathVariable UUID id) {
        return estudoService.buscarPorId(id);
    }

    @PostMapping
    public ResponseEntity<Estudo> criar(@Valid @RequestBody EstudoRequest dados) {
        Estudo criado = estudoService.criar(dados);
        URI localizacao = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}").buildAndExpand(criado.getId()).toUri();
        return ResponseEntity.created(localizacao).body(criado);
    }

    @PutMapping("/{id}")
    public Estudo atualizar(@PathVariable UUID id, @Valid @RequestBody EstudoRequest dados) {
        return estudoService.atualizar(id, dados);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> excluir(@PathVariable UUID id) {
        estudoService.excluir(id);
        return ResponseEntity.noContent().build();
    }
}
