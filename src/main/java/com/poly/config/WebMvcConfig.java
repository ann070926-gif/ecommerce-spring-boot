package com.poly.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {

        String userDir = System.getProperty("user.dir").replace("\\", "/");

        registry.addResourceHandler("/images/**")
                .addResourceLocations("file:///" + userDir + "/src/main/resources/static/images/");
    }
}
