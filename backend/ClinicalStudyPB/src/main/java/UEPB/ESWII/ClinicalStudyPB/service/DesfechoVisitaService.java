package UEPB.ESWII.ClinicalStudyPB.service;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

import org.springframework.dao.ConcurrencyFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import UEPB.ESWII.ClinicalStudyPB.domain.enums.StatusEstudo;
import UEPB.ESWII.ClinicalStudyPB.domain.enums.TipoDesfecho;
import UEPB.ESWII.ClinicalStudyPB.domain.model.Usuario;
import UEPB.ESWII.ClinicalStudyPB.domain.model.VisitaAgendada;
import UEPB.ESWII.ClinicalStudyPB.domain.regra.MaquinaEstadosVisita;
import UEPB.ESWII.ClinicalStudyPB.domain.regra.MaquinaEstadosVisita.DadosDesfecho;
import UEPB.ESWII.ClinicalStudyPB.repository.VisitaAgendadaRepository;

/**
 * RF13: registra o desfecho de uma visita agendada (realizada, ausente ou cancelada) aplicando a
 * {@link MaquinaEstadosVisita}, as pré-condições do estudo e a regra da data agendada, e gravando quem registrou e
 * quando.
 *
 * <h2>Ordem das verificações</h2>
 * <ol>
 *   <li>a visita existe (404);</li>
 *   <li>o estudo está em recrutamento ou execução (409 {@code ESTUDO_FORA_DE_RECRUTAMENTO_OU_EXECUCAO}); o
 *       encerrado bloqueia qualquer desfecho (regra 1);</li>
 *   <li>o participante está ativo ({@code exigirParticipacaoAtiva}, preenchido na T102);</li>
 *   <li>a transição é permitida: a visita precisa estar {@code AGENDADA} (409
 *       {@code TRANSICAO_VISITA_INVALIDA});</li>
 *   <li>os dados do desfecho (400 {@code DATA_REALIZACAO_FUTURA} e {@code MOTIVO_OBRIGATORIO});</li>
 *   <li>realizada e ausente exigem que o <b>dia</b> agendado não seja futuro (409
 *       {@code VISITA_AINDA_NAO_OCORREU}); cancelar vale a qualquer momento antes do desfecho.</li>
 * </ol>
 *
 * <h2>Atomicidade e concorrência</h2>
 * <p>O registro roda em uma única transação e duas requisições simultâneas para a mesma visita nunca geram dois
 * desfechos. A proteção tem duas camadas: (1) a linha da visita é carregada com bloqueio de escrita
 * ({@link VisitaAgendadaRepository#buscarComBloqueio}), então a segunda requisição espera a primeira terminar e
 * encontra a visita já finalizada, recebendo 409 {@code TRANSICAO_VISITA_INVALIDA}; (2) o controle de versão
 * otimista ({@code @Version} da entidade) é a barreira final: se mesmo assim a versão divergir, o
 * {@code saveAndFlush} dentro do método falha e a falha vira 409 {@code VISITA_ALTERADA_CONCORRENTEMENTE}. Nos dois
 * casos o resultado é um sucesso e um 409, nunca dois desfechos nem 500. O bloqueio da linha é o mesmo padrão que
 * {@code ParticipacaoService} usa no estudo (RQ-T81-09).</p>
 *
 * <p>O serviço recebe um {@link ComandoDesfecho} simples e o id do usuário; não conhece {@code HttpServletRequest}
 * nem {@code Jwt}. A retirada (T102) cancela as visitas futuras usando a {@link MaquinaEstadosVisita}, sem passar
 * por aqui. Não existe operação de desfazer: nenhum caminho volta uma visita a {@code AGENDADA}.</p>
 */
@Service
public class DesfechoVisitaService {

    public static final String ESTUDO_FORA_DE_RECRUTAMENTO_OU_EXECUCAO = "ESTUDO_FORA_DE_RECRUTAMENTO_OU_EXECUCAO";
    public static final String VISITA_AINDA_NAO_OCORREU = "VISITA_AINDA_NAO_OCORREU";
    public static final String VISITA_ALTERADA_CONCORRENTEMENTE = "VISITA_ALTERADA_CONCORRENTEMENTE";

    private static final DateTimeFormatter FORMATO_DATA = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    /**
     * Pedido de desfecho, sem dependência da camada web.
     *
     * @param tipo              obrigatório
     * @param dataHoraRealizada só vale para {@code REALIZADA}; omitida assume o instante atual
     * @param motivo            só vale para {@code CANCELADA}, onde é obrigatório
     * @param observacao        opcional
     */
    public record ComandoDesfecho(TipoDesfecho tipo, LocalDateTime dataHoraRealizada, String motivo,
            String observacao) {
    }

    private final VisitaAgendadaRepository visitaRepository;
    private final UsuarioAutenticado usuarioAutenticado;
    private final Clock clock;

    public DesfechoVisitaService(VisitaAgendadaRepository visitaRepository, UsuarioAutenticado usuarioAutenticado,
            Clock clock) {
        this.visitaRepository = visitaRepository;
        this.usuarioAutenticado = usuarioAutenticado;
        this.clock = clock;
    }

    /**
     * Registra o desfecho da visita e devolve a visita atualizada.
     *
     * @param visitaAgendadaId a visita agendada
     * @param comando          o tipo e os dados do desfecho
     * @param usuarioId        quem registra (o usuário do token)
     */
    @Transactional
    public VisitaAgendada registrar(UUID visitaAgendadaId, ComandoDesfecho comando, UUID usuarioId) {
        VisitaAgendada visita = visitaRepository.buscarComBloqueio(visitaAgendadaId)
                .orElseThrow(() -> new RecursoNaoEncontradoException(
                        "Visita agendada não encontrada: " + visitaAgendadaId));

        exigirEstudoEmRecrutamentoOuExecucao(visita);
        exigirParticipacaoAtiva(visita);
        MaquinaEstadosVisita.validarTransicao(visita.getSituacao(), comando.tipo());

        LocalDateTime agora = LocalDateTime.now(clock);
        MaquinaEstadosVisita.validarDados(comando.tipo(), comando.dataHoraRealizada(), comando.motivo(),
                comando.observacao(), agora);
        if (comando.tipo() != TipoDesfecho.CANCELADA) {
            exigirQueADataAgendadaJaChegou(visita, LocalDate.now(clock));
        }

        DadosDesfecho dados = MaquinaEstadosVisita.dadosParaGravar(comando.tipo(), comando.dataHoraRealizada(),
                comando.motivo(), comando.observacao(), agora);
        Usuario usuario = usuarioAutenticado.buscar(usuarioId);

        visita.setSituacao(MaquinaEstadosVisita.destinoDe(comando.tipo()));
        visita.setDataHoraRealizada(dados.dataHoraRealizada());
        visita.setMotivo(dados.motivo());
        visita.setObservacao(dados.observacao());
        visita.setAlteradoPor(usuario);
        visita.setAlteradoEm(agora);

        try {
            // saveAndFlush: a violação da versão acontece aqui dentro, e não só no commit
            return visitaRepository.saveAndFlush(visita);
        } catch (ConcurrencyFailureException ex) {
            throw visitaAlteradaConcorrentemente(ex);
        }
    }

    /** Regra 1: o estudo precisa estar em recrutamento ou execução; planejamento e encerrado recusam. */
    private void exigirEstudoEmRecrutamentoOuExecucao(VisitaAgendada visita) {
        StatusEstudo status = visita.getParticipacao().getEstudo().getStatus();
        if (status != StatusEstudo.RECRUTAMENTO && status != StatusEstudo.EXECUCAO) {
            throw new ConflitoRegraException(ESTUDO_FORA_DE_RECRUTAMENTO_OU_EXECUCAO,
                    "O desfecho da visita só pode ser registrado com o estudo em recrutamento ou execução");
        }
    }

    /**
     * Ponto único da regra 6 (participante retirado). A T101 cria a situação da participação e a T102 preenche
     * este método para lançar {@code ConflitoRegraException("PARTICIPANTE_RETIRADO", ...)} quando o participante
     * foi retirado do estudo. Até lá, a coluna não existe e nenhum participante está retirado.
     */
    private void exigirParticipacaoAtiva(VisitaAgendada visita) {
        // T102: if (participacao.estaRetirada()) throw new ConflitoRegraException("PARTICIPANTE_RETIRADO", ...)
    }

    /**
     * Realizada e ausente não valem para uma visita cuja data agendada ainda não chegou. A comparação é pelo dia: o
     * dia de hoje é permitido, para não impedir quem registra a visita antes do horário marcado.
     */
    private void exigirQueADataAgendadaJaChegou(VisitaAgendada visita, LocalDate hoje) {
        LocalDate dataAgendada = visita.dataAgendada();
        if (dataAgendada.isAfter(hoje)) {
            throw new ConflitoRegraException(VISITA_AINDA_NAO_OCORREU,
                    "A visita está agendada para o futuro (" + dataAgendada.format(FORMATO_DATA)
                            + "); até lá ela só pode ser cancelada");
        }
    }

    private static ConflitoRegraException visitaAlteradaConcorrentemente(Exception causa) {
        ConflitoRegraException erro = new ConflitoRegraException(VISITA_ALTERADA_CONCORRENTEMENTE,
                "A visita foi alterada por outra requisição; recarregue e tente de novo");
        erro.initCause(causa);
        return erro;
    }
}
