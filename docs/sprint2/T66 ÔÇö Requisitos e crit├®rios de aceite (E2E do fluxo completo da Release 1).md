# T66 — Requisitos e critérios de aceite (E2E do fluxo completo da Release 1)

Oct 31, 2026 · @Carlos André

## Escopo da T66

A T66 entrega o teste E2E do fluxo completo da Release 1: o pesquisador prepara um estudo, o coordenador inclui um participante, uma segunda inclusão é bloqueada por falta de vagas, e RF01 a RF04 continuam funcionando. Ela vale 4 horas, é de I7, vence na sexta 06/11 (antes do congelamento de código das 18h) e depende da T61 (tela de inclusão), da T63 (dados de demonstração) e da T65 (infraestrutura E2E).

|  | O que é |
| --- | --- |
| Entra na T66 | Ambiente sem mocks; page objects das telas dos RF05 a RF11; cenário feliz em duas etapas (pesquisador e coordenador); cenário de erro de vagas esgotadas; regressão mínima de RF01 a RF04 (login, cadastro de usuário e de estudo); execução no CI; capturas para o README; atualização do README de testes |
| Fica de fora | Infraestrutura, login por API e sessão por perfil (T65), dados de demonstração (T63), tela de inclusão (T61), E2E do aplicativo mobile (Sprint 3) e RF12 em diante |
| Entrega para outras tasks | Evidências de E2E e capturas de sucesso e de erro para o README principal (T70) e o README da release (T71); o mesmo roteiro serve de ensaio da demonstração da T72 |

A ordem do cenário segue o roteiro de demonstração da T72: o pesquisador define o estudo, o coordenador inclui um participante e uma tentativa é bloqueada por falta de vagas. Se o E2E passar, vocês têm um ensaio automático da apresentação.

## Pré-requisitos a confirmar antes de começar

A T66 só fecha na sexta porque a T61 fecha no mesmo dia; por isso, confirme o que já existe na quarta e na quinta e deixe só o passo final de inclusão para a sexta. Eu não vi o código, só os planos, então os itens "anotar" dependem de você conferir o repositório.

- [ ] **T65 na main:** infraestrutura do Playwright, login por API com sessão por perfil, `LoginPage` e job de CI verde. Sem isso a T66 vira retrabalho.
- [ ] **Telas integradas na main, sem mocks:** estado do estudo (T43), critérios (T46), visitas (T49), candidatos e ficha (T52), avaliação (T55), consentimento (T58). A inclusão (T61) entra na sexta.
- [ ] **Mocks desligados:** a T40 criou o MSW, ativado por variável de ambiente. Anotar o nome da variável e garantir que ela fica desligada no `.env.e2e` e no CI.
- [ ] **Dados de demonstração (T63):** anotar como ativar o perfil `demo`, quais usuários e senhas fictícias existem por perfil, quais são os dois estudos (um em planejamento e outro em recrutamento) e qual está a uma vaga do limite.
- [ ] **Quem enxerga o quê:** confirmar se o coordenador vê um estudo criado pelo pesquisador (o cenário precisa disso) e se há vínculo entre estudo e coordenador.
- [ ] **Comportamento do bloqueio de vagas:** anotar se, com as vagas esgotadas, a tela de inclusão desabilita o botão com a pré-condição "vaga disponível" pendente, se o backend devolve 409 com um código próprio, e qual é o texto exato da mensagem de vagas esgotadas. O plano diz que a segunda inclusão é bloqueada com essa mensagem.
- [ ] **Textos e rótulos reais:** botões de avançar estado e de incluir, diálogos de confirmação, formulários de critério, visita, candidato, avaliação e consentimento, contador "X de N vagas" e código do candidato (CAND-001).
- [ ] **Regras que o fluxo precisa respeitar:** sair de planejamento exige um critério de inclusão e uma visita; candidato só é cadastrado com o estudo em recrutamento; a inclusão exige elegibilidade, consentimento e vaga.
- [ ] **Telas do RF01 e RF04 da Sprint 1:** anotar como o administrador cadastra um usuário e como o pesquisador cadastra um estudo (campos e rotas), para a regressão.

## Requisitos e critérios de aceite

São 16 requisitos, e a T66 só está pronta quando todos os critérios forem verificáveis na branch principal. Os RQ-12 e RQ-13 são recomendações minhas, além do que o plano pede, e só entram se o tempo permitir. Mantenha as convenções da T65 (pastas `e2e/pages`, `e2e/fixtures`, `e2e/setup` e `e2e/tests`, sessão por perfil e seletores por papel e rótulo).

| ID | Requisito | Critério de aceite |
| --- | --- | --- |
| RQ-01 | Ambiente real, sem mocks | O MSW está desligado no `.env.e2e` e no CI; o backend e o PostgreSQL são reais, com as migrations aplicadas e os dados da T63 carregados pelo perfil `demo`. Uma verificação antes dos testes (health check e checagem da variável de mocks) falha com mensagem clara se algo estiver fora do esperado |
| RQ-02 | Dados únicos por execução | O cenário cria seu próprio estudo com um sufixo único (por exemplo, data e hora) no título, e candidatos com nomes fictícios únicos. Rodar a suíte duas vezes seguidas, localmente e sem reiniciar o banco, passa nas duas. Nenhum teste altera ou consome os estudos de demonstração da T63 |
| RQ-03 | Helpers de API para preparar estado | Existem funções que criam estudo, critérios, visita, avançam o estado, cadastram candidato, avaliam e registram consentimento usando a sessão por perfil da T65. Elas só preparam o estado dos cenários de erro e da regressão; o cenário feliz percorre todos esses passos pela interface |
| RQ-04 | Page objects das telas dos RF05 a RF11 | Existem objetos para estudos (lista e cadastro), detalhe do estudo e estado, critérios, visitas, candidatos (lista, cadastro e ficha), avaliação de elegibilidade, consentimento, inclusão e participantes, e cadastro de usuários. Seguem as convenções da T65, e os testes não têm seletores soltos |
| RQ-05 | Cenário feliz, etapa 1: pesquisador | Entra como pesquisador; cria um estudo com título, objetivo, responsável, datas e limite de participantes (use limite maior que 1); define um critério de inclusão, um de exclusão e uma visita prevista com atividade; avança para recrutamento confirmando o diálogo. Confere que o indicador mostra recrutamento e que critérios e visitas ficam somente leitura |
| RQ-06 | Cenário feliz, etapa 2: coordenador | Em um segundo contexto de navegador, entra como coordenador, abre o mesmo estudo e cadastra um candidato (o código aparece). Registra a avaliação respondendo a todos os critérios (inclusão se aplica, exclusão não se aplica) e confere o resultado "elegível"; registra o consentimento e confere o comprovante; confere as quatro pré-condições atendidas |
| RQ-07 | Cenário feliz, inclusão | O coordenador inclui o candidato confirmando o diálogo; o participante aparece na aba Participantes, a situação do candidato vira incluído e o contador de vagas passa de "0 de N" para "1 de N" |
| RQ-08 | Cenário de erro: vagas esgotadas | Com um estudo de limite 1 em recrutamento (criado pelos helpers do RQ-03), inclui o primeiro candidato e confere "1 de 1". Um segundo candidato elegível e com consentimento não pode ser incluído: a interface mostra a mensagem de vagas esgotadas (ou mantém o botão desabilitado com a pré-condição pendente, conforme a T61), nenhuma participação nova é criada e o contador continua em "1 de 1" |
| RQ-09 | Regressão RF01 e RF02: usuários e login | O administrador cadastra um usuário com nome e e-mail únicos, que aparece na listagem. O login válido, o inválido e o logout já são cobertos pela T65 e continuam passando na suíte completa |
| RQ-10 | Regressão RF04: estudos | O pesquisador cadastra um estudo com todos os campos, ele aparece na listagem e na página de detalhe; um campo obrigatório vazio mostra o erro de validação |
| RQ-11 | Estabilidade e legibilidade | O cenário feliz usa `test.step` para cada etapa, para a falha indicar onde parou; não há `waitForTimeout`, os testes passam com `--repeat-each=5` e dois números de workers, e dependem de estado criado por eles mesmos, não de ordem entre arquivos |
| RQ-12 | Recomendado: erros de regra no fluxo de recrutamento | Cobrir, nesta ordem e até onde der o tempo: candidato duplicado no mesmo estudo, avaliação incompleta, candidato não elegível e candidato sem consentimento sem poder ser incluído, e estudo sem critério de inclusão ou sem visita que não consegue sair de planejamento. Cada um confere a mensagem de erro da regra |
| RQ-13 | Recomendado: controle de acesso por perfil (RF03) | O coordenador não vê as abas de Critérios e Visitas nem o botão de avançar estado; o pesquisador não vê o cadastro de candidato; o administrador não acessa o recrutamento |
| RQ-14 | Execução no CI | O mesmo job da T65 (ou um segundo, com o mesmo gatilho) roda a suíte completa com o perfil `demo` carregado; o `timeout-minutes` foi ajustado ao novo tempo; o job fica verde na main e vermelho quando um teste é quebrado de propósito; relatório e traces são enviados com `if: always()` |
| RQ-15 | Capturas e evidências para o README | Os passos-chave geram capturas de tela nomeadas, de sucesso (estado em recrutamento, resultado elegível, comprovante de consentimento, participante incluído com "1 de N") e de erro (vagas esgotadas), salvas em uma pasta enviada como artefato do CI e prontas para a T70 e a T71 |
| RQ-16 | Documentação e controle de qualidade | A seção “Testes E2E” do README (T65) lista os cenários, os dados usados, como rodar um só e o relatório; defeitos encontrados viram Bug no Azure Boards ligado à task de origem, e nenhum teste fica com `skip` ou `fixme` sem um Bug citado no comentário; `tsc` e lint passam e o PR é revisado por outro integrante |

## Rastreabilidade: o que cada teste cobre

Cada requisito funcional da Release 1 (RF01 a RF11) tem ao menos um passo coberto pelo E2E; os que dependem dos itens recomendados estão marcados. Os nomes dos arquivos são sugestões e servem para o README e para a avaliação.

| Requisito funcional | Onde é coberto | Arquivo sugerido |
| --- | --- | --- |
| RF01 Usuários | Regressão: administrador cadastra usuário (RQ-09) | `regressao-sprint1.spec.ts` |
| RF02 Autenticação | Login válido, inválido e logout da T65; login de cada perfil no fluxo completo | `login.spec.ts` (T65) e `fluxo-completo.spec.ts` |
| RF03 Controle de acesso | Cada etapa do fluxo usa o perfil correto; teste de permissões se o RQ-13 entrar | `fluxo-completo.spec.ts` e `acesso-por-perfil.spec.ts` |
| RF04 Estudos | Criação do estudo no fluxo (RQ-05) e regressão com validação (RQ-10) | `fluxo-completo.spec.ts` e `regressao-sprint1.spec.ts` |
| RF05 Critérios | Critério de inclusão e de exclusão no fluxo; estudo sem critério não avança se o RQ-12 entrar | `fluxo-completo.spec.ts` e `regras-recrutamento.spec.ts` |
| RF06 Visitas | Visita com atividade no fluxo (RQ-05) | `fluxo-completo.spec.ts` |
| RF07 Estados | Avanço para recrutamento com confirmação e telas em somente leitura (RQ-05) | `fluxo-completo.spec.ts` |
| RF08 Candidatos | Cadastro no fluxo (RQ-06); duplicado se o RQ-12 entrar | `fluxo-completo.spec.ts` e `regras-recrutamento.spec.ts` |
| RF09 Elegibilidade | Avaliação completa e resultado "elegível" (RQ-06); avaliação incompleta e inelegibilidade se o RQ-12 entrar | `fluxo-completo.spec.ts` e `regras-recrutamento.spec.ts` |
| RF10 Consentimento | Registro e comprovante (RQ-06); inclusão sem consentimento se o RQ-12 entrar | `fluxo-completo.spec.ts` e `regras-recrutamento.spec.ts` |
| RF11 Inclusão e limite de vagas | Inclusão e contador (RQ-07) e vagas esgotadas (RQ-08) | `fluxo-completo.spec.ts` e `vagas-esgotadas.spec.ts` |

## Checklist de implementação por dia

A T66 depende da T61, que fecha no mesmo dia dela, então o plano é escrever tudo o que não depende da inclusão na quarta e na quinta e deixar para a sexta só o passo final, o cenário de erro, o CI e a documentação. As 4 horas se dividem em 1h15 na quarta, 1h30 na quinta e 1h15 na sexta. Meta: PR final integrado até as 16h de sexta, para sobrar tempo antes do congelamento de código das 18h.

### Quarta 04/11 (cerca de 1h15): base, helpers e regressão

- [ ] Criar a branch e confirmar os pré-requisitos da seção anterior, anotando as respostas no PR
- [ ] RQ-01: ajustar `.env.e2e` e o CI (mocks desligados, perfil `demo`) e criar a verificação do ambiente antes dos testes
- [ ] RQ-02 e RQ-03: criar os helpers de API de estudo, critérios, visita, avanço de estado e candidato, com sufixo único nos nomes
- [ ] RQ-04: escrever os page objects de estudos, detalhe e estado, critérios, visitas e cadastro de usuários
- [ ] RQ-09 e RQ-10: escrever a regressão de RF01 (cadastro de usuário) e RF04 (cadastro e validação de estudo)
- [ ] Rodar a suíte duas vezes seguidas e ver os dois ciclos passarem (RQ-02)

### Quinta 05/11 (cerca de 1h30): cenário feliz até as pré-condições

- [ ] Conferir se os dados da T63 já estão na main e atualizar o README com a ativação do perfil `demo`
- [ ] RQ-04: escrever os page objects de candidatos (lista, cadastro e ficha), avaliação e consentimento
- [ ] RQ-03: completar os helpers com avaliação, consentimento e criação de candidato elegível e consentido
- [ ] RQ-05: escrever a etapa do pesquisador do cenário feliz, com `test.step` por passo
- [ ] RQ-06: escrever a etapa do coordenador, em um segundo contexto de navegador, até as quatro pré-condições atendidas
- [ ] Rodar com `--repeat-each=5` e corrigir instáveis
- [ ] Abrir um PR parcial, com o CI verde, e integrá-lo; qualquer defeito encontrado vira Bug no Azure Boards, ligado à task de origem

### Sexta 06/11 (cerca de 1h15): inclusão, erro, CI e documentação

- [ ] Atualizar a branch e confirmar que a T61 está na main
- [ ] RQ-04: escrever o page object de inclusão e participantes
- [ ] RQ-07: completar o cenário feliz com a inclusão, a aba Participantes, a situação do candidato e o contador "1 de N"
- [ ] RQ-08: escrever o cenário de vagas esgotadas com estudo de limite 1 criado pelos helpers
- [ ] RQ-15: configurar as capturas nomeadas dos passos de sucesso e de erro e salvar em pasta enviada como artefato
- [ ] RQ-14: ajustar o `timeout-minutes`, rodar o CI, ver o job verde e quebrar um teste de propósito para ver o vermelho e o trace; desfazer a quebra
- [ ] RQ-12 e RQ-13: se sobrar tempo, escrever nessa ordem os erros de regra e o controle de acesso; se não sobrar, registrar no Azure Boards como pendência da Release 2
- [ ] RQ-16: atualizar o README, passar `tsc` e lint e pedir a revisão
- [ ] Integrar na main até as 16h, com o CI verde, e avisar I2 (README e diagramas) e I1 (tag da release) de que o resultado do E2E está disponível

## Definition of Done e evidências

A T66 só vai para Done quando os requisitos RQ-01 a RQ-11 e RQ-14 a RQ-16 estiverem atendidos (os RQ-12 e RQ-13 são opcionais) e as evidências abaixo estiverem anexadas ao PR e à task no Azure Boards. Isso segue a Definition of Done da Sprint 2: critérios atendidos, código revisado por outro integrante, CI verde, PR integrado à main e documentação atualizada.

- [ ] Link do PR aprovado por outro integrante e integrado à main antes do congelamento das 18h de sexta
- [ ] Link de uma execução verde da suíte completa no CI, na main
- [ ] Link ou captura da execução vermelha de propósito, com o trace aberto
- [ ] Relatório HTML do Playwright com o cenário feliz, o de vagas esgotadas e a regressão passando
- [ ] Saída do `--repeat-each=5` sem falha e duas execuções seguidas sem reiniciar o banco
- [ ] Pasta de capturas nomeadas dos passos de sucesso e de erro, entregue a I2 para o README principal (T70) e para o README da release (T71)
- [ ] Contagem de testes (passaram, falharam, ignorados) para o texto de testes da release
- [ ] Lista de Bugs abertos no Azure Boards a partir do E2E, com o estado de cada um
- [ ] Remaining Work em 0 e estado Done na task T66, com um comentário indicando onde estão a suíte e o relatório

Se a T66 passar das 4 horas estimadas, registre quanto passou e por quê (por exemplo, T61 atrasada ou muitos defeitos de integração), porque isso alimenta o burndown e a retrospectiva da Sprint.

## Pontos que costumam ser esquecidos

Oito armadilhas comuns fazem esse tipo de E2E passar uma vez e falhar na seguinte, ou não provar nada. Confira cada uma antes de abrir o PR.

| # | Armadilha | Como evitar |
| --- | --- | --- |
| 1 | O estudo de demonstração a uma vaga do limite é consumido na primeira execução; na segunda, a primeira inclusão já é bloqueada e o teste falha. O plano da Sprint cita esse estudo, mas ele só serve em banco novo | Crie um estudo de limite 1 por execução com os helpers (RQ-08) e deixe o estudo da T63 para a demonstração ao vivo da T72, sem ser alterado por teste |
| 2 | Logar os dois perfis na mesma aba sobrescreve a sessão do primeiro | Use dois contextos de navegador, cada um com o estado de autenticação do perfil (T65), e mantenha cada página ligada ao seu contexto |
| 3 | Mocks ligados por engano: os testes passam, mas nunca tocaram o backend | Faça a verificação de ambiente do RQ-01 e confira, em uma execução, que as requisições vão para a API real |
| 4 | Valores que mudam depois de uma ação, como o contador "X de N" e a situação do candidato, lidos antes de atualizar | Use as verificações do próprio Playwright (`expect(...).toHaveText`), que repetem até o valor chegar, e recarregue a página depois de qualquer preparação feita por API |
| 5 | Diálogos de confirmação ao avançar o estado e ao incluir deixam o teste parado sem clicar em confirmar | Inclua a confirmação nos métodos do page object, e não nos testes |
| 6 | Regras de ordem: candidato só é cadastrado com o estudo em recrutamento, e o estudo só avança com um critério de inclusão e uma visita. Um helper que pula essa ordem falha com 409 | Nos helpers, siga a ordem: estudo, critério, visita, avanço de estado, candidato, avaliação, consentimento |
| 7 | Dados repetidos (candidato com mesmo nome e data de nascimento, e-mail de usuário, título de estudo) e datas fixas que a validação do estudo passa a recusar com o tempo | Use sufixo único nos nomes e e-mails e calcule as datas do estudo a partir do dia da execução, e não fixas no código |
| 8 | A T61 atrasa e a T66 inteira atrasa junto | Integre na quinta o cenário até as pré-condições (PR parcial) e deixe na sexta só a inclusão e o erro de vagas; se a T61 passar do meio-dia de sexta, avise I2 e I1 na daily e priorize inclusão, erro de vagas e CI antes de capturas e documentação |

Por fim, nunca deixe um teste vermelho virar `skip` só para o CI ficar verde: se o E2E achou um defeito, ele cumpriu o papel dele. Abra o Bug no Azure Boards, ligado à task de origem, e priorize a correção antes do congelamento das 18h; se não der tempo, combine com I2 e I1 como o problema entra no README da release e na decisão sobre a tag.
