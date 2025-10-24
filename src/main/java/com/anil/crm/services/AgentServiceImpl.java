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
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AgentServiceImpl implements AgentService {

    private static final Logger log = LoggerFactory.getLogger(AgentServiceImpl.class);

    private final AgentRepository agentRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AgentMapper agentMapper;

    @Override
    @Transactional(readOnly = true)
    public List<AgentDto> getAllAgents() {
        log.debug("Fetching all agents");
        return agentRepository.findAll()
                .stream()
                .map(agentMapper::agentToAgentDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public AgentDto getAgentById(Long id) {
        log.debug("Fetching agent by id: {}", id);
        return agentRepository.findById(id)
                .map(agentMapper::agentToAgentDto)
                .orElseThrow(() -> new ResourceNotFoundException("Agent not found with id: " + id));
    }

    @Override
    @Transactional(readOnly = true)
    public AgentDto getAgentByEmail(String email) {
        log.debug("Fetching agent by email: {}", email);
        return agentRepository.findAgentByUserEmail(email)
                .map(agentMapper::agentToAgentDto)
                .orElseThrow(() -> new ResourceNotFoundException("Agent not found with email: " + email));
    }

    @Override
    @Transactional(readOnly = true)
    public List<AgentDto> getAgentsByUserName(String name) {
        log.debug("Searching agents by name: {}", name);
        return agentRepository.findAgentsByUserFirstNameContainingOrUserLastNameContaining(name, name)
                .stream()
                .map(agentMapper::agentToAgentDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<AgentDto> getAgentsByDepartment(String department) {
        log.debug("Fetching agents by department: {}", department);
        return agentRepository.findAgentsByDepartment(department)
                .stream()
                .map(agentMapper::agentToAgentDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public AgentDto createAgent(AgentDto agentDto) {
        log.info("Creating new agent with email: {}", agentDto.getEmail());

        if (userRepository.findByEmail(agentDto.getEmail()).isPresent()) {
            log.warn("Email already exists: {}", agentDto.getEmail());
            throw new EmailAlreadyExistsException("Email already in use: " + agentDto.getEmail());
        }

        User user = User.builder()
                .firstName(agentDto.getFirstName())
                .lastName(agentDto.getLastName())
                .email(agentDto.getEmail())
                .password(passwordEncoder.encode(agentDto.getPassword()))
                .role(Role.AGENT)
                .build();

        Agent agent = Agent.builder()
                .department(agentDto.getDepartment())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .user(user)
                .build();

        Agent savedAgent = agentRepository.save(agent);
        log.info("Agent created with id: {} and user id: {}", savedAgent.getId(), savedAgent.getUser().getId());

        return agentMapper.agentToAgentDto(savedAgent);
    }

    @Override
    @Transactional
    public AgentDto updateAgent(Long id, AgentDto agentDto) {
        log.info("Updating agent with id: {}", id);

        Agent existingAgent = agentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Agent not found with id: " + id));

        User existingUser = existingAgent.getUser();

        existingAgent.setDepartment(agentDto.getDepartment());
        existingAgent.setUpdatedAt(LocalDateTime.now());
        existingUser.setFirstName(agentDto.getFirstName());
        existingUser.setLastName(agentDto.getLastName());

        if (!existingUser.getEmail().equals(agentDto.getEmail())) {
            if (userRepository.findByEmail(agentDto.getEmail()).isPresent()) {
                log.warn("Email update failed. Email already exists: {}", agentDto.getEmail());
                throw new EmailAlreadyExistsException("Email already in use: " + agentDto.getEmail());
            }
            existingUser.setEmail(agentDto.getEmail());
        }

        if (agentDto.getPassword() != null && !agentDto.getPassword().isEmpty()) {
            existingUser.setPassword(passwordEncoder.encode(agentDto.getPassword()));
            log.info("Agent password updated for user id: {}", existingUser.getId());
        }

        Agent updatedAgent = agentRepository.save(existingAgent);

        return agentMapper.agentToAgentDto(updatedAgent);
    }

    @Override
    @Transactional
    public void deleteAgentById(Long id) {
        log.info("Deleting agent with id: {}", id);
        if (!agentRepository.existsById(id)) {
            log.warn("Failed to delete. Agent not found with id: {}", id);
            throw new ResourceNotFoundException("Agent not found with id: " + id);
        }
        agentRepository.deleteById(id);
    }
}