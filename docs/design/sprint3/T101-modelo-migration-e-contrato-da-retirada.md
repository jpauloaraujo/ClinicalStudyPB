# T101 · Modelo, migration e contrato da retirada

> **Sprint 3** · PBI-23 · RF16 Retirada de participante  
> **Responsável:** I4 · **Estimativa:** 3h · **Prazo:** Qua 18/11  
> **Depende de:** T73  
> **Situação:** ⬜ A implementar (este documento é o guia para quem for fazer)

> Antes de começar, leia as [convenções e o contrato compartilhado da Sprint 3](00-convencoes-e-contrato-compartilhado.md): nomes, endpoints, DTOs, códigos de erro, tabelas e dados de demonstração valem para esta task. A origem dos requisitos (especificação, critério de aceite do PBI, regras de negócio e o texto original da task) está na [seção 9](#9-origem-dos-requisitos-especificação-e-planejamento).

## 1. Objetivo

Criar a tabela de retirada e a coluna de situação da participação (`ATIVO` ou `RETIRADO`), as entidades JPA e o contrato do endpoint de retirada. Quando a coluna de situação entrar na `main`, avisar o grupo: a T102 a usa para bloquear ações nos serviços dos PBIs 19, 20 e 21.

## 2. Contexto: o que já existe e deve ser reaproveitado

- `Participacao` (`participacoes`) guarda `candidato`, `estudo`, `dataInclusao` e `incluidoPor`; `ParticipacaoResponse` é o DTO da lista de participantes (Sprint 2).
- Os participantes incluídos na Sprint 2 e nas Sprints anteriores existem em bancos já populados: a coluna nova precisa ter **padrão `ATIVO`** para eles.
- Esta task usa a faixa `V6_23_*`; as colunas e nomes estão na seção 5 das convenções. O formato de `RetiradaRequest` e `RetiradaResponse`, na seção 3.
- O pedido de retirada feito **pelo próprio participante no app (RF20)** é da Sprint 4 e vai reaproveitar o mesmo serviço; por isso o campo `solicitante` aceita `PARTICIPANTE`.

## 3. Onde implementar

| Arquivo | Ação | Responsabilidade |
|---|---|---|
| `backend/ClinicalStudyPB/src/main/resources/db/migration/V6_23_1__participant_withdrawal.sql` | Criar | Coluna `participacoes.situacao` e tabela `retiradas_participacao` |
| `backend/ClinicalStudyPB/src/main/java/UEPB/ESWII/ClinicalStudyPB/domain/enums/SituacaoParticipacao.java` | Criar | `ATIVO`, `RETIRADO` |
| `backend/ClinicalStudyPB/src/main/java/UEPB/ESWII/ClinicalStudyPB/domain/enums/SolicitanteRetirada.java` | Criar | `PARTICIPANTE`, `PESQUISADOR`, `COORDENACAO` |
| `backend/ClinicalStudyPB/src/main/java/UEPB/ESWII/ClinicalStudyPB/domain/model/Participacao.java` | Alterar | Campo `situacao` (padrão `ATIVO`) e `estaRetirada()` |
| `backend/ClinicalStudyPB/src/main/java/UEPB/ESWII/ClinicalStudyPB/domain/model/RetiradaParticipacao.java` | Criar | Entidade JPA da retirada |
| `backend/ClinicalStudyPB/src/main/java/UEPB/ESWII/ClinicalStudyPB/repository/RetiradaParticipacaoRepository.java` | Criar | `findByParticipacaoId` e `existsByParticipacaoId` |
| `backend/ClinicalStudyPB/src/main/java/UEPB/ESWII/ClinicalStudyPB/controller/dto/ParticipacaoResponse.java` | Alterar | Acrescenta `situacao` |
| `docs/api/openapi-sprint2.yaml` | Alterar | Schema `ParticipacaoResponse` ganha o campo `situacao` (só o schema; os caminhos não mudam) |
| `docs/api/openapi-sprint3.yaml` | Alterar | Operações da retirada, schemas e erros |
| `backend/ClinicalStudyPB/src/test/java/UEPB/ESWII/ClinicalStudyPB/FlywayMigrationIntegrationTest.java` | Alterar | Coluna, `CHECK`, tabela e `UNIQUE` |
| `docs/diagramas/dominio.md` | Alterar | Situação da participação e `RetiradaParticipacao` |

## 4. Requisitos a implementar

Cada requisito tem um identificador (`RQ-T101-NN`) para a revisão do Pull Request e para a conferência no fim da task: todos precisam estar atendidos.

### Migration `V6_23_1__participant_withdrawal.sql`

- **RQ-T101-01** Acrescentar a `participacoes` a coluna `situacao VARCHAR(20) NOT NULL DEFAULT 'ATIVO'` com `CHECK` `chk_participacoes_situacao` (`situacao IN ('ATIVO', 'RETIRADO')`). Em bancos populados, **todos os participantes existentes ficam `ATIVO`** (o padrão cobre isso; conferir por teste).
- **RQ-T101-02** Criar a tabela `retiradas_participacao`: `id UUID` (PK), `participacao_id UUID NOT NULL UNIQUE` (FK para `participacoes`; `uk_retiradas_participacao`), `motivo VARCHAR(500) NOT NULL`, `solicitante VARCHAR(20) NOT NULL` com `CHECK` em `PARTICIPANTE`, `PESQUISADOR`, `COORDENACAO`, `data_retirada DATE NOT NULL`, `registrado_por_id UUID NOT NULL` (FK para `usuarios`) e `registrado_em TIMESTAMP NOT NULL`.
- **RQ-T101-03** Usar `ALTER TABLE ... ADD COLUMN IF NOT EXISTS` (ou o equivalente compatível com o H2 em modo PostgreSQL e o PostgreSQL 16) e `CREATE TABLE IF NOT EXISTS`; sem `ON DELETE CASCADE`; a migration não apaga nem altera dados existentes.

### Enums e entidades

- **RQ-T101-04** `SituacaoParticipacao` e `SolicitanteRetirada` em `domain.enums`, com Javadoc dizendo que `RETIRADO` é **irreversível**.
- **RQ-T101-05** `Participacao` ganha `situacao` (`@Enumerated(EnumType.STRING)`, padrão `ATIVO`) e `estaRetirada()`; nenhum setter público deve permitir voltar de `RETIRADO` para `ATIVO` (a regra é do serviço, mas o método de mudança deve se chamar `retirar()` e não existir `reativar()`).
- **RQ-T101-06** `RetiradaParticipacao` com `participacao` (`@OneToOne LAZY`, único), `motivo`, `solicitante`, `dataRetirada`, `registradoPor` e `registradoEm`.
- **RQ-T101-07** `ParticipacaoResponse` ganha `situacao` (`ATIVO`/`RETIRADO`); o campo é aditivo, então clientes antigos continuam funcionando. Atualizar também o schema `ParticipacaoResponse` em `openapi-sprint2.yaml` (só o schema).

### Contrato (`openapi-sprint3.yaml`)

- **RQ-T101-08** Descrever `POST /api/participacoes/{participacaoId}/retirada` (só coordenador; 201 `RetiradaResponse`) e `GET /api/participacoes/{participacaoId}/retirada` (leitura; 200 `RetiradaResponse`; 404 se o participante não foi retirado).
- **RQ-T101-09** Schemas: `RetiradaRequest` (`motivo` 5 a 500 caracteres, `solicitante` com o enum, `dataRetirada` não futura), `RetiradaResponse` (com `visitasCanceladas`), enums `SituacaoParticipacao` e `SolicitanteRetirada`, com os exemplos fictícios das convenções.
- **RQ-T101-10** Documentar os erros: `400` (`VALIDACAO_CAMPOS`, `CORPO_INVALIDO`, `MOTIVO_OBRIGATORIO`, `DATA_RETIRADA_FUTURA`), `403`, `404` e `409` (`PARTICIPANTE_JA_RETIRADO`, `ESTUDO_ENCERRADO`); explicar o efeito (as visitas futuras `AGENDADA` viram `CANCELADA` com o motivo "retirada do estudo"; a vaga continua ocupada).
- **RQ-T101-11** Atualizar a contagem do `ContratoOpenApiSprint3Test` (10 + 2 = 12 operações) com o caso `@Disabled("T103")`.

### Verificação e aviso

- **RQ-T101-12** Subir o backend em **banco vazio** e em **banco populado pela Sprint 2** (perfil `demo`), no H2 e no PostgreSQL; conferir que todos os participantes existentes ficam `ATIVO`.
- **RQ-T101-13** Atualizar `FlywayMigrationIntegrationTest`: a coluna e o `CHECK`, a tabela, o `UNIQUE` (a segunda retirada da mesma participação é recusada) e o `CHECK` do solicitante.
- **RQ-T101-14** Atualizar `docs/diagramas/dominio.md`.
- **RQ-T101-15** **Avisar o grupo** quando a coluna de situação entrar na `main`: a T102 a usa nos PBIs 19, 20 e 21.

## 5. Contrato, regras e códigos de erro

- Operações 11 e 12 da seção 2 das convenções; códigos `PARTICIPANTE_JA_RETIRADO`, `ESTUDO_ENCERRADO`, `DATA_RETIRADA_FUTURA` e `MOTIVO_OBRIGATORIO`.

## 6. Testes

- `FlywayMigrationIntegrationTest` (coluna, `CHECK`, tabela, `UNIQUE`) e `RetiradaParticipacaoRepositoryTest`.
- Teste de que participantes já existentes ficam `ATIVO` depois da migração (banco populado) e de que o `ParticipacaoResponse` traz `situacao`.

## 7. Critérios de aceite

- [ ] A migration roda em banco vazio e em banco populado, no H2 e no PostgreSQL, e os participantes existentes ficam `ATIVO`.
- [ ] As entidades passam na validação do Hibernate e o `UNIQUE` da retirada está provado por teste.
- [ ] O contrato descreve o endpoint, os DTOs e os códigos de erro; o grupo foi avisado.

## 8. Cuidados e fora de escopo

- Não implementar regras nem endpoint aqui (T102 e T103).
- Não adicionar opção de reativar: a retirada é irreversível.

## 9. Origem dos requisitos (especificação e planejamento)

Esta seção reúne, **sem paráfrase**, o que a especificação do projeto e o planejamento da Sprint 3 exigem para esta task, para que o documento sozinho mostre de onde vem cada requisito da seção 4. Onde o texto original difere das convenções (ajustes 1 a 10, [seção 1](00-convencoes-e-contrato-compartilhado.md#1-ajustes-em-relação-ao-planejamento-e-por-quê)), vale o que está neste documento e nas convenções.

### 9.1 Requisito funcional da especificação

- **RF16** (Módulo D — Acompanhamento; interface Web): “Registrar a retirada de um participante do estudo.”

### 9.2 Critério de aceite do PBI (planejamento)

> O coordenador registra a retirada de um participante ativo com motivo, quem solicitou e data; o participante passa a RETIRADO de forma irreversível, as visitas futuras agendadas são canceladas com o motivo "retirada do estudo" na mesma operação, e o histórico fica preservado.

### 9.3 Regras de negócio do planejamento que afetam esta task

| # | Regra proposta | Tasks afetadas (no planejamento) |
|---|---|---|
| 6 | A retirada é registrada pelo coordenador, com motivo, quem solicitou e data (não futura), e é irreversível. O participante passa a RETIRADO e as visitas futuras AGENDADA viram CANCELADA com o motivo "retirada do estudo". A vaga continua ocupada, ou seja, o limite de vagas conta todos os que foram incluídos (ponto a validar com o PO) | T101, T102 (inclui os bloqueios nos serviços de T75, T81 e T89) e T104 |
| 10 | Erros seguem o formato único da Sprint 2 (400 validação, 403 perfil sem permissão, 404 inexistente, 409 regra de negócio, sempre com o código da regra). As migrations usam faixas de versão por PBI (V3_19_* a V3_25_*); antes de começar, confirmar que a maior versão da Sprint 2 é menor que V3_19, senão trocar o prefixo V3 pelo número seguinte ao da última migration | T74 e todas as de backend |

### 9.4 Texto original da task no planejamento

> **T101 · Modelo, migration e contrato da retirada.** Primeiro crie a migration V3_23_1__participant_withdrawal.sql com a tabela de retirada (participação com UNIQUE, motivo, quem solicitou entre participante, pesquisador ou coordenação, data da retirada, usuário que registrou e data do registro) e a coluna de situação ATIVO ou RETIRADO na tabela de participações, com padrão ATIVO e CHECK. Depois escreva as entidades JPA e confira, em banco populado, que os participantes incluídos na Sprint 2 ficam ATIVO depois da migração. Por fim, descreva no contrato o endpoint de retirada e os códigos de erro (participante já retirado, estudo encerrado e data futura). Avise o grupo quando a coluna de situação entrar na main: a T102 a usa para bloquear ações nos PBIs 19, 20 e 21.

### 9.5 Requisitos da especificação que atravessam esta task

- Regra 9.2 (tecnologia): “O backend deve disponibilizar uma **API HTTP REST** compartilhada pela web e pelo mobile, documentada no padrão **OpenAPI** (Swagger UI pode ser usado para consulta e testes).”
- Regra 9.5 e Entregas: “O sistema deve ter persistência de dados e controle de acesso por perfil.” e “Persistência dos dados, com scripts de criação/migração do banco e dados fictícios para demonstração.”
- **Definition of Done** da Sprint (texto completo na [seção 11 das convenções](00-convencoes-e-contrato-compartilhado.md#11-definition-of-done-e-requisitos-transversais-da-especificação)): os critérios de aceite estão atendidos e demonstráveis com dados fictícios; o código foi revisado, passou no CI e entrou na `main` por Pull Request; há testes automatizados e, no backend, a cobertura do código de negócio permanece em 80% ou mais; erros e validações são tratados com mensagens claras; Swagger e contrato YAML refletem os endpoints; os dados de demonstração estão no perfil `demo`; documentação e capturas estão no repositório; e RF01 a RF11 continuam funcionando.


---
Parte do conjunto de documentos de design da Sprint 3: [índice](README.md) · [convenções e contrato](00-convencoes-e-contrato-compartilhado.md).
