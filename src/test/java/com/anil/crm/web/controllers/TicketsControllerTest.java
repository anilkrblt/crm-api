package com.anil.crm.web.controllers;

import com.anil.crm.domain.TicketPriority;
import com.anil.crm.domain.TicketStatus;
import com.anil.crm.exceptions.ResourceNotFoundException; // Import your exception
import com.anil.crm.services.TicketService;
import com.anil.crm.web.models.AgentDto;
import com.anil.crm.web.models.CustomerDto;
import com.anil.crm.web.models.TicketDto;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
//import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(TicketsController.class) // Test only the TicketsController layer
@DisplayName("Tickets Controller Unit Tests")
class TicketsControllerTest {

    @Autowired
    MockMvc mockMvc; // To perform HTTP requests

    @MockitoBean
    TicketService ticketService; // Mock the dependency

    @Autowired
    ObjectMapper objectMapper; // To convert objects to/from JSON

    TicketDto validTicketDto1;
    TicketDto validTicketDto2;
    CustomerDto customerDto;
    AgentDto agentDto;

    @BeforeEach
    void setUp() {
        // Create sample DTOs
        customerDto = CustomerDto.builder().id(1L).firstName("Ali").lastName("Veli").email("ali@veli.com").build();
        agentDto = AgentDto.builder().id(1L).firstName("Ahmet").lastName("Yılmaz").email("ahmet@yilmaz.com").build();

        validTicketDto1 = TicketDto.builder()
                .id(1L)
                .customer(customerDto)
                .agent(agentDto)
                .subject("Ürün iade talebi")
                .description("Satın aldığım ürünü iade etmek istiyorum")
                .status(TicketStatus.OPEN)
                .priority(TicketPriority.MEDIUM)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        validTicketDto2 = TicketDto.builder()
                .id(2L)
                .customer(CustomerDto.builder().id(2L).build()) // Minimal customer for second ticket
                .agent(null) // Unassigned ticket
                .subject("Teknik sorun")
                .description("Uygulama açılmıyor")
                .status(TicketStatus.IN_PROGRESS)
                .priority(TicketPriority.HIGH)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    @Test
    @DisplayName("Get Ticket By ID - Success")
    void getTicketById_shouldReturnTicketDto_whenFound() throws Exception {
        // Given
        given(ticketService.getTicketById(validTicketDto1.getId())).willReturn(validTicketDto1);

        // When & Then
        mockMvc.perform(get("/api/tickets/{id}", validTicketDto1.getId())
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id", is(validTicketDto1.getId().intValue())))
                .andExpect(jsonPath("$.subject", is(validTicketDto1.getSubject())))
                .andExpect(jsonPath("$.customer.id", is(customerDto.getId().intValue())));

        then(ticketService).should(times(1)).getTicketById(validTicketDto1.getId());
    }

    @Test
    @DisplayName("Get Ticket By ID - Not Found")
    void getTicketById_shouldReturnNotFound_whenNotFound() throws Exception {
        // Given
        Long nonExistentId = 99L;
        given(ticketService.getTicketById(nonExistentId)).willThrow(new ResourceNotFoundException("Ticket not found"));

        // When & Then
        mockMvc.perform(get("/api/tickets/{id}", nonExistentId)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());

        then(ticketService).should(times(1)).getTicketById(nonExistentId);
    }

    @Test
    @DisplayName("Get All Tickets - Success")
    void getAllTickets_shouldReturnListOfTickets() throws Exception {
        // Given
        List<TicketDto> ticketList = Arrays.asList(validTicketDto1, validTicketDto2);
        given(ticketService.getAllTickets()).willReturn(ticketList);

        // When & Then
        mockMvc.perform(get("/api/tickets")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].id", is(validTicketDto1.getId().intValue())))
                .andExpect(jsonPath("$[1].id", is(validTicketDto2.getId().intValue())));

        then(ticketService).should(times(1)).getAllTickets();
    }

    @Test
    @DisplayName("Get Tickets By Customer - Success")
    void getTicketsByCustomer_shouldReturnCustomerTickets() throws Exception {
        // Given
        Long customerId = customerDto.getId();
        List<TicketDto> customerTickets = List.of(validTicketDto1);
        given(ticketService.getTicketsByCustomerId(customerId)).willReturn(customerTickets);

        // When & Then
        mockMvc.perform(get("/api/tickets/customer/{customerId}", customerId)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].customer.id", is(customerId.intValue())));

        then(ticketService).should(times(1)).getTicketsByCustomerId(customerId);
    }

    @Test
    @DisplayName("Get Tickets By Agent - Success")
    void getTicketsByAgent_shouldReturnAgentTickets() throws Exception {
        // Given
        Long agentId = agentDto.getId();
        List<TicketDto> agentTickets = List.of(validTicketDto1);
        given(ticketService.getTicketsByAgentId(agentId)).willReturn(agentTickets);

        // When & Then
        mockMvc.perform(get("/api/tickets/agent/{agentId}", agentId)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].agent.id", is(agentId.intValue())));

        then(ticketService).should(times(1)).getTicketsByAgentId(agentId);
    }

    @Test
    @DisplayName("Get Tickets By Status - Success")
    void getTicketsByStatus_shouldReturnFilteredTickets() throws Exception {
        // Given
        TicketStatus status = TicketStatus.OPEN;
        List<TicketDto> openTickets = List.of(validTicketDto1);
        given(ticketService.getTicketsByStatus(status)).willReturn(openTickets);

        // When & Then
        mockMvc.perform(get("/api/tickets/status")
                        .param("status", status.name()) // Send Enum name as string
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].status", is(status.name())));

        then(ticketService).should(times(1)).getTicketsByStatus(status);
    }

    @Test
    @DisplayName("Get Tickets By Priority - Success")
    void getTicketsByPriority_shouldReturnFilteredTickets() throws Exception {
        // Given
        TicketPriority priority = TicketPriority.HIGH;
        List<TicketDto> highPriorityTickets = List.of(validTicketDto2);
        given(ticketService.getTicketsByPriority(priority)).willReturn(highPriorityTickets);

        // When & Then
        mockMvc.perform(get("/api/tickets/priority")
                        .param("priority", priority.name()) // Send Enum name as string
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].priority", is(priority.name())));

        then(ticketService).should(times(1)).getTicketsByPriority(priority);
    }

    @Test
    @DisplayName("Create Ticket - Success")
    void createTicket_shouldReturnCreatedTicket_whenValid() throws Exception {
        // Given
        TicketDto ticketToCreate = TicketDto.builder()
                .subject("Yeni Bilet")
                .description("Açıklama...")
                .priority(TicketPriority.LOW)
                .status(TicketStatus.OPEN)
                .customer(CustomerDto.builder().id(customerDto.getId()).build()) // Only ID needed
                .build();

        TicketDto savedTicket = TicketDto.builder() // DTO returned by service (with ID, timestamps etc.)
                .id(3L)
                .subject("Yeni Bilet")
                .description("Açıklama...")
                .priority(TicketPriority.LOW)
                .status(TicketStatus.OPEN)
                .customer(customerDto) // Full customer DTO might be returned
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        given(ticketService.createTicket(any(TicketDto.class))).willReturn(savedTicket);

        // When & Then
        mockMvc.perform(post("/api/tickets")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(ticketToCreate)))
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"))
                .andExpect(jsonPath("$.id", is(savedTicket.getId().intValue())))
                .andExpect(jsonPath("$.subject", is(savedTicket.getSubject())));

        then(ticketService).should(times(1)).createTicket(any(TicketDto.class));
    }

    @Test
    @DisplayName("Create Ticket - Validation Failure")
    void createTicket_shouldReturnBadRequest_whenInvalid() throws Exception {
        // Given
        TicketDto invalidTicket = TicketDto.builder() // Missing required subject, customer etc.
                .description("Only description")
                .build();

        // When & Then
        mockMvc.perform(post("/api/tickets")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidTicket)))
                .andExpect(status().isBadRequest());

        then(ticketService).should(never()).createTicket(any(TicketDto.class));
    }

    @Test
    @DisplayName("Update Ticket - Success")
    void updateTicket_shouldReturnUpdatedTicket_whenValid() throws Exception {
        // Given
        Long ticketId = validTicketDto1.getId();
        TicketDto ticketUpdates = TicketDto.builder()
                .subject("Güncellenmiş İade Talebi")
                .description("Yeni açıklama.")
                .priority(TicketPriority.HIGH)
                .status(TicketStatus.IN_PROGRESS)
                .customer(CustomerDto.builder().id(customerDto.getId()).build()) // ID is enough
                .agent(AgentDto.builder().id(agentDto.getId()).build()) // Assign agent
                .build();

        TicketDto updatedTicketFromService = TicketDto.builder()
                .id(ticketId)
                .subject("Güncellenmiş İade Talebi")
                .description("Yeni açıklama.")
                .priority(TicketPriority.HIGH)
                .status(TicketStatus.IN_PROGRESS)
                .customer(customerDto)
                .agent(agentDto)
                .createdAt(validTicketDto1.getCreatedAt())
                .updatedAt(LocalDateTime.now())
                .build();

        given(ticketService.updateTicket(eq(ticketId), any(TicketDto.class))).willReturn(updatedTicketFromService);

        // When & Then
        mockMvc.perform(put("/api/tickets/{id}", ticketId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(ticketUpdates)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(ticketId.intValue())))
                .andExpect(jsonPath("$.subject", is(updatedTicketFromService.getSubject())))
                .andExpect(jsonPath("$.status", is(updatedTicketFromService.getStatus().name())));

        then(ticketService).should(times(1)).updateTicket(eq(ticketId), any(TicketDto.class));
    }

    @Test
    @DisplayName("Update Ticket - Not Found")
    void updateTicket_shouldReturnNotFound_whenNotFound() throws Exception {
        // Given
        Long nonExistentId = 99L;
        TicketDto ticketUpdates = TicketDto.builder() /* valid update data */.build();
        given(ticketService.updateTicket(eq(nonExistentId), any(TicketDto.class)))
                .willThrow(new ResourceNotFoundException("Ticket not found"));

        // When & Then
        mockMvc.perform(put("/api/tickets/{id}", nonExistentId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(ticketUpdates)))
                .andExpect(status().isNotFound());

        then(ticketService).should(times(1)).updateTicket(eq(nonExistentId), any(TicketDto.class));
    }

    @Test
    @DisplayName("Update Ticket Status - Success")
    void updateTicketStatus_shouldReturnUpdatedTicket_whenValid() throws Exception {
        // Given
        Long ticketId = validTicketDto1.getId();
        TicketStatus newStatus = TicketStatus.CLOSED;

        TicketDto updatedTicketFromService = TicketDto.builder()
                .id(ticketId)
                .subject(validTicketDto1.getSubject())
                .customer(validTicketDto1.getCustomer())
                .agent(validTicketDto1.getAgent())
                .status(newStatus) // Status updated
                .priority(validTicketDto1.getPriority())
                .createdAt(validTicketDto1.getCreatedAt())
                .updatedAt(LocalDateTime.now()) // Updated timestamp
                .build();

        given(ticketService.updateTicketStatus(eq(ticketId), eq(newStatus))).willReturn(updatedTicketFromService);

        // When & Then
        mockMvc.perform(patch("/api/tickets/{id}/status", ticketId)
                        .param("status", newStatus.name())
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(ticketId.intValue())))
                .andExpect(jsonPath("$.status", is(newStatus.name())));

        then(ticketService).should(times(1)).updateTicketStatus(eq(ticketId), eq(newStatus));
    }

    @Test
    @DisplayName("Update Ticket Status - Not Found")
    void updateTicketStatus_shouldReturnNotFound_whenNotFound() throws Exception {
        // Given
        Long nonExistentId = 99L;
        TicketStatus newStatus = TicketStatus.CLOSED;
        given(ticketService.updateTicketStatus(eq(nonExistentId), eq(newStatus)))
                .willThrow(new ResourceNotFoundException("Ticket not found"));

        // When & Then
        mockMvc.perform(patch("/api/tickets/{id}/status", nonExistentId)
                        .param("status", newStatus.name())
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());

        then(ticketService).should(times(1)).updateTicketStatus(eq(nonExistentId), eq(newStatus));
    }


    @Test
    @DisplayName("Delete Ticket - Success")
    void deleteTicket_shouldReturnNoContent_whenSuccess() throws Exception {
        // Given
        Long ticketIdToDelete = 1L;
        willDoNothing().given(ticketService).deleteTicketById(ticketIdToDelete);

        // When & Then
        mockMvc.perform(delete("/api/tickets/{id}", ticketIdToDelete)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());

        then(ticketService).should(times(1)).deleteTicketById(ticketIdToDelete);
    }

    @Test
    @DisplayName("Delete Ticket - Not Found")
    void deleteTicket_shouldReturnNotFound_whenNotFound() throws Exception {
        // Given
        Long nonExistentId = 99L;
        willThrow(new ResourceNotFoundException("Ticket not found"))
                .given(ticketService).deleteTicketById(nonExistentId);

        // When & Then
        mockMvc.perform(delete("/api/tickets/{id}", nonExistentId)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());

        then(ticketService).should(times(1)).deleteTicketById(nonExistentId);
    }
}