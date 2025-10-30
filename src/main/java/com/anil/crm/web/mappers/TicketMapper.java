package com.anil.crm.web.mappers;

import com.anil.crm.domain.Ticket;
import com.anil.crm.web.models.TicketDto;
import org.mapstruct.*;


@Mapper(componentModel = "spring",
        uses = {CustomerMapper.class, DepartmentMapper.class, AgentMapper.class},
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface TicketMapper {


    @Mapping(target = "customer", source = "customer")
    @Mapping(target = "department", source = "department")
    @Mapping(target = "assignedAgent", source = "assignedAgent")
    TicketDto ticketToTicketDto(Ticket ticket);



    @Mapping(target = "customer", ignore = true)
    @Mapping(target = "department", ignore = true)
    @Mapping(target = "assignedAgent", ignore = true)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "version", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Ticket ticketDtoToTicket(TicketDto ticketDto);



    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "version", ignore = true)
    @Mapping(target = "customer", ignore = true)
    @Mapping(target = "department", ignore = true)
    @Mapping(target = "assignedAgent", ignore = true)
    void updateTicketFromDto(TicketDto ticketDto, @MappingTarget Ticket existingTicket);
}