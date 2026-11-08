# Sequência: incluir participante (T69 / RF11)

`POST /api/estudos/{estudoId}/participantes`, só para o coordenador. Tudo em uma única transação, com o estudo bloqueado para escrita: inclusões simultâneas são atendidas uma por vez e o total nunca passa do limite de vagas. Cada verificação que falha devolve 409 com um código próprio e **nenhuma participação é gravada**.

```mermaid
sequenceDiagram
    autonumber
    actor C as Coordenador
    participant W as Web (ficha do candidato)
    participant K as ParticipacaoController
    participant S as ParticipacaoService
    participant DB as PostgreSQL

    C->>W: Clica em "Incluir no estudo"
    W->>K: POST /participantes {candidatoId} + Bearer JWT
    Note over K: @PreAuthorize: só COORDENADOR (senão 403)
    K->>S: incluir(estudoId, candidatoId, usuarioId)
    S->>DB: SELECT estudo FOR UPDATE (bloqueio de escrita)
    S->>S: 1. estudo em recrutamento?
    S->>DB: busca o candidato
    S->>S: 2. candidato é deste estudo?
    S->>S: 3. candidato ainda não incluído?
    S->>DB: avaliação vigente do candidato
    S->>S: 4. avaliação vigente aprovada?
    S->>DB: consentimento do candidato
    S->>S: 5. consentimento registrado?
    S->>DB: conta participações do estudo
    S->>S: 6. incluídos menor que o limite?
    alt alguma verificação falha
        S-->>K: ConflitoRegraException (ou 404 no passo 2)
        K-->>W: 409 {erro, codigo}
        Note over W: ESTUDO_FORA_DE_RECRUTAMENTO, CANDIDATO_JA_INCLUIDO,<br/>CANDIDATO_NAO_ELEGIVEL, CONSENTIMENTO_AUSENTE, VAGAS_ESGOTADAS
        W->>C: Mensagem clara do motivo
    else tudo certo
        S->>DB: INSERT participacao (data de inclusão = hoje, quem incluiu)
        S->>DB: UPDATE candidato SET situacao = INCLUIDO
        S-->>K: participação
        K-->>W: 201 participação
        W->>C: Participante aparece na lista e o contador de vagas sobe
    end
    Note over DB: COMMIT libera o bloqueio do estudo para a próxima inclusão
```

A data de inclusão é o "dia 0": somada ao dia previsto de cada `VisitaPlanejada` dá a data real das visitas daquele participante (`VisitaPlanejada.dataPrevista`), usada pelo aplicativo mobile nas próximas Sprints.
