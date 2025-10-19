package com.example.demo.repository;

import com.example.demo.model.Message;
import com.example.demo.model.Account;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MessageRepository extends JpaRepository<Message, Long> {

    // Get all messages sent by a specific user (customer)
    List<Message> findBySender(Account sender);

    // Count all messages that have not yet been marked as answered
    long countByAnsweredFalse();

    //  Get all messages that haven’t been answered by admin
    List<Message> findByAdminReplyIsNull();

    //  Get all messages sorted by most recent update
    List<Message> findBySenderOrderByLastUpdatedDesc(Account sender);
}
