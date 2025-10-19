package com.example.demo.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Represents a product review left by a customer.
 * Links a product, account (user), and optionally a purchase.
 */
@Entity
@Table(name = "review")
public class Review {

    /** Primary key: Unique identifier for each review. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** The product that this review is for. Cannot be null. */
    @ManyToOne(optional = false)
    @JoinColumn(name = "product_id")
    private Product product;

    /** The account (customer) who wrote the review. Cannot be null. */
    @ManyToOne(optional = false)
    @JoinColumn(name = "account_id")
    private Account account;

    /** The purchase linked to this review (optional). Helps validate genuine reviews. */
    @ManyToOne(optional = true)
    @JoinColumn(name = "purchase_id")
    private Purchase purchase;

    /** Star rating given by the user. Value between 1–5. */
    @Column(nullable = false)
    private int rating;

    /** The comment text provided by the user. Stored as long text. */
    @Column(columnDefinition = "TEXT")
    private String comment;

    /** Timestamp when the review was created. Automatically set on creation. */
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    /** Default constructor required by JPA. */
    public Review() {}

    /**
     * Constructor for creating a new review.
     *
     * @param product  The product being reviewed.
     * @param account  The user writing the review.
     * @param purchase Optional purchase linked to this review.
     * @param rating   Star rating (1–5).
     * @param comment  Review comment text.
     */
    public Review(Product product, Account account, Purchase purchase, int rating, String comment) {
        this.product = product;
        this.account = account;
        this.purchase = purchase;
        this.rating = rating;
        this.comment = comment;
    }

    /** Automatically sets createdAt before persisting if it is not already set. */
    @PrePersist
    public void prePersist() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }

    // ---------- Getters and Setters ----------

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Product getProduct() { return product; }
    public void setProduct(Product product) { this.product = product; }

    public Account getAccount() { return account; }
    public void setAccount(Account account) { this.account = account; }

    public Purchase getPurchase() { return purchase; }
    public void setPurchase(Purchase purchase) { this.purchase = purchase; }

    public int getRating() { return rating; }
    public void setRating(int rating) { this.rating = rating; }

    public String getComment() { return comment; }
    public void setComment(String comment) { this.comment = comment; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
