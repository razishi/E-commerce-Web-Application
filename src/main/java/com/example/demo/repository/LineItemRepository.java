package com.example.demo.repository;

import com.example.demo.model.LineItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.jpa.repository.Modifying;

/**
 * Repository interface for managing LineItem entities.
 * Provides CRUD operations and custom query methods related to line items.
 */
public interface LineItemRepository extends JpaRepository<LineItem, Long> {

    /**
     * Deletes all line items associated with a specific product ID.
     * This is a transactional and modifying query.
     *
     * @param productId The ID of the product whose line items should be deleted.
     */
    @Transactional
    @Modifying
    void deleteByProduct_Id(Long productId);
}
