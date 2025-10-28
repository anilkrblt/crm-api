package com.anil.crm.web.controllers;

import com.anil.crm.domain.Role;
import com.anil.crm.domain.User;
import com.anil.crm.exceptions.ResourceNotFoundException; // Import your exception
import com.anil.crm.services.TicketCommentService;
import com.anil.crm.web.models.TicketCommentDto;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
//import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
// Import Security related classes for testing @AuthenticationPrincipal
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf; // Import csrf
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


// Add security config if @PreAuthorize is used, otherwise @WebMvcTest might be enough
// @Import(SecurityConfig.class) // Usually not needed with @WithMockUser if just testing roles
@WebMvcTest(TicketCommentsController.class)
@DisplayName("Ticket Comments Controller Unit Tests")
class TicketCommentsControllerTest {

    @Autowired
    MockMvc mockMvc;

    @MockitoBean
    TicketCommentService ticketCommentService;

    @Autowired
    ObjectMapper objectMapper;

    TicketCommentDto validCommentDto1;
    TicketCommentDto validCommentDto2;
    User sampleUser;

    @BeforeEach
    void setUp() {
        sampleUser = User.builder()
                .id(10L)
                .firstName("Ali")
                .lastName("Veli")
                .email("ali.veli@email.com")
                .role(Role.CUSTOMER)
                .build();

        validCommentDto1 = TicketCommentDto.builder()
                .id(1L)
                .ticketId(1L)
                .comment("First comment text")
                .authorFirstName("Ali")
                .authorLastName("Veli")
                .authorRole(Role.CUSTOMER)
                .createdAt(LocalDateTime.now())
                .build();

        validCommentDto2 = TicketCommentDto.builder()
                .id(2L)
                .ticketId(1L)
                .comment("Agent response")
                .authorFirstName("Ahmet")
                .authorLastName("Yılmaz")
                .authorRole(Role.AGENT)
                .createdAt(LocalDateTime.now().plusMinutes(5))
                .build();
    }

    @Test
    @DisplayName("Get Comments By Ticket ID - Success")
    // Use @WithMockUser to simulate an authenticated user for @PreAuthorize
    @WithMockUser(authorities = {"CUSTOMER"})
    void getCommentsByTicket_shouldReturnComments_whenTicketExists() throws Exception {
        // Given
        Long ticketId = 1L;
        List<TicketCommentDto> commentList = Arrays.asList(validCommentDto1, validCommentDto2);
        given(ticketCommentService.getCommentsByTicketId(ticketId)).willReturn(commentList);

        // When & Then
        mockMvc.perform(get("/api/ticket-comments/ticket/{ticketId}", ticketId)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].id", is(validCommentDto1.getId().intValue())))
                .andExpect(jsonPath("$[1].id", is(validCommentDto2.getId().intValue())));

        then(ticketCommentService).should(times(1)).getCommentsByTicketId(ticketId);
    }

    @Test
    @DisplayName("Get Comments By Ticket ID - Ticket Not Found (Handled by Service)")
    @WithMockUser(authorities = {"AGENT"})
    void getCommentsByTicket_shouldReturnNotFound_whenTicketNotFound() throws Exception {
        // Given
        Long nonExistentTicketId = 99L;
        given(ticketCommentService.getCommentsByTicketId(nonExistentTicketId))
                .willThrow(new ResourceNotFoundException("Ticket not found"));

        // When & Then
        mockMvc.perform(get("/api/ticket-comments/ticket/{ticketId}", nonExistentTicketId)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound()); // Expect 404 from GlobalExceptionHandler

        then(ticketCommentService).should(times(1)).getCommentsByTicketId(nonExistentTicketId);
    }

    @Test
    @DisplayName("Get Comments By Author ID - Success")
    @WithMockUser(authorities = {"ADMIN"}) // Only ADMIN/AGENT allowed by controller
    void getCommentsByAuthor_shouldReturnComments() throws Exception {
        // Given
        Long authorId = sampleUser.getId();
        List<TicketCommentDto> authorComments = List.of(validCommentDto1);
        given(ticketCommentService.getCommentsByAuthorId(authorId)).willReturn(authorComments);

        // When & Then
        mockMvc.perform(get("/api/ticket-comments/author/{authorId}", authorId)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].authorFirstName", is(sampleUser.getFirstName())));

        then(ticketCommentService).should(times(1)).getCommentsByAuthorId(authorId);
    }


    @Test
    @DisplayName("Add Comment - Success")
    // Simulate a logged-in user with CUSTOMER authority for @AuthenticationPrincipal
    @WithMockUser(username = "ali.veli@email.com", authorities = {"CUSTOMER"})
    void addComment_shouldReturnCreatedComment_whenValid() throws Exception {
        // Given
        TicketCommentDto commentToCreate = TicketCommentDto.builder()
                .ticketId(1L)
                .comment("New comment from customer")
                .build();

        TicketCommentDto savedComment = TicketCommentDto.builder() // DTO returned by service
                .id(3L)
                .ticketId(1L)
                .comment("New comment from customer")
                .authorFirstName("Ali") // Assume service gets this from User
                .authorLastName("Veli")
                .authorRole(Role.CUSTOMER)
                .createdAt(LocalDateTime.now())
                .build();

        // Mock the service method that now accepts the User principal
        given(ticketCommentService.addComment(any(TicketCommentDto.class), any(User.class))).willReturn(savedComment);

        // When & Then
        mockMvc.perform(post("/api/ticket-comments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(commentToCreate))
                        // Add principal details for @AuthenticationPrincipal
                        .with(SecurityMockMvcRequestPostProcessors.user(sampleUser))
                        .with(csrf())) // Add CSRF token if security requires it (even if disabled globally, tests might need it)
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"))
                .andExpect(jsonPath("$.id", is(savedComment.getId().intValue())))
                .andExpect(jsonPath("$.comment", is(savedComment.getComment())))
                .andExpect(jsonPath("$.authorRole", is(Role.CUSTOMER.name())));

        then(ticketCommentService).should(times(1)).addComment(any(TicketCommentDto.class), any(User.class));
    }

    @Test
    @DisplayName("Add Comment - Validation Failure")
    @WithMockUser(authorities = {"CUSTOMER"})
    void addComment_shouldReturnBadRequest_whenInvalid() throws Exception {
        // Given
        TicketCommentDto invalidComment = TicketCommentDto.builder() // Missing ticketId and comment
                .build();

        // When & Then
        mockMvc.perform(post("/api/ticket-comments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidComment))
                        .with(SecurityMockMvcRequestPostProcessors.user(sampleUser))
                        .with(csrf()))
                .andExpect(status().isBadRequest());

        then(ticketCommentService).should(never()).addComment(any(TicketCommentDto.class), any(User.class));
    }

    @Test
    @DisplayName("Update Comment - Success")
    @WithMockUser(authorities = {"ADMIN"}) // Only ADMIN allowed
    void updateComment_shouldReturnUpdatedComment_whenValid() throws Exception {
        // Given
        Long commentId = validCommentDto1.getId();
        TicketCommentDto commentUpdates = TicketCommentDto.builder()
                .comment("Updated comment text.")
                .build(); // Only comment is updatable

        TicketCommentDto updatedCommentFromService = TicketCommentDto.builder()
                .id(commentId)
                .ticketId(validCommentDto1.getTicketId())
                .comment("Updated comment text.")
                .authorFirstName(validCommentDto1.getAuthorFirstName())
                .authorLastName(validCommentDto1.getAuthorLastName())
                .authorRole(validCommentDto1.getAuthorRole())
                .createdAt(validCommentDto1.getCreatedAt())
                .build(); // Assume service returns the updated DTO

        given(ticketCommentService.updateComment(eq(commentId), any(TicketCommentDto.class))).willReturn(updatedCommentFromService);

        // When & Then
        mockMvc.perform(put("/api/ticket-comments/{id}", commentId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(commentUpdates))
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(commentId.intValue())))
                .andExpect(jsonPath("$.comment", is(updatedCommentFromService.getComment())));

        then(ticketCommentService).should(times(1)).updateComment(eq(commentId), any(TicketCommentDto.class));
    }

    @Test
    @DisplayName("Update Comment - Not Found")
    @WithMockUser(authorities = {"ADMIN"})
    void updateComment_shouldReturnNotFound_whenNotFound() throws Exception {
        // Given
        Long nonExistentId = 99L;
        TicketCommentDto commentUpdates = TicketCommentDto.builder().comment("Update attempt").build();
        given(ticketCommentService.updateComment(eq(nonExistentId), any(TicketCommentDto.class)))
                .willThrow(new ResourceNotFoundException("Comment not found"));

        // When & Then
        mockMvc.perform(put("/api/ticket-comments/{id}", nonExistentId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(commentUpdates))
                        .with(csrf()))
                .andExpect(status().isNotFound());

        then(ticketCommentService).should(times(1)).updateComment(eq(nonExistentId), any(TicketCommentDto.class));
    }

    @Test
    @DisplayName("Delete Comment - Success")
    @WithMockUser(authorities = {"ADMIN"}) // Only ADMIN allowed
    void deleteComment_shouldReturnNoContent_whenSuccess() throws Exception {
        // Given
        Long commentIdToDelete = 1L;
        willDoNothing().given(ticketCommentService).deleteCommentById(commentIdToDelete);

        // When & Then
        mockMvc.perform(delete("/api/ticket-comments/{id}", commentIdToDelete)
                        .accept(MediaType.APPLICATION_JSON)
                        .with(csrf()))
                .andExpect(status().isNoContent());

        then(ticketCommentService).should(times(1)).deleteCommentById(commentIdToDelete);
    }

    @Test
    @DisplayName("Delete Comment - Not Found")
    @WithMockUser(authorities = {"ADMIN"})
    void deleteComment_shouldReturnNotFound_whenNotFound() throws Exception {
        // Given
        Long nonExistentId = 99L;
        willThrow(new ResourceNotFoundException("Comment not found"))
                .given(ticketCommentService).deleteCommentById(nonExistentId);

        // When & Then
        mockMvc.perform(delete("/api/ticket-comments/{id}", nonExistentId)
                        .accept(MediaType.APPLICATION_JSON)
                        .with(csrf()))
                .andExpect(status().isNotFound());

        then(ticketCommentService).should(times(1)).deleteCommentById(nonExistentId);
    }
}