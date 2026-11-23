# Rastreabilidade de erros e alterações — Sprints 1 a 3

> Documento geral, **independente de qualquer branch de task**. Consolida todos os erros, defeitos e não conformidades das Sprints 1, 2 e 3: causa, resolução e **o que funciona e o que não funciona** depois de cada alteração. O resumo também aparece no [README](../../README.md#6-testes-e-cobertura) e a lista oficial de defeitos está no [registro de defeitos](../testes/registro-de-defeitos.md).
> Atualizado em: 16/11/2026, sobre o ramo `develop` no commit `3257595` (PR #25).

## 0. Como usar

| Tema | Regra |
|---|---|
| IDs | Reaproveita os do [registro de defeitos](../testes/registro-de-defeitos.md) (`B-T67-01`, `B-T68-01`…, `D-S3-01`, `OBS-12-01`…) e continua a numeração dali (`D-S3-02` em diante). Use o ID na mensagem do commit e no PR que corrige o item |
| Fontes | `docs/testes/registro-de-defeitos.md`, `relatorio-de-testes-release1.md`, `relatorio-de-testes-sprints-1-a-3.md`, `docs/sprint1/status-sprint1.md`, `docs/sprint2/T67*.md` e `T68*.md`, histórico do Git (`git log origin/develop`) e as execuções de teste de 13 a 16/11/2026 |
| Onde atualizar | Este arquivo (histórico completo), `docs/testes/registro-de-defeitos.md` (lista oficial) e a seção 6 do `README.md` (resumo). Os três usam os mesmos IDs e estados |
| Registrar algo novo | Acrescente uma linha na seção 4 (erros) e/ou 5 (alterações) e atualize os resumos das seções 1 a 3, o registro de defeitos e o README |
| Branch | Este documento **nunca** vai junto com a branch de uma task: ele tem sempre uma branch própria de documentação |

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
| Sprint 3 | D-S3-00 a D-S3-14 e OBS-12-01 a 04 | D-S3-00, 01, 02, 04, 05, 07, 08, 12, 13 e OBS-12-01 a 04 | D-S3-03, 06, 09, 10, 11, 14 (ver seção 3) |

| Execução de 16/11/2026 na `develop` (`3257595`) | Resultado |
|---|---|
| Backend (`./mvnw verify`) | **672 testes, 0 falhas, 0 erros, 0 ignorados**; BUILD SUCCESS |
| Cobertura de linhas do código de negócio (`domain` + `service`) | **99,0%** (967 de 977 linhas); meta 80% |
| Web (`npm test`, `tsc -b`, `eslint`) | **257 testes, 0 falhas**; `tsc` e `eslint` limpos |
| E2E (Playwright) | 29 testes listados em 9 arquivos; **não executados** nesta rodada |
| CI do GitHub (jobs H2, PostgreSQL e Docker) | Não consultado a partir desta máquina; o resultado é do time |

## 2. Situação por task e branch (Sprint 3)

| Task / PR | Dono | Situação em 16/11 | Pendência principal |
|---|---|---|---|
| T73, T74/T95, T87 (modelo, contratos, resultados) | outros | ✅ na `develop` | nenhuma |
| T75 (PR #12 e #17) | zePadd / João Victor | ✅ na `develop` | nenhuma |
| T88, T89, T90 (resultados das avaliações, PRs #16, #18, #19) | outros | ✅ na `develop` | telas dos resultados (T91 a T93) |
| T96 (histórico, backend) | I7 | 🟡 na `develop` por push direto (`eb37e3a`, D-S3-03) | review do commit por link |
| T76 (endpoints do agendamento, PR #21) | zePadd + ajustes de I7 | ✅ na `develop` | confirmar o job PostgreSQL do CI (D-S3-09); RQ-T76-13 (avisar o I3) |
| T77 (telas de agendamento, PR #23) | I7 | ✅ na `develop` | E2E `acesso-por-perfil` não executado |
| Correção do Docker (PR #24) | I7 | 🟡 na `develop` | confirmar o job "Ambiente Docker (T67)" (D-S3-14) |
| T80, T81, T82 (desfecho: regras e endpoints, PR #20) | José Agra Neto | ✅ na `develop` | nenhuma |
| T83 (telas do desfecho, PR #20) | José Agra Neto | ✅ na `develop`, encaixada nas telas da T77 | nenhuma |
| T84 (demonstração dos desfechos, PR #20) | José Agra Neto | 🟡 script de demonstração na `develop` | RQ-T84-07 a 10 |
| T85 (testes do desfecho, PR #20) | José Agra Neto | ✅ 33 testes de endpoint, sem ignorados | nenhuma |
| T86 (E2E e documentação do RF13) | — | ❌ não iniciada | depende da infraestrutura E2E da T79 |
| T97 (ficha e histórico do participante, PR #25) | I7 | ✅ na `develop` | E2E não executado |
| T78, T79 (testes de backend e E2E do agendamento) | I1 | ⏳ não verificadas | confirmar com o dono |

---

## 3. Itens abertos hoje

| ID | O que está aberto | Quem |
|---|---|---|
| D-S3-03 | T96 entrou na `develop` sem PR (review pendente) | I7 |
| D-S3-06 | T84 (RQ-T84-07 a 10), T86 e T78/T79 por fazer | José / I1 |
| D-S3-09 | T76: a consulta da agenda no **PostgreSQL** foi corrigida e mesclada, mas só o job do CI confirma | time |
| D-S3-10 | `DemoDataIntegrationTest` falhou 2 testes numa rodada local e **não se reproduziu** (passou em 16/11); causa não esclarecida | acompanhar |
| D-S3-11 | O `@PreUpdate` de `VisitaAgendada` não existe na `develop` (volta o ponto da OBS-12-02) | revisão da T76 |
| D-S3-14 | Docker: Maven Central respondeu 429; o retry foi mesclado (#24), falta um job verde | time |
| DT-S1-02, DT-S1-03 | Vulnerabilidades de dev do Expo CLI; logout só no cliente | acompanhar |

---

## 4. Registro de erros, por sprint

### 4.1 Sprint 1

| ID | Origem | Descrição do erro | Sev. | Causa | Resolução | Funciona | Não funciona / pendente | Estado |
|---|---|---|---|---|---|---|---|---|
| B-T67-01 | `SecurityConfig` (login JWT e segurança da API; Azure 3060/3063; Bug #3706) | CORS com origens fixas: servir a web em outra porta ou host bloqueava a chamada (preflight 403) | Média | Só `localhost:5173` e `localhost:8082` estavam fixos no código | `app.cors.allowed-origins` / `CORS_ALLOWED_ORIGINS`, com os mesmos padrões. Commits `11a3dcd`, `508171f`, `f8a10a4` (08/11) | ✅ `CorsIntegrationTest`, `CorsOrigensConfiguradasIntegrationTest` | — | ✅ Fechado |
| DT-S1-01 | `status-sprint1.md` | Sem testes de componentes web/mobile nem E2E | — | Dívida técnica da Sprint 1 | Sprint 2 criou testes de web e 29 de E2E (Playwright) | ✅ web (257) e E2E (29 listados) | O **mobile** segue sem testes automatizados (limitação 3 da Release 1) | ✅ Fechado (parcial no mobile) |
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
| D-S3-01 | T75, PR #12 (zePadd) | `AgendamentoVisitaServiceTest.validaLimitesInclusivosDaJanela` falha | Média | O teste agendava `HOJE - 2` como limite da janela, mas o serviço verifica a data passada **antes** da janela (RQ-T75-04); o erro estava no teste | `961952f` (zePadd, 14/11); PR #12 mesclado em `8d54754` | ✅ passa nas execuções posteriores (672 em 16/11) | — | ✅ Fechado |
| OBS-12-01 | T75, PR #12 | `VisitaAgendada` sem o `@UniqueConstraint` de `(participacao_id, visita_planejada_id)` | — | Removido no PR #12 | Restaurado no PR #17 (`b2944f6`, 15/11) | ✅ lido na `develop` | — | ✅ Fechado |
| OBS-12-02 | T75, PR #12 | Sem `@PreUpdate`, `alteradoEm` deixava de ser atualizado sozinho | — | Removido no PR #12 | Restaurado no PR #17 | ✅ funcionou até o PR #21 | A T76 o removeu de novo (D-S3-11) | ✅ Fechado no PR #17; reaberto como D-S3-11 |
| OBS-12-03 | T75, PR #12 | `criado_por_id` e `criado_em` sem `updatable = false` | Baixa | Removido no PR #12 | Restaurado no PR #17 | ✅ lido na `develop` | — | ✅ Fechado |
| OBS-12-04 | T75, PR #12 | Códigos e tipo de erro do serviço diferiam do contrato (`AGENDAMENTO_DUPLICADO`, `DATA_PASSADA`, `ESTUDO_INVALIDO`, `VISITA_INCOMPATIVEL`, `REAGENDAMENTO_INVALIDO` contra `VISITA_JA_AGENDADA`, `DATA_NO_PASSADO` (400), `ESTUDO_FORA_DE_RECRUTAMENTO_OU_EXECUCAO`, `VISITA_NAO_ESTA_AGENDADA`) | Média | Serviço escrito sem seguir o contrato | Corrigido na T76 (`afc9bc6`, zePadd), mesclada no PR #21 | ✅ `ContratoOpenApiSprint3Test` (34 casos) e leitura da `develop`: os códigos antigos não existem mais | — | ✅ Fechado |
| D-S3-02 | T75 | `AgendamentoVisitaServiceTest` com 3 falhas (H2 e PostgreSQL) | Média | `HOJE` e `AGORA` vinham do relógio real, enquanto o serviço recebia `Clock` fixo em 12/11/2026 | `HOJE`/`AGORA` derivados do mesmo `Clock` fixo: `759377b` (`task/t75/domain`), consolidada no PR #17 (`b2944f6`) | ✅ `AgendamentoVisitaServiceTest` (10 testes) | — | ✅ Fechado |
| D-S3-03 | T96 (processo) | Push direto na `develop`, sem Pull Request (`eb37e3a`, 15/11) | Média | Branch criada com `checkout -b x origin/develop`, que define o upstream como a `develop` | `git branch --unset-upstream`; próximas branches com `--no-track` | ✅ o risco foi eliminado para as próximas branches | O código da T96 está na `develop` **sem PR**; ⏳ pedir o review pelo link do commit | 🟡 Contornado |
| D-S3-04 | `task80-85` (José Agra Neto) | 32 erros em `DesfechoEndpointTest` | Média | O teste cria a participação pela API (o JPA ainda não gravou) e insere a visita por SQL puro; o banco recusa pela FK `fk_visitas_agendadas_participacao`. O código dos endpoints estava correto | `CenarioDesfecho.visitaAgendada` ganhou um overload com `EntityManager` que faz `flush()` antes do INSERT; o `DesfechoEndpointTest` (`@Transactional`) o usa. O teste de reagendamento, antes `@Disabled("T76")`, foi ligado. Mesclada no PR #20 | ✅ `DesfechoEndpointTest` 33 testes, 0 ignorados; backend completo: 672 testes, 0 falhas; nenhum `@Disabled` na `develop` | — | ✅ Fechado |
| D-S3-05 | `task80-85` | Conflito com a `develop` (T76, T77, T90 e outros) | Média | Branch atrás da `develop`. 5 conflitos: `VisitaAgendadaResponse` (T76 e T82 criaram o mesmo DTO), `ContratoOpenApiSprint3Test`, `DemoDataIntegrationTest`, `acompanhamento.css` e `mensagensAcompanhamento.ts` (T77 e T83 criaram o mesmo arquivo) | Merge da `develop` na branch: DTO da T76 (assinatura com `Clock`) e `VisitaAgendadaService` adaptado; contrato e demo: união dos testes dos dois lados; CSS: união; mensagens: dois mapas (`mensagemDeAgendamento` e `mensagemDeAcompanhamento`), porque o código `ESTUDO_FORA_DE_RECRUTAMENTO_OU_EXECUCAO` tem texto diferente em cada tela. Mesclada no PR #20 | ✅ Sem marcadores de conflito; backend e web passam | — | ✅ Fechado |
| D-S3-06 | `task80-85` | Tasks incompletas e bloqueadas | Média | T77, T78 e T79 (PBI-19, I1) não estavam na `develop` | T77 (PR #23) e T83 encaixada (PR #20): `AcoesDaVisita` renderiza `BotaoRegistrarDesfecho`; selo nas telas; `DesfechoRegistrado` na tela da visita; confirmação do desfecho preservada | ✅ T80, T81, T82, T83 e T85 na `develop`; T77 na `develop` | T84 (RQ-T84-07 a 10) por fazer. T86 não iniciada. T78 e T79 não verificadas. Os documentos de design citam `task80-86`; o nome real era `task80-85` | 🟡 Parcial |
| D-S3-07 | T76 | `AgendamentoVisitaServiceTest.rejeitaParticipacaoInativaAntesDasDemaisRegras` falhava | Média | `validarEstadoDoEstudo` rodava antes da checagem do participante e trocava o código devolvido | `validarRegrasDeAgendamento` checa primeiro o participante (`PARTICIPANTE_RETIRADO`) e depois o estado do estudo. Commit `c298605` | ✅ `AgendamentoVisitaServiceTest` (10 testes) | — | ✅ Fechado |
| D-S3-08 | T76 (CI H2) | `AgendamentoEndpointTest.filtraEPaginaAgenda` voltava lista vazia | Média | Fuso horário: o filtro usava o "hoje" do relógio da aplicação (America/Recife, 15/11); a visita de demonstração usa `current_date` do banco, que no runner (UTC, 00:20) já era 16/11 | O teste lê a data real da visita no banco e filtra por ela. Commit `17a4030` | ✅ passa em fuso local e com `-Duser.timezone=UTC` | — | ✅ Fechado |
| D-S3-09 | T76 (CI PostgreSQL) | `could not determine data type of parameter $2` em `buscarAgenda` | Média | A consulta usava `(:de is null or ...)`; com valor nulo o PostgreSQL não infere o tipo (o H2 aceita) | Consulta sem parâmetro nulo (`>= :de`, `<= :ate`, `situacao in :situacoes`); `AgendaService` passa 1900-01-01 a 2999-12-31 e todas as situações quando não há filtro; teste novo sem filtros. Commit `d594e19`; mesclada no PR #21 | ✅ H2: backend completo, 672 testes | ❌ **PostgreSQL não executado nesta máquina** (Docker Desktop desligado); só o job do CI confirma | 🟡 Corrigido e mesclado, aguarda confirmação do CI |
| D-S3-10 | T76 (rodada local) | `DemoDataIntegrationTest`: `demoCarregaEstudoDeAcompanhamento` e `cargaDoAcompanhamentoEIdempotente` esperavam 3 participantes / 3 visitas e encontraram 5 / 8 | Baixa | Não esclarecida (dados de T90/T95 no mesmo estudo; suspeita de `target/` ou dados locais desatualizados) | Nenhuma alteração por causa dele | ✅ Passa no CI (H2 e PostgreSQL) e na execução de 16/11 (9 testes, 0 falhas) | Não se reproduziu; causa não confirmada | 🟡 Não esclarecido |
| D-S3-11 | T76 (`afc9bc6`) | A T76 removeu o `@PreUpdate` de `VisitaAgendada`, voltando ao ponto da OBS-12-02 | Baixa | Auditoria passou a ser feita só pelo serviço | A decidir pelo revisor: restaurar o `@PreUpdate` ou aceitar a auditoria só pelo serviço | ✅ Sem efeito hoje: reagendamento e desfecho (T81) definem `alteradoPor`/`alteradoEm` explicitamente | Uma alteração futura fora desses serviços deixaria `alteradoEm` desatualizado | 🟡 A decidir |
| D-S3-12 | Verificação local (processo) | `cmd //c mvnw.cmd ...` não rodava (comando não reconhecido) e com `-q` parecia sucesso | Baixa | Comando inválido no shell, sem mensagem de erro com `-q` | Refeitas com `./mvnw.cmd`, sem `-q`; qualquer "passou" feito de outro modo é desconsiderado | ✅ rodadas com `./mvnw.cmd` | — | ✅ Resolvido |
| D-S3-13 | Ferramentas (T97 e worktree) | Heredoc grande no Bash falhou ao criar arquivos de teste; Python indisponível para um patch no worktree temporário | Baixa | Limitação do ambiente | Usadas as ferramentas de escrita e edição de arquivos | ✅ | Sem impacto no código | ✅ Contornado |
| D-S3-14 | T77 (CI, job "Ambiente Docker (T67)", passo `docker compose up --build -d --wait`) | O build da imagem do backend falha em `RUN mvn -B -q dependency:go-offline` (Dockerfile, linha 9) com `Non-resolvable parent POM ... spring-boot-starter-parent:pom:4.1.1 ... status code: 429, Too Many Requests` em `repo.maven.apache.org` | Média | **Limite de requisições do Maven Central** (HTTP 429) no runner do CI, que baixava o POM pai e as dependências sem tentar de novo. É falha de infraestrutura externa e intermitente, não do código: a imagem do `web` e a do PostgreSQL foram construídas sem erro, e o `pom.xml` não mudou. **Não é erro do banco**, apesar de aparecer no job que sobe o Docker Compose | Cada passo do `mvn` no Dockerfile do backend repete até 5 vezes, esperando 20, 40, 60 e 80 s. Mesclada no PR #24 (`25d4c7e`) | ✅ Lógica do laço simulada (sucesso direto, após 2 e 4 falhas, e falha final depois de 5 tentativas) | ❌ A imagem **não foi construída nesta máquina** (Docker desligado); só o job do CI confirma. Não resolve um bloqueio longo do Maven Central | 🟡 Corrigido e mesclado, aguarda confirmação do CI |

---

## 5. Registro de alterações (o que foi mudado e como está)

| # | Alteração | Resolve | Commit / PR | Funciona | Não funciona / não verificado |
|---|---|---|---|---|---|
| A-01 | `HOJE`/`AGORA` do teste da T75 derivados do `Clock` fixo | D-S3-02 | `759377b` → PR #17 `b2944f6` | ✅ `AgendamentoVisitaServiceTest` | — |
| A-02 | Restaurar `@UniqueConstraint`, `@PreUpdate`, `updatable = false` na entidade | OBS-12-01/02/03 | PR #17 `b2944f6` | ✅ lido na `develop` | A T76 removeu o `@PreUpdate` (D-S3-11) |
| A-03 | Backend da T96 (serviço, controller, repositórios, testes, YAML/DTO/`schema-sprint3.d.ts`) | RF15 | `eb37e3a` (direto na `develop`) | ✅ `HistoricoParticipanteServiceTest`, `HistoricoParticipanteEndpointTest`, `ContratoOpenApiSprint3Test` | 🟡 sem PR (D-S3-03) |
| A-04 | Web da T97 (ficha do participante, linha do tempo, rota, `formatarDataHora`, links em `AbaParticipantes`), atualizada com a `develop` antes do PR: 8 conflitos resolvidos (`datas.ts`, `SeloSituacaoVisita`, `acompanhamento.css`, `AbaParticipantes`, `router.tsx` e testes); `situacoesVisita.ts` removido e a lista do filtro movida para `situacaoVisita.ts` | RF15 | PR #25 (`3257595`) | ✅ 257 testes de web; no navegador com o backend real: ficha, resumo 1/1/1/0, linha do tempo, resultado 130/85 mmHg e link para a tela da visita | ❌ E2E não executado; ❌ PostgreSQL só no CI |
| A-05 | `git branch --unset-upstream` na branch da T96 | D-S3-03 | configuração local | ✅ | — |
| A-06 | `flush` em `DesfechoEndpointTest` (overload de `CenarioDesfecho.visitaAgendada` com `EntityManager`) e teste de reagendamento ligado | D-S3-04 | PR #20 (`77287c9`) | ✅ 33 testes do desfecho e backend completo (672) | — |
| A-07 | Reordenar regras: participante antes do estado do estudo | D-S3-07 | `c298605` → PR #21 | ✅ `AgendamentoVisitaServiceTest` (10) | — |
| A-08 | Teste lê a data da visita no banco em vez do "hoje" do relógio | D-S3-08 | `17a4030` → PR #21 | ✅ passa em fuso local e `UTC` | — |
| A-09 | Consulta da agenda sem parâmetros nulos; chamada sem filtros no teste | D-S3-09 | `d594e19` → PR #21 | ✅ H2 | ❌ PostgreSQL só no CI |
| A-10 | Telas de agendamento da T77: `api/agendamento.ts`, `AbaAgenda`, `AgendamentoParticipantePage`, `VisitaAgendadaPage`, `AcoesDaVisita`, `FormularioDataHora`, `abasVisita.ts`, `mensagensAcompanhamento.ts`; aba Participantes com progresso e botão Agendamento; abas Participantes e Agenda para o pesquisador; rotas novas; E2E `acesso-por-perfil` atualizado | RF12 (T77) | PR #23 (`163b5b7`) | ✅ 189 testes de web na época, `tsc` e `eslint` limpos; no navegador com o backend real: progresso, agendamento com a data sugerida, erro `DATA_FORA_DA_JANELA` com a janela junto do campo e foco, agenda, tela da visita e pesquisador sem botões de escrita | ❌ E2E `acesso-por-perfil.spec.ts` não executado |
| A-11 | Dockerfile do backend: cada passo do `mvn` que baixa dependências repete até 5 vezes, esperando 20, 40, 60 e 80 s | D-S3-14 | PR #24 (`25d4c7e`) | ✅ Lógica do laço simulada | ❌ Imagem não construída aqui; só o CI confirma |
| A-12 | Merge da `develop` na `task80-85` (5 conflitos) e encaixe da T83 nas telas da T77: `AcoesDaVisita` com `BotaoRegistrarDesfecho`, `SeloSituacaoVisita` na agenda, na ficha e na tela da visita, `DesfechoRegistrado` na tela da visita, confirmação do desfecho preservada e formatador de data comum | D-S3-05, D-S3-06 | PR #20 (`77287c9`) | ✅ Web: 231 testes na época; no navegador: selo, botão Registrar desfecho e desfecho registrado pelo diálogo | ❌ E2E não executado; ⏳ T84 (RQ-T84-07 a 10) e T86 por fazer |
| A-13 | Atualização desta rastreabilidade, do registro de defeitos, do relatório de testes e do README, tudo em tabelas, com a execução de 16/11 | todos | branch de documentação própria | ✅ Números medidos na `develop`: 672 testes de backend, 257 de web, cobertura 99,0% | ⏳ Falta o PR; o CI do GitHub não foi consultado daqui |

## 6. PRs integrados na `develop` desde o início da Sprint 3

| PR | Conteúdo | Data | Itens de rastreabilidade ligados |
|---|---|---|---|
| #8 | Modelo de domínio da visita agendada (T73) | 12/11 | D-S3-00 |
| #10 | T73 alinhada ao documento de design | 12/11 | D-S3-00 |
| #11 | Contratos e dados de demonstração (T74 e T95) | 12/11 | — |
| #12 | Regras de agendamento (T75) | 14/11 | D-S3-01, OBS-12-01 a 04 |
| #13 | Modelo e migration dos resultados (T87) | 13/11 | — |
| #14 | Proteção da branch `main` (regras, fluxo de PR e evidências) | 13/11 | — |
| #15 | Relatório de testes das Sprints 1 a 3 e registro de defeitos | 13/11 | todos os IDs de defeitos |
| #16 | Contrato OpenAPI dos resultados (T88) | 14/11 | — |
| #17 | Sugestões de mudanças da T75 | 15/11 | D-S3-02, OBS-12-01 a 03 |
| #18 | Regras de registro dos resultados (T89) | 14/11 | — |
| #19 | Endpoints, autorização, Swagger e demonstração dos resultados (T90) | 15/11 | — |
| (direto) `eb37e3a` | Consulta agregada do histórico do participante (T96) | 15/11 | D-S3-03 |
| #21 | Endpoints do agendamento (T76) | 15/11 | OBS-12-04, D-S3-07, 08, 09, 11 |
| #22 | Rastreabilidade de erros e alterações | 15/11 | — |
| #24 | Retry do Maven no Dockerfile | 16/11 | D-S3-14 |
| #23 | Telas de agendamento (T77) | 16/11 | A-10 |
| #20 | Desfecho da visita (T80 a T85, parcial) | 16/11 | D-S3-04, 05, 06 |
| #25 | Tela do histórico do participante (T97) | 16/11 | A-04 |

## 7. Pendências por responsável (ordem sugerida)

| # | Responsável | Ação | Refs |
|---|---|---|---|
| 1 | Time | Conferir no GitHub os jobs "Backend (PostgreSQL)" e "Ambiente Docker (T67)" da `develop`; se o Docker voltar a falhar por 429, decidir um cache ou espelho do Maven no CI | D-S3-09, D-S3-14 |
| 2 | José / I1 | T84 (RQ-T84-07 a 10), T86 (E2E e documentação do RF13), T78 e T79 | D-S3-06 |
| 3 | I7 | Pedir o review do commit `eb37e3a` (T96); decidir com o revisor da T76 sobre o `@PreUpdate`; avisar o I3 (RQ-T76-13) | D-S3-03, D-S3-11 |
| 4 | Time | Executar os E2E (Playwright) com backend e web no ar: `acesso-por-perfil` foi alterado na T77 e nenhum E2E novo foi escrito desde a Sprint 2 | A-04, A-10, A-12 |
| 5 | Time | Registrar os defeitos novos no Azure Boards (padrão da T67: Task "Bug" filha do PBI e ligada à task de origem) | seção 4.3 |
| 6 | Acompanhamento | DT-S1-02 e DT-S1-03; investigar D-S3-10 só se voltar a falhar | — |
