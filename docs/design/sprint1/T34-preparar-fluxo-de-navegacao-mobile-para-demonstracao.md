# T34 · Preparar fluxo de navegação mobile para demonstração

> **Sprint 1** · PBI-08 · Protótipos navegáveis das interfaces  
> **Responsável:** I7 · **Estimativa:** 3h · **Prazo:** Sprint 1 (13/10 a 26/10/2026)  
> **Depende de:** T33, T12  
> **Situação:** 🟡 Parcialmente implementada (o documento separa o que existe do que falta)

## 1. Objetivo

Deixar pronto o que será mostrado na demonstração do mobile: o fluxo percorrível no Figma, o diagrama de navegação, o roteiro de apresentação e a navegação básica no aplicativo.

## 2. Contexto: o que já existe e deve ser reaproveitado

- Requisitos: `docs/sprint1/prototipos-mobile.md`, seções 4 e 6. Código da navegação: `docs/sprint1/implementacao-mobile.md`.
- Pronto no código: Expo Router com login fora das abas e quatro abas protegidas por sessão. Pendente: o fluxo no Figma.

## 3. Onde implementar

| Arquivo | Ação | Responsabilidade |
|---|---|---|
| `mobile/ClinicalStudyPB/src/app/_layout.tsx` | Existente | `Stack.Protected` alterna entre login e abas conforme haja sessão |
| `mobile/ClinicalStudyPB/src/app/(tabs)/_layout.tsx` | Existente | Abas Início, Estudos, Visitas e Perfil |
| `mobile/ClinicalStudyPB/src/contexts/sessao.tsx` | Existente | Sessão do participante (login em `/api/auth/login` e `/api/auth/me`) |
| `Figma` | Criar | Fluxos F1 a F7 em modo apresentação |
| `docs/sprint1/navegacao-mobile.md` | Criar (opcional) | Diagrama de navegação em Mermaid, caso a seção 4.1 do documento de requisitos não baste |

## 4. O que fazer em cada arquivo

### Fluxos percorríveis

- F1 Entrar (M01 → M02). F2 Ver meus estudos (M03 → M04). F3 Próximas visitas e histórico (M05 ⇄ M06 → M07).
- F4 Confirmar presença (M05 → M07 → M08). F5 Reagendamento (M07 → M09). F6 Retirada (M03 → M04 → M10). F7 Sair (Perfil → M01).

### Roteiro de 3 a 5 minutos

- Login → Meus estudos e detalhe → Visitas (próximas e histórico) → confirmar presença → solicitar reagendamento → solicitar retirada até a confirmação → sair.
- Se der tempo, mostrar um erro de login e um estado vazio.

### Verificação do código

- Sem sessão, o app abre no login; só o perfil participante entra; "Sair" no Perfil apaga o token e volta ao login.

## 7. Critérios de aceite

- [ ] Fluxo percorrível no Figma sem backend.
- [ ] Roteiro revisado pela equipe e anexado ao PBI.
- [ ] Links dos protótipos web e mobile disponíveis.

## 8. Cuidados e fora de escopo

- O fluxo no Figma é a parte que falta; o código de navegação já está entregue na T12.

---
Parte do conjunto de documentos de design da Sprint 1: [índice](README.md).
