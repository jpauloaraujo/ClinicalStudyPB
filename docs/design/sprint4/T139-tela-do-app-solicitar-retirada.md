# T139 · Tela do app: solicitar retirada

> **Sprint 4** · PBI-28 · RF20 Retirada voluntária pelo app  
> **Responsável:** I5 · **Estimativa:** 3h · **Prazo:** Dom 29/11  
> **Depende de:** T137  
> **Situação:** ⬜ A implementar (este documento é o guia para quem for fazer)

> Antes de começar, leia as [convenções e o contrato compartilhado da Sprint 4](00-convencoes-e-contrato-compartilhado.md): ajustes ao planejamento, endpoints, DTOs, códigos de erro, tabelas e dados de demonstração valem para esta task. A origem dos requisitos está na [seção 9](#9-origem-dos-requisitos-especificação-e-planejamento).

## 1. Objetivo

Tela do app para solicitar a retirada, com explicação dos efeitos, ciência obrigatória e estados pendente e retirado.

## 2. Contexto: o que já existe e deve ser reaproveitado

- `app/estudos/[estudoId].tsx` já mostra o cartão "Retirado em ...".
- Padrões de formulário e erros da T126.

## 3. Onde implementar

| Arquivo | Ação | Responsabilidade |
|---|---|---|
| `mobile/ClinicalStudyPB/src/app/estudos/[estudoId].tsx` | Alterar | Botão, aviso e foco |
| `mobile/ClinicalStudyPB/src/app/estudos/retirada.tsx` | Criar | Efeitos, ciência, motivo e confirmação |
| `mobile/ClinicalStudyPB/src/api/solicitacoes.ts` | Alterar | `pedirRetirada` |
| `mobile/ClinicalStudyPB/src/app/_layout.tsx` | Alterar | Registrar a tela |
| `mobile/ClinicalStudyPB/src/utils/solicitacoes.ts` | Alterar | Mensagens |
| `mobile/ClinicalStudyPB/src/api/mocks/index.ts` | Alterar | Rota |

## 4. Requisitos a implementar

Todos os requisitos (`RQ-T139-NN`) precisam estar atendidos para a task ser concluída.

- **RQ-T139-01** Botão **Solicitar retirada** (`botao-solicitar-retirada`) só com `podeSolicitarRetirada`.
- **RQ-T139-02** A tela explica: visitas futuras serão canceladas, a participação será encerrada, o histórico é mantido e a decisão é efetivada pelo coordenador.
- **RQ-T139-03** Caixa de ciência obrigatória (`caixa-ciencia`, `accessibilityRole="checkbox"`); o botão de envio fica desabilitado sem ela.
- **RQ-T139-04** Motivo opcional (até 500), indicado como opcional.
- **RQ-T139-05** Confirmação final em um segundo passo ("Confirmar pedido de retirada" / "Voltar").
- **RQ-T139-06** Após enviar, o detalhe mostra "Retirada solicitada em dd/mm/aaaa" e não há mais o botão.
- **RQ-T139-07** Com retirada pendente, os botões do RF19 não aparecem (vem dos booleanos do servidor) e o detalhe da visita explica o motivo, usando `retiradaPendente`.
- **RQ-T139-08** Depois da efetivação, o detalhe mostra a marca **Retirado** (já existente) sem pedido pendente.
- **RQ-T139-09** Recarregar o detalhe ao ganhar foco.
- **RQ-T139-10** Erros 409 com mensagem por código; sem conexão e 500 tratados; mocks atualizados.

## 5. Contrato, regras e códigos de erro

Operação 8 e campos da seção 3.2.

## 6. Testes

Mínimo junto; completo na T141.

## 7. Critérios de aceite

- [ ] Não é possível enviar sem ciência.
- [ ] Estados pendente e retirado aparecem corretamente no emulador.

## 8. Cuidados e fora de escopo

- Texto sem tom de pressão para o participante desistir ou continuar.
- Nunca mostrar o motivo registrado pelo coordenador.

## 9. Origem dos requisitos (especificação e planejamento)

Texto **sem paráfrase** da especificação e do planejamento. Onde ele diverge dos [ajustes da seção 1 das convenções](00-convencoes-e-contrato-compartilhado.md#1-ajustes-em-relação-ao-planejamento-e-por-quê), valem as convenções e este documento.

### 9.1 Requisito da especificação

- **RF20** (Módulo E, Aplicativo do participante; interface Mobile): “Permitir solicitar retirada voluntária do estudo.”

### 9.2 Critério de aceite do PBI (planejamento)

> o participante solicita a retirada de um estudo pelo app, ciente dos efeitos; o coordenador vê o pedido e efetiva a retirada pelo RF16; o app mostra o pedido pendente e, depois, a participação como RETIRADO; pedidos duplicados ou de participação já retirada são recusados.

### 9.3 Regras de negócio do planejamento que afetam esta task

| # | Regra proposta | Tasks afetadas |
|---|---|---|
| 4 | O participante solicita a retirada pelo app sem precisar justificar (motivo opcional), mas confirma que entende os efeitos. Há no máximo um pedido pendente por participação, e enquanto ele existe o app bloqueia confirmações e novos pedidos de reagendamento | T136, T137, T139 |

### 9.4 Texto original da task no planejamento

> **T139 · Tela do app: solicitar retirada.** Primeiro acrescente ao detalhe do estudo (T112) o botão Solicitar retirada. Depois crie a tela que explica os efeitos (visitas futuras canceladas e a participação encerrada), com a caixa de ciência obrigatória, o motivo opcional e uma confirmação final. Em seguida mostre o aviso "Retirada solicitada em ..." e esconda os botões do RF19 enquanto o pedido estiver pendente. Por fim, mostre a marca RETIRADO depois da efetivação.

### 9.5 Requisitos que atravessam esta task

- Regra 9.2: “O backend deve disponibilizar uma **API HTTP REST** compartilhada pela web e pelo mobile, documentada no padrão **OpenAPI**”.
- Entregas: “Persistência dos dados, com scripts de criação/migração do banco e dados fictícios para demonstração” e “Tratamento de erros e validações das regras de negócio”.
- Testes (seção 7): “Testes unitários para todas as regras de negócio relevantes”, “Testes de integração para os principais endpoints” e “Execução automatizada dos testes a cada pull request”.
- Regras 9.3 e 9.4: web para a equipe do estudo e app para o participante.
- **Definition of Done** (texto completo na [seção 11 das convenções](00-convencoes-e-contrato-compartilhado.md#11-definition-of-done)): critérios de aceite demonstráveis com dados fictícios; revisão, CI e Pull Request; testes automatizados e 80% de cobertura do negócio no backend; erros com mensagens claras; Swagger e YAML atualizados; dados de demonstração, documentação, capturas e nota de bastão no repositório; RF01 a RF18 continuam funcionando.

---
Parte dos documentos de design da Sprint 4: [índice](README.md) · [convenções e contrato](00-convencoes-e-contrato-compartilhado.md).
