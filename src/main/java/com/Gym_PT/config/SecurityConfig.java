package com.Gym_PT.config;

import com.Gym_PT.auth.JwtAuthFilter;
import com.Gym_PT.auth.JwtService;
import jakarta.servlet.http.HttpServletRequest;
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

                        // 🔓 인증 없이 열어둘 엔드포인트들
                        .requestMatchers("/api/auth/**").permitAll()
                        .requestMatchers("/api/gpt/**").permitAll()  // 🔥 GPT 루틴/식단 공개

                        // 🔒 인증 필요한 엔드포인트들
                        .requestMatchers("/api/users/me").authenticated()
                        .requestMatchers("/api/**").authenticated()

                        // 그 외는 허용
                        .anyRequest().permitAll()
                )
                .addFilterBefore(new SkippableJwtFilter(jwtService), UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    static class SkippableJwtFilter extends JwtAuthFilter {
        public SkippableJwtFilter(JwtService jwtService) {
            super(jwtService);
        }

        @Override
        protected boolean shouldNotFilter(HttpServletRequest request) {
            // auth 관련 엔드포인트는 JWT 필터 스킵
            return request.getRequestURI().startsWith("/api/auth/");
        }
    }
}
