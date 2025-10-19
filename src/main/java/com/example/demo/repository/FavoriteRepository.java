package com.example.demo.repository;

import com.example.demo.model.Account;
import com.example.demo.model.Favorite;
import com.example.demo.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.jpa.repository.Modifying;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for managing Favorite entities.
 * Provides CRUD operations and custom query methods related to user favorites.
 */
public interface FavoriteRepository extends JpaRepository<Favorite, Long> {

    /**
     * Finds all favorites associated with a specific account.
     *
     * @param account The account to search for.
     * @return A list of favorites linked to the given account.
     */
    List<Favorite> findByAccount(Account account);

    /**
     * Finds a specific favorite by account and product.
     *
     * @param account The account to search for.
     * @param product The product to search for.
     * @return An Optional containing the Favorite if found, or empty otherwise.
     */
    Optional<Favorite> findByAccountAndProduct(Account account, Product product);

    /**
     * Deletes a favorite entry based on account and product.
     *
     * @param account The account linked to the favorite.
     * @param product The product linked to the favorite.
     */
    void deleteByAccountAndProduct(Account account, Product product);

    /**
     * Deletes all favorite entries linked to a specific product ID.
     * Transactional and modifying operation.
     *
     * @param productId The ID of the product whose favorites should be deleted.
     */
    @Transactional
    @Modifying
    void deleteByProduct_Id(Long productId);
}
