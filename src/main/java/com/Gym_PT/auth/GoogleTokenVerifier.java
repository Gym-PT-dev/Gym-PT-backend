package com.Gym_PT.auth;

import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.jwk.source.RemoteJWKSet;
import com.nimbusds.jose.proc.JWSVerificationKeySelector;
import com.nimbusds.jose.proc.SecurityContext;                 // JOSE 쪽
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.proc.ConfigurableJWTProcessor;        // ⬅️ JWT 쪽
import com.nimbusds.jwt.proc.DefaultJWTProcessor;             // ⬅️ JWT 쪽
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.net.URL;
import java.util.Date;
import java.util.List;

@Component
public class GoogleTokenVerifier {

    private final String clientId;
    private final ConfigurableJWTProcessor<SecurityContext> processor;

    public GoogleTokenVerifier(@Value("${google.client.id}") String clientId) throws Exception {
        this.clientId = clientId;

        // 구글 공개키(JWK) 소스
        JWKSource<SecurityContext> jwkSource =
                new RemoteJWKSet<>(new URL("https://www.googleapis.com/oauth2/v3/certs"));

        // JWT Processor + RS256 키 선택자
        this.processor = new DefaultJWTProcessor<>();
        this.processor.setJWSKeySelector(
                new JWSVerificationKeySelector<>(JWSAlgorithm.RS256, jwkSource)
        );
    }

    public JWTClaimsSet verify(String idToken) {
        try {
            JWTClaimsSet claims = processor.process(idToken, null);

            // iss 검증
            String iss = claims.getIssuer();
            if (!"https://accounts.google.com".equals(iss) && !"accounts.google.com".equals(iss)) {
                throw new IllegalArgumentException("invalid iss: " + iss);
            }

            // aud 검증(내 웹 클라이언트 ID)
            List<String> aud = claims.getAudience();
            if (aud == null || !aud.contains(clientId)) {
                throw new IllegalArgumentException("invalid aud: " + aud);
            }

            // exp 검증
            Date exp = claims.getExpirationTime();
            if (exp == null || exp.before(new Date())) {
                throw new IllegalArgumentException("expired id_token");
            }

            return claims;
        } catch (Exception e) {
            throw new IllegalArgumentException("invalid id_token", e);
        }
    }
}