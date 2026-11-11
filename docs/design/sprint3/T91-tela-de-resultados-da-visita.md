# T91 · Tela de resultados da visita

> **Sprint 3** · PBI-21 · RF14 Resultados fictícios das avaliações previstas  
> **Responsável:** I6 · **Estimativa:** 4h · **Prazo:** Dom 15/11  
> **Depende de:** T88, T90  
> **Situação:** ⬜ A implementar (este documento é o guia para quem for fazer)

> Antes de começar, leia as [convenções e o contrato compartilhado da Sprint 3](00-convencoes-e-contrato-compartilhado.md): nomes, endpoints, DTOs, códigos de erro, tabelas e dados de demonstração valem para esta task. A origem dos requisitos (especificação, critério de aceite do PBI, regras de negócio e o texto original da task) está na [seção 9](#9-origem-dos-requisitos-especificação-e-planejamento).

## 1. Objetivo

Acrescentar à tela da visita (PBI-19 e PBI-20) a aba **Avaliações**, onde o pesquisador registra e edita o resultado de cada atividade de avaliação de uma visita realizada, e o coordenador consulta em modo somente leitura.

## 2. Contexto: o que já existe e deve ser reaproveitado

- `VisitaAgendadaPage` e `abasVisita.ts` (T77) são o ponto de encaixe: a aba entra por `abasVisita.ts`, sem reescrever a tela. `SeloSituacaoVisita` e `AcoesDaVisita` vêm das T77 e T83.
- API real da T90, por `visitasAgendadasApi` (`src/api/visitasAgendadas.ts`, criado na T83): acrescentar `resultados` e `registrarResultado`.
- Padrões: `useCarregamento`, `Campo`, `converterErro`, `mensagensAcompanhamento.ts`, testes com `renderizarAba` e `erroHttp`.
- O caso de participante retirado entra na T104 (o campo `bloqueio` já prevê `PARTICIPANTE_RETIRADO`).

## 3. Onde implementar

| Arquivo | Ação | Responsabilidade |
|---|---|---|
| `web/ClinicalStudyPB/src/api/visitasAgendadas.ts` | Alterar | `resultados(visitaAgendadaId)` e `registrarResultado(visitaAgendadaId, acaoId, corpo)` |
| `web/ClinicalStudyPB/src/pages/estudos/detalhe/acompanhamento/abasVisita.ts` | Alterar | Nova aba `avaliacoes` (`PESQUISADOR`, `COORDENADOR`) |
| `web/ClinicalStudyPB/src/pages/estudos/detalhe/acompanhamento/AbaAvaliacoesVisita.tsx` | Criar | Lista de atividades com formulário por atividade |
| `web/ClinicalStudyPB/src/pages/estudos/detalhe/acompanhamento/VisitaAgendadaPage.tsx` | Alterar | Renderiza as abas definidas em `abasVisita.ts` |
| `web/ClinicalStudyPB/src/pages/estudos/detalhe/acompanhamento/mensagensAcompanhamento.ts` | Alterar | Códigos dos resultados |
| `web/ClinicalStudyPB/src/pages/estudos/detalhe/acompanhamento/AbaAvaliacoesVisita.test.tsx` | Criar | Testes de componente |

## 4. Requisitos a implementar

Cada requisito tem um identificador (`RQ-T91-NN`) para a revisão do Pull Request e para a conferência no fim da task: todos precisam estar atendidos.

### Aba Avaliações

- **RQ-T91-01** Entrar em `abasVisita.ts` com `{ caminho: 'avaliacoes', rotulo: 'Avaliações', perfis: ['PESQUISADOR', 'COORDENADOR'] }`; a aba aparece em `VisitaAgendadaPage`, aberta pela agenda e pelo histórico.
- **RQ-T91-02** Listar as atividades da categoria avaliação da visita (de `visitasAgendadasApi.resultados`), cada uma com **nome**, descrição e os campos **valor**, **unidade** e **observação**, preenchidos quando já existe resultado, e o selo de texto **"pendente"** ou **"registrado"**.
- **RQ-T91-03** Mostrar o contador **"X de N avaliações registradas"** no topo, atualizado a cada salvamento; sem atividades de avaliação, mostrar o texto "Esta visita não tem avaliações previstas".

### Salvar e editar

- **RQ-T91-04** Cada atividade tem o seu botão **Salvar** (só pesquisador): salva **uma atividade por vez** (resultado parcial) com `registrarResultado`; durante o envio, o botão fica desabilitado; depois do sucesso, mostra confirmação, atualiza o selo para "registrado" e o contador, e mostra autor e data da última alteração (`alteradoPor` e `alteradoEm`).
- **RQ-T91-05** Editar um resultado já registrado usa o mesmo formulário; o botão só habilita quando algo mudou.
- **RQ-T91-06** Erros do servidor por campo (valor vazio, tamanhos) aparecem junto do campo; erros de regra são traduzidos por código (`VISITA_NAO_REALIZADA`, `ATIVIDADE_FORA_DA_CATEGORIA_AVALIACAO`, `ATIVIDADE_NAO_PERTENCE_A_VISITA`, `ESTUDO_ENCERRADO`, `VALOR_OBRIGATORIO`); código desconhecido mostra a mensagem do servidor.
- **RQ-T91-07** Validar no cliente: valor obrigatório (1 a 200 caracteres), unidade até 30, observação até 500, com contadores.

### Somente leitura e avisos

- **RQ-T91-08** Para o **coordenador**, a aba é somente leitura: valores exibidos como texto, sem botões, com o aviso "Somente o pesquisador registra resultados".
- **RQ-T91-09** Quando `bloqueio` for `VISITA_NAO_REALIZADA`, mostrar o aviso "Os resultados só podem ser registrados em visitas realizadas" e os campos desabilitados; quando for `ESTUDO_ENCERRADO`, mostrar o aviso explicando que o estudo está encerrado e deixar tudo somente leitura. O caso `PARTICIPANTE_RETIRADO` entra na T104.

### Estados e acabamento

- **RQ-T91-10** Carregamento, erro com "Tentar de novo" e visita sem atividades de avaliação.
- **RQ-T91-11** Rótulos acessíveis (cada campo associado ao nome da atividade, ex.: "Valor (Aferir pressão arterial)") e região nomeada "Avaliações da visita".
- **RQ-T91-12** Testes de componente (seção 6); `npm run lint`, `tsc -b` e `npm test` sem erro.

## 5. Contrato, regras e códigos de erro

- Consome as operações 8 e 9 da seção 2 das convenções.

## 6. Testes

- `AbaAvaliacoesVisita.test.tsx`: lista com pendentes e registrados; salvar uma atividade (corpo e selo); editar; erro de valor vazio por campo; erro de regra por código; contador; coordenador somente leitura; `bloqueio` de visita não realizada e de estudo encerrado; carregamento e erro.
- `VisitaAgendadaPage.test.tsx` atualizado: a aba Avaliações aparece para os dois perfis.

## 7. Critérios de aceite

- [ ] O pesquisador registra e edita o resultado de cada atividade de avaliação de uma visita realizada.
- [ ] O coordenador vê tudo em modo somente leitura, com aviso.
- [ ] Visitas não realizadas e estudos encerrados mostram o motivo do bloqueio.
- [ ] O contador "X de N avaliações registradas" reflete o servidor.

## 8. Cuidados e fora de escopo

- Entra pelo encaixe `abasVisita.ts`: não reescrever `VisitaAgendadaPage` nem a agenda.
- Não há exclusão de resultado.

## 9. Origem dos requisitos (especificação e planejamento)

Esta seção reúne, **sem paráfrase**, o que a especificação do projeto e o planejamento da Sprint 3 exigem para esta task, para que o documento sozinho mostre de onde vem cada requisito da seção 4. Onde o texto original difere das convenções (ajustes 1 a 10, [seção 1](00-convencoes-e-contrato-compartilhado.md#1-ajustes-em-relação-ao-planejamento-e-por-quê)), vale o que está neste documento e nas convenções.

### 9.1 Requisito funcional da especificação

- **RF14** (Módulo D — Acompanhamento; interface Web): “Registrar resultados fictícios das avaliações previstas.”

### 9.2 Critério de aceite do PBI (planejamento)

> O pesquisador registra e edita o resultado de cada atividade da categoria avaliação de uma visita REALIZADA; visitas não realizadas, atividades de outras categorias e estudos encerrados são recusados; o coordenador consulta os resultados em modo somente leitura.

### 9.3 Regras de negócio do planejamento que afetam esta task

| # | Regra proposta | Tasks afetadas (no planejamento) |
|---|---|---|
| 4 | Resultados fictícios só existem para visita REALIZADA e só para atividades da categoria avaliação. Quem registra é o pesquisador, com um resultado por atividade, editável enquanto o estudo não estiver encerrado, guardando autor e data da última alteração | T87, T89, T91 |

### 9.4 Texto original da task no planejamento

> **T91 · Tela de resultados da visita.** Primeiro crie, sobre a API da T90, a aba Avaliações da visita REALIZADA (aberta pela agenda), com a lista das atividades da categoria avaliação e, para cada uma, os campos valor, unidade e observação e o selo "pendente" ou "registrado". Depois salve cada atividade separadamente, com feedback e erros do servidor por campo, e mostre o contador "X de N avaliações registradas". Em seguida deixe a aba somente leitura para o coordenador e quando o estudo estiver encerrado, com aviso explicando o motivo (o caso do participante retirado entra na T104). Por fim, escreva os testes de componente. A tela da visita foi entregue pelos PBIs 19 e 20, então a aba entra pelo ponto de encaixe que eles deixaram.

### 9.5 Requisitos da especificação que atravessam esta task

- Estratégia de testes: “Testes automatizados de componentes nas interfaces web e mobile.” Em componentes web, testar o “comportamento das telas: validação de formulários, botões, mensagens de erro”. Regra 9.3: “A interface **web** atende aos perfis de pesquisador, coordenador e administrador.”
- **Definition of Done** da Sprint (texto completo na [seção 11 das convenções](00-convencoes-e-contrato-compartilhado.md#11-definition-of-done-e-requisitos-transversais-da-especificação)): os critérios de aceite estão atendidos e demonstráveis com dados fictícios; o código foi revisado, passou no CI e entrou na `main` por Pull Request; há testes automatizados e, no backend, a cobertura do código de negócio permanece em 80% ou mais; erros e validações são tratados com mensagens claras; Swagger e contrato YAML refletem os endpoints; os dados de demonstração estão no perfil `demo`; documentação e capturas estão no repositório; e RF01 a RF11 continuam funcionando.


---
Parte do conjunto de documentos de design da Sprint 3: [índice](README.md) · [convenções e contrato](00-convencoes-e-contrato-compartilhado.md).
