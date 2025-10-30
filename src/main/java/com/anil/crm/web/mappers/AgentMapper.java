package com.anil.crm.web.mappers;

import com.anil.crm.domain.Agent;
import com.anil.crm.web.models.AgentDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;


@Mapper(componentModel = "spring")
public interface AgentMapper {


    @Mapping(target = "password", ignore = true)
    @Mapping(target = "firstName", source = "user.firstName")
    @Mapping(target = "lastName", source = "user.lastName")
    @Mapping(target = "email", source = "user.email")
    @Mapping(target = "departmentName", source = "department.name")
    AgentDto agentToAgentDto(Agent agent);



    @Mapping(target = "user.firstName", source = "firstName")
    @Mapping(target = "user.lastName", source = "lastName")
    @Mapping(target = "user.email", source = "email")
    @Mapping(target = "department", ignore = true)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "version", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "user.id", ignore = true)
    @Mapping(target = "user.password", ignore = true)
    @Mapping(target = "user.role", ignore = true)
    Agent agentDtoToAgent(AgentDto agentDto);

}