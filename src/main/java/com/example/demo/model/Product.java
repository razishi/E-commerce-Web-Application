package com.example.demo.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.util.List;

/**
 * Represents a product in the e-commerce system.
 * Maps to the 'product' table in the database.
 */
@Entity
@Table(name = "product")
public class Product {

    /** View-only flag: Indicates if the product has been reviewed by the user. Not stored in the database. */
    @Transient
    private boolean reviewed;

    /** Gets whether the product is marked as reviewed. */
    public boolean isReviewed() {
        return reviewed;
    }

    /** Sets whether the product is marked as reviewed. */
    public void setReviewed(boolean reviewed) {
        this.reviewed = reviewed;
    }

    /** Primary key: Unique product ID. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Product name. Cannot be null. Maximum length: 150 characters. */
    @Column(nullable = false, length = 150)
    private String name;

    /** Detailed product description. Stored as long text. */
    @Column(columnDefinition = "TEXT")
    private String description;

    /** Product price with precision and scale (e.g., 99999999.99 max). */
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    /** URL to the product image. */
    @Column(name = "image_url")
    private String imageUrl;

    /** Category this product belongs to. */
    @ManyToOne
    @JoinColumn(name = "category_id")
    private Category category;

    /** Product stock quantity. Defaults to 0 if not set. */
    @Column(nullable = false)
    private int stock = 0;

    /** View-only flag: Indicates if the product is marked as a favorite by the current user. Not stored in the database. */
    @Transient
    private boolean favorite;

    /** Default constructor required by JPA. */
    public Product() {}

    /**
     * Constructor for creating a product with essential details.
     *
     * @param id          Product ID.
     * @param name        Product name.
     * @param description Product description.
     * @param price       Product price.
     * @param imageUrl    Product image URL.
     * @param category    Product category.
     */
    public Product(Long id, String name, String description,
                   BigDecimal price, String imageUrl, Category category) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.price = price;
        this.imageUrl = imageUrl;
        this.category = category;
    }

    /**
     * List of reviews associated with this product.
     * Cascade type ALL ensures reviews are saved/deleted with the product.
     * Orphan removal deletes reviews removed from the list.
     */
    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Review> reviews;

    /** Gets the list of reviews for this product. */
    public List<Review> getReviews() {
        return reviews;
    }

    /** Sets the list of reviews for this product. */
    public void setReviews(List<Review> reviews) {
        this.reviews = reviews;
    }

    /** Adds a review to this product. */
    public void addReview(Review review) {
        reviews.add(review);
        review.setProduct(this);
    }

    /** Removes a review from this product. */
    public void removeReview(Review review) {
        reviews.remove(review);
        review.setProduct(null);
    }

    // ---------- Standard Getters and Setters ----------

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    public Category getCategory() { return category; }
    public void setCategory(Category category) { this.category = category; }

    public int getStock() { return stock; }
    public void setStock(int stock) { this.stock = stock; }

    public boolean isFavorite() { return favorite; }
    public void setFavorite(boolean favorite) { this.favorite = favorite; }
}
