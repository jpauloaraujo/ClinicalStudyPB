# Testes da T93 — E2E, capturas e documentação do RF14

> Guia: [T93](../design/sprint3/T93-e2e-capturas-e-documentacao-do-rf14.md) · PBI-21 · RF14 Resultados fictícios das avaliações previstas
> Branch: `feature/t93-e2e-capturas-documentacao-rf14` (criada a partir da `develop` em 23/11, com T87 a T92 integradas).
> Só testes E2E, capturas e documentação: nenhum código de produção foi alterado.

## 1. Resultado no CI

| Verificação | Antes | Depois |
|---|---|---|
| Suíte E2E (job `E2E (Playwright)`) | 32 testes (2 de setup e 30 cenários) | **34 testes, 34 passaram** na primeira tentativa, sem repetição |
| `resultados-visita.spec.ts` | — | 2 cenários verdes |
| Demais jobs (backend H2 e PostgreSQL, web, mobile, Docker) | verdes | verdes |

## 2. O que foi implementado

| Arquivo | Conteúdo |
|---|---|
| `web/ClinicalStudyPB/e2e/tests/resultados-visita.spec.ts` (novo) | **Cenário 1:** o pesquisador abre a Triagem realizada pela aba **Agenda**, abre **Avaliações**, vê só as duas avaliações (a coleta de sangue não aparece), registra `120/80 mmHg` (selo "registrado", contador "1 de 2"), edita para `130/85` e confere a gravação na tela recarregada e pela API. Em outro contexto de navegador, o coordenador abre a mesma visita pela agenda e vê tudo somente leitura. **Cenário 2:** valor só com espaços (avaliação pendente) e valor apagado (avaliação registrada) mostram "Informe o valor do resultado.", desabilitam o Salvar e o Enter não grava nada; a tela recarregada e a API mostram o estado anterior. |
| `web/ClinicalStudyPB/e2e/pages/VisitaAgendadaPage.ts` (novo) | Page object da tela da visita e da aba Avaliações; registrado nas fixtures (`paginaVisitaAgendada`). |
| `web/ClinicalStudyPB/e2e/support/api.ts` | `estudoParaResultados`, `visitaRealizada` (agenda na data sugerida pelo servidor e registra REALIZADA) e `resultados` (GET para conferir o que foi gravado). |
| `docs/imagens/sprint3/rf14-*.png` | As quatro capturas geradas pelo E2E no CI (artefato `playwright-report`). |
| `README.md` | RF14 na tabela de funcionalidades (comportamento e erros), capturas, spec na tabela do E2E e contagem da suíte. |

## 3. Matriz RQ × entrega

| RQ | Prova |
|---|---|
| 01 | Cenário 1: abre pela agenda, registra valor e unidade, selo "registrado", contador "1 de 2", edita e confere |
| 02 | Cenário 2: erro na tela, Salvar desabilitado, nada gravado (tela recarregada e `GET .../resultados`) |
| 03 | Cenário 1: coordenador na mesma aba, com o aviso, sem Salvar e sem campos |
| 04 | Partida montada por `ApiE2E` (estudo, candidato e participante novos a cada execução; data sugerida pelo servidor) |
| 05 | `rf14-resultado-registrado`, `rf14-resultado-editado`, `rf14-resultado-erro-valor` e `rf14-resultado-leitura-coordenador` em `docs/imagens/sprint3/` |
| 06 | `README.md` atualizado |
| 07 | Azure Boards: T87 a T92 em Done; T93 com o Remaining Work zerado. Nenhum bug foi encontrado na T92 nem na T93 |
| 08 | Bastão ao PBI-22 (I2): os resultados (T89 e T90) e os dados de demonstração com resultados (`R__demo_21_resultados.sql`) já estão na `develop` |

## 4. Observações

- O guia pedia alterar `e2e/pages/VisitaAgendadaPage.ts`, mas o arquivo não existia, porque os E2E da T79 e da T86 não foram feitos. Ele foi criado nesta task; o `FichaParticipantePage`, usado pela T99 e pela T107, ficou intacto.
- **Capturas:** a primeira execução saiu com o cabeçalho fixo no meio das capturas de página inteira, porque a página estava rolada. O page object ganhou `rolarParaOTopo()`, chamado antes de cada captura.
- O erro de valor vazio é barrado na própria tela, então o E2E prova o "nada é gravado" pela tela recarregada e pela API. O `400 VALOR_OBRIGATORIO` do servidor está coberto no backend (T92).
