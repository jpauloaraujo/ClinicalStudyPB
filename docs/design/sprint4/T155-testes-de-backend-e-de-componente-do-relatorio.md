# T155 · Testes de backend e de componente do relatório

> **Sprint 4** · PBI-30 · RF22 Relatório de recrutamento e acompanhamento  
> **Responsável:** I3 · **Estimativa:** 3h · **Prazo:** Qui 03/12  
> **Depende de:** T151, T152, T154  
> **Situação:** ⬜ A implementar (este documento é o guia para quem for fazer)

> Antes de começar, leia as [convenções e o contrato compartilhado da Sprint 4](00-convencoes-e-contrato-compartilhado.md): ajustes ao planejamento, endpoints, DTOs, códigos de erro, tabelas e dados de demonstração valem para esta task. A origem dos requisitos está na [seção 9](#9-origem-dos-requisitos-especificação-e-planejamento).

## 1. Objetivo

Cobrir consultas, período, taxa, igualdade com o painel, CSV e componentes.

## 2. Contexto: o que já existe e deve ser reaproveitado

- Código das T151 a T154.

## 3. Onde implementar

| Arquivo | Ação | Responsabilidade |
|---|---|---|
| `backend/ClinicalStudyPB/src/test/java/UEPB/ESWII/ClinicalStudyPB/RelatorioServiceTest.java` | Alterar | Matriz |
| `backend/ClinicalStudyPB/src/test/java/UEPB/ESWII/ClinicalStudyPB/RelatorioPainelConsistenciaTest.java` | Criar | Totais iguais |
| `backend/ClinicalStudyPB/src/test/java/UEPB/ESWII/ClinicalStudyPB/ExportadorCsvTest.java` | Alterar | Colunas e codificação |
| `backend/ClinicalStudyPB/src/test/java/UEPB/ESWII/ClinicalStudyPB/RelatorioEndpointTest.java` | Alterar | Status |
| `web/ClinicalStudyPB/src/pages/estudos/detalhe/relatorio/RelatorioPage.test.tsx` | Criar | Filtro e seções |

## 4. Requisitos a implementar

Todos os requisitos (`RQ-T155-NN`) precisam estar atendidos para a task ser concluída.

- **RQ-T155-01** Consultas com valores conhecidos; período nas bordas (dia anterior, próprio dia, dia seguinte).
- **RQ-T155-02** Taxa sem visitas (`null`) e só com canceladas.
- **RQ-T155-03** Teste de igualdade: para o mesmo cenário, cada total do relatório sem período é igual ao do painel (candidatos, participantes, visitas e pedidos pendentes).
- **RQ-T155-04** CSV: colunas, BOM, separador, escape e aviso.
- **RQ-T155-05** Endpoints: 200, 400, 401, 403 e 404; PostgreSQL no CI.
- **RQ-T155-06** Componentes: filtro, validação, seções, estados e chamada de download.
- **RQ-T155-07** Registrar a cobertura de `RelatorioService` e `ExportadorCsv`.

## 5. Contrato, regras e códigos de erro

Documento da T150.

## 6. Testes

Os itens acima.

## 7. Critérios de aceite

- [ ] Suítes verdes e consistência comprovada.

## 8. Cuidados e fora de escopo

- Criar o cenário de comparação com retirado, atrasada e não agendada, onde divergências aparecem.

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

> **T155 · Testes de backend e de componente do relatório.** Escreva os testes junto com o código. Primeiro teste as consultas com valores conhecidos, o filtro de período e a taxa de comparecimento sem visitas. Depois teste que os totais do relatório são iguais aos do painel e que o CSV tem as colunas e a codificação esperadas. Por fim, escreva os testes de componente do filtro e das seções.

### 9.5 Requisitos que atravessam esta task

- Regra 9.2: “O backend deve disponibilizar uma **API HTTP REST** compartilhada pela web e pelo mobile, documentada no padrão **OpenAPI**”.
- Entregas: “Persistência dos dados, com scripts de criação/migração do banco e dados fictícios para demonstração” e “Tratamento de erros e validações das regras de negócio”.
- Testes (seção 7): “Testes unitários para todas as regras de negócio relevantes”, “Testes de integração para os principais endpoints” e “Execução automatizada dos testes a cada pull request”.
- Regra 9.3: “A interface **web** atende aos perfis de pesquisador, coordenador e administrador.”
- **Definition of Done** (texto completo na [seção 11 das convenções](00-convencoes-e-contrato-compartilhado.md#11-definition-of-done)): critérios de aceite demonstráveis com dados fictícios; revisão, CI e Pull Request; testes automatizados e 80% de cobertura do negócio no backend; erros com mensagens claras; Swagger e YAML atualizados; dados de demonstração, documentação, capturas e nota de bastão no repositório; RF01 a RF18 continuam funcionando.

---
Parte dos documentos de design da Sprint 4: [índice](README.md) · [convenções e contrato](00-convencoes-e-contrato-compartilhado.md).
