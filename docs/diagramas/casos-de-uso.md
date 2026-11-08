# Casos de uso (T69)

Quatro perfis e os requisitos RF01 a RF11, conforme a matriz de permissões implementada (`@PreAuthorize` e `security/Permissoes`). "Escreve" inclui criar, editar e excluir; "lê" é só consulta. O participante usa o aplicativo mobile, que na Sprint 2 só tem o login.

```mermaid
flowchart LR
    ADM(["Administrador"])
    PES(["Pesquisador"])
    COO(["Coordenador"])
    PAR(["Participante<br/>app mobile"])

    subgraph SP1["Sprint 1"]
        RF01("RF01 Gerenciar usuários")
        RF02("RF02 Autenticar")
        RF03("RF03 Controlar acesso por perfil")
        RF04("RF04 Gerenciar estudos")
    end
    subgraph MB["Módulo B: planejamento do estudo"]
        RF05("RF05 Critérios de inclusão e exclusão")
        RF06("RF06 Visitas planejadas e atividades")
        RF07("RF07 Estados do estudo")
    end
    subgraph MC["Módulo C: recrutamento"]
        RF08("RF08 Cadastrar candidatos")
        RF09("RF09 Avaliar elegibilidade")
        RF10("RF10 Registrar consentimento")
        RF11("RF11 Incluir participantes")
    end

    ADM --> RF01
    ADM --> RF02
    PES --> RF02
    COO --> RF02
    PAR --> RF02

    PES -->|escreve| RF04
    COO -.->|lê| RF04
    PES -->|escreve| RF05
    COO -.->|lê| RF05
    PES -->|escreve| RF06
    COO -.->|lê| RF06
    PES -->|avança o estado| RF07
    COO -.->|lê| RF07

    COO -->|escreve| RF08
    PES -.->|lê| RF08
    COO -->|escreve| RF09
    PES -.->|lê| RF09
    COO -->|escreve| RF10
    PES -.->|lê| RF10
    COO -->|escreve| RF11
    PES -.->|lê| RF11

    RF02 -. exigido por .-> RF03
    RF07 -. libera .-> RF08
    RF08 --> RF09
    RF09 --> RF11
    RF10 --> RF11
```

Legenda: seta cheia = o perfil escreve; seta tracejada = só lê. O administrador não acessa nenhum requisito de RF04 a RF11 (recebe 403).

| Requisito | Administrador | Pesquisador | Coordenador | Participante |
|---|:---:|:---:|:---:|:---:|
| RF01 Usuários | escreve | — | — | — |
| RF02 Autenticação | ✔ | ✔ | ✔ | ✔ (login mobile na Sprint 3) |
| RF04 Estudos | — | escreve | lê | — |
| RF05 Critérios | — | escreve (em planejamento) | lê | — |
| RF06 Visitas e atividades | — | escreve (em planejamento) | lê | — |
| RF07 Estados do estudo | — | avança | lê o histórico | — |
| RF08 Candidatos | — | lê | escreve (em recrutamento) | — |
| RF09 Elegibilidade | — | lê | escreve (em recrutamento) | — |
| RF10 Consentimento | — | lê | escreve (em recrutamento) | — |
| RF11 Participantes | — | lê | escreve (em recrutamento) | — |
