package com.example.demo.model;

import java.math.BigDecimal;

/**
 * Represents an item in the shopping cart.
 * Contains product details, quantity, and an optional comment.
 */
public class CartItem {

    /** The product associated with this cart item. */
    private Product product;

    /** The quantity of the product added to the cart. */
    private int quantity;

    /** Optional field for user notes (can be null). */
    private String comment;

    /** Default constructor. Required for frameworks like Spring MVC. */
    public CartItem() {
    }

    /**
     * Constructor to create a cart item with product, quantity, and comment.
     *
     * @param product The product associated with this item.
     * @param quantity The quantity of the product.
     * @param comment Optional comment from the user.
     */
    public CartItem(Product product, int quantity, String comment) {
        this.product = product;
        this.quantity = quantity;
        this.comment = comment;
    }

    /** Gets the product associated with this cart item. */
    public Product getProduct() {
        return product;
    }

    /** Sets the product associated with this cart item. */
    public void setProduct(Product product) {
        this.product = product;
    }

    /** Gets the quantity of the product. */
    public int getQuantity() {
        return quantity;
    }

    /** Sets the quantity of the product. */
    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    /** Gets the optional user comment. */
    public String getComment() {
        return comment;
    }

    /** Sets the optional user comment. */
    public void setComment(String comment) {
        this.comment = comment;
    }

    /**
     * Calculates the total price for this cart item.
     * Multiplies the product's price by the quantity.
     *
     * @return Total price as BigDecimal.
     */
    public BigDecimal getTotalPrice() {
        return product.getPrice().multiply(BigDecimal.valueOf(quantity));
    }
}
