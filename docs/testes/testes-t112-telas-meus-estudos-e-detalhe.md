# Testes da T112 — Telas Meus estudos e detalhe do estudo (app mobile)

> Plano: [T112-plano-de-implementacao](../design/sprint3/T112-plano-de-implementacao.md) · Guia: [T112](../design/sprint3/T112-telas-meus-estudos-e-detalhe-do-estudo.md)
> Branch: `feat/task112-meus-estudos-detalhe` (criada a partir de `feat/task110-login-logout-app`, que ainda não está na `develop`).
> Regra seguida: **nenhum teste foi excluído nem alterado para passar**. Toda falha seria registrada em [registro-de-defeitos](registro-de-defeitos.md) **antes** de qualquer correção. Nesta task **não houve nenhuma falha de teste** (seção 4).

## 1. Linha de base (antes da T112) e resultado final

Comandos em `mobile/ClinicalStudyPB`.

| Verificação | Antes | Depois |
|---|---|---|
| `npm test` (Jest) | 8 suítes, 90 testes passando | **10 suítes, 129 testes passando** |
| `npm run typecheck` | sem erros | sem erros |
| `npm run lint` | 0 erros, 1 aviso (`.expo/types/router.d.ts`, arquivo gerado, anterior à T112) | 0 erros, o mesmo aviso |
| `npx expo export --platform android` (bundle) | não medido | gerou o bundle sem erro |
| Jest com `TZ=America/Los_Angeles` e `TZ=Pacific/Kiritimati` (utilitário e telas, 34 testes) | — | passando nos dois fusos |

Os três passos do job "Mobile (Expo)" do CI (lint, typecheck e `npm test`) foram reproduzidos localmente.

## 2. Alterações da T112 (rastreabilidade)

| Arquivo | Tipo | O que mudou | RQ |
|---|---|---|---|
| `src/utils/estudos.ts` | **novo** | Rótulos e tons do status do estudo e da situação; `formatarData` sem `new Date`; `formatarPeriodo` | 01, 05, 10 |
| `src/api/estudos.ts` | alterado | `estudosApi.buscar(estudoId)` e o tipo `MeuEstudoDetalhe` | 05, 07 |
| `src/app/(tabs)/estudos.tsx` | alterado | Lista de cartões (M03): `FlatList`, `RefreshControl`, carregando, erro, vazio | 01 a 04, 09, 10 |
| `src/app/estudos/[estudoId].tsx` | alterado | Detalhe (M04): dados, retirada, atalho para visitas, carregando, erro e 404 | 05 a 07, 09, 10 |
| `src/api/mocks/index.ts` | alterado | Conta sem vínculo recebe 404 no detalhe, como no servidor | 07 |
| `src/utils/__tests__/estudos.test.ts` | **novo** | 14 testes do utilitário | 01, 05, 10 |
| `src/api/__tests__/estudos.test.ts` | ampliado | 5 casos de `buscar` | 05, 07 |
| `src/telas/__tests__/estudos.test.tsx` | **novo** | 20 testes de componente (11 da lista e 9 do detalhe) | 01 a 07, 09, 10 |

Não alterados: `app/_layout.tsx` (o cabeçalho "Estudo" e o botão "Voltar" já existiam, RQ-08), backend, web, contrato e `schema-sprint3.d.ts`.

## 3. Matriz RQ × teste

| RQ | Teste |
|---|---|
| 01 | Lista: "cada estudo é um cartão com título, status em português e data…"; "só o estudo com participação retirada tem o selo Retirado"; utilitário (rótulos e data) |
| 02 | "mantém a ordem recebida da API"; "tocar no cartão abre o detalhe" (`router.push` com o `estudoId`) |
| 03 | "mostra o carregamento"; "lista vazia mostra o texto do guia"; "erro mostra a mensagem e Tentar de novo" |
| 04 | "puxar para atualizar recarrega e o indicador termina"; "atualização que falha mantém os cartões e também termina o indicador" |
| 05 | Detalhe: "mostra título, objetivo completo, responsável, período, status e situação"; período parcial e ausente; retirado com data e sem a palavra "motivo"; API: `buscar` traz `retirada.dataRetirada` e nunca o motivo |
| 06 | "o atalho Ver visitas deste estudo navega para a aba Visitas com o estudoId" |
| 07 | Detalhe: carregando; "erro com Tentar de novo"; "404 mostra Estudo não encontrado. e o botão Voltar, sem Tentar de novo"; "sem histórico, Voltar leva para a lista"; API: 404 para id desconhecido e para conta sem vínculo, 403 para outro perfil |
| 08 | Sem teste novo: o cabeçalho é do `_layout.tsx` da T108 (conferido na seção 6) |
| 09 | "o cartão tem papel de botão e rótulo com título, status e data" (inclui o sufixo ", retirado") |
| 10 | "não mostra nenhum dado de outro participante nem JSON cru"; datas em dois fusos; utilitário sem `new Date` |
| 11 | Roteiro manual (seção 7); **parcial** |

## 4. Falhas encontradas e correções

**Nenhuma.** Os 20 testes de componente, os 14 do utilitário e os 5 novos da API passaram na primeira execução, e os 90 testes anteriores continuaram passando. Registrar isso é deliberado: não houve falha para esconder nem teste ajustado para passar. Cuidados que reduzem o risco de um teste que passa por engano:
- Os testes da lista conferem a **ordem** (a API devolve `[retirado, ativo]` e a tela mantém) e o **indicador de atualização** nos dois desfechos (sucesso e erro).
- O teste de datas roda também com `TZ` diferentes.
- O teste do motivo procura a palavra "motivo" no texto da tela e no JSON do detalhe.

## 5. Resultado final

- `npm test`: **10 suítes, 129 testes passando** (90 anteriores + 39 novos).
- `npm run typecheck`: sem erros. `npm run lint`: 0 erros (1 aviso anterior, de arquivo gerado).
- Bundle Android do `expo export`: gerado sem erro.

## 6. Lacunas conhecidas

1. **RQ-T112-11 (conferência com a API real em emulador ou celular): não executada.** Este ambiente não tem emulador. O que foi conferido: telas e fluxos com mocks (testes de componente) e o bundle. Falta rodar com `EXPO_PUBLIC_USE_MOCKS=true` e depois com o backend real e as contas `participante@` (vinculada ao P1 pela T111) e `demo.participante@` (P5).
2. **"Retirado" real:** o backend atual devolve sempre `ATIVO` (`TODO(T101)`); o selo e a data da retirada só aparecem com mocks e nos testes de componente até a T101/T103/T105 entrarem. O app já trata o valor.
3. **Cabeçalho "Estudo" e botão "Voltar" (RQ-08):** vêm da T108 e não foram verificados visualmente.
4. **Atalho para visitas:** o teste confere só a chamada de navegação com o `estudoId`; a aba Visitas ainda ignora o parâmetro (T117, pendência P30).
5. **Sem teste do desmonte durante a carga** (a tela evita atualizar estado depois de sair, mas nenhum teste cobre isso).
6. **Sem E2E (Maestro):** é da T114.

## 7. Roteiro de conferência manual

**Com mocks** (`EXPO_PUBLIC_USE_MOCKS=true npx expo start`):
1. Entrar com `participante@clinicalstudy.test` e abrir a aba Estudos: dois cartões ("Estudo de pressão arterial" em execução e "Estudo de sono" com o selo Retirado), data de inclusão em `dd/MM/yyyy`.
2. Tocar no primeiro: detalhe com objetivo completo, responsável, período e selos; "Ver visitas deste estudo" abre a aba Visitas.
3. Voltar e tocar no segundo: aparecem a marca "Retirado" e "Retirado em 10/11/2026", sem motivo.
4. Puxar a lista para baixo: o indicador aparece e some. Com `EXPO_PUBLIC_MOCK_ERRO=true`: erro com "Tentar de novo".
5. Entrar com `sem.vinculo@clinicalstudy.test`: "Você ainda não está incluído em nenhum estudo."

**Com a API real** (perfil `demo`, depois da T111): `participante@` (P1) e `demo.participante@` (P5) veem o estudo de acompanhamento, ativo; o estado retirado real só depois da T105.
