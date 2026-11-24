# T113 · Plano de implementação: testes de backend e de componente do app

> **Sprint 3** · PBI-24 · RF17 (RF02 e RF03 no mobile) · **Estimativa do planejamento:** 3h · **Depende de:** T109, T110, T111, T112
> Guia da task: [T113-testes-de-backend-e-de-componente-do-app.md](T113-testes-de-backend-e-de-componente-do-app.md) (16 requisitos `RQ-T113-NN`). Contrato: [00-convencoes-e-contrato-compartilhado.md](00-convencoes-e-contrato-compartilhado.md). Pendências herdadas: [T109-pendencias-para-proximas-tasks.md](T109-pendencias-para-proximas-tasks.md) (P5, P6, P7, P28).
> Caminhos relativos a `backend/ClinicalStudyPB/src/test/java/UEPB/ESWII/ClinicalStudyPB/` (**BE**) ou `mobile/ClinicalStudyPB/src/` (**MOB**), salvo indicação.
> Plano escrito em 22/11/2026. **Nada foi implementado.** Este plano e o da [T114](T114-plano-de-implementacao.md) são independentes: a T114 só precisa da T110 e da T112, não desta task.

## 0. Leitura rápida (o que mudou desde que o guia foi escrito)

O guia da T113 foi escrito **antes** de a T109, a T110, a T111 e a T112 existirem. Cada uma delas entregou os próprios testes. Por isso a T113 deixou de ser "escrever a bateria" e passou a ser **conferir, completar e provar**: o trabalho real é um levantamento de lacunas (seção 2), alguns testes novos pequenos e a medição de cobertura.

| Fato verificado em 22/11 | Efeito no plano |
|---|---|
| T110 e T112 estão na branch `feat/task112-meus-estudos-detalhe` (PR #43, aberto): **129 testes do app** em 10 suítes | A maior parte dos RQ-12 a RQ-15 **já tem teste**. A T113 confere a matriz e preenche só o que falta |
| T111 está na branch atual (PR #42, aberto): backend com **757 testes**, web com 315 | RQ-06 a RQ-10 (vínculo) já têm testes de serviço, endpoint e corrida. Os **nomes das classes diferem do guia** (decisão 2) |
| **`UNIQUE (usuario_id)` em `candidatos`** (T111, decisão 4): uma conta tem um único candidato, logo uma única participação | **RQ-T113-02 ("participante com dois estudos") não é montável pelo banco.** Ver decisão 1 |
| T101 (#39) e T102 (#40) continuam abertos: `situacao`/`RETIRADO` ainda não existem na `develop` | O "retirado" real (RQ-T113-02 e 05, P5 da T109) depende do caminho A ou B da decisão 3 |
| `MeEndpointTest.listaSoOsEstudosDaConta` foi reescrito na T111 (D-T111-02) com um estudo por conta | O teste "ordem da data de inclusão decrescente" saiu do endpoint; precisa de outro lugar (decisão 1) |
| Nenhuma das três branches (#38, #42, #43) está na `develop` | Ver pré-condições E1 a E3 |

### Pré-condições de entrada (conferir antes do Passo 0)

| # | Pré-condição | Como conferir | Se não estiver pronta |
|---|---|---|---|
| E1 | #38 (T110), #42 (T111) e #43 (T112) mergeados na `develop` | `gh pr view 43 --json state` → `MERGED` (idem 38 e 42) | Criar a branch da T113 a partir de `origin/feat/task112-meus-estudos-detalhe` e **mesclar** `origin/feat/task111-vinculo-conta-participante`. Conflito esperado só em `docs/testes/registro-de-defeitos.md` e no README da Sprint 3 (resolver mantendo os dois lados) |
| E2 | #39 (T101) mergeado, ou decisão explícita pelo **caminho B** (decisão 3) | `gh pr view 39 --json state` | Caminho B: o "retirado" fica coberto só no app (mocks) e por teste unitário no backend, com a pendência registrada |
| E3 | Docker disponível para o PostgreSQL local (RQ-T113-11) | `docker ps` | Registrar como lacuna e deixar o job `Backend (PostgreSQL)` do CI como evidência |

## 1. Decisões de projeto

1. **"Dois estudos" não cabe no banco (RQ-T113-02).** Com o `UNIQUE` da T111, uma conta de participante tem **um** candidato e portanto **uma** participação; "dois estudos para a mesma conta" só existe em teste unitário de `MeusEstudosService` com `ParticipacaoRepository` simulado. Plano: (a) ordenação decrescente por data de inclusão e lista com duas participações provadas no teste de serviço (Mockito), (b) o endpoint prova o que o banco permite (uma participação). **Registrar o desvio** no documento de testes e perguntar ao PO (T94, P27) se "uma conta por candidato" vale mesmo; se mudar, este requisito volta a ser testável pelo endpoint.
2. **Reaproveitar as classes que já existem, não duplicar.** O guia pede arquivos novos; o código tem equivalentes (tabela abaixo). Criar classes paralelas só para casar com o nome do guia geraria teste duplicado. A T113 **amplia** as classes existentes e registra a equivalência na matriz (seção 4).

   | Nome no guia | Classe real | Origem |
   |---|---|---|
   | `AcessoParticipanteTest` | `AcessoParticipanteTest` (9 testes) | T109 |
   | `MeEstudosEndpointTest` | `MeEndpointTest` (10 métodos, incluindo 403 parametrizado) | T109/T111 |
   | `VinculoContaServiceTest` | `service/VinculoContaServiceTest` (19 testes, ordem das 6 verificações) | T111 |
   | `VinculoContaEndpointTest` | `ContaParticipanteEndpointTest` (16 testes) | T111 |
   | `VinculoContaConcorrenciaTest` | `ContaParticipanteConcorrenciaTest` (2 cenários) | T111 |
   | `src/app/__tests__/*.test.tsx` | `src/telas/__tests__/*.test.tsx` | T110 (o Expo Router trata tudo em `src/app/` como rota) |

3. **Retirada (caminhos A e B).** Caminho A (T101 na `develop`): criar a participação retirada com `retirar()` e provar lista e detalhe no `/me` (fecha P5 da T109), mais o 409 `PARTICIPANTE_RETIRADO` do vínculo de ponta a ponta (P24 da T111). Caminho B (T101 aberta): manter o ramo coberto só nos testes unitários e nos testes de componente do app (mocks), e anotar a lacuna. O método de preparação do cenário ficará num **único ponto** (`CenarioParticipante`, decisão 4) para trocar só o corpo depois.
4. **`support/CenarioParticipante` (P6 da T109).** Extrair de `MeEndpointTest` a montagem por JDBC de estudo, candidato vinculado e outra conta, para reuso pela T113, T106 e T120. É refatoração de **teste**: só entra se os testes existentes continuarem passando sem alteração de asserção. Se complicar, cortar (ordem de corte, seção 7).
5. **O mais importante da task** (guia, seção 8): "o participante A nunca vê o estudo do B". Além do teste, fazer uma **verificação de sensibilidade**: remover temporariamente o filtro por conta em `AcessoParticipante`, confirmar que o teste **falha**, e desfazer. Registrar o resultado no documento de testes. Sem isso, o teste pode passar por engano.
6. **Falha de teste: registrar antes de corrigir.** Qualquer falha nova vira `D-T113-NN` em `docs/testes/registro-de-defeitos.md`, **antes** da correção, e nenhum teste é excluído (ver memória do projeto). Os testes de tela seguem a lição da T110: `userEvent` quando o manipulador devolve promessa controlada pelo teste; nunca deixar um `act` aberto.
7. **Cobertura.** O gate do CI é 80% (`jacoco.cobertura.minima`). O alvo de 95% do guia (RQ-T113-11) vale para três classes: `AcessoParticipante`, `MeusEstudosService`, `VinculoContaService`. Medir com `./mvnw -B verify` e ler `target/site/jacoco/jacoco.csv`.

## 2. Diagnóstico: o que já está provado e o que falta

Levantamento por **nome de teste** (22/11). A coluna "Situação" diz o que a T113 deve **confirmar lendo o corpo do teste** no Passo 1; "Lacuna" é trabalho novo.

### Backend

| RQ | Já existe (classe · teste) | Situação | Lacuna a fechar |
|---|---|---|---|
| 01 | `MeEndpointTest` · `listaSoOsEstudosDaConta`, `idPorParametroEhIgnorado`, `estudoDeOutroParticipanteRecebe404`; `AcessoParticipanteTest` · `participacaoDeOutraContaLanca404` | Provável cobertura parcial | Teste simétrico A × B no endpoint: duas contas vinculadas a candidatos de estudos **diferentes**; a lista de A não contém o estudo de B **e vice-versa**; o detalhe cruzado dá 404 nos dois sentidos |
| 02 | `MeEndpointTest` · `contaSemVinculoRecebeListaVazia` | Parcial | Ordem decrescente e dois estudos: teste de serviço (decisão 1). Retirado: caminho A ou B (decisão 3), sem o motivo |
| 03 | `MeEndpointTest` · `detalheDoEstudoDaConta`, `estudoDeOutroParticipanteRecebe404`, `contaSemVinculoRecebe404NoDetalhe` | Cobertura provável | Estudo **inexistente** (UUID aleatório) com **a mesma resposta** de estudo de outro: comparar corpo e código dos dois 404 |
| 04 | `MeEndpointTest` · `demaisPerfisRecebem403` (parametrizado), `semTokenRecebe401` | Cobertura provável | Conferir que os três perfis (admin, pesquisador, coordenador) valem para **lista e detalhe** |
| 05 | `MeuEstudoResponseTest` (4 testes do corte em 160 com reticências) | Corte coberto no DTO | Teste no **endpoint**: JSON sem `codigo`/`nomeFicticio` do candidato (percorrer as chaves) e `resumo` com ≤160 caracteres |
| 06 | `ContaParticipanteEndpointTest` · `vinculaEConsulta` | Parcial | Depois de vincular, **a própria conta** lista o estudo em `/api/me/estudos` (fluxo completo: criar usuário, vincular, entrar como participante, listar) |
| 07 | `VinculoContaServiceTest` (um teste por violação e 4 de ordem); endpoint: `contaDeOutroPerfil`, `contaInativa`, `participacaoJaTemConta`, `contaJaVinculada`, `ordemPerfilAntesDeInativa`, `naoEncontrados` | Coberto | Só `PARTICIPANTE_RETIRADO` de ponta a ponta (caminho A) |
| 08 | `ContaParticipanteEndpointTest` · `semToken`, `soOCoordenadorEscreve`, `leituraDaConta` | Coberto | Conferir pesquisador e coordenador em `GET .../conta` e 403 de administrador e participante |
| 09 | `ContaParticipanteEndpointTest` · `disponiveis`, `semDadosDeSeguranca` | Coberto | Nenhuma |
| 10 | `ContaParticipanteConcorrenciaTest` (2 cenários, sem `@Transactional`, `@AfterEach` limpa) | Coberto | Nenhuma; confirmar que a limpeza é completa |
| 11 | Cobertura medida na T111 só como gate global | **Não medido por classe** | Medir `AcessoParticipante`, `MeusEstudosService`, `VinculoContaService`; rodar no PostgreSQL |

### App

| RQ | Já existe (arquivo · teste) | Situação | Lacuna a fechar |
|---|---|---|---|
| 12 | MOB `telas/__tests__/login.test.tsx` (15 testes), `contexts/__tests__/sessao.test.tsx` (3 perfis, 403/404, rede, aviso) | Coberto pela T110 | Conferir que **os 3 perfis** têm mensagem "exclusivo para participantes" e token removido, credencial inválida, campos obrigatórios e rede |
| 13 | MOB `telas/__tests__/estudos.test.tsx` (11 da lista) | Coberto pela T112 | Nenhuma esperada (carregando, vazia, erro, Tentar de novo, pull-to-refresh) |
| 14 | mesmo arquivo (9 do detalhe) | Coberto pela T112 | Nenhuma esperada (dados, retirado sem motivo, 404 "Estudo não encontrado.") |
| 15 | MOB `api/__tests__/cliente.test.ts` (9 testes: `ErroApi`, rede, 401 com callback, 401 do login não expira) | Coberto | Nenhuma |
| 16 | `npm test` verde (129 testes, 10 suítes) | Parcial | Rodar `npm test -- --coverage`, registrar a cobertura e confirmar `lint` e `typecheck` no CI |

**Consequência:** o app já está provado; o trabalho novo da T113 é quase todo no backend (RQ-01, 02, 03, 05, 06, 11) mais a medição (RQ-11 e 16) e o documento de rastreabilidade.

## 3. Passos de implementação

Ordem pensada para que cada passo deixe a suíte verde.

### Passo 0 · Branch e linha de base (15 min)

- Conferir E1 a E3. Branch `feat/task113-testes-backend-e-componente-app`, conforme E1.
- Backend: `./mvnw -B verify` e anotar testes e cobertura. App: `npm ci`, `npm test`, `npm run lint`, `npm run typecheck` e anotar (esperado: 129 testes). Deixar `docs/tutorial-inicializacao.md` fora do PR.
- Se a linha de base já falhar, registrar em `registro-de-defeitos.md` antes de qualquer coisa.

### Passo 1 · Conferir a matriz lendo os testes (30 min)

- Ler o **corpo** dos testes citados na seção 2 (o levantamento foi por nome) e marcar cada RQ como "coberto" ou "lacuna". Corrigir a seção 2 deste plano se algo estiver diferente.
- Anotar o resultado numa tabela do documento `testes-t113-...` (Passo 8). Não alterar teste existente aqui.

### Passo 2 · `support/CenarioParticipante` (opcional, 30 min) (RQ 01, 02, 06 e P6)

- BE `support/CenarioParticipante.java`: `contaParticipante(mvc, tokenAdmin, email)` (cria o usuário `PARTICIPANTE` pela API), `participanteComConta(...)` (estudo em recrutamento, candidato incluído, vínculo pelo coordenador via `PUT .../conta`), reaproveitando `CenarioRecrutamento`.
- Migrar `MeEndpointTest` para usar o apoio **sem mudar asserções**. Se algum teste mudar de comportamento, desfazer a migração (decisão 4).

### Passo 3 · Backend `/me/estudos`: isolamento A × B (RQ 01, 03, 04, 05) (50 min)

Em `MeEndpointTest` (ou classe nova `MeIsolamentoTest` se a existente crescer demais):
- `aNuncaVeEstudoDeB` e `bNuncaVeEstudoDeA`: duas contas, dois estudos, listas disjuntas **e** detalhe cruzado 404 (RQ-01, RQ-03).
- `estudoInexistenteEIgualAEstudoDeOutro`: o 404 de UUID desconhecido e o de estudo alheio têm o mesmo corpo (não revela existência) (RQ-03).
- `tresPerfisRecebem403EmListaEDetalhe` (parametrizado por perfil e rota) e `semTokenEm401EmAmbasAsRotas` (RQ-04).
- `respostaNaoTrazDadosDoCandidato`: percorre as chaves do JSON da lista e do detalhe e falha se existir `codigo`, `nomeFicticio`, `candidato` ou `motivo`; `resumo` ≤160 (RQ-05).
- **Verificação de sensibilidade** (decisão 5): quebrar o filtro, ver o teste cair, desfazer. Registrar.

### Passo 4 · Backend: serviço, ordem e retirada (RQ 02) (35 min)

- `service/MeusEstudosServiceTest` (nova classe se não existir; **conferir** antes): duas participações (A mais antiga, B mais recente) → lista [B, A]; lista vazia; `resumo` cortado; situação `ATIVO` fixa enquanto o `TODO(T101)` existir.
- Caminho A: `retirado` no `/me` (lista e detalhe com `retirada` e sem motivo) e `vincularContaDeRetirado` → 409 `PARTICIPANTE_RETIRADO` de ponta a ponta. Caminho B: **não** escrever; anotar a pendência P5 e P24 no documento.

### Passo 5 · Backend: vínculo → `/me` (RQ 06, 08) (25 min)

- Em `ContaParticipanteEndpointTest`: `depoisDeVincularAContaVeOEstudo`: administrador cria a conta, coordenador vincula, a conta entra (`POST /api/auth/login`), lista `/api/me/estudos` e vê o estudo; `GET .../conta` devolve a conta (RQ-06).
- Completar a matriz de perfis de `GET .../conta` (pesquisador e coordenador 200; administrador e participante 403; sem token 401) se faltar (RQ-08).

### Passo 6 · Backend: cobertura e PostgreSQL (RQ 11) (35 min)

- `./mvnw -B verify`; ler `target/site/jacoco/jacoco.csv` e registrar a linha de `AcessoParticipante`, `MeusEstudosService` e `VinculoContaService`. Se alguma ficar abaixo de 95%, escrever o teste do ramo faltante (típico: caminho de `usuarioNulo`, `DataIntegrityViolationException`, estudo inexistente).
- Rodar a suíte completa no PostgreSQL 16 (container local com as variáveis `SPRING_DATASOURCE_*` do job do CI) e anotar o resultado. Sem Docker: a evidência é o job `Backend (PostgreSQL)` do PR.
- Opcional (P7 da T109): teste de contagem de consultas do `/me/estudos` (sem N+1), no molde do histórico (T96).

### Passo 7 · App: conferência e lacunas (RQ 12 a 16) (30 min)

- Rodar `npm test -- --coverage` e registrar a cobertura de `src/app/login.tsx`, `src/app/(tabs)/estudos.tsx`, `src/app/estudos/[estudoId].tsx`, `src/contexts/sessao.tsx`, `src/api/cliente.ts` e o total (RQ-16).
- Só se o Passo 1 achar lacuna: acrescentar o teste em `src/telas/__tests__/` ou `src/api/__tests__/`. Candidatos: login recusado com **cada um** dos três perfis na tela (e não só no contexto); erro 401 do detalhe chamando o callback de sessão.
- Conferir no CI o job **Mobile (Expo)** com `lint`, `typecheck` e `npm test`.

### Passo 8 · Documentação (30 min)

- `docs/testes/testes-t113-testes-de-backend-e-de-componente-do-app.md` no formato dos `testes-t110`, `t111` e `t112`: linha de base, comandos, **matriz RQ × teste** com a equivalência de nomes (decisão 2), cobertura por classe, verificação de sensibilidade, falhas (`D-T113-NN`), lacunas e desvios (RQ-02, retirado).
- `docs/design/sprint3/README.md`: marcar a T113 e anotar as ressalvas (retirada; "dois estudos").
- `T109-pendencias-para-proximas-tasks.md`: fechar P5, P6 e P7 conforme o que entrar; manter P24 se o caminho for B.

## 4. Mapa requisito × entrega

| RQ | Entregue em | Verificação |
|---|---|---|
| 01 | Passo 3 (A × B) | `aNuncaVeEstudoDeB`/`bNuncaVeEstudoDeA` + verificação de sensibilidade |
| 02 | Passos 3 e 4 | `MeusEstudosServiceTest` (ordem, duas participações); retirado só no caminho A; desvio da decisão 1 |
| 03 | Passo 3 | 404 igual para inexistente e alheio |
| 04 | Passo 3 | 403 em três perfis × duas rotas; 401 |
| 05 | Passo 3 | JSON sem campos do candidato; `MeuEstudoResponseTest` (160) |
| 06 | Passo 5 | `depoisDeVincularAContaVeOEstudo` |
| 07 | Passo 1 (conferência) e Passo 4 (retirado, caminho A) | `VinculoContaServiceTest`, `ContaParticipanteEndpointTest` |
| 08 | Passo 5 | Matriz de perfis do `GET .../conta` e do `PUT` |
| 09 | Passo 1 | `ContaParticipanteEndpointTest.disponiveis`, `semDadosDeSeguranca` |
| 10 | Passo 1 | `ContaParticipanteConcorrenciaTest` |
| 11 | Passo 6 | `jacoco.csv` por classe; suíte no PostgreSQL |
| 12 | Passos 1 e 7 | `login.test.tsx`, `sessao.test.tsx` |
| 13, 14 | Passo 1 | `estudos.test.tsx` |
| 15 | Passo 1 | `cliente.test.ts` |
| 16 | Passo 7 | `npm run lint`, `typecheck`, `npm test -- --coverage`; CI |

## 5. Inventário de arquivos (rastreabilidade do que será tocado)

| Ação | Arquivo | Passo | RQ |
|---|---|---|---|
| Criar (opcional) | BE `support/CenarioParticipante.java` | 2 | 01, 02, 06 |
| Alterar | BE `MeEndpointTest.java` (ou criar `MeIsolamentoTest.java`) | 3 | 01, 03, 04, 05 |
| Criar ou alterar | BE `service/MeusEstudosServiceTest.java` | 4 | 02 |
| Alterar | BE `ContaParticipanteEndpointTest.java` | 5 | 06, 08 |
| Alterar (se houver lacuna) | BE `AcessoParticipanteTest.java`, `service/VinculoContaServiceTest.java` | 6 | 11 |
| Alterar (se houver lacuna) | MOB `telas/__tests__/login.test.tsx`, `estudos.test.tsx`, `api/__tests__/cliente.test.ts` | 7 | 12, 13, 14, 15 |
| Criar | `docs/testes/testes-t113-testes-de-backend-e-de-componente-do-app.md` | 8 | todos |
| Alterar | `docs/testes/registro-de-defeitos.md` (só se houver falha) | qualquer | — |
| Alterar | `docs/design/sprint3/README.md`, `T109-pendencias-para-proximas-tasks.md` | 8 | 02, 07 |

Nenhum código de produção é alterado nesta task. Se um teste novo expuser um defeito de produção, o defeito é registrado (`D-T113-NN`) e a correção vira um commit separado, com a descrição do bug.

## 6. Riscos e cuidados

- **Teste que passa por engano** no isolamento A × B: por isso a verificação de sensibilidade (decisão 5).
- **Teste de concorrência sem limpeza** contamina os outros: manter o `@AfterEach` e conferir que o banco volta ao estado inicial.
- **Duplicar teste** só para casar nome do guia: não fazer (decisão 2).
- **Premissa "uma conta por candidato" ainda sem confirmação do PO** (P27): a decisão 1 muda se ela mudar.
- **Tempo-limite em suítes pesadas do app** (F-T110-03): usar `jest.setTimeout` só no arquivo afetado.
- **Escopo:** sem E2E (T114), sem alteração de código de produção, sem tela nova.

## 7. Estimativa e ordem de corte

| Bloco | Tempo |
|---|---|
| Passos 0 e 1 (base e conferência) | 0h45 |
| Passo 2 (apoio, opcional) | 0h30 |
| Passos 3 a 5 (testes novos do backend) | 1h50 |
| Passo 6 (cobertura e PostgreSQL) | 0h35 |
| Passos 7 e 8 (app e documentação) | 1h00 |
| **Total** | **~5h** |

A estimativa do planejamento é 3h; os testes do app já existem, mas a conferência, a medição por classe e o documento de rastreabilidade pesam. Se apertar, cortar nesta ordem: `CenarioParticipante` (Passo 2), teste N+1 (P7), PostgreSQL local (usar o job do CI). **Nunca cortar:** o teste A × B com a verificação de sensibilidade, o 404 igual para inexistente e alheio, o registro de falhas e o documento de testes. O prazo de 21/11 já passou: combinar o novo prazo com o grupo.

## 8. Critérios de aceite (conferência final)

- [ ] `/api/me/**` provado: só a conta logada, 403 para os três outros perfis, 404 igual para inexistente e alheio, sem dados do candidato.
- [ ] Regras do vínculo e concorrência confirmadas; vínculo → `/me` provado de ponta a ponta.
- [ ] Cobertura por classe medida e registrada (`AcessoParticipante`, `MeusEstudosService`, `VinculoContaService`) e gate de 80% do CI verde; suíte rodada no PostgreSQL.
- [ ] App: `lint`, `typecheck` e `npm test` verdes, cobertura registrada.
- [ ] `testes-t113-...md` com matriz RQ × teste, desvios (dois estudos, retirado) e falhas; README e pendências atualizados.

## 9. Índice de rastreabilidade deste plano

| Pergunta | Onde achar |
|---|---|
| De onde vem cada requisito? | Guia [T113](T113-testes-de-backend-e-de-componente-do-app.md), seção 4 (`RQ-T113-01` a `16`) e seção 9 (especificação: RF17, RF02, RF03 e estratégia de testes) |
| Qual teste já prova cada requisito? | Seção 2 (diagnóstico) deste plano |
| Qual passo entrega o que falta? | Seção 4 (mapa RQ × entrega) |
| Quais arquivos serão tocados? | Seção 5 |
| Onde registrar falhas? | `docs/testes/registro-de-defeitos.md`, linhas `D-T113-NN`, **antes** de corrigir |
| Quais pendências esta task fecha ou herda? | Passo 8: P5, P6, P7 (T109); P24 e P27 (T111) |
