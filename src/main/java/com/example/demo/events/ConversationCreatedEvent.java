package com.example.demo.events;

import com.example.demo.model.Conversation;

/**
 * Application event triggered when a new conversation is created.
 * This can be used to notify listeners, update UI via SSE, log actions, etc.
 */
public class ConversationCreatedEvent {
    // The newly created Conversation instance
    private final Conversation conversation;
    /**
     * Constructs a new event with the given conversation.
     *
     * @param conversation the newly created conversation
     */
    public ConversationCreatedEvent(Conversation conversation) {
        this.conversation = conversation;
    }

    /**
     * Returns the conversation associated with this event.
     *
     * @return the new Conversation
     */
    public Conversation getConversation() {
        return conversation;
    }
}