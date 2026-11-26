# Convenções e contrato compartilhado da Sprint 4

> **Leia este documento antes de qualquer task da Sprint 4.** Ele fixa, em um só lugar, os nomes, endpoints, DTOs, códigos de erro, tabelas, pontos de encaixe e dados de demonstração que as 49 tasks (T122 a T170) usam. Como cada PBI é feito por uma pessoa diferente, em sequência, é aqui que todos confirmam que estão falando da mesma coisa. Se uma task precisar mudar algo daqui, a mudança entra neste arquivo **no mesmo Pull Request**.
>
> Os documentos seguem o [Planejamento da Sprint 4](../../sprint4/Planejamento_Sprint_4_ClinicalStudyPB.md). Onde o planejamento pedia algo que não funciona com o código real das Sprints 1 a 3, a seção 1 explica o ajuste.

## 1. Ajustes em relação ao planejamento (e por quê)

Os ajustes marcados com **PO** mudam comportamento ou preenchem uma lacuna das regras: devem ser confirmados no Planning (T164). Enquanto não houver resposta, vale o que está aqui.

| # | Planejamento diz | Este design faz | Motivo |
|---|---|---|---|
| 1 | Migrations nas faixas `V4_26_*` a `V4_32_*`, "acima das faixas V3 da Sprint 3" (regra 8) | Prefixo **`V7_`**: `V7_26_1__participant_requests.sql` (T122) e `V7_30_1__candidate_registration_date.sql` (T151) | O banco já está na **`V6_24_1`**. O Flyway ordena por versão e, com `outOfOrder` desligado e `validate-on-migrate` ligado, recusaria `V4_26_1` em qualquer banco existente |
| 2 | Scripts de demonstração versionados (`V4_26_2__demo_...`, `V4_28_1__demo_...`) | Scripts **repetíveis** em `db/demo/`: `R__demo_26_solicitacoes.sql` e `R__demo_28_pedidos_retirada.sql` | Padrão das Sprints 2 e 3. Um script versionado em `db/demo` quebraria a validação quando o perfil `demo` fosse desligado |
| 3 | Entidade `ParticipantRequest` e caminhos `/me` | Código e caminhos em **português**: entidade `SolicitacaoParticipante`, tabela `solicitacoes_participante`, caminhos `/api/me/...` | Padrão do projeto. O nome do arquivo de migration continua em inglês, como os anteriores |
| 4 | Dados de demonstração dos pedidos (T128, T142) | Um **estudo novo**, "Demo - Solicitações do participante", com participantes próprios e uma conta nova (seção 7) | O estudo de demonstração da Sprint 3 está com 6 de 6 vagas e é conferido por testes e pelos fluxos Maestro; alterá-lo quebraria os dois. A conta `participante@clinicalstudy.test` já está vinculada ao P1 e a relação é um para um |
| 5 | A confirmação é limpa quando o coordenador atende o pedido (regra 3) | **Toda** mudança de data limpa a confirmação, inclusive o reagendamento direto do RF12. **PO** | Uma confirmação vale para uma data. Se o coordenador reagendar direto, o pedido pendente (se houver) continua `PENDENTE` para ser respondido |
| 6 | Regras 1 e 2 não citam o estado do estudo | Confirmar, pedir reagendamento e pedir retirada exigem estudo **não encerrado**; confirmar e pedir reagendamento exigem estudo em **recrutamento ou execução**. **PO** | Sem isso, nasceriam pedidos que o coordenador não consegue atender (o RF12 e o RF16 recusam) |
| 7 | Nada sobre encerrar um estudo com pedidos pendentes | Ao encerrar o estudo, os pedidos de **reagendamento** pendentes são recusados com "Estudo encerrado antes do atendimento do pedido." Um pedido de **retirada** pendente permanece e aparece como "não pode ser efetivado: estudo encerrado". **PO** | A retirada não pode ser recusada (regra 5) e o `RetiradaService` recusa estudo encerrado |
| 8 | Motivo da retirada opcional no app (regra 4); a efetivação usa "o motivo informado no app" | O coordenador envia `motivo` e `dataRetirada` na efetivação. O diálogo vem preenchido com o motivo do app; se ele tiver menos de 5 caracteres, vem com "Retirada solicitada pelo participante pelo aplicativo". **PO** | O `RetiradaService` (T102) exige motivo de ao menos 5 caracteres (`MOTIVO_OBRIGATORIO`) |
| 9 | T143 lista quatro situações de candidato | O painel conta as **seis** de `SituacaoCandidato` (`CADASTRADO`, `EM_AVALIACAO`, `ELEGIVEL`, `NAO_ELEGIVEL`, `CONSENTIDO`, `INCLUIDO`) | Com quatro, a soma não bateria com o total de candidatos |
| 10 | Filtro do relatório pela data de **cadastro** (T151) | Nova coluna `candidatos.criado_em` (migration `V7_30_1`), preenchida para os antigos com a data mais antiga conhecida | A tabela `candidatos` **não guarda** a data de cadastro |
| 11 | Nome do CSV com o "código do estudo" (T152) | `relatorio-<tipo>-<8 primeiros caracteres do id do estudo>-<aaaa-mm-dd>.csv` | O estudo **não tem código** no modelo |
| 12 | Relatório de acompanhamento com realizadas, ausentes, canceladas e pendentes (T150) | Acrescenta as colunas **atrasadas** e **não agendadas** | Sem elas os totais não seriam iguais aos do painel, o que a T155 exige |
| 13 | Cada PBI "integra tudo na main" | Cada PBI entra por **Pull Request na `develop`**; a `develop` vai para a `main` também por Pull Request, no fim de cada janela | É o fluxo real do repositório, e o CI roda em `develop` e `main` |
| 14 | Imagem `passagem_de_bastao.png` | A tabela "Dependências e passagem de bastão" do planejamento vale como fonte | O arquivo não está em `docs/sprint4/` |
| 15 | Acrescentar os passos do RF19 ao fluxo Maestro da Sprint 3 (T128) | Fluxo separado, `.maestro/fluxo-solicitacoes.yaml` | A conta do fluxo da Sprint 3 (P1) tem uma única visita futura, o RF19 precisa de duas e muda os dados, o que impediria repetir aquele fluxo |
| 16 | E2E web sobre os dados de demonstração (T135, T149 e T156) | O teste monta os dados pela API; a conferência com a demonstração é manual | O job E2E do CI não carrega o perfil `demo`, e os testes usam dados únicos por execução |
| 17 | O E2E do painel segue o link das visitas atrasadas (T149) | A visita atrasada é criada pela API: agendada para **hoje às 00:00** no fuso da aplicação | O RF12 recusa só datas anteriores a hoje; assim o teste cobre o caso real sem mexer no banco |
| 18 | Cada indicador do painel abre a lista filtrada (T146) | A agenda ganha o parâmetro `atrasada` | Os filtros atuais, por data e situação, não separam o que já passou hoje |
| 19 | O app esconde as ações do RF19 com retirada pendente (T139) | `MinhaVisitaResponse` ganha também `retiradaPendente` | A tela da visita precisa explicar por que os botões sumiram |
| 20 | Smoke em todo Pull Request e regressão na branch principal (T158) | A regressão roda também no push na `develop` e no Pull Request para a `main` | É na `develop` que os PBIs se integram (ajuste 13) |
| 21 | Histórico do participante com no máximo 3 consultas (RQ-T96-01, Sprint 3) | Passa a 4 (T133) | Os pedidos de cada visita são uma leitura nova |
| 22 | CSV em UTF-8 com BOM e ponto e vírgula (T152) | Acrescenta proteção contra fórmula em campos de texto | Motivos são texto livre e o arquivo é aberto no Excel |
| 23 | A tag usa o commit candidato indicado na T163 (T169) | A tag pode estar em um commit posterior, **só com mudanças de documentação**, registradas | Os diagramas e os README (T166 a T168) são escritos depois da T163 e precisam estar na tag |

## 2. Mapa dos endpoints novos (14 operações)

Contrato novo em **`docs/api/openapi-sprint4.yaml`** (criado na T123). Cada PBI acrescenta só os caminhos da sua fatia. Perfis: **C** coordenador, **P** pesquisador, **Part** participante; o administrador nunca acessa. "Leitura" = P e C.

| # | Operação | Perfis | Sucesso | PBI · task |
|---|---|---|---|---|
| 1 | `POST /api/me/visitas/{visitaAgendadaId}/confirmacao` | Part | 200 `MinhaVisitaResponse` | 26 · T125 |
| 2 | `POST /api/me/visitas/{visitaAgendadaId}/pedidos-reagendamento` | Part | 201 `MinhaSolicitacaoResponse` | 26 · T125 |
| 3 | `GET /api/me/pedidos` | Part | 200 `List<MinhaSolicitacaoResponse>` | 26 · T125 |
| 4 | `GET /api/estudos/{estudoId}/solicitacoes?tipo&situacao&pagina&tamanho` | Leitura | 200 `PaginaResponse<SolicitacaoResponse>` | 27 · T129 |
| 5 | `GET /api/estudos/{estudoId}/solicitacoes/pendentes` | Leitura | 200 `ContagemPendentesResponse` | 27 · T129 |
| 6 | `POST /api/solicitacoes/{solicitacaoId}/atendimento` | C | 200 `SolicitacaoResponse` | 27 · T131 |
| 7 | `POST /api/solicitacoes/{solicitacaoId}/recusa` | C | 200 `SolicitacaoResponse` | 27 · T131 |
| 8 | `POST /api/me/estudos/{estudoId}/pedido-retirada` | Part | 201 `MinhaSolicitacaoResponse` | 28 · T137 |
| 9 | `POST /api/solicitacoes/{solicitacaoId}/retirada` | C | 201 `RetiradaResponse` | 28 · T138 |
| 10 | `GET /api/estudos/{estudoId}/painel` | Leitura | 200 `PainelEstudoResponse` | 29 · T145 |
| 11 | `GET /api/paineis/estudos` | Leitura | 200 `List<ResumoDoEstudoResponse>` | 29 · T145 |
| 12 | `GET /api/estudos/{estudoId}/relatorio?de&ate` | Leitura | 200 `RelatorioEstudoResponse` | 30 · T153 |
| 13 | `GET /api/estudos/{estudoId}/relatorio/recrutamento.csv?de&ate` | Leitura | 200 `text/csv` | 30 · T153 |
| 14 | `GET /api/estudos/{estudoId}/relatorio/acompanhamento.csv?de&ate` | Leitura | 200 `text/csv` | 30 · T153 |

**Parâmetros novos em operação existente** (descritos no `openapi-sprint3.yaml`): `GET /api/estudos/{estudoId}/agenda` ganha `confirmada` (T133) e `atrasada` (T146).

### Testes de contrato (atenção)

- `ContratoOpenApiSprint3Test` usa o regex `me/.*`. Ao criar `/api/me/...` novos, o teste `apiNaoTemEndpointDaSprint3ForaDoContrato` **falha**. A T123 troca esse trecho por `me/(estudos(/\{estudoId})?|visitas/(proximas|historico))`.
- Criar `ContratoOpenApiSprint4Test` com o mesmo método do da Sprint 3 e o regex `^/api/(solicitacoes/.*|paineis/.*|me/(pedidos|visitas/\{visitaAgendadaId}/.*|estudos/\{estudoId}/pedido-retirada)|estudos/\{estudoId}/(solicitacoes(/.*)?|painel|relatorio(/.*)?))$`. Cada PBI atualiza a contagem: 3 (PBI-26), 7 (PBI-27), 9 (PBI-28), 11 (PBI-29) e 14 (PBI-30).
- Campos novos em DTOs que já existem (seção 3.2) são descritos no **`openapi-sprint3.yaml`**, onde o schema está: o teste da Sprint 3 compara os campos do YAML com os do record.
- Os scripts `api:tipos` da web e do app passam a gerar também `src/api/schema-sprint4.d.ts`.

## 3. Formato dos DTOs

Records em `controller.dto`, com `@Schema`. Entrada de data e hora local sem fuso (`yyyy-MM-ddTHH:mm`); saída `OffsetDateTime` no fuso do `Clock` da aplicação; datas puras `yyyy-MM-dd`.

### 3.1 DTOs novos

```jsonc
// PedidoReagendamentoRequest                    // PedidoRetiradaRequest
{ "motivo": "Consulta no mesmo horário",         { "cienteDosEfeitos": true, "motivo": null }
  "dataPreferida": "2026-11-30" }                // motivo 0..500
// motivo 5..500 após trim; dataPreferida opcional

// MinhaSolicitacaoResponse (app)
{ "id": "uuid", "tipo": "REAGENDAMENTO",         // REAGENDAMENTO | RETIRADA
  "estudoId": "uuid", "estudoTitulo": "…",
  "visitaAgendadaId": "uuid", "visitaNome": "Retorno A", "dataHoraVisita": "2026-11-28T09:00:00-03:00", // nulos na retirada
  "motivo": "…", "dataPreferida": "2026-11-30",
  "situacao": "PENDENTE",                        // PENDENTE | ATENDIDA | RECUSADA
  "resposta": null, "criadoEm": "…", "respondidoEm": null }

// SolicitacaoResponse (web) = MinhaSolicitacaoResponse sem estudoTitulo, mais:
{ "participacaoId": "uuid", "participanteCodigo": "CAND-001", "participanteNome": "…",
  "dataHoraAtual": "…", "janelaInicio": "2026-11-20", "janelaFim": "2026-12-04",   // nulos na retirada
  "respondidoPor": null }

// ContagemPendentesResponse          // AtendimentoRequest              // RecusaRequest (5..500)
{ "total": 3, "reagendamento": 2,     { "dataHora": "2026-11-30T10:00" } { "resposta": "Sem horário nessa semana" }
  "retirada": 1 }

// EfetivacaoRetiradaRequest (solicitante é sempre PARTICIPANTE, definido pelo servidor)
{ "motivo": "Mudança de cidade", "dataRetirada": "2026-11-28" }

// PainelEstudoResponse
{ "estudoId": "uuid", "titulo": "…", "status": "EXECUCAO", "geradoEm": "…",
  "candidatos": { "total": 5, "cadastrado": 0, "emAvaliacao": 0, "elegivel": 0, "naoElegivel": 0, "consentido": 0, "incluido": 5 },
  "participantes": { "ativos": 4, "retirados": 1, "limite": 8, "vagasRestantes": 3 },
  "visitas": { "realizadas": 1, "pendentes": 5, "atrasadas": 1, "naoAgendadas": 5, "ausentes": 0, "canceladas": 1,
               "presencasConfirmadasProximos7Dias": 0 },
  "pedidos": { "reagendamentoPendentes": 2, "retiradaPendentes": 1 } }

// ResumoDoEstudoResponse
{ "estudoId": "uuid", "titulo": "…", "status": "EXECUCAO", "incluidos": 5, "limite": 8, "visitasAtrasadas": 1, "pedidosPendentes": 3 }

// RelatorioEstudoResponse
{ "cabecalho": { "estudoId": "uuid", "titulo": "…", "status": "EXECUCAO", "responsavel": "…", "dataInicio": "…", "dataFim": "…",
                 "periodoDe": null, "periodoAte": null, "geradoPor": "…", "geradoEm": "…",
                 "aviso": "Dados fictícios, gerados para fins acadêmicos." },
  "recrutamento": { "cadastrados": 5, "avaliados": 5, "elegiveis": 5, "comConsentimento": 5, "incluidos": 5,
                    "criteriosDeInelegibilidade": [ { "criterioId": "uuid", "titulo": "…", "tipo": "EXCLUSAO", "quantidade": 2 } ] },
  "acompanhamento": { "visitas": [ { "visitaPlanejadaId": "uuid", "nome": "Triagem", "ordem": 1, "realizadas": 1, "ausentes": 0,
                                     "canceladas": 0, "pendentes": 0, "atrasadas": 1, "naoAgendadas": 2 } ],
                      "totais": { "realizadas": 1, "ausentes": 0, "canceladas": 1, "pendentes": 5, "atrasadas": 1, "naoAgendadas": 5 },
                      "taxaComparecimento": 100,                 // inteiro de 0 a 100; null sem realizadas nem ausentes
                      "retiradas": [ { "participanteCodigo": "CAND-005", "dataRetirada": "…", "motivo": "…", "solicitante": "PARTICIPANTE" } ],
                      "pedidos": { "reagendamento": { "pendentes": 2, "atendidos": 0, "recusados": 0 },
                                   "retirada": { "pendentes": 1, "atendidos": 1, "recusados": 0 } } } }
```

### 3.2 Campos novos em DTOs existentes (compatíveis, só acrescentam)

| DTO | Campos novos | Task |
|---|---|---|
| `MinhaVisitaResponse` | `presencaConfirmadaEm`, `pedidoPendente` (`{id, motivo, dataPreferida, criadoEm}` ou `null`), `podeConfirmar`, `podePedirReagendamento`, `retiradaPendente` | T125 |
| `MeuEstudoDetalheResponse` | `pedidoRetiradaPendente` (`{id, criadoEm}` ou `null`), `podeSolicitarRetirada` | T137 |
| `AgendaItemResponse` e `VisitaAgendadaResponse` | `presencaConfirmadaEm` | T133 |
| `HistoricoParticipanteResponse.VisitaDoHistorico` | `presencaConfirmadaEm`, `solicitacoes` (lista de `{id, tipo, situacao, motivo, resposta, criadoEm, respondidoEm}`) | T133 |
| `HistoricoParticipanteResponse.ParticipanteDoHistorico` | `pedidoRetirada` (`{solicitacaoId, motivo, criadoEm}` ou `null`) | T140 |

`podeConfirmar`, `podePedirReagendamento` e `podeSolicitarRetirada` são calculados **no servidor** com as mesmas regras da seção 4: a tela só mostra o botão, sem repetir a regra.

## 4. Regras, ordem de verificação e códigos de erro

Formato único: **409** `ConflitoRegraException`, **400** `ValidacaoRegraException` ou `VALIDACAO_CAMPOS`, **404** `RECURSO_NAO_ENCONTRADO`, **403** `ACESSO_NEGADO`.

| Código | HTTP | Quando | Criado em |
|---|---|---|---|
| `VISITA_NAO_ESTA_AGENDADA` (existe) | 409 | Confirmar ou pedir reagendamento de visita com desfecho | T124 |
| `VISITA_JA_OCORREU` | 409 | Data e hora da visita não são futuras | T124 |
| `ESTUDO_FORA_DE_RECRUTAMENTO_OU_EXECUCAO` (existe) | 409 | Estudo em outro estado | T124 |
| `PARTICIPANTE_RETIRADO` (existe) | 409 | Participação retirada | T124 |
| `RETIRADA_PENDENTE` | 409 | Há pedido de retirada pendente (bloqueia o RF19) | T124 (regra ligada na T136) |
| `PRESENCA_JA_CONFIRMADA` | 409 | Segunda confirmação | T124 |
| `PEDIDO_REAGENDAMENTO_PENDENTE` | 409 | Segundo pedido pendente para a visita | T124 |
| `MOTIVO_OBRIGATORIO` (existe) | 400 | Motivo com menos de 5 caracteres | T124 |
| `DATA_PREFERIDA_NO_PASSADO` | 400 | Preferência anterior a hoje | T124 |
| `SOLICITACAO_JA_RESPONDIDA` | 409 | Atender, recusar ou efetivar pedido que não está `PENDENTE` | T130 |
| `RESPOSTA_OBRIGATORIA` | 400 | Recusa com resposta menor que 5 caracteres | T130 |
| `TIPO_DE_SOLICITACAO_INCOMPATIVEL` | 409 | Atender retirada como reagendamento, ou efetivar um reagendamento | T130 |
| `RETIRADA_NAO_PODE_SER_RECUSADA` | 409 | Recusar pedido de retirada | T130 |
| `CIENCIA_OBRIGATORIA` | 400 | `cienteDosEfeitos` ausente ou falso | T136 |
| `PEDIDO_RETIRADA_PENDENTE` | 409 | Segundo pedido de retirada pendente | T136 |
| `ESTUDO_ENCERRADO` (existe) | 409 | Pedir ou efetivar retirada em estudo encerrado | T136 |
| `PARTICIPANTE_JA_RETIRADO` (existe) | 409 | Pedir ou efetivar retirada de quem já saiu | T136 |
| `PARAMETRO_INVALIDO` (existe) | 400 | Paginação inválida, `de` posterior a `ate`, ou `atrasada` com `situacao` diferente de `AGENDADA` | T129, T146, T153 |

Ordem de verificação (para no primeiro erro):

- **Confirmar:** 404 (visita de outra conta) → `VISITA_NAO_ESTA_AGENDADA` → `VISITA_JA_OCORREU` → `ESTUDO_FORA_DE_RECRUTAMENTO_OU_EXECUCAO` → `PARTICIPANTE_RETIRADO` → `RETIRADA_PENDENTE` → `PRESENCA_JA_CONFIRMADA`.
- **Pedir reagendamento:** as seis primeiras acima → `PEDIDO_REAGENDAMENTO_PENDENTE` → `MOTIVO_OBRIGATORIO` → `DATA_PREFERIDA_NO_PASSADO`.
- **Pedir retirada:** 404 (estudo de outra conta) → `ESTUDO_ENCERRADO` → `PARTICIPANTE_JA_RETIRADO` → `PEDIDO_RETIRADA_PENDENTE` → `CIENCIA_OBRIGATORIA`.
- **Atender ou recusar:** 404 → `TIPO_DE_SOLICITACAO_INCOMPATIVEL` (ou `RETIRADA_NAO_PODE_SER_RECUSADA`) → `SOLICITACAO_JA_RESPONDIDA` → regras do RF12 ou `RESPOSTA_OBRIGATORIA`.

Respostas padrão (constantes em `FechamentoDeSolicitacoes`):

| Constante | Texto | Quando |
|---|---|---|
| `RESPOSTA_DESFECHO` | `A visita recebeu desfecho antes do atendimento do pedido.` | Desfecho ou cancelamento da visita |
| `RESPOSTA_RETIRADA` | `participante retirado` | Retirada efetivada ou direta |
| `RESPOSTA_ESTUDO_ENCERRADO` | `Estudo encerrado antes do atendimento do pedido.` | Encerramento do estudo |

### Concorrência

1. A ordem de bloqueio é **participação → visita agendada → solicitação** (`SELECT ... FOR UPDATE` por `@Lock(PESSIMISTIC_WRITE)`). Pular um nível é permitido; inverter, não.
2. **Bloquear antes de carregar.** O serviço obtém só os identificadores por consulta de projeção, bloqueia e então lê o estado. Uma entidade carregada antes do bloqueio fica no contexto do Hibernate com o dado antigo.
3. Quem fecha pedidos busca apenas os `PENDENTE`, com bloqueio.
4. O pedido de retirada pode ser bloqueado logo depois da participação: nenhum fluxo o bloqueia a partir de uma visita.
5. O desfecho bloqueia a visita e depois fecha o reagendamento pendente; a retirada cancela as visitas e **depois** fecha os pedidos.

Assim, dois pedidos simultâneos para o mesmo par dão um sucesso e um 409 com o código da regra, e nenhum fluxo trava com outro. Não use índice único parcial: o H2 dos testes não o suporta.

## 5. Banco de dados

| Migration | Conteúdo |
|---|---|
| `V7_26_1__participant_requests.sql` (T122) | `ALTER TABLE visitas_agendadas ADD COLUMN IF NOT EXISTS presenca_confirmada_em TIMESTAMP`; tabela `solicitacoes_participante`: `id UUID`, `participacao_id` (FK), `tipo VARCHAR(20)`, `visita_agendada_id` (FK, opcional), `motivo VARCHAR(500)`, `data_preferida DATE`, `situacao VARCHAR(20) DEFAULT 'PENDENTE'`, `resposta VARCHAR(500)`, `respondido_por_id` (FK `usuarios`, opcional), `criado_em TIMESTAMP`, `respondido_em TIMESTAMP`, `versao BIGINT DEFAULT 0`; `CHECK`s de tipo, situação e campos; índices por participação, visita e `(situacao, tipo)` |
| `V7_30_1__candidate_registration_date.sql` (T151) | `ALTER TABLE candidatos ADD COLUMN IF NOT EXISTS criado_em TIMESTAMP` e preenchimento dos registros antigos |

Regras: nenhuma migration apaga dados; sem `ON DELETE CASCADE` nas novas FKs; cada uma é testada em banco vazio e em banco populado até a versão anterior (modelo: `MigracaoSeguraTest`) e no job `Backend (testes no PostgreSQL)`.

## 6. Nomes no código

**Backend** (`UEPB.ESWII.ClinicalStudyPB`)

| Camada | Classes novas |
|---|---|
| `domain.enums` | `TipoSolicitacao` (`REAGENDAMENTO`, `RETIRADA`), `SituacaoSolicitacao` (`PENDENTE`, `ATENDIDA`, `RECUSADA`) |
| `domain.model` | `SolicitacaoParticipante`; `VisitaAgendada` ganha `presencaConfirmadaEm`; `Candidato` ganha `criadoEm` |
| `domain.regra` | `RegraSolicitacao` (pura, sem Spring) |
| `repository` | `SolicitacaoParticipanteRepository` |
| `service` | `SolicitacaoParticipanteService` (ações do participante), `AtendimentoSolicitacaoService`, `FechamentoDeSolicitacoes`, `SolicitacaoConsultaService`, `PainelService`, `RelatorioService`, `ExportadorCsv` |
| `controller` | `SolicitacaoController`, `PainelController`, `RelatorioController`; `MeController` ganha as operações do participante |

Permissões: reutilizar `Permissoes.PARTICIPANTE`, `Permissoes.LEITURA` e `Permissoes.ESCRITA_RECRUTAMENTO`; nenhuma constante nova.

**Web** (`web/ClinicalStudyPB/src`): `api/solicitacoes.ts`, `api/painel.ts`, `api/relatorio.ts`; telas em `pages/estudos/detalhe/solicitacoes/` (`AbaSolicitacoes`, `DialogoAtender`, `DialogoRecusar`, `mensagensSolicitacao.ts`), `pages/estudos/detalhe/painel/` (`PainelEstudo`, `CartaoIndicador`, `BarraVagas`, `FunilRecrutamento`, `GraficoVisitas`), `pages/inicio/VisaoGeralEstudos.tsx` e `pages/estudos/detalhe/relatorio/` (`RelatorioPage`, `relatorio.css`).

**Mobile** (`mobile/ClinicalStudyPB/src`): `api/solicitacoes.ts`; telas `app/visitas/[visitaId].tsx` (ações), `app/visitas/reagendar.tsx`, `app/estudos/retirada.tsx`, `app/pedidos.tsx`; `components/CartaoSolicitacao.tsx`, `utils/solicitacoes.ts`.

**E2E:** specs `solicitacoes-reagendamento.spec.ts` (T135), `retirada-voluntaria.spec.ts` (T142), `painel.spec.ts` (T149), `relatorio.spec.ts` (T156); capturas `rf19-*`, `rf20-*`, `rf21-*`, `rf22-*` copiadas para `docs/imagens/sprint4/`; fluxo Maestro `.maestro/fluxo-solicitacoes.yaml`.

## 7. Dados de demonstração (perfil `demo`)

Ids no padrão `d7300000-0000-4000-8000-00000000PPNN` (`PP` = PBI, `NN` sequência). Datas relativas a `CURRENT_DATE` (**D**), `INSERT ... WHERE NOT EXISTS`, senha de todos os usuários `senha-de-teste` (mesmo hash BCrypt do `R__dados_demonstracao.sql`).

**Estudo "Demo - Solicitações do participante"** (`...000000002601`, `EXECUCAO`, limite 8, responsável `demo.pesquisador@clinicalstudy.test`, um critério de inclusão). Visitas previstas: **Triagem** (dia 0 ± 3), **Retorno A** (dia 10 ± 7) e **Retorno B** (dia 30 ± 7). Todos incluídos em D-5.

| Participante | Script | Visitas | Pedidos |
|---|---|---|---|
| `CAND-001` "Pessoa Omega (Ficticia)", conta **nova** `demo.solicitante@clinicalstudy.test` | 26 | Triagem `REALIZADA` D-5; Retorno A `AGENDADA` D+5 09:00; Retorno B `AGENDADA` D+25 09:00 | nenhum (o E2E mobile cria) |
| `CAND-002` "Pessoa Sigma (Ficticia)" | 26 | Triagem `AGENDADA` D-5 14:00 (**atrasada**); Retorno A `AGENDADA` D+6 10:00 | reagendamento `PENDENTE` (Retorno A) |
| `CAND-003` "Pessoa Tau (Ficticia)" | 26 | Retorno A `AGENDADA` D+4 11:00 | reagendamento `PENDENTE` (Retorno A) |
| `CAND-004` "Pessoa Upsilon (Ficticia)" | 28 | Retorno A `AGENDADA` D+7 15:00 | retirada `PENDENTE` |
| `CAND-005` "Pessoa Phi (Ficticia)" | 28 | Retorno A `CANCELADA` (D+8, motivo "retirada do estudo") | retirada `ATENDIDA`; participação `RETIRADO` |

Os valores do exemplo `PainelEstudoResponse` (seção 3.1) são os esperados com os dois scripts carregados.

**Testes existentes que precisam mudar:** `DemoDataIntegrationTest` conta `visitas_agendadas` e `participacoes` no banco inteiro (por exemplo, `AGENDADA:3`, 10 visitas, 7 participações). A T128 passa a filtrar essas consultas pelos estudos da Sprint 3, sem mudar os números esperados.

Os dados envelhecem: depois de alguns dias, as visitas "futuras" ficam no passado. Antes de um E2E ou de uma apresentação, recrie o banco (`docker compose down -v`).

## 8. Pontos de encaixe

| Encaixe | Entregue por | Quem usa |
|---|---|---|
| `solicitacoes_participante` com os dois tipos | T122 | T136, T138 |
| `RegraSolicitacao` com o parâmetro `retiradaPendente` | T124 | T136 (passa a informar o valor real) |
| `FechamentoDeSolicitacoes` | T130 | T138 (`recusarReagendamentosDaParticipacao`) |
| `AbaSolicitacoes` com coluna de tipo e ações por tipo | T132 | T140 |
| `AcoesDaVisita` e `LinhaDoTempoVisitas` | Sprint 3 | T133 |
| `DialogoRetirada` (T104) | Sprint 3 | T140 |
| `PainelService` | T144 | T151 |
| Links filtrados por parâmetros de URL | T146 | T147, T149 |

## 9. Padrões de teste

- **Backend:** Mockito com `Clock` fixo para regras e serviços; `@SpringBootTest` + MockMvc com `TokenDeTeste` para endpoints; helpers em `support/CenarioRecrutamento` (`estudoParaAgendamento`, `participanteIncluido`, `agendar`, `registrarDesfecho`); concorrência sem `@Transactional`, com limpeza no `@AfterEach` (modelo: `AgendamentoConcorrenciaTest`). O CI bloqueia abaixo de 80% das linhas de `domain` e `service`.
- **Web:** Vitest + Testing Library com `renderizarAba` e `erroHttp` (`src/test/ambiente.tsx`) e a API simulada com `vi.mock`. Toda tela cobre carregamento, vazio, erro e somente leitura.
- **E2E web:** Playwright com `ApiE2E` montando a partida pela API, dados únicos por execução, `test.step` e `capturar`.
- **Mobile:** Jest (`jest-expo`, fuso fixo no `jest.config.js`) + Testing Library, com mocks em `api/mocks`; Maestro para o E2E.

## 10. Ordem de integração

`T164` → `PBI-26 (I7)` → `PBI-27 (I4)` → `PBI-28 (I5)` → `PBI-29 (I6)` → `PBI-30 (I3)` → `PBI-31 (I1)` → `PBI-32 (I2)`. Cada Pull Request é revisado pelo dono do PBI seguinte (o último, por I7). Um PBI só começa com o anterior 100% na `develop`.

## 11. Definition of Done

> Uma task ou PBI só é concluído quando: os critérios de aceite estão atendidos e a funcionalidade é demonstrável com dados fictícios; o código foi revisado por outro integrante, passou no CI e entrou na branch principal por Pull Request; existem testes automatizados (unitários, de integração ou de componente) que passam e, no backend, a cobertura de linhas do código de negócio permanece em 80% ou mais; erros e validações das regras de negócio são tratados e mostrados com mensagens claras; o Swagger UI e o contrato YAML refletem os endpoints; os dados de demonstração da fatia estão no perfil demo; documentação e capturas de tela relevantes estão no repositório; a nota de passagem de bastão foi entregue ao próximo dono; e os requisitos RF01 a RF18 das Sprints anteriores continuam funcionando.

A **nota de passagem de bastão** fica em `docs/sprint4/bastao-pbi-NN.md`, com: o que entrou, como testar e o que o próximo dono vai reaproveitar.

---
[Índice dos documentos](README.md)
