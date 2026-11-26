# T140 · Pedidos de retirada na aba Solicitações e na ficha

> **Sprint 4** · PBI-28 · RF20 Retirada voluntária pelo app  
> **Responsável:** I5 · **Estimativa:** 3h · **Prazo:** Dom 29/11  
> **Depende de:** T132, T138  
> **Situação:** ⬜ A implementar (este documento é o guia para quem for fazer)

> Antes de começar, leia as [convenções e o contrato compartilhado da Sprint 4](00-convencoes-e-contrato-compartilhado.md): ajustes ao planejamento, endpoints, DTOs, códigos de erro, tabelas e dados de demonstração valem para esta task. A origem dos requisitos está na [seção 9](#9-origem-dos-requisitos-especificação-e-planejamento).

## 1. Objetivo

Tratar o pedido de retirada na aba Solicitações e na ficha do participante, reutilizando o diálogo da T104.

## 2. Contexto: o que já existe e deve ser reaproveitado

- `DialogoRetirada` (`participante`, `visitas`, `aoFechar`, `aoRetirar`) chama `retiradaApi.registrar`.
- `AbaSolicitacoes` e `acoesDaSolicitacao` (T132); `AreaAcoesParticipante` e `FichaParticipantePage`.

## 3. Onde implementar

| Arquivo | Ação | Responsabilidade |
|---|---|---|
| `web/ClinicalStudyPB/src/pages/estudos/detalhe/acompanhamento/DialogoRetirada.tsx` | Alterar | Valores iniciais, solicitante fixo e envio injetável |
| `web/ClinicalStudyPB/src/pages/estudos/detalhe/solicitacoes/AbaSolicitacoes.tsx` | Alterar | Ação "Registrar retirada" |
| `web/ClinicalStudyPB/src/pages/estudos/detalhe/acompanhamento/FichaParticipantePage.tsx` | Alterar | Aviso |
| `web/ClinicalStudyPB/src/pages/estudos/detalhe/acompanhamento/AreaAcoesParticipante.tsx` | Alterar | Usar o pedido pendente |
| `web/ClinicalStudyPB/src/api/solicitacoes.ts` | Alterar | `efetivarRetirada` |
| `backend/ClinicalStudyPB/src/main/java/UEPB/ESWII/ClinicalStudyPB/controller/dto/HistoricoParticipanteResponse.java` | Alterar | `pedidoRetirada` |
| `backend/ClinicalStudyPB/src/main/java/UEPB/ESWII/ClinicalStudyPB/service/HistoricoParticipanteService.java` | Alterar | Preencher |
| `docs/api/openapi-sprint3.yaml` | Alterar | Campo novo |

## 4. Requisitos a implementar

Todos os requisitos (`RQ-T140-NN`) precisam estar atendidos para a task ser concluída.

- **RQ-T140-01** Na aba, o pedido de retirada pendente tem só a ação **Registrar retirada** (sem Recusar).
- **RQ-T140-02** O diálogo abre com solicitante "Participante" **fixo**, motivo preenchido conforme o ajuste 8 e data de hoje; o coordenador pode editar o motivo.
- **RQ-T140-03** O diálogo recebe as visitas do participante (consulta do histórico) para a confirmação mostrar quantas serão canceladas e avisar quantos pedidos de reagendamento serão recusados.
- **RQ-T140-04** O envio usa a operação 9 quando há pedido; sem pedido, o diálogo continua como antes (RF16).
- **RQ-T140-05** `pedidoRetirada` e a contagem de reagendamentos saem dos pedidos já carregados pela consulta do histórico (T133): nenhuma consulta a mais.
- **RQ-T140-06** A ficha mostra "Retirada solicitada pelo participante em dd/mm/aaaa" com o motivo, para os dois perfis; o botão da área de ações abre o diálogo preenchido.
- **RQ-T140-07** Estudo encerrado: ação desabilitada com explicação.
- **RQ-T140-08** Pesquisador sem ações.
- **RQ-T140-09** Após efetivar, recarregar lista, contagem e ficha.
- **RQ-T140-10** Filtro por tipo mostra "Retirada".
- **RQ-T140-11** Completar o campo `pedidoRetirada` na `historicoFixture` e nas demais fixtures, para tipos e testes da web continuarem verdes.

## 5. Contrato, regras e códigos de erro

Operação 9 e `pedidoRetirada` (seção 3.2).

## 6. Testes

Diálogo preenchido e envio pela rota certa; sem Recusar; aviso na ficha; testes atuais do diálogo verdes.

## 7. Critérios de aceite

- [ ] O coordenador efetiva o pedido pela aba ou pela ficha.

## 8. Cuidados e fora de escopo

- Não criar um segundo diálogo de retirada.

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

### 9.4 Texto original da task no planejamento

> **T140 · Pedidos de retirada na aba Solicitações e na ficha.** Primeiro acrescente o tipo RETIRADA à aba Solicitações da T132, com a ação Registrar retirada, que abre o diálogo da T104 já preenchido com quem solicitou e o motivo. Depois mostre na ficha do participante o aviso "retirada solicitada pelo participante". Por fim, remova a opção Recusar para esse tipo.

### 9.5 Requisitos que atravessam esta task

- Regra 9.2: “O backend deve disponibilizar uma **API HTTP REST** compartilhada pela web e pelo mobile, documentada no padrão **OpenAPI**”.
- Entregas: “Persistência dos dados, com scripts de criação/migração do banco e dados fictícios para demonstração” e “Tratamento de erros e validações das regras de negócio”.
- Testes (seção 7): “Testes unitários para todas as regras de negócio relevantes”, “Testes de integração para os principais endpoints” e “Execução automatizada dos testes a cada pull request”.
- Regras 9.3 e 9.4: web para a equipe do estudo e app para o participante.
- **Definition of Done** (texto completo na [seção 11 das convenções](00-convencoes-e-contrato-compartilhado.md#11-definition-of-done)): critérios de aceite demonstráveis com dados fictícios; revisão, CI e Pull Request; testes automatizados e 80% de cobertura do negócio no backend; erros com mensagens claras; Swagger e YAML atualizados; dados de demonstração, documentação, capturas e nota de bastão no repositório; RF01 a RF18 continuam funcionando.

---
Parte dos documentos de design da Sprint 4: [índice](README.md) · [convenções e contrato](00-convencoes-e-contrato-compartilhado.md).
