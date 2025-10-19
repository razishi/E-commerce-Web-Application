package com.example.demo.model;

import jakarta.persistence.*;

/**
 * Represents a favorite product marked by a specific user (account).
 * Links an account to a product.
 */
@Entity
public class Favorite {

    /** Primary key: Unique identifier for each favorite record. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** The account (user) who marked this product as favorite. Cannot be null. */
    @ManyToOne(optional = false)
    private Account account;

    /** The product marked as favorite. Cannot be null. */
    @ManyToOne(optional = false)
    private Product product;

    /** Default constructor required by JPA. */
    public Favorite() {}

    /**
     * Constructor for convenience when creating a Favorite manually.
     *
     * @param account The account who favorites the product.
     * @param product The product being favorited.
     */
    public Favorite(Account account, Product product) {
        this.account = account;
        this.product = product;
    }

    /** Gets the favorite record ID. */
    public Long getId() {
        return id;
    }

    /** Gets the account linked to this favorite. */
    public Account getAccount() {
        return account;
    }

    /** Sets the account linked to this favorite. */
    public void setAccount(Account account) {
        this.account = account;
    }

    /** Gets the product linked to this favorite. */
    public Product getProduct() {
        return product;
    }

    /** Sets the product linked to this favorite. */
    public void setProduct(Product product) {
        this.product = product;
    }
}
