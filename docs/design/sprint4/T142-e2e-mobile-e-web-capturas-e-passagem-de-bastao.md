# T142 · E2E mobile e web, capturas e passagem de bastão

> **Sprint 4** · PBI-28 · RF20 Retirada voluntária pelo app  
> **Responsável:** I5 · **Estimativa:** 2h · **Prazo:** Dom 29/11  
> **Depende de:** T139, T140  
> **Situação:** ⬜ A implementar (este documento é o guia para quem for fazer)

> Antes de começar, leia as [convenções e o contrato compartilhado da Sprint 4](00-convencoes-e-contrato-compartilhado.md): ajustes ao planejamento, endpoints, DTOs, códigos de erro, tabelas e dados de demonstração valem para esta task. A origem dos requisitos está na [seção 9](#9-origem-dos-requisitos-especificação-e-planejamento).

## 1. Objetivo

E2E mobile e web do RF20, dados de demonstração, capturas, README e bastão para I6.

## 2. Contexto: o que já existe e deve ser reaproveitado

- `.maestro/fluxo-solicitacoes.yaml` (T128) e `WEB/e2e/tests/retirada.spec.ts` (T107).
- `R__demo_26` já criou o estudo de solicitações.

## 3. Onde implementar

| Arquivo | Ação | Responsabilidade |
|---|---|---|
| `backend/ClinicalStudyPB/src/main/resources/db/demo/R__demo_28_pedidos_retirada.sql` | Criar | Um pedido pendente e um efetivado |
| `backend/ClinicalStudyPB/src/test/java/UEPB/ESWII/ClinicalStudyPB/DemoSolicitacoesTest.java` | Alterar | Cenário completo |
| `mobile/ClinicalStudyPB/.maestro/fluxo-solicitacoes.yaml` | Alterar | Passos da retirada |
| `web/ClinicalStudyPB/e2e/tests/retirada-voluntaria.spec.ts` | Criar | Efetivação |
| `web/ClinicalStudyPB/e2e/support/api.ts` | Alterar | `pedirRetirada` |
| `docs/imagens/sprint4/` | Alterar | `rf20-*` |
| `README.md` | Alterar | RF20 |
| `docs/sprint4/bastao-pbi-28.md` | Criar | Nota para I6 |

## 4. Requisitos a implementar

Todos os requisitos (`RQ-T142-NN`) precisam estar atendidos para a task ser concluída.

- **RQ-T142-01** `R__demo_28_pedidos_retirada.sql` (não `V4_28_1`): `CAND-004` com pedido `PENDENTE` e `CAND-005` retirado com pedido `ATENDIDA`, registro em `retiradas_participacao` e visita cancelada, como na seção 7; idempotente.
- **RQ-T142-02** Conferir que, com os dois scripts, há visita realizada, atrasada, pendente, cancelada e não agendada, e pedidos pendentes e atendidos.
- **RQ-T142-03** Maestro: depois da confirmação e do reagendamento, pedir a retirada marcando a ciência, conferir o aviso e a ausência dos botões do RF19.
- **RQ-T142-04** Playwright: partida pela API (participante com conta e pedido), o coordenador efetiva pela aba; conferir pela API como participante a situação `RETIRADO` e o pedido atendido; pesquisador sem ação.
- **RQ-T142-05** Capturas `rf20-pedido-app`, `rf20-ciencia-obrigatoria`, `rf20-solicitacao-web`, `rf20-efetivada`.
- **RQ-T142-06** README: RF20 com regras, erros e capturas.
- **RQ-T142-07** Se o Maestro não puder rodar, registrar o motivo e não incluir captura inexistente.
- **RQ-T142-08** Bastão: os dados de demonstração e os números esperados do painel (seção 3.1).

## 5. Contrato, regras e códigos de erro

Seção 7.

## 6. Testes

Carga do demo, Playwright com `--repeat-each=3`, Maestro local.

## 7. Critérios de aceite

- [ ] Dados cobrem todas as situações do painel; E2E web verde; RF20 documentado.

## 8. Cuidados e fora de escopo

- O Maestro muda os dados: recriar o banco antes de repetir.

## 9. Origem dos requisitos (especificação e planejamento)

Texto **sem paráfrase** da especificação e do planejamento. Onde ele diverge dos [ajustes da seção 1 das convenções](00-convencoes-e-contrato-compartilhado.md#1-ajustes-em-relação-ao-planejamento-e-por-quê), valem as convenções e este documento.

### 9.1 Requisito da especificação

- **RF20** (Módulo E, Aplicativo do participante; interface Mobile): “Permitir solicitar retirada voluntária do estudo.”

### 9.2 Critério de aceite do PBI (planejamento)

> o participante solicita a retirada de um estudo pelo app, ciente dos efeitos; o coordenador vê o pedido e efetiva a retirada pelo RF16; o app mostra o pedido pendente e, depois, a participação como RETIRADO; pedidos duplicados ou de participação já retirada são recusados.

### 9.3 Regras de negócio do planejamento que afetam esta task

Nenhuma das nove regras cita esta task diretamente.

### 9.4 Texto original da task no planejamento

> **T142 · E2E mobile e web, capturas e passagem de bastão.** Primeiro acrescente ao fluxo Maestro o pedido de retirada e, no Playwright, a efetivação pelo coordenador. Depois crie o script V4_28_1__demo_withdrawal_requests.sql (perfil demo, repetível) com um pedido pendente e um já efetivado. Em seguida salve as capturas e atualize o README com o RF20. Por fim, passe o bastão a I6: os dados de demonstração agora têm todas as situações que o painel precisa contar.

### 9.5 Requisitos que atravessam esta task

- Regra 9.2: “O backend deve disponibilizar uma **API HTTP REST** compartilhada pela web e pelo mobile, documentada no padrão **OpenAPI**”.
- Entregas: “Persistência dos dados, com scripts de criação/migração do banco e dados fictícios para demonstração” e “Tratamento de erros e validações das regras de negócio”.
- Testes (seção 7): “Testes unitários para todas as regras de negócio relevantes”, “Testes de integração para os principais endpoints” e “Execução automatizada dos testes a cada pull request”.
- Regras 9.3 e 9.4: web para a equipe do estudo e app para o participante.
- **Definition of Done** (texto completo na [seção 11 das convenções](00-convencoes-e-contrato-compartilhado.md#11-definition-of-done)): critérios de aceite demonstráveis com dados fictícios; revisão, CI e Pull Request; testes automatizados e 80% de cobertura do negócio no backend; erros com mensagens claras; Swagger e YAML atualizados; dados de demonstração, documentação, capturas e nota de bastão no repositório; RF01 a RF18 continuam funcionando.

---
Parte dos documentos de design da Sprint 4: [índice](README.md) · [convenções e contrato](00-convencoes-e-contrato-compartilhado.md).
