# T119 · Integração com a API real e tratamento de estados

> **Sprint 3** · PBI-25 · RF18 App: próximas visitas e histórico de comparecimento  
> **Responsável:** I5 · **Estimativa:** 3h · **Prazo:** Seg 23/11  
> **Depende de:** T116, T117, T118  
> **Situação:** ⬜ A implementar (este documento é o guia para quem for fazer)

> Antes de começar, leia as [convenções e o contrato compartilhado da Sprint 3](00-convencoes-e-contrato-compartilhado.md): nomes, endpoints, DTOs, códigos de erro, tabelas e dados de demonstração valem para esta task. A origem dos requisitos (especificação, critério de aceite do PBI, regras de negócio e o texto original da task) está na [seção 9](#9-origem-dos-requisitos-especificação-e-planejamento).

## 1. Objetivo

Revisar as chamadas à API real nas duas telas e tratar carregamento, erro com **Tentar de novo**, falta de conexão e 401 (volta ao login), deixando os mocks só para os testes e conferindo o fluxo no emulador com os dados de demonstração.

## 2. Contexto: o que já existe e deve ser reaproveitado

- Cliente HTTP da T108 (`ErroApi` com `status`, `codigo`, `mensagem`; falha de rede com código `SEM_CONEXAO`; 401 central que limpa a sessão e leva ao login); componentes `Carregando`, `EstadoErro`, `EstadoVazio` e o modo de mocks `EXPO_PUBLIC_USE_MOCKS`.
- As telas das T117 e T118 já consomem `visitasApi`; esta task garante que **a entrega usa a API real** e que todos os estados estão tratados.
- Dados de demonstração: `participante@clinicalstudy.test` (P1: visita realizada com resultado e visita agendada), `demo.participante@clinicalstudy.test` (P6: retirado) e participante sem estudo; veja as tasks T74, T84, T105 e T111.

## 3. Onde implementar

| Arquivo | Ação | Responsabilidade |
|---|---|---|
| `mobile/ClinicalStudyPB/src/app/(tabs)/visitas.tsx` | Alterar | Estados de carregamento, erro, vazio e conexão |
| `mobile/ClinicalStudyPB/src/api/visitas.ts` | Alterar | Sem dependência de mocks fora do modo de mocks |
| `mobile/ClinicalStudyPB/src/api/cliente.ts` | Conferir | Falta de conexão e 401 |
| `mobile/ClinicalStudyPB/src/api/mocks/` | Conferir | Usados só com `EXPO_PUBLIC_USE_MOCKS=true` e nos testes |
| `mobile/ClinicalStudyPB/src/utils/erros.ts` | Criar (se faltar) | Mensagens por tipo de erro |

## 4. Requisitos a implementar

Cada requisito tem um identificador (`RQ-T119-NN`) para a revisão do Pull Request e para a conferência no fim da task: todos precisam estar atendidos.

### API real

- **RQ-T119-01** Revisar `visitasApi.proximas` e `.historico` (e `estudosApi`): caminhos `/api/me/visitas/proximas` e `/api/me/visitas/historico`, parâmetros `estudoId`, `pagina` e `tamanho`, tipos do contrato (sem `any`); **sem nenhum dado fixo no código das telas**.
- **RQ-T119-02** Confirmar que, **sem** `EXPO_PUBLIC_USE_MOCKS`, as duas telas chamam a API real do backend; com a variável ligada, usam os mocks (só para desenvolvimento e testes de componente).

### Estados de erro e de carregamento

- **RQ-T119-03** **Carregamento:** indicador com texto acessível na primeira carga e indicador discreto no pull-to-refresh e na paginação.
- **RQ-T119-04** **Erro do servidor** (4xx e 5xx): mensagem do servidor (`mensagem`) ou genérica, com o botão **Tentar de novo** que repete a chamada; nunca JSON cru.
- **RQ-T119-05** **Falta de conexão**: texto "Não foi possível conectar. Verifique sua conexão e tente de novo." com **Tentar de novo**; se já havia dados carregados, **manter a lista** e mostrar o aviso em vez de limpá-la.
- **RQ-T119-06** **401**: o cliente limpa a sessão e leva ao login com a mensagem "Sua sessão expirou. Entre novamente." (T108/T110); **403** em `/me` (perfil errado) leva ao login com a mensagem do app exclusivo para participantes.
- **RQ-T119-07** **Lista vazia** (sem erro) mostra o texto de estado vazio de cada tela; o participante retirado e o sem estudo não geram erro.

### Conferência no emulador

- **RQ-T119-08** Rodar o app em **emulador Android** (ou celular) contra o backend no perfil `demo` e conferir: `participante@clinicalstudy.test` vê a visita agendada nas próximas e a realizada no histórico; `demo.participante@clinicalstudy.test` vê as próximas vazias e a visita cancelada com "retirada do estudo" no histórico; o participante sem estudo vê o aviso; derrubar o backend durante o uso mostra o erro de conexão e **Tentar de novo** recupera.
- **RQ-T119-09** Registrar capturas e os defeitos encontrados como Bug no Azure Boards, ligados à task de origem.

## 5. Contrato, regras e códigos de erro

- Operações 18 e 19 da seção 2 das convenções; erros 400, 401 e 403 tratados pelo cliente.

## 6. Testes

- Testes de componente dos estados (T120): carregamento, erro com Tentar de novo, sem conexão com lista mantida, 401 e vazio.
- Roteiro manual no emulador com os três participantes de demonstração.

## 7. Critérios de aceite

- [ ] As duas telas usam a API real e tratam carregamento, erro, falta de conexão, 401 e lista vazia.
- [ ] Os mocks ficam só para os testes e o desenvolvimento.
- [ ] O fluxo foi conferido no emulador com os dados de demonstração.

## 8. Cuidados e fora de escopo

- Sem dados fixos nas telas: tudo vem da API.
- Não mostrar nunca o erro técnico ao participante.

## 9. Origem dos requisitos (especificação e planejamento)

Esta seção reúne, **sem paráfrase**, o que a especificação do projeto e o planejamento da Sprint 3 exigem para esta task, para que o documento sozinho mostre de onde vem cada requisito da seção 4. Onde o texto original difere das convenções (ajustes 1 a 10, [seção 1](00-convencoes-e-contrato-compartilhado.md#1-ajustes-em-relação-ao-planejamento-e-por-quê)), vale o que está neste documento e nas convenções.

### 9.1 Requisito funcional da especificação

- **RF18** (Módulo E — Aplicativo do participante; interface Mobile): “Exibir as próximas visitas e seu histórico de comparecimento.”

### 9.2 Critério de aceite do PBI (planejamento)

> O participante logado no app vê a lista das suas próximas visitas agendadas (data, hora, estudo, nome da visita e atividades previstas) e o histórico de visitas realizadas, ausentes e canceladas; nunca vê dados de outro participante; as telas tratam carregamento, erro e lista vazia.

### 9.3 Regras de negócio do planejamento que afetam esta task

Nenhuma das dez regras numeradas cita esta task diretamente; valem as regras gerais da Sprint (formato único de erro, Definition of Done).

### 9.4 Texto original da task no planejamento

> **T119 · Integração com a API real e tratamento de estados.** Primeiro revise as chamadas à API real nas duas telas, usando o cliente HTTP da T108. Depois trate carregamento, erro com botão Tentar de novo, falta de conexão e 401 (volta ao login). Por fim, deixe os mocks só para os testes e confira o fluxo no emulador com os dados de demonstração das tasks T74, T84, T105 e T111.

### 9.5 Requisitos da especificação que atravessam esta task

- Estratégia de testes: em componentes mobile, testar o “comportamento das telas mobile: login, listagem de visitas, confirmação”. Regra 9.4: “A interface **mobile** atende ao perfil de participante e executa em dispositivo ou emulador.”
- **Definition of Done** da Sprint (texto completo na [seção 11 das convenções](00-convencoes-e-contrato-compartilhado.md#11-definition-of-done-e-requisitos-transversais-da-especificação)): os critérios de aceite estão atendidos e demonstráveis com dados fictícios; o código foi revisado, passou no CI e entrou na `main` por Pull Request; há testes automatizados e, no backend, a cobertura do código de negócio permanece em 80% ou mais; erros e validações são tratados com mensagens claras; Swagger e contrato YAML refletem os endpoints; os dados de demonstração estão no perfil `demo`; documentação e capturas estão no repositório; e RF01 a RF11 continuam funcionando.


---
Parte do conjunto de documentos de design da Sprint 3: [índice](README.md) · [convenções e contrato](00-convencoes-e-contrato-compartilhado.md).
