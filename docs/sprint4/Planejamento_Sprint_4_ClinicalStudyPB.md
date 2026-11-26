# Planejamento da Sprint 4 — ClinicalStudyPB

## Visão geral e meta da Sprint

A Sprint 4 (24/11 a 07/12/2026) completa os requisitos da especificação e fecha a Release 2: ao final, o participante confirma presença, pede reagendamento e solicita a retirada pelo app; o coordenador atende essas solicitações na web; pesquisador e coordenador acompanham o estudo por um painel e geram o relatório de recrutamento e acompanhamento (RF19 a RF22).

| Item                             | Definição                                                                                                                                                                                                                   |
|----------------------------------|-----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| Meta da Sprint                   | Release 2 entregue com o ciclo completo do participante: do app (confirmar, pedir reagendamento, pedir retirada) ao atendimento na web, mais painel e relatório por estudo, com testes, cobertura e documentação da release |
| Requisitos                       | RF19 e RF20 (Módulo E, interface Mobile, com o atendimento do coordenador na Web); RF21 e RF22 (Módulo F, interface Web)                                                                                                    |
| Também previsto na especificação | Pelo menos um fluxo E2E automatizado na web e um no app; cobertura mínima de 80% no backend; testes a cada Pull Request; instruções de instalação para outra equipe assumir na Release 3; RF01 a RF18 continuam funcionando |
| Entrega                          | Release 2 em 07/12/2026: tag release2, GitHub Release, releases/release2/README.md com o processo das Sprints 3 e 4 e apresentação do sistema em funcionamento                                                              |
| Equipe e método                  | 7 integrantes (I1 a I7), Scrum, Azure Boards, GitHub, Stack A (Spring Boot, React + TypeScript, React Native + Expo, PostgreSQL)                                                                                            |
| Organização                      | Um PBI por integrante, em sequência: cada PBI tem uma janela de dois dias e, ao terminar, passa o bastão ao próximo, como no fluxo corrigido da Sprint 3                                                                    |
| Fora do escopo                   | Requisito surpresa e troca de projetos (Release 3). A Sprint 5 (08/12 a 17/12) fica para carry-over, melhorias e preparação da troca, a definir na Retrospectiva                                                            |

Premissa: a Release 2 abrange as Sprints 3 e 4 e é entregue em 07/12, seguindo o padrão da Release 1 (duas sprints por release). A especificação não fixa essa data, então ela é confirmada com a professora no Planning (T164). Os PBIs 19 a 25 da Sprint 3 estão integrados na branch principal; o que ficou pendente entra como carry-over e consome a margem de capacidade.

## Premissas e regras de negócio a validar no Planning

A especificação descreve RF19 a RF22 em uma linha cada, então as tasks abaixo assumem as nove regras a seguir; o PO (I2) deve confirmá-las ou corrigi-las na terça 24/11, porque elas mudam o backend, as telas e os testes. Pelo diagrama de perfis da especificação, o participante usa só o app, o coordenador atende as solicitações e registra a retirada (RF16), e pesquisador e coordenador consultam painel e relatório.

| \#  | Regra proposta                                                                                                                                                                                                                                                                                                                                                                                                                                                                    | Tasks afetadas             |
|-----|-----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|----------------------------|
| 1   | O participante confirma presença apenas em visita AGENDADA, com data futura, participação ativa e sem pedido de retirada pendente. A confirmação é única (uma segunda dá 409), guarda data e hora e não muda a situação da visita                                                                                                                                                                                                                                                 | T122, T124, T126           |
| 2   | O pedido de reagendamento vale para visita AGENDADA e futura, exige motivo (ao menos 5 caracteres) e aceita uma preferência de data opcional. Cada visita tem no máximo um pedido PENDENTE, e a visita só muda quando o coordenador atende                                                                                                                                                                                                                                        | T122, T124, T126           |
| 3   | Pedidos do participante têm a situação PENDENTE, ATENDIDA ou RECUSADA, e as duas últimas são finais. O coordenador atende o reagendamento escolhendo nova data dentro da janela da visita (regras do RF12), o que reagenda a visita, limpa a confirmação de presença e marca o pedido como ATENDIDA na mesma transação; ou recusa com resposta obrigatória. Se a visita receber desfecho ou for cancelada antes, o pedido pendente é recusado automaticamente com resposta padrão | T129, T130, T132           |
| 4   | O participante solicita a retirada pelo app sem precisar justificar (motivo opcional), mas confirma que entende os efeitos. Há no máximo um pedido pendente por participação, e enquanto ele existe o app bloqueia confirmações e novos pedidos de reagendamento                                                                                                                                                                                                                  | T136, T137, T139           |
| 5   | O pedido de retirada não pode ser recusado: o coordenador o efetiva pelo serviço do RF16 (T102), com "quem solicitou" igual a participante. Na efetivação, o pedido vira ATENDIDA e os pedidos de reagendamento pendentes daquela participação são recusados com a resposta "participante retirado"                                                                                                                                                                               | T138, T140                 |
| 6   | O painel (RF21) usa estas definições: visitas realizadas = REALIZADA; pendentes = AGENDADA com data futura; atrasadas = AGENDADA com data passada e sem desfecho; não agendadas = visitas previstas de participantes ativos sem agendamento. Participantes retirados não entram nas pendências. Pesquisador e coordenador veem o painel dos estudos que já podem ler                                                                                                              | T143, T144, T146           |
| 7   | O relatório (RF22) é gerado na hora, por estudo e com período opcional, sem ser armazenado. Ele sai em tela, em CSV e em versão para impressão, mostra quem gerou e quando, e avisa que os dados são fictícios                                                                                                                                                                                                                                                                    | T150, T151, T154           |
| 8   | Erros seguem o formato único da Sprint 2 (400, 403, 404 e 409, sempre com o código da regra). As migrations usam as faixas V4_26\_\* a V4_32\_\*, todas acima das faixas V3 da Sprint 3                                                                                                                                                                                                                                                                                           | T122 e todas as de backend |
| 9   | A Release 2 abrange as Sprints 3 e 4 e é entregue na segunda 07/12, com a apresentação no mesmo dia. Data e escopo devem ser confirmados com a professora                                                                                                                                                                                                                                                                                                                         | T164, T168 a T170          |

## Product Backlog Items da Sprint

São 7 PBIs, 49 tasks e 146 horas, com um PBI por integrante. Os PBIs andam em sequência: cada um tem uma janela de dois dias, e todas as suas tasks fecham dentro dela. A ordem segue as dependências, então cada PBI começa com o anterior já integrado na main e não precisa de mocks.

| PBI    | Entrega esperada                                                                                                  | Requisitos              | Resp. | Janela                                       | Horas | Tasks       | Recebe o bastão de |
|--------|-------------------------------------------------------------------------------------------------------------------|-------------------------|-------|----------------------------------------------|-------|-------------|--------------------|
| PBI-26 | Confirmação de presença e pedido de reagendamento no app, e a tabela de pedidos do participante (base dos demais) | RF19 (Mobile)           | I7    | Ter 24/11 a Qua 25/11                        | 21h   | T122 a T128 | Planning (T164)    |
| PBI-27 | Atendimento dos pedidos de reagendamento e confirmações na web                                                    | RF19 (Web)              | I4    | Qui 26/11 a Sex 27/11                        | 21h   | T129 a T135 | PBI-26 (I7)        |
| PBI-28 | Retirada voluntária: pedido no app e efetivação pelo coordenador                                                  | RF20                    | I5    | Sáb 28/11 a Dom 29/11                        | 21h   | T136 a T142 | PBI-27 (I4)        |
| PBI-29 | Painel do estudo e visão geral dos estudos                                                                        | RF21                    | I6    | Seg 30/11 a Ter 01/12                        | 21h   | T143 a T149 | PBI-28 (I5)        |
| PBI-30 | Relatório de recrutamento e acompanhamento, com CSV e impressão                                                   | RF22                    | I3    | Qua 02/12 a Qui 03/12                        | 21h   | T150 a T156 | PBI-29 (I6)        |
| PBI-31 | Qualidade, regressão e preparação da Release 2                                                                    | Testes, CI e instalação | I1    | Sex 04/12 a Sáb 05/12                        | 21h   | T157 a T163 | PBI-30 (I3)        |
| PBI-32 | Planning, acompanhamento e entrega da Release 2                                                                   | Release 2               | I2    | Ter 24/11 (Planning) e Dom 06/12 a Seg 07/12 | 20h   | T164 a T170 | PBI-31 (I1)        |
| Total  |                                                                                                                   |                         |       | 14 dias                                      | 146h  | 49 tasks    |                    |

A Sprint foi organizada como uma passagem de bastão:

| Decisão                                 | Como funciona                                                                                                                                                                     |
|-----------------------------------------|-----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| Um dono por PBI, em sequência           | Cada integrante faz a fatia inteira (migration, regras, endpoints, telas, testes, E2E e capturas) dentro da sua janela de dois dias. Fora dela, revisa Pull Requests e dá suporte |
| Passagem de bastão                      | A última task de cada PBI integra tudo na main, cria os dados de demonstração e deixa uma nota curta para o próximo dono: o que entrou, como testar e o que ele vai reaproveitar  |
| Preparação antes da janela              | O próximo dono pode ler o código do anterior e preparar a branch, mas só integra depois que o bastão chega                                                                        |
| Base compartilhada no início            | A tabela de pedidos do participante (T122) já nasce com os tipos REAGENDAMENTO e RETIRADA, então o PBI-28 só acrescenta regras                                                    |
| Reaproveitamento das Sprints anteriores | O atendimento usa o reagendamento do RF12 (T75), a retirada usa o serviço do RF16 (T102), e o relatório usa as consultas do painel (T144)                                         |
| Migrations em faixas                    | Cada PBI usa a sua faixa do Flyway (V4_26_1\_\_... para o PBI-26), sem conflito de numeração                                                                                      |

Premissa: as atribuições seguem o foco de cada integrante e são provisórias. Se o Planning trocar donos ou a ordem, a sequência precisa continuar respeitando as dependências da tabela acima.

## Tasks detalhadas

Cada PBI traz uma tabela de controle (responsável, horas, prazo e dependências) e, abaixo dela, a descrição de cada task em passos. Prazos são o fim do dia indicado e ficam todos dentro da janela do PBI; "depende de" significa que a task só pode ser concluída depois das listadas.

### PBI-26 · RF19 Confirmação de presença e pedido de reagendamento no app (Alta · 21h · Ter 24/11 a Qua 25/11)

Critério de aceite: no detalhe de uma visita agendada, o participante confirma presença ou pede reagendamento com motivo; o app mostra a situação de cada pedido (pendente, atendido ou recusado, com a resposta); pedidos inválidos são recusados com a regra violada. Este PBI cria a tabela de pedidos do participante que os PBIs 27 e 28 usam.

| Task                                                                    | Resp. | Horas | Prazo     | Depende de                 |
|-------------------------------------------------------------------------|-------|-------|-----------|----------------------------|
| T122 · Modelo e migration da confirmação e dos pedidos do participante  | I7    | 3h    | Ter 24/11 | T164, Sprint 3 (T73, T109) |
| T123 · Contrato OpenAPI do RF19 no app                                  | I7    | 2h    | Ter 24/11 | T122                       |
| T124 · Regras de confirmação e de pedido de reagendamento               | I7    | 4h    | Ter 24/11 | T122                       |
| T125 · Endpoints /me do RF19 e Swagger                                  | I7    | 3h    | Qua 25/11 | T123, T124                 |
| T126 · Telas do app: confirmar presença e pedir reagendamento           | I7    | 4h    | Qua 25/11 | T125                       |
| T127 · Testes de backend e de componente do RF19 no app                 | I7    | 3h    | Qua 25/11 | T124, T126                 |
| T128 · E2E mobile, capturas, dados de demonstração e passagem de bastão | I7    | 2h    | Qua 25/11 | T126                       |

**T122 · Modelo e migration da confirmação e dos pedidos do participante.** Primeiro acrescente à visita agendada a data e hora da confirmação de presença, vazia por padrão. Depois crie a entidade ParticipantRequest com participação, tipo (REAGENDAMENTO ou RETIRADA), visita agendada (só no reagendamento), motivo, preferência de data, situação (PENDENTE, ATENDIDA ou RECUSADA), resposta, quem respondeu e as datas de criação e de resposta. Em seguida escreva a migration V4_26_1\_\_participant_requests.sql com as chaves estrangeiras e os CHECK de tipo e situação, e crie a entidade JPA e o repositório. Por fim, suba o backend em banco populado pela Sprint 3. O tipo RETIRADA já entra agora para o PBI-28 não precisar de outra tabela.

**T123 · Contrato OpenAPI do RF19 no app.** Primeiro descreva, sob /me, a confirmação de presença de uma visita, o pedido de reagendamento de uma visita e a listagem dos pedidos do participante com situação e resposta. Depois acrescente à visita do RF18 os campos de confirmação e de pedido pendente, para o app mostrar os selos sem outra chamada. Por fim, escreva os códigos de erro das regras 1 e 2 em docs/api/openapi-sprint4.yaml, só com os caminhos desta fatia.

**T124 · Regras de confirmação e de pedido de reagendamento.** Primeiro escreva o serviço de domínio sem depender de Spring Web. Depois implemente a confirmação, verificando nesta ordem: visita do próprio participante, AGENDADA, data futura, participação ativa, sem pedido de retirada pendente e ainda não confirmada. Em seguida implemente o pedido de reagendamento com as mesmas verificações, mais motivo de ao menos 5 caracteres e nenhum outro pedido pendente para a visita. Cada falha lança uma exceção com código próprio, que vira 409 (ou 404 para visita de outro participante, como na regra 7 da Sprint 3).

**T125 · Endpoints /me do RF19 e Swagger.** Primeiro exponha os endpoints do contrato usando o componente de acesso do participante da T109, restritos ao perfil participante. Depois trate 400, 403, 404 e 409 no formato único de erro. Por fim, confira o Swagger contra o YAML e corrija o que divergir no mesmo Pull Request.

**T126 · Telas do app: confirmar presença e pedir reagendamento.** Primeiro acrescente ao detalhe da visita (T117) os botões Confirmar presença e Pedir reagendamento, visíveis só quando a regra permite. Depois crie a confirmação em um toque, com retorno claro, e o formulário de reagendamento com motivo obrigatório e preferência de data opcional. Em seguida mostre nos cartões de próximas visitas os selos "Presença confirmada" e "Reagendamento solicitado", e crie a lista Meus pedidos no Perfil, com a resposta do coordenador. Por fim, trate os erros 409 com a mensagem da regra.

**T127 · Testes de backend e de componente do RF19 no app.** Escreva os testes junto com o código. Primeiro faça a matriz unitária das regras 1 e 2: visita passada, já confirmada, com desfecho, de outro participante, participante retirado e pedido duplicado. Depois teste os endpoints (200, 201, 400, 403, 404 e 409) com PostgreSQL real. Por fim, escreva os testes de componente dos botões, do motivo obrigatório e dos selos, e confira a cobertura da fatia.

**T128 · E2E mobile, capturas, dados de demonstração e passagem de bastão.** Primeiro acrescente ao fluxo Maestro da Sprint 3 os passos de confirmar uma visita e pedir o reagendamento de outra. Depois crie o script V4_26_2\_\_demo_participant_requests.sql (perfil demo, repetível) com pedidos pendentes de reagendamento para o PBI-27. Em seguida salve as capturas de sucesso e de erro e atualize o README com o RF19 no app. Por fim, passe o bastão a I4 com uma nota curta: tabela de pedidos, regras e como gerar um pedido pelo app de demonstração.

### PBI-27 · RF19 Atendimento dos pedidos de reagendamento na web (Alta · 21h · Qui 26/11 a Sex 27/11)

Critério de aceite: o coordenador vê os pedidos do estudo, atende um reagendamento escolhendo uma data válida ou o recusa com resposta, e o participante vê o resultado no app; as presenças confirmadas aparecem na agenda e no histórico; o pesquisador consulta tudo em modo somente leitura.

| Task                                                    | Resp. | Horas | Prazo     | Depende de                |
|---------------------------------------------------------|-------|-------|-----------|---------------------------|
| T129 · Contrato e consulta dos pedidos do estudo        | I4    | 3h    | Qui 26/11 | T128                      |
| T130 · Regras de atendimento e recusa do reagendamento  | I4    | 4h    | Qui 26/11 | T129, Sprint 3 (T75, T81) |
| T131 · Endpoints, autorização e Swagger do atendimento  | I4    | 3h    | Qui 26/11 | T130                      |
| T132 · Aba Solicitações do estudo                       | I4    | 4h    | Sex 27/11 | T131                      |
| T133 · Presença confirmada na agenda e no histórico     | I4    | 2h    | Sex 27/11 | T125, T131                |
| T134 · Testes de backend e de componente do atendimento | I4    | 3h    | Sex 27/11 | T130, T132, T133          |
| T135 · E2E web, capturas e passagem de bastão           | I4    | 2h    | Sex 27/11 | T132                      |

**T129 · Contrato e consulta dos pedidos do estudo.** Primeiro descreva no contrato a listagem dos pedidos de um estudo, com filtro por tipo e situação e paginação, e a contagem de pendentes. Depois descreva o atendimento (nova data e hora) e a recusa (resposta), com os códigos de erro da regra 3. Por fim, implemente a consulta, trazendo participante, visita, janela válida e datas, sem uma consulta por pedido.

**T130 · Regras de atendimento e recusa do reagendamento.** Primeiro escreva o atendimento em uma única transação: reagende a visita pelo serviço da T75 (mesma janela e mesmas regras do RF12), limpe a confirmação de presença e marque o pedido como ATENDIDA com quem respondeu e quando. Depois implemente a recusa, com resposta obrigatória de ao menos 5 caracteres. Em seguida bloqueie pedidos já respondidos (409). Por fim, ajuste o serviço de desfecho da T81, em acordo com I3, para recusar automaticamente o pedido pendente quando a visita receber desfecho ou for cancelada.

**T131 · Endpoints, autorização e Swagger do atendimento.** Primeiro exponha a listagem, o atendimento e a recusa, com escrita só para o coordenador e leitura para coordenador e pesquisador. Depois trate 400, 403, 404 e 409 no formato único de erro. Por fim, confira o Swagger contra o YAML.

**T132 · Aba Solicitações do estudo.** Primeiro crie, no detalhe do estudo, a aba Solicitações com a contagem de pendentes no título e a lista com participante, visita, data atual, motivo, preferência e situação, filtrada por tipo e situação. Depois crie o diálogo Atender, que reaproveita o seletor de data da T77 com a janela válida, e o diálogo Recusar, com resposta obrigatória. Em seguida deixe a aba somente leitura para o pesquisador. Por fim, deixe a lista preparada para o tipo RETIRADA, que o PBI-28 acrescenta.

**T133 · Presença confirmada na agenda e no histórico.** Primeiro acrescente o selo "Presença confirmada" na agenda do estudo, pelo componente de ações da visita da T77. Depois mostre a confirmação e os pedidos de cada visita na linha do tempo do histórico (T97). Por fim, acrescente à agenda um filtro de visitas confirmadas.

**T134 · Testes de backend e de componente do atendimento.** Escreva os testes junto com o código. Primeiro teste o atendimento fora da janela, o pedido já respondido, a recusa sem resposta e a recusa automática após desfecho. Depois teste os endpoints (200, 400, 403, 404 e 409) e a transação: se o reagendamento falha, o pedido continua PENDENTE. Por fim, escreva os testes de componente dos dois diálogos.

**T135 · E2E web, capturas e passagem de bastão.** Primeiro escreva o cenário Playwright em que o coordenador atende um pedido dos dados de demonstração e recusa outro, conferindo pela API que o participante vê as respostas. Depois salve capturas de sucesso e de erro e atualize o README com o RF19 na web. Por fim, passe o bastão a I5: aba Solicitações pronta para receber o tipo RETIRADA.

### PBI-28 · RF20 Retirada voluntária pelo app (Alta · 21h · Sáb 28/11 a Dom 29/11)

Critério de aceite: o participante solicita a retirada de um estudo pelo app, ciente dos efeitos; o coordenador vê o pedido e efetiva a retirada pelo RF16; o app mostra o pedido pendente e, depois, a participação como RETIRADO; pedidos duplicados ou de participação já retirada são recusados.

| Task                                                      | Resp. | Horas | Prazo     | Depende de            |
|-----------------------------------------------------------|-------|-------|-----------|-----------------------|
| T136 · Regras e contrato do pedido de retirada            | I5    | 3h    | Sáb 28/11 | T135                  |
| T137 · Endpoints /me do pedido de retirada e Swagger      | I5    | 3h    | Sáb 28/11 | T136                  |
| T138 · Efetivação da retirada pelo coordenador            | I5    | 4h    | Sáb 28/11 | T136, Sprint 3 (T102) |
| T139 · Tela do app: solicitar retirada                    | I5    | 3h    | Dom 29/11 | T137                  |
| T140 · Pedidos de retirada na aba Solicitações e na ficha | I5    | 3h    | Dom 29/11 | T132, T138            |
| T141 · Testes de backend e de componente do RF20          | I5    | 3h    | Dom 29/11 | T138, T139, T140      |
| T142 · E2E mobile e web, capturas e passagem de bastão    | I5    | 2h    | Dom 29/11 | T139, T140            |

**T136 · Regras e contrato do pedido de retirada.** Primeiro implemente no serviço de pedidos o tipo RETIRADA: participação ativa, nenhum pedido de retirada pendente, ciência dos efeitos obrigatória e motivo opcional (regra 4). Depois inclua nas regras da T124 o bloqueio de confirmações e de novos pedidos de reagendamento enquanto a retirada estiver pendente. Por fim, descreva no contrato o pedido de retirada sob /me e a efetivação pelo coordenador, com os códigos de erro.

**T137 · Endpoints /me do pedido de retirada e Swagger.** Primeiro exponha o pedido de retirada de um estudo do participante, restrito ao perfil participante e com 404 para estudo de outro. Depois inclua no detalhe do estudo do RF17 o pedido pendente, se houver. Por fim, trate os erros no formato único e confira o Swagger contra o YAML.

**T138 · Efetivação da retirada pelo coordenador.** Primeiro crie o endpoint em que o coordenador registra a retirada a partir do pedido, chamando o serviço da T102 com "quem solicitou" igual a participante e o motivo informado no app. Depois, na mesma transação, marque o pedido como ATENDIDA e recuse os pedidos de reagendamento pendentes da participação com a resposta "participante retirado". Em seguida aplique o mesmo fechamento quando o coordenador registrar uma retirada direta pelo RF16. Por fim, garanta que o pedido de retirada não pode ser recusado (regra 5), revisando com I4, dono da T102.

**T139 · Tela do app: solicitar retirada.** Primeiro acrescente ao detalhe do estudo (T112) o botão Solicitar retirada. Depois crie a tela que explica os efeitos (visitas futuras canceladas e a participação encerrada), com a caixa de ciência obrigatória, o motivo opcional e uma confirmação final. Em seguida mostre o aviso "Retirada solicitada em ..." e esconda os botões do RF19 enquanto o pedido estiver pendente. Por fim, mostre a marca RETIRADO depois da efetivação.

**T140 · Pedidos de retirada na aba Solicitações e na ficha.** Primeiro acrescente o tipo RETIRADA à aba Solicitações da T132, com a ação Registrar retirada, que abre o diálogo da T104 já preenchido com quem solicitou e o motivo. Depois mostre na ficha do participante o aviso "retirada solicitada pelo participante". Por fim, remova a opção Recusar para esse tipo.

**T141 · Testes de backend e de componente do RF20.** Escreva os testes junto com o código. Primeiro teste o pedido duplicado, a participação já retirada, a ciência ausente e o bloqueio do RF19 com retirada pendente. Depois teste a efetivação: pedido ATENDIDA, participação RETIRADO, visitas futuras canceladas e reagendamentos pendentes recusados, tudo na mesma transação. Por fim, escreva os testes de componente da tela do app e do diálogo web.

**T142 · E2E mobile e web, capturas e passagem de bastão.** Primeiro acrescente ao fluxo Maestro o pedido de retirada e, no Playwright, a efetivação pelo coordenador. Depois crie o script V4_28_1\_\_demo_withdrawal_requests.sql (perfil demo, repetível) com um pedido pendente e um já efetivado. Em seguida salve as capturas e atualize o README com o RF20. Por fim, passe o bastão a I6: os dados de demonstração agora têm todas as situações que o painel precisa contar.

### PBI-29 · RF21 Painel do estudo (Alta · 21h · Seg 30/11 a Ter 01/12)

Critério de aceite: pesquisador e coordenador abrem o painel de um estudo e veem a quantidade de candidatos por situação, participantes incluídos (ativos, retirados e vagas) e visitas realizadas, pendentes, atrasadas e não agendadas, com os números conferindo com os dados de demonstração; cada indicador leva à lista correspondente.

| Task                                                  | Resp. | Horas | Prazo     | Depende de       |
|-------------------------------------------------------|-------|-------|-----------|------------------|
| T143 · Definição dos indicadores e contrato do painel | I6    | 2h    | Seg 30/11 | T142             |
| T144 · Consultas agregadas do painel no backend       | I6    | 4h    | Seg 30/11 | T143             |
| T145 · Endpoints, autorização e Swagger do painel     | I6    | 2h    | Seg 30/11 | T144             |
| T146 · Tela do painel do estudo                       | I6    | 5h    | Ter 01/12 | T145             |
| T147 · Visão geral dos estudos na página inicial      | I6    | 3h    | Ter 01/12 | T145             |
| T148 · Testes de backend e de componente do painel    | I6    | 3h    | Ter 01/12 | T144, T146, T147 |
| T149 · E2E, capturas e passagem de bastão             | I6    | 2h    | Ter 01/12 | T146             |

**T143 · Definição dos indicadores e contrato do painel.** Primeiro escreva a lista de indicadores com a definição exata de cada um, a partir da regra 6: candidatos por situação (cadastrado, elegível, não elegível, incluído); participantes ativos, retirados, limite e vagas restantes; visitas realizadas, pendentes, atrasadas, não agendadas, ausentes e canceladas; presenças confirmadas nos próximos 7 dias; e pedidos pendentes. Depois descreva no contrato o painel de um estudo e o resumo de todos os estudos que o usuário pode ler. Por fim, confirme as definições com o PO antes de escrever as consultas.

**T144 · Consultas agregadas do painel no backend.** Primeiro escreva as contagens com GROUP BY no banco, uma consulta por bloco (candidatos, participações, visitas, pedidos), sem carregar entidades na memória. Depois calcule as não agendadas cruzando visitas previstas e participantes ativos. Em seguida coloque tudo em um serviço de leitura reaproveitável, que o relatório do PBI-30 vai usar. Por fim, compare cada número com uma contagem manual nos dados de demonstração.

**T145 · Endpoints, autorização e Swagger do painel.** Primeiro exponha o painel do estudo e o resumo dos estudos, com leitura para pesquisador e coordenador (403 para os demais, 404 para estudo inexistente). Depois confira o Swagger contra o YAML. Por fim, meça o tempo de resposta com os dados de demonstração e registre no Pull Request.

**T146 · Tela do painel do estudo.** Primeiro transforme a aba Resumo do detalhe do estudo (T39) no painel, com cartões de indicadores agrupados em recrutamento, participantes e visitas. Depois acrescente a barra de vagas ocupadas, o funil simples de recrutamento e um gráfico de barras das visitas por situação, com Recharts, sem depender só de cor. Em seguida faça cada indicador abrir a lista já filtrada (por exemplo, visitas atrasadas na agenda). Por fim, trate carregamento, erro e estudo sem dados.

**T147 · Visão geral dos estudos na página inicial.** Primeiro crie, na página inicial do pesquisador e do coordenador, a tabela dos estudos com estado, incluídos e limite, visitas atrasadas e pedidos pendentes. Depois ordene por pendências, para o que precisa de atenção aparecer primeiro. Por fim, ligue cada linha ao painel do estudo.

**T148 · Testes de backend e de componente do painel.** Escreva os testes junto com o código. Primeiro teste cada contagem com um cenário de valores conhecidos, incluindo participante retirado e visita atrasada. Depois teste os endpoints (200, 403 e 404). Por fim, escreva os testes de componente dos cartões, do estado vazio e dos links filtrados.

**T149 · E2E, capturas e passagem de bastão.** Primeiro escreva o cenário Playwright que abre o painel do estudo de demonstração, confere alguns números e segue o link das visitas atrasadas. Depois salve as capturas e atualize o README com o RF21. Por fim, passe o bastão a I3: o serviço de leitura da T144 e as definições da T143 servem de base para o relatório.

### PBI-30 · RF22 Relatório de recrutamento e acompanhamento (Alta · 21h · Qua 02/12 a Qui 03/12)

Critério de aceite: pesquisador e coordenador geram, para um estudo e um período opcional, o relatório de recrutamento e de acompanhamento em tela, exportam em CSV e imprimem; os números conferem com o painel; o relatório mostra quem gerou e quando.

| Task                                                  | Resp. | Horas | Prazo     | Depende de       |
|-------------------------------------------------------|-------|-------|-----------|------------------|
| T150 · Conteúdo e contrato do relatório               | I3    | 2h    | Qua 02/12 | T149             |
| T151 · Consultas do relatório no backend              | I3    | 4h    | Qua 02/12 | T144, T150       |
| T152 · Exportação em CSV                              | I3    | 3h    | Qua 02/12 | T151             |
| T153 · Endpoints, autorização e Swagger do relatório  | I3    | 2h    | Qui 03/12 | T151, T152       |
| T154 · Tela do relatório e versão para impressão      | I3    | 5h    | Qui 03/12 | T153             |
| T155 · Testes de backend e de componente do relatório | I3    | 3h    | Qui 03/12 | T151, T152, T154 |
| T156 · E2E, capturas e passagem de bastão             | I3    | 2h    | Qui 03/12 | T154             |

**T150 · Conteúdo e contrato do relatório.** Primeiro defina as seções: cabeçalho (estudo, estado, responsável, datas, período, quem gerou e quando); recrutamento (funil de cadastrados, avaliados, elegíveis, com consentimento e incluídos, e os critérios que mais causaram inelegibilidade); acompanhamento (por visita prevista, as realizadas, ausentes, canceladas e pendentes, a taxa de comparecimento, as retiradas com motivo e quem solicitou, e os pedidos do participante). Depois descreva no contrato a versão em JSON e as duas exportações CSV, recrutamento e acompanhamento. Por fim, valide o conteúdo com o PO.

**T151 · Consultas do relatório no backend.** Primeiro reaproveite o serviço de leitura da T144 para os totais, para que painel e relatório nunca divirjam. Depois escreva as consultas novas: contagem por visita prevista, critérios de inelegibilidade a partir da avaliação vigente de cada candidato (RF09) e lista de retiradas. Em seguida aplique o filtro de período pelas datas de cadastro, inclusão e visita. Por fim, calcule a taxa de comparecimento como realizadas sobre realizadas mais ausentes, sem contar as canceladas, como no app (T118).

**T152 · Exportação em CSV.** Primeiro gere os dois CSV em UTF-8 com BOM e ponto e vírgula como separador, para abrir corretamente no Excel em português. Depois use cabeçalhos em português, datas no formato dd/mm/aaaa e o nome do arquivo com o código do estudo e a data. Por fim, inclua na primeira linha o aviso de dados fictícios.

**T153 · Endpoints, autorização e Swagger do relatório.** Primeiro exponha o relatório em JSON e as duas exportações, com leitura para pesquisador e coordenador (403 para os demais, 404 para estudo inexistente, 400 para período inválido). Depois documente no Swagger os tipos de resposta. Por fim, confira o Swagger contra o YAML.

**T154 · Tela do relatório e versão para impressão.** Primeiro crie a página do relatório, aberta pelo painel do estudo, com o filtro de período e as seções em tabelas. Depois acrescente os botões Exportar CSV e Imprimir. Em seguida escreva o estilo de impressão (@media print), sem menus e com quebras de página entre seções, para gerar PDF pelo navegador. Por fim, trate carregamento, erro e período sem dados.

**T155 · Testes de backend e de componente do relatório.** Escreva os testes junto com o código. Primeiro teste as consultas com valores conhecidos, o filtro de período e a taxa de comparecimento sem visitas. Depois teste que os totais do relatório são iguais aos do painel e que o CSV tem as colunas e a codificação esperadas. Por fim, escreva os testes de componente do filtro e das seções.

**T156 · E2E, capturas e passagem de bastão.** Primeiro escreva o cenário Playwright que gera o relatório do estudo de demonstração, aplica um período e baixa um CSV. Depois salve as capturas da tela, do CSV aberto e da versão impressa, e atualize o README com o RF22. Por fim, passe o bastão a I1: todos os requisitos da Release 2 estão na main e entra o congelamento de funcionalidades.

### PBI-31 · Qualidade, regressão e preparação da Release 2 (Alta · 21h · Sex 04/12 a Sáb 05/12)

Critério de aceite: RF01 a RF22 passam na regressão; há E2E automatizado na web e no app; a cobertura de linhas do código de negócio do backend está em 80% ou mais; o sistema sobe em um clone limpo seguindo o README; os defeitos críticos estão corrigidos e o relatório de testes está pronto para a documentação.

| Task                                                         | Resp. | Horas | Prazo     | Depende de |
|--------------------------------------------------------------|-------|-------|-----------|------------|
| T157 · Dados de demonstração consolidados da Release 2       | I1    | 3h    | Sex 04/12 | T156       |
| T158 · Suíte E2E web de regressão                            | I1    | 4h    | Sex 04/12 | T157       |
| T159 · E2E mobile no CI (Maestro com emulador)               | I1    | 4h    | Sex 04/12 | T157       |
| T160 · Cobertura e relatórios de teste no CI                 | I1    | 3h    | Sáb 05/12 | T158       |
| T161 · Execução em clone limpo e instruções para a Release 3 | I1    | 3h    | Sáb 05/12 | T157       |
| T162 · Triagem e correção de defeitos da Release 2           | I1    | 3h    | Sáb 05/12 | T158, T159 |
| T163 · Relatório de testes e passagem de bastão              | I1    | 1h    | Sáb 05/12 | T160, T162 |

**T157 · Dados de demonstração consolidados da Release 2.** Primeiro rode juntos os scripts de demonstração das Sprints 2, 3 e 4 em um banco vazio e confira que formam um cenário coerente: estudos em cada estado, participantes com visitas em todas as situações, pedidos do participante e retiradas. Depois corrija conflitos e confirme que os scripts não duplicam dados se rodarem duas vezes. Por fim, atualize no README a tabela de usuários fictícios por perfil, sem nenhuma credencial real.

**T158 · Suíte E2E web de regressão.** Primeiro reúna os cenários Playwright das três sprints em uma suíte única, com as marcas smoke (fluxo principal) e regressão (todos os RF). Depois rode a suíte completa no CI sobre os dados consolidados e corrija os testes instáveis. Por fim, deixe a smoke rodando em todo Pull Request e a regressão na branch principal.

**T159 · E2E mobile no CI (Maestro com emulador).** Primeiro configure um job do GitHub Actions com emulador Android que instala o app e roda o fluxo Maestro das Sprints 3 e 4 contra o backend com dados de demonstração. Depois publique a gravação e o resultado como artefatos do pipeline. Limite a tentativa a três horas: se não estabilizar, mantenha a execução local com evidência e registre a decisão como problema conhecido da Release 2.

**T160 · Cobertura e relatórios de teste no CI.** Primeiro confira o JaCoCo do backend com a regra de 80% das linhas do código de negócio e complete as lacunas dos serviços novos junto com os donos. Depois publique também os relatórios de cobertura dos testes de componente da web e do app. Por fim, guarde os números finais para o relatório de testes.

**T161 · Execução em clone limpo e instruções para a Release 3.** Primeiro clone o repositório em uma máquina sem nada instalado do projeto e siga o README ao pé da letra: Docker Compose, banco, backend, web, app no emulador e credenciais fictícias. Depois corrija cada passo que falhar e acrescente variáveis de ambiente, portas e uma seção de problemas comuns. Por fim, revise o texto pensando na equipe que vai receber o projeto na Release 3, como pede a especificação.

**T162 · Triagem e correção de defeitos da Release 2.** Primeiro registre como Bug no Azure Boards cada falha da regressão, ligada à task de origem, com severidade. Depois corrija os críticos até o congelamento de código, chamando o dono do PBI quando preciso. Por fim, liste os que ficam como problemas conhecidos, com justificativa.

**T163 · Relatório de testes e passagem de bastão.** Primeiro consolide em docs/testes/release2.md a quantidade de testes por nível, os resultados das suítes, a cobertura e as evidências dos E2E. Depois passe o bastão a I2 com o relatório, a lista de problemas conhecidos e o commit candidato à tag.

### PBI-32 · Planning, acompanhamento e entrega da Release 2 (Alta · 20h · Ter 24/11 e Dom 06/12 a Seg 07/12)

Critério de aceite: a Sprint foi planejada e acompanhada no Azure Boards; README principal e README da Release 2 estão completos, com o processo das Sprints 3 e 4; os diagramas renderizam no GitHub; a tag release2 e a GitHub Release existem; a apresentação foi ensaiada.

| Task                                                                          | Resp. | Horas | Prazo                             | Depende de               |
|-------------------------------------------------------------------------------|-------|-------|-----------------------------------|--------------------------|
| T164 · Planning: validar regras, confirmar a Release 2 e cadastrar a Sprint 4 | I2    | 2h    | Ter 24/11                         | Nenhuma                  |
| T165 · Acompanhamento das passagens de bastão e checkpoint                    | I2    | 2h    | Qua 02/12                         | Leitura de todos os PBIs |
| T166 · Diagramas atualizados da Release 2                                     | I2    | 3h    | Dom 06/12                         | T163                     |
| T167 · README principal atualizado                                            | I2    | 3h    | Dom 06/12                         | T163, T166               |
| T168 · README da Release 2 e processo de desenvolvimento                      | I2    | 5h    | Seg 07/12 (manhã)                 | T167                     |
| T169 · Tag release2 e GitHub Release                                          | I2    | 2h    | Seg 07/12 (antes da apresentação) | T168                     |
| T170 · Roteiro e ensaio da apresentação da Release 2                          | I2    | 3h    | Seg 07/12                         | T169                     |

**T164 · Planning: validar regras, confirmar a Release 2 e cadastrar a Sprint 4.** Esta task fecha no mesmo dia, porque I7 começa o PBI-26 logo depois. Primeiro leve as nove regras deste documento para a reunião, anote as respostas e corrija as tasks afetadas. Depois confirme com a professora a data e o escopo da Release 2. Em seguida cadastre a iteração, os PBIs, as tasks e as dependências como descrito na seção do Azure DevOps. Por fim, levante o carry-over da Sprint 3 e abra os itens ligados a quem vai resolver.

**T165 · Acompanhamento das passagens de bastão e checkpoint.** Primeiro confira, a cada passagem, se o PBI anterior está na main com dados de demonstração e nota para o próximo dono, e atualize o burndown. Depois, na quarta 02/12, conduza o checkpoint de 15 minutos: o que já foi entregue, se a sequência está no prazo e o que cortar. Por fim, escreva o rascunho da seção Processo de desenvolvimento da Sprint 4.

**T166 · Diagramas atualizados da Release 2.** Primeiro atualize o diagrama de casos de uso com RF01 a RF22 e o de classes com a visita agendada, os resultados, a retirada e os pedidos do participante. Depois desenhe em Mermaid três sequências: agendamento, desfecho e resultado; pedido de reagendamento do app até o atendimento na web; e retirada voluntária. Por fim, atualize a arquitetura e confira no GitHub que todos renderizam.

**T167 · README principal atualizado.** Primeiro atualize as sete seções exigidas com RF12 a RF22, usando as capturas de sucesso e de erro que cada dono deixou na sua última task. Depois atualize o stack com as versões, a instalação com o texto revisado da T161 e a seção de testes com o relatório da T163. Por fim, confira os links para as três releases e o Swagger UI.

**T168 · README da Release 2 e processo de desenvolvimento.** Primeiro crie releases/release2/README.md com objetivo, Sprints 3 e 4, situação de cada requisito RF12 a RF22 (concluído, parcial ou não implementado, com justificativa), funcionalidades com capturas, alterações de arquitetura, testes e cobertura, e problemas conhecidos. Depois escreva a seção Processo de desenvolvimento das duas sprints: backlog e tasks, papéis e contribuições dos sete integrantes, burndown do Azure DevOps com a análise do planejado contra o realizado, e a revisão com impedimentos e ações de melhoria. Inclua a mudança da Sprint 3 para a passagem de bastão como uma das ações de melhoria.

**T169 · Tag release2 e GitHub Release.** Primeiro confirme que a branch principal está verde no CI e que o commit é o candidato indicado na T163. Depois crie a tag release2 e a GitHub Release, com o resumo e o link para releases/release2/README.md. A avaliação usa a versão da tag, então nada é alterado depois dela.

**T170 · Roteiro e ensaio da apresentação da Release 2.** Primeiro monte o roteiro, nesta ordem: painel do estudo; agenda, desfecho e resultados; app com confirmação e pedido de reagendamento; atendimento pelo coordenador; retirada voluntária; relatório; e, por fim, testes, cobertura e Swagger UI. Depois ensaie com o time, cada dono apresentando o seu PBI. Por fim, deixe o ambiente de demonstração pronto com os dados consolidados da T157.

## Dependências e passagem de bastão

A Sprint inteira é o caminho crítico: os sete PBIs andam em sequência, e um dia de atraso em qualquer janela atrasa todos os seguintes e a Release 2.

![Passagem de bastão · 7 PBIs em 14 dias](passagem_de_bastao.png)

Cada passagem entrega ao próximo dono o que ele vai reaproveitar:

| Passagem | Quando    | O que o próximo dono recebe                                                                       |
|----------|-----------|---------------------------------------------------------------------------------------------------|
| I2 → I7  | Ter 24/11 | Regras validadas, Sprint 4 cadastrada no Azure Boards e carry-over levantado (T164)               |
| I7 → I4  | Qua 25/11 | Tabela de pedidos do participante, regras do RF19 e pedidos de demonstração (T122, T128)          |
| I4 → I5  | Sex 27/11 | Aba Solicitações pronta para o tipo RETIRADA e recusa automática de pedidos (T130, T132)          |
| I5 → I6  | Dom 29/11 | Dados de demonstração com todas as situações de visita, pedido e retirada (T142)                  |
| I6 → I3  | Ter 01/12 | Serviço de leitura e definições dos indicadores do painel (T143, T144)                            |
| I3 → I1  | Qui 03/12 | RF19 a RF22 na main: congelamento de funcionalidades (T156)                                       |
| I1 → I2  | Sáb 05/12 | Relatório de testes, problemas conhecidos e commit candidato à tag: congelamento de código (T163) |

Dentro de cada PBI, as dependências entre tasks estão nas tabelas de controle e na seção Fluxo, no fim do documento.

## Calendário de prazos por dia

Todas as 49 tasks têm dia de conclusão dentro da janela do seu PBI. A quinta 03/12 é o congelamento de funcionalidades (RF19 a RF22 na branch principal) e o sábado 05/12 às 18h é o congelamento de código, depois do qual só entram correções críticas. Não há feriado nacional na Sprint; os fins de semana têm janelas de PBI, como no fluxo corrigido da Sprint 3.

| Dia       | Marco do dia                                                                                              | Tasks que devem estar concluídas |
|-----------|-----------------------------------------------------------------------------------------------------------|----------------------------------|
| Ter 24/11 | Sprint Planning; início do PBI-26                                                                         | T164, T122, T123, T124           |
| Qua 25/11 | PBI-26 concluído; bastão I7 → I4                                                                          | T125, T126, T127, T128           |
| Qui 26/11 | Backend do atendimento                                                                                    | T129, T130, T131                 |
| Sex 27/11 | PBI-27 concluído; bastão I4 → I5                                                                          | T132, T133, T134, T135           |
| Sáb 28/11 | Backend do pedido e da efetivação da retirada                                                             | T136, T137, T138                 |
| Dom 29/11 | PBI-28 concluído; bastão I5 → I6                                                                          | T139, T140, T141, T142           |
| Seg 30/11 | Backend do painel                                                                                         | T143, T144, T145                 |
| Ter 01/12 | PBI-29 concluído; bastão I6 → I3                                                                          | T146, T147, T148, T149           |
| Qua 02/12 | Backend do relatório; checkpoint                                                                          | T150, T151, T152, T165           |
| Qui 03/12 | PBI-30 concluído; congelamento de funcionalidades; bastão I3 → I1                                         | T153, T154, T155, T156           |
| Sex 04/12 | Dados consolidados e suítes E2E                                                                           | T157, T158, T159                 |
| Sáb 05/12 | Congelamento de código às 18h; bastão I1 → I2                                                             | T160, T161, T162, T163           |
| Dom 06/12 | Diagramas e README principal                                                                              | T166, T167                       |
| Seg 07/12 | Entrega da Release 2: README da release, tag, GitHub Release, apresentação, Sprint Review e Retrospectiva | T168, T169, T170                 |

## Distribuição entre os integrantes e capacidade

Cada integrante recebe de 20 a 21 horas, dentro do limite hipotético de 24 horas por pessoa usado nas Sprints anteriores, concentradas na sua janela de dois dias. Cada dono escreve os testes da sua fatia, e o olhar independente vem da revisão cruzada: o Pull Request de cada PBI é revisado pelo próximo dono da sequência, que assim já conhece o código que vai receber.

| Integrante        | Foco principal                             | PBI                  | Janela                                       | Tasks       | Horas | Revisor dos PRs |
|-------------------|--------------------------------------------|----------------------|----------------------------------------------|-------------|-------|-----------------|
| I7 - Julio        | Mobile, protótipos e testes                | PBI-26 · RF19 no app | Ter 24/11 a Qua 25/11                        | T122 a T128 | 21h   | I4              |
| I4 - João Roberto | Backend: usuários, segurança e autorização | PBI-27 · RF19 na web | Qui 26/11 a Sex 27/11                        | T129 a T135 | 21h   | I5              |
| I5 - Johnattan    | Frontend React: autenticação e integração  | PBI-28 · RF20        | Sáb 28/11 a Dom 29/11                        | T136 a T142 | 21h   | I6              |
| I6 - Paulo        | Frontend React: estudos e protótipos       | PBI-29 · RF21        | Seg 30/11 a Ter 01/12                        | T143 a T149 | 21h   | I3              |
| I3 - Carlos       | Backend: domínio, persistência e estudos   | PBI-30 · RF22        | Qua 02/12 a Qui 03/12                        | T150 a T156 | 21h   | I1              |
| I1 - João Victor  | Scrum Master, arquitetura e DevOps         | PBI-31 · Qualidade   | Sex 04/12 a Sáb 05/12                        | T157 a T163 | 21h   | I2              |
| I2 - Neto         | Product Owner, testes e documentação       | PBI-32 · Release 2   | Ter 24/11, Qua 02/12 e Dom 06/12 a Seg 07/12 | T164 a T170 | 20h   | I7              |
| Total             |                                            | 7 PBIs               | 14 dias                                      | 49 tasks    | 146h  |                 |

| Indicador                      | Hipótese para discussão |
|--------------------------------|-------------------------|
| Disponibilidade por integrante | 12 horas por semana     |
| Capacidade em duas semanas     | 7 × 12h × 2 = 168 horas |
| Horas planejadas nas tasks     | 146 horas               |
| Margem de capacidade           | 22 horas (cerca de 13%) |

Pontos de atenção: cada dono concentra cerca de 21 horas em dois dias, então precisa reservar essa janela na agenda e chegar a ela já tendo lido o código do PBI anterior. As janelas de I5 (28 e 29/11), I1 (05/12) e I2 (06/12) caem no fim de semana e devem ser confirmadas no Planning; se alguém não puder, o Planning troca a ordem respeitando as dependências. Um atraso empurra todos os PBIs seguintes, por isso a margem de 22 horas fica reservada para isso, para o carry-over da Sprint 3 e para os defeitos de integração.

## Como organizar no Azure DevOps

Use o processo Scrum no Azure Boards. Primeiro, cadastre a iteração Sprint 4 de 24/11/2026 a 07/12/2026 em Project Settings \> Project configuration \> Iterations. Depois crie os sete PBIs (PBI-26 a PBI-32) com descrição, prioridade, critérios de aceite e a janela no título ou na descrição, associe todos à Sprint 4 e crie as 49 tasks como filhas dos respectivos PBIs, com Assigned To e Remaining Work em horas. Em seguida, registre as dependências com os vínculos Predecessor e Successor, incluindo a passagem de bastão (a última task de cada PBI é predecessora da primeira do seguinte), e a disponibilidade dos sete integrantes em Sprints \> Capacity. Por fim, abra defeitos como itens do tipo Bug ligados à task de origem e atualize o Remaining Work todos os dias, para que o burndown usado no README da Release 2 reflita a realidade.

## Definition of Done

Uma task ou PBI só é concluído quando: os critérios de aceite estão atendidos e a funcionalidade é demonstrável com dados fictícios; o código foi revisado por outro integrante, passou no CI e entrou na branch principal por Pull Request; existem testes automatizados (unitários, de integração ou de componente) que passam e, no backend, a cobertura de linhas do código de negócio permanece em 80% ou mais; erros e validações das regras de negócio são tratados e mostrados com mensagens claras; o Swagger UI e o contrato YAML refletem os endpoints; os dados de demonstração da fatia estão no perfil demo; documentação e capturas de tela relevantes estão no repositório; a nota de passagem de bastão foi entregue ao próximo dono; e os requisitos RF01 a RF18 das Sprints anteriores continuam funcionando.

## Cerimônias e acompanhamento

| Evento                                                   | Quando                                                         | Finalidade nesta Sprint                                                                                                       |
|----------------------------------------------------------|----------------------------------------------------------------|-------------------------------------------------------------------------------------------------------------------------------|
| Sprint Planning                                          | Ter 24/11                                                      | Validar meta, regras, ordem da passagem de bastão, janelas, horas, capacidade e carry-over da Sprint 3; confirmar a Release 2 |
| Daily Scrum                                              | Dias úteis, 15 minutos                                         | Sincronizar, expor impedimentos e confirmar que o dono da vez está no prazo                                                   |
| Passagens de bastão                                      | Fim de cada janela (25/11, 27/11, 29/11, 01/12, 03/12 e 05/12) | PBI integrado na main, dados de demonstração e nota para o próximo dono (T165)                                                |
| Checkpoint                                               | Qua 02/12                                                      | Conferir se a sequência está no prazo e decidir cortes de escopo, se preciso                                                  |
| Congelamento de funcionalidades                          | Qui 03/12                                                      | RF19 a RF22 integrados na main                                                                                                |
| Congelamento de código                                   | Sáb 05/12, 18h                                                 | Só correções críticas depois disso                                                                                            |
| Sprint Review, apresentação da Release 2 e Retrospectiva | Seg 07/12                                                      | Demonstrar a Release 2, receber feedback e propor melhorias para a Sprint 5                                                   |

## Riscos e respostas

| Risco                                                             | Resposta                                                                                                                         |
|-------------------------------------------------------------------|----------------------------------------------------------------------------------------------------------------------------------|
| Atraso em uma janela empurra todos os PBIs seguintes              | O próximo dono prepara a branch antes da janela, a daily confere o dono da vez e a margem de 22 horas cobre até um dia de atraso |
| Janela no fim de semana sem disponibilidade do dono               | Confirmar no Planning; se preciso, trocar a ordem respeitando as dependências                                                    |
| Regras do RF19 e do RF20 diferentes do que a professora espera    | Validar as regras 1 a 5 no Planning (T164), antes de I7 começar                                                                  |
| Atendimento ou retirada pela metade por falha no meio da operação | Atendimento e efetivação em uma transação (T130, T138) e testes dedicados (T134, T141)                                           |
| Números do painel e do relatório diferentes                       | O relatório reaproveita o serviço de leitura do painel (T151) e um teste compara os totais (T155)                                |
| E2E mobile no CI instável                                         | Tentativa limitada a três horas (T159); se falhar, execução local com evidência e registro como problema conhecido               |
| Clone limpo não sobe seguindo o README                            | Teste em máquina limpa antes da tag (T161), importante para a troca de projetos da Release 3                                     |
| Data ou escopo da Release 2 diferentes do previsto                | Confirmar com a professora no Planning e ajustar o PBI-32                                                                        |
| Carry-over da Sprint 3 maior que o esperado                       | Levantar no Planning (T164); se passar de cerca de 15 horas, aplicar a ordem de corte abaixo                                     |

Se a capacidade real for menor, a ordem de corte é: primeiro a visão geral dos estudos na página inicial (T147), depois o E2E mobile no CI (T159, mantendo a execução local) e a versão para impressão do relatório (parte da T154), e só então o filtro de presenças confirmadas na agenda (parte da T133). RF19, RF20, RF21, RF22 e a entrega da Release 2 não devem ser cortados, porque completam a especificação e são avaliados pela tag.

## Fluxo

A cadeia de tasks de cada PBI, dia a dia, com a passagem de bastão entre os donos.

Ter 24/11 T164 Planning (I2, antes de tudo, sem dependências)  
│  
▼  
PBI-26 · RF19 Confirmação e reagendamento no app ─ I7  
════════════════════════════════════════════════════════════  
Ter 24/11 T122 Modelo e migration dos pedidos ............ I7 3h  
├──► T123 Contrato OpenAPI ...................... I7 2h  
└──► T124 Regras de confirmação e reagendamento .. I7 4h  
Qua 25/11 T125 Endpoints /me e Swagger (T123, T124) ....... I7 3h  
▼  
T126 Telas do app ............................ I7 4h ──► T128 E2E, demos e bastão ... I7 2h  
T127 Testes de backend e componente .......... I7 3h  
  
passagem de bastão I7 → I4 (Qua 25/11)  
  
PBI-27 · RF19 Atendimento dos pedidos na web ─ I4  
════════════════════════════════════════════════════════════  
Qui 26/11 T129 Contrato e consulta dos pedidos ............ I4 3h  
▼  
T130 Regras de atendimento (usa T75, ajusta T81) I4 4h  
▼  
T131 Endpoints e Swagger ...................... I4 3h  
Sex 27/11 ├──► T132 Aba Solicitações ..................... I4 4h ──► T135 E2E e bastão ... I4 2h  
├──► T133 Presença na agenda e no histórico .... I4 2h  
└──► T134 Testes de backend e componente ...... I4 3h  
  
passagem de bastão I4 → I5 (Sex 27/11)  
  
PBI-28 · RF20 Retirada voluntária ─ I5  
════════════════════════════════════════════════════════════  
Sáb 28/11 T136 Regras e contrato do pedido de retirada .... I5 3h  
├──► T137 Endpoints /me e Swagger .............. I5 3h  
└──► T138 Efetivação pelo coordenador (usa T102) I5 4h  
Dom 29/11 T139 Tela do app (T137) ........................ I5 3h ──┐  
T140 Aba Solicitações e ficha (T132, T138) ... I5 3h ──┴─► T142 E2E e bastão ... I5 2h  
T141 Testes de backend e componente .......... I5 3h  
  
passagem de bastão I5 → I6 (Dom 29/11)  
  
PBI-29 · RF21 Painel do estudo ─ I6  
════════════════════════════════════════════════════════════  
Seg 30/11 T143 Indicadores e contrato .................... I6 2h  
▼  
T144 Consultas agregadas ...................... I6 4h  
▼  
T145 Endpoints e Swagger ...................... I6 2h  
Ter 01/12 ├──► T146 Tela do painel ........................ I6 5h ──► T149 E2E e bastão ... I6 2h  
├──► T147 Visão geral dos estudos ............... I6 3h  
└──► T148 Testes de backend e componente ...... I6 3h  
  
passagem de bastão I6 → I3 (Ter 01/12)  
  
PBI-30 · RF22 Relatório ─ I3  
════════════════════════════════════════════════════════════  
Qua 02/12 T150 Conteúdo e contrato ........................ I3 2h  
▼  
T151 Consultas do relatório (usa T144) ........ I3 4h  
▼  
T152 Exportação CSV ........................... I3 3h  
Qui 03/12 T153 Endpoints e Swagger ...................... I3 2h  
▼  
T154 Tela e impressão ......................... I3 5h ──► T156 E2E e bastão ... I3 2h  
T155 Testes de backend e componente .......... I3 3h  
  
congelamento de funcionalidades · passagem de bastão I3 → I1 (Qui 03/12)  
  
PBI-31 · Qualidade e regressão ─ I1  
════════════════════════════════════════════════════════════  
Sex 04/12 T157 Dados de demonstração consolidados ........ I1 3h  
├──► T158 Suíte E2E web de regressão ........... I1 4h  
└──► T159 E2E mobile no CI .................... I1 4h  
Sáb 05/12 T160 Cobertura e relatórios no CI (T158) ....... I1 3h  
T161 Clone limpo e instruções (T157) .......... I1 3h  
T162 Triagem e correção de defeitos .......... I1 3h ──► T163 Relatório de testes e bastão ... I1 1h  
  
congelamento de código às 18h · passagem de bastão I1 → I2 (Sáb 05/12)  
  
PBI-32 · Entrega da Release 2 ─ I2  
════════════════════════════════════════════════════════════  
(Qua 02/12 T165 Checkpoint, em paralelo ................... I2 2h)  
Dom 06/12 T166 Diagramas ................................ I2 3h  
▼  
T167 README principal ......................... I2 3h  
Seg 07/12 ▼  
T168 README da Release 2 e processo ........... I2 5h  
▼  
T169 Tag release2 e GitHub Release ............ I2 2h  
▼  
T170 Ensaio e apresentação .................... I2 3h  
▼  
Sprint Review, apresentação da Release 2 e Retrospectiva (Seg 07/12)
