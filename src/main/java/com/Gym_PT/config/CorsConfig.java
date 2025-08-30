package com.Gym_PT.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration
public class CorsConfig {

    // 예: application.properties
    // app.cors.allowed-origins=http://localhost:8081,http://localhost:19006,http://localhost:5173
    @Value("${app.cors.allowed-origins:http://localhost:8081,http://localhost:19006}")
    private String allowedOrigins;

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration c = new CorsConfiguration();

        // ✅ 허용 오리진(패턴) 등록
        for (String origin : allowedOrigins.split(",")) {
            String o = origin.trim();
            if (!o.isEmpty()) c.addAllowedOriginPattern(o);
        }

        // ✅ 헤더/메서드/쿠키
        c.addAllowedHeader("*");
        c.addAllowedMethod("*");
        c.setAllowCredentials(true);

        // (선택) 노출 헤더가 필요하면 추가 (예: Authorization)
        // c.addExposedHeader("Authorization");

        UrlBasedCorsConfigurationSource s = new UrlBasedCorsConfigurationSource();
        s.registerCorsConfiguration("/**", c);
        return s;
    }
}