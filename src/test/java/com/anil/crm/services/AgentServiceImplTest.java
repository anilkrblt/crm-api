package com.anil.crm.services;

import com.anil.crm.domain.Agent;
import com.anil.crm.domain.Role;
import com.anil.crm.domain.User;
import com.anil.crm.exceptions.EmailAlreadyExistsException;
import com.anil.crm.exceptions.ResourceNotFoundException;
import com.anil.crm.repositories.AgentRepository;
import com.anil.crm.repositories.UserRepository;
import com.anil.crm.web.mappers.AgentMapper;
import com.anil.crm.web.models.AgentDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Agent Service Implementation Unit Tests")
class AgentServiceImplTest {

    @Mock
    AgentRepository agentRepository;

    @Mock
    UserRepository userRepository;

    @Mock
    PasswordEncoder passwordEncoder;

    @Mock
    AgentMapper agentMapper;

    @InjectMocks
    AgentServiceImpl agentService;

    AgentDto agentDto;
    Agent agent;
    User user;

    @BeforeEach
    void setUp() {
        user = User.builder()
                .id(1L)
                .email("agent@test.com")
                .firstName("Test")
                .lastName("Agent")
                .role(Role.AGENT)
                .password("hashedPassword")
                .build();
        agent = Agent.builder()
                .id(1L)
                .department("Support")
                .user(user)
                .createdAt(LocalDateTime.now().minusDays(1))
                .updatedAt(LocalDateTime.now().minusDays(1))
                .build();
        agentDto = AgentDto.builder()
                .id(1L)
                .email("agent@test.com")
                .firstName("Test")
                .lastName("Agent")
                .department("Support")
                .createdAt(agent.getCreatedAt())
                .updatedAt(agent.getUpdatedAt())
                .build();
    }

    @Test
    @DisplayName("Get All Agents - Success")
    void getAllAgents_shouldReturnListOfAgentDtos() {
        // Given
        given(agentRepository.findAll()).willReturn(List.of(agent));
        given(agentMapper.agentToAgentDto(agent)).willReturn(agentDto);

        // When
        List<AgentDto> result = agentService.getAllAgents();

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(agentDto.getId(), result.get(0).getId());
        then(agentRepository).should().findAll();
        then(agentMapper).should().agentToAgentDto(agent);
    }

    @Test
    @DisplayName("Get Agent By ID - Success")
    void getAgentById_shouldReturnAgentDto_whenFound() {
        // Given
        given(agentRepository.findById(1L)).willReturn(Optional.of(agent));
        given(agentMapper.agentToAgentDto(agent)).willReturn(agentDto);

        // When
        AgentDto foundDto = agentService.getAgentById(1L);

        // Then
        assertNotNull(foundDto);
        assertEquals(agentDto.getId(), foundDto.getId());
        assertEquals(agentDto.getEmail(), foundDto.getEmail());
        then(agentRepository).should().findById(1L);
        then(agentMapper).should().agentToAgentDto(agent);
    }

    @Test
    @DisplayName("Get Agent By ID - Not Found")
    void getAgentById_shouldThrowResourceNotFoundException_whenNotFound() {
        // Given
        given(agentRepository.findById(99L)).willReturn(Optional.empty());

        // When & Then
        assertThrows(ResourceNotFoundException.class, () -> agentService.getAgentById(99L));
        then(agentRepository).should().findById(99L);
        then(agentMapper).should(never()).agentToAgentDto(any());
    }

    @Test
    @DisplayName("Get Agent By Email - Success")
    void getAgentByEmail_shouldReturnAgentDto_whenFound() {
        // Given
        given(agentRepository.findAgentByUserEmail("agent@test.com")).willReturn(Optional.of(agent));
        given(agentMapper.agentToAgentDto(agent)).willReturn(agentDto);

        // When
        AgentDto foundDto = agentService.getAgentByEmail("agent@test.com");

        // Then
        assertNotNull(foundDto);
        assertEquals(agentDto.getEmail(), foundDto.getEmail());
        then(agentRepository).should().findAgentByUserEmail("agent@test.com");
        then(agentMapper).should().agentToAgentDto(agent);
    }

    @Test
    @DisplayName("Get Agent By Email - Not Found")
    void getAgentByEmail_shouldThrowResourceNotFoundException_whenNotFound() {
        // Given
        given(agentRepository.findAgentByUserEmail("notfound@test.com")).willReturn(Optional.empty());

        // When & Then
        assertThrows(ResourceNotFoundException.class, () -> agentService.getAgentByEmail("notfound@test.com"));
        then(agentRepository).should().findAgentByUserEmail("notfound@test.com");
        then(agentMapper).should(never()).agentToAgentDto(any());
    }

    @Test
    @DisplayName("Get Agents By User Name - Success")
    void getAgentsByUserName_shouldReturnListOfAgentDtos() {
        // Given
        String name = "Test";
        given(agentRepository.findAgentsByUserFirstNameContainingOrUserLastNameContaining(name, name)).willReturn(List.of(agent));
        given(agentMapper.agentToAgentDto(agent)).willReturn(agentDto);

        // When
        List<AgentDto> result = agentService.getAgentsByUserName(name);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        then(agentRepository).should().findAgentsByUserFirstNameContainingOrUserLastNameContaining(name, name);
        then(agentMapper).should().agentToAgentDto(agent);
    }

    @Test
    @DisplayName("Get Agents By Department - Success")
    void getAgentsByDepartment_shouldReturnListOfAgentDtos() {
        // Given
        String department = "Support";
        given(agentRepository.findAgentsByDepartment(department)).willReturn(List.of(agent));
        given(agentMapper.agentToAgentDto(agent)).willReturn(agentDto);

        // When
        List<AgentDto> result = agentService.getAgentsByDepartment(department);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(department, result.get(0).getDepartment());
        then(agentRepository).should().findAgentsByDepartment(department);
        then(agentMapper).should().agentToAgentDto(agent);
    }

    @Test
    @DisplayName("Create Agent - Success")
    void createAgent_shouldSaveAndReturnAgent() {
        // Given
        AgentDto dtoToSave = AgentDto.builder()
                .email("new.agent@test.com")
                .firstName("New")
                .lastName("Agent")
                .password("password123")
                .department("Sales")
                .build();

        given(userRepository.findByEmail(dtoToSave.getEmail())).willReturn(Optional.empty());
        given(passwordEncoder.encode(dtoToSave.getPassword())).willReturn("hashedPassword");
        given(agentRepository.save(any(Agent.class))).willAnswer(invocation -> {
            Agent saved = invocation.getArgument(0);
            saved.setId(2L);
            saved.getUser().setId(2L);
            saved.setCreatedAt(LocalDateTime.now()); // Set timestamps for verification
            saved.setUpdatedAt(LocalDateTime.now());
            return saved;
        });
        given(agentMapper.agentToAgentDto(any(Agent.class))).willAnswer(invocation -> {
            Agent saved = invocation.getArgument(0);
            return AgentDto.builder()
                    .id(saved.getId())
                    .email(saved.getUser().getEmail())
                    .firstName(saved.getUser().getFirstName())
                    .lastName(saved.getUser().getLastName())
                    .department(saved.getDepartment())
                    .createdAt(saved.getCreatedAt())
                    .updatedAt(saved.getUpdatedAt())
                    .build();
        });

        // When
        AgentDto savedDto = agentService.createAgent(dtoToSave);

        // Then
        assertNotNull(savedDto);
        assertEquals(2L, savedDto.getId());
        assertEquals(dtoToSave.getEmail(), savedDto.getEmail());
        assertEquals(dtoToSave.getDepartment(), savedDto.getDepartment());
        assertNotNull(savedDto.getCreatedAt()); // Ensure timestamps are set
        then(userRepository).should().findByEmail(dtoToSave.getEmail());
        then(passwordEncoder).should().encode(dtoToSave.getPassword());
        then(agentRepository).should().save(any(Agent.class));
        then(agentMapper).should().agentToAgentDto(any(Agent.class));
    }

    @Test
    @DisplayName("Create Agent - Email Exists")
    void createAgent_shouldThrowEmailExistsException_whenEmailInUse() {
        // Given
        AgentDto dtoToSave = AgentDto.builder().email("agent@test.com").password("pwd").build();
        given(userRepository.findByEmail(dtoToSave.getEmail())).willReturn(Optional.of(user));

        // When & Then
        assertThrows(EmailAlreadyExistsException.class, () -> agentService.createAgent(dtoToSave));
        then(userRepository).should().findByEmail(dtoToSave.getEmail());
        then(passwordEncoder).should(never()).encode(anyString());
        then(agentRepository).should(never()).save(any(Agent.class));
    }

    @Test
    @DisplayName("Update Agent - Success")
    void updateAgent_shouldUpdateAndReturnAgent() {
        // Given
        Long agentId = 1L;
        AgentDto updatesDto = AgentDto.builder()
                .firstName("Updated First") // Name change
                .lastName("Updated Last")
                .email("updated.agent@test.com") // Email change
                .department("Updated Support") // Department change
                // No password -> should not update password
                .build();

        // Need the existing agent with user for the update process
        given(agentRepository.findById(agentId)).willReturn(Optional.of(agent));
        // Assume email update is valid (email not taken)
        given(userRepository.findByEmail(updatesDto.getEmail())).willReturn(Optional.empty());
        // Mock the save operation to return the updated entity
        given(agentRepository.save(any(Agent.class))).willAnswer(invocation -> invocation.getArgument(0)); // Return the input
        // Mock the mapper to return the DTO based on the updated entity
        given(agentMapper.agentToAgentDto(any(Agent.class))).willAnswer(invocation -> {
            Agent updated = invocation.getArgument(0);
            // Manually create DTO reflecting changes for assertion
            return AgentDto.builder()
                    .id(updated.getId())
                    .email(updated.getUser().getEmail()) // Updated email
                    .firstName(updated.getUser().getFirstName()) // Updated name
                    .lastName(updated.getUser().getLastName())
                    .department(updated.getDepartment()) // Updated department
                    .createdAt(updated.getCreatedAt())
                    .updatedAt(updated.getUpdatedAt()) // Should be updated now
                    .build();
        });


        // When
        AgentDto updatedDto = agentService.updateAgent(agentId, updatesDto);

        // Then
        assertNotNull(updatedDto);
        assertEquals(agentId, updatedDto.getId());
        assertEquals(updatesDto.getEmail(), updatedDto.getEmail());
        assertEquals(updatesDto.getFirstName(), updatedDto.getFirstName());
        assertEquals(updatesDto.getDepartment(), updatedDto.getDepartment());
        assertNotEquals(agent.getUpdatedAt(), updatedDto.getUpdatedAt()); // Check if updatedAt changed

        then(agentRepository).should().findById(agentId);
        then(userRepository).should().findByEmail(updatesDto.getEmail());
        then(passwordEncoder).should(never()).encode(anyString()); // Password not provided, shouldn't encode
        then(agentRepository).should().save(any(Agent.class));
        then(agentMapper).should().agentToAgentDto(any(Agent.class));
    }

    @Test
    @DisplayName("Update Agent - Update Password Success")
    void updateAgent_shouldUpdatePassword_whenPasswordProvided() {
        // Given
        Long agentId = 1L;
        String newPassword = "newPassword123";
        AgentDto updatesDto = AgentDto.builder()
                .firstName(agent.getUser().getFirstName()) // Keep other fields same
                .lastName(agent.getUser().getLastName())
                .email(agent.getUser().getEmail())
                .department(agent.getDepartment())
                .password(newPassword) // Provide new password
                .build();

        given(agentRepository.findById(agentId)).willReturn(Optional.of(agent));
        // If email is not changing, no need to mock userRepository.findByEmail
        given(passwordEncoder.encode(newPassword)).willReturn("hashedNewPassword");
        given(agentRepository.save(any(Agent.class))).willReturn(agent); // Return same agent for simplicity
        given(agentMapper.agentToAgentDto(agent)).willReturn(agentDto); // Return original DTO

        // When
        agentService.updateAgent(agentId, updatesDto);

        // Then
        then(agentRepository).should().findById(agentId);
        then(passwordEncoder).should().encode(newPassword); // Verify password encoding happened
        then(agentRepository).should().save(any(Agent.class));
    }


    @Test
    @DisplayName("Update Agent - Not Found")
    void updateAgent_shouldThrowNotFoundException_whenAgentNotFound() {
        // Given
        Long nonExistentId = 99L;
        AgentDto updatesDto = AgentDto.builder().email("any@email.com").build();
        given(agentRepository.findById(nonExistentId)).willReturn(Optional.empty());

        // When & Then
        assertThrows(ResourceNotFoundException.class, () -> agentService.updateAgent(nonExistentId, updatesDto));
        then(agentRepository).should().findById(nonExistentId);
        then(userRepository).should(never()).findByEmail(anyString());
        then(agentRepository).should(never()).save(any());
    }

    @Test
    @DisplayName("Update Agent - Email Exists")
    void updateAgent_shouldThrowEmailExistsException_whenEmailTaken() {
        // Given
        Long agentId = 1L;
        AgentDto updatesDto = AgentDto.builder()
                .email("existing.other@test.com") // Email that will be found
                .firstName("Test").lastName("Agent").department("Support") // Include required fields
                .build();
        User otherUser = User.builder().id(2L).email("existing.other@test.com").build();

        given(agentRepository.findById(agentId)).willReturn(Optional.of(agent));
        // Mock userRepository to find another user with the target email
        given(userRepository.findByEmail(updatesDto.getEmail())).willReturn(Optional.of(otherUser));

        // When & Then
        assertThrows(EmailAlreadyExistsException.class, () -> agentService.updateAgent(agentId, updatesDto));

        then(agentRepository).should().findById(agentId);
        then(userRepository).should().findByEmail(updatesDto.getEmail());
        then(agentRepository).should(never()).save(any()); // Should not save if email exists
    }


    @Test
    @DisplayName("Delete Agent By ID - Success")
    void deleteAgentById_shouldCallRepositoryDelete_whenExists() {
        // Given
        Long agentId = 1L;
        given(agentRepository.existsById(agentId)).willReturn(true);
        willDoNothing().given(agentRepository).deleteById(agentId); // Mock void method

        // When
        agentService.deleteAgentById(agentId);

        // Then
        then(agentRepository).should().existsById(agentId);
        then(agentRepository).should().deleteById(agentId);
    }

    @Test
    @DisplayName("Delete Agent By ID - Not Found")
    void deleteAgentById_shouldThrowNotFoundException_whenNotExists() {
        // Given
        Long nonExistentId = 99L;
        given(agentRepository.existsById(nonExistentId)).willReturn(false);

        // When & Then
        assertThrows(ResourceNotFoundException.class, () -> agentService.deleteAgentById(nonExistentId));

        then(agentRepository).should().existsById(nonExistentId);
        then(agentRepository).should(never()).deleteById(anyLong()); // Delete should not be called
    }
}