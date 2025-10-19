package com.example.demo.model;

import jakarta.persistence.*;
import java.util.Date;

/**
 * Represents a message exchanged between a user and admin in the system.
 * Supports admin replies, customer follow-ups, and tracking update timestamps.
 */
@Entity
public class Message {

    /** Admin reply content (optional). Stored as long text. */
    @Column(columnDefinition = "TEXT")
    private String adminReply;

    /** Customer follow-up reply to the admin (optional). Stored as long text. */
    @Column(columnDefinition = "TEXT")
    private String customerFollowUp;

    /** Timestamp for the last update on this message. */
    @Temporal(TemporalType.TIMESTAMP)
    private Date lastUpdated;

    /** Primary key: Unique identifier for each message. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** The account (user) who sent this message. */
    @ManyToOne
    private Account sender;

    /** The main content of the message. Cannot be null. Stored as long text. */
    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    /** Timestamp when the message was originally sent. */
    @Temporal(TemporalType.TIMESTAMP)
    private Date sentAt;

    /** Flag indicating if the message has been answered by admin. */
    private boolean answered = false;

    /** Admin reply content (alternative field). Stored as long text. */
    @Column(columnDefinition = "TEXT")
    private String reply;

    // ---------- Getters and Setters ----------

    /** Gets the message ID. */
    public Long getId() {
        return id;
    }

    /** Gets the sender (user) of the message. */
    public Account getSender() {
        return sender;
    }

    /** Sets the sender (user) of the message. */
    public void setSender(Account sender) {
        this.sender = sender;
    }

    /** Gets the main content of the message. */
    public String getContent() {
        return content;
    }

    /** Sets the main content of the message. */
    public void setContent(String content) {
        this.content = content;
    }

    /** Gets the sent timestamp of the message. */
    public Date getSentAt() {
        return sentAt;
    }

    /** Sets the sent timestamp of the message. */
    public void setSentAt(Date sentAt) {
        this.sentAt = sentAt;
    }

    /** Checks if the message has been answered. */
    public boolean isAnswered() {
        return answered;
    }

    /** Sets whether the message has been answered. */
    public void setAnswered(boolean answered) {
        this.answered = answered;
    }

    /** Gets the admin reply text. */
    public String getReply() {
        return reply;
    }

    /** Sets the admin reply text. */
    public void setReply(String reply) {
        this.reply = reply;
    }

    /** Gets the customer follow-up reply. */
    public String getCustomerFollowUp() {
        return customerFollowUp;
    }

    /** Sets the customer follow-up reply. */
    public void setCustomerFollowUp(String customerFollowUp) {
        this.customerFollowUp = customerFollowUp;
    }

    /** Gets the last updated timestamp of the message. */
    public Date getLastUpdated() {
        return lastUpdated;
    }

    /** Sets the last updated timestamp of the message. */
    public void setLastUpdated(Date lastUpdated) {
        this.lastUpdated = lastUpdated;
    }

    /** Gets the admin reply content. */
    public String getAdminReply() {
        return adminReply;
    }

    /** Sets the admin reply content. */
    public void setAdminReply(String adminReply) {
        this.adminReply = adminReply;
    }
}
