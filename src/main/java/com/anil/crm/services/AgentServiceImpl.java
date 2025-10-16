package com.anil.crm.services;

import com.anil.crm.domain.Agent;
import com.anil.crm.repositories.AgentRepository;
import com.anil.crm.web.mappers.AgentMapper;
import com.anil.crm.web.models.AgentDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AgentServiceImpl implements AgentService {

    private final AgentRepository agentRepository;
    private final AgentMapper agentMapper;


    @Override
    public List<AgentDto> getAllAgents() {
        var agents = agentRepository.findAll();

        return agents
                .stream()
                .map(agentMapper::agentToAgentDto)
                .toList();
    }

    @Override
    public Optional<AgentDto> getAgentById(Long id) {
        return agentRepository.findById(id)
                .map(agentMapper::agentToAgentDto);
    }


    @Override
    public List<AgentDto> getAgentsByName(String name) {
        var agents = agentRepository.findByFullNameContainingIgnoreCase(name);

        return agents
                .stream()
                .map(agentMapper::agentToAgentDto)
                .toList();
    }

    @Override
    public List<AgentDto> getAgentsByDepartment(String department) {
        var agents = agentRepository.findAgentsByDepartment(department);
        return agents
                .stream()
                .map(agentMapper::agentToAgentDto)
                .toList();
    }


    @Override
    public AgentDto saveAgent(AgentDto agentDto) {
        return agentMapper
                .agentToAgentDto(agentRepository.save(agentMapper.agentDtoToAgent(agentDto)));
    }

    @Transactional
    @Override
    public Optional<AgentDto> updateAgent(AgentDto agentDto) {
        return agentRepository.findById(agentDto.getId())
                .map(entity -> {
                    entity.setFullName(agentDto.getFullName());
                    entity.setDepartment(agentDto.getDepartment());
                    entity.setEmail(agentDto.getEmail());
                    entity.setUpdatedAt(LocalDateTime.now());
                    return agentMapper.agentToAgentDto(agentRepository.save(entity));
                });
    }


    @Transactional
    @Override
    public void deleteAgentById(Long id) {
        agentRepository.deleteById(id);
    }


}
