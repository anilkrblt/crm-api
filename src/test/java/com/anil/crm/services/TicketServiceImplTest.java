package com.anil.crm.services;

import com.anil.crm.domain.*;
import com.anil.crm.exceptions.ResourceNotFoundException;
import com.anil.crm.repositories.AgentRepository;
import com.anil.crm.repositories.CustomerRepository;
import com.anil.crm.repositories.DepartmentRepository;
import com.anil.crm.repositories.TicketRepository;
import com.anil.crm.web.mappers.TicketMapper;
import com.anil.crm.web.models.AgentDto;
import com.anil.crm.web.models.CustomerDto;
import com.anil.crm.web.models.DepartmentDto;
import com.anil.crm.web.models.TicketDto;
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
class TicketServiceImplTest {

    @Mock
    TicketRepository ticketRepository;
    @Mock
    CustomerRepository customerRepository;
    @Mock
    AgentRepository agentRepository;
    @Mock
    DepartmentRepository departmentRepository;
    @Mock
    TicketMapper ticketMapper;

    @InjectMocks
    TicketServiceImpl ticketService;

    Ticket ticket1;
    TicketDto ticketDto1;
    Customer customer;
    Agent agent;
    Department department;
    Long ticketId1 = 1L;
    Long customerId = 1L;
    Long agentId = 1L;
    Long departmentId = 1L;

    @BeforeEach
    void setUp() {
        User customerUser = User.builder().id(1L).email("cust@test.com").role(Role.CUSTOMER).build();
        User agentUser = User.builder().id(2L).email("agent@test.com").role(Role.AGENT).build();
        customer = Customer.builder().id(customerId).user(customerUser).build();
        department = Department.builder().id(departmentId).name("Teknik Destek").build();
        agent = Agent.builder().id(agentId).user(agentUser).department(department).build();

        ticket1 = Ticket.builder()
                .id(ticketId1)
                .customer(customer)
                .department(department)
                .assignedAgent(agent)
                .subject("Test Ticket 1")
                .status(TicketStatus.OPEN)
                .priority(TicketPriority.MEDIUM)
                .createdAt(LocalDateTime.now().minusHours(1))
                .updatedAt(LocalDateTime.now().minusHours(1))
                .build();

        ticketDto1 = TicketDto.builder()
                .id(ticketId1)
                .customer(CustomerDto.builder().id(customerId).build())
                .department(DepartmentDto.builder().id(departmentId).name("Teknik Destek").build())
                .assignedAgent(AgentDto.builder().id(agentId).build())
                .subject("Test Ticket 1")
                .status(TicketStatus.OPEN)
                .priority(TicketPriority.MEDIUM)
                .createdAt(ticket1.getCreatedAt())
                .updatedAt(ticket1.getUpdatedAt())
                .build();
    }

    @Test
    void getAllTickets() {
        given(ticketRepository.findAll()).willReturn(List.of(ticket1));
        given(ticketMapper.ticketToTicketDto(ticket1)).willReturn(ticketDto1);

        List<TicketDto> result = ticketService.getAllTickets();

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(ticketId1, result.get(0).getId());
        then(ticketRepository).should().findAll();
        then(ticketMapper).should().ticketToTicketDto(ticket1);
    }

    @Test
    void getTicketById() {
        given(ticketRepository.findById(ticketId1)).willReturn(Optional.of(ticket1));
        given(ticketMapper.ticketToTicketDto(ticket1)).willReturn(ticketDto1);

        TicketDto foundDto = ticketService.getTicketById(ticketId1);

        assertNotNull(foundDto);
        assertEquals(ticketId1, foundDto.getId());
        then(ticketRepository).should().findById(ticketId1);
        then(ticketMapper).should().ticketToTicketDto(ticket1);
    }

    @Test
    void getTicketById_NotFound() {
        Long nonExistentId = 99L;
        given(ticketRepository.findById(nonExistentId)).willReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> ticketService.getTicketById(nonExistentId));
        then(ticketRepository).should().findById(nonExistentId);
        then(ticketMapper).should(never()).ticketToTicketDto(any());
    }

    @Test
    void getTicketsByCustomerId() {
        // Given
        given(ticketRepository.findTicketsByCustomerId(customerId)).willReturn(List.of(ticket1));
        given(ticketMapper.ticketToTicketDto(ticket1)).willReturn(ticketDto1);

        List<TicketDto> result = ticketService.getTicketsByCustomerId(customerId);

        assertNotNull(result);
        assertEquals(1, result.size());
        then(ticketRepository).should().findTicketsByCustomerId(customerId);
    }

    @Test
    void getTicketsByAssignedAgentId() {
        given(ticketRepository.findTicketsByAssignedAgentId(agentId)).willReturn(List.of(ticket1));
        given(ticketMapper.ticketToTicketDto(ticket1)).willReturn(ticketDto1);

        List<TicketDto> result = ticketService.getTicketsByAssignedAgentId(agentId);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(agentId, result.get(0).getAssignedAgent().getId());
        then(ticketRepository).should().findTicketsByAssignedAgentId(agentId);
    }

    @Test
    void getTicketsByDepartmentId() {
        given(ticketRepository.findTicketsByDepartmentId(departmentId)).willReturn(List.of(ticket1));
        given(ticketMapper.ticketToTicketDto(ticket1)).willReturn(ticketDto1);

        List<TicketDto> result = ticketService.getTicketsByDepartmentId(departmentId);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(departmentId, result.get(0).getDepartment().getId());
        then(ticketRepository).should().findTicketsByDepartmentId(departmentId);
    }

    @Test
    void getTicketsByStatus() {
        TicketStatus status = TicketStatus.OPEN;
        given(ticketRepository.findTicketsByStatus(status)).willReturn(List.of(ticket1));
        given(ticketMapper.ticketToTicketDto(ticket1)).willReturn(ticketDto1);

        List<TicketDto> result = ticketService.getTicketsByStatus(status);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(status, result.get(0).getStatus());
        then(ticketRepository).should().findTicketsByStatus(status);
    }

    @Test
    void getTicketsByPriority() {
        TicketPriority priority = TicketPriority.MEDIUM;
        given(ticketRepository.getTicketsByPriority(priority)).willReturn(List.of(ticket1));
        given(ticketMapper.ticketToTicketDto(ticket1)).willReturn(ticketDto1);

        List<TicketDto> result = ticketService.getTicketsByPriority(priority);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(priority, result.get(0).getPriority());
        then(ticketRepository).should().getTicketsByPriority(priority);
    }

    @Test
    void createTicket() {
        TicketDto dtoToSave = TicketDto.builder()
                .subject("New Ticket")
                .description("Create test")
                .priority(TicketPriority.LOW)
                .status(TicketStatus.OPEN)
                .customer(CustomerDto.builder().id(customerId).build())
                .department(DepartmentDto.builder().id(departmentId).build())
                .assignedAgent(null)
                .build();

        Ticket transientTicket = new Ticket();
        Ticket savedTicket = new Ticket();
        savedTicket.setId(3L);
        savedTicket.setCustomer(customer);
        savedTicket.setDepartment(department);

        TicketDto finalDto = TicketDto.builder().id(3L).build();

        given(customerRepository.findById(customerId)).willReturn(Optional.of(customer));
        given(departmentRepository.findById(departmentId)).willReturn(Optional.of(department));
        given(ticketMapper.ticketDtoToTicket(dtoToSave)).willReturn(transientTicket);
        given(ticketRepository.save(transientTicket)).willReturn(savedTicket);
        given(ticketMapper.ticketToTicketDto(savedTicket)).willReturn(finalDto);

        TicketDto resultDto = ticketService.createTicket(dtoToSave);

        assertNotNull(resultDto);
        assertEquals(3L, resultDto.getId());

        assertEquals(customer, transientTicket.getCustomer());
        assertEquals(department, transientTicket.getDepartment());
        assertNull(transientTicket.getAssignedAgent());
        assertNull(transientTicket.getId());

        then(customerRepository).should().findById(customerId);
        then(departmentRepository).should().findById(departmentId);
        then(agentRepository).should(never()).findById(anyLong());
        then(ticketRepository).should().save(transientTicket);
    }

    @Test
    void createTicket_CustomerNotFound() {
        TicketDto dtoToSave = TicketDto.builder()
                .customer(CustomerDto.builder().id(99L).build())
                .department(DepartmentDto.builder().id(departmentId).build())
                .build();

        given(customerRepository.findById(99L)).willReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> ticketService.createTicket(dtoToSave));
        then(customerRepository).should().findById(99L);
        then(ticketRepository).should(never()).save(any());
    }

    @Test
    void createTicket_WithAgent() {
        TicketDto dtoToSave = TicketDto.builder()
                .customer(CustomerDto.builder().id(customerId).build())
                .department(DepartmentDto.builder().id(departmentId).build())
                .assignedAgent(AgentDto.builder().id(agentId).build())
                .build();

        given(customerRepository.findById(customerId)).willReturn(Optional.of(customer));
        given(departmentRepository.findById(departmentId)).willReturn(Optional.of(department));
        given(agentRepository.findById(agentId)).willReturn(Optional.of(agent));
        given(ticketMapper.ticketDtoToTicket(dtoToSave)).willReturn(new Ticket());
        given(ticketRepository.save(any(Ticket.class))).willReturn(new Ticket());
        given(ticketMapper.ticketToTicketDto(any(Ticket.class))).willReturn(new TicketDto());

        ticketService.createTicket(dtoToSave);

        then(agentRepository).should().findById(agentId);

        ArgumentCaptor<Ticket> ticketCaptor = ArgumentCaptor.forClass(Ticket.class);
        then(ticketRepository).should().save(ticketCaptor.capture());
        assertEquals(agent, ticketCaptor.getValue().getAssignedAgent());
    }

    @Test
    void updateTicket() {
        TicketDto updatesDto = TicketDto.builder()
                .subject("Updated Subject")
                .status(TicketStatus.CLOSED)
                .build();

        given(ticketRepository.findById(ticketId1)).willReturn(Optional.of(ticket1));
        willDoNothing().given(ticketMapper).updateTicketFromDto(updatesDto, ticket1);
        given(ticketRepository.save(ticket1)).willReturn(ticket1);
        given(ticketMapper.ticketToTicketDto(ticket1)).willReturn(ticketDto1);

        TicketDto resultDto = ticketService.updateTicket(ticketId1, updatesDto);

        assertNotNull(resultDto);
        then(ticketRepository).should().findById(ticketId1);
        then(ticketMapper).should().updateTicketFromDto(updatesDto, ticket1);
        then(ticketRepository).should().save(ticket1);
    }

    @Test
    void updateTicket_NotFound() {
        Long nonExistentId = 99L;
        TicketDto updatesDto = TicketDto.builder().build();
        given(ticketRepository.findById(nonExistentId)).willReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> ticketService.updateTicket(nonExistentId, updatesDto));
        then(ticketRepository).should().findById(nonExistentId);
        then(ticketMapper).should(never()).updateTicketFromDto(any(), any());
        then(ticketRepository).should(never()).save(any());
    }

    @Test
    void updateTicketStatus() {
        TicketStatus newStatus = TicketStatus.CLOSED;
        given(ticketRepository.findById(ticketId1)).willReturn(Optional.of(ticket1));
        given(ticketRepository.save(ticket1)).willReturn(ticket1);
        given(ticketMapper.ticketToTicketDto(ticket1)).willReturn(ticketDto1);

        ticketService.updateTicketStatus(ticketId1, newStatus);

        ArgumentCaptor<Ticket> ticketCaptor = ArgumentCaptor.forClass(Ticket.class);
        then(ticketRepository).should().save(ticketCaptor.capture());
        assertEquals(newStatus, ticketCaptor.getValue().getStatus());

        then(ticketRepository).should().findById(ticketId1);
    }

    @Test
    void updateTicketStatus_NotFound() {
        Long nonExistentId = 99L;
        TicketStatus newStatus = TicketStatus.CLOSED;
        given(ticketRepository.findById(nonExistentId)).willReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> ticketService.updateTicketStatus(nonExistentId, newStatus));
        then(ticketRepository).should(never()).save(any());
    }

    @Test
    void deleteTicketById() {
        given(ticketRepository.existsById(ticketId1)).willReturn(true);
        willDoNothing().given(ticketRepository).deleteById(ticketId1);

        assertDoesNotThrow(() -> ticketService.deleteTicketById(ticketId1));

        then(ticketRepository).should().existsById(ticketId1);
        then(ticketRepository).should().deleteById(ticketId1);
    }

    @Test
    void deleteTicketById_NotFound() {
        Long nonExistentId = 99L;
        given(ticketRepository.existsById(nonExistentId)).willReturn(false);

        assertThrows(ResourceNotFoundException.class, () -> ticketService.deleteTicketById(nonExistentId));

        then(ticketRepository).should().existsById(nonExistentId);
        then(ticketRepository).should(never()).deleteById(anyLong());
    }
}
