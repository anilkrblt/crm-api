package com.anil.crm.web.mappers;

import com.anil.crm.domain.TicketComment;
import com.anil.crm.web.models.TicketCommentDto;
import org.mapstruct.Mapper;

@Mapper
public interface TicketCommentMapper {
    TicketComment ticketCommentDtoToTicketComment(TicketCommentDto ticketCommentDto);

    TicketCommentDto ticketCommentToTicketCommentDto(TicketComment ticketComment);
}
