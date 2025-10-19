package com.example.demo.controller;

import com.example.demo.model.*;
import com.example.demo.repository.*;
import jakarta.servlet.http.HttpSession;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;


/**
 * Controller for managing the shopping cart:
 * - Viewing cart contents
 * - Adding, updating, and removing items
 * - Syncing cart with the database if the user is logged in
 */
@Controller
public class CartController {

    private final ProductRepository productRepository;
    private final CartItemRepository cartItemRepository;
    private final AccountRepository accountRepository;

    // Constructor injection for required repositories
    public CartController(ProductRepository productRepository,
                          CartItemRepository cartItemRepository,
                          AccountRepository accountRepository) {
        this.productRepository = productRepository;
        this.cartItemRepository = cartItemRepository;
        this.accountRepository = accountRepository;
    }

    /**
     * Displays the cart contents and total amount.
     */
    @GetMapping("/cart")
    public String viewCart(HttpSession session, Model model,
                           @ModelAttribute("cartMessage") String cartMessage,
                           @ModelAttribute("success") String success) {
        // Retrieve cart from session
        Map<Long, CartItem> cart = (Map<Long, CartItem>) session.getAttribute("cart");
        Map<Product, CartItem> cartItems = new HashMap<>();
        BigDecimal total = BigDecimal.ZERO;

        // Loop through items and calculate total
        if (cart != null) {
            for (Map.Entry<Long, CartItem> entry : cart.entrySet()) {
                Product product = productRepository.findById(entry.getKey()).orElse(null);
                if (product != null) {
                    cartItems.put(product, entry.getValue());
                    total = total.add(product.getPrice().multiply(BigDecimal.valueOf(entry.getValue().getQuantity())));
                }
            }
        }

        model.addAttribute("cartItems", cartItems);
        model.addAttribute("cartTotal", total);
        model.addAttribute("cartMessage", cartMessage);
        model.addAttribute("success", success);
        return "cart";
    }

    /**
     * Adds a product to the session cart and optionally saves it to the database.
     */
    @PostMapping("/cart/add")
    public String addToCart(@RequestParam Long productId,
                            @RequestParam int quantity,
                            @RequestParam(required = false) String comment,
                            @AuthenticationPrincipal UserDetails userDetails,
                            HttpSession session,
                            RedirectAttributes redirectAttributes) {

        Product product = productRepository.findById(productId).orElse(null);
        if (product == null || quantity <= 0) {
            redirectAttributes.addFlashAttribute("cartMessage", "❌ Invalid product or quantity.");
            return "redirect:/products";
        }

        // Get or create session cart
        Map<Long, CartItem> cart = (Map<Long, CartItem>) session.getAttribute("cart");
        if (cart == null) {
            cart = new HashMap<>();
            session.setAttribute("cart", cart);
        }

        // ---------- ⭐ STOCK CHECK ----------
        int currentQty = cart.getOrDefault(productId, new CartItem(product, 0, null))
                .getQuantity();
        if (currentQty + quantity > product.getStock()) {
            redirectAttributes.addFlashAttribute(
                    "cartMessage",
                    "❌ Only " + product.getStock() + " left in stock."
            );
            return "redirect:/products/" + productId;   // stay on product page
        }
        // ------------------------------------

        // Update quantity if item exists
        CartItem existingItem = cart.get(productId);
        if (existingItem != null) {
            existingItem.setQuantity(currentQty + quantity);
        } else {
            cart.put(productId, new CartItem(product, quantity, comment));
        }

        // Save to database if user is logged in
        if (userDetails != null) {
            Account account = accountRepository.findByUsername(userDetails.getUsername()).orElse(null);
            if (account != null) {
                // find existing DB cart item for this user+product (if any)
                java.util.List<CartItemEntity> entities = cartItemRepository.findAllByAccountAndProduct(account, product);
CartItemEntity entity;
if (entities.isEmpty()) {
    entity = new CartItemEntity(account, product, 0, null);
} else {
    entity = entities.get(0);
    // merge any duplicates
    if (entities.size() > 1) {
        for (int i = 1; i < entities.size(); i++) {
            entity.setQuantity(entity.getQuantity() + entities.get(i).getQuantity());
            cartItemRepository.delete(entities.get(i));
        }
    }
}

entity.setQuantity(entity.getQuantity() + quantity);
entity.setComment(comment);
cartItemRepository.save(entity);

            }
        }

        redirectAttributes.addFlashAttribute("success", "✅ Product added to cart.");
        return "redirect:/products";
    }


    /**
     * Removes a product from the session cart.
     */
    @PostMapping("/cart/remove")
    public String removeFromCart(@RequestParam Long productId,
                                 HttpSession session,
                                 RedirectAttributes redirectAttributes) {
        Map<Long, CartItem> cart = (Map<Long, CartItem>) session.getAttribute("cart");
        if (cart != null && cart.containsKey(productId)) {
            cart.remove(productId);
            redirectAttributes.addFlashAttribute("cartMessage", "🗑 Product removed from cart.");
        }
        return "redirect:/cart";
    }

    /**
     * Updates quantity and comment for a cart item.
     * Validates product availability before updating.
     */
    @PostMapping("/cart/update")
    public String updateCart(@RequestParam Long productId,
                             @RequestParam int quantity,
                             @RequestParam(required = false) String comment,
                             HttpSession session,
                             RedirectAttributes redirectAttributes) {
        Map<Long, CartItem> cart = (Map<Long, CartItem>) session.getAttribute("cart");
        Product product = productRepository.findById(productId).orElse(null);

        if (product == null || cart == null || !cart.containsKey(productId)) {
            redirectAttributes.addFlashAttribute("cartMessage", "⚠️ Product not found.");
            return "redirect:/cart";
        }

        if (quantity > product.getStock()) {
            redirectAttributes.addFlashAttribute("cartMessage",
                    "⚠️ Cannot update. Only " + product.getStock() + " left in stock.");
            return "redirect:/cart";
        }

        if (quantity > 0) {
            CartItem item = cart.get(productId);
            item.setQuantity(quantity);
            item.setComment(comment);
            session.setAttribute("cart", cart);
            redirectAttributes.addFlashAttribute("cartMessage", "🔄 Cart updated.");
        } else {
            redirectAttributes.addFlashAttribute("cartMessage", "⚠️ Invalid quantity.");
        }
        return "redirect:/cart";
    }
}
