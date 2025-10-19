package com.example.demo.service;

import com.example.demo.model.Product;
import com.example.demo.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;
    private final CartItemRepository cartItemRepository;
    private final FavoriteRepository favoriteRepository;
    private final LineItemRepository lineItemRepository;
    private final ReviewRepository reviewRepository;
    private final PurchaseRepository purchaseRepository;

    /**
     * Deletes a product and its dependent rows (cart items, favorites, line items)
     * in a single transaction, avoiding FK constraint violations.
     */
    @Transactional
    public void deleteProduct(Long productId) {
        cartItemRepository.deleteByProduct_Id(productId);
        favoriteRepository.deleteByProduct_Id(productId);
        lineItemRepository.deleteByProduct_Id(productId);
        productRepository.deleteById(productId);
    }

    /**
     * Returns top-selling products with optional search & sort filters
     * limited to orders made since {@code startDate}.
     * <p>
     * The repository query joins LineItem → Product so the Object[]
     * comes back as { productName, metric } (quantity or revenue).
     */
    @Transactional(readOnly = true)
    public List<Object[]> findTopSellingFilteredSortedAndTimed(String search,
                                                               String sortBy,
                                                               LocalDateTime startDate) {
        // Ensure non‑null parameters for JPQL LIKE / CASE usage
        if (search == null) search = "";
        if (sortBy == null || sortBy.isBlank()) sortBy = "quantity";
        return purchaseRepository.findTopSellingByFilter(startDate, search.toLowerCase(), sortBy);
    }

    /**
     * Calculates average rating per product and returns a map keyed by product id.
     * Products without reviews get 0.0.
     */
    @Transactional(readOnly = true)
    public Map<Long, Double> getAverageRatingsForProducts(List<Product> products) {
        Map<Long, Double> ratingMap = new HashMap<>();
        for (Product p : products) {
            double avg = reviewRepository.findAverageRatingByProductId(p.getId())
                    .orElse(0.0);
            ratingMap.put(p.getId(), avg);
        }
        return ratingMap;
    }
}