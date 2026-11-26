# T167 · README principal atualizado

> **Sprint 4** · PBI-32 · Planning, acompanhamento e entrega da Release 2  
> **Responsável:** I2 · **Estimativa:** 3h · **Prazo:** Dom 06/12  
> **Depende de:** T163, T166  
> **Situação:** ⬜ A implementar (este documento é o guia para quem for fazer)

> Antes de começar, leia as [convenções e o contrato compartilhado da Sprint 4](00-convencoes-e-contrato-compartilhado.md): ajustes ao planejamento, endpoints, DTOs, códigos de erro, tabelas e dados de demonstração valem para esta task. A origem dos requisitos está na [seção 9](#9-origem-dos-requisitos-especificação-e-planejamento).

## 1. Objetivo

Atualizar o README principal com as sete seções exigidas e os requisitos RF12 a RF22.

## 2. Contexto: o que já existe e deve ser reaproveitado

- `README.md` atual; texto de instalação da T161; relatório da T163; capturas em `docs/imagens/`.

## 3. Onde implementar

| Arquivo | Ação | Responsabilidade |
|---|---|---|
| `README.md` | Alterar | Sete seções |

## 4. Requisitos a implementar

Todos os requisitos (`RQ-T167-NN`) precisam estar atendidos para a task ser concluída.

- **RQ-T167-01** Apresentação com perfis, funcionalidades e integrantes (substituir os "a definir" pelos nomes confirmados com a equipe).
- **RQ-T167-02** Arquitetura com links ou diagramas da T166.
- **RQ-T167-03** Funcionalidades: RF01 a RF22 por código, com comportamento e capturas web e mobile de sucesso e de erro.
- **RQ-T167-04** Stack com as versões reais de `pom.xml` e dos `package.json`.
- **RQ-T167-05** Instalação e execução com o texto revisado da T161 e credenciais fictícias por perfil.
- **RQ-T167-06** Testes e cobertura com os números da T163.
- **RQ-T167-07** API: Swagger UI e os contratos das quatro sprints.
- **RQ-T167-08** Links para as três releases (a 3 marcada como futura).
- **RQ-T167-09** Conferir todos os links relativos e imagens.
- **RQ-T167-10** Atualizar estrutura do repositório e tabela de situação dos requisitos.

## 5. Contrato, regras e códigos de erro

Entregas, "README principal — seções obrigatórias".

## 6. Testes

Verificação de links e visualização no GitHub.

## 7. Critérios de aceite

- [ ] Sete seções completas, sem link quebrado.

## 8. Cuidados e fora de escopo

- Requisito sem evidência fica como parcial, com justificativa.

## 9. Origem dos requisitos (especificação e planejamento)

Texto **sem paráfrase** da especificação e do planejamento. Onde ele diverge dos [ajustes da seção 1 das convenções](00-convencoes-e-contrato-compartilhado.md#1-ajustes-em-relação-ao-planejamento-e-por-quê), valem as convenções e este documento.

### 9.1 Requisito da especificação

- Entregas (seção 10): “Cada release é entregue pelo repositório no GitHub, com código-fonte e documentação daquela versão.”
- Identificação da entrega: “Criar a tag `release1`, `release2` ou `release3` apontando para a versão entregue”, “Criar uma GitHub Release associada à tag, com resumo e link para o README da entrega” e “Apresentar o sistema funcionando, demonstrando requisitos, interfaces e testes”.

### 9.2 Critério de aceite do PBI (planejamento)

> a Sprint foi planejada e acompanhada no Azure Boards; README principal e README da Release 2 estão completos, com o processo das Sprints 3 e 4; os diagramas renderizam no GitHub; a tag release2 e a GitHub Release existem; a apresentação foi ensaiada.

### 9.3 Regras de negócio do planejamento que afetam esta task

Nenhuma das nove regras cita esta task diretamente.

### 9.4 Texto original da task no planejamento

> **T167 · README principal atualizado.** Primeiro atualize as sete seções exigidas com RF12 a RF22, usando as capturas de sucesso e de erro que cada dono deixou na sua última task. Depois atualize o stack com as versões, a instalação com o texto revisado da T161 e a seção de testes com o relatório da T163. Por fim, confira os links para as três releases e o Swagger UI.

### 9.5 Requisitos que atravessam esta task

- README principal: sete seções obrigatórias (apresentação, arquitetura, funcionalidades, stack, instalação, testes e API) e “links para as três releases”.
- README de cada release, seção **Processo de desenvolvimento**, para cada sprint: backlog e tarefas, organização da equipe, burndown e revisão.
- Identificação: “A avaliação usa a versão identificada pela tag.”
- **Definition of Done** (texto completo na [seção 11 das convenções](00-convencoes-e-contrato-compartilhado.md#11-definition-of-done)): critérios de aceite demonstráveis com dados fictícios; revisão, CI e Pull Request; testes automatizados e 80% de cobertura do negócio no backend; erros com mensagens claras; Swagger e YAML atualizados; dados de demonstração, documentação, capturas e nota de bastão no repositório; RF01 a RF18 continuam funcionando.

---
Parte dos documentos de design da Sprint 4: [índice](README.md) · [convenções e contrato](00-convencoes-e-contrato-compartilhado.md).
