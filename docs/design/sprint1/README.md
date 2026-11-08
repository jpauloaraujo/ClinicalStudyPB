# Documentos de design da Sprint 1

Cada task tem um documento que diz **o que implementar, em qual arquivo e em que ordem**. Para as tasks já implementadas, o documento descreve o que existe no código, para que o trabalho novo siga o mesmo padrão. Para as tasks ainda abertas, é o roteiro de quem for fazer.

- **35 tasks** na Sprint 1, **35 documentos**, um por task, em sequência (`T<número>-<assunto>.md`). Os documentos que já existiam em `docs/` aparecem como complemento ("já existia"), sem substituir o guia da task.
- Legenda: ✅ implementada · 🟡 parcial · ⬜ a implementar · 📋 entrega fora do código (Figma ou processo), especificada no próprio documento e ainda obrigatória.
- Caminhos e nomes de endpoint são os **reais do código** (`/api/estudos/...`), não os do plano (`/studies/...`).

| Task | Título | Resp. | Situação | Documento de design |
|---|---|---|---|---|
| T01 | Definir e documentar arquitetura Spring Boot + React + React Native + PostgreSQL | I1 | ✅ | [Design da T01](T01-definir-e-documentar-arquitetura-spring-boot-react-react.md)<br>[Documento de Arquitetura de Software (DAS)](../../sprint1/arquitetura.md) (já existia) |
| T02 | Configurar GitHub, estratégia de branches e regras de Pull Request | I1 | 📋 | [Design da T02](T02-configurar-github-estrategia-de-branches-e-regras-de-pull.md) |
| T03 | Elaborar diagrama inicial da arquitetura | I2 | ✅ | [Design da T03](T03-elaborar-diagrama-inicial-da-arquitetura.md)<br>[DAS, seção 2 (C4 em Mermaid)](../../sprint1/arquitetura.md) (já existia) |
| T04 | Criar README com estrutura e instruções iniciais | I7 | ✅ | [Design da T04](T04-criar-readme-com-estrutura-e-instrucoes-iniciais.md)<br>[README principal](../../../README.md) (já existia) |
| T05 | Configurar GitHub Actions para build e verificações básicas | I1 | ✅ | [Design da T05](T05-configurar-github-actions-para-build-e-verificacoes-basicas.md) |
| T06 | Inicializar projeto Spring Boot (controllers, services, repositories) | I3 | ✅ | [Design da T06](T06-inicializar-projeto-spring-boot-controllers-services.md)<br>[Implementação inicial do backend](../../sprint1/implementacao-backend-sprint1.md) (já existia) |
| T07 | Configurar PostgreSQL e conexão da aplicação | I1 | ✅ | [Design da T07](T07-configurar-postgresql-e-conexao-da-aplicacao.md)<br>[Implementação inicial do backend](../../sprint1/implementacao-backend-sprint1.md) (já existia) |
| T08 | Criar modelos JPA iniciais de usuários e estudos | I3 | ✅ | [Design da T08](T08-criar-modelos-jpa-iniciais-de-usuarios-e-estudos.md)<br>[Implementação inicial do backend, seção T08/T09](../../sprint1/implementacao-backend-sprint1.md) (já existia) |
| T09 | Criar migrations iniciais com Flyway | I2 | ✅ | [Design da T09](T09-criar-migrations-iniciais-com-flyway.md)<br>[Implementação inicial do backend, seção T08/T09](../../sprint1/implementacao-backend-sprint1.md) (já existia) |
| T10 | Configurar documentação da API com springdoc-openapi / Swagger UI | I2 | ✅ | [Design da T10](T10-configurar-documentacao-da-api-com-springdoc-openapi.md)<br>[Implementação inicial do backend, seção T10](../../sprint1/implementacao-backend-sprint1.md) (já existia) |
| T11 | Inicializar React + TypeScript com Vite e React Router | I5 | ✅ | [Design da T11](T11-inicializar-react-typescript-com-vite-e-react-router.md) |
| T12 | Inicializar React Native + Expo com TypeScript e navegação básica | I7 | ✅ | [Design da T12](T12-inicializar-react-native-expo-com-typescript-e-navegacao.md)<br>[Implementação do aplicativo mobile](../../sprint1/implementacao-mobile.md) (já existia) |
| T13 | Desenvolver layout base e navegação inicial da aplicação web | I6 | ✅ | [Design da T13](T13-desenvolver-layout-base-e-navegacao-inicial-da-aplicacao-web.md)<br>[Plano da T13](../../sprint1/task-layout-navegacao-web.md) (já existia)<br>[Implementação da T13](../../sprint1/implementacao-web-layout-navegacao.md) (já existia) |
| T14 | Configurar cliente HTTP Axios e variáveis de ambiente no React | I5 | ✅ | [Design da T14](T14-configurar-cliente-http-axios-e-variaveis-de-ambiente-no.md) |
| T15 | Criar DTOs e validações de usuários | I3 | ✅ | [Design da T15](T15-criar-dtos-e-validacoes-de-usuarios.md)<br>[DTOs e validações de usuários](../../sprint1/task-dtos-validacoes-usuarios.md) (já existia) |
| T16 | Implementar serviços e endpoints de cadastro e gerenciamento de usuários | I4 | ✅ | [Design da T16](T16-implementar-servicos-e-endpoints-de-cadastro-e.md) |
| T17 | Desenvolver formulário e listagem de usuários no React | I5 | ✅ | [Design da T17](T17-desenvolver-formulario-e-listagem-de-usuarios-no-react.md) |
| T18 | Implementar testes unitários e de integração do módulo de usuários | I2 | ✅ | [Design da T18](T18-implementar-testes-unitarios-e-de-integracao-do-modulo-de.md) |
| T19 | Configurar Spring Security, senhas protegidas e autenticação por token | I4 | ✅ | [Design da T19](T19-configurar-spring-security-senhas-protegidas-e-autenticacao.md) |
| T20 | Implementar endpoint de login e validação de credenciais | I4 | ✅ | [Design da T20](T20-implementar-endpoint-de-login-e-validacao-de-credenciais.md) |
| T21 | Desenvolver login web, gerenciamento de autenticação e logout | I5 | ✅ | [Design da T21](T21-desenvolver-login-web-gerenciamento-de-autenticacao-e-logout.md) |
| T22 | Criar testes de login válido, inválido e acesso não autenticado | I7 | ✅ | [Design da T22](T22-criar-testes-de-login-valido-invalido-e-acesso-nao.md) |
| T23 | Definir matriz de permissões dos quatro perfis | I1 | ✅ | [Design da T23](T23-definir-matriz-de-permissoes-dos-quatro-perfis.md)<br>[DAS, seção 4 (matriz RBAC)](../../sprint1/arquitetura.md) (já existia) |
| T24 | Implementar autorização por perfil no Spring Security | I4 | ✅ | [Design da T24](T24-implementar-autorizacao-por-perfil-no-spring-security.md) |
| T25 | Proteger rotas e funcionalidades da aplicação React | I6 | ✅ | [Design da T25](T25-proteger-rotas-e-funcionalidades-da-aplicacao-react.md) |
| T26 | Criar testes de permissões e acessos negados | I2 | ✅ | [Design da T26](T26-criar-testes-de-permissoes-e-acessos-negados.md) |
| T27 | Implementar DTOs, validações e serviço de estudos | I3 | ✅ | [Design da T27](T27-implementar-dtos-validacoes-e-servico-de-estudos.md) |
| T28 | Implementar endpoints REST para cadastro e consulta de estudos | I3 | ✅ | [Design da T28](T28-implementar-endpoints-rest-para-cadastro-e-consulta-de.md) |
| T29 | Desenvolver formulário e listagem de estudos no React | I6 | ✅ | [Design da T29](T29-desenvolver-formulario-e-listagem-de-estudos-no-react.md) |
| T30 | Implementar testes unitários e de integração de estudos | I2 | ✅ | [Design da T30](T30-implementar-testes-unitarios-e-de-integracao-de-estudos.md) |
| T31 | Integrar telas com a API e exibir erros de validação | I5 | ✅ | [Design da T31](T31-integrar-telas-com-a-api-e-exibir-erros-de-validacao.md) |
| T32 | Criar protótipos navegáveis das principais telas web no Figma | I6 | 📋 | [Design da T32](T32-criar-prototipos-navegaveis-das-principais-telas-web-no.md) |
| T33 | Criar protótipos navegáveis das telas do participante no Figma | I7 | 📋 | [Design da T33](T33-criar-prototipos-navegaveis-das-telas-do-participante-no.md)<br>[Requisitos dos protótipos mobile](../../sprint1/prototipos-mobile.md) (já existia) |
| T34 | Preparar fluxo de navegação mobile para demonstração | I7 | 🟡 | [Design da T34](T34-preparar-fluxo-de-navegacao-mobile-para-demonstracao.md)<br>[Requisitos dos protótipos mobile, seção 6](../../sprint1/prototipos-mobile.md) (já existia)<br>[Implementação do aplicativo mobile](../../sprint1/implementacao-mobile.md) (já existia) |
| T35 | Revisar fluxos e registrar ajustes e documentação | I1 | 📋 | [Design da T35](T35-revisar-fluxos-e-registrar-ajustes-e-documentacao.md) |

## Como usar

1. Abra o documento da sua task e leia as seções 2 (o que reaproveitar) e 3 (onde implementar).
2. Siga a seção 4, arquivo por arquivo, e confira os contratos da seção 5.
3. Escreva os testes da seção 6 e marque os critérios da seção 7 no Pull Request.
4. Se o contrato mudar, corrija `docs/api/openapi-sprint2.yaml` no mesmo PR (o `ContratoOpenApiTest` falha se divergir).
