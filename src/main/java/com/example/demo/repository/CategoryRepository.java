package com.example.demo.repository;

import com.example.demo.model.Category;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Repository interface for managing Category entities.
 * Provides basic CRUD operations and custom query methods.
 */
public interface CategoryRepository extends JpaRepository<Category, Long> {

    /**
     * Finds a category by its unique name.
     *
     * @param name The name of the category.
     * @return The Category entity matching the given name, or null if not found.
     */
    Category findByName(String name);
}
