# T40 · Tipos TypeScript e mocks a partir do contrato

> **Sprint 2** · PBI-09 · Fundamentos  
> **Responsável:** I5 · **Estimativa:** 3h · **Prazo:** Sex 30/10  
> **Depende de:** T37  
> **Situação:** ✅ Implementada (este documento descreve o que existe, para manter a coerência)

## 1. Objetivo

Permitir que a web seja desenvolvida antes do backend: tipos gerados do contrato, módulos de API tipados, um conversor de erros por campo e uma API fictícia (Mock Service Worker) ligada por variável de ambiente.

## 2. Contexto: o que já existe e deve ser reaproveitado

- Contrato: `docs/api/openapi-sprint2.yaml` (T37).
- Os tipos ficam em `src/api/schema.d.ts` (gerado e versionado); `src/api/tipos.ts` reexporta com nomes curtos.

## 3. Onde implementar

| Arquivo | Ação | Responsabilidade |
|---|---|---|
| `web/ClinicalStudyPB/src/api/schema.d.ts` | Existente (gerado) | Saída do `openapi-typescript`; nunca editar à mão |
| `web/ClinicalStudyPB/src/api/tipos.ts` | Existente | Aliases (`CriterioResponse`, `CandidatoResponse`...) e rótulos (`rotulosSituacao`, `rotulosCategoria`) |
| `web/ClinicalStudyPB/src/api/{criterios,visitas,candidatos,avaliacoes,consentimento,participantes,estadoEstudo}.ts` | Existente | Um módulo por recurso, usando o cliente Axios da T14 |
| `web/ClinicalStudyPB/src/api/erros.ts` | Existente | `converterErro`: formato do backend → `{mensagem, campos}` |
| `web/ClinicalStudyPB/src/mocks/{handlers,dados,browser,server,iniciar}.ts` | Existente | Respostas fictícias em memória para todos os endpoints |
| `web/ClinicalStudyPB/public/mockServiceWorker.js` | Existente (gerado) | Service worker do MSW |
| `web/ClinicalStudyPB/.env.mocks` | Existente | `VITE_USE_MOCKS=true` para o modo `dev:mock` |
| `web/ClinicalStudyPB/package.json` | Existente | Scripts `api:tipos` e `dev:mock` |
| `web/ClinicalStudyPB/src/main.tsx` | Existente | Chama `iniciarMocks()` antes de montar a aplicação |

## 4. O que fazer em cada arquivo

### Quando o contrato mudar

- 1. Corrigir `docs/api/openapi-sprint2.yaml`. 2. `npm run api:tipos`. 3. Ajustar `tipos.ts`, o módulo de API e os handlers do mock. 4. `npm test`.

### Mock

- `npm run dev:mock` sobe a web sem backend; entre com e-mail começando por `admin`, `pesquisador` ou `coordenador` e qualquer senha.
- O mock reproduz as regras principais (estado exigido, duplicidade, validação, permissões) e o mesmo formato de erro.
- Com a variável desligada (padrão) as mesmas chamadas vão ao backend real.

### converterErro

- Aceita `{erro, codigo?}` do backend e também uma lista de erros por campo, se a API passar a devolvê-la.

## 6. Testes

- `web/ClinicalStudyPB/src/api/erros.test.ts` e `src/mocks/mocks.test.tsx` (telas rodando sobre o mock).

## 7. Critérios de aceite

- [ ] Uma tela lista dados vindos do mock sem alteração de código.
- [ ] `npm run api:tipos` regenera os tipos sem erro.

## 8. Cuidados e fora de escopo

- O gerador é chamado por `npx` com versão fixa por causa do conflito de peer dependency com TypeScript 6.

---
Parte do conjunto de documentos de design da Sprint 2: [índice](README.md).
