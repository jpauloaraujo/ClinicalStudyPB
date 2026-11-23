# **Planejamento da Sprint 3 — ClinicalStudyPB**

## **Visão geral e meta da Sprint**

A Sprint 3 (10/11 a 23/11/2026) entrega o acompanhamento dos participantes na web e o início do aplicativo do participante: ao final, o coordenador agenda visitas e registra o desfecho de cada uma, o pesquisador registra os resultados fictícios, os dois consultam o histórico do participante e a retirada é registrada; no app, o participante entra, vê seus estudos e suas visitas (RF12 a RF18).

| Item | Definição |
| :---- | :---- |
| Meta da Sprint | Fluxo de acompanhamento funcionando de ponta a ponta: agendar visita, registrar realização, ausência ou cancelamento, registrar resultados, consultar histórico e retirar participante na web; login e consulta de estudos e visitas no app do participante |
| Requisitos | RF12, RF13, RF14, RF15 e RF16 (Módulo D, interface Web); RF17 e RF18 (Módulo E, interface Mobile), mais o login do participante no app (RF02 e RF03 no mobile) |
| Também previsto na especificação | Pelo menos um fluxo E2E no app mobile; testes e correção de defeitos; RF01 a RF11 das Sprints 1 e 2 continuam funcionando |
| Entrega | PBIs integrados na branch principal em sequência, um a cada dois dias, o último em 23/11/2026, demonstrado na Sprint Review de 23/11/2026, com README atualizado e capturas de cada requisito para a Release 2 |
| Equipe e método | 7 integrantes (I1 a I7), Scrum, Azure Boards, GitHub, Stack A (Spring Boot, React \+ TypeScript, React Native \+ Expo, PostgreSQL) |
| Fora do escopo (Sprint 4, proposta) | RF19 e RF20 (mobile), RF21 (painel), RF22 (relatório), E2E mobile no CI se não couber agora e fechamento da Release 2 (tag, GitHub Release e README da release). A especificação não diz quais requisitos entram em cada release nem a data da Release 2: confirmar com a professora |

Premissa: a Release 1 foi entregue em 09/11 e os PBIs 09 a 18 da Sprint 2 estão integrados na branch principal. O que tiver ficado pendente (bugs e pendências) entra como carry-over e consome a margem de capacidade (seção de capacidade, mais abaixo); a lista é levantada no Planning (T94).

## **Premissas e regras de negócio a validar no Planning**

A especificação descreve RF12 a RF18 em uma linha cada, então as tasks abaixo assumem as dez regras a seguir; o PO (I2) deve confirmá-las ou corrigi-las na terça 10/11, porque elas mudam o backend, as telas e os testes. Pelo diagrama de perfis da especificação, o coordenador agenda visitas e registra desfechos e retiradas, o pesquisador registra as avaliações (RF14), os dois consultam o histórico e o participante usa só o app.

| \# | Regra proposta | Tasks afetadas |
| :---- | :---- | :---- |
| 1 | Visitas só são agendadas para participante incluído e ativo (não retirado), com o estudo em recrutamento ou execução; estudo encerrado bloqueia qualquer agendamento | T75, T81, T89, T102 |
| 2 | Uma visita agendada por visita prevista e por participante. A data deve ser de hoje em diante e ficar dentro da janela da visita prevista: data de inclusão somada ao dia previsto, mais ou menos os dias de tolerância. Fora da janela, o sistema recusa com 409 e mostra a janela válida | T73, T75, T77 |
| 3 | A visita agendada nasce AGENDADA e só pode virar REALIZADA, AUSENTE ou CANCELADA; esses três estados são finais. Reagendar (trocar a data) só vale para AGENDADA. Realizada e ausente não aceitam data futura; cancelar exige motivo; realizada guarda data e hora reais | T75, T80, T81, T83 |
| 4 | Resultados fictícios só existem para visita REALIZADA e só para atividades da categoria avaliação. Quem registra é o pesquisador, com um resultado por atividade, editável enquanto o estudo não estiver encerrado, guardando autor e data da última alteração | T87, T89, T91 |
| 5 | O histórico é só leitura para pesquisador e coordenador e mostra todas as visitas previstas do participante, inclusive as ainda não agendadas, com situação, datas e resultados | T95, T96, T97 |
| 6 | A retirada é registrada pelo coordenador, com motivo, quem solicitou e data (não futura), e é irreversível. O participante passa a RETIRADO e as visitas futuras AGENDADA viram CANCELADA com o motivo "retirada do estudo". A vaga continua ocupada, ou seja, o limite de vagas conta todos os que foram incluídos (ponto a validar com o PO) | T101, T102 (inclui os bloqueios nos serviços de T75, T81 e T89) e T104 |
| 7 | A conta do participante é um usuário com perfil participante, criado pelo administrador (RF01) e vinculado pelo coordenador a um candidato já incluído, na relação um para um (o campo opcional previsto na regra 8 da Sprint 2). O participante só enxerga os próprios dados: acessar dados de outro retorna 404 | T109, T111, T116 |
| 8 | Apenas o perfil participante entra no app; os demais perfis recebem mensagem de que o acesso é pela web. O login usa o mesmo endpoint e o mesmo token da web | T108, T110 |
| 9 | No app, "próximas visitas" são as AGENDADA de hoje em diante, da mais próxima para a mais distante; o "histórico de comparecimento" reúne REALIZADA, AUSENTE e CANCELADA, da mais recente para a mais antiga | T116, T117, T118 |
| 10 | Erros seguem o formato único da Sprint 2 (400 validação, 403 perfil sem permissão, 404 inexistente, 409 regra de negócio, sempre com o código da regra). As migrations usam faixas de versão por PBI (V3\_19\_\* a V3\_25\_\*); antes de começar, confirmar que a maior versão da Sprint 2 é menor que V3\_19, senão trocar o prefixo V3 pelo número seguinte ao da última migration | T74 e todas as de backend |

O E2E mobile exigido pela especificação (pelo menos um fluxo no app) está planejado na T114 com uma ferramenta simples, como o Maestro, rodando localmente com evidência. Rodar emulador no CI é pesado, então o Planning decide se isso fica para a Sprint 4\.

## **Product Backlog Items da Sprint**

São 7 PBIs, 49 tasks e 146 horas, com um PBI por integrante, dono da sua fatia de ponta a ponta. Os 14 dias da Sprint (10/11 a 23/11) são divididos em 7 janelas iguais de 2 dias, uma por PBI, executadas em sequência: um PBI só começa quando o anterior está 100% concluído (código integrado, testes, README e Remaining Work zerado), e cada integrante trabalha somente no seu PBI, durante a sua janela. Todos têm prioridade alta; se a capacidade real for menor, o corte segue a ordem da seção de riscos.

| PBI | Entrega esperada | Requisitos | Resp. | Janela (2 dias) | Horas | Tasks | Depende dos PBIs |
| :---- | :---- | :---- | :---- | :---- | :---- | :---- | :---- |
| PBI-19 | Agendamento de visitas e tabela da visita agendada (base dos demais) | RF12 | I1 | Ter 10/11 a Qua 11/11 | 21h | T73 a T79 | Sprint 2 |
| PBI-20 | Registro de realização, ausência e cancelamento de visitas | RF13 | I3 | Qui 12/11 a Sex 13/11 | 21h | T80 a T86 | PBI-19 |
| PBI-21 | Resultados fictícios das avaliações previstas | RF14 | I6 | Sáb 14/11 a Dom 15/11 | 21h | T87 a T93 | PBI-19, PBI-20 |
| PBI-22 | Histórico do participante e acompanhamento da Sprint (Planning, checkpoint e E2E do fluxo) | RF15 | I2 | Seg 16/11 a Ter 17/11 (Planning em 10/11) | 20h | T94 a T100 | PBI-19, PBI-20, PBI-21 |
| PBI-23 | Retirada de participante | RF16 | I4 | Qua 18/11 a Qui 19/11 | 21h | T101 a T107 | PBI-19, PBI-20, PBI-21, PBI-22 |
| PBI-24 | App do participante: esqueleto, login, vínculo da conta e estudos em que está incluído | RF02 e RF03 (Mobile), RF17 | I7 | Sex 20/11 a Sáb 21/11 | 21h | T108 a T114 | Sprint 2, PBI-22 (ficha para o vínculo da conta) |
| PBI-25 | App do participante: próximas visitas e histórico de comparecimento | RF18 | I5 | Dom 22/11 a Seg 23/11 | 21h | T115 a T121 | PBI-19, PBI-23, PBI-24 |
| Total |  |  |  | 10/11 a 23/11 | 146h | 49 tasks |  |

A Sprint foi organizada em sequência: uma janela de dois dias por PBI, sem sobreposição.

| Decisão | Como funciona |
| :---- | :---- |
| Um PBI por vez | Só existe um PBI em andamento. O seguinte começa quando o anterior está 100% concluído: todas as tasks com Remaining Work zero, Pull Requests revisados e integrados na main, testes passando e README atualizado |
| Um dono por PBI | Cada integrante cuida da fatia inteira (migration, regras, endpoints, tela, testes, E2E e capturas) e trabalha somente nela, durante a sua janela de dois dias. Nenhum PBI é só "backend de um" e "tela de outro" |
| Sem mocks | Como cada PBI começa sobre o que o anterior deixou pronto na main, as telas já usam a API real. O contrato OpenAPI continua sendo escrito no início de cada PBI, antes do código |
| Passagem de bastão | No fim da janela, o dono avisa o dono do PBI seguinte, que revisa os Pull Requests do PBI que termina e já conhece o que vai usar |
| Dados de demonstração por PBI | Cada fatia acrescenta seus dados fictícios, que os PBIs seguintes e os E2E usam |
| Migrations em faixas | Cada PBI usa a sua faixa de versão do Flyway (por exemplo, V3\_19\_1\_\_... para o PBI-19), evitando conflito de numeração |
| Telas compartilhadas | Agenda, ficha do participante e tela da visita são entregues por um PBI e recebem as partes dos seguintes por pontos de encaixe, já que dois integrantes nunca trabalham ao mesmo tempo |

Premissa: as atribuições seguem o foco de cada integrante nas Sprints anteriores e são provisórias. Se o Planning trocar donos de PBI, as dependências da seção de caminho crítico continuam valendo.

## **Tasks detalhadas**

Cada PBI traz uma tabela de controle (responsável, horas, prazo e dependências) e, abaixo dela, a descrição de cada task em passos. Prazos são o fim do dia indicado e cada PBI tem dois dias: o primeiro leva o contrato, o modelo e as regras; o segundo, as telas, os testes, o E2E e a documentação. "Depende de" significa que a task só começa depois das listadas, que já estão na main.

### **PBI-19 · RF12 Agendamento de visitas (Alta · 21h · 10/11 a 11/11)**

Janela de execução (I1): Ter 10/11 e Qua 11/11, com o PBI 100% concluído e integrado na main ao fim de Qua 11/11. Dia 1 (Ter 10/11): T73, T74 e T75 (9h). Dia 2 (Qua 11/11): T76, T77, T78 e T79 (12h).

Critério de aceite: o coordenador agenda, reagenda e consulta as visitas de um participante ativo, com sugestão de datas a partir da inclusão; datas fora da janela e estudos fora de recrutamento ou execução são recusados com mensagem clara. Ao fechar, em 11/11, este PBI deixa na main a tabela e a agenda da visita agendada que os PBIs 20, 21, 22, 23 e 25 usam.

| Task | Resp. | Horas | Prazo | Depende de |
| :---- | :---- | :---- | :---- | :---- |
| T73 · Modelo de domínio e migration da visita agendada | I1 | 3h | Ter 10/11 | Sprint 2 (tabelas de participação e de visita prevista) |
| T74 · Contrato OpenAPI do agendamento e dados de demonstração | I1 | 2h | Ter 10/11 | T73 |
| T75 · Regras de agendamento (serviço de domínio) | I1 | 4h | Ter 10/11 | T73, T74 |
| T76 · Endpoints, autorização e Swagger do agendamento | I1 | 3h | Qua 11/11 | T75 |
| T77 · Telas de agendamento | I1 | 4h | Qua 11/11 | T74, T76 |
| T78 · Testes de backend do agendamento | I1 | 3h | Qua 11/11 | T75, T76 |
| T79 · E2E, capturas e documentação do RF12 | I1 | 2h | Qua 11/11 | T77 |

**T73 · Modelo de domínio e migration da visita agendada.** Esta task é a base de cinco PBIs, então deve ser feita primeiro e integrada rápido. Primeiro, defina a entidade ScheduledVisit com id, participação, visita prevista, data e hora agendadas, situação (AGENDADA, REALIZADA, AUSENTE ou CANCELADA), motivo ou observação, usuário e data de criação e de última alteração. Depois escreva a migration V3\_19\_1\_\_scheduled\_visit.sql com as chaves estrangeiras, o UNIQUE de (participação, visita prevista) e o CHECK da situação, e crie a entidade JPA e o repositório. Por fim, suba o backend em banco vazio e em banco populado pela Sprint 2, abra um Pull Request pequeno e avise o grupo quando ele entrar na main, porque T80, T87, T95, T101 e T116 dependem dele.

**T74 · Contrato OpenAPI do agendamento e dados de demonstração.** Primeiro liste os endpoints: visitas previstas e agendadas de uma participação, criar agendamento, reagendar, agenda do estudo por período e situação, e as datas sugeridas (data de inclusão somada ao dia previsto de cada visita). Depois descreva DTOs, exemplos fictícios e os códigos de erro das regras 1, 2 e 3, no formato único de erro, em docs/api/openapi-sprint3.yaml, acrescentando só os caminhos desta fatia para não conflitar com os outros PBIs. Por fim, crie o script V3\_19\_2\_\_demo\_scheduled\_visits.sql (só no perfil demo e repetível) com dois participantes e visitas em situações diferentes, incluindo uma REALIZADA, para a equipe testar sem montar o fluxo inteiro.

**T75 · Regras de agendamento (serviço de domínio).** Primeiro escreva o serviço de domínio sem depender de Spring Web, para testá-lo isoladamente. Depois implemente as verificações, nesta ordem: participação existente no estudo, estudo em recrutamento ou execução, visita prevista pertencente ao estudo, data de hoje em diante, data dentro da janela (data de inclusão mais o dia previsto, mais ou menos a tolerância) e ausência de agendamento anterior para a mesma visita prevista. Cada falha lança uma exceção com código próprio, que vira 409\. Em seguida implemente o reagendamento (só para AGENDADA, mesma janela) e o cálculo das datas sugeridas. Por fim, registre quem agendou e quando. O bloqueio de participante retirado entra na T102 (PBI-23), quando a coluna de situação passa a existir.

**T76 · Endpoints, autorização e Swagger do agendamento.** Primeiro exponha os endpoints do contrato, com escrita só para o coordenador e leitura para coordenador e pesquisador (403 para os demais, 404 para ids inexistentes). Depois implemente a consulta da agenda do estudo com filtro por período e por situação e paginação. Por fim, confira se o Swagger UI coincide com o YAML da T74 e corrija o YAML no mesmo Pull Request se algo divergir. Quando esta task entrar na main, avise I3, dono do próximo PBI.

**T77 · Telas de agendamento.** Primeiro crie, sobre a API real da T76, a lista de participantes incluídos do estudo, com o progresso "X de N visitas agendadas". Depois crie a ficha de agendamento do participante com as visitas previstas e as datas sugeridas, com os botões Agendar (data e hora já preenchidas com a sugestão) e Reagendar, mostrando a janela válida e as mensagens de erro das regras. Em seguida crie a visualização da agenda do estudo por período, com um componente de ações da visita como ponto de encaixe para o PBI-20 e o PBI-21, e deixe tudo somente leitura para o pesquisador. Por fim, escreva os testes de componente.

**T78 · Testes de backend do agendamento.** Escreva os testes junto com o código, não só no fim. Primeiro faça a matriz unitária das regras: estudo fora de recrutamento ou execução, data passada, fora da janela, duplicidade e reagendamento de visita não agendada. Depois teste os endpoints com PostgreSQL real (201, 400, 403, 404 e 409). Por fim, confira a cobertura dos serviços desta fatia, porque o CI bloqueia abaixo de 80%.

**T79 · E2E, capturas e documentação do RF12.** Primeiro escreva o cenário Playwright em que o coordenador agenda uma visita com a data sugerida e a reagenda, e o cenário de erro com data fora da janela. Depois salve capturas nomeadas de sucesso e de erro para o README da Release 2\. Por fim, atualize o README com o RF12 e zere o Remaining Work da task no Azure Boards.

### **PBI-20 · RF13 Realização, ausência e cancelamento de visitas (Alta · 21h · 12/11 a 13/11)**

Janela de execução (I3): Qui 12/11 e Sex 13/11, com o PBI 100% concluído e integrado na main ao fim de Sex 13/11. Dia 1 (Qui 12/11): T80, T81 e T82 (10h). Dia 2 (Sex 13/11): T83, T84, T85 e T86 (11h).

Critério de aceite: o coordenador registra o desfecho de uma visita agendada (realizada, ausente ou cancelada), com os dados exigidos por cada situação; desfechos são finais e não podem ser desfeitos; transições inválidas são recusadas com a regra violada.

| Task | Resp. | Horas | Prazo | Depende de |
| :---- | :---- | :---- | :---- | :---- |
| T80 · Máquina de estados da visita e contrato do desfecho | I3 | 3h | Qui 12/11 | T73 |
| T81 · Regras de registro do desfecho (serviço de domínio) | I3 | 4h | Qui 12/11 | T80 |
| T82 · Endpoints, autorização e Swagger do desfecho | I3 | 3h | Qui 12/11 | T81 |
| T83 · Telas de registro do desfecho | I3 | 4h | Sex 13/11 | T80, T82 |
| T84 · Dados de demonstração e integração com a agenda | I3 | 2h | Sex 13/11 | T76, T82 |
| T85 · Testes de backend do desfecho | I3 | 3h | Sex 13/11 | T81, T82 |
| T86 · E2E, capturas e documentação do RF13 | I3 | 2h | Sex 13/11 | T83 |

**T80 · Máquina de estados da visita e contrato do desfecho.** A retirada (T102) usará esta máquina para cancelar visitas futuras. Primeiro crie uma classe de domínio com as transições permitidas: AGENDADA vai para REALIZADA, AUSENTE ou CANCELADA, e esses três são finais. Depois defina os dados de cada desfecho: realizada guarda a data e hora reais (que não podem ser futuras), ausente aceita observação e cancelada exige motivo com ao menos 5 caracteres. Por fim, descreva no contrato um único endpoint de desfecho, com o tipo, a data real, o motivo e a observação, e os códigos de erro (transição inválida, data futura e motivo ausente). Mantenha a classe sem dependência de Spring Web.

**T81 · Regras de registro do desfecho (serviço de domínio).** Primeiro escreva o serviço que carrega a visita e aplica a máquina da T80. Depois acrescente a pré-condição da regra 1: estudo em recrutamento ou execução, ou 409 com código próprio (o bloqueio de participante retirado entra na T102). Em seguida bloqueie realizada e ausente para visitas com data agendada no futuro, mas permita cancelar a qualquer momento antes do desfecho. Por fim, grave quem registrou e quando, atualize a situação e a data de alteração da visita e garanta que o registro é atômico: duas requisições simultâneas não podem gerar dois desfechos para a mesma visita (controle de versão otimista ou bloqueio da linha).

**T82 · Endpoints, autorização e Swagger do desfecho.** Primeiro exponha o endpoint de desfecho e a consulta de uma visita, com escrita só para o coordenador e leitura para coordenador e pesquisador. Depois trate 400, 403, 404 e 409 no formato único de erro. Por fim, confira o Swagger contra o YAML e corrija o que divergir no mesmo Pull Request.

**T83 · Telas de registro do desfecho.** Primeiro acrescente, sobre a API da T82, o selo de situação de cada visita na agenda do PBI-19, com rótulo e cor, mais texto para não depender só da cor. Depois crie o botão Registrar desfecho e o diálogo com três opções: realizada (data e hora, padrão agora), ausente (observação) e cancelada (motivo obrigatório). Em seguida deixe visitas com desfecho somente leitura, mostrando o que foi registrado, e exiba as mensagens de erro das regras. Por fim, escreva os testes de componente do diálogo e do motivo obrigatório. A agenda foi entregue pelo PBI-19, então o encaixe é feito pelo componente de ações da visita, sem reescrever a agenda.

**T84 · Dados de demonstração e integração com a agenda.** Primeiro crie o script V3\_20\_1\_\_demo\_visit\_outcomes.sql (perfil demo, repetível) com visitas em cada situação para os PBIs 21, 22 e 25 e para o E2E da T99. Depois confira na agenda e no histórico que os desfechos aparecem, e avise I6, dono do próximo PBI, que os dados estão disponíveis. Por fim, registre os defeitos de integração como Bugs ligados à task de origem.

**T85 · Testes de backend do desfecho.** Escreva os testes junto com o código. Primeiro faça a matriz de transições: as três válidas e todas as inválidas a partir de estados finais. Depois teste data futura, motivo ausente e estudo encerrado. Por fim, teste os endpoints (200, 400, 403, 404 e 409\) e a concorrência de dois desfechos simultâneos para a mesma visita, que deve resultar em um sucesso e um 409\.

**T86 · E2E, capturas e documentação do RF13.** Primeiro escreva o cenário Playwright em que o coordenador registra uma visita como realizada, outra como ausente e outra como cancelada com motivo, e o cenário de erro do cancelamento sem motivo. Depois salve capturas nomeadas de sucesso e de erro. Por fim, atualize o README com o RF13 e zere o Remaining Work no Azure Boards.

### **PBI-21 · RF14 Resultados fictícios das avaliações (Alta · 21h · 14/11 a 15/11)**

Janela de execução (I6): Sáb 14/11 e Dom 15/11, com o PBI 100% concluído e integrado na main ao fim de Dom 15/11. Dia 1 (Sáb 14/11): T87, T88 e T89 (9h). Dia 2 (Dom 15/11): T90, T91, T92 e T93 (12h).

Critério de aceite: o pesquisador registra e edita o resultado de cada atividade da categoria avaliação de uma visita REALIZADA; visitas não realizadas, atividades de outras categorias e estudos encerrados são recusados; o coordenador consulta os resultados em modo somente leitura.

| Task | Resp. | Horas | Prazo | Depende de |
| :---- | :---- | :---- | :---- | :---- |
| T87 · Modelo e migration dos resultados | I6 | 3h | Sáb 14/11 | T73 |
| T88 · Contrato OpenAPI dos resultados | I6 | 2h | Sáb 14/11 | T87 |
| T89 · Regras de registro de resultados (serviço de domínio) | I6 | 4h | Sáb 14/11 | T84, T88 |
| T90 · Endpoints, autorização, Swagger e dados de demonstração | I6 | 3h | Dom 15/11 | T89 |
| T91 · Tela de resultados da visita | I6 | 4h | Dom 15/11 | T88, T90 |
| T92 · Testes de backend dos resultados | I6 | 3h | Dom 15/11 | T89, T90 |
| T93 · E2E, capturas e documentação do RF14 | I6 | 2h | Dom 15/11 | T91 |

**T87 · Modelo e migration dos resultados.** Primeiro defina a entidade de resultado com a visita agendada, a atividade prevista, o valor (texto curto obrigatório), a unidade e a observação (opcionais), o usuário e a data do registro e da última alteração. Depois escreva a migration V3\_21\_1\_\_evaluation\_result.sql com as chaves estrangeiras e o UNIQUE de (visita agendada, atividade), que garante um resultado por atividade, e as entidades JPA com o repositório. Por fim, suba o backend em banco vazio e em banco populado. Os resultados são fictícios; nenhum dado real de pessoa é armazenado.

**T88 · Contrato OpenAPI dos resultados.** Primeiro descreva os dois endpoints: a consulta dos resultados de uma visita (cada atividade de avaliação com o resultado ou vazio, e a contagem de pendentes) e o registro ou edição de um resultado por atividade. Depois escreva os DTOs com exemplos fictícios e os códigos de erro (visita não realizada, atividade fora da categoria avaliação, participante retirado e estudo encerrado), acrescentando só os caminhos desta fatia ao YAML da Sprint. Por fim, gere os tipos para a tela.

**T89 · Regras de registro de resultados (serviço de domínio).** Primeiro escreva o serviço de domínio testando com as visitas REALIZADAS dos dados de demonstração das T74 e T84. Depois valide, nesta ordem: visita REALIZADA, atividade pertencente à visita prevista e da categoria avaliação, estudo não encerrado e valor não vazio, cada falha com código de erro próprio. Em seguida permita salvar uma atividade de cada vez (resultado parcial) e calcular os pendentes. Por fim, aplique a edição, que atualiza autor e data da última alteração. O bloqueio de participante retirado entra na T102.

**T90 · Endpoints, autorização, Swagger e dados de demonstração.** Primeiro exponha os endpoints com escrita só para o pesquisador e leitura para pesquisador e coordenador. Depois trate 400, 403, 404 e 409 no formato único de erro e confira o Swagger contra o YAML. Por fim, crie o script V3\_21\_2\_\_demo\_results.sql (perfil demo, repetível) com resultados em uma visita realizada, para o histórico (PBI-22) e o E2E da T99.

**T91 · Tela de resultados da visita.** Primeiro crie, sobre a API da T90, a aba Avaliações da visita REALIZADA (aberta pela agenda), com a lista das atividades da categoria avaliação e, para cada uma, os campos valor, unidade e observação e o selo "pendente" ou "registrado". Depois salve cada atividade separadamente, com feedback e erros do servidor por campo, e mostre o contador "X de N avaliações registradas". Em seguida deixe a aba somente leitura para o coordenador e quando o estudo estiver encerrado, com aviso explicando o motivo (o caso do participante retirado entra na T104). Por fim, escreva os testes de componente. A tela da visita foi entregue pelos PBIs 19 e 20, então a aba entra pelo ponto de encaixe que eles deixaram.

**T92 · Testes de backend dos resultados.** Escreva os testes junto com o código. Primeiro teste cada regra isolada: visita agendada ou ausente, atividade de outra categoria, atividade de outra visita, estudo encerrado e valor vazio. Depois teste a edição e os pendentes. Por fim, teste os endpoints (200, 201, 400, 403, 404 e 409\) com PostgreSQL real e confira a cobertura da fatia.

**T93 · E2E, capturas e documentação do RF14.** Primeiro escreva o cenário Playwright em que o pesquisador registra o resultado de uma avaliação e depois o edita, o cenário de erro do valor vazio e a verificação de que o coordenador vê só leitura. Depois salve capturas nomeadas de sucesso e de erro. Por fim, atualize o README com o RF14 e zere o Remaining Work no Azure Boards.

### **PBI-22 · RF15 Histórico do participante e acompanhamento da Sprint (Alta · 20h · 16/11 a 17/11)**

Janela de execução (I2): Seg 16/11 e Ter 17/11, com o PBI 100% concluído e integrado na main ao fim de Ter 17/11. Dia 1 (Seg 16/11): T95 e T96 (7h). Dia 2 (Ter 17/11): T97, T98, T99 e T100 (11h). A T94 (Planning, 2h) é uma cerimônia e acontece na terça 10/11, antes da janela.

Critério de aceite: pesquisador e coordenador abrem a ficha de um participante e veem, em ordem, todas as visitas previstas com situação, datas e resultados, e um resumo com realizadas, ausentes, canceladas e pendentes. Este PBI também reúne o acompanhamento da Sprint (Planning, checkpoint e E2E do fluxo completo), por isso fica com I2, que já atua como PO. O Planning (T94) é uma cerimônia da terça 10/11 e acontece antes da janela do PBI.

| Task | Resp. | Horas | Prazo | Depende de |
| :---- | :---- | :---- | :---- | :---- |
| T94 · Planning: validar regras e cadastrar a Sprint 3 no Azure Boards | I2 | 2h | Ter 10/11 (cerimônia, antes da janela) | Nenhuma |
| T95 · Contrato e modelo de leitura do histórico | I2 | 3h | Seg 16/11 | T73 |
| T96 · Consulta agregada do histórico no backend | I2 | 4h | Seg 16/11 | T95 |
| T97 · Tela do histórico do participante | I2 | 4h | Ter 17/11 | T95, T96 |
| T98 · Testes de backend e de componente do histórico | I2 | 2h | Ter 17/11 | T96, T97 |
| T99 · E2E do fluxo de acompanhamento completo | I2 | 3h | Ter 17/11 | T76, T82, T90 e os dados de demonstração (T74, T84) |
| T100 · Checkpoint de integração e acompanhamento da Sprint | I2 | 2h | Ter 17/11 | PBIs 19, 20 e 21 (leitura) |

**T94 · Planning: validar regras e cadastrar a Sprint 3 no Azure Boards.** Esta task precisa estar pronta no mesmo dia, porque o resto da equipe depende dela para começar. Primeiro leve as dez regras de negócio deste documento para a reunião, anote a resposta de cada uma e corrija o texto das tasks afetadas. Depois cadastre a iteração, os PBIs, as tasks e as dependências como descrito na seção do Azure DevOps, mais abaixo. Em seguida registre a disponibilidade dos sete integrantes em Capacity, cada um na sua janela de dois dias, lembrando que sexta 20/11 é feriado e que as janelas dos PBIs 21, 24 e 25 caem em fim de semana ou feriado, o que precisa ser confirmado com os donos. Por fim, levante o que ficou pendente da Sprint 2 e abra itens de carry-over ligados a quem vai resolver.

**T95 · Contrato e modelo de leitura do histórico.** Primeiro defina no contrato o endpoint de histórico da participação: para cada visita prevista, a situação (não agendada, agendada, realizada, ausente ou cancelada), as datas e a lista de resultados, mais o resumo com a contagem de cada situação. Depois escreva os DTOs de leitura, sem criar tabelas novas: esta fatia só lê o que os outros PBIs gravam. Por fim, escreva os dados de demonstração de um participante com histórico completo, usando a tabela da T73 e os dados de demonstração das T74, T84 e T90.

**T96 · Consulta agregada do histórico no backend.** Primeiro implemente a consulta que junta visitas previstas, visitas agendadas e resultados, ordenada pelo dia previsto, evitando uma consulta por visita (o problema N+1) com projeção ou fetch join. Depois calcule o resumo no servidor, incluindo os pendentes (previstas sem agendamento ou ainda agendadas). Em seguida aplique a autorização de leitura para pesquisador e coordenador (403 para os outros, 404 para participante inexistente). Por fim, documente no Swagger. Os resultados da T89 já estão na main e entram na consulta; a marca de retirada é acrescentada pela T104 (PBI-23).

**T97 · Tela do histórico do participante.** Primeiro crie, sobre a API da T96, a ficha do participante (aberta pela aba Participantes) com os cartões de resumo: realizadas, ausentes, canceladas e pendentes. Depois crie a linha do tempo das visitas, cada uma expansível para mostrar os resultados, com filtro por situação e estados de carregamento, lista vazia e erro. Em seguida deixe na ficha uma área de ações como ponto de encaixe para a retirada (T104) e o vínculo da conta (T111). Por fim, escreva os testes de componente.

**T98 · Testes de backend e de componente do histórico.** Primeiro teste a consulta com participante sem visitas e com visitas em todas as situações. Depois teste a ordenação, o resumo e as permissões (200, 403 e 404). Por fim, confira a cobertura da fatia.

**T99 · E2E do fluxo de acompanhamento completo.** Esta task lê o trabalho dos PBIs 19, 20, 21 e 22, já integrados na main, então pode ser escrita de uma vez. Primeiro escreva o cenário: o coordenador agenda uma visita e registra que foi realizada, o pesquisador registra um resultado, e o histórico mostra tudo. Depois rode no CI com os dados de demonstração das outras fatias e abra Bugs no Azure Boards, ligados à task de origem, para o que falhar. Por fim, deixe o relatório e as capturas prontos para o README. O cenário da retirada fica na T107, depois que o PBI-23 existir.

**T100 · Checkpoint de integração e acompanhamento da Sprint.** Primeiro atualize o burndown e o Remaining Work e confirme que os PBIs 19, 20 e 21 estão 100% concluídos e integrados na main. Depois, na terça 17/11, conduza o checkpoint de 15 minutos, no meio da Sprint: o que está na main, o que atrasou, o que corre risco nos PBIs 23, 24 e 25 e o que cortar. Por fim, escreva o rascunho da seção Processo de desenvolvimento da Sprint 3 (objetivo, PBIs, organização da equipe, burndown e análise) para o README da Release 2, que será completado na Review.

### **PBI-23 · RF16 Retirada de participante (Alta · 21h · 18/11 a 19/11)**

Janela de execução (I4): Qua 18/11 e Qui 19/11, com o PBI 100% concluído e integrado na main ao fim de Qui 19/11. Dia 1 (Qua 18/11): T101, T102 e T103 (10h). Dia 2 (Qui 19/11): T104, T105, T106 e T107 (11h).

Critério de aceite: o coordenador registra a retirada de um participante ativo com motivo, quem solicitou e data; o participante passa a RETIRADO de forma irreversível, as visitas futuras agendadas são canceladas com o motivo "retirada do estudo" na mesma operação, e o histórico fica preservado.

| Task | Resp. | Horas | Prazo | Depende de |
| :---- | :---- | :---- | :---- | :---- |
| T101 · Modelo, migration e contrato da retirada | I4 | 3h | Qua 18/11 | T73 |
| T102 · Regras da retirada (serviço de domínio) | I4 | 4h | Qua 18/11 | T75, T80, T81, T89, T101 |
| T103 · Endpoints, autorização e Swagger da retirada | I4 | 3h | Qua 18/11 | T102 |
| T104 · Tela de retirada | I4 | 4h | Qui 19/11 | T97, T101, T103 |
| T105 · Dados de demonstração e conferência do cancelamento em cascata | I4 | 1h | Qui 19/11 | T76, T102 |
| T106 · Testes de backend da retirada | I4 | 4h | Qui 19/11 | T102, T103 |
| T107 · E2E, capturas e documentação do RF16 | I4 | 2h | Qui 19/11 | T104 |

**T101 · Modelo, migration e contrato da retirada.** Primeiro crie a migration V3\_23\_1\_\_participant\_withdrawal.sql com a tabela de retirada (participação com UNIQUE, motivo, quem solicitou entre participante, pesquisador ou coordenação, data da retirada, usuário que registrou e data do registro) e a coluna de situação ATIVO ou RETIRADO na tabela de participações, com padrão ATIVO e CHECK. Depois escreva as entidades JPA e confira, em banco populado, que os participantes incluídos na Sprint 2 ficam ATIVO depois da migração. Por fim, descreva no contrato o endpoint de retirada e os códigos de erro (participante já retirado, estudo encerrado e data futura). Avise o grupo quando a coluna de situação entrar na main: a T102 a usa para bloquear ações nos PBIs 19, 20 e 21.

**T102 · Regras da retirada (serviço de domínio).** Primeiro escreva o serviço em uma única transação. Depois valide as pré-condições: participante ativo, estudo não encerrado, motivo obrigatório com ao menos 5 caracteres e data não futura, cada falha com 409 ou 400 e código próprio. Em seguida grave a retirada, mude a participação para RETIRADO e cancele as visitas futuras ainda AGENDADA usando a máquina de estados da T80, com o motivo automático "retirada do estudo", sem tocar nas visitas que já têm desfecho. Em seguida acrescente a verificação de participante retirado (409 com código próprio) nos serviços de agendamento (T75), de desfecho (T81) e de resultados (T89), que foram entregues antes de a coluna de situação existir, com um teste para cada um. Por fim, mantenha a vaga ocupada (regra 6), de modo que a contagem de incluídos do RF11 não diminua, e deixe o registro sem opção de desfazer.

**T103 · Endpoints, autorização e Swagger da retirada.** Primeiro exponha o registro e a consulta da retirada, com escrita só para o coordenador e leitura para coordenador e pesquisador. Depois trate 400, 403, 404 e 409 no formato único de erro. Por fim, confira o Swagger contra o YAML e corrija o que divergir no mesmo Pull Request. O pedido de retirada feito pelo próprio participante no app (RF20) é da Sprint 4 e vai reaproveitar este serviço, então o serviço fica independente do endpoint web.

**T104 · Tela de retirada.** Primeiro crie, sobre a API da T103, o botão Registrar retirada na área de ações da ficha do participante e o diálogo com motivo, quem solicitou e data. Depois acrescente a confirmação avisando que a retirada é irreversível e que as visitas futuras serão canceladas, com a quantidade de visitas afetadas. Em seguida mostre a marca RETIRADO na lista de participantes, na ficha e no histórico (PBI-22), escondendo ações que não fazem mais sentido, e acrescente o aviso de participante retirado na aba de resultados (PBI-21). Por fim, trate os erros 400 e 409 e escreva os testes de componente. A ficha foi entregue pelo PBI-22, então o botão entra pelo ponto de encaixe que ela deixou.

**T105 · Dados de demonstração e conferência do cancelamento em cascata.** Primeiro crie o script V3\_23\_2\_\_demo\_withdrawals.sql (perfil demo, repetível) com um participante retirado que tinha visitas futuras, para o PBI-22, o PBI-25 e o E2E da T99. Depois confira na agenda e no histórico que as visitas aparecem canceladas com o motivo certo.

**T106 · Testes de backend da retirada.** Escreva os testes junto com o código. Primeiro teste as pré-condições e a irreversibilidade (uma segunda retirada dá 409). Depois teste o cancelamento em cascata: visitas futuras AGENDADA viram CANCELADA, as realizadas e ausentes permanecem como estavam, e tudo acontece na mesma transação, de modo que uma falha no meio não deixa a retirada pela metade. Por fim, teste os endpoints (201, 400, 403, 404 e 409\) e a regra de que a contagem de vagas não muda. Inclua os testes dos bloqueios acrescentados nos serviços de agendamento, desfecho e resultados.

**T107 · E2E, capturas e documentação do RF16.** Primeiro escreva o cenário Playwright em que o coordenador registra a retirada de um participante com visitas futuras e confere a marca RETIRADO e o cancelamento das visitas no histórico do participante, e o cenário de erro do motivo vazio. Depois salve capturas nomeadas de sucesso e de erro. Por fim, atualize o README com o RF16 e zere o Remaining Work no Azure Boards.

### **PBI-24 · RF17 App do participante: login, vínculo da conta e meus estudos (Alta · 21h · 20/11 a 21/11)**

Janela de execução (I7): Sex 20/11 e Sáb 21/11, com o PBI 100% concluído e integrado na main ao fim de Sáb 21/11. Dia 1 (Sex 20/11): T108, T109 e T110 (10h). Dia 2 (Sáb 21/11): T111, T112, T113 e T114 (11h).

Critério de aceite: o participante entra no app com a conta vinculada pelo coordenador, vê a lista dos estudos em que está incluído e o detalhe de cada um, e sai do app; outros perfis são recusados com mensagem clara; o participante nunca vê dados de outro; existe pelo menos um fluxo E2E no app. Ao fechar, em 21/11, este PBI deixa na main o esqueleto do app, os endpoints /me e o vínculo da conta que o PBI-25 usa.

| Task | Resp. | Horas | Prazo | Depende de |
| :---- | :---- | :---- | :---- | :---- |
| T108 · Esqueleto do app: navegação, cliente HTTP e contexto de autenticação | I7 | 3h | Sex 20/11 | Sprint 1 (app Expo com login, T12) |
| T109 · Acesso do participante e endpoints /me no backend | I7 | 4h | Sex 20/11 | Sprint 2 (participação e campo de usuário participante) |
| T110 · Login e logout no app com a API real | I7 | 3h | Sex 20/11 | T108 |
| T111 · Vínculo da conta do participante (backend e web) | I7 | 3h | Sáb 21/11 | T109, T97 (ficha do PBI-22) |
| T112 · Telas Meus estudos e detalhe do estudo | I7 | 3h | Sáb 21/11 | T108, T109 |
| T113 · Testes de backend e de componente do app | I7 | 3h | Sáb 21/11 | T109, T110, T111, T112 |
| T114 · E2E mobile (Maestro), capturas e documentação | I7 | 2h | Sáb 21/11 | T110, T112 |

**T108 · Esqueleto do app: navegação, cliente HTTP e contexto de autenticação.** O app Expo da Sprint 1 já tem Expo Router, login, sessão no SecureStore e as abas Início, Estudos, Visitas e Perfil; esta task o prepara para receber as telas do PBI-24 e do PBI-25. Primeiro atualize o cliente HTTP para o formato único de erro da Sprint 2 (status, código da regra, mensagem e erros por campo), com tratamento central de 401 que limpa a sessão e volta ao login. Depois gere os tipos TypeScript a partir do YAML da Sprint, como na web, e crie um modo de mocks ativado por variável de ambiente. Em seguida configure Jest e Testing Library no app, com um teste de exemplo rodando no job Mobile do CI. Por fim, documente no README do app como rodar com mocks e com o backend e avise I5, dono do último PBI, que o PBI-25 começa sobre este esqueleto.

**T109 · Acesso do participante e endpoints /me no backend.** Primeiro publique no contrato os endpoints sob /me, que identificam o participante pelo token e não recebem id. Depois implemente um componente que resolve, a partir do usuário logado, as participações vinculadas à conta (regra 7\) e a base dos controladores /me, restritos ao perfil participante (403 para os demais). Em seguida implemente GET /me/studies, com título, descrição resumida, estado do estudo, data de inclusão e situação da participação (ativo ou retirado), e o detalhe de um estudo, que devolve 404 se o estudo não for do participante. Por fim, documente no Swagger e avise I5: a T116 usa este componente para as consultas de visitas.

**T110 · Login e logout no app com a API real.** Primeiro revise a tela de login da Sprint 1 com o novo cliente: validação dos campos, mensagens de credencial inválida e de usuário desativado, e botão desabilitado durante o envio. Depois confirme a regra 8: pesquisador, coordenador e administrador recebem a mensagem de que o acesso é pela web, e participante sem participação vinculada entra e vê um aviso de que ainda não está em nenhum estudo. Em seguida teste a sessão restaurada ao reabrir o app e o logout no Perfil. Por fim, rode o fluxo em emulador Android ou em celular físico, além do navegador, pois a Sprint 1 só testou a versão web.

**T111 · Vínculo da conta do participante (backend e web).** Primeiro crie o endpoint em que o coordenador vincula uma conta de perfil participante a um participante incluído do estudo, usando o campo de usuário participante previsto na Sprint 2: um para um, conta ativa e de perfil participante, participação ativa (409 com código próprio em cada caso). Depois exponha a listagem das contas de participante ainda não vinculadas, para o formulário. Em seguida crie na área de ações da ficha do participante (ponto de encaixe deixado pelo PBI-22) o botão Vincular conta, com a lista de seleção e a conta vinculada exibida depois. Por fim, inclua no script de demonstração V3\_24\_1\_\_demo\_participant\_account.sql o vínculo do usuário participante@clinicalstudy.test a um participante com visitas, para o PBI-25 e o E2E mobile.

**T112 · Telas Meus estudos e detalhe do estudo.** Primeiro substitua o conteúdo provisório da aba Estudos pela lista de cartões dos estudos do participante (título, estado do estudo e data de inclusão), sobre o esqueleto da T108 e a API da T109. Depois crie o detalhe do estudo, com a descrição e a situação da participação, mostrando a marca RETIRADO quando for o caso, e um atalho para as visitas daquele estudo, que o PBI-25 preenche. Em seguida trate carregamento, erro com Tentar de novo, lista vazia e rolagem puxando para atualizar. Por fim, confira com o participante de demonstração.

**T113 · Testes de backend e de componente do app.** Escreva os testes junto com o código. Primeiro teste o backend: /me/studies só com as participações da conta logada, 403 para outros perfis, 404 para estudo de outro participante e as regras do vínculo (conta já vinculada, perfil errado, participante retirado). Depois escreva os testes de componente do app: login recusado para outro perfil, lista de estudos com dados, vazia e com erro. Por fim, confira a cobertura da fatia no backend.

**T114 · E2E mobile (Maestro), capturas e documentação.** Primeiro instale o Maestro e escreva o fluxo base: abrir o app, entrar com o participante de demonstração, abrir Meus estudos e o detalhe de um estudo, e sair; avise I5 para acrescentar os passos de visitas (T121). Depois rode o fluxo localmente em emulador e guarde a gravação ou as capturas como evidência, já que o CI com emulador fica para decisão do Planning. Por fim, salve capturas do login, da recusa de outro perfil e dos estudos, atualize o README com RF17 e o login mobile e zere o Remaining Work no Azure Boards.

### **PBI-25 · RF18 App: próximas visitas e histórico de comparecimento (Alta · 21h · 22/11 a 23/11)**

Janela de execução (I5): Dom 22/11 e Seg 23/11, com o PBI 100% concluído e integrado na main ao fim de Seg 23/11. Dia 1 (Dom 22/11): T115, T116 e T117 (10h). Dia 2 (Seg 23/11): T118, T119, T120 e T121 (11h).

Critério de aceite: o participante logado no app vê a lista das suas próximas visitas agendadas (data, hora, estudo, nome da visita e atividades previstas) e o histórico de visitas realizadas, ausentes e canceladas; nunca vê dados de outro participante; as telas tratam carregamento, erro e lista vazia.

| Task | Resp. | Horas | Prazo | Depende de |
| :---- | :---- | :---- | :---- | :---- |
| T115 · Contrato das consultas do participante | I5 | 2h | Dom 22/11 | T73, T109 |
| T116 · Backend das consultas de visitas do participante | I5 | 4h | Dom 22/11 | T73, T109, T115 |
| T117 · Tela de próximas visitas | I5 | 4h | Dom 22/11 | T108, T115, T116 |
| T118 · Tela de histórico de comparecimento | I5 | 3h | Seg 23/11 | T117 |
| T119 · Integração com a API real e tratamento de estados | I5 | 3h | Seg 23/11 | T116, T117, T118 |
| T120 · Testes de backend e de componente | I5 | 3h | Seg 23/11 | T116, T119 |
| T121 · Capturas, fluxo E2E mobile das visitas e documentação | I5 | 2h | Seg 23/11 | T114, T119 |

**T115 · Contrato das consultas do participante.** Primeiro descreva no contrato os dois endpoints do participante logado, próximas visitas e histórico, ambos sob /me e sem receber o id do participante, que vem do token. Depois defina o formato de cada visita: id, estudo, nome da visita prevista, data e hora com fuso (ISO 8601), situação e lista com os nomes das atividades previstas, sem os resultados, que não fazem parte do RF18. Por fim, gere os tipos TypeScript desse contrato para o app.

**T116 · Backend das consultas de visitas do participante.** Esta task usa o componente de acesso da T109, já entregue pelo PBI-24. Primeiro implemente a consulta das próximas visitas: AGENDADA com data de agora em diante, da mais próxima à mais distante. Depois implemente o histórico: REALIZADA, AUSENTE e CANCELADA, da mais recente para a mais antiga, com paginação. Em seguida garanta que só aparecem as visitas das participações da conta logada (regra 7), devolvendo vazio, e nunca dados de outro. Por fim, trate o participante retirado (visitas canceladas aparecem no histórico com o motivo, e as próximas ficam vazias), documente no Swagger e confira contra o YAML.

**T117 · Tela de próximas visitas.** Primeiro crie, sobre o esqueleto da T108 e a API da T116, a lista de cartões na aba Visitas, com data e hora em português (por exemplo, "ter, 24/11 às 09:30"), nome da visita, estudo e atividades previstas. Depois acrescente o detalhe da visita, a rolagem puxando para atualizar e o estado de lista vazia com texto claro. Em seguida formate as datas no fuso do aparelho, a partir da data com fuso que vem da API. Por fim, deixe os alvos de toque e os textos acessíveis, sem depender só de cor.

**T118 · Tela de histórico de comparecimento.** Primeiro crie a aba ou tela de histórico com a lista de visitas realizadas, ausentes e canceladas, cada uma com selo de texto e cor. Depois acrescente o resumo no topo (por exemplo, "comparecimento: 3 de 4 visitas"), calculado a partir das realizadas e ausentes, sem contar as canceladas. Por fim, mostre o motivo nas canceladas e trate lista vazia.

**T119 · Integração com a API real e tratamento de estados.** Primeiro revise as chamadas à API real nas duas telas, usando o cliente HTTP da T108. Depois trate carregamento, erro com botão Tentar de novo, falta de conexão e 401 (volta ao login). Por fim, deixe os mocks só para os testes e confira o fluxo no emulador com os dados de demonstração das tasks T74, T84, T105 e T111.

**T120 · Testes de backend e de componente.** Escreva os testes junto com o código. Primeiro teste a ordenação e o filtro das duas consultas, a propriedade dos dados (o participante A nunca vê visita do B) e o participante retirado. Depois escreva os testes de componente das telas com Jest e Testing Library: lista com dados, lista vazia, erro e selos. Por fim, confira a cobertura da fatia.

**T121 · Capturas, fluxo E2E mobile das visitas e documentação.** Primeiro acrescente ao fluxo E2E mobile da T114 o passo de abrir as próximas visitas e o histórico com o participante de demonstração. Depois salve capturas das duas telas e do estado de erro. Por fim, atualize o README com o RF18 e zere o Remaining Work no Azure Boards.

## **Dependências e passagens de bastão**

Como cada PBI só começa quando o anterior está 100% concluído, o caminho crítico é a própria sequência: um dia de atraso em qualquer janela empurra todas as seguintes e a Review. As dependências entre tasks estão nas tabelas de cada PBI; o que muda de mãos ao fim de cada janela está abaixo.

| Passagem | Quem entrega | Quem recebe | O que fica pronto na main | Prazo |
| :---- | :---- | :---- | :---- | :---- |
| PBI-19 para PBI-20 | I1 | I3 | Tabela e entidade da visita agendada (T73), regras e endpoints do agendamento, agenda com ponto de encaixe e dados de demonstração (T74) | Qua 11/11 |
| PBI-20 para PBI-21 | I3 | I6 | Máquina de estados da visita (T80), registro de desfecho, selo na agenda e visitas realizadas de demonstração (T84) | Sex 13/11 |
| PBI-21 para PBI-22 | I6 | I2 | Resultados das avaliações (T89 e T90) e dados de demonstração com resultados | Dom 15/11 |
| PBI-22 para PBI-23 | I2 | I4 | Ficha do participante com histórico e área de ações para a retirada e o vínculo da conta (T97) | Ter 17/11 |
| PBI-23 para PBI-24 | I4 | I7 | Coluna de situação ATIVO ou RETIRADO (T101), cancelamento em cascata e participante retirado de demonstração (T105) | Qui 19/11 |
| PBI-24 para PBI-25 | I7 | I5 | Esqueleto do app (T108), endpoints /me (T109), vínculo da conta (T111) e fluxo E2E mobile base (T114) | Sáb 21/11 |
| PBI-25 para a Review | I5 | Equipe | Próximas visitas e histórico de comparecimento no app, capturas e README | Seg 23/11 |

Se um PBI não fechar até o fim da sua janela, ele passa a ter prioridade total na daily seguinte e o próximo dono só começa quando ele fechar; o atraso é decidido ali, entre reforço do revisor, corte pela ordem da seção de riscos ou carry-over para a Sprint 4.

## **Calendário de prazos por dia**

Todas as 49 tasks têm dia de conclusão. Cada dia pertence a um único PBI e, ao fim do segundo dia de cada janela, o PBI está 100% concluído. Não há dias de reserva: os fins de semana e o feriado de sexta 20/11 (Dia da Consciência Negra) fazem parte das janelas do PBI-21, do PBI-24 e do PBI-25.

| Dia | PBI e responsável | Marco do dia | Tasks que devem estar concluídas |
| :---- | :---- | :---- | :---- |
| Ter 10/11 | PBI-19 · I1 | Sprint Planning; regras validadas; Sprint 3 cadastrada no Azure Boards; tabela da visita agendada, contrato e regras do agendamento | T94 (Planning), T73, T74, T75 |
| Qua 11/11 | PBI-19 · I1 | PBI-19 100%: endpoints, telas, testes e E2E do agendamento na main | T76, T77, T78, T79 |
| Qui 12/11 | PBI-20 · I3 | Máquina de estados, regras e endpoints do desfecho | T80, T81, T82 |
| Sex 13/11 | PBI-20 · I3 | PBI-20 100%: telas do desfecho, dados de demonstração, testes e E2E | T83, T84, T85, T86 |
| Sáb 14/11 | PBI-21 · I6 | Modelo, contrato e regras dos resultados | T87, T88, T89 |
| Dom 15/11 | PBI-21 · I6 | PBI-21 100%: endpoints, tela de resultados, testes e E2E | T90, T91, T92, T93 |
| Seg 16/11 | PBI-22 · I2 | Contrato e consulta agregada do histórico | T95, T96 |
| Ter 17/11 | PBI-22 · I2 | PBI-22 100%: ficha com histórico, testes, E2E do fluxo e checkpoint de meio de Sprint | T97, T98, T99, T100 |
| Qua 18/11 | PBI-23 · I4 | Migration da retirada, regras com cascata e bloqueios, endpoints | T101, T102, T103 |
| Qui 19/11 | PBI-23 · I4 | PBI-23 100%: tela, dados de demonstração, testes e E2E da retirada | T104, T105, T106, T107 |
| Sex 20/11 (feriado) | PBI-24 · I7 | Esqueleto do app, endpoints /me e login mobile | T108, T109, T110 |
| Sáb 21/11 | PBI-24 · I7 | PBI-24 100%: vínculo da conta, meus estudos, testes e E2E mobile base | T111, T112, T113, T114 |
| Dom 22/11 | PBI-25 · I5 | Contrato, backend das visitas do participante e tela de próximas visitas | T115, T116, T117 |
| Seg 23/11 | PBI-25 · I5 | PBI-25 100%; Sprint Review e Retrospectiva; burndown final | T118, T119, T120, T121 |

## **Distribuição entre os integrantes e capacidade**

Cada integrante recebe de 20 a 21 horas, todas concentradas na janela de dois dias do seu PBI. As atribuições são provisórias e devem ser confirmadas no Planning. Como cada integrante é dono de uma fatia inteira, ele escreve os testes da sua fatia, e o olhar independente vem da revisão cruzada: o Pull Request de cada PBI é revisado pelo dono do PBI seguinte, que está livre naquela janela e já vai usar o que foi entregue (o último PBI é revisado por I1).

| Integrante | Foco principal | PBI | Janela | Tasks | Horas | Revisor dos PRs |
| :---- | :---- | :---- | :---- | :---- | :---- | :---- |
| I1 \- João Victor | Scrum Master, arquitetura e DevOps | PBI-19 · RF12 | 10/11 a 11/11 | T73 a T79 | 21h | I3 |
| I3 \- Neto | Backend: domínio, persistência e estudos | PBI-20 · RF13 | 12/11 a 13/11 | T80 a T86 | 21h | I6 |
| I6 \- Paulo | Frontend React: estudos e protótipos | PBI-21 · RF14 | 14/11 a 15/11 | T87 a T93 | 21h | I2 |
| I2 \- Carlos | Product Owner, testes e documentação | PBI-22 · RF15 | 16/11 a 17/11 (Planning em 10/11) | T94 a T100 | 20h | I4 |
| I4 \- João Roberto | Backend: usuários, segurança e autorização | PBI-23 · RF16 | 18/11 a 19/11 | T101 a T107 | 21h | I7 |
| I7 \- Julio | Mobile, protótipos e testes | PBI-24 · RF17 | 20/11 a 21/11 | T108 a T114 | 21h | I5 |
| I5 \- Johnattan | Frontend React: autenticação e integração | PBI-25 · RF18 | 22/11 a 23/11 | T115 a T121 | 21h | I1 |
| Total |  | 7 PBIs | 10/11 a 23/11 | 49 tasks | 146h |  |

| Indicador | Valor |
| :---- | :---- |
| Janela de cada integrante | 2 dias seguidos, um PBI por vez |
| Carga por dia de trabalho | De 9h a 12h por dia durante a janela (cerca de 10,5h por dia em média) |
| Horas planejadas nas tasks | 146 horas |
| Margem entre janelas | Nenhuma: os 14 dias da Sprint estão todos ocupados |

Pontos de atenção: a carga diária é bem maior que a hipótese de 12 horas por semana usada nas Sprints anteriores, então a disponibilidade de cada integrante na sua janela precisa ser confirmada no Planning. As janelas do PBI-21 (sábado e domingo), do PBI-24 (feriado de sexta e sábado) e do PBI-25 (domingo e segunda, dia da Review) caem em dias não úteis ou na véspera da entrega; se algum dono não puder, o PBI-24 pode trocar de lugar com o PBI-23 ou com o PBI-22, porque só depende da Sprint 2 e da ficha do PBI-22. I2 concentra o Planning na terça 10/11, antes da sua janela. O carry-over da Sprint 2 e os defeitos de integração não têm margem própria e precisam ser absorvidos dentro das janelas.

## **Como organizar no Azure DevOps**

Use o processo Scrum no Azure Boards. Primeiro, cadastre a iteração Sprint 3 de 10/11/2026 a 23/11/2026 em Project Settings \> Project configuration \> Iterations. Depois crie os sete PBIs (PBI-19 a PBI-25) com descrição, prioridade e critérios de aceite, associe todos à Sprint 3 e crie as 49 tasks como filhas dos respectivos PBIs, com Assigned To, Remaining Work em horas e a data de término de cada task conforme o calendário por dia. Em seguida, registre as dependências entre tasks e entre PBIs com os vínculos Predecessor e Successor, para que o painel mostre a sequência, e a disponibilidade dos sete integrantes em Sprints \> Capacity, cada um apenas nos dois dias da sua janela. Por fim, abra defeitos como itens do tipo Bug ligados à task de origem e atualize o Remaining Work todos os dias, para que o burndown usado no README da Release 2 reflita a realidade. O campo Effort do PBI serve para estimativa relativa; as horas ficam nas tasks.

## **Definition of Done**

Uma task ou PBI só é concluído quando: os critérios de aceite estão atendidos e a funcionalidade é demonstrável com dados fictícios; o código foi revisado por outro integrante, passou no CI e entrou na branch principal por Pull Request; existem testes automatizados (unitários, de integração ou de componente) que passam e, no backend, a cobertura de linhas do código de negócio permanece em 80% ou mais; erros e validações das regras de negócio são tratados e mostrados com mensagens claras; o Swagger UI e o contrato YAML refletem os endpoints; os dados de demonstração da fatia estão no perfil demo; documentação e capturas de tela relevantes estão no repositório; e os requisitos RF01 a RF11 das Sprints anteriores continuam funcionando. Para o PBI, vale ainda que todas as suas tasks estão com Remaining Work zerado ao fim da janela, porque o PBI seguinte só começa depois disso.

## **Cerimônias e acompanhamento**

| Evento | Quando | Finalidade nesta Sprint |
| :---- | :---- | :---- |
| Sprint Planning | Ter 10/11 | Validar meta, regras, PBIs, horas, responsáveis, janelas, disponibilidade e carry-over da Sprint 2 |
| Daily Scrum | Todos os dias da Sprint, 15 minutos | Sincronizar com o dono da janela, expor impedimentos e confirmar se o PBI do dia vai fechar |
| Passagem de bastão | Fim de cada janela (11, 13, 15, 17, 19, 21 e 23/11), 15 minutos | O dono apresenta o que ficou na main, confirma o PBI 100% concluído e passa ao dono do PBI seguinte o que ele vai usar |
| Checkpoint de integração | Ter 17/11 | Meio da Sprint: ver o que atrasou e decidir cortes de escopo nos PBIs 23, 24 e 25, se preciso (T100) |
| Sprint Review e Retrospectiva | Seg 23/11, depois do fechamento do PBI-25 | Demonstrar o incremento (acompanhamento na web e app do participante), receber feedback e propor melhorias para a Sprint 4 |

## **Riscos e respostas**

| Risco | Resposta |
| :---- | :---- |
| Atraso em qualquer janela empurra todas as seguintes, sem dias de reserva | Decidir na daily do dia seguinte: o PBI atrasado tem prioridade, o revisor ajuda e, se necessário, corta-se pela ordem abaixo ou o item vai para a Sprint 4 |
| Carga de cerca de 10,5 horas por dia na janela, acima da hipótese anterior de 12 horas por semana | Confirmar a disponibilidade de cada dono no Planning; se não couber, reduzir o escopo do PBI pela ordem de corte ou liberar as tasks de testes e documentação para o revisor |
| Janelas em fim de semana e feriado (PBI-21, PBI-24 e PBI-25) | Confirmar com os donos no Planning; o PBI-24 pode trocar de lugar com os PBIs 22 ou 23, que não o bloqueiam |
| PBI-25 termina no dia da Review | Testes escritos junto com o código, roteiro do E2E mobile montado no PBI-24 e rascunho da Review pronto antes; só o ajuste final fica para segunda |
| Dois desfechos ou retirada pela metade por concorrência | Registro atômico com versão otimista ou bloqueio da linha (T81), retirada em uma transação (T102) e testes dedicados (T85, T106) |
| Participante retirado ainda poder agendar ou registrar, já que a coluna de situação só existe a partir do PBI-23 | A T102 acrescenta os bloqueios nos serviços de agendamento, desfecho e resultados, com testes para cada um |
| Conflito de versões de migration entre branches | Faixas de versão por PBI e conferência da maior versão da Sprint 2 no Planning (regra 10) |
| E2E mobile instável ou pesado no CI | Maestro rodando localmente com evidência; decisão sobre o CI no Planning |
| Carry-over da Sprint 2 maior que o esperado | Levantar no Planning (T94); como não há folga entre as janelas, o que não couber vai para a Sprint 4, exceto defeito crítico |
| Escopo e data da Release 2 indefinidos na especificação | Confirmar com a professora na primeira semana e ajustar a proposta da Sprint 4 |

Se a capacidade real for menor, a ordem de corte é: primeiro a agenda do estudo por período (T77) e o filtro por situação do histórico (T97), depois o reagendamento (T75 e T77) e o resumo de comparecimento do app (T118), e só então os cenários de erro dos E2E. O agendamento, o desfecho, os resultados, o histórico, a retirada, o login mobile e as listas de estudos e visitas do app não devem ser cortados, porque formam o fluxo de acompanhamento que a Sprint promete.
