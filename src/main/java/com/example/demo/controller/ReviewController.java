package com.example.demo.controller;

import com.example.demo.model.*;
import com.example.demo.repository.*;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDateTime;
import java.util.Optional;

@Controller
@RequestMapping("/reviews")
public class ReviewController {

    private final ProductRepository productRepository;
    private final AccountRepository accountRepository;
    private final PurchaseRepository purchaseRepository;
    private final ReviewRepository reviewRepository;

    @Autowired
    public ReviewController(ProductRepository productRepository,
                            AccountRepository accountRepository,
                            PurchaseRepository purchaseRepository,
                            ReviewRepository reviewRepository) {
        this.productRepository = productRepository;
        this.accountRepository = accountRepository;
        this.purchaseRepository = purchaseRepository;
        this.reviewRepository = reviewRepository;
    }

    /**
     * Display the review form for a specific product and purchase.
     * Accessible only by logged-in users.
     */
    @GetMapping("/new")
    public String showReviewForm(@RequestParam Long productId,
                                 @RequestParam Long purchaseId,
                                 @AuthenticationPrincipal UserDetails userDetails,
                                 Model model,
                                 RedirectAttributes redirectAttributes) {

        // Retrieve the account, product, and purchase from the database
        Optional<Account> accountOpt = accountRepository.findByUsername(userDetails.getUsername());
        Optional<Product> productOpt = productRepository.findById(productId);
        Optional<Purchase> purchaseOpt = purchaseRepository.findById(purchaseId);

        // If any entity is missing, return an error
        if (accountOpt.isEmpty() || productOpt.isEmpty() || purchaseOpt.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "⚠️ Invalid review request.");
            return "redirect:/orders";
        }

        Account account = accountOpt.get();
        Product product = productOpt.get();

        // Prevent duplicate reviews for the same product by the same user
        if (reviewRepository.findByAccountAndProduct(account, product).isPresent()) {
            redirectAttributes.addFlashAttribute("error", "⚠️ You’ve already reviewed this product.");
            return "redirect:/orders/" + purchaseId;
        }

        // Send data to the form page
        model.addAttribute("product", product);
        model.addAttribute("purchase", purchaseOpt.get());
        return "review-form";
    }

    /**
     * Handle submission of a new review.
     * Saves the review if the request is valid.
     */
    @PostMapping("/submit")
    public String submitReview(@RequestParam Long productId,
                               @RequestParam Long purchaseId,
                               @RequestParam int rating,
                               @RequestParam String comment,
                               @AuthenticationPrincipal UserDetails userDetails,
                               RedirectAttributes redirectAttributes) {

        // Validate all input data
        Optional<Account> accountOpt = accountRepository.findByUsername(userDetails.getUsername());
        Optional<Product> productOpt = productRepository.findById(productId);
        Optional<Purchase> purchaseOpt = purchaseRepository.findById(purchaseId);

        // If any data is missing, return error
        if (accountOpt.isEmpty() || productOpt.isEmpty() || purchaseOpt.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "⚠️ Error submitting review.");
            return "redirect:/orders";
        }

        Account account = accountOpt.get();
        Product product = productOpt.get();
        Purchase purchase = purchaseOpt.get();

        // Create and save the review
        Review review = new Review(product, account, purchase, rating, comment);
        review.setCreatedAt(LocalDateTime.now());
        reviewRepository.save(review);

        // Success message and redirect
        redirectAttributes.addFlashAttribute("success", "✅ Thank you for your review!");
        return "redirect:/orders/" + purchaseId;
    }
}
