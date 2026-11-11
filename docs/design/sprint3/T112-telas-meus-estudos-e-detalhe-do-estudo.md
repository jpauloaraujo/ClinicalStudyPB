# T112 · Telas Meus estudos e detalhe do estudo

> **Sprint 3** · PBI-24 · RF17 App do participante: login, vínculo da conta e meus estudos (RF02 e RF03 no mobile)  
> **Responsável:** I7 · **Estimativa:** 3h · **Prazo:** Sáb 21/11  
> **Depende de:** T108, T109  
> **Situação:** ⬜ A implementar (este documento é o guia para quem for fazer)

> Antes de começar, leia as [convenções e o contrato compartilhado da Sprint 3](00-convencoes-e-contrato-compartilhado.md): nomes, endpoints, DTOs, códigos de erro, tabelas e dados de demonstração valem para esta task. A origem dos requisitos (especificação, critério de aceite do PBI, regras de negócio e o texto original da task) está na [seção 9](#9-origem-dos-requisitos-especificação-e-planejamento).

## 1. Objetivo

Substituir o conteúdo provisório da aba Estudos pela lista de cartões dos estudos do participante e criar o detalhe do estudo, tratando carregamento, erro, lista vazia e rolagem para atualizar, sobre o esqueleto da T108 e a API da T109.

## 2. Contexto: o que já existe e deve ser reaproveitado

- A aba `src/app/(tabs)/estudos.tsx` hoje mostra `EmBreve` ("M03 Meus estudos — conteúdo depende dos endpoints do participante (RF17)"). O protótipo (`docs/sprint1/prototipos-mobile.md`) define **M03 Meus estudos** (cartões: título, status do estudo, data de inclusão; estado vazio "Você ainda não está incluído em nenhum estudo") e **M04 Detalhe do estudo** (título, objetivo, responsável, período, status da participação, lista de visitas previstas, botões Ver visitas e Solicitar retirada).
- API: `GET /api/me/estudos` e `GET /api/me/estudos/{estudoId}` (T109, tipos em `src/api/schema.d.ts`); `estudosApi.listar` já foi criada na T110.
- Componentes base da T108 (`Carregando`, `EstadoErro`, `EstadoVazio`, `Selo`, `Cartao`, `Tela`); a tela do detalhe é declarada no `_layout.tsx` da T108.
- **Fora de escopo:** "Solicitar retirada" (RF20) e a lista de visitas previstas do estudo no detalhe (o planejamento só pede o atalho para as visitas, que o PBI-25 preenche).

## 3. Onde implementar

| Arquivo | Ação | Responsabilidade |
|---|---|---|
| `mobile/ClinicalStudyPB/src/api/estudos.ts` | Alterar | `estudosApi.buscar(estudoId)` |
| `mobile/ClinicalStudyPB/src/app/(tabs)/estudos.tsx` | Alterar | Lista de cartões (M03) |
| `mobile/ClinicalStudyPB/src/app/estudos/[estudoId].tsx` | Criar | Detalhe do estudo (M04) |
| `mobile/ClinicalStudyPB/src/utils/estudos.ts` | Criar | Rótulos em português do estado do estudo e da participação |
| `mobile/ClinicalStudyPB/src/app/__tests__/estudos.test.tsx` | Criar | Testes de componente (feitos na T113) |

## 4. Requisitos a implementar

Cada requisito tem um identificador (`RQ-T112-NN`) para a revisão do Pull Request e para a conferência no fim da task: todos precisam estar atendidos.

### Lista Meus estudos (M03)

- **RQ-T112-01** Cada estudo é um **cartão** com título, **status do estudo** (Planejamento, Recrutamento, Execução ou Encerrado, em `Selo` com texto) e **data de inclusão** (`dd/MM/yyyy`); estudos com participação **RETIRADO** mostram também o selo "Retirado".
- **RQ-T112-02** Ordenar por data de inclusão da mais recente para a mais antiga (a API já devolve nessa ordem); tocar no cartão abre o detalhe (`/estudos/{estudoId}`).
- **RQ-T112-03** Estados: **carregamento** (`Carregando`), **erro** com mensagem do servidor ou de conexão e o botão **Tentar de novo** (`EstadoErro`), e **lista vazia** com "Você ainda não está incluído em nenhum estudo." (`EstadoVazio`).
- **RQ-T112-04** **Rolagem puxando para atualizar** (`RefreshControl`) recarrega a lista; o indicador some ao terminar, também em caso de erro.

### Detalhe do estudo (M04)

- **RQ-T112-05** Mostrar **título, objetivo (completo), responsável, período** (`dataInicio` a `dataFim`, com "—" quando ausentes), **status do estudo** e a **situação da participação** (Ativo ou Retirado); para participante retirado, mostrar a **marca "Retirado"** e a data da retirada (sem o motivo).
- **RQ-T112-06** Botão **Ver visitas deste estudo** (atalho): leva à aba Visitas com o filtro do estudo (parâmetro `estudoId`); a aba Visitas é preenchida pelo PBI-25 (T117), mas o atalho já navega para ela.
- **RQ-T112-07** Estados de **carregamento** e **erro** com **Tentar de novo**; um estudo que não é do participante (404) mostra "Estudo não encontrado." e o botão de voltar, sem travar o app.
- **RQ-T112-08** Cabeçalho com título "Estudo" e botão voltar em português (declarados na T108).

### Acessibilidade e acabamento

- **RQ-T112-09** Alvos de toque de pelo menos 44 pontos, `accessibilityRole="button"` e `accessibilityLabel` com título e status em cada cartão ("Estudo X, em execução, incluído em 20/10/2026"); o selo nunca depende só de cor.
- **RQ-T112-10** Todos os textos em português; datas no formato brasileiro; nenhum JSON cru exibido.
- **RQ-T112-11** Conferir com o **participante de demonstração** (`participante@clinicalstudy.test`, vinculado ao P1 pela T111) e com `demo.participante@clinicalstudy.test` (retirado) em emulador ou celular.

## 5. Contrato, regras e códigos de erro

- Consome as operações 13 e 14 da seção 2 das convenções (`MeuEstudoResponse`, `MeuEstudoDetalheResponse`).

## 6. Testes

- Testes de componente (T113): lista com dados, vazia e com erro; detalhe com dados, retirado e 404; pull-to-refresh e Tentar de novo.
- Roteiro manual com os dois participantes de demonstração.

## 7. Critérios de aceite

- [ ] O participante vê a lista dos estudos em que está incluído e o detalhe de cada um.
- [ ] Carregamento, erro, lista vazia e atualização por rolagem funcionam.
- [ ] O participante retirado vê a marca "Retirado".

## 8. Cuidados e fora de escopo

- Sem botão de retirada (RF20 é da Sprint 4).
- O app nunca mostra dados de outro participante (garantido pelo backend; a tela não monta consultas por id de participante).

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

Nenhuma das dez regras numeradas cita esta task diretamente; valem as regras gerais da Sprint (formato único de erro, Definition of Done).

### 9.4 Texto original da task no planejamento

> **T112 · Telas Meus estudos e detalhe do estudo.** Primeiro substitua o conteúdo provisório da aba Estudos pela lista de cartões dos estudos do participante (título, estado do estudo e data de inclusão), sobre o esqueleto da T108 e a API da T109. Depois crie o detalhe do estudo, com a descrição e a situação da participação, mostrando a marca RETIRADO quando for o caso, e um atalho para as visitas daquele estudo, que o PBI-25 preenche. Em seguida trate carregamento, erro com Tentar de novo, lista vazia e rolagem puxando para atualizar. Por fim, confira com o participante de demonstração.

### 9.5 Requisitos da especificação que atravessam esta task

- Estratégia de testes: em componentes mobile, testar o “comportamento das telas mobile: login, listagem de visitas, confirmação”. Regra 9.4: “A interface **mobile** atende ao perfil de participante e executa em dispositivo ou emulador.”
- **Definition of Done** da Sprint (texto completo na [seção 11 das convenções](00-convencoes-e-contrato-compartilhado.md#11-definition-of-done-e-requisitos-transversais-da-especificação)): os critérios de aceite estão atendidos e demonstráveis com dados fictícios; o código foi revisado, passou no CI e entrou na `main` por Pull Request; há testes automatizados e, no backend, a cobertura do código de negócio permanece em 80% ou mais; erros e validações são tratados com mensagens claras; Swagger e contrato YAML refletem os endpoints; os dados de demonstração estão no perfil `demo`; documentação e capturas estão no repositório; e RF01 a RF11 continuam funcionando.


---
Parte do conjunto de documentos de design da Sprint 3: [índice](README.md) · [convenções e contrato](00-convencoes-e-contrato-compartilhado.md).
