# T150 · Conteúdo e contrato do relatório

> **Sprint 4** · PBI-30 · RF22 Relatório de recrutamento e acompanhamento  
> **Responsável:** I3 · **Estimativa:** 2h · **Prazo:** Qua 02/12  
> **Depende de:** T149  
> **Situação:** ⬜ A implementar (este documento é o guia para quem for fazer)

> Antes de começar, leia as [convenções e o contrato compartilhado da Sprint 4](00-convencoes-e-contrato-compartilhado.md): ajustes ao planejamento, endpoints, DTOs, códigos de erro, tabelas e dados de demonstração valem para esta task. A origem dos requisitos está na [seção 9](#9-origem-dos-requisitos-especificação-e-planejamento).

## 1. Objetivo

Definir o conteúdo do relatório, validar com o PO e publicar o contrato das três operações.

## 2. Contexto: o que já existe e deve ser reaproveitado

- Painel (T143/T144) para os totais; taxa de comparecimento do app (T118): realizadas ÷ (realizadas + ausentes).
- `Estudo` não tem código; `Candidato` ainda não tem data de cadastro.

## 3. Onde implementar

| Arquivo | Ação | Responsabilidade |
|---|---|---|
| `docs/sprint4/conteudo-do-relatorio.md` | Criar | Seções, fórmulas, período e colunas do CSV |
| `docs/api/openapi-sprint4.yaml` | Alterar | Operações 12 a 14 |
| `backend/ClinicalStudyPB/src/main/java/UEPB/ESWII/ClinicalStudyPB/controller/dto/RelatorioEstudoResponse.java` | Criar | DTO |
| `backend/ClinicalStudyPB/src/test/java/UEPB/ESWII/ClinicalStudyPB/ContratoOpenApiSprint4Test.java` | Alterar | 14 operações |

## 4. Requisitos a implementar

Todos os requisitos (`RQ-T150-NN`) precisam estar atendidos para a task ser concluída.

- **RQ-T150-01** Cabeçalho: estudo, estado, responsável, datas, período, quem gerou, quando e o aviso de dados fictícios.
- **RQ-T150-02** Recrutamento: cadastrados, avaliados, elegíveis, com consentimento e incluídos (pela **mesma fórmula por situação do funil do painel**, T146, para os números não divergirem) e os critérios que causaram inelegibilidade, em ordem decrescente de quantidade.
- **RQ-T150-03** Acompanhamento: por visita prevista (na ordem), realizadas, ausentes, canceladas, pendentes, atrasadas e não agendadas (ajuste 12), totais, taxa de comparecimento, retiradas com motivo e solicitante, e pedidos por tipo e situação.
- **RQ-T150-04** Período (`de`, `ate`, inclusivos e opcionais): cadastrados, avaliados, elegíveis, consentimento e critérios pela data de cadastro do candidato; incluídos pela data de inclusão; visitas agendadas pela data agendada; não agendadas pela data sugerida (inclusão + dia previsto); retiradas pela data da retirada; pedidos pela data de criação.
- **RQ-T150-05** Candidato antigo sem data de cadastro entra só no relatório sem período.
- **RQ-T150-06** Colunas dos dois CSV definidas no documento: recrutamento (`seção; item; quantidade`) e acompanhamento (`visita; realizadas; ausentes; canceladas; pendentes; atrasadas; não agendadas`, seguidas das retiradas).
- **RQ-T150-07** Nenhum nome de participante além do código no relatório e no CSV.
- **RQ-T150-08** Validar o conteúdo com o PO e registrar a resposta no documento.
- **RQ-T150-09** Contrato: JSON e dois CSV (`text/csv`), com 400 `PARAMETRO_INVALIDO`, 401, 403 e 404; atualizar a contagem (14) com `@Disabled("T153")`.

## 5. Contrato, regras e códigos de erro

Operações 12 a 14; DTO da seção 3.1.

## 6. Testes

Teste de contrato dos schemas.

## 7. Critérios de aceite

- [ ] Conteúdo aprovado, com fórmulas e regra de período escritas.

## 8. Cuidados e fora de escopo

- O funil com período pode não ser decrescente (incluídos usa outra data): explicar isso no documento e na tela.

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

> **T150 · Conteúdo e contrato do relatório.** Primeiro defina as seções: cabeçalho (estudo, estado, responsável, datas, período, quem gerou e quando); recrutamento (funil de cadastrados, avaliados, elegíveis, com consentimento e incluídos, e os critérios que mais causaram inelegibilidade); acompanhamento (por visita prevista, as realizadas, ausentes, canceladas e pendentes, a taxa de comparecimento, as retiradas com motivo e quem solicitou, e os pedidos do participante). Depois descreva no contrato a versão em JSON e as duas exportações CSV, recrutamento e acompanhamento. Por fim, valide o conteúdo com o PO.

### 9.5 Requisitos que atravessam esta task

- Regra 9.2: “O backend deve disponibilizar uma **API HTTP REST** compartilhada pela web e pelo mobile, documentada no padrão **OpenAPI**”.
- Entregas: “Persistência dos dados, com scripts de criação/migração do banco e dados fictícios para demonstração” e “Tratamento de erros e validações das regras de negócio”.
- Testes (seção 7): “Testes unitários para todas as regras de negócio relevantes”, “Testes de integração para os principais endpoints” e “Execução automatizada dos testes a cada pull request”.
- Regra 9.3: “A interface **web** atende aos perfis de pesquisador, coordenador e administrador.”
- **Definition of Done** (texto completo na [seção 11 das convenções](00-convencoes-e-contrato-compartilhado.md#11-definition-of-done)): critérios de aceite demonstráveis com dados fictícios; revisão, CI e Pull Request; testes automatizados e 80% de cobertura do negócio no backend; erros com mensagens claras; Swagger e YAML atualizados; dados de demonstração, documentação, capturas e nota de bastão no repositório; RF01 a RF18 continuam funcionando.

---
Parte dos documentos de design da Sprint 4: [índice](README.md) · [convenções e contrato](00-convencoes-e-contrato-compartilhado.md).
