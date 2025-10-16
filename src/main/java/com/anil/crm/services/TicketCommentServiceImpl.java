package com.anil.crm.services;

import com.anil.crm.repositories.TicketCommentRepository;
import com.anil.crm.web.mappers.TicketCommentMapper;
import com.anil.crm.web.models.TicketCommentDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TicketCommentServiceImpl implements TicketCommentService {

    private final TicketCommentRepository ticketCommentRepository;
    private final TicketCommentMapper ticketCommentMapper;

    @Override
    public TicketCommentDto getCommentById(Long id) {
        return null;
    }

    @Override
    public List<TicketCommentDto> getCommentsByTicketId(Long ticketId) {
        return List.of();
    }

    @Override
    public List<TicketCommentDto> getCommentsByAgentId(Long agentId) {
        return List.of();
    }

    @Override
    public List<TicketCommentDto> getCommentsByCustomerId(Long customerId) {
        return List.of();
    }

    @Override
    public TicketCommentDto addComment(TicketCommentDto commentDto) {
        return null;
    }

    @Override
    public TicketCommentDto updateComment(TicketCommentDto commentDto) {
        return null;
    }

    @Override
    public void deleteCommentById(Long id) {

    }
}
