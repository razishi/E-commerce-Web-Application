package com.example.demo.repository;

import com.example.demo.model.MessageEntry;
import com.example.demo.model.Conversation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository interface for managing MessageEntry entities.
 * Provides CRUD operations and custom query methods related to messages within conversations.
 */
@Repository
public interface MessageEntryRepository extends JpaRepository<MessageEntry, Long> {

    /**
     * Retrieves all message entries in a conversation, ordered by the time they were sent (ascending).
     *
     * @param conversation The conversation to search in.
     * @return A list of message entries ordered by sent time.
     */
    List<MessageEntry> findByConversationOrderBySentAtAsc(Conversation conversation);

    /**
     * Counts the number of unread messages in a conversation sent by the user (not admin).
     *
     * @param conversation The conversation to check.
     * @return The count of unread user messages.
     */
    long countByConversationAndFromAdminFalseAndReadByAdminFalse(Conversation conversation);

    /**
     * Retrieves all unread messages in a conversation sent by the user (not admin).
     *
     * @param conversation The conversation to search in.
     * @return A list of unread user message entries.
     */
    List<MessageEntry> findByConversationAndFromAdminFalseAndReadByAdminFalse(Conversation conversation);
}
