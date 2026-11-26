# T133 · Presença confirmada na agenda e no histórico

> **Sprint 4** · PBI-27 · RF19 Atendimento dos pedidos de reagendamento na web  
> **Responsável:** I4 · **Estimativa:** 2h · **Prazo:** Sex 27/11  
> **Depende de:** T125, T131  
> **Situação:** ⬜ A implementar (este documento é o guia para quem for fazer)

> Antes de começar, leia as [convenções e o contrato compartilhado da Sprint 4](00-convencoes-e-contrato-compartilhado.md): ajustes ao planejamento, endpoints, DTOs, códigos de erro, tabelas e dados de demonstração valem para esta task. A origem dos requisitos está na [seção 9](#9-origem-dos-requisitos-especificação-e-planejamento).

## 1. Objetivo

Mostrar a confirmação de presença na agenda e no histórico, os pedidos na linha do tempo e um filtro de visitas confirmadas.

## 2. Contexto: o que já existe e deve ser reaproveitado

- `AcoesDaVisita` é usado na agenda e na tela da visita; `LinhaDoTempoVisitas` mostra cada visita do histórico.
- `AgendaItemResponse`, `VisitaAgendadaResponse` e `HistoricoParticipanteResponse` são do backend; `HistoricoParticipanteService` tem limite de consultas testado.

## 3. Onde implementar

| Arquivo | Ação | Responsabilidade |
|---|---|---|
| `backend/ClinicalStudyPB/src/main/java/UEPB/ESWII/ClinicalStudyPB/controller/dto/AgendaItemResponse.java` | Alterar | `presencaConfirmadaEm` |
| `backend/ClinicalStudyPB/src/main/java/UEPB/ESWII/ClinicalStudyPB/controller/dto/VisitaAgendadaResponse.java` | Alterar | Idem |
| `backend/ClinicalStudyPB/src/main/java/UEPB/ESWII/ClinicalStudyPB/controller/dto/HistoricoParticipanteResponse.java` | Alterar | Confirmação e `solicitacoes` |
| `backend/ClinicalStudyPB/src/main/java/UEPB/ESWII/ClinicalStudyPB/service/HistoricoParticipanteService.java` | Alterar | Uma consulta a mais |
| `backend/ClinicalStudyPB/src/main/java/UEPB/ESWII/ClinicalStudyPB/controller/AgendaController.java` | Alterar | Parâmetro `confirmada` |
| `backend/ClinicalStudyPB/src/main/java/UEPB/ESWII/ClinicalStudyPB/service/AgendaService.java` | Alterar | Filtro |
| `backend/ClinicalStudyPB/src/main/java/UEPB/ESWII/ClinicalStudyPB/repository/VisitaAgendadaRepository.java` | Alterar | Condição na busca |
| `docs/api/openapi-sprint3.yaml` | Alterar | Campos e parâmetro |
| `web/ClinicalStudyPB/src/pages/estudos/detalhe/acompanhamento/AcoesDaVisita.tsx` | Alterar | Selo |
| `web/ClinicalStudyPB/src/pages/estudos/detalhe/acompanhamento/AbaAgenda.tsx` | Alterar | Filtro |
| `web/ClinicalStudyPB/src/pages/estudos/detalhe/acompanhamento/VisitaAgendadaPage.tsx` | Alterar | Passar a confirmação às ações da visita |
| `web/ClinicalStudyPB/src/pages/estudos/detalhe/acompanhamento/LinhaDoTempoVisitas.tsx` | Alterar | Confirmação e pedidos |

## 4. Requisitos a implementar

Todos os requisitos (`RQ-T133-NN`) precisam estar atendidos para a task ser concluída.

- **RQ-T133-01** Selo "Presença confirmada" (com data e hora no texto acessível) na agenda e na tela da visita, vindo do componente de ações, inclusive para o pesquisador (o selo não depende de haver botão).
- **RQ-T133-02** O histórico mostra, em cada visita expandida, a confirmação e os pedidos (tipo, situação, motivo, resposta e datas) em ordem cronológica.
- **RQ-T133-03** O serviço do histórico carrega os pedidos em **uma** consulta adicional (máximo passa de 3 para 4); atualizar o teste de contagem da T98 com a justificativa.
- **RQ-T133-04** `GET /api/estudos/{estudoId}/agenda` aceita `confirmada` (`true`/`false`, opcional), combinável com `de`, `ate` e `situacao`.
- **RQ-T133-05** Filtro "Somente confirmadas" na agenda, guardado na URL, voltando para a página 0.
- **RQ-T133-06** Atualizar o YAML da Sprint 3 e os tipos.
- **RQ-T133-07** O filtro é item de corte (último da ordem de corte): o selo e o histórico não podem ser cortados.

## 5. Contrato, regras e códigos de erro

Seção 3.2.

## 6. Testes

Backend: filtro, campos novos e contagem de consultas. Web: selo para os dois perfis, filtro e linha do tempo.

## 7. Critérios de aceite

- [ ] A confirmação aparece na agenda, na visita e no histórico.
- [ ] Depois de um reagendamento, o selo some.

## 8. Cuidados e fora de escopo

- Campos novos são opcionais na resposta: ajustar as fixtures de teste (`agendamentoFixture`, `historicoFixture`).

## 9. Origem dos requisitos (especificação e planejamento)

Texto **sem paráfrase** da especificação e do planejamento. Onde ele diverge dos [ajustes da seção 1 das convenções](00-convencoes-e-contrato-compartilhado.md#1-ajustes-em-relação-ao-planejamento-e-por-quê), valem as convenções e este documento.

### 9.1 Requisito da especificação

- **RF19** (Módulo E, Aplicativo do participante; interface Mobile): “Permitir confirmar presença prevista ou solicitar reagendamento de visita.”

### 9.2 Critério de aceite do PBI (planejamento)

> o coordenador vê os pedidos do estudo, atende um reagendamento escolhendo uma data válida ou o recusa com resposta, e o participante vê o resultado no app; as presenças confirmadas aparecem na agenda e no histórico; o pesquisador consulta tudo em modo somente leitura.

### 9.3 Regras de negócio do planejamento que afetam esta task

Nenhuma das nove regras cita esta task diretamente.

### 9.4 Texto original da task no planejamento

> **T133 · Presença confirmada na agenda e no histórico.** Primeiro acrescente o selo "Presença confirmada" na agenda do estudo, pelo componente de ações da visita da T77. Depois mostre a confirmação e os pedidos de cada visita na linha do tempo do histórico (T97). Por fim, acrescente à agenda um filtro de visitas confirmadas.

### 9.5 Requisitos que atravessam esta task

- Regra 9.2: “O backend deve disponibilizar uma **API HTTP REST** compartilhada pela web e pelo mobile, documentada no padrão **OpenAPI**”.
- Entregas: “Persistência dos dados, com scripts de criação/migração do banco e dados fictícios para demonstração” e “Tratamento de erros e validações das regras de negócio”.
- Testes (seção 7): “Testes unitários para todas as regras de negócio relevantes”, “Testes de integração para os principais endpoints” e “Execução automatizada dos testes a cada pull request”.
- Regra 9.3: “A interface **web** atende aos perfis de pesquisador, coordenador e administrador.”
- **Definition of Done** (texto completo na [seção 11 das convenções](00-convencoes-e-contrato-compartilhado.md#11-definition-of-done)): critérios de aceite demonstráveis com dados fictícios; revisão, CI e Pull Request; testes automatizados e 80% de cobertura do negócio no backend; erros com mensagens claras; Swagger e YAML atualizados; dados de demonstração, documentação, capturas e nota de bastão no repositório; RF01 a RF18 continuam funcionando.

---
Parte dos documentos de design da Sprint 4: [índice](README.md) · [convenções e contrato](00-convencoes-e-contrato-compartilhado.md).
