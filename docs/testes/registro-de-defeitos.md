# Registro de defeitos e não conformidades — ClinicalStudyPB

Registro vivo dos defeitos encontrados nos testes e nas revisões, do primeiro ao último, para manter a **rastreabilidade** entre requisito, teste e correção. Cada linha tem um identificador estável: use-o na mensagem do commit e na descrição do Pull Request que corrige o defeito.

Este é o resumo oficial. O histórico completo (causa, resolução, o que funciona e o que não funciona, alterações e PRs) está na [rastreabilidade de erros e alterações](../rastreabilidade/rastreabilidade-erros-e-alteracoes.md), que usa os mesmos identificadores e estados. Relatórios que usam este registro: [Sprints 1 a 3](relatorio-de-testes-sprints-1-a-3.md) e [Release 1](relatorio-de-testes-release1.md).

**Atualizado em 16/11/2026**, sobre o ramo `develop` no commit `3257595`.

| Como registrar um defeito novo | |
|---|---|
| 1 | Acrescente uma linha na tabela da seção 1 com o próximo identificador, a origem, a evidência (comando, teste e mensagem) e o estado |
| 2 | Mude o estado quando houver correção e aponte o commit ou o PR. Não apague defeitos fechados |
| 3 | Atualize também a rastreabilidade de erros e a seção 6 do `README.md`, em uma branch de documentação própria (nunca junto da branch de uma task) |

| Legenda | Significado |
|---|---|
| Severidade **Alta** | Perde dado, burla regra ou bloqueia o fluxo principal |
| Severidade **Média** | Comportamento incorreto com contorno, ou bloqueia um merge |
| Severidade **Baixa** | Cosmético, de teste ou de processo |
| Estado **Aberto** | Sem correção |
| Estado **Em verificação** | Corrigido e mesclado; falta confirmação (em geral, do CI) |
| Estado **Fechado** | Corrigido e verificado |

## 1. Resumo

| ID | Data | Origem | Descrição | Severidade | Estado | Teste que o cobre | Resolução |
|---|---|---|---|---|---|---|---|
| B-T67-01 | Nov/2026 | Sprint 1 (segurança) · T67 | CORS com origens fixas no código impedia servir a web em outra porta ou host | Média | Fechado | `CorsIntegrationTest`, `CorsOrigensConfiguradasIntegrationTest` | `CORS_ALLOWED_ORIGINS` |
| B-T68-01 | Nov/2026 | Sprint 2 · T42 | `PUT`/`POST` do estudo aceitavam mudar o estado fora da transição, permitindo abrir o recrutamento sem critério ou visita | Alta | Fechado | `EstudoServiceTest`, `EstudoTransicaoRf07Test` | `409 ESTADO_ALTERADO_FORA_DA_TRANSICAO` |
| B-T68-02 | Nov/2026 | Sprint 2 · T54 | Candidato incluído podia ser reavaliado | Média | Fechado | `AvaliacaoEndpointTest` | `409 CANDIDATO_JA_INCLUIDO` |
| B-T68-03 | Nov/2026 | Sprint 2 · T60 | A inclusão podia ultrapassar o limite de vagas sob concorrência | Alta | Fechado | `ParticipacaoConcorrenciaTest` | Bloqueio pessimista do estudo |
| B-T68-04 | Nov/2026 | Sprint 2 · PR #5 | `AuthContext` e `Login` do PR #5 quebravam o lint, usavam endpoint e chave de token errados e duplicavam a `LoginPage` | Média | Fechado | `AuthContext.test.tsx`, E2E de login | `AuthContext` sobre a sessão real; `Login` duplicado removido |
| B-AUD-01 | Nov/2026 | Sprint 2 · T62 | Faltava o teste de endpoint dos participantes e o teste de concorrência não limpava os dados | Baixa | Fechado | `ParticipacaoEndpointTest` (15) | Teste criado e limpeza no `@AfterEach` |
| D-S2-01 | 07/11/2026 | Sprint 2 · merge `518fc2e` | Depois do merge da `main`, o backend não compilava com os testes antigos (`ConsentimentoServiceTest`, `VisitaServiceTest`) | Média | Fechado | Suíte completa (376 testes na época) | Testes adaptados ao bloqueio de escrita do estudo |
| D-S2-02 | 07/11/2026 | Sprint 2 · commit `0306618` | `.env.e2e` versionado apesar do `.gitignore` | Baixa | Fechado | — | Removido do índice |
| D-S2-03 | 09/11/2026 | Sprint 2 · commit `1394554` | Arquivos `.pyc` rastreados no Git | Baixa | Fechado | — | Removidos; `__pycache__` ignorado |
| D-S2-04 | 09/11/2026 | Sprint 2 · commit `4151afc` | 4 documentos duplicados com nome corrompido | Baixa | Fechado | — | Duplicatas removidas |
| D-S3-00 | 12/11/2026 | Sprint 3 · T73 · PR #10 | A T73 divergia do documento de design (versão, auditoria, cascata, CHECKs) | Média | Fechado | `VisitaAgendadaRepositoryTest` | Migration `V6_19_1` reescrita |
| D-S3-01 | 13/11/2026 | Sprint 3 · T75 · PR #12 | `validaLimitesInclusivosDaJanela` falhava: agendava uma data passada e a regra (correta) a recusava | Média | Fechado | `AgendamentoVisitaServiceTest` | `961952f`; PR #12 mesclado em `8d54754` |
| OBS-12-01 | 13/11/2026 | Sprint 3 · T75 · PR #12 | A entidade `VisitaAgendada` perdeu o `@UniqueConstraint` | Média | Fechado | `VisitaAgendadaRepositoryTest` | Restaurado no PR #17 |
| OBS-12-02 | 13/11/2026 | Sprint 3 · T75 · PR #12 | A entidade perdeu o `@PreUpdate` | Baixa | Fechado | — | Restaurado no PR #17; removido de novo na T76 (ver D-S3-11) |
| OBS-12-03 | 13/11/2026 | Sprint 3 · T75 · PR #12 | `criadoPor` e `criadoEm` deixaram de ser `updatable = false` | Baixa | Fechado | — | Restaurado no PR #17 |
| OBS-12-04 | 13/11/2026 | Sprint 3 · T75 · PR #12 | Códigos e tipo de erro do serviço diferiam do contrato `openapi-sprint3.yaml` | Média | Fechado | `ContratoOpenApiSprint3Test` | Corrigido na T76 (PR #21) |
| D-S3-02 | 15/11/2026 | Sprint 3 · T75 | `AgendamentoVisitaServiceTest` com 3 falhas: datas do relógio real contra `Clock` fixo | Média | Fechado | `AgendamentoVisitaServiceTest` | `Clock` fixo no teste (PR #17) |
| D-S3-03 | 15/11/2026 | Sprint 3 · T96 | Push direto na `develop`, sem Pull Request (`eb37e3a`), porque a branch foi criada com o upstream na `develop` | Média | Aberto | `HistoricoParticipanteServiceTest`, `HistoricoParticipanteEndpointTest` | Upstream removido; ⏳ falta o review do commit |
| D-S3-04 | 15/11/2026 | Sprint 3 · T85 · `task80-85` | 32 erros em `DesfechoEndpointTest` (participação ainda não gravada antes do INSERT por SQL) | Média | Fechado | `DesfechoEndpointTest` (33) | `flush()` no cenário de teste (PR #20) |
| D-S3-05 | 15/11/2026 | Sprint 3 · `task80-85` | Conflito da branch com a `develop` em 5 arquivos | Média | Fechado | Suíte completa | Resolvido e mesclado (PR #20) |
| D-S3-06 | 15/11/2026 | Sprint 3 · `task80-85` | T84 (RQ-T84-07 a 10), T86, T78 e T79 por fazer; T83 estava bloqueada pela T77 | Média | Aberto (parcial) | — | T77 (PR #23) e T83 (PR #20) na `develop`; restam T84, T86, T78 e T79 |
| D-S3-07 | 15/11/2026 | Sprint 3 · T76 | Ordem das regras: o estado do estudo era checado antes do participante | Média | Fechado | `AgendamentoVisitaServiceTest` | `c298605` |
| D-S3-08 | 15/11/2026 | Sprint 3 · T76 (CI H2) | `filtraEPaginaAgenda` voltava vazio no CI por diferença de fuso (UTC contra America/Recife) | Média | Fechado | `AgendamentoEndpointTest` | `17a4030` |
| D-S3-09 | 16/11/2026 | Sprint 3 · T76 (CI PostgreSQL) | `could not determine data type of parameter $2` na consulta da agenda | Média | Em verificação | `AgendamentoEndpointTest` (H2) | `d594e19` (PR #21); falta confirmar o job PostgreSQL do CI |
| D-S3-10 | 15/11/2026 | Sprint 3 · T76 (rodada local) | `DemoDataIntegrationTest` falhou 2 testes localmente (esperava 3 participantes, achou 5) | Baixa | Em verificação | `DemoDataIntegrationTest` (passa em 16/11) | Não reproduzido; causa não esclarecida |
| D-S3-11 | 15/11/2026 | Sprint 3 · T76 | A T76 removeu o `@PreUpdate` de `VisitaAgendada` | Baixa | Em verificação | Sem teste | A decidir pelo revisor |
| D-S3-12 | 15/11/2026 | Sprint 3 · verificação local | `cmd //c mvnw.cmd ...` não rodava e, com `-q`, parecia sucesso | Baixa | Fechado | — | Refeito com `./mvnw.cmd` |
| D-S3-13 | 15/11/2026 | Sprint 3 · ferramentas | Heredoc grande falhou e Python indisponível no ambiente | Baixa | Fechado | — | Ferramentas de escrita de arquivos |
| D-S3-14 | 16/11/2026 | Sprint 3 · T77 (CI Docker) | Build do backend falhou com Maven Central respondendo 429 (Too Many Requests); não é erro do código nem do banco | Média | Em verificação | Job "Ambiente Docker (T67)" | Retry de 5 tentativas no Dockerfile (PR #24); falta um job verde |

Resumo em 16/11/2026: **29 itens** registrados; **23 fechados**, **4 em verificação** (D-S3-09, 10, 11 e 14) e **2 abertos** (D-S3-03 e D-S3-06, ambos de processo e de tasks por fazer). Nenhuma falha de teste na `develop` (672 testes de backend e 257 de web).

---

## 2. Detalhe dos itens que ainda não estão fechados

| Item | Campo | Valor |
|---|---|---|
| **D-S3-03** | Origem | T96, commit `eb37e3a` direto na `develop` |
| | Causa | A branch foi criada com `checkout -b x origin/develop`, o que define o upstream como a `develop`; o push caiu direto nela |
| | Correção | `git branch --unset-upstream`; as próximas branches são criadas com `--no-track` |
| | Pendência | Pedir o review do commit por link, sem criar outra branch |
| **D-S3-06** | Origem | `task80-85` (T83 a T86) e PBI-19 |
| | Já resolvido | T77 (PR #23) e T83 (PR #20) na `develop` |
| | Pendência | T84 (RQ-T84-07 a 10), T86 (E2E e documentação do RF13), T78 e T79 |
| **D-S3-09** | Origem | T76, consulta `buscarAgenda` |
| | Causa | `(:de is null or ...)`: com valor nulo o PostgreSQL não infere o tipo |
| | Correção | Consulta sem parâmetro nulo (`>= :de`, `<= :ate`, `situacao in :situacoes`) |
| | Pendência | O job "Backend (testes no PostgreSQL)" do CI confirma; não foi executado localmente |
| **D-S3-10** | Origem | `DemoDataIntegrationTest`, rodada local de 15/11 |
| | Situação | Não se reproduziu no CI nem em 16/11 (9 testes, 0 falhas); causa não confirmada |
| **D-S3-11** | Origem | T76 removeu o `@PreUpdate` de `VisitaAgendada` |
| | Efeito | Nenhum hoje: o reagendamento e o desfecho definem `alteradoPor` e `alteradoEm` no serviço |
| | Pendência | O revisor decide entre restaurar o `@PreUpdate` ou aceitar a auditoria só pelo serviço |
| **D-S3-14** | Origem | Job "Ambiente Docker (T67)" do CI |
| | Causa | Limite de requisições do Maven Central (HTTP 429) no runner |
| | Correção | Cada passo do `mvn` do Dockerfile repete até 5 vezes, esperando 20, 40, 60 e 80 s |
| | Pendência | Um job verde no CI; se o 429 persistir, usar cache ou espelho do Maven |

## 3. Defeito D-S3-01 (histórico, fechado)

| Campo | Valor |
|---|---|
| **Pull Request** | #12, branch `task/t75/domain`, autoria de zePadd |
| **Task e requisito** | T75, regras de agendamento (RQ-T75-04: a data é de hoje em diante e, depois, dentro da janela) |
| **Mensagem** | `AgendamentoVisitaServiceTest.validaLimitesInclusivosDaJanela:119 ConflitoRegra A data agendada não pode estar no passado.` |
| **Causa** | O teste fixava "hoje" em 12/11/2026 e agendava `HOJE.minusDays(2)` esperando-a como limite inferior da janela, mas o serviço verifica a data passada **antes** da janela (ordem do design). O erro estava no teste |
| **Determinístico?** | Sim: o relógio é fixo no teste (`Clock.fixed`) |
| **Correção** | `961952f` (zePadd, 14/11); PR #12 mesclado em `8d54754` |
| **Estado** | **Fechado** |
