# Rastreabilidade de erros e alterações — Sprints 1 a 3

> Documento geral, **independente de qualquer branch de task**. Consolida todos os erros, defeitos e não conformidades das Sprints 1, 2 e 3: causa, resolução e **o que funciona e o que não funciona** depois de cada alteração.
> Atualizado em: 15/11/2026.

## 0. Como usar

| Tema | Regra |
|---|---|
| IDs | Reaproveita os do [registro de defeitos](../testes/registro-de-defeitos.md) (`B-T67-01`, `B-T68-01`…, `D-S3-01`, `OBS-12-01`…) e continua a numeração dali (`D-S3-02` em diante). Use o ID na mensagem do commit e no PR que corrige o item |
| Fontes | `docs/testes/registro-de-defeitos.md`, `relatorio-de-testes-release1.md`, `relatorio-de-testes-sprints-1-a-3.md`, `docs/sprint1/status-sprint1.md`, `docs/sprint2/T67*.md` e `T68*.md`, histórico do Git (`git log origin/develop`) e as execuções de teste e do CI de 13 a 15/11/2026 |
| Divergência com o registro oficial | O `registro-de-defeitos.md` da `develop` está desatualizado (ver seção 6). Aqui está o estado real |
| Registrar algo novo | Acrescente uma linha na seção 4 (erros) e/ou 5 (alterações) e atualize os resumos das seções 1 a 3 |

### Legenda

| Símbolo | Significado |
|---|---|
| ✅ | Funciona e foi **verificado** (teste, CI ou leitura do código na `develop`) |
| 🟡 | Aplicado, mas **verificação incompleta** (explicado na linha) |
| ❌ | **Não funciona** ou não foi corrigido |
| ⏳ | Pendente de ação de alguém (commit, PR, decisão) |

| Severidade | Significado |
|---|---|
| Alta | Perde dado, burla regra ou bloqueia o fluxo principal |
| Média | Comportamento incorreto com contorno, ou bloqueia um merge |
| Baixa | Cosmético, de teste ou de processo |

---

## 1. Resumo geral

| Sprint | Itens registrados | Fechados | Abertos / pendentes |
|---|---|---|---|
| Sprint 1 | 1 defeito (B-T67-01) + 4 dívidas técnicas (DT-S1-01 a 04) | B-T67-01, DT-S1-01, DT-S1-04 | DT-S1-02, DT-S1-03 |
| Sprint 2 | 9 (B-T68-01 a 04, B-AUD-01, D-S2-01 a 04) | 9 | nenhum |
| Sprint 3 | D-S3-00 a D-S3-13 e OBS-12-01 a 04 | D-S3-00, 01, 02, 07, 08, 12, 13 e OBS-12-01 a 03 | ver seção 3 |

| Referência de testes | Resultado |
|---|---|
| `develop` no último relatório (13/11) | 478 testes de backend, 133 de web, 29 de E2E; cobertura de linhas 99,2%; sem falha |
| `task/t76/endpoint` em 15/11 | Testes da T76 passam no H2; o PostgreSQL depende do CI |

## 2. Situação por task e branch (Sprint 3)

| Task / branch | Dono | Situação | Pendência principal |
|---|---|---|---|
| T73, T74/T95, T87 (modelo, contratos, resultados) | outros | ✅ na `develop` | nenhuma |
| T75 (`task/t75/domain`, PR #12 e #17) | zePadd / João Victor | ✅ mesclada na `develop` | alinhar os códigos de erro ao contrato (OBS-12-04, resolvido na T76) |
| T88, T89, T90 (resultados) | outros | ✅ na `develop` | nenhuma |
| T96 (histórico, backend) | I7 | 🟡 na `develop` por push direto (`eb37e3a`, D-S3-03) | review do commit por link |
| T97 (tela do histórico, web) | I7 | 🟡 implementada e testada (166 testes web, tsc e lint ok); **não commitada** (`stash@{0}`) | commit/PR do dono; conflito com T83 |
| T76 (`task/t76/endpoint`) | zePadd + ajustes de I7 | 🟡 commitada e enviada; H2 ✅, **PostgreSQL só no CI** | confirmar CI do PostgreSQL; RQ-T76-13 (avisar I3) |
| T80-T86 (`feature/task80-85/desfecho-visita`) | José Agra Neto | ❌ 32 testes de endpoint falham | aplicar o `flush`, atualizar com a `develop`, concluir T83/T84/T86 |

---

## 3. Itens abertos hoje

| ID | O que está aberto | Quem |
|---|---|---|
| D-S3-04 | `DesfechoEndpointTest`: 32 erros por falta de `flush` (correção comprovada, **não aplicada**) | José (task80-85) |
| D-S3-05 | Conflito da `task80-85` com a `develop` (T90) | José |
| D-S3-06 | T83 e T84 incompletas e T86 não iniciada, bloqueadas por T77 | I1 / José |
| D-S3-09 | T76: consulta da agenda no **PostgreSQL** corrigida, mas **só o CI confirma** | T76 |
| D-S3-10 | `DemoDataIntegrationTest`: 2 falhas na rodada local que **não se reproduziram no CI** (causa não esclarecida) | investigar se reaparecer |
| D-S3-11 | T76 removeu o `@PreUpdate` de `VisitaAgendada` (volta o ponto da OBS-12-02) | revisão da T76 |
| D-S3-03 | T96 entrou na `develop` sem PR (review pendente) | I7 |
| OBS-12-04 | Códigos de erro do serviço divergem do contrato na `develop` (corrigido só na T76, ainda não mesclada) | T76 |
| DT-S1-02, DT-S1-03 | Vulnerabilidades de dev do Expo CLI; logout só no cliente | acompanhar |

---

## 4. Registro de erros, por sprint

### 4.1 Sprint 1

| ID | Origem | Descrição do erro | Sev. | Causa | Resolução | Funciona | Não funciona / pendente | Estado |
|---|---|---|---|---|---|---|---|---|
| B-T67-01 | `SecurityConfig` (login JWT e segurança da API; Azure 3060/3063; Bug #3706) | CORS com origens fixas: servir a web em outra porta ou host bloqueava a chamada (preflight 403) | Média | Só `localhost:5173` e `localhost:8082` estavam fixos no código | `app.cors.allowed-origins` / `CORS_ALLOWED_ORIGINS`, com os mesmos padrões. Commits `11a3dcd`, `508171f`, `f8a10a4` (08/11) | ✅ `CorsIntegrationTest`, `CorsOrigensConfiguradasIntegrationTest` | — | ✅ Fechado |
| DT-S1-01 | `status-sprint1.md` | Sem testes de componentes web/mobile nem E2E | — | Dívida técnica da Sprint 1 | Sprint 2 criou 133 testes de web e 29 de E2E (Playwright) | ✅ web e E2E | O **mobile** segue sem testes automatizados (limitação 3 da Release 1) | ✅ Fechado (parcial no mobile) |
| DT-S1-02 | `status-sprint1.md` | `npm audit` do mobile aponta vulnerabilidades em dependências de desenvolvimento do Expo CLI (`node-forge`, `zod`) | — | A correção forçada quebraria o SDK | Nenhuma; acompanhar as atualizações do Expo | — | Sem registro de resolução | ⏳ Aberto |
| DT-S1-03 | `status-sprint1.md` | Logout só no cliente (JWT stateless): o token vale por até 30 min mesmo se a conta for desativada | — | Risco aceito pelo time | Nenhuma mudança registrada | — | Token continua válido até expirar | ⏳ Aberto (risco aceito) |
| DT-S1-04 | `status-sprint1.md` | Testes locais usam H2; o PostgreSQL só era exercitado pelo CI | — | Sem banco PostgreSQL nos testes locais | CI ganhou o job **Backend (testes no PostgreSQL)** (PostgreSQL 16) | ✅ job do CI | Continua dependendo do CI, não da máquina local | ✅ Fechado |

### 4.2 Sprint 2 (Release 1)

| ID | Origem | Descrição do erro | Sev. | Causa | Resolução | Funciona | Não funciona / pendente | Estado |
|---|---|---|---|---|---|---|---|---|
| B-T68-01 | T42 | `PUT`/`POST` do estudo aceitavam mudar o estado fora da transição (abrir o recrutamento sem critério ou visita) | Alta | Estado alterável fora do endpoint de transição | Estado só muda por `POST /transicoes`; `409 ESTADO_ALTERADO_FORA_DA_TRANSICAO` | ✅ `EstudoServiceTest`, `EstudoTransicaoRf07Test` | — | ✅ Fechado |
| B-T68-02 | T54 | Candidato incluído podia ser reavaliado | Média | Faltava a checagem no serviço | `409 CANDIDATO_JA_INCLUIDO` em `AvaliacaoService` | ✅ `AvaliacaoEndpointTest` | — | ✅ Fechado |
| B-T68-03 | T60 | A inclusão podia ultrapassar o limite de vagas sob concorrência | **Alta** | Condição de corrida entre inclusões simultâneas | Bloqueio pessimista do estudo (`buscarComBloqueio`) | ✅ `ParticipacaoConcorrenciaTest`; validado por mutação manual (sem o bloqueio o teste falha) | — | ✅ Fechado |
| B-T68-04 | PR #5 / T21 | `AuthContext`/`Login` do PR #5 quebravam o lint, usavam endpoint e chave de token errados, duplicavam a `LoginPage` e não expunham o usuário exigido pela T21 | Média | PR #5 não alinhado ao projeto | `AuthContext` virou a porta de autenticação sobre a sessão real; `Login` duplicado removido. Commits `74ec30b`, `b0eba54` (09/11) | ✅ `AuthContext.test.tsx`, E2E de login | — | ✅ Fechado |
| B-AUD-01 | T62 | Faltava o teste de endpoint dos participantes e o teste de concorrência não limpava os dados | Baixa | Item do design não implementado | `ParticipacaoEndpointTest` (15) e limpeza no `@AfterEach` | ✅ os próprios testes | — | ✅ Fechado |
| D-S2-01 | Merge `518fc2e` (07/11) | Depois do merge da `main` na branch de correção, o backend **não compilava** com os testes antigos (`ConsentimentoServiceTest`, `VisitaServiceTest`) | Média | Testes não acompanharam o bloqueio de escrita do estudo e os novos métodos `buscarEstudoId` | Testes adaptados; contrato `Erro`, `converterErro` e mock da web ajustados | ✅ Na época: backend 376 testes, cobertura 99,2%, web 127 testes, E2E 29 | — | ✅ Fechado |
| D-S2-02 | Commit `0306618` (07/11) | `.env.e2e` versionado apesar do `.gitignore` | Baixa | Arquivo entrou antes da regra de ignore | Removido do índice; o CI usa `.env.e2e.example` | ✅ | — | ✅ Fechado |
| D-S2-03 | Commit `1394554` (09/11) | Arquivos `.pyc` rastreados no Git | Baixa | `__pycache__` não ignorado | Removidos; `__pycache__` ignorado | ✅ | — | ✅ Fechado |
| D-S2-04 | Commit `4151afc` (09/11) | 4 documentos duplicados com nome corrompido e textos desatualizados | Baixa | Documentação defasada em relação ao código | Duplicatas removidas; documentação alinhada ao código | ✅ | — | ✅ Fechado |

**Riscos e limitações da Release 1** (seção 11 do relatório; não são defeitos, ficam como acompanhamento):

| # | Risco / limitação | Situação |
|---|---|---|
| 1 | Docker só provado pelo job do CI | ⏳ não reavaliado |
| 2 | Testes locais em H2; bloqueio de linha do PostgreSQL só no CI | ⏳ não reavaliado |
| 3 | App mobile sem testes automatizados | ⏳ não reavaliado |
| 4 | E2E só em Chromium | ⏳ não reavaliado |
| 5 | Sem medição de cobertura no web | ⏳ não reavaliado |
| 6 | Sem testes de carga, desempenho e segurança profunda | ⏳ não reavaliado |
| 7 | Sem teste automatizado de acessibilidade | ⏳ não reavaliado |
| 8 | Quatro códigos de erro conferidos só pelo status HTTP; 3 trechos de `UsuarioService` sem teste | ⏳ não reavaliado |
| 9 | Qualquer pesquisador edita estudos de outro (decisão de produto) | ⏳ não reavaliado |
| 10 | Teste de concorrência usa banco compartilhado entre classes | ⏳ não reavaliado |

### 4.3 Sprint 3

| ID | Origem | Descrição do erro | Sev. | Causa | Resolução | Funciona | Não funciona / pendente | Estado |
|---|---|---|---|---|---|---|---|---|
| D-S3-00 | T73, PR #10 (`057090e`, 12/11) | A T73 divergia do documento de design | Média | Migration e entidade fora do design (sem versão, motivo/observação juntos, sem auditoria, com cascata) | Migration `V6_19_1` reescrita (versão, motivo/observação separados, `criado_por_id`/`alterado_por_id`, CHECKs, índices, `ON DELETE RESTRICT`); `SituacaoVisitaAgendada` → `SituacaoVisita`; `@Version`; repositório com os 4 métodos | ✅ na época, 444 testes, 0 falhas | — | ✅ Fechado |
| D-S3-01 | T75, PR #12 (zePadd) | `AgendamentoVisitaServiceTest.validaLimitesInclusivosDaJanela` falha | Média | O teste agendava `HOJE - 2` como limite da janela, mas o serviço verifica a data passada **antes** da janela (RQ-T75-04); o erro estava no teste | `961952f` (zePadd, 14/11); PR #12 mesclado em `8d54754` | ✅ passa nas execuções posteriores | O registro oficial ainda o mostra como Aberto | ✅ Fechado |
| OBS-12-01 | T75, PR #12 | `VisitaAgendada` sem o `@UniqueConstraint` de `(participacao_id, visita_planejada_id)` | — | Removido no PR #12 | Restaurado no PR #17 (`b2944f6`, 15/11) | ✅ lido na `develop` | — | ✅ Fechado |
| OBS-12-02 | T75, PR #12 | Sem `@PreUpdate`, `alteradoEm` deixava de ser atualizado sozinho | — | Removido no PR #12 | Restaurado no PR #17 | ✅ lido na `develop` | A T76 o removeu de novo (D-S3-11) | ✅ Fechado na `develop` |
| OBS-12-03 | T75, PR #12 | `criado_por_id` e `criado_em` sem `updatable = false` | Baixa | Removido no PR #12 | Restaurado no PR #17 | ✅ lido na `develop` | — | ✅ Fechado |
| OBS-12-04 | T75, PR #12 | Códigos e tipo de erro do serviço diferem do contrato: a `develop` usa `AGENDAMENTO_DUPLICADO`, `DATA_PASSADA`, `ESTUDO_INVALIDO`, `VISITA_INCOMPATIVEL`, `REAGENDAMENTO_INVALIDO`; o contrato define `VISITA_JA_AGENDADA`, `DATA_NO_PASSADO` (400), `ESTUDO_FORA_DE_RECRUTAMENTO_OU_EXECUCAO`, `VISITA_NAO_ESTA_AGENDADA` | Média | Serviço escrito sem seguir o contrato | Corrigido na T76 (`afc9bc6`, zePadd) | ✅ na branch da T76 (`ContratoOpenApiSprint3Test`, 30 testes) | ❌ **Continua aberto na `develop`**; só vale quando a T76 for mesclada | ❌ Aberto |
| D-S3-02 | T75 | `AgendamentoVisitaServiceTest` com 3 falhas (H2 e PostgreSQL) | Média | `HOJE` e `AGORA` vinham do relógio real, enquanto o serviço recebia `Clock` fixo em 12/11/2026 | `HOJE`/`AGORA` derivados do mesmo `Clock` fixo: `759377b` (`task/t75/domain`), consolidada no PR #17 (`b2944f6`) | ✅ `AgendamentoVisitaServiceTest` (10 testes); a `develop` já tem a correção | — | ✅ Fechado |
| D-S3-03 | T96 (processo) | Push direto na `develop`, sem Pull Request (`eb37e3a`, 15/11) | Média | Branch criada com `checkout -b x origin/develop`, que define o upstream como a `develop` | `git branch --unset-upstream`; próximas branches com `--no-track` | ✅ o risco foi eliminado para as próximas branches | O código da T96 está na `develop` **sem PR**; ⏳ pedir o review pelo link do commit | 🟡 Contornado |
| D-S3-04 | `task80-85` (José Agra Neto) | 32 erros em `DesfechoEndpointTest` | Média | O teste cria a participação pela API (JPA ainda não gravou) e insere a visita por SQL puro; o banco recusa pela FK `fk_visitas_agendadas_participacao`. O código dos endpoints está correto | Correção comprovada em worktree temporário (já removido): `entityManager.flush()` antes do `agendada(...)` | ✅ comprovada: 33 testes passam, 1 ignorado. Web: tsc, lint e 172 testes ok | ❌ **Não aplicada** à branch. Backend: 647 testes, 0 falhas, 32 erros | ❌ Aberto |
| D-S3-05 | `task80-85` | Conflito com a `develop` | Média | Branch 1 commit atrás (T90, `6d3b6a3`). Conflitos em `ContratoOpenApiSprint3Test.java` (branch: 10 operações e `@Disabled` T76/T90; `develop`: 8 e sem o T90) e `DemoDataIntegrationTest.java` | Sugerida: manter a remoção do `@Disabled("T90")` da `develop` e a contagem 10 da branch | — | ❌ Não resolvido | ❌ Aberto |
| D-S3-06 | `task80-85` | Tasks incompletas e bloqueadas | Média | T77, T78 e T79 (PBI-19, I1) não estão na `develop`; faltam `AbaAgenda`, `AcoesDaVisita`, `VisitaAgendadaPage` e `api/agendamento.ts` | A T76 destrava os `@Disabled("T76")` quando for mesclada | ✅ T80, T81, T82 concluídas | T85 escrita, mas falha (D-S3-04). T83 e T84 parciais (T84: faltam RQ-T84-07 a 10). T86 não iniciada. Os documentos de design citam `task80-86`; o nome real é `task80-85` | ❌ Aberto |
| D-S3-07 | T76 | `AgendamentoVisitaServiceTest.rejeitaParticipacaoInativaAntesDasDemaisRegras` falhava | Média | `validarEstadoDoEstudo` rodava antes da checagem do participante e trocava o código devolvido | `validarRegrasDeAgendamento` checa primeiro o participante (`PARTICIPANTE_RETIRADO`) e depois o estado do estudo. Commit `c298605` | ✅ `AgendamentoVisitaServiceTest` (10 testes) | — | ✅ Fechado |
| D-S3-08 | T76 (CI H2) | `AgendamentoEndpointTest.filtraEPaginaAgenda` voltava lista vazia | Média | Fuso horário: o filtro usava o "hoje" do relógio da aplicação (America/Recife, 15/11); a visita de demonstração usa `current_date` do banco, que no runner (UTC, 00:20) já era 16/11 | O teste lê a data real da visita no banco e filtra por ela. Commit `17a4030` | ✅ passa em fuso local e com `-Duser.timezone=UTC` (44 testes, 0 falhas) | — | ✅ Fechado |
| D-S3-09 | T76 (CI PostgreSQL) | `could not determine data type of parameter $2` em `buscarAgenda` | Média | A consulta usava `(:de is null or ...)`; com valor nulo o PostgreSQL não infere o tipo (o H2 aceita) | Consulta sem parâmetro nulo (`>= :de`, `<= :ate`, `situacao in :situacoes`); `AgendaService` passa 1900-01-01 a 2999-12-31 e todas as situações quando não há filtro; teste novo sem filtros. Commit `d594e19` | ✅ H2: 49 testes, 0 falhas | ❌ **PostgreSQL não executado localmente** (Docker Desktop desligado); só o CI confirma | 🟡 Corrigido, aguarda o CI |
| D-S3-10 | T76 (rodada local) | `DemoDataIntegrationTest`: `demoCarregaEstudoDeAcompanhamento` e `cargaDoAcompanhamentoEIdempotente` esperavam 3 participantes / 3 visitas e encontraram 5 / 8 | Baixa | Não esclarecida (dados de T90/T95 no mesmo estudo; suspeita de `target/` ou dados locais desatualizados) | Nenhuma alteração por causa dele | ✅ No CI (H2 e PostgreSQL): 7 testes, 0 falhas | Não se reproduziu no CI; causa não confirmada | 🟡 Não esclarecido |
| D-S3-11 | T76 (`afc9bc6`) | A T76 removeu o `@PreUpdate` de `VisitaAgendada`, voltando ao ponto da OBS-12-02 | Baixa | Auditoria passou a ser feita só pelo serviço | A decidir pelo revisor: restaurar o `@PreUpdate` ou aceitar a auditoria só pelo serviço | ✅ Sem efeito hoje: reagendamento e desfecho (T81) definem `alteradoPor`/`alteradoEm` explicitamente | Uma alteração futura fora desses serviços deixaria `alteradoEm` desatualizado | 🟡 A decidir |
| D-S3-12 | Verificação local (processo) | `cmd //c mvnw.cmd ...` não rodava (comando não reconhecido) e com `-q` parecia sucesso | Baixa | Comando inválido no shell, sem mensagem de erro com `-q` | Refeitas com `./mvnw.cmd`, sem `-q`; qualquer "passou" feito de outro modo é desconsiderado | ✅ rodadas com `./mvnw.cmd` | — | ✅ Resolvido |
| D-S3-13 | Ferramentas (T97 e worktree) | Heredoc grande no Bash falhou ao criar os testes da T97; Python indisponível para um patch no worktree temporário | Baixa | Limitação do ambiente | Usadas as ferramentas Write e Edit | ✅ | Sem impacto no código | ✅ Contornado |

---

## 5. Registro de alterações (o que foi mudado e como está)

| # | Alteração | Resolve | Commit / local | Funciona | Não funciona / não verificado |
|---|---|---|---|---|---|
| A-01 | `HOJE`/`AGORA` do teste da T75 derivados do `Clock` fixo | D-S3-02 | `759377b` (`task/t75/domain`) → PR #17 `b2944f6` | ✅ `AgendamentoVisitaServiceTest` | — |
| A-02 | Restaurar `@UniqueConstraint`, `@PreUpdate`, `updatable = false` na entidade | OBS-12-01/02/03 | PR #17 `b2944f6` | ✅ lido na `develop` | A T76 removeu o `@PreUpdate` (D-S3-11) |
| A-03 | Backend da T96 (serviço, controller, repositórios, testes, YAML/DTO/`schema-sprint3.d.ts`) | RF15 | `eb37e3a` (direto na `develop`) | ✅ `HistoricoParticipanteServiceTest`, `HistoricoParticipanteEndpointTest`, `ContratoOpenApiSprint3Test` | 🟡 sem PR (D-S3-03) |
| A-04 | Web da T97 (ficha do participante, linha do tempo, rota, `formatarDataHora`, links em `AbaParticipantes`) | RF15 | `stash@{0}` | ✅ tsc, lint e 166 testes | ⏳ não commitada; conflito previsto com `SeloSituacaoVisita`/`acompanhamento.css` da T83 |
| A-05 | `git branch --unset-upstream` na branch da T96 | D-S3-03 | configuração local | ✅ | — |
| A-06 | Correção sugerida (`flush`) em `DesfechoEndpointTest` | D-S3-04 | worktree temporário (removido) | ✅ comprovada (33 ok, 1 ignorado) | ❌ **Não aplicada** à branch do José |
| A-07 | Reordenar regras: participante antes do estado do estudo | D-S3-07 | `c298605` (T76) | ✅ `AgendamentoVisitaServiceTest` (10) | — |
| A-08 | Teste lê a data da visita no banco em vez do "hoje" do relógio | D-S3-08 | `17a4030` (T76) | ✅ passa em fuso local e `UTC` | — |
| A-09 | Consulta da agenda sem parâmetros nulos; chamada sem filtros no teste | D-S3-09 | `d594e19` (T76) | ✅ H2: `AgendamentoEndpointTest` (4), `AgendamentoVisitaServiceTest` (10), `ContratoOpenApiSprint3Test` (30), `VisitaAgendadaRepositoryTest` (5) | ❌ PostgreSQL só no CI |

## 6. Divergências entre este levantamento e o registro oficial

O `docs/testes/registro-de-defeitos.md` e o `relatorio-de-testes-sprints-1-a-3.md`, escritos em 13/11, estão **atrás** do que foi mesclado depois. A atualização deles é decisão do time; **não foi alterada aqui**.

| Item | No registro oficial | Estado real |
|---|---|---|
| D-S3-01 | Aberto | Fechado: PR #12 mesclado (`8d54754`) após `961952f` |
| OBS-12-01, 02, 03 | Abertos | Fechados: PR #17 (`b2944f6`), confirmados no código da `develop` |
| OBS-12-04 | Aberto | Continua aberto na `develop`; corrigido só na T76 (não mesclada) |
| "5 abertos, nenhum na `develop`" | Resumo | Hoje há itens abertos fora da `develop` (T76 e `task80-85`) |
| Contagem de testes | 478 backend (13/11) | Cresceu com T88-T90 e T96; reexecutar para atualizar |
| D-S3-02 a D-S3-13 | Não existem | Acrescentados neste documento |

## 7. Pendências por responsável (ordem sugerida)

| # | Responsável | Ação | Refs |
|---|---|---|---|
| 1 | T76 | Conferir o CI do PostgreSQL com `c298605`, `17a4030` e `d594e19`; decidir o `@PreUpdate`; avisar o I3 ao mesclar (RQ-T76-13) | D-S3-09, D-S3-11 |
| 2 | `task80-85` (José) | Aplicar o `flush`, atualizar com a `develop` e concluir T83/T84/T86 depois da T77; combinar com a T97 qual `SeloSituacaoVisita` fica | D-S3-04, 05, 06 |
| 3 | T96 (I7) | Pedir o review do commit `eb37e3a` | D-S3-03 |
| 4 | T97 (I7) | Recuperar o `stash@{0}`, commitar na branch própria e abrir PR | A-04 |
| 5 | Time | Atualizar o `registro-de-defeitos.md` e o relatório de testes com a seção 6; registrar os defeitos novos no Azure Boards (padrão da T67: Task "Bug" filha do PBI e ligada à task de origem) | seção 6 |
| 6 | Acompanhamento | DT-S1-02 e DT-S1-03; investigar D-S3-10 só se voltar a falhar | — |
