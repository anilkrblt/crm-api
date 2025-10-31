package com.anil.crm.services;


import com.anil.crm.domain.*;
import com.anil.crm.exceptions.ResourceNotFoundException;
import com.anil.crm.repositories.*;
import com.anil.crm.web.mappers.TicketMapper;
import com.anil.crm.web.models.TicketDto;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TicketServiceImpl implements TicketService {

    private static final Logger log = LoggerFactory.getLogger(TicketServiceImpl.class);

    private final TicketRepository ticketRepository;
    private final CustomerRepository customerRepository;
    private final DepartmentRepository departmentRepository;
    private final AgentRepository agentRepository;
    private final UserRepository userRepository;
    private final TicketMapper ticketMapper;

    @Override
    @Transactional(readOnly = true)
    public List<TicketDto> getAllTickets() {
        log.debug("Fetching all tickets");
        return ticketRepository.findAll()
                .stream()
                .map(ticketMapper::ticketToTicketDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public TicketDto getTicketById(Long id) {
        log.debug("Fetching ticket by id: {}", id);
        return ticketRepository.findById(id)
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
        return ticketRepository.findTicketsByCustomerId(customerId)
                .stream()
                .map(ticketMapper::ticketToTicketDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<TicketDto> getTicketsByAssignedAgentId(Long agentId) {
        log.debug("Fetching tickets assigned to agentId: {}", agentId);
        return ticketRepository.findTicketsByAssignedAgentId(agentId)
                .stream()
                .map(ticketMapper::ticketToTicketDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<TicketDto> getTicketsByDepartmentId(Long departmentId) {
        log.debug("Fetching tickets for departmentId: {}", departmentId);
        return ticketRepository.findTicketsByDepartmentId(departmentId)
                .stream()
                .map(ticketMapper::ticketToTicketDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<TicketDto> getTicketsByStatus(TicketStatus status) {
        log.debug("Fetching tickets by status: {}", status);
        return ticketRepository.findTicketsByStatus(status)
                .stream()
                .map(ticketMapper::ticketToTicketDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<TicketDto> getTicketsByPriority(TicketPriority priority) {
        log.debug("Fetching tickets by priority: {}", priority);
        return ticketRepository.getTicketsByPriority(priority)
                .stream()
                .map(ticketMapper::ticketToTicketDto)
                .collect(Collectors.toList());
    }


    @Override
    @Transactional
    public TicketDto createTicket(TicketDto ticketDto) {
        log.info("Creating new ticket with subject: {}", ticketDto.getSubject());

        Customer customer = customerRepository.findById(ticketDto.getCustomer().getId())
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found with id: " + ticketDto.getCustomer().getId()));

        Department department = departmentRepository.findById(ticketDto.getDepartment().getId())
                .orElseThrow(() -> new ResourceNotFoundException("Department not found with id: " + ticketDto.getDepartment().getId()));

        Agent assignedAgent = null;
        if (ticketDto.getAssignedAgent() != null && ticketDto.getAssignedAgent().getId() != null) {
            assignedAgent = agentRepository.findById(ticketDto.getAssignedAgent().getId())
                    .orElseThrow(() -> new ResourceNotFoundException("Assigned agent not found with id: " + ticketDto.getAssignedAgent().getId()));
        }

        Ticket ticket = ticketMapper.ticketDtoToTicket(ticketDto);
        ticket.setId(null);
        ticket.setCustomer(customer);
        ticket.setDepartment(department);
        ticket.setAssignedAgent(assignedAgent);
        Ticket savedTicket = ticketRepository.save(ticket);
        log.info("New ticket created with id: {}", savedTicket.getId());

        return ticketMapper.ticketToTicketDto(savedTicket);
    }

    @Override
    @Transactional
    public TicketDto updateTicket(Long id, TicketDto ticketDto) {
        log.info("Updating ticket with id: {}", id);

        Ticket existingTicket = ticketRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Failed to update. Ticket not found with id: {}", id);
                    return new ResourceNotFoundException("Ticket not found with id: " + id);
                });

        ticketMapper.updateTicketFromDto(ticketDto, existingTicket);

        if (ticketDto.getDepartment() != null && ticketDto.getDepartment().getId() != null &&
                (existingTicket.getDepartment() == null || !existingTicket.getDepartment().getId().equals(ticketDto.getDepartment().getId()))) {
            Department newDepartment = departmentRepository.findById(ticketDto.getDepartment().getId())
                    .orElseThrow(() -> new ResourceNotFoundException("Department not found with id: " + ticketDto.getDepartment().getId()));
            existingTicket.setDepartment(newDepartment);
            log.info("Ticket {} department updated to {}", id, newDepartment.getName());
        }

        Agent newAssignedAgent = null;
        if (ticketDto.getAssignedAgent() != null && ticketDto.getAssignedAgent().getId() != null) {
            newAssignedAgent = agentRepository.findById(ticketDto.getAssignedAgent().getId())
                    .orElseThrow(() -> new ResourceNotFoundException("Assigned agent not found with id: " + ticketDto.getAssignedAgent().getId()));
        }

        Long existingAgentId = (existingTicket.getAssignedAgent() != null) ? existingTicket.getAssignedAgent().getId() : null;
        Long newAgentId = (newAssignedAgent != null) ? newAssignedAgent.getId() : null;

        if (!java.util.Objects.equals(existingAgentId, newAgentId)) {
            existingTicket.setAssignedAgent(newAssignedAgent);
            log.info("Ticket {} assigned agent updated to Agent ID: {}", id, newAgentId != null ? newAgentId : "null");
        }

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

        log.info("Updating status for ticket id: {} from {} to {}", ticketId, ticket.getStatus(), status);
        ticket.setStatus(status);

        Ticket updatedTicket = ticketRepository.save(ticket);
        log.info("Ticket status updated successfully for id: {}", updatedTicket.getId());
        return ticketMapper.ticketToTicketDto(updatedTicket);
    }

    @Transactional
    @Override
    public TicketDto assignAgentToTicket(Long id, Long agentId) {


        Ticket ticket = ticketRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("id with ticket " + id + "doesnt exist"));

        Agent agent = agentRepository.findById(agentId)
                .orElseThrow(() -> new ResourceNotFoundException("id with agent " + agentId + "doesnt exist"));


        ticket.setAssignedAgent(agent);
        Ticket updatedTicket = ticketRepository.save(ticket);

        return ticketMapper.ticketToTicketDto(updatedTicket);
    }


    @Override
    @Transactional
    public void deleteTicketById(Long id) {
        log.info("Attempting to delete ticket with id: {}", id);
        if (!ticketRepository.existsById(id)) {
            log.warn("Failed to delete. Ticket not found with id: {}", id);
            throw new ResourceNotFoundException("Ticket not found with id: " + id);
        }

        ticketRepository.deleteById(id);
        log.info("Ticket deleted successfully with id: {}", id);
    }


}