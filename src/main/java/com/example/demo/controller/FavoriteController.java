package com.example.demo.controller;

import com.example.demo.model.Account;
import com.example.demo.model.Favorite;
import com.example.demo.model.Product;
import com.example.demo.repository.AccountRepository;
import com.example.demo.repository.FavoriteRepository;
import com.example.demo.repository.ProductRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import jakarta.transaction.Transactional;

import java.security.Principal;
import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/favorites")// Base path for all endpoints in this controller
public class FavoriteController {

    private final FavoriteRepository favoriteRepository;
    private final AccountRepository accountRepository;
    private final ProductRepository productRepository;

    // Constructor injection for required repositories
    public FavoriteController(FavoriteRepository favoriteRepository,
                              AccountRepository accountRepository,
                              ProductRepository productRepository) {
        this.favoriteRepository = favoriteRepository;
        this.accountRepository = accountRepository;
        this.productRepository = productRepository;
    }

    /**
     * Display all favorite products for the logged-in user.
     */
    @GetMapping
    public String viewFavorites(Model model, Principal principal) {
        Account account = accountRepository.findByUsername(principal.getName()).orElse(null);
        if (account != null) {
            List<Favorite> favorites = favoriteRepository.findByAccount(account);
            model.addAttribute("favorites", favorites);
        }
        return "favorites"; // Thymeleaf template: favorites.html
    }

    /**
     * Adds a product to the user's favorites list.
     */
    @GetMapping("/add/{productId}")
    public String addFavorite(@PathVariable Long productId,
                              Principal principal,
                              RedirectAttributes redirectAttributes) {
        Account account = accountRepository.findByUsername(principal.getName()).orElse(null);
        Product product = productRepository.findById(productId).orElse(null);

        if (account != null && product != null) {
            // Only add if not already a favorite
            if (favoriteRepository.findByAccountAndProduct(account, product).isEmpty()) {
                Favorite favorite = new Favorite(account, product);
                favoriteRepository.save(favorite);
                redirectAttributes.addFlashAttribute("successMessage", "✅ Product added to favorites!");
            } else {
                redirectAttributes.addFlashAttribute("successMessage", "⚠️ Product is already in favorites.");
            }
        }

        return "redirect:/products";
    }

    /**
     * Removes a product from the user's favorites list.
     * Marked @Transactional to ensure the delete operation is safely committed.
     */
    @Transactional
    @GetMapping("/remove/{productId}")
    public String removeFavorite(@PathVariable Long productId,
                                 Principal principal,
                                 RedirectAttributes redirectAttributes) {
        Optional<Account> accountOpt = accountRepository.findByUsername(principal.getName());
        Optional<Product> productOpt = productRepository.findById(productId);

        if (accountOpt.isPresent() && productOpt.isPresent()) {
            favoriteRepository.deleteByAccountAndProduct(accountOpt.get(), productOpt.get());
            redirectAttributes.addFlashAttribute("successMessage", "✅ Product removed from favorites!");
        } else {
            redirectAttributes.addFlashAttribute("successMessage", "⚠️ Could not remove product from favorites.");
        }

        return "redirect:/products";
    }

    /**
     * Toggles a product in the user's favorites list:
     * - If it already exists: remove it.
     * - If it doesn't: add it.
     */
    @PostMapping("/toggle")
    public String toggleFavorite(@RequestParam Long productId,
                                 Principal principal,
                                 RedirectAttributes redirectAttributes) {
        Account account = accountRepository.findByUsername(principal.getName()).orElse(null);
        Product product = productRepository.findById(productId).orElse(null);

        if (account != null && product != null) {
            Optional<Favorite> existing = favoriteRepository.findByAccountAndProduct(account, product);
            if (existing.isPresent()) {
                favoriteRepository.delete(existing.get());
                redirectAttributes.addFlashAttribute("successMessage", "🗑 Removed from favorites.");
            } else {
                favoriteRepository.save(new Favorite(account, product));
                redirectAttributes.addFlashAttribute("successMessage", "❤️ Added to favorites.");
            }
        }

        return "redirect:/products/" + productId;
    }
}
