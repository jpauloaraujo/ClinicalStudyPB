# T50, T55, T58, T59, T61, T65 e T66: o que foi implementado

Registro de 07/11/2026. Antes de escrever qualquer coisa, conferi o que já existia, para não refazer o que estava pronto. Para completar a T59 e a T66 foi preciso entregar também as telas **T58** (consentimento) e **T61** (inclusão e aba Participantes), que eram as dependências que faltavam; elas estão documentadas aqui também.

## Resumo

| Task | Situação | O que foi feito | O que falta |
|---|---|---|---|
| **T50** Testes do RF06 (visitas) | ✅ Completa | 3 suítes de backend e 8 testes de componente novos | Fluxo normal: PR revisado e CI verde |
| **T55** Tela de avaliação de elegibilidade | ✅ Já estava completa | Nada de código; conferi a cobertura contra o plano | Fluxo normal |
| **T58** Tela de consentimento | ✅ Completa | `SecaoConsentimento` real, 12 testes de componente | Fluxo normal |
| **T59** Testes do RF10 | ✅ Completa | 2 suítes de backend (30 testes) e os testes de componente da T58 | Fluxo normal |
| **T61** Tela de inclusão e aba Participantes | ✅ Completa | `SecaoInclusao`, `AbaParticipantes`, mensagens por código, 21 testes de componente | Fluxo normal |
| **T65** Infraestrutura E2E | ✅ Completa no código | Verificação de ambiente; execução local conferida | Só o que depende do GitHub e de pessoas (seção 6) |
| **T66** E2E do fluxo completo | ✅ Completa no código | Toda a base e todos os cenários, já ligados às telas reais | Ver o job verde no GitHub e o Azure Boards |

Números de 07/11/2026: backend **333 testes**, 0 falhas, cobertura de domínio e serviços **98,3%**; web **126** testes de componente; E2E **29 passam, nenhum ignorado**.

## 1. T50, testes do RF06

**O que já existia:** `RecrutamentoIntegrationTest.visitasEAcoes` e `AbaVisitas.test.tsx` com 9 testes.

| Arquivo | Testes | O que cobre |
|---|---|---|
| `VisitaServiceTest` | 14 | Dia repetido (criar e editar), ordem pelo dia e reordenação, cascata das ações, estado bloqueado (3 estados × 6 operações), 404 |
| `VisitaRequestValidacaoTest` | 19 | Nome (100/101), dia (-1/0), tolerância (-1/0/30/31), descrição da ação (500/501), categoria |
| `VisitaEndpointTest` | 24 | 201, 400 por campo, 401, 403 por perfil, 404, 409 de dia repetido e de estado, ordenação e remoção em cascata pela API |
| `AbaVisitas.test.tsx` (ampliado) | +8 (17) | Erro de carregamento e de servidor, carregando, erro de campo, edição de visita e de atividade, erro ao excluir, somente leitura |
| `support/CenarioRecrutamento.java` (novo) | n/a | Auxiliar compartilhado: cria estudo, critérios, visitas, candidatos, avaliação e consentimento pela API (reutilizável por T53, T56 e T62) |

## 2. T55, tela de avaliação de elegibilidade

`SecaoAvaliacao` e os 12 testes de `FichaCandidatoPage.test.tsx` já cobriam o plano (dois grupos, explicação da exclusão, botão desabilitado, resultado, critérios reprovadores, histórico, erros 400 e 409). **Nenhum código foi alterado.** A avaliação agora também é provada pela interface no E2E. Achado: depois de salvar, o formulário recomeça vazio e a reavaliação exige todas as respostas de novo.

## 3. T58, tela de consentimento

**Arquivo:** `src/pages/estudos/detalhe/candidato/SecaoConsentimento.tsx` (substituiu o espaço reservado); `FichaCandidatoPage` passa `aoRegistrar={recarregar}`; estilo `.termo` em `recrutamento.css`.

**Comportamento (RF10)**
- Carrega o consentimento; o 404 significa só "ainda não registrado". Outros erros de carga aparecem em alerta e o formulário some.
- Sem registro: aviso "Consentimento simulado, dados fictícios", termo `TCLE-SIMULADO v1` em caixa rolável, caixa de confirmação obrigatória, campo de data e hora (padrão: agora) e botão **Registrar**, desabilitado até confirmar.
- A data só é enviada se o usuário a editar; sem edição o servidor usa o instante atual (evita diferença de fuso entre navegador e API).
- Depois de registrar: mostra o **comprovante** (data e hora, responsável e versão do termo), o formulário desaparece e a ficha é recarregada (situação "Consentido").
- Erros 409 (`CONSENTIMENTO_DUPLICADO` e outros) e 400 (data futura) aparecem em alerta e o formulário continua.
- Somente leitura, com aviso, para quem não é coordenador, com o estudo fora de recrutamento ou com candidato já incluído.

**Testes (`SecaoConsentimento.test.tsx`, 12):** termo e aviso, botão desabilitado até confirmar, registro sem data, registro com data editada, comprovante depois de registrar, comprovante direto, 409, 400, erro de carregamento, carregando, perfil sem permissão, estudo fora de recrutamento, candidato incluído.

## 4. T59, testes do RF10

| Arquivo | Testes | O que cobre |
|---|---|---|
| `ConsentimentoServiceTest` | 15 | Padrões (agora, versão padrão, usuário como responsável), data passada e versão própria, data futura (400), duplicidade, fora de recrutamento (3 estados), candidato incluído, situação do candidato, `buscar` |
| `ConsentimentoEndpointTest` | 15 | 201 com comprovante, 400, 403 por perfil, 401, 404, 409 (3 códigos), resumo no detalhe do candidato, consentimento antes da avaliação |
| `SecaoConsentimento.test.tsx` | 12 | Componente da T58 (ver seção 3) |

## 5. T61, tela de inclusão e aba Participantes

**Arquivos:** `candidato/SecaoInclusao.tsx` (substituiu o espaço reservado), `abas/AbaParticipantes.tsx` (nova), `mensagensInclusao.ts` (novo), `routes/router.tsx` (a rota Participantes usa a aba real; `AbaProvisoria.tsx` foi removida por ficar sem uso), `FichaCandidatoPage` (passa `estudo` e `aoIncluir`).

**Comportamento (RF11)**
- Lista as quatro pré-condições, cada uma "Atendida" ou "Pendente" com o motivo: estudo em recrutamento, candidato elegível (avaliação aprovada), consentimento registrado e vaga disponível ("Vagas esgotadas.").
- O botão **Incluir no estudo** só habilita com as quatro atendidas e só para o coordenador; abre confirmação; ao confirmar chama a API, atualiza as vagas e recarrega a ficha (situação "Incluído").
- Cada código 409 vira uma mensagem clara (`mensagensInclusao.ts`): `ESTUDO_FORA_DE_RECRUTAMENTO`, `CANDIDATO_JA_INCLUIDO`, `CANDIDATO_NAO_ELEGIVEL`, `CONSENTIMENTO_AUSENTE` e `VAGAS_ESGOTADAS`; um código desconhecido cai na mensagem do servidor. A tela só espelha as regras: o backend continua decidindo.
- Candidato incluído: aviso "Candidato incluído como participante do estudo." e sem botão.
- Aba **Participantes**: tabela com código, nome, data de inclusão e quem incluiu, e o contador "X de N vagas"; estados vazio, carregando e erro.

**Testes:** `SecaoInclusao.test.tsx` (4 casos parametrizados de pré-condição pendente, vaga esgotada, confirmação, 5 códigos de erro parametrizados, código desconhecido, incluído, perfil, erro de vagas) e `AbaParticipantes.test.tsx` (5): lista e contador, vazio, erro, acesso negado, carregando.

## 6. T65, infraestrutura E2E

**O que já existia (mantido):** Playwright, `playwright.config.ts`, setup de usuários e sessões, page objects de login, `login.spec.ts` e `sessao.spec.ts`, job `e2e` do CI e a seção do README.

**Acrescentado:** `e2e/setup/ambiente.setup.ts` (confirma que o backend responde, que a web abre e que o Mock Service Worker está desligado).

**Verificação local** (backend real com H2 em memória, `spring-boot:run` com o classpath de teste, e a web em `npm run dev`): suíte completa passa; duas execuções seguidas passam; `--repeat-each=5` com 4 trabalhadores: 137 passam; `--repeat-each=3` com 1 trabalhador: 83 passam; `tsc -p tsconfig.e2e.json` e `eslint` limpos.

**O que depende do GitHub e de pessoas (não é código)**
1. Ver o job `e2e` verde no GitHub.
2. Quebrar um teste de propósito para ver o job vermelho e o trace baixável.
3. Revisão do PR por outro integrante.
4. Um colega rodar os testes só pelo README em um clone limpo.
5. Atualizar o Azure Boards.

## 7. T66, E2E do fluxo completo

| Camada | Arquivos (`web/ClinicalStudyPB/e2e`) |
|---|---|
| Suporte | `support/api.ts`, `support/unico.ts`, `support/capturas.ts`, `support/pendencias.ts` |
| Ambiente | `setup/ambiente.setup.ts` |
| Page objects | `UsuariosPage`, `EstudosPage`, `EstudoDetalhePage`, `CriteriosPage`, `VisitasPage`, `CandidatosPage`, `FichaCandidatoPage`, `ParticipantesPage` |
| Testes | `fluxo-completo`, `vagas-esgotadas`, `regras-recrutamento`, `regressao-sprint1`, `acesso-por-perfil` |

**Requisitos do documento de requisitos da T66:** RQ-01 a RQ-13 e RQ-15 cumpridos; RQ-14 (CI) usa o mesmo job `e2e`, que já executa a suíte e envia `test-results` (com as 12 capturas) como artefato, e falta só vê-lo verde no GitHub; RQ-16 depende do Azure Boards (I2) para registrar os Bugs.

**Ao ligar as telas T58 e T61**, os quatro testes que estavam ignorados passaram sem alterar a lógica; só foi preciso tornar exatos os nomes das regiões (`Inclusão` casava também com "Critérios de inclusão"). A chave `support/pendencias.ts` ficou com os dois valores em `false`.

## 8. Como reproduzir tudo localmente

```bash
# Backend com banco em memória (sem PostgreSQL), na porta 8081
cd backend/ClinicalStudyPB
SPRING_DATASOURCE_URL="jdbc:h2:mem:e2e;MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE;DEFAULT_NULL_ORDERING=HIGH;DB_CLOSE_DELAY=-1" \
SPRING_DATASOURCE_USERNAME=sa SPRING_DATASOURCE_PASSWORD= SPRING_DATASOURCE_DRIVER_CLASS_NAME=org.h2.Driver \
JWT_SECRET=segredo-ficticio-local-e2e-com-mais-de-256-bits-0123456789 \
./mvnw spring-boot:run -Dspring-boot.run.useTestClasspath=true

# Web (outro terminal) e testes (terceiro terminal)
cd web/ClinicalStudyPB && npm run dev
cd web/ClinicalStudyPB && npm run test:e2e
```

Com PostgreSQL local, use o procedimento da seção "Testes E2E" do README.
