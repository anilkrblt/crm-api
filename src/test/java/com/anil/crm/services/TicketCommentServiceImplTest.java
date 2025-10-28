package com.anil.crm.services;

import com.anil.crm.domain.*; // Import your domain classes
import com.anil.crm.exceptions.ResourceNotFoundException;
import com.anil.crm.repositories.TicketCommentRepository;
import com.anil.crm.repositories.TicketRepository;
import com.anil.crm.web.mappers.TicketCommentMapper;
import com.anil.crm.web.models.TicketCommentDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor; // To capture arguments
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Ticket Comment Service Implementation Unit Tests")
class TicketCommentServiceImplTest {

    @Mock // Mock dependencies
    TicketCommentRepository ticketCommentRepository;
    @Mock
    TicketRepository ticketRepository;
    @Mock
    TicketCommentMapper ticketCommentMapper;

    @InjectMocks // Inject mocks into the service
    TicketCommentServiceImpl ticketCommentService;

    // Test Data
    TicketComment comment1, comment2;
    TicketCommentDto commentDto1, commentDto2;
    Ticket ticket;
    User authorUser;
    Long commentId1 = 1L;
    Long commentId2 = 2L;
    Long ticketId = 1L;
    Long authorId = 1L;

    @BeforeEach
    void setUp() {
        // --- Setup User (Author) ---
        authorUser = User.builder()
                .id(authorId)
                .email("author@test.com")
                .firstName("Test")
                .lastName("Author")
                .role(Role.CUSTOMER)
                .build();

        // --- Setup Ticket ---
        ticket = Ticket.builder()
                .id(ticketId)
                .subject("Test Ticket")
                .build(); // Add other necessary fields if needed

        // --- Setup Comments (Entities) ---
        comment1 = TicketComment.builder()
                .id(commentId1)
                .ticket(ticket)
                .author(authorUser)
                .comment("First comment")
                .createdAt(LocalDateTime.now().minusMinutes(10))
                .build();
        comment2 = TicketComment.builder()
                .id(commentId2)
                .ticket(ticket)
                .author(authorUser) // Same author, different comment
                .comment("Second comment")
                .createdAt(LocalDateTime.now().minusMinutes(5))
                .build();

        // --- Setup Comment DTOs ---
        commentDto1 = TicketCommentDto.builder()
                .id(commentId1)
                .ticketId(ticketId)
                .comment("First comment")
                .authorFirstName("Test")
                .authorLastName("Author")
                .authorRole(Role.CUSTOMER)
                .createdAt(comment1.getCreatedAt())
                .build();
        commentDto2 = TicketCommentDto.builder()
                .id(commentId2)
                .ticketId(ticketId)
                .comment("Second comment")
                .authorFirstName("Test")
                .authorLastName("Author")
                .authorRole(Role.CUSTOMER)
                .createdAt(comment2.getCreatedAt())
                .build();
    }

    @Test
    @DisplayName("Get Comments By Ticket ID - Success")
    void getCommentsByTicketId_shouldReturnListOfCommentDtos() {
        // Given
        given(ticketCommentRepository.findByTicketIdOrderByCreatedAtAsc(ticketId)).willReturn(List.of(comment1, comment2));
        given(ticketCommentMapper.ticketCommentToTicketCommentDto(comment1)).willReturn(commentDto1);
        given(ticketCommentMapper.ticketCommentToTicketCommentDto(comment2)).willReturn(commentDto2);

        // When
        List<TicketCommentDto> result = ticketCommentService.getCommentsByTicketId(ticketId);

        // Then
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals(commentId1, result.get(0).getId());
        assertEquals(commentId2, result.get(1).getId());
        then(ticketCommentRepository).should().findByTicketIdOrderByCreatedAtAsc(ticketId);
        then(ticketCommentMapper).should(times(2)).ticketCommentToTicketCommentDto(any(TicketComment.class));
    }

    @Test
    @DisplayName("Get Comments By Author ID - Success")
    void getCommentsByAuthorId_shouldReturnListOfCommentDtos() {
        // Given
        given(ticketCommentRepository.findByAuthorIdOrderByCreatedAtDesc(authorId)).willReturn(List.of(comment2, comment1)); // Desc order
        given(ticketCommentMapper.ticketCommentToTicketCommentDto(comment1)).willReturn(commentDto1);
        given(ticketCommentMapper.ticketCommentToTicketCommentDto(comment2)).willReturn(commentDto2);

        // When
        List<TicketCommentDto> result = ticketCommentService.getCommentsByAuthorId(authorId);

        // Then
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals(commentId2, result.get(0).getId()); // Verify order
        assertEquals(commentId1, result.get(1).getId());
        then(ticketCommentRepository).should().findByAuthorIdOrderByCreatedAtDesc(authorId);
        then(ticketCommentMapper).should(times(2)).ticketCommentToTicketCommentDto(any(TicketComment.class));
    }

    @Test
    @DisplayName("Add Comment - Success")
    void addComment_shouldSaveAndReturnCommentDto() {
        // Given
        TicketCommentDto dtoToSave = TicketCommentDto.builder()
                .ticketId(ticketId)
                .comment("New test comment")
                .build();

        // Mock finding the ticket
        given(ticketRepository.findById(dtoToSave.getTicketId())).willReturn(Optional.of(ticket));

        // Mock the save operation - capture the argument to verify
        ArgumentCaptor<TicketComment> commentCaptor = ArgumentCaptor.forClass(TicketComment.class);
        given(ticketCommentRepository.save(commentCaptor.capture())).willAnswer(invocation -> {
            TicketComment saved = invocation.getArgument(0);
            saved.setId(3L); // Simulate ID generation
            saved.setCreatedAt(LocalDateTime.now()); // Simulate @CreationTimestamp
            return saved;
        });

        // Mock the final mapping
        given(ticketCommentMapper.ticketCommentToTicketCommentDto(any(TicketComment.class))).willAnswer(invocation -> {
            TicketComment saved = invocation.getArgument(0);
            return TicketCommentDto.builder()
                    .id(saved.getId())
                    .ticketId(saved.getTicket().getId())
                    .comment(saved.getComment())
                    .authorFirstName(saved.getAuthor().getFirstName())
                    .authorLastName(saved.getAuthor().getLastName())
                    .authorRole(saved.getAuthor().getRole())
                    .createdAt(saved.getCreatedAt())
                    .build();
        });

        // When
        TicketCommentDto resultDto = ticketCommentService.addComment(dtoToSave, authorUser);

        // Then
        assertNotNull(resultDto);
        assertEquals(3L, resultDto.getId());
        assertEquals(dtoToSave.getComment(), resultDto.getComment());
        assertEquals(ticketId, resultDto.getTicketId());
        assertEquals(authorUser.getFirstName(), resultDto.getAuthorFirstName());
        assertNotNull(resultDto.getCreatedAt());

        // Verify the captured comment before saving
        TicketComment capturedComment = commentCaptor.getValue();
        assertEquals(dtoToSave.getComment(), capturedComment.getComment());
        assertEquals(ticket, capturedComment.getTicket());
        assertEquals(authorUser, capturedComment.getAuthor());
        assertNull(capturedComment.getId()); // ID should be null before save
        assertNull(capturedComment.getCreatedAt()); // Timestamp should be null before save

        then(ticketRepository).should().findById(dtoToSave.getTicketId());
        then(ticketCommentRepository).should().save(any(TicketComment.class));
        then(ticketCommentMapper).should().ticketCommentToTicketCommentDto(any(TicketComment.class));
    }

    @Test
    @DisplayName("Add Comment - Ticket Not Found")
    void addComment_shouldThrowNotFoundException_whenTicketNotFound() {
        // Given
        TicketCommentDto dtoToSave = TicketCommentDto.builder().ticketId(99L).comment("test").build();
        given(ticketRepository.findById(dtoToSave.getTicketId())).willReturn(Optional.empty());

        // When & Then
        assertThrows(ResourceNotFoundException.class, () -> ticketCommentService.addComment(dtoToSave, authorUser));

        then(ticketRepository).should().findById(dtoToSave.getTicketId());
        then(ticketCommentRepository).should(never()).save(any()); // Save should not be called
        then(ticketCommentMapper).should(never()).ticketCommentToTicketCommentDto(any());
    }

    @Test
    @DisplayName("Update Comment - Success")
    void updateComment_shouldUpdateCommentTextAndReturnDto() {
        // Given
        Long commentId = commentId1;
        TicketCommentDto updatesDto = TicketCommentDto.builder()
                .comment("Updated comment text")
                .build(); // Only comment text can be updated

        // Mock finding the existing comment
        given(ticketCommentRepository.findById(commentId)).willReturn(Optional.of(comment1));
        // Mock the save operation
        given(ticketCommentRepository.save(any(TicketComment.class))).willAnswer(invocation -> invocation.getArgument(0));
        // Mock the final mapping
        given(ticketCommentMapper.ticketCommentToTicketCommentDto(any(TicketComment.class))).willAnswer(invocation -> {
            TicketComment updated = invocation.getArgument(0);
            assertEquals(updatesDto.getComment(), updated.getComment()); // Verify comment text was updated
            // Return a DTO reflecting the change
            return TicketCommentDto.builder()
                    .id(updated.getId())
                    .comment(updated.getComment())
                    .ticketId(updated.getTicket().getId())
                    .authorFirstName(updated.getAuthor().getFirstName())
                    // ... other fields
                    .build();
        });

        // When
        TicketCommentDto resultDto = ticketCommentService.updateComment(commentId, updatesDto);

        // Then
        assertNotNull(resultDto);
        assertEquals(commentId, resultDto.getId());
        assertEquals(updatesDto.getComment(), resultDto.getComment());

        then(ticketCommentRepository).should().findById(commentId);
        then(ticketCommentRepository).should().save(comment1); // Verify save was called with the modified object
        then(ticketCommentMapper).should().ticketCommentToTicketCommentDto(comment1);
    }

    @Test
    @DisplayName("Update Comment - Not Found")
    void updateComment_shouldThrowNotFoundException_whenCommentNotFound() {
        // Given
        Long nonExistentId = 99L;
        TicketCommentDto updatesDto = TicketCommentDto.builder().comment("update").build();
        given(ticketCommentRepository.findById(nonExistentId)).willReturn(Optional.empty());

        // When & Then
        assertThrows(ResourceNotFoundException.class, () -> ticketCommentService.updateComment(nonExistentId, updatesDto));
        then(ticketCommentRepository).should().findById(nonExistentId);
        then(ticketCommentRepository).should(never()).save(any());
    }

    @Test
    @DisplayName("Delete Comment By ID - Success")
    void deleteCommentById_shouldCallRepositoryDelete_whenExists() {
        // Given
        Long commentId = commentId1;
        given(ticketCommentRepository.existsById(commentId)).willReturn(true);
        willDoNothing().given(ticketCommentRepository).deleteById(commentId); // Mock void method

        // When
        ticketCommentService.deleteCommentById(commentId);

        // Then
        then(ticketCommentRepository).should().existsById(commentId);
        then(ticketCommentRepository).should().deleteById(commentId);
    }

    @Test
    @DisplayName("Delete Comment By ID - Not Found")
    void deleteCommentById_shouldThrowNotFoundException_whenNotExists() {
        // Given
        Long nonExistentId = 99L;
        given(ticketCommentRepository.existsById(nonExistentId)).willReturn(false);

        // When & Then
        assertThrows(ResourceNotFoundException.class, () -> ticketCommentService.deleteCommentById(nonExistentId));

        then(ticketCommentRepository).should().existsById(nonExistentId);
        then(ticketCommentRepository).should(never()).deleteById(anyLong()); // Delete should not be called
    }
}