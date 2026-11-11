# T75 · Regras de agendamento (serviço de domínio)

> **Sprint 3** · PBI-19 · RF12 Agendamento de visitas  
> **Responsável:** I1 · **Estimativa:** 4h · **Prazo:** Ter 10/11  
> **Depende de:** T73, T74  
> **Situação:** ⬜ A implementar (este documento é o guia para quem for fazer)

> Antes de começar, leia as [convenções e o contrato compartilhado da Sprint 3](00-convencoes-e-contrato-compartilhado.md): nomes, endpoints, DTOs, códigos de erro, tabelas e dados de demonstração valem para esta task.

## 1. Objetivo

Implementar as regras do RF12 em serviços de domínio testáveis sem a camada web: agendar, reagendar, calcular a janela e sugerir as datas, registrando quem agendou e quando.

## 2. Contexto: o que já existe e deve ser reaproveitado

- `VisitaPlanejada.estaNaJanela`, `inicioDaJanela` e `fimDaJanela` já existem; o agendamento só os chama com a `dataInclusao` da participação.
- `EstudoAcesso` (busca de estudo, `exigirRecrutamento`, `exigirPlanejamento`, `buscarComBloqueio`), `ConflitoRegraException` (409 com código), `RegraNegocioException` (400 genérico), `RecursoNaoEncontradoException` (404) e `UsuarioAutenticado` (usuário do token).
- `ParticipacaoService` e `AvaliacaoService` mostram o padrão: serviço `@Transactional`, verificações em ordem, um código por regra.
- O bloqueio de participante retirado entra na T102 (a coluna de situação só existe a partir da T101).
- Regras 1, 2 e 3 e a ordem das verificações: premissas do planejamento e seção 4 das convenções.

## 3. Onde implementar

| Arquivo | Ação | Responsabilidade |
|---|---|---|
| `backend/ClinicalStudyPB/src/main/java/UEPB/ESWII/ClinicalStudyPB/service/ValidacaoRegraException.java` | Criar | Erro 400 com código próprio |
| `backend/ClinicalStudyPB/src/main/java/UEPB/ESWII/ClinicalStudyPB/controller/ApiExceptionHandler.java` | Alterar | Trata `ValidacaoRegraException` em `resposta(400, codigo, ...)` |
| `backend/ClinicalStudyPB/src/main/java/UEPB/ESWII/ClinicalStudyPB/domain/regra/JanelaDeAgendamento.java` | Criar | Record `(inicio, fim)` com `contem(LocalDate)` e fábrica a partir da visita prevista e da inclusão |
| `backend/ClinicalStudyPB/src/main/java/UEPB/ESWII/ClinicalStudyPB/config/RelogioConfig.java` | Criar | Bean `Clock` no fuso `app.fuso-horario` |
| `backend/ClinicalStudyPB/src/main/resources/application.properties` | Alterar | `app.fuso-horario=${FUSO_HORARIO:America/Fortaleza}` |
| `backend/ClinicalStudyPB/src/main/java/UEPB/ESWII/ClinicalStudyPB/service/AgendamentoService.java` | Criar | Agendar, reagendar, listar visitas do participante e sugerir datas |
| `backend/ClinicalStudyPB/src/main/java/UEPB/ESWII/ClinicalStudyPB/repository/ParticipacaoRepository.java` | Alterar | Consulta da participação com estudo e candidato quando necessário |

## 4. Requisitos a implementar

Cada requisito tem um identificador (`RQ-T75-NN`) para a revisão do Pull Request e para a conferência no fim da task: todos precisam estar atendidos.

### Infraestrutura compartilhada

- **RQ-T75-01** Criar `ValidacaoRegraException(String codigo, String mensagem)` e tratá-la em `ApiExceptionHandler` com HTTP 400 e o código informado, no mesmo formato único de erro. `RegraNegocioException` continua respondendo `DADOS_INVALIDOS`.
- **RQ-T75-02** Criar o bean `Clock` (`Clock.system(ZoneId.of(fusoHorario))`) lendo `app.fuso-horario`; todo "agora" e "hoje" do agendamento vem desse `Clock`, nunca de `LocalDate.now()` solto, para os testes fixarem o dia.
- **RQ-T75-03** Criar `JanelaDeAgendamento` (record imutável, sem Spring) com `inicio`, `fim`, `contem(LocalDate)` e `static de(VisitaPlanejada, LocalDate dataInclusao)` que usa os métodos de `VisitaPlanejada`.

### Agendar (`agendar(participacaoId, visitaPlanejadaId, dataHora, usuarioId)`)

- **RQ-T75-04** Executar as verificações **nesta ordem**: (1) participação existe (404); (2) estudo em recrutamento **ou** execução, senão 409 `ESTUDO_FORA_DE_RECRUTAMENTO_OU_EXECUCAO` (planejamento e encerrado são recusados); (3) a visita prevista pertence ao estudo da participação, senão 404; (4) a data (parte de `dataHora`) é de hoje em diante, senão 400 `DATA_NO_PASSADO`; (5) a data está dentro da janela `inclusão + dia previsto ± tolerância`, senão 409 `DATA_FORA_DA_JANELA` com a janela válida na mensagem (`de dd/MM/yyyy a dd/MM/yyyy`); (6) não existe agendamento para o par (participação, visita prevista), senão 409 `VISITA_JA_AGENDADA`.
- **RQ-T75-05** Gravar a `VisitaAgendada` com situação `AGENDADA`, `criadoPor`/`alteradoPor` (usuário do token via `UsuarioAutenticado`), `criadoEm`/`alteradoEm` (hora do `Clock`) e `versao` 0.
- **RQ-T75-06** Tratar a corrida de dois agendamentos simultâneos do mesmo par: o `UNIQUE` do banco é a barreira final; capturar `DataIntegrityViolationException` e traduzi-la em 409 `VISITA_JA_AGENDADA` (sem 500).
- **RQ-T75-07** Aceitar a hora informada como está (horário local do centro); não existe regra de horário de funcionamento nesta Sprint.

### Reagendar (`reagendar(visitaAgendadaId, novaDataHora, usuarioId)`)

- **RQ-T75-08** Verificações: visita existe (404); estudo em recrutamento ou execução (409); situação `AGENDADA`, senão 409 `VISITA_NAO_ESTA_AGENDADA`; nova data de hoje em diante (400 `DATA_NO_PASSADO`); nova data **na mesma janela** (409 `DATA_FORA_DA_JANELA`).
- **RQ-T75-09** Atualizar só `dataHoraAgendada`, `alteradoPor` e `alteradoEm`; a situação continua `AGENDADA` e o registro **não** vira um novo agendamento.
- **RQ-T75-10** Reagendar para a mesma data e hora é aceito (não é erro), mas atualiza `alteradoEm`.

### Consulta e sugestão de datas

- **RQ-T75-11** `visitasDoParticipante(participacaoId)` devolve, para **todas** as visitas previstas do estudo (ordenadas por `ordem`), a janela, a **data sugerida** (`inclusão + dia previsto`) e o agendamento existente, se houver: é o que o contrato chama de `VisitaDoParticipanteResponse`.
- **RQ-T75-12** A data sugerida mais antiga que hoje continua sendo calculada e devolvida (a tela decide como mostrar); apenas o agendamento a recusa.
- **RQ-T75-13** Nenhuma consulta por visita dentro de um laço: usar uma consulta única (ou duas) para carregar as visitas previstas e os agendamentos da participação.

### Bloqueio de participante retirado (adiado)

- **RQ-T75-14** Deixar um ponto único e comentado no serviço (`exigirParticipacaoAtiva`) que a **T102** preenche com `PARTICIPANTE_RETIRADO`; esta task não cria a coluna de situação.

## 5. Contrato, regras e códigos de erro

- Erros desta task: `ESTUDO_FORA_DE_RECRUTAMENTO_OU_EXECUCAO` (409), `VISITA_JA_AGENDADA` (409), `DATA_FORA_DA_JANELA` (409), `DATA_NO_PASSADO` (400), `VISITA_NAO_ESTA_AGENDADA` (409) e 404 para participação ou visita prevista inexistente.
- Uma visita **cancelada** continua ocupando o `UNIQUE`: agendar de novo recebe `VISITA_JA_AGENDADA` (consequência assumida das regras 2 e 3).

## 6. Testes

- Cada regra isolada com Mockito e `Clock` fixo (a matriz completa é a T78).
- Teste do `ApiExceptionHandler` para `ValidacaoRegraException` (400 com o código informado).
- `JanelaDeAgendamento`: limites inclusivos (primeiro e último dia da janela) e dia fora dos dois lados.

## 7. Critérios de aceite

- [ ] Agendar dentro da janela grava a visita com autor e data; cada regra violada falha com o código e o HTTP certos.
- [ ] Reagendar só vale para `AGENDADA` e dentro da janela.
- [ ] A sugestão de datas devolve janela e data sugerida para todas as visitas previstas, sem N+1.
- [ ] Duas requisições simultâneas para o mesmo par resultam em um 201 e um 409.

## 8. Cuidados e fora de escopo

- O serviço não depende de `HttpServletRequest`, de `Jwt` nem de qualquer classe web: o usuário chega por parâmetro.
- Não acrescentar a verificação de participante retirado (T102) nem a de visita com desfecho além de `VISITA_NAO_ESTA_AGENDADA`.

---
Parte do conjunto de documentos de design da Sprint 3: [índice](README.md) · [convenções e contrato](00-convencoes-e-contrato-compartilhado.md).
