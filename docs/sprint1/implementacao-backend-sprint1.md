# Implementação inicial do backend — ClinicalStudyPB

Este material documenta a estrutura inicial do backend Spring Boot (`PBI-02`), contemplando as camadas da aplicação (`T06`), configuração de conexão e propriedades (`T07`), modelos JPA iniciais (`T08`), migrations versionadas com **Flyway** (`T09`) e documentação interativa com **springdoc-openapi / Swagger UI** (`T10`).

## Estrutura criada

- `config`: [`OpenApiConfig`](../backend/ClinicalStudyPB/src/main/java/UEPB/ESWII/ClinicalStudyPB/config/OpenApiConfig.java) com metadados da especificação OpenAPI 3, esquema de autenticação Bearer JWT e liberação pública das rotas do Swagger UI.
- `domain/model`: entidades JPA `Usuario` e `Estudo` documentadas com `@Schema`.
- `domain/enums`: `PerfilUsuario` e `StatusEstudo`.
- `repository`: interfaces Spring Data JPA (`UsuarioRepository` e `EstudoRepository` com `@EntityGraph` para evitar consultas N+1).
- `service`: operações básicas de cadastro, consulta, atualização e exclusão, com validações de negócio iniciais.
- `controller`: endpoints REST para usuários e estudos anotados com `@Tag`, `@Operation`, `@ApiResponses` e `@Parameter`, além de tratamento de erros (`ApiExceptionHandler`).
- `controller/dto`: objeto de entrada `EstudoRequest`, que recebe `responsavelId` em vez de uma entidade inteira.
- `resources/db/migration`:
  - `V1__create_initial_tables.sql`: criação das tabelas `usuarios` e `estudos` com chaves primárias, estrangeiras, constraints de validação (`CHECK`, `UNIQUE`) e índices.
  - `V2__seed_initial_data.sql`: carga determinística inicial com 4 usuários fictícios (um para cada perfil, com senha `"senha-de-teste"` criptografada em BCrypt) e 2 estudos clínicos demonstrativos.

## Endpoints iniciais

### Autenticação — `/api/auth/login` (T19 / T20 — Tasks 3060 / 3063)

- `POST /api/auth/login`: autentica usuário por e-mail e senha e retorna o Bearer Token JWT. Rota pública.
- Todas as demais rotas de `/api/**` agora exigem autenticação via cabeçalho `Authorization: Bearer <token>`.

Exemplo de requisição:

```json
{
  "email": "admin@clinicalstudy.test",
  "senha": "senha-de-teste"
}
```

Exemplo de resposta de sucesso (HTTP 200):

```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "tipo": "Bearer"
}
```

Casos de erro:
- Corpo inválido ou dados ausentes: HTTP 400 Bad Request (`{"erro": "..."}`).
- Senha incorreta, e-mail inexistente ou usuário inativo (`ativo = false`): HTTP 401 Unauthorized com mensagem genérica (`{"erro": "Credenciais inválidas"}`).

### Usuários — `/api/usuarios`

- `GET /api/usuarios`: lista usuários (requer token JWT).
- `GET /api/usuarios/{id}`: consulta usuário por UUID (requer token JWT).
- `POST /api/usuarios`: cadastra usuário (requer token JWT).
- `PUT /api/usuarios/{id}`: atualiza usuário (requer token JWT).
- `DELETE /api/usuarios/{id}`: exclui usuário (requer token JWT).

Exemplo de corpo para `POST /api/usuarios`:

```json
{
  "nome": "Usuário de Teste",
  "email": "teste@clinicalstudy.test",
  "senha": "senha-temporaria",
  "perfil": "PESQUISADOR"
}
```

O cadastro e a atualização utilizam o bean centralizado `PasswordEncoder` (BCrypt custo 10) injetado no `UsuarioService`.

### Estudos — `/api/estudos`

- `GET /api/estudos`: lista estudos (requer token JWT).
- `GET /api/estudos/{id}`: consulta estudo por UUID (requer token JWT).
- `POST /api/estudos`: cadastra estudo (requer token JWT).
- `PUT /api/estudos/{id}`: atualiza estudo (requer token JWT).
- `DELETE /api/estudos/{id}`: exclui estudo (requer token JWT).

Exemplo de corpo para `POST /api/estudos`:

```json
{
  "titulo": "Estudo demonstrativo",
  "objetivo": "Avaliar um protocolo fictício.",
  "responsavelId": "22222222-2222-4222-8222-222222222222",
  "dataInicio": "2026-11-01",
  "dataFim": "2027-02-01",
  "limiteParticipantes": 50,
  "status": "PLANEJAMENTO"
}
```

O UUID `22222222-2222-4222-8222-222222222222` já está disponível no banco após a execução automática da migration `V2__seed_initial_data.sql` (`pesquisador@clinicalstudy.test`).

## Segurança e Autenticação por Token (Task 3060 / Task 3063)

A segurança adota o modelo **stateless** do Spring Security com **Resource Server JWT** e assinatura simétrica **HS256**:

- **Contrato do Token JWT:**
  - `sub`: UUID do usuário autenticado.
  - `role`: perfil do usuário (`ADMINISTRADOR`, `PESQUISADOR`, `COORDENADOR`, `PARTICIPANTE`), mapeado para autoridade `ROLE_<PERFIL>`.
  - `iss`: emissor configurado (`jwt.issuer`, padrão `clinicalstudypb`).
  - `iat`: instante de emissão do token.
  - `exp`: instante de expiração (validade temporal configurável com padrão de 30 minutos).
- **Variáveis de ambiente do backend:**
  - `JWT_SECRET`: segredo obrigatório de pelo menos 256 bits (32 caracteres) para assinatura HS256. Não há valor padrão inseguro versionado no código de produção. Para desenvolvimento local, pode ser configurado em `application-dev.properties` (ignorado pelo Git).
  - `JWT_ISSUER`: identificador do emissor do token (padrão: `clinicalstudypb`).
  - `JWT_EXPIRATION_MINUTES`: tempo de validade do token em minutos (padrão: `30`).
- **Política de Expiração e Logout:**
  - O token gerado tem validade de **30 minutos**.
  - Por se tratar de autenticação stateless via Bearer Token, o logout é realizado pelo cliente (SPA Web ou App Mobile) ao descartar o token armazenado localmente.
  - Um token JWT emitido permanece criptograficamente válido até atingir sua data/hora de expiração (`exp`). A inativação de uma conta bloqueia novos logins imediatamente no endpoint `/api/auth/login`. Não há controle por blacklist ou refresh token nesta versão mínima.
- **CORS:** configurado para a origem local documentada da aplicação web (`http://localhost:5173`).

## Documentação OpenAPI / Swagger UI (T10)

Com a aplicação em execução na porta `8081`:

- **Swagger UI:** `http://localhost:8081/swagger-ui.html` (redireciona para `/swagger-ui/index.html`)
- **OpenAPI JSON:** `http://localhost:8081/v3/api-docs`

O Swagger UI é público e já conta com o botão **Authorize** configurado para o esquema `bearerAuth` (`HTTP Bearer JWT`). Cole o token obtido no login no campo de autorização do Swagger para testar endpoints restritos.

## Decisões de modelagem e banco de dados (T08 / T09)

- Os IDs são UUIDs.
- O e-mail do usuário é obrigatório e único (`uk_usuarios_email`).
- Os perfis seguem a especificação: `ADMINISTRADOR`, `PESQUISADOR`, `COORDENADOR` e `PARTICIPANTE` (validados também no banco via `chk_usuarios_perfil`).
- O estudo tem relação `ManyToOne` com o usuário responsável (`fk_estudos_responsavel`).
- O status inicial do estudo é `PLANEJAMENTO`; os estados incluem `RECRUTAMENTO`, `EXECUCAO` e `ENCERRADO` (`chk_estudos_status`).
- A validação no serviço e no banco (`chk_estudos_datas` e `chk_estudos_limite_participantes`) impede data final anterior à data inicial e limite de participantes menor que 1.
- O Hibernate opera com `spring.jpa.hibernate.ddl-auto=validate`, garantindo que as entidades JPA estejam sempre sincronizadas com as migrations do Flyway.

## Como testar localmente

1. Confira `src/main/resources/application.properties` ou crie `src/main/resources/application-dev.properties` a partir de `application-dev.properties.example` para ajustar as credenciais locais do PostgreSQL e definir `jwt.secret`.
2. No terminal, entre em `backend/ClinicalStudyPB`.
3. Para rodar todos os testes automatizados (com banco H2 em memória e chave de teste isolada):
   - Linux/macOS: `./mvnw test`
   - Windows: `mvnw.cmd test`
4. Para rodar especificamente a suíte de testes de autenticação e segurança:
   - Linux/macOS: `./mvnw test -Dtest=AuthenticationIntegrationTest`
   - Windows: `mvnw.cmd test -Dtest=AuthenticationIntegrationTest`
5. Para executar a aplicação conectada ao PostgreSQL:
   - Defina a variável de ambiente: `$env:JWT_SECRET="seu-segredo-com-pelo-menos-32-caracteres"` (PowerShell) ou adicione em `application-dev.properties`.
   - Execute `./mvnw spring-boot:run` ou `mvnw.cmd spring-boot:run`.
6. Acesse `http://localhost:8081/swagger-ui.html` no navegador ou teste com `curl`/Postman.

