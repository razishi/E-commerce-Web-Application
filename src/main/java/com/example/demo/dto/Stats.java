package com.example.demo.dto;

/**
 * DTO (Data Transfer Object) that encapsulates statistical summary data
 * for the admin dashboard or analytics view.
 */
public class Stats {
    private long productCount; // Total number of products
    private long orderCount;   // Total number of orders (purchases)
    private long userCount;    // Total number of registered users

    /**
     * Constructs a Stats object with counts for products, orders, and users.
     *
     * @param productCount number of products in the system
     * @param orderCount   number of orders placed
     * @param userCount    number of registered user accounts
     */

    public Stats(long productCount, long orderCount, long userCount) {
        this.productCount = productCount;
        this.orderCount = orderCount;
        this.userCount = userCount;
    }

    /**
     * Returns the number of products.
     *
     * @return product count
     */
    public long getProductCount() {
        return productCount;
    }

    /**
     * Returns the number of orders.
     *
     * @return order count
     */

    public long getOrderCount() {
        return orderCount;
    }

    /**
     * Returns the number of users.
     *
     * @return user count
     */
    public long getUserCount() {
        return userCount;
    }
}
