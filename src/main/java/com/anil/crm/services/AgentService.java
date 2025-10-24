package com.anil.crm.services;

import com.anil.crm.web.models.AgentDto;
import java.util.List;

public interface AgentService {

    List<AgentDto> getAllAgents();

    AgentDto getAgentById(Long id);

    AgentDto getAgentByEmail(String email);

    List<AgentDto> getAgentsByUserName(String name);

    List<AgentDto> getAgentsByDepartment(String department);

    AgentDto createAgent(AgentDto agentDto);

    AgentDto updateAgent(Long id, AgentDto agentDto);

    void deleteAgentById(Long id);
}