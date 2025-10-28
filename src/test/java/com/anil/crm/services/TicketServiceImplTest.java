package com.anil.crm.services;

import com.anil.crm.domain.*; // Import your domain classes
import com.anil.crm.exceptions.ResourceNotFoundException;
import com.anil.crm.repositories.TicketRepository;
import com.anil.crm.web.mappers.TicketMapper;
import com.anil.crm.web.models.AgentDto;
import com.anil.crm.web.models.CustomerDto;
import com.anil.crm.web.models.TicketDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
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
@DisplayName("Ticket Service Implementation Unit Tests")
class TicketServiceImplTest {

    @Mock // Mock dependencies
    TicketRepository ticketRepository;
    @Mock
    TicketMapper ticketMapper;

    @InjectMocks // Inject mocks into the service
    TicketServiceImpl ticketService;

    // Test Data
    Ticket ticket1, ticket2;
    TicketDto ticketDto1, ticketDto2;
    Customer customer;
    Agent agent;
    User customerUser, agentUser;
    Long ticketId1 = 1L;
    Long ticketId2 = 2L;
    Long customerId = 1L;
    Long agentId = 1L;

    @BeforeEach
    void setUp() {
        // --- Setup Users ---
        customerUser = User.builder().id(1L).email("cust@test.com").role(Role.CUSTOMER).build();
        agentUser = User.builder().id(2L).email("agent@test.com").role(Role.AGENT).build();

        // --- Setup Profiles ---
        customer = Customer.builder().id(customerId).user(customerUser).build();
        agent = Agent.builder().id(agentId).user(agentUser).build();

        // --- Setup Tickets (Entities) ---
        ticket1 = Ticket.builder()
                .id(ticketId1)
                .customer(customer)
                .agent(agent)
                .subject("Subject 1")
                .description("Desc 1")
                .status(TicketStatus.OPEN)
                .priority(TicketPriority.MEDIUM)
                .createdAt(LocalDateTime.now().minusHours(1))
                .updatedAt(LocalDateTime.now().minusHours(1))
                .build();
        ticket2 = Ticket.builder()
                .id(ticketId2)
                .customer(customer) // Same customer, different ticket
                .agent(null) // Unassigned
                .subject("Subject 2")
                .description("Desc 2")
                .status(TicketStatus.IN_PROGRESS)
                .priority(TicketPriority.HIGH)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        // --- Setup Ticket DTOs ---
        // Assume mapper correctly maps nested DTOs
        ticketDto1 = TicketDto.builder()
                .id(ticketId1)
                .customer(CustomerDto.builder().id(customerId).email(customerUser.getEmail()).build())
                .agent(AgentDto.builder().id(agentId).email(agentUser.getEmail()).build())
                .subject("Subject 1")
                .description("Desc 1")
                .status(TicketStatus.OPEN)
                .priority(TicketPriority.MEDIUM)
                .createdAt(ticket1.getCreatedAt())
                .updatedAt(ticket1.getUpdatedAt())
                .build();
        ticketDto2 = TicketDto.builder()
                .id(ticketId2)
                .customer(CustomerDto.builder().id(customerId).email(customerUser.getEmail()).build())
                .agent(null)
                .subject("Subject 2")
                .description("Desc 2")
                .status(TicketStatus.IN_PROGRESS)
                .priority(TicketPriority.HIGH)
                .createdAt(ticket2.getCreatedAt())
                .updatedAt(ticket2.getUpdatedAt())
                .build();
    }

    @Test
    @DisplayName("Get All Tickets - Success")
    void getAllTickets_shouldReturnListOfTicketDtos() {
        // Given
        given(ticketRepository.findAll()).willReturn(List.of(ticket1, ticket2));
        given(ticketMapper.ticketToTicketDto(ticket1)).willReturn(ticketDto1);
        given(ticketMapper.ticketToTicketDto(ticket2)).willReturn(ticketDto2);

        // When
        List<TicketDto> result = ticketService.getAllTickets();

        // Then
        assertNotNull(result);
        assertEquals(2, result.size());
        then(ticketRepository).should().findAll();
        then(ticketMapper).should(times(2)).ticketToTicketDto(any(Ticket.class));
    }

    @Test
    @DisplayName("Get Ticket By ID - Success")
    void getTicketById_shouldReturnTicketDto_whenFound() {
        // Given
        given(ticketRepository.findById(ticketId1)).willReturn(Optional.of(ticket1));
        given(ticketMapper.ticketToTicketDto(ticket1)).willReturn(ticketDto1);

        // When
        TicketDto foundDto = ticketService.getTicketById(ticketId1);

        // Then
        assertNotNull(foundDto);
        assertEquals(ticketId1, foundDto.getId());
        assertEquals(ticketDto1.getSubject(), foundDto.getSubject());
        then(ticketRepository).should().findById(ticketId1);
        then(ticketMapper).should().ticketToTicketDto(ticket1);
    }

    @Test
    @DisplayName("Get Ticket By ID - Not Found")
    void getTicketById_shouldThrowResourceNotFoundException_whenNotFound() {
        // Given
        Long nonExistentId = 99L;
        given(ticketRepository.findById(nonExistentId)).willReturn(Optional.empty());

        // When & Then
        assertThrows(ResourceNotFoundException.class, () -> ticketService.getTicketById(nonExistentId));
        then(ticketRepository).should().findById(nonExistentId);
        then(ticketMapper).should(never()).ticketToTicketDto(any());
    }

    @Test
    @DisplayName("Get Tickets By Customer ID - Success")
    void getTicketsByCustomerId_shouldReturnCustomerTickets() {
        // Given
        given(ticketRepository.findTicketsByCustomerId(customerId)).willReturn(List.of(ticket1, ticket2));
        given(ticketMapper.ticketToTicketDto(ticket1)).willReturn(ticketDto1);
        given(ticketMapper.ticketToTicketDto(ticket2)).willReturn(ticketDto2);

        // When
        List<TicketDto> result = ticketService.getTicketsByCustomerId(customerId);

        // Then
        assertNotNull(result);
        assertEquals(2, result.size());
        then(ticketRepository).should().findTicketsByCustomerId(customerId);
        then(ticketMapper).should(times(2)).ticketToTicketDto(any(Ticket.class));
    }

    @Test
    @DisplayName("Get Tickets By Agent ID - Success")
    void getTicketsByAgentId_shouldReturnAgentTickets() {
        // Given
        given(ticketRepository.findTicketsByAgentId(agentId)).willReturn(List.of(ticket1));
        given(ticketMapper.ticketToTicketDto(ticket1)).willReturn(ticketDto1);

        // When
        List<TicketDto> result = ticketService.getTicketsByAgentId(agentId);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(agentId, result.get(0).getAgent().getId());
        then(ticketRepository).should().findTicketsByAgentId(agentId);
        then(ticketMapper).should().ticketToTicketDto(ticket1);
    }

    @Test
    @DisplayName("Get Tickets By Status - Success")
    void getTicketsByStatus_shouldReturnFilteredTickets() {
        // Given
        TicketStatus status = TicketStatus.OPEN;
        given(ticketRepository.findTicketsByStatus(status)).willReturn(List.of(ticket1));
        given(ticketMapper.ticketToTicketDto(ticket1)).willReturn(ticketDto1);

        // When
        List<TicketDto> result = ticketService.getTicketsByStatus(status);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(status, result.get(0).getStatus());
        then(ticketRepository).should().findTicketsByStatus(status);
        then(ticketMapper).should().ticketToTicketDto(ticket1);
    }

    @Test
    @DisplayName("Get Tickets By Priority - Success")
    void getTicketsByPriority_shouldReturnFilteredTickets() {
        // Given
        TicketPriority priority = TicketPriority.HIGH;
        given(ticketRepository.getTicketsByPriority(priority)).willReturn(List.of(ticket2));
        given(ticketMapper.ticketToTicketDto(ticket2)).willReturn(ticketDto2);

        // When
        List<TicketDto> result = ticketService.getTicketsByPriority(priority);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(priority, result.get(0).getPriority());
        then(ticketRepository).should().getTicketsByPriority(priority);
        then(ticketMapper).should().ticketToTicketDto(ticket2);
    }

    @Test
    @DisplayName("Create Ticket - Success")
    void createTicket_shouldSaveAndReturnTicket() {
        // Given
        TicketDto dtoToSave = TicketDto.builder() // DTO without ID or timestamps
                .subject("New Ticket")
                .description("Create test")
                .priority(TicketPriority.LOW)
                .status(TicketStatus.OPEN)
                .customer(CustomerDto.builder().id(customerId).build())
                .build();
        Ticket ticketToSave = new Ticket(); // Assume mapper creates this without ID/timestamps
        Ticket savedTicket = new Ticket(); // Entity returned by save (with ID/timestamps)
        savedTicket.setId(3L);
        savedTicket.setCreatedAt(LocalDateTime.now());
        savedTicket.setUpdatedAt(LocalDateTime.now());

        TicketDto finalDto = new TicketDto(); // DTO returned by service
        finalDto.setId(3L);
        finalDto.setCreatedAt(savedTicket.getCreatedAt());
        finalDto.setUpdatedAt(savedTicket.getUpdatedAt());

        given(ticketMapper.ticketDtoToTicket(dtoToSave)).willReturn(ticketToSave);
        given(ticketRepository.save(any(Ticket.class))).willReturn(savedTicket);
        given(ticketMapper.ticketToTicketDto(savedTicket)).willReturn(finalDto);

        // When
        TicketDto resultDto = ticketService.createTicket(dtoToSave);

        // Then
        assertNotNull(resultDto);
        assertEquals(3L, resultDto.getId());
        assertNotNull(resultDto.getCreatedAt()); // Check if timestamps are set
        assertNotNull(resultDto.getUpdatedAt());
        assertNull(ticketToSave.getId()); // Verify ID was set to null before save

        then(ticketMapper).should().ticketDtoToTicket(dtoToSave);
        then(ticketRepository).should().save(ticketToSave);
        then(ticketMapper).should().ticketToTicketDto(savedTicket);
    }

    @Test
    @DisplayName("Update Ticket - Success")
    void updateTicket_shouldUpdateAndReturnTicket() {
        // Given
        Long ticketId = ticketId1;
        TicketDto updatesDto = TicketDto.builder()
                .subject("Updated Subject 1")
                .description("Updated Desc 1")
                .priority(TicketPriority.HIGH)
                .status(TicketStatus.CLOSED)
                .agent(AgentDto.builder().id(agentId).build()) // Keep agent
                .customer(CustomerDto.builder().id(customerId).build()) // Keep customer
                .build();

        // Mock finding the existing ticket
        given(ticketRepository.findById(ticketId)).willReturn(Optional.of(ticket1));
        // Mock the mapper's update method (it's void)
        willDoNothing().given(ticketMapper).updateTicketFromDto(eq(updatesDto), eq(ticket1));
        // Mock the save operation
        given(ticketRepository.save(any(Ticket.class))).willAnswer(invocation -> invocation.getArgument(0)); // Return the modified ticket1
        // Mock the final mapping to DTO
        given(ticketMapper.ticketToTicketDto(any(Ticket.class))).willAnswer(invocation -> {
            Ticket updated = invocation.getArgument(0);
            // Create DTO reflecting the expected changes
            return TicketDto.builder()
                    .id(updated.getId())
                    .subject(updated.getSubject()) // Assume mapper updated this
                    .status(updated.getStatus()) // Assume mapper updated this
                    .priority(updated.getPriority()) // Assume mapper updated this
                    .customer(CustomerDto.builder().id(customerId).build()) // Keep original customer DTO ref
                    .agent(AgentDto.builder().id(agentId).build()) // Keep original agent DTO ref
                    .createdAt(updated.getCreatedAt())
                    .updatedAt(updated.getUpdatedAt()) // Should be updated now
                    .build();
        });


        // When
        TicketDto resultDto = ticketService.updateTicket(ticketId, updatesDto);

        // Then
        assertNotNull(resultDto);
        assertEquals(ticketId, resultDto.getId());
        // Verify mapper was called to apply updates
        then(ticketMapper).should().updateTicketFromDto(updatesDto, ticket1);
        // Verify save was called
        then(ticketRepository).should().save(ticket1);
        // Verify final mapping was done
        then(ticketMapper).should().ticketToTicketDto(ticket1);
        // Assert that the updatedAt timestamp was likely modified (though exact time is tricky)
        assertNotEquals(ticket1.getUpdatedAt(), resultDto.getUpdatedAt()); // Should be different after service call
    }

    @Test
    @DisplayName("Update Ticket - Not Found")
    void updateTicket_shouldThrowNotFoundException_whenTicketNotFound() {
        // Given
        Long nonExistentId = 99L;
        TicketDto updatesDto = TicketDto.builder().build();
        given(ticketRepository.findById(nonExistentId)).willReturn(Optional.empty());

        // When & Then
        assertThrows(ResourceNotFoundException.class, () -> ticketService.updateTicket(nonExistentId, updatesDto));
        then(ticketRepository).should().findById(nonExistentId);
        then(ticketMapper).should(never()).updateTicketFromDto(any(), any());
        then(ticketRepository).should(never()).save(any());
    }

    @Test
    @DisplayName("Update Ticket Status - Success")
    void updateTicketStatus_shouldUpdateStatusAndReturnTicket() {
        // Given
        Long ticketId = ticketId1;
        TicketStatus newStatus = TicketStatus.CLOSED;

        given(ticketRepository.findById(ticketId)).willReturn(Optional.of(ticket1));
        given(ticketRepository.save(any(Ticket.class))).willAnswer(invocation -> invocation.getArgument(0)); // Return the modified ticket1
        // Mock final mapping
        given(ticketMapper.ticketToTicketDto(any(Ticket.class))).willAnswer(invocation -> {
            Ticket updated = invocation.getArgument(0);
            assertEquals(newStatus, updated.getStatus()); // Assert status was changed before mapping
            return TicketDto.builder().id(updated.getId()).status(updated.getStatus()).build(); // Simplified DTO
        });

        // When
        TicketDto resultDto = ticketService.updateTicketStatus(ticketId, newStatus);

        // Then
        assertNotNull(resultDto);
        assertEquals(ticketId, resultDto.getId());
        assertEquals(newStatus, resultDto.getStatus());
        assertEquals(newStatus, ticket1.getStatus()); // Verify the original object was modified

        then(ticketRepository).should().findById(ticketId);
        then(ticketRepository).should().save(ticket1);
        then(ticketMapper).should().ticketToTicketDto(ticket1);
    }

    @Test
    @DisplayName("Update Ticket Status - Not Found")
    void updateTicketStatus_shouldThrowNotFoundException_whenTicketNotFound() {
        // Given
        Long nonExistentId = 99L;
        TicketStatus newStatus = TicketStatus.CLOSED;
        given(ticketRepository.findById(nonExistentId)).willReturn(Optional.empty());

        // When & Then
        assertThrows(ResourceNotFoundException.class, () -> ticketService.updateTicketStatus(nonExistentId, newStatus));
        then(ticketRepository).should().findById(nonExistentId);
        then(ticketRepository).should(never()).save(any());
    }

    @Test
    @DisplayName("Delete Ticket By ID - Success")
    void deleteTicketById_shouldCallRepositoryDelete_whenExists() {
        // Given
        Long ticketId = ticketId1;
        given(ticketRepository.existsById(ticketId)).willReturn(true);
        willDoNothing().given(ticketRepository).deleteById(ticketId); // Mock void method

        // When
        ticketService.deleteTicketById(ticketId);

        // Then
        then(ticketRepository).should().existsById(ticketId);
        then(ticketRepository).should().deleteById(ticketId);
    }

    @Test
    @DisplayName("Delete Ticket By ID - Not Found")
    void deleteTicketById_shouldThrowNotFoundException_whenNotExists() {
        // Given
        Long nonExistentId = 99L;
        given(ticketRepository.existsById(nonExistentId)).willReturn(false);

        // When & Then
        assertThrows(ResourceNotFoundException.class, () -> ticketService.deleteTicketById(nonExistentId));
        then(ticketRepository).should().existsById(nonExistentId);
        then(ticketRepository).should(never()).deleteById(anyLong()); // Delete should not be called
    }
}