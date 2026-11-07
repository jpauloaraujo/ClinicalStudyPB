# T66 · E2E do fluxo completo da Release 1

> **Sprint 2** · PBI-17 · Qualidade, integração e dados de demonstração  
> **Responsável:** I7 · **Estimativa:** 4h · **Prazo:** Sex 06/11  
> **Depende de:** T61, T63, T65  
> **Situação:** ✅ Implementada (este documento descreve o que existe, para manter a coerência)

## 1. Objetivo

Provar por testes de ponta a ponta o fluxo da Release 1: o pesquisador planeja e abre o recrutamento, o coordenador cadastra, avalia, registra o consentimento e inclui, e a segunda inclusão com vagas esgotadas é bloqueada.

## 2. Contexto: o que já existe e deve ser reaproveitado

- Documento anterior, com os 16 requisitos e o checklist por dia: `docs/sprint2/T66 — Requisitos e critérios de aceite (E2E do fluxo completo da Release 1).md`.
- Base já pronta (T65): `playwright.config.ts`, `e2e/setup/auth.setup.ts`, `e2e/fixtures/index.ts` (sessão por perfil), `e2e/pages/LoginPage.ts`.
- Depende das telas da T61 (inclusão e Participantes), dos dados da T63 (perfil `demo`) e do ambiente sem mocks (T67).

## 3. Onde implementar

| Arquivo | Ação | Responsabilidade |
|---|---|---|
| `web/ClinicalStudyPB/e2e/support/api.ts` | Existente (criado na implementação) | Helpers que preparam estado pela API com a sessão da T65: criar estudo, critérios, visita, avançar estado, cadastrar candidato, avaliar, registrar consentimento |
| `web/ClinicalStudyPB/e2e/setup/ambiente.setup.ts` | Existente (criado na implementação) | Verificação do ambiente antes dos testes (backend e web no ar; mocks desligados), junto do setup de sessões da T65 |
| `web/ClinicalStudyPB/e2e/pages/EstudosPage.ts, EstudoDetalhePage.ts, CriteriosPage.ts, VisitasPage.ts` | Existente (criado na implementação) | Lista e cadastro de estudos, detalhe e estado, critérios, visitas |
| `web/ClinicalStudyPB/e2e/pages/CandidatosPage.ts, FichaCandidatoPage.ts, ParticipantesPage.ts` | Existente (criado na implementação) | Lista e cadastro, ficha (avaliação, consentimento, inclusão) e aba Participantes |
| `web/ClinicalStudyPB/e2e/pages/UsuariosPage.ts` | Existente (criado na implementação) | Cadastro de usuário (regressão RF01) |
| `web/ClinicalStudyPB/e2e/tests/fluxo-completo.spec.ts` | Existente (criado na implementação) | Cenário feliz em dois contextos (pesquisador e coordenador), com `test.step` |
| `web/ClinicalStudyPB/e2e/tests/vagas-esgotadas.spec.ts` | Existente (criado na implementação) | Estudo de limite 1: a segunda inclusão é bloqueada |
| `web/ClinicalStudyPB/e2e/tests/regressao-sprint1.spec.ts` | Existente (criado na implementação) | Regressão: cadastro de usuário (RF01) e de estudo, com validação (RF04) |
| `web/ClinicalStudyPB/e2e/tests/regras-recrutamento.spec.ts, acesso-por-perfil.spec.ts` | Existente (criado na implementação) | RQ-12 e RQ-13: erros de regra e acesso por perfil |
| `.github/workflows/ci.yml` | Existente (ampliado na implementação) | Perfil `demo` carregado, `timeout-minutes` ajustado, artefato com as capturas |

## 4. O que fazer em cada arquivo

### Ambiente (RQ-01)

- Sem mocks (`VITE_USE_MOCKS` desligado em `.env.e2e` e no CI); backend e PostgreSQL reais com o perfil `demo`; uma verificação antes dos testes confirma que o ambiente respondeu.

### Dados únicos por execução (RQ-02, RQ-03)

- Estudo com sufixo único de data e hora no título e candidatos com nomes fictícios únicos; a suíte roda duas vezes seguidas sem reiniciar o ambiente.

### Cenário feliz (RQ-05 a RQ-07), `fluxo-completo.spec.ts`

- Pesquisador: entra, cria o estudo (limite maior que 1), define um critério de inclusão, um de exclusão e uma visita com atividade, avança para recrutamento com a confirmação; as abas ficam somente leitura.
- Coordenador (segundo contexto): abre o estudo, cadastra um candidato (o código aparece), avalia respondendo a todos os critérios (resultado elegível), registra o consentimento (comprovante) e confere as quatro pré-condições.
- Inclui o candidato confirmando o diálogo; o participante aparece em Participantes, a situação vira incluído e o contador passa de "0 de N" para "1 de N".

### Erro: vagas esgotadas (RQ-08), `vagas-esgotadas.spec.ts`

- Estudo de limite 1 em recrutamento, preparado pelos helpers; inclui o primeiro e confere "1 de 1"; o segundo candidato elegível e consentido não pode ser incluído e a mensagem de vagas esgotadas aparece.

### Regressão (RQ-09, RQ-10)

- Administrador cadastra usuário com nome e e-mail únicos e o vê na listagem; pesquisador cadastra estudo com todos os campos e o vê na lista e no detalhe; campo obrigatório vazio mostra o erro.

### Estabilidade (RQ-11) e CI (RQ-14)

- `test.step` por etapa; nada de `waitForTimeout`; passa com `--repeat-each=5` e com dois números de workers; no CI o job fica verde na main e vermelho quando um teste quebra.

### Evidências (RQ-15, RQ-16)

- Capturas nomeadas dos passos-chave (recrutamento, elegível, comprovante, "1 de N", vagas esgotadas) enviadas como artefato; atualizar a seção "Testes E2E" do README; defeitos viram Bug no Azure Boards ligado à task de origem.

### Ordem sugerida

- Quarta 04/11: ambiente, helpers, page objects de estudos/critérios/visitas e a regressão. Quinta 05/11: page objects de candidatos, avaliação e consentimento e o cenário até as pré-condições. Sexta 06/11: inclusão, vagas esgotadas, capturas, CI e documentação.

### Situação da entrega (07/11/2026)

- Implementado e passando, com todas as telas reais: helpers de API (`support/api.ts`), dados únicos, capturas, 8 page objects, verificação do ambiente, regressão de RF01 e RF04, fluxo completo (planejamento, candidato, avaliação, consentimento, inclusão, participante na lista e contador "1 de N"), vagas esgotadas, regras de recrutamento e controle de acesso por perfil.
- Resultado local contra o backend real: 29 testes passam, nenhum ignorado; 137 passam com `--repeat-each=5` e 4 trabalhadores; 83 passam com `--repeat-each=3` e 1 trabalhador.
- A chave `e2e/support/pendencias.ts` ficou com os dois valores em `false` (telas T58 e T61 entregues). Ela permanece no código apenas como mecanismo de desligar testes dependentes de tela futura.

## 6. Testes

- `npm run test:e2e` localmente e no job `e2e`; rodar duas vezes seguidas e com `--repeat-each=5`.

## 7. Critérios de aceite

- [ ] O cenário feliz e o de vagas esgotadas passam no CI.
- [ ] Regressão de RF01 e RF04 passa.
- [ ] Capturas e documentação entregues.

## 8. Cuidados e fora de escopo

- Não use `PUT` para mudar o estado nos helpers: use `POST /api/estudos/{id}/transicoes`.
- RQ-12 e RQ-13 são recomendações; se faltar tempo, registre como pendência da Release 2.

---
Parte do conjunto de documentos de design da Sprint 2: [índice](README.md).
