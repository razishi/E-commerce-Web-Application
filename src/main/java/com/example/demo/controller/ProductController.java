package com.example.demo.controller;

import com.example.demo.model.Account;
import com.example.demo.model.Favorite;
import com.example.demo.model.Product;
import com.example.demo.model.Review;
import com.example.demo.repository.AccountRepository;
import com.example.demo.repository.FavoriteRepository;
import com.example.demo.repository.ProductRepository;
import com.example.demo.repository.ReviewRepository;
import com.example.demo.service.ProductService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.security.Principal;
import java.util.*;

@Controller
public class ProductController {

    private final ProductRepository productRepository;
    private final FavoriteRepository favoriteRepository;
    private final AccountRepository accountRepository;
    private final ReviewRepository reviewRepository;
    private final ProductService productService;

    public ProductController(ProductRepository productRepository,
                             FavoriteRepository favoriteRepository,
                             AccountRepository accountRepository,
                             ReviewRepository reviewRepository,
                             ProductService productService) {
        this.productRepository = productRepository;
        this.favoriteRepository = favoriteRepository;
        this.accountRepository = accountRepository;
        this.reviewRepository = reviewRepository;
        this.productService = productService;
    }

    /**
     * Displays the main product listing page.
     * Supports search, category filtering, max price filter, and sorting by price or rating.
     */
    @GetMapping("/products")
    public String listProducts(@RequestParam(required = false) String q,
                               @RequestParam(required = false) String category,
                               @RequestParam(required = false) BigDecimal max,
                               @RequestParam(required = false) String sort,
                               Model model,
                               Principal principal) {

        // Step 1: Apply search filter
        List<Product> products = (q == null || q.isBlank())
                ? productRepository.findAll()
                : productRepository.findByNameContainingIgnoreCase(q);

        // Step 2: Filter by category
        if (category != null && !category.isBlank()) {
            products.retainAll(productRepository.findByCategoryName(category));
        }

        // Step 3: Filter by price
        if (max != null) {
            products.removeIf(p -> p.getPrice().compareTo(max) > 0);
        }

        // Step 4: Sort logic
        model.addAttribute("sortedByRating", false); // default

        if ("rating_asc".equals(sort) || "rating_desc".equals(sort)) {
            Map<Long, Double> ratingMap = productService.getAverageRatingsForProducts(products);
            final Map<Long, Double> finalRatings = ratingMap; // make effectively final for lambda

            products.sort((p1, p2) -> {
                double r1 = finalRatings.getOrDefault(p1.getId(), 0.0);
                double r2 = finalRatings.getOrDefault(p2.getId(), 0.0);
                return "rating_asc".equals(sort) ? Double.compare(r1, r2) : Double.compare(r2, r1);
            });

            model.addAttribute("avgRatings", ratingMap);
            model.addAttribute("sortedByRating", true);

        } else if ("price_asc".equals(sort)) {
            products.sort(Comparator.comparing(Product::getPrice));

        } else if ("price_desc".equals(sort)) {
            products.sort(Comparator.comparing(Product::getPrice).reversed());
        }

        // Step 5: Mark favorites if user is logged in
        if (principal != null) {
            accountRepository.findByUsername(principal.getName()).ifPresent(account -> {
                Set<Long> favIds = new HashSet<>();
                for (Favorite fav : favoriteRepository.findByAccount(account)) {
                    favIds.add(fav.getProduct().getId());
                }
                products.forEach(p -> p.setFavorite(favIds.contains(p.getId())));
            });
        }

        // Step 6: Add data to model
        model.addAttribute("products", products);
        model.addAttribute("categories", productRepository.findDistinctCategories());
        model.addAttribute("sort", sort);
        model.addAttribute("searchQuery", q);
        model.addAttribute("selectedCategory", category);
        model.addAttribute("maxPrice", max);

        return "products";
    }

    /**
     * Displays the product detail page for a single product, including reviews and similar items.
     */
    @GetMapping("/products/{id}")
    public String viewProductDetails(@PathVariable Long id,
                                     Model model,
                                     Principal principal) {
        Optional<Product> productOpt = productRepository.findById(id);
        if (productOpt.isEmpty()) {
            return "redirect:/products";
        }

        Product product = productOpt.get();
        model.addAttribute("product", product);

        // Fetch and display all reviews for this product
        List<Review> reviews = reviewRepository.findByProduct(product);
        model.addAttribute("reviews", reviews);

        // Mark product as favorite if the user is logged in and it's favorited
        if (principal != null) {
            accountRepository.findByUsername(principal.getName()).ifPresent(account -> {
                boolean isFavorite = favoriteRepository.findByAccountAndProduct(account, product).isPresent();
                model.addAttribute("isFavorite", isFavorite);
            });
        }

        // Suggest similar products from the same category
        List<Product> similarProducts = productRepository.findByCategoryName(product.getCategory().getName());
        similarProducts.removeIf(p -> p.getId().equals(product.getId()));
        model.addAttribute("similarProducts", similarProducts);

        return "product-details";
    }
}
