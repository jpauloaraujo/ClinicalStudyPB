# T131 · Endpoints, autorização e Swagger do atendimento

> **Sprint 4** · PBI-27 · RF19 Atendimento dos pedidos de reagendamento na web  
> **Responsável:** I4 · **Estimativa:** 3h · **Prazo:** Qui 26/11  
> **Depende de:** T130  
> **Situação:** ⬜ A implementar (este documento é o guia para quem for fazer)

> Antes de começar, leia as [convenções e o contrato compartilhado da Sprint 4](00-convencoes-e-contrato-compartilhado.md): ajustes ao planejamento, endpoints, DTOs, códigos de erro, tabelas e dados de demonstração valem para esta task. A origem dos requisitos está na [seção 9](#9-origem-dos-requisitos-especificação-e-planejamento).

## 1. Objetivo

Expor listagem, contagem, atendimento e recusa com autorização, erros no formato único e Swagger fiel ao contrato.

## 2. Contexto: o que já existe e deve ser reaproveitado

- `Permissoes.LEITURA` e `ESCRITA_RECRUTAMENTO`; `RetiradaController` é o modelo de anotações.
- `Autenticacao.idDe(jwt)` identifica o coordenador.

## 3. Onde implementar

| Arquivo | Ação | Responsabilidade |
|---|---|---|
| `backend/ClinicalStudyPB/src/main/java/UEPB/ESWII/ClinicalStudyPB/controller/SolicitacaoController.java` | Criar | Operações 4 a 7 |
| `backend/ClinicalStudyPB/src/test/java/UEPB/ESWII/ClinicalStudyPB/SolicitacaoEndpointTest.java` | Criar | Fumaça e permissões |
| `backend/ClinicalStudyPB/src/test/java/UEPB/ESWII/ClinicalStudyPB/ContratoOpenApiSprint4Test.java` | Alterar | Reativar os casos |
| `web/ClinicalStudyPB/src/api/schema-sprint4.d.ts` | Alterar | Regenerado |
| `docs/api/openapi-sprint4.yaml` | Alterar | Ajustes de conferência |

## 4. Requisitos a implementar

Todos os requisitos (`RQ-T131-NN`) precisam estar atendidos para a task ser concluída.

- **RQ-T131-01** `GET` de listagem e contagem com `@PreAuthorize(Permissoes.LEITURA)`.
- **RQ-T131-02** `POST` de atendimento e recusa com `@PreAuthorize(Permissoes.ESCRITA_RECRUTAMENTO)`.
- **RQ-T131-03** `@Valid` nos corpos: `dataHora` obrigatória; `resposta` até 500 (o mínimo é regra do serviço).
- **RQ-T131-04** Enum inválido em `tipo` ou `situacao`: 400 `PARAMETRO_INVALIDO`.
- **RQ-T131-05** 401, 403 (administrador, participante e, na escrita, pesquisador), 404 e 409 no formato único.
- **RQ-T131-06** `@Tag(name = "Solicitações")` e `@Operation` com regra e códigos.
- **RQ-T131-07** Conferir Swagger × YAML e corrigir no mesmo Pull Request; remover `@Disabled("T131")`.
- **RQ-T131-08** Regenerar os tipos da web.

## 5. Contrato, regras e códigos de erro

Operações 4 a 7, seções 2, 3.1 e 4.

## 6. Testes

Fumaça por operação e matriz de permissões; o restante é a T134.

## 7. Critérios de aceite

- [ ] Coordenador escreve; pesquisador só lê; demais recebem 403.
- [ ] Contrato verde.

## 8. Cuidados e fora de escopo

- O caminho `/api/solicitacoes/...` não leva `estudoId`: não confiar em dado do cliente além do id do pedido.

## 9. Origem dos requisitos (especificação e planejamento)

Texto **sem paráfrase** da especificação e do planejamento. Onde ele diverge dos [ajustes da seção 1 das convenções](00-convencoes-e-contrato-compartilhado.md#1-ajustes-em-relação-ao-planejamento-e-por-quê), valem as convenções e este documento.

### 9.1 Requisito da especificação

- **RF19** (Módulo E, Aplicativo do participante; interface Mobile): “Permitir confirmar presença prevista ou solicitar reagendamento de visita.”

### 9.2 Critério de aceite do PBI (planejamento)

> o coordenador vê os pedidos do estudo, atende um reagendamento escolhendo uma data válida ou o recusa com resposta, e o participante vê o resultado no app; as presenças confirmadas aparecem na agenda e no histórico; o pesquisador consulta tudo em modo somente leitura.

### 9.3 Regras de negócio do planejamento que afetam esta task

| # | Regra proposta | Tasks afetadas |
|---|---|---|
| 8 | Erros seguem o formato único da Sprint 2 (400, 403, 404 e 409, sempre com o código da regra). As migrations usam as faixas V4_26_\* a V4_32_\*, todas acima das faixas V3 da Sprint 3 | T122 e todas as de backend |

### 9.4 Texto original da task no planejamento

> **T131 · Endpoints, autorização e Swagger do atendimento.** Primeiro exponha a listagem, o atendimento e a recusa, com escrita só para o coordenador e leitura para coordenador e pesquisador. Depois trate 400, 403, 404 e 409 no formato único de erro. Por fim, confira o Swagger contra o YAML.

### 9.5 Requisitos que atravessam esta task

- Regra 9.2: “O backend deve disponibilizar uma **API HTTP REST** compartilhada pela web e pelo mobile, documentada no padrão **OpenAPI**”.
- Entregas: “Persistência dos dados, com scripts de criação/migração do banco e dados fictícios para demonstração” e “Tratamento de erros e validações das regras de negócio”.
- Testes (seção 7): “Testes unitários para todas as regras de negócio relevantes”, “Testes de integração para os principais endpoints” e “Execução automatizada dos testes a cada pull request”.
- Regra 9.3: “A interface **web** atende aos perfis de pesquisador, coordenador e administrador.”
- **Definition of Done** (texto completo na [seção 11 das convenções](00-convencoes-e-contrato-compartilhado.md#11-definition-of-done)): critérios de aceite demonstráveis com dados fictícios; revisão, CI e Pull Request; testes automatizados e 80% de cobertura do negócio no backend; erros com mensagens claras; Swagger e YAML atualizados; dados de demonstração, documentação, capturas e nota de bastão no repositório; RF01 a RF18 continuam funcionando.

---
Parte dos documentos de design da Sprint 4: [índice](README.md) · [convenções e contrato](00-convencoes-e-contrato-compartilhado.md).
