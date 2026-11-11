# T111 · Vínculo da conta do participante (backend e web)

> **Sprint 3** · PBI-24 · RF17 App do participante: login, vínculo da conta e meus estudos (RF02 e RF03 no mobile)  
> **Responsável:** I7 · **Estimativa:** 3h · **Prazo:** Sáb 21/11  
> **Depende de:** T109, T97 (ficha do PBI-22)  
> **Situação:** ⬜ A implementar (este documento é o guia para quem for fazer)

> Antes de começar, leia as [convenções e o contrato compartilhado da Sprint 3](00-convencoes-e-contrato-compartilhado.md): nomes, endpoints, DTOs, códigos de erro, tabelas e dados de demonstração valem para esta task.

## 1. Objetivo

Permitir que o coordenador vincule uma conta de perfil participante a um participante incluído (relação um para um), com o endpoint, a listagem das contas ainda não vinculadas, a ação na ficha do participante (ponto de encaixe do PBI-22) e o vínculo de demonstração para o PBI-25 e o E2E mobile.

## 2. Contexto: o que já existe e deve ser reaproveitado

- O vínculo grava o `Usuario` em `Candidato.usuario` (`candidatos.usuario_id`, opcional desde a Sprint 2). A conta é criada pelo **administrador** (RF01); o coordenador só **vincula**.
- O um-para-um exige uma garantia no banco: hoje `candidatos.usuario_id` não tem `UNIQUE`. Esta task usa a migration `V6_24_1__unique_candidate_account.sql`.
- O acesso do participante (`AcessoParticipante`, T109) lê esse vínculo; a ficha do participante e a área de ações (`AreaAcoesParticipante`) são da T97; a situação `RETIRADO` é da T101.
- `/api/usuarios` é do administrador: o coordenador não pode listar usuários por lá, então a lista de contas disponíveis tem endpoint próprio.
- Demo: `participante@clinicalstudy.test` (seed V2) e `demo.participante@clinicalstudy.test` (T63) são contas de perfil participante ainda sem vínculo; P1 e P6 do estudo "Demo - Acompanhamento de participantes" (seção 7 das convenções).

## 3. Onde implementar

| Arquivo | Ação | Responsabilidade |
|---|---|---|
| `backend/ClinicalStudyPB/src/main/resources/db/migration/V6_24_1__unique_candidate_account.sql` | Criar | `UNIQUE` de `candidatos.usuario_id` |
| `docs/api/openapi-sprint3.yaml` | Alterar | Operações 15 a 17 e erros do vínculo |
| `backend/ClinicalStudyPB/src/main/java/UEPB/ESWII/ClinicalStudyPB/service/VinculoContaService.java` | Criar | Vincular, consultar e listar contas disponíveis |
| `backend/ClinicalStudyPB/src/main/java/UEPB/ESWII/ClinicalStudyPB/repository/UsuarioRepository.java` | Alterar | Contas de participante ativas e não vinculadas |
| `backend/ClinicalStudyPB/src/main/java/UEPB/ESWII/ClinicalStudyPB/controller/ContaParticipanteController.java` | Criar | `PUT/GET .../conta` e `GET /api/contas-participante/disponiveis` |
| `backend/ClinicalStudyPB/src/main/java/UEPB/ESWII/ClinicalStudyPB/controller/dto/` | Criar | `VincularContaRequest`, `ContaVinculadaResponse`, `ContaParticipanteResponse` |
| `backend/ClinicalStudyPB/src/test/java/UEPB/ESWII/ClinicalStudyPB/ContratoOpenApiSprint3Test.java` | Alterar | Contagem (17 operações) |
| `web/ClinicalStudyPB/src/api/contaParticipante.ts` | Criar | `contaParticipanteApi` |
| `web/ClinicalStudyPB/src/pages/estudos/detalhe/acompanhamento/DialogoVincularConta.tsx` | Criar | Lista de seleção e confirmação |
| `web/ClinicalStudyPB/src/pages/estudos/detalhe/acompanhamento/AreaAcoesParticipante.tsx` | Alterar | Botão **Vincular conta** e a conta exibida |
| `backend/ClinicalStudyPB/src/main/resources/db/demo/R__demo_24_conta_participante.sql` | Criar | Vínculos de demonstração |
| `backend/ClinicalStudyPB/src/test/java/UEPB/ESWII/ClinicalStudyPB/DemoDataIntegrationTest.java` | Alterar | Vínculos e carga dupla |

## 4. Requisitos a implementar

Cada requisito tem um identificador (`RQ-T111-NN`) para a revisão do Pull Request e para a conferência no fim da task: todos precisam estar atendidos.

### Migration e contrato

- **RQ-T111-01** `V6_24_1__unique_candidate_account.sql`: `CREATE UNIQUE INDEX IF NOT EXISTS uk_candidatos_usuario ON candidatos (usuario_id)` (vários `NULL` continuam permitidos); testar em banco vazio e populado, no H2 e no PostgreSQL; a migration falha (e deve ser corrigida) se um banco de produção tiver dois candidatos com a mesma conta, o que não deve existir.
- **RQ-T111-02** Descrever no contrato: `PUT /api/participacoes/{participacaoId}/conta` (só coordenador; corpo `VincularContaRequest { usuarioId }`; 200 `ContaVinculadaResponse`), `GET /api/participacoes/{participacaoId}/conta` (leitura; 200 ou 404 se não houver conta vinculada) e `GET /api/contas-participante/disponiveis` (só coordenador; 200 `List<ContaParticipanteResponse>`), com os erros `CONTA_NAO_E_PARTICIPANTE`, `CONTA_INATIVA`, `CONTA_JA_VINCULADA`, `PARTICIPACAO_JA_TEM_CONTA` e `PARTICIPANTE_RETIRADO` (409), 404 e 403. Atualizar a contagem (14 + 3 = 17).

### Regras do vínculo (`VinculoContaService.vincular`)

- **RQ-T111-03** Verificar nesta ordem: (1) a participação existe (404) e o usuário informado existe (404); (2) a participação está **ativa**, senão 409 `PARTICIPANTE_RETIRADO`; (3) a conta tem perfil **`PARTICIPANTE`**, senão 409 `CONTA_NAO_E_PARTICIPANTE`; (4) a conta está **ativa**, senão 409 `CONTA_INATIVA`; (5) a participação **ainda não tem conta**, senão 409 `PARTICIPACAO_JA_TEM_CONTA`; (6) a conta **não está vinculada a outro** participante, senão 409 `CONTA_JA_VINCULADA`.
- **RQ-T111-04** Gravar o vínculo em `Candidato.usuario` na mesma transação; tratar a corrida (dois vínculos da mesma conta) pelo `UNIQUE`: capturar `DataIntegrityViolationException` e traduzir em 409 `CONTA_JA_VINCULADA` (nunca 500).
- **RQ-T111-05** O vínculo é **um para um** e **não há desvincular** nesta Sprint (fora de escopo; registrar no README e na T94 se o PO pedir).
- **RQ-T111-06** `GET .../conta` devolve a conta vinculada (`participacaoId`, `usuarioId`, `nome`, `email`) ou 404 `RECURSO_NAO_ENCONTRADO`; **nunca** devolve a senha nem o hash.

### Contas disponíveis

- **RQ-T111-07** `GET /api/contas-participante/disponiveis` (só coordenador) lista os usuários **ativos**, de perfil `PARTICIPANTE`, que **não estão vinculados** a nenhum candidato, ordenados por nome, com `usuarioId`, `nome` e `email` (DTO próprio: nada de entidade `Usuario`).
- **RQ-T111-08** Administrador, pesquisador e participante recebem 403; sem token, 401.

### Web: vincular a conta na ficha

- **RQ-T111-09** Na `AreaAcoesParticipante` (ponto de encaixe da T97), o botão **Vincular conta** aparece **só para o coordenador**, só com o participante `ATIVO` e **sem conta vinculada**; abre `DialogoVincularConta` com a **lista de seleção** das contas disponíveis (nome e e-mail), estado de carregamento, lista vazia ("Não há contas de participante disponíveis. Peça ao administrador para cadastrar uma.") e erro.
- **RQ-T111-10** Ao confirmar, chamar `contaParticipanteApi.vincular`, fechar o diálogo, mostrar a confirmação e **exibir a conta vinculada** na ficha ("Conta vinculada: nome (e-mail)") com o botão escondido; o pesquisador também vê a conta vinculada, em leitura.
- **RQ-T111-11** Mensagens por código (`CONTA_JA_VINCULADA`, `CONTA_INATIVA`, `CONTA_NAO_E_PARTICIPANTE`, `PARTICIPACAO_JA_TEM_CONTA`, `PARTICIPANTE_RETIRADO`) em `mensagensAcompanhamento.ts`; código desconhecido mostra a mensagem do servidor.
- **RQ-T111-12** Testes de componente do diálogo e da área de ações.

### Dados de demonstração `R__demo_24_conta_participante.sql`

- **RQ-T111-13** Vincular `participante@clinicalstudy.test` ao **P1** (que tem visita realizada com resultado e visita agendada) e `demo.participante@clinicalstudy.test` ao **P6** (retirado, para mostrar o estado retirado no app), com `NOT EXISTS`, ids no padrão `d6300000-0000-4000-8000-0000000024NN`, testado no H2 e no PostgreSQL e idempotente.
- **RQ-T111-14** Atualizar `DemoDataIntegrationTest` (vínculos e carga dupla) e avisar I5 de que os dados para o PBI-25 e o E2E mobile estão disponíveis.

## 5. Contrato, regras e códigos de erro

- Operações 15, 16 e 17 da seção 2 das convenções; códigos `CONTA_NAO_E_PARTICIPANTE`, `CONTA_INATIVA`, `CONTA_JA_VINCULADA`, `PARTICIPACAO_JA_TEM_CONTA` e `PARTICIPANTE_RETIRADO`.
- Regra 7: um para um, conta ativa e de perfil participante, participação ativa.

## 6. Testes

- Os testes completos do vínculo são a T113; nesta task escreva junto com o código a unidade das regras e os testes do diálogo.
- `FlywayMigrationIntegrationTest` e `DemoDataIntegrationTest`.

## 7. Critérios de aceite

- [ ] O coordenador vincula uma conta de participante a um participante ativo pela ficha e vê a conta vinculada.
- [ ] Cada regra violada falha com o código certo e o vínculo é um para um, garantido no banco.
- [ ] O perfil `demo` traz as duas contas vinculadas.

## 8. Cuidados e fora de escopo

- O coordenador não cria contas nem lista usuários em geral: só vê as contas disponíveis.
- Sem desvincular nesta Sprint.

---
Parte do conjunto de documentos de design da Sprint 3: [índice](README.md) · [convenções e contrato](00-convencoes-e-contrato-compartilhado.md).
