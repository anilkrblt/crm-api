package com.anil.crm.services;

import com.anil.crm.domain.Ticket;
import com.anil.crm.repositories.TicketRepository;
import com.anil.crm.web.mappers.TicketMapper;
import com.anil.crm.web.models.TicketDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TicketServiceImpl implements TicketService {

    private final TicketRepository ticketRepository;
    private final TicketMapper ticketMapper;

    @Override
    public List<TicketDto> getAllTickets() {
        return ticketRepository
                .findAll()
                .stream()
                .map(ticketMapper::ticketToTicketDto)
                .toList();
    }

    @Override
    public Optional<TicketDto> getTicketById(Long id) {
        return ticketRepository
                .findById(id)
                .map(ticketMapper::ticketToTicketDto);
    }


    @Override
    public List<TicketDto> getTicketsByCustomerId(Long customerId) {
        return ticketRepository
                .findTicketsByCustomerId(customerId)
                .stream()
                .map(ticketMapper::ticketToTicketDto)
                .toList();
    }

    @Override
    public List<TicketDto> getTicketsByAgentId(Long agentId) {
        return ticketRepository
                .findTicketsByAgentId(agentId)
                .stream()
                .map(ticketMapper::ticketToTicketDto)
                .toList();
    }

    @Override
    public List<TicketDto> getTicketsByStatus(String status) {
        return ticketRepository
                .findTicketsByStatus(status)
                .stream()
                .map(ticketMapper::ticketToTicketDto)
                .toList();
    }

    @Override
    public List<TicketDto> getTicketsByPriority(String priority) {
        return ticketRepository
                .getTicketsByPriority(priority)
                .stream()
                .map(ticketMapper::ticketToTicketDto)
                .toList();
    }

    @Override
    public TicketDto createTicket(TicketDto ticketDto) {
        return ticketMapper.ticketToTicketDto(ticketRepository.save(ticketMapper.ticketDtoToTicket(ticketDto)));
    }

    @Override
    public TicketDto updateTicket(TicketDto ticketDto) {

        Ticket ticket = ticketRepository.findById(ticketDto.getId()).orElse(null);
        if (ticket != null) {
            ticket.setStatus(ticketDto.getStatus());
            ticket.setUpdatedAt(LocalDateTime.now());
            ticket.setAgent(ticketDto.getAgent());
            ticket.setPriority(ticketDto.getPriority());
            ticket.setDescription(ticketDto.getDescription());
            ticket.setSubject(ticketDto.getSubject());
            ticket.setCustomer(ticketDto.getCustomer());
            ticketRepository.save(ticket);
        }
        return ticketMapper.ticketToTicketDto(ticket);
    }

    @Override
    public void deleteTicketById(Long id) {
        ticketRepository.deleteById(id);
    }

    @Override
    public TicketDto updateTicketStatus(Long ticketId, String status) {
        Ticket ticket = ticketRepository.findById(ticketId).orElse(null);
        if (ticket != null) {
            ticket.setStatus(status);
            ticketRepository.save(ticket);
        }
        return ticketMapper.ticketToTicketDto(ticket);
    }
}
