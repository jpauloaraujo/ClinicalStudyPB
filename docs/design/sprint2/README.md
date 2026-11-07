# Documentos de design da Sprint 2

Cada task tem um documento que diz **o que implementar, em qual arquivo e em que ordem**. Para as tasks já implementadas, o documento descreve o que existe no código, para que o trabalho novo siga o mesmo padrão. Para as tasks ainda abertas, é o roteiro de quem for fazer.

- **37 tasks** na Sprint 2, **37 documentos**, um por task, em sequência (`T<número>-<assunto>.md`). Os documentos que já existiam em `docs/` aparecem como complemento ("já existia"), sem substituir o guia da task.
- Legenda: ✅ implementada · 🟡 parcial · ⬜ a implementar · 📋 entrega fora do código (Figma ou processo), especificada no próprio documento e ainda obrigatória.
- Caminhos e nomes de endpoint são os **reais do código** (`/api/estudos/...`), não os do plano (`/studies/...`).

| Task | Título | Resp. | Situação | Documento de design |
|---|---|---|---|---|
| T36 | Modelo de domínio e regras de negócio | I1 | ✅ | [Design da T36](T36-modelo-de-dominio-e-regras-de-negocio.md)<br>[Implementação do modelo de domínio de recrutamento](../../sprint2/Implementa%C3%A7%C3%A3o%20do%20modelo%20de%20dom%C3%ADnio%20de%20recrutamento.md) (já existia)<br>[Diagrama de classes, estados e regras](../../diagramas/dominio.md) (já existia) |
| T37 | Contrato OpenAPI dos endpoints da Sprint | I1 | 🟡 | [Design da T37](T37-contrato-openapi-dos-endpoints-da-sprint.md)<br>[Contrato OpenAPI (YAML)](../../api/openapi-sprint2.yaml) (já existia)<br>[Mapeamento, seção 3.1](../../sprint2/Tasks%20at%C3%A9%2001-11%20%E2%80%94%20mapeamento%20e%20complemento.md) (já existia) |
| T38 | Migrations Flyway e entidades JPA | I3 | ✅ | [Design da T38](T38-migrations-flyway-e-entidades-jpa.md)<br>[T38 · T41 · T51](../../sprint2/T38-T41-T51-implementacao.md) (já existia)<br>[Modelo de domínio de recrutamento, seção 2.2](../../sprint2/Implementa%C3%A7%C3%A3o%20do%20modelo%20de%20dom%C3%ADnio%20de%20recrutamento.md) (já existia) |
| T39 | Página de detalhe do estudo com abas | I6 | ✅ | [Design da T39](T39-pagina-de-detalhe-do-estudo-com-abas.md) |
| T40 | Tipos TypeScript e mocks a partir do contrato | I5 | ✅ | [Design da T40](T40-tipos-typescript-e-mocks-a-partir-do-contrato.md) |
| T41 | Máquina de estados do estudo (domínio) | I3 | ✅ | [Design da T41](T41-maquina-de-estados-do-estudo-dominio.md)<br>[T38 · T41 · T51](../../sprint2/T38-T41-T51-implementacao.md) (já existia)<br>[Matriz de estados](../../diagramas/dominio.md) (já existia) |
| T42 | Endpoint de transição de estado | I4 | ✅ | [Design da T42](T42-endpoint-de-transicao-de-estado.md) |
| T43 | Tela de estado e transições | I6 | ✅ | [Design da T43](T43-tela-de-estado-e-transicoes.md) |
| T44 | Testes do RF07 | I2 | ✅ | [Design da T44](T44-testes-do-rf07.md)<br>[Resultado dos testes da T44 e da T47](../../sprint2/T44%20e%20T47%20%E2%80%94%20resultado%20dos%20testes.md) (já existia)<br>[Sequência: avançar o estado](../../diagramas/sequencia-avancar-estado.md) (já existia) |
| T45 | Backend de critérios | I4 | ✅ | [Design da T45](T45-backend-de-criterios.md) |
| T46 | Tela de critérios | I6 | ✅ | [Design da T46](T46-tela-de-criterios.md) |
| T47 | Testes do RF05 | I7 | ✅ | [Design da T47](T47-testes-do-rf05.md)<br>[Resultado dos testes da T44 e da T47](../../sprint2/T44%20e%20T47%20%E2%80%94%20resultado%20dos%20testes.md) (já existia) |
| T48 | Backend de visitas previstas e atividades | I4 | ✅ | [Design da T48](T48-backend-de-visitas-previstas-e-atividades.md) |
| T49 | Tela de visitas e atividades | I5 | ✅ | [Design da T49](T49-tela-de-visitas-e-atividades.md) |
| T50 | Testes do RF06 (visitas previstas e atividades) | I7 | ✅ | [Design da T50](T50-testes-do-rf06-visitas-previstas-e-atividades.md) |
| T51 | Backend de candidatos | I3 | ✅ | [Design da T51](T51-backend-de-candidatos.md)<br>[T38 · T41 · T51, seção T51](../../sprint2/T38-T41-T51-implementacao.md) (já existia) |
| T52 | Tela de candidatos e ficha do candidato | I5 | ✅ | [Design da T52](T52-tela-de-candidatos-e-ficha-do-candidato.md) |
| T53 | Testes do RF08 (candidatos) | I4 | 🟡 | [Design da T53](T53-testes-do-rf08-candidatos.md) |
| T54 | Backend de elegibilidade | I3 | ✅ | [Design da T54](T54-backend-de-elegibilidade.md) |
| T55 | Tela de avaliação de elegibilidade | I7 | ✅ | [Design da T55](T55-tela-de-avaliacao-de-elegibilidade.md) |
| T56 | Testes do RF09 (elegibilidade) | I2 | 🟡 | [Design da T56](T56-testes-do-rf09-elegibilidade.md) |
| T57 | Backend de consentimento | I4 | ✅ | [Design da T57](T57-backend-de-consentimento.md) |
| T58 | Tela de consentimento | I5 | ✅ | [Design da T58](T58-tela-de-consentimento.md) |
| T59 | Testes do RF10 (consentimento) | I7 | ✅ | [Design da T59](T59-testes-do-rf10-consentimento.md) |
| T60 | Backend de inclusão de participantes | I1 | ✅ | [Design da T60](T60-backend-de-inclusao-de-participantes.md) |
| T61 | Tela de inclusão e lista de participantes | I6 | ✅ | [Design da T61](T61-tela-de-inclusao-e-lista-de-participantes.md) |
| T62 | Testes do RF11 (limite de vagas e concorrência) | I2 | 🟡 | [Design da T62](T62-testes-do-rf11-limite-de-vagas-e-concorrencia.md) |
| T63 | Dados fictícios de demonstração | I4 | ⬜ | [Design da T63](T63-dados-ficticios-de-demonstracao.md) |
| T64 | Cobertura mínima de 80% no CI (JaCoCo) | I1 | ✅ | [Design da T64](T64-cobertura-minima-de-80-no-ci-jacoco.md) |
| T65 | Infraestrutura de testes E2E (Playwright) | I7 | ✅ | [Design da T65](T65-infraestrutura-de-testes-e2e-playwright.md)<br>[Requisitos e critérios de aceite](../../sprint2/T65%20%E2%80%94%20Requisitos%20e%20crit%C3%A9rios%20de%20aceite%20%28E2E%20com%20Playwright%29.md) (já existia)<br>[Implementação da infraestrutura E2E](../../sprint2/T65%20%E2%80%94%20Implementa%C3%A7%C3%A3o%20da%20infraestrutura%20E2E.md) (já existia) |
| T66 | E2E do fluxo completo da Release 1 | I7 | ✅ | [Design da T66](T66-e2e-do-fluxo-completo-da-release-1.md)<br>[Requisitos e critérios de aceite (checklist por dia)](../../sprint2/T66%20%E2%80%94%20Requisitos%20e%20crit%C3%A9rios%20de%20aceite%20%28E2E%20do%20fluxo%20completo%20da%20Release%201%29.md) (já existia) |
| T67 | Integração e correções: planejamento do estudo | I6 | ⬜ | [Design da T67](T67-integracao-e-correcoes-planejamento-do-estudo.md) |
| T68 | Integração e correções: recrutamento | I5 | ⬜ | [Design da T68](T68-integracao-e-correcoes-recrutamento.md) |
| T69 | Diagramas: casos de uso, classes, sequência e arquitetura | I2 | ✅ | [Design da T69](T69-diagramas-casos-de-uso-classes-sequencia-e-arquitetura.md)<br>[Casos de uso](../../diagramas/casos-de-uso.md) (já existia)<br>[Classes (domínio)](../../diagramas/dominio.md) (já existia)<br>[Sequência: avançar o estado](../../diagramas/sequencia-avancar-estado.md) (já existia)<br>[Sequência: incluir participante](../../diagramas/sequencia-inclusao-participante.md) (já existia)<br>[Arquitetura](../../diagramas/arquitetura.md) (já existia) |
| T70 | README principal | I2 | 🟡 | [Design da T70](T70-readme-principal.md) |
| T71 | README da release e processo de desenvolvimento | I2 | ⬜ | [Design da T71](T71-readme-da-release-e-processo-de-desenvolvimento.md) |
| T72 | Tag release1, GitHub Release e apresentação | I1 | ⬜ | [Design da T72](T72-tag-release1-github-release-e-apresentacao.md) |

## Como usar

1. Abra o documento da sua task e leia as seções 2 (o que reaproveitar) e 3 (onde implementar).
2. Siga a seção 4, arquivo por arquivo, e confira os contratos da seção 5.
3. Escreva os testes da seção 6 e marque os critérios da seção 7 no Pull Request.
4. Se o contrato mudar, corrija `docs/api/openapi-sprint2.yaml` no mesmo PR (o `ContratoOpenApiTest` falha se divergir).
