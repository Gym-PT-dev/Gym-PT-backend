// com/Gym_PT/config/SecurityConfig.java
package com.Gym_PT.config;

import com.Gym_PT.auth.JwtAuthFilter;
import com.Gym_PT.auth.JwtService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
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
    @Order(0)
    SecurityFilterChain api(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        // 프로필 API는 로그인(인증)만 요구
                        .requestMatchers(HttpMethod.PUT, "/users/*/profile").authenticated()
                        .requestMatchers(HttpMethod.GET, "/users/*/profile").authenticated()
                        .anyRequest().permitAll()
                )
                // ✅ JWT 필터 등록 (UsernamePasswordAuthenticationFilter 앞)
                .addFilterBefore(new JwtAuthFilter(jwtService), UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
