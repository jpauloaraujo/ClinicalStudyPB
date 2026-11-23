# T121 · Capturas, fluxo E2E mobile das visitas e documentação

> **Sprint 3** · PBI-25 · RF18 App: próximas visitas e histórico de comparecimento  
> **Responsável:** I5 · **Estimativa:** 2h · **Prazo:** Seg 23/11  
> **Depende de:** T114, T119  
> **Situação:** ⬜ A implementar (este documento é o guia para quem for fazer)

> Antes de começar, leia as [convenções e o contrato compartilhado da Sprint 3](00-convencoes-e-contrato-compartilhado.md): nomes, endpoints, DTOs, códigos de erro, tabelas e dados de demonstração valem para esta task. A origem dos requisitos (especificação, critério de aceite do PBI, regras de negócio e o texto original da task) está na [seção 9](#9-origem-dos-requisitos-especificação-e-planejamento).

## 1. Objetivo

Acrescentar ao fluxo E2E mobile da T114 os passos das próximas visitas e do histórico, salvar as capturas das duas telas e do estado de erro, atualizar o README com o RF18 e zerar o Remaining Work, fechando a Sprint para a Review.

## 2. Contexto: o que já existe e deve ser reaproveitado

- O fluxo base `mobile/ClinicalStudyPB/.maestro/fluxo-participante.yaml` e a evidência (`docs/sprint3/e2e-mobile-evidencia.md`) são da T114: login, Meus estudos, detalhe e logout, com o participante de demonstração.
- As telas de visitas (T117 e T118) usam identificadores acessíveis (`accessibilityLabel`/`testID`) que o Maestro usa; o participante de demonstração `participante@clinicalstudy.test` (P1) tem uma visita agendada e uma realizada.
- O estado de erro pode ser provocado derrubando o backend (ou com `EXPO_PUBLIC_MOCK_ERRO` no modo de mocks, só para a captura).
- Esta task é do dia da Sprint Review (23/11): o rascunho da Review já deve estar pronto, e aqui só entra o ajuste final.

## 3. Onde implementar

| Arquivo | Ação | Responsabilidade |
|---|---|---|
| `mobile/ClinicalStudyPB/.maestro/fluxo-participante.yaml` | Alterar | Passos das próximas visitas e do histórico |
| `mobile/ClinicalStudyPB/.maestro/fluxo-visitas.yaml` | Criar | (Opcional) fluxo separado só das visitas |
| `docs/imagens/sprint3/` | Alterar | Capturas das duas telas e do estado de erro |
| `docs/sprint3/e2e-mobile-evidencia.md` | Alterar | Evidência do fluxo completo |
| `README.md` | Alterar | RF18 com as capturas |

## 4. Requisitos a implementar

Cada requisito tem um identificador (`RQ-T121-NN`) para a revisão do Pull Request e para a conferência no fim da task: todos precisam estar atendidos.

### Fluxo E2E mobile das visitas

- **RQ-T121-01** Acrescentar ao fluxo da T114, **depois do detalhe do estudo**, os passos: abrir a aba **Visitas**, confirmar a lista das **próximas visitas** (nome da visita e data em português), abrir o **detalhe** da visita, voltar, abrir o **Histórico** e confirmar a visita realizada com o selo e o **resumo de comparecimento**, e então sair.
- **RQ-T121-02** Usar identificadores acessíveis em vez de coordenadas; o fluxo continua **repetível** (`launchApp: clearState`) e só depende do perfil `demo`.
- **RQ-T121-03** Rodar o fluxo completo **localmente em emulador Android** (ou celular físico), com o backend no perfil `demo`, e atualizar a evidência (gravação ou capturas passo a passo) e o comando em `docs/sprint3/e2e-mobile-evidencia.md`.

### Capturas

- **RQ-T121-04** Salvar as capturas das **próximas visitas** (`rf18-proximas-visitas`), do **histórico de comparecimento** com o resumo (`rf18-historico-comparecimento`), do **detalhe da visita** e do **estado de erro** com Tentar de novo (`rf18-erro-conexao`) em `docs/imagens/sprint3/`; incluir o participante retirado no histórico (motivo "retirada do estudo"), se couber.

### Documentação e fechamento

- **RQ-T121-05** Atualizar o `README.md` com o **RF18**: comportamento (próximas visitas da mais próxima para a mais distante; histórico da mais recente para a mais antiga; resumo de comparecimento sem as canceladas), regras (o participante só vê os próprios dados), erros e as capturas.
- **RQ-T121-06** Atualizar a contagem de testes e a lista de requisitos do README para o fim da Sprint (RF12 a RF18) e conferir que a suíte inteira (backend, web, E2E web, app) continua passando.
- **RQ-T121-07** Zerar o **Remaining Work** das tasks do PBI-25 no Azure Boards, registrar os Bugs encontrados e conferir que o PBI está 100% na `main`; **I1 revisa os Pull Requests deste PBI**.
- **RQ-T121-08** Preparar o ajuste final da **Sprint Review e da Retrospectiva** de 23/11: demonstração do incremento (acompanhamento na web e app do participante), burndown final e o rascunho de processo (T100).

## 6. Testes

- O fluxo Maestro completo (login, estudos, detalhe, próximas visitas, histórico e logout), localmente com evidência.
- Suíte de backend, web, E2E web e app sem regressão.

## 7. Critérios de aceite

- [ ] O fluxo E2E mobile cobre as próximas visitas e o histórico e passa em emulador, com a evidência atualizada.
- [ ] As capturas das duas telas e do estado de erro estão no repositório e o README descreve o RF18.
- [ ] O PBI-25 está 100% na `main`, com o Remaining Work zerado, pronto para a Review.

## 8. Cuidados e fora de escopo

- Não depender de datas fixas: o participante de demonstração tem datas relativas ao dia da carga.
- Última task da Sprint: só ajustes finais, sem funcionalidade nova.

## 9. Origem dos requisitos (especificação e planejamento)

Esta seção reúne, **sem paráfrase**, o que a especificação do projeto e o planejamento da Sprint 3 exigem para esta task, para que o documento sozinho mostre de onde vem cada requisito da seção 4. Onde o texto original difere das convenções (ajustes 1 a 10, [seção 1](00-convencoes-e-contrato-compartilhado.md#1-ajustes-em-relação-ao-planejamento-e-por-quê)), vale o que está neste documento e nas convenções.

### 9.1 Requisito funcional da especificação

- **RF18** (Módulo E — Aplicativo do participante; interface Mobile): “Exibir as próximas visitas e seu histórico de comparecimento.”

### 9.2 Critério de aceite do PBI (planejamento)

> O participante logado no app vê a lista das suas próximas visitas agendadas (data, hora, estudo, nome da visita e atividades previstas) e o histórico de visitas realizadas, ausentes e canceladas; nunca vê dados de outro participante; as telas tratam carregamento, erro e lista vazia.

### 9.3 Regras de negócio do planejamento que afetam esta task

Nenhuma das dez regras numeradas cita esta task diretamente; valem as regras gerais da Sprint (formato único de erro, Definition of Done).

### 9.4 Texto original da task no planejamento

> **T121 · Capturas, fluxo E2E mobile das visitas e documentação.** Primeiro acrescente ao fluxo E2E mobile da T114 o passo de abrir as próximas visitas e o histórico com o participante de demonstração. Depois salve capturas das duas telas e do estado de erro. Por fim, atualize o README com o RF18 e zere o Remaining Work no Azure Boards.

### 9.5 Requisitos da especificação que atravessam esta task

- Estratégia de testes: “Pelo menos um fluxo E2E automatizado na web e um no mobile.” (exemplo da especificação: “Cadastrar estudo, incluir participante e agendar visita”).
- README principal, seção 3: “**Funcionalidades desenvolvidas** — requisitos por código RF, comportamento e capturas web e mobile, incluindo casos de sucesso e de erro/validação.”
- Entregas: “Os requisitos de releases anteriores devem continuar funcionando.” (RF01 a RF11 continuam funcionando).
- Para o mobile “basta uma aplicação executável em emulador ou dispositivo; não é necessário publicar nas lojas”.
- **Definition of Done** da Sprint (texto completo na [seção 11 das convenções](00-convencoes-e-contrato-compartilhado.md#11-definition-of-done-e-requisitos-transversais-da-especificação)): os critérios de aceite estão atendidos e demonstráveis com dados fictícios; o código foi revisado, passou no CI e entrou na `main` por Pull Request; há testes automatizados e, no backend, a cobertura do código de negócio permanece em 80% ou mais; erros e validações são tratados com mensagens claras; Swagger e contrato YAML refletem os endpoints; os dados de demonstração estão no perfil `demo`; documentação e capturas estão no repositório; e RF01 a RF11 continuam funcionando.


---
Parte do conjunto de documentos de design da Sprint 3: [índice](README.md) · [convenções e contrato](00-convencoes-e-contrato-compartilhado.md).
