package com.anil.crm.services;

import com.anil.crm.web.models.TicketCommentDto;
import java.util.List;
import java.util.Optional;

public interface TicketCommentService {

    Optional<TicketCommentDto> getCommentById(Long id);

    List<TicketCommentDto> getCommentsByTicketId(Long ticketId);

    List<TicketCommentDto> getCommentsByAgentId(Long agentId);

    List<TicketCommentDto> getCommentsByCustomerId(Long customerId);

    TicketCommentDto addComment(TicketCommentDto commentDto);

    TicketCommentDto updateComment(TicketCommentDto commentDto);

    void deleteCommentById(Long id);
}
