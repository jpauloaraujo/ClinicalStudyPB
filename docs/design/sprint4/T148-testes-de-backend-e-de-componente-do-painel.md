# T148 · Testes de backend e de componente do painel

> **Sprint 4** · PBI-29 · RF21 Painel do estudo  
> **Responsável:** I6 · **Estimativa:** 3h · **Prazo:** Ter 01/12  
> **Depende de:** T144, T146, T147  
> **Situação:** ⬜ A implementar (este documento é o guia para quem for fazer)

> Antes de começar, leia as [convenções e o contrato compartilhado da Sprint 4](00-convencoes-e-contrato-compartilhado.md): ajustes ao planejamento, endpoints, DTOs, códigos de erro, tabelas e dados de demonstração valem para esta task. A origem dos requisitos está na [seção 9](#9-origem-dos-requisitos-especificação-e-planejamento).

## 1. Objetivo

Cobrir contagens, permissões e componentes do painel.

## 2. Contexto: o que já existe e deve ser reaproveitado

- Serviços e telas das T144 a T147.

## 3. Onde implementar

| Arquivo | Ação | Responsabilidade |
|---|---|---|
| `backend/ClinicalStudyPB/src/test/java/UEPB/ESWII/ClinicalStudyPB/PainelServiceTest.java` | Alterar | Matriz |
| `backend/ClinicalStudyPB/src/test/java/UEPB/ESWII/ClinicalStudyPB/PainelEndpointTest.java` | Alterar | Status |
| `backend/ClinicalStudyPB/src/test/java/UEPB/ESWII/ClinicalStudyPB/AgendaEndpointTest.java` | Alterar | `atrasada` |
| `web/ClinicalStudyPB/src/pages/estudos/detalhe/painel/PainelEstudo.test.tsx` | Criar | Cartões e links |
| `web/ClinicalStudyPB/src/pages/InicioPage.test.tsx` | Alterar | Visão geral |

## 4. Requisitos a implementar

Todos os requisitos (`RQ-T148-NN`) precisam estar atendidos para a task ser concluída.

- **RQ-T148-01** Cada contagem com cenário de valores conhecidos, incluindo participante retirado, visita atrasada, estudo vazio e estudo sem visitas previstas.
- **RQ-T148-02** Invariantes: soma das situações dos candidatos = total; ativos + retirados + vagas restantes = limite (quando não excedido).
- **RQ-T148-03** Endpoints: 200, 401, 403 e 404; PostgreSQL no CI.
- **RQ-T148-04** Consistência: o total de `agenda?atrasada=true` é igual a `visitas.atrasadas`.
- **RQ-T148-05** Componentes: cartões, estado vazio, erro, links filtrados por perfil, alternativa textual do gráfico.
- **RQ-T148-06** Registrar a cobertura do `PainelService`.

## 5. Contrato, regras e códigos de erro

Definições da T143.

## 6. Testes

Os itens acima.
- `atrasada` com `situacao` diferente de `AGENDADA` (400) e os dois valores do parâmetro.

## 7. Critérios de aceite

- [ ] Suítes verdes e cobertura registrada.

## 8. Cuidados e fora de escopo

- Usar `Clock` fixo para as fronteiras de data.

## 9. Origem dos requisitos (especificação e planejamento)

Texto **sem paráfrase** da especificação e do planejamento. Onde ele diverge dos [ajustes da seção 1 das convenções](00-convencoes-e-contrato-compartilhado.md#1-ajustes-em-relação-ao-planejamento-e-por-quê), valem as convenções e este documento.

### 9.1 Requisito da especificação

- **RF21** (Módulo F, Monitoramento; interface Web): “Exibir painel com quantidade de candidatos, participantes incluídos e visitas realizadas ou pendentes.”

### 9.2 Critério de aceite do PBI (planejamento)

> pesquisador e coordenador abrem o painel de um estudo e veem a quantidade de candidatos por situação, participantes incluídos (ativos, retirados e vagas) e visitas realizadas, pendentes, atrasadas e não agendadas, com os números conferindo com os dados de demonstração; cada indicador leva à lista correspondente.

### 9.3 Regras de negócio do planejamento que afetam esta task

| # | Regra proposta | Tasks afetadas |
|---|---|---|
| 8 | Erros seguem o formato único da Sprint 2 (400, 403, 404 e 409, sempre com o código da regra). As migrations usam as faixas V4_26_\* a V4_32_\*, todas acima das faixas V3 da Sprint 3 | T122 e todas as de backend |

### 9.4 Texto original da task no planejamento

> **T148 · Testes de backend e de componente do painel.** Escreva os testes junto com o código. Primeiro teste cada contagem com um cenário de valores conhecidos, incluindo participante retirado e visita atrasada. Depois teste os endpoints (200, 403 e 404). Por fim, escreva os testes de componente dos cartões, do estado vazio e dos links filtrados.

### 9.5 Requisitos que atravessam esta task

- Regra 9.2: “O backend deve disponibilizar uma **API HTTP REST** compartilhada pela web e pelo mobile, documentada no padrão **OpenAPI**”.
- Entregas: “Persistência dos dados, com scripts de criação/migração do banco e dados fictícios para demonstração” e “Tratamento de erros e validações das regras de negócio”.
- Testes (seção 7): “Testes unitários para todas as regras de negócio relevantes”, “Testes de integração para os principais endpoints” e “Execução automatizada dos testes a cada pull request”.
- Regra 9.3: “A interface **web** atende aos perfis de pesquisador, coordenador e administrador.”
- **Definition of Done** (texto completo na [seção 11 das convenções](00-convencoes-e-contrato-compartilhado.md#11-definition-of-done)): critérios de aceite demonstráveis com dados fictícios; revisão, CI e Pull Request; testes automatizados e 80% de cobertura do negócio no backend; erros com mensagens claras; Swagger e YAML atualizados; dados de demonstração, documentação, capturas e nota de bastão no repositório; RF01 a RF18 continuam funcionando.

---
Parte dos documentos de design da Sprint 4: [índice](README.md) · [convenções e contrato](00-convencoes-e-contrato-compartilhado.md).
