package com.anil.crm.web.controllers;

import com.anil.crm.config.SecurityConfig;
import com.anil.crm.domain.Role;
import com.anil.crm.domain.User;
import com.anil.crm.exceptions.ResourceNotFoundException;
import com.anil.crm.services.JwtService;
import com.anil.crm.services.TicketCommentService;
import com.anil.crm.services.UserDetailsServiceImpl;
import com.anil.crm.web.models.TicketCommentDto;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration;

import java.time.LocalDateTime;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = TicketCommentsController.class,
        excludeAutoConfiguration = {UserDetailsServiceAutoConfiguration.class})
@Import(SecurityConfig.class)
class TicketCommentsControllerIntegrationTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @MockitoBean
    TicketCommentService ticketCommentService;
    @MockitoBean
    JwtService jwtService;
    @MockitoBean
    UserDetailsServiceImpl userDetailsService;

    TicketCommentDto testCommentDto;
    User sampleCustomerUser;
    Long ticketId = 1L;
    Long commentId = 1L;
    Long authorId = 1L;

    @BeforeEach
    void setUp() {
        sampleCustomerUser = User.builder()
                .id(authorId)
                .email("customer@test.com")
                .firstName("Test")
                .lastName("Customer")
                .role(Role.CUSTOMER)
                .build();

        testCommentDto = TicketCommentDto.builder()
                .id(commentId)
                .ticketId(ticketId)
                .comment("Bu bir test yorumudur")
                .authorFirstName("Test")
                .authorLastName("Customer")
                .authorRole(Role.CUSTOMER)
                .createdAt(LocalDateTime.now())
                .build();
    }

    @Test
    @WithMockUser(authorities = "CUSTOMER")
    void getCommentsByTicket() throws Exception {
        given(ticketCommentService.getCommentsByTicketId(ticketId)).willReturn(List.of(testCommentDto));

        mockMvc.perform(get("/api/ticket-comments/ticket/{ticketId}", ticketId)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id", is(commentId.intValue())));

        then(ticketCommentService).should().getCommentsByTicketId(ticketId);
    }

    @Test
    @WithMockUser(authorities = "AGENT")
    void getCommentsByAuthor() throws Exception {
        given(ticketCommentService.getCommentsByAuthorId(authorId)).willReturn(List.of(testCommentDto));

        mockMvc.perform(get("/api/ticket-comments/author/{authorId}", authorId)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].authorFirstName", is("Test")));

        then(ticketCommentService).should().getCommentsByAuthorId(authorId);
    }

    @Test
    @WithMockUser(authorities = "CUSTOMER")
    void addComment() throws Exception {
        TicketCommentDto dtoToCreate = TicketCommentDto.builder()
                .ticketId(ticketId)
                .comment("Yeni yorum")
                .build();

        TicketCommentDto savedDto = TicketCommentDto.builder()
                .id(2L)
                .ticketId(ticketId)
                .comment("Yeni yorum")
                .authorFirstName(sampleCustomerUser.getFirstName())
                .authorLastName(sampleCustomerUser.getLastName())
                .authorRole(sampleCustomerUser.getRole())
                .createdAt(LocalDateTime.now())
                .build();

        given(ticketCommentService.addComment(any(TicketCommentDto.class), any(User.class))).willReturn(savedDto);

        mockMvc.perform(post("/api/ticket-comments")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dtoToCreate))
                        .with(SecurityMockMvcRequestPostProcessors.user(sampleCustomerUser)))
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"))
                .andExpect(jsonPath("$.id", is(2)))
                .andExpect(jsonPath("$.comment", is("Yeni yorum")));

        then(ticketCommentService).should().addComment(any(TicketCommentDto.class), any(User.class));
    }

    @Test
    @WithMockUser(authorities = "ADMIN")
    void updateComment() throws Exception {
        Long commentId = 1L;
        String updatedCommentText = "Güncellenmiş yorum metni";

        TicketCommentDto dtoToUpdate = TicketCommentDto.builder()
                .comment(updatedCommentText)
                .ticketId(ticketId)
                .build();

        TicketCommentDto updatedDto = TicketCommentDto.builder()
                .id(commentId)
                .comment(updatedCommentText)
                .ticketId(ticketId)
                .build();

        given(ticketCommentService.updateComment(eq(commentId), any(TicketCommentDto.class))).willReturn(updatedDto);

        mockMvc.perform(put("/api/ticket-comments/{id}", commentId)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dtoToUpdate)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(commentId.intValue())))
                .andExpect(jsonPath("$.comment", is(updatedCommentText)));

        then(ticketCommentService).should().updateComment(eq(commentId), any(TicketCommentDto.class));
    }


    @Test
    @WithMockUser(authorities = "ADMIN")
    void deleteComment() throws Exception {
        willDoNothing().given(ticketCommentService).deleteCommentById(commentId);

        mockMvc.perform(delete("/api/ticket-comments/{id}", commentId)
                        .with(csrf()))
                .andExpect(status().isNoContent());

        then(ticketCommentService).should().deleteCommentById(commentId);
    }

    @Test
    @WithMockUser(authorities = "ADMIN")
    void deleteComment_NotFound() throws Exception {
        Long nonExistentId = 99L;
        willThrow(new ResourceNotFoundException("Bulunamadı"))
                .given(ticketCommentService).deleteCommentById(nonExistentId);

        mockMvc.perform(delete("/api/ticket-comments/{id}", nonExistentId)
                        .with(csrf()))
                .andExpect(status().isNotFound());

        then(ticketCommentService).should().deleteCommentById(nonExistentId);
    }
}
