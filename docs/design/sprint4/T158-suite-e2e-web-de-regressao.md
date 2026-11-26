# T158 · Suíte E2E web de regressão

> **Sprint 4** · PBI-31 · Qualidade, regressão e preparação da Release 2  
> **Responsável:** I1 · **Estimativa:** 4h · **Prazo:** Sex 04/12  
> **Depende de:** T157  
> **Situação:** ⬜ A implementar (este documento é o guia para quem for fazer)

> Antes de começar, leia as [convenções e o contrato compartilhado da Sprint 4](00-convencoes-e-contrato-compartilhado.md): ajustes ao planejamento, endpoints, DTOs, códigos de erro, tabelas e dados de demonstração valem para esta task. A origem dos requisitos está na [seção 9](#9-origem-dos-requisitos-especificação-e-planejamento).

## 1. Objetivo

Reunir os E2E das três sprints em uma suíte com marcas `smoke` e `regressao`, rodando no CI.

## 2. Contexto: o que já existe e deve ser reaproveitado

- 11 specs atuais mais os 4 da Sprint 4 em `WEB/e2e/tests/`; job `E2E (Playwright)` roda tudo em PR e push em `main`/`develop`.
- O banco do CI não usa o perfil `demo`; os testes criam os dados.

## 3. Onde implementar

| Arquivo | Ação | Responsabilidade |
|---|---|---|
| `web/ClinicalStudyPB/e2e/tests/` | Alterar | Marcas |
| `web/ClinicalStudyPB/package.json` | Alterar | `test:e2e:smoke` e `test:e2e:regressao` |
| `.github/workflows/ci.yml` | Alterar | Seleção por evento |
| `docs/testes/matriz-regressao-release2.md` | Criar | RF × spec |

## 4. Requisitos a implementar

Todos os requisitos (`RQ-T158-NN`) precisam estar atendidos para a task ser concluída.

- **RQ-T158-01** Marcar com `{ tag: '@smoke' }` o fluxo principal (login, planejamento, inclusão, agendamento, desfecho, solicitação e painel) e com `@regressao` todos.
- **RQ-T158-02** Todo RF com interface web tem ao menos um cenário; RF17 a RF20 do app ficam na T159, e isso consta na matriz.
- **RQ-T158-03** Pull Request roda a smoke; push na `main` e Pull Request para a `main` rodam a regressão completa. Como os PBIs entram pela `develop`, o push na `develop` também roda a regressão.
- **RQ-T158-04** O projeto `setup` continua executando antes.
- **RQ-T158-05** Corrigir testes instáveis na causa (espera por estado, dados únicos), sem aumentar retries nem usar espera fixa.
- **RQ-T158-06** Provar estabilidade com `--repeat-each=3` e registrar o resultado.
- **RQ-T158-07** Rever o `timeout-minutes` do job conforme a duração real.
- **RQ-T158-08** Preservar os artefatos de relatório.
- **RQ-T158-09** Matriz RF × spec com o resultado da última execução.

## 5. Contrato, regras e códigos de erro

Capturas e nomes da seção 6.

## 6. Testes

`npm run test:e2e:smoke` e `npm run test:e2e:regressao`.

## 7. Critérios de aceite

- [ ] Smoke em todo PR, regressão completa na `main`, sem instabilidade conhecida.

## 8. Cuidados e fora de escopo

- Teste desativado precisa de Bug aberto e registro nos problemas conhecidos.

## 9. Origem dos requisitos (especificação e planejamento)

Texto **sem paráfrase** da especificação e do planejamento. Onde ele diverge dos [ajustes da seção 1 das convenções](00-convencoes-e-contrato-compartilhado.md#1-ajustes-em-relação-ao-planejamento-e-por-quê), valem as convenções e este documento.

### 9.1 Requisito da especificação

- Estratégia de testes (seção 7): “Cobertura mínima de **80% das linhas do código de negócio do backend**”, “Testes automatizados de componentes nas interfaces web e mobile” e “Pelo menos um fluxo E2E automatizado na web e um no mobile”.
- Entregas (seção 10): “Os requisitos de releases anteriores devem continuar funcionando.” Vale para RF01 a RF22.

### 9.2 Critério de aceite do PBI (planejamento)

> RF01 a RF22 passam na regressão; há E2E automatizado na web e no app; a cobertura de linhas do código de negócio do backend está em 80% ou mais; o sistema sobe em um clone limpo seguindo o README; os defeitos críticos estão corrigidos e o relatório de testes está pronto para a documentação.

### 9.3 Regras de negócio do planejamento que afetam esta task

Nenhuma das nove regras cita esta task diretamente.

### 9.4 Texto original da task no planejamento

> **T158 · Suíte E2E web de regressão.** Primeiro reúna os cenários Playwright das três sprints em uma suíte única, com as marcas smoke (fluxo principal) e regressão (todos os RF). Depois rode a suíte completa no CI sobre os dados consolidados e corrija os testes instáveis. Por fim, deixe a smoke rodando em todo Pull Request e a regressão na branch principal.

### 9.5 Requisitos que atravessam esta task

- Regra 9.6: “As equipes devem implementar testes automatizados e apresentar evidências de cobertura.”
- Regra 9.7: “O projeto deve conter documentação de instalação e execução que permita a outra equipe assumir o desenvolvimento na Release 3.”
- Entregas: “Arquivos de configuração para instalar, executar e testar o sistema, **sem senhas ou credenciais reais**.”
- **Definition of Done** (texto completo na [seção 11 das convenções](00-convencoes-e-contrato-compartilhado.md#11-definition-of-done)): critérios de aceite demonstráveis com dados fictícios; revisão, CI e Pull Request; testes automatizados e 80% de cobertura do negócio no backend; erros com mensagens claras; Swagger e YAML atualizados; dados de demonstração, documentação, capturas e nota de bastão no repositório; RF01 a RF18 continuam funcionando.

---
Parte dos documentos de design da Sprint 4: [índice](README.md) · [convenções e contrato](00-convencoes-e-contrato-compartilhado.md).
