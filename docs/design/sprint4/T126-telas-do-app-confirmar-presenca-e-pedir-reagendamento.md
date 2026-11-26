# T126 · Telas do app: confirmar presença e pedir reagendamento

> **Sprint 4** · PBI-26 · RF19 Confirmação de presença e pedido de reagendamento no app  
> **Responsável:** I7 · **Estimativa:** 4h · **Prazo:** Qua 25/11  
> **Depende de:** T125  
> **Situação:** ⬜ A implementar (este documento é o guia para quem for fazer)

> Antes de começar, leia as [convenções e o contrato compartilhado da Sprint 4](00-convencoes-e-contrato-compartilhado.md): ajustes ao planejamento, endpoints, DTOs, códigos de erro, tabelas e dados de demonstração valem para esta task. A origem dos requisitos está na [seção 9](#9-origem-dos-requisitos-especificação-e-planejamento).

## 1. Objetivo

Telas do app para confirmar presença, pedir reagendamento, mostrar os selos nas próximas visitas e listar "Meus pedidos".

## 2. Contexto: o que já existe e deve ser reaproveitado

- `app/visitas/[visitaId].tsx` recebe a visita pelo parâmetro `visita` (JSON do cartão) e hoje não tem ações.
- `ListaProximas` (`app/(tabs)/visitas.tsx`) carrega só ao montar; `CartaoVisita`, `Selo`, `Botao`, `EstadoErro` e `EstadoVazio` existem.
- `requisitar`, `ErroApi`, `mensagemDaConsulta` e os mocks (`EXPO_PUBLIC_USE_MOCKS`) já estão prontos.
- Telas internas são registradas no `Stack` de `app/_layout.tsx` com `telaInterna(titulo)`.

## 3. Onde implementar

| Arquivo | Ação | Responsabilidade |
|---|---|---|
| `mobile/ClinicalStudyPB/src/api/solicitacoes.ts` | Criar | `confirmar`, `pedirReagendamento`, `listar` |
| `mobile/ClinicalStudyPB/src/app/visitas/[visitaId].tsx` | Alterar | Botões e selos |
| `mobile/ClinicalStudyPB/src/app/visitas/reagendar.tsx` | Criar | Formulário do pedido |
| `mobile/ClinicalStudyPB/src/app/pedidos.tsx` | Criar | Meus pedidos |
| `mobile/ClinicalStudyPB/src/app/(tabs)/perfil.tsx` | Alterar | Botão "Meus pedidos" |
| `mobile/ClinicalStudyPB/src/app/(tabs)/visitas.tsx` | Alterar | Recarregar ao voltar o foco |
| `mobile/ClinicalStudyPB/src/app/_layout.tsx` | Alterar | Registrar as telas |
| `mobile/ClinicalStudyPB/src/components/CartaoVisita.tsx` | Alterar | Selos |
| `mobile/ClinicalStudyPB/src/components/CartaoSolicitacao.tsx` | Criar | Item de "Meus pedidos" |
| `mobile/ClinicalStudyPB/src/utils/solicitacoes.ts` | Criar | Rótulos, tons e mensagens por código |
| `mobile/ClinicalStudyPB/src/api/mocks/index.ts` | Alterar | Rotas e dados dos três endpoints |

## 4. Requisitos a implementar

Todos os requisitos (`RQ-T126-NN`) precisam estar atendidos para a task ser concluída.

- **RQ-T126-01** No detalhe, mostrar **Confirmar presença** (`testID="botao-confirmar-presenca"`) só se `podeConfirmar` e **Pedir reagendamento** (`botao-pedir-reagendamento`) só se `podePedirReagendamento`.
- **RQ-T126-02** Confirmar em um toque: desabilitar o botão durante o envio, atualizar a tela com a resposta e mostrar "Presença confirmada em <data e hora>".
- **RQ-T126-03** Formulário de reagendamento: motivo obrigatório (5 a 500, contador, `campo-motivo`), preferência de data opcional (`campo-data-preferida`, `dd/mm/aaaa`, convertida para `aaaa-mm-dd`; data inválida ou passada é avisada no campo), botão `botao-enviar-pedido`.
- **RQ-T126-04** Depois de enviar, voltar ao detalhe com "Pedido enviado. Aguarde a resposta do coordenador." e o selo "Reagendamento solicitado".
- **RQ-T126-05** Nos cartões de próximas visitas, selos **Presença confirmada** (tom sucesso) e **Reagendamento solicitado** (tom aviso), também lidos em `descricaoDaVisita` (acessibilidade).
- **RQ-T126-06** Recarregar próximas ao voltar o foco (`useFocusEffect`) para os selos refletirem a ação sem puxar para atualizar.
- **RQ-T126-07** "Meus pedidos" (`link-meus-pedidos` no Perfil): tipo, estudo, visita, data, motivo, preferência, situação (Pendente, Atendido, Recusado) e a **resposta do coordenador**; estados de carregamento, vazio ("Você ainda não fez pedidos."), erro e puxar para atualizar.
- **RQ-T126-08** Tratar `409` com a mensagem da regra por código (mapa em `utils/solicitacoes.ts`, com a mensagem do servidor como reserva) e recarregar a visita, porque a situação mudou.
- **RQ-T126-09** `400` de motivo aparece no campo; sem conexão usa o texto padrão; `401` e `403` seguem o comportamento do cliente.
- **RQ-T126-10** Alvos de toque de pelo menos 44 pontos e nenhum estado dependente só de cor.
- **RQ-T126-11** Mocks: as três rotas respondem como a API, inclusive um 409.

## 5. Contrato, regras e códigos de erro

Operações 1 a 3 e códigos da seção 4. A tela não recalcula regra: usa `podeConfirmar` e `podePedirReagendamento`.

## 6. Testes

Escrever junto o mínimo de cada tela; a cobertura completa é a T127.

## 7. Critérios de aceite

- [ ] Confirmar e pedir reagendamento funcionam contra a API real no emulador.
- [ ] Botões somem quando a regra não permite.
- [ ] "Meus pedidos" mostra a situação e a resposta.
- [ ] `npm run lint`, `npm run typecheck` e `npm test` verdes.

## 8. Cuidados e fora de escopo

- Não adicionar biblioteca de seletor de data sem necessidade: o campo de texto validado basta.
- Os parâmetros de rota ficam velhos depois de uma ação: usar o estado atualizado, não o JSON original.
- O botão de retirada é do PBI-28.

## 9. Origem dos requisitos (especificação e planejamento)

Texto **sem paráfrase** da especificação e do planejamento. Onde ele diverge dos [ajustes da seção 1 das convenções](00-convencoes-e-contrato-compartilhado.md#1-ajustes-em-relação-ao-planejamento-e-por-quê), valem as convenções e este documento.

### 9.1 Requisito da especificação

- **RF19** (Módulo E, Aplicativo do participante; interface Mobile): “Permitir confirmar presença prevista ou solicitar reagendamento de visita.”

### 9.2 Critério de aceite do PBI (planejamento)

> no detalhe de uma visita agendada, o participante confirma presença ou pede reagendamento com motivo; o app mostra a situação de cada pedido (pendente, atendido ou recusado, com a resposta); pedidos inválidos são recusados com a regra violada. Este PBI cria a tabela de pedidos do participante que os PBIs 27 e 28 usam.

### 9.3 Regras de negócio do planejamento que afetam esta task

| # | Regra proposta | Tasks afetadas |
|---|---|---|
| 1 | O participante confirma presença apenas em visita AGENDADA, com data futura, participação ativa e sem pedido de retirada pendente. A confirmação é única (uma segunda dá 409), guarda data e hora e não muda a situação da visita | T122, T124, T126 |
| 2 | O pedido de reagendamento vale para visita AGENDADA e futura, exige motivo (ao menos 5 caracteres) e aceita uma preferência de data opcional. Cada visita tem no máximo um pedido PENDENTE, e a visita só muda quando o coordenador atende | T122, T124, T126 |

### 9.4 Texto original da task no planejamento

> **T126 · Telas do app: confirmar presença e pedir reagendamento.** Primeiro acrescente ao detalhe da visita (T117) os botões Confirmar presença e Pedir reagendamento, visíveis só quando a regra permite. Depois crie a confirmação em um toque, com retorno claro, e o formulário de reagendamento com motivo obrigatório e preferência de data opcional. Em seguida mostre nos cartões de próximas visitas os selos "Presença confirmada" e "Reagendamento solicitado", e crie a lista Meus pedidos no Perfil, com a resposta do coordenador. Por fim, trate os erros 409 com a mensagem da regra.

### 9.5 Requisitos que atravessam esta task

- Regra 9.2: “O backend deve disponibilizar uma **API HTTP REST** compartilhada pela web e pelo mobile, documentada no padrão **OpenAPI**”.
- Entregas: “Persistência dos dados, com scripts de criação/migração do banco e dados fictícios para demonstração” e “Tratamento de erros e validações das regras de negócio”.
- Testes (seção 7): “Testes unitários para todas as regras de negócio relevantes”, “Testes de integração para os principais endpoints” e “Execução automatizada dos testes a cada pull request”.
- Regra 9.4: “A interface **mobile** atende ao perfil de participante e executa em dispositivo ou emulador.”
- **Definition of Done** (texto completo na [seção 11 das convenções](00-convencoes-e-contrato-compartilhado.md#11-definition-of-done)): critérios de aceite demonstráveis com dados fictícios; revisão, CI e Pull Request; testes automatizados e 80% de cobertura do negócio no backend; erros com mensagens claras; Swagger e YAML atualizados; dados de demonstração, documentação, capturas e nota de bastão no repositório; RF01 a RF18 continuam funcionando.

---
Parte dos documentos de design da Sprint 4: [índice](README.md) · [convenções e contrato](00-convencoes-e-contrato-compartilhado.md).
