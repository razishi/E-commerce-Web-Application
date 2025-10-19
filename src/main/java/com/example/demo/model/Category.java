package com.example.demo.model;

import jakarta.persistence.*;
import java.util.List;

/**
 * Represents a product category in the system.
 * Maps to the 'category' table in the database.
 */
@Entity
@Table(name = "category")
public class Category {

    /** Primary key: Unique identifier for each category. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Category name. Must be unique, cannot be null, maximum length 60 characters. */
    @Column(nullable = false, unique = true, length = 60)
    private String name;

    /**
     * List of products that belong to this category.
     * One category can have many products.
     * This relationship is managed by the 'category' field in the Product entity.
     */
    @OneToMany(mappedBy = "category")
    private List<Product> products;

    /** Default constructor required by JPA. */
    public Category() { }

    /**
     * Constructor to create a Category with ID and name.
     *
     * @param id   Category ID.
     * @param name Category name.
     */
    public Category(Long id, String name) {
        this.id = id;
        this.name = name;
    }

    /** Gets the category ID. */
    public Long getId() { return id; }

    /** Sets the category ID. */
    public void setId(Long id) { this.id = id; }

    /** Gets the category name. */
    public String getName() { return name; }

    /** Sets the category name. */
    public void setName(String name) { this.name = name; }
}
