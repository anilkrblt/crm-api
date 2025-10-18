package com.anil.crm.services;

import com.anil.crm.domain.TicketComment;
import com.anil.crm.repositories.TicketCommentRepository;
import com.anil.crm.web.mappers.TicketCommentMapper;
import com.anil.crm.web.models.TicketCommentDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class TicketCommentServiceImpl implements TicketCommentService {

    private final TicketCommentRepository ticketCommentRepository;
    private final TicketCommentMapper ticketCommentMapper;

    @Override
    public Optional<TicketCommentDto> getCommentById(Long id) {
        return ticketCommentRepository.findById(id)
                .map(ticketCommentMapper::ticketCommentToTicketCommentDto);
    }

    @Override
    public List<TicketCommentDto> getCommentsByTicketId(Long ticketId) {
        List<TicketComment> ticketComments = ticketCommentRepository.findAll();
        var ticketCommentDtos = ticketComments.stream().filter(ticketComment -> {
            if (ticketComment.getTicket().getId().equals(ticketId)) {
                return true;
            }
            return false;
        }).map(ticketCommentMapper::ticketCommentToTicketCommentDto).toList();
        return ticketCommentDtos;

    }

    @Override
    public List<TicketCommentDto> getCommentsByAgentId(Long agentId) {
        List<TicketComment> ticketComments = ticketCommentRepository.findAll();
        var ticketCommentDtos = ticketComments.stream().filter(ticketComment -> {
            if (ticketComment.getTicket().getAgent().getId().equals(agentId)) {
                return true;
            }
            return false;
        }).map(ticketCommentMapper::ticketCommentToTicketCommentDto).toList();
        return ticketCommentDtos;

    }

    @Override
    public List<TicketCommentDto> getCommentsByCustomerId(Long customerId) {
        List<TicketComment> ticketComments = ticketCommentRepository.findAll();
        var ticketCommentDtos = ticketComments.stream().filter(ticketComment -> {
            if (ticketComment.getTicket().getCustomer().getId().equals(customerId)) {
                return true;
            }
            return false;
        }).map(ticketCommentMapper::ticketCommentToTicketCommentDto).toList();
        return ticketCommentDtos;
    }

    @Override
    public TicketCommentDto addComment(TicketCommentDto commentDto) {
        return ticketCommentMapper.ticketCommentToTicketCommentDto(ticketCommentRepository.save(ticketCommentMapper.ticketCommentDtoToTicketComment(commentDto)));
    }

    @Override
    public TicketCommentDto updateComment(TicketCommentDto commentDto) {
        TicketComment ticketComment = ticketCommentRepository.findById(commentDto.getId()).orElse(null);
        if (ticketComment != null) {
            ticketComment.setComment(commentDto.getComment());
            ticketCommentRepository.save(ticketComment);
        }
        return ticketCommentMapper.ticketCommentToTicketCommentDto(ticketComment);
    }

    @Override
    public void deleteCommentById(Long id) {
        ticketCommentRepository.deleteById(id);
    }
}
