package com.anil.crm.web.controllers;

import com.anil.crm.domain.User;
import com.anil.crm.services.TicketCommentService;
import com.anil.crm.web.models.TicketCommentDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/ticket-comments")
@RequiredArgsConstructor
@Tag(name = "Ticket Comments", description = "Biletlere yorum ekleme ve listeleme işlemleri")
@SecurityRequirement(name = "bearerAuth")
public class TicketCommentsController {

    private final TicketCommentService ticketCommentService;



    @Operation(summary = "Bir bilete ait tüm yorumları getir",
            description = "Bir biletin (ticket) ID'sini kullanarak ona ait tüm yorumları listeler. " +
                    "CUSTOMER rolü sadece kendi biletinin yorumlarını görmelidir (Servis katmanında kontrol edilmeli).")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Yorumlar başarıyla bulundu",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = TicketCommentDto.class))),
            @ApiResponse(responseCode = "404", description = "Bilet (Ticket) bulunamadı", content = @Content),
            @ApiResponse(responseCode = "403", description = "Bu kaynağa erişim yetkiniz yok", content = @Content)
    })
    @GetMapping("/ticket/{ticketId}")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'AGENT', 'CUSTOMER')")
    public ResponseEntity<List<TicketCommentDto>> getCommentsByTicket(
            @Parameter(description = "Yorumları listelenecek biletin ID'si")
            @PathVariable Long ticketId) {

        return ResponseEntity.ok(ticketCommentService.getCommentsByTicketId(ticketId));
    }

    @Operation(summary = "Bir yazara (kullanıcıya) ait tüm yorumları getir",
            description = "Bir yazarın (User ID) sistemdeki tüm yorumlarını listeler. Sadece ADMIN ve AGENT erişebilir.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Yorumlar başarıyla bulundu"),
            @ApiResponse(responseCode = "403", description = "Bu kaynağa erişim yetkiniz yok", content = @Content)
    })
    @GetMapping("/author/{authorId}")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'AGENT')")
    public ResponseEntity<List<TicketCommentDto>> getCommentsByAuthor(
            @Parameter(description = "Yorumları listelenecek yazarın (User) ID'si")
            @PathVariable Long authorId) {

        return ResponseEntity.ok(ticketCommentService.getCommentsByAuthorId(authorId));
    }

    @Operation(summary = "Bir bilete yeni bir yorum ekle",
            description = "Bir bilete (ticket) yeni bir yorum ekler. Yorumun yazarı (author) " +
                    "otomatik olarak o an giriş yapmış (authenticated) kullanıcı olarak atanır.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Yorum başarıyla oluşturuldu"),
            @ApiResponse(responseCode = "400", description = "Geçersiz istek (örn: yorum metni boş)", content = @Content),
            @ApiResponse(responseCode = "404", description = "Bilet (Ticket) bulunamadı", content = @Content),
            @ApiResponse(responseCode = "403", description = "Bu kaynağa erişim yetkiniz yok", content = @Content)
    })
    @PostMapping
    @PreAuthorize("hasAnyAuthority('CUSTOMER', 'AGENT')")
    public ResponseEntity<TicketCommentDto> addComment(
            @Parameter(description = "Eklenecek yorumun verisi (ticketId ve comment metni)")
            @Valid @RequestBody TicketCommentDto commentDto,
            @Parameter(hidden = true)
            @AuthenticationPrincipal User author) {

        TicketCommentDto savedComment = ticketCommentService.addComment(commentDto, author);

        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(savedComment.getId())
                .toUri();

        return ResponseEntity.created(location).body(savedComment);
    }

    @Operation(summary = "Mevcut bir yorumu güncelle (Sadece Admin)",
            description = "Bir yorumun sadece metnini günceller. Sadece ADMIN rolü yapabilir.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Yorum başarıyla güncellendi"),
            @ApiResponse(responseCode = "400", description = "Geçersiz istek (örn: yorum metni boş)", content = @Content),
            @ApiResponse(responseCode = "404", description = "Yorum bulunamadı", content = @Content),
            @ApiResponse(responseCode = "403", description = "Bu kaynağa erişim yetkiniz yok", content = @Content)
    })
    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<TicketCommentDto> updateComment(
            @Parameter(description = "Güncellenecek yorumun ID'si")
            @PathVariable Long id,
            @Parameter(description = "Yorumun yeni metni")
            @Valid @RequestBody TicketCommentDto commentDto) {

        return ResponseEntity.ok(ticketCommentService.updateComment(id, commentDto));
    }

    @Operation(summary = "Bir yorumu sil (Sadece Admin)",
            description = "Bir yorumu ID'si ile kalıcı olarak siler. Sadece ADMIN rolü yapabilir.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Yorum başarıyla silindi", content = @Content),
            @ApiResponse(responseCode = "404", description = "Yorum bulunamadı", content = @Content),
            @ApiResponse(responseCode = "403", description = "Bu kaynağa erişim yetkiniz yok", content = @Content)
    })
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<Void> deleteComment(@PathVariable Long id) {
        ticketCommentService.deleteCommentById(id);
        return ResponseEntity.noContent().build();
    }
}