package com.anil.crm.repositories;

import com.anil.crm.domain.Agent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface AgentRepository extends JpaRepository<Agent, Long> {


    @Query("SELECT a FROM Agent a WHERE a.user.email = :email")
    Optional<Agent> findAgentByUserEmail(String email);


    @Query("SELECT a FROM Agent a WHERE " +
            "LOWER(a.user.firstName) LIKE LOWER(CONCAT('%', :name, '%')) OR " +
            "LOWER(a.user.lastName) LIKE LOWER(CONCAT('%', :name, '%'))")
    List<Agent> findAgentsByUserFirstNameContainingOrUserLastNameContaining(String name, String name2);


    List<Agent> findAgentsByDepartment(String department);
}