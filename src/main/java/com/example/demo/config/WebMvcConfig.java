package com.example.demo.config;        // ← keep the package line you already have

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Web MVC configuration class for simple, non-controller-based view redirections.
 */
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    /**
     * This method adds view controllers for simple URL-to-URL redirects.
     *
     * In this case, it redirects the root path "/" to "/products",
     * so when a user visits http://localhost:8080/, they are forwarded to /products.
     */
    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        registry.addRedirectViewController("/", "/products");
    }
}
