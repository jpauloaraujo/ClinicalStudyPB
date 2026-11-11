# T94 · Planning: validar regras e cadastrar a Sprint 3 no Azure Boards

> **Sprint 3** · PBI-22 · RF15 Histórico do participante e acompanhamento da Sprint  
> **Responsável:** I2 · **Estimativa:** 2h · **Prazo:** Ter 10/11 (cerimônia, antes da janela)  
> **Depende de:** Nenhuma  
> **Situação:** 📋 Entrega fora do código (cerimônia e processo), especificada neste documento e ainda obrigatória

> Antes de começar, leia as [convenções e o contrato compartilhado da Sprint 3](00-convencoes-e-contrato-compartilhado.md): nomes, endpoints, DTOs, códigos de erro, tabelas e dados de demonstração valem para esta task.

## 1. Objetivo

Conduzir o Sprint Planning de terça 10/11: validar com o PO as dez regras de negócio e os ajustes técnicos deste design, cadastrar a Sprint 3 no Azure Boards, registrar a capacidade e levantar o carry-over da Sprint 2. É uma cerimônia (não é código), mas **o resto da equipe depende dela para começar**: precisa estar pronta no mesmo dia.

## 2. Contexto: o que já existe e deve ser reaproveitado

- As dez regras de negócio estão no [Planejamento da Sprint 3](../../sprint3/Planejamento_da_Sprint_3_ClinicalStudyPB_v2.md); os ajustes técnicos propostos por este design estão na seção 1 das [convenções](00-convencoes-e-contrato-compartilhado.md).
- A Sprint 2 usou um CSV de importação (`docs/azure-boards/sprint2-backlog.csv`, com `README.md` explicando a importação) e o processo Basic/Scrum do Azure Boards; os bugs entram como itens do tipo Task com título "Bug" ou como Bug, conforme o processo do projeto.
- O calendário é de 14 dias sem folga, com janelas de 2 dias por PBI; sexta 20/11 é feriado (Dia da Consciência Negra) e as janelas dos PBIs 21, 24 e 25 caem em fim de semana, feriado ou véspera da Review.
- A Release 1 foi entregue em 09/11 (tag `release1`).

## 3. Onde implementar

| Arquivo | Ação | Responsabilidade |
|---|---|---|
| `docs/sprint3/ata-planning-sprint3.md` | Criar | Ata com as decisões sobre as regras e os ajustes, e os resultados de cada ponto |
| `docs/azure-boards/sprint3-backlog.csv` | Criar | CSV de importação com a iteração, os 7 PBIs e as 49 tasks |
| `docs/azure-boards/README.md` | Alterar | Instruções da Sprint 3 e links |
| `docs/design/sprint3/00-convencoes-e-contrato-compartilhado.md` | Alterar | Corrige o que o PO decidir de forma diferente |
| `Azure Boards` | Configurar | Iteração, PBIs, tasks, vínculos, Capacity e carry-over |

## 4. Requisitos a implementar

Cada requisito tem um identificador (`RQ-T94-NN`) para a revisão do Pull Request e para a conferência no fim da task: todos precisam estar atendidos.

### Validar as dez regras com o PO

- **RQ-T94-01** Levar as **dez regras de negócio** do planejamento para a reunião e registrar, na ata, a resposta do PO a cada uma (**confirmada** ou **corrigida**, com o texto novo), numa tabela com as colunas: regra, decisão, texto final, tasks afetadas.
- **RQ-T94-02** Corrigir, no mesmo dia, o texto dos documentos de design das tasks afetadas (os da regra 1: T75, T81, T89, T102; regra 2: T73, T75, T77; regra 3: T75, T80, T81, T83; regra 4: T87, T89, T91; regra 5: T95, T96, T97; regra 6: T101, T102, T104; regra 7: T109, T111, T116; regra 8: T108, T110; regra 9: T116, T117, T118; regra 10: T74 e todas as de backend).
- **RQ-T94-03** Pontos que precisam de **resposta explícita do PO**: (a) a vaga de quem se retira continua ocupada (regra 6); (b) uma visita cancelada **não pode ser agendada de novo** (consequência das regras 2 e 3); (c) "atividades da categoria avaliação" significa só `AVALIACAO_CLINICA` ou também `QUESTIONARIO`; (d) quem pode ver os resultados (pesquisador escreve, coordenador lê); (e) a janela de agendamento é calculada a partir da data de inclusão.

### Validar os ajustes técnicos deste design

- **RQ-T94-04** Confirmar o **prefixo `V6_`** das migrations (maior versão da Sprint 2 é `V5`, então a regra 10 manda trocar o prefixo `V3`), os **scripts `R__demo_*` repetíveis**, os **caminhos em português** (`/api/me/estudos`), o **fuso** `America/Fortaleza` e a abertura da aba Participantes ao pesquisador (ajustes 1, 2, 3, 6 e 7 das convenções).
- **RQ-T94-05** Decidir se o **E2E mobile com Maestro** roda só localmente com evidência ou também no CI (a decisão afeta a T114 e a T121), e registrar na ata.
- **RQ-T94-06** Confirmar com a professora (ou registrar a pendência com data) o **escopo e a data da Release 2** e se RF19 a RF22 ficam na Sprint 4 (a especificação não diz).

### Cadastrar a Sprint 3 no Azure Boards

- **RQ-T94-07** Criar a iteração **Sprint 3** (10/11/2026 a 23/11/2026) em Project Settings > Project configuration > Iterations.
- **RQ-T94-08** Criar os **7 PBIs** (PBI-19 a PBI-25) com descrição, prioridade alta e critérios de aceite do planejamento, associados à Sprint 3, e as **49 tasks** (T73 a T121) como filhas dos PBIs, com **Assigned To**, **Remaining Work** em horas e a **data de término** de cada task conforme o calendário por dia.
- **RQ-T94-09** Registrar as dependências entre tasks e entre PBIs com os vínculos **Predecessor** e **Successor**, para o painel mostrar a sequência (a tabela "Depende de" de cada PBI e o documento de cada task listam as dependências).
- **RQ-T94-10** Gerar o arquivo `docs/azure-boards/sprint3-backlog.csv` no mesmo formato do da Sprint 2 (ou importar direto) e atualizar `docs/azure-boards/README.md`.
- **RQ-T94-11** Cadastrar os 49 documentos de design como link (campo de descrição ou anexo) em cada task, para cada dono encontrar o seu guia.

### Capacidade e carry-over

- **RQ-T94-12** Registrar a disponibilidade dos sete integrantes em **Sprints > Capacity**, cada um apenas nos **dois dias da sua janela** (I1: 10 e 11/11; I3: 12 e 13/11; I6: 14 e 15/11; I2: 16 e 17/11, com o Planning em 10/11; I4: 18 e 19/11; I7: 20 e 21/11; I5: 22 e 23/11).
- **RQ-T94-13** Perguntar a cada dono se **consegue** a sua janela (a carga é de 9 a 12 horas por dia, acima da hipótese de 12 horas por semana das Sprints anteriores) e, para os PBIs 21, 24 e 25, que caem em fim de semana, feriado ou véspera da Review, combinar a troca de lugar prevista (o PBI-24 pode trocar com o PBI-23 ou com o PBI-22).
- **RQ-T94-14** Levantar o que ficou **pendente da Sprint 2** (bugs e pendências) e abrir itens de carry-over ligados a quem vai resolver; lembrar que não há margem: o que não couber vai para a Sprint 4, exceto defeito crítico.
- **RQ-T94-15** Confirmar a **maior versão de migration** da Sprint 2 no repositório (`V5`) e comunicar ao grupo o prefixo `V6_`.

### Entregáveis do dia

- **RQ-T94-16** Ata publicada em `docs/sprint3/ata-planning-sprint3.md` e avisada ao grupo no mesmo dia.
- **RQ-T94-17** Sprint 3 cadastrada (iteração, PBIs, tasks, vínculos e capacity) e design corrigido onde o PO decidiu diferente.

## 6. Testes

- Conferência (não há teste automatizado): cada uma das 10 regras tem decisão registrada; o painel do Azure Boards mostra 7 PBIs, 49 tasks e 146 horas; cada dono vê a sua janela na Capacity.

## 7. Critérios de aceite

- [ ] As dez regras e os ajustes técnicos têm decisão registrada na ata, e os documentos afetados foram corrigidos.
- [ ] A Sprint 3 está cadastrada com as 49 tasks, horas, responsáveis, datas e dependências.
- [ ] A capacidade dos sete integrantes e o carry-over da Sprint 2 estão registrados.
- [ ] A pendência da Release 2 com a professora tem responsável e data.

## 8. Cuidados e fora de escopo

- É uma cerimônia: não cria código. O resultado é a ata e o Azure Boards.
- Se o PO mudar uma regra depois do Planning, a mudança entra na ata e nos documentos de design **no mesmo dia**, antes de o PBI afetado começar.

---
Parte do conjunto de documentos de design da Sprint 3: [índice](README.md) · [convenções e contrato](00-convencoes-e-contrato-compartilhado.md).
