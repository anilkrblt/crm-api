package com.anil.crm.web.controllers;

import com.anil.crm.exceptions.ResourceNotFoundException; // Import your exception
import com.anil.crm.services.AgentService;
import com.anil.crm.web.models.AgentDto;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
//import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.*; // BDDMockito style
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AgentsController.class) // Test only the AgentsController layer
@DisplayName("Agents Controller Unit Tests")
class AgentsControllerTest {

    @Autowired
    MockMvc mockMvc; // To perform HTTP requests

    @MockitoBean
    AgentService agentService; // Mock the dependency

    @Autowired
    ObjectMapper objectMapper; // To convert objects to/from JSON

    AgentDto validAgentDto1;
    AgentDto validAgentDto2;

    @BeforeEach
    void setUp() {
        // Create sample DTOs used in multiple tests
        validAgentDto1 = AgentDto.builder()
                .id(1L)
                .firstName("Ahmet")
                .lastName("Yılmaz")
                .email("ahmet.yilmaz@company.com")
                .department("Teknik Destek")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        validAgentDto2 = AgentDto.builder()
                .id(2L)
                .firstName("Ayşe")
                .lastName("Demir")
                .email("ayse.demir@company.com")
                .department("Müşteri Hizmetleri")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    @Test
    @DisplayName("Get Agent By ID - Success")
    void getAgentById_shouldReturnAgentDto_whenFound() throws Exception {
        // Given (Setup Mock)
        given(agentService.getAgentById(validAgentDto1.getId())).willReturn(validAgentDto1);

        // When & Then (Perform Request & Assert Response)
        mockMvc.perform(get("/api/agents/{id}", validAgentDto1.getId())
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id", is(validAgentDto1.getId().intValue())))
                .andExpect(jsonPath("$.firstName", is(validAgentDto1.getFirstName())));
        // Add more jsonPath assertions as needed

        // Verify that the service method was called exactly once
        then(agentService).should(times(1)).getAgentById(validAgentDto1.getId());
    }

    @Test
    @DisplayName("Get Agent By ID - Not Found")
    void getAgentById_shouldReturnNotFound_whenNotFound() throws Exception {
        // Given
        Long nonExistentId = 99L;
        given(agentService.getAgentById(nonExistentId)).willThrow(new ResourceNotFoundException("Agent not found"));

        // When & Then
        mockMvc.perform(get("/api/agents/{id}", nonExistentId)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());

        then(agentService).should(times(1)).getAgentById(nonExistentId);
    }

    @Test
    @DisplayName("Get All Agents - Success")
    void getAllAgents_shouldReturnListOfAgents() throws Exception {
        // Given
        List<AgentDto> agentList = Arrays.asList(validAgentDto1, validAgentDto2);
        given(agentService.getAllAgents()).willReturn(agentList);

        // When & Then
        mockMvc.perform(get("/api/agents")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(2))) // Check if the list size is 2
                .andExpect(jsonPath("$[0].id", is(validAgentDto1.getId().intValue())))
                .andExpect(jsonPath("$[1].id", is(validAgentDto2.getId().intValue())));

        then(agentService).should(times(1)).getAllAgents();
    }

    @Test
    @DisplayName("Search Agents By Name - Success")
    void searchAgentsByName_shouldReturnMatchingAgents() throws Exception {
        // Given
        String searchName = "Ahmet";
        List<AgentDto> resultList = List.of(validAgentDto1);
        given(agentService.getAgentsByUserName(searchName)).willReturn(resultList);

        // When & Then
        mockMvc.perform(get("/api/agents/search")
                        .param("name", searchName) // Add query parameter
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].firstName", is("Ahmet")));

        then(agentService).should(times(1)).getAgentsByUserName(searchName);
    }

    @Test
    @DisplayName("Get Agents By Department - Success")
    void getAgentsByDepartment_shouldReturnMatchingAgents() throws Exception {
        // Given
        String department = "Teknik Destek";
        List<AgentDto> resultList = List.of(validAgentDto1);
        given(agentService.getAgentsByDepartment(department)).willReturn(resultList);

        // When & Then
        mockMvc.perform(get("/api/agents/department/{department}", department)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].department", is(department)));

        then(agentService).should(times(1)).getAgentsByDepartment(department);
    }

    @Test
    @DisplayName("Create Agent - Success")
    void createAgent_shouldReturnCreatedAgent_whenValid() throws Exception {
        // Given
        AgentDto agentToCreate = AgentDto.builder() // DTO without ID
                .firstName("Zeynep")
                .lastName("Çelik")
                .email("zeynep.celik@company.com")
                .department("Pazarlama")
                .password("YeniSifre123.")
                .build();

        AgentDto savedAgent = AgentDto.builder() // DTO returned by service (with ID)
                .id(3L)
                .firstName("Zeynep")
                .lastName("Çelik")
                .email("zeynep.celik@company.com")
                .department("Pazarlama")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        given(agentService.createAgent(any(AgentDto.class))).willReturn(savedAgent);

        // When & Then
        mockMvc.perform(post("/api/agents")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(agentToCreate))) // Send DTO as JSON
                .andExpect(status().isCreated()) // Expect 201 Created
                .andExpect(header().exists("Location")) // Expect Location header
                .andExpect(jsonPath("$.id", is(savedAgent.getId().intValue())))
                .andExpect(jsonPath("$.email", is(savedAgent.getEmail())));

        then(agentService).should(times(1)).createAgent(any(AgentDto.class));
    }

    @Test
    @DisplayName("Create Agent - Validation Failure")
    void createAgent_shouldReturnBadRequest_whenInvalid() throws Exception {
        // Given
        AgentDto invalidAgent = AgentDto.builder() // Missing required fields
                .email("invalid-email") // Invalid email format
                .build();

        // When & Then
        mockMvc.perform(post("/api/agents")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidAgent)))
                .andExpect(status().isBadRequest()); // Expect 400 Bad Request

        // Service method should NOT be called if validation fails
        then(agentService).should(never()).createAgent(any(AgentDto.class));
    }


    @Test
    @DisplayName("Update Agent - Success")
    void updateAgent_shouldReturnUpdatedAgent_whenValid() throws Exception {
        // Given
        Long agentId = validAgentDto1.getId();
        AgentDto agentUpdates = AgentDto.builder()
                .firstName("Ahmet")
                .lastName("Yılmaz")
                .email("ahmet.yilmaz.new@company.com") // Email updated
                .department("Kıdemli Teknik Destek") // Department updated
                .build();

        AgentDto updatedAgentFromService = AgentDto.builder()
                .id(agentId)
                .firstName("Ahmet")
                .lastName("Yılmaz")
                .email("ahmet.yilmaz.new@company.com")
                .department("Kıdemli Teknik Destek")
                .createdAt(validAgentDto1.getCreatedAt()) // Should remain the same
                .updatedAt(LocalDateTime.now()) // Should be updated
                .build();

        given(agentService.updateAgent(eq(agentId), any(AgentDto.class))).willReturn(updatedAgentFromService);

        // When & Then
        mockMvc.perform(put("/api/agents/{id}", agentId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(agentUpdates)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(agentId.intValue())))
                .andExpect(jsonPath("$.email", is(updatedAgentFromService.getEmail())))
                .andExpect(jsonPath("$.department", is(updatedAgentFromService.getDepartment())));

        then(agentService).should(times(1)).updateAgent(eq(agentId), any(AgentDto.class));
    }

    @Test
    @DisplayName("Update Agent - Not Found")
    void updateAgent_shouldReturnNotFound_whenNotFound() throws Exception {
        // Given
        Long nonExistentId = 99L;
        AgentDto agentUpdates = AgentDto.builder() /* valid update data */ .build();
        given(agentService.updateAgent(eq(nonExistentId), any(AgentDto.class)))
                .willThrow(new ResourceNotFoundException("Agent not found"));

        // When & Then
        mockMvc.perform(put("/api/agents/{id}", nonExistentId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(agentUpdates)))
                .andExpect(status().isNotFound());

        then(agentService).should(times(1)).updateAgent(eq(nonExistentId), any(AgentDto.class));
    }

    @Test
    @DisplayName("Delete Agent - Success")
    void deleteAgent_shouldReturnNoContent_whenSuccess() throws Exception {
        // Given
        Long agentIdToDelete = 1L;
        // Mock void method: do nothing when called
        willDoNothing().given(agentService).deleteAgentById(agentIdToDelete);

        // When & Then
        mockMvc.perform(delete("/api/agents/{id}", agentIdToDelete)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent()); // Expect 204 No Content

        then(agentService).should(times(1)).deleteAgentById(agentIdToDelete);
    }

    @Test
    @DisplayName("Delete Agent - Not Found")
    void deleteAgent_shouldReturnNotFound_whenNotFound() throws Exception {
        // Given
        Long nonExistentId = 99L;
        willThrow(new ResourceNotFoundException("Agent not found"))
                .given(agentService).deleteAgentById(nonExistentId);

        // When & Then
        mockMvc.perform(delete("/api/agents/{id}", nonExistentId)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());

        then(agentService).should(times(1)).deleteAgentById(nonExistentId);
    }
}