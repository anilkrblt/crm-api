package com.anil.crm.web.mappers;


import com.anil.crm.domain.Agent;
import com.anil.crm.web.models.AgentDto;
import org.mapstruct.Mapper;

@Mapper
public interface AgentMapper {

    Agent agentDtoToAgent(AgentDto dto);
    AgentDto agentToAgentDto(Agent agent);

}
