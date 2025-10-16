package com.anil.crm.web.controllers;

import com.anil.crm.services.AgentService;
import com.anil.crm.web.models.AgentDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/agents")
@RequiredArgsConstructor
@Tag(name = "Agents", description = "CRUD operations for agents")
public class AgentsController {

    private final AgentService agentService;

    @GetMapping("/{id}")
    @Operation(summary = "Get agent by ID", description = "Retrieve a single agent by their unique ID")
    public ResponseEntity<AgentDto> getAgentById(
            @Parameter(description = "ID of the agent to retrieve", required = true)
            @PathVariable Long id) {

        return agentService.getAgentById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping
    @Operation(summary = "Get all agents", description = "Retrieve a list of all agents")
    public ResponseEntity<List<AgentDto>> getAllAgents() {
        return ResponseEntity.ok(agentService.getAllAgents());
    }

    @GetMapping("/search")
    @Operation(summary = "Search agents by name", description = "Retrieve agents whose names contain the given query string")
    public ResponseEntity<List<AgentDto>> getAgentsByName(
            @Parameter(description = "Name query string", required = true)
            @RequestParam String name) {

        return ResponseEntity.ok(agentService.getAgentsByName(name));
    }

    @GetMapping("/department/{department}")
    @Operation(summary = "Get agents by department", description = "Retrieve all agents working in a specific department")
    public ResponseEntity<List<AgentDto>> getAgentsByDepartment(
            @Parameter(description = "Department name", required = true)
            @PathVariable String department) {

        return ResponseEntity.ok(agentService.getAgentsByDepartment(department));
    }

    @PostMapping
    @Operation(summary = "Create a new agent", description = "Add a new agent to the system")
    public ResponseEntity<AgentDto> createAgent(
            @Parameter(description = "Agent details to create", required = true)
            @RequestBody AgentDto agentDto) {

        AgentDto savedAgent = agentService.saveAgent(agentDto);
        return ResponseEntity.ok(savedAgent);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update an existing agent", description = "Update an existing agent by ID")
    public ResponseEntity<AgentDto> updateAgent(
            @Parameter(description = "ID of the agent to update", required = true)
            @PathVariable Long id,
            @Parameter(description = "Updated agent details", required = true)
            @RequestBody AgentDto agentDto) {

        agentDto.setId(id);
        return agentService.updateAgent(agentDto)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete an agent", description = "Delete an agent by their ID")
    public ResponseEntity<Void> deleteAgent(
            @Parameter(description = "ID of the agent to delete", required = true)
            @PathVariable Long id) {

        agentService.deleteAgentById(id);
        return ResponseEntity.noContent().build();
    }
}
