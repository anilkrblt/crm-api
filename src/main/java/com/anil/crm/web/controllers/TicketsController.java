package com.anil.crm.web.controllers;

import com.anil.crm.services.TicketService;
import com.anil.crm.web.models.TicketDto;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tickets")
@RequiredArgsConstructor
public class TicketsController {

    private final TicketService ticketService;

    @GetMapping("/{id}")
    public ResponseEntity<TicketDto> getTicketById(@PathVariable Long id) {
        return ticketService.getTicketById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping
    public ResponseEntity<List<TicketDto>> getAllTickets() {
        return ResponseEntity.ok(ticketService.getAllTickets());
    }

    @GetMapping("/customer/{customerId}")
    public ResponseEntity<List<TicketDto>> getTicketsByCustomer(@PathVariable Long customerId) {
        return ResponseEntity.ok(ticketService.getTicketsByCustomerId(customerId));
    }

    @GetMapping("/agent/{agentId}")
    public ResponseEntity<List<TicketDto>> getTicketsByAgent(@PathVariable Long agentId) {
        return ResponseEntity.ok(ticketService.getTicketsByAgentId(agentId));
    }

    @GetMapping("/status")
    public ResponseEntity<List<TicketDto>> getTicketsByStatus(@RequestParam String status) {
        return ResponseEntity.ok(ticketService.getTicketsByStatus(status));
    }

    @GetMapping("/priority")
    public ResponseEntity<List<TicketDto>> getTicketsByPriority(@RequestParam String priority) {
        return ResponseEntity.ok(ticketService.getTicketsByPriority(priority));
    }

    @PostMapping
    public ResponseEntity<TicketDto> createTicket(@RequestBody TicketDto ticketDto) {
        return ResponseEntity.ok(ticketService.createTicket(ticketDto));
    }

    @PutMapping("/{id}")
    public ResponseEntity<TicketDto> updateTicket(@PathVariable Long id, @RequestBody TicketDto ticketDto) {
        ticketDto.setId(id);
        return ResponseEntity.ok(ticketService.updateTicket(ticketDto));
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<TicketDto> updateTicketStatus(@PathVariable Long id, @RequestParam String status) {
        return ResponseEntity.ok(ticketService.updateTicketStatus(id, status));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTicket(@PathVariable Long id) {
        ticketService.deleteTicketById(id);
        return ResponseEntity.noContent().build();
    }
}
