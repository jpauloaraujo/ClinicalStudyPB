# Evidência do E2E mobile (T114)

> Plano: [T114-plano-de-implementacao](../design/sprint3/T114-plano-de-implementacao.md) · Guia: [T114](../design/sprint3/T114-e2e-mobile-maestro-capturas-e-documentacao.md)
> Branch: `feat/task114-e2e-mobile-maestro`.

## 1. Situação: fluxo escrito, **execução em emulador pendente**

| Item | Estado |
|---|---|
| `.maestro/fluxo-participante.yaml` (RQ-T114-01, 02, 04) | Escrito |
| `.maestro/login-perfil-recusado.yaml` (RQ-T114-03) | Escrito |
| Identificadores (`testID`) nas telas e testes de componente (RQ-T114-02) | Feito: 3 testes novos, `npm test` com 132 passando |
| **Execução em emulador, gravação ou capturas e saída do `maestro test` (RQ-T114-05)** | **Não executada** |
| Capturas em `docs/imagens/sprint3/` (RQ-T114-08) | **Pendentes**, dependem da execução |
| Bugs de plataforma no Azure Boards (RQ-T114-07) e Remaining Work (RQ-T114-11) | Pendentes, ação humana |
| Aviso à I5 (RQ-T114-12) | Pendente, ação humana (no PR) |

**Por que não foi executada:** o ambiente em que a task foi implementada não tem `maestro` nem `adb` nem emulador. Os dois arquivos `.yaml` **não foram validados pelo Maestro**: a sintaxe foi escrita pela documentação, e a primeira execução pode pedir ajustes (ver seção 4). A task só pode ser dada como concluída depois da seção 3 preenchida.

## 2. Como executar (resumo; detalhes no [README do app](../../mobile/ClinicalStudyPB/README.md#e2e-com-maestro))

```bash
# backend em perfil demo no ar; emulador Android com o Expo Go aberto
cd mobile/ClinicalStudyPB
EXPO_PUBLIC_API_URL=http://10.0.2.2:8081 npm start     # terminal 1
maestro test .maestro/fluxo-participante.yaml           # terminal 2 (duas vezes seguidas: repetibilidade)
maestro test .maestro/login-perfil-recusado.yaml
maestro record .maestro/fluxo-participante.yaml         # opcional: vídeo
```

## 3. Registro da execução (preencher)

| Campo | Valor |
|---|---|
| Data | |
| Maestro (`maestro --version`) | |
| Emulador / AVD e versão do Android | |
| Expo Go (versão) e Expo SDK | SDK 58 |
| `appId` usado | `host.exp.exponent` (Expo Go) |
| Backend | perfil `demo`, commit: |
| `fluxo-participante.yaml`, 1ª execução | |
| `fluxo-participante.yaml`, 2ª execução (repetível) | |
| `login-perfil-recusado.yaml` | |
| Evidência (vídeo ou capturas) | |
| Saída do comando (colar o trecho final com "Flow ... Completed") | |

## 4. Pontos a conferir na primeira execução

1. **Expo Go:** a primeira abertura pode mostrar o menu do desenvolvedor ou um aviso de versão que cobre a tela de login. Se travar o fluxo, descartar com um `tapOn` do botão do aviso, ou trocar para *development build* (`npx expo run:android`, com `android.package` no `app.json` e `appId` próprio). A troca deve ser registrada aqui com o motivo.
2. **Texto exato** do status e do título no seed `demo` para o P1: o fluxo espera "Demo - Acompanhamento de participantes" e o selo "Em execução" na lista e "Ativo" no detalhe. Se o seed mostrar outro valor, ajustar a variável `ESTUDO` e as asserções.
3. **`tapOn: id:`** depende de o `testID` virar `resource-id` no Android; se o Maestro não achar, usar o texto ("Entrar", "Sair") ou o rótulo acessível.
4. **`back`** é o botão voltar do Android; no iOS usar o botão do cabeçalho.
5. **`takeScreenshot`** grava no diretório atual: mover os `rf17-*.png` para `docs/imagens/sprint3/` e referenciá-los no `README.md` principal (hoje a linha do RF17 cita que as capturas entram depois).
6. **Tempo:** a espera de 120 s do login cobre o primeiro empacotamento do Metro; reduzir se ficar longo demais.

## 5. Decisões

- **CI com emulador fora** (decisão do Planning, T94): os fluxos rodam só localmente, com evidência neste documento. Candidato à Sprint 4.
- **Expo Go como padrão** (o guia manda); o plano prevê a troca para *development build* se for instável (decisão 1 do plano).
- **Sem mocks:** o E2E usa a API real; a conta `participante@clinicalstudy.test` precisa do vínculo ao P1 (T111). Em backend sem a T111 ela cai em "lista vazia" e o fluxo falha na asserção do estudo.
