# T88 · Contrato OpenAPI dos resultados

> **Sprint 3** · PBI-21 · RF14 Resultados fictícios das avaliações previstas  
> **Responsável:** I6 · **Estimativa:** 2h · **Prazo:** Sáb 14/11  
> **Depende de:** T87  
> **Situação:** ⬜ A implementar (este documento é o guia para quem for fazer)

> Antes de começar, leia as [convenções e o contrato compartilhado da Sprint 3](00-convencoes-e-contrato-compartilhado.md): nomes, endpoints, DTOs, códigos de erro, tabelas e dados de demonstração valem para esta task. A origem dos requisitos (especificação, critério de aceite do PBI, regras de negócio e o texto original da task) está na [seção 9](#9-origem-dos-requisitos-especificação-e-planejamento).

## 1. Objetivo

Escrever, antes do código, o contrato das duas operações de resultados (consulta e registro ou edição por atividade) e gerar os tipos para a tela.

## 2. Contexto: o que já existe e deve ser reaproveitado

- `docs/api/openapi-sprint3.yaml` já tem as operações do PBI-19 e do PBI-20; cada PBI acrescenta **só os seus** caminhos. O schema `Erro` e o enum `SituacaoVisita` já existem.
- Os DTOs e os exemplos fictícios estão na seção 3 das convenções (`ResultadosDaVisitaResponse`, `ResultadoRequest`), e os códigos, na seção 4.
- `npm run api:tipos` gera `schema.d.ts` e `schema-sprint3.d.ts` (T74); `src/api/tipos.ts` reexporta os tipos.

## 3. Onde implementar

| Arquivo | Ação | Responsabilidade |
|---|---|---|
| `docs/api/openapi-sprint3.yaml` | Alterar | Duas operações, schemas e erros dos resultados |
| `web/ClinicalStudyPB/src/api/schema-sprint3.d.ts` | Alterar | Regenerado |
| `web/ClinicalStudyPB/src/api/tipos.ts` | Alterar | Reexporta os tipos dos resultados |
| `backend/ClinicalStudyPB/src/test/java/UEPB/ESWII/ClinicalStudyPB/ContratoOpenApiSprint3Test.java` | Alterar | Contagem de operações e caso `@Disabled("T90")` |

## 4. Requisitos a implementar

Cada requisito tem um identificador (`RQ-T88-NN`) para a revisão do Pull Request e para a conferência no fim da task: todos precisam estar atendidos.

### Operações

- **RQ-T88-01** `GET /api/visitas-agendadas/{visitaAgendadaId}/resultados` (leitura: pesquisador e coordenador): lista **cada atividade da categoria `AVALIACAO_CLINICA`** da visita prevista, com o resultado ou vazio, e as contagens `total`, `registrados` e `pendentes`.
- **RQ-T88-02** `PUT /api/visitas-agendadas/{visitaAgendadaId}/resultados/{acaoId}` (só pesquisador): registra ou edita o resultado **de uma atividade**; responde **201** no primeiro registro e **200** na edição, com o `ResultadoAtividadeResponse`.

### Schemas e erros

- **RQ-T88-03** Declarar `ResultadosDaVisitaResponse` (com `visitaAgendadaId`, `situacaoVisita`, `bloqueio` anulável com o enum `VISITA_NAO_REALIZADA`, `ESTUDO_ENCERRADO`, `PARTICIPANTE_RETIRADO`, `total`, `registrados`, `pendentes` e a lista `atividades`), `AtividadeComResultado`, `ResultadoAtividadeResponse` e `ResultadoRequest` (`valor` obrigatório de 1 a 200 caracteres, `unidade` até 30, `observacao` até 500), com exemplos fictícios.
- **RQ-T88-04** Documentar os erros: `400` (`VALIDACAO_CAMPOS`, `CORPO_INVALIDO`, `VALOR_OBRIGATORIO`), `403`, `404`, `409` (`VISITA_NAO_REALIZADA`, `ATIVIDADE_FORA_DA_CATEGORIA_AVALIACAO`, `ATIVIDADE_NAO_PERTENCE_A_VISITA`, `ESTUDO_ENCERRADO` e, a partir da T102, `PARTICIPANTE_RETIRADO`), todos no formato único de erro.
- **RQ-T88-05** Descrever em cada operação quem pode chamar: escrita só do pesquisador, leitura de pesquisador e coordenador; o coordenador **nunca** escreve resultados.
- **RQ-T88-06** Explicar o campo `bloqueio`: é o motivo pelo qual a tela deve mostrar os resultados somente leitura (a tela decide pelo perfil; o servidor pelo estado).

### Tipos e teste de contrato

- **RQ-T88-07** Rodar `npm run api:tipos`, reexportar em `src/api/tipos.ts` os tipos dos resultados e conferir `npx tsc -b` e `npm run lint`.
- **RQ-T88-08** Atualizar a contagem de operações do `ContratoOpenApiSprint3Test` (7 + 2 = 9) com o novo caso `@Disabled("T90")`, habilitado na T90.

## 5. Contrato, regras e códigos de erro

- Operações 8 e 9 da seção 2 das convenções.

## 6. Testes

- `ContratoOpenApiSprint3Test` (caso novo desabilitado até a T90) e `tsc -b` com os tipos regenerados.

## 7. Critérios de aceite

- [ ] O YAML descreve as duas operações com DTOs, exemplos, perfis e códigos de erro.
- [ ] Os tipos TypeScript compilam e estão reexportados.

## 8. Cuidados e fora de escopo

- Acrescentar só os caminhos desta fatia ao YAML (evita conflito).
- Sem mocks: nenhum handler MSW.

## 9. Origem dos requisitos (especificação e planejamento)

Esta seção reúne, **sem paráfrase**, o que a especificação do projeto e o planejamento da Sprint 3 exigem para esta task, para que o documento sozinho mostre de onde vem cada requisito da seção 4. Onde o texto original difere das convenções (ajustes 1 a 10, [seção 1](00-convencoes-e-contrato-compartilhado.md#1-ajustes-em-relação-ao-planejamento-e-por-quê)), vale o que está neste documento e nas convenções.

### 9.1 Requisito funcional da especificação

- **RF14** (Módulo D — Acompanhamento; interface Web): “Registrar resultados fictícios das avaliações previstas.”

### 9.2 Critério de aceite do PBI (planejamento)

> O pesquisador registra e edita o resultado de cada atividade da categoria avaliação de uma visita REALIZADA; visitas não realizadas, atividades de outras categorias e estudos encerrados são recusados; o coordenador consulta os resultados em modo somente leitura.

### 9.3 Regras de negócio do planejamento que afetam esta task

| # | Regra proposta | Tasks afetadas (no planejamento) |
|---|---|---|
| 10 | Erros seguem o formato único da Sprint 2 (400 validação, 403 perfil sem permissão, 404 inexistente, 409 regra de negócio, sempre com o código da regra). As migrations usam faixas de versão por PBI (V3_19_* a V3_25_*); antes de começar, confirmar que a maior versão da Sprint 2 é menor que V3_19, senão trocar o prefixo V3 pelo número seguinte ao da última migration | T74 e todas as de backend |

### 9.4 Texto original da task no planejamento

> **T88 · Contrato OpenAPI dos resultados.** Primeiro descreva os dois endpoints: a consulta dos resultados de uma visita (cada atividade de avaliação com o resultado ou vazio, e a contagem de pendentes) e o registro ou edição de um resultado por atividade. Depois escreva os DTOs com exemplos fictícios e os códigos de erro (visita não realizada, atividade fora da categoria avaliação, participante retirado e estudo encerrado), acrescentando só os caminhos desta fatia ao YAML da Sprint. Por fim, gere os tipos para a tela.

### 9.5 Requisitos da especificação que atravessam esta task

- Regra 9.2 (tecnologia): “O backend deve disponibilizar uma **API HTTP REST** compartilhada pela web e pelo mobile, documentada no padrão **OpenAPI** (Swagger UI pode ser usado para consulta e testes).”
- **Definition of Done** da Sprint (texto completo na [seção 11 das convenções](00-convencoes-e-contrato-compartilhado.md#11-definition-of-done-e-requisitos-transversais-da-especificação)): os critérios de aceite estão atendidos e demonstráveis com dados fictícios; o código foi revisado, passou no CI e entrou na `main` por Pull Request; há testes automatizados e, no backend, a cobertura do código de negócio permanece em 80% ou mais; erros e validações são tratados com mensagens claras; Swagger e contrato YAML refletem os endpoints; os dados de demonstração estão no perfil `demo`; documentação e capturas estão no repositório; e RF01 a RF11 continuam funcionando.


---
Parte do conjunto de documentos de design da Sprint 3: [índice](README.md) · [convenções e contrato](00-convencoes-e-contrato-compartilhado.md).
