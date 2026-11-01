package UEPB.ESWII.ClinicalStudyPB.controller;

import java.util.UUID;

import org.springframework.security.oauth2.jwt.Jwt;

/** Extrai do token o usuário que fez a requisição. */
final class Autenticacao {

    private Autenticacao() {
    }

    static UUID idDe(Jwt jwt) {
        return jwt == null ? null : UUID.fromString(jwt.getSubject());
    }
}
