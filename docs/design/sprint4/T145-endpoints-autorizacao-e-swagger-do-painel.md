# T145 · Endpoints, autorização e Swagger do painel

> **Sprint 4** · PBI-29 · RF21 Painel do estudo  
> **Responsável:** I6 · **Estimativa:** 2h · **Prazo:** Seg 30/11  
> **Depende de:** T144  
> **Situação:** ⬜ A implementar (este documento é o guia para quem for fazer)

> Antes de começar, leia as [convenções e o contrato compartilhado da Sprint 4](00-convencoes-e-contrato-compartilhado.md): ajustes ao planejamento, endpoints, DTOs, códigos de erro, tabelas e dados de demonstração valem para esta task. A origem dos requisitos está na [seção 9](#9-origem-dos-requisitos-especificação-e-planejamento).

## 1. Objetivo

Expor o painel do estudo e o resumo dos estudos e registrar o tempo de resposta.

## 2. Contexto: o que já existe e deve ser reaproveitado

- `Permissoes.LEITURA`; padrão de controller das Sprints anteriores.

## 3. Onde implementar

| Arquivo | Ação | Responsabilidade |
|---|---|---|
| `backend/ClinicalStudyPB/src/main/java/UEPB/ESWII/ClinicalStudyPB/controller/PainelController.java` | Criar | Operações 10 e 11 |
| `backend/ClinicalStudyPB/src/test/java/UEPB/ESWII/ClinicalStudyPB/PainelEndpointTest.java` | Criar | Permissões e 404 |
| `backend/ClinicalStudyPB/src/test/java/UEPB/ESWII/ClinicalStudyPB/ContratoOpenApiSprint4Test.java` | Alterar | Reativar |
| `web/ClinicalStudyPB/src/api/schema-sprint4.d.ts` | Alterar | Regenerado |

## 4. Requisitos a implementar

Todos os requisitos (`RQ-T145-NN`) precisam estar atendidos para a task ser concluída.

- **RQ-T145-01** `GET /api/estudos/{estudoId}/painel` e `GET /api/paineis/estudos`, ambos `LEITURA`.
- **RQ-T145-02** 403 para administrador e participante, 401 sem token, 404 para estudo inexistente, 400 para id malformado.
- **RQ-T145-03** `@Tag(name = "Painel")` e `@Operation` com as definições resumidas.
- **RQ-T145-04** Conferir Swagger × YAML, remover `@Disabled`, gerar tipos.
- **RQ-T145-05** Medir o tempo das duas operações com o perfil `demo` (por exemplo `curl -w "%{time_total}"`, 10 repetições) e registrar mediana e máximo no Pull Request.

## 5. Contrato, regras e códigos de erro

Operações 10 e 11.

## 6. Testes

Endpoints e permissões; o restante na T148.

## 7. Critérios de aceite

- [ ] Pesquisador e coordenador leem; contrato verde; tempo registrado.

## 8. Cuidados e fora de escopo

- Sem cache nesta fatia: o painel reflete o estado atual.

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

> **T145 · Endpoints, autorização e Swagger do painel.** Primeiro exponha o painel do estudo e o resumo dos estudos, com leitura para pesquisador e coordenador (403 para os demais, 404 para estudo inexistente). Depois confira o Swagger contra o YAML. Por fim, meça o tempo de resposta com os dados de demonstração e registre no Pull Request.

### 9.5 Requisitos que atravessam esta task

- Regra 9.2: “O backend deve disponibilizar uma **API HTTP REST** compartilhada pela web e pelo mobile, documentada no padrão **OpenAPI**”.
- Entregas: “Persistência dos dados, com scripts de criação/migração do banco e dados fictícios para demonstração” e “Tratamento de erros e validações das regras de negócio”.
- Testes (seção 7): “Testes unitários para todas as regras de negócio relevantes”, “Testes de integração para os principais endpoints” e “Execução automatizada dos testes a cada pull request”.
- Regra 9.3: “A interface **web** atende aos perfis de pesquisador, coordenador e administrador.”
- **Definition of Done** (texto completo na [seção 11 das convenções](00-convencoes-e-contrato-compartilhado.md#11-definition-of-done)): critérios de aceite demonstráveis com dados fictícios; revisão, CI e Pull Request; testes automatizados e 80% de cobertura do negócio no backend; erros com mensagens claras; Swagger e YAML atualizados; dados de demonstração, documentação, capturas e nota de bastão no repositório; RF01 a RF18 continuam funcionando.

---
Parte dos documentos de design da Sprint 4: [índice](README.md) · [convenções e contrato](00-convencoes-e-contrato-compartilhado.md).
