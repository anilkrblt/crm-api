package com.anil.crm.web.controllers;

import com.anil.crm.exceptions.ResourceNotFoundException;
import com.anil.crm.services.AgentService;
import com.anil.crm.services.JwtService;
import com.anil.crm.services.UserDetailsServiceImpl;
import com.anil.crm.web.models.AgentDto;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration;
import java.time.LocalDateTime;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = AgentsController.class,
        excludeAutoConfiguration = {UserDetailsServiceAutoConfiguration.class})
class AgentsControllerTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @MockitoBean
    AgentService agentService;
    @MockitoBean
    JwtService jwtService;
    @MockitoBean
    UserDetailsServiceImpl userDetailsService;

    AgentDto testAgentDto;

    @BeforeEach
    void setUp() {
        testAgentDto = AgentDto.builder()
                .id(1L)
                .firstName("Test")
                .lastName("Agent")
                .email("test@agent.com")
                .departmentName("Destek")
                .build();
    }

    @Test
    @WithMockUser
    void getAgentById() throws Exception {
        given(agentService.getAgentById(1L)).willReturn(testAgentDto);

        mockMvc.perform(get("/api/agents/1")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk()) // HTTP 200 OK mi?
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.email", is("test@agent.com")));

        then(agentService).should(times(1)).getAgentById(1L);
    }

    @Test
    @DisplayName("GET /api/agents/{id} - Bulunamadı (404 Not Found)")
    @WithMockUser
    void getAgentById_NotFound() throws Exception {
        given(agentService.getAgentById(99L)).willThrow(new ResourceNotFoundException("Bulunamadı"));

        mockMvc.perform(get("/api/agents/99")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser
    void findAgents_listAll() throws Exception {
        given(agentService.findAgents(null, null)).willReturn(List.of(testAgentDto));

        mockMvc.perform(get("/api/agents")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id", is(1)));

        then(agentService).should().findAgents(null, null);
    }

    @Test
    @DisplayName("GET /api/agents?name=Test - İsme Göre Filtrele")
    @WithMockUser
    void findAgents_filterByName() throws Exception {
        given(agentService.findAgents("Test", null)).willReturn(List.of(testAgentDto));

        mockMvc.perform(get("/api/agents")
                        .param("name", "Test")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].firstName", is("Test")));

        then(agentService).should().findAgents("Test", null);
    }

    @Test
    @DisplayName("GET /api/agents?department=Destek - Departmana Göre Filtrele")
    @WithMockUser
    void findAgents_filterByDepartment() throws Exception {
        given(agentService.findAgents(null, "Destek")).willReturn(List.of(testAgentDto));

        mockMvc.perform(get("/api/agents")
                        .param("department", "Destek")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].departmentName", is("Destek")));

        then(agentService).should().findAgents(null, "Destek");
    }

    @Test
    @WithMockUser(authorities = "ADMIN")
    void createAgent() throws Exception {
        AgentDto savedDto = AgentDto.builder()
                .id(2L)
                .email("yeni@agent.com")
                .firstName("Yeni")
                .lastName("Ajan")
                .departmentName("Teknik Destek")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        given(agentService.createAgent(any(AgentDto.class))).willReturn(savedDto);

        String requestBodyJson = """
        {
          "email": "yeni@agent.com",
          "firstName": "Yeni",
          "lastName": "Ajan",
          "password": "GuvenliSifre123!",
          "departmentName": "Teknik Destek"
        }
        """;

        mockMvc.perform(post("/api/agents")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(requestBodyJson))
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"))
                .andExpect(jsonPath("$.id", is(2)))
                .andExpect(jsonPath("$.email", is("yeni@agent.com")));

        then(agentService).should(times(1)).createAgent(any(AgentDto.class));
    }


    @Test
    @WithMockUser(authorities = "ADMIN")
    void updateAgent() throws Exception {
        Long agentIdToUpdate = 1L;

        AgentDto dtoToUpdate = AgentDto.builder()
                .firstName("Güncel")
                .lastName("Ajan")
                .email("guncel@agent.com")
                .departmentName("Satış")
                .build();

        AgentDto updatedDto = AgentDto.builder()
                .id(agentIdToUpdate)
                .firstName("Güncel")
                .lastName("Ajan")
                .email("guncel@agent.com")
                .departmentName("Satış")
                .updatedAt(LocalDateTime.now())
                .build();

        given(agentService.updateAgent(eq(agentIdToUpdate), any(AgentDto.class))).willReturn(updatedDto);

        mockMvc.perform(put("/api/agents/{id}", agentIdToUpdate)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dtoToUpdate)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(agentIdToUpdate.intValue())))
                .andExpect(jsonPath("$.email", is("guncel@agent.com")))
                .andExpect(jsonPath("$.firstName", is("Güncel")));

        then(agentService).should(times(1)).updateAgent(eq(agentIdToUpdate), any(AgentDto.class));
    }


    @Test
    @WithMockUser(authorities = "ADMIN")
    void deleteAgent() throws Exception {
        willDoNothing().given(agentService).deleteAgentById(1L);

        mockMvc.perform(delete("/api/agents/1")
                        .with(csrf()))
                .andExpect(status().isNoContent());

        then(agentService).should().deleteAgentById(1L);
    }
}
