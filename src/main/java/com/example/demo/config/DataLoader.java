package com.example.demo.config;

import com.example.demo.model.Category;
import com.example.demo.repository.CategoryRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Arrays;


/**
 * Configuration class responsible for inserting default categories
 * into the database when the application starts, if none exist.
 */
@Configuration
public class DataLoader {

    /**
     * Defines a CommandLineRunner bean that runs on application startup.
     * It checks if the Category table is empty and preloads default data.
     *
     * @param repo the CategoryRepository used to interact with the database
     * @return a CommandLineRunner that inserts default categories
     */
    @Bean
    CommandLineRunner preloadCategories(CategoryRepository repo) {
        return args -> {
            // Only insert if the table is empty (to avoid duplicates on restarts)
            if (repo.count() == 0) {

                // Create a list of default categories to insert
                repo.saveAll(Arrays.asList(
                        new Category(null, "Electronics"),
                        new Category(null, "Clothes"),
                        new Category(null, "Accessories"),
                        new Category(null, "Shoes"),
                        new Category(null, "Glasses"),
                        new Category(null, "Sports"),
                        new Category(null, "Books")
                ));
                // Confirmation message in the console
                System.out.println("✅ Default categories inserted.");
            }
        };
    }
}
