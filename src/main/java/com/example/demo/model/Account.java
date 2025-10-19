package com.example.demo.model;

import jakarta.persistence.*;
import java.util.Date;

/**
 * Represents a user account in the system.
 * Maps to the 'account' table in the database.
 */
@Entity
@Table(name = "account")
public class Account {

    /** Primary key: Unique account ID, generated automatically. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Unique username used for login. */
    @Column(nullable = false, unique = true)
    private String username;

    /** Unique email address, max length 120 characters. */
    @Column(nullable = false, unique = true, length = 120)
    private String email;

    /** Hashed password (stored securely). */
    @Column(nullable = false, length = 60)
    private String passwordHash;

    /** User role, e.g., ROLE_USER or ROLE_ADMIN. */
    @Column(nullable = false, length = 20)
    private String role;

    /** Indicates if the account is enabled or disabled. */
    private boolean enabled = true;

    /** Plain password (not stored in DB), used temporarily during form submissions. */
    @Transient
    private String password;

    /** User's first name. */
    @Column(name = "first_name", nullable = false)
    private String firstName;

    /** User's last name. */
    @Column(name = "last_name", nullable = false)
    private String lastName;

    /** Account creation date. Automatically set when the account is first saved. */
    @Column(name = "created_at")
    @Temporal(TemporalType.TIMESTAMP)
    private Date createdAt;

    /**
     * Sets the createdAt field automatically before persisting if it's null.
     */
    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = new Date();
        }
    }

    /** Default constructor for JPA. */
    public Account() {}

    /**
     * Constructor with key fields for programmatic account creation.
     */
    public Account(Long id, String username, String email, String passwordHash,
                   String role, boolean enabled) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.passwordHash = passwordHash;
        this.role = role;
        this.enabled = enabled;
    }

    // ---------- Getters & Setters ----------

    /** Gets the account ID. */
    public Long getId() { return id; }

    /** Sets the account ID. */
    public void setId(Long id) { this.id = id; }

    /** Gets the username. */
    public String getUsername() { return username; }

    /** Sets the username. */
    public void setUsername(String username) { this.username = username; }

    /** Gets the email. */
    public String getEmail() { return email; }

    /** Sets the email. */
    public void setEmail(String email) { this.email = email; }

    /** Gets the password hash. */
    public String getPasswordHash() { return passwordHash; }

    /** Sets the password hash. */
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }

    /** Gets the role. */
    public String getRole() { return role; }

    /** Sets the role. */
    public void setRole(String role) { this.role = role; }

    /** Checks if the account is enabled. */
    public boolean isEnabled() { return enabled; }

    /** Sets whether the account is enabled. */
    public void setEnabled(boolean enabled) { this.enabled = enabled; }

    /** Gets the first name. */
    public String getFirstName() { return firstName; }

    /** Sets the first name. */
    public void setFirstName(String firstName) { this.firstName = firstName; }

    /** Gets the last name. */
    public String getLastName() { return lastName; }

    /** Sets the last name. */
    public void setLastName(String lastName) { this.lastName = lastName; }

    /** Gets the plain password (used temporarily in forms). */
    public String getPassword() { return password; }

    /** Sets the plain password (used temporarily in forms). */
    public void setPassword(String password) { this.password = password; }

    /** Gets the creation date. */
    public Date getCreatedAt() { return createdAt; }

    /** Sets the creation date. */
    public void setCreatedAt(Date createdAt) { this.createdAt = createdAt; }
}
