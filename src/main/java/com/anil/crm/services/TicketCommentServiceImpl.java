package com.anil.crm.services;

import com.anil.crm.domain.Ticket;
import com.anil.crm.domain.TicketComment;
import com.anil.crm.domain.User;
import com.anil.crm.exceptions.ResourceNotFoundException;
import com.anil.crm.repositories.TicketCommentRepository;
import com.anil.crm.repositories.TicketRepository; // 1. YENİ BAĞIMLILIK (Ticket'ı bulmak için)
import com.anil.crm.web.mappers.TicketCommentMapper;
import com.anil.crm.web.models.TicketCommentDto;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TicketCommentServiceImpl implements TicketCommentService {

    private static final Logger log = LoggerFactory.getLogger(TicketCommentServiceImpl.class);

    private final TicketCommentRepository ticketCommentRepository;
    private final TicketRepository ticketRepository;
    private final TicketCommentMapper ticketCommentMapper;

    @Override
    @Transactional(readOnly = true)
    public List<TicketCommentDto> getCommentsByTicketId(Long ticketId) {
        log.debug("Fetching comments for ticketId: {}", ticketId);

        return ticketCommentRepository.findByTicketIdOrderByCreatedAtAsc(ticketId)
                .stream()
                .map(ticketCommentMapper::ticketCommentToTicketCommentDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<TicketCommentDto> getCommentsByAuthorId(Long authorId) {
        log.debug("Fetching comments for authorId: {}", authorId);

        return ticketCommentRepository.findByAuthorIdOrderByCreatedAtDesc(authorId)
                .stream()
                .map(ticketCommentMapper::ticketCommentToTicketCommentDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public TicketCommentDto addComment(TicketCommentDto commentDto, User author) {
        log.info("Adding new comment to ticketId: {} by user: {}", commentDto.getTicketId(), author.getEmail());

        Ticket ticket = ticketRepository.findById(commentDto.getTicketId())
                .orElseThrow(() -> new ResourceNotFoundException("Ticket not found with id: " + commentDto.getTicketId()));

        TicketComment newComment = new TicketComment();
        newComment.setComment(commentDto.getComment());
        newComment.setTicket(ticket);
        newComment.setAuthor(author);

        TicketComment savedComment = ticketCommentRepository.save(newComment);
        log.info("Comment created with id: {}", savedComment.getId());


        return ticketCommentMapper.ticketCommentToTicketCommentDto(savedComment);
    }

    @Override
    @Transactional
    public TicketCommentDto updateComment(Long id, TicketCommentDto commentDto) {
        log.info("Updating comment with id: {}", id);

        TicketComment existingComment = ticketCommentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Comment not found with id: " + id));

        existingComment.setComment(commentDto.getComment());

        TicketComment updatedComment = ticketCommentRepository.save(existingComment);

        return ticketCommentMapper.ticketCommentToTicketCommentDto(updatedComment);
    }

    @Override
    @Transactional
    public void deleteCommentById(Long id) {
        log.info("Deleting comment with id: {}", id);

        if (!ticketCommentRepository.existsById(id)) {
            log.warn("Failed to delete. Comment not found with id: {}", id);
            throw new ResourceNotFoundException("Comment not found with id: " + id);
        }
        ticketCommentRepository.deleteById(id);
    }
}