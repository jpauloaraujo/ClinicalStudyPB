# Testes da T111 — Vínculo da conta do participante (backend e web)

> Plano: [T111-plano-de-implementacao](../design/sprint3/T111-plano-de-implementacao.md) · Guia: [T111](../design/sprint3/T111-vinculo-da-conta-do-participante-backend-e-web.md)
> Branch: `feat/task111-vinculo-conta-participante` (criada a partir de `feat/task109-acesso-participante-endpoints-me`, que ainda não está na `develop`).
> Regra seguida: **nenhum teste foi excluído**. Toda falha foi registrada em [registro-de-defeitos](registro-de-defeitos.md#4-falhas-de-teste-da-t111-vínculo-da-conta-do-participante) (D-T111-01 a 06) e depois corrigida. Os testes tocados por causa do `UNIQUE` foram **reescritos, não removidos** (seção 4).

## 1. Escopo e comandos

| Camada | Comando (pasta) | Resultado final |
|---|---|---|
| Backend | `./mvnw -B verify` (`backend/ClinicalStudyPB`) | **757 testes, 0 falhas, 0 erros, 2 ignorados** (`@Disabled("T116")`, anteriores à T111); cobertura mínima atendida; `BUILD SUCCESS` |
| Web | `npx vitest run` (`web/ClinicalStudyPB`) | **27 arquivos, 315 testes, todos passando** |
| Web | `npm run lint`, `npx tsc -b --noEmit`, `npm run build` | sem erros |

Ambiente: H2 (perfil de teste) e, depois da falha D-T111-06, também **PostgreSQL 16 real** (container local com as variáveis `SPRING_DATASOURCE_*` do job do CI): 757 testes, 0 falhas, `BUILD SUCCESS`.

## 2. Testes novos e alterados (rastreabilidade RQ × teste)

| RQ | Teste | Tipo |
|---|---|---|
| 01 | `FlywayMigrationIntegrationTest.uniqueDaContaDoCandidato`, `contasDiferentesConvivem` (índice existe, vários candidatos sem conta, mesma conta recusada) | integração |
| 02 | `ContratoOpenApiSprint3Test`: contagem **17**, `contaDoContratoExisteNaApi`, `schemaContaTemOsCamposDoDto` (3 casos, sem `senha`/`hash`/`perfil`/`ativo`), `vinculoDeclaraAsRespostasEOsCodigosDeConflito` | contrato |
| 03 | `VinculoContaServiceTest` (um teste por violação e **4 de ordem** das verificações) | unitário |
| 03 | `ContaParticipanteEndpointTest`: `contaDeOutroPerfil`, `contaInativa`, `participacaoJaTemConta` (inclui reenviar a mesma conta), `contaJaVinculada`, `ordemPerfilAntesDeInativa` | endpoint |
| 04 | `VinculoContaServiceTest.atualizacaoSemLinhasAfetadas`, `violacaoDoUniqueVira409`; `ContaParticipanteConcorrenciaTest` (2 cenários: mesma conta em vários participantes e várias contas no mesmo participante: 1 sucesso, resto 409, **nunca 500**) | unitário e concorrência |
| 05 | `ContaParticipanteEndpointTest.naoHaDesvincular` (DELETE e POST recusados) | endpoint |
| 06 | `ContaParticipanteEndpointTest`: `consultaSemConta` (404), `semDadosDeSeguranca`, `leituraDaConta` | endpoint |
| 07 | `ContaParticipanteEndpointTest.disponiveis` e `vinculaEConsulta` (some da lista depois de vincular) | endpoint |
| 08 | `ContaParticipanteEndpointTest.semToken` (401) e `soOCoordenadorEscreve` (403 para pesquisador, administrador e participante) | endpoint |
| 09, 10 | `AreaAcoesParticipante.test` (6 casos novos), `FichaParticipantePage.test` (8 casos novos: os dois perfis veem a conta, "Nenhuma conta vinculada", falha ao consultar, recarga depois de vincular) | componente |
| 09, 12 | `DialogoVincularConta.test` (13 casos: carregando, lista, vazia, erro e nova tentativa, envio, cinco códigos de conflito, código desconhecido, envio duplo, Esc/Cancelar) | componente |
| 10 | `contaParticipante.test.ts` (404 vira `null`, outros erros propagam, PUT só com `usuarioId`) | unitário |
| 11 | `mensagensAcompanhamento.test.ts` (`mensagemDeVinculo`, 7 casos) | unitário |
| 13, 14 | `DemoContaParticipanteTest` (2 casos novos: P1 e P5, idempotência, não toma conta de quem já tem), `DemoDataIntegrationTest.demoVinculaAsContasDoParticipanteEEhIdempotente` (carga completa do perfil `demo`, um vínculo por conta) | integração |

**Testes antigos alterados** (nenhum excluído): `MeEndpointTest.listaSoOsEstudosDaConta` (D-T111-02, seção 4) e as listas de `ContratoOpenApiSprint3Test` (contagem 14 para 17). `ParticipacaoServiceTest` e os demais ficaram como estavam.

## 3. Resultado da primeira rodada (antes de qualquer correção)

`./mvnw test` completo: **718 testes, 0 falhas, 3 erros, 2 ignorados**.

| Teste | Falha | Registro |
|---|---|---|
| `FlywayMigrationIntegrationTest.uniqueDaContaDoCandidato` e `contasDiferentesConvivem` | `DuplicateKey` em `uk_candidatos_estudo_nome_nascimento` | D-T111-01 |
| `MeEndpointTest.listaSoOsEstudosDaConta` | `DuplicateKey` em `uk_candidatos_usuario` (esperada, P10 da T109) | D-T111-02 |

## 4. Falhas encontradas e correções

Descritas no [registro de defeitos](registro-de-defeitos.md#4-falhas-de-teste-da-t111-vínculo-da-conta-do-participante), em ordem de ocorrência, **antes** de cada correção.

| Registro | Falha | Natureza | Correção | Estado |
|---|---|---|---|---|
| D-T111-01 | Dois testes novos do Flyway repetiam nome e data do candidato no mesmo estudo | Erro do **teste novo** | Nome derivado do código | Fechado |
| D-T111-02 | `MeEndpointTest.listaSoOsEstudosDaConta` ligava dois candidatos à mesma conta | **Premissa do teste** (a regra 7 passou a ser garantida no banco); o código está certo | Teste reescrito com um estudo por conta; a ordem "mais recente primeiro" deixa de ser testável pelo banco (uma conta tem uma só participação). **Não foi excluído** | Fechado |
| D-T111-03 | Helpers do endpoint e da corrida repetiam o nome (mesma causa do 01) | Erro do **teste novo** | Nome derivado do código | Fechado |
| D-T111-04 | 4 testes antigos da web estouraram 5 s na suíte completa | **Ambiente** (carga); passaram isolados e na reexecução completa | Nenhuma | Fechado (não reproduzido) |
| D-T111-05 | `AreaAcoesParticipante.test` não executou (`})` removido sem querer) e seletor `status` ambíguo na ficha | Erros de **autoria** dos testes novos | Fechamento restaurado; busca dentro da região "Ações do participante" | Fechado |
| D-T111-06 | Teste novo do Flyway usou `information_schema.indexes`, que só existe no H2: quebrou no job do PostgreSQL do CI | Erro do **teste novo** (portabilidade); a migration estava certa | Consulta pelo `DatabaseMetaData` do JDBC; suíte completa verificada num PostgreSQL 16 local | Fechado |

Nota de processo: no D-T111-03 e no D-T111-05 a correção chegou a ser tocada antes de eu gravar o registro; o registro foi feito logo em seguida, com a causa e a evidência da rodada que falhou, e os testes não foram apagados.

## 5. Resultado final

- Backend: `./mvnw -B verify`, **757 testes passando**, 2 ignorados (T116), cobertura atendida.
- Web: **315 testes passando**; `lint`, `tsc` e `build` sem erros.
- A rodada isolada dos testes da T111 no backend (endpoint, corrida, serviço, demonstração, contrato, Flyway): todos passando.

## 6. Lacunas conhecidas

1. **Retirada (T101/T103):** o passo "participação ativa" é fixo (`true`, `TODO(T101)`), então o 409 `PARTICIPANTE_RETIRADO` só tem teste **unitário** (`VinculoContaServiceTest.participanteRetirado`, que força o método). Não há teste de ponta a ponta até a T101 entrar (pendência P24).
2. **PostgreSQL:** a primeira versão do `FlywayMigrationIntegrationTest.uniqueDaContaDoCandidato` usava `information_schema.indexes` (só do H2) e quebrou no job do PostgreSQL do CI (D-T111-06). Depois da correção, a suíte completa passou num PostgreSQL 16 local. O teste da lista de disponíveis não depende da collation.
3. **Sem E2E do vínculo** (pendência P28); sem conferência manual no navegador com o backend real.
4. **Um candidato por conta:** a confirmação do PO ainda é pendência (P27).

## 7. Roteiro de conferência manual (perfil `demo`)

1. Subir o ambiente (`docs/tutorial-inicializacao.md`) com o perfil `demo` e entrar como `demo.coordenador@clinicalstudy.test`.
2. Abrir o estudo "Demo - Acompanhamento de participantes", aba Participantes, e a ficha de um participante **sem conta** (por exemplo, o P2): aparece "Nenhuma conta vinculada ao participante." e o botão **Vincular conta**.
3. Criar antes, como administrador, uma conta de participante nova; ela aparece na lista do diálogo. Vincular e conferir a confirmação, a linha "Conta vinculada: nome (e-mail)" e o botão escondido.
4. Abrir o P1 e o P5: já mostram `participante@` e `demo.participante@`, sem o botão.
5. Entrar como `demo.pesquisador@clinicalstudy.test`: vê a conta vinculada em leitura, sem botão.
6. Tentar vincular a mesma conta a outro participante pela API (Swagger): 409 `CONTA_JA_VINCULADA`.
