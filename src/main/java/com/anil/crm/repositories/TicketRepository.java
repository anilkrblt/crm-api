package com.anil.crm.repositories;

import com.anil.crm.domain.Department; // Import Department if needed for a method signature
import com.anil.crm.domain.Ticket;
import com.anil.crm.domain.TicketPriority;
import com.anil.crm.domain.TicketStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TicketRepository extends JpaRepository<Ticket, Long> {

    // --- Existing Methods (Still Valid) ---
    List<Ticket> findTicketsByCustomerId(Long customerId);
    List<Ticket> findTicketsByStatus(TicketStatus status);
    List<Ticket> getTicketsByPriority(TicketPriority priority); // Consider renaming to findTicketsByPriority for consistency
    boolean existsByCustomerId(Long customerId);

    // --- Updated Methods (Reflecting Entity Changes) ---

    /**
     * Finds tickets assigned to a specific agent.
     * Renamed from findTicketsByAgentId.
     */
    List<Ticket> findTicketsByAssignedAgentId(Long agentId);

    /**
     * Checks if any tickets are assigned to a specific agent.
     * Renamed from existsByAgentId. Needed for Agent deletion check.
     */
    boolean existsByAssignedAgentId(Long agentId);

    // --- New Methods (For Department Relationship) ---

    /**
     * Finds tickets belonging to a specific department.
     */
    List<Ticket> findTicketsByDepartmentId(Long departmentId);
    // Alternatively, you could find by Department object:
    // List<Ticket> findTicketsByDepartment(Department department);

    /**
     * Checks if any tickets belong to a specific department.
     * Needed for Department deletion check.
     */
    boolean existsByDepartmentId(Long departmentId);

}