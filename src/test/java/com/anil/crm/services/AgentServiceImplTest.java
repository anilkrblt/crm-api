package com.anil.crm.services;

import com.anil.crm.domain.Agent;
import com.anil.crm.domain.Department;
import com.anil.crm.domain.Role;
import com.anil.crm.domain.User;
import com.anil.crm.exceptions.EmailAlreadyExistsException;
import com.anil.crm.exceptions.ResourceInUseException;
import com.anil.crm.exceptions.ResourceNotFoundException;
import com.anil.crm.repositories.AgentRepository;
import com.anil.crm.repositories.DepartmentRepository;
import com.anil.crm.repositories.TicketRepository;
import com.anil.crm.repositories.UserRepository;
import com.anil.crm.web.mappers.AgentMapper;
import com.anil.crm.web.models.AgentDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.*;

@ExtendWith(MockitoExtension.class)
class AgentServiceImplTest {

    @Mock
    AgentRepository agentRepository;
    @Mock
    UserRepository userRepository;
    @Mock
    DepartmentRepository departmentRepository;
    @Mock
    TicketRepository ticketRepository;
    @Mock
    PasswordEncoder passwordEncoder;
    @Mock
    AgentMapper agentMapper;

    @InjectMocks
    AgentServiceImpl agentService;

    Agent agent1;
    AgentDto agentDto1;
    User agentUser1;
    Department techSupport;
    Long agentId = 1L;
    String agentEmail = "agent@test.com";

    @BeforeEach
    void setUp() {
        techSupport = Department.builder().id(1L).name("Teknik Destek").build();

        agentUser1 = User.builder()
                .id(1L)
                .email(agentEmail)
                .firstName("Test")
                .lastName("Agent")
                .role(Role.AGENT)
                .password("hashedPassword")
                .build();

        agent1 = Agent.builder()
                .id(agentId)
                .department(techSupport)
                .user(agentUser1)
                .build();

        agentDto1 = AgentDto.builder()
                .id(agentId)
                .email(agentEmail)
                .firstName("Test")
                .lastName("Agent")
                .departmentName("Teknik Destek")
                .build();
    }

    @Test
    void getAgentById_shouldReturnAgentDto_whenFound() {
        given(agentRepository.findById(agentId)).willReturn(Optional.of(agent1));
        given(agentMapper.agentToAgentDto(agent1)).willReturn(agentDto1);

        AgentDto foundDto = agentService.getAgentById(agentId);

        assertNotNull(foundDto);
        assertEquals(agentId, foundDto.getId());
        assertEquals(agentEmail, foundDto.getEmail());
        then(agentRepository).should().findById(agentId);
        then(agentMapper).should().agentToAgentDto(agent1);
    }

    @Test
    void getAgentById_shouldThrowResourceNotFoundException_whenNotFound() {
        Long nonExistentId = 99L;
        given(agentRepository.findById(nonExistentId)).willReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> {
            agentService.getAgentById(nonExistentId);
        });

        then(agentRepository).should().findById(nonExistentId);
        then(agentMapper).should(never()).agentToAgentDto(any());
    }

    @Test
    void findAgents_shouldReturnAllAgents_whenNoFilters() {
        given(agentRepository.findAll()).willReturn(List.of(agent1));
        given(agentMapper.agentToAgentDto(agent1)).willReturn(agentDto1);

        List<AgentDto> result = agentService.findAgents(null, null);

        assertNotNull(result);
        assertEquals(1, result.size());
        then(agentRepository).should().findAll();
    }

    @Test
    void findAgents_shouldFilterByName_whenNameProvided() {
        String name = "Test";
        given(agentRepository.findAgentsByUserFirstNameContainingOrUserLastNameContaining(name, name)).willReturn(List.of(agent1));
        given(agentMapper.agentToAgentDto(agent1)).willReturn(agentDto1);

        List<AgentDto> result = agentService.findAgents(name, null);

        assertNotNull(result);
        assertEquals(1, result.size());
        then(agentRepository).should().findAgentsByUserFirstNameContainingOrUserLastNameContaining(name, name);
    }

    @Test
    void findAgents_shouldFilterByDepartment_whenDepartmentProvided() {
        String deptName = "Teknik";
        given(agentRepository.findAgentsByDepartmentNameContainingIgnoreCase(deptName)).willReturn(List.of(agent1));
        given(agentMapper.agentToAgentDto(agent1)).willReturn(agentDto1);

        List<AgentDto> result = agentService.findAgents(null, deptName);

        assertNotNull(result);
        assertEquals(1, result.size());
        then(agentRepository).should().findAgentsByDepartmentNameContainingIgnoreCase(deptName);
    }

    @Test
    void findAgents_shouldFilterByBoth_whenBothProvided() {
        String name = "Test";
        String deptName = "Teknik";
        given(agentRepository.findByDepartmentNameContainingAndUserNameContaining(deptName, name)).willReturn(List.of(agent1));
        given(agentMapper.agentToAgentDto(agent1)).willReturn(agentDto1);

        List<AgentDto> result = agentService.findAgents(name, deptName);

        assertNotNull(result);
        assertEquals(1, result.size());
        then(agentRepository).should().findByDepartmentNameContainingAndUserNameContaining(deptName, name);
    }

    @Test
    void createAgent_shouldSaveAndReturnAgent() {
        AgentDto dtoToSave = AgentDto.builder()
                .email("new@agent.com")
                .firstName("New")
                .lastName("Agent")
                .password("password123")
                .departmentName("Teknik Destek")
                .build();

        given(userRepository.findByEmail(dtoToSave.getEmail())).willReturn(Optional.empty());
        given(departmentRepository.findByName(dtoToSave.getDepartmentName())).willReturn(Optional.of(techSupport));
        given(passwordEncoder.encode(dtoToSave.getPassword())).willReturn("hashedPassword");

        Agent agentToSave = Agent.builder().department(techSupport).user(User.builder().email(dtoToSave.getEmail()).build()).build();
        Agent savedAgent = Agent.builder().id(2L).department(techSupport).user(User.builder().id(2L).email(dtoToSave.getEmail()).build()).build();

        given(agentRepository.save(any(Agent.class))).willReturn(savedAgent);
        given(agentMapper.agentToAgentDto(savedAgent)).willReturn(AgentDto.builder().id(2L).email(dtoToSave.getEmail()).build());

        AgentDto resultDto = agentService.createAgent(dtoToSave);

        assertNotNull(resultDto);
        assertEquals(2L, resultDto.getId());
        assertEquals(dtoToSave.getEmail(), resultDto.getEmail());

        then(userRepository).should().findByEmail(dtoToSave.getEmail());
        then(departmentRepository).should().findByName(dtoToSave.getDepartmentName());
        then(passwordEncoder).should().encode(dtoToSave.getPassword());
        then(agentRepository).should().save(any(Agent.class));
        then(agentMapper).should().agentToAgentDto(savedAgent);
    }

    @Test
    void createAgent_shouldThrowEmailExistsException() {
        AgentDto dtoToSave = AgentDto.builder().email(agentEmail).build();
        given(userRepository.findByEmail(agentEmail)).willReturn(Optional.of(agentUser1));

        assertThrows(EmailAlreadyExistsException.class, () -> agentService.createAgent(dtoToSave));

        then(departmentRepository).should(never()).findByName(anyString());
        then(agentRepository).should(never()).save(any());
    }

    @Test
    void createAgent_shouldThrowResourceNotFoundException_whenDeptNotFound() {
        AgentDto dtoToSave = AgentDto.builder()
                .email("new@agent.com")
                .departmentName("Olmayan Departman")
                .build();

        given(userRepository.findByEmail(dtoToSave.getEmail())).willReturn(Optional.empty());
        given(departmentRepository.findByName(dtoToSave.getDepartmentName())).willReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> agentService.createAgent(dtoToSave));

        then(userRepository).should().findByEmail(dtoToSave.getEmail());
        then(departmentRepository).should().findByName(dtoToSave.getDepartmentName());
        then(agentRepository).should(never()).save(any());
    }

    @Test
    void updateAgent_shouldUpdateAndReturnDto() {
        AgentDto updatesDto = AgentDto.builder()
                .firstName("Updated")
                .lastName("Agent")
                .email(agentEmail)
                .departmentName("Teknik Destek")
                .build();

        given(agentRepository.findById(agentId)).willReturn(Optional.of(agent1));
        given(agentRepository.save(agent1)).willReturn(agent1);
        given(agentMapper.agentToAgentDto(agent1)).willReturn(agentDto1);

        AgentDto resultDto = agentService.updateAgent(agentId, updatesDto);

        assertNotNull(resultDto);
        then(agentRepository).should().findById(agentId);
        then(agentRepository).should().save(agent1);

        ArgumentCaptor<Agent> agentCaptor = ArgumentCaptor.forClass(Agent.class);
        then(agentRepository).should().save(agentCaptor.capture());
        assertEquals("Updated", agentCaptor.getValue().getUser().getFirstName());
    }

    @Test
    void deleteAgentById_shouldDelete_whenNoTickets() {
        given(agentRepository.existsById(agentId)).willReturn(true);
        given(ticketRepository.existsByAssignedAgentId(agentId)).willReturn(false);
        willDoNothing().given(agentRepository).deleteById(agentId);

        assertDoesNotThrow(() -> agentService.deleteAgentById(agentId));

        then(agentRepository).should().existsById(agentId);
        then(ticketRepository).should().existsByAssignedAgentId(agentId);
        then(agentRepository).should().deleteById(agentId);
    }

    @Test
    void deleteAgentById_shouldThrowNotFound_whenAgentNotExists() {
        Long nonExistentId = 99L;
        given(agentRepository.existsById(nonExistentId)).willReturn(false);

        assertThrows(ResourceNotFoundException.class, () -> agentService.deleteAgentById(nonExistentId));

        then(agentRepository).should().existsById(nonExistentId);
        then(ticketRepository).should(never()).existsByAssignedAgentId(anyLong());
        then(agentRepository).should(never()).deleteById(anyLong());
    }

    @Test
    void deleteAgentById_shouldThrowInUse_whenTicketsExist() {
        given(agentRepository.existsById(agentId)).willReturn(true);
        given(ticketRepository.existsByAssignedAgentId(agentId)).willReturn(true);

        assertThrows(ResourceInUseException.class, () -> agentService.deleteAgentById(agentId));

        then(agentRepository).should().existsById(agentId);
        then(ticketRepository).should().existsByAssignedAgentId(agentId);
        then(agentRepository).should(never()).deleteById(anyLong());
    }
}