package com.example.demo.repository;

import com.example.demo.model.Review;
import com.example.demo.model.Account;
import com.example.demo.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.List;

/**
 * Repository interface for managing Review entities.
 * Provides CRUD operations and custom queries related to product reviews.
 */
public interface ReviewRepository extends JpaRepository<Review, Long> {

    /**
     * Finds a review by a specific user on a specific product.
     *
     * @param account The user who submitted the review.
     * @param product The product being reviewed.
     * @return An Optional containing the review, if found.
     */
    Optional<Review> findByAccountAndProduct(Account account, Product product);

    /**
     * Retrieves all reviews associated with a given product.
     *
     * @param product The product whose reviews to fetch.
     * @return A list of reviews for the specified product.
     */
    List<Review> findAllByProduct(Product product);

    /**
     * Retrieves all reviews for a specific product (alternative naming).
     *
     * @param product The product to find reviews for.
     * @return A list of reviews.
     */
    List<Review> findByProduct(Product product);  // Redundant with findAllByProduct, but acceptable

    /**
     * Calculates the average rating for a given product by its ID.
     *
     * @param productId The ID of the product.
     * @return An Optional containing the average rating, or empty if none exist.
     */
    @Query("SELECT AVG(r.rating) FROM Review r WHERE r.product.id = :productId")
    Optional<Double> findAverageRatingByProductId(@Param("productId") Long productId);
}
