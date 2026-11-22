# T109 · O que as próximas tasks precisam alterar

> Gerado na implementação da T109 (19 a 20/11/2026, opção A: a T109 não depende da retirada). Cada item diz **onde** mexer, **o quê** e **como conferir**. Os testes da T109 estão em [testes-t109](../../testes/testes-t109-acesso-do-participante-e-endpoints-me.md).

## 1. O que a T109 entregou

- `GET /api/me/estudos` e `GET /api/me/estudos/{estudoId}` (só `PARTICIPANTE`), `AcessoParticipante`, `MeusEstudosService`, `MeController`, DTOs, enum `SituacaoParticipacao` e `Permissoes.PARTICIPANTE`.
- Contrato `openapi-sprint3.yaml` com 12 operações e tipos do app regenerados em `schema-sprint3.d.ts`.
- Seed de demonstração `R__demo_24_conta_participante.sql`: `demo.participante@clinicalstudy.test` vinculada ao P5 (CAND-005); `participante@clinicalstudy.test` (seed V2) fica **sem vínculo** de propósito, para o aviso "Você ainda não está incluído em nenhum estudo.".

## 2. Pendências por task

### T101 · modelo, migration e contrato da retirada (obrigatório)

| # | Alterar | Onde | Como conferir |
|---|---|---|---|
| P2 | A coluna `situacao` da participação deve usar a enum **já existente** `domain.enums.SituacaoParticipacao` (`ATIVO`, `RETIRADO`), em vez de criar outra | `Participacao.java`, nova migration `V6_23_*` | `situacaoParticipacaoDoContratoIgualAoDoBackend` continua passando |
| P2 | Trocar o método `situacaoDe` de `MeusEstudosService` (hoje devolve sempre `ATIVO`, marcado `TODO(T101)`) pela leitura da coluna | `service/MeusEstudosService.java` | Novo teste: participação retirada aparece na lista com `RETIRADO` |
| P2 | Trocar o método `retiradaDe` (hoje devolve `null`, marcado `TODO(T101/T103)`) para devolver `new RetiradaDoMeuEstudo(dataRetirada)`; **nunca** passar o motivo | `service/MeusEstudosService.java`, `RetiradaDoMeuEstudo.java` | Teste: `$.retirada.dataRetirada` presente e `$.retirada.motivo` ausente |
| P2 | Buscar `grep -rn "TODO(T101" backend/` antes de fechar a T101: devem sobrar zero ocorrências | backend | O comando não retorna nada |
| P3 | Atualizar a descrição dos dois endpoints `/me` e do schema `SituacaoParticipacao` no YAML, que hoje dizem "enquanto a retirada não existir, vem sempre ATIVO" | `docs/api/openapi-sprint3.yaml` | Texto sem a ressalva |

### T103 · endpoints da retirada

| # | Alterar | Onde | Como conferir |
|---|---|---|---|
| P1 | Somar as 2 operações da retirada à contagem esperada: **12 + 2 = 14**, que é o número que o design da T109 (RQ-T109-03) já previa | `ContratoOpenApiSprint3Test.OPERACOES_ESPERADAS` | O teste de contagem passa com 14 |
| P4 | A exigência de 409 do teste `operacoesDeclaramAsRespostasDeErro` vale para o `POST` da retirada; não relaxar além de `/api/me/**` | mesmo teste | Continua exigindo 401, 403, 404 e 409 fora do `/me` |

### T106 / T113 · testes

| # | Alterar | Onde |
|---|---|---|
| P5 | Teste do caso **retirado** no `/me` (lista e detalhe), que não pôde ser escrito na T109 | `MeEndpointTest` ou novo teste da T106/T113 |
| P6 | A T113 pode reaproveitar os cenários de `MeEndpointTest` (montagem por JDBC de estudo, candidato vinculado e outra conta) em vez de recriá-los; considerar extrair para `support/CenarioParticipante` |
| P7 | Teste de contagem de consultas (sem N+1) do `/me/estudos`, como o do histórico (T96): o repositório usa `@EntityGraph`, mas ainda não há teste que o prove |

### T111 · vínculo da conta do participante

| # | Alterar | Onde | Como conferir |
|---|---|---|---|
| P8 | A migration `V6_24_1` cria o `UNIQUE` em `candidatos.usuario_id`. O seed `R__demo_24_conta_participante.sql` já respeita o um para um (não vincula uma conta que já tem candidato); conferir que a carga do perfil `demo` ainda roda sem violar o `UNIQUE` | `db/migration`, `db/demo` | Subir o perfil `demo` em banco limpo | ✅ **Fechada na T111**: `DemoDataIntegrationTest.demoVinculaAsContasDoParticipanteEEhIdempotente` |
| P9 | `AcessoParticipante.participacoesDe` hoje devolve **lista** (a restrição de unicidade ainda não existe e o design prevê várias participações por conta). Se a T111 impuser um candidato por conta, uma conta continua tendo uma participação por candidato; não mudar a assinatura sem avisar a T116 | `service/AcessoParticipante.java` | `AcessoParticipanteTest` |
| P10 | Os testes que ligam a conta usam `INSERT` direto em `candidatos.usuario_id`; ao entrar o `UNIQUE`, `MeEndpointTest.listaSoOsEstudosDaConta` **vai falhar** porque vincula **dois candidatos à mesma conta**. Ajustar o teste (duas contas, ou um candidato com dois estudos não é possível) **documentando a falha antes**, como nesta task | `MeEndpointTest.listaSoOsEstudosDaConta` | Registrar em `registro-de-defeitos.md` antes de corrigir | ✅ **Fechada na T111**: registrada como D-T111-02 e o teste reescrito (um candidato por conta), sem ser excluído |

### T110 · login e logout no app

| # | Alterar | Onde |
|---|---|---|
| P11 | Para testar o aviso "Você ainda não está incluído em nenhum estudo.", usar `participante@clinicalstudy.test` (sem vínculo, lista vazia com 200); para o fluxo com estudo, `demo.participante@clinicalstudy.test` (perfil `demo`). ⚠️ **Mudou na T111**: no perfil `demo`, `participante@` agora está vinculada ao P1 (veja o `R__demo_24`); para o aviso de lista vazia use uma conta nova criada pelo administrador ou o perfil de teste |
| P12 | Usar `estudosApi.listar()` sobre `GET /api/me/estudos`; lista vazia não é erro |

### T112 · telas Meus estudos e detalhe

| # | Alterar | Onde |
|---|---|---|
| P13 | Tipos prontos em `src/api/schema-sprint3.d.ts` (`MeuEstudoResponse`, `MeuEstudoDetalheResponse`). **O design da T109 cita `schema.d.ts`, mas o `npm run api:tipos` gera os da Sprint 3 em `schema-sprint3.d.ts`**: corrigir o texto dos designs da T109, T112 e T115 |
| P14 | `situacaoParticipacao` e `retirada` já existem no tipo, mas só têm valor real depois da T101; a tela deve tratar `RETIRADO` (selo) desde já |
| P15 | `resumo` termina com `…` quando cortado e tem no máximo 160 caracteres; o detalhe traz `objetivo` completo |
| P16 | Atualizar os mocks do app (`src/api/mocks`) para o formato real de `MeuEstudoResponse` e `MeuEstudoDetalheResponse` |

### T115 / T116 · consultas de visitas do participante

| # | Alterar | Onde |
|---|---|---|
| P17 | Usar `AcessoParticipante.participacaoDoEstudo(usuarioId, estudoId)` e `participacaoPorId(...)` para validar que o `estudoId` pedido em `?estudoId=` é da conta (404 se não for). Não recriar a regra |
| P18 | Receber o `usuarioId` por `Autenticacao.idDe(jwt)` no controller; o componente não conhece `Jwt` |
| P19 | `MeController` já tem `@PreAuthorize(Permissoes.PARTICIPANTE)` no nível da classe; os novos endpoints `/me/visitas/**` devem ficar no mesmo controller (ou em outro com a mesma anotação) e o regex `me/.*` do `ContratoOpenApiSprint3Test` já os cobre |
| P20 | Somar as 2 operações ao `OPERACOES_ESPERADAS` e à constante `OPERACOES_ME` |

### README da Sprint 3 e rastreabilidade

| # | Alterar | Onde |
|---|---|---|
| P21 | Marcar a T109 no README da Sprint 3 como concluída **com ressalva**: a situação `RETIRADO` e a data de retirada dependem da T101/T103 | `docs/design/sprint3/README.md` |
| P22 | RQ-T109-14: avisar a **I5** de que o `AcessoParticipante` está pronto. Ação humana, no Pull Request | PR |
| P23 | Atualizar a rastreabilidade de erros (`docs/rastreabilidade/rastreabilidade-erros-e-alteracoes.md`) e o `README.md` com D-T109-01 e D-T109-02, **em branch de documentação própria**, como manda o registro de defeitos | docs |

### Pendências criadas pela T111 (vínculo da conta do participante)

| # | Alterar | Onde | Como conferir |
|---|---|---|---|
| P24 | **T101/T104:** trocar o corpo de `VinculoContaService.participacaoAtiva` por `!participacao.estaRetirada()` (hoje devolve `true`, marcado `TODO(T101)`) e o `TODO(T104)` de `AreaAcoesParticipante` (esconder o botão também com a participação retirada). Quando a T101 entrar, o ramo 409 `PARTICIPANTE_RETIRADO` passa a ter um teste de ponta a ponta no `ContaParticipanteEndpointTest` | `service/VinculoContaService.java`, `AreaAcoesParticipante.tsx` | `grep -rn "TODO(T101" backend/` e `grep -rn "TODO(T104" web/` sem resultados |
| P25 | **T105:** a conta `demo.participante@` está no **P5**, não no P6 (retirado) como pede o RQ-T111-13, porque o P6 só passa a existir na T105. Na T105, anular o vínculo do P5 e ligar a conta ao P6 em `R__demo_24_conta_participante.sql` | `db/demo/R__demo_24_conta_participante.sql`, `DemoContaParticipanteTest` | Carga demo em banco limpo; `participante@` segue no P1 |
| P26 | **T110:** `participante@clinicalstudy.test` deixa de ser a conta "sem vínculo" no perfil `demo` (agora está no P1). Atualizar o roteiro 6 e o `testes-t110-...md`; o aviso de lista vazia se demonstra com uma conta nova criada pelo administrador (fluxo real do RF01), e nos testes automáticos, no perfil de teste, `participante@` segue sem vínculo | `docs/testes/testes-t110-login-e-logout-no-app.md` | Roteiro manual |
| P27 | **PO (T94):** confirmar que uma conta de participante serve a **um só candidato** (consequência do `UNIQUE`): quem participa de dois estudos precisa de duas contas. Registrar a resposta no README | README da Sprint 3 | Resposta registrada |
| P28 | **T113/T114:** nenhum E2E novo cobre o vínculo (a T111 entrega testes de serviço, endpoint, corrida, componente e dados de demonstração). Se o grupo quiser, acrescentar um passo ao E2E da ficha | `web/ClinicalStudyPB/e2e` | Fluxo E2E do vínculo |
| P29 | **I5 (PBI-25):** avisar, no Pull Request da T111, que `participante@` → P1 (visita realizada com resultado e visita agendada) e `demo.participante@` → P5 estão disponíveis no perfil `demo` (RQ-T111-14). Ação humana | PR | Aviso feito |
