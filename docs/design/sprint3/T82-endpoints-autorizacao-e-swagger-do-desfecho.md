# T82 · Endpoints, autorização e Swagger do desfecho

> **Sprint 3** · PBI-20 · RF13 Realização, ausência e cancelamento de visitas  
> **Responsável:** I3 · **Estimativa:** 3h · **Prazo:** Qui 12/11  
> **Depende de:** T81  
> **Situação:** ⬜ A implementar (este documento é o guia para quem for fazer)

> Antes de começar, leia as [convenções e o contrato compartilhado da Sprint 3](00-convencoes-e-contrato-compartilhado.md): nomes, endpoints, DTOs, códigos de erro, tabelas e dados de demonstração valem para esta task. A origem dos requisitos (especificação, critério de aceite do PBI, regras de negócio e o texto original da task) está na [seção 9](#9-origem-dos-requisitos-especificação-e-planejamento).

## 1. Objetivo

Expor o endpoint de desfecho e a consulta de uma visita, com autorização por perfil, erros no formato único e o Swagger igual ao contrato.

## 2. Contexto: o que já existe e deve ser reaproveitado

- `VisitaAgendadaResponse` e o `AgendamentoController` (que já responde `PUT /api/visitas-agendadas/{id}`) vêm da T76; `DesfechoVisitaService` vem da T81; o contrato do desfecho, da T80.
- `Permissoes.LEITURA` e `Permissoes.ESCRITA_RECRUTAMENTO`; `Autenticacao.idDe(jwt)`; `ApiExceptionHandler` (formato único).

## 3. Onde implementar

| Arquivo | Ação | Responsabilidade |
|---|---|---|
| `backend/ClinicalStudyPB/src/main/java/UEPB/ESWII/ClinicalStudyPB/controller/VisitaAgendadaController.java` | Criar | `GET /api/visitas-agendadas/{id}` e `POST .../desfecho` |
| `backend/ClinicalStudyPB/src/main/java/UEPB/ESWII/ClinicalStudyPB/controller/dto/DesfechoRequest.java` | Criar | `tipo`, `dataHoraRealizada`, `motivo`, `observacao` com validação |
| `backend/ClinicalStudyPB/src/main/java/UEPB/ESWII/ClinicalStudyPB/service/AgendamentoService.java` | Alterar | Método `buscar(id)` da visita (consulta), se ainda não existir |
| `backend/ClinicalStudyPB/src/test/java/UEPB/ESWII/ClinicalStudyPB/ContratoOpenApiSprint3Test.java` | Alterar | Habilita o caso do desfecho e inclui o `GET` (7 operações) |
| `docs/api/openapi-sprint3.yaml` | Alterar | Acrescenta `GET /api/visitas-agendadas/{id}` e corrige divergências |

## 4. Requisitos a implementar

Cada requisito tem um identificador (`RQ-T82-NN`) para a revisão do Pull Request e para a conferência no fim da task: todos precisam estar atendidos.

### Endpoints

- **RQ-T82-01** `POST /api/visitas-agendadas/{visitaAgendadaId}/desfecho` (só coordenador) recebe `DesfechoRequest` e responde **200** com a `VisitaAgendadaResponse` atualizada (o recurso já existe: o desfecho o altera, não cria).
- **RQ-T82-02** `GET /api/visitas-agendadas/{visitaAgendadaId}` (leitura: pesquisador e coordenador) devolve a `VisitaAgendadaResponse` completa (a tela da visita usa esta consulta).
- **RQ-T82-03** `DesfechoRequest` com Bean Validation: `tipo` obrigatório (`@NotNull`), `motivo` e `observacao` com no máximo 500 caracteres; as regras de motivo e data ficam na máquina (T80), não no DTO.
- **RQ-T82-04** Os dois métodos ficam no `VisitaAgendadaController` (`@RequestMapping("/api")`), sem conflito com o `PUT` de reagendamento do `AgendamentoController`.

### Autorização e erros

- **RQ-T82-05** `@PreAuthorize`: escrita `Permissoes.ESCRITA_RECRUTAMENTO`, leitura `Permissoes.LEITURA`; sem token 401; administrador e participante recebem 403 nas duas operações.
- **RQ-T82-06** Status: 200; 400 (`VALIDACAO_CAMPOS` para `tipo` ausente ou inválido, `CORPO_INVALIDO` para JSON ou enum malformado, `DATA_REALIZACAO_FUTURA`, `MOTIVO_OBRIGATORIO`); 403; 404 (visita inexistente); 409 (`TRANSICAO_VISITA_INVALIDA`, `ESTUDO_FORA_DE_RECRUTAMENTO_OU_EXECUCAO`, `VISITA_AINDA_NAO_OCORREU`, `VISITA_ALTERADA_CONCORRENTEMENTE`).
- **RQ-T82-07** Todos no formato único de erro (`status`, `codigoRegra`, `mensagem`, `errosCampo`, `erro`, `codigo`).

### Swagger e contrato

- **RQ-T82-08** `@Tag(name = "Visitas agendadas")`, `@Operation` com a descrição das regras (estados finais, motivo obrigatório, data real não futura).
- **RQ-T82-09** Acrescentar ao YAML o `GET /api/visitas-agendadas/{visitaAgendadaId}`, remover o `@Disabled("T82")` do `ContratoOpenApiSprint3Test` e atualizar a contagem esperada (5 da T74 + 2 = 7 operações); se o Swagger divergir do YAML, **corrigir o YAML no mesmo Pull Request**.

## 5. Contrato, regras e códigos de erro

- Operações 6 e 7 da seção 2 das convenções.

## 6. Testes

- A bateria completa de endpoints está na T85; aqui, no mínimo, `ContratoOpenApiSprint3Test` verde e um teste de fumaça de cada uma das duas operações.

## 7. Critérios de aceite

- [ ] O endpoint de desfecho e a consulta respondem conforme o contrato.
- [ ] O Swagger UI e o YAML coincidem.
- [ ] Os perfis sem permissão recebem 403 e a visita inexistente, 404.

## 8. Cuidados e fora de escopo

- Um único endpoint de desfecho para os três tipos.
- O controller não aplica regra de negócio: só valida o DTO e chama o serviço.

## 9. Origem dos requisitos (especificação e planejamento)

Esta seção reúne, **sem paráfrase**, o que a especificação do projeto e o planejamento da Sprint 3 exigem para esta task, para que o documento sozinho mostre de onde vem cada requisito da seção 4. Onde o texto original difere das convenções (ajustes 1 a 10, [seção 1](00-convencoes-e-contrato-compartilhado.md#1-ajustes-em-relação-ao-planejamento-e-por-quê)), vale o que está neste documento e nas convenções.

### 9.1 Requisito funcional da especificação

- **RF13** (Módulo D — Acompanhamento; interface Web): “Registrar a realização, ausência ou cancelamento de visitas.”

### 9.2 Critério de aceite do PBI (planejamento)

> O coordenador registra o desfecho de uma visita agendada (realizada, ausente ou cancelada), com os dados exigidos por cada situação; desfechos são finais e não podem ser desfeitos; transições inválidas são recusadas com a regra violada.

### 9.3 Regras de negócio do planejamento que afetam esta task

| # | Regra proposta | Tasks afetadas (no planejamento) |
|---|---|---|
| 10 | Erros seguem o formato único da Sprint 2 (400 validação, 403 perfil sem permissão, 404 inexistente, 409 regra de negócio, sempre com o código da regra). As migrations usam faixas de versão por PBI (V3_19_* a V3_25_*); antes de começar, confirmar que a maior versão da Sprint 2 é menor que V3_19, senão trocar o prefixo V3 pelo número seguinte ao da última migration | T74 e todas as de backend |

### 9.4 Texto original da task no planejamento

> **T82 · Endpoints, autorização e Swagger do desfecho.** Primeiro exponha o endpoint de desfecho e a consulta de uma visita, com escrita só para o coordenador e leitura para coordenador e pesquisador. Depois trate 400, 403, 404 e 409 no formato único de erro. Por fim, confira o Swagger contra o YAML e corrija o que divergir no mesmo Pull Request.

### 9.5 Requisitos da especificação que atravessam esta task

- Regra 9.2 (tecnologia): “O backend deve disponibilizar uma **API HTTP REST** compartilhada pela web e pelo mobile, documentada no padrão **OpenAPI** (Swagger UI pode ser usado para consulta e testes).”
- Entregas: “Tratamento de erros e validações das regras de negócio.”
- **Definition of Done** da Sprint (texto completo na [seção 11 das convenções](00-convencoes-e-contrato-compartilhado.md#11-definition-of-done-e-requisitos-transversais-da-especificação)): os critérios de aceite estão atendidos e demonstráveis com dados fictícios; o código foi revisado, passou no CI e entrou na `main` por Pull Request; há testes automatizados e, no backend, a cobertura do código de negócio permanece em 80% ou mais; erros e validações são tratados com mensagens claras; Swagger e contrato YAML refletem os endpoints; os dados de demonstração estão no perfil `demo`; documentação e capturas estão no repositório; e RF01 a RF11 continuam funcionando.


---
Parte do conjunto de documentos de design da Sprint 3: [índice](README.md) · [convenções e contrato](00-convencoes-e-contrato-compartilhado.md).
