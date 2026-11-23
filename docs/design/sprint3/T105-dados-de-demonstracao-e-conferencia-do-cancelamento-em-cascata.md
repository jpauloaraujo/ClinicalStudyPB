# T105 · Dados de demonstração e conferência do cancelamento em cascata

> **Sprint 3** · PBI-23 · RF16 Retirada de participante  
> **Responsável:** I4 · **Estimativa:** 1h · **Prazo:** Qui 19/11  
> **Depende de:** T76, T102  
> **Situação:** ⬜ A implementar (este documento é o guia para quem for fazer)

> Antes de começar, leia as [convenções e o contrato compartilhado da Sprint 3](00-convencoes-e-contrato-compartilhado.md): nomes, endpoints, DTOs, códigos de erro, tabelas e dados de demonstração valem para esta task. A origem dos requisitos (especificação, critério de aceite do PBI, regras de negócio e o texto original da task) está na [seção 9](#9-origem-dos-requisitos-especificação-e-planejamento).

## 1. Objetivo

Dar ao PBI-22, ao PBI-25 e ao E2E da T99 um participante retirado que tinha visitas futuras, e conferir na agenda e no histórico que as visitas aparecem canceladas com o motivo certo.

## 2. Contexto: o que já existe e deve ser reaproveitado

- O estudo "Demo - Acompanhamento de participantes" e os participantes P1 a P5 existem (T74 e T84); a seção 7 das convenções descreve **P6** (CAND-006 "Pessoa Ni (Ficticia)", inclusão D-15, retirado).
- O script roda **sem passar pelo serviço**: precisa gravar o mesmo estado que a retirada produz (participação `RETIRADO`, linha em `retiradas_participacao`, visita futura `CANCELADA` com o motivo "retirada do estudo").
- Com P6, o estudo (limite 6) fica com 6 de 6 vagas ocupadas: a retirada **não libera a vaga**.

## 3. Onde implementar

| Arquivo | Ação | Responsabilidade |
|---|---|---|
| `backend/ClinicalStudyPB/src/main/resources/db/demo/R__demo_23_retiradas.sql` | Criar | Participante P6 retirado com visitas |
| `backend/ClinicalStudyPB/src/test/java/UEPB/ESWII/ClinicalStudyPB/DemoDataIntegrationTest.java` | Alterar | Contagens e carga dupla |

## 4. Requisitos a implementar

Cada requisito tem um identificador (`RQ-T105-NN`) para a revisão do Pull Request e para a conferência no fim da task: todos precisam estar atendidos.

### Script `R__demo_23_retiradas.sql`

- **RQ-T105-01** Criar o **P6** (candidato `INCLUIDO` com avaliação aprovada e consentimento, inclusão em D-15) no estudo `...000000001901`, com `participacoes.situacao = 'RETIRADO'`.
- **RQ-T105-02** Gravar a retirada em `retiradas_participacao`: motivo "Mudança de cidade", solicitante `PARTICIPANTE`, data da retirada D-2, registrada pelo coordenador demo.
- **RQ-T105-03** Visitas de P6: Triagem `REALIZADA` (D-15) e Retorno 30 `CANCELADA` agendada em D+15 com o motivo **"retirada do estudo"**; Retorno 60 sem agendamento.
- **RQ-T105-04** Ids no padrão `d6300000-0000-4000-8000-0000000023NN`, datas relativas a `CURRENT_DATE`, `NOT EXISTS` em cada insert e sintaxe testada no H2 e no PostgreSQL.
- **RQ-T105-05** Atualizar `DemoDataIntegrationTest` (6 participantes, 1 retirada, contagem de visitas por situação) depois de **duas** cargas.

### Conferência do cancelamento em cascata

- **RQ-T105-06** Conferir na **agenda** e no **histórico** (ficha) de P6 que as visitas aparecem canceladas com o motivo "retirada do estudo", que a Triagem realizada permanece e que o estudo mostra 6 de 6 vagas.
- **RQ-T105-07** Conferir que o resultado de uma retirada real (feita pela interface em um participante novo) produz o mesmo estado que o script, e registrar qualquer diferença como Bug.
- **RQ-T105-08** Avisar I7 (PBI-24) e I5 (PBI-25) de que o participante retirado de demonstração está disponível: o vínculo de conta (T111) o usa para mostrar o estado retirado no app.

## 6. Testes

- `DemoDataIntegrationTest` com a carga dupla e as contagens.

## 7. Critérios de aceite

- [ ] O perfil `demo` traz um participante retirado com visita futura cancelada pelo motivo "retirada do estudo".
- [ ] A agenda e o histórico mostram o cancelamento em cascata e o estudo continua com 6 de 6 vagas.

## 8. Cuidados e fora de escopo

- Não alterar P1 a P5 nem o estudo da T63.
- O script deve produzir o mesmo estado que o serviço, para a demonstração não mentir.

## 9. Origem dos requisitos (especificação e planejamento)

Esta seção reúne, **sem paráfrase**, o que a especificação do projeto e o planejamento da Sprint 3 exigem para esta task, para que o documento sozinho mostre de onde vem cada requisito da seção 4. Onde o texto original difere das convenções (ajustes 1 a 10, [seção 1](00-convencoes-e-contrato-compartilhado.md#1-ajustes-em-relação-ao-planejamento-e-por-quê)), vale o que está neste documento e nas convenções.

### 9.1 Requisito funcional da especificação

- **RF16** (Módulo D — Acompanhamento; interface Web): “Registrar a retirada de um participante do estudo.”

### 9.2 Critério de aceite do PBI (planejamento)

> O coordenador registra a retirada de um participante ativo com motivo, quem solicitou e data; o participante passa a RETIRADO de forma irreversível, as visitas futuras agendadas são canceladas com o motivo "retirada do estudo" na mesma operação, e o histórico fica preservado.

### 9.3 Regras de negócio do planejamento que afetam esta task

| # | Regra proposta | Tasks afetadas (no planejamento) |
|---|---|---|
| 10 | Erros seguem o formato único da Sprint 2 (400 validação, 403 perfil sem permissão, 404 inexistente, 409 regra de negócio, sempre com o código da regra). As migrations usam faixas de versão por PBI (V3_19_* a V3_25_*); antes de começar, confirmar que a maior versão da Sprint 2 é menor que V3_19, senão trocar o prefixo V3 pelo número seguinte ao da última migration | T74 e todas as de backend |

### 9.4 Texto original da task no planejamento

> **T105 · Dados de demonstração e conferência do cancelamento em cascata.** Primeiro crie o script V3_23_2__demo_withdrawals.sql (perfil demo, repetível) com um participante retirado que tinha visitas futuras, para o PBI-22, o PBI-25 e o E2E da T99. Depois confira na agenda e no histórico que as visitas aparecem canceladas com o motivo certo.

### 9.5 Requisitos da especificação que atravessam esta task

- Regra 9.5 e Entregas: “O sistema deve ter persistência de dados e controle de acesso por perfil.” e “Persistência dos dados, com scripts de criação/migração do banco e dados fictícios para demonstração.”
- Entregas: arquivos de configuração para instalar, executar e testar “**sem senhas ou credenciais reais**” (os dados de demonstração são fictícios).
- **Definition of Done** da Sprint (texto completo na [seção 11 das convenções](00-convencoes-e-contrato-compartilhado.md#11-definition-of-done-e-requisitos-transversais-da-especificação)): os critérios de aceite estão atendidos e demonstráveis com dados fictícios; o código foi revisado, passou no CI e entrou na `main` por Pull Request; há testes automatizados e, no backend, a cobertura do código de negócio permanece em 80% ou mais; erros e validações são tratados com mensagens claras; Swagger e contrato YAML refletem os endpoints; os dados de demonstração estão no perfil `demo`; documentação e capturas estão no repositório; e RF01 a RF11 continuam funcionando.


---
Parte do conjunto de documentos de design da Sprint 3: [índice](README.md) · [convenções e contrato](00-convencoes-e-contrato-compartilhado.md).
