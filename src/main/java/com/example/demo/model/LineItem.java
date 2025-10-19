package com.example.demo.model;

import jakarta.persistence.*;
import java.math.BigDecimal;

/**
 * Represents a single line item in a purchase.
 * Maps to the 'line_item' table in the database.
 * Each line item is linked to one purchase and one product.
 */
@Entity
@Table(name = "line_item")
public class LineItem {

    /** Primary key: Unique identifier for each line item. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * The purchase this line item belongs to.
     * Many line items can be part of one purchase.
     * Lazy fetching to avoid unnecessary data loading.
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "purchase_id")
    private Purchase purchase;

    /**
     * The product associated with this line item.
     * Eager fetching ensures product details are loaded immediately with the line item.
     */
    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "product_id")
    private Product product;

    /** Quantity of the product purchased. Cannot be null. */
    @Column(nullable = false)
    private int quantity;

    /**
     * Price per unit at the time of purchase.
     * Precision up to 10 digits with 2 decimal places.
     */
    @Column(name = "unit_price", precision = 10, scale = 2, nullable = false)
    private BigDecimal unitPrice;

    /**
     * Optional comment or engraving text linked to this line item.
     * Can be used for custom notes.
     */
    @Column(name = "comment", length = 255)
    private String engravingText;

    /** Default constructor required by JPA. */
    public LineItem() {}

    /**
     * Constructor to create a line item with all required fields.
     *
     * @param purchase        The purchase this item belongs to.
     * @param product         The product being purchased.
     * @param quantity        Quantity of the product.
     * @param unitPrice       Price per unit of the product.
     * @param engravingText   Optional engraving text or comment.
     */
    public LineItem(Purchase purchase, Product product, int quantity, BigDecimal unitPrice, String engravingText) {
        this.purchase = purchase;
        this.product = product;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
        this.engravingText = engravingText;
    }

    // ---------- Getters and Setters ----------

    /** Gets the line item ID. */
    public Long getId() { return id; }

    /** Sets the line item ID. */
    public void setId(Long id) { this.id = id; }

    /** Gets the purchase associated with this line item. */
    public Purchase getPurchase() { return purchase; }

    /** Sets the purchase associated with this line item. */
    public void setPurchase(Purchase purchase) { this.purchase = purchase; }

    /** Gets the product associated with this line item. */
    public Product getProduct() { return product; }

    /** Sets the product associated with this line item. */
    public void setProduct(Product product) { this.product = product; }

    /** Gets the quantity of the product in this line item. */
    public int getQuantity() { return quantity; }

    /** Sets the quantity of the product in this line item. */
    public void setQuantity(int quantity) { this.quantity = quantity; }

    /** Gets the unit price for this product at purchase time. */
    public BigDecimal getUnitPrice() { return unitPrice; }

    /** Sets the unit price for this product at purchase time. */
    public void setUnitPrice(BigDecimal unitPrice) { this.unitPrice = unitPrice; }

    /** Gets the optional engraving text or comment. */
    public String getEngravingText() { return engravingText; }

    /** Sets the optional engraving text or comment. */
    public void setEngravingText(String engravingText) { this.engravingText = engravingText; }
}
