package com.example.demo.dto;

import java.time.LocalDateTime;


/**
 * Data Transfer Object (DTO) used for sending chat messages between client and server.
 * This DTO is used especially in Server-Sent Events (SSE) and message rendering.
 */
public class ChatMessageDTO {
    private Long id;                     // Unique ID of the message
    private String sender;              // Username of the message sender
    private String content;             // Content of the message
    private LocalDateTime timestamp;    // Time the message was sent
    private boolean admin;              // Flag indicating if the sender is an admin


    // Default constructor (required for serialization/deserialization)
    public ChatMessageDTO() {}

    // Constructor for quick initialization
    public ChatMessageDTO(Long id, String sender, String content, LocalDateTime timestamp, boolean admin) {
        this.id = id;
        this.sender = sender;
        this.content = content;
        this.timestamp = timestamp;
        this.admin = admin;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getSender() { return sender; }
    public void setSender(String sender) { this.sender = sender; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
    public boolean isAdmin() { return admin; }
    public void setAdmin(boolean admin) { this.admin = admin; }
}