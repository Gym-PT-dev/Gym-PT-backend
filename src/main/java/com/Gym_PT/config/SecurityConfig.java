// com/Gym_PT/config/SecurityConfig.java
package com.Gym_PT.config;

import com.Gym_PT.auth.JwtAuthFilter;
import com.Gym_PT.auth.JwtService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
public class SecurityConfig {

    private final JwtService jwtService;

    public SecurityConfig(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    @Bean
    SecurityFilterChain api(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        // ✅ dev 로그인/헬스체크 완전 허용
                        .requestMatchers("/api/auth/dev/**").permitAll()

                        // ✅ 프로필 API는 인증 필요
                        .requestMatchers(HttpMethod.PUT, "/users/*/profile").authenticated()
                        .requestMatchers(HttpMethod.GET, "/users/*/profile").authenticated()

                        // 그 외는 일단 허용(필요시 tightening)
                        .anyRequest().permitAll()
                )
                // ✅ JWT 필터 등록 (dev 엔드포인트는 필터 스킵)
                .addFilterBefore(new SkippableJwtFilter(jwtService), UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    /**
     * dev 엔드포인트는 필터를 아예 타지 않게 우회하는 래퍼.
     * (기존 JwtAuthFilter가 OncePerRequestFilter라 가정)
     */
    static class SkippableJwtFilter extends JwtAuthFilter {
        public SkippableJwtFilter(JwtService jwtService) { super(jwtService); }

        @Override
        protected boolean shouldNotFilter(HttpServletRequest request) {
            String path = request.getRequestURI();
            return path.startsWith("/api/auth/dev/");
        }

        @Override
        protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
                throws java.io.IOException, jakarta.servlet.ServletException {
            super.doFilterInternal(request, response, chain);
        }
    }
}
