package com.anil.crm.web.controllers;

import com.anil.crm.services.AgentService;
import com.anil.crm.web.models.AgentDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/agents")
@RequiredArgsConstructor
@Tag(name = "Agents", description = "Ajan (çalışan) kayıt ve profil işlemleri")
public class AgentsController {

    private final AgentService agentService;

    @Operation(summary = "Bir ajanı ID ile getir")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Ajan başarıyla bulundu",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = AgentDto.class))),
            @ApiResponse(responseCode = "404", description = "Belirtilen ID ile ajan bulunamadı", content = @Content),
            @ApiResponse(responseCode = "403", description = "Bu kaynağa erişim yetkiniz yok", content = @Content)
    })
    @GetMapping("/{id}")
    public ResponseEntity<AgentDto> getAgentById(@Parameter(description = "Ajan ID'si") @PathVariable Long id) {

        return ResponseEntity.ok(agentService.getAgentById(id));
    }



    @GetMapping
    @Operation(summary = "Ajanları listele veya filtrele",
            description = "Sistemdeki ajanları listeler. Opsiyonel olarak 'name' (ad/soyad içerir) veya 'department' (tam departman adı) " +
                    "query parametreleri ile filtreleme yapılabilir. Her iki parametre de verilirse, belirtilen departmandaki isimle eşleşenler getirilir.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Ajanlar başarıyla listelendi/filtrelendi"),
            @ApiResponse(responseCode = "403", description = "Yetkisiz erişim", content = @Content)
    })
    public ResponseEntity<List<AgentDto>> findAgents(
            @Parameter(description = "Filtrelemek için ad veya soyad parçası (opsiyonel)")
            @RequestParam(required = false) String name,
            @Parameter(description = "Filtrelemek için tam departman adı (opsiyonel)")
            @RequestParam(required = false) String department
    ) {
        List<AgentDto> agents = agentService.findAgents(name, department);
        return ResponseEntity.ok(agents);
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
    public ResponseEntity<Void> deleteAgent(@Parameter(description = "Silinecek ajan ID'si") @PathVariable Long id) {

        agentService.deleteAgentById(id);
        return ResponseEntity.noContent().build();
    }
}