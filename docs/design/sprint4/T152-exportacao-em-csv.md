# T152 · Exportação em CSV

> **Sprint 4** · PBI-30 · RF22 Relatório de recrutamento e acompanhamento  
> **Responsável:** I3 · **Estimativa:** 3h · **Prazo:** Qua 02/12  
> **Depende de:** T151  
> **Situação:** ⬜ A implementar (este documento é o guia para quem for fazer)

> Antes de começar, leia as [convenções e o contrato compartilhado da Sprint 4](00-convencoes-e-contrato-compartilhado.md): ajustes ao planejamento, endpoints, DTOs, códigos de erro, tabelas e dados de demonstração valem para esta task. A origem dos requisitos está na [seção 9](#9-origem-dos-requisitos-especificação-e-planejamento).

## 1. Objetivo

Gerar os dois CSV compatíveis com o Excel em português.

## 2. Contexto: o que já existe e deve ser reaproveitado

- Dados do `RelatorioService`.

## 3. Onde implementar

| Arquivo | Ação | Responsabilidade |
|---|---|---|
| `backend/ClinicalStudyPB/src/main/java/UEPB/ESWII/ClinicalStudyPB/service/ExportadorCsv.java` | Criar | Escrita dos CSV |
| `backend/ClinicalStudyPB/src/test/java/UEPB/ESWII/ClinicalStudyPB/ExportadorCsvTest.java` | Criar | Bytes e colunas |

## 4. Requisitos a implementar

Todos os requisitos (`RQ-T152-NN`) precisam estar atendidos para a task ser concluída.

- **RQ-T152-01** UTF-8 com BOM (`EF BB BF`), separador `;`, fim de linha `\r\n`.
- **RQ-T152-02** Primeira linha: o aviso "Dados fictícios, gerados para fins acadêmicos."; depois estudo, período, quem gerou e quando; linha em branco; tabela.
- **RQ-T152-03** Cabeçalhos em português; datas `dd/mm/aaaa` e data e hora `dd/mm/aaaa HH:mm`.
- **RQ-T152-04** Escapar campos com `;`, aspas ou quebra de linha (aspas duplicadas).
- **RQ-T152-05** Proteger contra injeção de fórmula: campo de texto iniciado por `=`, `+`, `-`, `@`, tabulação ou retorno recebe `'` antes (motivos são texto livre).
- **RQ-T152-06** Nome do arquivo: `relatorio-<recrutamento|acompanhamento>-<8 primeiros caracteres do id>-<aaaa-mm-dd>.csv` (ajuste 11).
- **RQ-T152-07** Taxa nula aparece como "—".
- **RQ-T152-08** Sem biblioteca externa se a implementação própria cobrir o escape; sem estado.

## 5. Contrato, regras e códigos de erro

Colunas do documento da T150.

## 6. Testes

BOM, separador, escape, fórmula, acentos, datas, arquivo sem dados e nome do arquivo.

## 7. Critérios de aceite

- [ ] Os dois arquivos abrem no Excel em português com colunas e acentos corretos.

## 8. Cuidados e fora de escopo

- Conferir manualmente no Excel ou LibreOffice e guardar a captura para a T156.

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

> **T152 · Exportação em CSV.** Primeiro gere os dois CSV em UTF-8 com BOM e ponto e vírgula como separador, para abrir corretamente no Excel em português. Depois use cabeçalhos em português, datas no formato dd/mm/aaaa e o nome do arquivo com o código do estudo e a data. Por fim, inclua na primeira linha o aviso de dados fictícios.

### 9.5 Requisitos que atravessam esta task

- Regra 9.2: “O backend deve disponibilizar uma **API HTTP REST** compartilhada pela web e pelo mobile, documentada no padrão **OpenAPI**”.
- Entregas: “Persistência dos dados, com scripts de criação/migração do banco e dados fictícios para demonstração” e “Tratamento de erros e validações das regras de negócio”.
- Testes (seção 7): “Testes unitários para todas as regras de negócio relevantes”, “Testes de integração para os principais endpoints” e “Execução automatizada dos testes a cada pull request”.
- Regra 9.3: “A interface **web** atende aos perfis de pesquisador, coordenador e administrador.”
- **Definition of Done** (texto completo na [seção 11 das convenções](00-convencoes-e-contrato-compartilhado.md#11-definition-of-done)): critérios de aceite demonstráveis com dados fictícios; revisão, CI e Pull Request; testes automatizados e 80% de cobertura do negócio no backend; erros com mensagens claras; Swagger e YAML atualizados; dados de demonstração, documentação, capturas e nota de bastão no repositório; RF01 a RF18 continuam funcionando.

---
Parte dos documentos de design da Sprint 4: [índice](README.md) · [convenções e contrato](00-convencoes-e-contrato-compartilhado.md).
