package com.example.demo.repository;

import com.example.demo.model.StatusType;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

/**
 * Repository interface for managing StatusType entities.
 * Provides methods to retrieve status types by label or fetch all status types.
 */
public interface StatusTypeRepository extends JpaRepository<StatusType, Long> {

    /**
     * Finds a status type by its unique label.
     *
     * @param label The status label (e.g., "Pending", "Completed").
     * @return The StatusType entity matching the given label.
     */
    StatusType findByLabel(String label);

    /**
     * Retrieves all available status types.
     *
     * @return A list of all StatusType entities.
     */
    List<StatusType> findAll();
}
