package com.example.demo.controller;

import com.example.demo.dto.Stats;
import com.example.demo.model.Account;
import com.example.demo.model.Purchase;
import com.example.demo.model.StatusType;
import com.example.demo.repository.*;

import com.example.demo.service.ProductService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.format.annotation.DateTimeFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import com.example.demo.dto.DailyRevenueDTO;
import com.example.demo.service.PurchaseService;



/**
 * Admin controller for managing dashboard, users, orders, analytics, and messages.
 */
@Controller
@RequestMapping("/admin")
public class AdminDashboardController {

    private final ProductRepository productRepository;
    private final AccountRepository accountRepository;
    private final MessageRepository messageRepository;
    private final PurchaseRepository purchaseRepository;
    private final StatusTypeRepository statusTypeRepository;
    private final ProductService productService;
    private final PurchaseService purchaseService;


    // Constructor injection for all required services and repositories
    public AdminDashboardController(ProductRepository productRepository,
                                    AccountRepository accountRepository,
                                    MessageRepository messageRepository,
                                    PurchaseRepository purchaseRepository,
                                    StatusTypeRepository statusTypeRepository,
                                    ProductService productService ,
                                    PurchaseService purchaseService) {
        this.productRepository = productRepository;
        this.accountRepository = accountRepository;
        this.messageRepository = messageRepository;
        this.purchaseRepository = purchaseRepository;
        this.statusTypeRepository = statusTypeRepository;
        this.productService = productService;
        this.purchaseService = purchaseService;

    }

    /**
     * Displays the main admin dashboard with basic statistics.
     */
    @GetMapping
    public String dashboard(Model model) {
        long productCount = productRepository.count();
        long userCount = accountRepository.count();
        long orderCount = purchaseRepository.count();

        model.addAttribute("stats", new Stats(productCount, orderCount, userCount));
        return "admin/dashboard";
    }

    /**
     * Displays a list of all users, with optional filtering by email and username.
     */
    @GetMapping("/users")
    public String listUsers(@RequestParam(value = "email", required = false) String email,
                            @RequestParam(value = "username", required = false) String username,
                            Model model) {
        List<Account> users = accountRepository.findAll();

        if (email != null && !email.isBlank()) {
            users.removeIf(u -> u.getEmail() == null || !u.getEmail().toLowerCase().contains(email.toLowerCase()));
        }

        if (username != null && !username.isBlank()) {
            users.removeIf(u -> u.getUsername() == null || !u.getUsername().toLowerCase().contains(username.toLowerCase()));
        }

        model.addAttribute("users", users);
        model.addAttribute("email", email);
        model.addAttribute("username", username);
        return "admin/users";
    }

    /**
     * Provides the number of unread messages to be used globally in the navbar.
     */
    @ModelAttribute("unreadMessageCount")
    public long getUnreadMessageCount() {
        return messageRepository.countByAnsweredFalse();
    }

    /**
     * Displays a list of all orders in the system.
     */
    @GetMapping("/orders")
    public String viewAllOrders(@RequestParam(value = "date", required = false) @DateTimeFormat
                                            (iso = DateTimeFormat.ISO.DATE) LocalDate date,
                                @RequestParam(value = "name", required = false) String name,
                                Model model) {
        List<Purchase> orders = purchaseRepository.findAll();

        if (date != null) {
            orders.removeIf(order -> !order.getCreatedAt().toLocalDate().equals(date));
        }

        if (name != null && !name.isBlank()) {
            orders.removeIf(order -> order.getAccount() == null || !order.getAccount().getUsername().toLowerCase().contains(name.toLowerCase()));
        }

        model.addAttribute("orders", orders);
        model.addAttribute("selectedDate", date);
        model.addAttribute("searchName", name);
        return "admin/orders";
    }


    /**
     * Displays details for a specific order by its ID.
     */
    @GetMapping("/orders/{id}")
    public String viewOrderDetails(@PathVariable Long id, Model model) {
        Optional<Purchase> optional = purchaseRepository.findById(id);
        if (optional.isEmpty()) return "redirect:/admin/orders";

        Purchase order = optional.get();
        model.addAttribute("order", order);

        // Load all possible status options for order status dropdown
        List<StatusType> statusOptions = statusTypeRepository.findAll();
        model.addAttribute("statusOptions", statusOptions);

        return "admin/order-details";
    }


    /**
     * Handles updating the status of an order.
     */
    @PostMapping("/orders/update-status")
    public String updateOrderStatus(@RequestParam Long orderId,
                                    @RequestParam Long statusId,
                                    RedirectAttributes redirectAttributes) {
        Optional<Purchase> purchaseOpt = purchaseRepository.findById(orderId);
        Optional<StatusType> statusOpt = statusTypeRepository.findById(statusId);

        if (purchaseOpt.isPresent() && statusOpt.isPresent()) {
            Purchase purchase = purchaseOpt.get();
            purchase.setStatus(statusOpt.get());
            purchaseRepository.save(purchase);
            redirectAttributes.addFlashAttribute("success", "✅ Order status updated successfully!");
        } else {
            redirectAttributes.addFlashAttribute("error", "⚠️ Failed to update order status.");
        }

        return "redirect:/admin/orders/" + orderId;
    }

    /**
     * Displays a list of top-selling products, with optional filters:
     * - search by name
     * - sort by quantity, name, or revenue
     * - filter by time (today, week, month, all)
     */
    @GetMapping("/top-products")
    public String viewTopSellingProducts(@RequestParam(required = false) String search,
                                         @RequestParam(required = false, defaultValue = "quantity") String sortBy,
                                         @RequestParam(required = false, defaultValue = "all") String time,
                                         Model model) {

        if (search == null) search = "";
        LocalDateTime startDate;

        // Determine the start date based on the selected time range
        switch (time) {
            case "today" -> startDate = LocalDate.now().atStartOfDay();
            case "week" -> startDate = LocalDate.now().minusDays(7).atStartOfDay();
            case "month" -> startDate = LocalDate.now().withDayOfMonth(1).atStartOfDay();
            default -> startDate = LocalDate.of(2000, 1, 1).atStartOfDay(); // everything
        }

        // Query top products with filters applied
        List<Object[]> topProducts = productService.findTopSellingFilteredSortedAndTimed(search, sortBy, startDate);

        model.addAttribute("topProducts", topProducts);
        model.addAttribute("search", search);
        model.addAttribute("sortBy", sortBy);
        model.addAttribute("time", time);
        return "admin/top-products";
    }

    /**
     * Displays the revenue chart with optional filtering by time and status.
     */
    @GetMapping("/revenue-chart")
    public String viewRevenueChart(
            @RequestParam(defaultValue = "all") String time,
            @RequestParam(defaultValue = "all") String status,
            Model model) {

        List<Object[]> revenueData;
        // Determine time filter
        LocalDateTime startDate;
        switch (time) {
            case "month" -> startDate = LocalDate.now().withDayOfMonth(1).atStartOfDay();
            case "week" -> startDate = LocalDate.now().minusDays(7).atStartOfDay();
            case "today" -> startDate = LocalDate.now().atStartOfDay();
            default -> startDate = null; // no filtering
        }

        // Apply appropriate filtering logic based on status and time
        if (startDate != null && !"all".equalsIgnoreCase(status)) {
            // filter by date AND status
            revenueData = purchaseRepository.findRevenueByStatusSince(startDate, status);
        } else if (startDate != null) {
            // filter by date only
            revenueData = purchaseRepository.findRevenueSince(startDate);
        } else if (!"all".equalsIgnoreCase(status)) {
            // filter by status only
            revenueData = purchaseRepository.findRevenueByStatus(status);
        } else {
            // no filters
            revenueData = purchaseRepository.findMonthlyRevenue();
        }

        model.addAttribute("revenueData", revenueData);
        model.addAttribute("selectedTime", time);
        model.addAttribute("selectedStatus", status);
        return "admin/revenue-chart";
    }


    /**
     * Displays the analytics page with daily revenue data.
     */
    @GetMapping("/analytics")
    public String showAnalytics(Model model) {
        List<DailyRevenueDTO> revenueData = purchaseService.getDailyRevenue();
        model.addAttribute("revenueData", revenueData);
        return "admin/analytics"; // this matches templates/admin/analytics.html
    }



}
