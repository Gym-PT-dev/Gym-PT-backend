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
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        .requestMatchers("/api/auth/**").permitAll()
                        .requestMatchers(HttpMethod.PUT, "/users/*/profile").authenticated()
                        .requestMatchers(HttpMethod.GET, "/users/*/profile").authenticated()
                        .anyRequest().permitAll()
                )
                .addFilterBefore(new SkippableJwtFilter(jwtService), UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    static class SkippableJwtFilter extends JwtAuthFilter {
        public SkippableJwtFilter(JwtService jwtService) { super(jwtService); }
        @Override
        protected boolean shouldNotFilter(HttpServletRequest request) {
            String p = request.getRequestURI();
            return p.startsWith("/api/auth/");
        }
        @Override
        protected void doFilterInternal(HttpServletRequest req, HttpServletResponse res, FilterChain chain)
                throws java.io.IOException, jakarta.servlet.ServletException {
            super.doFilterInternal(req, res, chain);
        }
    }
}
