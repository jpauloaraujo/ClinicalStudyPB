# T75 · Regras de agendamento (serviço de domínio)

> **Sprint 3** · PBI-19 · RF12 Agendamento de visitas  
> **Responsável:** I1 · **Estimativa:** 4h · **Prazo:** Ter 10/11  
> **Depende de:** T73, T74  
> **Situação:** ⬜ A implementar (este documento é o guia para quem for fazer)

> Antes de começar, leia as [convenções e o contrato compartilhado da Sprint 3](00-convencoes-e-contrato-compartilhado.md): nomes, endpoints, DTOs, códigos de erro, tabelas e dados de demonstração valem para esta task. A origem dos requisitos (especificação, critério de aceite do PBI, regras de negócio e o texto original da task) está na [seção 9](#9-origem-dos-requisitos-especificação-e-planejamento).

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

## 9. Origem dos requisitos (especificação e planejamento)

Esta seção reúne, **sem paráfrase**, o que a especificação do projeto e o planejamento da Sprint 3 exigem para esta task, para que o documento sozinho mostre de onde vem cada requisito da seção 4. Onde o texto original difere das convenções (ajustes 1 a 10, [seção 1](00-convencoes-e-contrato-compartilhado.md#1-ajustes-em-relação-ao-planejamento-e-por-quê)), vale o que está neste documento e nas convenções.

### 9.1 Requisito funcional da especificação

- **RF12** (Módulo D — Acompanhamento; interface Web): “Agendar visitas para os participantes incluídos.”

### 9.2 Critério de aceite do PBI (planejamento)

> O coordenador agenda, reagenda e consulta as visitas de um participante ativo, com sugestão de datas a partir da inclusão; datas fora da janela e estudos fora de recrutamento ou execução são recusados com mensagem clara. Ao fechar, em 11/11, este PBI deixa na main a tabela e a agenda da visita agendada que os PBIs 20, 21, 22, 23 e 25 usam.

### 9.3 Regras de negócio do planejamento que afetam esta task

| # | Regra proposta | Tasks afetadas (no planejamento) |
|---|---|---|
| 1 | Visitas só são agendadas para participante incluído e ativo (não retirado), com o estudo em recrutamento ou execução; estudo encerrado bloqueia qualquer agendamento | T75, T81, T89, T102 |
| 2 | Uma visita agendada por visita prevista e por participante. A data deve ser de hoje em diante e ficar dentro da janela da visita prevista: data de inclusão somada ao dia previsto, mais ou menos os dias de tolerância. Fora da janela, o sistema recusa com 409 e mostra a janela válida | T73, T75, T77 |
| 3 | A visita agendada nasce AGENDADA e só pode virar REALIZADA, AUSENTE ou CANCELADA; esses três estados são finais. Reagendar (trocar a data) só vale para AGENDADA. Realizada e ausente não aceitam data futura; cancelar exige motivo; realizada guarda data e hora reais | T75, T80, T81, T83 |
| 6 | A retirada é registrada pelo coordenador, com motivo, quem solicitou e data (não futura), e é irreversível. O participante passa a RETIRADO e as visitas futuras AGENDADA viram CANCELADA com o motivo "retirada do estudo". A vaga continua ocupada, ou seja, o limite de vagas conta todos os que foram incluídos (ponto a validar com o PO) | T101, T102 (inclui os bloqueios nos serviços de T75, T81 e T89) e T104 |
| 10 | Erros seguem o formato único da Sprint 2 (400 validação, 403 perfil sem permissão, 404 inexistente, 409 regra de negócio, sempre com o código da regra). As migrations usam faixas de versão por PBI (V3_19_* a V3_25_*); antes de começar, confirmar que a maior versão da Sprint 2 é menor que V3_19, senão trocar o prefixo V3 pelo número seguinte ao da última migration | T74 e todas as de backend |

### 9.4 Texto original da task no planejamento

> **T75 · Regras de agendamento (serviço de domínio).** Primeiro escreva o serviço de domínio sem depender de Spring Web, para testá-lo isoladamente. Depois implemente as verificações, nesta ordem: participação existente no estudo, estudo em recrutamento ou execução, visita prevista pertencente ao estudo, data de hoje em diante, data dentro da janela (data de inclusão mais o dia previsto, mais ou menos a tolerância) e ausência de agendamento anterior para a mesma visita prevista. Cada falha lança uma exceção com código próprio, que vira 409. Em seguida implemente o reagendamento (só para AGENDADA, mesma janela) e o cálculo das datas sugeridas. Por fim, registre quem agendou e quando. O bloqueio de participante retirado entra na T102 (PBI-23), quando a coluna de situação passa a existir.

### 9.5 Requisitos da especificação que atravessam esta task

- Entregas: “Tratamento de erros e validações das regras de negócio.”
- Estratégia de testes (seção 7): “Cobertura mínima de **80% das linhas do código de negócio do backend** (excluindo código gerado e configurações)”, “Testes unitários para todas as regras de negócio relevantes”, “Testes de integração para os principais endpoints” e “Execução automatizada dos testes a cada pull request ou integração à branch principal”. Regra 9.6: “As equipes devem implementar testes automatizados e apresentar evidências de cobertura.”
- **Definition of Done** da Sprint (texto completo na [seção 11 das convenções](00-convencoes-e-contrato-compartilhado.md#11-definition-of-done-e-requisitos-transversais-da-especificação)): os critérios de aceite estão atendidos e demonstráveis com dados fictícios; o código foi revisado, passou no CI e entrou na `main` por Pull Request; há testes automatizados e, no backend, a cobertura do código de negócio permanece em 80% ou mais; erros e validações são tratados com mensagens claras; Swagger e contrato YAML refletem os endpoints; os dados de demonstração estão no perfil `demo`; documentação e capturas estão no repositório; e RF01 a RF11 continuam funcionando.


---
Parte do conjunto de documentos de design da Sprint 3: [índice](README.md) · [convenções e contrato](00-convencoes-e-contrato-compartilhado.md).
