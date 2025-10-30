package com.anil.crm.repositories;

import com.anil.crm.domain.Ticket;
import com.anil.crm.domain.TicketPriority;
import com.anil.crm.domain.TicketStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TicketRepository extends JpaRepository<Ticket, Long> {

    List<Ticket> findTicketsByCustomerId(Long customerId);
    List<Ticket> findTicketsByStatus(TicketStatus status);
    List<Ticket> getTicketsByPriority(TicketPriority priority);
    boolean existsByCustomerId(Long customerId);

    List<Ticket> findTicketsByAssignedAgentId(Long agentId);


    boolean existsByAssignedAgentId(Long agentId);


    List<Ticket> findTicketsByDepartmentId(Long departmentId);

    boolean existsByDepartmentId(Long departmentId);

}