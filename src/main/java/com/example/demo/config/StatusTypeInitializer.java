package com.example.demo.config;

import com.example.demo.model.StatusType;
import com.example.demo.repository.StatusTypeRepository;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * This component initializes default order status types (e.g., Pending, Shipped)
 * into the database when the application starts, but only if none exist.
 */
@Component
public class StatusTypeInitializer {

    private final StatusTypeRepository statusTypeRepository;

    /**
     * Constructor injection of the StatusTypeRepository.
     *
     * @param statusTypeRepository repository used to interact with the status_type table
     */
    public StatusTypeInitializer(StatusTypeRepository statusTypeRepository) {
        this.statusTypeRepository = statusTypeRepository;
    }

    /**
     * This method runs automatically after the bean is constructed (@PostConstruct).
     * It checks if the status type table is empty, and if so, inserts default statuses.
     */

    @PostConstruct
    public void initStatuses() {
        // Only insert default statuses if the table is empty
        if (statusTypeRepository.count() == 0) {
            List<String> statuses = List.of("Pending", "Shipped", "Delivered", "Cancelled");

            // Save each status label as a new StatusType entity
            for (String label : statuses) {
                statusTypeRepository.save(new StatusType(label));
            }
        }
    }
}
