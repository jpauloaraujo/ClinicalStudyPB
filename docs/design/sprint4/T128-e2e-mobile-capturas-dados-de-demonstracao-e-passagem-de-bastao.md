# T128 · E2E mobile, capturas, dados de demonstração e passagem de bastão

> **Sprint 4** · PBI-26 · RF19 Confirmação de presença e pedido de reagendamento no app  
> **Responsável:** I7 · **Estimativa:** 2h · **Prazo:** Qua 25/11  
> **Depende de:** T126  
> **Situação:** ⬜ A implementar (este documento é o guia para quem for fazer)

> Antes de começar, leia as [convenções e o contrato compartilhado da Sprint 4](00-convencoes-e-contrato-compartilhado.md): ajustes ao planejamento, endpoints, DTOs, códigos de erro, tabelas e dados de demonstração valem para esta task. A origem dos requisitos está na [seção 9](#9-origem-dos-requisitos-especificação-e-planejamento).

## 1. Objetivo

Fechar o PBI-26: E2E mobile, dados de demonstração, capturas, README e nota de passagem de bastão para I4.

## 2. Contexto: o que já existe e deve ser reaproveitado

- Fluxos em `APP/.maestro/` usam `appId: host.exp.exponent` e `testID`; `identificadores-e2e.test.tsx` protege os identificadores no Jest.
- Demonstração em `db/demo/R__demo_*.sql`; `DemoDataIntegrationTest` tem contagens globais.

## 3. Onde implementar

| Arquivo | Ação | Responsabilidade |
|---|---|---|
| `mobile/ClinicalStudyPB/.maestro/fluxo-solicitacoes.yaml` | Criar | Confirmar uma visita e pedir reagendamento de outra |
| `mobile/ClinicalStudyPB/src/telas/__tests__/identificadores-solicitacoes-e2e.test.tsx` | Criar | `testID`s do fluxo |
| `backend/ClinicalStudyPB/src/main/resources/db/demo/R__demo_26_solicitacoes.sql` | Criar | Estudo, participantes e pedidos pendentes |
| `backend/ClinicalStudyPB/src/test/java/UEPB/ESWII/ClinicalStudyPB/DemoSolicitacoesTest.java` | Criar | Carga e idempotência |
| `backend/ClinicalStudyPB/src/test/java/UEPB/ESWII/ClinicalStudyPB/DemoDataIntegrationTest.java` | Alterar | Filtrar contagens pelos estudos da Sprint 3 |
| `README.md` | Alterar | RF19 no app e credencial nova |
| `docs/imagens/sprint4/` | Criar | Capturas `rf19-*` |
| `docs/sprint4/bastao-pbi-26.md` | Criar | Nota para I4 |

## 4. Requisitos a implementar

Todos os requisitos (`RQ-T128-NN`) precisam estar atendidos para a task ser concluída.

- **RQ-T128-01** Criar `R__demo_26_solicitacoes.sql` (não `V4_26_2`: ajuste 2) com o estudo, as visitas previstas e os participantes `CAND-001` a `CAND-003` da seção 7, incluindo avaliação aprovada, consentimento e participação, como no `R__demo_23`.
- **RQ-T128-02** Criar a conta `demo.solicitante@clinicalstudy.test` (perfil `PARTICIPANTE`, hash do `R__dados_demonstracao.sql`) e vinculá-la ao `CAND-001`.
- **RQ-T128-03** Dois pedidos de reagendamento `PENDENTE` (`CAND-002` e `CAND-003`) com motivos fictícios.
- **RQ-T128-04** Script idempotente (`INSERT ... WHERE NOT EXISTS`), com SQL que rode no H2 e no PostgreSQL.
- **RQ-T128-05** Ajustar as consultas globais do `DemoDataIntegrationTest` para os estudos da Sprint 3, sem mudar os valores esperados.
- **RQ-T128-06** Rodar todos os testes que carregam `db/demo` (`AgendamentoEndpointTest`, `HistoricoParticipanteEndpointTest`, `ResultadoAtividadeEndpointTest`, `RetiradaEndpointTest` e os `Demo*Test`) e corrigir no mesmo Pull Request o que o estudo novo afetar, assim como os scripts de `scripts/verificar-*.py`, se necessário.
- **RQ-T128-07** Maestro: login com a conta nova, aba Visitas, confirmar **Retorno A**, conferir o selo, abrir **Retorno B**, pedir reagendamento com motivo, conferir o selo e a lista "Meus pedidos", sair. Capturar sucesso e erro (`rf19-confirmacao`, `rf19-pedido-reagendamento`, `rf19-meus-pedidos`, `rf19-erro-motivo`).
- **RQ-T128-08** O fluxo é um arquivo separado do `fluxo-participante.yaml` (ajuste 15): usa outra conta, porque a do P1 só tem uma visita futura, e muda os dados.
- **RQ-T128-09** Cabeçalho do YAML com pré-requisitos, inclusive banco recém-criado (o fluxo muda os dados).
- **RQ-T128-10** Executar no emulador; se não for possível, registrar no documento "não executado", com o motivo, sem apresentar captura que não foi gerada.
- **RQ-T128-11** README: RF19 no app (comportamento, erros, capturas), a credencial e o status.
- **RQ-T128-12** Nota de bastão: tabela de pedidos, regras, como gerar um pedido pelo app e o que o PBI-27 reaproveita (`SolicitacaoParticipante`, `RegraSolicitacao`, dados).

## 5. Contrato, regras e códigos de erro

Dados da seção 7 e capturas da seção 6 das convenções.

## 6. Testes

Carga do demo no H2 e no PostgreSQL, duas execuções sem duplicar; Jest dos identificadores; Maestro local.

## 7. Critérios de aceite

- [ ] O PBI-27 tem dois pedidos pendentes de demonstração.
- [ ] Os testes de demonstração das Sprints anteriores continuam verdes.
- [ ] README, capturas (ou registro da pendência) e nota de bastão no repositório.

## 8. Cuidados e fora de escopo

- Nenhum dado ou credencial real.
- Não alterar o estudo "Demo - Acompanhamento de participantes".

## 9. Origem dos requisitos (especificação e planejamento)

Texto **sem paráfrase** da especificação e do planejamento. Onde ele diverge dos [ajustes da seção 1 das convenções](00-convencoes-e-contrato-compartilhado.md#1-ajustes-em-relação-ao-planejamento-e-por-quê), valem as convenções e este documento.

### 9.1 Requisito da especificação

- **RF19** (Módulo E, Aplicativo do participante; interface Mobile): “Permitir confirmar presença prevista ou solicitar reagendamento de visita.”

### 9.2 Critério de aceite do PBI (planejamento)

> no detalhe de uma visita agendada, o participante confirma presença ou pede reagendamento com motivo; o app mostra a situação de cada pedido (pendente, atendido ou recusado, com a resposta); pedidos inválidos são recusados com a regra violada. Este PBI cria a tabela de pedidos do participante que os PBIs 27 e 28 usam.

### 9.3 Regras de negócio do planejamento que afetam esta task

Nenhuma das nove regras cita esta task diretamente.

### 9.4 Texto original da task no planejamento

> **T128 · E2E mobile, capturas, dados de demonstração e passagem de bastão.** Primeiro acrescente ao fluxo Maestro da Sprint 3 os passos de confirmar uma visita e pedir o reagendamento de outra. Depois crie o script V4_26_2__demo_participant_requests.sql (perfil demo, repetível) com pedidos pendentes de reagendamento para o PBI-27. Em seguida salve as capturas de sucesso e de erro e atualize o README com o RF19 no app. Por fim, passe o bastão a I4 com uma nota curta: tabela de pedidos, regras e como gerar um pedido pelo app de demonstração.

### 9.5 Requisitos que atravessam esta task

- Regra 9.2: “O backend deve disponibilizar uma **API HTTP REST** compartilhada pela web e pelo mobile, documentada no padrão **OpenAPI**”.
- Entregas: “Persistência dos dados, com scripts de criação/migração do banco e dados fictícios para demonstração” e “Tratamento de erros e validações das regras de negócio”.
- Testes (seção 7): “Testes unitários para todas as regras de negócio relevantes”, “Testes de integração para os principais endpoints” e “Execução automatizada dos testes a cada pull request”.
- Regra 9.4: “A interface **mobile** atende ao perfil de participante e executa em dispositivo ou emulador.”
- **Definition of Done** (texto completo na [seção 11 das convenções](00-convencoes-e-contrato-compartilhado.md#11-definition-of-done)): critérios de aceite demonstráveis com dados fictícios; revisão, CI e Pull Request; testes automatizados e 80% de cobertura do negócio no backend; erros com mensagens claras; Swagger e YAML atualizados; dados de demonstração, documentação, capturas e nota de bastão no repositório; RF01 a RF18 continuam funcionando.

---
Parte dos documentos de design da Sprint 4: [índice](README.md) · [convenções e contrato](00-convencoes-e-contrato-compartilhado.md).
