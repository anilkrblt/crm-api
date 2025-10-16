package com.anil.crm.web.mappers;

import com.anil.crm.domain.Ticket;
import com.anil.crm.web.models.TicketDto;
import org.mapstruct.Mapper;

@Mapper
public interface TicketMapper {
    Ticket ticketDtoToTicket(TicketDto ticketDto);
    TicketDto ticketToTicketDto(Ticket ticket);

}
