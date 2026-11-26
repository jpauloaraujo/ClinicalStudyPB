# T163 · Relatório de testes e passagem de bastão

> **Sprint 4** · PBI-31 · Qualidade, regressão e preparação da Release 2  
> **Responsável:** I1 · **Estimativa:** 1h · **Prazo:** Sáb 05/12  
> **Depende de:** T160, T162  
> **Situação:** ⬜ A implementar (este documento é o guia para quem for fazer)

> Antes de começar, leia as [convenções e o contrato compartilhado da Sprint 4](00-convencoes-e-contrato-compartilhado.md): ajustes ao planejamento, endpoints, DTOs, códigos de erro, tabelas e dados de demonstração valem para esta task. A origem dos requisitos está na [seção 9](#9-origem-dos-requisitos-especificação-e-planejamento).

## 1. Objetivo

Consolidar o relatório de testes da Release 2 e passar o bastão a I2 com o commit candidato à tag.

## 2. Contexto: o que já existe e deve ser reaproveitado

- `docs/testes/relatorio-de-testes-sprints-1-a-3.md` é o modelo.

## 3. Onde implementar

| Arquivo | Ação | Responsabilidade |
|---|---|---|
| `docs/testes/release2.md` | Alterar | Completar o relatório iniciado na T160 |
| `docs/sprint4/bastao-pbi-31.md` | Criar | Nota para I2 |

## 4. Requisitos a implementar

Todos os requisitos (`RQ-T163-NN`) precisam estar atendidos para a task ser concluída.

- **RQ-T163-01** Quantidade de testes por nível e camada (backend unidade e integração, PostgreSQL, web, app, E2E web e mobile).
- **RQ-T163-02** Resultado de cada suíte com data, commit e link da execução do CI.
- **RQ-T163-03** Cobertura do backend (linhas do negócio) e cobertura da web e do app.
- **RQ-T163-04** Evidências dos E2E (relatório do Playwright, gravação ou capturas do Maestro) ou a indicação de que não foi executado.
- **RQ-T163-05** Matriz RF01 a RF22 × testes.
- **RQ-T163-06** Comandos para reproduzir.
- **RQ-T163-07** Problemas conhecidos, com link para a lista da T162.
- **RQ-T163-08** Indicar o commit candidato à tag na `main`, com CI verde.
- **RQ-T163-09** Entregar a nota de bastão a I2.

## 5. Contrato, regras e códigos de erro

Seção 7 da especificação.

## 6. Testes

Conferir os números contra os artefatos do CI do commit indicado.

## 7. Critérios de aceite

- [ ] Relatório publicado e commit candidato informado.

## 8. Cuidados e fora de escopo

- Não copiar números de execuções antigas.

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

> **T163 · Relatório de testes e passagem de bastão.** Primeiro consolide em docs/testes/release2.md a quantidade de testes por nível, os resultados das suítes, a cobertura e as evidências dos E2E. Depois passe o bastão a I2 com o relatório, a lista de problemas conhecidos e o commit candidato à tag.

### 9.5 Requisitos que atravessam esta task

- Regra 9.6: “As equipes devem implementar testes automatizados e apresentar evidências de cobertura.”
- Regra 9.7: “O projeto deve conter documentação de instalação e execução que permita a outra equipe assumir o desenvolvimento na Release 3.”
- Entregas: “Arquivos de configuração para instalar, executar e testar o sistema, **sem senhas ou credenciais reais**.”
- **Definition of Done** (texto completo na [seção 11 das convenções](00-convencoes-e-contrato-compartilhado.md#11-definition-of-done)): critérios de aceite demonstráveis com dados fictícios; revisão, CI e Pull Request; testes automatizados e 80% de cobertura do negócio no backend; erros com mensagens claras; Swagger e YAML atualizados; dados de demonstração, documentação, capturas e nota de bastão no repositório; RF01 a RF18 continuam funcionando.

---
Parte dos documentos de design da Sprint 4: [índice](README.md) · [convenções e contrato](00-convencoes-e-contrato-compartilhado.md).
