package com.anil.crm.services;

import com.anil.crm.domain.Agent;
import com.anil.crm.repositories.AgentRepository;
import com.anil.crm.web.mappers.AgentMapper;
import com.anil.crm.web.models.AgentDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class AgentServiceImplTest {

    @Autowired
    AgentService agentService;

    @Autowired
    AgentRepository agentRepository;

    @Autowired
    AgentMapper agentMapper;

    Agent testAgent;

    @BeforeEach
    void setUp() {
        testAgent = agentRepository.save(Agent.builder()
                .fullName("John Doe")
                .email("john@example.com")
                .passwordHash("hashedpassword")
                .department("IT")
                .build());
    }

    @Test
    void getAgentById() {
        Optional<AgentDto> agent = agentService.getAgentById(testAgent.getId());
        assertTrue(agent.isPresent());
        assertEquals("John Doe", agent.get().getFullName());
    }

    @Test
    void getAgentsByName() {
        List<AgentDto> agents = agentService.getAgentsByName("John");
        assertFalse(agents.isEmpty());
        assertEquals("John Doe", agents.get(0).getFullName());
    }

    @Test
    void getAllAgents() {
        List<AgentDto> agents = agentService.getAllAgents();
        assertFalse(agents.isEmpty());
    }

    @Test
    void saveAgent() {
        AgentDto newAgent = AgentDto.builder()
                .fullName("Jane Smith")
                .email("jane@example.com")
                .passwordHash("hashedpassword")
                .department("HR")
                .build();

        AgentDto savedAgent = agentService.saveAgent(newAgent);
        assertNotNull(savedAgent.getId());
        assertEquals("Jane Smith", savedAgent.getFullName());
    }

    @Test
    void updateAgent() {
        AgentDto updateDto = agentMapper.agentToAgentDto(testAgent);
        updateDto.setFullName("Updated Name");

        Optional<AgentDto> updated = agentService.updateAgent(updateDto);
        assertTrue(updated.isPresent());
        assertEquals("Updated Name", updated.get().getFullName());
    }

    @Test
    void deleteAgentById() {
        agentService.deleteAgentById(testAgent.getId());
        Optional<Agent> deleted = agentRepository.findById(testAgent.getId());
        assertTrue(deleted.isEmpty());
    }

    @Test
    void getAgentsByDepartment() {
        List<AgentDto> agents = agentService.getAgentsByDepartment("IT");
        assertFalse(agents.isEmpty());
        assertEquals("IT", agents.get(0).getDepartment());
    }
}
