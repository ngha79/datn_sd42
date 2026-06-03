package com.base.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Value("${app.upload.temp-dir}")
    private String tempDir;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // Map URL /uploads/temp/** > thư mục thực trên disk
        registry.addResourceHandler("/" + tempDir + "/**")
                .addResourceLocations("file:" + tempDir + "/");
    }
}