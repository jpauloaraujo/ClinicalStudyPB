# T93 · E2E, capturas e documentação do RF14

> **Sprint 3** · PBI-21 · RF14 Resultados fictícios das avaliações previstas  
> **Responsável:** I6 · **Estimativa:** 2h · **Prazo:** Dom 15/11  
> **Depende de:** T91  
> **Situação:** ⬜ A implementar (este documento é o guia para quem for fazer)

> Antes de começar, leia as [convenções e o contrato compartilhado da Sprint 3](00-convencoes-e-contrato-compartilhado.md): nomes, endpoints, DTOs, códigos de erro, tabelas e dados de demonstração valem para esta task. A origem dos requisitos (especificação, critério de aceite do PBI, regras de negócio e o texto original da task) está na [seção 9](#9-origem-dos-requisitos-especificação-e-planejamento).

## 1. Objetivo

Provar o RF14 no navegador (registrar e editar um resultado, valor vazio e coordenador em leitura), guardar as capturas e atualizar a documentação, passando o bastão ao PBI-22.

## 2. Contexto: o que já existe e deve ser reaproveitado

- Infraestrutura E2E e specs das T79 e T86 (`ApiE2E`, `VisitaAgendadaPage` do E2E).
- O estado de partida precisa de uma visita **realizada** com atividade de avaliação: criado por `ApiE2E` (participante incluído, visita prevista com atividade `AVALIACAO_CLINICA`, agendamento em dia permitido e desfecho realizada).
- O E2E tem fixtures por perfil (`comoPerfil`); o pesquisador e o coordenador usam contextos de navegador separados.

## 3. Onde implementar

| Arquivo | Ação | Responsabilidade |
|---|---|---|
| `web/ClinicalStudyPB/e2e/support/api.ts` | Alterar | `criarAtividade`, `registrarDesfecho` e `registrarResultado` |
| `web/ClinicalStudyPB/e2e/pages/VisitaAgendadaPage.ts` | Alterar | Aba Avaliações |
| `web/ClinicalStudyPB/e2e/tests/resultados-visita.spec.ts` | Criar | Cenários de sucesso, de erro e de leitura |
| `README.md` | Alterar | RF14 com as capturas |
| `docs/imagens/sprint3/` | Alterar | Capturas finais do RF14 |

## 4. Requisitos a implementar

Cada requisito tem um identificador (`RQ-T93-NN`) para a revisão do Pull Request e para a conferência no fim da task: todos precisam estar atendidos.

### Cenários Playwright (`resultados-visita.spec.ts`)

- **RQ-T93-01** **Sucesso:** o pesquisador abre a visita realizada pela agenda, abre a aba Avaliações, registra o resultado de uma avaliação (valor e unidade), vê o selo "registrado" e o contador "1 de N"; depois **edita** o valor e confere a alteração.
- **RQ-T93-02** **Erro:** tentar salvar com valor vazio mostra o erro e nada é gravado.
- **RQ-T93-03** **Leitura:** o coordenador abre a mesma aba e vê os resultados somente leitura, sem botão Salvar e com o aviso.
- **RQ-T93-04** O estado de partida é montado por `ApiE2E`; dados únicos por execução.

### Capturas e documentação

- **RQ-T93-05** Salvar `rf14-resultado-registrado`, `rf14-resultado-erro-valor` e `rf14-resultado-leitura-coordenador`; copiar as finais para `docs/imagens/sprint3/`.
- **RQ-T93-06** Atualizar o `README.md` com o RF14.
- **RQ-T93-07** Zerar o Remaining Work das tasks do PBI-21 no Azure Boards e registrar os Bugs.
- **RQ-T93-08** Passar o bastão a I2 (PBI-22): resultados (T89 e T90) e dados de demonstração com resultados estão na `main`; I2 revisa os Pull Requests deste PBI.

## 6. Testes

- `resultados-visita.spec.ts` no CI; E2E dos PBIs anteriores e da Release 1 sem regressão.

## 7. Critérios de aceite

- [ ] Registrar, editar, o erro e a leitura do coordenador estão provados no navegador.
- [ ] Capturas e README atualizados; o PBI-21 está 100% na `main`.

## 8. Cuidados e fora de escopo

- Não montar a visita realizada pela interface (use `ApiE2E`).
- O fim de semana não muda o roteiro: a data usada é sempre relativa a hoje.

## 9. Origem dos requisitos (especificação e planejamento)

Esta seção reúne, **sem paráfrase**, o que a especificação do projeto e o planejamento da Sprint 3 exigem para esta task, para que o documento sozinho mostre de onde vem cada requisito da seção 4. Onde o texto original difere das convenções (ajustes 1 a 10, [seção 1](00-convencoes-e-contrato-compartilhado.md#1-ajustes-em-relação-ao-planejamento-e-por-quê)), vale o que está neste documento e nas convenções.

### 9.1 Requisito funcional da especificação

- **RF14** (Módulo D — Acompanhamento; interface Web): “Registrar resultados fictícios das avaliações previstas.”

### 9.2 Critério de aceite do PBI (planejamento)

> O pesquisador registra e edita o resultado de cada atividade da categoria avaliação de uma visita REALIZADA; visitas não realizadas, atividades de outras categorias e estudos encerrados são recusados; o coordenador consulta os resultados em modo somente leitura.

### 9.3 Regras de negócio do planejamento que afetam esta task

Nenhuma das dez regras numeradas cita esta task diretamente; valem as regras gerais da Sprint (formato único de erro, Definition of Done).

### 9.4 Texto original da task no planejamento

> **T93 · E2E, capturas e documentação do RF14.** Primeiro escreva o cenário Playwright em que o pesquisador registra o resultado de uma avaliação e depois o edita, o cenário de erro do valor vazio e a verificação de que o coordenador vê só leitura. Depois salve capturas nomeadas de sucesso e de erro. Por fim, atualize o README com o RF14 e zere o Remaining Work no Azure Boards.

### 9.5 Requisitos da especificação que atravessam esta task

- Estratégia de testes: “Pelo menos um fluxo E2E automatizado na web e um no mobile.” (exemplo da especificação: “Cadastrar estudo, incluir participante e agendar visita”).
- README principal, seção 3: “**Funcionalidades desenvolvidas** — requisitos por código RF, comportamento e capturas web e mobile, incluindo casos de sucesso e de erro/validação.”
- Entregas: “Os requisitos de releases anteriores devem continuar funcionando.” (RF01 a RF11 continuam funcionando).
- **Definition of Done** da Sprint (texto completo na [seção 11 das convenções](00-convencoes-e-contrato-compartilhado.md#11-definition-of-done-e-requisitos-transversais-da-especificação)): os critérios de aceite estão atendidos e demonstráveis com dados fictícios; o código foi revisado, passou no CI e entrou na `main` por Pull Request; há testes automatizados e, no backend, a cobertura do código de negócio permanece em 80% ou mais; erros e validações são tratados com mensagens claras; Swagger e contrato YAML refletem os endpoints; os dados de demonstração estão no perfil `demo`; documentação e capturas estão no repositório; e RF01 a RF11 continuam funcionando.


---
Parte do conjunto de documentos de design da Sprint 3: [índice](README.md) · [convenções e contrato](00-convencoes-e-contrato-compartilhado.md).
