# T72 — Roteiro de entrega e demonstração da Release 1

> Plano (Sprint 2): confirmar a `main` verde no CI e o clone limpo executando conforme o README; criar a tag `release1` no commit entregue e a GitHub Release com o resumo e o link para `releases/release1/README.md`; ensaiar a demonstração. A avaliação usa a versão da tag, então nada muda depois dela.

A tag e a GitHub Release são feitas por quem tem permissão no repositório. Este documento deixa tudo pronto para esse passo.

## 1. Antes da tag (conferir, nesta ordem)

1. **A `main` contém tudo:** PR `develop` → `main` mesclado (a `ultimate-develop` entra na `develop` antes).
2. **CI verde na `main`:** Backend, Backend (PostgreSQL), Web, E2E, Mobile e Ambiente Docker (T67).
3. **Clone limpo funciona**, em uma pasta nova:

   ```bash
   git clone https://github.com/jpauloaraujo/ClinicalStudyPB.git clone-limpo
   cd clone-limpo
   docker compose up --build -d --wait
   python3 scripts/verificar-ambiente.py
   python3 scripts/verificar-recrutamento.py
   docker compose down -v
   ```

   O último comando de cada script deve ser "N de N verificações passaram."
4. **README e `releases/release1/README.md` completos:** nomes da equipe, burndown do Azure DevOps e retrospectiva (as duas últimas seções do relatório da release dependem de dados do Azure).

## 2. Tag e GitHub Release

```bash
git checkout main && git pull
git tag -a release1 -m "Release 1: planejamento e recrutamento (RF01 a RF11)"
git push origin release1
```

Na página **Releases → Draft a new release**: escolher a tag `release1`, título "Release 1", e no texto colocar o resumo (RF01 a RF11 concluídos, 419 testes de backend com 99,2% de cobertura de negócio, 130 testes web, 29 E2E) e o link `releases/release1/README.md`. Depois da tag, nenhum commit entra na versão avaliada.

## 3. Ensaio da demonstração

Ambiente: `docker compose up --build -d --wait` (perfil `demo`). Web em http://localhost:5173. Senha de todos: `senha-de-teste`.

| # | Quem | O que mostrar | Resultado esperado |
|---|---|---|---|
| 1 | Pesquisador (`demo.pesquisador@clinicalstudy.test`) | Estudo "Demo - Protocolo em planejamento": abas Critérios e Visitas; avançar para recrutamento | O indicador de estado muda para Recrutamento. Mostrar antes o bloqueio de avançar sem critério/visita em um estudo novo |
| 2 | Coordenador (`demo.coordenador@clinicalstudy.test`) | Estudo "Demo - Recrutamento com ultima vaga" → Candidatos → `CAND-004` → seção Inclusão | Quatro pré-condições atendidas, "1 de 2 vagas ocupadas"; **Incluir no estudo** → confirmar |
| 3 | Coordenador | Candidato `CAND-006` → Inclusão | Pré-condição "Vaga disponível" pendente, "Vagas esgotadas", botão desabilitado; pela API, 409 `VAGAS_ESGOTADAS` |
| 4 | Coordenador | Aba Participantes | Lista com o contador "2 de 2 vagas" |
| 5 | Qualquer | Relatório de testes e cobertura | Artefatos `cobertura-jacoco` e `playwright-report` do CI; resumo da cobertura (99,2%) |
| 6 | Qualquer | Swagger UI em http://localhost:8081/swagger-ui.html | Botão Authorize com o token de `POST /api/auth/login` |

Para repetir a demonstração do zero, `docker compose down -v` apaga o banco e o próximo `up` recarrega os dados fictícios. Os passos 2 e 3 consomem a última vaga: refaça o `down -v` antes de cada ensaio.
