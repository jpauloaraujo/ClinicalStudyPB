# T135 · E2E web, capturas e passagem de bastão

> **Sprint 4** · PBI-27 · RF19 Atendimento dos pedidos de reagendamento na web  
> **Responsável:** I4 · **Estimativa:** 2h · **Prazo:** Sex 27/11  
> **Depende de:** T132  
> **Situação:** ⬜ A implementar (este documento é o guia para quem for fazer)

> Antes de começar, leia as [convenções e o contrato compartilhado da Sprint 4](00-convencoes-e-contrato-compartilhado.md): ajustes ao planejamento, endpoints, DTOs, códigos de erro, tabelas e dados de demonstração valem para esta task. A origem dos requisitos está na [seção 9](#9-origem-dos-requisitos-especificação-e-planejamento).

## 1. Objetivo

E2E web do atendimento, capturas, README e passagem de bastão para I5.

## 2. Contexto: o que já existe e deve ser reaproveitado

- `ApiE2E` e fixtures em `WEB/e2e/`; dados únicos por execução; `capturar` grava em `test-results/capturas/`.
- Cuidado com capturas: rolar ao topo antes (ver `AgendaPage.rolarParaOTopo`).

## 3. Onde implementar

| Arquivo | Ação | Responsabilidade |
|---|---|---|
| `web/ClinicalStudyPB/e2e/tests/solicitacoes-reagendamento.spec.ts` | Criar | Atender e recusar |
| `web/ClinicalStudyPB/e2e/pages/SolicitacoesPage.ts` | Criar | Page object |
| `web/ClinicalStudyPB/e2e/fixtures/index.ts` | Alterar | Fixture |
| `web/ClinicalStudyPB/e2e/support/api.ts` | Alterar | `participanteComConta`, `como participante`, `pedirReagendamento` |
| `docs/imagens/sprint4/` | Alterar | `rf19-solicitacoes`, `rf19-atendimento`, `rf19-erro-janela`, `rf19-recusa` |
| `README.md` | Alterar | RF19 na web |
| `docs/sprint4/bastao-pbi-27.md` | Criar | Nota para I5 |

## 4. Requisitos a implementar

Todos os requisitos (`RQ-T135-NN`) precisam estar atendidos para a task ser concluída.

- **RQ-T135-01** Cenário: o coordenador atende um pedido e recusa outro; conferir **pela API, como o participante**, a situação e a resposta (`GET /api/me/pedidos`).
- **RQ-T135-02** O planejamento fala em usar os dados de demonstração; o CI não roda o perfil `demo`, então a partida é montada pela API (dois participantes com conta, visitas futuras e pedidos). Os dados do `R__demo_26` ficam para a demonstração manual.
- **RQ-T135-03** Para cada participante da partida: o administrador cria a conta fictícia, o coordenador a vincula (`PUT /api/participacoes/{participacaoId}/conta`), e o teste faz login como ela para criar o pedido pela API.
- **RQ-T135-04** Cenário de erro: data fora da janela mantém o pedido pendente.
- **RQ-T135-05** Pesquisador abre a aba e não vê botões.
- **RQ-T135-06** Dados únicos por execução; provar estabilidade com `--repeat-each=3`.
- **RQ-T135-07** Capturas listadas acima copiadas para `docs/imagens/sprint4/`.
- **RQ-T135-08** README: RF19 na web, com regras, erros e capturas; marcar RF19 concluído.
- **RQ-T135-09** Nota de bastão: aba pronta para `RETIRADA` (`acoesDaSolicitacao`), `FechamentoDeSolicitacoes.recusarReagendamentosDaParticipacao`.

## 5. Contrato, regras e códigos de erro

Operações 4 a 7.

## 6. Testes

`npm run test:e2e -- solicitacoes-reagendamento`.

## 7. Critérios de aceite

- [ ] E2E verde localmente e no CI.
- [ ] RF19 documentado e bastão entregue.

## 8. Cuidados e fora de escopo

- A suíte E2E web não tem sessão de participante: usar só a API para ele, sem abrir a web.

## 9. Origem dos requisitos (especificação e planejamento)

Texto **sem paráfrase** da especificação e do planejamento. Onde ele diverge dos [ajustes da seção 1 das convenções](00-convencoes-e-contrato-compartilhado.md#1-ajustes-em-relação-ao-planejamento-e-por-quê), valem as convenções e este documento.

### 9.1 Requisito da especificação

- **RF19** (Módulo E, Aplicativo do participante; interface Mobile): “Permitir confirmar presença prevista ou solicitar reagendamento de visita.”

### 9.2 Critério de aceite do PBI (planejamento)

> o coordenador vê os pedidos do estudo, atende um reagendamento escolhendo uma data válida ou o recusa com resposta, e o participante vê o resultado no app; as presenças confirmadas aparecem na agenda e no histórico; o pesquisador consulta tudo em modo somente leitura.

### 9.3 Regras de negócio do planejamento que afetam esta task

Nenhuma das nove regras cita esta task diretamente.

### 9.4 Texto original da task no planejamento

> **T135 · E2E web, capturas e passagem de bastão.** Primeiro escreva o cenário Playwright em que o coordenador atende um pedido dos dados de demonstração e recusa outro, conferindo pela API que o participante vê as respostas. Depois salve capturas de sucesso e de erro e atualize o README com o RF19 na web. Por fim, passe o bastão a I5: aba Solicitações pronta para receber o tipo RETIRADA.

### 9.5 Requisitos que atravessam esta task

- Regra 9.2: “O backend deve disponibilizar uma **API HTTP REST** compartilhada pela web e pelo mobile, documentada no padrão **OpenAPI**”.
- Entregas: “Persistência dos dados, com scripts de criação/migração do banco e dados fictícios para demonstração” e “Tratamento de erros e validações das regras de negócio”.
- Testes (seção 7): “Testes unitários para todas as regras de negócio relevantes”, “Testes de integração para os principais endpoints” e “Execução automatizada dos testes a cada pull request”.
- Regra 9.3: “A interface **web** atende aos perfis de pesquisador, coordenador e administrador.”
- **Definition of Done** (texto completo na [seção 11 das convenções](00-convencoes-e-contrato-compartilhado.md#11-definition-of-done)): critérios de aceite demonstráveis com dados fictícios; revisão, CI e Pull Request; testes automatizados e 80% de cobertura do negócio no backend; erros com mensagens claras; Swagger e YAML atualizados; dados de demonstração, documentação, capturas e nota de bastão no repositório; RF01 a RF18 continuam funcionando.

---
Parte dos documentos de design da Sprint 4: [índice](README.md) · [convenções e contrato](00-convencoes-e-contrato-compartilhado.md).
