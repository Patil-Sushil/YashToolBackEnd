package com.kalibyte.YashTools.common.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.thymeleaf.spring6.SpringTemplateEngine;
import org.thymeleaf.templatemode.TemplateMode;
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver;

@Configuration
public class ThymeleafPdfConfig {

    @Bean(name = "pdfTemplateEngine")
    public SpringTemplateEngine pdfTemplateEngine() {
        SpringTemplateEngine engine = new SpringTemplateEngine();
        engine.addTemplateResolver(pdfTemplateResolver());
        return engine;
    }

    @Bean(name = "pdfTemplateResolver")
    public ClassLoaderTemplateResolver pdfTemplateResolver() {
        ClassLoaderTemplateResolver resolver = new ClassLoaderTemplateResolver();
        resolver.setPrefix("templates/");
        resolver.setSuffix(".html");
        // Use XML mode for strict XHTML rendering
        resolver.setTemplateMode(TemplateMode.XML);
        resolver.setCharacterEncoding("UTF-8");
        resolver.setOrder(1);
        resolver.setCheckExistence(true);
        resolver.setCacheable(true);
        resolver.setCacheTTLMs(3600000L); // 1 hour cache
        return resolver;
    }
}