# T153 · Endpoints, autorização e Swagger do relatório

> **Sprint 4** · PBI-30 · RF22 Relatório de recrutamento e acompanhamento  
> **Responsável:** I3 · **Estimativa:** 2h · **Prazo:** Qui 03/12  
> **Depende de:** T151, T152  
> **Situação:** ⬜ A implementar (este documento é o guia para quem for fazer)

> Antes de começar, leia as [convenções e o contrato compartilhado da Sprint 4](00-convencoes-e-contrato-compartilhado.md): ajustes ao planejamento, endpoints, DTOs, códigos de erro, tabelas e dados de demonstração valem para esta task. A origem dos requisitos está na [seção 9](#9-origem-dos-requisitos-especificação-e-planejamento).

## 1. Objetivo

Expor o relatório em JSON e as duas exportações, com autorização e Swagger.

## 2. Contexto: o que já existe e deve ser reaproveitado

- `Permissoes.LEITURA`; `Autenticacao.idDe(jwt)` para `geradoPor`; CORS em `SecurityConfig`.

## 3. Onde implementar

| Arquivo | Ação | Responsabilidade |
|---|---|---|
| `backend/ClinicalStudyPB/src/main/java/UEPB/ESWII/ClinicalStudyPB/controller/RelatorioController.java` | Criar | Operações 12 a 14 |
| `backend/ClinicalStudyPB/src/main/java/UEPB/ESWII/ClinicalStudyPB/security/SecurityConfig.java` | Alterar | Expor `Content-Disposition` no CORS |
| `backend/ClinicalStudyPB/src/test/java/UEPB/ESWII/ClinicalStudyPB/RelatorioEndpointTest.java` | Criar | Status e cabeçalhos |
| `web/ClinicalStudyPB/src/api/schema-sprint4.d.ts` | Alterar | Regenerado |

## 4. Requisitos a implementar

Todos os requisitos (`RQ-T153-NN`) precisam estar atendidos para a task ser concluída.

- **RQ-T153-01** Três `GET` com `LEITURA`; `de` e `ate` opcionais em `aaaa-mm-dd`.
- **RQ-T153-02** CSV com `Content-Type: text/csv; charset=UTF-8` e `Content-Disposition: attachment; filename="..."`.
- **RQ-T153-03** 400 para data malformada ou período invertido, 403, 404 e 401 no formato único (JSON, mesmo nas rotas CSV).
- **RQ-T153-04** `@Operation` e `produces` corretos no Swagger; conferir contra o YAML e remover `@Disabled`.
- **RQ-T153-05** Expor `Content-Disposition` para a web ler o nome do arquivo.

## 5. Contrato, regras e códigos de erro

Operações 12 a 14.

## 6. Testes

Permissões, cabeçalhos, corpo com BOM e erros.

## 7. Critérios de aceite

- [ ] Os três endpoints respondem como o contrato; Swagger conferido.

## 8. Cuidados e fora de escopo

- `CorsIntegrationTest` continua verde.

## 9. Origem dos requisitos (especificação e planejamento)

Texto **sem paráfrase** da especificação e do planejamento. Onde ele diverge dos [ajustes da seção 1 das convenções](00-convencoes-e-contrato-compartilhado.md#1-ajustes-em-relação-ao-planejamento-e-por-quê), valem as convenções e este documento.

### 9.1 Requisito da especificação

- **RF22** (Módulo F, Monitoramento; interface Web): “Gerar relatório simplificado de recrutamento e acompanhamento por estudo.”

### 9.2 Critério de aceite do PBI (planejamento)

> pesquisador e coordenador geram, para um estudo e um período opcional, o relatório de recrutamento e de acompanhamento em tela, exportam em CSV e imprimem; os números conferem com o painel; o relatório mostra quem gerou e quando.

### 9.3 Regras de negócio do planejamento que afetam esta task

| # | Regra proposta | Tasks afetadas |
|---|---|---|
| 8 | Erros seguem o formato único da Sprint 2 (400, 403, 404 e 409, sempre com o código da regra). As migrations usam as faixas V4_26_\* a V4_32_\*, todas acima das faixas V3 da Sprint 3 | T122 e todas as de backend |

### 9.4 Texto original da task no planejamento

> **T153 · Endpoints, autorização e Swagger do relatório.** Primeiro exponha o relatório em JSON e as duas exportações, com leitura para pesquisador e coordenador (403 para os demais, 404 para estudo inexistente, 400 para período inválido). Depois documente no Swagger os tipos de resposta. Por fim, confira o Swagger contra o YAML.

### 9.5 Requisitos que atravessam esta task

- Regra 9.2: “O backend deve disponibilizar uma **API HTTP REST** compartilhada pela web e pelo mobile, documentada no padrão **OpenAPI**”.
- Entregas: “Persistência dos dados, com scripts de criação/migração do banco e dados fictícios para demonstração” e “Tratamento de erros e validações das regras de negócio”.
- Testes (seção 7): “Testes unitários para todas as regras de negócio relevantes”, “Testes de integração para os principais endpoints” e “Execução automatizada dos testes a cada pull request”.
- Regra 9.3: “A interface **web** atende aos perfis de pesquisador, coordenador e administrador.”
- **Definition of Done** (texto completo na [seção 11 das convenções](00-convencoes-e-contrato-compartilhado.md#11-definition-of-done)): critérios de aceite demonstráveis com dados fictícios; revisão, CI e Pull Request; testes automatizados e 80% de cobertura do negócio no backend; erros com mensagens claras; Swagger e YAML atualizados; dados de demonstração, documentação, capturas e nota de bastão no repositório; RF01 a RF18 continuam funcionando.

---
Parte dos documentos de design da Sprint 4: [índice](README.md) · [convenções e contrato](00-convencoes-e-contrato-compartilhado.md).
