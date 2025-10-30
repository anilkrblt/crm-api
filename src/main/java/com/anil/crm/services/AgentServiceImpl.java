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
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AgentServiceImpl implements AgentService {

    private static final Logger log = LoggerFactory.getLogger(AgentServiceImpl.class);

    private final AgentRepository agentRepository;
    private final DepartmentRepository departmentRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AgentMapper agentMapper;
    private final TicketRepository ticketRepository;


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
    public List<AgentDto> findAgents(String name, String departmentName) {
        log.debug("Finding agents with name like '{}' and department like '{}'", name, departmentName);
        List<Agent> agents;

        boolean hasNameFilter = name != null && !name.trim().isEmpty();
        boolean hasDepartmentFilter = departmentName != null && !departmentName.trim().isEmpty();

        if (hasNameFilter && hasDepartmentFilter) {
            log.debug("Filtering by both department name containing and user name containing");
            agents = agentRepository.findByDepartmentNameContainingAndUserNameContaining(departmentName, name);
        } else if (hasNameFilter) {
            log.debug("Filtering by user name containing only");
            agents = agentRepository.findAgentsByUserFirstNameContainingOrUserLastNameContaining(name, name);
        } else if (hasDepartmentFilter) {
            log.debug("Filtering by department name containing only");
            agents = agentRepository.findAgentsByDepartmentNameContainingIgnoreCase(departmentName);
        } else {
            log.debug("No filters applied, fetching all agents");
            agents = agentRepository.findAll();
        }

        return agents.stream()
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

        Department department = departmentRepository.findByName(agentDto.getDepartmentName())
                .orElseThrow(() -> {
                    log.warn("Create agent failed. Department not found with name: {}", agentDto.getDepartmentName());
                    return new ResourceNotFoundException("Department not found with name: " + agentDto.getDepartmentName());
                });

        User user = User.builder()
                .firstName(agentDto.getFirstName())
                .lastName(agentDto.getLastName())
                .email(agentDto.getEmail())
                .password(passwordEncoder.encode(agentDto.getPassword()))
                .role(Role.AGENT)
                .build();

        Agent agent = Agent.builder()
                .department(department)
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

        if (agentDto.getDepartmentName() != null &&
                !agentDto.getDepartmentName().equalsIgnoreCase(existingAgent.getDepartment().getName())) {

            log.debug("Department change requested for agent {}: from '{}' to '{}'",
                    id, existingAgent.getDepartment().getName(), agentDto.getDepartmentName());

            Department newDepartment = departmentRepository.findByName(agentDto.getDepartmentName())
                    .orElseThrow(() -> {
                        log.warn("Update failed. Department not found with name: {}", agentDto.getDepartmentName());
                        return new ResourceNotFoundException("Department not found with name: " + agentDto.getDepartmentName());
                    });

            existingAgent.setDepartment(newDepartment);
            log.info("Agent {} department updated to {}", id, newDepartment.getName());
        }

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
        log.info("Agent profile updated successfully for id: {}", id);

        return agentMapper.agentToAgentDto(updatedAgent);
    }

    @Override
    @Transactional
    public void deleteAgentById(Long id) {
        log.info("Attempting to delete agent with id: {}", id);

        if (!agentRepository.existsById(id)) {
            log.warn("Failed to delete. Agent not found with id: {}", id);
            throw new ResourceNotFoundException("Agent not found with id: " + id);
        }

        if (ticketRepository.existsByAssignedAgentId(id)) {
            log.warn("Failed to delete agent {}. Agent has associated tickets.", id);
            throw new ResourceInUseException("Cannot delete agent. Agents are still assigned to it.");
        }

        agentRepository.deleteById(id);
        log.info("Agent deleted successfully with id: {}", id);
    }
}