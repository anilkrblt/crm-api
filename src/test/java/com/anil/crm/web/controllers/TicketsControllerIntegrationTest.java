package com.anil.crm.web.controllers;

import com.anil.crm.domain.TicketPriority;
import com.anil.crm.domain.TicketStatus;
import com.anil.crm.services.JwtService;
import com.anil.crm.services.TicketService;
import com.anil.crm.services.UserDetailsServiceImpl;
import com.anil.crm.web.models.AgentDto;
import com.anil.crm.web.models.CustomerDto;
import com.anil.crm.web.models.DepartmentDto;
import com.anil.crm.web.models.TicketDto;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
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


@WebMvcTest(controllers = TicketsController.class,
        excludeAutoConfiguration = {UserDetailsServiceAutoConfiguration.class})
class TicketsControllerIntegrationTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @MockitoBean
    TicketService ticketService;
    @MockitoBean
    JwtService jwtService;
    @MockitoBean
    UserDetailsServiceImpl userDetailsService;

    TicketDto testTicketDto;
    Long ticketId = 1L;
    Long customerId = 1L;
    Long agentId = 1L;
    Long departmentId = 1L;

    @BeforeEach
    void setUp() {
        testTicketDto = TicketDto.builder()
                .id(ticketId)
                .subject("Test Bilet Başlığı")
                .status(TicketStatus.OPEN)
                .priority(TicketPriority.MEDIUM)
                .customer(CustomerDto.builder().id(customerId).firstName("Test").lastName("Customer").build())
                .department(DepartmentDto.builder().id(departmentId).name("Teknik Destek").build())
                .assignedAgent(AgentDto.builder().id(agentId).firstName("Test").lastName("Agent").build())
                .createdAt(LocalDateTime.now())
                .build();
    }

    @Test
    @WithMockUser(authorities = "CUSTOMER")
    void getTicketById() throws Exception {
        given(ticketService.getTicketById(ticketId)).willReturn(testTicketDto);

        mockMvc.perform(get("/api/tickets/{id}", ticketId)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(ticketId.intValue())))
                .andExpect(jsonPath("$.subject", is(testTicketDto.getSubject())));

        then(ticketService).should().getTicketById(ticketId);
    }

    @Test
    void getTicketById_Unauthorized() throws Exception {
        given(ticketService.getTicketById(ticketId)).willReturn(testTicketDto);

        mockMvc.perform(get("/api/tickets/{id}", ticketId)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());

        then(ticketService).should(never()).getTicketById(anyLong());
    }

    @Test
    @WithMockUser(authorities = "ADMIN")
    void getAllTickets() throws Exception {
        given(ticketService.getAllTickets()).willReturn(List.of(testTicketDto));

        mockMvc.perform(get("/api/tickets")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id", is(ticketId.intValue())));

        then(ticketService).should().getAllTickets();
    }

    @Test
    @WithMockUser(authorities = "AGENT")
    void getTicketsByCustomer() throws Exception {
        given(ticketService.getTicketsByCustomerId(customerId)).willReturn(List.of(testTicketDto));

        mockMvc.perform(get("/api/tickets/customer/{id}", customerId)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].customer.id", is(customerId.intValue())));

        then(ticketService).should().getTicketsByCustomerId(customerId);
    }

    @Test
    @WithMockUser(authorities = "ADMIN")
    void getTicketsByAssignedAgent() throws Exception {
        given(ticketService.getTicketsByAssignedAgentId(agentId)).willReturn(List.of(testTicketDto));

        mockMvc.perform(get("/api/tickets/assigned-agent/{id}", agentId)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].assignedAgent.id", is(agentId.intValue())));

        then(ticketService).should().getTicketsByAssignedAgentId(agentId);
    }

    @Test
    @WithMockUser(authorities = "AGENT")
    void getTicketsByDepartment() throws Exception {
        given(ticketService.getTicketsByDepartmentId(departmentId)).willReturn(List.of(testTicketDto));

        mockMvc.perform(get("/api/tickets/department/{id}", departmentId)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].department.id", is(departmentId.intValue())));

        then(ticketService).should().getTicketsByDepartmentId(departmentId);
    }

    @Test
    @WithMockUser(authorities = "AGENT")
    void getTicketsByStatus() throws Exception {
        given(ticketService.getTicketsByStatus(TicketStatus.OPEN)).willReturn(List.of(testTicketDto));

        mockMvc.perform(get("/api/tickets/status")
                        .param("status", "OPEN")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].status", is("OPEN")));

        then(ticketService).should().getTicketsByStatus(TicketStatus.OPEN);
    }

    @Test
    @WithMockUser(authorities = "AGENT")
    void getTicketsByPriority() throws Exception {
        given(ticketService.getTicketsByPriority(TicketPriority.MEDIUM)).willReturn(List.of(testTicketDto));

        mockMvc.perform(get("/api/tickets/priority")
                        .param("priority", "MEDIUM")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].priority", is("MEDIUM")));

        then(ticketService).should().getTicketsByPriority(TicketPriority.MEDIUM);
    }

    @Test
    @WithMockUser(authorities = "CUSTOMER")
    void createTicket() throws Exception {
        TicketDto dtoToCreate = TicketDto.builder()
                .subject("Test Bilet")
                .description("Açıklama")
                .status(TicketStatus.OPEN)
                .priority(TicketPriority.LOW)
                .customer(CustomerDto.builder().id(1L).build())
                .department(DepartmentDto.builder().id(1L).build())
                .build();

        TicketDto savedDto = TicketDto.builder().id(3L).subject("Test Bilet").build();

        given(ticketService.createTicket(any(TicketDto.class))).willReturn(savedDto);

        mockMvc.perform(post("/api/tickets")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dtoToCreate)))
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"))
                .andExpect(jsonPath("$.id", is(3)));

        then(ticketService).should().createTicket(any(TicketDto.class));
    }



    @Test
    @WithMockUser(authorities = "AGENT")
    void updateTicket() throws Exception {

        Long ticketIdToUpdate = 1L;

        TicketDto dtoToUpdate = TicketDto.builder()
                .subject("Güncel Başlık")
                .description("Güncel açıklama.")
                .status(TicketStatus.IN_PROGRESS)
                .priority(TicketPriority.HIGH)
                .customer(CustomerDto.builder().id(1L).build())
                .department(DepartmentDto.builder().id(1L).build())
                .assignedAgent(null)
                .build();

        TicketDto updatedDto = TicketDto.builder()
                .id(ticketIdToUpdate)
                .subject("Güncel Başlık")
                .description("Güncel açıklama.")
                .status(TicketStatus.IN_PROGRESS)
                .priority(TicketPriority.HIGH)
                .customer(CustomerDto.builder().id(1L).build())
                .department(DepartmentDto.builder().id(1L).build())
                .updatedAt(LocalDateTime.now())
                .build();

        given(ticketService.updateTicket(eq(ticketIdToUpdate), any(TicketDto.class))).willReturn(updatedDto);

        mockMvc.perform(put("/api/tickets/{id}", ticketIdToUpdate)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dtoToUpdate)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.subject", is("Güncel Başlık")));

        then(ticketService).should().updateTicket(eq(ticketIdToUpdate), any(TicketDto.class));
    }

    @Test
    @WithMockUser(authorities = "AGENT")
    void updateTicketStatus() throws Exception {
        TicketStatus newStatus = TicketStatus.CLOSED;
        TicketDto updatedDto = TicketDto.builder().id(ticketId).status(newStatus).build();

        given(ticketService.updateTicketStatus(ticketId, newStatus)).willReturn(updatedDto);

        mockMvc.perform(patch("/api/tickets/{id}/status", ticketId)
                        .with(csrf())
                        .param("status", "CLOSED")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("CLOSED")));

        then(ticketService).should().updateTicketStatus(ticketId, newStatus);
    }



    @Test
    @WithMockUser(authorities = "ADMIN")
    void deleteTicket() throws Exception {
        willDoNothing().given(ticketService).deleteTicketById(ticketId);

        mockMvc.perform(delete("/api/tickets/{id}", ticketId)
                        .with(csrf()))
                .andExpect(status().isNoContent());

        then(ticketService).should().deleteTicketById(ticketId);
    }
}
