# T31 · Integrar telas com a API e exibir erros de validação

> **Sprint 1** · PBI-07 · RF04 Cadastro de estudos clínicos  
> **Responsável:** I5 · **Estimativa:** 2h · **Prazo:** Sprint 1 (13/10 a 26/10/2026)  
> **Depende de:** T29, T17  
> **Situação:** ✅ Implementada (este documento descreve o que existe, para manter a coerência)

## 1. Objetivo

Mostrar ao usuário o motivo de cada erro da API, no campo do formulário quando for erro de validação.

## 2. Contexto: o que já existe e deve ser reaproveitado

- Formato de erro do backend: `{"erro": "campo: mensagem"}` (validação) ou `{"erro": "mensagem"}` (regra de negócio).
- Na Sprint 2 (T40) foi criado `src/api/erros.ts` (`converterErro`), que também entende `codigo`; `services/erros.ts` continua servindo as telas da Sprint 1.

## 3. Onde implementar

| Arquivo | Ação | Responsabilidade |
|---|---|---|
| `web/ClinicalStudyPB/src/services/erros.ts` | Existente | `extrairErro`: converte erro do Axios em `{mensagem, campo?, status?}` |
| `web/ClinicalStudyPB/src/pages/usuarios/UsuarioFormPage.tsx` | Existente | Usa `extrairErro` para o campo e o alerta geral |
| `web/ClinicalStudyPB/src/pages/estudos/EstudoFormPage.tsx` | Existente | Idem |

## 4. O que fazer em cada arquivo

### extrairErro

- Sem resposta (backend fora do ar): "Não foi possível conectar à API…".
- 400 com `campo: mensagem` e `campo` só com letras: devolve `campo` e `mensagem`.
- Outros casos: texto do servidor ou mensagem padrão por status (401, 403, 404).

### Formulários

- Se vier `campo`, mostrar no campo; senão, alerta geral no topo do formulário.

## 6. Testes

- Coberto pelos testes de componente das telas da Sprint 2 (mesmo padrão, com `converterErro`).

## 7. Critérios de aceite

- [ ] Erro de campo aparece junto do campo.
- [ ] Erro de regra aparece em alerta.
- [ ] Backend fora do ar mostra mensagem de conexão.

## 8. Cuidados e fora de escopo

- Novas telas devem usar `converterErro` (`src/api/erros.ts`), que é a versão atual.

---
Parte do conjunto de documentos de design da Sprint 1: [índice](README.md).
