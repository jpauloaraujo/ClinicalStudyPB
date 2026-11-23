# Testes da T113 — Testes de backend e de componente do app

> Plano: [T113-plano-de-implementacao](../design/sprint3/T113-plano-de-implementacao.md) · Guia: [T113](../design/sprint3/T113-testes-de-backend-e-de-componente-do-app.md)
> Branch: `feat/task113-testes-backend-e-componente-app` (criada a partir de `feat/task112-meus-estudos-detalhe`, com a `feat/task111-vinculo-conta-participante` mesclada, porque nenhuma das duas está na `develop`).
> Regra seguida: **nenhum teste foi excluído nem alterado para passar**. A única falha (D-T113-01) foi registrada em [registro-de-defeitos](registro-de-defeitos.md#5-falhas-de-teste-da-t113-testes-de-backend-e-de-componente-do-app) **antes** da correção. Nenhum código de produção foi alterado.

## 1. Linha de base e resultado final

| Verificação | Antes da T113 | Depois |
|---|---|---|
| Backend `./mvnw -B verify` | 757 testes, 0 falhas, 2 ignorados (`@Disabled("T116")`) | **765 testes, 0 falhas, 0 erros, 2 ignorados**; `BUILD SUCCESS`; cobertura de linhas 98,9% |
| App `npm test -- --coverage` | 10 suítes, 129 testes, 95,25% das linhas | igual (nenhum teste novo foi necessário no app, seção 4) |

Cobertura por classe da fatia (JaCoCo, `jacoco.csv`):

| Classe | Linhas | Ramos | Meta do guia (95%) |
|---|---|---|---|
| `AcessoParticipante` | 15/15 (100%) | 9/10 (90%) | linhas atendida; 1 ramo não coberto (combinação de nulos em `participacaoDoEstudo`, já testada por `idsNulosLancam404`) |
| `MeusEstudosService` | 16/16 (100%) | sem ramos | atendida |
| `VinculoContaService` | 38/38 (100%) | 14/14 (100%) | atendida |

## 2. Testes novos

| Arquivo | Testes | RQ |
|---|---|---|
| `service/MeusEstudosServiceTest` | 4: conta com **dois estudos** na ordem recebida; lista vazia; resumo ≤160 e objetivo completo no detalhe, sem retirada; 404 propagado | 02, 03, 05 |
| `MeIsolamentoEndpointTest` | 4, com **login real** de duas contas: A e B em estudos diferentes só veem o próprio e o detalhe cruzado dá 404 nos dois sentidos; A e B no mesmo estudo; JSON sem nenhum campo de candidato em nenhum nível; vincular → entrar → listar | 01, 03, 05, 06 |

**Verificação de sensibilidade (decisão 5 do plano):** troquei temporariamente `participacoesDe` para devolver `participacoes.findAll()` (filtro por conta removido). `MeIsolamentoEndpointTest` falhou em **3 dos 4** testes (lista de A com 2 itens, lista da conta ainda sem vínculo com 1 item). O arquivo foi restaurado e `git diff` em `src/main` está vazio. O teste "A nunca vê o B" protege de fato o isolamento.

## 3. Matriz RQ × teste (inclui o que já existia)

| RQ | Prova | Origem |
|---|---|---|
| 01 | `MeIsolamentoEndpointTest.aNuncaVeEstudoDeBENemBDeA`, `doisParticipantesNoMesmoEstudo`; `MeEndpointTest.listaSoOsEstudosDaConta`, `idPorParametroEhIgnorado` | T113 e T109 |
| 02 | `MeusEstudosServiceTest.doisEstudosMantemAOrdem`, `semParticipacaoListaVazia`; `MeEndpointTest.contaSemVinculoRecebeListaVazia`. **Retirado: não testado** (seção 5) | T113 e T109 |
| 03 | `MeEndpointTest.estudoDeOutroParticipanteRecebe404` (corpo igual ao de estudo inexistente), `contaSemVinculoRecebe404NoDetalhe`; `MeIsolamentoEndpointTest` (404 cruzado) | T109 e T113 |
| 04 | `MeEndpointTest.demaisPerfisRecebem403` (admin, pesquisador, coordenador; lista e detalhe), `semTokenRecebe401` | T109 |
| 05 | `MeuEstudoResponseTest` (corte em 160), `MeEndpointTest.detalheDoEstudoDaConta`, `MeIsolamentoEndpointTest.respostaNaoTrazDadosDoCandidato`, `MeusEstudosServiceTest.resumoCortadoEObjetivoCompleto` | T109 e T113 |
| 06 | `MeIsolamentoEndpointTest.depoisDeVincularAContaVeOEstudo`; `ContaParticipanteEndpointTest.vinculaEConsulta` | T113 e T111 |
| 07 | `service/VinculoContaServiceTest` (violações e ordem), `ContaParticipanteEndpointTest` (um teste por código, `naoEncontrados`). `PARTICIPANTE_RETIRADO` só no teste unitário | T111 |
| 08 | `ContaParticipanteEndpointTest.semToken`, `soOCoordenadorEscreve`, `leituraDaConta` | T111 |
| 09 | `ContaParticipanteEndpointTest.disponiveis`, `semDadosDeSeguranca` | T111 |
| 10 | `ContaParticipanteConcorrenciaTest` (2 cenários, sem `@Transactional`, limpeza no `@AfterEach`) | T111 |
| 11 | Cobertura da seção 1; suíte no PostgreSQL: **não executada localmente** (seção 5) | T113 |
| 12 | `src/telas/__tests__/login.test.tsx` (`it.each` dos três perfis recusados, credencial inválida, campos obrigatórios, rede); `sessao.test.tsx` (token removido) | T110 |
| 13, 14 | `src/telas/__tests__/estudos.test.tsx` (20 testes: lista com dados, vazia, erro, Tentar de novo, pull-to-refresh, detalhe, retirado, 404) | T112 |
| 15 | `src/api/__tests__/cliente.test.ts` (`ErroApi`, rede, 401 com callback, 401 do login não expira) | T108/T110 |
| 16 | `npm test -- --coverage` verde, 95,25% das linhas; `lint` e `typecheck` no CI | T110/T112 |

Equivalência de nomes entre o guia e o código (decisão 2 do plano): `MeEstudosEndpointTest` = `MeEndpointTest`; `VinculoContaEndpointTest` = `ContaParticipanteEndpointTest`; `VinculoContaConcorrenciaTest` = `ContaParticipanteConcorrenciaTest`; testes do app em `src/telas/__tests__/` e não em `src/app/__tests__/` (o Expo Router trata tudo em `src/app/` como rota).

## 4. Falhas encontradas e correções

| Registro | Falha | Natureza | Correção | Estado |
|---|---|---|---|---|
| D-T113-01 | Os 4 testes de `MeIsolamentoEndpointTest` receberam 401 no login | Erro do **teste novo**: o login normaliza o e-mail para minúsculas e o helper gravava maiúsculas | `toLowerCase` no helper; asserções intactas | Fechado |

Nenhum teste foi excluído. Nenhum teste do app falhou (129 passando antes e depois).

## 5. Lacunas e desvios

1. **RQ-T113-02, "dois estudos" pelo endpoint:** o `UNIQUE (usuario_id)` da T111 impede uma conta ter dois candidatos; o caso está provado só no teste de serviço, com `AcessoParticipante` simulado. Depende da confirmação do PO (P27 da T109).
2. **Retirado no `/me` (P5 da T109) e 409 `PARTICIPANTE_RETIRADO` de ponta a ponta (P24):** a T101 (#39) e a T102 (#40) já estão na `develop`, e `MeusEstudosService` lê a situação e a data da retirada. Cobertos no `MeusEstudosServiceTest` (retirado com data e sem motivo; retirado sem registro) e no `MeEndpointTest` (retirado na lista e no detalhe). O 409 `PARTICIPANTE_RETIRADO` do vínculo está no `VinculoContaServiceTest`; o de ponta a ponta pelo endpoint segue sem teste.
3. **PostgreSQL:** a suíte desta task rodou só no H2. Os testes novos usam SQL portável (`CURRENT_DATE`, `CAST ... AS UUID`), mas a evidência no PostgreSQL é o job **Backend (PostgreSQL)** do CI do PR.
4. **Ramo de `AcessoParticipante`** não coberto (1 de 10): ver seção 1.
5. **Não feitos (opcionais do plano):** `support/CenarioParticipante` (P6) e o teste de contagem de consultas do `/me/estudos` (P7).
6. **App:** as lacunas da T110 e T112 (emulador, cabeçalho "Estudo", desmonte durante a carga) continuam; o E2E é da T114.
