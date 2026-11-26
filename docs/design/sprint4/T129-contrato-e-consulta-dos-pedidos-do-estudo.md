# T129 · Contrato e consulta dos pedidos do estudo

> **Sprint 4** · PBI-27 · RF19 Atendimento dos pedidos de reagendamento na web  
> **Responsável:** I4 · **Estimativa:** 3h · **Prazo:** Qui 26/11  
> **Depende de:** T128  
> **Situação:** ⬜ A implementar (este documento é o guia para quem for fazer)

> Antes de começar, leia as [convenções e o contrato compartilhado da Sprint 4](00-convencoes-e-contrato-compartilhado.md): ajustes ao planejamento, endpoints, DTOs, códigos de erro, tabelas e dados de demonstração valem para esta task. A origem dos requisitos está na [seção 9](#9-origem-dos-requisitos-especificação-e-planejamento).

## 1. Objetivo

Descrever no contrato a listagem, a contagem, o atendimento e a recusa dos pedidos de um estudo, e implementar a consulta sem N+1.

## 2. Contexto: o que já existe e deve ser reaproveitado

- `AgendaService`/`VisitaAgendadaRepository.buscarAgenda` são o modelo de paginação (`PaginaResponse`, `PARAMETRO_INVALIDO`, `join fetch` com `countQuery`).
- `JanelaDeAgendamento.de(visitaPrevista, dataInclusao)` calcula a janela válida.
- `SolicitacaoParticipante` e os dados do `R__demo_26` vêm do PBI-26.

## 3. Onde implementar

| Arquivo | Ação | Responsabilidade |
|---|---|---|
| `docs/api/openapi-sprint4.yaml` | Alterar | Operações 4 a 7 |
| `backend/ClinicalStudyPB/src/main/java/UEPB/ESWII/ClinicalStudyPB/repository/SolicitacaoParticipanteRepository.java` | Alterar | Busca paginada e contagem |
| `backend/ClinicalStudyPB/src/main/java/UEPB/ESWII/ClinicalStudyPB/service/SolicitacaoConsultaService.java` | Criar | Listagem e contagem |
| `backend/ClinicalStudyPB/src/main/java/UEPB/ESWII/ClinicalStudyPB/controller/dto/SolicitacaoResponse.java` | Criar | Item da lista |
| `backend/ClinicalStudyPB/src/main/java/UEPB/ESWII/ClinicalStudyPB/controller/dto/ContagemPendentesResponse.java` | Criar | Total por tipo |
| `backend/ClinicalStudyPB/src/main/java/UEPB/ESWII/ClinicalStudyPB/controller/dto/AtendimentoRequest.java` | Criar | `dataHora` |
| `backend/ClinicalStudyPB/src/main/java/UEPB/ESWII/ClinicalStudyPB/controller/dto/RecusaRequest.java` | Criar | `resposta` |
| `backend/ClinicalStudyPB/src/test/java/UEPB/ESWII/ClinicalStudyPB/ContratoOpenApiSprint4Test.java` | Alterar | 7 operações |
| `backend/ClinicalStudyPB/src/test/java/UEPB/ESWII/ClinicalStudyPB/SolicitacaoConsultaServiceTest.java` | Criar | Consulta e filtros |

## 4. Requisitos a implementar

Todos os requisitos (`RQ-T129-NN`) precisam estar atendidos para a task ser concluída.

- **RQ-T129-01** Descrever `GET /api/estudos/{estudoId}/solicitacoes` com `tipo`, `situacao`, `pagina` (padrão 0) e `tamanho` (1 a 100, padrão 20), e `GET .../solicitacoes/pendentes`.
- **RQ-T129-02** Descrever `POST /api/solicitacoes/{solicitacaoId}/atendimento` e `/recusa`, com os códigos da regra 3 e as respostas 200, 400, 401, 403, 404 e 409.
- **RQ-T129-03** `SolicitacaoResponse` traz participante (código e nome fictício), visita, data atual, janela válida, motivo, preferência, situação, resposta, quem respondeu e datas; nos pedidos de retirada, os campos de visita e janela são nulos.
- **RQ-T129-04** Ordenação: pendentes primeiro, depois por `criadoEm` crescente entre os pendentes e decrescente entre os respondidos; desempate por `id`.
- **RQ-T129-05** A consulta usa `join fetch` (participação, candidato, visita agendada com `left join`, visita prevista, respondido por) e `countQuery`; número de consultas independente da quantidade de pedidos.
- **RQ-T129-06** A contagem de pendentes usa `GROUP BY tipo` em uma consulta e devolve zero para tipo sem pedido.
- **RQ-T129-07** Estudo inexistente: 404. Paginação inválida: `PARAMETRO_INVALIDO`.
- **RQ-T129-08** Atualizar a contagem do teste de contrato (7), com as operações ainda sem controller em teste `@Disabled("T131")`, e gerar os tipos.

## 5. Contrato, regras e códigos de erro

Operações 4 a 7 (seção 2) e DTOs (seção 3.1).

## 6. Testes

Filtros combinados, paginação, ordenação, estudo sem pedidos, isolamento entre estudos e contagem de consultas.

## 7. Critérios de aceite

- [ ] O contrato descreve as quatro operações.
- [ ] A consulta devolve tudo o que a aba precisa, sem consulta por pedido.

## 8. Cuidados e fora de escopo

- Filtrar sempre pelo estudo do caminho.
- A listagem já inclui o tipo `RETIRADA`, mesmo antes do PBI-28.

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

> **T129 · Contrato e consulta dos pedidos do estudo.** Primeiro descreva no contrato a listagem dos pedidos de um estudo, com filtro por tipo e situação e paginação, e a contagem de pendentes. Depois descreva o atendimento (nova data e hora) e a recusa (resposta), com os códigos de erro da regra 3. Por fim, implemente a consulta, trazendo participante, visita, janela válida e datas, sem uma consulta por pedido.

### 9.5 Requisitos que atravessam esta task

- Regra 9.2: “O backend deve disponibilizar uma **API HTTP REST** compartilhada pela web e pelo mobile, documentada no padrão **OpenAPI**”.
- Entregas: “Persistência dos dados, com scripts de criação/migração do banco e dados fictícios para demonstração” e “Tratamento de erros e validações das regras de negócio”.
- Testes (seção 7): “Testes unitários para todas as regras de negócio relevantes”, “Testes de integração para os principais endpoints” e “Execução automatizada dos testes a cada pull request”.
- Regra 9.3: “A interface **web** atende aos perfis de pesquisador, coordenador e administrador.”
- **Definition of Done** (texto completo na [seção 11 das convenções](00-convencoes-e-contrato-compartilhado.md#11-definition-of-done)): critérios de aceite demonstráveis com dados fictícios; revisão, CI e Pull Request; testes automatizados e 80% de cobertura do negócio no backend; erros com mensagens claras; Swagger e YAML atualizados; dados de demonstração, documentação, capturas e nota de bastão no repositório; RF01 a RF18 continuam funcionando.

---
Parte dos documentos de design da Sprint 4: [índice](README.md) · [convenções e contrato](00-convencoes-e-contrato-compartilhado.md).
