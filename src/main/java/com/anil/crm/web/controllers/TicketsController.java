package com.anil.crm.web.controllers;

import com.anil.crm.domain.TicketPriority;
import com.anil.crm.domain.TicketStatus;
import com.anil.crm.services.TicketService;
import com.anil.crm.web.models.TicketDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

@Tag(name = "Tickets API", description = "Bilet (Ticket) yönetimi için endpointler")
@RestController
@RequestMapping("/api/tickets")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
public class TicketsController {

    private final TicketService ticketService;

    @Operation(summary = "Bir bileti ID ile getir")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Bilet bulundu"),
            @ApiResponse(responseCode = "404", description = "Bilet bulunamadı", content = @Content),
            @ApiResponse(responseCode = "403", description = "Yetkisiz erişim", content = @Content)
    })
    @GetMapping("/{id}")
    public ResponseEntity<TicketDto> getTicketById(
            @Parameter(description = "Aranan biletin ID'si")
            @PathVariable Long id) {
        return ResponseEntity.ok(ticketService.getTicketById(id));
    }

    @Operation(summary = "Tüm biletleri listele (Sadece Admin/Ajan)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Biletler listelendi"),
            @ApiResponse(responseCode = "403", description = "Yetkisiz erişim", content = @Content)
    })
    @GetMapping
    public ResponseEntity<List<TicketDto>> getAllTickets() {
        return ResponseEntity.ok(ticketService.getAllTickets());
    }

    @Operation(summary = "Bir müşteriye ait biletleri getir (Sadece Admin/Ajan)")
    @GetMapping("/customer/{customerId}")
    public ResponseEntity<List<TicketDto>> getTicketsByCustomer(
            @Parameter(description = "Müşteri ID'si")
            @PathVariable Long customerId) {
        return ResponseEntity.ok(ticketService.getTicketsByCustomerId(customerId));
    }

    @Operation(summary = "Bir ajana atanmış biletleri getir (Sadece Admin/Ajan)",
            description = "Belirli bir ajana atanmış (assigned) biletleri listeler.")
    @GetMapping("/assigned-agent/{agentId}")
    public ResponseEntity<List<TicketDto>> getTicketsByAssignedAgent(
                                                                      @Parameter(description = "Atanmış ajanın ID'si")
                                                                      @PathVariable Long agentId) {
        return ResponseEntity.ok(ticketService.getTicketsByAssignedAgentId(agentId));
    }


    @Operation(summary = "Bir departmana ait biletleri getir (Sadece Admin/Ajan)", description = "Belirli bir departmana ait tüm biletleri listeler.")
    @GetMapping("/department/{departmentId}")
    public ResponseEntity<List<TicketDto>> getTicketsByDepartment(
            @Parameter(description = "Departman ID'si")
            @PathVariable Long departmentId) {
        return ResponseEntity.ok(ticketService.getTicketsByDepartmentId(departmentId));
    }


    @Operation(summary = "Duruma (status) göre biletleri getir (Sadece Admin/Ajan)")
    @GetMapping("/status")
    public ResponseEntity<List<TicketDto>> getTicketsByStatus(
            @Parameter(description = "Durum (örn: OPEN, CLOSED)")
            @RequestParam TicketStatus status) {
        return ResponseEntity.ok(ticketService.getTicketsByStatus(status));
    }

    @Operation(summary = "Önceliğe (priority) göre biletleri getir (Sadece Admin/Ajan)")
    @GetMapping("/priority")
    public ResponseEntity<List<TicketDto>> getTicketsByPriority(
            @Parameter(description = "Öncelik (örn: HIGH, LOW)")
            @RequestParam TicketPriority priority) {
        return ResponseEntity.ok(ticketService.getTicketsByPriority(priority));
    }

    @Operation(summary = "Yeni bir bilet oluştur (Sadece Müşteri)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Bilet başarıyla oluşturuldu"),
            @ApiResponse(responseCode = "400", description = "Geçersiz bilet verisi", content = @Content),
            @ApiResponse(responseCode = "403", description = "Sadece müşteriler bilet oluşturabilir", content = @Content)
    })
    @PostMapping
    public ResponseEntity<TicketDto> createTicket(
            @Parameter(description = "Oluşturulacak bilet verisi (customer.id ve department.id/name zorunlu)")
            @Valid @RequestBody TicketDto ticketDto) {

        TicketDto createdTicket = ticketService.createTicket(ticketDto);

        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(createdTicket.getId())
                .toUri();

        return ResponseEntity.created(location).body(createdTicket);
    }

    @Operation(summary = "Mevcut bir bileti güncelle (Sadece Admin/Ajan)",
            description = "Biletin konu, açıklama, durum, öncelik gibi temel bilgilerini günceller. " +
                    "Departman veya atanan ajan değişikliği genellikle ayrı endpoint'lerle yapılır.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Bilet başarıyla güncellendi"),
            @ApiResponse(responseCode = "400", description = "Geçersiz bilet verisi", content = @Content),
            @ApiResponse(responseCode = "404", description = "Bilet bulunamadı", content = @Content),
            @ApiResponse(responseCode = "403", description = "Yetkisiz erişim", content = @Content)
    })
    @PutMapping("/{id}")
    public ResponseEntity<TicketDto> updateTicket(
            @Parameter(description = "Güncellenecek biletin ID'si")
            @PathVariable Long id,
            @Parameter(description = "Güncel bilet verisi (departman/ajan güncellenmez)")
            @Valid @RequestBody TicketDto ticketDto) {

        TicketDto updatedTicket = ticketService.updateTicket(id, ticketDto);
        return ResponseEntity.ok(updatedTicket);
    }

    @Operation(summary = "Bir biletin durumunu (status) güncelle (Sadece Admin/Ajan)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Durum başarıyla güncellendi"),
            @ApiResponse(responseCode = "404", description = "Bilet bulunamadı", content = @Content),
            @ApiResponse(responseCode = "403", description = "Yetkisiz erişim", content = @Content)
    })
    @PatchMapping("/{id}/status")
    public ResponseEntity<TicketDto> updateTicketStatus(
            @Parameter(description = "Bilet ID'si")
            @PathVariable Long id,
            @Parameter(description = "Yeni durum (örn: IN_PROGRESS)")
            @RequestParam TicketStatus status) {

        return ResponseEntity.ok(ticketService.updateTicketStatus(id, status));
    }

    @Operation(summary = "Bir bileti ID ile sil (Sadece Admin/Ajan)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Bilet başarıyla silindi", content = @Content),
            @ApiResponse(responseCode = "404", description = "Bilet bulunamadı", content = @Content),
            @ApiResponse(responseCode = "403", description = "Yetkisiz erişim", content = @Content)
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTicket(
            @Parameter(description = "Silinecek biletin ID'si")
            @PathVariable Long id) {

        ticketService.deleteTicketById(id);
        return ResponseEntity.noContent().build();
    }
}