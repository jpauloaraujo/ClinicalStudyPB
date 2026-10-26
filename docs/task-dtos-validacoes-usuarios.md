# Task — DTOs e validações de usuários

## O que foi implementado

- `UsuarioCriacaoRequest`: contrato de entrada para cadastro, com validação de nome, e-mail, senha e perfil.
- `UsuarioAtualizacaoRequest`: contrato de entrada para atualização; a senha pode ser omitida, mas, se enviada, deve ter de 8 a 100 caracteres. O campo `ativo` é obrigatório.
- `UsuarioResponse`: contrato de saída que expõe somente ID, nome, e-mail, perfil e estado ativo — nunca a senha ou seu hash.
- `UsuarioController`: passa a receber DTOs validados com `@Valid` e devolver `UsuarioResponse`, sem expor diretamente a entidade JPA.
- `UsuarioService`: converte os dados de entrada em entidade, verifica e-mail duplicado e continua armazenando a senha com BCrypt.
- `UsuarioDtoValidationTest`: testes de validação dos casos válidos e inválidos de cadastro e atualização.

## Regras de validação

| Campo | Regra |
|---|---|
| `nome` | Obrigatório, não pode ser vazio e tem no máximo 120 caracteres |
| `email` | Obrigatório, formato de e-mail válido, no máximo 180 caracteres e único no sistema |
| `senha` no cadastro | Obrigatória, entre 8 e 100 caracteres |
| `senha` na atualização | Opcional; se enviada, entre 8 e 100 caracteres |
| `perfil` | Obrigatório; deve ser um dos valores de `PerfilUsuario` |
| `ativo` na atualização | Obrigatório |

A API já possui `ApiExceptionHandler`, que transforma erros do Bean Validation em resposta HTTP 400.

## Exemplos de requisição

### POST `/api/usuarios`

```json
{
  "nome": "Helena Costa",
  "email": "helena@example.com",
  "senha": "senha-segura-123",
  "perfil": "PESQUISADOR"
}
```

### PUT `/api/usuarios/{id}`

```json
{
  "nome": "Helena Costa",
  "email": "helena@example.com",
  "senha": null,
  "perfil": "PESQUISADOR",
  "ativo": true
}
```

Na atualização, `senha` pode ser `null` para manter a senha atual.

## Teste

No diretório `backend/ClinicalStudyPB`, executar:

```bash
./mvnw -Dtest=UsuarioDtoValidationTest test
```

Também executar `./mvnw test` para verificar a integração com o restante do projeto.
