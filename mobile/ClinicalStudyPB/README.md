# ClinicalStudyPB · App do participante (Expo)

Aplicativo mobile (Expo SDK 58, Expo Router) do participante: login, meus estudos e visitas. Só o perfil **Participante** entra; os demais perfis usam a aplicação web. Todos os dados são fictícios.

## Variáveis de ambiente

Copie `.env.example` para `.env.local` (ignorado pelo Git) e ajuste.

| Variável | Para que serve | Padrão |
|---|---|---|
| `EXPO_PUBLIC_API_URL` | URL do backend | `http://localhost:8081` |
| `EXPO_PUBLIC_USE_MOCKS` | `true` troca a API por respostas em memória (`src/api/mocks/`) | desligado |
| `EXPO_PUBLIC_MOCK_ERRO` | `true`, junto dos mocks, faz `/api/me/*` responder 500 para exercitar o estado de erro | desligado |

Onde o app roda define a URL da API:

| Onde o app roda | `EXPO_PUBLIC_API_URL` |
|---|---|
| Navegador (`npm run web`) ou simulador iOS | `http://localhost:8081` |
| Emulador Android | `http://10.0.2.2:8081` |
| Celular físico (Expo Go) | `http://<IP-da-máquina>:8081`, com celular e computador na mesma rede Wi-Fi |

## Como rodar

```bash
npm ci
```

**Com o backend** (dados de demonstração, perfil `demo`). Na raiz do repositório, suba o ambiente (`docker compose up -d db backend`, que já usa o perfil `demo`) ou rode o backend local com `SPRING_PROFILES_ACTIVE=demo` (veja `docs/tutorial-inicializacao.md`). Depois:

```bash
npm start          # Expo na porta 8082 (a para web: npm run web)
```

**Com mocks** (sem backend, para desenvolver telas e testar estados de erro):

```bash
# PowerShell
$env:EXPO_PUBLIC_USE_MOCKS="true"; npm start
# Bash
EXPO_PUBLIC_USE_MOCKS=true npm start
```

Os mocks existem para desenvolvimento e testes de componente. **A entrega roda contra a API real.**

### Participante de demonstração

| E-mail | Senha |
|---|---|
| `participante@clinicalstudy.test` | `senha-de-teste` |

## Comandos

| Comando | O que faz |
|---|---|
| `npm run lint` | ESLint |
| `npm run typecheck` | TypeScript sem emitir arquivos |
| `npm test` | Jest (preset `jest-expo`) e Testing Library; roda também no job **Mobile** do CI |
| `npm run api:tipos` | Gera `src/api/schema.d.ts` (Sprint 2) e `src/api/schema-sprint3.d.ts` (Sprint 3) a partir de `docs/api/*.yaml`; rode de novo quando o contrato mudar |

## E2E com Maestro

Fluxos de ponta a ponta do app, com o [Maestro](https://maestro.mobile.dev), em `.maestro/`. Rodam **localmente em emulador Android** contra a API real (sem mocks); não há E2E mobile no CI (decisão do Planning, T94).

| Fluxo | O que faz |
|---|---|
| `.maestro/fluxo-participante.yaml` | Login com `participante@clinicalstudy.test`, aba **Estudos**, detalhe do estudo "Demo - Acompanhamento de participantes", aba **Visitas** (próximas, detalhe da visita e histórico com o resumo "comparecimento: 1 de 1 visitas") e **Sair** pelo Perfil (volta ao login) |
| `.maestro/fluxo-visitas.yaml` | (T121, opcional) só as visitas: próximas, detalhe e histórico com o resumo |
| `.maestro/login-perfil-recusado.yaml` | Login com `coordenador@clinicalstudy.test`: mensagem "exclusivo para participantes", sem abrir as abas |

Os dois começam com `launchApp: clearState` (repetíveis) e usam texto visível e `testID` (`campo-email`, `campo-senha`, `botao-entrar`, `cartao-estudo`, `botao-sair`), nunca coordenadas.

**Pré-requisitos:** Java 17+, Android Studio com um AVD (Android 13+), [Expo Go](https://expo.dev/go) instalado no emulador e o Maestro CLI (`maestro --version`). Instalação do Maestro: <https://docs.maestro.dev/getting-started/installing-maestro> (no Windows, siga a seção do Windows ou use o WSL).

**Passo a passo:**

1. Backend com o perfil `demo` (na raiz: `docker compose up -d db backend`, ou `SPRING_PROFILES_ACTIVE=demo`); `participante@` precisa estar vinculada ao P1 (T111).
2. Neste diretório, `npm ci` e, com o emulador aberto, o Expo apontando para a API do emulador:
   ```bash
   # PowerShell
   $env:EXPO_PUBLIC_API_URL="http://10.0.2.2:8081"; npm start
   # Bash
   EXPO_PUBLIC_API_URL=http://10.0.2.2:8081 npm start
   ```
3. Em outro terminal, também neste diretório:
   ```bash
   maestro test .maestro/fluxo-participante.yaml
   maestro test .maestro/login-perfil-recusado.yaml
   # celular físico ou outro endereço do Expo:
   maestro test -e EXPO_URL=exp://192.168.0.10:8082 .maestro/fluxo-participante.yaml
   # gravar vídeo da execução:
   maestro record .maestro/fluxo-participante.yaml
   ```
4. As capturas (`takeScreenshot`) caem neste diretório como `rf17-*.png`; mova-as para `docs/imagens/sprint3/` (o `.gitignore` do app ignora as soltas).

Para inspecionar a tela e achar identificadores: `maestro studio`. A evidência de cada execução fica em [`docs/sprint3/e2e-mobile-evidencia.md`](../../docs/sprint3/e2e-mobile-evidencia.md).

## Estrutura

```
src/
├── api/
│   ├── cliente.ts        # fetch único: token, timeout de 15 s, 401 central (definirAoExpirarSessao)
│   ├── erro.ts           # ErroApi (status, codigo, errosCampo) e mensagemDoErro()
│   ├── mocks/            # respostas em memória (EXPO_PUBLIC_USE_MOCKS)
│   └── schema*.d.ts      # tipos gerados do contrato OpenAPI
├── app/                  # rotas (Expo Router): login, (tabs), estudos/[estudoId], visitas/[visitaId]
├── components/           # Tela, Cartao, Botao, Selo, EstadoVazio, EstadoErro, Carregando e o tema
└── contexts/sessao.tsx   # sessão (SecureStore); um 401 fora do login limpa o token e volta ao login
```

## Erros da API

O cliente entende o formato único de erro da Sprint 2 (`status`, `codigoRegra`, `mensagem`, `errosCampo[{campo, erro}]`). Nas telas, use `mensagemDoErro(erro)` para exibir a mensagem. Falha de rede vira `ErroApi` com `codigo: 'SEM_CONEXAO'`.
