package UEPB.ESWII.ClinicalStudyPB.service;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import UEPB.ESWII.ClinicalStudyPB.domain.enums.SituacaoVisita;
import UEPB.ESWII.ClinicalStudyPB.domain.enums.SolicitanteRetirada;
import UEPB.ESWII.ClinicalStudyPB.domain.enums.StatusEstudo;
import UEPB.ESWII.ClinicalStudyPB.domain.enums.TipoDesfecho;
import UEPB.ESWII.ClinicalStudyPB.domain.model.Participacao;
import UEPB.ESWII.ClinicalStudyPB.domain.model.RetiradaParticipacao;
import UEPB.ESWII.ClinicalStudyPB.domain.model.Usuario;
import UEPB.ESWII.ClinicalStudyPB.domain.model.VisitaAgendada;
import UEPB.ESWII.ClinicalStudyPB.domain.regra.MaquinaEstadosVisita;
import UEPB.ESWII.ClinicalStudyPB.domain.regra.MaquinaEstadosVisita.DadosDesfecho;
import UEPB.ESWII.ClinicalStudyPB.repository.ParticipacaoRepository;
import UEPB.ESWII.ClinicalStudyPB.repository.RetiradaParticipacaoRepository;
import UEPB.ESWII.ClinicalStudyPB.repository.VisitaAgendadaRepository;

/**
 * RF16 (regra 6 da Sprint 3; T102): regras de domínio da retirada de participante, executadas em uma
 * <strong>única transação atômica</strong>.
 *
 * <h2>Ordem das verificações</h2>
 * <ol>
 *   <li>a participação existe (carregada com bloqueio de escrita {@code PESSIMISTIC_WRITE}, 404 se inexistente);</li>
 *   <li>o participante está {@code ATIVO} (409 {@code PARTICIPANTE_JA_RETIRADO}; a retirada é irreversível);</li>
 *   <li>o estudo não está encerrado (409 {@code ESTUDO_ENCERRADO});</li>
 *   <li>o motivo é obrigatório com ao menos 5 caracteres depois do {@code trim} (400 {@code MOTIVO_OBRIGATORIO});</li>
 *   <li>a data da retirada é obrigatória e não pode ser futura (400 {@code DATA_RETIRADA_FUTURA});</li>
 *   <li>quem solicitou é obrigatório (400 {@code VALIDACAO_CAMPOS}).</li>
 * </ol>
 *
 * <h2>Efeitos e decisões registradas</h2>
 * <ul>
 *   <li>Grava {@link RetiradaParticipacao}, muda {@link Participacao} para {@code RETIRADO} e cancela as visitas
 *       futuras ({@code dataHoraAgendada > agora}) ainda {@code AGENDADA} usando {@link MaquinaEstadosVisita} com o
 *       motivo automático {@value #MOTIVO_CANCELAMENTO_RETIRADA}.</li>
 *   <li><strong>Visitas vencidas (RQ-T102-13):</strong> visitas {@code AGENDADA} cuja data e hora já passaram
 *       ({@code dataHoraAgendada <= agora}) <strong>não</strong> são canceladas pela retirada. Como o desfecho é
 *       bloqueado após a retirada, o coordenador deve registrá-las antes de retirar o participante (a tela da T104
 *       avisa quando existirem).</li>
 *   <li><strong>Preservação e vagas:</strong> visitas já finalizadas ({@code REALIZADA}, {@code AUSENTE},
 *       {@code CANCELADA}) e resultados permanecem intactos; a vaga continua ocupada (a contagem de incluídos do
 *       RF11 não diminui) e não existe operação para desfazer a retirada.</li>
 *   <li><strong>Independência da camada web:</strong> recebe um {@link ComandoRetirada} simples e o id do usuário,
 *       sem conhecer {@code HttpServletRequest} nem {@code Jwt}, permitindo reuso pelo pedido de retirada no app
 *       (RF20, Sprint 4).</li>
 * </ul>
 */
@Service
public class RetiradaService {

    public static final String MOTIVO_CANCELAMENTO_RETIRADA = "retirada do estudo";
    public static final String PARTICIPANTE_JA_RETIRADO = "PARTICIPANTE_JA_RETIRADO";
    public static final String PARTICIPANTE_RETIRADO = "PARTICIPANTE_RETIRADO";
    public static final String ESTUDO_ENCERRADO = "ESTUDO_ENCERRADO";
    public static final String MOTIVO_OBRIGATORIO = "MOTIVO_OBRIGATORIO";
    public static final String DATA_RETIRADA_FUTURA = "DATA_RETIRADA_FUTURA";
    public static final int MOTIVO_TAMANHO_MINIMO = 5;

    /**
     * Dados do pedido de retirada, independentes da camada HTTP.
     *
     * @param motivo       obrigatório, ao menos 5 caracteres após {@code trim}
     * @param solicitante  quem solicitou ({@code PARTICIPANTE}, {@code PESQUISADOR} ou {@code COORDENACAO})
     * @param dataRetirada data da retirada, nunca futura
     */
    public record ComandoRetirada(String motivo, SolicitanteRetirada solicitante, LocalDate dataRetirada) {
    }

    /** Retirada persistida junto à contagem de visitas futuras canceladas pelo motivo da retirada. */
    public record ResultadoRetirada(RetiradaParticipacao retirada, int visitasCanceladas) {
    }

    private final ParticipacaoRepository participacoes;
    private final RetiradaParticipacaoRepository retiradas;
    private final VisitaAgendadaRepository visitasAgendadas;
    private final UsuarioAutenticado usuarioAutenticado;
    private final Clock clock;

    public RetiradaService(ParticipacaoRepository participacoes, RetiradaParticipacaoRepository retiradas,
            VisitaAgendadaRepository visitasAgendadas, UsuarioAutenticado usuarioAutenticado, Clock clock) {
        this.participacoes = participacoes;
        this.retiradas = retiradas;
        this.visitasAgendadas = visitasAgendadas;
        this.usuarioAutenticado = usuarioAutenticado;
        this.clock = clock;
    }

    /**
     * Registra a retirada de um participante e cancela suas visitas futuras {@code AGENDADA} na mesma transação.
     *
     * @param participacaoId identificador da participação
     * @param comando        motivo, solicitante e data da retirada
     * @param usuarioId      usuário responsável pelo registro
     * @return a retirada gravada e a quantidade de visitas futuras canceladas
     */
    @Transactional
    public ResultadoRetirada registrar(UUID participacaoId, ComandoRetirada comando, UUID usuarioId) {
        Participacao participacao = participacoes.buscarComBloqueio(participacaoId)
                .orElseThrow(() -> new RecursoNaoEncontradoException(
                        "Participação não encontrada: " + participacaoId));

        if (participacao.estaRetirada()) {
            throw new ConflitoRegraException(PARTICIPANTE_JA_RETIRADO,
                    "O participante já foi retirado do estudo");
        }
        if (participacao.getEstudo().getStatus() == StatusEstudo.ENCERRADO) {
            throw new ConflitoRegraException(ESTUDO_ENCERRADO,
                    "Não é possível registrar retirada em um estudo encerrado");
        }

        String motivoAparado = validarMotivo(comando == null ? null : comando.motivo());
        LocalDate hoje = LocalDate.now(clock);
        LocalDate dataRetirada = validarDataRetirada(comando == null ? null : comando.dataRetirada(), hoje);
        SolicitanteRetirada solicitante = validarSolicitante(comando == null ? null : comando.solicitante());

        Usuario usuario = usuarioAutenticado.buscar(usuarioId);
        LocalDateTime agora = LocalDateTime.now(clock);

        RetiradaParticipacao retirada = new RetiradaParticipacao();
        retirada.setParticipacao(participacao);
        retirada.setMotivo(motivoAparado);
        retirada.setSolicitante(solicitante);
        retirada.setDataRetirada(dataRetirada);
        retirada.setRegistradoPor(usuario);
        retirada.setRegistradoEm(agora);
        RetiradaParticipacao salva = retiradas.saveAndFlush(retirada);

        participacao.retirar();
        participacoes.saveAndFlush(participacao);

        List<VisitaAgendada> futuras = visitasAgendadas.listarFuturasPorSituacao(
                participacaoId, SituacaoVisita.AGENDADA, agora);
        for (VisitaAgendada visita : futuras) {
            cancelarVisitaPorRetirada(visita, usuario, agora);
        }

        return new ResultadoRetirada(salva, futuras.size());
    }

    /**
     * Consulta o registro de retirada de uma participação, junto ao total de visitas canceladas por ela.
     *
     * @throws RecursoNaoEncontradoException se a participação não existir ou ainda estiver ativa (sem retirada)
     */
    @Transactional(readOnly = true)
    public ResultadoRetirada buscar(UUID participacaoId) {
        if (!participacoes.existsById(participacaoId)) {
            throw new RecursoNaoEncontradoException("Participação não encontrada: " + participacaoId);
        }
        RetiradaParticipacao retirada = retiradas.findByParticipacaoId(participacaoId)
                .orElseThrow(() -> new RecursoNaoEncontradoException(
                        "Retirada não encontrada para a participação: " + participacaoId));
        int canceladas = Math.toIntExact(visitasAgendadas.countByParticipacaoIdAndSituacaoAndMotivo(
                participacaoId, SituacaoVisita.CANCELADA, MOTIVO_CANCELAMENTO_RETIRADA));
        return new ResultadoRetirada(retirada, canceladas);
    }

    private void cancelarVisitaPorRetirada(VisitaAgendada visita, Usuario usuario, LocalDateTime agora) {
        MaquinaEstadosVisita.validarTransicao(visita.getSituacao(), TipoDesfecho.CANCELADA);
        MaquinaEstadosVisita.validarDados(TipoDesfecho.CANCELADA, null, MOTIVO_CANCELAMENTO_RETIRADA,
                visita.getObservacao(), agora);
        DadosDesfecho dados = MaquinaEstadosVisita.dadosParaGravar(TipoDesfecho.CANCELADA, null,
                MOTIVO_CANCELAMENTO_RETIRADA, visita.getObservacao(), agora);

        visita.setSituacao(MaquinaEstadosVisita.destinoDe(TipoDesfecho.CANCELADA));
        visita.setDataHoraRealizada(dados.dataHoraRealizada());
        visita.setMotivo(dados.motivo());
        visita.setObservacao(dados.observacao());
        visita.setAlteradoPor(usuario);
        visita.setAlteradoEm(agora);
        visitasAgendadas.saveAndFlush(visita);
    }

    private static String validarMotivo(String motivo) {
        if (motivo == null || motivo.trim().length() < MOTIVO_TAMANHO_MINIMO) {
            throw new ValidacaoRegraException(MOTIVO_OBRIGATORIO,
                    "Informe o motivo da retirada, com ao menos " + MOTIVO_TAMANHO_MINIMO + " caracteres");
        }
        return motivo.trim();
    }

    private static LocalDate validarDataRetirada(LocalDate dataRetirada, LocalDate hoje) {
        if (dataRetirada == null) {
            throw new ValidacaoRegraException("VALIDACAO_CAMPOS", "dataRetirada: A data da retirada é obrigatória");
        }
        if (dataRetirada.isAfter(hoje)) {
            throw new ValidacaoRegraException(DATA_RETIRADA_FUTURA,
                    "A data da retirada não pode estar no futuro");
        }
        return dataRetirada;
    }

    private static SolicitanteRetirada validarSolicitante(SolicitanteRetirada solicitante) {
        if (solicitante == null) {
            throw new ValidacaoRegraException("VALIDACAO_CAMPOS", "solicitante: Informe quem solicitou a retirada");
        }
        return solicitante;
    }
}
