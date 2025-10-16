package com.anil.crm.repositories;

import com.anil.crm.domain.Agent;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AgentRepository extends JpaRepository<Agent, Long> {
    List<Agent> findByFullNameContainingIgnoreCase(String name);

    List<Agent> findAgentsByDepartment(String department);
}
