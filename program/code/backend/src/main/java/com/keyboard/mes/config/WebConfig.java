package com.keyboard.mes.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Web 层通用配置。
 *
 * <p>为前后端分离联调开放跨域访问，允许前端开发服务器调用 /api 下的后端接口。</p>
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Value("${app.cors.allowed-origins:http://localhost:5173,http://127.0.0.1:5173,http://localhost:3000}")
    private String allowedOrigins;

    private final AuthInterceptor authInterceptor;

    public WebConfig(AuthInterceptor authInterceptor) {
        this.authInterceptor = authInterceptor;
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")
                .allowedOriginPatterns(allowedOrigins.split(","))
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(true)
                .maxAge(3600);
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(authInterceptor)
                .addPathPatterns(
                        "/api/**",
                        "/sysUser/**",
                        "/productModel/**",
                        "/material/**",
                        "/productBom/**",
                        "/processRoute/**",
                        "/productionOrder/**",
                        "/productionTask/**",
                        "/workReport/**",
                        "/inspectionRecord/**",
                        "/reworkOrder/**")
                .excludePathPatterns(
                        "/api/auth/login",
                        "/api/auth/logout",
                        "/api/auth/current",
                        "/api/auth/unauthenticated",
                        "/api/health",
                        "/api/endpoints",
                        "/api-contract.json",
                        "/error");
    }
}
