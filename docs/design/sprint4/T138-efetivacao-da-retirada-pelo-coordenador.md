# T138 · Efetivação da retirada pelo coordenador

> **Sprint 4** · PBI-28 · RF20 Retirada voluntária pelo app  
> **Responsável:** I5 · **Estimativa:** 4h · **Prazo:** Sáb 28/11  
> **Depende de:** T136, Sprint 3 (T102)  
> **Situação:** ⬜ A implementar (este documento é o guia para quem for fazer)

> Antes de começar, leia as [convenções e o contrato compartilhado da Sprint 4](00-convencoes-e-contrato-compartilhado.md): ajustes ao planejamento, endpoints, DTOs, códigos de erro, tabelas e dados de demonstração valem para esta task. A origem dos requisitos está na [seção 9](#9-origem-dos-requisitos-especificação-e-planejamento).

## 1. Objetivo

Efetivar a retirada a partir do pedido, em uma transação, e fechar os pedidos também na retirada direta do RF16.

## 2. Contexto: o que já existe e deve ser reaproveitado

- `RetiradaService.registrar(participacaoId, ComandoRetirada, usuarioId)` bloqueia a participação, grava a retirada, muda a situação e cancela visitas futuras.
- `FechamentoDeSolicitacoes.recusarReagendamentosDaParticipacao` (T130).
- O planejamento pede revisão com o dono da T102 (I4).

## 3. Onde implementar

| Arquivo | Ação | Responsabilidade |
|---|---|---|
| `backend/ClinicalStudyPB/src/main/java/UEPB/ESWII/ClinicalStudyPB/service/AtendimentoSolicitacaoService.java` | Alterar | `efetivarRetirada` |
| `backend/ClinicalStudyPB/src/main/java/UEPB/ESWII/ClinicalStudyPB/service/RetiradaService.java` | Alterar | Fechamento dos pedidos |
| `backend/ClinicalStudyPB/src/main/java/UEPB/ESWII/ClinicalStudyPB/service/FechamentoDeSolicitacoes.java` | Alterar | `atenderPedidoDeRetirada` |
| `backend/ClinicalStudyPB/src/main/java/UEPB/ESWII/ClinicalStudyPB/controller/SolicitacaoController.java` | Alterar | Operação 9 |
| `backend/ClinicalStudyPB/src/main/java/UEPB/ESWII/ClinicalStudyPB/controller/dto/EfetivacaoRetiradaRequest.java` | Criar | `motivo`, `dataRetirada` |
| `backend/ClinicalStudyPB/src/test/java/UEPB/ESWII/ClinicalStudyPB/EfetivacaoRetiradaTest.java` | Criar | Regra 5 |
| `backend/ClinicalStudyPB/src/test/java/UEPB/ESWII/ClinicalStudyPB/RetiradaServiceTest.java` | Alterar | Novo colaborador |

## 4. Requisitos a implementar

Todos os requisitos (`RQ-T138-NN`) precisam estar atendidos para a task ser concluída.

- **RQ-T138-01** `efetivarRetirada(solicitacaoId, motivo, dataRetirada, usuarioId)`, em uma transação: obter por projeção `participacaoId` e `tipo` (404 → `TIPO_DE_SOLICITACAO_INCOMPATIVEL`); bloquear a participação e depois o pedido; conferir `PENDENTE` (`SOLICITACAO_JA_RESPONDIDA`); chamar `RetiradaService.registrar` com `solicitante = PARTICIPANTE`.
- **RQ-T138-02** O servidor define o solicitante; o corpo não o aceita.
- **RQ-T138-03** `motivo` e `dataRetirada` seguem as validações do RF16 (`MOTIVO_OBRIGATORIO`, `DATA_RETIRADA_FUTURA`); `dataRetirada` obrigatória.
- **RQ-T138-04** `RetiradaService.registrar` passa a, na mesma transação: marcar o pedido de retirada pendente como `ATENDIDA` e recusar os reagendamentos pendentes com `participante retirado`. Assim a efetivação e a retirada direta têm o mesmo fechamento (um só lugar).
- **RQ-T138-05** No `RetiradaService`, cancelar as visitas (como hoje) e **depois** fechar os pedidos, para manter a ordem de bloqueio participação → visita → solicitação. O cancelamento em cascata não passa pelo `DesfechoVisitaService`, então a resposta gravada é a da retirada.
- **RQ-T138-06** A retirada direta com pedido pendente é permitida e o atende, qualquer que seja o solicitante informado.
- **RQ-T138-07** O pedido de retirada não pode ser recusado (`RETIRADA_NAO_PODE_SER_RECUSADA`, regra 5).
- **RQ-T138-08** `POST /api/solicitacoes/{solicitacaoId}/retirada` com `ESCRITA_RECRUTAMENTO`, `201` com `RetiradaResponse` e `Location` para `/api/participacoes/{id}/retirada`.
- **RQ-T138-09** Qualquer falha desfaz tudo (participação, visitas e pedidos).
- **RQ-T138-10** Reativar o teste de contrato, registrar a revisão de I4 no Pull Request.

## 5. Contrato, regras e códigos de erro

Operação 9; ordem de bloqueio participação → visita → solicitação.

## 6. Testes

Efetivação completa, retirada direta com pedido, estudo encerrado, pedido já atendido, falha no meio revertendo tudo.

## 7. Critérios de aceite

- [ ] Pedido `ATENDIDA`, participação `RETIRADO`, visitas futuras canceladas e reagendamentos recusados, tudo junto.
- [ ] RF16 continua funcionando sem pedido.

## 8. Cuidados e fora de escopo

- Não duplicar o cancelamento em cascata.
- A vaga continua ocupada (RF16).

## 9. Origem dos requisitos (especificação e planejamento)

Texto **sem paráfrase** da especificação e do planejamento. Onde ele diverge dos [ajustes da seção 1 das convenções](00-convencoes-e-contrato-compartilhado.md#1-ajustes-em-relação-ao-planejamento-e-por-quê), valem as convenções e este documento.

### 9.1 Requisito da especificação

- **RF20** (Módulo E, Aplicativo do participante; interface Mobile): “Permitir solicitar retirada voluntária do estudo.”

### 9.2 Critério de aceite do PBI (planejamento)

> o participante solicita a retirada de um estudo pelo app, ciente dos efeitos; o coordenador vê o pedido e efetiva a retirada pelo RF16; o app mostra o pedido pendente e, depois, a participação como RETIRADO; pedidos duplicados ou de participação já retirada são recusados.

### 9.3 Regras de negócio do planejamento que afetam esta task

| # | Regra proposta | Tasks afetadas |
|---|---|---|
| 5 | O pedido de retirada não pode ser recusado: o coordenador o efetiva pelo serviço do RF16 (T102), com "quem solicitou" igual a participante. Na efetivação, o pedido vira ATENDIDA e os pedidos de reagendamento pendentes daquela participação são recusados com a resposta "participante retirado" | T138, T140 |
| 8 | Erros seguem o formato único da Sprint 2 (400, 403, 404 e 409, sempre com o código da regra). As migrations usam as faixas V4_26_\* a V4_32_\*, todas acima das faixas V3 da Sprint 3 | T122 e todas as de backend |

### 9.4 Texto original da task no planejamento

> **T138 · Efetivação da retirada pelo coordenador.** Primeiro crie o endpoint em que o coordenador registra a retirada a partir do pedido, chamando o serviço da T102 com "quem solicitou" igual a participante e o motivo informado no app. Depois, na mesma transação, marque o pedido como ATENDIDA e recuse os pedidos de reagendamento pendentes da participação com a resposta "participante retirado". Em seguida aplique o mesmo fechamento quando o coordenador registrar uma retirada direta pelo RF16. Por fim, garanta que o pedido de retirada não pode ser recusado (regra 5), revisando com I4, dono da T102.

### 9.5 Requisitos que atravessam esta task

- Regra 9.2: “O backend deve disponibilizar uma **API HTTP REST** compartilhada pela web e pelo mobile, documentada no padrão **OpenAPI**”.
- Entregas: “Persistência dos dados, com scripts de criação/migração do banco e dados fictícios para demonstração” e “Tratamento de erros e validações das regras de negócio”.
- Testes (seção 7): “Testes unitários para todas as regras de negócio relevantes”, “Testes de integração para os principais endpoints” e “Execução automatizada dos testes a cada pull request”.
- Regras 9.3 e 9.4: web para a equipe do estudo e app para o participante.
- **Definition of Done** (texto completo na [seção 11 das convenções](00-convencoes-e-contrato-compartilhado.md#11-definition-of-done)): critérios de aceite demonstráveis com dados fictícios; revisão, CI e Pull Request; testes automatizados e 80% de cobertura do negócio no backend; erros com mensagens claras; Swagger e YAML atualizados; dados de demonstração, documentação, capturas e nota de bastão no repositório; RF01 a RF18 continuam funcionando.

---
Parte dos documentos de design da Sprint 4: [índice](README.md) · [convenções e contrato](00-convencoes-e-contrato-compartilhado.md).
