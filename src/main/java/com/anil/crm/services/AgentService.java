package com.anil.crm.services;

import com.anil.crm.web.models.AgentDto;

import java.util.List;

public interface AgentService {


    AgentDto getAgentById(Long id);

    List<AgentDto> findAgents(String name, String department);

    AgentDto createAgent(AgentDto agentDto);

    AgentDto updateAgent(Long id, AgentDto agentDto);

    void deleteAgentById(Long id);


}