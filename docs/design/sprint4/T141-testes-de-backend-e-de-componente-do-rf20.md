# T141 · Testes de backend e de componente do RF20

> **Sprint 4** · PBI-28 · RF20 Retirada voluntária pelo app  
> **Responsável:** I5 · **Estimativa:** 3h · **Prazo:** Dom 29/11  
> **Depende de:** T138, T139, T140  
> **Situação:** ⬜ A implementar (este documento é o guia para quem for fazer)

> Antes de começar, leia as [convenções e o contrato compartilhado da Sprint 4](00-convencoes-e-contrato-compartilhado.md): ajustes ao planejamento, endpoints, DTOs, códigos de erro, tabelas e dados de demonstração valem para esta task. A origem dos requisitos está na [seção 9](#9-origem-dos-requisitos-especificação-e-planejamento).

## 1. Objetivo

Cobrir o RF20 com testes de regra, de transação e de componente (app e web).

## 2. Contexto: o que já existe e deve ser reaproveitado

- `RetiradaCascataIntegracaoTest` e `RetiradaConcorrenciaTest` são os modelos.

## 3. Onde implementar

| Arquivo | Ação | Responsabilidade |
|---|---|---|
| `backend/ClinicalStudyPB/src/test/java/UEPB/ESWII/ClinicalStudyPB/PedidoRetiradaEndpointTest.java` | Alterar | Status e regras |
| `backend/ClinicalStudyPB/src/test/java/UEPB/ESWII/ClinicalStudyPB/EfetivacaoRetiradaTest.java` | Alterar | Transação real |
| `backend/ClinicalStudyPB/src/test/java/UEPB/ESWII/ClinicalStudyPB/PedidoRetiradaConcorrenciaTest.java` | Criar | Corridas |
| `mobile/ClinicalStudyPB/src/telas/__tests__/retirada.test.tsx` | Criar | Tela do app |
| `web/ClinicalStudyPB/src/pages/estudos/detalhe/acompanhamento/DialogoRetirada.test.tsx` | Alterar | Modo pedido |

## 4. Requisitos a implementar

Todos os requisitos (`RQ-T141-NN`) precisam estar atendidos para a task ser concluída.

- **RQ-T141-01** Pedido duplicado, participação já retirada, estudo encerrado, ciência ausente e bloqueio do RF19.
- **RQ-T141-02** Efetivação: pedido `ATENDIDA`, participação `RETIRADO`, visitas futuras canceladas, reagendamentos recusados com `participante retirado`, retirada com solicitante `PARTICIPANTE`.
- **RQ-T141-03** Retirada direta pelo RF16 com pedidos pendentes tem o mesmo fechamento.
- **RQ-T141-04** Transação: falha forçada no cancelamento não deixa nada gravado.
- **RQ-T141-05** Concorrência: dois pedidos simultâneos (um 201, um 409); efetivar duas vezes (um 201, um 409); efetivar contra retirada direta; efetivar contra registrar desfecho em visita com reagendamento pendente, sem 500, sem impasse e sem estado misto.
- **RQ-T141-06** Endpoints: 201, 400, 401, 403, 404 e 409; rodar no PostgreSQL.
- **RQ-T141-07** App: ciência, motivo opcional, confirmação, aviso pendente, marca retirado e botões do RF19 ocultos.
- **RQ-T141-08** Web: diálogo preenchido, ausência de Recusar e aviso na ficha.
- **RQ-T141-09** Registrar cobertura dos serviços alterados.

## 5. Contrato, regras e códigos de erro

Seção 4.

## 6. Testes

Os itens acima.

## 7. Critérios de aceite

- [ ] Suítes das três camadas verdes, com a transação comprovada.

## 8. Cuidados e fora de escopo

- Sem `@Transactional` nos testes de reversão e corrida.

## 9. Origem dos requisitos (especificação e planejamento)

Texto **sem paráfrase** da especificação e do planejamento. Onde ele diverge dos [ajustes da seção 1 das convenções](00-convencoes-e-contrato-compartilhado.md#1-ajustes-em-relação-ao-planejamento-e-por-quê), valem as convenções e este documento.

### 9.1 Requisito da especificação

- **RF20** (Módulo E, Aplicativo do participante; interface Mobile): “Permitir solicitar retirada voluntária do estudo.”

### 9.2 Critério de aceite do PBI (planejamento)

> o participante solicita a retirada de um estudo pelo app, ciente dos efeitos; o coordenador vê o pedido e efetiva a retirada pelo RF16; o app mostra o pedido pendente e, depois, a participação como RETIRADO; pedidos duplicados ou de participação já retirada são recusados.

### 9.3 Regras de negócio do planejamento que afetam esta task

| # | Regra proposta | Tasks afetadas |
|---|---|---|
| 8 | Erros seguem o formato único da Sprint 2 (400, 403, 404 e 409, sempre com o código da regra). As migrations usam as faixas V4_26_\* a V4_32_\*, todas acima das faixas V3 da Sprint 3 | T122 e todas as de backend |

### 9.4 Texto original da task no planejamento

> **T141 · Testes de backend e de componente do RF20.** Escreva os testes junto com o código. Primeiro teste o pedido duplicado, a participação já retirada, a ciência ausente e o bloqueio do RF19 com retirada pendente. Depois teste a efetivação: pedido ATENDIDA, participação RETIRADO, visitas futuras canceladas e reagendamentos pendentes recusados, tudo na mesma transação. Por fim, escreva os testes de componente da tela do app e do diálogo web.

### 9.5 Requisitos que atravessam esta task

- Regra 9.2: “O backend deve disponibilizar uma **API HTTP REST** compartilhada pela web e pelo mobile, documentada no padrão **OpenAPI**”.
- Entregas: “Persistência dos dados, com scripts de criação/migração do banco e dados fictícios para demonstração” e “Tratamento de erros e validações das regras de negócio”.
- Testes (seção 7): “Testes unitários para todas as regras de negócio relevantes”, “Testes de integração para os principais endpoints” e “Execução automatizada dos testes a cada pull request”.
- Regras 9.3 e 9.4: web para a equipe do estudo e app para o participante.
- **Definition of Done** (texto completo na [seção 11 das convenções](00-convencoes-e-contrato-compartilhado.md#11-definition-of-done)): critérios de aceite demonstráveis com dados fictícios; revisão, CI e Pull Request; testes automatizados e 80% de cobertura do negócio no backend; erros com mensagens claras; Swagger e YAML atualizados; dados de demonstração, documentação, capturas e nota de bastão no repositório; RF01 a RF18 continuam funcionando.

---
Parte dos documentos de design da Sprint 4: [índice](README.md) · [convenções e contrato](00-convencoes-e-contrato-compartilhado.md).
