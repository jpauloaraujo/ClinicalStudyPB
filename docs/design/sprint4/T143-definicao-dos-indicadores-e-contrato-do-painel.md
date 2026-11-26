# T143 · Definição dos indicadores e contrato do painel

> **Sprint 4** · PBI-29 · RF21 Painel do estudo  
> **Responsável:** I6 · **Estimativa:** 2h · **Prazo:** Seg 30/11  
> **Depende de:** T142  
> **Situação:** ⬜ A implementar (este documento é o guia para quem for fazer)

> Antes de começar, leia as [convenções e o contrato compartilhado da Sprint 4](00-convencoes-e-contrato-compartilhado.md): ajustes ao planejamento, endpoints, DTOs, códigos de erro, tabelas e dados de demonstração valem para esta task. A origem dos requisitos está na [seção 9](#9-origem-dos-requisitos-especificação-e-planejamento).

## 1. Objetivo

Fixar a definição exata de cada indicador do painel e publicar o contrato.

## 2. Contexto: o que já existe e deve ser reaproveitado

- `SituacaoCandidato` (6 valores), `SituacaoParticipacao`, `SituacaoVisita`, `VagasResponse` (a vaga do retirado continua ocupada).
- Próximas visitas: `dataHoraAgendada >= agora`.

## 3. Onde implementar

| Arquivo | Ação | Responsabilidade |
|---|---|---|
| `docs/sprint4/indicadores-do-painel.md` | Criar | Definições aprovadas pelo PO |
| `docs/api/openapi-sprint4.yaml` | Alterar | Operações 10 e 11 |
| `backend/ClinicalStudyPB/src/main/java/UEPB/ESWII/ClinicalStudyPB/controller/dto/PainelEstudoResponse.java` | Criar | DTO com records aninhados |
| `backend/ClinicalStudyPB/src/main/java/UEPB/ESWII/ClinicalStudyPB/controller/dto/ResumoDoEstudoResponse.java` | Criar | Item da visão geral |
| `backend/ClinicalStudyPB/src/test/java/UEPB/ESWII/ClinicalStudyPB/ContratoOpenApiSprint4Test.java` | Alterar | 11 operações |

## 4. Requisitos a implementar

Todos os requisitos (`RQ-T143-NN`) precisam estar atendidos para a task ser concluída.

- **RQ-T143-01** Candidatos: total e contagem por cada uma das seis situações (ajuste 9); a soma das seis é o total.
- **RQ-T143-02** Participantes: `ativos`, `retirados`, `limite` e `vagasRestantes = max(0, limite - (ativos + retirados))`.
- **RQ-T143-03** Visitas: `realizadas`, `ausentes` e `canceladas` contam **todas** as participações (histórico); `pendentes` = `AGENDADA` com data e hora maior ou igual a agora, de participantes ativos; `atrasadas` = `AGENDADA` com data e hora menor que agora, de ativos; `naoAgendadas` = para cada participante ativo, visitas previstas do estudo sem agendamento.
- **RQ-T143-04** `presencasConfirmadasProximos7Dias`: visitas pendentes, confirmadas, com data entre agora e o fim do 7º dia.
- **RQ-T143-05** Pedidos: reagendamentos e retiradas `PENDENTE` do estudo.
- **RQ-T143-06** Resumo dos estudos: `incluidos` (participações), `limite`, `visitasAtrasadas`, `pedidosPendentes`; todos os estudos que o perfil lê.
- **RQ-T143-07** Escrever o documento de indicadores com a fórmula, um exemplo com os dados de demonstração e a lista a que cada número leva.
- **RQ-T143-08** Confirmar as definições com o PO **antes** da T144 e registrar a resposta no documento.
- **RQ-T143-09** Contrato das operações 10 e 11 (200, 401, 403, 404 na do estudo); atualizar a contagem, com `@Disabled("T145")`, e gerar tipos.

## 5. Contrato, regras e códigos de erro

Operações 10 e 11 e DTOs da seção 3.1; regra 6.

## 6. Testes

Teste de contrato dos schemas.

## 7. Critérios de aceite

- [ ] Cada indicador tem definição única, aprovada e escrita.

## 8. Cuidados e fora de escopo

- "Agora" vem do `Clock` do centro, igual nas três contagens.

## 9. Origem dos requisitos (especificação e planejamento)

Texto **sem paráfrase** da especificação e do planejamento. Onde ele diverge dos [ajustes da seção 1 das convenções](00-convencoes-e-contrato-compartilhado.md#1-ajustes-em-relação-ao-planejamento-e-por-quê), valem as convenções e este documento.

### 9.1 Requisito da especificação

- **RF21** (Módulo F, Monitoramento; interface Web): “Exibir painel com quantidade de candidatos, participantes incluídos e visitas realizadas ou pendentes.”

### 9.2 Critério de aceite do PBI (planejamento)

> pesquisador e coordenador abrem o painel de um estudo e veem a quantidade de candidatos por situação, participantes incluídos (ativos, retirados e vagas) e visitas realizadas, pendentes, atrasadas e não agendadas, com os números conferindo com os dados de demonstração; cada indicador leva à lista correspondente.

### 9.3 Regras de negócio do planejamento que afetam esta task

| # | Regra proposta | Tasks afetadas |
|---|---|---|
| 6 | O painel (RF21) usa estas definições: visitas realizadas = REALIZADA; pendentes = AGENDADA com data futura; atrasadas = AGENDADA com data passada e sem desfecho; não agendadas = visitas previstas de participantes ativos sem agendamento. Participantes retirados não entram nas pendências. Pesquisador e coordenador veem o painel dos estudos que já podem ler | T143, T144, T146 |

### 9.4 Texto original da task no planejamento

> **T143 · Definição dos indicadores e contrato do painel.** Primeiro escreva a lista de indicadores com a definição exata de cada um, a partir da regra 6: candidatos por situação (cadastrado, elegível, não elegível, incluído); participantes ativos, retirados, limite e vagas restantes; visitas realizadas, pendentes, atrasadas, não agendadas, ausentes e canceladas; presenças confirmadas nos próximos 7 dias; e pedidos pendentes. Depois descreva no contrato o painel de um estudo e o resumo de todos os estudos que o usuário pode ler. Por fim, confirme as definições com o PO antes de escrever as consultas.

### 9.5 Requisitos que atravessam esta task

- Regra 9.2: “O backend deve disponibilizar uma **API HTTP REST** compartilhada pela web e pelo mobile, documentada no padrão **OpenAPI**”.
- Entregas: “Persistência dos dados, com scripts de criação/migração do banco e dados fictícios para demonstração” e “Tratamento de erros e validações das regras de negócio”.
- Testes (seção 7): “Testes unitários para todas as regras de negócio relevantes”, “Testes de integração para os principais endpoints” e “Execução automatizada dos testes a cada pull request”.
- Regra 9.3: “A interface **web** atende aos perfis de pesquisador, coordenador e administrador.”
- **Definition of Done** (texto completo na [seção 11 das convenções](00-convencoes-e-contrato-compartilhado.md#11-definition-of-done)): critérios de aceite demonstráveis com dados fictícios; revisão, CI e Pull Request; testes automatizados e 80% de cobertura do negócio no backend; erros com mensagens claras; Swagger e YAML atualizados; dados de demonstração, documentação, capturas e nota de bastão no repositório; RF01 a RF18 continuam funcionando.

---
Parte dos documentos de design da Sprint 4: [índice](README.md) · [convenções e contrato](00-convencoes-e-contrato-compartilhado.md).
