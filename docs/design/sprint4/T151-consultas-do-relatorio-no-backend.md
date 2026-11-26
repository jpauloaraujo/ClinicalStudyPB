# T151 · Consultas do relatório no backend

> **Sprint 4** · PBI-30 · RF22 Relatório de recrutamento e acompanhamento  
> **Responsável:** I3 · **Estimativa:** 4h · **Prazo:** Qua 02/12  
> **Depende de:** T144, T150  
> **Situação:** ⬜ A implementar (este documento é o guia para quem for fazer)

> Antes de começar, leia as [convenções e o contrato compartilhado da Sprint 4](00-convencoes-e-contrato-compartilhado.md): ajustes ao planejamento, endpoints, DTOs, códigos de erro, tabelas e dados de demonstração valem para esta task. A origem dos requisitos está na [seção 9](#9-origem-dos-requisitos-especificação-e-planejamento).

## 1. Objetivo

Implementar as consultas do relatório reutilizando o painel, criando a data de cadastro do candidato.

## 2. Contexto: o que já existe e deve ser reaproveitado

- `PainelService` (T144).
- `AvaliacaoElegibilidade` (`vigente`, `resultado`, `respostas`), `RegraElegibilidade` (inclusão determinante não atendida; exclusão que se aplica), `Consentimento`, `RetiradaParticipacao`.
- `candidatos` (`V4`) não tem `criado_em`.

## 3. Onde implementar

| Arquivo | Ação | Responsabilidade |
|---|---|---|
| `backend/ClinicalStudyPB/src/main/resources/db/migration/V7_30_1__candidate_registration_date.sql` | Criar | Coluna e preenchimento |
| `backend/ClinicalStudyPB/src/main/java/UEPB/ESWII/ClinicalStudyPB/domain/model/Candidato.java` | Alterar | `criadoEm` |
| `backend/ClinicalStudyPB/src/main/java/UEPB/ESWII/ClinicalStudyPB/service/CandidatoService.java` | Alterar | Preencher com o `Clock` |
| `backend/ClinicalStudyPB/src/main/resources/db/demo/R__demo_28_pedidos_retirada.sql` | Alterar | Data de cadastro dos candidatos de demonstração |
| `backend/ClinicalStudyPB/src/main/java/UEPB/ESWII/ClinicalStudyPB/service/RelatorioService.java` | Criar | Montagem do relatório |
| `backend/ClinicalStudyPB/src/main/java/UEPB/ESWII/ClinicalStudyPB/repository/` | Alterar | Consultas novas |
| `backend/ClinicalStudyPB/src/test/java/UEPB/ESWII/ClinicalStudyPB/RelatorioServiceTest.java` | Criar | Valores conhecidos |
| `backend/ClinicalStudyPB/src/test/java/UEPB/ESWII/ClinicalStudyPB/MigracaoSeguraTest.java` | Alterar | `V7_30_1` |

## 4. Requisitos a implementar

Todos os requisitos (`RQ-T151-NN`) precisam estar atendidos para a task ser concluída.

- **RQ-T151-01** Migration: `criado_em TIMESTAMP` nula; preencher os existentes com a menor data entre a avaliação mais antiga, o consentimento e a inclusão; sem nenhuma, permanece nula. SQL válido no H2 e no PostgreSQL.
- **RQ-T151-02** Novos candidatos recebem `criadoEm` do `Clock`; a data não é editável pela API.
- **RQ-T151-03** Os candidatos de demonstração são carregados depois das migrations e ficariam sem data: acrescentar ao fim do `R__demo_28` um `UPDATE` idempotente (`WHERE criado_em IS NULL`, só ids `d6300000-...` e `d7300000-...`) com datas relativas a `CURRENT_DATE`, anteriores à avaliação de cada um. Não editar os demais scripts para isso.
- **RQ-T151-04** Totais sem período vêm do `PainelService`, para painel e relatório nunca divergirem; com período, usar as mesmas consultas com parâmetros de data, sem copiar fórmulas.
- **RQ-T151-05** As etapas do funil usam a contagem por `SituacaoCandidato` do `PainelService`, com a fórmula da T146; com período, a mesma fórmula sobre os candidatos cadastrados nele (incluídos: participações com data de inclusão no período).
- **RQ-T151-06** Contagem por visita prevista com `GROUP BY`, incluindo visitas previstas sem nenhum agendamento.
- **RQ-T151-07** Critérios: considerar só a avaliação vigente `REPROVADO`; contar cada critério que reprovou pela mesma lógica da `RegraElegibilidade`.
- **RQ-T151-08** Retiradas ordenadas por data, com código do participante, motivo e solicitante.
- **RQ-T151-09** Taxa: `round(100 × realizadas / (realizadas + ausentes))`; sem nenhuma das duas, `null`; canceladas não contam.
- **RQ-T151-10** `de` posterior a `ate`: `PARAMETRO_INVALIDO`; estudo inexistente: 404.
- **RQ-T151-11** Relatório gerado na hora, sem armazenamento (regra 7), com `geradoPor` e `geradoEm`.
- **RQ-T151-12** Número de consultas independente do número de participantes.

## 5. Contrato, regras e códigos de erro

Documento da T150.

## 6. Testes

Cenário conhecido, período nas bordas, sem dados, taxa sem visitas, critérios com várias falhas, candidato sem data e migration sobre banco populado.

## 7. Critérios de aceite

- [ ] Totais iguais aos do painel sem período; migration segura.

## 8. Cuidados e fora de escopo

- Combinar com o dono do painel qualquer mudança no `PainelService`.
- Datas com hora: o `ate` inclui o dia inteiro.

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
| 8 | Erros seguem o formato único da Sprint 2 (400, 403, 404 e 409, sempre com o código da regra). As migrations usam as faixas V4_26_\* a V4_32_\*, todas acima das faixas V3 da Sprint 3 | T122 e todas as de backend |

### 9.4 Texto original da task no planejamento

> **T151 · Consultas do relatório no backend.** Primeiro reaproveite o serviço de leitura da T144 para os totais, para que painel e relatório nunca divirjam. Depois escreva as consultas novas: contagem por visita prevista, critérios de inelegibilidade a partir da avaliação vigente de cada candidato (RF09) e lista de retiradas. Em seguida aplique o filtro de período pelas datas de cadastro, inclusão e visita. Por fim, calcule a taxa de comparecimento como realizadas sobre realizadas mais ausentes, sem contar as canceladas, como no app (T118).

### 9.5 Requisitos que atravessam esta task

- Regra 9.2: “O backend deve disponibilizar uma **API HTTP REST** compartilhada pela web e pelo mobile, documentada no padrão **OpenAPI**”.
- Entregas: “Persistência dos dados, com scripts de criação/migração do banco e dados fictícios para demonstração” e “Tratamento de erros e validações das regras de negócio”.
- Testes (seção 7): “Testes unitários para todas as regras de negócio relevantes”, “Testes de integração para os principais endpoints” e “Execução automatizada dos testes a cada pull request”.
- Regra 9.3: “A interface **web** atende aos perfis de pesquisador, coordenador e administrador.”
- **Definition of Done** (texto completo na [seção 11 das convenções](00-convencoes-e-contrato-compartilhado.md#11-definition-of-done)): critérios de aceite demonstráveis com dados fictícios; revisão, CI e Pull Request; testes automatizados e 80% de cobertura do negócio no backend; erros com mensagens claras; Swagger e YAML atualizados; dados de demonstração, documentação, capturas e nota de bastão no repositório; RF01 a RF18 continuam funcionando.

---
Parte dos documentos de design da Sprint 4: [índice](README.md) · [convenções e contrato](00-convencoes-e-contrato-compartilhado.md).
