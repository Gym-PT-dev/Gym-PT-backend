package com.Gym_PT.auth;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;

import java.io.IOException;
import java.util.List;

public class JwtAuthFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(JwtAuthFilter.class);

    private final JwtService jwtService;

    public JwtAuthFilter(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest req,
                                    HttpServletResponse res,
                                    FilterChain chain) throws ServletException, IOException {

        // 이미 인증된 경우(skip)
        if (SecurityContextHolder.getContext().getAuthentication() != null) {
            chain.doFilter(req, res);
            return;
        }

        String h = req.getHeader(HttpHeaders.AUTHORIZATION);
        String token = null;

        if (StringUtils.hasText(h)) {
            // Bearer 접두사 대소문자, 공백 안전 처리
            String prefix = "Bearer ";
            if (h.regionMatches(true, 0, prefix, 0, prefix.length())) {
                token = h.substring(prefix.length()).trim();
            }
        }

        if (StringUtils.hasText(token)) {
            try {
                // 토큰 유효성 검증 + subject 추출 (필요하면 만료검사/서명검사 내부에서)
                String sub = jwtService.parseSubject(token); // Users.id (문자열)라고 가정

                // 권한 필요 없으면 빈 리스트도 OK. (인가 규칙에서 authenticated()만 검사 시)
                // ROLE이 필요하면 아래처럼 기본 ROLE_USER 하나 부여
                List<GrantedAuthority> authorities = List.of(new SimpleGrantedAuthority("ROLE_USER"));

                AbstractAuthenticationToken auth =
                        new UsernamePasswordAuthenticationToken(sub, null, authorities);

                // 요청 메타 정보(details) 부여 (IP, 세션 등)
                auth.setDetails(new WebAuthenticationDetailsSource().buildDetails(req));

                // ✅ 실제 인증 컨텍스트에 주입
                SecurityContextHolder.getContext().setAuthentication(auth);

                // 디버그 로그
                if (log.isDebugEnabled()) {
                    log.debug("JWT authenticated: sub={}", sub);
                }
            } catch (Exception e) {
                // 이전엔 조용히 무시 → 이제는 디버깅 위해 한 줄 남김
                log.debug("JWT parsing/validation failed: {}", e.getMessage());
            }
        }

        chain.doFilter(req, res);
    }
}
