package com.hospital.hms.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    private final SessionAuthInterceptor sessionAuthInterceptor;
    private final MvcAuthInterceptor mvcAuthInterceptor;
    private final MvcRoleInterceptor mvcRoleInterceptor;
    private final PresenceInterceptor presenceInterceptor;
    private final AuditInterceptor auditInterceptor;

    public WebConfig(SessionAuthInterceptor sessionAuthInterceptor,
            MvcAuthInterceptor mvcAuthInterceptor,
            MvcRoleInterceptor mvcRoleInterceptor,
            PresenceInterceptor presenceInterceptor,
            AuditInterceptor auditInterceptor) {
        this.sessionAuthInterceptor = sessionAuthInterceptor;
        this.mvcAuthInterceptor = mvcAuthInterceptor;
        this.mvcRoleInterceptor = mvcRoleInterceptor;
        this.presenceInterceptor = presenceInterceptor;
        this.auditInterceptor = auditInterceptor;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(sessionAuthInterceptor)
                .addPathPatterns("/api/**")
                .excludePathPatterns("/api/auth/login", "/api/auth/register");
        registry.addInterceptor(mvcAuthInterceptor)
                .addPathPatterns("/app/**");
        registry.addInterceptor(mvcRoleInterceptor)
                .addPathPatterns("/app/**");
        registry.addInterceptor(presenceInterceptor)
                .addPathPatterns("/api/**", "/app/**")
                .excludePathPatterns("/api/auth/login", "/api/auth/register", "/login", "/register", "/");
        registry.addInterceptor(auditInterceptor)
                .addPathPatterns("/api/**", "/app/**")
                .excludePathPatterns("/api/auth/login", "/api/auth/register");
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")
                .allowedOriginPatterns("*")
                .allowedMethods("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS")
                .allowCredentials(true);
    }
}
