# T84 · Dados de demonstração e integração com a agenda

> **Sprint 3** · PBI-20 · RF13 Realização, ausência e cancelamento de visitas  
> **Responsável:** I3 · **Estimativa:** 2h · **Prazo:** Sex 13/11  
> **Depende de:** T76, T82  
> **Situação:** ⬜ A implementar (este documento é o guia para quem for fazer)

> Antes de começar, leia as [convenções e o contrato compartilhado da Sprint 3](00-convencoes-e-contrato-compartilhado.md): nomes, endpoints, DTOs, códigos de erro, tabelas e dados de demonstração valem para esta task. A origem dos requisitos (especificação, critério de aceite do PBI, regras de negócio e o texto original da task) está na [seção 9](#9-origem-dos-requisitos-especificação-e-planejamento).

## 1. Objetivo

Dar aos PBIs 21, 22 e 25 e ao E2E da T99 visitas em todas as situações (inclusive `AUSENTE` e `CANCELADA`), conferir que os desfechos aparecem na agenda e registrar os defeitos de integração.

## 2. Contexto: o que já existe e deve ser reaproveitado

- O estudo "Demo - Acompanhamento de participantes" e os participantes P1 a P3 foram criados pelo `R__demo_19_visitas_agendadas.sql` (T74); a seção 7 das convenções fixa ids, datas relativas e a tabela de situações.
- `DemoDataIntegrationTest` confere contagens e idempotência dos scripts do perfil `demo`.
- I6 é o dono do próximo PBI e precisa de visitas `REALIZADA` com atividades de categoria `AVALIACAO_CLINICA`.

## 3. Onde implementar

| Arquivo | Ação | Responsabilidade |
|---|---|---|
| `backend/ClinicalStudyPB/src/main/resources/db/demo/R__demo_20_desfechos.sql` | Criar | Participantes P4 e P5 e visitas em todas as situações |
| `backend/ClinicalStudyPB/src/test/java/UEPB/ESWII/ClinicalStudyPB/DemoDataIntegrationTest.java` | Alterar | Contagens e carga dupla do novo script |

## 4. Requisitos a implementar

Cada requisito tem um identificador (`RQ-T84-NN`) para a revisão do Pull Request e para a conferência no fim da task: todos precisam estar atendidos.

### Script `R__demo_20_desfechos.sql`

- **RQ-T84-01** Criar os participantes **P4** (CAND-004 "Pessoa Lambda (Ficticia)", inclusão D-35) e **P5** (CAND-005 "Pessoa Mi (Ficticia)", inclusão D-10) no estudo `...000000001901`, como candidatos `INCLUIDO` com avaliação aprovada, consentimento e participação (mesmo molde dos P1 a P3).
- **RQ-T84-02** Visitas de **P4**: Triagem `REALIZADA` (agendada e realizada em D-35); Retorno 30 `AUSENTE` (agendada em D-4, dentro da janela D-10 a D); Retorno 60 `CANCELADA` (agendada em D+25, motivo "Participante em viagem").
- **RQ-T84-03** Visitas de **P5**: Triagem `CANCELADA` (agendada em D-8, motivo "Consulta remarcada pelo centro"); Retorno 30 `AGENDADA` em D+20.
- **RQ-T84-04** Respeitar as constraints da T73: `REALIZADA` com `data_hora_realizada` e `CANCELADA` com `motivo`; `criado_por`/`alterado_por` do coordenador demo.
- **RQ-T84-05** Ids no padrão `d6300000-0000-4000-8000-0000000020NN`, datas relativas a `CURRENT_DATE`, `NOT EXISTS` em cada insert (idempotente) e sintaxe testada no H2 e no PostgreSQL.
- **RQ-T84-06** Com P1 a P5, o estudo (limite 6) fica com 5 de 6 vagas ocupadas; P6 entra no PBI-23.

### Integração com a agenda

- **RQ-T84-07** Conferir na agenda do estudo e na tela da visita que cada situação aparece com o selo certo e que as ações respeitam a situação (visita com desfecho sem botões de escrita).
- **RQ-T84-08** Conferir no agendamento do participante que as visitas com desfecho aparecem somente leitura.
- **RQ-T84-09** Avisar I6 (dono do PBI-21) de que os dados estão disponíveis e passar o que ele vai usar: `AcoesDaVisita`, a tela da visita e as visitas `REALIZADA` de P1 e P4.
- **RQ-T84-10** Registrar os defeitos de integração como **Bug** no Azure Boards, ligados à task de origem, e corrigir os da sua área.

### Testes dos dados

- **RQ-T84-11** Atualizar `DemoDataIntegrationTest`: contagem de participantes (5) e de visitas agendadas por situação depois de **duas** cargas.

## 6. Testes

- `DemoDataIntegrationTest` com a carga dupla e as contagens por situação.
- Subida do backend no perfil `demo` em H2 e PostgreSQL.

## 7. Critérios de aceite

- [ ] O perfil `demo` traz visitas em todas as situações, com os ids e as datas da seção 7 das convenções.
- [ ] O script é idempotente e roda em H2 e PostgreSQL.
- [ ] Os desfechos aparecem na agenda e na tela da visita; os defeitos estão registrados no Azure Boards.

## 8. Cuidados e fora de escopo

- Não alterar o estudo da T63 (`...000000000102`) nem os dados do script `R__demo_19`.
- Não usar datas fixas: o demo precisa continuar válido na data da apresentação.

## 9. Origem dos requisitos (especificação e planejamento)

Esta seção reúne, **sem paráfrase**, o que a especificação do projeto e o planejamento da Sprint 3 exigem para esta task, para que o documento sozinho mostre de onde vem cada requisito da seção 4. Onde o texto original difere das convenções (ajustes 1 a 10, [seção 1](00-convencoes-e-contrato-compartilhado.md#1-ajustes-em-relação-ao-planejamento-e-por-quê)), vale o que está neste documento e nas convenções.

### 9.1 Requisito funcional da especificação

- **RF13** (Módulo D — Acompanhamento; interface Web): “Registrar a realização, ausência ou cancelamento de visitas.”

### 9.2 Critério de aceite do PBI (planejamento)

> O coordenador registra o desfecho de uma visita agendada (realizada, ausente ou cancelada), com os dados exigidos por cada situação; desfechos são finais e não podem ser desfeitos; transições inválidas são recusadas com a regra violada.

### 9.3 Regras de negócio do planejamento que afetam esta task

| # | Regra proposta | Tasks afetadas (no planejamento) |
|---|---|---|
| 10 | Erros seguem o formato único da Sprint 2 (400 validação, 403 perfil sem permissão, 404 inexistente, 409 regra de negócio, sempre com o código da regra). As migrations usam faixas de versão por PBI (V3_19_* a V3_25_*); antes de começar, confirmar que a maior versão da Sprint 2 é menor que V3_19, senão trocar o prefixo V3 pelo número seguinte ao da última migration | T74 e todas as de backend |

### 9.4 Texto original da task no planejamento

> **T84 · Dados de demonstração e integração com a agenda.** Primeiro crie o script V3_20_1__demo_visit_outcomes.sql (perfil demo, repetível) com visitas em cada situação para os PBIs 21, 22 e 25 e para o E2E da T99. Depois confira na agenda e no histórico que os desfechos aparecem, e avise I6, dono do próximo PBI, que os dados estão disponíveis. Por fim, registre os defeitos de integração como Bugs ligados à task de origem.

### 9.5 Requisitos da especificação que atravessam esta task

- Regra 9.5 e Entregas: “O sistema deve ter persistência de dados e controle de acesso por perfil.” e “Persistência dos dados, com scripts de criação/migração do banco e dados fictícios para demonstração.”
- Entregas: arquivos de configuração para instalar, executar e testar “**sem senhas ou credenciais reais**” (os dados de demonstração são fictícios).
- **Definition of Done** da Sprint (texto completo na [seção 11 das convenções](00-convencoes-e-contrato-compartilhado.md#11-definition-of-done-e-requisitos-transversais-da-especificação)): os critérios de aceite estão atendidos e demonstráveis com dados fictícios; o código foi revisado, passou no CI e entrou na `main` por Pull Request; há testes automatizados e, no backend, a cobertura do código de negócio permanece em 80% ou mais; erros e validações são tratados com mensagens claras; Swagger e contrato YAML refletem os endpoints; os dados de demonstração estão no perfil `demo`; documentação e capturas estão no repositório; e RF01 a RF11 continuam funcionando.


---
Parte do conjunto de documentos de design da Sprint 3: [índice](README.md) · [convenções e contrato](00-convencoes-e-contrato-compartilhado.md).
