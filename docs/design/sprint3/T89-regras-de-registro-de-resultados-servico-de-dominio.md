# T89 · Regras de registro de resultados (serviço de domínio)

> **Sprint 3** · PBI-21 · RF14 Resultados fictícios das avaliações previstas  
> **Responsável:** I6 · **Estimativa:** 4h · **Prazo:** Sáb 14/11  
> **Depende de:** T84, T88  
> **Situação:** ⬜ A implementar (este documento é o guia para quem for fazer)

> Antes de começar, leia as [convenções e o contrato compartilhado da Sprint 3](00-convencoes-e-contrato-compartilhado.md): nomes, endpoints, DTOs, códigos de erro, tabelas e dados de demonstração valem para esta task.

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

---
Parte do conjunto de documentos de design da Sprint 3: [índice](README.md) · [convenções e contrato](00-convencoes-e-contrato-compartilhado.md).
