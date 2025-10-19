package com.example.demo.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import java.util.Date;

/**
 * Represents a single message within a conversation.
 * Can be sent by either a customer (user) or an admin.
 */
@Entity
public class MessageEntry {

    /** Primary key: Unique identifier for each message entry. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * The conversation this message belongs to.
     * Uses @JsonBackReference to avoid circular references during JSON serialization.
     */
    @ManyToOne
    @JsonBackReference
    private Conversation conversation;

    /** The account (user or admin) who sent this message. */
    @ManyToOne
    private Account sender;

    /** The text content of the message. Stored as long text. */
    @Column(columnDefinition = "TEXT")
    private String content;

    /** Timestamp when the message was sent. */
    @Temporal(TemporalType.TIMESTAMP)
    private Date sentAt;

    /** Flag indicating if this message was sent by an admin. */
    private boolean fromAdmin;

    /** Flag indicating if this message has been read by an admin. */
    private boolean readByAdmin = false;

    // ---------- Getters and Setters ----------

    /** Gets the message entry ID. */
    public Long getId() {
        return id;
    }

    /** Gets the conversation linked to this message. */
    public Conversation getConversation() {
        return conversation;
    }

    /** Sets the conversation linked to this message. */
    public void setConversation(Conversation conversation) {
        this.conversation = conversation;
    }

    /** Gets the sender (account) of the message. */
    public Account getSender() {
        return sender;
    }

    /** Sets the sender (account) of the message. */
    public void setSender(Account sender) {
        this.sender = sender;
    }

    /** Gets the text content of the message. */
    public String getContent() {
        return content;
    }

    /** Sets the text content of the message. */
    public void setContent(String content) {
        this.content = content;
    }

    /** Gets the timestamp when the message was sent. */
    public Date getSentAt() {
        return sentAt;
    }

    /** Sets the timestamp when the message was sent. */
    public void setSentAt(Date sentAt) {
        this.sentAt = sentAt;
    }

    /** Checks if the message was sent by an admin. */
    public boolean isFromAdmin() {
        return fromAdmin;
    }

    /** Sets whether the message was sent by an admin. */
    public void setFromAdmin(boolean fromAdmin) {
        this.fromAdmin = fromAdmin;
    }

    /** Checks if the message has been read by an admin. */
    public boolean isReadByAdmin() {
        return readByAdmin;
    }

    /** Sets whether the message has been read by an admin. */
    public void setReadByAdmin(boolean readByAdmin) {
        this.readByAdmin = readByAdmin;
    }
}
