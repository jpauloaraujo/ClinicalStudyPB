# T90 · Endpoints, autorização, Swagger e dados de demonstração

> **Sprint 3** · PBI-21 · RF14 Resultados fictícios das avaliações previstas  
> **Responsável:** I6 · **Estimativa:** 3h · **Prazo:** Dom 15/11  
> **Depende de:** T89  
> **Situação:** ⬜ A implementar (este documento é o guia para quem for fazer)

> Antes de começar, leia as [convenções e o contrato compartilhado da Sprint 3](00-convencoes-e-contrato-compartilhado.md): nomes, endpoints, DTOs, códigos de erro, tabelas e dados de demonstração valem para esta task.

## 1. Objetivo

Expor as duas operações de resultados com escrita só do pesquisador e leitura dos dois perfis, no formato único de erro, com o Swagger igual ao YAML, e criar o script de demonstração com resultados para o histórico (PBI-22) e para o E2E da T99.

## 2. Contexto: o que já existe e deve ser reaproveitado

- `Permissoes.LEITURA`, `Permissoes.ESCRITA_PLANEJAMENTO` (`hasRole('PESQUISADOR')`); nesta task entra o alias `Permissoes.ESCRITA_RESULTADOS` com a mesma expressão, para o código dizer o que quer dizer.
- O serviço e os DTOs de domínio vêm da T89; o contrato, da T88. O estilo dos controllers é o de `ParticipacaoController`.
- O demo do PBI-19 e do PBI-20 já traz P1 e P4 com a Triagem `REALIZADA` (seção 7 das convenções).

## 3. Onde implementar

| Arquivo | Ação | Responsabilidade |
|---|---|---|
| `backend/ClinicalStudyPB/src/main/java/UEPB/ESWII/ClinicalStudyPB/security/Permissoes.java` | Alterar | Alias `ESCRITA_RESULTADOS` |
| `backend/ClinicalStudyPB/src/main/java/UEPB/ESWII/ClinicalStudyPB/controller/ResultadoAtividadeController.java` | Criar | `GET` e `PUT` dos resultados |
| `backend/ClinicalStudyPB/src/main/java/UEPB/ESWII/ClinicalStudyPB/controller/dto/` | Criar | `ResultadosDaVisitaResponse`, `AtividadeComResultadoResponse`, `ResultadoAtividadeResponse`, `ResultadoRequest` |
| `backend/ClinicalStudyPB/src/test/java/UEPB/ESWII/ClinicalStudyPB/ContratoOpenApiSprint3Test.java` | Alterar | Habilita o caso dos resultados (9 operações) |
| `backend/ClinicalStudyPB/src/main/resources/db/demo/R__demo_21_resultados.sql` | Criar | Resultados das Triagens de P1 e P4 |
| `backend/ClinicalStudyPB/src/test/java/UEPB/ESWII/ClinicalStudyPB/DemoDataIntegrationTest.java` | Alterar | Contagem de resultados e carga dupla |

## 4. Requisitos a implementar

Cada requisito tem um identificador (`RQ-T90-NN`) para a revisão do Pull Request e para a conferência no fim da task: todos precisam estar atendidos.

### Endpoints e autorização

- **RQ-T90-01** `GET /api/visitas-agendadas/{visitaAgendadaId}/resultados` com `Permissoes.LEITURA` (pesquisador e coordenador), devolvendo `ResultadosDaVisitaResponse`.
- **RQ-T90-02** `PUT /api/visitas-agendadas/{visitaAgendadaId}/resultados/{acaoId}` com `Permissoes.ESCRITA_RESULTADOS` (**só o pesquisador**); `ResultadoRequest` com `valor` (`@NotBlank`, `@Size(max = 200)`), `unidade` (`@Size(max = 30)`) e `observacao` (`@Size(max = 500)`); responde **201** no primeiro registro e **200** na edição, com o `ResultadoAtividadeResponse` (valor, unidade, observação, autor e datas).
- **RQ-T90-03** **403** para o coordenador na escrita e para o administrador e o participante em qualquer operação; **401** sem token; **404** para visita ou atividade inexistente; **400** (`VALIDACAO_CAMPOS`, `CORPO_INVALIDO`, `VALOR_OBRIGATORIO`); **409** (`VISITA_NAO_REALIZADA`, `ATIVIDADE_FORA_DA_CATEGORIA_AVALIACAO`, `ATIVIDADE_NAO_PERTENCE_A_VISITA`, `ESTUDO_ENCERRADO`).
- **RQ-T90-04** `@Tag(name = "Resultados das avaliações")` e `@Operation` com a descrição da regra 4. Conferir o Swagger contra o YAML, remover o `@Disabled("T90")` do `ContratoOpenApiSprint3Test` e atualizar a contagem (9 operações); corrigir o YAML no mesmo Pull Request se algo divergir.

### Dados de demonstração `R__demo_21_resultados.sql`

- **RQ-T90-05** Inserir o resultado `120/80` mmHg da atividade "Aferir pressão arterial" na Triagem `REALIZADA` de **P1** e `130/85` mmHg na de **P4**; deixar as demais avaliações pendentes (o "Coletar sangue" é `EXAME_LABORATORIAL` e nunca recebe resultado).
- **RQ-T90-06** Autor: `demo.pesquisador@clinicalstudy.test`; datas relativas a `CURRENT_DATE`; ids `d6300000-0000-4000-8000-0000000021NN`; `NOT EXISTS` em cada insert; testado no H2 e no PostgreSQL.
- **RQ-T90-07** Atualizar `DemoDataIntegrationTest` (duas cargas) e avisar I2 (dono do PBI-22) de que os resultados de demonstração existem.

## 5. Contrato, regras e códigos de erro

- Operações 8 e 9 da seção 2 das convenções; códigos da seção 4.
- Escrita **só** do pesquisador (regra 4): o coordenador consulta em modo somente leitura.

## 6. Testes

- Teste de fumaça de cada endpoint e o `ContratoOpenApiSprint3Test` verde; a bateria completa é a T92.
- `DemoDataIntegrationTest` com a carga dupla.

## 7. Critérios de aceite

- [ ] Pesquisador registra e edita resultados; coordenador só consulta.
- [ ] O Swagger e o YAML coincidem.
- [ ] O perfil `demo` traz resultados para o histórico e o E2E.

## 8. Cuidados e fora de escopo

- O coordenador não escreve resultados: 403, não 409.
- Sem exclusão de resultado.

---
Parte do conjunto de documentos de design da Sprint 3: [índice](README.md) · [convenções e contrato](00-convencoes-e-contrato-compartilhado.md).
