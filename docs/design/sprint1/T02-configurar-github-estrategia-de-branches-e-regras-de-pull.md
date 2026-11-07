# T02 · Configurar GitHub, estratégia de branches e regras de Pull Request

> **Sprint 1** · PBI-01 · Configuração do repositório e arquitetura  
> **Responsável:** I1 · **Estimativa:** 5h · **Prazo:** Sprint 1 (13/10 a 26/10/2026)  
> **Depende de:** nenhuma  
> **Situação:** 📋 Entrega fora do código (Figma ou processo): este documento especifica tudo o que precisa ser produzido, e a entrega continua sendo obrigatória

## 1. Objetivo

Definir como a equipe de sete pessoas trabalha no mesmo repositório sem sobrescrever o trabalho umas das outras: quais branches existem, como nomear, o que um Pull Request precisa para entrar e quem aprova. É configuração no GitHub e um texto de regras; não há código de produção.

## 2. Contexto: o que já existe e deve ser reaproveitado

- O repositório é um monorepo com `backend/`, `web/` e `mobile/` (ver `README.md`, seção 9).
- Branches existentes: `main` (entrega) e `develop` (integração); o CI roda nas duas e em todo PR para elas (`.github/workflows/ci.yml`).
- A Definition of Done do plano exige PR aprovado por outro integrante (`docs/sprint1/sprint1-plano.md`, seção 8).

## 3. Onde implementar

| Arquivo | Ação | Responsabilidade |
|---|---|---|
| `GitHub → Settings → Branches` | Configurar | Regra de proteção para `main` e `develop` |
| `GitHub → Settings → Collaborators` | Configurar | Os sete integrantes com permissão de escrita |
| `.github/pull_request_template.md` | Criar (opcional) | Checklist que aparece em todo PR |
| `README.md` | Alterar | Seção curta "Como contribuir" com as regras abaixo |

## 4. O que fazer em cada arquivo

### Estratégia de branches

- `main`: só recebe merge vindo de `develop`, no fim da Sprint, e é o que recebe a tag de release.
- `develop`: integração diária; todo trabalho entra por PR.
- Branches de trabalho saem de `develop` com o padrão `feat/tNN-assunto`, `fix/assunto` ou `docs/assunto` (ex.: `feat/t45-criterios`).
- Uma branch por task; commits pequenos, mensagem começando pelo número da task (ex.: `T45: valida descrição repetida`).

### Regras de Pull Request (proteção de branch)

- Exigir PR para entrar em `main` e `develop` (sem push direto).
- Exigir 1 aprovação de outro integrante (quem escreveu não aprova o próprio PR).
- Exigir que os jobs do CI estejam verdes (`Backend`, `Backend (testes no PostgreSQL)`, `Web`, `E2E`, `Mobile`).
- Apagar a branch depois do merge.

### Template de PR (opcional)

- Campos: task relacionada, o que mudou, como testar, capturas de tela (se houver tela), checklist (testes passando, contrato OpenAPI atualizado, docs atualizadas).

## 6. Testes

- Abrir um PR de teste e conferir que o merge fica bloqueado sem aprovação e sem CI verde.

## 7. Critérios de aceite

- [ ] Branches `main` e `develop` protegidas.
- [ ] Regras de PR escritas no README.
- [ ] Todos os integrantes conseguem abrir PR para a `develop`.

## 8. Cuidados e fora de escopo

- O push direto na `main` contorna a revisão; com a proteção ligada ele deixa de ser possível.
- A proteção de branch só pode ser configurada por quem administra o repositório no GitHub.

---
Parte do conjunto de documentos de design da Sprint 1: [índice](README.md).
