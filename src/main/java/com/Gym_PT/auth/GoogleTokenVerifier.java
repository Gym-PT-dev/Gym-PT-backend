package com.Gym_PT.auth;

import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.jwk.source.RemoteJWKSet;
import com.nimbusds.jose.proc.JWSVerificationKeySelector;
import com.nimbusds.jose.proc.SecurityContext;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.proc.ConfigurableJWTProcessor;
import com.nimbusds.jwt.proc.DefaultJWTProcessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.net.URL;
import java.util.*;
import java.util.stream.Collectors;

@Component
public class GoogleTokenVerifier {

    private static final Logger log = LoggerFactory.getLogger(GoogleTokenVerifier.class);

    private final Set<String> allowedClientIds; // Web + Android 모두
    private final ConfigurableJWTProcessor<SecurityContext> processor;

    public GoogleTokenVerifier(@Value("${google.client.ids}") String clientIdsCsv) throws Exception {
        // 콤마로 분리하여 공백 제거
        this.allowedClientIds = Arrays.stream(clientIdsCsv.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toCollection(LinkedHashSet::new));

        // 구글 공개키(JWK) 소스
        JWKSource<SecurityContext> jwkSource =
                new RemoteJWKSet<>(new URL("https://www.googleapis.com/oauth2/v3/certs"));

        // JWT Processor + RS256 키 선택자
        this.processor = new DefaultJWTProcessor<>();
        this.processor.setJWSKeySelector(
                new JWSVerificationKeySelector<>(JWSAlgorithm.RS256, jwkSource)
        );

        if (log.isInfoEnabled()) {
            log.info("[GoogleTokenVerifier] Allowed audiences: {}", this.allowedClientIds);
        }
    }

    public JWTClaimsSet verify(String idToken) {
        try {
            JWTClaimsSet claims = processor.process(idToken, null);

            // iss 검증
            String iss = claims.getIssuer();
            if (!"https://accounts.google.com".equals(iss) && !"accounts.google.com".equals(iss)) {
                throw new IllegalArgumentException("invalid iss: " + iss);
            }

            // aud 검증: Web 또는 Android 중 하나라도 매칭되면 통과
            List<String> aud = claims.getAudience();
            if (aud == null || aud.isEmpty()) {
                throw new IllegalArgumentException("missing aud");
            }
            boolean audOk = aud.stream().anyMatch(allowedClientIds::contains);
            if (!audOk) {
                // 힌트용 상세 로그
                log.warn("Audience not allowed. aud={}, allowed={}", aud, allowedClientIds);
                throw new IllegalArgumentException("invalid aud: " + aud);
            }

            // exp 검증
            Date exp = claims.getExpirationTime();
            if (exp == null || exp.before(new Date())) {
                throw new IllegalArgumentException("expired id_token");
            }

            // (옵션) azp 힌트 로그 — 여러 aud일 때 실제 승인 파티
            Object azp = claims.getClaim("azp");
            if (azp != null && log.isDebugEnabled()) {
                log.debug("azp (authorized party): {}", azp);
            }

            if (log.isDebugEnabled()) {
                log.debug("Google id_token verified. sub={}, email={}, aud={}",
                        claims.getSubject(), claims.getClaim("email"), aud);
            }

            return claims;
        } catch (Exception e) {
            // 상위에서 401로 매핑됨
            throw new IllegalArgumentException("invalid id_token", e);
        }
    }
}
