package com.example.demo.repository;

import com.example.demo.model.Account;
import com.example.demo.model.CartItemEntity;
import com.example.demo.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for managing {@link CartItemEntity} persistence.
 * Spring Data JPA will generate the query implementations automatically
 * based on the method names.
 */
public interface CartItemRepository extends JpaRepository<CartItemEntity, Long> {

    /**
     * Returns the cart‑line (if any) that belongs to the given account
     * and refers to the given product.
     */
    List<CartItemEntity> findAllByAccountAndProduct(Account account, Product product);

    /**
     * Finds all cart items associated with a specific account.
     */
    List<CartItemEntity> findByAccount(Account account);

    /**
     * Deletes all cart items associated with the given account.
     */
    void deleteByAccount(Account account);

    /**
     * Deletes all cart items linked to a specific product ID.
     */
    @Transactional
    @Modifying
    void deleteByProduct_Id(Long productId);
}