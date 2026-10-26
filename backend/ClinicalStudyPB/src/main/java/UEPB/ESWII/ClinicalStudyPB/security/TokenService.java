package UEPB.ESWII.ClinicalStudyPB.security;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;

import UEPB.ESWII.ClinicalStudyPB.domain.model.Usuario;

/**
 * Serviço de geração e emissão de tokens JWT assinados (T19 / T20).
 */
@Service
public class TokenService {

    private final JwtEncoder jwtEncoder;
    private final String issuer;
    private final long expirationMinutes;

    public TokenService(
            JwtEncoder jwtEncoder,
            @Value("${jwt.issuer:clinicalstudypb}") String issuer,
            @Value("${jwt.expiration-minutes:30}") long expirationMinutes) {
        this.jwtEncoder = jwtEncoder;
        this.issuer = issuer;
        this.expirationMinutes = expirationMinutes;
    }

    /**
     * Emite um token JWT assinado para o usuário com as claims do contrato:
     * sub (UUID), role (Perfil), iss (emissor), iat (emissão) e exp (expiração).
     */
    public String gerarToken(Usuario usuario) {
        Instant agora = Instant.now();
        Instant expiracao = agora.plus(expirationMinutes, ChronoUnit.MINUTES);

        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer(issuer)
                .subject(usuario.getId().toString())
                .issuedAt(agora)
                .expiresAt(expiracao)
                .claim("role", usuario.getPerfil().name())
                .build();

        JwsHeader header = JwsHeader.with(MacAlgorithm.HS256).build();
        return jwtEncoder.encode(JwtEncoderParameters.from(header, claims)).getTokenValue();
    }
}
