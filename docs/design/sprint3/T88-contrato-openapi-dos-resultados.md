# T88 · Contrato OpenAPI dos resultados

> **Sprint 3** · PBI-21 · RF14 Resultados fictícios das avaliações previstas  
> **Responsável:** I6 · **Estimativa:** 2h · **Prazo:** Sáb 14/11  
> **Depende de:** T87  
> **Situação:** ⬜ A implementar (este documento é o guia para quem for fazer)

> Antes de começar, leia as [convenções e o contrato compartilhado da Sprint 3](00-convencoes-e-contrato-compartilhado.md): nomes, endpoints, DTOs, códigos de erro, tabelas e dados de demonstração valem para esta task.

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

---
Parte do conjunto de documentos de design da Sprint 3: [índice](README.md) · [convenções e contrato](00-convencoes-e-contrato-compartilhado.md).
