# T149 · E2E, capturas e passagem de bastão

> **Sprint 4** · PBI-29 · RF21 Painel do estudo  
> **Responsável:** I6 · **Estimativa:** 2h · **Prazo:** Ter 01/12  
> **Depende de:** T146  
> **Situação:** ⬜ A implementar (este documento é o guia para quem for fazer)

> Antes de começar, leia as [convenções e o contrato compartilhado da Sprint 4](00-convencoes-e-contrato-compartilhado.md): ajustes ao planejamento, endpoints, DTOs, códigos de erro, tabelas e dados de demonstração valem para esta task. A origem dos requisitos está na [seção 9](#9-origem-dos-requisitos-especificação-e-planejamento).

## 1. Objetivo

E2E do painel, capturas, README e bastão para I3.

## 2. Contexto: o que já existe e deve ser reaproveitado

- Playwright do CI sem perfil `demo`: montar o estado pela API.

## 3. Onde implementar

| Arquivo | Ação | Responsabilidade |
|---|---|---|
| `web/ClinicalStudyPB/e2e/tests/painel.spec.ts` | Criar | Cenário |
| `web/ClinicalStudyPB/e2e/pages/PainelPage.ts` | Criar | Page object |
| `web/ClinicalStudyPB/e2e/fixtures/index.ts` | Alterar | Fixture |
| `docs/imagens/sprint4/` | Alterar | `rf21-*` |
| `README.md` | Alterar | RF21 |
| `docs/sprint4/bastao-pbi-29.md` | Criar | Nota para I3 |
| `web/ClinicalStudyPB/e2e/support/api.ts` | Alterar | `hojeNoFusoDoCentro` e visita atrasada |

## 4. Requisitos a implementar

Todos os requisitos (`RQ-T149-NN`) precisam estar atendidos para a task ser concluída.

- **RQ-T149-01** Montar pela API um estudo com valores conhecidos (ajuste 16): candidatos em duas situações, participantes incluídos, uma visita realizada, agendamentos futuros e **uma visita atrasada**.
- **RQ-T149-02** Criar a visita atrasada pela própria API (ajuste 17): visita prevista no dia 0 com tolerância 0 e agendamento para **hoje às 00:00**. O servidor recusa só datas anteriores a hoje, então o agendamento é aceito e já nasce com data e hora passadas. "Hoje" é calculado no fuso da aplicação (`app.fuso-horario`, padrão `America/Recife`), nunca no fuso da máquina do teste (o runner do CI usa UTC).
- **RQ-T149-03** Abrir o painel, conferir os números (incluindo 1 atrasada), seguir o link das visitas atrasadas e conferir que a agenda lista exatamente essa visita.
- **RQ-T149-04** Executar também manualmente contra o perfil `demo`, conferir os números da seção 3.1 e capturar o painel e a lista de atrasadas.
- **RQ-T149-05** Capturas `rf21-painel`, `rf21-visao-geral`, `rf21-atrasadas`, `rf21-estudo-vazio`.
- **RQ-T149-06** README com RF21 e definições resumidas.
- **RQ-T149-07** Estabilidade com `--repeat-each=3`.
- **RQ-T149-08** Bastão: `PainelService`, definições da T143 e como o relatório deve reutilizá-los.

## 5. Contrato, regras e códigos de erro

Operações 10 e 11.

## 6. Testes

`npm run test:e2e -- painel`.

## 7. Critérios de aceite

- [ ] E2E verde, RF21 documentado, bastão entregue.

## 8. Cuidados e fora de escopo

- Dados únicos por execução; não depender de totais globais na página inicial.

## 9. Origem dos requisitos (especificação e planejamento)

Texto **sem paráfrase** da especificação e do planejamento. Onde ele diverge dos [ajustes da seção 1 das convenções](00-convencoes-e-contrato-compartilhado.md#1-ajustes-em-relação-ao-planejamento-e-por-quê), valem as convenções e este documento.

### 9.1 Requisito da especificação

- **RF21** (Módulo F, Monitoramento; interface Web): “Exibir painel com quantidade de candidatos, participantes incluídos e visitas realizadas ou pendentes.”

### 9.2 Critério de aceite do PBI (planejamento)

> pesquisador e coordenador abrem o painel de um estudo e veem a quantidade de candidatos por situação, participantes incluídos (ativos, retirados e vagas) e visitas realizadas, pendentes, atrasadas e não agendadas, com os números conferindo com os dados de demonstração; cada indicador leva à lista correspondente.

### 9.3 Regras de negócio do planejamento que afetam esta task

Nenhuma das nove regras cita esta task diretamente.

### 9.4 Texto original da task no planejamento

> **T149 · E2E, capturas e passagem de bastão.** Primeiro escreva o cenário Playwright que abre o painel do estudo de demonstração, confere alguns números e segue o link das visitas atrasadas. Depois salve as capturas e atualize o README com o RF21. Por fim, passe o bastão a I3: o serviço de leitura da T144 e as definições da T143 servem de base para o relatório.

### 9.5 Requisitos que atravessam esta task

- Regra 9.2: “O backend deve disponibilizar uma **API HTTP REST** compartilhada pela web e pelo mobile, documentada no padrão **OpenAPI**”.
- Entregas: “Persistência dos dados, com scripts de criação/migração do banco e dados fictícios para demonstração” e “Tratamento de erros e validações das regras de negócio”.
- Testes (seção 7): “Testes unitários para todas as regras de negócio relevantes”, “Testes de integração para os principais endpoints” e “Execução automatizada dos testes a cada pull request”.
- Regra 9.3: “A interface **web** atende aos perfis de pesquisador, coordenador e administrador.”
- **Definition of Done** (texto completo na [seção 11 das convenções](00-convencoes-e-contrato-compartilhado.md#11-definition-of-done)): critérios de aceite demonstráveis com dados fictícios; revisão, CI e Pull Request; testes automatizados e 80% de cobertura do negócio no backend; erros com mensagens claras; Swagger e YAML atualizados; dados de demonstração, documentação, capturas e nota de bastão no repositório; RF01 a RF18 continuam funcionando.

---
Parte dos documentos de design da Sprint 4: [índice](README.md) · [convenções e contrato](00-convencoes-e-contrato-compartilhado.md).
