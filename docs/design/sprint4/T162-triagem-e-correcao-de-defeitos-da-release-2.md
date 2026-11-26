# T162 · Triagem e correção de defeitos da Release 2

> **Sprint 4** · PBI-31 · Qualidade, regressão e preparação da Release 2  
> **Responsável:** I1 · **Estimativa:** 3h · **Prazo:** Sáb 05/12  
> **Depende de:** T158, T159  
> **Situação:** ⬜ A implementar (este documento é o guia para quem for fazer)

> Antes de começar, leia as [convenções e o contrato compartilhado da Sprint 4](00-convencoes-e-contrato-compartilhado.md): ajustes ao planejamento, endpoints, DTOs, códigos de erro, tabelas e dados de demonstração valem para esta task. A origem dos requisitos está na [seção 9](#9-origem-dos-requisitos-especificação-e-planejamento).

## 1. Objetivo

Registrar, classificar e corrigir os defeitos encontrados na regressão da Release 2.

## 2. Contexto: o que já existe e deve ser reaproveitado

- `docs/testes/registro-de-defeitos.md` e `docs/rastreabilidade/rastreabilidade-erros-e-alteracoes.md` com ids por sprint.

## 3. Onde implementar

| Arquivo | Ação | Responsabilidade |
|---|---|---|
| `docs/testes/registro-de-defeitos.md` | Alterar | Itens `D-S4-NN` |
| `docs/rastreabilidade/rastreabilidade-erros-e-alteracoes.md` | Alterar | Causa e resolução |
| `docs/sprint4/problemas-conhecidos-release2.md` | Criar | O que fica |

## 4. Requisitos a implementar

Todos os requisitos (`RQ-T162-NN`) precisam estar atendidos para a task ser concluída.

- **RQ-T162-01** Cada falha vira Bug no Azure Boards, ligado à task de origem, com severidade (crítica, alta, média, baixa) e passos para reproduzir.
- **RQ-T162-02** Definir crítico: perda ou corrupção de dados, acesso indevido, requisito RF01 a RF22 inutilizável ou instalação impossível.
- **RQ-T162-03** Corrigir os críticos até 05/12 às 18h, chamando o dono do PBI quando preciso.
- **RQ-T162-04** Toda correção tem teste que falhava antes e passa depois.
- **RQ-T162-05** Correções por Pull Request com CI verde.
- **RQ-T162-06** Atualizar o registro de defeitos e a rastreabilidade.
- **RQ-T162-07** Listar os não corrigidos com impacto, contorno e justificativa.
- **RQ-T162-08** Rodar a regressão de novo depois das correções.

## 5. Contrato, regras e códigos de erro

Definition of Done e congelamento de código.

## 6. Testes

Regressão completa (T158) após as correções.

## 7. Critérios de aceite

- [ ] Nenhum crítico aberto; os demais registrados.

## 8. Cuidados e fora de escopo

- Depois do congelamento, só correção crítica.

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

> **T162 · Triagem e correção de defeitos da Release 2.** Primeiro registre como Bug no Azure Boards cada falha da regressão, ligada à task de origem, com severidade. Depois corrija os críticos até o congelamento de código, chamando o dono do PBI quando preciso. Por fim, liste os que ficam como problemas conhecidos, com justificativa.

### 9.5 Requisitos que atravessam esta task

- Regra 9.6: “As equipes devem implementar testes automatizados e apresentar evidências de cobertura.”
- Regra 9.7: “O projeto deve conter documentação de instalação e execução que permita a outra equipe assumir o desenvolvimento na Release 3.”
- Entregas: “Arquivos de configuração para instalar, executar e testar o sistema, **sem senhas ou credenciais reais**.”
- **Definition of Done** (texto completo na [seção 11 das convenções](00-convencoes-e-contrato-compartilhado.md#11-definition-of-done)): critérios de aceite demonstráveis com dados fictícios; revisão, CI e Pull Request; testes automatizados e 80% de cobertura do negócio no backend; erros com mensagens claras; Swagger e YAML atualizados; dados de demonstração, documentação, capturas e nota de bastão no repositório; RF01 a RF18 continuam funcionando.

---
Parte dos documentos de design da Sprint 4: [índice](README.md) · [convenções e contrato](00-convencoes-e-contrato-compartilhado.md).
