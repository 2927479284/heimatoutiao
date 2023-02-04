package com.heima.article.config;

import com.heima.common.interceptor.TokenInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new TokenInterceptor()).addPathPatterns("/**")
                .excludePathPatterns("/api/v1/article/load/**",
                        "/api/v1/article/loadmore/**",
                        "/api/v1/article/loadnew/**",
                        "/api/v1/article/save")
                //放行swagger和knife4j
                .excludePathPatterns( "/v2/api-docs",
                        "/doc.html",
                        "/swagger-resources/configuration/ui",
                        "/swagger-resources",
                        "/swagger-resources/configuration/security",
                        "/swagger-ui.html",
                        "/webjars/**",
                        "/actuator/**");
    }
}