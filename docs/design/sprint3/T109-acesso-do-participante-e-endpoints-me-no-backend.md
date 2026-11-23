# T109 · Acesso do participante e endpoints /me no backend

> **Sprint 3** · PBI-24 · RF17 App do participante: login, vínculo da conta e meus estudos (RF02 e RF03 no mobile)  
> **Responsável:** I7 · **Estimativa:** 4h · **Prazo:** Sex 20/11  
> **Depende de:** Sprint 2 (participação e campo de usuário participante)  
> **Situação:** ⬜ A implementar (este documento é o guia para quem for fazer)

> Antes de começar, leia as [convenções e o contrato compartilhado da Sprint 3](00-convencoes-e-contrato-compartilhado.md): nomes, endpoints, DTOs, códigos de erro, tabelas e dados de demonstração valem para esta task. A origem dos requisitos (especificação, critério de aceite do PBI, regras de negócio e o texto original da task) está na [seção 9](#9-origem-dos-requisitos-especificação-e-planejamento).

## 1. Objetivo

Criar a base de acesso do participante no backend: um componente que resolve, a partir do usuário logado, as participações vinculadas à conta (regra 7) e os endpoints `/api/me/estudos`, restritos ao perfil participante. O participante só enxerga os próprios dados: acessar dados de outro retorna 404. A T116 (PBI-25) usa este componente para as consultas de visitas.

## 2. Contexto: o que já existe e deve ser reaproveitado

- A conta do participante é um `Usuario` com perfil `PARTICIPANTE`, criado pelo administrador (RF01) e vinculado pelo coordenador (T111) ao `Candidato` pelo campo `usuario` (opcional, previsto na Sprint 2): o vínculo é o caminho `Usuario → Candidato → Participacao`.
- `Candidato.usuario` (`usuario_id`) já existe na tabela `candidatos` desde a `V4`; o `UNIQUE` do um-para-um chega na T111 (`V6_24_1`). `ParticipacaoRepository`, `PerfilUsuario.PARTICIPANTE`, `Permissoes`, `Autenticacao.idDe(jwt)` e `AuthController` (`/api/auth/me`).
- O seed V2 traz `participante@clinicalstudy.test` (perfil `PARTICIPANTE`, senha `senha-de-teste`); o login pela API funciona para qualquer perfil, e a recusa dos outros perfis no app é feita no cliente (regra 8).
- Padrões: controllers com `@Tag`/`@Operation`/`@PreAuthorize`, serviços `@Transactional`, `RecursoNaoEncontradoException` (404), formato único de erro; DTOs na seção 3 das convenções.

## 3. Onde implementar

| Arquivo | Ação | Responsabilidade |
|---|---|---|
| `docs/api/openapi-sprint3.yaml` | Alterar | Operações `GET /api/me/estudos` e `GET /api/me/estudos/{estudoId}` |
| `mobile/ClinicalStudyPB/src/api/schema.d.ts` | Alterar | Regenerado |
| `backend/ClinicalStudyPB/src/main/java/UEPB/ESWII/ClinicalStudyPB/security/Permissoes.java` | Alterar | `PARTICIPANTE = "hasRole('PARTICIPANTE')"` |
| `backend/ClinicalStudyPB/src/main/java/UEPB/ESWII/ClinicalStudyPB/service/AcessoParticipante.java` | Criar | Resolve as participações da conta logada |
| `backend/ClinicalStudyPB/src/main/java/UEPB/ESWII/ClinicalStudyPB/repository/ParticipacaoRepository.java` | Alterar | `findByCandidatoUsuarioId` e busca por estudo |
| `backend/ClinicalStudyPB/src/main/java/UEPB/ESWII/ClinicalStudyPB/service/MeusEstudosService.java` | Criar | Lista e detalhe dos estudos do participante |
| `backend/ClinicalStudyPB/src/main/java/UEPB/ESWII/ClinicalStudyPB/controller/MeController.java` | Criar | Base dos endpoints `/api/me/**` (restrita ao participante) |
| `backend/ClinicalStudyPB/src/main/java/UEPB/ESWII/ClinicalStudyPB/controller/dto/` | Criar | `MeuEstudoResponse`, `MeuEstudoDetalheResponse` |
| `backend/ClinicalStudyPB/src/test/java/UEPB/ESWII/ClinicalStudyPB/ContratoOpenApiSprint3Test.java` | Alterar | Contagem (14 operações) |

## 4. Requisitos a implementar

Cada requisito tem um identificador (`RQ-T109-NN`) para a revisão do Pull Request e para a conferência no fim da task: todos precisam estar atendidos.

### Contrato dos endpoints `/me`

- **RQ-T109-01** Descrever em `openapi-sprint3.yaml` `GET /api/me/estudos` (200 `List<MeuEstudoResponse>`) e `GET /api/me/estudos/{estudoId}` (200 `MeuEstudoDetalheResponse`; 404 se o estudo não for do participante), **sem receber o id do participante**: ele é identificado pelo token. Perfil permitido: só `PARTICIPANTE` (403 para os demais, 401 sem token).
- **RQ-T109-02** Declarar os schemas `MeuEstudoResponse` (`participacaoId`, `estudoId`, `titulo`, `resumo`, `statusEstudo`, `dataInclusao`, `situacaoParticipacao`) e `MeuEstudoDetalheResponse` (os mesmos mais `objetivo`, `dataInicio`, `dataFim`, `responsavelNome` e `retirada` com só a `dataRetirada`), com exemplos fictícios; o **motivo da retirada não é exposto** ao app.
- **RQ-T109-03** Atualizar a contagem de operações do `ContratoOpenApiSprint3Test` (12 + 2 = 14) e regenerar os tipos do app (`npm run api:tipos` no mobile).

### Componente `AcessoParticipante`

- **RQ-T109-04** Criar `AcessoParticipante` (`@Component` em `service`) com `participacoesDe(UUID usuarioId)` (todas as participações cujo candidato tem `usuario.id = usuarioId`, ordenadas pela data de inclusão decrescente), `participacaoDoEstudo(UUID usuarioId, UUID estudoId)` e `participacaoPorId(UUID usuarioId, UUID participacaoId)`, estes dois lançando **404** (`RecursoNaoEncontradoException`) quando a participação não for da conta logada (a regra 7 manda `404`, nunca `403`, para não revelar que o dado existe).
- **RQ-T109-05** Participante **sem participação vinculada** (conta sem candidato) devolve lista vazia, não erro (a T110 mostra o aviso no app).
- **RQ-T109-06** Incluir participações **retiradas** (`situacaoParticipacao = RETIRADO`): o participante continua vendo o estudo, com a marca de retirado.
- **RQ-T109-07** O componente recebe o `UUID` do usuário (`Autenticacao.idDe(jwt)`); não conhece `Jwt` nem web. A T116 o reutiliza para as visitas.
- **RQ-T109-08** Criar a constante `Permissoes.PARTICIPANTE = "hasRole('PARTICIPANTE')"`.

### Endpoints

- **RQ-T109-09** `MeController` (`@RequestMapping("/api/me")`, `@PreAuthorize(Permissoes.PARTICIPANTE)` no controller): `GET /api/me/estudos` devolve a lista dos estudos do participante logado (um item por participação); `GET /api/me/estudos/{estudoId}` devolve o detalhe e responde **404** se o estudo não for dele (inclusive para um estudo que existe mas é de outro participante).
- **RQ-T109-10** `resumo` é o objetivo do estudo cortado em 160 caracteres (com reticências quando cortado); `statusEstudo` é o `StatusEstudo`; `situacaoParticipacao` é `ATIVO` ou `RETIRADO` (a coluna existe desde a T101); `responsavelNome` é o nome do responsável do estudo.
- **RQ-T109-11** **Nenhum dado de outro participante** em nenhuma resposta (nem contagens), e nenhum campo de candidato além do necessário (o app não recebe código nem nome fictício de candidatos).
- **RQ-T109-12** Erros no formato único: 401, 403 (administrador, pesquisador e coordenador), 404.
- **RQ-T109-13** Documentar no Swagger (`@Tag(name = "Participante (me)")`, `@Operation`) e conferir contra o YAML (remover o `@Disabled` do teste de contrato quando os endpoints existirem).
- **RQ-T109-14** Avisar **I5** de que o `AcessoParticipante` está na `main`.

## 5. Contrato, regras e códigos de erro

- Operações 13 e 14 da seção 2 das convenções; regra 7 (um para um; só os próprios dados; outro participante é 404).
- Perfis: só `PARTICIPANTE`; qualquer outro perfil recebe 403.

## 6. Testes

- Os testes completos são a T113; nesta task escreva junto com o código o teste de unidade do `AcessoParticipante` e um teste de fumaça dos endpoints.

## 7. Critérios de aceite

- [ ] O participante logado vê só os seus estudos e o detalhe de cada um; outro participante recebe 404.
- [ ] Os demais perfis recebem 403 em `/api/me/**`.
- [ ] O `AcessoParticipante` está pronto para a T116 e o contrato e os tipos estão atualizados.

## 8. Cuidados e fora de escopo

- Nunca receber o id do participante por parâmetro nem aceitá-lo: ele vem sempre do token.
- A retirada pelo participante (RF20) e a confirmação de visitas (RF19) são da Sprint 4.

## 9. Origem dos requisitos (especificação e planejamento)

Esta seção reúne, **sem paráfrase**, o que a especificação do projeto e o planejamento da Sprint 3 exigem para esta task, para que o documento sozinho mostre de onde vem cada requisito da seção 4. Onde o texto original difere das convenções (ajustes 1 a 10, [seção 1](00-convencoes-e-contrato-compartilhado.md#1-ajustes-em-relação-ao-planejamento-e-por-quê)), vale o que está neste documento e nas convenções.

### 9.1 Requisito funcional da especificação

- **RF17** (Módulo E — Aplicativo do participante; interface Mobile): “Permitir ao participante consultar os estudos em que está incluído.”
- **RF02** (Módulo A — Usuários e autenticação; interface Web/Mobile): “Permitir autenticação e encerramento de sessão.”
- **RF03** (Módulo A — Usuários e autenticação; interface Web/Mobile): “Restringir funcionalidades conforme o perfil do usuário.”
- Escopo do planejamento para este PBI: “RF17 e RF18 (Módulo E, interface Mobile), mais o login do participante no app (RF02 e RF03 no mobile)”.

### 9.2 Critério de aceite do PBI (planejamento)

> O participante entra no app com a conta vinculada pelo coordenador, vê a lista dos estudos em que está incluído e o detalhe de cada um, e sai do app; outros perfis são recusados com mensagem clara; o participante nunca vê dados de outro; existe pelo menos um fluxo E2E no app. Ao fechar, em 21/11, este PBI deixa na main o esqueleto do app, os endpoints /me e o vínculo da conta que o PBI-25 usa.

### 9.3 Regras de negócio do planejamento que afetam esta task

| # | Regra proposta | Tasks afetadas (no planejamento) |
|---|---|---|
| 7 | A conta do participante é um usuário com perfil participante, criado pelo administrador (RF01) e vinculado pelo coordenador a um candidato já incluído, na relação um para um (o campo opcional previsto na regra 8 da Sprint 2). O participante só enxerga os próprios dados: acessar dados de outro retorna 404 | T109, T111, T116 |
| 10 | Erros seguem o formato único da Sprint 2 (400 validação, 403 perfil sem permissão, 404 inexistente, 409 regra de negócio, sempre com o código da regra). As migrations usam faixas de versão por PBI (V3_19_* a V3_25_*); antes de começar, confirmar que a maior versão da Sprint 2 é menor que V3_19, senão trocar o prefixo V3 pelo número seguinte ao da última migration | T74 e todas as de backend |

### 9.4 Texto original da task no planejamento

> **T109 · Acesso do participante e endpoints /me no backend.** Primeiro publique no contrato os endpoints sob /me, que identificam o participante pelo token e não recebem id. Depois implemente um componente que resolve, a partir do usuário logado, as participações vinculadas à conta (regra 7) e a base dos controladores /me, restritos ao perfil participante (403 para os demais). Em seguida implemente GET /me/studies, com título, descrição resumida, estado do estudo, data de inclusão e situação da participação (ativo ou retirado), e o detalhe de um estudo, que devolve 404 se o estudo não for do participante. Por fim, documente no Swagger e avise I5: a T116 usa este componente para as consultas de visitas.

### 9.5 Requisitos da especificação que atravessam esta task

- Regra 9.2 (tecnologia): “O backend deve disponibilizar uma **API HTTP REST** compartilhada pela web e pelo mobile, documentada no padrão **OpenAPI** (Swagger UI pode ser usado para consulta e testes).”
- Entregas: “Tratamento de erros e validações das regras de negócio.”
- **Definition of Done** da Sprint (texto completo na [seção 11 das convenções](00-convencoes-e-contrato-compartilhado.md#11-definition-of-done-e-requisitos-transversais-da-especificação)): os critérios de aceite estão atendidos e demonstráveis com dados fictícios; o código foi revisado, passou no CI e entrou na `main` por Pull Request; há testes automatizados e, no backend, a cobertura do código de negócio permanece em 80% ou mais; erros e validações são tratados com mensagens claras; Swagger e contrato YAML refletem os endpoints; os dados de demonstração estão no perfil `demo`; documentação e capturas estão no repositório; e RF01 a RF11 continuam funcionando.


---
Parte do conjunto de documentos de design da Sprint 3: [índice](README.md) · [convenções e contrato](00-convencoes-e-contrato-compartilhado.md).
