# Registro de defeitos e não conformidades — ClinicalStudyPB

Registro vivo dos defeitos encontrados nos testes e nas revisões, do primeiro ao último, para manter a **rastreabilidade** entre requisito, teste e correção. Cada linha tem um identificador estável: use-o na mensagem do commit e na descrição do Pull Request que corrige o defeito.

Relatórios que usam este registro: [Sprints 1 a 3](relatorio-de-testes-sprints-1-a-3.md) e [Release 1](relatorio-de-testes-release1.md).

**Como registrar um defeito novo:** acrescente uma linha na tabela da seção 1 (e, se precisar de mais detalhe, uma subseção da seção 2) com o próximo identificador, a evidência (comando, teste e mensagem) e o estado. Mude o estado quando houver correção e aponte o commit ou o PR. Não apague defeitos fechados.

**Legenda:** Severidade **Alta** (perde dado, burla regra ou bloqueia o fluxo principal), **Média** (comportamento incorreto com contorno ou bloqueia um merge), **Baixa** (cosmético ou de teste). Estado **Aberto**, **Em correção** ou **Fechado**.

## 1. Resumo

| ID | Data | Origem | Descrição | Severidade | Estado | Teste que o cobre |
|---|---|---|---|---|---|---|
| **D-S3-01** | 13/11/2026 | Sprint 3 · T75 · **PR #12** | O teste `validaLimitesInclusivosDaJanela` falha: agenda uma data passada e a regra da data passada a recusa | Média | **Aberto** | `AgendamentoVisitaServiceTest.validaLimitesInclusivosDaJanela` (o próprio teste falhando) |
| **OBS-12-01** | 13/11/2026 | Sprint 3 · T75 · PR #12 | A entidade `VisitaAgendada` perdeu o `@UniqueConstraint` do `@Table` | A confirmar | **Aberto** | `VisitaAgendadaRepositoryTest` (o UNIQUE continua no banco) |
| **OBS-12-02** | 13/11/2026 | Sprint 3 · T75 · PR #12 | A entidade perdeu o `@PreUpdate`: `alteradoEm` deixa de ser atualizado sozinho | A confirmar | **Aberto** | Sem teste (criar na T78) |
| **OBS-12-03** | 13/11/2026 | Sprint 3 · T75 · PR #12 | `criadoPor` e `criadoEm` deixaram de ser `updatable = false` | Baixa | **Aberto** | Sem teste |
| **OBS-12-04** | 13/11/2026 | Sprint 3 · T75 · PR #12 | Códigos e tipo de erro do serviço diferem do contrato `openapi-sprint3.yaml` e do design da T75 | Média | **Aberto** | `ContratoOpenApiSprint3Test` (quando a T76 ligar o caso da API) |
| B-T67-01 | Nov/2026 | Sprint 1 (segurança) · T67 | CORS com origens fixas no código impedia servir a web em outra porta ou host | Média | Fechado | `CorsIntegrationTest`, `CorsOrigensConfiguradasIntegrationTest` |
| B-T68-01 | Nov/2026 | Sprint 2 · T42 | `PUT`/`POST` do estudo aceitavam mudar o estado fora da transição, permitindo abrir o recrutamento sem critério ou visita | Alta | Fechado | `EstudoServiceTest`, `EstudoTransicaoRf07Test` |
| B-T68-02 | Nov/2026 | Sprint 2 · T54 | Candidato incluído podia ser reavaliado | Média | Fechado | `AvaliacaoEndpointTest` |
| B-T68-03 | Nov/2026 | Sprint 2 · T60 | A inclusão podia ultrapassar o limite de vagas sob concorrência | Alta | Fechado | `ParticipacaoConcorrenciaTest` |
| B-T68-04 | Nov/2026 | Sprint 2 · PR #5 | `AuthContext` e `Login` do PR #5 quebravam o lint, usavam endpoint e chave de token errados e duplicavam a `LoginPage` | Média | Fechado | `AuthContext.test.tsx`, E2E de login |
| B-AUD-01 | Nov/2026 | Sprint 2 · T62 | Faltava o teste de endpoint dos participantes pedido pelo design e o teste de concorrência não limpava os dados | Baixa | Fechado | `ParticipacaoEndpointTest` (15) |

Resumo: **5 abertos** (todos no PR #12, que ainda não foi integrado) e 6 fechados. Nenhum defeito aberto na `develop`.

---

## 2. Detalhe dos defeitos abertos

### D-S3-01 · `AgendamentoVisitaServiceTest.validaLimitesInclusivosDaJanela` falha

| Campo | Valor |
|---|---|
| **Pull Request** | #12, branch `task/t75/domain`, autoria de zePadd |
| **Commits** | `859ccd2`, `39af5b2`, `3aa0fc0` e `cf5792d` (de 12/11/2026, 23:44 a 23:57) |
| **Task e requisito** | T75, regras de agendamento (RQ-T75-04, passos 4 e 5: a data é de hoje em diante e, depois, dentro da janela) |
| **Como reproduzir** | `git fetch origin pull/12/head:pr-12`, entrar na branch e rodar `./mvnw test` em `backend/ClinicalStudyPB`. Para ver o que o CI vê, usar `pull/12/merge` |
| **Resultado** | Só a branch: 479 testes, 0 falhas, **1 erro**, 2 ignorados. Com o merge na `develop` (`499c951`): 488 testes, 0 falhas, **1 erro**, 2 ignorados |
| **Mensagem** | `AgendamentoVisitaServiceTest.validaLimitesInclusivosDaJanela:119 ConflitoRegra A data agendada não pode estar no passado.` |
| **Causa** | O teste fixa "hoje" em 12/11/2026 e agenda `HOJE.minusDays(2)` esperando que seja aceita como o limite inferior inclusivo da janela. Mas o serviço verifica a data passada (`DATA_PASSADA`) **antes** de verificar a janela, exatamente na ordem que o design da T75 manda (RQ-T75-04: passo 4, data de hoje em diante; passo 5, janela). Logo a data de dois dias atrás é recusada pela regra correta e **o erro está no teste**, que espera o contrário |
| **Determinístico?** | Sim. O relógio é fixo no teste (`Clock.fixed`), então não depende do dia em que roda |
| **Impacto** | O CI falha nos jobs de backend e bloqueia o merge do PR. Afeta só a T75, nada da `develop` |
| **Como corrigir** | Ajustar o teste para usar limites **válidos** (a primeira data aceita é hoje, mesmo que o início da janela seja anterior) e manter um caso próprio em que uma data passada dentro da janela é recusada |
| **Sobre os "2 erros"** | Foi relatado que o PR tinha 2 erros no backend. Localmente só foi reproduzido **1** (o acima). O segundo não foi identificado: provavelmente é o mesmo teste contado em dois jobs do CI (H2 e PostgreSQL). **Pendente:** conferir o log do CI do PR #12 e, se houver outro erro, registrar aqui como D-S3-02 |
| **Estado** | **Aberto** |

### OBS-12-01 a OBS-12-04 · Pontos de revisão do mesmo Pull Request

São diferenças entre o PR #12 e o design da Sprint 3 / o código já integrado, encontradas na leitura do diff. **Não são falhas de teste**: precisam de decisão do autor e do revisor (I3) antes do merge.

| ID | Observação | Referência |
|---|---|---|
| OBS-12-01 | `VisitaAgendada` voltou a `@Table(name = "visitas_agendadas")`, sem o `@UniqueConstraint` de `(participacao_id, visita_planejada_id)`. O UNIQUE continua existindo no banco (migration `V6_19_1`), então não há efeito funcional, mas a entidade deixa de documentá-lo | RQ-T73-02; RQ-T73-09 |
| OBS-12-02 | O método `@PreUpdate` que atualizava `alteradoEm` foi removido. O reagendamento precisa então definir `alteradoPor` e `alteradoEm` no serviço (RQ-T75-09); fora dele, uma alteração futura (desfecho, T81) deixaria `alteradoEm` desatualizado | RQ-T73-09; RQ-T75-09 |
| OBS-12-03 | `criado_por_id` e `criado_em` deixaram de ser `updatable = false`; a auditoria de criação passa a poder ser alterada por engano | RQ-T73-09 |
| OBS-12-04 | O serviço usa `ConflitoRegraException` (HTTP 409) com os códigos `AGENDAMENTO_DUPLICADO`, `DATA_PASSADA`, `FORA_DA_JANELA`, `ESTUDO_INVALIDO`, `VISITA_INCOMPATIVEL`, `REAGENDAMENTO_INVALIDO` e `PARTICIPANTE_RETIRADO`. O contrato (`openapi-sprint3.yaml`) e a T75 definem `VISITA_JA_AGENDADA`, `DATA_NO_PASSADO` (**400**, via `ValidacaoRegraException`), `DATA_FORA_DA_JANELA`, `ESTUDO_FORA_DE_RECRUTAMENTO_OU_EXECUCAO` e `VISITA_NAO_ESTA_AGENDADA`. A diferença quebraria o contrato quando a T76 ligar o teste da API | RQ-T75-01, RQ-T75-04, RQ-T75-08; contrato da T74 |

---

## 3. Defeitos fechados (histórico)

Os seis defeitos fechados da Release 1 estão detalhados (descrição, correção e teste de regressão) na [seção 10 do relatório da Release 1](relatorio-de-testes-release1.md#10-defeitos). A correção de cada um mantém o teste que o cobre, listado na tabela da seção 1.
