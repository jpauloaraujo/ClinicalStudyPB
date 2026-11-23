# T119 · Estados das telas de visitas e roteiro de conferência no emulador

> Guia: [T119](../design/sprint3/T119-integracao-com-a-api-real-e-tratamento-de-estados.md) · Branch: `feature/t119-integracao-api-visitas`.

## 1. O que a tela faz em cada situação

| Situação | O que o participante vê | Onde está no código |
|---|---|---|
| Primeira carga | Indicador com o texto "Carregando…" (acessível) | `Carregando`, em `(tabs)/visitas.tsx` e `ListaHistoricoVisitas.tsx` |
| Atualizar puxando | Indicador discreto do `RefreshControl`; a lista continua na tela | `RefreshControl` |
| Carregar mais (histórico) | "Carregando mais…" no rodapé, sem apagar a lista | `ListaHistoricoVisitas.tsx` |
| Erro do servidor (4xx e 5xx) | Mensagem do servidor ou "Não foi possível carregar agora. Tente de novo." e o botão **Tentar de novo**; nunca JSON cru | `utils/erros.ts` (`mensagemDaConsulta`) |
| Sem conexão | "Não foi possível conectar. Verifique sua conexão e tente de novo." e **Tentar de novo**; sem a URL da API | `utils/erros.ts` |
| Falha com a lista já carregada | A lista é mantida e o aviso aparece no topo | `ListaHeaderComponent` das duas listas |
| 401 | O cliente limpa a sessão e volta ao login: "Sua sessão expirou. Entre novamente." | `api/cliente.ts`, `contexts/sessao.tsx` |
| 403 em `/api/me/*` | Volta ao login com "Este aplicativo é exclusivo para participantes. Use a aplicação web." | `definirAoPerfilRecusado` em `api/cliente.ts`, `contexts/sessao.tsx` |
| Lista vazia (retirado ou sem estudo) | "Nenhuma visita agendada." / "Nenhuma visita no histórico ainda."; não é erro | `EstadoVazio` |

Os mocks só entram com `EXPO_PUBLIC_USE_MOCKS=true`; sem a variável, as duas telas chamam `/api/me/visitas/proximas` e `/api/me/visitas/historico` na API real.

## 2. Roteiro manual no emulador (RQ-T119-08): **ainda não executado**

Pré-requisitos: backend no perfil `demo`, Expo com `EXPO_PUBLIC_API_URL=http://10.0.2.2:8081` e emulador Android com o Expo Go (mesmo preparo do [E2E mobile](e2e-mobile-evidencia.md)).

| # | Conta | Passos | Resultado esperado | Feito |
|---|---|---|---|---|
| 1 | `participante@clinicalstudy.test` (P1) | Abrir a aba Visitas | A visita "Retorno 30 dias" aparece em Próximas | ☐ |
| 2 | P1 | Tocar em **Histórico** | A "Triagem" realizada e o resumo "comparecimento: 1 de 1 visitas" | ☐ |
| 3 | `demo.participante@clinicalstudy.test` | Abrir Próximas e Histórico | Próximas vazias; no histórico, a visita cancelada com o motivo "retirada do estudo" | ☐ |
| 4 | Participante sem estudo | Abrir a aba Visitas | Lista vazia e sem erro | ☐ |
| 5 | Qualquer | Parar o backend durante o uso e atualizar | Aviso de conexão e **Tentar de novo**; com o backend de volta, a lista recarrega | ☐ |

Defeitos encontrados vão para o Azure Boards como Bug ligado a esta task (RQ-T119-09). Capturas em `docs/imagens/sprint3/`.
