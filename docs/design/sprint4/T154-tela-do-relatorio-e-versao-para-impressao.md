# T154 · Tela do relatório e versão para impressão

> **Sprint 4** · PBI-30 · RF22 Relatório de recrutamento e acompanhamento  
> **Responsável:** I3 · **Estimativa:** 5h · **Prazo:** Qui 03/12  
> **Depende de:** T153  
> **Situação:** ⬜ A implementar (este documento é o guia para quem for fazer)

> Antes de começar, leia as [convenções e o contrato compartilhado da Sprint 4](00-convencoes-e-contrato-compartilhado.md): ajustes ao planejamento, endpoints, DTOs, códigos de erro, tabelas e dados de demonstração valem para esta task. A origem dos requisitos está na [seção 9](#9-origem-dos-requisitos-especificação-e-planejamento).

## 1. Objetivo

Página do relatório com filtro de período, tabelas, exportação CSV e versão para impressão.

## 2. Contexto: o que já existe e deve ser reaproveitado

- Painel (T146) é a porta de entrada; `api` (Axios) envia o token.
- A parte de impressão é item de corte.

## 3. Onde implementar

| Arquivo | Ação | Responsabilidade |
|---|---|---|
| `web/ClinicalStudyPB/src/api/relatorio.ts` | Criar | JSON e download |
| `web/ClinicalStudyPB/src/pages/estudos/detalhe/relatorio/RelatorioPage.tsx` | Criar | Tela |
| `web/ClinicalStudyPB/src/pages/estudos/detalhe/relatorio/relatorio.css` | Criar | Estilo e `@media print` |
| `web/ClinicalStudyPB/src/routes/router.tsx` | Alterar | Rota `relatorio` |
| `web/ClinicalStudyPB/src/pages/estudos/detalhe/painel/PainelEstudo.tsx` | Alterar | Botão "Gerar relatório" |

## 4. Requisitos a implementar

Todos os requisitos (`RQ-T154-NN`) precisam estar atendidos para a task ser concluída.

- **RQ-T154-01** Rota `/estudos/:estudoId/relatorio` para pesquisador e coordenador, aberta pelo painel (sem aba nova).
- **RQ-T154-02** Filtro "De" e "Até" com botão "Gerar", validação de período invertido no cliente e valores na URL.
- **RQ-T154-03** Cabeçalho, recrutamento, acompanhamento, retiradas e pedidos em tabelas com `caption`.
- **RQ-T154-04** Botões **Exportar CSV** (recrutamento e acompanhamento): baixar como `blob` pela API autenticada, usando o nome do `Content-Disposition`; liberar a URL do objeto; erro mostrado na tela.
- **RQ-T154-05** O download usa o mesmo período mostrado.
- **RQ-T154-06** Botão **Imprimir** (`window.print()`).
- **RQ-T154-07** `@media print`: esconder menu, cabeçalho, abas, filtros e botões; quebra de página entre seções; tabelas sem cortar linhas; aviso de dados fictícios visível.
- **RQ-T154-08** Mostrar quem gerou e quando.
- **RQ-T154-09** Estados de carregamento, erro com "Tentar de novo" e período sem dados.
- **RQ-T154-10** Texto curto explicando as datas usadas pelo período.

## 5. Contrato, regras e códigos de erro

Operações 12 a 14.

## 6. Testes

Básicos junto; completos na T155.

## 7. Critérios de aceite

- [ ] Relatório em tela, CSV e PDF pelo navegador com os mesmos números.

## 8. Cuidados e fora de escopo

- Nunca colocar o token na URL de download.

## 9. Origem dos requisitos (especificação e planejamento)

Texto **sem paráfrase** da especificação e do planejamento. Onde ele diverge dos [ajustes da seção 1 das convenções](00-convencoes-e-contrato-compartilhado.md#1-ajustes-em-relação-ao-planejamento-e-por-quê), valem as convenções e este documento.

### 9.1 Requisito da especificação

- **RF22** (Módulo F, Monitoramento; interface Web): “Gerar relatório simplificado de recrutamento e acompanhamento por estudo.”

### 9.2 Critério de aceite do PBI (planejamento)

> pesquisador e coordenador geram, para um estudo e um período opcional, o relatório de recrutamento e de acompanhamento em tela, exportam em CSV e imprimem; os números conferem com o painel; o relatório mostra quem gerou e quando.

### 9.3 Regras de negócio do planejamento que afetam esta task

| # | Regra proposta | Tasks afetadas |
|---|---|---|
| 7 | O relatório (RF22) é gerado na hora, por estudo e com período opcional, sem ser armazenado. Ele sai em tela, em CSV e em versão para impressão, mostra quem gerou e quando, e avisa que os dados são fictícios | T150, T151, T154 |

### 9.4 Texto original da task no planejamento

> **T154 · Tela do relatório e versão para impressão.** Primeiro crie a página do relatório, aberta pelo painel do estudo, com o filtro de período e as seções em tabelas. Depois acrescente os botões Exportar CSV e Imprimir. Em seguida escreva o estilo de impressão (@media print), sem menus e com quebras de página entre seções, para gerar PDF pelo navegador. Por fim, trate carregamento, erro e período sem dados.

### 9.5 Requisitos que atravessam esta task

- Regra 9.2: “O backend deve disponibilizar uma **API HTTP REST** compartilhada pela web e pelo mobile, documentada no padrão **OpenAPI**”.
- Entregas: “Persistência dos dados, com scripts de criação/migração do banco e dados fictícios para demonstração” e “Tratamento de erros e validações das regras de negócio”.
- Testes (seção 7): “Testes unitários para todas as regras de negócio relevantes”, “Testes de integração para os principais endpoints” e “Execução automatizada dos testes a cada pull request”.
- Regra 9.3: “A interface **web** atende aos perfis de pesquisador, coordenador e administrador.”
- **Definition of Done** (texto completo na [seção 11 das convenções](00-convencoes-e-contrato-compartilhado.md#11-definition-of-done)): critérios de aceite demonstráveis com dados fictícios; revisão, CI e Pull Request; testes automatizados e 80% de cobertura do negócio no backend; erros com mensagens claras; Swagger e YAML atualizados; dados de demonstração, documentação, capturas e nota de bastão no repositório; RF01 a RF18 continuam funcionando.

---
Parte dos documentos de design da Sprint 4: [índice](README.md) · [convenções e contrato](00-convencoes-e-contrato-compartilhado.md).
