package com.example.demo.model;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import java.util.*;

/**
 * Represents a conversation between a customer and the support/admin.
 * Stores messages and the last updated timestamp.
 */
@Entity
public class Conversation {

    /** Primary key: Unique identifier for the conversation. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** The user (customer) who owns this conversation. */
    @ManyToOne
    private Account user;

    /**
     * List of message entries associated with this conversation.
     * One conversation can have many messages.
     * Cascade type ALL ensures messages are saved or deleted along with the conversation.
     */
    @OneToMany(mappedBy = "conversation", cascade = CascadeType.ALL)
    @JsonManagedReference
    private List<MessageEntry> messages = new ArrayList<>();

    /** Timestamp for the last time this conversation was updated. */
    @Temporal(TemporalType.TIMESTAMP)
    private Date lastUpdated;

    // ---------- Getters & Setters ----------

    /** Gets the conversation ID. */
    public Long getId() {
        return id;
    }

    /** Gets the user (customer) associated with this conversation. */
    public Account getUser() {
        return user;
    }

    /** Sets the user (customer) for this conversation. */
    public void setUser(Account user) {
        this.user = user;
    }

    /** Gets the list of messages in this conversation. */
    public List<MessageEntry> getMessages() {
        return messages;
    }

    /** Sets the list of messages for this conversation. */
    public void setMessages(List<MessageEntry> messages) {
        this.messages = messages;
    }

    /** Gets the last updated timestamp of the conversation. */
    public Date getLastUpdated() {
        return lastUpdated;
    }

    /** Sets the last updated timestamp of the conversation. */
    public void setLastUpdated(Date lastUpdated) {
        this.lastUpdated = lastUpdated;
    }
}
