# T113 · Testes de backend e de componente do app

> **Sprint 3** · PBI-24 · RF17 App do participante: login, vínculo da conta e meus estudos (RF02 e RF03 no mobile)  
> **Responsável:** I7 · **Estimativa:** 3h · **Prazo:** Sáb 21/11  
> **Depende de:** T109, T110, T111, T112  
> **Situação:** ⬜ A implementar (este documento é o guia para quem for fazer)

> Antes de começar, leia as [convenções e o contrato compartilhado da Sprint 3](00-convencoes-e-contrato-compartilhado.md): nomes, endpoints, DTOs, códigos de erro, tabelas e dados de demonstração valem para esta task.

## 1. Objetivo

Provar, no backend, que os endpoints `/me` só mostram as participações da conta logada e as regras do vínculo, e, no app, o login, a recusa de outros perfis e as telas de estudos, mantendo a cobertura da fatia acima de 80%.

## 2. Contexto: o que já existe e deve ser reaproveitado

- Backend: modelos `ParticipacaoEndpointTest` e `ConsentimentoEndpointTest` (tokens reais), `CenarioRecrutamento` e `TokenDeTeste` (que já têm `PARTICIPANTE`, `ADMIN`, `COORDENADOR` e `PESQUISADOR`).
- Mobile: Jest e Testing Library configurados na T108 (preset `jest-expo`, alias `@/`), com o cliente HTTP simulado (`jest.mock('@/api/cliente')` ou `fetch` simulado) e os mocks de `src/api/mocks/`.
- Para os testes de endpoint é preciso uma conta de participante **vinculada** a um participante incluído: criar pelo fluxo (usuário `PARTICIPANTE` criado pelo administrador via API, vínculo pelo coordenador via `PUT .../conta`) em um método de apoio de `CenarioRecrutamento`.

## 3. Onde implementar

| Arquivo | Ação | Responsabilidade |
|---|---|---|
| `backend/ClinicalStudyPB/src/test/java/UEPB/ESWII/ClinicalStudyPB/support/CenarioRecrutamento.java` | Alterar | `contaParticipante(...)` e `vincularConta(...)` |
| `backend/ClinicalStudyPB/src/test/java/UEPB/ESWII/ClinicalStudyPB/AcessoParticipanteTest.java` | Criar | Resolução das participações (Mockito) |
| `backend/ClinicalStudyPB/src/test/java/UEPB/ESWII/ClinicalStudyPB/MeEstudosEndpointTest.java` | Criar | `/api/me/estudos` com tokens reais |
| `backend/ClinicalStudyPB/src/test/java/UEPB/ESWII/ClinicalStudyPB/VinculoContaServiceTest.java` | Criar | Regras do vínculo isoladas |
| `backend/ClinicalStudyPB/src/test/java/UEPB/ESWII/ClinicalStudyPB/VinculoContaEndpointTest.java` | Criar | 200, 403, 404 e 409 com tokens reais |
| `backend/ClinicalStudyPB/src/test/java/UEPB/ESWII/ClinicalStudyPB/VinculoContaConcorrenciaTest.java` | Criar | Mesma conta em dois participantes ao mesmo tempo |
| `mobile/ClinicalStudyPB/src/app/__tests__/login.test.tsx` | Alterar | Complementa os da T110 |
| `mobile/ClinicalStudyPB/src/app/__tests__/estudos.test.tsx` | Criar | Lista e detalhe |
| `mobile/ClinicalStudyPB/src/api/__tests__/cliente.test.ts` | Alterar | Erros, 401 e conexão |

## 4. Requisitos a implementar

Cada requisito tem um identificador (`RQ-T113-NN`) para a revisão do Pull Request e para a conferência no fim da task: todos precisam estar atendidos.

### Backend: `/me/estudos`

- **RQ-T113-01** **Só as participações da conta logada**: com dois participantes vinculados (A e B), A vê só os estudos de A e B só os de B (a lista de A nunca contém o estudo de B).
- **RQ-T113-02** Participante com **dois estudos** vê os dois, ordenados pela data de inclusão decrescente; participante **sem vínculo** recebe lista vazia (200); participante **retirado** continua vendo o estudo com `situacaoParticipacao = RETIRADO` e `retirada` no detalhe, **sem o motivo**.
- **RQ-T113-03** `GET /api/me/estudos/{estudoId}`: 200 para o próprio estudo; **404** para um estudo que existe mas é de outro participante e para estudo inexistente (a resposta é a mesma, para não revelar a existência).
- **RQ-T113-04** **403** para administrador, pesquisador e coordenador em `/api/me/**`; **401** sem token.
- **RQ-T113-05** `resumo` cortado em 160 caracteres; nenhum campo de candidato (código, nome fictício) na resposta.

### Backend: vínculo da conta

- **RQ-T113-06** Vínculo válido: 200 com a conta vinculada; depois dele, o participante passa a ver o estudo em `/api/me/estudos` e `GET .../conta` devolve a conta.
- **RQ-T113-07** Regras: conta de perfil errado (`CONTA_NAO_E_PARTICIPANTE`), conta desativada (`CONTA_INATIVA`), participação que já tem conta (`PARTICIPACAO_JA_TEM_CONTA`), conta já vinculada a outro participante (`CONTA_JA_VINCULADA`) e participante retirado (`PARTICIPANTE_RETIRADO`); usuário inexistente e participação inexistente: 404; a ordem das verificações respeitada.
- **RQ-T113-08** Permissões: só o coordenador vincula (403 para pesquisador, administrador e participante) e lista contas disponíveis; `GET .../conta` para pesquisador e coordenador; 401 sem token.
- **RQ-T113-09** `GET /api/contas-participante/disponiveis` lista só contas ativas, de perfil participante e não vinculadas, e não traz senha.
- **RQ-T113-10** **Concorrência** (sem `@Transactional`, com limpeza no `@AfterEach`): a mesma conta vinculada a dois participantes ao mesmo tempo resulta em exatamente um 200 e um 409 `CONTA_JA_VINCULADA`, sem 500.
- **RQ-T113-11** Rodar tudo também no job `Backend (PostgreSQL)` e conferir a cobertura de `AcessoParticipante`, `MeusEstudosService` e `VinculoContaService` (meta 95%).

### App: componentes e cliente

- **RQ-T113-12** **Login:** recusado para administrador, pesquisador e coordenador (mensagem "exclusivo para participantes", token removido), credencial inválida, campos obrigatórios e falha de rede.
- **RQ-T113-13** **Lista de estudos:** com dados (cartões com título, status e data), vazia (mensagem), com erro (botão Tentar de novo chama a API de novo) e carregamento; pull-to-refresh.
- **RQ-T113-14** **Detalhe:** com dados, participante retirado (marca) e 404 ("Estudo não encontrado.").
- **RQ-T113-15** **Cliente:** erro da Sprint 2 convertido em `ErroApi`, falha de rede e 401 com o callback de sessão expirada.
- **RQ-T113-16** `npm run lint`, `npm run typecheck` e `npm test` verdes localmente e no CI; registrar a cobertura de testes do app se o Jest a reportar.

## 6. Testes

- As classes da seção 3 no backend e os arquivos de teste do app; o `ContratoOpenApiSprint3Test` verde (17 operações).
- Toda a suíte da Sprint 2 e dos PBIs anteriores continua passando.

## 7. Critérios de aceite

- [ ] `/api/me/**` só mostra as participações da conta logada e responde 403 e 404 como definido.
- [ ] As regras do vínculo e a concorrência estão provadas.
- [ ] O login e as telas de estudos do app têm testes de componente passando no CI.
- [ ] A cobertura de `domain` e `service` da fatia está acima de 80%.

## 8. Cuidados e fora de escopo

- O teste "o participante A nunca vê o estudo do B" é o mais importante da task.
- O teste de concorrência é o único que não reverte por `@Transactional`: sempre limpar os dados.

---
Parte do conjunto de documentos de design da Sprint 3: [índice](README.md) · [convenções e contrato](00-convencoes-e-contrato-compartilhado.md).
