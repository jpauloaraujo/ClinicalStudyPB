# T76 · Endpoints, autorização e Swagger do agendamento

> **Sprint 3** · PBI-19 · RF12 Agendamento de visitas  
> **Responsável:** I1 · **Estimativa:** 3h · **Prazo:** Qua 11/11  
> **Depende de:** T75  
> **Situação:** ⬜ A implementar (este documento é o guia para quem for fazer)

> Antes de começar, leia as [convenções e o contrato compartilhado da Sprint 3](00-convencoes-e-contrato-compartilhado.md): nomes, endpoints, DTOs, códigos de erro, tabelas e dados de demonstração valem para esta task.

## 1. Objetivo

Expor as 5 operações do contrato do PBI-19 com autorização por perfil e o erro no formato único, e deixar o Swagger idêntico ao YAML. Quando entrar na `main`, avisar I3 (dono do PBI-20).

## 2. Contexto: o que já existe e deve ser reaproveitado

- `Permissoes.LEITURA` (pesquisador e coordenador) e `Permissoes.ESCRITA_RECRUTAMENTO` (coordenador); `Autenticacao.idDe(jwt)`; `PaginaResponse`; padrão dos controllers `ParticipacaoController` e `CandidatoController` (`@Tag`, `@Operation`, `@PreAuthorize`).
- O serviço `AgendamentoService` e os códigos de erro vêm da T75; o contrato, da T74.
- `ApiExceptionHandler` já converte as exceções do serviço no formato único; esta task não o altera.

## 3. Onde implementar

| Arquivo | Ação | Responsabilidade |
|---|---|---|
| `backend/ClinicalStudyPB/src/main/java/UEPB/ESWII/ClinicalStudyPB/controller/AgendamentoController.java` | Criar | `GET .../visitas`, `POST .../visitas-agendadas` e `PUT /api/visitas-agendadas/{id}` |
| `backend/ClinicalStudyPB/src/main/java/UEPB/ESWII/ClinicalStudyPB/controller/AgendaController.java` | Criar | `GET /api/estudos/{id}/agenda` e `/agenda/participantes` |
| `backend/ClinicalStudyPB/src/main/java/UEPB/ESWII/ClinicalStudyPB/service/AgendaService.java` | Criar | Consulta da agenda com filtros e paginação, e do progresso por participante |
| `backend/ClinicalStudyPB/src/main/java/UEPB/ESWII/ClinicalStudyPB/repository/VisitaAgendadaRepository.java` | Alterar | Consultas da agenda (por estudo, período e situação) |
| `backend/ClinicalStudyPB/src/main/java/UEPB/ESWII/ClinicalStudyPB/controller/dto/` | Criar | `VisitaDoParticipanteResponse`, `VisitaAgendadaRequest`, `VisitaReagendamentoRequest`, `VisitaAgendadaResponse`, `AgendaItemResponse`, `ParticipanteAgendaResponse` |
| `backend/ClinicalStudyPB/src/test/java/UEPB/ESWII/ClinicalStudyPB/ContratoOpenApiSprint3Test.java` | Alterar | Remove o `@Disabled` da T74 |
| `docs/api/openapi-sprint3.yaml` | Alterar | Só se o Swagger divergir (corrige no mesmo PR) |

## 4. Requisitos a implementar

Cada requisito tem um identificador (`RQ-T76-NN`) para a revisão do Pull Request e para a conferência no fim da task: todos precisam estar atendidos.

### Endpoints do agendamento

- **RQ-T76-01** `GET /api/participacoes/{participacaoId}/visitas` (leitura: pesquisador e coordenador) devolve `List<VisitaDoParticipanteResponse>` com janela, data sugerida e agendamento.
- **RQ-T76-02** `POST /api/participacoes/{participacaoId}/visitas-agendadas` (só coordenador) recebe `VisitaAgendadaRequest` (`visitaPlanejadaId` e `dataHora` obrigatórios, validados com Bean Validation) e responde **201** com `VisitaAgendadaResponse` e o cabeçalho `Location` de `/api/visitas-agendadas/{id}`.
- **RQ-T76-03** `PUT /api/visitas-agendadas/{visitaAgendadaId}` (só coordenador) recebe `VisitaReagendamentoRequest` (`dataHora` obrigatória) e responde **200** com `VisitaAgendadaResponse`.
- **RQ-T76-04** O `VisitaAgendadaResponse` traz todos os campos da seção 3 das convenções, com `dataHora` e `dataHoraRealizada` como `OffsetDateTime` no fuso configurado e a janela (`janelaInicio`, `janelaFim`) calculada na hora.
- **RQ-T76-05** Códigos de status: 201, 200, 400 (`VALIDACAO_CAMPOS`, `CORPO_INVALIDO` e `DATA_NO_PASSADO`), 403 (outro perfil, inclusive o administrador e o participante), 404 (participação, visita agendada ou visita prevista inexistente) e 409 (regras da T75), sempre no formato único de erro.

### Agenda do estudo

- **RQ-T76-06** `GET /api/estudos/{estudoId}/agenda` (leitura) devolve `PaginaResponse<AgendaItemResponse>` ordenada por `dataHora` crescente, com os filtros opcionais `de` e `ate` (`yyyy-MM-dd`, inclusivos, aplicados à data agendada), `situacao` (enum `SituacaoVisita`) e `pagina`/`tamanho` (padrão 0 e 20, máximo 100).
- **RQ-T76-07** `GET /api/estudos/{estudoId}/agenda/participantes` (leitura) devolve, para cada participante do estudo, `visitasPrevistas` (total de visitas previstas do estudo) e `visitasAgendadas` (agendamentos em qualquer situação), ordenado pelo código do candidato.
- **RQ-T76-08** Estudo inexistente responde 404; `de` maior que `ate` responde 400 com `PARAMETRO_INVALIDO`; valor inválido de `situacao` responde 400 (`PARAMETRO_INVALIDO`).
- **RQ-T76-09** As duas consultas usam projeção ou `JOIN FETCH` (sem uma consulta por linha) e funcionam no H2 e no PostgreSQL.

### Autorização e documentação

- **RQ-T76-10** `@PreAuthorize` em todos os métodos: escrita `Permissoes.ESCRITA_RECRUTAMENTO`, leitura `Permissoes.LEITURA`. Sem token: 401.
- **RQ-T76-11** `@Tag(name = "Agendamento")` (e "Agenda") e `@Operation` com resumo e descrição das regras; o Swagger UI em `/swagger-ui.html` mostra as 5 operações.
- **RQ-T76-12** Comparar o Swagger com `docs/api/openapi-sprint3.yaml`: remover o `@Disabled` do `ContratoOpenApiSprint3Test` (T74) e fazê-lo passar; se algo divergir, corrigir o YAML **no mesmo Pull Request**.
- **RQ-T76-13** Ao entrar na `main`, avisar I3 (PBI-20), que revisa este Pull Request.

## 5. Contrato, regras e códigos de erro

- Endpoints 1 a 5 da seção 2 das convenções.
- Códigos: os da T75 mais os de validação de campo.

## 6. Testes

- Os testes de endpoint completos estão na T78; nesta task, o mínimo é o `ContratoOpenApiSprint3Test` verde e um teste de fumaça de cada endpoint (criar, reagendar, listar).

## 7. Critérios de aceite

- [ ] As 5 operações respondem conforme o contrato, com os perfis e códigos certos.
- [ ] O Swagger e o YAML coincidem (`ContratoOpenApiSprint3Test` verde).
- [ ] A agenda filtra por período e situação e pagina.

## 8. Cuidados e fora de escopo

- O controller só converte DTO, chama o serviço e devolve a resposta; nenhuma regra de negócio nele.
- Não expor entidade JPA na resposta.

---
Parte do conjunto de documentos de design da Sprint 3: [índice](README.md) · [convenções e contrato](00-convencoes-e-contrato-compartilhado.md).
