package com.example.demo.controller;
import com.example.demo.model.*;
import com.example.demo.repository.*;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

@Controller
public class OrderController {

    private final PurchaseRepository purchaseRepository;
    private final AccountRepository accountRepository;
    private final StatusTypeRepository statusTypeRepository;
    private final CartItemRepository cartItemRepository;
    private final ReviewRepository reviewRepository;
    private final ProductRepository productRepository;

    @Autowired
    public OrderController(PurchaseRepository purchaseRepository,
                           AccountRepository accountRepository,
                           StatusTypeRepository statusTypeRepository,
                           CartItemRepository cartItemRepository,
                           ReviewRepository reviewRepository,
                           ProductRepository productRepository) {
        this.purchaseRepository = purchaseRepository;
        this.accountRepository = accountRepository;
        this.statusTypeRepository = statusTypeRepository;
        this.cartItemRepository = cartItemRepository;
        this.reviewRepository = reviewRepository;
        this.productRepository = productRepository;

    }

    /**
     * Displays the checkout form to the user.
     */
    @GetMapping("/checkout")
    public String showCheckoutForm() {
        return "checkout";
    }

    /**
     * Handles the confirmation of a user's checkout process.
     * Validates stock, creates a purchase, updates stock, saves purchase, clears cart.
     */
    @Transactional
    @PostMapping("/checkout/confirm")
    public String processCheckout(@RequestParam String fullName,
                                  @RequestParam String phoneNumber,
                                  @RequestParam String address,
                                  @RequestParam String paymentMethod,
                                  @RequestParam(required = false) String cardNumber,
                                  @RequestParam(required = false) String expiryDate,
                                  @RequestParam(required = false) String cvv,
                                  @AuthenticationPrincipal UserDetails userDetails,
                                  HttpSession session,
                                  RedirectAttributes redirectAttributes) {

        // Get cart from session
        Map<Long, CartItem> cart = (Map<Long, CartItem>) session.getAttribute("cart");
        if (cart == null || cart.isEmpty()) {
            redirectAttributes.addFlashAttribute("cartMessage", "🛒 Your cart is empty.");
            return "redirect:/cart";
        }

        // Validate user account
        Optional<Account> optionalAccount = accountRepository.findByUsername(userDetails.getUsername());
        if (optionalAccount.isEmpty()) {
            redirectAttributes.addFlashAttribute("cartMessage", "⚠️ Unable to find user account.");
            return "redirect:/cart";
        }

        Account account = optionalAccount.get();

        // Step 1: Validate stock before purchase
        for (CartItem cartItem : cart.values()) {
            Product freshProduct = productRepository.findById(cartItem.getProduct().getId()).orElse(null);
            if (freshProduct == null) {
                redirectAttributes.addFlashAttribute("cartMessage", "❌ Product no longer exists.");
                return "redirect:/cart";
            }

            int requestedQty = cartItem.getQuantity();
            int availableStock = freshProduct.getStock();

            if (requestedQty > availableStock) {
                redirectAttributes.addFlashAttribute("cartMessage",
                        "❌ Not enough stock for \"" + freshProduct.getName() +
                                "\". Only " + availableStock + " units left. Please update your cart.");
                return "redirect:/cart";
            }
        }

        // Step 2: Prepare purchase record
        Purchase purchase = new Purchase();
        purchase.setAccount(account);
        purchase.setCreatedAt(LocalDateTime.now());
        purchase.setStatus(statusTypeRepository.findByLabel("Pending"));
        purchase.setFullName(fullName);
        purchase.setAddress(address);
        purchase.setPhoneNumber(phoneNumber);
        purchase.setPaymentMethod(paymentMethod);

        BigDecimal total = BigDecimal.ZERO;
        List<LineItem> lineItems = new ArrayList<>();

        //  Step 3: Create LineItems and deduct stock
        for (CartItem cartItem : cart.values()) {
            Product freshProduct = productRepository.findById(cartItem.getProduct().getId()).orElse(null);
            if (freshProduct == null) continue;

            int quantity = cartItem.getQuantity();

            freshProduct.setStock(freshProduct.getStock() - quantity);
            productRepository.save(freshProduct);

            LineItem lineItem = new LineItem();
            lineItem.setProduct(freshProduct);
            lineItem.setQuantity(quantity);
            lineItem.setUnitPrice(freshProduct.getPrice());
            lineItem.setEngravingText(cartItem.getComment());
            lineItem.setPurchase(purchase);

            BigDecimal itemTotal = freshProduct.getPrice().multiply(BigDecimal.valueOf(quantity));
            total = total.add(itemTotal);

            lineItems.add(lineItem);
        }

        purchase.setTotal(total);
        purchase.setLineItems(lineItems);

        // Step 4: Save purchase and clear cart
        purchaseRepository.save(purchase);
        session.removeAttribute("cart");
        cartItemRepository.deleteByAccount(account);

        redirectAttributes.addFlashAttribute("success", "✅ Your order has been placed successfully!");
        return "redirect:/orders";
    }

    /**
     * Displays all orders for the currently logged-in user.
     */
    @GetMapping("/orders")
    public String viewOrders(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        Optional<Account> optionalAccount = accountRepository.findByUsername(userDetails.getUsername());
        if (optionalAccount.isEmpty()) {
            return "redirect:/login";
        }

        Account account = optionalAccount.get();
        List<Purchase> orders = purchaseRepository.findByAccount(account);
        model.addAttribute("orders", orders);

        return "orders";
    }

    /**
     * Displays a specific order, ensuring that it belongs to the logged-in user.
     * Also checks if each product has already been reviewed.
     */
    @GetMapping("/orders/{id}")
    public String viewSingleOrder(@PathVariable Long id,
                                  @AuthenticationPrincipal UserDetails userDetails,
                                  Model model,
                                  RedirectAttributes redirectAttributes) {

        Optional<Account> accountOpt = accountRepository.findByUsername(userDetails.getUsername());
        if (accountOpt.isEmpty()) return "redirect:/login";

        Optional<Purchase> purchaseOpt = purchaseRepository.findById(id);
        if (purchaseOpt.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "⚠️ Order not found.");
            return "redirect:/orders";
        }

        Purchase purchase = purchaseOpt.get();
        Account account = accountOpt.get();

        if (!purchase.getAccount().getUsername().equals(userDetails.getUsername())) {
            redirectAttributes.addFlashAttribute("error", "⚠️ You are not authorized to view this order.");
            return "redirect:/orders";
        }

        // Mark which products have been reviewed
        for (LineItem item : purchase.getLineItems()) {
            boolean reviewed = reviewRepository.findByAccountAndProduct(account, item.getProduct()).isPresent();
            item.getProduct().setReviewed(reviewed);
        }

        model.addAttribute("order", purchase);
        return "order-details";
    }


}
