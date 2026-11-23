# T89 · Regras de registro de resultados (serviço de domínio)

> **Sprint 3** · PBI-21 · RF14 Resultados fictícios das avaliações previstas  
> **Responsável:** I6 · **Estimativa:** 4h · **Prazo:** Sáb 14/11  
> **Depende de:** T84, T88  
> **Situação:** ⬜ A implementar (este documento é o guia para quem for fazer)

> Antes de começar, leia as [convenções e o contrato compartilhado da Sprint 3](00-convencoes-e-contrato-compartilhado.md): nomes, endpoints, DTOs, códigos de erro, tabelas e dados de demonstração valem para esta task. A origem dos requisitos (especificação, critério de aceite do PBI, regras de negócio e o texto original da task) está na [seção 9](#9-origem-dos-requisitos-especificação-e-planejamento).

## 1. Objetivo

Implementar o serviço de domínio que consulta e registra os resultados das atividades de avaliação de uma visita realizada, com resultado parcial (uma atividade por vez), pendentes, edição e autor e data da última alteração.

## 2. Contexto: o que já existe e deve ser reaproveitado

- `VisitaAgendada`, `ResultadoAtividade` e os repositórios (T73, T87); `Acao.categoria`; `EstudoAcesso`; `UsuarioAutenticado`; o bean `Clock` (T75); `ConflitoRegraException` e `ValidacaoRegraException`.
- Os dados de demonstração das T74 e T84 trazem visitas `REALIZADA` (P1 e P4 do estudo "Demo - Acompanhamento de participantes") para testar o serviço.
- O bloqueio de participante retirado entra na T102 (ponto `exigirParticipacaoAtiva`, como nas T75 e T81).

## 3. Onde implementar

| Arquivo | Ação | Responsabilidade |
|---|---|---|
| `backend/ClinicalStudyPB/src/main/java/UEPB/ESWII/ClinicalStudyPB/service/ResultadoAtividadeService.java` | Criar | `consultar` e `registrar` |
| `backend/ClinicalStudyPB/src/main/java/UEPB/ESWII/ClinicalStudyPB/repository/ResultadoAtividadeRepository.java` | Alterar | Consultas necessárias à regra |
| `backend/ClinicalStudyPB/src/main/java/UEPB/ESWII/ClinicalStudyPB/repository/AcaoRepository.java` | Alterar | Atividades de avaliação da visita prevista, ordenadas por nome |

## 4. Requisitos a implementar

Cada requisito tem um identificador (`RQ-T89-NN`) para a revisão do Pull Request e para a conferência no fim da task: todos precisam estar atendidos.

### `registrar(visitaAgendadaId, acaoId, comando, usuarioId)`

- **RQ-T89-01** Verificar **nesta ordem**: (1) a visita agendada existe (404) e a atividade existe (404); (2) a visita está `REALIZADA`, senão 409 `VISITA_NAO_REALIZADA`; (3) a atividade pertence à **visita prevista** desta visita agendada, senão 409 `ATIVIDADE_NAO_PERTENCE_A_VISITA`; (4) a categoria da atividade é `AVALIACAO_CLINICA`, senão 409 `ATIVIDADE_FORA_DA_CATEGORIA_AVALIACAO`; (5) o estudo **não** está encerrado, senão 409 `ESTUDO_ENCERRADO`; (6) o `valor` não é vazio nem só espaços, senão 400 `VALOR_OBRIGATORIO`.
- **RQ-T89-02** Ponto único e comentado `exigirParticipacaoAtiva` (T102 o preenche com `PARTICIPANTE_RETIRADO`).
- **RQ-T89-03** **Primeiro registro:** criar o `ResultadoAtividade` com `registradoPor`/`registradoEm` e `alteradoPor`/`alteradoEm` iguais (usuário do token e `Clock`). **Edição:** atualizar `valor`, `unidade` e `observacao` e **só** `alteradoPor` e `alteradoEm`; `registradoPor` e `registradoEm` não mudam.
- **RQ-T89-04** Devolver ao chamador se o resultado foi criado ou editado (o controller responde 201 ou 200).
- **RQ-T89-05** `valor`, `unidade` e `observacao` são gravados com `trim`; `unidade` e `observacao` em branco viram nulos.

### Resultado parcial e pendentes

- **RQ-T89-06** Cada chamada salva **uma atividade**; salvar uma não exige as outras (resultado parcial). A visita pode ter só alguns resultados registrados.
- **RQ-T89-07** `consultar(visitaAgendadaId)` devolve **todas** as atividades `AVALIACAO_CLINICA` da visita prevista (ordenadas por nome), cada uma com o resultado ou vazio, e calcula `total`, `registrados` e `pendentes` (`total - registrados`).
- **RQ-T89-08** `consultar` não exige visita `REALIZADA`: para outras situações devolve as atividades sem resultado e preenche o campo `bloqueio`.
- **RQ-T89-09** Calcular `bloqueio` na consulta, por prioridade: `VISITA_NAO_REALIZADA`, depois `ESTUDO_ENCERRADO` e, a partir da T102, `PARTICIPANTE_RETIRADO`; sem motivo, `null` (a tela usa para avisar e deixar somente leitura).
- **RQ-T89-10** Sem consulta por atividade dentro de um laço (nada de N+1): carregar as atividades e os resultados da visita em uma ou duas consultas.

### Regras que não existem aqui

- **RQ-T89-11** Não há exclusão de resultado nesta Sprint; só registro e edição.
- **RQ-T89-12** Não há restrição de formato do `valor` (é texto curto livre) nem comparação com faixa de referência.
- **RQ-T89-13** A permissão por perfil (só o pesquisador escreve) é do controller (T90); o serviço não conhece o perfil.

## 5. Contrato, regras e códigos de erro

- Erros: `VISITA_NAO_REALIZADA`, `ATIVIDADE_NAO_PERTENCE_A_VISITA`, `ATIVIDADE_FORA_DA_CATEGORIA_AVALIACAO` e `ESTUDO_ENCERRADO` (409); `VALOR_OBRIGATORIO` (400); 404 para visita ou atividade inexistente.
- Regra 4: resultado só para visita `REALIZADA`, só para atividade de avaliação, um por atividade, editável enquanto o estudo não estiver encerrado, com autor e data da última alteração.

## 6. Testes

- Testes unitários de cada verificação e da gravação (criação e edição), com Mockito e `Clock` fixo; a matriz completa e os endpoints são da T92.
- Teste com os dados de demonstração (visitas `REALIZADA` de P1 e P4).

## 7. Critérios de aceite

- [ ] O serviço registra e edita o resultado de uma atividade de avaliação de uma visita realizada, um por vez.
- [ ] Cada regra violada falha com o código certo e na ordem definida.
- [ ] A consulta devolve as atividades com resultado ou pendentes e as contagens.
- [ ] Autor e data da última alteração são atualizados na edição.

## 8. Cuidados e fora de escopo

- O serviço não depende de classes web; o usuário chega por parâmetro.
- A retirada (`PARTICIPANTE_RETIRADO`) entra na T102, não aqui.

## 9. Origem dos requisitos (especificação e planejamento)

Esta seção reúne, **sem paráfrase**, o que a especificação do projeto e o planejamento da Sprint 3 exigem para esta task, para que o documento sozinho mostre de onde vem cada requisito da seção 4. Onde o texto original difere das convenções (ajustes 1 a 10, [seção 1](00-convencoes-e-contrato-compartilhado.md#1-ajustes-em-relação-ao-planejamento-e-por-quê)), vale o que está neste documento e nas convenções.

### 9.1 Requisito funcional da especificação

- **RF14** (Módulo D — Acompanhamento; interface Web): “Registrar resultados fictícios das avaliações previstas.”

### 9.2 Critério de aceite do PBI (planejamento)

> O pesquisador registra e edita o resultado de cada atividade da categoria avaliação de uma visita REALIZADA; visitas não realizadas, atividades de outras categorias e estudos encerrados são recusados; o coordenador consulta os resultados em modo somente leitura.

### 9.3 Regras de negócio do planejamento que afetam esta task

| # | Regra proposta | Tasks afetadas (no planejamento) |
|---|---|---|
| 1 | Visitas só são agendadas para participante incluído e ativo (não retirado), com o estudo em recrutamento ou execução; estudo encerrado bloqueia qualquer agendamento | T75, T81, T89, T102 |
| 4 | Resultados fictícios só existem para visita REALIZADA e só para atividades da categoria avaliação. Quem registra é o pesquisador, com um resultado por atividade, editável enquanto o estudo não estiver encerrado, guardando autor e data da última alteração | T87, T89, T91 |
| 6 | A retirada é registrada pelo coordenador, com motivo, quem solicitou e data (não futura), e é irreversível. O participante passa a RETIRADO e as visitas futuras AGENDADA viram CANCELADA com o motivo "retirada do estudo". A vaga continua ocupada, ou seja, o limite de vagas conta todos os que foram incluídos (ponto a validar com o PO) | T101, T102 (inclui os bloqueios nos serviços de T75, T81 e T89) e T104 |
| 10 | Erros seguem o formato único da Sprint 2 (400 validação, 403 perfil sem permissão, 404 inexistente, 409 regra de negócio, sempre com o código da regra). As migrations usam faixas de versão por PBI (V3_19_* a V3_25_*); antes de começar, confirmar que a maior versão da Sprint 2 é menor que V3_19, senão trocar o prefixo V3 pelo número seguinte ao da última migration | T74 e todas as de backend |

### 9.4 Texto original da task no planejamento

> **T89 · Regras de registro de resultados (serviço de domínio).** Primeiro escreva o serviço de domínio testando com as visitas REALIZADAS dos dados de demonstração das T74 e T84. Depois valide, nesta ordem: visita REALIZADA, atividade pertencente à visita prevista e da categoria avaliação, estudo não encerrado e valor não vazio, cada falha com código de erro próprio. Em seguida permita salvar uma atividade de cada vez (resultado parcial) e calcular os pendentes. Por fim, aplique a edição, que atualiza autor e data da última alteração. O bloqueio de participante retirado entra na T102.

### 9.5 Requisitos da especificação que atravessam esta task

- Entregas: “Tratamento de erros e validações das regras de negócio.”
- Estratégia de testes (seção 7): “Cobertura mínima de **80% das linhas do código de negócio do backend** (excluindo código gerado e configurações)”, “Testes unitários para todas as regras de negócio relevantes”, “Testes de integração para os principais endpoints” e “Execução automatizada dos testes a cada pull request ou integração à branch principal”. Regra 9.6: “As equipes devem implementar testes automatizados e apresentar evidências de cobertura.”
- **Definition of Done** da Sprint (texto completo na [seção 11 das convenções](00-convencoes-e-contrato-compartilhado.md#11-definition-of-done-e-requisitos-transversais-da-especificação)): os critérios de aceite estão atendidos e demonstráveis com dados fictícios; o código foi revisado, passou no CI e entrou na `main` por Pull Request; há testes automatizados e, no backend, a cobertura do código de negócio permanece em 80% ou mais; erros e validações são tratados com mensagens claras; Swagger e contrato YAML refletem os endpoints; os dados de demonstração estão no perfil `demo`; documentação e capturas estão no repositório; e RF01 a RF11 continuam funcionando.


---
Parte do conjunto de documentos de design da Sprint 3: [índice](README.md) · [convenções e contrato](00-convencoes-e-contrato-compartilhado.md).
