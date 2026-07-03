package com.devon.flashsale.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Path;
import java.nio.file.Paths;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Value("${app.storage.location}")
    private String storageLocation;
    
    @Value("${app.storage.public.path}")
	private String publicPath;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        Path uploadPath = Paths.get(storageLocation).toAbsolutePath();
        registry.addResourceHandler(publicPath+"/**")
                .addResourceLocations(uploadPath.toUri().toString());
    }
}
