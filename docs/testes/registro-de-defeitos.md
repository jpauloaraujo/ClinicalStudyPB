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
| D-T109-01 | 19/11/2026 | Sprint 3 · T109 (primeira rodada do backend) | `ContratoOpenApiSprint3Test.operacoesDeclaramAsRespostasDeErro` falhou: `GET /api/me/estudos` declara só `200, 401, 403`, e o teste exige `404` e `409` em toda operação | Baixa | Fechado | `ContratoOpenApiSprint3Test.operacoesDeclaramAsRespostasDeErro` | Erro da **premissa do teste** (escrita para a agenda, com 404/409 possíveis): a lista do `/me` não tem id nem regra de negócio. Teste refinado (404 e 409 deixam de ser exigidos onde são impossíveis; nada removido). Detalhe em [testes-t109](testes-t109-acesso-do-participante-e-endpoints-me.md#4-falhas-encontradas-e-correções) |
| D-T109-02 | 19/11/2026 | Sprint 3 · T109 (primeira rodada do backend) | `MeEndpointTest.detalheDoEstudoDaConta` falhou: `jsonPath("$.resumo.length()")` devolveu `null` | Baixa | Fechado | `MeEndpointTest.detalheDoEstudoDaConta` | Erro do **teste novo** (a função `length()` do JsonPath não se aplica a texto); o código estava correto. Asserção trocada por `matchesPattern("(?s).{1,160}")`. Detalhe em [testes-t109](testes-t109-acesso-do-participante-e-endpoints-me.md#4-falhas-encontradas-e-correções) |
| D-T110-01 | 20/11/2026 | Sprint 3 · T110 (primeira rodada do app) | `npm run typecheck` falhou em `login.tsx`: `useRef<TextInput>` não tem `focus` neste React Native | Baixa | Fechado | `npm run typecheck` | Erro do **código novo**; `ref` tipado com `ComponentRef<typeof TextInput>`. [testes-t110](testes-t110-login-e-logout-no-app.md#4-falhas-encontradas-e-correções), F-T110-01 |
| D-T110-02 | 20/11/2026 | Sprint 3 · T110 (primeira rodada do app) | `npm run lint` falhou em `(tabs)/index.tsx`: `react-hooks/set-state-in-effect` | Baixa | Fechado | `npm run lint` | Erro do **código novo**; `setState` só em callbacks assíncronos. F-T110-02 |
| D-T110-03 | 20/11/2026 | Sprint 3 · T110 (testes novos) | `login.test` estourou 5000 ms no primeiro teste (intermitente, carga das 8 suítes em paralelo) | Baixa | Fechado | `login.test.tsx` | Erro do **ambiente de teste**; `jest.setTimeout(30000)` no arquivo novo. F-T110-03 |
| D-T110-04 | 20/11/2026 | Sprint 3 · T110 (testes novos) | O teste de envio pendente travava: `await fireEvent.press` esperava a promessa que o próprio teste controlava; três reparos seguidos (F-T110-05 a 07) causaram cascata de 11 falhas, `props.onPress` indefinido e `{enter}` inválido | Baixa | Fechado | `login.test.tsx` › "desabilita o botão durante o envio…" | Erro do **teste novo**; tela correta. Passou a usar `userEvent` (`press` e `type(..., { submitEditing: true })`). F-T110-04 a 07 |

Nota: estas linhas foram registradas na branch da task (como a T109); a rastreabilidade de erros e o `README.md` raiz ainda precisam ser atualizados em branch de documentação própria (pendência P23).

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

---

## 4. Falhas de teste da T111 (vínculo da conta do participante)

> Seção própria da T111, em ordem de ocorrência. Regra do projeto: **a falha é registrada aqui antes de ser corrigida** e nenhum teste é excluído. Os testes e o contexto estão em [testes-t111](testes-t111-vinculo-da-conta-do-participante.md). Os registros D-T109-01/02 e D-T110-01 a 04 estão nas branches da T109 e da T110.

### D-T111-01 · Dados do teste novo repetiam nome e data de nascimento no mesmo estudo

| Campo | Valor |
|---|---|
| **Data e rodada** | 22/11/2026 · 1ª rodada completa do backend (`./mvnw test`, 718 testes) |
| **Task e requisito** | T111, RQ-T111-01 (migration `V6_24_1`) |
| **Testes** | `FlywayMigrationIntegrationTest.uniqueDaContaDoCandidato` e `contasDiferentesConvivem` (2 erros, ambos meus, escritos nesta task) |
| **Mensagem** | `DuplicateKey ... Unique index or primary key violation: "uk_candidatos_estudo_nome_nascimento" (estudo_id, nome_ficticio, data_nascimento)` ao inserir o segundo candidato |
| **Causa** | Erro do **teste novo**, não da migration: o helper `inserirCandidatoComConta` inseria todos os candidatos de um estudo com o mesmo `nome_ficticio` e a mesma data de nascimento, e o índice único da Sprint 2 (`uk_candidatos_estudo_nome_nascimento`, V4) recusa a segunda linha antes de o teste chegar ao índice novo. A migration `V6_24_1` não tem relação com o erro |
| **Determinístico?** | Sim |
| **Correção** | Gerar o `nome_ficticio` a partir do código do candidato, como nos demais testes |
| **Estado** | **Fechado** em 22/11/2026: corrigido e confirmado nas rodadas seguintes (backend completo: 757 testes, 0 falhas; ver [testes-t111](testes-t111-vinculo-da-conta-do-participante.md), seção 5) |

### D-T111-02 · `MeEndpointTest.listaSoOsEstudosDaConta` vincula duas participações à mesma conta

| Campo | Valor |
|---|---|
| **Data e rodada** | 22/11/2026 · 1ª rodada completa do backend (`./mvnw test`, 718 testes) |
| **Task e requisito** | T111, RQ-T111-01 (índice único `uk_candidatos_usuario`); pendência **P10** da T109 |
| **Teste** | `MeEndpointTest.listaSoOsEstudosDaConta` (T109) |
| **Mensagem** | `DuplicateKey ... Unique index or primary key violation: "uk_candidatos_usuario ON candidatos(usuario_id)" VALUES (UUID '44444444-4444-4444-8444-444444444444')` em `incluir:81`, na linha 101 do teste |
| **Causa** | **Esperada e prevista** na [pendência P10](../design/sprint3/T109-pendencias-para-proximas-tasks.md): o teste liga **dois candidatos à mesma conta** (`CAND-901` e `CAND-902`), o que a regra 7 (um para um) agora proíbe no banco. O erro é da **premissa do teste**; o código de produção está certo e a falha prova que o índice funciona |
| **Determinístico?** | Sim |
| **Correção** | Reescrever o teste sem repetir a conta: a lista de uma conta traz um estudo, e a ordem "da inclusão mais recente para a mais antiga" passa a ser provada no nível do serviço (`MeusEstudosService`, sem o `UNIQUE`). O teste **não é excluído** |
| **Estado** | **Fechado** em 22/11/2026: corrigido e confirmado nas rodadas seguintes (backend completo: 757 testes, 0 falhas; ver [testes-t111](testes-t111-vinculo-da-conta-do-participante.md), seção 5) |

### D-T111-03 · Os helpers dos testes de endpoint e de corrida repetiam o nome do candidato (mesma causa do D-T111-01)

| Campo | Valor |
|---|---|
| **Data e rodada** | 22/11/2026 · 2ª rodada (testes da T111 e dos arquivos que ela tocou, 133 testes, 2 erros) |
| **Task e requisito** | T111, RQ-T111-03/04 (regras e corrida do vínculo) |
| **Testes** | `ContaParticipanteEndpointTest.contaJaVinculada` (linha 264) e `ContaParticipanteConcorrenciaTest.mesmaContaEmVariosParticipantes` (linha 134), ambos escritos nesta task. Na rodada, os outros 14 testes do endpoint e o outro de corrida passaram |
| **Mensagem** | `DuplicateKey ... uk_candidatos_estudo_nome_nascimento` ao inserir o **segundo** candidato do mesmo estudo (só os testes que criam dois candidatos no mesmo estudo chegam a esse ponto) |
| **Causa** | Erro do **teste novo**, igual ao D-T111-01: os helpers `criarParticipacao` e `novaParticipacao` fixam `nome_ficticio` e `data_nascimento`, e o índice único da Sprint 2 recusa o segundo candidato igual no mesmo estudo. Repeti o padrão porque copiei os helpers sem tornar o nome único. O vínculo e o banco estão certos |
| **Determinístico?** | Sim |
| **Correção** | Nome do candidato derivado do código (`"Pessoa " + codigo`), nos dois helpers |
| **Estado** | **Fechado** em 22/11/2026: corrigido e confirmado nas rodadas seguintes (backend completo: 757 testes, 0 falhas; ver [testes-t111](testes-t111-vinculo-da-conta-do-participante.md), seção 5) |

### D-T111-04 · Quatro testes antigos da web estouraram 5 s na rodada completa e passaram isolados

| Campo | Valor |
|---|---|
| **Data e rodada** | 22/11/2026 · 1ª rodada completa da web (`npx vitest run`, 275 testes: 271 passaram, 4 falharam) |
| **Task e requisito** | T111 (a mudança da ficha e da área de ações); nenhum requisito específico |
| **Testes** | `mocks.test.tsx` (2: "a aba de critérios lista os dados do mock…" e "o mock devolve o erro de duplicidade…"), `FichaCandidatoPage.test.tsx` ("mostra os dados do candidato e as três seções…") e `AbaAgenda.test.tsx` ("lista a agenda do estudo…"). Nenhum é da T111 e nenhum dos arquivos foi alterado |
| **Mensagem** | Estouro do tempo do teste (5,0 a 7,1 s; o limite do Vitest é 5 s) |
| **Causa** | Provável **carga da máquina** na execução em paralelo: os mesmos 3 arquivos, rodados sozinhos logo depois, passaram (33 de 33). Os arquivos da T111 (ficha, área de ações, diálogo) passaram na mesma rodada. Causa não confirmada além disso |
| **Determinístico?** | **Não** (intermitente) |
| **Correção** | Nenhuma no código. Reexecutar a suíte completa e conferir no CI; se voltar a ocorrer, tratar como defeito de teste lento (aumentar o limite desses arquivos) em registro próprio |
| **Estado** | **Fechado (não reproduzido)** em 22/11/2026: a suíte completa da web foi reexecutada (27 arquivos, 315 testes) e passou inteira, inclusive com o `mvn verify` rodando ao mesmo tempo. Se voltar no CI, abrir registro próprio |

### D-T111-05 · Dois erros de autoria nos testes novos da web (sintaxe e seletor ambíguo)

| Campo | Valor |
|---|---|
| **Data e rodada** | 22/11/2026 · 1ª execução dos testes novos da web (`npx vitest run src/api/contaParticipante.test.ts src/pages/estudos/detalhe/acompanhamento`: 161 passaram, 1 falhou, 1 arquivo não executou) |
| **Task e requisito** | T111, RQ-T111-09, 10 e 12 (testes de componente) |
| **Testes** | (a) `AreaAcoesParticipante.test.tsx`: **o arquivo inteiro não executou** (`BabelError: Unexpected token (111:0)`; o `eslint` e o `tsc` acusaram `'}' expected`). (b) `FichaParticipantePage.test.tsx` › "depois de vincular, recarrega a conta, mostra a confirmação e esconde o botão": `Found multiple elements with the role "status"` |
| **Causa** | **Erros do teste novo**, nenhum do código de produção. (a) Ao acrescentar o `describe` novo com um script de substituição, removi sem querer o `})` que fechava o `describe` original da T97; os testes antigos não foram apagados, só ficaram sem o fechamento. (b) A ficha já tem outro elemento `role="status"` (a contagem "Exibindo N de N visitas" do filtro), então `getByRole('status')` sem escopo é ambíguo |
| **Determinístico?** | Sim |
| **Correção** | (a) Restaurar o fechamento do `describe` original. (b) Procurar a confirmação dentro da região "Ações do participante" |
| **Estado** | **Fechado** em 22/11/2026: corrigido e confirmado nas rodadas seguintes (backend completo: 757 testes, 0 falhas; ver [testes-t111](testes-t111-vinculo-da-conta-do-participante.md), seção 5) |

### D-T111-06 · Teste novo do Flyway usou uma consulta que só existe no H2 e quebrou no PostgreSQL (CI)

| Campo | Valor |
|---|---|
| **Data e rodada** | 22/11/2026 · CI do PR #42, job "Backend (testes no PostgreSQL)" (757 testes, 1 erro; os outros 5 jobs passaram) |
| **Task e requisito** | T111, RQ-T111-01 (a migration deve funcionar no H2 **e** no PostgreSQL) |
| **Teste** | `FlywayMigrationIntegrationTest.uniqueDaContaDoCandidato:208`, escrito nesta task |
| **Mensagem** | `BadSqlGrammar StatementCallback; bad SQL grammar [SELECT COUNT(*) FROM information_schema.indexes WHERE LOWER(index_name) = 'uk_candidatos_usuario']` |
| **Causa** | Erro do **teste novo**: `information_schema.indexes` existe no H2, mas **não no PostgreSQL**. A migration e o índice estão certos (as demais asserções do mesmo teste e o job H2 passaram). Era exatamente a lacuna que o documento de testes registrava ("não rodei o job do PostgreSQL localmente") |
| **Determinístico?** | Sim |
| **Correção** | Verificar a existência do índice pelo `DatabaseMetaData.getIndexInfo` do JDBC, que é portável, comparando o nome sem diferenciar maiúsculas; o resto do teste (aceita vários sem conta e recusa a mesma conta) não muda |
| **Estado** | **Fechado** em 22/11/2026: com a consulta portável, a suíte completa do backend foi executada contra um **PostgreSQL 16 real** (mesmo setup do job do CI): 757 testes, 0 falhas, `BUILD SUCCESS`; no H2 o `FlywayMigrationIntegrationTest` também passa (14 de 14) |
