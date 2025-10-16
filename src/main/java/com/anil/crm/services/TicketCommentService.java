package com.anil.crm.services;

import com.anil.crm.web.models.TicketCommentDto;
import java.util.List;

public interface TicketCommentService {

    TicketCommentDto getCommentById(Long id);

    List<TicketCommentDto> getCommentsByTicketId(Long ticketId);

    List<TicketCommentDto> getCommentsByAgentId(Long agentId);

    List<TicketCommentDto> getCommentsByCustomerId(Long customerId);

    TicketCommentDto addComment(TicketCommentDto commentDto);

    TicketCommentDto updateComment(TicketCommentDto commentDto);

    void deleteCommentById(Long id);
}
