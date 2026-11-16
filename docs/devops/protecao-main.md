# Proteção da branch `main`

**Release:** R2 · **Sprint:** 3 · **Aplicada em:** 13/11/2026

## Por que

Até a Release 1, a `main` não tinha proteção: qualquer integrante podia dar push direto (inclusive force push), e um PR podia entrar com o CI vermelho. Foi o que aconteceu com o PR #11, que deixou o job `Backend (testes no PostgreSQL)` falhando na `main`. A proteção abaixo faz o GitHub impedir esses desvios, em vez de depender só da combinação entre a equipe.

O repositório passou a ser **público** para que a proteção de branch funcione no plano gratuito do GitHub (em repositório privado de conta pessoal, ela exige o GitHub Pro). Público significa que qualquer pessoa pode ver e clonar o código; **só os colaboradores com permissão de escrita** conseguem enviar código, e mesmo eles só entram na `main` por Pull Request. Os dados do sistema são todos fictícios.

## Regras configuradas

| Regra | Valor | Efeito |
| --- | --- | --- |
| Exigir Pull Request antes do merge | Sim | Bloqueia push direto na `main` |
| Aprovações obrigatórias | 1 | Outro integrante precisa aprovar (revisão cruzada) |
| Descartar aprovações quando houver novos commits | Sim | A aprovação vale para o código final |
| Exigir checagens de status | Sim, com a branch atualizada | O CI precisa passar antes do merge |
| Checagens obrigatórias | `Backend (Java 21 + Maven)`, `Backend (testes no PostgreSQL)`, `Web (React + Vite)`, `Mobile (Expo)`, `E2E (Playwright)`, `Ambiente Docker (T67)` | Todos os jobs do `ci.yml` |
| Exigir resolução das conversas | Sim | Comentários da revisão não ficam pendentes |
| Bloquear force push e exclusão | Sim | Preserva o histórico |
| Aplicar também a administradores | Sim | Ninguém contorna a regra, nem o dono do repositório |

## Fluxo de trabalho com a proteção

1. Cada task tem a sua branch, criada a partir da `develop` (ex.: `feature/task87/domain/resultados`).
2. O PR da task vai para a `develop`; o CI roda e outro integrante revisa.
3. A `develop` entra na `main` também por PR, com aprovação e os seis jobs verdes.
4. Se a `main` avançar enquanto o PR está aberto, atualize a branch (**Update branch**) antes do merge.

Se um job novo for criado no `ci.yml` ou um nome de job mudar, atualize a lista de checagens obrigatórias em **Settings → Branches → `main`**: um check obrigatório que não roda mais deixa todos os PRs travados esperando por ele.

## Quem pode alterar

Só quem tem permissão de **Admin** no repositório (o dono da conta `jpauloaraujo`).

## Alternativa por linha de comando (Admin)

```bash
gh api -X PUT repos/jpauloaraujo/ClinicalStudyPB/branches/main/protection \
  -H "Accept: application/vnd.github+json" \
  --input - <<'JSON'
{
  "required_status_checks": {
    "strict": true,
    "contexts": [
      "Backend (Java 21 + Maven)",
      "Backend (testes no PostgreSQL)",
      "Web (React + Vite)",
      "Mobile (Expo)",
      "E2E (Playwright)",
      "Ambiente Docker (T67)"
    ]
  },
  "enforce_admins": true,
  "required_pull_request_reviews": {
    "required_approving_review_count": 1,
    "dismiss_stale_reviews": true
  },
  "required_conversation_resolution": true,
  "restrictions": null,
  "allow_force_pushes": false,
  "allow_deletions": false
}
JSON
```

Conferência: `gh api repos/jpauloaraujo/ClinicalStudyPB/branches/main/protection`.

## Evidências

- [x] Regra aplicada na `main` (conferida pela API em 13/11/2026: 6 checagens, 1 aprovação, aprovações descartadas com novos commits, conversas resolvidas, sem force push nem exclusão, valendo para administradores).
- [ ] Tentativa de push direto na `main` recusada (print).
- [ ] Primeiro PR da `develop` para a `main` com aprovação de outro integrante e as seis checagens verdes.
