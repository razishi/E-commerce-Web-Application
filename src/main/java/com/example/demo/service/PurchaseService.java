package com.example.demo.service;

import com.example.demo.dto.DailyRevenueDTO;
import com.example.demo.repository.PurchaseRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service class for managing business logic related to purchases.
 * Handles revenue calculations and aggregates purchase data.
 */
@Service
public class PurchaseService {

    private final PurchaseRepository purchaseRepository;

    /**
     * Constructor-based dependency injection for PurchaseRepository.
     *
     * @param purchaseRepository The repository handling purchase data access.
     */
    public PurchaseService(PurchaseRepository purchaseRepository) {
        this.purchaseRepository = purchaseRepository;
    }

    /**
     * Retrieves a list of daily revenue figures.
     * Converts raw query results from PurchaseRepository into a list of DTOs.
     *
     * @return A list of DailyRevenueDTO objects containing date and total revenue per day.
     */
    public List<DailyRevenueDTO> getDailyRevenue() {
        List<Object[]> results = purchaseRepository.sumRevenueByDate();
        return results.stream()
                .map(row -> new DailyRevenueDTO(
                        row[0].toString(),                          // Date as String
                        ((BigDecimal) row[1]).doubleValue()))      // Total revenue as double
                .collect(Collectors.toList());
    }
}
