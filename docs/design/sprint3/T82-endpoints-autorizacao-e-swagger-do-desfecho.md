# T82 · Endpoints, autorização e Swagger do desfecho

> **Sprint 3** · PBI-20 · RF13 Realização, ausência e cancelamento de visitas  
> **Responsável:** I3 · **Estimativa:** 3h · **Prazo:** Qui 12/11  
> **Depende de:** T81  
> **Situação:** ⬜ A implementar (este documento é o guia para quem for fazer)

> Antes de começar, leia as [convenções e o contrato compartilhado da Sprint 3](00-convencoes-e-contrato-compartilhado.md): nomes, endpoints, DTOs, códigos de erro, tabelas e dados de demonstração valem para esta task.

## 1. Objetivo

Expor o endpoint de desfecho e a consulta de uma visita, com autorização por perfil, erros no formato único e o Swagger igual ao contrato.

## 2. Contexto: o que já existe e deve ser reaproveitado

- `VisitaAgendadaResponse` e o `AgendamentoController` (que já responde `PUT /api/visitas-agendadas/{id}`) vêm da T76; `DesfechoVisitaService` vem da T81; o contrato do desfecho, da T80.
- `Permissoes.LEITURA` e `Permissoes.ESCRITA_RECRUTAMENTO`; `Autenticacao.idDe(jwt)`; `ApiExceptionHandler` (formato único).

## 3. Onde implementar

| Arquivo | Ação | Responsabilidade |
|---|---|---|
| `backend/ClinicalStudyPB/src/main/java/UEPB/ESWII/ClinicalStudyPB/controller/VisitaAgendadaController.java` | Criar | `GET /api/visitas-agendadas/{id}` e `POST .../desfecho` |
| `backend/ClinicalStudyPB/src/main/java/UEPB/ESWII/ClinicalStudyPB/controller/dto/DesfechoRequest.java` | Criar | `tipo`, `dataHoraRealizada`, `motivo`, `observacao` com validação |
| `backend/ClinicalStudyPB/src/main/java/UEPB/ESWII/ClinicalStudyPB/service/AgendamentoService.java` | Alterar | Método `buscar(id)` da visita (consulta), se ainda não existir |
| `backend/ClinicalStudyPB/src/test/java/UEPB/ESWII/ClinicalStudyPB/ContratoOpenApiSprint3Test.java` | Alterar | Habilita o caso do desfecho e inclui o `GET` (7 operações) |
| `docs/api/openapi-sprint3.yaml` | Alterar | Acrescenta `GET /api/visitas-agendadas/{id}` e corrige divergências |

## 4. Requisitos a implementar

Cada requisito tem um identificador (`RQ-T82-NN`) para a revisão do Pull Request e para a conferência no fim da task: todos precisam estar atendidos.

### Endpoints

- **RQ-T82-01** `POST /api/visitas-agendadas/{visitaAgendadaId}/desfecho` (só coordenador) recebe `DesfechoRequest` e responde **200** com a `VisitaAgendadaResponse` atualizada (o recurso já existe: o desfecho o altera, não cria).
- **RQ-T82-02** `GET /api/visitas-agendadas/{visitaAgendadaId}` (leitura: pesquisador e coordenador) devolve a `VisitaAgendadaResponse` completa (a tela da visita usa esta consulta).
- **RQ-T82-03** `DesfechoRequest` com Bean Validation: `tipo` obrigatório (`@NotNull`), `motivo` e `observacao` com no máximo 500 caracteres; as regras de motivo e data ficam na máquina (T80), não no DTO.
- **RQ-T82-04** Os dois métodos ficam no `VisitaAgendadaController` (`@RequestMapping("/api")`), sem conflito com o `PUT` de reagendamento do `AgendamentoController`.

### Autorização e erros

- **RQ-T82-05** `@PreAuthorize`: escrita `Permissoes.ESCRITA_RECRUTAMENTO`, leitura `Permissoes.LEITURA`; sem token 401; administrador e participante recebem 403 nas duas operações.
- **RQ-T82-06** Status: 200; 400 (`VALIDACAO_CAMPOS` para `tipo` ausente ou inválido, `CORPO_INVALIDO` para JSON ou enum malformado, `DATA_REALIZACAO_FUTURA`, `MOTIVO_OBRIGATORIO`); 403; 404 (visita inexistente); 409 (`TRANSICAO_VISITA_INVALIDA`, `ESTUDO_FORA_DE_RECRUTAMENTO_OU_EXECUCAO`, `VISITA_AINDA_NAO_OCORREU`, `VISITA_ALTERADA_CONCORRENTEMENTE`).
- **RQ-T82-07** Todos no formato único de erro (`status`, `codigoRegra`, `mensagem`, `errosCampo`, `erro`, `codigo`).

### Swagger e contrato

- **RQ-T82-08** `@Tag(name = "Visitas agendadas")`, `@Operation` com a descrição das regras (estados finais, motivo obrigatório, data real não futura).
- **RQ-T82-09** Acrescentar ao YAML o `GET /api/visitas-agendadas/{visitaAgendadaId}`, remover o `@Disabled("T82")` do `ContratoOpenApiSprint3Test` e atualizar a contagem esperada (5 da T74 + 2 = 7 operações); se o Swagger divergir do YAML, **corrigir o YAML no mesmo Pull Request**.

## 5. Contrato, regras e códigos de erro

- Operações 6 e 7 da seção 2 das convenções.

## 6. Testes

- A bateria completa de endpoints está na T85; aqui, no mínimo, `ContratoOpenApiSprint3Test` verde e um teste de fumaça de cada uma das duas operações.

## 7. Critérios de aceite

- [ ] O endpoint de desfecho e a consulta respondem conforme o contrato.
- [ ] O Swagger UI e o YAML coincidem.
- [ ] Os perfis sem permissão recebem 403 e a visita inexistente, 404.

## 8. Cuidados e fora de escopo

- Um único endpoint de desfecho para os três tipos.
- O controller não aplica regra de negócio: só valida o DTO e chama o serviço.

---
Parte do conjunto de documentos de design da Sprint 3: [índice](README.md) · [convenções e contrato](00-convencoes-e-contrato-compartilhado.md).
