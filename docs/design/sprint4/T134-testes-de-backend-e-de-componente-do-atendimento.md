# T134 · Testes de backend e de componente do atendimento

> **Sprint 4** · PBI-27 · RF19 Atendimento dos pedidos de reagendamento na web  
> **Responsável:** I4 · **Estimativa:** 3h · **Prazo:** Sex 27/11  
> **Depende de:** T130, T132, T133  
> **Situação:** ⬜ A implementar (este documento é o guia para quem for fazer)

> Antes de começar, leia as [convenções e o contrato compartilhado da Sprint 4](00-convencoes-e-contrato-compartilhado.md): ajustes ao planejamento, endpoints, DTOs, códigos de erro, tabelas e dados de demonstração valem para esta task. A origem dos requisitos está na [seção 9](#9-origem-dos-requisitos-especificação-e-planejamento).

## 1. Objetivo

Cobrir o atendimento com testes de regra, de endpoint com transação real e de componente dos diálogos.

## 2. Contexto: o que já existe e deve ser reaproveitado

- Código das T129 a T133; `RetiradaConcorrenciaTest` e `DesfechoConcorrenciaTest` como modelos.

## 3. Onde implementar

| Arquivo | Ação | Responsabilidade |
|---|---|---|
| `backend/ClinicalStudyPB/src/test/java/UEPB/ESWII/ClinicalStudyPB/AtendimentoSolicitacaoServiceTest.java` | Alterar | Matriz |
| `backend/ClinicalStudyPB/src/test/java/UEPB/ESWII/ClinicalStudyPB/SolicitacaoEndpointTest.java` | Alterar | Todos os status |
| `backend/ClinicalStudyPB/src/test/java/UEPB/ESWII/ClinicalStudyPB/AtendimentoTransacaoTest.java` | Criar | Reversão e concorrência |
| `web/ClinicalStudyPB/src/pages/estudos/detalhe/solicitacoes/AbaSolicitacoes.test.tsx` | Criar | Lista e perfis |
| `web/ClinicalStudyPB/src/pages/estudos/detalhe/solicitacoes/DialogoAtender.test.tsx` | Criar | Diálogo |
| `web/ClinicalStudyPB/src/pages/estudos/detalhe/solicitacoes/DialogoRecusar.test.tsx` | Criar | Diálogo |

## 4. Requisitos a implementar

Todos os requisitos (`RQ-T134-NN`) precisam estar atendidos para a task ser concluída.

- **RQ-T134-01** Regra: atendimento fora da janela, no passado, estudo encerrado, participante retirado, pedido já respondido, recusa sem resposta e recusa de retirada.
- **RQ-T134-02** Recusa automática após cada tipo de desfecho, com a resposta padrão e o usuário que registrou.
- **RQ-T134-03** Endpoints: 200, 400, 401, 403, 404 e 409.
- **RQ-T134-04** Transação: quando o reagendamento falha, o pedido segue `PENDENTE`, a visita mantém data e confirmação.
- **RQ-T134-05** Concorrência: atender e recusar o mesmo pedido ao mesmo tempo dá um sucesso e um 409; atender contra registrar desfecho não gera 500 nem estado misto.
- **RQ-T134-06** Rodar no PostgreSQL do CI.
- **RQ-T134-07** Componentes: os dois diálogos (campos obrigatórios, erro do servidor, envio desabilitado, fechar), filtros, contagem e somente leitura.
- **RQ-T134-08** Registrar a cobertura de `AtendimentoSolicitacaoService`, `FechamentoDeSolicitacoes` e `SolicitacaoConsultaService`.

## 5. Contrato, regras e códigos de erro

Seção 4.

## 6. Testes

Os itens acima; `./mvnw verify` e `npm test` na web.

## 7. Critérios de aceite

- [ ] Suítes verdes e transação comprovada por teste.

## 8. Cuidados e fora de escopo

- Testes de transação e concorrência sem `@Transactional`.

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

> **T134 · Testes de backend e de componente do atendimento.** Escreva os testes junto com o código. Primeiro teste o atendimento fora da janela, o pedido já respondido, a recusa sem resposta e a recusa automática após desfecho. Depois teste os endpoints (200, 400, 403, 404 e 409) e a transação: se o reagendamento falha, o pedido continua PENDENTE. Por fim, escreva os testes de componente dos dois diálogos.

### 9.5 Requisitos que atravessam esta task

- Regra 9.2: “O backend deve disponibilizar uma **API HTTP REST** compartilhada pela web e pelo mobile, documentada no padrão **OpenAPI**”.
- Entregas: “Persistência dos dados, com scripts de criação/migração do banco e dados fictícios para demonstração” e “Tratamento de erros e validações das regras de negócio”.
- Testes (seção 7): “Testes unitários para todas as regras de negócio relevantes”, “Testes de integração para os principais endpoints” e “Execução automatizada dos testes a cada pull request”.
- Regra 9.3: “A interface **web** atende aos perfis de pesquisador, coordenador e administrador.”
- **Definition of Done** (texto completo na [seção 11 das convenções](00-convencoes-e-contrato-compartilhado.md#11-definition-of-done)): critérios de aceite demonstráveis com dados fictícios; revisão, CI e Pull Request; testes automatizados e 80% de cobertura do negócio no backend; erros com mensagens claras; Swagger e YAML atualizados; dados de demonstração, documentação, capturas e nota de bastão no repositório; RF01 a RF18 continuam funcionando.

---
Parte dos documentos de design da Sprint 4: [índice](README.md) · [convenções e contrato](00-convencoes-e-contrato-compartilhado.md).
