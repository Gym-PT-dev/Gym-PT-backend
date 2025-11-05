package com.Gym_PT.config;

import com.Gym_PT.auth.JwtAuthFilter;
import com.Gym_PT.auth.JwtService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final JwtService jwtService;

    public SecurityConfig(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    @Bean
    SecurityFilterChain api(HttpSecurity http) throws Exception {
        http
                // ✅ CORS 활성화 (CorsConfigurationSource @Bean 사용)
                .cors(Customizer.withDefaults())
                .csrf(csrf -> csrf.disable())
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        // ✅ 프리플라이트는 무조건 통과
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        // ✅ 로그인/토큰 교환/헬스체크 허용
                        .requestMatchers("/api/auth/**").permitAll()
                        // 예시: 보호 API
                        .requestMatchers(HttpMethod.PUT, "/users/*/profile").authenticated()
                        .requestMatchers(HttpMethod.GET, "/users/*/profile").authenticated()
                        // 나머지는 필요에 따라 조이기
                        .anyRequest().permitAll()
                )
                // ✅ JWT 필터 (auth 경로 스킵)
                .addFilterBefore(new SkippableJwtFilter(jwtService), UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    /** 최초 로그인/토큰 교환 경로는 JWT 검사 스킵 */
    static class SkippableJwtFilter extends JwtAuthFilter {
        public SkippableJwtFilter(JwtService jwtService) { super(jwtService); }

        @Override
        protected boolean shouldNotFilter(HttpServletRequest request) {
            String p = request.getRequestURI();
            return p.startsWith("/api/auth/"); // /api/auth/** 전부 스킵
        }

        @Override
        protected void doFilterInternal(HttpServletRequest req, HttpServletResponse res, FilterChain chain)
                throws java.io.IOException, jakarta.servlet.ServletException {
            super.doFilterInternal(req, res, chain);
        }
    }
}
