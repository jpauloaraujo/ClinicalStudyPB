# T156 · E2E, capturas e passagem de bastão

> **Sprint 4** · PBI-30 · RF22 Relatório de recrutamento e acompanhamento  
> **Responsável:** I3 · **Estimativa:** 2h · **Prazo:** Qui 03/12  
> **Depende de:** T154  
> **Situação:** ⬜ A implementar (este documento é o guia para quem for fazer)

> Antes de começar, leia as [convenções e o contrato compartilhado da Sprint 4](00-convencoes-e-contrato-compartilhado.md): ajustes ao planejamento, endpoints, DTOs, códigos de erro, tabelas e dados de demonstração valem para esta task. A origem dos requisitos está na [seção 9](#9-origem-dos-requisitos-especificação-e-planejamento).

## 1. Objetivo

E2E do relatório, capturas, README e bastão para I1, com o congelamento de funcionalidades.

## 2. Contexto: o que já existe e deve ser reaproveitado

- Playwright com `page.waitForEvent('download')`.

## 3. Onde implementar

| Arquivo | Ação | Responsabilidade |
|---|---|---|
| `web/ClinicalStudyPB/e2e/tests/relatorio.spec.ts` | Criar | Gerar, filtrar e baixar |
| `web/ClinicalStudyPB/e2e/pages/RelatorioPage.ts` | Criar | Page object |
| `docs/imagens/sprint4/` | Alterar | `rf22-*` |
| `README.md` | Alterar | RF22 |
| `docs/sprint4/bastao-pbi-30.md` | Criar | Nota para I1 |

## 4. Requisitos a implementar

Todos os requisitos (`RQ-T156-NN`) precisam estar atendidos para a task ser concluída.

- **RQ-T156-01** Partida pela API, porque o CI não carrega o perfil `demo` (ajuste 16); abrir pelo painel, gerar, aplicar período, baixar um CSV e conferir nome, BOM e uma linha de dados.
- **RQ-T156-02** Erro: período invertido mostra a mensagem e não chama a API.
- **RQ-T156-03** Capturas `rf22-relatorio`, `rf22-csv`, `rf22-impressao` (usar `page.emulateMedia({ media: 'print' })`) e `rf22-erro-periodo`; a do CSV aberto é manual.
- **RQ-T156-04** Rodar contra o perfil `demo` e conferir os números com o painel.
- **RQ-T156-05** README com RF22.
- **RQ-T156-06** Conferir que RF19 a RF22 estão na `develop` e levados à `main`; avisar o congelamento de funcionalidades.
- **RQ-T156-07** Bastão para I1: requisitos entregues, pendências e dados de demonstração.

## 5. Contrato, regras e códigos de erro

Operações 12 a 14.

## 6. Testes

`npm run test:e2e -- relatorio`.

## 7. Critérios de aceite

- [ ] E2E verde; RF22 documentado; congelamento comunicado.

## 8. Cuidados e fora de escopo

- Não afirmar que um requisito está concluído sem CI verde.

## 9. Origem dos requisitos (especificação e planejamento)

Texto **sem paráfrase** da especificação e do planejamento. Onde ele diverge dos [ajustes da seção 1 das convenções](00-convencoes-e-contrato-compartilhado.md#1-ajustes-em-relação-ao-planejamento-e-por-quê), valem as convenções e este documento.

### 9.1 Requisito da especificação

- **RF22** (Módulo F, Monitoramento; interface Web): “Gerar relatório simplificado de recrutamento e acompanhamento por estudo.”

### 9.2 Critério de aceite do PBI (planejamento)

> pesquisador e coordenador geram, para um estudo e um período opcional, o relatório de recrutamento e de acompanhamento em tela, exportam em CSV e imprimem; os números conferem com o painel; o relatório mostra quem gerou e quando.

### 9.3 Regras de negócio do planejamento que afetam esta task

Nenhuma das nove regras cita esta task diretamente.

### 9.4 Texto original da task no planejamento

> **T156 · E2E, capturas e passagem de bastão.** Primeiro escreva o cenário Playwright que gera o relatório do estudo de demonstração, aplica um período e baixa um CSV. Depois salve as capturas da tela, do CSV aberto e da versão impressa, e atualize o README com o RF22. Por fim, passe o bastão a I1: todos os requisitos da Release 2 estão na main e entra o congelamento de funcionalidades.

### 9.5 Requisitos que atravessam esta task

- Regra 9.2: “O backend deve disponibilizar uma **API HTTP REST** compartilhada pela web e pelo mobile, documentada no padrão **OpenAPI**”.
- Entregas: “Persistência dos dados, com scripts de criação/migração do banco e dados fictícios para demonstração” e “Tratamento de erros e validações das regras de negócio”.
- Testes (seção 7): “Testes unitários para todas as regras de negócio relevantes”, “Testes de integração para os principais endpoints” e “Execução automatizada dos testes a cada pull request”.
- Regra 9.3: “A interface **web** atende aos perfis de pesquisador, coordenador e administrador.”
- **Definition of Done** (texto completo na [seção 11 das convenções](00-convencoes-e-contrato-compartilhado.md#11-definition-of-done)): critérios de aceite demonstráveis com dados fictícios; revisão, CI e Pull Request; testes automatizados e 80% de cobertura do negócio no backend; erros com mensagens claras; Swagger e YAML atualizados; dados de demonstração, documentação, capturas e nota de bastão no repositório; RF01 a RF18 continuam funcionando.

---
Parte dos documentos de design da Sprint 4: [índice](README.md) · [convenções e contrato](00-convencoes-e-contrato-compartilhado.md).
