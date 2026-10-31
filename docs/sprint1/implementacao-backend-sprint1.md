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

### Usuários — `/api/usuarios`

- `GET /api/usuarios`: lista usuários.
- `GET /api/usuarios/{id}`: consulta usuário por UUID.
- `POST /api/usuarios`: cadastra usuário.
- `PUT /api/usuarios/{id}`: atualiza usuário.
- `DELETE /api/usuarios/{id}`: exclui usuário.

Exemplo de corpo para `POST /api/usuarios`:

```json
{
  "nome": "Usuário de Teste",
  "email": "teste@clinicalstudy.test",
  "senha": "senha-temporaria",
  "perfil": "PESQUISADOR"
}
```

**Importante:** o serviço já armazena a senha usando BCrypt e não devolve a senha no JSON. Quando a task de segurança (`T19`) for feita, alinhem a configuração do `PasswordEncoder` e reutilizem `OpenApiConfig.SWAGGER_WHITELIST` na `SecurityFilterChain`.

### Estudos — `/api/estudos`

- `GET /api/estudos`: lista estudos.
- `GET /api/estudos/{id}`: consulta estudo por UUID.
- `POST /api/estudos`: cadastra estudo.
- `PUT /api/estudos/{id}`: atualiza estudo.
- `DELETE /api/estudos/{id}`: exclui estudo.

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

## Documentação OpenAPI / Swagger UI (T10)

Com a aplicação em execução na porta `8081`:

- **Swagger UI:** `http://localhost:8081/swagger-ui.html` (redireciona para `/swagger-ui/index.html`)
- **OpenAPI JSON:** `http://localhost:8081/v3/api-docs`

O Swagger UI já conta com o botão **Authorize** configurado para o esquema `bearerAuth` (`HTTP Bearer JWT`) e com a propriedade `springdoc.swagger-ui.persist-authorization=true` ativada.

## Decisões de modelagem e banco de dados (T08 / T09)

- Os IDs são UUIDs.
- O e-mail do usuário é obrigatório e único (`uk_usuarios_email`).
- Os perfis seguem a especificação: `ADMINISTRADOR`, `PESQUISADOR`, `COORDENADOR` e `PARTICIPANTE` (validados também no banco via `chk_usuarios_perfil`).
- O estudo tem relação `ManyToOne` com o usuário responsável (`fk_estudos_responsavel`).
- O status inicial do estudo é `PLANEJAMENTO`; os estados incluem `RECRUTAMENTO`, `EXECUCAO` e `ENCERRADO` (`chk_estudos_status`).
- A validação no serviço e no banco (`chk_estudos_datas` e `chk_estudos_limite_participantes`) impede data final anterior à data inicial e limite de participantes menor que 1.
- O Hibernate opera com `spring.jpa.hibernate.ddl-auto=validate`, garantindo que as entidades JPA estejam sempre sincronizadas com as migrations do Flyway.

## Como testar localmente

1. Confira `src/main/resources/application.properties` ou crie `src/main/resources/application-dev.properties` a partir de `application-dev.properties.example` para ajustar as credenciais locais do PostgreSQL.
2. No terminal, entre em `backend/ClinicalStudyPB`.
3. Execute `./mvnw test` em Linux/macOS ou `mvnw.cmd test` no Windows (os testes executam automaticamente as migrations do Flyway e validam o OpenAPI/Swagger UI usando H2 em modo PostgreSQL).
4. Para executar a aplicação conectada ao PostgreSQL, use `./mvnw spring-boot:run` ou `mvnw.cmd spring-boot:run`.
5. Acesse `http://localhost:8081/swagger-ui.html` no navegador ou teste os endpoints com Postman, Insomnia ou `curl`.
