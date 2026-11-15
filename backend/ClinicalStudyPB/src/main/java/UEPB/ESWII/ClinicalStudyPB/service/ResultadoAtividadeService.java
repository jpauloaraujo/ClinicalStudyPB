package UEPB.ESWII.ClinicalStudyPB.service;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import UEPB.ESWII.ClinicalStudyPB.controller.dto.ResultadoAtividadeResponse;
import UEPB.ESWII.ClinicalStudyPB.controller.dto.ResultadosDaVisitaResponse;
import UEPB.ESWII.ClinicalStudyPB.domain.enums.BloqueioResultados;
import UEPB.ESWII.ClinicalStudyPB.domain.enums.CategoriaAcao;
import UEPB.ESWII.ClinicalStudyPB.domain.enums.SituacaoVisita;
import UEPB.ESWII.ClinicalStudyPB.domain.enums.StatusEstudo;
import UEPB.ESWII.ClinicalStudyPB.domain.model.Acao;
import UEPB.ESWII.ClinicalStudyPB.domain.model.ResultadoAtividade;
import UEPB.ESWII.ClinicalStudyPB.domain.model.Usuario;
import UEPB.ESWII.ClinicalStudyPB.domain.model.VisitaAgendada;
import UEPB.ESWII.ClinicalStudyPB.repository.AcaoRepository;
import UEPB.ESWII.ClinicalStudyPB.repository.ResultadoAtividadeRepository;
import UEPB.ESWII.ClinicalStudyPB.repository.UsuarioRepository;
import UEPB.ESWII.ClinicalStudyPB.repository.VisitaAgendadaRepository;

/**
 * Regras dos resultados fictícios das avaliações de uma visita (RF14, regra 4; T89).
 *
 * <p>Os resultados são <strong>fictícios</strong> (LGPD). O pesquisador registra um resultado por vez (resultado
 * parcial) para cada atividade da categoria {@code AVALIACAO_CLINICA} da visita prevista de uma visita
 * {@code REALIZADA}, e pode editá-lo enquanto o estudo não estiver encerrado. A permissão por perfil (só o pesquisador
 * escreve) é do controller (T90): este serviço não conhece o perfil e recebe o usuário por parâmetro.</p>
 */
@Service
public class ResultadoAtividadeService {

    private final VisitaAgendadaRepository visitasAgendadas;
    private final AcaoRepository acoes;
    private final ResultadoAtividadeRepository resultados;
    private final UsuarioRepository usuarios;
    private final Clock clock;

    public ResultadoAtividadeService(VisitaAgendadaRepository visitasAgendadas, AcaoRepository acoes,
            ResultadoAtividadeRepository resultados, UsuarioRepository usuarios, Clock clock) {
        this.visitasAgendadas = visitasAgendadas;
        this.acoes = acoes;
        this.resultados = resultados;
        this.usuarios = usuarios;
        this.clock = clock;
    }

    /** Valores informados pelo pesquisador para o resultado de uma atividade. */
    public record ComandoResultado(String valor, String unidade, String observacao) {
    }

    /** Uma atividade de avaliação da visita prevista e o seu resultado ({@code null} enquanto estiver pendente). */
    public record AtividadeComResultado(Acao acao, ResultadoAtividade resultado) {

        public boolean registrado() {
            return resultado != null;
        }
    }

    /**
     * Atividades de avaliação de uma visita agendada, com as contagens e o motivo do modo somente leitura
     * ({@code bloqueio} nulo quando a visita aceita registro e edição).
     */
    public record ResultadosDaVisita(VisitaAgendada visitaAgendada, BloqueioResultados bloqueio,
            List<AtividadeComResultado> atividades) {

        public int total() {
            return atividades.size();
        }

        public int registrados() {
            return (int) atividades.stream().filter(AtividadeComResultado::registrado).count();
        }

        public int pendentes() {
            return total() - registrados();
        }
    }

    /** Resultado salvo e se ele foi criado agora (o controller responde 201) ou editado (200). */
    public record ResultadoRegistrado(ResultadoAtividade resultado, boolean criado) {
    }

    /**
     * Lista todas as atividades {@code AVALIACAO_CLINICA} da visita prevista, ordenadas pelo nome, cada uma com o seu
     * resultado ou pendente. Não exige visita realizada: em vez de recusar, informa o {@code bloqueio}.
     */
    @Transactional(readOnly = true)
    public ResultadosDaVisita consultar(UUID visitaAgendadaId) {
        VisitaAgendada visita = buscarVisita(visitaAgendadaId);

        List<Acao> avaliacoes = acoes.findByVisitaIdAndCategoriaOrderByNomeAsc(
                visita.getVisitaPlanejada().getId(), CategoriaAcao.AVALIACAO_CLINICA);
        Map<UUID, ResultadoAtividade> porAtividade = resultados.findByVisitaAgendadaId(visitaAgendadaId).stream()
                .collect(Collectors.toMap(r -> r.getAcao().getId(), Function.identity()));

        List<AtividadeComResultado> atividades = avaliacoes.stream()
                .map(acao -> new AtividadeComResultado(acao, porAtividade.get(acao.getId())))
                .toList();
        return new ResultadosDaVisita(visita, bloqueio(visita), atividades);
    }

    /**
     * Registra o resultado de uma atividade ou, se ela já tiver resultado, o edita. As verificações seguem a ordem do
     * design da T89 (RQ-T89-01); cada falha tem o seu código.
     */
    @Transactional
    public ResultadoRegistrado registrar(UUID visitaAgendadaId, UUID acaoId, ComandoResultado comando,
            UUID usuarioId) {
        // (1) visita agendada e atividade existem
        VisitaAgendada visita = buscarVisita(visitaAgendadaId);
        Acao acao = acoes.findById(acaoId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Atividade não encontrada: " + acaoId));

        // (2) só visita realizada tem resultado
        if (visita.getSituacao() != SituacaoVisita.REALIZADA) {
            throw new ConflitoRegraException("VISITA_NAO_REALIZADA",
                    "Resultados só podem ser registrados em visita realizada");
        }
        // (3) a atividade é da visita prevista desta visita agendada
        if (!acao.getVisita().getId().equals(visita.getVisitaPlanejada().getId())) {
            throw new ConflitoRegraException("ATIVIDADE_NAO_PERTENCE_A_VISITA",
                    "A atividade não pertence à visita prevista desta visita agendada");
        }
        // (4) só atividades de avaliação clínica recebem resultado
        if (acao.getCategoria() != CategoriaAcao.AVALIACAO_CLINICA) {
            throw new ConflitoRegraException("ATIVIDADE_FORA_DA_CATEGORIA_AVALIACAO",
                    "Só atividades da categoria avaliação clínica recebem resultado");
        }
        // (5) estudo encerrado não aceita novos resultados nem edições
        if (estudoEncerrado(visita)) {
            throw new ConflitoRegraException("ESTUDO_ENCERRADO",
                    "O estudo está encerrado e não aceita novos resultados");
        }
        exigirParticipacaoAtiva(visita);
        // (6) valor obrigatório
        String valor = limpar(comando == null ? null : comando.valor());
        if (valor == null) {
            throw new ValidacaoRegraException("VALOR_OBRIGATORIO", "O valor do resultado é obrigatório");
        }

        Usuario usuario = usuarios.getReferenceById(usuarioId);
        LocalDateTime agora = LocalDateTime.now(clock);
        ResultadoAtividade resultado = resultados.findByVisitaAgendadaIdAndAcaoId(visitaAgendadaId, acaoId)
                .orElse(null);
        boolean criado = resultado == null;
        if (criado) {
            resultado = new ResultadoAtividade();
            resultado.setVisitaAgendada(visita);
            resultado.setAcao(acao);
            resultado.setRegistradoPor(usuario);
            resultado.setRegistradoEm(agora);
        }
        resultado.setValor(valor);
        resultado.setUnidade(limpar(comando.unidade()));
        resultado.setObservacao(limpar(comando.observacao()));
        resultado.setAlteradoPor(usuario);
        resultado.setAlteradoEm(agora);
        return new ResultadoRegistrado(resultados.save(resultado), criado);
    }

    /** Resposta da API para o registro: o resultado salvo e se ele foi criado agora (201) ou editado (200). */
    public record RespostaDoRegistro(ResultadoAtividadeResponse resultado, boolean criado) {
    }

    /**
     * {@link #consultar(UUID)} já convertido para a resposta da API (T90). A conversão fica dentro da transação
     * porque lê relações LAZY (nomes de quem registrou e alterou) e o projeto usa {@code open-in-view=false}.
     */
    @Transactional(readOnly = true)
    public ResultadosDaVisitaResponse consultarResposta(UUID visitaAgendadaId) {
        return ResultadosDaVisitaResponse.de(consultar(visitaAgendadaId), clock.getZone());
    }

    /** {@link #registrar} já convertido para a resposta da API (T90), dentro da mesma transação. */
    @Transactional
    public RespostaDoRegistro registrarResposta(UUID visitaAgendadaId, UUID acaoId, ComandoResultado comando,
            UUID usuarioId) {
        ResultadoRegistrado registrado = registrar(visitaAgendadaId, acaoId, comando, usuarioId);
        return new RespostaDoRegistro(ResultadoAtividadeResponse.de(registrado.resultado(), clock.getZone()),
                registrado.criado());
    }

    /**
     * Ponto único da regra do participante retirado: a T102 preenche este método com 409
     * {@code PARTICIPANTE_RETIRADO} (e a consulta passa a devolver o bloqueio correspondente). Hoje não existe
     * situação de retirada na participação, então nada é verificado.
     */
    private void exigirParticipacaoAtiva(VisitaAgendada visita) {
        // T102: recusar com ConflitoRegraException("PARTICIPANTE_RETIRADO", ...) quando a participação estiver retirada.
    }

    /** Motivo do modo somente leitura, por prioridade: visita não realizada, estudo encerrado (e, na T102, retirada). */
    private BloqueioResultados bloqueio(VisitaAgendada visita) {
        if (visita.getSituacao() != SituacaoVisita.REALIZADA) {
            return BloqueioResultados.VISITA_NAO_REALIZADA;
        }
        if (estudoEncerrado(visita)) {
            return BloqueioResultados.ESTUDO_ENCERRADO;
        }
        return null;
    }

    private static boolean estudoEncerrado(VisitaAgendada visita) {
        return visita.getVisitaPlanejada().getEstudo().getStatus() == StatusEstudo.ENCERRADO;
    }

    private VisitaAgendada buscarVisita(UUID visitaAgendadaId) {
        return visitasAgendadas.findById(visitaAgendadaId).orElseThrow(
                () -> new RecursoNaoEncontradoException("Visita agendada não encontrada: " + visitaAgendadaId));
    }

    /** {@code trim}; texto vazio ou só com espaços vira {@code null}. */
    private static String limpar(String texto) {
        if (texto == null) {
            return null;
        }
        String limpo = texto.trim();
        return limpo.isEmpty() ? null : limpo;
    }
}
