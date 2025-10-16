package com.anil.crm.services;

import com.anil.crm.web.models.TicketDto;
import java.util.List;
import java.util.Optional;

public interface TicketService {

    Optional<TicketDto> getTicketById(Long id);

    List<TicketDto> getAllTickets();

    List<TicketDto> getTicketsByCustomerId(Long customerId);

    List<TicketDto> getTicketsByAgentId(Long agentId);

    List<TicketDto> getTicketsByStatus(String status);

    List<TicketDto> getTicketsByPriority(String priority);

    TicketDto createTicket(TicketDto ticketDto);

    TicketDto updateTicket(TicketDto ticketDto);

    void deleteTicketById(Long id);

    // opsiyonel
    TicketDto updateTicketStatus(Long ticketId, String status);
}
