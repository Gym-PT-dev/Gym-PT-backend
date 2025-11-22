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
                .cors(Customizer.withDefaults())
                .csrf(csrf -> csrf.disable())
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        // Preflight
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()

                        // 로그인/토큰 관련 엔드포인트는 누구나 접근
                        .requestMatchers("/api/auth/**").permitAll()

                        // 내 정보 조회/수정은 인증 필요
                        .requestMatchers("/api/users/me").authenticated()

                        // 일반적으로 /api/**는 JWT 필요하게
                        .requestMatchers("/api/**").authenticated()

                        // 나머지는 일단 허용 (필요하면 나중에 조이기)
                        .anyRequest().permitAll()
                )
                // ✅ JWT 필터를 UsernamePasswordAuthenticationFilter 앞에 둔다
                .addFilterBefore(new SkippableJwtFilter(jwtService), UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    // JwtAuthFilter를 상속해서, 특정 경로는 그냥 스킵할 수 있게 함
    static class SkippableJwtFilter extends JwtAuthFilter {
        public SkippableJwtFilter(JwtService jwtService) {
            super(jwtService);
        }

        @Override
        protected boolean shouldNotFilter(HttpServletRequest request) {
            String p = request.getRequestURI();
            // 🔹 여기서 "JWT 검사 건너뛸" 경로만 적어준다
            //    /api/auth/** → 로그인/토큰 발급/리프레시 같은 곳
            if (p.startsWith("/api/auth/")) {
                return true;
            }
            // 그 외 /api/** 는 전부 JWT 필터 타게 함
            return false;
        }

        @Override
        protected void doFilterInternal(HttpServletRequest req,
                                        HttpServletResponse res,
                                        FilterChain chain)
                throws java.io.IOException, jakarta.servlet.ServletException {
            // 부모 JwtAuthFilter의 로직 그대로 사용
            super.doFilterInternal(req, res, chain);
        }
    }
}
