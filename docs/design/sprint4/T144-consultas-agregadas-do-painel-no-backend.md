# T144 · Consultas agregadas do painel no backend

> **Sprint 4** · PBI-29 · RF21 Painel do estudo  
> **Responsável:** I6 · **Estimativa:** 4h · **Prazo:** Seg 30/11  
> **Depende de:** T143  
> **Situação:** ⬜ A implementar (este documento é o guia para quem for fazer)

> Antes de começar, leia as [convenções e o contrato compartilhado da Sprint 4](00-convencoes-e-contrato-compartilhado.md): ajustes ao planejamento, endpoints, DTOs, códigos de erro, tabelas e dados de demonstração valem para esta task. A origem dos requisitos está na [seção 9](#9-origem-dos-requisitos-especificação-e-planejamento).

## 1. Objetivo

Implementar o serviço de leitura do painel com contagens no banco, reutilizável pelo relatório.

## 2. Contexto: o que já existe e deve ser reaproveitado

- `VisitaAgendadaRepository.contarPorSituacao` e `contarPorParticipacaoNoEstudo` são exemplos de projeção com `GROUP BY`.
- Repositórios: `CandidatoRepository`, `ParticipacaoRepository`, `VisitaPlanejadaRepository.countByEstudoId`, `SolicitacaoParticipanteRepository`.

## 3. Onde implementar

| Arquivo | Ação | Responsabilidade |
|---|---|---|
| `backend/ClinicalStudyPB/src/main/java/UEPB/ESWII/ClinicalStudyPB/service/PainelService.java` | Criar | `doEstudo` e `resumoDosEstudos` |
| `backend/ClinicalStudyPB/src/main/java/UEPB/ESWII/ClinicalStudyPB/repository/CandidatoRepository.java` | Alterar | Contagem por situação |
| `backend/ClinicalStudyPB/src/main/java/UEPB/ESWII/ClinicalStudyPB/repository/ParticipacaoRepository.java` | Alterar | Contagem por situação e por estudo |
| `backend/ClinicalStudyPB/src/main/java/UEPB/ESWII/ClinicalStudyPB/repository/VisitaAgendadaRepository.java` | Alterar | Contagens do painel |
| `backend/ClinicalStudyPB/src/main/java/UEPB/ESWII/ClinicalStudyPB/repository/SolicitacaoParticipanteRepository.java` | Alterar | Pendentes por tipo e estudo |
| `backend/ClinicalStudyPB/src/test/java/UEPB/ESWII/ClinicalStudyPB/PainelServiceTest.java` | Criar | Valores conhecidos |

## 4. Requisitos a implementar

Todos os requisitos (`RQ-T144-NN`) precisam estar atendidos para a task ser concluída.

- **RQ-T144-01** Uma consulta com `GROUP BY` por bloco (candidatos, participações, visitas, pedidos), sem carregar entidades.
- **RQ-T144-02** A consulta de visitas separa pendentes de atrasadas com `CASE` sobre `agora` e filtra participação ativa só onde a definição pede.
- **RQ-T144-03** `naoAgendadas = ativos × visitasPrevistas − agendamentos (de qualquer situação) de participantes ativos`, nunca negativo.
- **RQ-T144-04** `resumoDosEstudos` usa consultas agrupadas por estudo (número fixo, independente da quantidade de estudos), ordenadas por `visitasAtrasadas + pedidosPendentes` decrescente e depois título.
- **RQ-T144-05** Estudo sem dados devolve zeros; estudo inexistente, 404.
- **RQ-T144-06** `@Transactional(readOnly = true)` e sem dependência web além dos DTOs.
- **RQ-T144-07** Expor métodos reutilizáveis para o relatório (T151) sem duplicar fórmulas.
- **RQ-T144-08** Comparar cada número com contagem manual nos dados de demonstração e colar a tabela no Pull Request.

## 5. Contrato, regras e códigos de erro

Definições da T143.

## 6. Testes

Cenário com valores conhecidos, incluindo retirado, atrasada, não agendada e confirmação; fronteira "exatamente agora" e 7º dia; contagem de consultas.

## 7. Critérios de aceite

- [ ] Números iguais aos do documento de indicadores e aos esperados na demonstração.
- [ ] Consultas em número fixo.

## 8. Cuidados e fora de escopo

- Testar o JPQL com `CASE` no H2 e no PostgreSQL.
- Participante retirado fora das pendências (regra 6).

## 9. Origem dos requisitos (especificação e planejamento)

Texto **sem paráfrase** da especificação e do planejamento. Onde ele diverge dos [ajustes da seção 1 das convenções](00-convencoes-e-contrato-compartilhado.md#1-ajustes-em-relação-ao-planejamento-e-por-quê), valem as convenções e este documento.

### 9.1 Requisito da especificação

- **RF21** (Módulo F, Monitoramento; interface Web): “Exibir painel com quantidade de candidatos, participantes incluídos e visitas realizadas ou pendentes.”

### 9.2 Critério de aceite do PBI (planejamento)

> pesquisador e coordenador abrem o painel de um estudo e veem a quantidade de candidatos por situação, participantes incluídos (ativos, retirados e vagas) e visitas realizadas, pendentes, atrasadas e não agendadas, com os números conferindo com os dados de demonstração; cada indicador leva à lista correspondente.

### 9.3 Regras de negócio do planejamento que afetam esta task

| # | Regra proposta | Tasks afetadas |
|---|---|---|
| 6 | O painel (RF21) usa estas definições: visitas realizadas = REALIZADA; pendentes = AGENDADA com data futura; atrasadas = AGENDADA com data passada e sem desfecho; não agendadas = visitas previstas de participantes ativos sem agendamento. Participantes retirados não entram nas pendências. Pesquisador e coordenador veem o painel dos estudos que já podem ler | T143, T144, T146 |
| 8 | Erros seguem o formato único da Sprint 2 (400, 403, 404 e 409, sempre com o código da regra). As migrations usam as faixas V4_26_\* a V4_32_\*, todas acima das faixas V3 da Sprint 3 | T122 e todas as de backend |

### 9.4 Texto original da task no planejamento

> **T144 · Consultas agregadas do painel no backend.** Primeiro escreva as contagens com GROUP BY no banco, uma consulta por bloco (candidatos, participações, visitas, pedidos), sem carregar entidades na memória. Depois calcule as não agendadas cruzando visitas previstas e participantes ativos. Em seguida coloque tudo em um serviço de leitura reaproveitável, que o relatório do PBI-30 vai usar. Por fim, compare cada número com uma contagem manual nos dados de demonstração.

### 9.5 Requisitos que atravessam esta task

- Regra 9.2: “O backend deve disponibilizar uma **API HTTP REST** compartilhada pela web e pelo mobile, documentada no padrão **OpenAPI**”.
- Entregas: “Persistência dos dados, com scripts de criação/migração do banco e dados fictícios para demonstração” e “Tratamento de erros e validações das regras de negócio”.
- Testes (seção 7): “Testes unitários para todas as regras de negócio relevantes”, “Testes de integração para os principais endpoints” e “Execução automatizada dos testes a cada pull request”.
- Regra 9.3: “A interface **web** atende aos perfis de pesquisador, coordenador e administrador.”
- **Definition of Done** (texto completo na [seção 11 das convenções](00-convencoes-e-contrato-compartilhado.md#11-definition-of-done)): critérios de aceite demonstráveis com dados fictícios; revisão, CI e Pull Request; testes automatizados e 80% de cobertura do negócio no backend; erros com mensagens claras; Swagger e YAML atualizados; dados de demonstração, documentação, capturas e nota de bastão no repositório; RF01 a RF18 continuam funcionando.

---
Parte dos documentos de design da Sprint 4: [índice](README.md) · [convenções e contrato](00-convencoes-e-contrato-compartilhado.md).
