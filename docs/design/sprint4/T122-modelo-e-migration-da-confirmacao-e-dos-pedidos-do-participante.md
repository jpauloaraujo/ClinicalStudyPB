# T122 · Modelo e migration da confirmação e dos pedidos do participante

> **Sprint 4** · PBI-26 · RF19 Confirmação de presença e pedido de reagendamento no app  
> **Responsável:** I7 · **Estimativa:** 3h · **Prazo:** Ter 24/11  
> **Depende de:** T164, Sprint 3 (T73, T109)  
> **Situação:** ⬜ A implementar (este documento é o guia para quem for fazer)

> Antes de começar, leia as [convenções e o contrato compartilhado da Sprint 4](00-convencoes-e-contrato-compartilhado.md): ajustes ao planejamento, endpoints, DTOs, códigos de erro, tabelas e dados de demonstração valem para esta task. A origem dos requisitos está na [seção 9](#9-origem-dos-requisitos-especificação-e-planejamento).

## 1. Objetivo

Criar a base de dados do PBI-26 e dos PBIs 27 e 28: a data de confirmação de presença na visita agendada e a solicitação do participante (tipos `REAGENDAMENTO` e `RETIRADA`), com migration, entidade e repositório.

## 2. Contexto: o que já existe e deve ser reaproveitado

- `VisitaAgendada` (`visitas_agendadas`, `V6_19_1`) tem `@Version`, auditoria (`criadoPor`, `alteradoPor`) e `estaAgendada()`.
- `Participacao` tem `situacao` (`ATIVO`/`RETIRADO`) e `estaRetirada()`; `RetiradaParticipacao` (`V6_23_1`) é o modelo de entidade com FKs `ON DELETE RESTRICT`.
- Flyway com `validate-on-migrate=true`; `MigracaoSeguraTest` e `FlywayMigrationIntegrationTest` mostram como testar migration sobre banco populado.
- Locks existentes: `VisitaAgendadaRepository.buscarComBloqueio` e `ParticipacaoRepository.buscarComBloqueio`.

## 3. Onde implementar

| Arquivo | Ação | Responsabilidade |
|---|---|---|
| `backend/ClinicalStudyPB/src/main/resources/db/migration/V7_26_1__participant_requests.sql` | Criar | Coluna de confirmação e tabela de solicitações |
| `backend/ClinicalStudyPB/src/main/java/UEPB/ESWII/ClinicalStudyPB/domain/enums/TipoSolicitacao.java` | Criar | `REAGENDAMENTO`, `RETIRADA` |
| `backend/ClinicalStudyPB/src/main/java/UEPB/ESWII/ClinicalStudyPB/domain/enums/SituacaoSolicitacao.java` | Criar | `PENDENTE`, `ATENDIDA`, `RECUSADA` |
| `backend/ClinicalStudyPB/src/main/java/UEPB/ESWII/ClinicalStudyPB/domain/model/SolicitacaoParticipante.java` | Criar | Entidade JPA |
| `backend/ClinicalStudyPB/src/main/java/UEPB/ESWII/ClinicalStudyPB/domain/model/VisitaAgendada.java` | Alterar | Campo `presencaConfirmadaEm`, `confirmarPresenca`, `limparConfirmacao` |
| `backend/ClinicalStudyPB/src/main/java/UEPB/ESWII/ClinicalStudyPB/repository/SolicitacaoParticipanteRepository.java` | Criar | Consultas e bloqueio |
| `backend/ClinicalStudyPB/src/test/java/UEPB/ESWII/ClinicalStudyPB/SolicitacaoParticipanteRepositoryTest.java` | Criar | Mapeamento e restrições |
| `backend/ClinicalStudyPB/src/test/java/UEPB/ESWII/ClinicalStudyPB/MigracaoSeguraTest.java` | Alterar | `V7_26_1` sobre banco da Sprint 3 |
| `docs/diagramas/dominio.md` | Alterar | Seção da solicitação do participante |

## 4. Requisitos a implementar

Todos os requisitos (`RQ-T122-NN`) precisam estar atendidos para a task ser concluída.

### Migration
- **RQ-T122-01** Criar `V7_26_1__participant_requests.sql` (não `V4_26_1`: ajuste 1) com `ALTER TABLE visitas_agendadas ADD COLUMN IF NOT EXISTS presenca_confirmada_em TIMESTAMP` (nula por padrão).
- **RQ-T122-02** Criar `CREATE TABLE IF NOT EXISTS solicitacoes_participante` com as colunas da seção 5 das convenções; `id`, `participacao_id`, `tipo`, `situacao`, `criado_em` e `versao` são `NOT NULL`; `situacao` tem padrão `'PENDENTE'` e `versao`, `0`.
- **RQ-T122-03** Nomear as constraints: `pk_solicitacoes_participante`, `fk_solicitacoes_participacao`, `fk_solicitacoes_visita_agendada`, `fk_solicitacoes_respondido_por`, todas `ON DELETE RESTRICT`.
- **RQ-T122-04** `CHECK chk_solicitacoes_tipo` e `chk_solicitacoes_situacao` com os valores dos enums.
- **RQ-T122-05** `CHECK chk_solicitacoes_campos_por_tipo`: `REAGENDAMENTO` exige `visita_agendada_id` e `motivo`; `RETIRADA` exige `visita_agendada_id` nulo e `data_preferida` nula.
- **RQ-T122-06** `CHECK chk_solicitacoes_resposta`: `PENDENTE` tem `respondido_em` nulo; as demais o exigem; `RECUSADA` exige `resposta`; `RETIRADA` nunca é `RECUSADA` (regra 5).
- **RQ-T122-07** Índices: `idx_solicitacoes_participacao` (`participacao_id`), `idx_solicitacoes_visita` (`visita_agendada_id`) e `idx_solicitacoes_situacao_tipo` (`situacao, tipo`).
### Entidade e repositório
- **RQ-T122-08** `SolicitacaoParticipante` com `@ManyToOne(fetch = LAZY)` para participação, visita agendada (opcional) e `respondidoPor` (opcional), `@Enumerated(STRING)`, `@Version` e `@JsonIgnore` nas relações, no estilo de `RetiradaParticipacao`.
- **RQ-T122-09** Métodos de domínio: `pendente()`, `atender(Usuario, LocalDateTime)` e `recusar(String resposta, Usuario, LocalDateTime)`; os dois últimos só mudam uma solicitação pendente (`IllegalStateException` nos outros casos; o serviço verifica antes e lança o 409).
- **RQ-T122-10** Em `VisitaAgendada`: `presencaConfirmadaEm` (`presenca_confirmada_em`), `confirmarPresenca(LocalDateTime)`, `limparConfirmacao()` e `presencaConfirmada()`. A confirmação não muda `situacao` (regra 1).
- **RQ-T122-11** `SolicitacaoParticipanteRepository`: `existsByVisitaAgendadaIdAndSituacao`, `existsByParticipacaoIdAndTipoAndSituacao`, `findByVisitaAgendadaIdAndSituacao`, `findByParticipacaoIdAndSituacao`, `listarDasParticipacoes(ids)` (mais recentes primeiro, com `join fetch` de visita, visita prevista, participação e estudo) e `buscarComBloqueio(id)`.
- **RQ-T122-12** Subir o backend em banco populado pela Sprint 3 (`docker compose up --build` sobre um volume criado na `develop` anterior) e registrar no Pull Request que a `V7_26_1` foi aplicada sem perda de dados.

## 5. Contrato, regras e códigos de erro

Tabelas da seção 5 e nomes da seção 6 das convenções. Esta task não cria endpoint nem código de erro.

## 6. Testes

- Repositório: salvar os dois tipos; cada `CHECK` recusa a combinação inválida (`DataIntegrityViolationException`); consultas por visita, participação e situação.
- Migration: banco migrado até `6.24.1` com dados, depois até a última versão, mantendo as contagens; coluna nova nula.
- Rodar no job `Backend (testes no PostgreSQL)`.

## 7. Critérios de aceite

- [ ] A `V7_26_1` aplica em banco vazio e em banco populado, no H2 e no PostgreSQL.
- [ ] A tabela já aceita `RETIRADA`, sem nova migration no PBI-28.
- [ ] `ddl-auto=validate` passa com a entidade nova.

## 8. Cuidados e fora de escopo

- Não criar índice único parcial (`WHERE situacao = 'PENDENTE'`): o H2 não suporta. A unicidade é garantida por bloqueio no serviço (T124).
- Nunca editar migrations já aplicadas.

## 9. Origem dos requisitos (especificação e planejamento)

Texto **sem paráfrase** da especificação e do planejamento. Onde ele diverge dos [ajustes da seção 1 das convenções](00-convencoes-e-contrato-compartilhado.md#1-ajustes-em-relação-ao-planejamento-e-por-quê), valem as convenções e este documento.

### 9.1 Requisito da especificação

- **RF19** (Módulo E, Aplicativo do participante; interface Mobile): “Permitir confirmar presença prevista ou solicitar reagendamento de visita.”

### 9.2 Critério de aceite do PBI (planejamento)

> no detalhe de uma visita agendada, o participante confirma presença ou pede reagendamento com motivo; o app mostra a situação de cada pedido (pendente, atendido ou recusado, com a resposta); pedidos inválidos são recusados com a regra violada. Este PBI cria a tabela de pedidos do participante que os PBIs 27 e 28 usam.

### 9.3 Regras de negócio do planejamento que afetam esta task

| # | Regra proposta | Tasks afetadas |
|---|---|---|
| 1 | O participante confirma presença apenas em visita AGENDADA, com data futura, participação ativa e sem pedido de retirada pendente. A confirmação é única (uma segunda dá 409), guarda data e hora e não muda a situação da visita | T122, T124, T126 |
| 2 | O pedido de reagendamento vale para visita AGENDADA e futura, exige motivo (ao menos 5 caracteres) e aceita uma preferência de data opcional. Cada visita tem no máximo um pedido PENDENTE, e a visita só muda quando o coordenador atende | T122, T124, T126 |
| 8 | Erros seguem o formato único da Sprint 2 (400, 403, 404 e 409, sempre com o código da regra). As migrations usam as faixas V4_26_\* a V4_32_\*, todas acima das faixas V3 da Sprint 3 | T122 e todas as de backend |

### 9.4 Texto original da task no planejamento

> **T122 · Modelo e migration da confirmação e dos pedidos do participante.** Primeiro acrescente à visita agendada a data e hora da confirmação de presença, vazia por padrão. Depois crie a entidade ParticipantRequest com participação, tipo (REAGENDAMENTO ou RETIRADA), visita agendada (só no reagendamento), motivo, preferência de data, situação (PENDENTE, ATENDIDA ou RECUSADA), resposta, quem respondeu e as datas de criação e de resposta. Em seguida escreva a migration V4_26_1__participant_requests.sql com as chaves estrangeiras e os CHECK de tipo e situação, e crie a entidade JPA e o repositório. Por fim, suba o backend em banco populado pela Sprint 3. O tipo RETIRADA já entra agora para o PBI-28 não precisar de outra tabela.

### 9.5 Requisitos que atravessam esta task

- Regra 9.2: “O backend deve disponibilizar uma **API HTTP REST** compartilhada pela web e pelo mobile, documentada no padrão **OpenAPI**”.
- Entregas: “Persistência dos dados, com scripts de criação/migração do banco e dados fictícios para demonstração” e “Tratamento de erros e validações das regras de negócio”.
- Testes (seção 7): “Testes unitários para todas as regras de negócio relevantes”, “Testes de integração para os principais endpoints” e “Execução automatizada dos testes a cada pull request”.
- Regra 9.4: “A interface **mobile** atende ao perfil de participante e executa em dispositivo ou emulador.”
- **Definition of Done** (texto completo na [seção 11 das convenções](00-convencoes-e-contrato-compartilhado.md#11-definition-of-done)): critérios de aceite demonstráveis com dados fictícios; revisão, CI e Pull Request; testes automatizados e 80% de cobertura do negócio no backend; erros com mensagens claras; Swagger e YAML atualizados; dados de demonstração, documentação, capturas e nota de bastão no repositório; RF01 a RF18 continuam funcionando.

---
Parte dos documentos de design da Sprint 4: [índice](README.md) · [convenções e contrato](00-convencoes-e-contrato-compartilhado.md).
