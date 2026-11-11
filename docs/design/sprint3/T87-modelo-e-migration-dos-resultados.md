# T87 · Modelo e migration dos resultados

> **Sprint 3** · PBI-21 · RF14 Resultados fictícios das avaliações previstas  
> **Responsável:** I6 · **Estimativa:** 3h · **Prazo:** Sáb 14/11  
> **Depende de:** T73  
> **Situação:** ⬜ A implementar (este documento é o guia para quem for fazer)

> Antes de começar, leia as [convenções e o contrato compartilhado da Sprint 3](00-convencoes-e-contrato-compartilhado.md): nomes, endpoints, DTOs, códigos de erro, tabelas e dados de demonstração valem para esta task. A origem dos requisitos (especificação, critério de aceite do PBI, regras de negócio e o texto original da task) está na [seção 9](#9-origem-dos-requisitos-especificação-e-planejamento).

## 1. Objetivo

Criar a tabela, a entidade e o repositório do **resultado de uma atividade de avaliação** de uma visita realizada: um resultado por atividade e por visita agendada, com autor e data. Os resultados são fictícios; nenhum dado real de pessoa é armazenado.

## 2. Contexto: o que já existe e deve ser reaproveitado

- `Acao` (`acoes`) tem `categoria` (`CategoriaAcao`: `AVALIACAO_CLINICA`, `EXAME_LABORATORIAL`, `EXAME_IMAGEM`, `QUESTIONARIO`, `MEDICACAO`, `OUTROS`). A regra 4 vale para a categoria `AVALIACAO_CLINICA` (ajuste 4 das convenções).
- `VisitaAgendada` e a migration `V6_19_1` vêm da T73; esta task usa a faixa `V6_21_*`.
- Estilo das entidades e das constraints: o mesmo da T73 e da `V4`. A migration não pode alterar nada da Sprint 2.
- Colunas, constraints e nomes: seção 5 das convenções.

## 3. Onde implementar

| Arquivo | Ação | Responsabilidade |
|---|---|---|
| `backend/ClinicalStudyPB/src/main/resources/db/migration/V6_21_1__create_evaluation_results.sql` | Criar | Tabela `resultados_atividade` |
| `backend/ClinicalStudyPB/src/main/java/UEPB/ESWII/ClinicalStudyPB/domain/model/ResultadoAtividade.java` | Criar | Entidade JPA |
| `backend/ClinicalStudyPB/src/main/java/UEPB/ESWII/ClinicalStudyPB/repository/ResultadoAtividadeRepository.java` | Criar | Consultas por visita agendada e por atividade |
| `backend/ClinicalStudyPB/src/test/java/UEPB/ESWII/ClinicalStudyPB/FlywayMigrationIntegrationTest.java` | Alterar | Tabela, `UNIQUE` e chaves estrangeiras |
| `docs/diagramas/dominio.md` | Alterar | Acrescenta `ResultadoAtividade` |

## 4. Requisitos a implementar

Cada requisito tem um identificador (`RQ-T87-NN`) para a revisão do Pull Request e para a conferência no fim da task: todos precisam estar atendidos.

### Migration `V6_21_1__create_evaluation_results.sql`

- **RQ-T87-01** Tabela `resultados_atividade`: `id UUID` (PK), `visita_agendada_id UUID NOT NULL` (FK para `visitas_agendadas`), `acao_id UUID NOT NULL` (FK para `acoes` com `ON DELETE RESTRICT`), `valor VARCHAR(200) NOT NULL`, `unidade VARCHAR(30)`, `observacao VARCHAR(500)`, `registrado_por_id UUID NOT NULL`, `registrado_em TIMESTAMP NOT NULL`, `alterado_por_id UUID NOT NULL`, `alterado_em TIMESTAMP NOT NULL` (os dois `*_por_id` são FKs para `usuarios`).
- **RQ-T87-02** `UNIQUE (visita_agendada_id, acao_id)` (`uk_resultados_atividade_visita_acao`): **um resultado por atividade** e por visita agendada.
- **RQ-T87-03** Índice em `visita_agendada_id`; `CREATE TABLE IF NOT EXISTS`; tipos compatíveis com H2 (modo PostgreSQL) e PostgreSQL 16; nenhuma `ON DELETE CASCADE`.

### Entidade e repositório

- **RQ-T87-04** `ResultadoAtividade` com `visitaAgendada` e `acao` (`@ManyToOne LAZY`, obrigatórios), `valor`, `unidade`, `observacao`, `registradoPor`, `registradoEm`, `alteradoPor`, `alteradoEm`; Lombok e `@JsonIgnore` nas associações, como `VisitaAgendada`.
- **RQ-T87-05** `ResultadoAtividadeRepository` com `findByVisitaAgendadaId`, `findByVisitaAgendadaIdAndAcaoId` e `countByVisitaAgendadaId`.

### Verificação

- **RQ-T87-06** Subir o backend em **banco vazio** e em **banco populado** (perfil `demo`), no H2 e no PostgreSQL; `ddl-auto=validate` passa.
- **RQ-T87-07** Atualizar `FlywayMigrationIntegrationTest`: a tabela existe, o `UNIQUE` recusa o segundo resultado da mesma atividade na mesma visita e as FKs recusam referências inexistentes.
- **RQ-T87-08** Atualizar `docs/diagramas/dominio.md` com `ResultadoAtividade` e a relação com `VisitaAgendada` e `Acao`.
- **RQ-T87-09** Documentar no código (Javadoc) e na descrição do Pull Request que os resultados são **fictícios** (LGPD): nenhum dado real é armazenado.

## 5. Contrato, regras e códigos de erro

- Sem endpoints. A regra 4 (só visita `REALIZADA`, só `AVALIACAO_CLINICA`) é do serviço (T89); aqui o banco só garante a unicidade e as referências.

## 6. Testes

- `FlywayMigrationIntegrationTest` (tabela, `UNIQUE`, FKs).
- `ResultadoAtividadeRepositoryTest`: salvar, buscar por visita e por atividade e violar o `UNIQUE`.

## 7. Critérios de aceite

- [ ] A migration roda em banco vazio e em banco populado, no H2 e no PostgreSQL.
- [ ] A entidade passa na validação do Hibernate e o `UNIQUE` está provado por teste.
- [ ] O diagrama de domínio inclui o resultado.

## 8. Cuidados e fora de escopo

- Não criar serviço, DTO nem endpoint aqui (T88 a T90).
- O valor é texto curto livre; não modelar tipos numéricos nem faixas de referência.

## 9. Origem dos requisitos (especificação e planejamento)

Esta seção reúne, **sem paráfrase**, o que a especificação do projeto e o planejamento da Sprint 3 exigem para esta task, para que o documento sozinho mostre de onde vem cada requisito da seção 4. Onde o texto original difere das convenções (ajustes 1 a 10, [seção 1](00-convencoes-e-contrato-compartilhado.md#1-ajustes-em-relação-ao-planejamento-e-por-quê)), vale o que está neste documento e nas convenções.

### 9.1 Requisito funcional da especificação

- **RF14** (Módulo D — Acompanhamento; interface Web): “Registrar resultados fictícios das avaliações previstas.”

### 9.2 Critério de aceite do PBI (planejamento)

> O pesquisador registra e edita o resultado de cada atividade da categoria avaliação de uma visita REALIZADA; visitas não realizadas, atividades de outras categorias e estudos encerrados são recusados; o coordenador consulta os resultados em modo somente leitura.

### 9.3 Regras de negócio do planejamento que afetam esta task

| # | Regra proposta | Tasks afetadas (no planejamento) |
|---|---|---|
| 4 | Resultados fictícios só existem para visita REALIZADA e só para atividades da categoria avaliação. Quem registra é o pesquisador, com um resultado por atividade, editável enquanto o estudo não estiver encerrado, guardando autor e data da última alteração | T87, T89, T91 |
| 10 | Erros seguem o formato único da Sprint 2 (400 validação, 403 perfil sem permissão, 404 inexistente, 409 regra de negócio, sempre com o código da regra). As migrations usam faixas de versão por PBI (V3_19_* a V3_25_*); antes de começar, confirmar que a maior versão da Sprint 2 é menor que V3_19, senão trocar o prefixo V3 pelo número seguinte ao da última migration | T74 e todas as de backend |

### 9.4 Texto original da task no planejamento

> **T87 · Modelo e migration dos resultados.** Primeiro defina a entidade de resultado com a visita agendada, a atividade prevista, o valor (texto curto obrigatório), a unidade e a observação (opcionais), o usuário e a data do registro e da última alteração. Depois escreva a migration V3_21_1__evaluation_result.sql com as chaves estrangeiras e o UNIQUE de (visita agendada, atividade), que garante um resultado por atividade, e as entidades JPA com o repositório. Por fim, suba o backend em banco vazio e em banco populado. Os resultados são fictícios; nenhum dado real de pessoa é armazenado.

### 9.5 Requisitos da especificação que atravessam esta task

- Regra 9.5 e Entregas: “O sistema deve ter persistência de dados e controle de acesso por perfil.” e “Persistência dos dados, com scripts de criação/migração do banco e dados fictícios para demonstração.”
- **Definition of Done** da Sprint (texto completo na [seção 11 das convenções](00-convencoes-e-contrato-compartilhado.md#11-definition-of-done-e-requisitos-transversais-da-especificação)): os critérios de aceite estão atendidos e demonstráveis com dados fictícios; o código foi revisado, passou no CI e entrou na `main` por Pull Request; há testes automatizados e, no backend, a cobertura do código de negócio permanece em 80% ou mais; erros e validações são tratados com mensagens claras; Swagger e contrato YAML refletem os endpoints; os dados de demonstração estão no perfil `demo`; documentação e capturas estão no repositório; e RF01 a RF11 continuam funcionando.


---
Parte do conjunto de documentos de design da Sprint 3: [índice](README.md) · [convenções e contrato](00-convencoes-e-contrato-compartilhado.md).
