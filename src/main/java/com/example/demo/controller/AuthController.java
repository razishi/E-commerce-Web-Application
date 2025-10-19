package com.example.demo.controller;

import com.example.demo.model.*;
import com.example.demo.repository.*;
import jakarta.servlet.http.HttpSession;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Handles authentication and registration-related functionality:
 * - Login page
 * - Registration form and logic
 * - Restoring user's cart after login
 */
@Controller
public class AuthController {

    private final AccountRepository accountRepository;
    private final PasswordEncoder passwordEncoder;
    private final CartItemRepository cartItemRepository;

    // Constructor injection for required services/repositories

    public AuthController(AccountRepository accountRepository,
                          PasswordEncoder passwordEncoder,
                          CartItemRepository cartItemRepository) {
        this.accountRepository = accountRepository;
        this.passwordEncoder = passwordEncoder;
        this.cartItemRepository = cartItemRepository;
    }

    /**
     * Displays the custom login page.
     * Mapped to GET /login
     */
    @GetMapping("/login")
    public String loginPage() {
        return "auth/login";
    }

    /**
     * Displays the registration form.
     * Mapped to GET /register
     */
    @GetMapping("/register")
    public String showRegistrationForm(Model model) {
        model.addAttribute("account", new Account());
        return "auth/register";
    }

    /**
     * Handles user registration submission.
     * Validates email and username for uniqueness before saving.
     * Mapped to POST /register
     */
    @PostMapping("/register")
    public String processRegistration(@ModelAttribute("account") Account account, Model model) {

        boolean hasError = false;

        // Check if email is already taken
        if (accountRepository.findByEmail(account.getEmail()).isPresent()) {
            model.addAttribute("emailError", "⚠ This email address is already registered.");
            hasError = true;
        }

        // Check if username is already taken
        if (accountRepository.findByUsername(account.getUsername()).isPresent()) {
            model.addAttribute("usernameError", "⚠ This username is already taken.");
            hasError = true;
        }

        // Return to registration form if there are validation errors
        if (hasError) {
            model.addAttribute("registrationAttempted", true);
            return "auth/register";
        }

        // All good: encode password and save account
        account.setPasswordHash(passwordEncoder.encode(account.getPassword()));
        account.setRole("ROLE_USER");
        account.setEnabled(true);

        accountRepository.save(account);
        return "redirect:/products";
    }

    /**
     * After successful login, restore the user's cart from the database.
     * Mapped to GET /login/success
     */
    @GetMapping("/login/success")
    public String loadCartAfterLogin(@AuthenticationPrincipal UserDetails userDetails,
                                     HttpSession session) {
        // Get the account object from the database using the logged-in username
        Account account = accountRepository.findByUsername(userDetails.getUsername()).orElse(null);

        // Retrieve the saved cart items for the user
        if (account != null) {
            List<CartItemEntity> savedItems = cartItemRepository.findByAccount(account);

            // Convert saved entities into in-session cart object (Map of productId to CartItem)
            Map<Long, CartItem> cart = new HashMap<>();
            for (CartItemEntity item : savedItems) {
                cart.put(item.getProduct().getId(), new CartItem(
                        item.getProduct(),
                        item.getQuantity(),
                        item.getComment()
                ));
            }
            // Save cart into the HTTP session
            session.setAttribute("cart", cart);
        }
        return "redirect:/products";
    }
}
