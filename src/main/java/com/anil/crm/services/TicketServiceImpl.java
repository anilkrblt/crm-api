package com.anil.crm.services;

import com.anil.crm.repositories.TicketRepository;
import com.anil.crm.web.mappers.TicketMapper;
import com.anil.crm.web.models.TicketDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class TicketServiceImpl implements TicketService {

    private final TicketRepository ticketRepository;
    private final TicketMapper ticketMapper;

    @Override
    public Optional<TicketDto> getTicketById(Long id) {
        return Optional.empty();
    }

    @Override
    public List<TicketDto> getAllTickets() {
        return List.of();
    }

    @Override
    public List<TicketDto> getTicketsByCustomerId(Long customerId) {
        return List.of();
    }

    @Override
    public List<TicketDto> getTicketsByAgentId(Long agentId) {
        return List.of();
    }

    @Override
    public List<TicketDto> getTicketsByStatus(String status) {
        return List.of();
    }

    @Override
    public List<TicketDto> getTicketsByPriority(String priority) {
        return List.of();
    }

    @Override
    public TicketDto createTicket(TicketDto ticketDto) {
        return null;
    }

    @Override
    public TicketDto updateTicket(TicketDto ticketDto) {
        return null;
    }

    @Override
    public void deleteTicketById(Long id) {

    }

    @Override
    public TicketDto updateTicketStatus(Long ticketId, String status) {
        return null;
    }
}
