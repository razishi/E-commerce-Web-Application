
package com.example.demo.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Web configuration class that maps a custom URL path ("/uploads/**")
 * to a directory on the server's file system.
 *
 * This allows accessing uploaded files (like product images) through the browser.
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    /**
     * Configures Spring MVC to serve static files from the "uploads" folder
     * in the file system when the URL starts with "/uploads/".
     *
     * Example:
     * If you upload an image to "uploads/image1.png",
     * it can be accessed at "http://localhost:8080/uploads/image1.png"
     */
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/uploads/**")
                    .addResourceLocations("file:" + System.getProperty("user.dir") + "/uploads/");
    }
}
