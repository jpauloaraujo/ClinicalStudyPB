# T98 · Testes de backend e de componente do histórico

> **Sprint 3** · PBI-22 · RF15 Histórico do participante e acompanhamento da Sprint  
> **Responsável:** I2 · **Estimativa:** 2h · **Prazo:** Ter 17/11  
> **Depende de:** T96, T97  
> **Situação:** ⬜ A implementar (este documento é o guia para quem for fazer)

> Antes de começar, leia as [convenções e o contrato compartilhado da Sprint 3](00-convencoes-e-contrato-compartilhado.md): nomes, endpoints, DTOs, códigos de erro, tabelas e dados de demonstração valem para esta task.

## 1. Objetivo

Provar a consulta agregada, a ordenação, o resumo e as permissões do histórico, e os componentes da ficha, mantendo a cobertura da fatia acima de 80%.

## 2. Contexto: o que já existe e deve ser reaproveitado

- Modelos: `AvaliacaoEndpointTest`, `ParticipacaoEndpointTest` (MockMvc com tokens reais) e os testes de componente da Sprint 2 (`SecaoInclusao.test.tsx`).
- `CenarioRecrutamento` e os métodos das T78, T85 e T92 montam participante, agendamento, desfecho e resultado.

## 3. Onde implementar

| Arquivo | Ação | Responsabilidade |
|---|---|---|
| `backend/ClinicalStudyPB/src/test/java/UEPB/ESWII/ClinicalStudyPB/HistoricoParticipanteServiceTest.java` | Criar | Resumo e montagem (Mockito) |
| `backend/ClinicalStudyPB/src/test/java/UEPB/ESWII/ClinicalStudyPB/HistoricoParticipanteEndpointTest.java` | Criar | 200, 403, 404 e 401 com tokens reais |
| `web/ClinicalStudyPB/src/pages/estudos/detalhe/acompanhamento/FichaParticipantePage.test.tsx` | Alterar | Complementa os da T97 |

## 4. Requisitos a implementar

Cada requisito tem um identificador (`RQ-T98-NN`) para a revisão do Pull Request e para a conferência no fim da task: todos precisam estar atendidos.

### Backend

- **RQ-T98-01** Participante **sem visitas agendadas**: todas as visitas previstas aparecem como `NAO_AGENDADA`, `naoAgendadas = previstas`, `pendentes = previstas` e os demais contadores zerados.
- **RQ-T98-02** Participante com visitas **em todas as situações** (`AGENDADA`, `REALIZADA`, `AUSENTE`, `CANCELADA` e uma não agendada): cada situação aparece certa, com datas, motivo e resultados, e o **invariante** `previstas = realizadas + ausentes + canceladas + agendadas + naoAgendadas` vale.
- **RQ-T98-03** **Ordenação** pelo dia previsto (criar as visitas previstas fora de ordem e conferir a ordem na resposta); resultados ordenados pelo nome da atividade; visita realizada sem resultado devolve lista vazia.
- **RQ-T98-04** Estudo sem visitas previstas: lista vazia e resumo zerado (200, não 404).
- **RQ-T98-05** Permissões: **200** para pesquisador e coordenador; **403** para administrador e participante; **401** sem token; **404** para participação inexistente.
- **RQ-T98-06** O histórico é somente leitura: `POST`, `PUT` e `DELETE` em `/api/participacoes/{id}/historico` respondem 405 (ou 403 pelo perfil), sem alterar dados.
- **RQ-T98-07** A consulta usa um número fixo de consultas ao banco, **independente do número de visitas** (teste com 1 e com 10 visitas previstas, contando as consultas, por exemplo com as estatísticas do Hibernate).
- **RQ-T98-08** Rodar também no job `Backend (PostgreSQL)`.

### Componente e cobertura

- **RQ-T98-09** Completar os testes de `FichaParticipantePage` da T97 com os casos de ordem e de resumo vindos de um corpo completo da API.
- **RQ-T98-10** Conferir a cobertura de `HistoricoParticipanteService` (linhas e ramos) e registrar o resultado no Pull Request; meta 95% ou mais; toda a suíte anterior continua passando.

## 6. Testes

- As classes da seção 3 e o `ContratoOpenApiSprint3Test` verde (10 operações).

## 7. Critérios de aceite

- [ ] A consulta está provada para participante sem visitas e com visitas em todas as situações.
- [ ] A ordenação, o resumo e as permissões (200, 403, 404) têm teste.
- [ ] A cobertura da fatia está acima de 80%.

## 8. Cuidados e fora de escopo

- O invariante do resumo é o teste mais importante desta task.
- Não depender da data do dia.

---
Parte do conjunto de documentos de design da Sprint 3: [índice](README.md) · [convenções e contrato](00-convencoes-e-contrato-compartilhado.md).
