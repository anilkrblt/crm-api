package com.anil.crm.web.models;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Null;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TicketCommentDto {

    @Null
    private Long id;

    @NotNull(message = "Ticket ID boş olamaz")
    private Long ticketId;

    @NotBlank(message = "Yazar tipi boş olamaz")
    private String authorType;

    private Long customerId;
    private Long agentId;

    @NotBlank(message = "Yorum boş olamaz")
    private String comment;

    private LocalDateTime createdAt;
}
