package com.anil.crm.web.mappers;

import com.anil.crm.domain.TicketComment;
import com.anil.crm.web.models.TicketCommentDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;


@Mapper(componentModel = "spring")
public interface TicketCommentMapper {


    @Mapping(target = "authorFirstName", source = "author.firstName")
    @Mapping(target = "authorLastName", source = "author.lastName")
    @Mapping(target = "authorRole", source = "author.role")
    @Mapping(target = "ticketId", source = "ticket.id")
    TicketCommentDto ticketCommentToTicketCommentDto(TicketComment ticketComment);


    @Mapping(target = "ticket", ignore = true)
    @Mapping(target = "author", ignore = true)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "version", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    TicketComment ticketCommentDtoToTicketComment(TicketCommentDto ticketCommentDto);
}