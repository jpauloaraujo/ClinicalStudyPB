# T157 · Dados de demonstração consolidados da Release 2

> **Sprint 4** · PBI-31 · Qualidade, regressão e preparação da Release 2  
> **Responsável:** I1 · **Estimativa:** 3h · **Prazo:** Sex 04/12  
> **Depende de:** T156  
> **Situação:** ⬜ A implementar (este documento é o guia para quem for fazer)

> Antes de começar, leia as [convenções e o contrato compartilhado da Sprint 4](00-convencoes-e-contrato-compartilhado.md): ajustes ao planejamento, endpoints, DTOs, códigos de erro, tabelas e dados de demonstração valem para esta task. A origem dos requisitos está na [seção 9](#9-origem-dos-requisitos-especificação-e-planejamento).

## 1. Objetivo

Garantir que os dados de demonstração das Sprints 2, 3 e 4 formam um cenário único, coerente e idempotente.

## 2. Contexto: o que já existe e deve ser reaproveitado

- Scripts em `db/demo/`: `R__dados_demonstracao`, `R__demo_19` a `R__demo_24`, `R__demo_26` e `R__demo_28`; Flyway roda repetíveis em ordem de descrição.
- `DemoDataIntegrationTest`, `DemoContaParticipanteTest`, `DemoSolicitacoesTest`.

## 3. Onde implementar

| Arquivo | Ação | Responsabilidade |
|---|---|---|
| `backend/ClinicalStudyPB/src/main/resources/db/demo/` | Alterar | Correções de conflito |
| `backend/ClinicalStudyPB/src/test/java/UEPB/ESWII/ClinicalStudyPB/DemoConsolidadoRelease2Test.java` | Criar | Cenário completo e idempotência |
| `README.md` | Alterar | Tabela de usuários fictícios |
| `scripts/verificar-ambiente.py` | Alterar | Conferir a carga, se necessário |

## 4. Requisitos a implementar

Todos os requisitos (`RQ-T157-NN`) precisam estar atendidos para a task ser concluída.

- **RQ-T157-01** Carregar em banco vazio com o perfil `demo`, no H2 e no PostgreSQL.
- **RQ-T157-02** Conferir: ao menos um estudo em cada um dos quatro estados; visitas em todas as situações (inclusive atrasada e não agendada); pedidos pendentes, atendidos e recusados; retiradas; contas de participante vinculadas.
- **RQ-T157-03** Se faltar um estado ou um pedido recusado, acrescentar no script da Sprint 4 correspondente.
- **RQ-T157-04** Executar cada script duas vezes sem duplicar nem restaurar dados alterados.
- **RQ-T157-05** Conferir ids sem colisão e relação um para um conta–candidato.
- **RQ-T157-06** Subir sobre um banco da Release 1 e da Sprint 3, sem erro de checksum.
- **RQ-T157-07** README: tabela de usuários por perfil, incluindo `demo.solicitante@clinicalstudy.test`, indicando que são fictícios e que as datas são relativas à carga.
- **RQ-T157-08** Documentar como recriar o banco.

## 5. Contrato, regras e códigos de erro

Seção 7 das convenções.

## 6. Testes

Teste consolidado e os testes de demonstração existentes.

## 7. Critérios de aceite

- [ ] Um banco limpo demonstra todos os requisitos; duas cargas não duplicam.

## 8. Cuidados e fora de escopo

- Mudar um script repetível o executa de novo em bancos existentes: só com `INSERT` protegido.
- Nenhuma credencial real.

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

> **T157 · Dados de demonstração consolidados da Release 2.** Primeiro rode juntos os scripts de demonstração das Sprints 2, 3 e 4 em um banco vazio e confira que formam um cenário coerente: estudos em cada estado, participantes com visitas em todas as situações, pedidos do participante e retiradas. Depois corrija conflitos e confirme que os scripts não duplicam dados se rodarem duas vezes. Por fim, atualize no README a tabela de usuários fictícios por perfil, sem nenhuma credencial real.

### 9.5 Requisitos que atravessam esta task

- Regra 9.6: “As equipes devem implementar testes automatizados e apresentar evidências de cobertura.”
- Regra 9.7: “O projeto deve conter documentação de instalação e execução que permita a outra equipe assumir o desenvolvimento na Release 3.”
- Entregas: “Arquivos de configuração para instalar, executar e testar o sistema, **sem senhas ou credenciais reais**.”
- **Definition of Done** (texto completo na [seção 11 das convenções](00-convencoes-e-contrato-compartilhado.md#11-definition-of-done)): critérios de aceite demonstráveis com dados fictícios; revisão, CI e Pull Request; testes automatizados e 80% de cobertura do negócio no backend; erros com mensagens claras; Swagger e YAML atualizados; dados de demonstração, documentação, capturas e nota de bastão no repositório; RF01 a RF18 continuam funcionando.

---
Parte dos documentos de design da Sprint 4: [índice](README.md) · [convenções e contrato](00-convencoes-e-contrato-compartilhado.md).
