# T109 · Resumo vivo da implementação

> Documento **vivo**: é o ponto de entrada de tudo o que foi feito na T109 (Acesso do participante e endpoints /me no backend, PBI-24, Sprint 3). Atualize-o a cada nova implementação seguindo a seção 9. Os detalhes ficam nos documentos ligados aqui; este arquivo guarda o panorama e o histórico.

| | |
|---|---|
| **Branch** | `feat/task109-acesso-participante-endpoints-me` |
| **Design da task** | [T109](T109-acesso-do-participante-e-endpoints-me-no-backend.md) |
| **Testes e falhas** | [testes-t109](../../testes/testes-t109-acesso-do-participante-e-endpoints-me.md) · [registro de defeitos](../../testes/registro-de-defeitos.md) |
| **Pendências para outras tasks** | [T109-pendencias-para-proximas-tasks](T109-pendencias-para-proximas-tasks.md) |
| **Situação** | Implementada com a opção A (ressalva: retirada, seção 3). Sem commit nem PR ainda |
| **Última atualização** | 20/11/2026 |

## 1. Estado atual dos requisitos

| RQ | Situação | Observação |
|---|---|---|
| 01 Contrato dos endpoints `/me` | Feito | 12 operações no `openapi-sprint3.yaml` |
| 02 Schemas | Feito | Motivo da retirada não é exposto |
| 03 Contagem e tipos do app | Feito com 12 | O design previa 14; ver seção 3 |
| 04 `AcessoParticipante` e 404 | Feito | |
| 05 Sem vínculo devolve lista vazia | Feito | |
| 06 Incluir retiradas | **Parcial** | Nenhuma retirada existe ainda (T101) |
| 07 Só recebe `UUID` | Feito | |
| 08 `Permissoes.PARTICIPANTE` | Feito | |
| 09 `MeController` | Feito | |
| 10 Resumo, status, situação, responsável | **Parcial** | `situacaoParticipacao` é sempre `ATIVO` |
| 11 Nenhum dado de outro nem de candidato | Feito | |
| 12 Erros 401, 403, 404 | Feito | |
| 13 Swagger e contrato | Feito | O teste de contrato não tinha `@Disabled` |
| 14 Avisar a I5 | **Pendente** | Ação humana, no Pull Request |

## 2. O que foi implementado

**Backend** (`backend/ClinicalStudyPB/src/main/java/UEPB/ESWII/ClinicalStudyPB/`)

| Arquivo | Ação | Função |
|---|---|---|
| `security/Permissoes.java` | Alterado | `PARTICIPANTE = "hasRole('PARTICIPANTE')"` |
| `repository/ParticipacaoRepository.java` | Alterado | 3 consultas por conta, com `@EntityGraph` (estudo e responsável) |
| `domain/enums/SituacaoParticipacao.java` | Criado | `ATIVO`, `RETIRADO` |
| `service/AcessoParticipante.java` | Criado | Resolve as participações da conta; 404 para dado de outra conta |
| `service/MeusEstudosService.java` | Criado | Lista e detalhe; pontos `situacaoDe` e `retiradaDe` com `TODO(T101)` |
| `controller/MeController.java` | Criado | `/api/me/estudos` e `/api/me/estudos/{estudoId}` |
| `controller/dto/MeuEstudoResponse.java`, `MeuEstudoDetalheResponse.java`, `RetiradaDoMeuEstudo.java` | Criados | DTOs; `resumir` corta o objetivo em 160 |
| `resources/db/demo/R__demo_24_conta_participante.sql` | Criado | Vincula `demo.participante@…` ao CAND-005 |

**Contrato e app**

| Arquivo | Ação |
|---|---|
| `docs/api/openapi-sprint3.yaml` | Tag "Participante (me)", 2 operações, schemas `SituacaoParticipacao`, `MeuEstudoResponse`, `RetiradaDoMeuEstudo`, `MeuEstudoDetalheResponse` |
| `mobile/ClinicalStudyPB/src/api/schema-sprint3.d.ts` | Regenerado com `npm run api:tipos` |

**Testes** (`backend/ClinicalStudyPB/src/test/java/UEPB/ESWII/ClinicalStudyPB/`)

| Arquivo | Casos |
|---|---|
| `AcessoParticipanteTest` | 8 |
| `MeuEstudoResponseTest` | 4 |
| `MeEndpointTest` | 11 |
| `DemoContaParticipanteTest` | 3 |
| `ContratoOpenApiSprint3Test` | +5 casos e 1 teste refinado (D-T109-01) |

## 3. Decisões

| # | Decisão | Motivo | Impacto |
|---|---|---|---|
| D1 | **Opção A**: a T109 não depende da retirada (T101 a T103). `situacaoParticipacao` é sempre `ATIVO` e `retirada` é `null` | A retirada não existe na branch; a opção B bloquearia a T112 e a T116, e a C invadiria o escopo da T101 | Contrato final já publicado; ao entrar a T101, só `MeusEstudosService` muda (pendência P2) |
| D2 | O enum `SituacaoParticipacao` nasce na T109 e a T101 o reaproveita | Evitar dois enums iguais | Pendência P2 |
| D3 | Estudo de outro participante e estudo inexistente dão a **mesma** resposta 404 | Não revelar que o dado existe (regra 7) | Coberto por `estudoDeOutroParticipanteRecebe404` |
| D4 | Conta sem candidato vinculado devolve `200 []` | A T110 mostra o aviso no app | Coberto por `contaSemVinculoRecebeListaVazia` |
| D5 | O seed vincula a conta **demo** e deixa `participante@…` sem vínculo | Permite demonstrar os dois casos | Pendência P11 |
| D6 | Contagem de operações do contrato: 12, não 14 | As 2 operações da retirada não existem | Pendência P1 |

## 4. Resultado dos testes

| Rodada | Data | Total | Falhas | Observação |
|---|---|---|---|---|
| 1 | 19/11/2026 | 703 | 2 | D-T109-01 e D-T109-02, documentadas antes de corrigir |
| 2 | 19/11/2026 | 703 | 0 | Duas correções, ambas em teste; nenhum código de produção mudou |

Comando: `./mvnw test` em `backend/ClinicalStudyPB`. Também passam `npm run typecheck` no app.

## 5. Defeitos

| ID | Resumo | Estado |
|---|---|---|
| D-T109-01 | Teste de contrato exigia 404 e 409 em toda operação, inclusive na lista do `/me` | Fechado |
| D-T109-02 | `jsonPath("$.resumo.length()")` devolvia `null` (erro do teste novo) | Fechado |

## 6. Divergências entre o design e o repositório

| # | Divergência | Tratamento |
|---|---|---|
| 1 | O design cita 14 operações; a branch tem 12 | D6 |
| 2 | O design cita `schema.d.ts`; os tipos da Sprint 3 vão em `schema-sprint3.d.ts` | Pendência P13 (corrigir o texto dos designs T109, T112 e T115) |
| 3 | O design manda remover `@Disabled` do teste de contrato; ele não existia | Nada a fazer |
| 4 | O RQ-06 e o RQ-10 dependem de colunas da T101 | D1 |

## 7. Como validar rapidamente

1. `./mvnw test` em `backend/ClinicalStudyPB` (703 testes, 0 falhas).
2. Subir o backend e abrir o Swagger: tag **Participante (me)**.
3. Com o perfil `demo`, entrar como `demo.participante@clinicalstudy.test` e chamar `GET /api/me/estudos` (um estudo). Com `participante@clinicalstudy.test`, a resposta é `[]`.
4. Com qualquer outro perfil, as duas rotas devolvem 403; sem token, 401.

## 8. Próximos passos

- [ ] Commit e Pull Request (descrever a opção A e as pendências).
- [ ] Avisar a I5 de que o `AcessoParticipante` está na branch (RQ-14, pendência P22).
- [ ] Atualizar o README da Sprint 3 e a rastreabilidade de erros, em branch de documentação (P21 e P23).
- [ ] Quando a T101 entrar: executar a lista P2 de [pendências](T109-pendencias-para-proximas-tasks.md) e remover `TODO(T101)`.

## 9. Como atualizar este documento

A cada nova implementação ou correção ligada à T109:

1. Ajuste as seções 1 a 8 para refletirem o **estado atual** (requisitos, arquivos, decisões, testes, defeitos, próximos passos).
2. Acrescente **uma linha** no histórico abaixo, com data, o que mudou e o commit ou PR.
3. Se um teste falhar, registre primeiro em [registro de defeitos](../../testes/registro-de-defeitos.md) e em [testes-t109](../../testes/testes-t109-acesso-do-participante-e-endpoints-me.md); só depois corrija, e cite o ID aqui.
4. Marque as pendências resolvidas em [T109-pendencias-para-proximas-tasks](T109-pendencias-para-proximas-tasks.md).

## 10. Histórico de atualizações

| Data | Mudança | Commit ou PR |
|---|---|---|
| 19/11/2026 | Branch criada e plano de implementação definido (opção A) | — |
| 19/11/2026 | Contrato, `AcessoParticipante`, `MeusEstudosService`, `MeController`, DTOs, seed demo e 31 testes novos; primeira rodada com 2 falhas (D-T109-01 e 02) | Sem commit |
| 20/11/2026 | Falhas corrigidas (só em teste); segunda rodada com 703 testes e 0 falhas; documentos de testes e de pendências criados | Sem commit |
| 20/11/2026 | Este resumo vivo criado | Sem commit |
