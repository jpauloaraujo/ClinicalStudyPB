# T80 · Máquina de estados da visita e contrato do desfecho

> **Sprint 3** · PBI-20 · RF13 Realização, ausência e cancelamento de visitas  
> **Responsável:** I3 · **Estimativa:** 3h · **Prazo:** Qui 12/11  
> **Depende de:** T73  
> **Situação:** ⬜ A implementar (este documento é o guia para quem for fazer)

> Antes de começar, leia as [convenções e o contrato compartilhado da Sprint 3](00-convencoes-e-contrato-compartilhado.md): nomes, endpoints, DTOs, códigos de erro, tabelas e dados de demonstração valem para esta task. A origem dos requisitos (especificação, critério de aceite do PBI, regras de negócio e o texto original da task) está na [seção 9](#9-origem-dos-requisitos-especificação-e-planejamento).

## 1. Objetivo

Definir, como regra pura de domínio, o ciclo de vida da visita agendada (`AGENDADA` → `REALIZADA`, `AUSENTE` ou `CANCELADA`, todos finais) e os dados exigidos por cada desfecho, e descrever no contrato o endpoint único de desfecho. A retirada (T102) usará esta máquina para cancelar visitas futuras.

## 2. Contexto: o que já existe e deve ser reaproveitado

- `MaquinaEstadosEstudo` (`domain/regra`) é o modelo: classe `final`, métodos estáticos, sem Spring Web, constantes com os códigos de erro e `ConflitoRegraException` para cada violação. Siga o mesmo estilo.
- A entidade `VisitaAgendada` e o enum `SituacaoVisita` vêm da T73; o contrato inicial (`openapi-sprint3.yaml`) e a estrutura do teste de contrato, da T74.
- `ValidacaoRegraException` (400 com código) foi criada na T75.
- Regra 3: a visita nasce `AGENDADA` e só vira `REALIZADA`, `AUSENTE` ou `CANCELADA`; os três são finais; realizada guarda data e hora reais (não futuras); cancelar exige motivo (5 caracteres ou mais).

## 3. Onde implementar

| Arquivo | Ação | Responsabilidade |
|---|---|---|
| `backend/ClinicalStudyPB/src/main/java/UEPB/ESWII/ClinicalStudyPB/domain/enums/TipoDesfecho.java` | Criar | Enum `REALIZADA`, `AUSENTE`, `CANCELADA` (os desfechos aceitos) |
| `backend/ClinicalStudyPB/src/main/java/UEPB/ESWII/ClinicalStudyPB/domain/regra/MaquinaEstadosVisita.java` | Criar | Transições permitidas e validação dos dados do desfecho |
| `docs/api/openapi-sprint3.yaml` | Alterar | Operação `POST /api/visitas-agendadas/{id}/desfecho`, `DesfechoRequest` e códigos de erro |
| `backend/ClinicalStudyPB/src/test/java/UEPB/ESWII/ClinicalStudyPB/MaquinaEstadosVisitaTest.java` | Criar | Teste da máquina (a matriz completa é a T85) |

## 4. Requisitos a implementar

Cada requisito tem um identificador (`RQ-T80-NN`) para a revisão do Pull Request e para a conferência no fim da task: todos precisam estar atendidos.

### `MaquinaEstadosVisita` (domínio puro)

- **RQ-T80-01** Classe `final` com construtor privado e métodos estáticos, sem dependência de Spring Web, JPA ou `Clock` implícito (o "agora" chega por parâmetro).
- **RQ-T80-02** Constantes dos códigos: `TRANSICAO_INVALIDA = "TRANSICAO_VISITA_INVALIDA"`, `DATA_REALIZACAO_FUTURA`, `MOTIVO_OBRIGATORIO`.
- **RQ-T80-03** `transicaoPermitida(SituacaoVisita origem, SituacaoVisita destino)`: verdadeiro só para `AGENDADA → REALIZADA`, `AGENDADA → AUSENTE` e `AGENDADA → CANCELADA`; das 16 combinações, só 3 são válidas; `null` em qualquer lado é falso.
- **RQ-T80-04** `proximas(SituacaoVisita origem)` devolve o conjunto de destinos permitidos (os três para `AGENDADA`, vazio para os estados finais), para a tela saber o que oferecer.
- **RQ-T80-05** `estadoFinal(SituacaoVisita)`: verdadeiro para `REALIZADA`, `AUSENTE` e `CANCELADA`.
- **RQ-T80-06** `destinoDe(TipoDesfecho)` converte o tipo de desfecho na situação correspondente.
- **RQ-T80-07** `validarTransicao(SituacaoVisita atual, TipoDesfecho tipo)` lança `ConflitoRegraException` com o código `TRANSICAO_VISITA_INVALIDA` e uma mensagem clara quando a visita não está `AGENDADA` (ex.: "A visita já foi registrada como realizada e não pode mais ser alterada").

### Dados exigidos por desfecho

- **RQ-T80-08** `validarDados(TipoDesfecho tipo, LocalDateTime dataHoraRealizada, String motivo, String observacao, LocalDateTime agora)` lança `ValidacaoRegraException` (400): **realizada** com `dataHoraRealizada` posterior a `agora` → `DATA_REALIZACAO_FUTURA`; **cancelada** com `motivo` nulo, em branco ou com menos de 5 caracteres depois de `trim` → `MOTIVO_OBRIGATORIO`.
- **RQ-T80-09** **Realizada:** `dataHoraRealizada` é opcional na entrada (assume `agora`); `observacao` é opcional. **Ausente:** não guarda data real; `observacao` opcional. **Cancelada:** `motivo` obrigatório; `observacao` opcional; não guarda data real.
- **RQ-T80-10** Tamanhos máximos: `motivo` e `observacao` com até 500 caracteres (Bean Validation no DTO da T82; a máquina não repete a regra de tamanho).
- **RQ-T80-11** Campos que não pertencem ao tipo escolhido são ignorados, não geram erro (ex.: `motivo` enviado em uma realizada não é gravado).

### Contrato do desfecho (`openapi-sprint3.yaml`)

- **RQ-T80-12** Descrever **um único endpoint**: `POST /api/visitas-agendadas/{visitaAgendadaId}/desfecho`, só coordenador, resposta 200 com `VisitaAgendadaResponse` (schema da T74).
- **RQ-T80-13** Declarar `DesfechoRequest` (`tipo` obrigatório com o enum `TipoDesfecho`, `dataHoraRealizada`, `motivo`, `observacao`) com um exemplo para cada tipo (seção 3 das convenções).
- **RQ-T80-14** Documentar as respostas: `400` (`VALIDACAO_CAMPOS`, `CORPO_INVALIDO`, `DATA_REALIZACAO_FUTURA`, `MOTIVO_OBRIGATORIO`), `403`, `404` e `409` (`TRANSICAO_VISITA_INVALIDA`, `ESTUDO_FORA_DE_RECRUTAMENTO_OU_EXECUCAO`, `VISITA_AINDA_NAO_OCORREU`, `VISITA_ALTERADA_CONCORRENTEMENTE`), no formato único de erro.
- **RQ-T80-15** Atualizar a contagem de operações do `ContratoOpenApiSprint3Test` (o endpoint fica descrito, mas o teste de existência só passa na T82; marque o novo caso `@Disabled("T82")`).

## 5. Contrato, regras e códigos de erro

- Transições: 3 de 16 válidas. Estados finais: `REALIZADA`, `AUSENTE`, `CANCELADA`.
- Erros: `TRANSICAO_VISITA_INVALIDA` (409), `DATA_REALIZACAO_FUTURA` (400) e `MOTIVO_OBRIGATORIO` (400).

## 6. Testes

- `MaquinaEstadosVisitaTest`: as 16 combinações de origem e destino (só 3 válidas), `proximas` de cada estado, `estadoFinal` e `validarDados` (data futura, motivo ausente e com 4 caracteres, motivo com 5 caracteres válido, motivo só com espaços).
- Sem Spring, sem banco: o teste deve rodar em milissegundos.

## 7. Critérios de aceite

- [ ] A máquina decide as transições e os dados de cada desfecho sem depender de Spring Web.
- [ ] O contrato descreve o endpoint único, os DTOs, os exemplos e os códigos de erro.
- [ ] I3 avisa I1, I6 e I4 de que a máquina está na `main` (a T102 a usa).

## 8. Cuidados e fora de escopo

- Não acessar banco nem repositório aqui (T81).
- Não criar um endpoint por desfecho: o planejamento fixa **um único** endpoint.

## 9. Origem dos requisitos (especificação e planejamento)

Esta seção reúne, **sem paráfrase**, o que a especificação do projeto e o planejamento da Sprint 3 exigem para esta task, para que o documento sozinho mostre de onde vem cada requisito da seção 4. Onde o texto original difere das convenções (ajustes 1 a 10, [seção 1](00-convencoes-e-contrato-compartilhado.md#1-ajustes-em-relação-ao-planejamento-e-por-quê)), vale o que está neste documento e nas convenções.

### 9.1 Requisito funcional da especificação

- **RF13** (Módulo D — Acompanhamento; interface Web): “Registrar a realização, ausência ou cancelamento de visitas.”

### 9.2 Critério de aceite do PBI (planejamento)

> O coordenador registra o desfecho de uma visita agendada (realizada, ausente ou cancelada), com os dados exigidos por cada situação; desfechos são finais e não podem ser desfeitos; transições inválidas são recusadas com a regra violada.

### 9.3 Regras de negócio do planejamento que afetam esta task

| # | Regra proposta | Tasks afetadas (no planejamento) |
|---|---|---|
| 3 | A visita agendada nasce AGENDADA e só pode virar REALIZADA, AUSENTE ou CANCELADA; esses três estados são finais. Reagendar (trocar a data) só vale para AGENDADA. Realizada e ausente não aceitam data futura; cancelar exige motivo; realizada guarda data e hora reais | T75, T80, T81, T83 |
| 10 | Erros seguem o formato único da Sprint 2 (400 validação, 403 perfil sem permissão, 404 inexistente, 409 regra de negócio, sempre com o código da regra). As migrations usam faixas de versão por PBI (V3_19_* a V3_25_*); antes de começar, confirmar que a maior versão da Sprint 2 é menor que V3_19, senão trocar o prefixo V3 pelo número seguinte ao da última migration | T74 e todas as de backend |

### 9.4 Texto original da task no planejamento

> **T80 · Máquina de estados da visita e contrato do desfecho.** A retirada (T102) usará esta máquina para cancelar visitas futuras. Primeiro crie uma classe de domínio com as transições permitidas: AGENDADA vai para REALIZADA, AUSENTE ou CANCELADA, e esses três são finais. Depois defina os dados de cada desfecho: realizada guarda a data e hora reais (que não podem ser futuras), ausente aceita observação e cancelada exige motivo com ao menos 5 caracteres. Por fim, descreva no contrato um único endpoint de desfecho, com o tipo, a data real, o motivo e a observação, e os códigos de erro (transição inválida, data futura e motivo ausente). Mantenha a classe sem dependência de Spring Web.

### 9.5 Requisitos da especificação que atravessam esta task

- Regra 9.2 (tecnologia): “O backend deve disponibilizar uma **API HTTP REST** compartilhada pela web e pelo mobile, documentada no padrão **OpenAPI** (Swagger UI pode ser usado para consulta e testes).”
- Entregas: “Tratamento de erros e validações das regras de negócio.”
- **Definition of Done** da Sprint (texto completo na [seção 11 das convenções](00-convencoes-e-contrato-compartilhado.md#11-definition-of-done-e-requisitos-transversais-da-especificação)): os critérios de aceite estão atendidos e demonstráveis com dados fictícios; o código foi revisado, passou no CI e entrou na `main` por Pull Request; há testes automatizados e, no backend, a cobertura do código de negócio permanece em 80% ou mais; erros e validações são tratados com mensagens claras; Swagger e contrato YAML refletem os endpoints; os dados de demonstração estão no perfil `demo`; documentação e capturas estão no repositório; e RF01 a RF11 continuam funcionando.


---
Parte do conjunto de documentos de design da Sprint 3: [índice](README.md) · [convenções e contrato](00-convencoes-e-contrato-compartilhado.md).
