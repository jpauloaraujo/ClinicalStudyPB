# T72 · Tag release1, GitHub Release e apresentação

> **Sprint 2** · PBI-18 · Documentação e entrega da Release 1  
> **Responsável:** I1 · **Estimativa:** 2h · **Prazo:** Seg 09/11 (antes da apresentação)  
> **Depende de:** T66, T71  
> **Situação:** 🟡 Roteiro pronto em `docs/sprint2/T72 — Roteiro de entrega e demonstração da Release 1.md`; falta criar a tag e a GitHub Release no repositório

## 1. Objetivo

Marcar a versão entregue com a tag `release1`, publicar a GitHub Release e ensaiar a demonstração.

## 2. Contexto: o que já existe e deve ser reaproveitado

- Depois da tag nada é alterado: a avaliação usa a versão dela.

## 3. Onde implementar

| Arquivo | Ação | Responsabilidade |
|---|---|---|
| `GitHub → Releases` | Criar | Release `release1` com resumo e link para `releases/release1/README.md` |
| `Repositório (tag)` | Criar | Tag anotada `release1` no commit entregue |

## 4. O que fazer em cada arquivo

### Verificações antes de marcar

- A `main` está verde no CI (os cinco jobs).
- Um clone limpo executa conforme o README.
- O README da release (T71) está na `main`.

### Comandos

- `git checkout main && git pull`
- `git tag -a release1 -m "Release 1 — Sprints 1 e 2" <commit>`
- `git push origin release1`
- No GitHub: Releases → Draft a new release → escolher a tag `release1` → colar o resumo e o link para `releases/release1/README.md` → Publish.

### Roteiro do ensaio com o time

- 1. O pesquisador define o estudo (critérios e visitas) e avança para recrutamento.
- 2. O coordenador cadastra, avalia, registra o consentimento e inclui um participante.
- 3. Uma tentativa de inclusão é bloqueada por falta de vagas.
- 4. Mostrar o relatório de testes e cobertura e o Swagger UI.
- Use os dados do perfil `demo` (T63).

## 7. Critérios de aceite

- [ ] A tag `release1` e a Release existem no GitHub.
- [ ] O ensaio foi feito com o time na ordem acima.

## 8. Cuidados e fora de escopo

- Não mover a tag depois de publicada.
- Se algo precisar mudar depois da tag, é uma decisão do time e deve ser comunicada.

---
Parte do conjunto de documentos de design da Sprint 2: [índice](README.md).
