package com.anil.crm.web.mappers;

import com.anil.crm.domain.Ticket;
import com.anil.crm.web.models.TicketDto;
import org.mapstruct.*; // Import necessary annotations

/**
 * Maps between Ticket entity and TicketDto.
 * Uses other mappers for nested objects (Customer, Department, Agent).
 */
@Mapper(componentModel = "spring",
        uses = {CustomerMapper.class, DepartmentMapper.class, AgentMapper.class}, // 1. Add dependencies
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface TicketMapper {

    /**
     * Converts Ticket Entity to TicketDto (for responses).
     */
    // 2. Update source/target for nested objects
    @Mapping(target = "customer", source = "customer")
    @Mapping(target = "department", source = "department")
    @Mapping(target = "assignedAgent", source = "assignedAgent") // Map new field
    TicketDto ticketToTicketDto(Ticket ticket);

    /**
     * Converts TicketDto to Ticket Entity (partial, for requests).
     * Associated entities (customer, department, assignedAgent) should be
     * fetched and set manually in the service layer based on IDs from the DTO.
     */
    // 3. Ignore complex nested objects during DTO -> Entity mapping
    @Mapping(target = "customer", ignore = true)
    @Mapping(target = "department", ignore = true)
    @Mapping(target = "assignedAgent", ignore = true)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "version", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Ticket ticketDtoToTicket(TicketDto ticketDto);

    /**
     * Updates an existing Ticket entity from a TicketDto.
     * Ignores ID, timestamps, and complex associations.
     * Handles updates for subject, description, status, priority.
     * Assigned agent and department updates should be handled separately in the service if needed.
     */
    // 4. Update ignored fields for the update method
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true) // Will be set manually or via @UpdateTimestamp
    @Mapping(target = "version", ignore = true)
    @Mapping(target = "customer", ignore = true) // Don't change customer during general update
    @Mapping(target = "department", ignore = true) // Department shouldn't usually change easily
    @Mapping(target = "assignedAgent", ignore = true) // Agent assignment is a separate action
    void updateTicketFromDto(TicketDto ticketDto, @MappingTarget Ticket existingTicket);
}