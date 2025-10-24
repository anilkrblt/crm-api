package com.anil.crm.services;

import com.anil.crm.domain.Ticket;
import com.anil.crm.domain.TicketPriority;
import com.anil.crm.domain.TicketStatus;
import com.anil.crm.exceptions.ResourceNotFoundException;
import com.anil.crm.repositories.TicketRepository;
import com.anil.crm.web.mappers.TicketMapper;
import com.anil.crm.web.models.TicketDto;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TicketServiceImpl implements TicketService {

    private static final Logger log = LoggerFactory.getLogger(TicketServiceImpl.class);

    private final TicketRepository ticketRepository;
    private final TicketMapper ticketMapper;

    @Override
    @Transactional(readOnly = true)
    public List<TicketDto> getAllTickets() {
        log.debug("Fetching all tickets");
        return ticketRepository
                .findAll()
                .stream()
                .map(ticketMapper::ticketToTicketDto)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public TicketDto getTicketById(Long id) {
        log.debug("Fetching ticket by id: {}", id);
        return ticketRepository
                .findById(id)
                .map(ticketMapper::ticketToTicketDto)
                .orElseThrow(() -> {
                    log.warn("Ticket not found with id: {}", id);
                    return new ResourceNotFoundException("Ticket not found with id: " + id);
                });
    }


    @Override
    @Transactional(readOnly = true)
    public List<TicketDto> getTicketsByCustomerId(Long customerId) {
        log.debug("Fetching tickets for customerId: {}", customerId);
        return ticketRepository
                .findTicketsByCustomerId(customerId)
                .stream()
                .map(ticketMapper::ticketToTicketDto)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<TicketDto> getTicketsByAgentId(Long agentId) {
        log.debug("Fetching tickets for agentId: {}", agentId);
        return ticketRepository
                .findTicketsByAgentId(agentId)
                .stream()
                .map(ticketMapper::ticketToTicketDto)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<TicketDto> getTicketsByStatus(TicketStatus status) {
        log.debug("Fetching tickets by status: {}", status);
        return ticketRepository
                .findTicketsByStatus(status)
                .stream()
                .map(ticketMapper::ticketToTicketDto)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<TicketDto> getTicketsByPriority(TicketPriority priority) {
        log.debug("Fetching tickets by priority: {}", priority);
        return ticketRepository
                .getTicketsByPriority(priority)
                .stream()
                .map(ticketMapper::ticketToTicketDto)
                .toList();
    }

    @Override
    @Transactional
    public TicketDto createTicket(TicketDto ticketDto) {
        Ticket ticket = ticketMapper.ticketDtoToTicket(ticketDto);

        ticket.setId(null);
        ticket.setCreatedAt(LocalDateTime.now());
        ticket.setUpdatedAt(LocalDateTime.now());

        Ticket savedTicket = ticketRepository.save(ticket);
        log.info("New ticket created with id: {}", savedTicket.getId());
        return ticketMapper.ticketToTicketDto(savedTicket);
    }

    @Override
    @Transactional
    public TicketDto updateTicket(Long id, TicketDto ticketDto) {

        Ticket existingTicket = ticketRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Failed to update. Ticket not found with id: {}", id);
                    return new ResourceNotFoundException("Ticket not found with id: " + id);
                });

        ticketMapper.updateTicketFromDto(ticketDto, existingTicket);
        existingTicket.setUpdatedAt(LocalDateTime.now());

        Ticket updatedTicket = ticketRepository.save(existingTicket);
        log.info("Ticket updated with id: {}", updatedTicket.getId());
        return ticketMapper.ticketToTicketDto(updatedTicket);
    }

    @Override
    @Transactional
    public TicketDto updateTicketStatus(Long ticketId, TicketStatus status) {
        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> {
                    log.warn("Failed to update status. Ticket not found with id: {}", ticketId);
                    return new ResourceNotFoundException("Ticket not found with id: " + ticketId);
                });

        ticket.setStatus(status);

        Ticket updatedTicket = ticketRepository.save(ticket);
        log.info("Ticket status updated for id: {}", updatedTicket.getId());
        return ticketMapper.ticketToTicketDto(updatedTicket);
    }


    @Override
    @Transactional
    public void deleteTicketById(Long id) {
        if (!ticketRepository.existsById(id)) {
            log.warn("Failed to delete. Ticket not found with id: {}", id);
            throw new ResourceNotFoundException("Ticket not found with id: " + id);
        }

        ticketRepository.deleteById(id);
        log.info("Ticket deleted with id: {}", id);
    }
}