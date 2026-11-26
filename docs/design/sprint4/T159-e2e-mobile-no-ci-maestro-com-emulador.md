# T159 · E2E mobile no CI (Maestro com emulador)

> **Sprint 4** · PBI-31 · Qualidade, regressão e preparação da Release 2  
> **Responsável:** I1 · **Estimativa:** 4h · **Prazo:** Sex 04/12  
> **Depende de:** T157  
> **Situação:** ⬜ A implementar (este documento é o guia para quem for fazer)

> Antes de começar, leia as [convenções e o contrato compartilhado da Sprint 4](00-convencoes-e-contrato-compartilhado.md): ajustes ao planejamento, endpoints, DTOs, códigos de erro, tabelas e dados de demonstração valem para esta task. A origem dos requisitos está na [seção 9](#9-origem-dos-requisitos-especificação-e-planejamento).

## 1. Objetivo

Tentar, por no máximo três horas, rodar o Maestro em emulador Android no GitHub Actions.

## 2. Contexto: o que já existe e deve ser reaproveitado

- Fluxos em `APP/.maestro/` abrem o Expo Go (`appId: host.exp.exponent`) e usam `10.0.2.2` para o backend.
- Segundo item da ordem de corte.

## 3. Onde implementar

| Arquivo | Ação | Responsabilidade |
|---|---|---|
| `.github/workflows/e2e-mobile.yml` | Criar | Job com emulador |
| `mobile/ClinicalStudyPB/.maestro/` | Alterar | Parâmetros para o CI |
| `mobile/ClinicalStudyPB/README.md` | Alterar | Como rodar local e no CI |
| `docs/sprint4/e2e-mobile-release2.md` | Criar | Evidência ou decisão |

## 4. Requisitos a implementar

Todos os requisitos (`RQ-T159-NN`) precisam estar atendidos para a task ser concluída.

- **RQ-T159-01** Job separado, em `workflow_dispatch` e push na `main`, para não atrasar os PRs.
- **RQ-T159-02** Subir PostgreSQL e o backend com o perfil `demo`; esperar a API responder.
- **RQ-T159-03** Emulador com `reactivecircus/android-emulator-runner` (com KVM habilitado no runner), versão fixada.
- **RQ-T159-04** Instalar o app: Expo Go compatível com o SDK 58 com `npm start`, ou build próprio (`npx expo prebuild` + Gradle) ajustando o `appId`; registrar a opção escolhida e o motivo.
- **RQ-T159-05** Instalar o Maestro em versão fixada e executar os fluxos das Sprints 3 e 4, recriando o banco entre fluxos que alteram dados.
- **RQ-T159-06** Publicar gravação, capturas, relatório e logs como artefatos, mesmo em falha.
- **RQ-T159-07** Ações de terceiros fixadas em versão ou commit; nenhum segredo real.
- **RQ-T159-08** Se não estabilizar em três horas: manter execução local com evidência (gravação ou capturas reais, data, dispositivo) e registrar como problema conhecido.
- **RQ-T159-09** Nunca marcar como executado um fluxo que não rodou.

## 5. Contrato, regras e códigos de erro

Regra de testes: ao menos um fluxo E2E no mobile.

## 6. Testes

Três execuções seguidas verdes para considerar estável.

## 7. Critérios de aceite

- [ ] Job estável com artefatos, ou decisão registrada com evidência local.

## 8. Cuidados e fora de escopo

- Não tornar o job obrigatório enquanto for instável.
- Baixar binários só das fontes oficiais.

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

> **T159 · E2E mobile no CI (Maestro com emulador).** Primeiro configure um job do GitHub Actions com emulador Android que instala o app e roda o fluxo Maestro das Sprints 3 e 4 contra o backend com dados de demonstração. Depois publique a gravação e o resultado como artefatos do pipeline. Limite a tentativa a três horas: se não estabilizar, mantenha a execução local com evidência e registre a decisão como problema conhecido da Release 2.

### 9.5 Requisitos que atravessam esta task

- Regra 9.6: “As equipes devem implementar testes automatizados e apresentar evidências de cobertura.”
- Regra 9.7: “O projeto deve conter documentação de instalação e execução que permita a outra equipe assumir o desenvolvimento na Release 3.”
- Entregas: “Arquivos de configuração para instalar, executar e testar o sistema, **sem senhas ou credenciais reais**.”
- **Definition of Done** (texto completo na [seção 11 das convenções](00-convencoes-e-contrato-compartilhado.md#11-definition-of-done)): critérios de aceite demonstráveis com dados fictícios; revisão, CI e Pull Request; testes automatizados e 80% de cobertura do negócio no backend; erros com mensagens claras; Swagger e YAML atualizados; dados de demonstração, documentação, capturas e nota de bastão no repositório; RF01 a RF18 continuam funcionando.

---
Parte dos documentos de design da Sprint 4: [índice](README.md) · [convenções e contrato](00-convencoes-e-contrato-compartilhado.md).
