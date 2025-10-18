package com.anil.crm.repositories;

import com.anil.crm.domain.Ticket;
import com.anil.crm.web.models.TicketDto;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;

public interface TicketRepository extends JpaRepository<Ticket, Long> {
    List<Ticket> findTicketsByCustomerId(Long customerId);

    List<Ticket> findTicketsByAgentId(Long agentId);

    List<Ticket> findTicketsByStatus(String status);

    List<Ticket> getTicketsByPriority(String priority);
}
