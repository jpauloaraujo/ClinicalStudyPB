# T106 · Testes de backend da retirada

> **Sprint 3** · PBI-23 · RF16 Retirada de participante  
> **Responsável:** I4 · **Estimativa:** 4h · **Prazo:** Qui 19/11  
> **Depende de:** T102, T103  
> **Situação:** ⬜ A implementar (este documento é o guia para quem for fazer)

> Antes de começar, leia as [convenções e o contrato compartilhado da Sprint 3](00-convencoes-e-contrato-compartilhado.md): nomes, endpoints, DTOs, códigos de erro, tabelas e dados de demonstração valem para esta task.

## 1. Objetivo

Provar as pré-condições, a irreversibilidade, o cancelamento em cascata na mesma transação, a regra de que a contagem de vagas não muda, os endpoints e os bloqueios acrescentados nos serviços de agendamento, desfecho e resultados.

## 2. Contexto: o que já existe e deve ser reaproveitado

- Modelos: `ParticipacaoServiceTest`, `ParticipacaoEndpointTest` e `ParticipacaoConcorrenciaTest`; `CenarioRecrutamento` com os métodos das T78, T85 e T92.
- Para provar que **tudo acontece na mesma transação**, o teste força uma falha no meio (por exemplo, um `@MockitoSpyBean` do repositório de visitas que lança exceção ao salvar a segunda visita) e confere que nada foi gravado.
- Os bloqueios novos nos três serviços (T102) precisam de um teste cada.

## 3. Onde implementar

| Arquivo | Ação | Responsabilidade |
|---|---|---|
| `backend/ClinicalStudyPB/src/test/java/UEPB/ESWII/ClinicalStudyPB/RetiradaServiceTest.java` | Criar | Pré-condições, cascata e atomicidade (Mockito) |
| `backend/ClinicalStudyPB/src/test/java/UEPB/ESWII/ClinicalStudyPB/RetiradaRequestValidacaoTest.java` | Criar | Validação do DTO |
| `backend/ClinicalStudyPB/src/test/java/UEPB/ESWII/ClinicalStudyPB/RetiradaEndpointTest.java` | Criar | 201, 400, 403, 404 e 409 com tokens reais |
| `backend/ClinicalStudyPB/src/test/java/UEPB/ESWII/ClinicalStudyPB/RetiradaCascataIntegracaoTest.java` | Criar | Cascata e atomicidade com o banco |
| `backend/ClinicalStudyPB/src/test/java/UEPB/ESWII/ClinicalStudyPB/RetiradaConcorrenciaTest.java` | Criar | Duas retiradas simultâneas |
| `backend/ClinicalStudyPB/src/test/java/UEPB/ESWII/ClinicalStudyPB/ParticipanteRetiradoBloqueiosTest.java` | Criar | Agendamento, desfecho e resultados de retirado |

## 4. Requisitos a implementar

Cada requisito tem um identificador (`RQ-T106-NN`) para a revisão do Pull Request e para a conferência no fim da task: todos precisam estar atendidos.

### Pré-condições e irreversibilidade

- **RQ-T106-01** Participante ativo e estudo em recrutamento ou execução: retirada aceita.
- **RQ-T106-02** **Segunda retirada** do mesmo participante: 409 `PARTICIPANTE_JA_RETIRADO` (a retirada é irreversível); estudo **encerrado**: 409 `ESTUDO_ENCERRADO`.
- **RQ-T106-03** Motivo nulo, em branco e com 4 caracteres: 400 `MOTIVO_OBRIGATORIO`; com 5 caracteres: aceito; 500 e 501 caracteres (limite do DTO).
- **RQ-T106-04** `dataRetirada` de hoje e do passado aceitas; do futuro: 400 `DATA_RETIRADA_FUTURA` (com `Clock` fixo).
- **RQ-T106-05** A ordem das verificações: participante já retirado vence o estudo encerrado; o estudo encerrado vence o motivo ausente.

### Cancelamento em cascata e atomicidade

- **RQ-T106-06** Visitas futuras `AGENDADA` viram `CANCELADA` com o motivo **"retirada do estudo"** (e `alteradoPor`/`alteradoEm` atualizados); visitas `REALIZADA` e `AUSENTE` **permanecem como estavam**, e as já `CANCELADA` mantêm o motivo original.
- **RQ-T106-07** Visitas `AGENDADA` **vencidas** (data passada sem desfecho) permanecem `AGENDADA` (decisão documentada na T102).
- **RQ-T106-08** `visitasCanceladas` do `RetiradaResponse` e do `GET` bate com as visitas realmente canceladas.
- **RQ-T106-09** **Mesma transação:** com falha forçada ao salvar a segunda visita, a participação continua `ATIVO`, não há linha em `retiradas_participacao` e nenhuma visita foi cancelada (nada pela metade).
- **RQ-T106-10** O participante retirado continua com histórico preservado: `GET .../historico` mostra as visitas e resultados.

### Vagas

- **RQ-T106-11** Depois da retirada, `VagasResponse` (`incluidos`, `restantes`) **não muda**; incluir um novo candidato no estudo cheio ainda dá `VAGAS_ESGOTADAS` (a vaga continua ocupada).

### Endpoints com tokens reais (e PostgreSQL)

- **RQ-T106-12** `POST`: 201 com o corpo completo e `Location`; 400 (motivo ausente, solicitante ausente ou inválido, data ausente, JSON malformado, data futura); 403 para pesquisador, administrador e participante; 401; 404 (participação inexistente); 409 (já retirado e estudo encerrado).
- **RQ-T106-13** `GET`: 200 depois da retirada; 404 antes; 403 para administrador e participante; leitura permitida ao pesquisador.
- **RQ-T106-14** `ParticipacaoResponse` (lista de participantes) traz `situacao = RETIRADO` depois da retirada.
- **RQ-T106-15** **Concorrência** (sem `@Transactional`, com limpeza no `@AfterEach`): duas retiradas simultâneas do mesmo participante resultam em exatamente um 201 e um 409.

### Bloqueios nos serviços dos outros PBIs

- **RQ-T106-16** Participante retirado: **agendar** e **reagendar** → 409 `PARTICIPANTE_RETIRADO`; **registrar desfecho** → 409 `PARTICIPANTE_RETIRADO`; **registrar e editar resultado** → 409 `PARTICIPANTE_RETIRADO`; a consulta de resultados devolve `bloqueio = PARTICIPANTE_RETIRADO`. Um teste para cada um.
- **RQ-T106-17** A ordem das verificações respeitada: estado do estudo primeiro, depois a situação do participante.

### Cobertura

- **RQ-T106-18** Conferir a cobertura de `RetiradaService` e dos trechos acrescentados (linhas e ramos), registrar no Pull Request e confirmar que toda a suíte anterior continua passando, também no job `Backend (PostgreSQL)`.

## 6. Testes

- As classes da seção 3 e o `ContratoOpenApiSprint3Test` verde (12 operações).

## 7. Critérios de aceite

- [ ] As pré-condições e a irreversibilidade estão provadas.
- [ ] O cancelamento em cascata e a atomicidade estão provados, e a contagem de vagas não muda.
- [ ] Os endpoints respondem 201, 400, 403, 404 e 409; os três bloqueios novos têm teste.

## 8. Cuidados e fora de escopo

- Não depender da data do dia.
- O teste de falha forçada precisa provar o rollback: conferir o estado final no banco, não só a exceção.

---
Parte do conjunto de documentos de design da Sprint 3: [índice](README.md) · [convenções e contrato](00-convencoes-e-contrato-compartilhado.md).
