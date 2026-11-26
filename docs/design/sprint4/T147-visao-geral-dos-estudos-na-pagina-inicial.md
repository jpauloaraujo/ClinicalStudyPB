# T147 · Visão geral dos estudos na página inicial

> **Sprint 4** · PBI-29 · RF21 Painel do estudo  
> **Responsável:** I6 · **Estimativa:** 3h · **Prazo:** Ter 01/12  
> **Depende de:** T145  
> **Situação:** ⬜ A implementar (este documento é o guia para quem for fazer)

> Antes de começar, leia as [convenções e o contrato compartilhado da Sprint 4](00-convencoes-e-contrato-compartilhado.md): ajustes ao planejamento, endpoints, DTOs, códigos de erro, tabelas e dados de demonstração valem para esta task. A origem dos requisitos está na [seção 9](#9-origem-dos-requisitos-especificação-e-planejamento).

## 1. Objetivo

Mostrar na página inicial a visão geral dos estudos, ordenada por pendências.

## 2. Contexto: o que já existe e deve ser reaproveitado

- `InicioPage` hoje tem só atalhos e serve também o administrador.
- Primeiro item da ordem de corte.

## 3. Onde implementar

| Arquivo | Ação | Responsabilidade |
|---|---|---|
| `web/ClinicalStudyPB/src/pages/InicioPage.tsx` | Alterar | Incluir a visão geral |
| `web/ClinicalStudyPB/src/pages/inicio/VisaoGeralEstudos.tsx` | Criar | Tabela |
| `web/ClinicalStudyPB/src/api/painel.ts` | Alterar | `resumoDosEstudos` |
| `web/ClinicalStudyPB/src/pages/InicioPage.test.tsx` | Criar | Perfis e estados |

## 4. Requisitos a implementar

Todos os requisitos (`RQ-T147-NN`) precisam estar atendidos para a task ser concluída.

- **RQ-T147-01** Mostrar a tabela só para pesquisador e coordenador; o administrador não chama a API.
- **RQ-T147-02** Colunas: estudo, estado, incluídos/limite, visitas atrasadas e pedidos pendentes.
- **RQ-T147-03** Manter a ordem devolvida pelo servidor (atrasadas + pendentes, depois título).
- **RQ-T147-04** Cada linha leva ao painel (`/estudos/{id}/resumo`) por link com nome acessível.
- **RQ-T147-05** Estados de carregamento, vazio ("Nenhum estudo cadastrado.") e erro, sem esconder os atalhos.
- **RQ-T147-06** Destacar pendências com texto, não só cor.

## 5. Contrato, regras e códigos de erro

Operação 11.

## 6. Testes

Três perfis, ordem, links e estados.

## 7. Critérios de aceite

- [ ] A página inicial mostra o que precisa de atenção primeiro.

## 8. Cuidados e fora de escopo

- Se for cortada, registrar em T165 e nos problemas conhecidos; o endpoint pode permanecer.

## 9. Origem dos requisitos (especificação e planejamento)

Texto **sem paráfrase** da especificação e do planejamento. Onde ele diverge dos [ajustes da seção 1 das convenções](00-convencoes-e-contrato-compartilhado.md#1-ajustes-em-relação-ao-planejamento-e-por-quê), valem as convenções e este documento.

### 9.1 Requisito da especificação

- **RF21** (Módulo F, Monitoramento; interface Web): “Exibir painel com quantidade de candidatos, participantes incluídos e visitas realizadas ou pendentes.”

### 9.2 Critério de aceite do PBI (planejamento)

> pesquisador e coordenador abrem o painel de um estudo e veem a quantidade de candidatos por situação, participantes incluídos (ativos, retirados e vagas) e visitas realizadas, pendentes, atrasadas e não agendadas, com os números conferindo com os dados de demonstração; cada indicador leva à lista correspondente.

### 9.3 Regras de negócio do planejamento que afetam esta task

Nenhuma das nove regras cita esta task diretamente.

### 9.4 Texto original da task no planejamento

> **T147 · Visão geral dos estudos na página inicial.** Primeiro crie, na página inicial do pesquisador e do coordenador, a tabela dos estudos com estado, incluídos e limite, visitas atrasadas e pedidos pendentes. Depois ordene por pendências, para o que precisa de atenção aparecer primeiro. Por fim, ligue cada linha ao painel do estudo.

### 9.5 Requisitos que atravessam esta task

- Regra 9.2: “O backend deve disponibilizar uma **API HTTP REST** compartilhada pela web e pelo mobile, documentada no padrão **OpenAPI**”.
- Entregas: “Persistência dos dados, com scripts de criação/migração do banco e dados fictícios para demonstração” e “Tratamento de erros e validações das regras de negócio”.
- Testes (seção 7): “Testes unitários para todas as regras de negócio relevantes”, “Testes de integração para os principais endpoints” e “Execução automatizada dos testes a cada pull request”.
- Regra 9.3: “A interface **web** atende aos perfis de pesquisador, coordenador e administrador.”
- **Definition of Done** (texto completo na [seção 11 das convenções](00-convencoes-e-contrato-compartilhado.md#11-definition-of-done)): critérios de aceite demonstráveis com dados fictícios; revisão, CI e Pull Request; testes automatizados e 80% de cobertura do negócio no backend; erros com mensagens claras; Swagger e YAML atualizados; dados de demonstração, documentação, capturas e nota de bastão no repositório; RF01 a RF18 continuam funcionando.

---
Parte dos documentos de design da Sprint 4: [índice](README.md) · [convenções e contrato](00-convencoes-e-contrato-compartilhado.md).
