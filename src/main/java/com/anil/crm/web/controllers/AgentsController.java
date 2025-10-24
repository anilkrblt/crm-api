package com.anil.crm.web.controllers;

import com.anil.crm.services.AgentService;
import com.anil.crm.web.models.AgentDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/agents")
@RequiredArgsConstructor
@Tag(name = "Agents", description = "Ajan (çalışan) kayıt ve profil işlemleri")
@PreAuthorize("hasAuthority('ADMIN')")
public class AgentsController {

    private final AgentService agentService;

    @GetMapping("/{id}")
    @Operation(summary = "Bir ajanı ID ile getir")
    public ResponseEntity<AgentDto> getAgentById(
            @Parameter(description = "Ajan ID'si") @PathVariable Long id) {

        return ResponseEntity.ok(agentService.getAgentById(id));
    }

    @GetMapping
    @Operation(summary = "Tüm ajanları listele")
    public ResponseEntity<List<AgentDto>> getAllAgents() {
        return ResponseEntity.ok(agentService.getAllAgents());
    }

    @GetMapping("/search")
    @Operation(summary = "Ajanları isme göre ara")
    public ResponseEntity<List<AgentDto>> searchAgentsByName(
            @Parameter(description = "Aranacak isim veya soyisim") @RequestParam String name) {

        return ResponseEntity.ok(agentService.getAgentsByUserName(name));
    }

    @GetMapping("/department/{department}")
    @Operation(summary = "Departmana göre ajanları getir")
    public ResponseEntity<List<AgentDto>> getAgentsByDepartment(
            @Parameter(description = "Departman adı") @PathVariable String department) {

        return ResponseEntity.ok(agentService.getAgentsByDepartment(department));
    }

    @PostMapping
    @Operation(summary = "Yeni bir ajan kaydı oluştur (Registration)")
    public ResponseEntity<AgentDto> createAgent(
            @Validated(AgentDto.CreateValidation.class)
            @RequestBody AgentDto agentDto) {

        AgentDto savedAgent = agentService.createAgent(agentDto);

        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(savedAgent.getId())
                .toUri();
        return ResponseEntity.created(location).body(savedAgent);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Mevcut bir ajanı güncelle")
    public ResponseEntity<AgentDto> updateAgent(
            @Parameter(description = "Güncellenecek ajan ID'si") @PathVariable Long id,
            @Valid @RequestBody AgentDto agentDto) {

        AgentDto updatedAgent = agentService.updateAgent(id, agentDto);
        return ResponseEntity.ok(updatedAgent);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Bir ajanı sil")
    public ResponseEntity<Void> deleteAgent(
            @Parameter(description = "Silinecek ajan ID'si") @PathVariable Long id) {

        agentService.deleteAgentById(id);
        return ResponseEntity.noContent().build();
    }
}