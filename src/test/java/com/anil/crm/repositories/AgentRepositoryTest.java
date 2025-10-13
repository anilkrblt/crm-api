package com.anil.crm.repositories;


import com.anil.crm.entities.Agent;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

//@DataJpaTest
@SpringBootTest
class AgentRepositoryTest {

    @Autowired
    private  AgentRepository agentRepository;


    @Test
    void agentRepositoryGetAllTest() {
        var agents = agentRepository.findAll();
        agents.forEach(System.out::println);
    }

    @Test
    void agentRepositoryAddAgentTest() {
        Agent agent1 = Agent.builder()
                .fullName("Ayşe Demir")
                .email("ayse.demir@example.com")
                .passwordHash("hashed_password")
                .department("Teknik")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        Agent agent2 = Agent.builder()
                .fullName("Mehmet Kaya")
                .email("mehmet.kaya@example.com")
                .passwordHash("hashed_password")
                .department("Satış")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        agentRepository.save(agent1);
        agentRepository.save(agent2);

        List<Agent> agents = agentRepository.findAll();
        assertThat(agents).hasSize(2);
        agents.forEach(System.out::println);
    }
}