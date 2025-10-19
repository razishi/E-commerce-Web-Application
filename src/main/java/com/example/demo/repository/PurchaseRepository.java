package com.example.demo.repository;

import com.example.demo.model.Account;
import com.example.demo.model.Purchase;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface PurchaseRepository extends JpaRepository<Purchase, Long> {

    //  Monthly revenue grouped by year-month (includes ALL orders)
    @Query("SELECT FUNCTION('DATE_FORMAT', p.createdAt, '%Y-%m') AS month, SUM(p.total) " +
            "FROM Purchase p " +
            "GROUP BY FUNCTION('DATE_FORMAT', p.createdAt, '%Y-%m') " +
            "ORDER BY month")
    List<Object[]> findMonthlyRevenue();
    @Query("SELECT FUNCTION('DATE_FORMAT', p.createdAt, '%Y-%m-%d'), SUM(p.total) " +
            "FROM Purchase p " +
            "WHERE p.createdAt >= :startDate " +
            "GROUP BY FUNCTION('DATE_FORMAT', p.createdAt, '%Y-%m-%d') " +
            "ORDER BY FUNCTION('DATE_FORMAT', p.createdAt, '%Y-%m-%d')")
    List<Object[]> findRevenueSince(@Param("startDate") LocalDateTime startDate);

    @Query("SELECT FUNCTION('DATE_FORMAT', p.createdAt, '%Y-%m-%d'), SUM(p.total) " +
            "FROM Purchase p " +
            "WHERE p.createdAt >= :startDate AND p.status.label = :status " +
            "GROUP BY FUNCTION('DATE_FORMAT', p.createdAt, '%Y-%m-%d') " +
            "ORDER BY FUNCTION('DATE_FORMAT', p.createdAt, '%Y-%m-%d')")
    List<Object[]> findRevenueByStatusSince(@Param("startDate") LocalDateTime startDate, @Param("status") String status);

    @Query("SELECT FUNCTION('DATE_FORMAT', p.createdAt, '%Y-%m-%d'), SUM(p.total) " +
            "FROM Purchase p " +
            "WHERE p.status.label = :status " +
            "GROUP BY FUNCTION('DATE_FORMAT', p.createdAt, '%Y-%m-%d') " +
            "ORDER BY FUNCTION('DATE_FORMAT', p.createdAt, '%Y-%m-%d')")
    List<Object[]> findRevenueByStatus(@Param("status") String status);

    //  All orders by account
    List<Purchase> findByAccount(Account account);

    //  All recent orders since a specific date
    @Query("SELECT p FROM Purchase p WHERE p.createdAt >= :startDate")
    List<Purchase> findRecentOrders(@Param("startDate") LocalDateTime startDate);

    //  Top-selling products - all time
    @Query("SELECT li.product.name, SUM(li.quantity) " +
            "FROM LineItem li " +
            "GROUP BY li.product.name " +
            "ORDER BY SUM(li.quantity) DESC")
    List<Object[]> findTopSellingProducts();

    //  Filtered by product name
    @Query("SELECT li.product.name, SUM(li.quantity) " +
            "FROM LineItem li " +
            "WHERE LOWER(li.product.name) LIKE %:search% " +
            "GROUP BY li.product.name " +
            "ORDER BY SUM(li.quantity) DESC")
    List<Object[]> findTopSellingProductsByName(@Param("search") String search);

    //  Sorted by name
    @Query("SELECT li.product.name, SUM(li.quantity) " +
            "FROM LineItem li " +
            "WHERE LOWER(li.product.name) LIKE %:search% " +
            "GROUP BY li.product.name " +
            "ORDER BY li.product.name ASC")
    List<Object[]> findTopSellingSortedByName(@Param("search") String search);

    //  Sorted by quantity
    @Query("SELECT li.product.name, SUM(li.quantity) " +
            "FROM LineItem li " +
            "WHERE LOWER(li.product.name) LIKE %:search% " +
            "GROUP BY li.product.name " +
            "ORDER BY SUM(li.quantity) DESC")
    List<Object[]> findTopSellingSortedByQuantity(@Param("search") String search);

    //  Sorted by revenue
    @Query("SELECT li.product.name, SUM(li.quantity), SUM(li.quantity * li.product.price) " +
            "FROM LineItem li " +
            "WHERE LOWER(li.product.name) LIKE %:search% " +
            "GROUP BY li.product.name " +
            "ORDER BY SUM(li.quantity * li.product.price) DESC")
    List<Object[]> findTopSellingSortedByRevenue(@Param("search") String search);

    //  Combined filter (date, name, sortBy)
    @Query("SELECT li.product.name, SUM(li.quantity), SUM(li.quantity * li.product.price) " +
            "FROM LineItem li " +
            "WHERE li.purchase.createdAt >= :startDate AND LOWER(li.product.name) LIKE %:search% " +
            "GROUP BY li.product.name " +
            "ORDER BY " +
            "CASE WHEN :sortBy = 'name' THEN li.product.name END ASC, " +
            "CASE WHEN :sortBy = 'quantity' THEN SUM(li.quantity) END DESC, " +
            "CASE WHEN :sortBy = 'revenue' THEN SUM(li.quantity * li.product.price) END DESC")
    List<Object[]> findTopSellingByFilter(
            @Param("startDate") LocalDateTime startDate,
            @Param("search") String search,
            @Param("sortBy") String sortBy
    );

    @Query("SELECT FUNCTION('DATE_FORMAT', p.createdAt, '%Y-%m-%d'), SUM(p.total) " +
            "FROM Purchase p " +
            "WHERE p.createdAt >= :startDate AND (:status IS NULL OR p.status.label = :status) " +
            "GROUP BY FUNCTION('DATE_FORMAT', p.createdAt, '%Y-%m-%d') " +
            "ORDER BY FUNCTION('DATE_FORMAT', p.createdAt, '%Y-%m-%d')")
    List<Object[]> findRevenueSinceWithStatus(
            @Param("startDate") LocalDateTime startDate,
            @Param("status") String status
    );

    @Query("SELECT FUNCTION('DATE_FORMAT', p.createdAt, '%Y-%m'), SUM(p.total) " +
            "FROM Purchase p " +
            "WHERE (:status IS NULL OR p.status.label = :status) " +
            "GROUP BY FUNCTION('DATE_FORMAT', p.createdAt, '%Y-%m') " +
            "ORDER BY FUNCTION('DATE_FORMAT', p.createdAt, '%Y-%m')")
    List<Object[]> findMonthlyRevenueWithStatus(@Param("status") String status);

    @Query("SELECT DATE(p.createdAt), SUM(p.total) FROM Purchase p GROUP BY DATE(p.createdAt) ORDER BY DATE(p.createdAt)")
    List<Object[]> sumRevenueByDate();


}
