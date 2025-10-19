package com.example.demo.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * Represents a status type for purchases (e.g., Pending, Shipped, Delivered).
 * Maps to the 'status_types' table in the database.
 * Uses Lombok to automatically generate getters and setters.
 */
@Entity
@Table(name = "status_types")
@Getter
@Setter
public class StatusType {

    /** Primary key: Unique identifier for each status type. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Status label (e.g., "Pending", "Completed"). Must be unique and not null. */
    @Column(nullable = false, unique = true)
    private String label;

    /**
     * List of purchases associated with this status.
     * Cascade type ALL ensures related purchases can be managed with the status.
     */
    @OneToMany(mappedBy = "status", cascade = CascadeType.ALL)
    private List<Purchase> purchases;

    /** Default no-argument constructor required by JPA. */
    public StatusType() {}

    /**
     * Constructor for creating a new StatusType with a label.
     *
     * @param label Status label name.
     */
    public StatusType(String label) {
        this.label = label;
    }
}
