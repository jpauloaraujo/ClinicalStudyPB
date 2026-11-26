# T130 · Regras de atendimento e recusa do reagendamento

> **Sprint 4** · PBI-27 · RF19 Atendimento dos pedidos de reagendamento na web  
> **Responsável:** I4 · **Estimativa:** 4h · **Prazo:** Qui 26/11  
> **Depende de:** T129, Sprint 3 (T75, T81)  
> **Situação:** ⬜ A implementar (este documento é o guia para quem for fazer)

> Antes de começar, leia as [convenções e o contrato compartilhado da Sprint 4](00-convencoes-e-contrato-compartilhado.md): ajustes ao planejamento, endpoints, DTOs, códigos de erro, tabelas e dados de demonstração valem para esta task. A origem dos requisitos está na [seção 9](#9-origem-dos-requisitos-especificação-e-planejamento).

## 1. Objetivo

Implementar o atendimento e a recusa do reagendamento, atômicos, e a recusa automática quando a visita recebe desfecho, é cancelada ou o estudo é encerrado.

## 2. Contexto: o que já existe e deve ser reaproveitado

- `AgendamentoService.reagendar(visitaId, dataHora, usuarioId)` aplica todas as regras do RF12 (`DATA_FORA_DA_JANELA`, `DATA_NO_PASSADO`, `ESTUDO_FORA_...`, `PARTICIPANTE_RETIRADO`, `VISITA_NAO_ESTA_AGENDADA`).
- `DesfechoVisitaService.registrar` bloqueia a visita e é o único caminho de desfecho; `RetiradaService` cancela visitas direto.
- O encerramento do estudo passa por `EstudoTransicaoService`.
- O dono da T81 é I3: combine a alteração com ele.

## 3. Onde implementar

| Arquivo | Ação | Responsabilidade |
|---|---|---|
| `backend/ClinicalStudyPB/src/main/java/UEPB/ESWII/ClinicalStudyPB/service/AtendimentoSolicitacaoService.java` | Criar | `atender` e `recusar` |
| `backend/ClinicalStudyPB/src/main/java/UEPB/ESWII/ClinicalStudyPB/service/FechamentoDeSolicitacoes.java` | Criar | Recusas automáticas |
| `backend/ClinicalStudyPB/src/main/java/UEPB/ESWII/ClinicalStudyPB/service/AgendamentoVisitaService.java` | Alterar | `reagendarVisita` limpa a confirmação |
| `backend/ClinicalStudyPB/src/main/java/UEPB/ESWII/ClinicalStudyPB/service/DesfechoVisitaService.java` | Alterar | Recusar pendente da visita |
| `backend/ClinicalStudyPB/src/main/java/UEPB/ESWII/ClinicalStudyPB/service/EstudoTransicaoService.java` | Alterar | Recusar pendentes ao encerrar |
| `backend/ClinicalStudyPB/src/test/java/UEPB/ESWII/ClinicalStudyPB/AtendimentoSolicitacaoServiceTest.java` | Criar | Regra 3 |
| `backend/ClinicalStudyPB/src/main/java/UEPB/ESWII/ClinicalStudyPB/repository/SolicitacaoParticipanteRepository.java` | Alterar | Projeção dos identificadores e busca de pendentes com bloqueio |
| `backend/ClinicalStudyPB/src/test/java/UEPB/ESWII/ClinicalStudyPB/DesfechoVisitaServiceTest.java` | Alterar | Novo colaborador |
| `backend/ClinicalStudyPB/src/test/java/UEPB/ESWII/ClinicalStudyPB/EstudoTransicaoServiceTest.java` | Alterar | Novo colaborador no construtor |
| `backend/ClinicalStudyPB/src/test/java/UEPB/ESWII/ClinicalStudyPB/EstudoTransicaoCombinacoesServiceTest.java` | Alterar | Novo colaborador no construtor |

## 4. Requisitos a implementar

Todos os requisitos (`RQ-T130-NN`) precisam estar atendidos para a task ser concluída.

### Atendimento
- **RQ-T130-01** `atender(solicitacaoId, dataHora, usuarioId)` em **uma** transação: obter por projeção só `participacaoId`, `visitaAgendadaId` e `tipo` do pedido (404), **sem carregar entidades**; bloquear participação → visita → pedido; só então conferir `PENDENTE`, reagendar por `AgendamentoService.reagendar` e marcar `ATENDIDA` com quem respondeu e quando. O `tipo` não muda e pode ser conferido pela projeção.
- **RQ-T130-02** Pedido de retirada: `TIPO_DE_SOLICITACAO_INCOMPATIVEL`. Pedido já respondido: `SOLICITACAO_JA_RESPONDIDA`.
- **RQ-T130-03** Se o reagendamento lança erro, nada é gravado: o pedido continua `PENDENTE` e a visita, inalterada.
- **RQ-T130-04** Toda mudança de data limpa `presencaConfirmadaEm` (dentro de `reagendarVisita`, o que cobre também o reagendamento direto; ajuste 5).
- **RQ-T130-05** Reagendamento direto do RF12 **não** responde o pedido pendente.
### Recusa
- **RQ-T130-06** `recusar` usa a mesma projeção e a mesma ordem de bloqueio de `atender`.
- **RQ-T130-07** `recusar(solicitacaoId, resposta, usuarioId)`: 404 → retirada dá `RETIRADA_NAO_PODE_SER_RECUSADA` → `SOLICITACAO_JA_RESPONDIDA` → `RESPOSTA_OBRIGATORIA` (menos de 5 caracteres após `trim`). A visita não muda.
### Fechamento automático
- **RQ-T130-08** `FechamentoDeSolicitacoes.recusarPendentesDaVisita(visita, usuario, agora)` recusa o pedido de reagendamento pendente com `RESPOSTA_DESFECHO`; `DesfechoVisitaService` o chama na mesma transação, para realizada, ausente e cancelada.
- **RQ-T130-09** `recusarReagendamentosDaParticipacao(participacao, usuario, agora)` usa `RESPOSTA_RETIRADA` (ligado ao RF16 na T138).
- **RQ-T130-10** `recusarReagendamentosDoEstudo(estudo, usuario, agora)` usa `RESPOSTA_ESTUDO_ENCERRADO`, chamado na transição para `ENCERRADO` (ajuste 7).
- **RQ-T130-11** Os métodos de fechamento exigem transação existente (`Propagation.MANDATORY`).
- **RQ-T130-12** Cada método de fechamento busca as solicitações com `@Lock(PESSIMISTIC_WRITE)` e filtro `situacao = PENDENTE`, para nunca responder um pedido já respondido por outra transação.
- **RQ-T130-13** Combinar com I3, dono da T81, a alteração do `DesfechoVisitaService` e registrar a revisão no Pull Request.

## 5. Contrato, regras e códigos de erro

Regra 3; códigos, respostas padrão e ordem de bloqueio na seção 4.

## 6. Testes

Fora da janela, no passado, pedido já respondido, recusa sem resposta, desfecho de cada tipo, encerramento do estudo e reagendamento direto limpando a confirmação. A transação real é testada na T134.

## 7. Critérios de aceite

- [ ] Atender reagenda, limpa a confirmação e fecha o pedido, tudo ou nada.
- [ ] Desfecho, cancelamento e encerramento não deixam reagendamento pendente.
- [ ] Testes do RF12 e do RF13 continuam verdes.

## 8. Cuidados e fora de escopo

- Não duplicar as regras de janela: chamar o serviço da T75.
- Não capturar a exceção do reagendamento dentro da transação.

## 9. Origem dos requisitos (especificação e planejamento)

Texto **sem paráfrase** da especificação e do planejamento. Onde ele diverge dos [ajustes da seção 1 das convenções](00-convencoes-e-contrato-compartilhado.md#1-ajustes-em-relação-ao-planejamento-e-por-quê), valem as convenções e este documento.

### 9.1 Requisito da especificação

- **RF19** (Módulo E, Aplicativo do participante; interface Mobile): “Permitir confirmar presença prevista ou solicitar reagendamento de visita.”

### 9.2 Critério de aceite do PBI (planejamento)

> o coordenador vê os pedidos do estudo, atende um reagendamento escolhendo uma data válida ou o recusa com resposta, e o participante vê o resultado no app; as presenças confirmadas aparecem na agenda e no histórico; o pesquisador consulta tudo em modo somente leitura.

### 9.3 Regras de negócio do planejamento que afetam esta task

| # | Regra proposta | Tasks afetadas |
|---|---|---|
| 3 | Pedidos do participante têm a situação PENDENTE, ATENDIDA ou RECUSADA, e as duas últimas são finais. O coordenador atende o reagendamento escolhendo nova data dentro da janela da visita (regras do RF12), o que reagenda a visita, limpa a confirmação de presença e marca o pedido como ATENDIDA na mesma transação; ou recusa com resposta obrigatória. Se a visita receber desfecho ou for cancelada antes, o pedido pendente é recusado automaticamente com resposta padrão | T129, T130, T132 |
| 8 | Erros seguem o formato único da Sprint 2 (400, 403, 404 e 409, sempre com o código da regra). As migrations usam as faixas V4_26_\* a V4_32_\*, todas acima das faixas V3 da Sprint 3 | T122 e todas as de backend |

### 9.4 Texto original da task no planejamento

> **T130 · Regras de atendimento e recusa do reagendamento.** Primeiro escreva o atendimento em uma única transação: reagende a visita pelo serviço da T75 (mesma janela e mesmas regras do RF12), limpe a confirmação de presença e marque o pedido como ATENDIDA com quem respondeu e quando. Depois implemente a recusa, com resposta obrigatória de ao menos 5 caracteres. Em seguida bloqueie pedidos já respondidos (409). Por fim, ajuste o serviço de desfecho da T81, em acordo com I3, para recusar automaticamente o pedido pendente quando a visita receber desfecho ou for cancelada.

### 9.5 Requisitos que atravessam esta task

- Regra 9.2: “O backend deve disponibilizar uma **API HTTP REST** compartilhada pela web e pelo mobile, documentada no padrão **OpenAPI**”.
- Entregas: “Persistência dos dados, com scripts de criação/migração do banco e dados fictícios para demonstração” e “Tratamento de erros e validações das regras de negócio”.
- Testes (seção 7): “Testes unitários para todas as regras de negócio relevantes”, “Testes de integração para os principais endpoints” e “Execução automatizada dos testes a cada pull request”.
- Regra 9.3: “A interface **web** atende aos perfis de pesquisador, coordenador e administrador.”
- **Definition of Done** (texto completo na [seção 11 das convenções](00-convencoes-e-contrato-compartilhado.md#11-definition-of-done)): critérios de aceite demonstráveis com dados fictícios; revisão, CI e Pull Request; testes automatizados e 80% de cobertura do negócio no backend; erros com mensagens claras; Swagger e YAML atualizados; dados de demonstração, documentação, capturas e nota de bastão no repositório; RF01 a RF18 continuam funcionando.

---
Parte dos documentos de design da Sprint 4: [índice](README.md) · [convenções e contrato](00-convencoes-e-contrato-compartilhado.md).
