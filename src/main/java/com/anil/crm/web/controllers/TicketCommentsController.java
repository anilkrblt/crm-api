package com.anil.crm.web.controllers;

import com.anil.crm.services.TicketCommentService;
import com.anil.crm.web.models.TicketCommentDto;
import lombok.RequiredArgsConstructor;
import org.springframework.data.crossstore.ChangeSetPersister;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/ticket-comments")
@RequiredArgsConstructor
public class TicketCommentsController {

    private final TicketCommentService ticketCommentService;

    @GetMapping("/{id}")
    public ResponseEntity<TicketCommentDto> getCommentById(@PathVariable Long id) {
        TicketCommentDto comment = ticketCommentService.getCommentById(id).orElse(null);
        return ResponseEntity.ok(comment);
    }

    @GetMapping("/ticket/{ticketId}")
    public ResponseEntity<List<TicketCommentDto>> getCommentsByTicket(@PathVariable Long ticketId) {
        return ResponseEntity.ok(ticketCommentService.getCommentsByTicketId(ticketId));
    }

    @GetMapping("/agent/{agentId}")
    public ResponseEntity<List<TicketCommentDto>> getCommentsByAgent(@PathVariable Long agentId) {
        return ResponseEntity.ok(ticketCommentService.getCommentsByAgentId(agentId));
    }

    @GetMapping("/customer/{customerId}")
    public ResponseEntity<List<TicketCommentDto>> getCommentsByCustomer(@PathVariable Long customerId) {
        return ResponseEntity.ok(ticketCommentService.getCommentsByCustomerId(customerId));
    }

    @PostMapping
    public ResponseEntity<TicketCommentDto> addComment(@RequestBody TicketCommentDto commentDto) {
        return ResponseEntity.ok(ticketCommentService.addComment(commentDto));
    }

    @PutMapping("/{id}")
    public ResponseEntity<TicketCommentDto> updateComment(
            @PathVariable Long id,
            @RequestBody TicketCommentDto commentDto) {
        commentDto.setId(id);
        return ResponseEntity.ok(ticketCommentService.updateComment(commentDto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteComment(@PathVariable Long id) {
        ticketCommentService.deleteCommentById(id);
        return ResponseEntity.noContent().build();
    }
}
