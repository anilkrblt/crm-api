package com.anil.crm.web.controllers;

import com.anil.crm.services.AgentService;
import com.anil.crm.web.models.AgentDto;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doNothing;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AgentsController.class)
class AgentsControllerIntegrationTest {

    @Autowired
    MockMvc mockMvc;

    @MockitoBean
    AgentService agentService;

    @Autowired
    ObjectMapper objectMapper;

    AgentDto validAgent;

    @BeforeEach
    void setUp() {
        validAgent = AgentDto.builder()
                .id(1L)
                .fullName("Agent1")
                .passwordHash("hashpassword")
                .email("agent1@gmail.com")
                .department("IT Solutions")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    @Test
    void getAgentById() throws Exception {
        given(agentService.getAgentById(anyLong())).willReturn(Optional.of(validAgent));

        mockMvc.perform(get("/api/agents/1")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.fullName").value("Agent1"));
    }

    @Test
    void getAllAgents() throws Exception {
        List<AgentDto> agents = Arrays.asList(validAgent);
        given(agentService.getAllAgents()).willReturn(agents);

        mockMvc.perform(get("/api/agents")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].email").value("agent1@gmail.com"));
    }

    @Test
    void getAgentsByName() throws Exception {
        List<AgentDto> agents = Arrays.asList(validAgent);
        given(agentService.getAgentsByName("Agent1")).willReturn(agents);

        mockMvc.perform(get("/api/agents/search")
                        .param("name", "Agent1")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].fullName").value("Agent1"));
    }

    @Test
    void getAgentsByDepartment() throws Exception {
        List<AgentDto> agents = Arrays.asList(validAgent);
        given(agentService.getAgentsByDepartment("IT Solutions")).willReturn(agents);

        mockMvc.perform(get("/api/agents/department/IT Solutions")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].department").value("IT Solutions"));
    }

    @Test
    void createAgent() throws Exception {
        given(agentService.saveAgent(any(AgentDto.class))).willReturn(validAgent);

        mockMvc.perform(post("/api/agents")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validAgent)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("agent1@gmail.com"));
    }

    @Test
    void updateAgent() throws Exception {
        given(agentService.updateAgent(any(AgentDto.class))).willReturn(Optional.of(validAgent));

        mockMvc.perform(put("/api/agents/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validAgent)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L));
    }

    @Test
    void deleteAgent() throws Exception {
        doNothing().when(agentService).deleteAgentById(anyLong());

        mockMvc.perform(delete("/api/agents/1"))
                .andExpect(status().isNoContent());
    }
}
