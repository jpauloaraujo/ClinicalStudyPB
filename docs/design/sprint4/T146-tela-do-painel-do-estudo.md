# T146 · Tela do painel do estudo

> **Sprint 4** · PBI-29 · RF21 Painel do estudo  
> **Responsável:** I6 · **Estimativa:** 5h · **Prazo:** Ter 01/12  
> **Depende de:** T145  
> **Situação:** ⬜ A implementar (este documento é o guia para quem for fazer)

> Antes de começar, leia as [convenções e o contrato compartilhado da Sprint 4](00-convencoes-e-contrato-compartilhado.md): ajustes ao planejamento, endpoints, DTOs, códigos de erro, tabelas e dados de demonstração valem para esta task. A origem dos requisitos está na [seção 9](#9-origem-dos-requisitos-especificação-e-planejamento).

## 1. Objetivo

Transformar a aba Resumo no painel do estudo, com cartões, barra de vagas, funil, gráfico e links filtrados.

## 2. Contexto: o que já existe e deve ser reaproveitado

- `AbaResumo` mostra os dados cadastrais; `useEstudoDetalhe`, `useCarregamento`.
- `AbaCandidatos` é só do coordenador; `AbaParticipantes`, `AbaAgenda` e `AbaSolicitacoes` são dos dois.
- Recharts ainda não está no `package.json`.

## 3. Onde implementar

| Arquivo | Ação | Responsabilidade |
|---|---|---|
| `web/ClinicalStudyPB/package.json` | Alterar | Dependência `recharts` |
| `web/ClinicalStudyPB/src/api/painel.ts` | Criar | Módulo de API |
| `web/ClinicalStudyPB/src/pages/estudos/detalhe/abas/AbaResumo.tsx` | Alterar | Painel + dados cadastrais |
| `web/ClinicalStudyPB/src/pages/estudos/detalhe/painel/` | Criar | `PainelEstudo`, `CartaoIndicador`, `BarraVagas`, `FunilRecrutamento`, `GraficoVisitas` |
| `web/ClinicalStudyPB/src/pages/estudos/detalhe/acompanhamento/AbaAgenda.tsx` | Alterar | Filtros pela URL e "atrasadas" |
| `web/ClinicalStudyPB/src/pages/estudos/detalhe/abas/AbaCandidatos.tsx` | Alterar | Filtro `situacao` pela URL |
| `web/ClinicalStudyPB/src/pages/estudos/detalhe/abas/AbaParticipantes.tsx` | Alterar | Filtro de situação pela URL |
| `backend/ClinicalStudyPB/src/main/java/UEPB/ESWII/ClinicalStudyPB/controller/AgendaController.java` | Alterar | Parâmetro `atrasada` |
| `backend/ClinicalStudyPB/src/main/java/UEPB/ESWII/ClinicalStudyPB/service/AgendaService.java` | Alterar | Filtro |

## 4. Requisitos a implementar

Todos os requisitos (`RQ-T146-NN`) precisam estar atendidos para a task ser concluída.

- **RQ-T146-01** Manter os dados cadastrais e acrescentar os cartões em três grupos: recrutamento, participantes e visitas (mais pedidos e confirmações).
- **RQ-T146-02** Barra de vagas ocupadas com texto "N de L vagas ocupadas" e `role="progressbar"` com valores.
- **RQ-T146-03** Funil simples de recrutamento calculado das contagens por situação: cadastrados = total; avaliados = `elegivel + naoElegivel + consentido + incluido`; elegíveis = `elegivel + consentido + incluido`; com consentimento = `consentido + incluido`; incluídos = `incluido`.
- **RQ-T146-04** Gráfico de barras das visitas por situação com Recharts (versão compatível com React 19), com rótulo e valor em cada barra e uma tabela equivalente acessível: não depender só de cor.
- **RQ-T146-05** Cada indicador é um link para a lista filtrada: candidatos por situação → `candidatos?situacao=`; ativos e retirados → `participantes?situacao=`; atrasadas → `agenda?atrasada=true`; pendentes, realizadas, ausentes e canceladas → `agenda?situacao=` (pendentes usam `agenda?atrasada=false`); pedidos → `solicitacoes?situacao=PENDENTE&tipo=`; confirmações → `agenda?confirmada=true&atrasada=false&ate=<hoje + 7>`.
- **RQ-T146-06** Para o pesquisador, os indicadores de candidatos aparecem **sem** link (não acessa a aba). Não agendadas leva a `participantes`.
- **RQ-T146-07** Backend: parâmetro opcional `atrasada` na agenda, só de participantes ativos e visitas `AGENDADA`: `true` devolve data e hora anterior a agora e `false`, maior ou igual a agora, para os links baterem com os cartões; atualizar o YAML da Sprint 3.
- **RQ-T146-08** `atrasada` junto de `situacao` diferente de `AGENDADA` responde 400 `PARAMETRO_INVALIDO`; a agenda mostra o filtro ativo e permite limpá-lo.
- **RQ-T146-09** As listas leem o filtro da URL e continuam funcionando sem parâmetro.
- **RQ-T146-10** Estados de carregamento, erro com "Tentar de novo" e estudo sem dados (mensagem, sem gráfico vazio).
- **RQ-T146-11** Layout sem rolagem horizontal em largura pequena.

## 5. Contrato, regras e códigos de erro

Operação 10.

## 6. Testes

Básicos junto; completos na T148.

## 7. Critérios de aceite

- [ ] Números iguais aos da API; cada indicador abre a lista certa.
- [ ] Build e testes verdes.

## 8. Cuidados e fora de escopo

- `ResponsiveContainer` não mede tamanho no jsdom: dar dimensão fixa ao testar.
- Se o filtro `pendentes` não puder bater exatamente, ajustar o filtro, não o número do cartão.

## 9. Origem dos requisitos (especificação e planejamento)

Texto **sem paráfrase** da especificação e do planejamento. Onde ele diverge dos [ajustes da seção 1 das convenções](00-convencoes-e-contrato-compartilhado.md#1-ajustes-em-relação-ao-planejamento-e-por-quê), valem as convenções e este documento.

### 9.1 Requisito da especificação

- **RF21** (Módulo F, Monitoramento; interface Web): “Exibir painel com quantidade de candidatos, participantes incluídos e visitas realizadas ou pendentes.”

### 9.2 Critério de aceite do PBI (planejamento)

> pesquisador e coordenador abrem o painel de um estudo e veem a quantidade de candidatos por situação, participantes incluídos (ativos, retirados e vagas) e visitas realizadas, pendentes, atrasadas e não agendadas, com os números conferindo com os dados de demonstração; cada indicador leva à lista correspondente.

### 9.3 Regras de negócio do planejamento que afetam esta task

| # | Regra proposta | Tasks afetadas |
|---|---|---|
| 6 | O painel (RF21) usa estas definições: visitas realizadas = REALIZADA; pendentes = AGENDADA com data futura; atrasadas = AGENDADA com data passada e sem desfecho; não agendadas = visitas previstas de participantes ativos sem agendamento. Participantes retirados não entram nas pendências. Pesquisador e coordenador veem o painel dos estudos que já podem ler | T143, T144, T146 |

### 9.4 Texto original da task no planejamento

> **T146 · Tela do painel do estudo.** Primeiro transforme a aba Resumo do detalhe do estudo (T39) no painel, com cartões de indicadores agrupados em recrutamento, participantes e visitas. Depois acrescente a barra de vagas ocupadas, o funil simples de recrutamento e um gráfico de barras das visitas por situação, com Recharts, sem depender só de cor. Em seguida faça cada indicador abrir a lista já filtrada (por exemplo, visitas atrasadas na agenda). Por fim, trate carregamento, erro e estudo sem dados.

### 9.5 Requisitos que atravessam esta task

- Regra 9.2: “O backend deve disponibilizar uma **API HTTP REST** compartilhada pela web e pelo mobile, documentada no padrão **OpenAPI**”.
- Entregas: “Persistência dos dados, com scripts de criação/migração do banco e dados fictícios para demonstração” e “Tratamento de erros e validações das regras de negócio”.
- Testes (seção 7): “Testes unitários para todas as regras de negócio relevantes”, “Testes de integração para os principais endpoints” e “Execução automatizada dos testes a cada pull request”.
- Regra 9.3: “A interface **web** atende aos perfis de pesquisador, coordenador e administrador.”
- **Definition of Done** (texto completo na [seção 11 das convenções](00-convencoes-e-contrato-compartilhado.md#11-definition-of-done)): critérios de aceite demonstráveis com dados fictícios; revisão, CI e Pull Request; testes automatizados e 80% de cobertura do negócio no backend; erros com mensagens claras; Swagger e YAML atualizados; dados de demonstração, documentação, capturas e nota de bastão no repositório; RF01 a RF18 continuam funcionando.

---
Parte dos documentos de design da Sprint 4: [índice](README.md) · [convenções e contrato](00-convencoes-e-contrato-compartilhado.md).
