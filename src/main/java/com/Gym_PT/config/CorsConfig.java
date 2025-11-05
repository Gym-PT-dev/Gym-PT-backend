package com.Gym_PT.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

@Configuration
public class CorsConfig {

    // 예) application.properties: app.cors.allowed-origins=http://localhost:8081
    @Value("${app.cors.allowed-origins:http://localhost:8081}")
    private String allowedOrigins;

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration c = new CorsConfiguration();

        // ✅ 정확한 오리진 등록 (패턴 X)
        for (String origin : allowedOrigins.split(",")) {
            String o = origin.trim();
            if (!o.isEmpty()) c.addAllowedOrigin(o);
        }

        // ✅ 허용 헤더/메서드
        c.addAllowedHeader("*");
        c.addAllowedMethod("*");

        // ✅ 현재 구조(본문으로 토큰 주고받음): 쿠키 미사용 → false
        c.setAllowCredentials(false);

        // (필요 시) 노출 헤더
        // c.addExposedHeader("Authorization");

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", c);

        // 디버깅용 (배포 로그에서 값 확인)
        System.out.println("[CORS] allowedOrigins = " + allowedOrigins);

        return source;
    }

    // ✅ 필터 순서 최우선으로 등록 (프리플라이트/본요청에 헤더 강제 부착)
    @Bean
    public FilterRegistrationBean<CorsFilter> corsFilterRegistration(CorsConfigurationSource source) {
        FilterRegistrationBean<CorsFilter> bean = new FilterRegistrationBean<>(new CorsFilter(source));
        bean.setOrder(Ordered.HIGHEST_PRECEDENCE);
        return bean;
    }
}
