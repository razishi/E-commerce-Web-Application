package com.example.demo.dto;

/**
 * DTO (Data Transfer Object) for representing daily revenue summary.
 * Typically used to transfer daily revenue statistics to the frontend (e.g., for charting or admin dashboards).
 */
public class DailyRevenueDTO {
    private String date;   // The date for which the revenue is calculated (e.g., "2024-07-08")
    private double total;  // Total revenue for that date


    /**
     * Constructor for initializing the DTO.
     *
     * @param date  the date as a String
     * @param total the total revenue amount for that day
     */
    public DailyRevenueDTO(String date, double total) {
        this.date = date;
        this.total = total;
    }

    /**
     * Gets the date associated with this revenue record.
     *
     * @return date as a string
     */
    public String getDate() {
        return date;
    }

    /**
     * Gets the total revenue value.
     *
     * @return total revenue
     */
    public double getTotal() {
        return total;
    }
}
