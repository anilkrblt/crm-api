package com.anil.crm.web.mappers;

import com.anil.crm.domain.Ticket;
import com.anil.crm.web.models.TicketDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;


@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface TicketMapper {

    Ticket ticketDtoToTicket(TicketDto ticketDto);
    TicketDto ticketToTicketDto(Ticket ticket);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    void updateTicketFromDto(TicketDto ticketDto, @MappingTarget Ticket existingTicket);
}