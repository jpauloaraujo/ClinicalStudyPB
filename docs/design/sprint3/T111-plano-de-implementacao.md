# T111 · Plano de implementação: vínculo da conta do participante (backend e web)

> **Sprint 3** · PBI-24 · RF17 (e RF03 no mobile) · **Estimativa do planejamento:** 3h · **Depende de:** T109 (concluída, na branch atual) e T97 (ficha do participante, já presente: `FichaParticipantePage` e `AreaAcoesParticipante`)
> Guia da task: [T111-vinculo-da-conta-do-participante-backend-e-web.md](T111-vinculo-da-conta-do-participante-backend-e-web.md) (14 requisitos `RQ-T111-NN`). Contrato: [00-convencoes-e-contrato-compartilhado.md](00-convencoes-e-contrato-compartilhado.md). Pendências herdadas: [T109-pendencias-para-proximas-tasks.md](T109-pendencias-para-proximas-tasks.md) (P8, P9, P10).
> Caminhos relativos a `backend/ClinicalStudyPB/src/` (**BE**) ou `web/ClinicalStudyPB/src/` (**WEB**), salvo indicação.

## 0. Revisão de 22/11/2026 (leia primeiro)

O plano foi escrito em 21/11 e **revisado em 22/11** contra o estado real dos PRs. O que mudou:

| Fato verificado em 22/11 | Efeito no plano |
|---|---|
| **T101 (#39) e T102 (#40) estão abertos** (`REVIEW_REQUIRED`), não mergeados. A T101 já traz `V6_23_1__participant_withdrawal.sql`, a coluna `participacoes.situacao` (`ATIVO`/`RETIRADO`) e `Participacao.estaRetirada()` | A decisão 1 passa a ter **dois caminhos** (A e B, abaixo). A decisão 9 fica resolvida na direção certa: `V6_23_1` < `V6_24_1`, desde que o #39 entre antes |
| **T108 (#32), T109 (#33) e T110 (#38) estão aprovados e sem conflito**, mas **não mergeados** | A T111 **não pode** partir da `develop` hoje (ela não tem `AcessoParticipante` nem `/api/me/**`). Passo 0 reescrito |
| A correção de fuso (#41) entrou na `develop` | O E2E não falha mais entre 00h e 03h UTC; `VinculoContaService` e o resto do código novo devem usar o `Clock` da aplicação, nunca `now()` solto |
| `ContratoOpenApiSprint3Test.OPERACOES_ESPERADAS` na `develop` é 14 (T115 incluída); com a T109 mergeada continua 14 | A contagem do plano muda de 15 para **14 + 3 = 17** (e **19** se a T103 entrar antes). Decisão 8 corrigida |
| `registro-de-defeitos.md` agora tem D-T109-01/02 e D-T110-01 a 04 na branch da T110 | As falhas desta task continuam a numeração **D-T111-NN** (primeira falha: D-T111-01) |

### Pré-condições de entrada (conferir antes do Passo 0)

| # | Pré-condição | Como conferir | Se não estiver pronta |
|---|---|---|---|
| E1 | #32, #33 e #38 mergeados na `develop`, nessa ordem | `gh pr view 38 --json state` → `MERGED` | Partir da branch da T110 (como o plano de 21/11 previa) e rebasear depois |
| E2 | #39 (T101) mergeado, ou decisão explícita de seguir pelo **caminho B** | `gh pr view 39 --json state` | Caminho B: `participacaoAtiva` fixo em `true` com `TODO(T101)` |
| E3 | Confirmação do PO sobre "uma conta por candidato" (decisão 4) | Registrar a resposta no README | Seguir com a regra do design e registrar a premissa |

**Decisão 1 revisada (dois caminhos):**
- **Caminho A (T101 já na `develop`):** `participacaoAtiva(p)` devolve `!p.estaRetirada()`. O passo 2 do RQ-03 passa a ser **testável de ponta a ponta**: criar participação retirada (`retirar()`) e esperar 409 `PARTICIPANTE_RETIRADO`. O `TODO(T104)` da ficha web também pode ser resolvido (o tipo da ficha ganha a situação se o histórico a expuser; senão continua presumido).
- **Caminho B (T101 ainda aberta):** como no plano de 21/11 (método fixo `true`, `TODO(T101)`), com o ramo "retirado" coberto só por teste unitário que força o método. Registrar a pendência na T101.
- Qualquer que seja o caminho, o método continua **único e privado**, para que só o corpo mude.

### Índice de rastreabilidade deste plano

| Pergunta | Onde achar |
|---|---|
| De onde vem cada requisito? | Guia [T111](T111-vinculo-da-conta-do-participante-backend-e-web.md), seção 4 (`RQ-T111-01` a `14`) e seção 9 (especificação) |
| Qual passo entrega cada requisito? | Seção 4 deste plano (mapa RQ × entrega) |
| Quais arquivos serão criados ou alterados? | Seção 8 (inventário de arquivos) |
| Quais testes provam cada requisito? | Seção 4 (coluna "Verificação") e Passo 12 (documento `testes-t111-...`) |
| Quais pendências herdadas esta task fecha? | Seção 9 (P8, P9, P10 da [T109](T109-pendencias-para-proximas-tasks.md)) |
| Onde registrar falhas de teste? | `docs/testes/registro-de-defeitos.md`, linhas `D-T111-NN`, **antes** de corrigir |

## 1. Diagnóstico: o que já existe e o que falta

Estado verificado no código da branch `feat/task110-login-logout-app`:

| Item | Situação hoje | Consequência para a T111 |
|---|---|---|
| `Candidato.usuario` (`candidatos.usuario_id`) | Existe, opcional, **sem `UNIQUE`** | A migration `V6_24_1` cria o índice único. Última migration: `V6_21_1` |
| `AcessoParticipante` e `/api/me/**` (T109) | Prontos; `participacoesDe` devolve lista | Não mudar a assinatura (P9). O vínculo só alimenta `Usuario → Candidato → Participacao` |
| `Participacao.situacao` e retirada (T101/T103/T104) | **Não existem** (`Participacao` não tem `situacao`; há `TODO(T101)` em `MeusEstudosService`) | O RQ-03 passo 2 ("participação ativa") não pode ser lido do banco ainda. Ver decisão 1 |
| `AreaAcoesParticipante` (T97) | Existe, com a lista `acoesDoPerfil` e só o botão "Agendar visitas"; **devolve `null` para o pesquisador** | O botão entra como nova entrada da lista. A exibição da conta para o pesquisador (RQ-10) precisa ficar **fora** dessa área. Ver decisão 6 |
| `ParticipanteDoHistorico` (tipo da ficha) | Não traz `situacao` nem conta | A ficha busca a conta por `GET .../conta`; a situação "ativo" é presumida até a T104 |
| `mensagensAcompanhamento.ts` | Tem agendamento, desfecho e resultados; `PARTICIPANTE_RETIRADO` já traduzido só para agendar | Acrescentar um bloco do vínculo |
| `R__demo_24_conta_participante.sql` | **Já existe** (T109): liga `demo.participante@` ao **P5**; `participante@` fica sem vínculo | O guia manda "Criar", mas é "Alterar". O guia também manda P1 e **P6**; o P6 e o `R__demo_23` não existem. Ver decisão 3 |
| `openapi-sprint3.yaml` | 14 operações após T109 e T115 (`/api/me/**` incluídas) | **14 + 3 = 17** (T109 e T115 incluídas; 19 se a T103 entrar antes). Ver decisão 8 |
| `MeEndpointTest.listaSoOsEstudosDaConta` | Liga **a mesma conta a dois candidatos** | **Vai falhar** com o `UNIQUE` (P10). Registrar a falha antes de corrigir |
| `DemoContaParticipanteTest` | Verifica o vínculo ao P5 | Ajustar conforme a decisão 3 |

## 2. Decisões de projeto

1. **Retirada ainda não existe: um ponto único para "participação ativa".** Em `VinculoContaService`, um método privado `participacaoAtiva(Participacao)` devolve `true` com `// TODO(T101)`, no mesmo estilo de `MeusEstudosService`. O passo 2 da ordem do RQ-03 fica no lugar certo no código e só o corpo do método muda quando a T101 entrar. O ramo 409 `PARTICIPANTE_RETIRADO` não é testável de ponta a ponta agora: cobri-lo com teste unitário que força o método (visibilidade de pacote ou subclasse de teste) ou registrar como pendência para a T101/T113. Acrescentar o `TODO(T101` à lista do `grep` de fechamento da T101 (P2).
2. **Corrida e um para um sem confiar só na leitura.** Gravar o vínculo com `UPDATE candidatos SET usuario_id = :usuario WHERE id = :candidato AND usuario_id IS NULL` (método `@Modifying` no `CandidatoRepository`) e conferir as linhas afetadas: `0` significa que outra requisição vinculou antes, então 409 `PARTICIPACAO_JA_TEM_CONTA`. A violação do `UNIQUE` (mesma conta em dois candidatos) estoura **dentro** desse `UPDATE`, então o `try/catch` de `DataIntegrityViolationException` fica no serviço e vira 409 `CONTA_JA_VINCULADA`. Com `save()` e `@Transactional`, o erro só apareceria no `commit`, fora do `try`, e viraria 500. As checagens de leitura (RQ-03 passos 5 e 6) continuam, para devolver o código certo no caso comum.
3. **Seed: respeitar o que existe.**
   - `participante@clinicalstudy.test` → **P1** (CAND-001, candidato `d6300000-0000-4000-8000-000000001911`, criado em `R__demo_19_visitas_agendadas.sql`), como pede o guia.
   - `demo.participante@clinicalstudy.test` **continua no P5** (T109). O P6 não existe até a T105; mover o vínculo agora quebraria o `DemoContaParticipanteTest` e a idempotência. Registrar como pendência para a **T105** (RQ-T105-08): migrar o vínculo para o P6 quando ele existir, com `UPDATE ... SET usuario_id = NULL` do P5 antes. Desvio documentado do RQ-T111-13.
   - **Efeito colateral:** no perfil `demo`, `participante@` deixa de ser a conta "sem vínculo" usada pela T110 (roteiro 6, pendência P11). O aviso "Você ainda não está incluído em nenhum estudo." passa a ser demonstrado criando uma conta nova pelo administrador (é o fluxo real do RF01) ou, nos testes automáticos, no perfil de teste, onde `participante@` continua sem vínculo (o seed `R__demo_*` só roda no perfil `demo`). Atualizar o roteiro e o documento de testes da T110 e a P11. Os mocks do app não mudam.
4. **Um candidato por conta tem uma consequência de produto.** Candidato é por estudo; com `UNIQUE (usuario_id)`, quem participa de dois estudos precisa de **duas contas**. É o que o design manda (regra 7), mas **confirmar com o PO (T94)**, como a decisão 8 das convenções, e registrar no README.
5. **Reenviar a mesma conta à mesma participação não é idempotente:** devolve 409 `PARTICIPACAO_JA_TEM_CONTA` (a verificação 5 vem antes da 6). Sem desvincular (RQ-05).
6. **Web: conta exibida na ficha, botão na área de ações.** A ficha (`FichaParticipantePage`) passa a buscar `contaParticipanteApi.buscar(participacaoId)` em paralelo ao histórico. `contaParticipanteApi.buscar` trata **404 como "sem conta"** (devolve `null`), nunca como erro de tela. A linha "Conta vinculada: nome (e-mail)" é desenhada pela ficha para os **dois** perfis. `AreaAcoesParticipante` ganha as props opcionais `conta` (já carregada) e usa o resultado para esconder o botão. Assim o pesquisador vê a conta em leitura mesmo com a área vazia. A situação "ativo" é presumida até a T104 (comentário `TODO(T104)`); o servidor recusa com 409 de qualquer forma.
7. **Tipos pelo gerador, não à mão.** Editar o YAML e rodar `npm run api:tipos` na web (gera `schema-sprint3.d.ts`); reexportar `ContaVinculadaResponse`, `ContaParticipanteResponse` e `VincularContaRequest` em `WEB api/tipos.ts` e `contaParticipanteApi` em `WEB api/index.ts`. O app mobile **não** consome estes endpoints; não regenerar os tipos dele.
8. **Contagem do contrato (revisada em 22/11):** com a T109 e a T115 na `develop`, `OPERACOES_ESPERADAS` é 14; a T111 soma 3 e vai para **17**. Se a T103 entrar antes, 19. Onde este plano diz "15 operações", leia "14 + 3". O mesmo vale para o conjunto `OPERACOES_CONTA` novo no teste. A exigência de 409 do `operacoesDeclaramAsRespostasDeErro` vale para o `PUT` do vínculo (declarar 401, 403, 404 e 409).
9. **Ordem das migrations (risco real).** Com `outOfOrder` desligado e `validate-on-migrate=true`, um banco que aplicar a `V6_24_1` **recusa** depois a `V6_23_1` (T101). O plano da Sprint integra o PBI-23 antes do PBI-24, mas a T109 foi feita "opção A", sem a retirada. Antes de abrir o PR, confirmar que a T101 não vai entrar depois. Se for entrar, combinar com o dono da T101 usar versão **maior** que `V6_24_1` (por exemplo `V6_24_2__participant_withdrawal.sql`) e corrigir a seção 5 das convenções no mesmo PR.
10. **Segurança dos DTOs.** `ContaVinculadaResponse` e `ContaParticipanteResponse` são records próprios (`usuarioId`, `nome`, `email`, e `participacaoId` no primeiro). Nunca devolver `Usuario`, `senha`, `perfil` nem `ativo`. A consulta de disponíveis projeta direto para o DTO.

## 3. Passos de implementação

Ordem pensada para que cada passo deixe o projeto compilando e os testes verdes.

### Passo 0 · Branch e linha de base (10 min)

- Conferir E1 a E3 (seção 0). Criar `feat/task111-vinculo-conta-participante` a partir da `develop` **se a T109 já estiver nela**; do contrário, a partir de `origin/feat/task109-acesso-participante-endpoints-me` (a T111 só depende da T109, não da T110).
- Rodar `./mvnw test` no backend e `npm test`, `npm run lint`, `npm run build` na web; anotar a linha de base.
- Deixar o `docs/tutorial-inicializacao.md` (não versionado) fora deste PR.

### Passo 1 · Migration `V6_24_1` (20 min)

- BE `db/migration/V6_24_1__unique_candidate_account.sql`: `CREATE UNIQUE INDEX IF NOT EXISTS uk_candidatos_usuario ON candidatos (usuario_id);` (vários `NULL` continuam permitidos no H2 e no PostgreSQL).
- Em `FlywayMigrationIntegrationTest`: (a) o índice existe; (b) duas contas `NULL` convivem; (c) duas linhas com a mesma conta são recusadas; (d) a migration aplica em banco já populado pela Sprint 2 (o teste existente das migrations antigas cobre a base). Rodar também contra PostgreSQL (job `Backend (PostgreSQL)` ou `SPRING_DATASOURCE_*` local).

### Passo 2 · Contrato OpenAPI (20 min)

- `docs/api/openapi-sprint3.yaml`: três operações novas (a 15ª a 17ª) com `operationId`, `security`, exemplos e as respostas 401, 403, 404 e 409 (com a lista dos códigos nos exemplos); esquemas `VincularContaRequest`, `ContaVinculadaResponse`, `ContaParticipanteResponse`.
- `ContratoOpenApiSprint3Test`: `OPERACOES_ESPERADAS = 17`, novo `OPERACOES_CONTA` incluído em `containsAll` e no teste de "todo caminho do YAML existe na API" (decisão 8). Este teste falha até o Passo 4 terminar; é esperado.

### Passo 3 · DTOs, repositórios e serviço (60 min)

- BE `controller/dto/`: `VincularContaRequest(@NotNull UUID usuarioId)`, `ContaVinculadaResponse`, `ContaParticipanteResponse`, todos com `@Schema` como os demais.
- BE `repository/UsuarioRepository`: `listarContasDisponiveis()` com `@Query` JPQL projetando `new ContaParticipanteResponse(u.id, u.nome, u.email)`, `where u.perfil = PARTICIPANTE and u.ativo = true and not exists (select 1 from Candidato c where c.usuario = u) order by u.nome`.
- BE `repository/CandidatoRepository`: `vincularConta(candidatoId, usuarioId)` com `@Modifying(clearAutomatically = true)` e a cláusula `usuario is null`; `existsByUsuarioId(UUID)`. `ParticipacaoRepository.buscarComCandidato` já existe e serve ao `GET .../conta` (usar `join fetch p.candidato c left join fetch c.usuario`).
- BE `service/VinculoContaService.java` (`@Service`, `@Transactional`):
  1. `vincular(participacaoId, usuarioId)` na ordem do RQ-03: participação (404) → usuário (404) → `participacaoAtiva` (409 `PARTICIPANTE_RETIRADO`) → perfil `PARTICIPANTE` (409 `CONTA_NAO_E_PARTICIPANTE`) → `ativo` (409 `CONTA_INATIVA`) → candidato sem conta (409 `PARTICIPACAO_JA_TEM_CONTA`) → conta livre (409 `CONTA_JA_VINCULADA`) → gravar pela decisão 2.
  2. `consultar(participacaoId)`: 404 se a participação não existe **ou** não tem conta (`RECURSO_NAO_ENCONTRADO`).
  3. `contasDisponiveis()`.
  - Lançar `ConflitoRegraException(codigo, mensagem)` (409) e `RecursoNaoEncontradoException` (404), com mensagens em português claras; não criar exceção nova.

### Passo 4 · Controller (20 min)

- BE `controller/ContaParticipanteController.java`, `@Tag("Conta do participante")`, sem `@RequestMapping` de classe (as rotas têm bases diferentes):
  - `PUT /api/participacoes/{participacaoId}/conta` · `@PreAuthorize(Permissoes.ESCRITA_RECRUTAMENTO)` · `@Valid @RequestBody VincularContaRequest` · 200.
  - `GET /api/participacoes/{participacaoId}/conta` · `@PreAuthorize(Permissoes.LEITURA)`.
  - `GET /api/contas-participante/disponiveis` · `@PreAuthorize(Permissoes.ESCRITA_RECRUTAMENTO)`.
- `@Operation` descrevendo os códigos de erro, no estilo de `HistoricoParticipanteController`. O `SecurityConfig` já exige autenticação em `/api/**`; não precisa de regra nova (sem token = 401, perfil errado = 403 pelo `@PreAuthorize`).
- Conferir `ContratoOpenApiSprint3Test` e o Swagger UI (`OpenApiDocumentationIntegrationTest`).

### Passo 5 · Testes de backend (50 min)

O guia deixa a bateria completa para a T113; aqui entram as regras e os contratos que protegem o código novo.
- `VinculoContaServiceTest` (Mockito): uma linha por violação, **na ordem do RQ-03** (por exemplo, conta inativa e de outro perfil ao mesmo tempo devolve `CONTA_NAO_E_PARTICIPANTE`); caminho feliz; `UPDATE` com 0 linhas → `PARTICIPACAO_JA_TEM_CONTA`; `DataIntegrityViolationException` → `CONTA_JA_VINCULADA`; ramo `PARTICIPANTE_RETIRADO` conforme a decisão 1.
- `ContaParticipanteEndpointTest` (MockMvc com `TokenDeTeste`, como `HistoricoParticipanteEndpointTest`): matriz de perfis das três rotas (coordenador ok; admin e participante 403; pesquisador lê e recebe 403 no `PUT` e em `disponiveis`; sem token 401), 404 de participação e de usuário, `usuarioId` ausente → 400 `VALIDACAO_CAMPOS`, **resposta sem `senha`/`hash`**, `disponiveis` só com contas ativas, de perfil participante, não vinculadas e ordenadas por nome, e some da lista depois de vinculada.
- Teste de corrida (sem `@Transactional`, limpando no `@AfterEach`, modelo `ParticipacaoConcorrenciaTest`): duas threads vinculando a **mesma conta** a dois candidatos devolvem 200 e 409 `CONTA_JA_VINCULADA`, nunca 500.
- Depois de verde, conferir a cobertura de `service` (gate de 80% do CI).

### Passo 6 · Corrigir `MeEndpointTest` (P10) (15 min)

- **Antes de corrigir**, rodar a suíte e registrar a falha de `listaSoOsEstudosDaConta` em `docs/testes/registro-de-defeitos.md` (regra do projeto; nunca excluir o teste sem registro).
- Correção: um candidato por conta. Reescrever o teste com **duas contas de participante** (a lista de uma conta traz um estudo; o isolamento entre contas já é coberto por `idPorParametroEhIgnorado` e pelos testes de 404). Se o caso "ordem do mais recente para o mais antigo" precisar de dois estudos, mover essa verificação para um teste de `MeusEstudosService` com participações montadas sem o `UNIQUE` (repositório mockado).
- Procurar outros `INSERT INTO candidatos ... usuario_id` com a mesma conta repetida: `grep -rn "usuario_id" backend/.../src/test`.

### Passo 7 · Dados de demonstração (25 min)

- BE `db/demo/R__demo_24_conta_participante.sql` (alterar, não criar): manter o bloco do `demo.participante@` → P5 e acrescentar o de `participante@clinicalstudy.test` → P1, com `usuario_id IS NULL`, `EXISTS` e `NOT EXISTS` da própria conta (mesmo molde do bloco existente; sintaxe que roda no H2 e no PostgreSQL). Atualizar o cabeçalho do script com o que mudou e a nota sobre o P6 (decisão 3). A numeração `d6300000-...0000000024NN` do guia só é necessária se o script inserir linhas novas; aqui só faz `UPDATE`.
- `DemoContaParticipanteTest`: acrescentar P1 (montar o candidato na transação, como o P5) e o caso "não vincula uma conta que já tem candidato" para a nova conta.
- `DemoDataIntegrationTest`: afirmar os dois vínculos, que a carga dupla não os altera nem duplica e que `COUNT(usuario_id)` bate com `COUNT(DISTINCT usuario_id)` (um para um). Subir o perfil `demo` em banco limpo e conferir que a carga não viola o `UNIQUE` (P8).

### Passo 8 · Web: API, tipos e mensagens (20 min)

- Rodar `npm run api:tipos` em `web/ClinicalStudyPB`.
- WEB `api/contaParticipante.ts`: `contaParticipanteApi` com `buscar(participacaoId)` (404 → `null`), `vincular(participacaoId, usuarioId)` e `listarDisponiveis()`; reexportar em `api/index.ts` e `api/tipos.ts`.
- WEB `acompanhamento/mensagensAcompanhamento.ts`: bloco `MENSAGENS_CONTA` com os cinco códigos (`CONTA_JA_VINCULADA`, `CONTA_INATIVA`, `CONTA_NAO_E_PARTICIPANTE`, `PARTICIPACAO_JA_TEM_CONTA`, `PARTICIPANTE_RETIRADO` com texto próprio do vínculo, pois o atual fala de agendar) e `mensagemDeVinculo(codigo, mensagemDoServidor)`; código desconhecido mostra a mensagem do servidor. Teste em `mensagensAcompanhamento.test.ts`.

### Passo 9 · Web: diálogo, área de ações e ficha (60 min)

- WEB `acompanhamento/DialogoVincularConta.tsx`, no molde de `DialogoDesfecho` (`useId`, foco na primeira opção, `aoFechar`, `aoVincular`). Estados: carregando as contas; **lista vazia** ("Não há contas de participante disponíveis. Peça ao administrador para cadastrar uma."), com o botão de confirmar desabilitado; erro ao carregar com "Tentar de novo"; lista de seleção (rádios com nome e e-mail); enviando (botão "Vinculando…" desabilitado, sem envio duplo); erro de regra no alerta do diálogo (via `mensagemDeVinculo`), mantendo o diálogo aberto para tentar outra conta. Tecla Esc e fundo fecham, como no diálogo do desfecho.
- WEB `AreaAcoesParticipante.tsx`: nova entrada `vincular-conta` em `acoesDoPerfil` (só `COORDENADOR`, só com a conta carregada **e** `conta === null`), abrindo o diálogo; ao concluir chama `aoAlterar()` e mostra a confirmação ("Conta de Fulano vinculada com sucesso."). Atualizar o comentário do contrato do componente (T111 deixa de ser "a fazer").
- WEB `FichaParticipantePage.tsx`: buscar a conta, mostrar "Conta vinculada: nome (e-mail)" **ou** o texto "Nenhuma conta vinculada ao participante." (para os dois perfis) e repassar `conta` à área de ações. Falha ao carregar a conta não derruba a ficha: mostra aviso discreto e esconde o botão.
- CSS em `acompanhamento.css` reaproveitando as classes existentes (`secao`, `alerta`, `acoes-participante`).

### Passo 10 · Testes web (40 min)

- `DialogoVincularConta.test.tsx`: carregando; lista com nome e e-mail; vazia; erro e nova tentativa; confirmar sem escolher; sucesso chama `vincular` com o `usuarioId` escolhido e `aoVincular`; cada código de erro mostra a mensagem certa e **não fecha**; código desconhecido mostra a do servidor; sem envio duplo; Esc fecha.
- `AreaAcoesParticipante.test.tsx` (ampliar): botão só para coordenador; some com conta vinculada; some enquanto a conta não foi carregada; pesquisador não vê botão.
- `FichaParticipantePage.test.tsx` (ampliar): mostra "Conta vinculada: …" para coordenador **e** pesquisador; 404 da conta mostra "Nenhuma conta vinculada" (sem erro de tela); após vincular, recarrega e esconde o botão.
- `contaParticipante` (api): 404 vira `null`; outros erros propagam.
- Usar `renderizarAba` e `erroHttp` de `src/test/ambiente.tsx` e `vi.mock` das APIs, como nas telas vizinhas.

### Passo 11 · Conferência automática (15 min)

- BE: `./mvnw verify` (inclui cobertura). WEB: `npm test`, `npm run lint`, `npm run build`.
- Se um teste existente falhar, **registrar em `registro-de-defeitos.md` antes de corrigir** (já previsto no Passo 6 para o `MeEndpointTest`).
- Conferir que o E2E `acesso-por-perfil` da web não foi afetado (a ficha continua sem botões novos para o pesquisador).

### Passo 12 · Documentação e comunicação (30 min)

- `docs/testes/testes-t111-vinculo-da-conta-do-participante.md` no formato dos `testes-t109-*` e `testes-t110-*` (escopo, como executar, resultados, cobertura, matriz RQ × teste, lacunas, falhas).
- `docs/design/sprint3/README.md`: marcar a T111; **nota de ressalva** da retirada (decisão 1) e do P6 (decisão 3).
- `T109-pendencias-para-proximas-tasks.md`: fechar P8 e P10; acrescentar, para a **T101/T104**, "trocar `participacaoAtiva` e o `TODO(T104)` da ficha"; para a **T105**, "mover o vínculo da conta demo do P5 para o P6"; para a **T110**, a mudança do roteiro 6 e da P11.
- Registrar no README e na T94 a decisão 4 (um candidato por conta) e a ausência de desvincular (RQ-05).
- RQ-T111-14: avisar a **I5** (PBI-25) no PR de que `participante@` → P1 (visitas realizada e agendada) e `demo.participante@` → P5 estão disponíveis no perfil `demo`. Ação humana.
- Gravar os documentos com as ferramentas de escrita de arquivos, sem heredoc nem crases no shell.

## 4. Mapa requisito × entrega

| RQ | Entregue em | Verificação |
|---|---|---|
| 01 | Passo 1 | `FlywayMigrationIntegrationTest`; PostgreSQL |
| 02 | Passo 2 | `ContratoOpenApiSprint3Test` (17 operações; 409 declarado) |
| 03 | Passo 3 | `VinculoContaServiceTest` (ordem das 6 verificações); decisão 1 para o ramo "retirado" |
| 04 | Passo 3 | `UPDATE` condicional; teste de corrida (409, nunca 500) |
| 05 | Passos 3 e 4 | Não existe rota nem método de desvincular |
| 06 | Passos 3 e 4 | `ContaParticipanteEndpointTest` (404 sem conta; sem `senha` no JSON) |
| 07 | Passos 3 e 4 | Endpoint: filtro, ordem e some após vincular |
| 08 | Passo 4 | Matriz de perfis (403 e 401) |
| 09 | Passo 9 | `DialogoVincularConta.test`, `AreaAcoesParticipante.test` |
| 10 | Passo 9 | `FichaParticipantePage.test` (os dois perfis veem a conta) |
| 11 | Passo 8 | `mensagensAcompanhamento.test` |
| 12 | Passo 10 | `npm test` |
| 13 | Passo 7 | `DemoContaParticipanteTest`; desvio do P6 na decisão 3 |
| 14 | Passos 7 e 12 | `DemoDataIntegrationTest`; aviso à I5 |

## 5. Riscos e cuidados

- **Erro 500 na corrida.** Sem o `UPDATE` condicional e o `try/catch` dentro do serviço (decisão 2), a violação do `UNIQUE` aparece no `commit` e vira 500.
- **Migration fora de ordem** com a T101 (decisão 9): combinar a ordem de integração antes do PR.
- **Vazamento de dados.** Nenhum DTO traz `senha`, perfil ou `ativo`; testar o JSON. O coordenador só vê contas disponíveis, nunca lista geral (`/api/usuarios` é do administrador).
- **`@Modifying` e cache do JPA.** Usar `clearAutomatically` ou reler a participação depois do `UPDATE` ao montar a resposta; senão o `ContaVinculadaResponse` sai com a conta antiga.
- **Pesquisador sem área de ações.** Colocar a conta só dentro de `AreaAcoesParticipante` a esconderia dele (RQ-10); por isso a decisão 6.
- **Demonstração.** `participante@` deixa de ser a conta vazia no perfil `demo` (decisão 3); atualizar a T110 para não deixar o roteiro falhando.
- **Escopo:** sem criar conta, sem desvincular, sem trocar a conta de um participante, sem busca na lista (a lista é pequena), sem alteração no app mobile.

## 6. Critérios de aceite (conferência final)

- [ ] O coordenador vincula uma conta de participante a um participante ativo pela ficha e vê "Conta vinculada: …"; o pesquisador vê a conta em leitura.
- [ ] Cada regra violada falha com o código certo (409), a ordem das verificações é a do guia e o um para um é garantido no banco, inclusive na corrida (nunca 500).
- [ ] `GET .../conta` devolve 404 sem conta e nunca expõe senha; `disponiveis` só lista contas ativas, de participante e livres.
- [ ] O perfil `demo` sobe em banco limpo e traz `participante@` → P1 e `demo.participante@` → P5, idempotente (carga dupla).
- [ ] `MeEndpointTest` corrigido **depois** de registrada a falha em `registro-de-defeitos.md`.
- [ ] `./mvnw verify`, `npm test`, `npm run lint` e `npm run build` passam; cobertura do backend ≥ 80%.
- [ ] Swagger e `openapi-sprint3.yaml` com 17 operações; README, pendências e documento de testes atualizados; I5 avisada.

## 7. Estimativa

| Bloco | Tempo |
|---|---|
| Passos 0 a 2 (base, migration, contrato) | 0h50 |
| Passos 3 a 4 (serviço e controller) | 1h20 |
| Passos 5 e 6 (testes de backend e correção do P10) | 1h05 |
| Passo 7 (demonstração) | 0h25 |
| Passos 8 a 10 (web e testes) | 2h00 |
| Passos 11 e 12 (conferência e documentação) | 0h45 |
| **Total** | **~6h25** |

A estimativa do planejamento é 3h. Os 3h25 a mais vêm da corrida tratada no banco, da correção do `MeEndpointTest`, dos testes de componente das três telas e da documentação. Se o prazo (sáb 21/11) apertar, cortar nesta ordem: o teste de corrida (manter a captura do `DataIntegrityViolationException` no teste unitário), os testes extras da ficha, e deixar a bateria completa para a T113 como o guia permite. Nunca cortar a ordem das verificações, o `UNIQUE` nem o registro da falha do P10. O prazo de 21/11 já passou: o novo prazo deve ser combinado com o grupo antes de iniciar.

## 8. Inventário de arquivos (rastreabilidade do que será tocado)

Caminhos relativos a `backend/ClinicalStudyPB/src/` (**BE**) e `web/ClinicalStudyPB/src/` (**WEB**). "Passo" remete à seção 3; "RQ" aos requisitos do guia.

| Ação | Arquivo | Passo | RQ |
|---|---|---|---|
| Criar | BE `main/resources/db/migration/V6_24_1__unique_candidate_account.sql` | 1 | 01 |
| Alterar | BE `test/.../FlywayMigrationIntegrationTest.java` | 1 | 01 |
| Alterar | `docs/api/openapi-sprint3.yaml` | 2 | 02 |
| Alterar | BE `test/.../ContratoOpenApiSprint3Test.java` | 2 | 02 |
| Criar | BE `controller/dto/VincularContaRequest.java`, `ContaVinculadaResponse.java`, `ContaParticipanteResponse.java` | 3 | 02, 06, 07 |
| Alterar | BE `repository/UsuarioRepository.java`, `CandidatoRepository.java` | 3 | 03, 04, 07 |
| Criar | BE `service/VinculoContaService.java` | 3 | 03, 04, 05, 06 |
| Criar | BE `controller/ContaParticipanteController.java` | 4 | 02, 06, 07, 08 |
| Criar | BE `test/.../VinculoContaServiceTest.java`, `ContaParticipanteEndpointTest.java`, teste de corrida | 5 | 03, 04, 06, 07, 08 |
| Alterar | BE `test/.../MeEndpointTest.java` (P10) e `docs/testes/registro-de-defeitos.md` | 6 | 01 (efeito) |
| Alterar | BE `main/resources/db/demo/R__demo_24_conta_participante.sql`, `DemoContaParticipanteTest`, `DemoDataIntegrationTest` | 7 | 13, 14 |
| Criar/Alterar | WEB `api/contaParticipante.ts`, `api/index.ts`, `api/tipos.ts`, `api/schema-sprint3.d.ts` (gerado) | 8 | 11 |
| Alterar | WEB `pages/estudos/detalhe/acompanhamento/mensagensAcompanhamento.ts` e teste | 8 | 11 |
| Criar | WEB `.../acompanhamento/DialogoVincularConta.tsx` e teste | 9, 10 | 09, 12 |
| Alterar | WEB `AreaAcoesParticipante.tsx`, `FichaParticipantePage.tsx`, `acompanhamento.css` e testes | 9, 10 | 09, 10, 12 |
| Criar | `docs/testes/testes-t111-vinculo-da-conta-do-participante.md` | 12 | todos |
| Alterar | `docs/design/sprint3/README.md`, `T109-pendencias-para-proximas-tasks.md` | 12 | 14 |

## 9. Pendências herdadas que esta task fecha (e as que ela cria)

| Origem | Item | Situação ao fim da T111 | Onde fica registrado |
|---|---|---|---|
| T109 P8 | Perfil `demo` sobe sem violar o `UNIQUE` | Fechada (Passo 7) | `DemoDataIntegrationTest` |
| T109 P9 | `participacoesDe` continua devolvendo lista | Mantida (assinatura intacta) | `AcessoParticipanteTest` |
| T109 P10 | `MeEndpointTest.listaSoOsEstudosDaConta` falha com o `UNIQUE` | Fechada (Passo 6), com registro **D-T111-01** antes da correção | `registro-de-defeitos.md` |
| T109 P11 | Roteiro 6 da T110 usava `participante@` como conta sem vínculo | **Criada**: atualizar roteiro e `testes-t110` (decisão 3) | `testes-t110-...md` |
| T101/T104 | Trocar `participacaoAtiva` e o `TODO(T104)` da ficha | **Criada** (só no caminho B) | `T109-pendencias...md` |
| T105 | Mover o vínculo da conta demo do P5 para o P6 | **Criada** | `T109-pendencias...md` |
| PO (T94) | "Uma conta por candidato" | **Criada**: aguardando confirmação | README da Sprint 3 |
