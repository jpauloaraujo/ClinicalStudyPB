# T103 · Endpoints, autorização e Swagger da retirada

> **Sprint 3** · PBI-23 · RF16 Retirada de participante  
> **Responsável:** I4 · **Estimativa:** 3h · **Prazo:** Qua 18/11  
> **Depende de:** T102  
> **Situação:** ⬜ A implementar (este documento é o guia para quem for fazer)

> Antes de começar, leia as [convenções e o contrato compartilhado da Sprint 3](00-convencoes-e-contrato-compartilhado.md): nomes, endpoints, DTOs, códigos de erro, tabelas e dados de demonstração valem para esta task. A origem dos requisitos (especificação, critério de aceite do PBI, regras de negócio e o texto original da task) está na [seção 9](#9-origem-dos-requisitos-especificação-e-planejamento).

## 1. Objetivo

Expor o registro e a consulta da retirada com escrita só do coordenador e leitura dos dois perfis, no formato único de erro, e conferir o Swagger contra o contrato. O serviço continua independente do endpoint, para o pedido de retirada do app (RF20, Sprint 4) reaproveitá-lo.

## 2. Contexto: o que já existe e deve ser reaproveitado

- `RetiradaService` (T102) e o contrato (T101); `Permissoes.LEITURA` e `Permissoes.ESCRITA_RECRUTAMENTO`; `Autenticacao.idDe(jwt)`; `ApiExceptionHandler`.
- O `ParticipacaoResponse` já traz `situacao` desde a T101.

## 3. Onde implementar

| Arquivo | Ação | Responsabilidade |
|---|---|---|
| `backend/ClinicalStudyPB/src/main/java/UEPB/ESWII/ClinicalStudyPB/controller/RetiradaController.java` | Criar | `POST` e `GET` de `/api/participacoes/{participacaoId}/retirada` |
| `backend/ClinicalStudyPB/src/main/java/UEPB/ESWII/ClinicalStudyPB/controller/dto/RetiradaRequest.java` | Criar | `motivo`, `solicitante`, `dataRetirada` com validação |
| `backend/ClinicalStudyPB/src/main/java/UEPB/ESWII/ClinicalStudyPB/controller/dto/RetiradaResponse.java` | Criar | Com `visitasCanceladas` |
| `backend/ClinicalStudyPB/src/test/java/UEPB/ESWII/ClinicalStudyPB/ContratoOpenApiSprint3Test.java` | Alterar | Habilita o caso da retirada (12 operações) |
| `docs/api/openapi-sprint3.yaml` | Alterar | Só se o Swagger divergir (corrige no mesmo PR) |

## 4. Requisitos a implementar

Cada requisito tem um identificador (`RQ-T103-NN`) para a revisão do Pull Request e para a conferência no fim da task: todos precisam estar atendidos.

### Endpoints

- **RQ-T103-01** `POST /api/participacoes/{participacaoId}/retirada` (só coordenador) recebe `RetiradaRequest` (`motivo` `@NotBlank` e `@Size(min = 5, max = 500)`, `solicitante` `@NotNull`, `dataRetirada` `@NotNull`) e responde **201** com `RetiradaResponse` (`id`, `participacaoId`, `motivo`, `solicitante`, `dataRetirada`, `registradoPor`, `registradoEm`, `visitasCanceladas`) e `Location` do `GET`.
- **RQ-T103-02** `GET /api/participacoes/{participacaoId}/retirada` (leitura: pesquisador e coordenador) devolve a mesma resposta, com `visitasCanceladas` calculado (visitas `CANCELADA` com o motivo "retirada do estudo"); **404** se o participante ainda não foi retirado ou a participação não existe.
- **RQ-T103-03** Erros no formato único: 400 (`VALIDACAO_CAMPOS`, `CORPO_INVALIDO`, `MOTIVO_OBRIGATORIO`, `DATA_RETIRADA_FUTURA`), 403 (pesquisador na escrita; administrador e participante em tudo), 404, 409 (`PARTICIPANTE_JA_RETIRADO`, `ESTUDO_ENCERRADO`) e 401.

### Swagger e independência do serviço

- **RQ-T103-04** `@Tag(name = "Retirada")` e `@Operation` com a descrição da regra 6 (irreversível; cancela visitas futuras; mantém a vaga). Remover o `@Disabled("T103")` do `ContratoOpenApiSprint3Test`, atualizar a contagem (12) e corrigir o YAML **no mesmo Pull Request** se o Swagger divergir.
- **RQ-T103-05** O controller só converte DTO e chama `RetiradaService`; o serviço **não** depende do controller (o RF20 do app chamará o mesmo serviço com `solicitante = PARTICIPANTE`).

## 5. Contrato, regras e códigos de erro

- Operações 11 e 12 da seção 2 das convenções.

## 6. Testes

- A bateria completa é a T106; aqui, o `ContratoOpenApiSprint3Test` verde e um teste de fumaça de cada operação.

## 7. Critérios de aceite

- [ ] O coordenador registra e consulta a retirada; o pesquisador só consulta.
- [ ] Os códigos de status e de erro seguem o contrato; o Swagger e o YAML coincidem.

## 8. Cuidados e fora de escopo

- Não criar endpoint de desfazer retirada.
- Não expor o motivo da retirada em endpoints do app (o app só mostra a data, T109).

## 9. Origem dos requisitos (especificação e planejamento)

Esta seção reúne, **sem paráfrase**, o que a especificação do projeto e o planejamento da Sprint 3 exigem para esta task, para que o documento sozinho mostre de onde vem cada requisito da seção 4. Onde o texto original difere das convenções (ajustes 1 a 10, [seção 1](00-convencoes-e-contrato-compartilhado.md#1-ajustes-em-relação-ao-planejamento-e-por-quê)), vale o que está neste documento e nas convenções.

### 9.1 Requisito funcional da especificação

- **RF16** (Módulo D — Acompanhamento; interface Web): “Registrar a retirada de um participante do estudo.”

### 9.2 Critério de aceite do PBI (planejamento)

> O coordenador registra a retirada de um participante ativo com motivo, quem solicitou e data; o participante passa a RETIRADO de forma irreversível, as visitas futuras agendadas são canceladas com o motivo "retirada do estudo" na mesma operação, e o histórico fica preservado.

### 9.3 Regras de negócio do planejamento que afetam esta task

| # | Regra proposta | Tasks afetadas (no planejamento) |
|---|---|---|
| 10 | Erros seguem o formato único da Sprint 2 (400 validação, 403 perfil sem permissão, 404 inexistente, 409 regra de negócio, sempre com o código da regra). As migrations usam faixas de versão por PBI (V3_19_* a V3_25_*); antes de começar, confirmar que a maior versão da Sprint 2 é menor que V3_19, senão trocar o prefixo V3 pelo número seguinte ao da última migration | T74 e todas as de backend |

### 9.4 Texto original da task no planejamento

> **T103 · Endpoints, autorização e Swagger da retirada.** Primeiro exponha o registro e a consulta da retirada, com escrita só para o coordenador e leitura para coordenador e pesquisador. Depois trate 400, 403, 404 e 409 no formato único de erro. Por fim, confira o Swagger contra o YAML e corrija o que divergir no mesmo Pull Request. O pedido de retirada feito pelo próprio participante no app (RF20) é da Sprint 4 e vai reaproveitar este serviço, então o serviço fica independente do endpoint web.

### 9.5 Requisitos da especificação que atravessam esta task

- Regra 9.2 (tecnologia): “O backend deve disponibilizar uma **API HTTP REST** compartilhada pela web e pelo mobile, documentada no padrão **OpenAPI** (Swagger UI pode ser usado para consulta e testes).”
- Entregas: “Tratamento de erros e validações das regras de negócio.”
- **Definition of Done** da Sprint (texto completo na [seção 11 das convenções](00-convencoes-e-contrato-compartilhado.md#11-definition-of-done-e-requisitos-transversais-da-especificação)): os critérios de aceite estão atendidos e demonstráveis com dados fictícios; o código foi revisado, passou no CI e entrou na `main` por Pull Request; há testes automatizados e, no backend, a cobertura do código de negócio permanece em 80% ou mais; erros e validações são tratados com mensagens claras; Swagger e contrato YAML refletem os endpoints; os dados de demonstração estão no perfil `demo`; documentação e capturas estão no repositório; e RF01 a RF11 continuam funcionando.


---
Parte do conjunto de documentos de design da Sprint 3: [índice](README.md) · [convenções e contrato](00-convencoes-e-contrato-compartilhado.md).
