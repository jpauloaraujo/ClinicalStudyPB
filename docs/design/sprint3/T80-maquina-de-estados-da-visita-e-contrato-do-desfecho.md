# T80 · Máquina de estados da visita e contrato do desfecho

> **Sprint 3** · PBI-20 · RF13 Realização, ausência e cancelamento de visitas  
> **Responsável:** I3 · **Estimativa:** 3h · **Prazo:** Qui 12/11  
> **Depende de:** T73  
> **Situação:** ⬜ A implementar (este documento é o guia para quem for fazer)

> Antes de começar, leia as [convenções e o contrato compartilhado da Sprint 3](00-convencoes-e-contrato-compartilhado.md): nomes, endpoints, DTOs, códigos de erro, tabelas e dados de demonstração valem para esta task.

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

---
Parte do conjunto de documentos de design da Sprint 3: [índice](README.md) · [convenções e contrato](00-convencoes-e-contrato-compartilhado.md).
