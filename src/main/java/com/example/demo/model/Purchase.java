package com.example.demo.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents a purchase/order made by a user.
 * Stores checkout details, total amount, purchase date, and status.
 */
@Entity
@Table(name = "purchase")
public class Purchase {

    /** Full name provided during checkout. */
    @Column(name = "full_name")
    private String fullName;

    /** Shipping or billing address provided during checkout. */
    @Column(name = "address")
    private String address;

    /** Phone number provided by the customer during checkout. */
    @Column(name = "phone_number")
    private String phoneNumber;

    /** Payment method selected by the customer. */
    @Column(name = "payment_method")
    private String paymentMethod;

    /** Primary key: Unique identifier for each purchase. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** The account (user) who made this purchase. Cannot be null. */
    @ManyToOne(optional = false)
    private Account account;

    /**
     * List of line items included in this purchase.
     * Cascade type ALL ensures line items are saved/deleted with the purchase.
     * Orphan removal deletes line items removed from the list.
     */
    @OneToMany(mappedBy = "purchase", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<LineItem> lineItems = new ArrayList<>();

    /** The date and time when the purchase was created. Defaults to the current time. */
    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();

    /** Status of the purchase (e.g., pending, shipped, delivered). */
    @ManyToOne
    @JoinColumn(name = "status_id")
    private StatusType status;

    /** Total amount for the purchase. Precision up to 99999999.99. */
    @Column(name = "total", precision = 10, scale = 2)
    private BigDecimal total;

    /** Default constructor required by JPA. */
    public Purchase() {}

    /**
     * Constructor for creating a purchase with basic details.
     *
     * @param id         Purchase ID.
     * @param account    Account making the purchase.
     * @param createdAt  Date and time when the purchase was created.
     * @param status     Status of the purchase.
     */
    public Purchase(Long id, Account account, LocalDateTime createdAt, StatusType status) {
        this.id = id;
        this.account = account;
        this.createdAt = createdAt;
        this.status = status;
    }

    // ---------- Getters & Setters ----------

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Account getAccount() { return account; }
    public void setAccount(Account account) { this.account = account; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public StatusType getStatus() { return status; }
    public void setStatus(StatusType status) { this.status = status; }

    public List<LineItem> getLineItems() { return lineItems; }
    public void setLineItems(List<LineItem> lineItems) { this.lineItems = lineItems; }

    public BigDecimal getTotal() { return total; }
    public void setTotal(BigDecimal total) { this.total = total; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public String getPhoneNumber() { return phoneNumber; }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }

    public String getPaymentMethod() { return paymentMethod; }
    public void setPaymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; }
}
