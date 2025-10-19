package com.example.demo.model;

import jakarta.persistence.*;

/**
 * Represents a cart item stored in the database, linked to a specific user (Account) and Product.
 * Maps to the 'cart_item' table.
 */
@Entity
@Table(name = "cart_item")
public class CartItemEntity {

    /** Primary key: Unique ID for each cart item record. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** The account (user) associated with this cart item. Cannot be null. */
    @ManyToOne(optional = false)
    private Account account;

    /** The product associated with this cart item. Cannot be null. */
    @ManyToOne(optional = false)
    private Product product;

    /** Quantity of the product added to the cart. */
    private int quantity;

    /** Optional comment field for user notes, max 255 characters. */
    @Column(length = 255)
    private String comment;

    /** Default constructor required by JPA. */
    public CartItemEntity() {}

    /**
     * Constructor to create a new CartItemEntity.
     *
     * @param account  The account owning this cart item.
     * @param product  The product added to the cart.
     * @param quantity The quantity of the product.
     * @param comment  Optional comment about the cart item.
     */
    public CartItemEntity(Account account, Product product, int quantity, String comment) {
        this.account = account;
        this.product = product;
        this.quantity = quantity;
        this.comment = comment;
    }

    /** Gets the cart item ID. */
    public Long getId() { return id; }

    /** Gets the account associated with this cart item. */
    public Account getAccount() { return account; }

    /** Sets the account associated with this cart item. */
    public void setAccount(Account account) { this.account = account; }

    /** Gets the product associated with this cart item. */
    public Product getProduct() { return product; }

    /** Sets the product associated with this cart item. */
    public void setProduct(Product product) { this.product = product; }

    /** Gets the quantity of the product. */
    public int getQuantity() { return quantity; }

    /** Sets the quantity of the product. */
    public void setQuantity(int quantity) { this.quantity = quantity; }

    /** Gets the optional user comment. */
    public String getComment() { return comment; }

    /** Sets the optional user comment. */
    public void setComment(String comment) { this.comment = comment; }
}
