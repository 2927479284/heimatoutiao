package com.heima.wemedia.config;


import com.heima.common.interceptor.TokenInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        /**
         一个*：只匹配字符，不匹配路径（/）
         两个**：匹配字符，和路径（/）

         举例子：
         - /**： 匹配所有路径
         - /admin/**：匹配 /admin/ 下的所有路径
         - /secure/*：只匹配 /secure/user，不匹配 /secure/user/info
         */
        registry.addInterceptor(new TokenInterceptor()).addPathPatterns("/**")
                //放行路径1：一般在这里放行的是登录路径和feign接口路径
                .excludePathPatterns("/login/**","/api/v1/channel/listAll")
                //放行路径2：还需要放行swagger和knife4j，否则这俩技术对应的API页面无法正常访问
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
