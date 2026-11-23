# T92 · Testes de backend dos resultados

> **Sprint 3** · PBI-21 · RF14 Resultados fictícios das avaliações previstas  
> **Responsável:** I6 · **Estimativa:** 3h · **Prazo:** Dom 15/11  
> **Depende de:** T89, T90  
> **Situação:** ⬜ A implementar (este documento é o guia para quem for fazer)

> Antes de começar, leia as [convenções e o contrato compartilhado da Sprint 3](00-convencoes-e-contrato-compartilhado.md): nomes, endpoints, DTOs, códigos de erro, tabelas e dados de demonstração valem para esta task. A origem dos requisitos (especificação, critério de aceite do PBI, regras de negócio e o texto original da task) está na [seção 9](#9-origem-dos-requisitos-especificação-e-planejamento).

## 1. Objetivo

Provar cada regra do RF14, a edição, os pendentes e os endpoints com PostgreSQL real, mantendo a cobertura da fatia acima de 80%.

## 2. Contexto: o que já existe e deve ser reaproveitado

- Modelos: `ParticipacaoServiceTest` (Mockito), `AvaliacaoEndpointTest` (matriz de validação e de permissões por perfil) e `ParticipacaoEndpointTest`.
- `CenarioRecrutamento` com os métodos das T78 e T85 e `TokenDeTeste`; para ter uma visita `REALIZADA` pela API, o teste agenda em dia permitido (hoje) e registra a realizada.
- O estudo de teste precisa de visita prevista com atividades de categoria `AVALIACAO_CLINICA` e de outra categoria (por exemplo `EXAME_LABORATORIAL`).

## 3. Onde implementar

| Arquivo | Ação | Responsabilidade |
|---|---|---|
| `backend/ClinicalStudyPB/src/test/java/UEPB/ESWII/ClinicalStudyPB/ResultadoAtividadeServiceTest.java` | Criar | Regras isoladas, edição e pendentes (Mockito) |
| `backend/ClinicalStudyPB/src/test/java/UEPB/ESWII/ClinicalStudyPB/ResultadoRequestValidacaoTest.java` | Criar | Limites do DTO |
| `backend/ClinicalStudyPB/src/test/java/UEPB/ESWII/ClinicalStudyPB/ResultadoAtividadeEndpointTest.java` | Criar | 200, 201, 400, 403, 404 e 409 com tokens reais |
| `backend/ClinicalStudyPB/src/test/java/UEPB/ESWII/ClinicalStudyPB/support/CenarioRecrutamento.java` | Alterar | `visitaRealizada(...)` e `acaoDeAvaliacao(...)` |

## 4. Requisitos a implementar

Cada requisito tem um identificador (`RQ-T92-NN`) para a revisão do Pull Request e para a conferência no fim da task: todos precisam estar atendidos.

### Regras isoladas (`ResultadoAtividadeServiceTest`)

- **RQ-T92-01** Visita `AGENDADA`, `AUSENTE` e `CANCELADA`: `VISITA_NAO_REALIZADA`; visita `REALIZADA`: aceita.
- **RQ-T92-02** Atividade de outra categoria (`EXAME_LABORATORIAL`, `MEDICACAO`, `QUESTIONARIO`...): `ATIVIDADE_FORA_DA_CATEGORIA_AVALIACAO`; atividade de **outra visita prevista**: `ATIVIDADE_NAO_PERTENCE_A_VISITA`.
- **RQ-T92-03** Estudo **encerrado**: `ESTUDO_ENCERRADO` na edição e no primeiro registro; recrutamento e execução aceitam.
- **RQ-T92-04** Valor vazio, só com espaços e nulo: `VALOR_OBRIGATORIO`; valor de 200 caracteres aceito.
- **RQ-T92-05** A ordem das verificações (visita não realizada vence a categoria; a categoria vence o estudo encerrado, e este vence o valor vazio).
- **RQ-T92-06** Resultado **parcial**: salvar uma atividade não exige as outras; `registrados`, `pendentes` e `total` corretos a cada passo.
- **RQ-T92-07** Edição: atualiza `valor`, `unidade`, `observacao`, `alteradoPor` e `alteradoEm`, e **não** altera `registradoPor` nem `registradoEm`; o primeiro registro grava os dois pares iguais.
- **RQ-T92-08** `bloqueio` na consulta: `VISITA_NAO_REALIZADA`, `ESTUDO_ENCERRADO` e `null` (e, depois da T102, `PARTICIPANTE_RETIRADO`).

### Validação do DTO

- **RQ-T92-09** `valor` obrigatório, 1 a 200 caracteres; `unidade` até 30; `observacao` até 500 (valores-limite 200/201, 30/31 e 500/501).

### Endpoints com PostgreSQL real

- **RQ-T92-10** `PUT`: **201** no primeiro registro e **200** na edição, com o corpo completo; 400 (valor ausente, valor com 201 caracteres, JSON malformado); 403 para **coordenador**, administrador e participante; 401 sem token; 404 (visita e atividade inexistentes); 409 de cada regra.
- **RQ-T92-11** `GET`: 200 para pesquisador e coordenador; 403 para administrador e participante; atividades de outras categorias **não** aparecem na lista; `total`, `registrados` e `pendentes` corretos; 404 para visita inexistente.
- **RQ-T92-12** Depois de registrar tudo, `pendentes` é zero; uma visita `AGENDADA` devolve as atividades sem resultado e `bloqueio = VISITA_NAO_REALIZADA`.
- **RQ-T92-13** Rodar também no job `Backend (PostgreSQL)` do CI.

### Cobertura

- **RQ-T92-14** Conferir a cobertura de `ResultadoAtividadeService` (linhas e ramos) e registrar o resultado no Pull Request; meta 95% ou mais.
- **RQ-T92-15** Toda a suíte anterior continua passando.

## 6. Testes

- As classes da seção 3 e o `ContratoOpenApiSprint3Test` verde (9 operações).

## 7. Critérios de aceite

- [ ] Cada regra do RF14 tem teste de sucesso e de erro.
- [ ] A edição preserva o autor do primeiro registro e atualiza a última alteração.
- [ ] Os endpoints respondem 200, 201, 400, 403, 404 e 409 com tokens reais.
- [ ] A cobertura da fatia está acima de 80%.

## 8. Cuidados e fora de escopo

- Não depender da data do dia.
- O coordenador recebe **403** (permissão), nunca 409, na escrita.

## 9. Origem dos requisitos (especificação e planejamento)

Esta seção reúne, **sem paráfrase**, o que a especificação do projeto e o planejamento da Sprint 3 exigem para esta task, para que o documento sozinho mostre de onde vem cada requisito da seção 4. Onde o texto original difere das convenções (ajustes 1 a 10, [seção 1](00-convencoes-e-contrato-compartilhado.md#1-ajustes-em-relação-ao-planejamento-e-por-quê)), vale o que está neste documento e nas convenções.

### 9.1 Requisito funcional da especificação

- **RF14** (Módulo D — Acompanhamento; interface Web): “Registrar resultados fictícios das avaliações previstas.”

### 9.2 Critério de aceite do PBI (planejamento)

> O pesquisador registra e edita o resultado de cada atividade da categoria avaliação de uma visita REALIZADA; visitas não realizadas, atividades de outras categorias e estudos encerrados são recusados; o coordenador consulta os resultados em modo somente leitura.

### 9.3 Regras de negócio do planejamento que afetam esta task

| # | Regra proposta | Tasks afetadas (no planejamento) |
|---|---|---|
| 10 | Erros seguem o formato único da Sprint 2 (400 validação, 403 perfil sem permissão, 404 inexistente, 409 regra de negócio, sempre com o código da regra). As migrations usam faixas de versão por PBI (V3_19_* a V3_25_*); antes de começar, confirmar que a maior versão da Sprint 2 é menor que V3_19, senão trocar o prefixo V3 pelo número seguinte ao da última migration | T74 e todas as de backend |

### 9.4 Texto original da task no planejamento

> **T92 · Testes de backend dos resultados.** Escreva os testes junto com o código. Primeiro teste cada regra isolada: visita agendada ou ausente, atividade de outra categoria, atividade de outra visita, estudo encerrado e valor vazio. Depois teste a edição e os pendentes. Por fim, teste os endpoints (200, 201, 400, 403, 404 e 409) com PostgreSQL real e confira a cobertura da fatia.

### 9.5 Requisitos da especificação que atravessam esta task

- Estratégia de testes (seção 7): “Cobertura mínima de **80% das linhas do código de negócio do backend** (excluindo código gerado e configurações)”, “Testes unitários para todas as regras de negócio relevantes”, “Testes de integração para os principais endpoints” e “Execução automatizada dos testes a cada pull request ou integração à branch principal”. Regra 9.6: “As equipes devem implementar testes automatizados e apresentar evidências de cobertura.”
- **Definition of Done** da Sprint (texto completo na [seção 11 das convenções](00-convencoes-e-contrato-compartilhado.md#11-definition-of-done-e-requisitos-transversais-da-especificação)): os critérios de aceite estão atendidos e demonstráveis com dados fictícios; o código foi revisado, passou no CI e entrou na `main` por Pull Request; há testes automatizados e, no backend, a cobertura do código de negócio permanece em 80% ou mais; erros e validações são tratados com mensagens claras; Swagger e contrato YAML refletem os endpoints; os dados de demonstração estão no perfil `demo`; documentação e capturas estão no repositório; e RF01 a RF11 continuam funcionando.


---
Parte do conjunto de documentos de design da Sprint 3: [índice](README.md) · [convenções e contrato](00-convencoes-e-contrato-compartilhado.md).
