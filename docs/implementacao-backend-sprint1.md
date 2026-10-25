# Implementação inicial do backend — ClinicalStudyPB

Este material complementa o backend existente para as tasks de inicialização das camadas Spring Boot e criação dos modelos JPA iniciais de usuários e estudos.

## Estrutura criada

- `domain/model`: entidades JPA `Usuario` e `Estudo`.
- `domain/enums`: `PerfilUsuario` e `StatusEstudo`.
- `repository`: interfaces Spring Data JPA.
- `service`: operações básicas de cadastro, consulta, atualização e exclusão, com validações de negócio iniciais.
- `controller`: endpoints REST para usuários e estudos e tratamento simples de erros.
- `controller/dto`: objeto de entrada para estudos, que recebe `responsavelId` em vez de uma entidade inteira.

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

**Importante:** o serviço já armazena a senha usando BCrypt e não devolve a senha no JSON. A implementação de login/autorização continua fora do escopo desta alteração. Quando a task de segurança for feita, alinhem a configuração do `PasswordEncoder` para reutilizar a mesma configuração em toda a aplicação.

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
  "responsavelId": "UUID-DE-UM-USUARIO-EXISTENTE",
  "dataInicio": "2026-11-01",
  "dataFim": "2027-02-01",
  "limiteParticipantes": 50,
  "status": "PLANEJAMENTO"
}
```

Troque `UUID-DE-UM-USUARIO-EXISTENTE` por um UUID retornado pelo cadastro de usuários.

## Decisões de modelagem

- Os IDs são UUIDs.
- O e-mail do usuário é obrigatório e único.
- Os perfis seguem a especificação: `ADMINISTRADOR`, `PESQUISADOR`, `COORDENADOR` e `PARTICIPANTE`.
- O estudo tem relação `ManyToOne` com o usuário responsável: um usuário pode ser responsável por vários estudos; cada estudo possui um responsável.
- O status inicial do estudo é `PLANEJAMENTO`; os estados também incluem `RECRUTAMENTO`, `EXECUCAO` e `ENCERRADO`.
- A validação inicial impede data final anterior à data inicial e limite de participantes menor que 1.

## Como testar localmente

1. Confira `src/main/resources/application.properties` e configure a conexão PostgreSQL no perfil ativo `dev` (a configuração de datasource não faz parte desta alteração).
2. No terminal, entre em `backend/ClinicalStudyPB`.
3. Execute `./mvnw test` em Linux/macOS ou `mvnw.cmd test` no Windows.
4. Para executar a aplicação, use `./mvnw spring-boot:run` ou `mvnw.cmd spring-boot:run`.
5. Com o servidor ativo, teste os endpoints com Postman, Insomnia ou `curl`.

## Fora do escopo destas tasks

- Configuração do PostgreSQL e credenciais locais (task de conexão ao banco).
- Migrations Flyway.
- Login, JWT, Spring Security e hash de senhas (tasks de autenticação/segurança).
- Regras completas de autorização por perfil.
- Testes automatizados específicos para os novos endpoints.

O `spring.jpa.hibernate.ddl-auto=update` já existente permanece inalterado; migrations e política de schema devem ser alinhadas com a equipe.
