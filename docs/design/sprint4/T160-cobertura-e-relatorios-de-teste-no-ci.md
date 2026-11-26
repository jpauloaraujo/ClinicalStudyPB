# T160 · Cobertura e relatórios de teste no CI

> **Sprint 4** · PBI-31 · Qualidade, regressão e preparação da Release 2  
> **Responsável:** I1 · **Estimativa:** 3h · **Prazo:** Sáb 05/12  
> **Depende de:** T158  
> **Situação:** ⬜ A implementar (este documento é o guia para quem for fazer)

> Antes de começar, leia as [convenções e o contrato compartilhado da Sprint 4](00-convencoes-e-contrato-compartilhado.md): ajustes ao planejamento, endpoints, DTOs, códigos de erro, tabelas e dados de demonstração valem para esta task. A origem dos requisitos está na [seção 9](#9-origem-dos-requisitos-especificação-e-planejamento).

## 1. Objetivo

Garantir 80% de cobertura do negócio no backend e publicar os relatórios de cobertura da web e do app.

## 2. Contexto: o que já existe e deve ser reaproveitado

- JaCoCo com regra sobre `domain` e `service` e `COBERTURA_BLOQUEAR: 'true'`; `.github/scripts/resumo-cobertura.py`.
- Web em Vitest 5; app em Jest.

## 3. Onde implementar

| Arquivo | Ação | Responsabilidade |
|---|---|---|
| `.github/workflows/ci.yml` | Alterar | Cobertura e artefatos |
| `web/ClinicalStudyPB/package.json` | Alterar | `@vitest/coverage-v8` (mesma versão do Vitest) e `test:cobertura` |
| `mobile/ClinicalStudyPB/package.json` | Alterar | `test:cobertura` |
| `mobile/ClinicalStudyPB/jest.config.js` | Alterar | `collectCoverageFrom` |
| `backend/ClinicalStudyPB/src/test/java/UEPB/ESWII/ClinicalStudyPB/` | Alterar | Lacunas dos serviços novos |
| `docs/testes/release2.md` | Criar | Cobertura (documento completado na T163) |

## 4. Requisitos a implementar

Todos os requisitos (`RQ-T160-NN`) precisam estar atendidos para a task ser concluída.

- **RQ-T160-01** Confirmar que o build falha abaixo de 80% (testar localmente com limite artificialmente alto e desfazer).
- **RQ-T160-02** Revisar a cobertura de `RegraSolicitacao`, serviços de solicitação, `PainelService`, `RelatorioService` e `ExportadorCsv`; completar lacunas com os donos.
- **RQ-T160-03** Testes novos verificam comportamento, não apenas executam linhas.
- **RQ-T160-04** Web: gerar relatório de cobertura (texto e HTML), excluindo tipos gerados e mocks.
- **RQ-T160-05** App: idem com Jest.
- **RQ-T160-06** Publicar os três relatórios como artefatos e um resumo no `GITHUB_STEP_SUMMARY`.
- **RQ-T160-07** Sem exclusões novas no JaCoCo para subir o número.
- **RQ-T160-08** Registrar os números finais com o commit de referência.

## 5. Contrato, regras e códigos de erro

Especificação, seção 7.

## 6. Testes

`./mvnw verify`, `npm run test:cobertura` (web e app).

## 7. Critérios de aceite

- [ ] Backend com 80% ou mais e relatórios das três camadas publicados.

## 8. Cuidados e fora de escopo

- Cobertura de web e app é informativa: não criar bloqueio novo sem acordo.

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

> **T160 · Cobertura e relatórios de teste no CI.** Primeiro confira o JaCoCo do backend com a regra de 80% das linhas do código de negócio e complete as lacunas dos serviços novos junto com os donos. Depois publique também os relatórios de cobertura dos testes de componente da web e do app. Por fim, guarde os números finais para o relatório de testes.

### 9.5 Requisitos que atravessam esta task

- Regra 9.6: “As equipes devem implementar testes automatizados e apresentar evidências de cobertura.”
- Regra 9.7: “O projeto deve conter documentação de instalação e execução que permita a outra equipe assumir o desenvolvimento na Release 3.”
- Entregas: “Arquivos de configuração para instalar, executar e testar o sistema, **sem senhas ou credenciais reais**.”
- **Definition of Done** (texto completo na [seção 11 das convenções](00-convencoes-e-contrato-compartilhado.md#11-definition-of-done)): critérios de aceite demonstráveis com dados fictícios; revisão, CI e Pull Request; testes automatizados e 80% de cobertura do negócio no backend; erros com mensagens claras; Swagger e YAML atualizados; dados de demonstração, documentação, capturas e nota de bastão no repositório; RF01 a RF18 continuam funcionando.

---
Parte dos documentos de design da Sprint 4: [índice](README.md) · [convenções e contrato](00-convencoes-e-contrato-compartilhado.md).
