package com.anil.crm.services;

import com.anil.crm.domain.Role;
import com.anil.crm.domain.Ticket;
import com.anil.crm.domain.TicketComment;
import com.anil.crm.domain.User;
import com.anil.crm.exceptions.ResourceNotFoundException;
import com.anil.crm.repositories.TicketCommentRepository;
import com.anil.crm.repositories.TicketRepository;
import com.anil.crm.web.mappers.TicketCommentMapper;
import com.anil.crm.web.models.TicketCommentDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.*;

@ExtendWith(MockitoExtension.class)
class TicketCommentServiceImplTest {

    @Mock
    TicketCommentRepository ticketCommentRepository;
    @Mock
    TicketRepository ticketRepository;
    @Mock
    TicketCommentMapper ticketCommentMapper;

    @InjectMocks
    TicketCommentServiceImpl ticketCommentService;

    Ticket ticket;
    User authorUser;
    TicketComment comment1;
    TicketCommentDto commentDto1;
    Long ticketId = 1L;
    Long authorId = 1L;
    Long commentId1 = 1L;

    @BeforeEach
    void setUp() {
        authorUser = User.builder()
                .id(authorId)
                .email("author@test.com")
                .firstName("Test")
                .lastName("Author")
                .role(Role.CUSTOMER)
                .build();

        ticket = Ticket.builder()
                .id(ticketId)
                .subject("Test Ticket")
                .build();

        comment1 = TicketComment.builder()
                .id(commentId1)
                .ticket(ticket)
                .author(authorUser)
                .comment("This is a test comment")
                .createdAt(LocalDateTime.now().minusMinutes(10))
                .build();

        commentDto1 = TicketCommentDto.builder()
                .id(commentId1)
                .ticketId(ticketId)
                .comment("This is a test comment")
                .authorFirstName("Test")
                .authorLastName("Author")
                .authorRole(Role.CUSTOMER)
                .createdAt(comment1.getCreatedAt())
                .build();
    }

    @Test
    void getCommentsByTicketId() {
        given(ticketCommentRepository.findByTicketIdOrderByCreatedAtAsc(ticketId)).willReturn(List.of(comment1));
        given(ticketCommentMapper.ticketCommentToTicketCommentDto(comment1)).willReturn(commentDto1);

        List<TicketCommentDto> result = ticketCommentService.getCommentsByTicketId(ticketId);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(commentId1, result.get(0).getId());
        then(ticketCommentRepository).should().findByTicketIdOrderByCreatedAtAsc(ticketId);
        then(ticketCommentMapper).should().ticketCommentToTicketCommentDto(comment1);
    }

    @Test
    void getCommentsByAuthorId() {
        given(ticketCommentRepository.findByAuthorIdOrderByCreatedAtDesc(authorId)).willReturn(List.of(comment1));
        given(ticketCommentMapper.ticketCommentToTicketCommentDto(comment1)).willReturn(commentDto1);

        List<TicketCommentDto> result = ticketCommentService.getCommentsByAuthorId(authorId);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(authorUser.getFirstName(), result.get(0).getAuthorFirstName());
        then(ticketCommentRepository).should().findByAuthorIdOrderByCreatedAtDesc(authorId);
        then(ticketCommentMapper).should().ticketCommentToTicketCommentDto(comment1);
    }

    @Test
    void addComment() {
        TicketCommentDto dtoToSave = TicketCommentDto.builder()
                .ticketId(ticketId)
                .comment("New comment")
                .build();

        TicketComment transientComment = new TicketComment();
        transientComment.setComment(dtoToSave.getComment());
        transientComment.setTicket(ticket);
        transientComment.setAuthor(authorUser);

        TicketComment savedComment = new TicketComment();
        savedComment.setId(2L);
        savedComment.setTicket(ticket);
        savedComment.setAuthor(authorUser);
        savedComment.setComment(dtoToSave.getComment());
        savedComment.setCreatedAt(LocalDateTime.now());

        TicketCommentDto savedDto = TicketCommentDto.builder()
                .id(2L)
                .comment(dtoToSave.getComment())
                .authorFirstName(authorUser.getFirstName())
                .build();

        given(ticketRepository.findById(dtoToSave.getTicketId())).willReturn(Optional.of(ticket));
        given(ticketCommentRepository.save(any(TicketComment.class))).willReturn(savedComment);
        given(ticketCommentMapper.ticketCommentToTicketCommentDto(savedComment)).willReturn(savedDto);

        TicketCommentDto resultDto = ticketCommentService.addComment(dtoToSave, authorUser);

        assertNotNull(resultDto);
        assertEquals(2L, resultDto.getId());
        assertEquals(dtoToSave.getComment(), resultDto.getComment());
        assertEquals(authorUser.getFirstName(), resultDto.getAuthorFirstName());

        ArgumentCaptor<TicketComment> commentCaptor = ArgumentCaptor.forClass(TicketComment.class);
        then(ticketCommentRepository).should().save(commentCaptor.capture());
        assertEquals(ticket, commentCaptor.getValue().getTicket());
        assertEquals(authorUser, commentCaptor.getValue().getAuthor());
        assertEquals(dtoToSave.getComment(), commentCaptor.getValue().getComment());
    }

    @Test
    void addComment_TicketNotFound() {
        Long nonExistentTicketId = 99L;
        TicketCommentDto dtoToSave = TicketCommentDto.builder().ticketId(nonExistentTicketId).comment("test").build();
        given(ticketRepository.findById(nonExistentTicketId)).willReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> {
            ticketCommentService.addComment(dtoToSave, authorUser);
        });

        then(ticketRepository).should().findById(nonExistentTicketId);
        then(ticketCommentRepository).should(never()).save(any());
    }

    @Test
    void updateComment() {
        Long commentId = commentId1;
        String updatedText = "This is the updated comment text";
        TicketCommentDto updatesDto = TicketCommentDto.builder().comment(updatedText).build();

        given(ticketCommentRepository.findById(commentId)).willReturn(Optional.of(comment1));
        given(ticketCommentRepository.save(any(TicketComment.class))).willReturn(comment1);
        given(ticketCommentMapper.ticketCommentToTicketCommentDto(comment1)).willReturn(commentDto1);

        ticketCommentService.updateComment(commentId, updatesDto);

        ArgumentCaptor<TicketComment> commentCaptor = ArgumentCaptor.forClass(TicketComment.class);
        then(ticketCommentRepository).should().save(commentCaptor.capture());
        assertEquals(updatedText, commentCaptor.getValue().getComment());

        then(ticketCommentRepository).should().findById(commentId);
        then(ticketCommentMapper).should().ticketCommentToTicketCommentDto(comment1);
    }

    @Test
    void updateComment_NotFound() {
        Long nonExistentId = 99L;
        TicketCommentDto updatesDto = TicketCommentDto.builder().comment("update").build();
        given(ticketCommentRepository.findById(nonExistentId)).willReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> {
            ticketCommentService.updateComment(nonExistentId, updatesDto);
        });

        then(ticketCommentRepository).should().findById(nonExistentId);
        then(ticketCommentRepository).should(never()).save(any());
    }

    @Test
    void deleteCommentById() {
        given(ticketCommentRepository.existsById(commentId1)).willReturn(true);
        willDoNothing().given(ticketCommentRepository).deleteById(commentId1);

        assertDoesNotThrow(() -> ticketCommentService.deleteCommentById(commentId1));

        then(ticketCommentRepository).should().existsById(commentId1);
        then(ticketCommentRepository).should().deleteById(commentId1);
    }

    @Test
    void deleteCommentById_NotFound() {
        Long nonExistentId = 99L;
        given(ticketCommentRepository.existsById(nonExistentId)).willReturn(false);

        assertThrows(ResourceNotFoundException.class, () -> {
            ticketCommentService.deleteCommentById(nonExistentId);
        });

        then(ticketCommentRepository).should().existsById(nonExistentId);
        then(ticketCommentRepository).should(never()).deleteById(anyLong());
    }
}
