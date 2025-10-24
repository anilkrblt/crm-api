package com.anil.crm.services;

import com.anil.crm.domain.TicketPriority;
import com.anil.crm.domain.TicketStatus;
import com.anil.crm.web.models.TicketDto;

import java.util.List;


public interface TicketService {

    TicketDto getTicketById(Long id);

    List<TicketDto> getAllTickets();

    List<TicketDto> getTicketsByCustomerId(Long customerId);

    List<TicketDto> getTicketsByAgentId(Long agentId);

    List<TicketDto> getTicketsByStatus(TicketStatus status);

    List<TicketDto> getTicketsByPriority(TicketPriority priority);

    TicketDto createTicket(TicketDto ticketDto);

    TicketDto updateTicket(Long id, TicketDto ticketDto);

    void deleteTicketById(Long id);

    TicketDto updateTicketStatus(Long ticketId, TicketStatus status);
}