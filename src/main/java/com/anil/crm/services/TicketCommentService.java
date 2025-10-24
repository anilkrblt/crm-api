package com.anil.crm.services;

import com.anil.crm.domain.User;
import com.anil.crm.web.models.TicketCommentDto;

import java.util.List;

public interface TicketCommentService {


    List<TicketCommentDto> getCommentsByTicketId(Long ticketId);

    List<TicketCommentDto> getCommentsByAuthorId(Long authorId);

    TicketCommentDto addComment(TicketCommentDto commentDto, User author);

    TicketCommentDto updateComment(Long id, TicketCommentDto commentDto);

    void deleteCommentById(Long id);

}