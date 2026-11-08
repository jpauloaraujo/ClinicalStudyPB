# Sequência: avançar o estado do estudo (T69 / RF07)

`POST /api/estudos/{estudoId}/transicoes`, só para o pesquisador. A autorização acontece antes do controller; as regras estão em `MaquinaEstadosEstudo` (pura) e a consulta e a gravação em `EstudoTransicaoService`.

```mermaid
sequenceDiagram
    autonumber
    actor P as Pesquisador
    participant W as Web (IndicadorEstado)
    participant C as EstudoTransicaoController
    participant S as EstudoTransicaoService
    participant M as MaquinaEstadosEstudo
    participant DB as PostgreSQL

    P->>W: Clica em "Avançar para Recrutamento"
    W->>P: Diálogo "Não é possível voltar"
    P->>W: Confirma
    W->>C: POST /transicoes {destino} + Bearer JWT
    Note over C: @PreAuthorize: só PESQUISADOR (senão 403)
    C->>S: avancar(estudoId, destino, usuarioId do token)
    S->>DB: SELECT estudo FOR UPDATE (bloqueio)
    DB-->>S: estudo (estado atual)
    S->>DB: conta critérios de inclusão e visitas previstas
    DB-->>S: contagens
    S->>M: validar(origem, destino, criterios, visitas)
    alt fora da sequência ou pré-condição não atendida
        M-->>S: ConflitoRegraException (código da regra)
        S-->>C: exceção
        C-->>W: 409 {erro, codigo}
        W->>P: Alerta com a mensagem da regra
    else válido
        M-->>S: ok
        S->>DB: UPDATE estudo SET status = destino
        S->>DB: INSERT historico_status_estudo (anterior, novo, usuário, data)
        S-->>C: estudo atualizado
        C-->>W: 200 estudo
        W->>P: Indicador mostra o novo estado e critérios e visitas ficam somente leitura
    end
```

Como o estudo é bloqueado na leitura, dois avanços simultâneos são atendidos um de cada vez: o segundo encontra o novo estado e é recusado como `TRANSICAO_ESTADO_INVALIDA`.
