# T124 · Regras de confirmação e de pedido de reagendamento

> **Sprint 4** · PBI-26 · RF19 Confirmação de presença e pedido de reagendamento no app  
> **Responsável:** I7 · **Estimativa:** 4h · **Prazo:** Ter 24/11  
> **Depende de:** T122  
> **Situação:** ⬜ A implementar (este documento é o guia para quem for fazer)

> Antes de começar, leia as [convenções e o contrato compartilhado da Sprint 4](00-convencoes-e-contrato-compartilhado.md): ajustes ao planejamento, endpoints, DTOs, códigos de erro, tabelas e dados de demonstração valem para esta task. A origem dos requisitos está na [seção 9](#9-origem-dos-requisitos-especificação-e-planejamento).

## 1. Objetivo

Implementar as regras de confirmação de presença e de pedido de reagendamento em um serviço de domínio sem dependência de Spring Web.

## 2. Contexto: o que já existe e deve ser reaproveitado

- `AcessoParticipante.participacoesDe(usuarioId)` resolve as participações da conta; o 404 esconde dados de outro participante.
- `MinhasVisitasService` considera próxima a visita `AGENDADA` com `dataHoraAgendada >= agora` (`Clock`).
- `AgendamentoVisitaService` e `DesfechoVisitaService` mostram o padrão de códigos como constantes e `ConflitoRegraException`/`ValidacaoRegraException`.

## 3. Onde implementar

| Arquivo | Ação | Responsabilidade |
|---|---|---|
| `backend/ClinicalStudyPB/src/main/java/UEPB/ESWII/ClinicalStudyPB/domain/regra/RegraSolicitacao.java` | Criar | Verificações puras, na ordem da seção 4 |
| `backend/ClinicalStudyPB/src/main/java/UEPB/ESWII/ClinicalStudyPB/service/SolicitacaoParticipanteService.java` | Criar | Confirmar, pedir reagendamento, listar pedidos |
| `backend/ClinicalStudyPB/src/main/java/UEPB/ESWII/ClinicalStudyPB/service/AcessoParticipante.java` | Alterar | `idsDaVisita(usuarioId, visitaAgendadaId)`, sem carregar entidades |
| `backend/ClinicalStudyPB/src/main/java/UEPB/ESWII/ClinicalStudyPB/repository/VisitaAgendadaRepository.java` | Alterar | Projeção dos identificadores da visita da conta |
| `backend/ClinicalStudyPB/src/test/java/UEPB/ESWII/ClinicalStudyPB/RegraSolicitacaoTest.java` | Criar | Matriz das regras 1 e 2 |
| `backend/ClinicalStudyPB/src/test/java/UEPB/ESWII/ClinicalStudyPB/SolicitacaoParticipanteServiceTest.java` | Criar | Orquestração com Mockito |

## 4. Requisitos a implementar

Todos os requisitos (`RQ-T124-NN`) precisam estar atendidos para a task ser concluída.

### Regra pura
- **RQ-T124-01** `RegraSolicitacao.validarConfirmacao(visita, estudoStatus, participacaoRetirada, retiradaPendente, agora)` lança, nesta ordem: `VISITA_NAO_ESTA_AGENDADA`, `VISITA_JA_OCORREU` (quando `dataHoraAgendada` não é depois de `agora`), `ESTUDO_FORA_DE_RECRUTAMENTO_OU_EXECUCAO`, `PARTICIPANTE_RETIRADO`, `RETIRADA_PENDENTE`, `PRESENCA_JA_CONFIRMADA`.
- **RQ-T124-02** `validarPedidoReagendamento(..., haPedidoPendente, motivo, dataPreferida, hoje)` repete as cinco primeiras e depois: `PEDIDO_REAGENDAMENTO_PENDENTE`, `MOTIVO_OBRIGATORIO` (menos de 5 caracteres após `trim`), `DATA_PREFERIDA_NO_PASSADO`.
- **RQ-T124-03** Expor `podeConfirmar(...)` e `podePedirReagendamento(...)` (booleanos, sem exceção) usando exatamente as mesmas condições, para os DTOs da T125.
- **RQ-T124-04** Mensagens em português que dizem a regra violada (ex.: "A presença nesta visita já foi confirmada.").
### Serviço
- **RQ-T124-05** Antes de bloquear, obter **só os identificadores** (`participacaoId` e `visitaAgendadaId`) com `AcessoParticipante.idsDaVisita(usuarioId, visitaAgendadaId)`, uma consulta de projeção filtrada pela conta (`candidato.usuario.id`); sem resultado, `RecursoNaoEncontradoException`. **Não carregar `Participacao` nem `VisitaAgendada` antes do bloqueio** (por isso as escritas não usam `participacoesDe`): o Hibernate devolveria a cópia já carregada, com dado antigo.
- **RQ-T124-06** `confirmarPresenca(usuarioId, visitaAgendadaId)`: com os ids, bloquear **participação e depois visita** (`buscarComBloqueio` dos dois repositórios); só então consultar a retirada pendente, validar e gravar `presencaConfirmadaEm = agora`, `alteradoPor` (usuário participante) e `alteradoEm`.
- **RQ-T124-07** `pedirReagendamento(usuarioId, visitaAgendadaId, motivo, dataPreferida)`: mesmos passos; criar `SolicitacaoParticipante` `PENDENTE` com motivo aparado. A visita **não muda** (regra 2).
- **RQ-T124-08** `listar(usuarioId)`: solicitações de todas as participações da conta, mais recentes primeiro; conta sem participação devolve lista vazia.
- **RQ-T124-09** `retiradaPendente` vem de `existsByParticipacaoIdAndTipoAndSituacao(..., RETIRADA, PENDENTE)` desde já; a T136 passa a criar esses pedidos.
- **RQ-T124-10** Sem `@PreAuthorize`, `Jwt` ou tipos web no serviço; métodos de escrita `@Transactional`.

## 5. Contrato, regras e códigos de erro

Códigos e ordem de verificação na seção 4 das convenções; ordem de bloqueio na subseção "Concorrência".

## 6. Testes

- Matriz com `Clock` fixo: visita passada, no exato minuto, já confirmada, com cada desfecho, de outro participante, participante retirado, retirada pendente, estudo encerrado, pedido duplicado, motivo com 4 e 5 caracteres, preferência ontem, hoje e nula.
- Verificar que nenhuma falha grava dados.
- Teste de integração: a segunda de duas confirmações simultâneas recebe `PRESENCA_JA_CONFIRMADA`, não `VISITA_ALTERADA_CONCORRENTEMENTE` (prova de que o estado é lido depois do bloqueio).

## 7. Critérios de aceite

- [ ] Cada regra violada tem código próprio e a ordem é a documentada.
- [ ] A confirmação é única e não muda a situação da visita.
- [ ] O pedido não altera data nem situação da visita.

## 8. Cuidados e fora de escopo

- Visita de outro participante é **404**, nunca 403.
- Usar sempre o `Clock` injetado, nunca `LocalDateTime.now()` sem ele.

## 9. Origem dos requisitos (especificação e planejamento)

Texto **sem paráfrase** da especificação e do planejamento. Onde ele diverge dos [ajustes da seção 1 das convenções](00-convencoes-e-contrato-compartilhado.md#1-ajustes-em-relação-ao-planejamento-e-por-quê), valem as convenções e este documento.

### 9.1 Requisito da especificação

- **RF19** (Módulo E, Aplicativo do participante; interface Mobile): “Permitir confirmar presença prevista ou solicitar reagendamento de visita.”

### 9.2 Critério de aceite do PBI (planejamento)

> no detalhe de uma visita agendada, o participante confirma presença ou pede reagendamento com motivo; o app mostra a situação de cada pedido (pendente, atendido ou recusado, com a resposta); pedidos inválidos são recusados com a regra violada. Este PBI cria a tabela de pedidos do participante que os PBIs 27 e 28 usam.

### 9.3 Regras de negócio do planejamento que afetam esta task

| # | Regra proposta | Tasks afetadas |
|---|---|---|
| 1 | O participante confirma presença apenas em visita AGENDADA, com data futura, participação ativa e sem pedido de retirada pendente. A confirmação é única (uma segunda dá 409), guarda data e hora e não muda a situação da visita | T122, T124, T126 |
| 2 | O pedido de reagendamento vale para visita AGENDADA e futura, exige motivo (ao menos 5 caracteres) e aceita uma preferência de data opcional. Cada visita tem no máximo um pedido PENDENTE, e a visita só muda quando o coordenador atende | T122, T124, T126 |
| 8 | Erros seguem o formato único da Sprint 2 (400, 403, 404 e 409, sempre com o código da regra). As migrations usam as faixas V4_26_\* a V4_32_\*, todas acima das faixas V3 da Sprint 3 | T122 e todas as de backend |

### 9.4 Texto original da task no planejamento

> **T124 · Regras de confirmação e de pedido de reagendamento.** Primeiro escreva o serviço de domínio sem depender de Spring Web. Depois implemente a confirmação, verificando nesta ordem: visita do próprio participante, AGENDADA, data futura, participação ativa, sem pedido de retirada pendente e ainda não confirmada. Em seguida implemente o pedido de reagendamento com as mesmas verificações, mais motivo de ao menos 5 caracteres e nenhum outro pedido pendente para a visita. Cada falha lança uma exceção com código próprio, que vira 409 (ou 404 para visita de outro participante, como na regra 7 da Sprint 3).

### 9.5 Requisitos que atravessam esta task

- Regra 9.2: “O backend deve disponibilizar uma **API HTTP REST** compartilhada pela web e pelo mobile, documentada no padrão **OpenAPI**”.
- Entregas: “Persistência dos dados, com scripts de criação/migração do banco e dados fictícios para demonstração” e “Tratamento de erros e validações das regras de negócio”.
- Testes (seção 7): “Testes unitários para todas as regras de negócio relevantes”, “Testes de integração para os principais endpoints” e “Execução automatizada dos testes a cada pull request”.
- Regra 9.4: “A interface **mobile** atende ao perfil de participante e executa em dispositivo ou emulador.”
- **Definition of Done** (texto completo na [seção 11 das convenções](00-convencoes-e-contrato-compartilhado.md#11-definition-of-done)): critérios de aceite demonstráveis com dados fictícios; revisão, CI e Pull Request; testes automatizados e 80% de cobertura do negócio no backend; erros com mensagens claras; Swagger e YAML atualizados; dados de demonstração, documentação, capturas e nota de bastão no repositório; RF01 a RF18 continuam funcionando.

---
Parte dos documentos de design da Sprint 4: [índice](README.md) · [convenções e contrato](00-convencoes-e-contrato-compartilhado.md).
