# Convenções e contrato compartilhado da Sprint 3

> **Leia este documento antes de qualquer task da Sprint 3.** Ele fixa, em um só lugar, os nomes, endpoints, DTOs, códigos de erro, tabelas, pontos de encaixe e dados de demonstração que as 49 tasks (T73 a T121) usam. Como cada PBI é feito por uma pessoa diferente, em sequência, e o seguinte começa sobre o que o anterior deixou na `main`, é aqui que todos confirmam que estão falando da mesma coisa. Se uma task precisar mudar algo daqui, a mudança entra neste arquivo **no mesmo Pull Request**.
>
> Os documentos de design seguem o planejamento ([Planejamento da Sprint 3](../../sprint3/Planejamento_da_Sprint_3_ClinicalStudyPB_v2.md)). Onde o planejamento pedia algo que não funciona com o código real da Sprint 2, a seção 1 explica o ajuste.

## 1. Ajustes em relação ao planejamento (e por quê)

| # | Planejamento diz | Este design faz | Motivo |
|---|---|---|---|
| 1 | Migrations `V3_19_*` a `V3_25_*` (regra 10: se a maior versão da Sprint 2 for maior que `V3_19`, trocar o prefixo) | Prefixo **`V6_`**: `V6_19_1__create_scheduled_visits.sql`, `V6_21_1__create_evaluation_results.sql`, `V6_23_1__participant_withdrawal.sql`, `V6_24_1__unique_candidate_account.sql` | A Sprint 2 terminou em `V5` (`V3` normaliza e-mails, `V4` cria o recrutamento, `V5` cria o histórico de estados). O Flyway ordena por versão e, com `outOfOrder` desligado, recusa uma migration com versão menor que a última aplicada. `V3_19_1` seria rejeitada em qualquer banco que já tenha a `V5` |
| 2 | Scripts de demonstração versionados `V3_19_2__demo_...sql`, "só no perfil demo e repetível" | Scripts **repetíveis** `R__demo_19_visitas_agendadas.sql`, `R__demo_20_desfechos.sql`, `R__demo_21_resultados.sql`, `R__demo_23_retiradas.sql`, `R__demo_24_conta_participante.sql` em `db/demo/` | É o padrão da T63 (`R__dados_demonstracao.sql`). Um script versionado no local `db/demo` quebraria a validação do Flyway quando o perfil `demo` fosse desligado em um banco que o aplicou. Repetíveis rodam depois de todas as versionadas e por ordem de descrição (`dados demonstracao` < `demo 19` < `demo 20` ...), então a ordem fica garantida |
| 3 | Caminhos em inglês (`/me/studies`) | Caminhos em **português**, como toda a API: `/api/me/estudos`, `/api/me/visitas/proximas` | Regra do projeto desde a Sprint 2 (`/api/estudos/.../criterios`) |
| 4 | "Atividades da categoria avaliação" | `CategoriaAcao.AVALIACAO_CLINICA` (o enum já existe; as demais são `EXAME_LABORATORIAL`, `EXAME_IMAGEM`, `QUESTIONARIO`, `MEDICACAO` e `OUTROS`) | O enum da Sprint 2 não tem um valor chamado "AVALIACAO". **Validar com o PO (T94)** se `QUESTIONARIO` também conta |
| 5 | Erros 400 "sempre com o código da regra" | Nova `ValidacaoRegraException(codigo, mensagem)` (HTTP 400), criada na T75 e tratada em `ApiExceptionHandler` | Hoje `RegraNegocioException` responde 400 sempre com o código genérico `DADOS_INVALIDOS` |
| 6 | Data "com fuso" na API do app | O banco guarda `TIMESTAMP` no horário local do centro; a API **devolve** `OffsetDateTime` calculado com o fuso configurado em `app.fuso-horario` (padrão `America/Fortaleza`, UTC-3) e **recebe** data e hora locais sem fuso (`2026-11-19T09:30`) | O projeto inteiro usa `LocalDateTime`; o app precisa do deslocamento (ISO 8601) para exibir no fuso do aparelho. Um bean `Clock` no mesmo fuso permite fixar "hoje" nos testes |
| 7 | O pesquisador registra resultados e os dois perfis consultam o histórico | A aba **Participantes** e a ficha do participante passam a ser visíveis também para o **pesquisador** (hoje só o coordenador). Isso altera `abasEstudo` e o E2E `acesso-por-perfil.spec.ts` | Sem isso o pesquisador não chega à tela de resultados (RF14) nem ao histórico (RF15) |
| 8 | "Uma visita agendada por visita prevista e por participante" + "CANCELADA é final" | Consequência explícita: uma visita **cancelada não pode ser agendada de novo** (o `UNIQUE (participação, visita prevista)` a mantém) | Decisão do planejamento; **confirmar com o PO (T94)**, porque a retirada cancela visitas e a clínica real às vezes remarca |
| 9 | Datas de demonstração | Os scripts `R__demo_*` usam datas **relativas a `CURRENT_DATE` no momento da carga** (`CURRENT_DATE - INTERVAL '20' DAY`) e inserem só na primeira carga (`NOT EXISTS`) | Datas fixas ficariam no passado e quebrariam o agendamento e o reagendamento de demonstração. Testar a sintaxe no H2 e no PostgreSQL |
| 10 | Agenda "por período e situação" | Também é entregue o progresso por participante (`GET /api/estudos/{id}/agenda/participantes`) que a tela de participantes da T77 mostra como "X de N visitas agendadas" | O planejamento pede a tela, mas não dizia qual endpoint a alimenta |

## 2. Mapa dos endpoints novos (19 operações)

O contrato fica em `docs/api/openapi-sprint3.yaml`, **separado** do da Sprint 2 (`openapi-sprint2.yaml` tem um teste que exige exatamente 24 operações e só aceita caminhos do recrutamento). Cada PBI acrescenta ao YAML só os caminhos da sua fatia. Perfis: **C** = coordenador, **P** = pesquisador, **Part** = participante; o administrador nunca acessa (403). "Leitura" = P e C.

| # | Operação | Perfis | Sucesso | PBI · task |
|---|---|---|---|---|
| 1 | `GET /api/participacoes/{participacaoId}/visitas` | Leitura | 200 `List<VisitaDoParticipanteResponse>` | 19 · T76 |
| 2 | `POST /api/participacoes/{participacaoId}/visitas-agendadas` | C | 201 `VisitaAgendadaResponse` | 19 · T76 |
| 3 | `PUT /api/visitas-agendadas/{visitaAgendadaId}` (reagendar) | C | 200 `VisitaAgendadaResponse` | 19 · T76 |
| 4 | `GET /api/estudos/{estudoId}/agenda?de&ate&situacao&pagina&tamanho` | Leitura | 200 `PaginaResponse<AgendaItemResponse>` | 19 · T76 |
| 5 | `GET /api/estudos/{estudoId}/agenda/participantes` | Leitura | 200 `List<ParticipanteAgendaResponse>` | 19 · T76 |
| 6 | `GET /api/visitas-agendadas/{visitaAgendadaId}` | Leitura | 200 `VisitaAgendadaResponse` | 20 · T82 |
| 7 | `POST /api/visitas-agendadas/{visitaAgendadaId}/desfecho` | C | 200 `VisitaAgendadaResponse` | 20 · T82 |
| 8 | `GET /api/visitas-agendadas/{visitaAgendadaId}/resultados` | Leitura | 200 `ResultadosDaVisitaResponse` | 21 · T90 |
| 9 | `PUT /api/visitas-agendadas/{visitaAgendadaId}/resultados/{acaoId}` | P | 201 (primeiro registro) ou 200 (edição) `ResultadoAtividadeResponse` | 21 · T90 |
| 10 | `GET /api/participacoes/{participacaoId}/historico` | Leitura | 200 `HistoricoParticipanteResponse` | 22 · T96 |
| 11 | `POST /api/participacoes/{participacaoId}/retirada` | C | 201 `RetiradaResponse` | 23 · T103 |
| 12 | `GET /api/participacoes/{participacaoId}/retirada` | Leitura | 200 `RetiradaResponse` (404 se não houver) | 23 · T103 |
| 13 | `GET /api/me/estudos` | Part | 200 `List<MeuEstudoResponse>` | 24 · T109 |
| 14 | `GET /api/me/estudos/{estudoId}` | Part | 200 `MeuEstudoDetalheResponse` (404 se não for do participante) | 24 · T109 |
| 15 | `PUT /api/participacoes/{participacaoId}/conta` | C | 200 `ContaVinculadaResponse` | 24 · T111 |
| 16 | `GET /api/participacoes/{participacaoId}/conta` | Leitura | 200 `ContaVinculadaResponse` (404 se não houver) | 24 · T111 |
| 17 | `GET /api/contas-participante/disponiveis` | C | 200 `List<ContaParticipanteResponse>` | 24 · T111 |
| 18 | `GET /api/me/visitas/proximas?estudoId` | Part | 200 `List<MinhaVisitaResponse>` | 25 · T116 |
| 19 | `GET /api/me/visitas/historico?estudoId&pagina&tamanho` | Part | 200 `HistoricoMinhasVisitasResponse` | 25 · T116 |

Os caminhos não colidem com o regex do `ContratoOpenApiTest` da Sprint 2 (`estudos/{id}/(transicoes|historico-status|criterios|visitas|candidatos|participantes)`, `visitas/.*`, `candidatos/.*`): por isso usam `participacoes/`, `visitas-agendadas/`, `agenda` e `me/`. A **T74** cria `ContratoOpenApiSprint3Test` com o mesmo método do da Sprint 2 (todo caminho do YAML existe na API e todo caminho da Sprint 3 da API está no YAML) e cada PBI atualiza a contagem esperada.

Mudanças em respostas existentes (compatíveis, só acrescentam campos): `ParticipacaoResponse` ganha `situacao` (`ATIVO`/`RETIRADO`) na **T101/T104**; o `VagasResponse` **não muda** (a vaga de quem se retira continua ocupada).

## 3. Formato dos DTOs

Todos em `UEPB.ESWII.ClinicalStudyPB.controller.dto` (records), com `@Schema` nos campos, no mesmo estilo dos da Sprint 2. Datas e horas: **entrada** local sem fuso (`yyyy-MM-ddTHH:mm` ou `yyyy-MM-ddTHH:mm:ss`), **saída** `OffsetDateTime` ISO 8601 (`2026-11-19T09:30:00-03:00`); datas puras são `yyyy-MM-dd`.

```jsonc
// VisitaDoParticipanteResponse (um item por visita prevista do estudo, ordenado por "ordem")
{ "visitaPlanejadaId": "uuid", "nome": "Retorno 30 dias", "ordem": 2, "diaPrevisto": 30, "tolerancia": 5,
  "dataSugerida": "2026-11-19", "janelaInicio": "2026-11-14", "janelaFim": "2026-11-24",
  "agendamento": null }                       // ou um VisitaAgendadaResponse

// VisitaAgendadaRequest (POST)         // VisitaReagendamentoRequest (PUT)
{ "visitaPlanejadaId": "uuid", "dataHora": "2026-11-19T09:30" }      { "dataHora": "2026-11-20T10:00" }

// VisitaAgendadaResponse
{ "id": "uuid", "participacaoId": "uuid", "estudoId": "uuid", "visitaPlanejadaId": "uuid",
  "visitaNome": "Retorno 30 dias", "participanteCodigo": "CAND-001", "participanteNome": "Pessoa Teta (Ficticia)",
  "dataHora": "2026-11-19T09:30:00-03:00", "situacao": "AGENDADA",           // AGENDADA | REALIZADA | AUSENTE | CANCELADA
  "dataHoraRealizada": null, "motivo": null, "observacao": null,
  "janelaInicio": "2026-11-14", "janelaFim": "2026-11-24",
  "criadoPor": "Coordenador Demo (Ficticio)", "criadoEm": "2026-11-11T10:00:00-03:00",
  "alteradoPor": "Coordenador Demo (Ficticio)", "alteradoEm": "2026-11-11T10:00:00-03:00", "versao": 0 }

// AgendaItemResponse (item da agenda do estudo)
{ "visitaAgendadaId": "uuid", "participacaoId": "uuid", "participanteCodigo": "CAND-001", "participanteNome": "Pessoa Teta (Ficticia)",
  "visitaPlanejadaId": "uuid", "visitaNome": "Retorno 30 dias", "dataHora": "2026-11-19T09:30:00-03:00", "situacao": "AGENDADA" }

// ParticipanteAgendaResponse (T77: "X de N visitas agendadas")
{ "participacaoId": "uuid", "candidatoId": "uuid", "codigo": "CAND-001", "nomeFicticio": "Pessoa Teta (Ficticia)",
  "dataInclusao": "2026-10-20", "visitasPrevistas": 3, "visitasAgendadas": 2 }   // "agendadas" = qualquer situação, pois já têm agendamento

// DesfechoRequest (POST .../desfecho)
{ "tipo": "REALIZADA",                    // REALIZADA | AUSENTE | CANCELADA (enum TipoDesfecho)
  "dataHoraRealizada": "2026-11-19T09:40", // só REALIZADA; omitido = agora
  "motivo": null,                          // CANCELADA: obrigatório, 5 a 500 caracteres
  "observacao": null }                     // AUSENTE e REALIZADA: opcional, até 500 caracteres

// ResultadosDaVisitaResponse
{ "visitaAgendadaId": "uuid", "situacaoVisita": "REALIZADA",
  "bloqueio": null,                        // null | VISITA_NAO_REALIZADA | ESTUDO_ENCERRADO | PARTICIPANTE_RETIRADO
  "total": 1, "registrados": 1, "pendentes": 0,
  "atividades": [ { "acaoId": "uuid", "nome": "Aferir pressão arterial", "descricao": "Medida simulada",
      "resultado": { "valor": "120/80", "unidade": "mmHg", "observacao": null,
        "registradoPor": "Pesquisador Demo (Ficticio)", "registradoEm": "2026-11-11T11:00:00-03:00",
        "alteradoPor": "Pesquisador Demo (Ficticio)", "alteradoEm": "2026-11-11T11:00:00-03:00" } } ] }   // resultado null = pendente

// ResultadoRequest (PUT .../resultados/{acaoId})
{ "valor": "120/80", "unidade": "mmHg", "observacao": "Sentado, após 5 minutos" }   // valor 1-200; unidade ≤30; observacao ≤500

// HistoricoParticipanteResponse
{ "participante": { "participacaoId": "uuid", "candidatoId": "uuid", "codigo": "CAND-001", "nomeFicticio": "Pessoa Teta (Ficticia)",
                    "dataInclusao": "2026-10-20" },     // T104 acrescenta "situacao" e "retirada" {dataRetirada, motivo, solicitante}
  "resumo": { "previstas": 3, "realizadas": 1, "ausentes": 0, "canceladas": 0, "agendadas": 1, "naoAgendadas": 1, "pendentes": 2 },
  "visitas": [ { "visitaPlanejadaId": "uuid", "nome": "Triagem", "ordem": 1, "diaPrevisto": 0, "tolerancia": 3,
      "dataSugerida": "2026-10-20", "situacao": "REALIZADA",       // NAO_AGENDADA | AGENDADA | REALIZADA | AUSENTE | CANCELADA
      "visitaAgendadaId": "uuid", "dataHoraAgendada": "…", "dataHoraRealizada": "…", "motivo": null, "observacao": null,
      "resultados": [ { "acaoId": "uuid", "nome": "Aferir pressão arterial", "valor": "120/80", "unidade": "mmHg", "observacao": null } ] } ] }
// pendentes = previstas sem agendamento + AGENDADAS

// RetiradaRequest (POST)                                      // RetiradaResponse
{ "motivo": "Mudança de cidade",                               { "id": "uuid", "participacaoId": "uuid", "motivo": "…",
  "solicitante": "PARTICIPANTE",                                 "solicitante": "PARTICIPANTE", "dataRetirada": "2026-11-18",
  "dataRetirada": "2026-11-18" }                                 "registradoPor": "…", "registradoEm": "…", "visitasCanceladas": 2 }
// solicitante: PARTICIPANTE | PESQUISADOR | COORDENACAO

// MeuEstudoResponse                          // MeuEstudoDetalheResponse (= MeuEstudoResponse + os campos abaixo)
{ "participacaoId": "uuid", "estudoId": "uuid", "titulo": "…", "resumo": "(objetivo cortado em 160 caracteres)",
  "statusEstudo": "EXECUCAO", "dataInclusao": "2026-10-20", "situacaoParticipacao": "ATIVO" }
// detalhe: "objetivo": "(completo)", "dataInicio": "2026-11-01", "dataFim": "2027-05-01", "responsavelNome": "…",
//          "retirada": null                  // ou { "dataRetirada": "2026-11-18" } (o motivo não é exposto ao app)

// ContaParticipanteResponse                  // ContaVinculadaResponse
{ "usuarioId": "uuid", "nome": "…", "email": "…" }        { "participacaoId": "uuid", "usuarioId": "uuid", "nome": "…", "email": "…" }
// vínculo: PUT body { "usuarioId": "uuid" }

// MinhaVisitaResponse (nunca traz resultados: não fazem parte do RF18)
{ "id": "uuid", "participacaoId": "uuid", "estudoId": "uuid", "estudoTitulo": "…", "visitaNome": "Retorno 30 dias",
  "dataHora": "2026-11-19T09:30:00-03:00", "situacao": "AGENDADA", "dataHoraRealizada": null, "motivo": null,
  "atividades": ["Escala de dor", "Exame físico"] }

// HistoricoMinhasVisitasResponse
{ "resumo": { "realizadas": 3, "ausentes": 1, "canceladas": 1 },
  "conteudo": [ MinhaVisitaResponse ], "pagina": 0, "tamanho": 20, "totalElementos": 5, "totalPaginas": 1 }
```

Limites de validação (`jakarta.validation`, mensagens em português como na Sprint 2): `motivo` 5 a 500 caracteres após `trim`; `observacao` até 500; `valor` 1 a 200; `unidade` até 30; `tamanho` de página de 1 a 100 (padrão 20).

## 4. Códigos de erro novos

Formato único (o da Sprint 2): `{ status, codigoRegra, mensagem, errosCampo[], erro, codigo }`. **409** = regra de negócio (`ConflitoRegraException`), **400** = dado inválido com código próprio (`ValidacaoRegraException`, nova) ou `VALIDACAO_CAMPOS` (campo a campo, do Bean Validation), **404** = `RECURSO_NAO_ENCONTRADO`, **403** = `ACESSO_NEGADO`.

| Código | HTTP | Regra | Task que cria |
|---|---|---|---|
| `ESTUDO_FORA_DE_RECRUTAMENTO_OU_EXECUCAO` | 409 | Agendar, reagendar e registrar desfecho exigem estudo em recrutamento ou execução (regra 1) | T75 |
| `VISITA_JA_AGENDADA` | 409 | Já existe agendamento para essa visita prevista e esse participante (regra 2) | T75 |
| `DATA_FORA_DA_JANELA` | 409 | Data fora de `inclusão + dia previsto ± tolerância`; a mensagem traz a janela válida (regra 2) | T75 |
| `DATA_NO_PASSADO` | 400 | Data de agendamento ou reagendamento anterior a hoje (regra 2) | T75 |
| `VISITA_NAO_ESTA_AGENDADA` | 409 | Reagendar visita que já tem desfecho (regra 3) | T75 |
| `TRANSICAO_VISITA_INVALIDA` | 409 | Desfecho a partir de situação diferente de `AGENDADA` (regra 3) | T80 |
| `DATA_REALIZACAO_FUTURA` | 400 | `dataHoraRealizada` maior que agora (regra 3) | T80 |
| `MOTIVO_OBRIGATORIO` | 400 | Cancelamento sem motivo de 5 ou mais caracteres; também na retirada | T80 (reutilizado na T102) |
| `VISITA_AINDA_NAO_OCORREU` | 409 | `REALIZADA` ou `AUSENTE` para visita cuja data agendada é futura (T81) | T81 |
| `VISITA_ALTERADA_CONCORRENTEMENTE` | 409 | Dois desfechos ao mesmo tempo na mesma visita; o segundo recebe este erro | T81 |
| `VISITA_NAO_REALIZADA` | 409 | Resultado só existe para visita `REALIZADA` (regra 4) | T89 |
| `ATIVIDADE_FORA_DA_CATEGORIA_AVALIACAO` | 409 | Só atividades `AVALIACAO_CLINICA` recebem resultado (regra 4) | T89 |
| `ATIVIDADE_NAO_PERTENCE_A_VISITA` | 409 | A atividade não é da visita prevista desta visita agendada | T89 |
| `ESTUDO_ENCERRADO` | 409 | Resultado (edição) e retirada não valem com o estudo encerrado | T89 (reutilizado na T102) |
| `VALOR_OBRIGATORIO` | 400 | Valor do resultado em branco | T89 |
| `PARTICIPANTE_RETIRADO` | 409 | Agendar, registrar desfecho, registrar resultado ou vincular conta de participante retirado (regra 6) | T102 (T75, T81, T89) e T111 |
| `PARTICIPANTE_JA_RETIRADO` | 409 | Segunda retirada do mesmo participante (irreversível) | T102 |
| `DATA_RETIRADA_FUTURA` | 400 | `dataRetirada` posterior a hoje | T102 |
| `CONTA_NAO_E_PARTICIPANTE` | 409 | O usuário informado não tem perfil participante | T111 |
| `CONTA_INATIVA` | 409 | A conta está desativada | T111 |
| `CONTA_JA_VINCULADA` | 409 | A conta já pertence a outro participante (relação um para um) | T111 |
| `PARTICIPACAO_JA_TEM_CONTA` | 409 | O participante já tem uma conta vinculada | T111 |

Ordem das verificações em cada serviço: **existência (404) → estado do estudo → situação da participação → regras do dado**. O 403 vem antes de tudo, do `@PreAuthorize`.

## 5. Banco de dados

| Migration | Conteúdo |
|---|---|
| `V6_19_1__create_scheduled_visits.sql` (T73) | Tabela `visitas_agendadas`: `id UUID`, `participacao_id` (FK `participacoes`), `visita_planejada_id` (FK `visitas_planejadas`), `data_hora_agendada TIMESTAMP NOT NULL`, `situacao VARCHAR(20) NOT NULL DEFAULT 'AGENDADA'`, `data_hora_realizada TIMESTAMP`, `motivo VARCHAR(500)`, `observacao VARCHAR(500)`, `versao BIGINT NOT NULL DEFAULT 0`, `criado_por_id`, `criado_em`, `alterado_por_id`, `alterado_em` (FKs para `usuarios`). `UNIQUE (participacao_id, visita_planejada_id)` (`uk_visitas_agendadas_participacao_visita`), `CHECK` da situação (`chk_visitas_agendadas_situacao`), `CHECK` de campos por situação (realizada exige `data_hora_realizada`; cancelada exige `motivo`) e índices em `participacao_id` e em `(data_hora_agendada, situacao)` |
| `V6_21_1__create_evaluation_results.sql` (T87) | Tabela `resultados_atividade`: `id`, `visita_agendada_id` (FK), `acao_id` (FK `acoes`, `ON DELETE RESTRICT`), `valor VARCHAR(200) NOT NULL`, `unidade VARCHAR(30)`, `observacao VARCHAR(500)`, `registrado_por_id`, `registrado_em`, `alterado_por_id`, `alterado_em`; `UNIQUE (visita_agendada_id, acao_id)` |
| `V6_23_1__participant_withdrawal.sql` (T101) | Coluna `participacoes.situacao VARCHAR(20) NOT NULL DEFAULT 'ATIVO'` com `CHECK (situacao IN ('ATIVO','RETIRADO'))`; tabela `retiradas_participacao`: `id`, `participacao_id UNIQUE` (FK), `motivo VARCHAR(500) NOT NULL`, `solicitante VARCHAR(20) NOT NULL` (`CHECK` em `PARTICIPANTE`, `PESQUISADOR`, `COORDENACAO`), `data_retirada DATE NOT NULL`, `registrado_por_id`, `registrado_em TIMESTAMP NOT NULL` |
| `V6_24_1__unique_candidate_account.sql` (T111) | `CREATE UNIQUE INDEX uk_candidatos_usuario ON candidatos (usuario_id)` (vários `NULL` continuam permitidos); garante o um para um da regra 7 |

Regras gerais: `CREATE TABLE IF NOT EXISTS`, nomes de constraints como os da `V4`, nenhuma migration altera ou apaga dados da Sprint 2, e cada uma é testada em banco vazio e em banco populado pela Sprint 2 (`FlywayMigrationIntegrationTest` e o job `Backend (PostgreSQL)`).

## 6. Nomes no código

**Backend** (pacote base `UEPB.ESWII.ClinicalStudyPB`)

| Camada | Classes novas |
|---|---|
| `domain.enums` | `SituacaoVisita` (`AGENDADA`, `REALIZADA`, `AUSENTE`, `CANCELADA`), `TipoDesfecho` (`REALIZADA`, `AUSENTE`, `CANCELADA`), `SituacaoParticipacao` (`ATIVO`, `RETIRADO`), `SolicitanteRetirada` (`PARTICIPANTE`, `PESQUISADOR`, `COORDENACAO`) |
| `domain.model` | `VisitaAgendada`, `ResultadoAtividade`, `RetiradaParticipacao`; `Participacao` ganha `situacao` |
| `domain.regra` | `MaquinaEstadosVisita` (pura, sem Spring), `JanelaDeAgendamento` (record com início, fim e `contem`, usada pelo agendamento e pela sugestão de datas) |
| `repository` | `VisitaAgendadaRepository`, `ResultadoAtividadeRepository`, `RetiradaParticipacaoRepository` |
| `service` | `ValidacaoRegraException`, `AgendamentoService`, `AgendaService`, `DesfechoVisitaService`, `ResultadoAtividadeService`, `HistoricoParticipanteService`, `RetiradaService`, `VinculoContaService`, `AcessoParticipante`, `MeusEstudosService`, `MinhasVisitasService` |
| `controller` | `AgendamentoController`, `AgendaController`, `VisitaAgendadaController`, `ResultadoAtividadeController`, `HistoricoParticipanteController`, `RetiradaController`, `ContaParticipanteController`, `MeController`; `Permissoes` ganha `PARTICIPANTE = "hasRole('PARTICIPANTE')"` (T109) e `ESCRITA_RESULTADOS = "hasRole('PESQUISADOR')"` (T90) |
| `config` | Bean `Clock` (fuso de `app.fuso-horario`, variável `FUSO_HORARIO`, padrão `America/Fortaleza`) |

**Web** (`web/ClinicalStudyPB/src`)

| Item | Arquivo |
|---|---|
| Módulos de API | `api/agendamento.ts` (`agendamentoApi`), `api/visitasAgendadas.ts` (`visitasAgendadasApi`: buscar, desfecho, resultados), `api/historico.ts`, `api/retirada.ts`, `api/contaParticipante.ts`; tipos gerados em `api/schema-sprint3.d.ts` e reexportados em `api/tipos.ts` (script `npm run api:tipos` passa a gerar os dois arquivos) |
| Rotas novas (todas sob `/estudos/:estudoId`) | `agenda` (aba, `AbaAgenda`), `participantes/:participacaoId` (`FichaParticipantePage`), `participantes/:participacaoId/agendamento` (`AgendamentoParticipantePage`), `participantes/:participacaoId/visitas/:visitaAgendadaId` (`VisitaAgendadaPage`) |
| Pasta das telas | `pages/estudos/detalhe/acompanhamento/` (`AbaAgenda`, `AgendamentoParticipantePage`, `VisitaAgendadaPage`, `AcoesDaVisita`, `SeloSituacaoVisita`, `DialogoDesfecho`, `AbaAvaliacoesVisita`, `FichaParticipantePage`, `LinhaDoTempoVisitas`, `AreaAcoesParticipante`, `DialogoRetirada`, `DialogoVincularConta`) e testes `*.test.tsx` ao lado |
| Abas do estudo | `abas.ts`: `participantes` passa a `['PESQUISADOR', 'COORDENADOR']` e entra `{ caminho: 'agenda', rotulo: 'Agenda', perfis: ['PESQUISADOR', 'COORDENADOR'] }` |
| Fonte única das abas da visita | `acompanhamento/abasVisita.ts` (como `abas.ts`): `dados` (T77/T83) e `avaliacoes` (T91, perfis P e C) |
| Mensagens por código | `acompanhamento/mensagensAcompanhamento.ts` (como `mensagensInclusao.ts`): traduz os códigos da seção 4 |

**E2E** (`web/ClinicalStudyPB/e2e`): specs `agendamento.spec.ts` (T79), `desfecho-visita.spec.ts` (T86), `resultados-visita.spec.ts` (T93), `fluxo-acompanhamento.spec.ts` (T99), `retirada.spec.ts` (T107); page objects `AgendaPage`, `AgendamentoParticipantePage`, `VisitaAgendadaPage`, `FichaParticipantePage`; novos métodos em `support/api.ts` (`ApiE2E`): `participanteIncluido`, `agendar`, `registrarDesfecho`, `registrarResultado`, `retirar`. Capturas em `test-results/capturas/` com o padrão `rf12-agendamento-sucesso`, `rf12-agendamento-erro-janela`, `rf13-desfecho-realizada`, `rf13-desfecho-erro-motivo`, `rf14-resultado-registrado`, `rf14-resultado-erro-valor`, `rf15-historico`, `rf16-retirada-confirmacao`, `rf16-retirada-historico`, `rf17-login-mobile`, `rf17-meus-estudos`, `rf18-proximas-visitas`, `rf18-historico-comparecimento`, `rf18-erro-conexao`; as imagens finais para o README vão para `docs/imagens/sprint3/`.

**Mobile** (`mobile/ClinicalStudyPB/src`): `api/cliente.ts` (cliente HTTP, T108), `api/schema.d.ts` (tipos gerados do YAML da Sprint 3), `api/mocks/` (respostas em memória quando `EXPO_PUBLIC_USE_MOCKS=true`), `api/estudos.ts` e `api/visitas.ts` (módulos por recurso), `utils/datas.ts` (formatação em pt-BR), `components/` (`Selo`, `EstadoVazio`, `EstadoErro` e `Carregando` novos; `Tela`, `Cartao` e `Botao` já existem em `ui.tsx`), telas `app/(tabs)/estudos.tsx`, `app/estudos/[estudoId].tsx`, `app/(tabs)/visitas.tsx`, `app/visitas/[visitaId].tsx`; testes em `__tests__/` ao lado dos arquivos; fluxos Maestro em `.maestro/`.

## 7. Dados de demonstração (perfil `demo`)

Cada PBI acrescenta um `R__demo_NN_*.sql` em `backend/ClinicalStudyPB/src/main/resources/db/demo/`, com `INSERT ... SELECT ... WHERE NOT EXISTS`, ids fixos no padrão `d6300000-0000-4000-8000-0000000019NN` (`19` = PBI-19; `NN` de 01 a 99) e datas relativas a `CURRENT_DATE`. Hoje = **D**. Senha de todos os usuários: `senha-de-teste`.

**Estudo "Demo - Acompanhamento de participantes"** (`...-000000001901`, criado no PBI-19, em `EXECUCAO`, limite 6, responsável `demo.pesquisador@clinicalstudy.test`, com 1 critério de inclusão). Visitas previstas: **Triagem** (dia 0, tolerância 3), **Retorno 30 dias** (dia 30, tolerância 5) e **Retorno 60 dias** (dia 60, tolerância 7). Atividades: Triagem tem "Aferir pressão arterial" (`AVALIACAO_CLINICA`) e "Coletar sangue" (`EXAME_LABORATORIAL`); Retorno 30 tem "Escala de dor" (`QUESTIONARIO`) e "Exame físico" (`AVALIACAO_CLINICA`); Retorno 60 tem "Avaliação final" (`AVALIACAO_CLINICA`) e "Entrega de medicação" (`MEDICACAO`).

| Participante | Inclusão | PBI que o cria | Visitas |
|---|---|---|---|
| P1 · CAND-001 "Pessoa Teta (Ficticia)" | D-20 | 19 | Triagem `REALIZADA` (agendada D-20 09:00, realizada 09:10); Retorno 30 `AGENDADA` em D+10 09:30; Retorno 60 sem agendamento |
| P2 · CAND-002 "Pessoa Iota (Ficticia)" | D-3 | 19 | Triagem `AGENDADA` em D 15:00; demais sem agendamento |
| P3 · CAND-003 "Pessoa Kappa (Ficticia)" | D-1 | 19 | Nenhuma visita agendada (para demonstrar o agendamento) |
| P4 · CAND-004 "Pessoa Lambda (Ficticia)" | D-35 | 20 | Triagem `REALIZADA`; Retorno 30 `AUSENTE` (D-4); Retorno 60 `CANCELADA` (motivo "Participante em viagem") |
| P5 · CAND-005 "Pessoa Mi (Ficticia)" | D-10 | 20 | Triagem `CANCELADA` (motivo "Consulta remarcada pelo centro"); Retorno 30 `AGENDADA` em D+20 |
| P6 · CAND-006 "Pessoa Ni (Ficticia)" | D-15 | 23 | `RETIRADO` (motivo "Mudança de cidade", solicitante `PARTICIPANTE`, data D-2); Triagem `REALIZADA`; Retorno 30 `CANCELADA` com motivo "retirada do estudo" |

PBI-21 acrescenta resultados em "Aferir pressão arterial" da Triagem de P1 (`120/80` mmHg) e de P4 (`130/85` mmHg), deixando as demais avaliações pendentes. PBI-24 vincula `participante@clinicalstudy.test` (seed V2) a P1 e `demo.participante@clinicalstudy.test` a P6 (mostra o estado retirado no app). Com os 6 participantes o estudo fica **6 de 6 vagas** (a retirada de P6 não libera a vaga). O estudo de recrutamento da T63 (`...000000000102`) **não é alterado**, para manter o roteiro da apresentação da Release 1.

## 8. Pontos de encaixe (telas compartilhadas)

Como dois integrantes nunca trabalham ao mesmo tempo, quem entrega a tela deixa o encaixe pronto e documentado, e quem vem depois só acrescenta:

| Encaixe | Entregue por | Quem usa |
|---|---|---|
| `AcoesDaVisita` (botões de ação da visita) em `VisitaAgendadaPage` e na linha da agenda | T77 (Reagendar) | T83 (Registrar desfecho) |
| `abasVisita.ts` (abas da tela da visita) | T77 (`dados`) | T91 (`avaliacoes`) |
| `SeloSituacaoVisita` (rótulo + cor) | T83 | T97 (histórico), T118 (espelho no app) |
| `AreaAcoesParticipante` na `FichaParticipantePage` (lista de botões por perfil e situação) | T97 | T104 (Registrar retirada), T111 (Vincular conta) |
| Esqueleto do app (cliente, tipos, mocks, Jest, componentes base) | T108 | T110, T112, T117, T118 |

## 9. Padrões de teste

- **Backend:** `@SpringBootTest` + MockMvc com tokens reais (`TokenDeTeste`), como na Sprint 2; `CenarioRecrutamento` (pacote `support`) ganha `participanteIncluido(mvc, tokenPesquisador, tokenCoordenador, limite)`, `visitaPrevista(mvc, token, estudoId, nome, dia, tolerancia)` e `agendar(mvc, tokenCoordenador, participacaoId, visitaPlanejadaId, dataHora)`. A matriz de regras usa Mockito (serviço isolado) com um `Clock` fixo. Testes de concorrência não usam `@Transactional` e limpam os dados no `@AfterEach` (modelo: `ParticipacaoConcorrenciaTest`). Cada task de teste confere a cobertura de `domain` e `service` da fatia (o CI bloqueia abaixo de 80%).
- **PostgreSQL real:** o job `Backend (PostgreSQL)` já roda toda a suíte; quando o planejamento diz "com PostgreSQL real", é esse job (localmente, `SPRING_DATASOURCE_*`).
- **Web:** Vitest + Testing Library com `renderizarAba` e `erroHttp` (`src/test/ambiente.tsx`) e a API simulada com `vi.mock` (padrão das telas da Sprint 2); estados obrigatórios em toda tela: carregamento, lista vazia, erro, somente leitura por perfil.
- **E2E web:** Playwright com `ApiE2E` montando o estado de partida pela API e o fluxo testado pela interface; dados únicos por execução; `test.step` e capturas nomeadas.
- **Mobile:** Jest (`jest-expo`) + `@testing-library/react-native`, com `@/api/cliente` simulado; **Maestro** para o fluxo E2E local.
- **Regressão:** `README` (seção de testes) e o relatório de testes são atualizados no fim da Sprint; os 434 + 133 + 29 testes da Release 1 continuam passando.

## 10. Ordem de integração

`PBI-19 (I1)` → `PBI-20 (I3)` → `PBI-21 (I6)` → `PBI-22 (I2)` → `PBI-23 (I4)` → `PBI-24 (I7)` → `PBI-25 (I5)`. Cada Pull Request é revisado pelo dono do PBI seguinte (o último, por I1). Um PBI só começa com o anterior 100% na `main`. As passagens de bastão e o que cada uma entrega estão no [índice](README.md).

## 11. Definition of Done e requisitos transversais da especificação

### Definition of Done (texto do planejamento)

> Uma task ou PBI só é concluído quando: os critérios de aceite estão atendidos e a funcionalidade é demonstrável com dados fictícios; o código foi revisado por outro integrante, passou no CI e entrou na branch principal por Pull Request; existem testes automatizados (unitários, de integração ou de componente) que passam e, no backend, a cobertura de linhas do código de negócio permanece em 80% ou mais; erros e validações das regras de negócio são tratados e mostrados com mensagens claras; o Swagger UI e o contrato YAML refletem os endpoints; os dados de demonstração da fatia estão no perfil demo; documentação e capturas de tela relevantes estão no repositório; e os requisitos RF01 a RF11 das Sprints anteriores continuam funcionando. Para o PBI, vale ainda que todas as suas tasks estão com Remaining Work zerado ao fim da janela, porque o PBI seguinte só começa depois disso.

### Requisitos da especificação que atravessam a Sprint 3

Cada documento de task cita, na sua seção 9.5, os que se aplicam a ela. O conjunto completo:

| Tema | Texto da especificação |
|---|---|
| API REST e OpenAPI | Regra 9.2 (tecnologia): “O backend deve disponibilizar uma **API HTTP REST** compartilhada pela web e pelo mobile, documentada no padrão **OpenAPI** (Swagger UI pode ser usado para consulta e testes).” |
| Persistência, migrações e dados fictícios | Regra 9.5 e Entregas: “O sistema deve ter persistência de dados e controle de acesso por perfil.” e “Persistência dos dados, com scripts de criação/migração do banco e dados fictícios para demonstração.” |
| Erros e validações | Entregas: “Tratamento de erros e validações das regras de negócio.” |
| Testes e cobertura do backend | Estratégia de testes (seção 7): “Cobertura mínima de **80% das linhas do código de negócio do backend** (excluindo código gerado e configurações)”, “Testes unitários para todas as regras de negócio relevantes”, “Testes de integração para os principais endpoints” e “Execução automatizada dos testes a cada pull request ou integração à branch principal”. Regra 9.6: “As equipes devem implementar testes automatizados e apresentar evidências de cobertura.” |
| Testes de componentes web e perfis da web | Estratégia de testes: “Testes automatizados de componentes nas interfaces web e mobile.” Em componentes web, testar o “comportamento das telas: validação de formulários, botões, mensagens de erro”. Regra 9.3: “A interface **web** atende aos perfis de pesquisador, coordenador e administrador.” |
| Testes de componentes mobile e perfil do app | Estratégia de testes: em componentes mobile, testar o “comportamento das telas mobile: login, listagem de visitas, confirmação”. Regra 9.4: “A interface **mobile** atende ao perfil de participante e executa em dispositivo ou emulador.” |
| Fluxos E2E | Estratégia de testes: “Pelo menos um fluxo E2E automatizado na web e um no mobile.” (exemplo da especificação: “Cadastrar estudo, incluir participante e agendar visita”). |
| README: funcionalidades com capturas | README principal, seção 3: “**Funcionalidades desenvolvidas** — requisitos por código RF, comportamento e capturas web e mobile, incluindo casos de sucesso e de erro/validação.” |
| Requisitos anteriores | Entregas: “Os requisitos de releases anteriores devem continuar funcionando.” (RF01 a RF11 continuam funcionando). |
| Credenciais | Entregas: arquivos de configuração para instalar, executar e testar “**sem senhas ou credenciais reais**” (os dados de demonstração são fictícios). |
| Processo de desenvolvimento | README de cada release, seção **Processo de desenvolvimento** por sprint: (1) Backlog e tarefas, (2) Organização da equipe, (3) Burndown (“gráfico do Azure DevOps com breve análise planejado × realizado”) e (4) Revisão (“o que foi entregue, impedimentos e ações de melhoria”). Gerenciamento ágil: Azure DevOps. |
| Mobile | Para o mobile “basta uma aplicação executável em emulador ou dispositivo; não é necessário publicar nas lojas”. |
