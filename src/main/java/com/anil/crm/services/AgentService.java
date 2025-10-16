package com.anil.crm.services;

import com.anil.crm.web.models.AgentDto;

import java.util.List;
import java.util.Optional;

public interface AgentService {

    Optional<AgentDto> getAgentById(Long id);

    List<AgentDto> getAgentsByName(String name);

    List<AgentDto> getAllAgents();

    AgentDto saveAgent(AgentDto agentDto);

    Optional<AgentDto> updateAgent(AgentDto agentDto);

    void deleteAgentById(Long id);

    List<AgentDto> getAgentsByDepartment(String department);
}




