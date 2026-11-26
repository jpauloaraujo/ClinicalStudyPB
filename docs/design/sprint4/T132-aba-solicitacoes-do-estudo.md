# T132 · Aba Solicitações do estudo

> **Sprint 4** · PBI-27 · RF19 Atendimento dos pedidos de reagendamento na web  
> **Responsável:** I4 · **Estimativa:** 4h · **Prazo:** Sex 27/11  
> **Depende de:** T131  
> **Situação:** ⬜ A implementar (este documento é o guia para quem for fazer)

> Antes de começar, leia as [convenções e o contrato compartilhado da Sprint 4](00-convencoes-e-contrato-compartilhado.md): ajustes ao planejamento, endpoints, DTOs, códigos de erro, tabelas e dados de demonstração valem para esta task. A origem dos requisitos está na [seção 9](#9-origem-dos-requisitos-especificação-e-planejamento).

## 1. Objetivo

Criar a aba **Solicitações** no detalhe do estudo, com contagem de pendentes, lista filtrada e os diálogos Atender e Recusar.

## 2. Contexto: o que já existe e deve ser reaproveitado

- `abas.ts` é a fonte única das abas; `router.tsx` usa `perfisDaAba`.
- `AbaAgenda` é o modelo de lista com filtros, paginação, `useCarregamento` e estados.
- `FormularioDataHora` (T77) limita a data à janela; `converterErro` e `interpretarErroDeAgendamento` traduzem erros.

## 3. Onde implementar

| Arquivo | Ação | Responsabilidade |
|---|---|---|
| `web/ClinicalStudyPB/src/api/solicitacoes.ts` | Criar | `listar`, `pendentes`, `atender`, `recusar` |
| `web/ClinicalStudyPB/src/api/tipos.ts` | Alterar | Reexportar tipos |
| `web/ClinicalStudyPB/src/pages/estudos/detalhe/abas.ts` | Alterar | Aba `solicitacoes` |
| `web/ClinicalStudyPB/src/routes/router.tsx` | Alterar | Rota |
| `web/ClinicalStudyPB/src/pages/estudos/detalhe/EstudoDetalhePage.tsx` | Alterar | Contagem no título |
| `web/ClinicalStudyPB/src/pages/estudos/detalhe/solicitacoes/AbaSolicitacoes.tsx` | Criar | Lista e filtros |
| `web/ClinicalStudyPB/src/pages/estudos/detalhe/solicitacoes/DialogoAtender.tsx` | Criar | Nova data |
| `web/ClinicalStudyPB/src/pages/estudos/detalhe/solicitacoes/DialogoRecusar.tsx` | Criar | Resposta |
| `web/ClinicalStudyPB/src/pages/estudos/detalhe/solicitacoes/mensagensSolicitacao.ts` | Criar | Texto por código |
| `web/ClinicalStudyPB/src/mocks/handlers.ts` | Alterar | Respostas simuladas |

## 4. Requisitos a implementar

Todos os requisitos (`RQ-T132-NN`) precisam estar atendidos para a task ser concluída.

- **RQ-T132-01** Aba `{ caminho: 'solicitacoes', rotulo: 'Solicitações', perfis: ['PESQUISADOR', 'COORDENADOR'] }`.
- **RQ-T132-02** Título "Solicitações (N)" quando houver pendentes; atualizar depois de cada ação; falha na contagem não quebra a página.
- **RQ-T132-03** Colunas: participante, visita, data atual, motivo, preferência, situação, resposta e ações.
- **RQ-T132-04** Filtros por tipo e situação (padrão: Pendente), lidos e gravados nos parâmetros da URL (`?tipo=&situacao=`), com paginação.
- **RQ-T132-05** **Atender**: diálogo com motivo e preferência visíveis e `FormularioDataHora` com a janela do item; a data inicial é a preferência, se estiver na janela, senão a data atual; erro de janela aparece no campo.
- **RQ-T132-06** **Recusar**: resposta obrigatória (5 a 500, contador), com confirmação.
- **RQ-T132-07** Após sucesso, mensagem de status, lista e contagem recarregadas. `SOLICITACAO_JA_RESPONDIDA` mostra a mensagem e recarrega.
- **RQ-T132-08** Pesquisador: sem botões, com o texto "Seu perfil tem acesso apenas para consulta das solicitações."
- **RQ-T132-09** Estados de carregamento, vazio, erro com "Tentar de novo".
- **RQ-T132-10** Preparar o tipo `RETIRADA`: coluna de tipo e ações decididas por uma função única (`acoesDaSolicitacao`), que nesta task não devolve ação para retirada.
- **RQ-T132-11** Diálogos com `role="dialog"`, rótulo, foco no primeiro campo e `Escape` para fechar.

## 5. Contrato, regras e códigos de erro

Operações 4 a 7.

## 6. Testes

Casos básicos junto com o código; completos na T134.

## 7. Critérios de aceite

- [ ] O coordenador atende e recusa; o pesquisador só consulta.
- [ ] A contagem acompanha as ações.
- [ ] Lint, testes e build verdes.

## 8. Cuidados e fora de escopo

- O campo de visita é nulo na retirada: tratar sem quebrar a linha.
- Não repetir a validação de janela no cliente além do `min`/`max`.

## 9. Origem dos requisitos (especificação e planejamento)

Texto **sem paráfrase** da especificação e do planejamento. Onde ele diverge dos [ajustes da seção 1 das convenções](00-convencoes-e-contrato-compartilhado.md#1-ajustes-em-relação-ao-planejamento-e-por-quê), valem as convenções e este documento.

### 9.1 Requisito da especificação

- **RF19** (Módulo E, Aplicativo do participante; interface Mobile): “Permitir confirmar presença prevista ou solicitar reagendamento de visita.”

### 9.2 Critério de aceite do PBI (planejamento)

> o coordenador vê os pedidos do estudo, atende um reagendamento escolhendo uma data válida ou o recusa com resposta, e o participante vê o resultado no app; as presenças confirmadas aparecem na agenda e no histórico; o pesquisador consulta tudo em modo somente leitura.

### 9.3 Regras de negócio do planejamento que afetam esta task

| # | Regra proposta | Tasks afetadas |
|---|---|---|
| 3 | Pedidos do participante têm a situação PENDENTE, ATENDIDA ou RECUSADA, e as duas últimas são finais. O coordenador atende o reagendamento escolhendo nova data dentro da janela da visita (regras do RF12), o que reagenda a visita, limpa a confirmação de presença e marca o pedido como ATENDIDA na mesma transação; ou recusa com resposta obrigatória. Se a visita receber desfecho ou for cancelada antes, o pedido pendente é recusado automaticamente com resposta padrão | T129, T130, T132 |

### 9.4 Texto original da task no planejamento

> **T132 · Aba Solicitações do estudo.** Primeiro crie, no detalhe do estudo, a aba Solicitações com a contagem de pendentes no título e a lista com participante, visita, data atual, motivo, preferência e situação, filtrada por tipo e situação. Depois crie o diálogo Atender, que reaproveita o seletor de data da T77 com a janela válida, e o diálogo Recusar, com resposta obrigatória. Em seguida deixe a aba somente leitura para o pesquisador. Por fim, deixe a lista preparada para o tipo RETIRADA, que o PBI-28 acrescenta.

### 9.5 Requisitos que atravessam esta task

- Regra 9.2: “O backend deve disponibilizar uma **API HTTP REST** compartilhada pela web e pelo mobile, documentada no padrão **OpenAPI**”.
- Entregas: “Persistência dos dados, com scripts de criação/migração do banco e dados fictícios para demonstração” e “Tratamento de erros e validações das regras de negócio”.
- Testes (seção 7): “Testes unitários para todas as regras de negócio relevantes”, “Testes de integração para os principais endpoints” e “Execução automatizada dos testes a cada pull request”.
- Regra 9.3: “A interface **web** atende aos perfis de pesquisador, coordenador e administrador.”
- **Definition of Done** (texto completo na [seção 11 das convenções](00-convencoes-e-contrato-compartilhado.md#11-definition-of-done)): critérios de aceite demonstráveis com dados fictícios; revisão, CI e Pull Request; testes automatizados e 80% de cobertura do negócio no backend; erros com mensagens claras; Swagger e YAML atualizados; dados de demonstração, documentação, capturas e nota de bastão no repositório; RF01 a RF18 continuam funcionando.

---
Parte dos documentos de design da Sprint 4: [índice](README.md) · [convenções e contrato](00-convencoes-e-contrato-compartilhado.md).
