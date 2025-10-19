package com.example.demo.repository;

import com.example.demo.model.Conversation;
import com.example.demo.model.Account;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository interface for managing Conversation entities.
 * Provides CRUD operations and custom query methods related to user conversations.
 */
@Repository
public interface ConversationRepository extends JpaRepository<Conversation, Long> {

    /**
     * Finds all conversations associated with a specific user account.
     *
     * @param user The account of the user.
     * @return A list of conversations linked to the given user.
     */
    List<Conversation> findByUser(Account user);
}
