# T112 · Plano de implementação: telas Meus estudos e detalhe do estudo (app mobile)

> **Sprint 3** · PBI-24 · RF17 (e RF02/RF03 no mobile) · **Estimativa do planejamento:** 3h · **Depende de:** T108 e T109 (guia) e, na prática, da **T110** (`estudosApi.listar`, sessão e mocks já com o formato real)
> Guia da task: [T112-telas-meus-estudos-e-detalhe-do-estudo.md](T112-telas-meus-estudos-e-detalhe-do-estudo.md) (11 requisitos `RQ-T112-NN`). Contrato: [00-convencoes-e-contrato-compartilhado.md](00-convencoes-e-contrato-compartilhado.md). Pendências herdadas: [T109-pendencias-para-proximas-tasks.md](T109-pendencias-para-proximas-tasks.md) (P13 a P16). Plano irmão: [T111-plano-de-implementacao.md](T111-plano-de-implementacao.md).
> Caminhos relativos a `mobile/ClinicalStudyPB/src/` (**APP**), salvo indicação. **Este plano é independente do da T111**: nenhuma das duas tasks altera arquivos da outra, e a ordem de execução entre elas não importa (ver seção 1).

## 0. Índice de rastreabilidade deste plano

| Pergunta | Onde achar |
|---|---|
| De onde vem cada requisito? | Guia [T112](T112-telas-meus-estudos-e-detalhe-do-estudo.md), seção 4 (`RQ-T112-01` a `11`) e seção 9 (RF17, RF02, RF03, critério de aceite do PBI-24) |
| Qual passo entrega cada requisito? | Seção 5 (mapa RQ × entrega × teste) |
| Quais arquivos serão criados ou alterados? | Seção 6 (inventário) |
| Quais pendências herdadas esta task fecha? | Seção 7 (P13 a P16 da T109) |
| Onde registrar falhas de teste? | `docs/testes/registro-de-defeitos.md`, linhas `D-T112-NN`, **antes** de corrigir |
| Onde documentar o resultado dos testes? | `docs/testes/testes-t112-telas-meus-estudos-e-detalhe.md` (Passo 8) |

## 1. Diagnóstico: o que já existe e o que falta

Estado verificado em 22/11/2026 na branch `feat/task110-login-logout-app` (PR #38, aprovado e sem conflito, ainda não mergeado):

| Item | Situação hoje | Consequência para a T112 |
|---|---|---|
| `app/(tabs)/estudos.tsx` | Mostra `EmBreve` ("M03 Meus estudos") | Substituir pela lista (RQ-01 a 04) |
| `app/estudos/[estudoId].tsx` | Rota declarada pela T108, só `EmBreve` ("chega na T112") | Implementar o detalhe (RQ-05 a 07); o `_layout.tsx` **já** declara a tela com cabeçalho "Estudo" e botão "Voltar" (RQ-08 pronto) |
| `api/estudos.ts` | Só `estudosApi.listar` e o tipo `MeuEstudo` (T110) | Acrescentar `buscar(estudoId)` e o tipo `MeuEstudoDetalhe` |
| Tipos `MeuEstudoResponse` e `MeuEstudoDetalheResponse` | Já em `api/schema-sprint3.d.ts` (T109/T115). `situacaoParticipacao` e `retirada` existem no tipo | Nada a gerar. O design cita `schema.d.ts`; o arquivo certo é `schema-sprint3.d.ts` (P13) |
| Mocks (`api/mocks/dados.ts` e `index.ts`) | **Já no formato real** (T115): 2 estudos, o segundo `RETIRADO` com `retirada.dataRetirada`; o detalhe devolve 404 para id desconhecido | P16 já está atendida; só conferir. Dá para testar lista, detalhe e retirado sem backend |
| Componentes (`Tela`, `Cartao`, `Botao`, `Selo`, `Carregando`, `EstadoErro`, `EstadoVazio`) | Existem (T108) | Reutilizar; **não criar** componente novo de estado. `Selo` tem tons `neutro/info/sucesso/aviso/perigo` |
| `ErroApi` | Tem `status`, `codigo`, `mensagem` | O 404 do detalhe se detecta por `erro.status === 404` |
| `mensagemDoErro` (`api/erro.ts`) | Existe (T110) | Usar para a mensagem do erro de carga |
| Aba `visitas.tsx` | `EmBreve`; **não lê parâmetros** | O atalho (RQ-06) navega com `estudoId` como parâmetro; quem o consome é a T117. A T112 só precisa que a navegação **não quebre** |
| `src/utils/` | **Não existe** | Criar `utils/estudos.ts` (rótulos e datas) |
| Backend: situação da participação | Hoje devolve **sempre `ATIVO`** (`TODO(T101)`); a T101 (#39) está aberta e traz a coluna `situacao` | Em app real, o selo "Retirado" **só aparece depois** da T101/T103. Em mock aparece já. Documentar na seção 4 (decisão 5) |
| Dados de demonstração | `participante@` hoje **sem vínculo**; após a T111 passa a ver o P1; `demo.participante@` no P5 (retirado só depois da T105) | RQ-11 (conferência manual) depende da T111; ver decisão 6 |

**Independência da T111:** a T112 consome apenas `GET /api/me/estudos` e `/api/me/estudos/{id}`, prontos na T109. A T111 muda quem **tem** vínculo, não o contrato. Por isso as duas podem ser feitas em qualquer ordem; só o RQ-11 (conferência com a conta real) exige a T111 pronta.

## 2. Decisões de projeto

1. **Formatação num único lugar.** `utils/estudos.ts` exporta `rotuloStatusEstudo`, `tomStatusEstudo`, `rotuloSituacaoParticipacao`, `formatarData` (`'2026-10-30'` → `'30/10/2026'`) e `formatarPeriodo`. A conversão de data **não passa por `new Date(...)`**: `'2026-10-30'` interpretado como UTC mostraria 29/10 em fuso negativo (mesma classe de erro do #41). Dividir a string por `-` e remontar.
2. **Rótulos fechados e com fallback.** Mapas `Record` para os quatro status (`PLANEJAMENTO` → "Planejamento", `RECRUTAMENTO` → "Recrutamento", `EXECUCAO` → "Em execução", `ENCERRADO` → "Encerrado") e dois para a situação. Um valor desconhecido (backend novo) mostra o texto cru em vez de quebrar, e o teste cobre esse caso.
3. **Lista com `FlatList`** (não `ScrollView` + `map`): rolagem de verdade, `RefreshControl` (RQ-04) e `keyExtractor` por `participacaoId`. O título "Meus estudos" vem do cabeçalho da aba; **não** usar `Tela` se ela já rolar (`Tela` é um `ScrollView`; aninhar listas virtualizadas dentro dele gera aviso do React Native). Confirmar lendo `ui.tsx` no Passo 2; se `Tela` rolar, a lista usa o mesmo fundo e margens sem ela.
4. **Estados da lista sem sobreposição:** carregando (primeira carga) → `Carregando`; erro (sem dados) → `EstadoErro` com "Tentar de novo"; vazio → `EstadoVazio` "Você ainda não está incluído em nenhum estudo."; dados → lista. Se o **refresh** falhar com dados já na tela, **manter os dados** e mostrar o aviso de erro acima da lista (não apagar o que o participante já viu). O indicador de refresh sempre termina (`finally`), inclusive no erro (RQ-04).
5. **"Retirado" depende do backend real.** O app já trata `RETIRADO` (selo "Retirado" no tom `aviso`, e a data da retirada no detalhe, **nunca o motivo**). Com o backend atual (sempre `ATIVO`) esse ramo só é visível com mocks (`EXPO_PUBLIC_USE_MOCKS=true`) e nos testes de componente. Registrar isso no documento de testes como lacuna conhecida até a T101/T103, e **não** mexer no backend nesta task.
6. **Conferência manual (RQ-11) em duas etapas.** (a) Com mocks, já: os dois estudos, o selo, o 404. (b) Com a API real: `participante@clinicalstudy.test` (vinculado ao P1 **depois da T111**) e `demo.participante@` (P5). O estado "retirado" real só depois da T105. Se a T111 não estiver pronta, executar só a etapa (a) e deixar o RQ-11 marcado como parcial, como foi feito com o RQ-T110-10/11.
7. **Atalho "Ver visitas deste estudo".** `router.push({ pathname: '/(tabs)/visitas', params: { estudoId } })`. A aba hoje ignora o parâmetro; o teste da T112 confere **só a chamada de navegação** (parâmetro certo), não o filtro, que é da T117. Registrar a pendência "ler `estudoId` em `visitas.tsx`" na T117 (seção 7).
8. **Acessibilidade como requisito, não acabamento.** Cartão = `Pressable` com `accessibilityRole="button"`, `accessibilityLabel` no formato do RQ-09 (`Estudo X, em execução, incluído em 30/10/2026`, acrescentando `, retirado` quando for o caso) e `minHeight: 44`. O selo nunca é a única indicação: o rótulo textual está sempre presente.
9. **Testes nesta task.** O guia deixa os testes de componente para a T113, mas a T110 mostrou que entregar sem teste deixa a regra sem prova. A T112 entrega os testes **das duas telas e do utilitário**; a T113 amplia (cenários extras, E2E de integração). Isto evita que a T113 vire gargalo e mantém a Definition of Done ("há testes automatizados").
10. **Sem dado sensível no log.** Nenhum `console.log` de resposta da API. Mensagens de erro vêm de `mensagemDoErro`; nada de JSON cru na tela (RQ-10).

## 3. Passos de implementação

Cada passo deixa o projeto compilando e os testes verdes (`npm run typecheck`, `npm run lint`, `npx jest`).

### Passo 0 · Branch e linha de base (10 min)

- Pré-condição: T110 (#38) mergeada na `develop` (ou partir de `origin/feat/task110-login-logout-app`). Criar `feat/task112-meus-estudos-detalhe`.
- Rodar `npm ci`, `npm run typecheck`, `npm run lint` e `npx jest` em `mobile/ClinicalStudyPB`; anotar a linha de base (na T110: 90 testes verdes).
- Deixar `docs/tutorial-inicializacao.md` (não versionado) fora do PR.

### Passo 1 · Utilitário de rótulos e datas (20 min)

- APP `utils/estudos.ts` (decisões 1 e 2): `rotuloStatusEstudo`, `tomStatusEstudo`, `rotuloSituacaoParticipacao`, `formatarData`, `formatarPeriodo` (`"01/10/2026 a 01/05/2027"`; ausência de uma ponta mostra "—" naquela ponta; as duas ausentes mostram "Período não informado").
- APP `utils/__tests__/estudos.test.ts`: os quatro status, valor desconhecido, data com zero à esquerda, data **sem deslocamento de fuso** (rodar também com `TZ=America/Los_Angeles` para provar o ponto da decisão 1), período completo, só início, só fim, nenhum.

### Passo 2 · API do detalhe (15 min)

- APP `api/estudos.ts`: tipo `MeuEstudoDetalhe = components['schemas']['MeuEstudoDetalheResponse']` e `estudosApi.buscar(estudoId)` sobre `GET /api/me/estudos/{estudoId}` (usar `encodeURIComponent` no id). Atualizar o comentário do arquivo (a T112 deixa de ser "a fazer").
- Ler `components/ui.tsx` e decidir a decisão 3 (se `Tela` rola).
- APP `api/__tests__/estudos.test.ts` (ampliar, no molde do `listar`): `buscar` devolve o detalhe com mocks; id desconhecido rejeita com `ErroApi` de `status 404`.

### Passo 3 · Lista Meus estudos (M03) (45 min)

- APP `app/(tabs)/estudos.tsx` (decisões 3 e 4):
  1. Estado: `estudos | null`, `carregando`, `atualizando`, `erro`.
  2. `carregar()` chama `estudosApi.listar()`; tratar o desmonte do componente (não atualizar estado depois de sair da tela).
  3. Cartão (`Cartao` dentro de `Pressable`): título, `Selo` do status do estudo, `Selo` "Retirado" (tom `aviso`) quando `situacaoParticipacao === 'RETIRADO'`, "Incluído em dd/MM/yyyy" e o `resumo`.
  4. `onPress` → `router.push('/estudos/' + estudoId)`.
  5. Estados do RQ-03 e `RefreshControl` do RQ-04.
- Conferir no emulador ou com a suíte de componente que a ordem é a da API (sem reordenar no cliente, RQ-02).

### Passo 4 · Detalhe do estudo (M04) (50 min)

- APP `app/estudos/[estudoId].tsx`:
  1. `useLocalSearchParams<{ estudoId: string }>()`; carregar com `estudosApi.buscar`.
  2. Conteúdo (RQ-05): título, objetivo completo, responsável, período (`formatarPeriodo`), `Selo` do status do estudo, `Selo` da situação (Ativo em `sucesso`, Retirado em `aviso`); para retirado, "Retirado em dd/MM/yyyy" a partir de `retirada.dataRetirada`. **Não existe campo de motivo no tipo e a tela não o deve inventar.**
  3. Botão "Ver visitas deste estudo" (decisão 7).
  4. Estados (RQ-07): `Carregando`; erro genérico com "Tentar de novo"; **404** mostra "Estudo não encontrado." com botão "Voltar" (`router.back()`; se não houver histórico, `router.replace('/(tabs)/estudos')`).
- Não repetir o título dentro do corpo em conflito com o cabeçalho "Estudo": o cabeçalho é fixo (RQ-08), o título do estudo vai no corpo.

### Passo 5 · Testes das telas (50 min)

Molde: `src/telas/__tests__/inicio-e-perfil.test.tsx` (mock de `useSessao`, `jest.spyOn(estudosApi, ...)`, `@testing-library/react-native`).
- APP `app/__tests__/estudos.test.tsx` (nome citado no guia) ou `telas/__tests__/estudos.test.tsx` (seguir a pasta já usada pela T110; **decidir pela existente**, e registrar o caminho no documento de testes):
  - **Lista:** carregando; dados com título, rótulo do status, data formatada e `accessibilityLabel` completo; selo "Retirado"; vazia (texto exato do RQ-03); erro com "Tentar de novo" que recarrega; pull-to-refresh recarrega e termina o indicador (sucesso **e** erro); refresh com erro **mantém** os dados; toque no cartão chama `router.push` com o id certo.
  - **Detalhe:** dados completos (objetivo inteiro, responsável, período); período parcial; retirado com data e **sem** a palavra "motivo"; erro com "Tentar de novo"; 404 mostra "Estudo não encontrado." e o botão volta; o atalho chama a navegação com `estudoId`.
- Falha de teste: **registrar em `registro-de-defeitos.md` (`D-T112-NN`) antes de corrigir**, como manda a regra do projeto; nunca excluir o teste sem registro.

### Passo 6 · Conferência automática (10 min)

- `npm run typecheck`, `npm run lint`, `npx jest` (tudo verde; anotar a contagem final) e `npx expo export --platform web`/o build que o job "Mobile (Expo)" do CI usa (conferir em `.github/workflows/ci.yml`).
- Rodar o app com `EXPO_PUBLIC_USE_MOCKS=true` e percorrer: login → Estudos → cartão → detalhe → atalho → voltar; conta `sem.vinculo@clinicalstudy.test` mostra a lista vazia.

### Passo 7 · Conferência manual com a API real (20 min, RQ-11)

- Subir o ambiente (`docs/tutorial-inicializacao.md`), perfil `demo`, e abrir o app em emulador ou celular (`EXPO_PUBLIC_API_URL` apontando para o backend).
- Etapa (a) (mocks) e (b) (API real) da decisão 6; registrar o que **não** pôde ser verificado (retirado real).

### Passo 8 · Documentação e comunicação (30 min)

- `docs/testes/testes-t112-telas-meus-estudos-e-detalhe.md` no formato do `testes-t110-*` (escopo, como executar, resultados, matriz RQ × teste, lacunas, falhas, roteiro manual).
- `docs/design/sprint3/README.md`: marcar a T112 e acrescentar o link deste plano (como a T110 tem o seu); ressalva da decisão 5.
- `T109-pendencias-para-proximas-tasks.md`: fechar P13 a P16; acrescentar para a **T117** a leitura do parâmetro `estudoId` em `visitas.tsx`.
- Gravar documentos com Write/Edit, sem heredoc nem crases no shell.

## 4. Riscos e cuidados

- **Fuso nas datas** (decisão 1): `new Date('2026-10-30')` mostra o dia errado em fuso negativo. Teste com `TZ` diferente.
- **`ScrollView` com `FlatList` aninhada** (decisão 3): aviso do React Native e rolagem quebrada.
- **Estado após desmontar:** voltar da tela durante a carga não pode atualizar estado (aviso e vazamento).
- **Dependência do backend:** o selo "Retirado" real depende da T101/T103; não prometer demonstração que o backend ainda não sustenta (decisão 5).
- **Privacidade:** o app nunca exibe motivo de retirada nem dados de outro participante; o tipo não tem o campo e o teste confere a ausência da palavra.
- **Rota do detalhe:** `estudoId` malformado deve cair no 404 do servidor, sem quebrar o app.
- **Escopo:** sem botão "Solicitar retirada" (RF20, Sprint 4), sem lista de visitas dentro do detalhe, sem alteração em backend, web ou contrato.

## 5. Mapa requisito × entrega × verificação

| RQ | Entregue em | Verificação |
|---|---|---|
| 01 | Passos 1 e 3 | Teste da lista: título, rótulo do status, data `dd/MM/yyyy`, selo "Retirado" |
| 02 | Passo 3 | Ordem da API preservada; toque chama `router.push('/estudos/{id}')` |
| 03 | Passo 3 | Testes de carregando, erro com "Tentar de novo" e vazio (texto exato) |
| 04 | Passo 3 | Teste de pull-to-refresh (sucesso e erro; indicador termina) |
| 05 | Passos 1 e 4 | Teste do detalhe: objetivo completo, responsável, período, status, situação, data da retirada, sem motivo |
| 06 | Passo 4 | Teste do atalho: navegação com `estudoId` |
| 07 | Passo 4 | Testes de carregando, erro e 404 ("Estudo não encontrado." + voltar) |
| 08 | (T108, já pronto) | Conferir no Passo 6 que o cabeçalho "Estudo" e "Voltar" aparecem |
| 09 | Passos 3 e 4 | `accessibilityRole`, `accessibilityLabel` e `minHeight: 44` conferidos nos testes |
| 10 | Passos 1, 3 e 4 | Nenhum JSON cru; datas brasileiras (teste do utilitário) |
| 11 | Passo 7 | Roteiro manual (mocks e API real); parcial se a T111 não estiver pronta |

## 6. Inventário de arquivos (rastreabilidade do que será tocado)

| Ação | Arquivo (APP) | Passo | RQ |
|---|---|---|---|
| Criar | `utils/estudos.ts` e `utils/__tests__/estudos.test.ts` | 1 | 01, 05, 10 |
| Alterar | `api/estudos.ts` e `api/__tests__/estudos.test.ts` | 2 | 05, 07 |
| Alterar | `app/(tabs)/estudos.tsx` | 3 | 01 a 04, 09, 10 |
| Alterar | `app/estudos/[estudoId].tsx` | 4 | 05 a 07, 09, 10 |
| Criar | teste das telas (`estudos.test.tsx`; pasta conforme Passo 5) | 5 | 01 a 07, 09 |
| Criar | `docs/testes/testes-t112-telas-meus-estudos-e-detalhe.md` | 8 | todos |
| Alterar | `docs/design/sprint3/README.md`, `T109-pendencias-para-proximas-tasks.md`, `docs/testes/registro-de-defeitos.md` (só se houver falha) | 8 | 11 |
| **Não alterar** | `app/_layout.tsx` (RQ-08 já pronto), backend, web, `openapi-sprint3.yaml`, `schema-sprint3.d.ts` | — | — |

## 7. Pendências herdadas que esta task fecha (e as que ela cria)

| Origem | Item | Situação ao fim da T112 | Onde fica registrado |
|---|---|---|---|
| T109 P13 | Tipos em `schema-sprint3.d.ts`, não `schema.d.ts` | Fechada: usar o arquivo certo e corrigir o texto dos designs T109, T112 e T115 | `T109-pendencias...md` |
| T109 P14 | Tratar `RETIRADO` desde já | Fechada (selo e data da retirada) | Testes de componente |
| T109 P15 | `resumo` com até 160 caracteres e `…`; detalhe com `objetivo` completo | Fechada | Teste do detalhe |
| T109 P16 | Mocks no formato real | Já atendida pela T115; só conferida | Passo 6 |
| T117 | Ler o parâmetro `estudoId` em `visitas.tsx` | **Criada** | `T109-pendencias...md` |
| T101/T103/T105 | Ver o "Retirado" real na API | **Criada**: lacuna do RQ-11 até lá | `testes-t112-...md` |
| T111 | Conta com vínculo para a conferência real | **Dependência** do RQ-11 | Passo 7 |
| T113/T114 | Ampliar os testes e o E2E (Maestro) sobre estas telas | Entrada: lista de testes já feitos na T112 | `testes-t112-...md` |

## 8. Critérios de aceite (conferência final)

- [ ] O participante vê a lista dos estudos em que está incluído (ordem da API) e abre o detalhe de cada um.
- [ ] Carregamento, erro com "Tentar de novo", lista vazia e rolagem para atualizar funcionam (o indicador sempre termina).
- [ ] O detalhe mostra objetivo completo, responsável, período, status e situação; "Retirado" e a data aparecem, e o motivo **nunca**.
- [ ] Estudo inexistente (404) mostra "Estudo não encontrado." e deixa voltar.
- [ ] Datas corretas em qualquer fuso (teste com `TZ` diferente) e textos em português.
- [ ] `npm run typecheck`, `npm run lint` e `npx jest` passam; CI do app verde.
- [ ] Documento de testes, README, pendências e (se houver falha) registro de defeitos atualizados.

## 9. Estimativa

| Bloco | Tempo |
|---|---|
| Passos 0 a 2 (base, utilitário, API) | 0h45 |
| Passos 3 e 4 (lista e detalhe) | 1h35 |
| Passo 5 (testes das telas) | 0h50 |
| Passos 6 e 7 (conferência automática e manual) | 0h30 |
| Passo 8 (documentação) | 0h30 |
| **Total** | **~4h10** |

A estimativa do planejamento é 3h. O excedente vem dos testes de componente (que o guia empurra para a T113), da conferência com a API real e da documentação. Se for preciso cortar, cortar nesta ordem: a conferência com a API real (fica parcial, como na T110), os testes extras de acessibilidade e o refresh com erro. **Nunca cortar:** o tratamento de fuso nas datas, a ausência do motivo da retirada, o 404 do detalhe e o registro das falhas de teste antes de corrigir.
